package de.wwu.scdh.saxon.harden;

import java.net.URI;
import java.io.Reader;

import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.lib.StandardUnparsedTextResolver;

/**
 * Based on {@link StandardURIResolver}, this resolver restricts the
 * access to the local file system by using a {@link
 * FilesystemFilter}.<P>
 *
 * The allowed locations are set from a system property or an
 * environment variable, which define a list of paths.  The name of
 * the system property is {@link FileSystemFilter.PROPERTY}. The name
 * of the environment variable is {@link FilesystemFilter.ENVIRON}.
 * Paths are separated by {@link FilesystemFilter.SEPARATOR}.<P>
 *
 * Configuration errors result in an empty set of allowed paths,
 * but will be notified on stderr.<P>
 *
 * <code>~</code> can be used to point to the user's home directory.
 *
 * @see FilesystemFilter
 * @see FilesystemFilter.fromPropertyOrEnviron()
 */
public class HardenedUnparsedTextResolver extends StandardUnparsedTextResolver {

    private FilesystemFilter filter = FilesystemFilter.fromPropertiesOrEnvironment();;

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws XPathException {
	if (filter.check(absoluteURI)) {
	    return super.resolve(absoluteURI, encoding, config);
	} else {
	    throw new XPathException("path not allowed: " + absoluteURI.toString());
	}
    }

}
