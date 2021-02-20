package dts.logic.boundaries;

import dts.logic.boundaries.subboundaries.UserRoleBoundary;

public class NewUserDetails {

	private String email;
	private UserRoleBoundary role;
	private String username;
	private String avatar;
	
	public NewUserDetails() {
		
	}
	
	public NewUserDetails(String email, UserRoleBoundary role, String username, String avatar) {
		setEmail(email);
		setRole(role);
		setUsername(username);
		setAvatar(avatar);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRoleBoundary getRole() {
		return role;
	}

	public void setRole(UserRoleBoundary role) {
		this.role = role;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
}
