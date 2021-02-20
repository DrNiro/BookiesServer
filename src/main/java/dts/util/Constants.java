package dts.util;

import java.util.regex.Pattern;

public class Constants {
	final public static String SYSTEM_MANAGER_EMAIL = "systemManager@bookies.com";
	
	final public static String DELIMITER = ";";
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);	
	
	public static final String DEFAULT_PAGE_SIZE = "20";
	
	public static final String DEFAULT_PAGE_OFFSET = "0";
	
}
