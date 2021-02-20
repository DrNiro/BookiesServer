package dts.util.exceptions;

public class UserAlreadyExistException extends EntityAlreadyExistException{
	private static final long serialVersionUID = 6987452098792504521L;

	public UserAlreadyExistException() {
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyExistException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyExistException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
