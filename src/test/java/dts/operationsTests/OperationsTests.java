package dts.operationsTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.RestTemplate;

import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.Item;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;
import dts.util.Functions;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // causes the server to go up
public class OperationsTests {
	
	private int port;
	
	private String createUserUrl;
	private String deleteUsersUrl;
	private String loginUserUrl;
	
	private String createItemUrl;
	private String deleteItemsUrl;
	
	private String invokeOperationUrl;
	private String deleteOperationsUrl;
	
	private String spaceName;
	
	private RestTemplate restTemplate;
	
	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void init() {
		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
		this.deleteUsersUrl = "http://localhost:" + this.port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.loginUserUrl = "http://localhost:" + this.port + "/dts/users/login/{userSpace}/{userEmail}";

		this.createItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}";
		this.deleteItemsUrl = "http://localhost:" + this.port + "/dts/admin/items/{adminSpace}/{adminEmail}";

		this.invokeOperationUrl = "http://localhost:" + this.port + "/dts/operations";
		this.deleteOperationsUrl = "http://localhost:" + this.port + "/dts/admin/operations/{adminSpace}/{adminEmail}";
		
		this.restTemplate = new RestTemplate();
	}

	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}
	
	// cleanup the database before each test 
	@BeforeEach
	public void setup() {
		this.restTemplate.delete(this.deleteItemsUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteUsersUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteOperationsUrl, this.spaceName, "admin@gmail.com");
	}

	// cleanup the database after each test
	@AfterEach
	public void tearDown() {
		this.restTemplate.delete(this.deleteItemsUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteUsersUrl, this.spaceName, "admin@gmail.com");
		this.restTemplate.delete(this.deleteOperationsUrl, this.spaceName, "admin@gmail.com");
	}
	
	@Test
	public void testCreateItemOperationWithPlayerRole() {
		// create player user
		UserBoundary player = createPlayer();

		// create operation
		Map<String, Object> operationAttr = new HashMap<>();
		operationAttr.put("bookName", "Harry Potter");
		operationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		operationAttr.put("bookAttributes", Collections.singletonMap("author", "J. K. Rolling"));
		operationAttr.put("owner", player.getUserId());
		
		// invoke operation
		OperationBoundary operation = new OperationBoundary("createNewBook", new User(player.getUserId()), operationAttr);
		ItemBoundary newItem = this.restTemplate.postForObject(this.invokeOperationUrl, operation, ItemBoundary.class);
		
		// assert for item details
		assertThat(newItem).isNotNull();
		assertThat(newItem.getItemId().getId()).isNotNull();
		
//		check user is back to PLAYER role by trying to create new item from service. (throwing forbidden)
		UserBoundary existingUser = this.restTemplate.getForObject(this.loginUserUrl, UserBoundary.class, player.getUserId().getSpace(), player.getUserId().getEmail());
	
		ItemBoundary testNewItem = new ItemBoundary(null, "type", "name", true, null, 
				new User(player.getUserId()), null, null);
		
		// validate that user role is back to PLAYER role.
		assertThrows(Forbidden.class, () -> this.restTemplate.postForObject(this.createItemUrl, testNewItem, ItemBoundary.class, existingUser.getUserId().getSpace(), existingUser.getUserId().getEmail()));
	}
	
	@Test
	public void testDeleteBookOperationWithPlayerRole() {
		// create player user
		UserBoundary player = createPlayer();
		
		// create createNewBook operation
		Map<String, Object> operationAttr = new HashMap<>();
		operationAttr.put("bookName", "Harry Potter");
		operationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		operationAttr.put("bookAttributes", Collections.singletonMap("author", "J. K. Rolling"));
		operationAttr.put("owner", player.getUserId());
		
		OperationBoundary createItemOperation = new OperationBoundary("createNewBook", new User(player.getUserId()), operationAttr);
		ItemBoundary newItem = this.restTemplate.postForObject(this.invokeOperationUrl, createItemOperation, ItemBoundary.class);
				
		assertThat(newItem).extracting("active").isEqualTo(true);
		
//		update the item to inactive with operation
		OperationBoundary deleteItemOperation = new OperationBoundary("deleteBook", new User(player.getUserId()), new Item(newItem.getItemId()), null);
		ItemBoundary deletedItem = this.restTemplate.postForObject(this.invokeOperationUrl, deleteItemOperation, ItemBoundary.class);
		
		assertThat(deletedItem).extracting("active").isEqualTo(false);
	}
	
	@Test
	public void testSwapItemPossesionOperationWithPlayerRole() {
		UserBoundary player = createPlayer();
		
		// create createNewBook operation
		Map<String, Object> createBookOperationAttr = new HashMap<>();
		createBookOperationAttr.put("bookName", "Harry Potter");
		createBookOperationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		createBookOperationAttr.put("bookAttributes", Collections.singletonMap("author", "J. K. Rolling"));
		createBookOperationAttr.put("owner", player.getUserId());
		
		OperationBoundary createItemOperation = new OperationBoundary("createNewBook", new User(player.getUserId()), createBookOperationAttr);
		ItemBoundary item = this.restTemplate.postForObject(this.invokeOperationUrl, createItemOperation, ItemBoundary.class);
		
		// create swapBookPossesion operation
		Map<String, Object> operationAttr = new HashMap<>();
		UserIdBoundary newOwnerId = new UserIdBoundary(this.spaceName, "newOwnerUserEmail@gmail.com");
		operationAttr.put("newOwnerUserId", newOwnerId);
		OperationBoundary swapPossesionOperation = new OperationBoundary("swapBookPossesion", new User(player.getUserId()), new Item(item.getItemId()), operationAttr);

//		swap owners
		ItemBoundary newOwnersItem = this.restTemplate.postForObject(this.invokeOperationUrl, swapPossesionOperation, ItemBoundary.class);
		
//		assert that returned item owner attribute is equal to the new owner id.
		assertThat(((UserIdBoundary) Functions.convertLinkedTreeToClass(newOwnersItem.getItemAttributes().get("owner"), UserIdBoundary.class)).toString()).isEqualTo(newOwnerId.toString());
//		assertThat(newOwnersItem.getItemAttributes().get("owner")).isEqualTo(newOwnerId.toString());

	}
	
	@Test
	public void testToggleOfferingItemStatusOperationWithPlayerRole() {
		UserBoundary player = createPlayer();
		
		// create createNewBook operation
		Map<String, Object> createBookOperationAttr = new HashMap<>();
		createBookOperationAttr.put("bookName", "Harry Potter");
		createBookOperationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		createBookOperationAttr.put("bookAttributes", Collections.singletonMap("author", "J. K. Rolling"));
		createBookOperationAttr.put("owner", player.getUserId());
		
		OperationBoundary createItemOperation = new OperationBoundary("createNewBook", new User(player.getUserId()), createBookOperationAttr);
		ItemBoundary item = this.restTemplate.postForObject(this.invokeOperationUrl, createItemOperation, ItemBoundary.class);

//		assert that on creation offering is true
		assertThat((boolean) item.getItemAttributes().get("offering")).isEqualTo(true);
		
//		toggleOffering has null operationAttributes
		OperationBoundary operation = new OperationBoundary("toggleOffering", new User(player.getUserId()), new Item(item.getItemId()), null);
		
		ItemBoundary updatedItem = this.restTemplate.postForObject(this.invokeOperationUrl, operation, ItemBoundary.class);
		
//		assert that update exist and offering changed to false.
		assertThat(updatedItem).isNotNull();
		assertThat((boolean) updatedItem.getItemAttributes().get("offering")).isEqualTo(false);
		
	}
	
	private UserBoundary createPlayer() {
		//	create player user.
		NewUserDetails newPlayerDetails = new NewUserDetails("player@gmail.com", UserRoleBoundary.PLAYER, "playerina", "XD");
		return this.restTemplate.postForObject(this.createUserUrl, newPlayerDetails, UserBoundary.class);
	}
}
