package dts.util.exceptions;

//@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class EntityNullPointerException extends NullPointerException {
	private static final long serialVersionUID = 2927476040777506115L;

	public EntityNullPointerException() {

	}

	public EntityNullPointerException(String msg) {
		super(msg);
	}

}
