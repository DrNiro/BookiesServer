package dts.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dts.logic.SearchAndBindItemService;
import dts.logic.SearchOperationsService;
import dts.logic.SearchUsersService;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.UserBoundary;
import dts.util.Constants;

@RestController
public class AdminController {

	private SearchAndBindItemService itemsService;
	private SearchUsersService searchUsersService;
	private SearchOperationsService searchOperationsService;

	@Autowired
	public void setServices(SearchAndBindItemService itemsService, SearchUsersService searchUsersService, SearchOperationsService searchOperationsService) {
		this.itemsService = itemsService;
		this.searchUsersService = searchUsersService;
		this.searchOperationsService = searchOperationsService;
	}
	
	@RequestMapping(method = RequestMethod.DELETE,
			path = "/dts/admin/users/{adminSpace}/{adminEmail}")
	public void deleteAllUsersInSpace(@PathVariable("adminSpace") String adminSpace,
									  @PathVariable("adminEmail") String adminEmail) {
		this.searchUsersService.deleteAllUsers(adminSpace, adminEmail);
	}
	
	@RequestMapping(method = RequestMethod.DELETE,
			path = "/dts/admin/items/{adminSpace}/{adminEmail}")
	public void deleteAllItemsInSpace(@PathVariable("adminSpace") String adminSpace,
			  						  @PathVariable("adminEmail") String adminEmail) {
		this.itemsService.deleteAll(adminSpace, adminEmail);
	}
	
	@RequestMapping(method = RequestMethod.DELETE,
			path = "/dts/admin/operations/{adminSpace}/{adminEmail}")
	public void deleteAllOperationsInSpace(@PathVariable("adminSpace") String adminSpace,
			  						 	   @PathVariable("adminEmail") String adminEmail) {
		this.searchOperationsService.deleteAllActions(adminSpace, adminEmail);
	}

	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/admin/users/{adminSpace}/{adminEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] exportAllUsers(@PathVariable("adminSpace") String adminSpace,
										 @PathVariable("adminEmail") String adminEmail,
										 @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
									     @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchUsersService.getAllUsers(adminSpace, adminEmail, size, page).toArray(new UserBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/admin/operations/{adminSpace}/{adminEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public OperationBoundary[] exportAllOps(@PathVariable("adminSpace") String adminSpace,
										    @PathVariable("adminEmail") String adminEmail,
										    @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
										    @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchOperationsService.getAllOperations(adminSpace, adminEmail, size, page).toArray(new OperationBoundary[0]);
	}
	
}
