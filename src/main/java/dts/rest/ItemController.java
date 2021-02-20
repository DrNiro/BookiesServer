package dts.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.util.Constants;


@RestController
public class ItemController {
	private SearchAndBindItemService searchAndBindItemService;
	
	@Autowired
	public void setService(SearchAndBindItemService searchAndBindItemService) {
		this.searchAndBindItemService = searchAndBindItemService;
	}

	@RequestMapping(method = RequestMethod.POST,
			path = "/dts/items/{managerSpace}/{managerEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary createItem(@PathVariable("managerSpace") String managerSpace,
								   @PathVariable("managerEmail") String managerEmail,
								   @RequestBody ItemBoundary item) {
		return this.searchAndBindItemService.create(managerSpace, managerEmail, item);
	}

	@RequestMapping(method = RequestMethod.PUT,
			path = "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateItem(@PathVariable() String managerSpace,
						   @PathVariable() String managerEmail,
						   @PathVariable() String itemSpace,
						   @PathVariable() String itemId,
						   @RequestBody ItemBoundary item,
						   HttpServletResponse response){
		this.searchAndBindItemService.update(managerSpace, managerEmail, itemSpace, itemId, item);
		System.err.println("item updated: " + item);
	    response.setStatus(HttpServletResponse.SC_CREATED);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary getSpecificItem(@PathVariable("userSpace") String userSpace,
										@PathVariable("userEmail") String userEmail,
										@PathVariable("itemSpace") String itemSpace,
										@PathVariable("itemId") String itemId) {
		return this.searchAndBindItemService.getSpecificItem(userSpace, userEmail, itemSpace, itemId);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] getAllItems(@PathVariable("userSpace") String userSpace,
									  @PathVariable("userEmail") String userEmail,
									  @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
								      @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.getAll(userSpace,userEmail, size, page).toArray(new ItemBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.PUT,
			path = "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public void bindExistItemToExistChildItem(@PathVariable("managerSpace") String managerSpace,
											  @PathVariable("managerEmail") String managerEmail,
											  @PathVariable("itemSpace") String itemSpace,
											  @PathVariable("itemId") String itemId,
											  @RequestBody ItemIdBoundary item,
											  HttpServletResponse response) {
		this.searchAndBindItemService.bindItemToChild(managerSpace, managerEmail, itemSpace, itemId, item);
	    response.setStatus(HttpServletResponse.SC_CREATED);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] getAllChildrenOfExistItem(@PathVariable("userSpace") String userSpace,
													@PathVariable("userEmail") String userEmail,
													@PathVariable("itemSpace") String itemSpace,
													@PathVariable("itemId") String itemId,
													@RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
												    @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.getAllChildren(userSpace, userEmail, itemSpace, itemId, size, page).toArray(new ItemBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/parents",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] getItemParents(@PathVariable("userSpace") String userSpace,
										 @PathVariable("userEmail") String userEmail,
										 @PathVariable("itemSpace") String itemSpace,
										 @PathVariable("itemId") String itemId,
										 @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
									     @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.getItemParents(userSpace, userEmail, itemSpace, itemId, size, page).toArray(new ItemBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/search/byNamePattern/{namePattern}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] searchItemsByNamePattern(@PathVariable("userSpace") String userSpace,
			 									   @PathVariable("userEmail") String userEmail,
			 									   @PathVariable("namePattern") String namePattern,
			 									   @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
												   @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.searchByNamePattern(userSpace, userEmail, namePattern, size, page).toArray(new ItemBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/search/byType/{type}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] searchItemsByType(@PathVariable("userSpace") String userSpace,
										    @PathVariable("userEmail") String userEmail,
										    @PathVariable("type") String type,
										    @RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
										    @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.searchByType(userSpace, userEmail, type, size, page).toArray(new ItemBoundary[0]);
	}
	
	@RequestMapping(method = RequestMethod.GET,
			path = "/dts/items/{userSpace}/{userEmail}/search/near/{lat}/{lng}/{distance}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] searchItemsByLocation(@PathVariable("userSpace") String userSpace,
		    									@PathVariable("userEmail") String userEmail,
		    									@PathVariable("lat") String lat,
		    									@PathVariable("lng") String lng,
		    									@PathVariable("distance") String distance,
		    									@RequestParam(name = "size", required = false, defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
											    @RequestParam(name = "page", required = false, defaultValue = Constants.DEFAULT_PAGE_OFFSET) int page) {
		return this.searchAndBindItemService.searchByLocation(userSpace, userEmail, lat, lng, distance, size, page).toArray(new ItemBoundary[0]);
	}
	
}
