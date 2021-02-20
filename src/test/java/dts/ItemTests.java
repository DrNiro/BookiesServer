package dts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;
import dts.util.Constants;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // causes the server to go up
public class ItemTests {
	private int port;
	private String spaceName;
	
	private String testUserMail;
	private String testItemId;
	private String testItemType;
	private String testItemName;
	private boolean testItemActive;
	private double testItemLat;
	private double testItemLng;
	
	private String createItemUrl;
	private String updateItemUrl;
	private String deleteItemsUrl;
	private String deleteUsersUrl;
	private String deleteOperationsUrl;
	private String getItemUrl;
	private String getAllItemsUrl;
	private String getAllChildrenUrl;
	private String getAllParentsUrl;
	private String bindItemUrl;
	
	private String createUserUrl;
	
	private RestTemplate restTemplate;

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}

	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	@PostConstruct
	public void init() {
		this.testUserMail = "testUser@Mail.com";
		this.testItemId = "1";
		this.testItemType = "testItemType";
		this.testItemName = "testItemName";
		this.testItemActive = true;
		this.testItemLat = 1;
		this.testItemLng = 2;

		this.createItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}";
		this.updateItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}";
		this.deleteItemsUrl = "http://localhost:" + this.port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		this.deleteUsersUrl = "http://localhost:" + this.port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.deleteOperationsUrl = "http://localhost:" + this.port + "/dts/admin/operations/{adminSpace}/{adminEmail}";
		this.getItemUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}";
		this.getAllItemsUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}";
		this.bindItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children";
		this.getAllChildrenUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children";
		this.getAllParentsUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/parents";
		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
		this.restTemplate = new RestTemplate();
	}

	// cleanup the database before each test 
	@BeforeEach
	public void setup() {
		this.restTemplate.delete(this.deleteItemsUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteUsersUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteOperationsUrl, this.spaceName, "admin@gmail.com");
	}

	// cleanup the database after each test
	@AfterEach
	public void tearDown() {
		this.restTemplate.delete(this.deleteItemsUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteUsersUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteOperationsUrl, this.spaceName, "admin@gmail.com");
	}

	@Test
	public void contextLoads() {

	}

	@Test
	public void testCreateItemWithEmptyDatabase() {
		long now = System.currentTimeMillis();

//	GIVEN the server is up (Automatically by SpringBootTest)
		UserBoundary manager = createManager();
		
//	WHEN I POST /dts/items/{managerSpace}/{managerEmail}
		ItemBoundary originalItem = createItem(manager);

//	THEN the result HTTP STATUS 2XX
//		AND item is created

		// solo checks - originalItem
		assertThat(originalItem.getItemId()).isNotNull();

		assertThat(originalItem.getCreatedTimestamp()).isNotNull();

		assertThat(originalItem.getCreatedBy()).isNotNull();

		assertThat(originalItem.getCreatedBy().getUserId()).isNotNull();

		assertThat(originalItem.getLocation()).isNotNull();

		assertThat(originalItem.getCreatedTimestamp().getTime()).isGreaterThan(now);

		assertThat(originalItem.getLocation()).extracting("lat", "lng").containsExactly(this.testItemLat, this.testItemLng);

		assertThat(originalItem.getActive()).isEqualTo(this.testItemActive);

		assertThat(originalItem.getItemAttributes()).isEmpty();
	}

	@Test
	public void testUpdateItemWhileItemExistWithNullLocation() {
		String newItemType = "newTestItemType";
		String newItemName = "newTestItemName";
		boolean newTestActive = false;

//	GIVEN the server is up (Automatically by SpringBootTest)
//		AND there's an exist an item
		UserBoundary manager = createManager();
		ItemBoundary existingItem = createItem(manager);

//	WHEN I PUT /dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}
//		AND type updated
//		AND name updated
//		AND active updated
//		AND locationBoundary is null
		ItemBoundary update = new ItemBoundary(null,			// id (null - can't update)
											   newItemType,		// type (updated)
											   newItemName, 	// name (updated)
											   newTestActive, 	// active (updated)
											   null, 			// time stamp (null - can't update)
											   null, 			// createdBy (null - can't update)
											   null, 			// location (null - stay as original)
											   null);			// attributes (null - no attributes)
		
		this.restTemplate.put(this.updateItemUrl, update, manager.getUserId().getSpace(), manager.getUserId().getEmail(),
														  existingItem.getItemId().getSpace(), existingItem.getItemId().getId());

//	THEN the result HTTP STATUS 2XX
//		AND the item is updated
//		AND location is not null
//		/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}
		ItemBoundary updatedItem = this.restTemplate.getForObject(this.getItemUrl, ItemBoundary.class, manager.getUserId().getSpace(),
				manager.getUserId().getEmail(), existingItem.getItemId().getSpace(), existingItem.getItemId().getId());
		
		// null checks - updatedItem
		assertThat(updatedItem.getItemId()).isNotNull();

		assertThat(updatedItem.getCreatedTimestamp()).isNotNull();

		assertThat(updatedItem.getCreatedBy()).isNotNull();

		assertThat(updatedItem.getCreatedBy().getUserId()).isNotNull();

		assertThat(updatedItem.getLocation()).isNotNull();
		
		assertThat(updatedItem.getActive()).isNotNull();
		
		assertThat(updatedItem.getName()).isNotNull();
		
		assertThat(updatedItem.getType()).isNotNull();

		// compare checks - updatedItem vs originalItem
		assertThat(updatedItem.getItemId().getSpace()).isEqualTo(existingItem.getItemId().getSpace());

		assertThat(updatedItem.getCreatedTimestamp().getTime()).isEqualTo(existingItem.getCreatedTimestamp().getTime());

		assertThat(updatedItem.getCreatedBy().getUserId()).extracting("space", "email").containsExactly(
				existingItem.getCreatedBy().getUserId().getSpace(), existingItem.getCreatedBy().getUserId().getEmail());

		assertThat(updatedItem.getLocation()).extracting("lat", "lng").containsExactly(existingItem.getLocation().getLat(), existingItem.getLocation().getLng());

		assertThat(updatedItem).extracting("active", "itemAttributes").containsExactly(newTestActive, existingItem.getItemAttributes());
	}

	@Test
	public void testGetItemWhileItemExist() {
		long now = System.currentTimeMillis();

		// GIVEN the server is up (Automatically by SpringBootTest)
		// AND there is an item at
		// /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}
		UserBoundary manager = createManager();
		
		ItemBoundary originalItem = createItem(manager);

		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}
		ItemBoundary fetchedItem = this.restTemplate.getForObject(this.getItemUrl, ItemBoundary.class, this.spaceName,
				manager.getUserId().getEmail(), this.spaceName, originalItem.getItemId().getId());

		// THEN the result HTTP STATUS 2XX

		// solo checks - fetchedItem
		assertThat(fetchedItem.getCreatedTimestamp().getTime()).isGreaterThan(now);
		
		assertThat(fetchedItem.getItemId()).isNotNull();

		assertThat(fetchedItem.getCreatedTimestamp()).isNotNull();

		assertThat(fetchedItem.getCreatedBy()).isNotNull();

		assertThat(fetchedItem.getCreatedBy().getUserId()).isNotNull();

		assertThat(fetchedItem.getLocation()).isNotNull();

		// compare checks - fetchedItem vs originalItem
		assertThat(fetchedItem.getItemId().getSpace()).isEqualTo(originalItem.getItemId().getSpace());

		assertThat(fetchedItem.getCreatedTimestamp().getTime()).isEqualTo(originalItem.getCreatedTimestamp().getTime());

		assertThat(fetchedItem.getCreatedBy().getUserId()).extracting("space", "email").containsExactly(
				originalItem.getCreatedBy().getUserId().getSpace(), originalItem.getCreatedBy().getUserId().getEmail());

		assertThat(fetchedItem.getLocation()).extracting("lat", "lng")
				.containsExactly(originalItem.getLocation().getLat(), originalItem.getLocation().getLng());

		assertThat(fetchedItem).extracting("active", "itemAttributes").containsExactly(originalItem.getActive(),
				originalItem.getItemAttributes());
	}

	@Test
	public void testGetItemWhileItemNotExist() {
		//	GIVEN the server is up (Automatically by SpringBootTest)
		//		AND a manager exist.
		UserBoundary manager = createManager();
		
		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}
		// THEN the result HTTP STATUS NOT 2XX
		assertThrows(NotFound.class, () -> restTemplate.getForObject(this.getItemUrl, ItemBoundary.class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), this.spaceName, this.testItemId));
	}

	@Test
	public void testGetAllItemsWhileDataBaseIsEmpty() {
		//	GIVEN the server is up (Automatically by SpringBootTest)
		//		AND manager or player exist.
		UserBoundary manager = createManager();
		
		// WHEN I GET /dts/items/{managerSpace}/{managerEmail}
		ItemBoundary[] itemsArray = this.restTemplate.getForObject(this.getAllItemsUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail());

		// THEN the server returns empty array
		assertThat(itemsArray.length).isEqualTo(0);
	}

	@Test
	public void testGetAllItemsWithFullDataBase() {
		// 	GIVEN the server is up (Automatically by SpringBootTest)
		//		AND manager user exist
		// 		AND database contains at least 1 item
		UserBoundary manager = createManager();
		
		int numOfItems = 10;
		createItemContent(numOfItems, "name", "type", manager.getUserId());
		
		// WHEN I GET /dts/items/{userSpace}/{userEmail}?size={defaultSize}&page={defaultPage}
		ItemBoundary[] itemsArray = this.restTemplate.getForObject(this.getAllItemsUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail());

		// THEN the server returns an array with first page of all the items in the system
		if(numOfItems <= Integer.parseInt(Constants.DEFAULT_PAGE_SIZE)) {
			assertThat(itemsArray).hasSize(numOfItems);
		} else {
			assertThat(itemsArray).hasSize(Integer.parseInt(Constants.DEFAULT_PAGE_SIZE));
		}
	}

	@Test
	public void testBindChildItemToParentItemWhileParentItemExist() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		UserBoundary manager = createManager();
		
		ItemBoundary parentItem = createItem(manager, "parent", "someType");

		ItemBoundary childItem = createItem(manager, "child", "someType");

		// WHEN I PUT
		// /dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children
		// AND I GET all children at
		// /dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children
		this.restTemplate.put(this.bindItemUrl, childItem.getItemId(),
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentItem.getItemId().getSpace(), parentItem.getItemId().getId());

		ItemBoundary[] childrenArray = this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentItem.getItemId().getSpace(), parentItem.getItemId().getId());

		// THEN the result HTTP STATUS 2XX
		assertThat(childrenArray).hasSize(1);

		ItemBoundary fetchedChildItem = childrenArray[0];
		
		// solo checks - fetchedChildItem
		assertThat(fetchedChildItem.getItemId()).isNotNull();

		assertThat(fetchedChildItem.getCreatedTimestamp()).isNotNull();

		assertThat(fetchedChildItem.getCreatedBy()).isNotNull();

		assertThat(fetchedChildItem.getCreatedBy().getUserId()).isNotNull();

		assertThat(fetchedChildItem.getLocation()).isNotNull();

		// compare checks - parentItem vs childItem
		assertNotEquals(parentItem.getItemId().getId(), childItem.getItemId().getId());

		// compare checks - fetchedChildItem vs childItem
		assertThat(fetchedChildItem.getItemId().getSpace()).isEqualTo(childItem.getItemId().getSpace());

		assertThat(fetchedChildItem.getItemId().getId()).isEqualTo(childItem.getItemId().getId());

		assertThat(fetchedChildItem.getCreatedTimestamp().getTime())
				.isEqualTo(childItem.getCreatedTimestamp().getTime());

		assertThat(fetchedChildItem.getCreatedBy().getUserId()).extracting("space", "email").containsExactly(
				childItem.getCreatedBy().getUserId().getSpace(), childItem.getCreatedBy().getUserId().getEmail());

		assertThat(fetchedChildItem.getLocation()).extracting("lat", "lng")
				.containsExactly(childItem.getLocation().getLat(), childItem.getLocation().getLng());

		assertThat(fetchedChildItem).extracting("active", "itemAttributes").containsExactly(childItem.getActive(),
				childItem.getItemAttributes());
	}

	@Test
	public void testGetAllChildrensWhileDataBaseIsEmpty() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// 	AND manager exist
		UserBoundary manager = createManager();
		
		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children
		// THEN the server returns not found response
		assertThrows(NotFound.class, () -> this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), this.spaceName, this.testItemId));
	}

	@Test
	public void testGetAllChildrensWhileParentExistWithoutChildren() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// 	AND manager exist
		UserBoundary manager = createManager();
		
		ItemBoundary parentItem = createItem(manager, "parent", "Type");

		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children
		ItemBoundary[] childrenArray = this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentItem.getItemId().getSpace(), parentItem.getItemId().getId());

		// THEN the server returns empty array
		assertThat(childrenArray).hasSize(0);
	}

	@Test
	public void testGetAllChildrensWhileParentItemNotExist() {
		String notExistingId = "100000000";
		
		// GIVEN the server is up (Automatically by SpringBootTest)
		// 	AND manager exist
		UserBoundary manager = createManager();
		
		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children
		// THEN the server returns not found response
		assertThrows(NotFound.class, () -> this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), this.spaceName, notExistingId));
	}

	@Test
	public void testGetAllParentsWhileChildItemExistWithoutParents() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// 	AND manager exist
		UserBoundary manager = createManager();
		
		ItemBoundary childItem = createItem(manager, "child", "Type");

		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/parents
		ItemBoundary[] parentsArray = this.restTemplate.getForObject(this.getAllParentsUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), childItem.getItemId().getSpace(), childItem.getItemId().getId());

		// THEN the server returns empty array
		assertThat(parentsArray).hasSize(0);
	}

	@Test
	public void testGetAllParentsWhileDatabaseIsEmpty() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// 	AND manager exist
		UserBoundary manager = createManager();
		
		// WHEN I GET /dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/parents
		// THEN the server returns empty array
		assertThrows(NotFound.class, () -> this.restTemplate.getForObject(this.getAllParentsUrl, ItemBoundary[].class,
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), this.spaceName, this.testItemId));
	}
	
	private UserBoundary createManager() {
		//	create manager to create item.
		NewUserDetails newManagerDetails = new NewUserDetails("manager@gmail.com", UserRoleBoundary.MANAGER, "managerista", "-_-;");
		return this.restTemplate.postForObject(this.createUserUrl, newManagerDetails, UserBoundary.class);
	}
	
	private ItemBoundary createItem(UserBoundary manager) {
		return this.restTemplate.postForObject(this.createItemUrl,
				new ItemBoundary(null, this.testItemType, this.testItemName, this.testItemActive, null, 
						new User(manager.getUserId()),
						new LocationBoundary(this.testItemLat, this.testItemLng), null),
				ItemBoundary.class, manager.getUserId().getSpace(), manager.getUserId().getEmail());
	}
	
	private ItemBoundary createItem(UserBoundary manager, String itemName, String itemType) {
		return this.restTemplate.postForObject(this.createItemUrl,
				new ItemBoundary(null, itemType, itemName, this.testItemActive, null, 
						new User(manager.getUserId()),
						new LocationBoundary(this.testItemLat, this.testItemLng), null),
				ItemBoundary.class, manager.getUserId().getSpace(), manager.getUserId().getEmail());
	}
	
	private List<ItemBoundary> createItemContent(int numOfItems, String baseName, String baseType, UserIdBoundary creatorId) {
		return IntStream.range(0, numOfItems)
				.mapToObj(i -> new ItemBoundary(new ItemIdBoundary(spaceName, "" + i), baseType + " #" + i, baseName + " #" + i, true, 
						new Date(), new User(creatorId), new LocationBoundary(1.0, 1.0), null))
				.map(boundary -> this.restTemplate.postForObject(this.createItemUrl, boundary, ItemBoundary.class, creatorId.getSpace(), creatorId.getEmail()))
				.collect(Collectors.toList());
	}
}