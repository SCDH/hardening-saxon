package de.wwu.scdh.saxon.hardening;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;



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

    /**
     * The standard constructor sets no allowed locations at all.
     */
    public FilesystemFilter() {
	String empty[] = {};
	this.allowedLocations = empty;
	notifyEmpty();
    }

    /**
     * Make a new {@link FilesystemFilter}.<P>
     *
     * <code>~</code> can be used to point to the user's home directory.
     *
     * @param allowedLocations  locations on the file system allowed for read and write access
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

	    try {
		String normalizedPath;

		if (location.startsWith("~")) {
		    normalizedPath = System.getProperty("user.home") + location.substring(1);
		} else {
		    normalizedPath = location;
		}

		// make absolute
		normalizedPath = new File(normalizedPath).getAbsolutePath();
		// assert path separator (/) at end
		if (!normalizedPath.endsWith("/") && !normalizedPath.endsWith(File.separator)) {
		    // if path does not end with a path separator,
		    // resolving against it will interpret the last path
		    // segment as a file
		    normalizedPath = normalizedPath + File.separator;
		}
		// normalize
		URI uri = new URI("file", normalizedPath, "").normalize();
		// store to field
		this.allowedLocations[i] = uri.getSchemeSpecificPart();
	    } catch (URISyntaxException e) {
		throw new FilesystemFilterException("invalid path configured for FilesystemFilter: " + e.getMessage());
	    }
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
	    System.err.println(e.getMessage());
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
	System.err.println
	    ("WARNING: No allowed file system locations configured for file system filter. Set the '"
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
		if (absolute.getPath() == null) {
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
}
