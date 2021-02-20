package dts.pagingAndSortingTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ItemPagingTests {
	private String spaceName;
	private int port;
	
	private String createItemUrl;
	private String deleteItemsUrl;
	private String getItemUrl;
	private String getAllItemsUrl;
	private String getAllChildrenUrl; 
	private String getAllParentsUrl;
	private String bindItemUrl;
	private String getAllItemsByNameUrl;
	private String getAllItemsByTypeUrl;
	private String getAllItemsByLocationUrl;
	
	private String createUserUrl;
	private String deleteAllUsersUrl;
	
	private RestTemplate restTemplate;
	
	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void init() {
		this.createItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}";
		this.deleteItemsUrl = "http://localhost:" + this.port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		this.getItemUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}";
		this.getAllItemsUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}";
		this.bindItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children";
		this.getAllChildrenUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children";
		this.getAllParentsUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/parents";
		this.getAllItemsByNameUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/byNamePattern/{namePattern}";
		this.getAllItemsByTypeUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/byType/{type}";
		this.getAllItemsByLocationUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/near/{lat}/{lng}/{distance}";
		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
		this.deleteAllUsersUrl = "http://localhost:" + port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.restTemplate = new RestTemplate();
	}
	
////	 cleanup the database before each test 
//	@BeforeEach
//	public void setup() { 
//		this.restTemplate.delete(this.deleteItemsUrl, "2021a.hadar.bonavida", "testAdmin@gmail.com");
//		this.restTemplate.delete(this.deleteAllUsersUrl, "2021a.hadar.bonavida", "testAdmin@gmail.com");
//	}

//	 cleanup the database after each test 
	@AfterEach
	public void clean() { 
		this.restTemplate.delete(this.deleteItemsUrl, "2021a.hadar.bonavida", "testAdmin@gmail.com");
		this.restTemplate.delete(this.deleteAllUsersUrl, "2021a.hadar.bonavida", "testAdmin@gmail.com");
	}
	
	@Test
	public void testGetAllItemsByLocationAndDistanceWithValidPermissions() {
		int numOfItems = 3;
		
//		create manager
		UserBoundary manager = createManager();

//		create items with (lat, lng) * i, where i is in range of 1 to numOfItems.
		@SuppressWarnings("unused")
		List<ItemBoundary> itemContent = createItemContent(numOfItems, 1.1, 2.2, manager.getUserId());
		
		int pageSize = 10;
		double lat = 0.;
		double lng = 0.;
		double distance = 5.;
		
//		WHEN I GET "/dts/items/{userSpace}/{userEmail}/search/near/{lat}/{lng}/{distance}?size={size}&page={page}"
		ItemBoundary[] actualResponse = this.restTemplate.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class,
								manager.getUserId().getSpace(), manager.getUserId().getEmail(), lat, lng, distance, pageSize, 0);
		
		for(int q = 0; q < actualResponse.length; q++) {
			System.err.println("item: " + actualResponse[q].toString());
		}
		
		assertThat(actualResponse).hasSizeLessThan(pageSize);			
		
//		assert actual items received are between good values
		assertThat(actualResponse).allMatch(item -> item.getLocation().getLat() >= lat - distance);
		assertThat(actualResponse).allMatch(item -> item.getLocation().getLat() <= lat + distance);
		assertThat(actualResponse).allMatch(item -> item.getLocation().getLng() >= lng - distance);
		assertThat(actualResponse).allMatch(item -> item.getLocation().getLng() <= lng + distance);
		
	}
	
	@Test
	public void testGetAllItemsByNamePatternWithValidPermissions() {
		int numOfItems = 22;
		
//		create userItemCreator with manager role
		UserBoundary manager = createManager();
		
//		create #numOfItems items in DB
		@SuppressWarnings("unused")
		List<ItemBoundary> dbContent = createItemContent(numOfItems, "name", "type", manager.getUserId());
		
		int pageSize = 20;
//		WHEN I GET "/dts/items/{userSpace}/{userEmail}/search/byNamePattern/{namePattern}?size=20&page=0"
		ItemBoundary[] actualResponse = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, manager.getUserId().getSpace(), manager.getUserId().getEmail(), "name #1", pageSize, 0);
		
		int numOfItemsWithNameExpected = 11;
//		THEN get #numOfItemsWithNameExpected items from db.
		if(numOfItemsWithNameExpected <= pageSize) {
			assertThat(actualResponse).hasSize(numOfItemsWithNameExpected);			
		} else {
			assertThat(actualResponse).hasSize(pageSize);
		}
	}

	@Test
	public void testGetAllItemsByTypeWithValidPermission() {
		int numOfItems = 10;
		
//		create userItemCreator with manager role
		UserBoundary manager = createManager();
		
//		create #numOfItems items in DB
		@SuppressWarnings("unused")
		List<ItemBoundary> dbContent = createItemContent(numOfItems, "name", "type", manager.getUserId());
		
		int pageSize = 20;
//		WHEN I GET "/dts/items/{userSpace}/{userEmail}/search/byType/{type}?size=20&page=0"
		ItemBoundary[] actualResponse = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, manager.getUserId().getSpace(), manager.getUserId().getEmail(), "type #2", pageSize, 0);
		
		int numOfItemsWithNameExpected = 1;
//		THEN get #numOfItemsWithNameExpected items from db.
		if(numOfItemsWithNameExpected <= pageSize) {
			assertThat(actualResponse).hasSize(numOfItemsWithNameExpected);			
		} else {
			assertThat(actualResponse).hasSize(pageSize);
		}
	}
	
	@Test
	public void testGetAllItemsWithValidPermission() {
//		create 30 (> 20) items in DB
		int numOfItems = 30;
		
//		create userItemCreator with manager role
		NewUserDetails newManagerDetails = new NewUserDetails("manager@gmail.com", UserRoleBoundary.MANAGER, "managerista", "-_-;");
		UserBoundary manager = this.restTemplate.postForObject(this.createUserUrl, newManagerDetails, UserBoundary.class);
		
		@SuppressWarnings("unused")
		List<ItemBoundary> dbContent = createItemContent(numOfItems, "name", "type", manager.getUserId());
		
		int pageSize = 20;
//		WHEN I GET "/dts/items/{userSpace}/{userEmail}?size=20&page=0"
		ItemBoundary[] actualResponse = this.restTemplate
				.getForObject(this.getAllItemsUrl + "?size={size}&page={page}", ItemBoundary[].class, manager.getUserId().getSpace(), manager.getUserId().getEmail(), pageSize, 0);
		
//		THEN get #pageSize items from db.
		if(numOfItems <= pageSize) {
			assertThat(actualResponse).hasSize(numOfItems);
		} else {
			assertThat(actualResponse).hasSize(pageSize);
		}
	}

	@Test
	public void testGetChildrenFromExistingParentWithValidPermission() {
		int size = 5;
		int numOfChildren = 3;
		
//		create userItemCreator with manager role
		UserBoundary manager = createManager();
		
//		create parent in db
		ItemBoundary parentContent = createItem(manager, "Parent TestItem", "someType");

//		TEST THE TEST: created parent item.
//		/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}
		ItemBoundary testParentCreated = this.restTemplate.getForObject(this.getItemUrl, ItemBoundary.class, manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentContent.getItemId().getSpace(), parentContent.getItemId().getId());
		assertThat(testParentCreated).isNotNull();
		
//		create #numOfChildren children in db
		List<ItemBoundary> childrenContent = createItemContent(numOfChildren, "Child Test Item", "someType", manager.getUserId());
		
//		TEST THE TEST: created #numOfChildren new items named Child...
		ItemBoundary[] testChildrenCreated = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, manager.getUserId().getSpace(), manager.getUserId().getEmail(), "Child", size, 0);
		if(numOfChildren <= size) {
			assertThat(testChildrenCreated).hasSize(numOfChildren);			
		} else {
			assertThat(testChildrenCreated).hasSize(size);
		}
		
//		bind all children to parent
//		PUT /dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children
		bindChildrenToParent(parentContent, childrenContent, manager.getUserId());
		
//		ACTUAL TEST: get all children with pagination
//		/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children
		ItemBoundary[] actualTestAllChildrenOfParent = this.restTemplate
				.getForObject(this.getAllChildrenUrl + "?size={size}&page={page}", ItemBoundary[].class,
						manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentContent.getItemId().getSpace(), parentContent.getItemId().getId(), size, 0);
		
		if(numOfChildren <= size) {
			assertThat(actualTestAllChildrenOfParent).hasSize(numOfChildren);			
		} else {
			assertThat(actualTestAllChildrenOfParent).hasSize(size);
		}
		
		assertThat(actualTestAllChildrenOfParent).hasSameElementsAs(childrenContent);
	}
	
	@Test
	public void testGetParentsFromExistingItemWithExistingParents() {
		int size = 15;
		int numOfParents = 10;
		
//		create userItemCreator with manager role
		UserBoundary manager = createManager();
		
//		create parent in db
//		POST /dts/items/{managerSpace}/{managerEmail}
		ItemBoundary childContent = createItem(manager, "Child Test Item", "someType");
		
//		create #numOfChildren children in db
		List<ItemBoundary> parentsContent = createItemContent(numOfParents, "Parent Test Item", "someType", manager.getUserId());
		
//		bind all children to parent
//		PUT /dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children
		bindParentsToChild(childContent, parentsContent, manager.getUserId());

//		ACTUAL TEST: get all children with pagination
//		/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children
		ItemBoundary[] actualTestAllParentsOfChild = this.restTemplate
				.getForObject(this.getAllParentsUrl + "?size={size}&page={page}", ItemBoundary[].class,
						manager.getUserId().getSpace(), manager.getUserId().getEmail(), childContent.getItemId().getSpace(), childContent.getItemId().getId(), size, 0);

		if(numOfParents <= size) {
			assertThat(actualTestAllParentsOfChild).hasSize(numOfParents);			
		} else {
			assertThat(actualTestAllParentsOfChild).hasSize(size);
		}
		
		assertThat(actualTestAllParentsOfChild).hasSameElementsAs(parentsContent);
	}

	private List<ItemBoundary> createItemContent(int numOfItems, String baseName, String baseType, UserIdBoundary creatorId) {
		return IntStream.range(0, numOfItems)
				.mapToObj(i -> new ItemBoundary(new ItemIdBoundary(spaceName, "" + i), baseType + " #" + i, baseName + " #" + i, true, 
						new Date(), new User(creatorId), new LocationBoundary(1.0, 1.0), null))
				.map(boundary -> this.restTemplate.postForObject(this.createItemUrl, boundary, ItemBoundary.class, creatorId.getSpace(), creatorId.getEmail()))
				.collect(Collectors.toList());
	}
	
	private List<ItemBoundary> createItemContent(int numOfItems, Double baseLat, Double baseLng, UserIdBoundary creatorId) {
		return IntStream.range(0, numOfItems)
				.mapToObj(i -> new ItemBoundary(new ItemIdBoundary(spaceName, "" + i), "book", "Book Name", true, 
						new Date(), new User(creatorId), new LocationBoundary(baseLat * (i+1), baseLng * (i+1)), null))
				.map(boundary -> this.restTemplate.postForObject(this.createItemUrl, boundary, ItemBoundary.class, creatorId.getSpace(), creatorId.getEmail()))
				.collect(Collectors.toList());
	}
	
	private void bindChildrenToParent(ItemBoundary parent, List<ItemBoundary> children, UserIdBoundary managerId) {
//		/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children
		children.forEach(boundary -> this.restTemplate.put(this.bindItemUrl, boundary.getItemId(), managerId.getSpace(), managerId.getEmail(), parent.getItemId().getSpace(), parent.getItemId().getId()));
	}
	
	private void bindParentsToChild(ItemBoundary child, List<ItemBoundary> parents, UserIdBoundary managerId) {
		parents.forEach(boundary -> this.restTemplate.put(this.bindItemUrl, child.getItemId(), managerId.getSpace(), managerId.getEmail(), boundary.getItemId().getSpace(), boundary.getItemId().getId()));
	}

	private UserBoundary createManager() {
		//	create manager to create item.
		NewUserDetails newManagerDetails = new NewUserDetails("manager@gmail.com", UserRoleBoundary.MANAGER, "managerista", "-_-;");
		return this.restTemplate.postForObject(this.createUserUrl, newManagerDetails, UserBoundary.class);
	}
	
	private ItemBoundary createItem(UserBoundary manager, String itemName, String itemType) {
		return this.restTemplate.postForObject(this.createItemUrl,
				new ItemBoundary(null, itemType, itemName, true, null, 
						new User(manager.getUserId()),
						new LocationBoundary(5.2, 10.3), null),
				ItemBoundary.class, manager.getUserId().getSpace(), manager.getUserId().getEmail());
	}
	
}
