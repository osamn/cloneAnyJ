package ReIW.tiny.cloneAny.pojo;

public class UnboundMethodParameterNameException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnboundMethodParameterNameException() {
	}

	public UnboundMethodParameterNameException(String message) {
		super(message);
	}

	public UnboundMethodParameterNameException(Throwable cause) {
		super(cause);
	}

	public UnboundMethodParameterNameException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnboundMethodParameterNameException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
