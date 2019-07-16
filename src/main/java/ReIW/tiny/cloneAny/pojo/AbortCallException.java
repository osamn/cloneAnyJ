package ReIW.tiny.cloneAny.pojo;

import ReIW.tiny.cloneAny.AssemblyException;

@SuppressWarnings("serial")
public class AbortCallException extends AssemblyException {

	public AbortCallException() {
	}

	public AbortCallException(String message) {
		super(message);
	}

	public AbortCallException(Throwable cause) {
		super(cause);
	}

	public AbortCallException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbortCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


}