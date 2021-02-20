package dts.util.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dts.dal.data.UserRole;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Permissional {

	public UserRole[] rolesArray();
	
}
