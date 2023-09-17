package de.wwu.scdh.saxon.harden;

import java.net.URI;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.lib.StandardResultDocumentResolver;
import net.sf.saxon.event.Receiver;

/**
 * Based on {@link StandardResultDocumentResolver}, this resolver
 * restricts the access to the local file system by using a {@link
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
public class HardenedResultDocumentResolver extends StandardResultDocumentResolver {

    private static FilesystemFilter filter = FilesystemFilter.fromPropertiesOrEnvironment();;

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamResult createResult(URI absoluteURI) throws XPathException, IOException {
	if (filter.check(absoluteURI)) {
	    return super.createResult(absoluteURI);
	} else {
	    throw new XPathException("path not allowed: " + absoluteURI.toString());
	}
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public static StreamResult makeOutputFile(URI absoluteURI) throws XPathException {
	if (filter.check(absoluteURI)) {
	    return StandardResultDocumentResolver.makeOutputFile(absoluteURI);
	} else {
	    throw new XPathException("path not allowed: " + absoluteURI.toString());
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamResult resolve(String href, String base) throws XPathException {
	if (filter.check(href, base)) {
	    return super.resolve(href, base);
	} else {
	    throw new XPathException("path not allowed: " + href);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receiver resolve(XPathContext context, String href, String base, SerializationProperties properties)
	throws XPathException {
	if (filter.check(href, base)) {
	    return super.resolve(context, href, base, properties);
	} else {
	    throw new XPathException("path not allowed: " + href);
	}
    }

}
