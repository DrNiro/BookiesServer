package dts.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dts.logic.UsersService;
import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.UserBoundary;

@RestController
public class UserController {
	private UsersService userService;

	@Autowired
	public void setService(UsersService userService) {
		this.userService = userService;
	}
	
	@RequestMapping(method = RequestMethod.POST,
					path = "/dts/users",
					produces = MediaType.APPLICATION_JSON_VALUE,
					consumes = MediaType.APPLICATION_JSON_VALUE) 
	public UserBoundary createNewUser(@RequestBody NewUserDetails newUserDetails) {
		return this.userService.createUser(new UserBoundary(newUserDetails));			
	}
	
	@RequestMapping(method = RequestMethod.GET,
					path = "/dts/users/login/{userSpace}/{userEmail}",
					produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary loginUserAndRecieveDetails(@PathVariable("userSpace") String userSpace,
												   @PathVariable("userEmail") String userEmail) {
		return this.userService.login(userSpace, userEmail);
	}
	
	@RequestMapping(method = RequestMethod.PUT,
			path = "/dts/users/{userSpace}/{userEmail}",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateUser(@PathVariable("userSpace") String userSpace,
						   @PathVariable("userEmail") String userEmail,
						   @RequestBody UserBoundary user,
						   HttpServletResponse response) {
		this.userService.updateUser(userSpace, userEmail, user);
		System.err.println("user space: " + userSpace + " user email: " + userEmail + "\n" + user);
	    response.setStatus(HttpServletResponse.SC_CREATED);
	}
	
}
