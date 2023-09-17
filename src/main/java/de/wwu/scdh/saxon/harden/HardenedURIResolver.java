package de.wwu.scdh.saxon.harden;

import javax.xml.transform.Source;

import net.sf.saxon.trans.XPathException;
import net.sf.saxon.lib.StandardURIResolver;

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
public class HardenedURIResolver extends StandardURIResolver {

    private FilesystemFilter filter = FilesystemFilter.fromPropertiesOrEnvironment();;

    /**
     * {@inheritDoc}
     */
    @Override
    public Source resolve(String href, String base) throws XPathException {
	if (filter.check(href, base)) {
	    return super.resolve(href, base);
	} else {
	    throw new XPathException("path not allowed: " + href);
	}
    }

}
