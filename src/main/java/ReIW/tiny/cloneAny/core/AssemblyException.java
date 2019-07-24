package ReIW.tiny.cloneAny.core;

@SuppressWarnings("serial")
public class AssemblyException extends RuntimeException {

	public AssemblyException() {
	}

	public AssemblyException(String message) {
		super(message);
	}

	public AssemblyException(Throwable cause) {
		super(cause);
	}

	public AssemblyException(String message, Throwable cause) {
		super(message, cause);
	}

	public AssemblyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
