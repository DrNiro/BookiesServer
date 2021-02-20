package dts.logic.rdbservices;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.UserDao;
import dts.dal.data.UserEntity;
//import dts.dal.data.UserRole;
import dts.logic.SearchUsersService;
import dts.logic.boundaries.UserBoundary;
import dts.logic.converters.UserConverter;
import dts.util.Constants;
import dts.util.ErrorMessages;
import dts.util.Functions;
import dts.util.exceptions.UserNotFoundException;

@Service
public class RdbUserService implements SearchUsersService {

	private String spaceName;
	private UserDao userDao;
	private UserConverter userConverter;
	
	@Autowired
	public RdbUserService(UserDao userDao, UserConverter userConverter) {
		this.userDao = userDao;
		this.userConverter = userConverter;
	}
	
	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	@Override
	@Transactional
	public UserBoundary createUser(UserBoundary user) throws RuntimeException {
//		validate user details.
		if (user == null) {
			throw new NullPointerException("user is null");
		}
		
		if(user.getUsername() == null || user.getUsername() == "") {
			throw new RuntimeException("username is null or empty");
		}

		if(user.getUserId() == null) {
			throw new RuntimeException("user id is null");
		} else if(user.getUserId().getEmail() == null) {
			throw new RuntimeException("user email is null");
		} else if(!Functions.isValidEmail(user.getUserId().getEmail())) {
			throw new RuntimeException(user.getUserId().getEmail() + " not a valid email");
		}
		
		if(user.getAvatar() == null || user.getAvatar().equals("")) {
			throw new RuntimeException("avatar is null or empty");
		}

//		assign userSpace
		user.getUserId().setSpace(this.spaceName); 

//		check if user already exist in the db with this id.
		UserEntity existingUserEntity = getExistingUserEntity(user.getUserId().getSpace(), user.getUserId().getEmail());
		if(existingUserEntity != null) {
			throw new RuntimeException(user.getUserId().getEmail() +  " already exist in the system");
		}
		
//		convert entity to boundary and save in db.
		UserEntity newUserEntity = this.userConverter.toEntity(user);
		newUserEntity = this.userDao.save(newUserEntity);
		
		return this.userConverter.toBoundary(newUserEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public UserBoundary login(String userSpace, String userEmail) throws RuntimeException {
		UserEntity existingUser = getExistingUserEntityOrThrow(userSpace, userEmail, ErrorMessages.USER_NOT_EXIST);
		
		return userConverter.toBoundary(existingUser);
	}

	@Override
	@Transactional
	public UserBoundary updateUser(String userSpace, String userEmail, UserBoundary update) throws RuntimeException {
		UserEntity existingUser = getExistingUserEntityOrThrow(userSpace, userEmail, ErrorMessages.USER_NOT_EXIST);
		
//		validate user details.
		if (userSpace == null || userEmail == null || update == null) {
			throw new NullPointerException("one of the input parameters is null");
		}

		if(update.getUsername() == null || update.getUsername() == "") {
			throw new RuntimeException("username is null or empty");
		}

		if(update.getAvatar() == null || update.getAvatar().equals("")) {
			throw new RuntimeException("avatar is null or empty");
		}
				
		UserBoundary originalUser = this.userConverter.toBoundary(existingUser); // convert to boundary

//		user can't update email - keep original user id.
		update.setUserId(originalUser.getUserId());
		
//		if update values are null, keep original values
		if(update.getRole() == null) {
			update.setRole(originalUser.getRole());
		}
		
//		convert back from boundary to entity 
		UserEntity updatedUserEntity = this.userConverter.toEntity(update);
		
		updatedUserEntity = this.userDao.save(updatedUserEntity); // update db
		
		return update; // return updated boundary
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail) {
		return StreamSupport.stream(
				this.userDao.findAll().spliterator(),  
				false) // Iterable to Stream<UserEntity>,
				.map(entity -> this.userConverter.toBoundary(entity)) // Stream<UserBoundary>
				.collect(Collectors.toList()); // List<UserBoundary>
	}

	@Override
	@Transactional
	public void deleteAllUsers(String adminSpace, String adminEmail) {
		this.userDao.deleteAll();
	}
//	public void deleteAllUsers(String adminSpace, String adminEmail) {
////		permission: admin only.
//		if(this.userDao.findById(adminSpace + Constants.DELIMITER + adminEmail).get().getRole().name().equals(UserRole.ADMIN.name())) {
//			this.userDao.deleteAll();
//		} else {
//			throw new RuntimeException("permissions: admin only.");
//		}
//	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail, int size, int page) {
		return this.userDao.findAll(PageRequest.of(page, size, Direction.DESC, "username", "userId"))
				.getContent()
				.stream()
				.map(this.userConverter::toBoundary)
				.collect(Collectors.toList());
	}
//	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail, int size, int page) {
////		permission: admin only.
//		if(this.userDao.findById(adminSpace + Constants.DELIMITER + adminEmail).get().getRole().name().equals(UserRole.ADMIN.name())) {
//			return this.userDao.findAll(PageRequest.of(page, size, Direction.DESC, "username", "userId"))
//					.getContent()
//					.stream()
//					.map(this.userConverter::toBoundary)
//					.collect(Collectors.toList());
//		} else {
//			throw new RuntimeException("permissions: admin only.");
//		}
//	}
	
//	Return user by userSpace and userEmail if exist in db, else return null.
	private UserEntity getExistingUserEntity(String userSpace, String userEmail) {
		Optional<UserEntity> userOption = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail);
		if (!userOption.isPresent()) {
			return null;
		}
		return userOption.get();
	}
	
//	Return user by userSpace and userEmail if exist in db, else return null.
	private UserEntity getExistingUserEntityOrThrow(String userSpace, String userEmail, String errMsg) throws UserNotFoundException {
		Optional<UserEntity> userOption = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail);
		if (!userOption.isPresent()) {
			throw new UserNotFoundException(errMsg);
		}
		return userOption.get();
	}

}
