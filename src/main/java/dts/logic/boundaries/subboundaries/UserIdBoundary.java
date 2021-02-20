package dts.logic.boundaries.subboundaries;

import dts.util.Constants;

public class UserIdBoundary {

	private String space;
	private String email;
	
	public UserIdBoundary() {
		
	}

	public UserIdBoundary(String space, String email) {
		setSpace(space);
		setEmail(email);
	}
	
	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return getSpace() + Constants.DELIMITER + getEmail();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof UserIdBoundary)) {
			return false;
		}
		UserIdBoundary otherId = (UserIdBoundary) o;
		if (!(getSpace().equals(otherId.getSpace()))) {
			return false;
		}
		if(!(getEmail().equals(otherId.getEmail()))) {
			return false;
		}
		return true;
	}
	
	
}
