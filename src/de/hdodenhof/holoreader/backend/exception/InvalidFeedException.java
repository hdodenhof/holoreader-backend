package de.hdodenhof.holoreader.backend.exception;

public class InvalidFeedException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidFeedException() {
        super("UNSPECIFIED_ERROR");
    }

    public InvalidFeedException(String msg) {
        super(msg);
    }

}
