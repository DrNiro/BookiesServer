package dts.logic;

import java.util.List;

import dts.logic.boundaries.UserBoundary;

public interface SearchUsersService extends UsersService {

	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail, int size, int page);
	
}
