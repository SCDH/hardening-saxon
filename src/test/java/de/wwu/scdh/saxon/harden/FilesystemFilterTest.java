package de.wwu.scdh.saxon.harden;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;



public class FilesystemFilterTest {


    @Test
    public void constructDefault() {
	FilesystemFilter filter = new FilesystemFilter();
	assertEquals(0, filter.getAllowedLocations().length);
    }

    @Test
    public void constructNull() {
	assertThrows(FilesystemFilterException.class, () -> new FilesystemFilter(null));
    }

    @Test
    public void constructNullFilePath() {
	String allowed[] = { null };
	assertThrows(FilesystemFilterException.class, () -> new FilesystemFilter(allowed));
    }

    @Test
    public void constructEmptyPath() throws FilesystemFilterException {
	String allowed[] = { "" };
	assertThrows(FilesystemFilterException.class, () -> new FilesystemFilter(allowed));
    }

    @Test
    public void constructFileURI() throws FilesystemFilterException {
	String allowed[] = { "file:/application/xsl" };
	assertThrows(FilesystemFilterException.class, () -> new FilesystemFilter(allowed));
    }

    @Test
    public void constructEmptyArray() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = {};
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(0, filter.getAllowedLocations().length);
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void construct() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs", "/application/dir/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(3, filter.getAllowedLocations().length);
	assertEquals("/application/xsl/", filter.getAllowedLocations()[0]);
	assertEquals("/application/docs/", filter.getAllowedLocations()[1]);
	assertEquals("/application/dir/", filter.getAllowedLocations()[2]);
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void constructUserHome() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "~/xsl" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(1, filter.getAllowedLocations().length);
	assertTrue(filter.getAllowedLocations()[0].startsWith(System.getProperty("user.home")));
	assertTrue(filter.getAllowedLocations()[0].endsWith("/xsl/"));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void constructRelative() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "xsl" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(1, filter.getAllowedLocations().length);
	// assertEquals("", filter.getAllowedLocations()[0]);
	assertTrue(filter.getAllowedLocations()[0].endsWith("/xsl/"));
	assertTrue(filter.getAllowedLocations()[0].length() > 4);
    }

    @Test
    public void fromPropertiesOrEnviron() {
	FilesystemFilter filter = FilesystemFilter.fromPropertiesOrEnvironment();
	int n = 0;
	if (System.getProperty(FilesystemFilter.PROPERTY) != null) {
	    n = System.getProperty(FilesystemFilter.PROPERTY).split(FilesystemFilter.SEPARATOR).length;
	} else if (System.getenv(FilesystemFilter.ENVIRON) != null) {
	    n = System.getenv(FilesystemFilter.ENVIRON).split(FilesystemFilter.SEPARATOR).length;
	}
	assertEquals(n, filter.getAllowedLocations().length);
    }

    @Test
    public void fromProperties() {
	System.setProperty(FilesystemFilter.PROPERTY, "~/xsl,~/doc");
	FilesystemFilter filter = FilesystemFilter.fromPropertiesOrEnvironment();
	assertEquals(2, filter.getAllowedLocations().length);
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void fileURI() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("file:/etc/passwd")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void filePath() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("/etc/passwd")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void denyRelative() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertFalse(filter.check(new URI("xsl/id.xsl")));
	assertFalse(filter.check(new URI("./xsl/id.xsl")));
	assertFalse(filter.check(new URI("../xsl/id.xsl")));
	assertFalse(filter.check(new URI("...../xsl/id.xsl")));
	assertFalse(filter.check(new URI("file:xsl/id.xsl")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void filterPaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("/application/xsl/id.xsl")));
	assertTrue(filter.check(new URI("/application/xsl/lib/i18n.xsl")));
	assertTrue(filter.check(new URI("/application/docs/hello.xml")));
	assertFalse(filter.check(new URI("/application/config/secret")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void normalizePaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertFalse(filter.check(new URI("/application/xsl/../config/secret")));
	assertFalse(filter.check(new URI("/application/xsl/../../etc/passwd")));
	// normalizing may also result in allowed paths
	assertTrue(filter.check(new URI("/etc/../application/xsl/id.xsl")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void prefixPaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertFalse(filter.check(new URI("/application/xslt/id.xsl")));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void nonFileURIs() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("http://example.com/application/xsl/../config/secret")));
	assertTrue(filter.check(new URI("https://example.com/application/xsl/../../etc/passwd")));
	assertTrue(filter.check(new URI("ftp://example.com/application/xsl/id.xsl")));
    }


    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void resolvePaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check("other/brother.xsl", "/application/xsl/id.xsl"));
	assertTrue(filter.check("../docs/brother.xml", "/application/xsl/id.xsl"));
	assertFalse(filter.check("../other/secret", "/application/xsl/id.xsl"));
	assertFalse(filter.check("shadow", "/etc/passwd"));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void resolveAbsPaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check("/application/xsl/brother.xsl", "/application/xsl/id.xsl"));
	assertTrue(filter.check("/application/xsl/../docs/brother.xml", "/application/xsl/id.xsl"));
	assertFalse(filter.check("/application/../other/secret", "/application/xsl/id.xsl"));
	assertFalse(filter.check("/etc/shadow", "/etc/passwd"));
	assertFalse(filter.check("", ""));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void resolveNonFilePaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check("other.xsl", "http://example.com/xsl/id.xsl"));
	assertTrue(filter.check("other.xml", "https://example.com/xsl/id.xsl"));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void resolvePathsCornerCases() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertFalse(filter.check("", ""));
	assertFalse(filter.check("id.xsl", "relative.xsl"));
	assertFalse(filter.check("id.xsl", null));
	assertFalse(filter.check(null, "/application/xsl/id.xsl"));
	assertFalse(filter.check(null, "/application/xsl/id.xsl"));
	assertFalse(filter.check("/application/xsl/id.xsl", null)); // TODO?
    }

}
