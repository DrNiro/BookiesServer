package dts.util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class EntityAlreadyExistException extends RuntimeException{
	private static final long serialVersionUID = 6589455866554378440L;

	public EntityAlreadyExistException() {

	}

	public EntityAlreadyExistException(String message) {
		super(message);
	}

	public EntityAlreadyExistException(Throwable cause) {
		super(cause);
	}

	public EntityAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
