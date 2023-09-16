package de.wwu.scdh.saxon.hardening;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;




public class FilesystemFilterTest {


    FilesystemFilter filter;


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

    @Test
    public void construct() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs", "/application/dir/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(3, filter.getAllowedLocations().length);
	assertEquals("/application/xsl/", filter.getAllowedLocations()[0]);
	assertEquals("/application/docs/", filter.getAllowedLocations()[1]);
	assertEquals("/application/dir/", filter.getAllowedLocations()[2]);
    }

    @Test
    public void constructUserHome() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "~/xsl" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertEquals(1, filter.getAllowedLocations().length);
	assertTrue(filter.getAllowedLocations()[0].startsWith(System.getProperty("user.home")));
	assertTrue(filter.getAllowedLocations()[0].endsWith("/xsl/"));
    }

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
    public void fileURI() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("file:/etc/passwd")));
    }

    @Test
    public void filePath() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("/etc/passwd")));
    }

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

    @Test
    public void filterPaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertTrue(filter.check(new URI("/application/xsl/id.xsl")));
	assertTrue(filter.check(new URI("/application/xsl/lib/i18n.xsl")));
	assertTrue(filter.check(new URI("/application/docs/hello.xml")));
	assertFalse(filter.check(new URI("/application/config/secret")));
    }

    @Test
    public void normalizePaths() throws FilesystemFilterException, URISyntaxException {
	String allowed[] = { "/application/xsl", "/application/docs" };
	FilesystemFilter filter = new FilesystemFilter(allowed);
	assertFalse(filter.check(new URI("/application/xsl/../config/secret")));
	assertFalse(filter.check(new URI("/application/xsl/../../etc/passwd")));
	// normalizing may also result in allowed paths
	assertTrue(filter.check(new URI("/etc/../application/xsl/id.xsl")));
    }

}
