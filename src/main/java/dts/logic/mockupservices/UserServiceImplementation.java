package dts.logic.mockupservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dts.dal.data.UserEntity;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Service;

import dts.logic.UsersService;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.converters.UserConverter;

//@Service
public class UserServiceImplementation implements UsersService, CommandLineRunner {
	private String spaceName;
	private Map<String, UserEntity> userStore;
	private UserConverter userConverter;
	
	@Value("${spring.application.name:defaultappname}") 
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}
	
	@Autowired
	public void setUserConverter(UserConverter userConverter) {
		this.userConverter = userConverter;
	}
	
	@PostConstruct
	public void init() {
		this.userStore = Collections.synchronizedMap(new HashMap<>()); // thread safe map - concurrent hash map
	}
	
	@Override
	public void run(String... args) throws Exception { 
		System.err.println(this.spaceName);
	}

	
	@Override
	public UserBoundary createUser(UserBoundary user) throws RuntimeException {		
		if(user.getUserId().getEmail() == null) {
			throw new RuntimeException("user email is null");
		}
		
		user.getUserId().setSpace(this.spaceName);

		if (userStore.containsKey((new UserIdBoundary(user.getUserId().getSpace(), user.getUserId().getEmail())).toString())) {
			throw new RuntimeException("email already exist in the system");
		}
		
		UserEntity userEntity = this.userConverter.toEntity(user); // convert from boundary to entity 
		
		// MOCKUP database store of the entity		
		this.userStore.put(user.getUserId().toString(), userEntity);
					
		return this.userConverter.toBoundary(userEntity);
	}

	@Override
	public UserBoundary login(String userSpace, String userEmail) throws RuntimeException {
		if (!userStore.containsKey((new UserIdBoundary(userSpace, userEmail)).toString())) {
			throw new RuntimeException("user does not exist in the system");
		}
		
		UserEntity userEntity = userStore.get((new UserIdBoundary(userSpace, userEmail)).toString());
		
		return userConverter.toBoundary(userEntity);
	}

	@Override
	public UserBoundary updateUser(String userSpace, String userEmail, UserBoundary update) throws RuntimeException {
		if (!userStore.containsKey(new UserIdBoundary(userSpace, userEmail).toString())) {
			throw new RuntimeException("user does not exist in the system");
		}
		
		UserBoundary originalUser = this.userConverter.toBoundary(this.userStore.get(new UserIdBoundary(userSpace, userEmail).toString()));
		
		update.setUserId(originalUser.getUserId());
		
		UserEntity userEntity = this.userConverter.toEntity(update); // convert from boundary to entity 
		
		// MockUp database store of user
		this.userStore.put(update.getUserId().toString(), userEntity); 
		
		return this.userConverter.toBoundary(userEntity);
	}

	@Override
	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail) {
		return this.userStore
				.values() // Collection<UserEntity>
				.stream() // Stream<UserEntity>
				.map(entity -> userConverter.toBoundary(entity)
				) // Stream<UserBoundary>
				.collect(Collectors.toList()); // List<UserBoundary> 
	}

	@Override
	public void deleteAllUsers(String adminSpace, String adminEmail) {
		this.userStore.clear();
	}

}
