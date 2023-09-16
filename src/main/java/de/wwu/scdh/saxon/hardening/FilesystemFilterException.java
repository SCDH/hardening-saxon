package de.wwu.scdh.saxon.hardening;

public class FilesystemFilterException extends Exception {
    public FilesystemFilterException(String msg) {
        super(msg);
    }
    public FilesystemFilterException(Throwable cause) {
        super(cause);
    }
    public FilesystemFilterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
