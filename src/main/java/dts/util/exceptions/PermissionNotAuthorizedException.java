package dts.util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class PermissionNotAuthorizedException extends RuntimeException {
	private static final long serialVersionUID = -7342310758614472747L;

	public PermissionNotAuthorizedException() {

	}

	public PermissionNotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermissionNotAuthorizedException(String message) {
		super(message);
	}

	public PermissionNotAuthorizedException(Throwable cause) {
		super(cause);
	}
	
	
	
}
