package dts.logic.converters;

import org.springframework.stereotype.Component;

import dts.dal.data.UserEntity;
import dts.dal.data.UserRole;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;
import dts.util.Constants;

@Component
public class UserConverter {

	public UserEntity toEntity(UserBoundary userBoundary) {
		if (userBoundary == null) {
			return null;
		}
		
		UserEntity userEntity = new UserEntity();
		
		if (userBoundary.getUserId() != null) {
			userEntity.setUserId(userBoundary.getUserId().toString()); // UserId --> String
		}
		
		userEntity.setRole(UserRole.valueOf(userBoundary.getRole().toString()));

		userEntity.setUsername(userBoundary.getUsername());
		
		userEntity.setAvatar(userBoundary.getAvatar());
		
		return userEntity;
	}

	
	public UserBoundary toBoundary(UserEntity userEntity) {
		if (userEntity == null) {
			return null;
		}
		
		UserBoundary userBoundary = new UserBoundary();
		
		if (userEntity.getUserId() != null) {
			userBoundary.setUserId(createUserIdFromString(userEntity.getUserId())); // String --> UserId
		}

		userBoundary.setRole(UserRoleBoundary.valueOf(userEntity.getRole().name()));

		userBoundary.setUsername(userEntity.getUsername());
		
		userBoundary.setAvatar(userEntity.getAvatar());
		
		return userBoundary;
	}
	
	private UserIdBoundary createUserIdFromString(String userIdAsString) {
		if (userIdAsString != null) {
			String[] args = userIdAsString.split(Constants.DELIMITER);
			return new UserIdBoundary(args[0], args[1]);
		} else {
			return null;
		}
	}
	
}
