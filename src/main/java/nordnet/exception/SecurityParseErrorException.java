package nordnet.exception;

public class SecurityParseErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SecurityParseErrorException() {
        super();
    }

    public SecurityParseErrorException(String msg) {
        super(msg);
    }
}
