package nordnet.exception;

public class SecurityNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SecurityNotFoundException() {
        super();
    }

    public SecurityNotFoundException(String msg) {
        super(msg);
    }
}
