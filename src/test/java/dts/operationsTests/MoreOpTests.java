package dts.operationsTests;

import static org.assertj.core.api.Assertions.assertThat;

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
import dts.util.Swap;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MoreOpTests {

	private int port;
	
	private String invokeOperationUrl;
	private String createUserUrl;
	private String bindItemUrl;
	private String getAllChildrenUrl;
	
	private String createItemUrl;
	
	private String deleteItemsUrl;
	private String deleteUsersUrl;
	private String deleteOperationsUrl;
	
	private String spaceName;
	
	private RestTemplate restTemplate;
	
	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void init() {
		this.invokeOperationUrl = "http://localhost:" + this.port + "/dts/operations";
		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
	
		this.invokeOperationUrl = "http://localhost:" + this.port + "/dts/operations";
		this.getAllChildrenUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/{itemSpace}/{itemId}/children";
		this.bindItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}/{itemSpace}/{itemId}/children";
		this.deleteItemsUrl = "http://localhost:" + this.port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		this.deleteUsersUrl = "http://localhost:" + this.port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.deleteOperationsUrl = "http://localhost:" + this.port + "/dts/admin/operations/{adminSpace}/{adminEmail}";

		this.createItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}";
		
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

//	// cleanup the database after each test
//	@AfterEach
//	public void tearDown() {
//		this.restTemplate.delete(this.deleteItemsUrl, this.spaceName, "admin@gmail.com");
//		this.restTemplate.delete(this.deleteUsersUrl, this.spaceName, "admin@gmail.com");
//		this.restTemplate.delete(this.deleteOperationsUrl, this.spaceName, "admin@gmail.com");
//	}
	
	@Test
	public void testSwapExistingItemBetweenTwoExistingUsersAndBindToItem() {
		// create 2 players.
		UserBoundary fromUser = createPlayer("from@gmail.com", "fromPlayer");
		UserBoundary toUser = createPlayer("to@gmail.com", "toPlayer");
		
		// create book with operation
		Map<String, Object> createNewBookoperationAttr = new HashMap<>();
		createNewBookoperationAttr.put("bookName", "Harry Potter");
		createNewBookoperationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		Map<String, Object> bookAttr = new HashMap<>();
		bookAttr.put("author", "J. K. Rowlings");
		bookAttr.put("title", "Harry Potter and The Prisoner From Azkhaban");
		createNewBookoperationAttr.put("bookAttributes", bookAttr);
		
		OperationBoundary newBookOperation = new OperationBoundary("createNewBook", new User(fromUser.getUserId()), createNewBookoperationAttr);
		ItemBoundary newBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		
		// assert that book has fromUser
		assertThat(((UserIdBoundary) Functions.convertLinkedTreeToClass(newBook.getItemAttributes().get("owner"), UserIdBoundary.class)).toString()).isEqualTo(fromUser.getUserId().toString());
		
		// swap book with operation
		Map<String, Object> swapBookOperationAttributes = new HashMap<>();
		LocationBoundary toUserLocation = new LocationBoundary(30.9, 25.1);
		Swap rawSwap = new Swap(newBook.getItemId(), fromUser.getUserId(), toUser.getUserId(), toUserLocation);
		swapBookOperationAttributes.put("swap", rawSwap);
		
		OperationBoundary swapOperation = new OperationBoundary("swapBook", new User(fromUser.getUserId()), new Item(newBook.getItemId()), swapBookOperationAttributes);
		ItemBoundary swapItemBoundary = this.restTemplate.postForObject(this.invokeOperationUrl, swapOperation, ItemBoundary.class);
		
		// assert is not null
		assertThat(swapItemBoundary).isNotNull();
		System.err.println(swapItemBoundary.toString());
		
		// get all children of item
		ItemBoundary[] childrenArray = this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				fromUser.getUserId().getSpace(), fromUser.getUserId().getEmail(), newBook.getItemId().getSpace(), newBook.getItemId().getId());
		
		// assert there is 1 child and its type is swap and the owner is toUserId.getEmail()
		assertThat(childrenArray).hasSize(1);
		assertThat(childrenArray[0].getType()).isEqualTo("swap");
		
//		ItemBoundary parentItem = this.restTemplate.getForObject(this.getItemUrl, ItemBoundary.class, fromUser.getUserId().getSpace(),
//				fromUser.getUserId().getEmail(), newBook.getItemId().getSpace(), newBook.getItemId().getId());
//		
//		assertThat(((UserIdBoundary) Functions.convertLinkedTreeToClass(parentItem.getItemAttributes().get("owner"), UserIdBoundary.class)).getEmail()).isEqualTo(toUser.getUserId().getEmail());
		
	}

	@Test
	public void testBindWhileCreatingBooksWithOperations() {
		UserBoundary player = createPlayer("play@gmail.com", "playlay");
		UserBoundary manager = createManager(); // only manager can use bind function out of designated operation.
		
		// create book with operation
		Map<String, Object> createNewBookoperationAttr = new HashMap<>();
		createNewBookoperationAttr.put("bookName", "Harry Potter");
		createNewBookoperationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		Map<String, Object> bookAttr = new HashMap<>();
		bookAttr.put("author", "J. K. Rowlings");
		bookAttr.put("title", "Harry Potter and The Prisoner From Azkhaban");
		createNewBookoperationAttr.put("bookAttributes", bookAttr);
		
		OperationBoundary newBookOperation = new OperationBoundary("createNewBook", new User(player.getUserId()), createNewBookoperationAttr);
		ItemBoundary parentBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		
		ItemBoundary childBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		
		this.restTemplate.put(this.bindItemUrl, childBook.getItemId(),
				manager.getUserId().getSpace(), manager.getUserId().getEmail(), parentBook.getItemId().getSpace(), parentBook.getItemId().getId());

		ItemBoundary[] childrenArray = this.restTemplate.getForObject(this.getAllChildrenUrl, ItemBoundary[].class,
				player.getUserId().getSpace(), player.getUserId().getEmail(), parentBook.getItemId().getSpace(), parentBook.getItemId().getId());
		
		assertThat(childrenArray).hasSize(1);
	}
	
	@Test
	public void testFindAllBookInDistanceOperation() { 
		UserBoundary player = createPlayer("player@gmail.com", "player");
		UserBoundary manager = createManager();
		
		// create in a dumb way 10 books. (half active, half inactive && scaling location).
		for(int i = 0; i < 10; i++) {
			createItem(manager, "item #"+(i+1), "book", (i%2==0), 3.0*(i+1), 5.0*(i+1));
		}
		
		double distance = 10.0;
		double lat = 24.2;
		double lng = 25.3;
		
		OperationBoundary operation = new OperationBoundary();
		operation.setType("findBooksInDistance");
		operation.addAttribute("distance", distance);
		operation.addAttribute("myLocation", new LocationBoundary(lat, lng));
		operation.addAttribute("pageSize", 20);
		operation.addAttribute("pageOffset", 0);
		operation.setInvokedBy(new User(player.getUserId()));
		
		ItemBoundary[] booksNearby = this.restTemplate.postForObject(this.invokeOperationUrl, operation, ItemBoundary[].class);
		
		assertThat(booksNearby).isNotNull();
		if(distance == 10.0 && lat == 24.2 && lng == 25.3) {
			assertThat(booksNearby).hasSize(2); // calculated by specific data... not dynamic. (distance=10.0, lat=24.2, lng=25.3)
		}
		assertThat(booksNearby).allMatch(item -> item.getActive() == true); // all items are active
		assertThat(booksNearby).allMatch(item -> item.getType().equals("book")); // all items are books
		assertThat(booksNearby).allMatch(item -> item.getLocation().getLat() >= lat - distance); // all books are within distance.
		assertThat(booksNearby).allMatch(item -> item.getLocation().getLat() <= lat + distance);
		assertThat(booksNearby).allMatch(item -> item.getLocation().getLng() >= lng - distance);
		assertThat(booksNearby).allMatch(item -> item.getLocation().getLng() <= lng + distance);
	}
	
	@Test
	public void testGetAllUserItemsOperation() {
		UserBoundary player = createPlayer("player@gmail.com", "playyyyy");
		
		// create book with operation
		Map<String, Object> createNewBookoperationAttr = new HashMap<>();
		createNewBookoperationAttr.put("bookName", "Harry Potter");
		createNewBookoperationAttr.put("bookLocation", new LocationBoundary(10.0, 15.2));
		Map<String, Object> bookAttr = new HashMap<>();
		bookAttr.put("author", "J. K. Rowlings");
		bookAttr.put("title", "Harry Potter and The Prisoner From Azkhaban");
		createNewBookoperationAttr.put("bookAttributes", bookAttr);
		OperationBoundary newBookOperation = new OperationBoundary("createNewBook", new User(player.getUserId()), createNewBookoperationAttr);

		ItemBoundary firstBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		ItemBoundary secondBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		ItemBoundary thirdBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		ItemBoundary fourthBook = this.restTemplate.postForObject(this.invokeOperationUrl, newBookOperation, ItemBoundary.class);
		
		OperationBoundary getAllUserBooksOperation = new OperationBoundary();
		getAllUserBooksOperation.setType("getAllUserBooks");
		getAllUserBooksOperation.setInvokedBy(new User(player.getUserId()));
		getAllUserBooksOperation.addAttribute("owner", player.getUserId());
		ItemBoundary[] myBooks = this.restTemplate.postForObject(this.invokeOperationUrl, getAllUserBooksOperation, ItemBoundary[].class);
		
		assertThat(myBooks).isNotNull();
		assertThat(myBooks).hasSize(4);
		assertThat(myBooks).allMatch(item -> item.getCreatedBy().getUserId().getEmail().equals(player.getUserId().getEmail()));
		assertThat(myBooks).allMatch(item -> item.getType().equals("book"));
	}
	
	private UserBoundary createPlayer(String email, String username) {
		//	create player user.
		NewUserDetails newPlayerDetails = new NewUserDetails(email, UserRoleBoundary.PLAYER, username, "XD");
		return this.restTemplate.postForObject(this.createUserUrl, newPlayerDetails, UserBoundary.class);
	}
	
	private UserBoundary createManager() {
		//	create manager to create item.
		NewUserDetails newManagerDetails = new NewUserDetails("manager@gmail.com", UserRoleBoundary.MANAGER, "managerista", "-_-;");
		return this.restTemplate.postForObject(this.createUserUrl, newManagerDetails, UserBoundary.class);
	}
	
	private ItemBoundary createItem(UserBoundary manager, String itemName, String itemType, boolean active, double lat, double lng) {
		return this.restTemplate.postForObject(this.createItemUrl,
				new ItemBoundary(null, itemType, itemName, active, null, 
						new User(manager.getUserId()),
						new LocationBoundary(lat, lng), null),
				ItemBoundary.class, manager.getUserId().getSpace(), manager.getUserId().getEmail());
	}
	
}
