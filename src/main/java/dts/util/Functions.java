package dts.util;

import java.util.regex.Matcher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Functions {

	public static boolean isValidEmail(String email) {
        Matcher matcher = Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
	}
	
	public static Object convertLinkedTreeToClass(Object obj, Class<?> toClass) {
		try {
			return new ObjectMapper().convertValue(obj, toClass);
		} catch (Exception e) {
			throw e;
		}
		
	}
}
