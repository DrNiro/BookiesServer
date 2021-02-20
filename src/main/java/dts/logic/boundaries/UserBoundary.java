package dts.logic.boundaries;

import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;

public class UserBoundary {
	private UserIdBoundary userId;
	private UserRoleBoundary role;
	private String username;
	private String avatar;
	
	public UserBoundary() {
		
	}
	
	public UserBoundary(UserIdBoundary userId, UserRoleBoundary role, String username, String avatar) {
		setUserId(userId);
		setRole(role);
		setUsername(username);
		setAvatar(avatar);
	}
	
	public UserBoundary(NewUserDetails newUserDetails) throws IllegalArgumentException {	
//		setRole(UserRoleBoundary.valueOf(newUserDetails.getRole())); // throwing illegal argument exception if not having enum value.

		setUserId(new UserIdBoundary("" ,newUserDetails.getEmail())); // spaceName null, implemented in userServiceImp 
		setRole(newUserDetails.getRole());
		setUsername(newUserDetails.getUsername());
		setAvatar(newUserDetails.getAvatar());
	}

	public UserIdBoundary getUserId() {
		return userId;
	}

	public void setUserId(UserIdBoundary userId) {
		this.userId = userId;
	}

	public UserRoleBoundary getRole() {
		return role;
	}

	public void setRole(UserRoleBoundary userRole) {
		this.role = userRole;
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

	@Override
	public String toString() {
		return "UserBoundary [userId=" + userId + ", role=" + role + ", username=" + username + ", avatar=" + avatar
				+ "]";
	}
	
}
