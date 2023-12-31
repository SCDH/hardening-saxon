package de.wwu.scdh.saxon.harden;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;


/**
 * A filter that restricts access to the file system to a specific
 * paths given by configuration. Requests to URI schemes other than
 * <code>file</code> will pass the check. URIs without a specified
 * scheme will be treated as in the file scheme.
 *
 */
public class FilesystemFilter {

    /**
     * Only paths under this path will be accessible through this resource resolver.
     */
    private final String allowedLocations[];

    public static final String SEPARATOR = ",";

    public static final String PROPERTY = FilesystemFilter.class.getName();

    public static final String ENVIRON = "SAXON_ALLOWED_PATHS";

    private static final Logger LOG = new StandardLogger();

    /**
     * The standard constructor sets no allowed locations at all.
     */
    public FilesystemFilter() {
	String empty[] = {};
	this.allowedLocations = empty;
	notifyEmpty();
    }

    /**
     * Make a new {@link FilesystemFilter} from an array of file
     * system paths. The paths should follow the OS-specific form,
     * e.g. <code>c:\\users\\<P> on Windows or
     * <code>~/projects/xsl</code> on *nix. Paths are converted to
     * healty URIs internally. Relative paths are converted to
     * absolute paths.
     *
     * <code>~</code> can be used to point to the user's home directory.
     *
     * @param allowedLocations  locations (paths) on the file system allowed for read and write access
     */
    public FilesystemFilter (String[] allowedLocations) throws FilesystemFilterException {

	if (allowedLocations == null) {
	    throw new FilesystemFilterException("allowedLocations may not be null");
	} else {
	    this.allowedLocations = new String[allowedLocations.length];
	}

	if (allowedLocations.length == 0) {
	    notifyEmpty();
	}

	for (int i = 0; i < allowedLocations.length; i++) {

	    String location = allowedLocations[i];

	    // check preconditions
	    if (location == null) {
		throw new FilesystemFilterException("configuration error: allowed location may not be null.");
	    } else if (location.startsWith("file:")) {
		throw new FilesystemFilterException("configuration error: allowed location may not start with 'file:'");
	    } else if (location.isEmpty()) {
		throw new FilesystemFilterException("configuration error: allowed location may not be the empty string");
	    }

	    if (location.startsWith("~")) {
		location = System.getProperty("user.home") + location.substring(1);
	    }

	    // make absolute
	    File path = new File(location).getAbsoluteFile();
	    // make URI and normalize
	    URI uri = path.toURI().normalize();
	    // store to field
	    if (uri.getSchemeSpecificPart().endsWith("/")) {
		// assert terminating path separator: a terminating
		// slash makes the check method robust when an allowed
		// path is the prefix of a non-allowed path
		this.allowedLocations[i] = uri.getSchemeSpecificPart();
	    } else {
		this.allowedLocations[i] = uri.getSchemeSpecificPart() + "/";
	    }
	    // LOG.info(uri.getRawSchemeSpecificPart() + " added to allowed paths");
	}

    }

    /**
     * This static method returns a {@link FilesystemFilter} and sets
     * its allowed locations from a system property or an environment
     * variable, which define a list of paths.  The name of the system
     * property is {@link FileSystemFilter.PROPERTY}. The name of the
     * environment variable is {@link FilesystemFilter.ENVIRON}.
     * Paths are separated by {@link FilesystemFilter.SEPARATOR}.<P>
     *
     * Configuration errors result in an empty set of allowed paths,
     * but will be notified on stderr.<P>
     *
     * <code>~</code> can be used to point to the user's home directory.
     */
    public static FilesystemFilter fromPropertiesOrEnvironment() {
	try {
	    return new FilesystemFilter(getPropOrEnv());
	} catch (FilesystemFilterException e) {
	    LOG.error(e.getMessage());
	    return new FilesystemFilter();
	}
    }

    /**
     * Get an array of paths from a system property or environment
     * variable.
     */
    protected static String[] getPropOrEnv() {
	if (System.getProperty(PROPERTY) != null) {
	    return System.getProperty(PROPERTY).split(SEPARATOR);
	} else if (System.getenv(ENVIRON) != null) {
	    return System.getenv(ENVIRON).split(SEPARATOR);
	} else {
	    String rc[] = {};
	    return rc;
	}
    }

    /**
     * Notify users that no allowed paths are configured.
     */
    protected static void notifyEmpty() {
	LOG.warning
	    ("No allowed file system locations configured for file system filter. Set the '"
	     + ENVIRON
	     + "' environment variable or the '"
	     + PROPERTY
	     + "' system property in order to add allowed paths. E.g. 'java -D"
	     + PROPERTY
	     + "=~/xsl,~/docs ...' to do so.");
    }

    /**
     * Returns the allowed locations.
     */
    public String[] getAllowedLocations() {
	return this.allowedLocations;
    }

    /**
     * Check if the given absolute {@link URI} is allowed. URIs in the
     * file system scheme will pass the check if the file is under one
     * of the allowed paths.  All relative file URIs will fail.  If
     * there is no scheme given for the URI, then it is treated as a
     * file URI.  All other URIs will pass the check.
     *
     * @param absolute  {@link URI} to check
     *
     * @return false if and only if a file URI pointing outside of allowed locations
     */
    public boolean check(URI absolute) {

	try {

	    // 1. add "file:" scheme if no scheme specified
	    if (absolute.getScheme() == null) {
		//absolute = new URI("file:" + absolute.toString());
		absolute = new URI("file", absolute.getSchemeSpecificPart(), absolute.getFragment());
	    }

	    // 2. normalize URI, i.e. process '.' and '..'
	    absolute = absolute.normalize();


	    // 3. check
	    if (absolute.getScheme().equals("file")) {
		if (absolute.getSchemeSpecificPart() == null) {
		    // illegal file URI: null path
		    return false;
		} else if (!absolute.isAbsolute()) {
		    // relative URIs fail the check
		    return false;
		} else {
		    boolean allowed = false;
		    for (String location : this.allowedLocations) {
			if (absolute.getSchemeSpecificPart().startsWith(location)) {
			    allowed = true;
			    break;
			}
		    }
		    return allowed;
		}
	    } else {
		// not a file URI
		return true;
	    }
	} catch (NullPointerException e) {
	    // illegal URI
	    return false;
	} catch (IllegalArgumentException e) {
	    // illegal URI
	    return false;
	} catch (URISyntaxException e) {
	    // illegal URI
	    return false;
	}
    }

    /**
     * This checks if the given <code>href</code> points to an allowed
     * file system location or is a non-file URI. A relative reference
     * is first resolved against the URI given in the second parameter.
     *
     * @param href  the reference to be checked
     * @param base  the base URI to be used to resolve a relative href
     *
     * @return false if and only if a file URI pointing outside of allowed locations
     *
     * @see FilesystemFilter.check(java.net.URI)
     */
    public boolean check(String href, String base) {
	try {
	    // resolve relative href
	    URI baseUri = new URI(base);
	    URI absolute = baseUri.resolve(href);
	    // check URI
	    return check(absolute);
	} catch (URISyntaxException e) {
	    return false;
	} catch (NullPointerException e) {
	    return false;
	}
    }
}
