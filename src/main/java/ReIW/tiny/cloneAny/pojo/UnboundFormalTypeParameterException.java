package ReIW.tiny.cloneAny.pojo;

public class UnboundFormalTypeParameterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnboundFormalTypeParameterException() {
	}

	public UnboundFormalTypeParameterException(String message) {
		super(message);
	}

	public UnboundFormalTypeParameterException(Throwable cause) {
		super(cause);
	}

	public UnboundFormalTypeParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnboundFormalTypeParameterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
