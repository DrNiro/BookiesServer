package dts.util.annotation.aspect;


import java.lang.reflect.Method;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.UserDao;
import dts.dal.data.UserEntity;
import dts.dal.data.UserRole;
import dts.logic.boundaries.OperationBoundary;
import dts.util.Constants;
import dts.util.annotation.Permissional;
import dts.util.exceptions.PermissionNotAuthorizedException;
import dts.util.exceptions.UserNotFoundException;

@Component
@Aspect
public class PermissionAspect {
	private UserDao userDao;
	
	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@Around("@annotation(dts.util.annotation.Permissional)")
	@Transactional(readOnly = true)
	public Object permissionChecker(ProceedingJoinPoint joinPoint) throws Throwable {
		// Pre-Processing
		Signature signature = joinPoint.getSignature();
		Object[] args = joinPoint.getArgs();
		Method joinPointMethod = ((MethodSignature)signature).getMethod();
		Permissional permissionalData = joinPointMethod.getAnnotation(Permissional.class);
		UserRole[] rolesArray = permissionalData.rolesArray();
		
		Optional<UserEntity> user = null;
		
		if(args.length == 1) {
			if(!args[0].getClass().equals(OperationBoundary.class)) {
				throw new RuntimeException("method " + signature.getName() + " parameter 0 must be of type: OperationBoundary");
			} else {
				OperationBoundary op = (OperationBoundary) args[0];
				user = this.userDao.findById(op.getInvokedBy().getUserId().getSpace() + Constants.DELIMITER + op.getInvokedBy().getUserId().getEmail());
			}
		} else if(args.length == 0) {
			throw new RuntimeException("method " + signature.getName() + " OperationBoundary or (String, String) parameters");
		} else {
			if(!args[0].getClass().equals(String.class)) {
				throw new RuntimeException("method " + signature.getName() + " parameter 0 must be of type: String");
			}
			
			if(!args[1].getClass().equals(String.class)) {
				throw new RuntimeException("method " + signature.getName() + " parameter 1 must be of type: String");
			}
			
			user = this.userDao.findById(args[0].toString() + Constants.DELIMITER + args[1].toString());
		}

		if(!user.isPresent()) {
			throw new UserNotFoundException("User not found.");
		}
		
		for(int i = 0; i < rolesArray.length; i++) {
			if(user.get().getRole().name().equals(rolesArray[i].name())) {
				// invoke original operation
				try {
					Object originalOperationRv = joinPoint.proceed();
					return originalOperationRv;
				} catch (Throwable e) {
					throw e;
				}
			}
		}
		throw new PermissionNotAuthorizedException("User with " + user.get().getRole().name() + " role not authorized to perform " + signature.getName());
		
		// Post-Processing
		// nothing...

	}
	
}
