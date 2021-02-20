package dts.util.annotation.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.logic.SearchUsersService;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;

@Component
@Aspect
public class TempManagerAspect {
	private SearchUsersService userService;
	
	@Autowired
	public void setUserService(SearchUsersService userService) {
		this.userService = userService;
	}
	
	@Around("@annotation(dts.util.annotation.TempManagerPermmision)")
	@Transactional(readOnly = true)
	public Object tempManager(ProceedingJoinPoint joinPoint) throws Throwable {
		// Pre-Processing
		Object[] args = joinPoint.getArgs();

		OperationBoundary operation;
		
		if(!args[0].getClass().equals(OperationBoundary.class)) {
			throw new RuntimeException("annotation made for invokeOperation functions only.");
		} else {
			operation = (OperationBoundary) args[0];
		}
		
		UserBoundary user = this.userService.login(operation.getInvokedBy().getUserId().getSpace(), operation.getInvokedBy().getUserId().getEmail());
		
		user.setRole(UserRoleBoundary.MANAGER);
		this.userService.updateUser(user.getUserId().getSpace(), user.getUserId().getEmail(), user);

		Object rv;
		// invoke original
		try {
			rv = joinPoint.proceed();
		} catch (Throwable e) {
			throw e;
		}
		
		// post processing
//		revert user role to player.
		user.setRole(UserRoleBoundary.PLAYER);
		this.userService.updateUser(user.getUserId().getSpace(), user.getUserId().getEmail(), user);
		
//		return book as ItemBoundary
		return rv;
	}
	
}
