package dts.complexTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpClientErrorException.MethodNotAllowed;

import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.Item;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;
import dts.util.Functions;

//@RunWith(SpringRunner.class)
//@TestPropertySource("/tests.properties")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // causes the server to go up
public class ComplexTests {	
	private String createUserUrl;
	
	private String createItemUrl;
	private String deleteItemsUrl;
	
	private String getAllItemsByNameUrl;
	private String getAllItemsByTypeUrl;
	private String getAllItemsByLocationUrl;
	
	private String invokeOperationUrl;
	
	private String deleteAllUsersUrl;
	private String deleteAllItemsUrl;
	private String deleteAllOperationsUrl;

	private String testAdminMail;
	
	private int port;
	
	private String spaceName;

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
		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
		
		this.createItemUrl = "http://localhost:" + this.port + "/dts/items/{managerSpace}/{managerEmail}";
		this.deleteItemsUrl = "http://localhost:" + this.port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		
		this.getAllItemsByNameUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/byNamePattern/{namePattern}";
		this.getAllItemsByTypeUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/byType/{type}";
		this.getAllItemsByLocationUrl = "http://localhost:" + this.port + "/dts/items/{userSpace}/{userEmail}/search/near/{lat}/{lng}/{distance}";

		this.invokeOperationUrl = "http://localhost:" + this.port + "/dts/operations";

		this.deleteAllUsersUrl = "http://localhost:" + port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.deleteAllItemsUrl = "http://localhost:" + port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		this.deleteAllOperationsUrl = "http://localhost:" + port + "/dts/admin/operations/{adminSpace}/{adminEmail}";
		
		this.testAdminMail = "admin@test.com";
		
		this.restTemplate = new RestTemplate();
	}
	
	// cleanup the database before each test
	@BeforeEach
	public void setup() {
		this.restTemplate.delete(this.deleteAllUsersUrl, this.spaceName, this.testAdminMail);
		this.restTemplate.delete(this.deleteAllItemsUrl, this.spaceName, this.testAdminMail);
		this.restTemplate.delete(this.deleteAllOperationsUrl, this.spaceName, this.testAdminMail);

	}

	// cleanup the database after each test
	@AfterEach
	public void tearDown() {
		this.restTemplate.delete(this.deleteAllUsersUrl, this.spaceName, this.testAdminMail);
		this.restTemplate.delete(this.deleteAllItemsUrl, this.spaceName, this.testAdminMail);
		this.restTemplate.delete(this.deleteAllOperationsUrl, this.spaceName, this.testAdminMail);
	}
	
	@Test
	public void contextLoads() {

	}
	
	@Test
	public void heavyLoad() {
		int numOfItems = 4;
		
		String firstManagerMail = "first@manager.com";
		String firstManagerName = "First Manager Name";
		String firstManagerAvatar = "-_-;";
		
		String secondManagerMail = "second@manager.com";
		String secondManagerName = "Second Manager Name";
		String secondManagerAvatar = "'-_-";

//		create userItemCreator with manager role
		UserBoundary firstManager = createManager(firstManagerMail, firstManagerName, firstManagerAvatar);
		UserBoundary secondManager = createManager(secondManagerMail, secondManagerName, secondManagerAvatar);
		
//		create #numOfItems items in DB
		List<ItemBoundary> firstDBContent = createItemsContentAsManager(numOfItems, "Harry Potter", 1.0, 1.0, firstManager.getUserId());
		List<ItemBoundary> secondDBContent = createItemsContentAsManager(numOfItems, "A Song of Ice and Fire", 100.0, 100.0, secondManager.getUserId());
		
		String firstPlayerMail = "first@player.com";
		String firstPlayerName = "First Player Name";
		String firstPlayerAvatar = "XD";
		
		String secondPlayerMail = "second@player.com";
		String secondPlayerName = "Second Player Name";
		String secondPlayerAvatar = "d:";

//		create userItemCreator with manager role
		UserBoundary firstPlayer = createPlayer(firstPlayerMail, firstPlayerName, firstPlayerAvatar);
		UserBoundary secondPlayer = createPlayer(secondPlayerMail, secondPlayerName, secondPlayerAvatar);
		
//		create books in DB
		ItemBoundary firstPlayerFirstBook = createBookContentAsPlayer("Elantris", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerSecondBook = createBookContentAsPlayer("The Final Empire", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerThirdBook = createBookContentAsPlayer("The Well of Ascension", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerFourthBook = createBookContentAsPlayer("The Hero of Ages", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerFifthBook = createBookContentAsPlayer("Warbreaker", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerSixthBook = createBookContentAsPlayer("The Way of Kings", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerSeventhBook = createBookContentAsPlayer("The Alloy of Law", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerEighthBook = createBookContentAsPlayer("Words of Radiance", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerNinthBook = createBookContentAsPlayer("Shadows of Self", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerTenthBook = createBookContentAsPlayer("The Bands of Mourning", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerEleventhBook = createBookContentAsPlayer("Oathbringer", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		ItemBoundary firstPlayerTwelfthBook = createBookContentAsPlayer("Rhythm of War", 1000.0, 1000.0, "Brandon Sanderson", firstPlayer);
		
		ItemBoundary secondPlayerFirstBook = createBookContentAsPlayer("The Crystal Shard", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayertSecondBook = createBookContentAsPlayer("Streams of Silver", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerThirdBook = createBookContentAsPlayer("The Halfling's Gem", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerFourthBook = createBookContentAsPlayer("Homeland", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerFifthBook = createBookContentAsPlayer("Exile", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerSixthBook = createBookContentAsPlayer("Sojourn", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerSeventhBook = createBookContentAsPlayer("The Legacy", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerEighthBook = createBookContentAsPlayer("Starless Night", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerNinthBook = createBookContentAsPlayer("Siege of Darkness", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		ItemBoundary secondPlayerTenthBook = createBookContentAsPlayer("Passage to Dawn", 2000.0, 2000.0, "R. A. Salvatore", secondPlayer);
		
		// asserts 1 - unauthorized creations
		assertThrows(Forbidden.class, () -> createBookContentAsPlayer("The Hunger Games", 1.0, 1.0, "Suzanne Collins", firstManager));
		assertThrows(Forbidden.class, () -> createBookContentAsPlayer("Catching Fire", 1.0, 1.0, "Suzanne Collins", secondManager));
		assertThrows(Forbidden.class, () -> createItemsContentAsManager(1, "Mockingjay - Part 1", 1.0, 1.0, firstPlayer.getUserId()));
		assertThrows(Forbidden.class, () -> createItemsContentAsManager(1, "Mockingjay - Part 2", 1.0, 1.0, secondPlayer.getUserId()));

		// delete books in DB
		ItemBoundary firstPlayerTwelfthBookDeleted = deleteItemContentAsPlayer(firstPlayerTwelfthBook, firstPlayer);
		ItemBoundary firstPlayerEleventhBookDeleted = deleteItemContentAsPlayer(firstPlayerEleventhBook, firstPlayer);
		ItemBoundary firstPlayerTenthBookDeleted = deleteItemContentAsPlayer(firstPlayerTenthBook, firstPlayer);
		ItemBoundary secondPlayerTenthBookDeleted = deleteItemContentAsPlayer(secondPlayerTenthBook, secondPlayer);

		// asserts 2 - authorized deletions
		assertThat(firstPlayerTwelfthBookDeleted).isEqualTo(firstPlayerTwelfthBook);
		assertThat(firstPlayerEleventhBookDeleted).isEqualTo(firstPlayerEleventhBook);
		assertThat(firstPlayerTenthBookDeleted).isEqualTo(firstPlayerTenthBook);
		assertThat(secondPlayerTenthBookDeleted).isEqualTo(secondPlayerTenthBook);
		assertThat(firstPlayerTwelfthBookDeleted).isNotEqualTo(secondPlayerTenthBook);
		assertThat(firstPlayerEleventhBookDeleted).isNotEqualTo(firstPlayerTenthBook);
		assertThat(firstPlayerTenthBookDeleted).isNotEqualTo(firstPlayerEleventhBook);
		assertThat(secondPlayerTenthBookDeleted).isNotEqualTo(firstPlayerTwelfthBook);


		// asserts 3 - unauthorized deletions
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(firstPlayerFirstBook, firstPlayer.getUserId()));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(secondPlayerFirstBook, secondPlayer.getUserId()));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(firstDBContent.get(0), firstManager));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(secondDBContent.get(0), secondManager));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(firstPlayerSecondBook, firstManager.getUserId()));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(secondPlayertSecondBook, secondManager.getUserId()));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(firstDBContent.get(1), firstPlayer));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(secondDBContent.get(1), secondPlayer));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(secondDBContent.get(2), firstManager.getUserId()));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(firstDBContent.get(2), secondManager.getUserId()));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(firstPlayerThirdBook, secondPlayer));
		assertThrows(Forbidden.class, () -> deleteItemContentAsPlayer(secondPlayerThirdBook, firstPlayer));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(firstDBContent.get(3), firstManager.getUserId()));
		assertThrows(MethodNotAllowed.class, () -> deleteItemContentAsManager(secondDBContent.get(3), secondManager.getUserId()));
		
		// swap books in db
		ItemBoundary firstPlayerNinthBookSwapped = swapItemContentAsPlayer(firstPlayerNinthBook, firstPlayer, secondPlayer.getUserId());
		ItemBoundary secondPlayerNinthBookSwapped = swapItemContentAsPlayer(secondPlayerNinthBook, secondPlayer, firstPlayer.getUserId());
		ItemBoundary firstPlayerEighthBookSwapped = swapItemContentAsPlayer(firstPlayerEighthBook, firstPlayer, firstManager.getUserId());
		ItemBoundary secondPlayerEighthBookSwapped = swapItemContentAsPlayer(secondPlayerEighthBook, secondPlayer, firstManager.getUserId());
		ItemBoundary firstPlayerSeventhBookSwapped = swapItemContentAsPlayer(firstPlayerSeventhBook, firstPlayer, secondManager.getUserId());
		ItemBoundary secondPlayerSeventhBookSwapped = swapItemContentAsPlayer(secondPlayerSeventhBook, secondPlayer, secondManager.getUserId());
		
		// asserts 4 - authorized swaps
		assertThat(firstPlayerNinthBookSwapped).isEqualTo(firstPlayerNinthBook);
		assertThat(secondPlayerNinthBookSwapped).isEqualTo(secondPlayerNinthBook);
		assertThat(firstPlayerEighthBookSwapped).isEqualTo(firstPlayerEighthBook);
		assertThat(secondPlayerEighthBookSwapped).isEqualTo(secondPlayerEighthBook);
		assertThat(firstPlayerSeventhBookSwapped).isEqualTo(firstPlayerSeventhBook);
		assertThat(secondPlayerSeventhBookSwapped).isEqualTo(secondPlayerSeventhBook);
		assertThat(firstPlayerNinthBookSwapped.getName()).isEqualTo(firstPlayerNinthBook.getName());
		assertThat(secondPlayerNinthBookSwapped.getName()).isEqualTo(secondPlayerNinthBook.getName());
		assertThat(firstPlayerEighthBookSwapped.getName()).isEqualTo(firstPlayerEighthBook.getName());
		assertThat(secondPlayerEighthBookSwapped.getName()).isEqualTo(secondPlayerEighthBook.getName());
		assertThat(firstPlayerSeventhBookSwapped.getName()).isEqualTo(firstPlayerSeventhBook.getName());
		assertThat(secondPlayerSeventhBookSwapped.getName()).isEqualTo(secondPlayerSeventhBook.getName());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(firstPlayerNinthBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(secondPlayer.getUserId());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(secondPlayerNinthBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(firstPlayer.getUserId());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(firstPlayerEighthBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(firstManager.getUserId());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(secondPlayerEighthBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(firstManager.getUserId());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(firstPlayerSeventhBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(secondManager.getUserId());
		assertThat((UserIdBoundary) Functions.convertLinkedTreeToClass(secondPlayerSeventhBookSwapped.getItemAttributes().get("owner"), UserIdBoundary.class)).isEqualTo(secondManager.getUserId());

		// asserts 5 - unauthorized swaps
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(firstPlayerSixthBook, secondPlayer, secondPlayer.getUserId()));
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(firstPlayerFifthBook, firstManager, firstManager.getUserId()));
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(firstPlayerFourthBook, secondManager, secondManager.getUserId()));
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(secondPlayerSixthBook, firstPlayer, firstPlayer.getUserId()));
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(secondPlayerFifthBook, firstManager, firstManager.getUserId()));
		assertThrows(Forbidden.class, () -> swapItemContentAsPlayer(secondPlayerFourthBook, secondManager, secondManager.getUserId()));
		
		// get by type per page
		int totalBooks = 30;
		int totalActiveBooks = 26;
		int pageSize = 20;
		
//		WHEN I GET "/dts/items/{userSpace}/{userEmail}/search/byType/{type}?size=20&page=0"
		ItemBoundary[] firstManagerResponse1 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), "book", pageSize, 0);
		ItemBoundary[] firstManagerResponse2 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), "book", pageSize, 1);
		ItemBoundary[] secondManagerResponse1 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), "otherType", pageSize, 0);
		ItemBoundary[] secondManagerResponse2 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), "otherType", pageSize, 1);
		ItemBoundary[] firstPlayerResponse1 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), "book", pageSize, 0);
		ItemBoundary[] firstPlayerResponse2 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), "book", pageSize, 1);
		ItemBoundary[] secondPlayerResponse1 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), "otherType", pageSize, 0);
		ItemBoundary[] secondPlayerResponse2 = this.restTemplate
				.getForObject(this.getAllItemsByTypeUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), "otherType", pageSize, 1);


		// asserts 6 - page size by type
		assertThat(firstManagerResponse1).hasSize(pageSize);
		assertThat(firstManagerResponse2).hasSize(totalBooks-pageSize);
		assertThat(secondManagerResponse1).hasSize(0);
		assertThat(secondManagerResponse2).hasSize(0);
		assertThat(firstPlayerResponse1).hasSize(pageSize);
		assertThat(firstPlayerResponse2).hasSize(totalActiveBooks-pageSize);
		assertThat(secondPlayerResponse1).hasSize(0);
		assertThat(secondPlayerResponse2).hasSize(0);
		
		// get by location per page
		ItemBoundary[] firstManagerResponse3 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), 1.0, 1.0, 0.0, pageSize, 0);
		ItemBoundary[] firstManagerResponse4 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), 1.0, 1.0, 1.0, pageSize, 0);
		ItemBoundary[] secondManagerResponse3 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), 100.0, 100.0, 2.0, pageSize, 0);
		ItemBoundary[] secondManagerResponse4 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), 100.0, 100.0, 3.0, pageSize, 0);
		ItemBoundary[] firstPlayerResponse3 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), 1000.0, 1000.0, 0.0, pageSize, 0);
		ItemBoundary[] firstPlayerResponse4 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), 1000.0, 1000.0, 0.0, pageSize, 1);
		ItemBoundary[] secondPlayerResponse3 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), 2000.0, 2000.0, 10.0, pageSize, 0);
		ItemBoundary[] secondPlayerResponse4 = this.restTemplate
				.getForObject(this.getAllItemsByLocationUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), 2000.0, 2000.0, 10.0, pageSize, 1);

		
		// asserts 6 - page size by location
		assertThat(firstManagerResponse3).hasSize(1);
		assertThat(firstManagerResponse4).hasSize(2);
		assertThat(secondManagerResponse3).hasSize(1);
		assertThat(secondManagerResponse4).hasSize(1);
		assertThat(firstPlayerResponse3).hasSize(9);
		assertThat(firstPlayerResponse4).hasSize(0);
		assertThat(secondPlayerResponse3).hasSize(9);
		assertThat(secondPlayerResponse4).hasSize(0);

		// get by name per page
		ItemBoundary[] firstManagerResponse5 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), "Harry Potter", pageSize, 0);
		ItemBoundary[] firstManagerResponse6 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, firstManager.getUserId().getSpace(), firstManager.getUserId().getEmail(), "Harry Potter", pageSize, 1);
		ItemBoundary[] secondManagerResponse5 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), "A Song of Ice and Fire", pageSize, 0);
		ItemBoundary[] secondManagerResponse6 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, secondManager.getUserId().getSpace(), secondManager.getUserId().getEmail(), "A Song of Ice and Fire", pageSize, 1);
		ItemBoundary[] firstPlayerResponse5 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), "#1", pageSize, 0);
		ItemBoundary[] firstPlayerResponse6 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, firstPlayer.getUserId().getSpace(), firstPlayer.getUserId().getEmail(), "#2", pageSize, 0);
		ItemBoundary[] secondPlayerResponse5 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), "Elantris", pageSize, 0);
		ItemBoundary[] secondPlayerResponse6 = this.restTemplate
				.getForObject(this.getAllItemsByNameUrl + "?size={size}&page={page}", ItemBoundary[].class, secondPlayer.getUserId().getSpace(), secondPlayer.getUserId().getEmail(), "The Crystal Shard", pageSize, 0);

		// asserts 6 - page size by name
		assertThat(firstManagerResponse5).hasSize(numOfItems);
		assertThat(firstManagerResponse6).hasSize(0);
		assertThat(secondManagerResponse5).hasSize(numOfItems);
		assertThat(secondManagerResponse6).hasSize(0);
		assertThat(firstPlayerResponse5).hasSize(2);
		assertThat(firstPlayerResponse6).hasSize(2);
		assertThat(secondPlayerResponse5).hasSize(1);
		assertThat(secondPlayerResponse6).hasSize(1);

	}
	
	
	private UserBoundary createManager(String managerMail, String managerName, String managerAvatar) {
		//	create manager
		NewUserDetails newManagerDetails = new NewUserDetails(managerMail, UserRoleBoundary.MANAGER, managerName, managerAvatar);
		return this.restTemplate.postForObject(this.createUserUrl, newManagerDetails, UserBoundary.class);
	}
	
	private UserBoundary createPlayer(String playerMail, String playerrName, String playerAvatar) {
		//	create player
		NewUserDetails newPlayerDetails = new NewUserDetails(playerMail, UserRoleBoundary.PLAYER, playerrName, playerAvatar);
		return this.restTemplate.postForObject(this.createUserUrl, newPlayerDetails, UserBoundary.class);
	}

	private List<ItemBoundary> createItemsContentAsManager(int numOfItems, String baseName, Double baseLat, Double baseLng, UserIdBoundary creatorId) {
		return IntStream.range(0, numOfItems)
				.mapToObj(i -> new ItemBoundary(new ItemIdBoundary(spaceName, "" + i), "book", baseName + " #" + i, true, 
						new Date(), new User(creatorId), new LocationBoundary(baseLat * (i+1), baseLng * (i+1)), new HashMap<String, Object>(){{
						    put("author", "R. A. Salvatore");
						    put("offering", true);
						    put("owner", creatorId);
						}}))
				.map(boundary -> this.restTemplate.postForObject(this.createItemUrl, boundary, ItemBoundary.class, creatorId.getSpace(), creatorId.getEmail()))
				.collect(Collectors.toList());
	}
	
	private ItemBoundary createBookContentAsPlayer(String bookName, Double bookLat, Double bookLng, String bookAuthor, UserBoundary creator) {
		Map<String, Object> createBookOperationAttr = new HashMap<>();
		createBookOperationAttr.put("bookName", bookName);
		createBookOperationAttr.put("bookLocation", new LocationBoundary(bookLat, bookLng));
		createBookOperationAttr.put("bookAttributes", Collections.singletonMap("author", bookAuthor));
		createBookOperationAttr.put("owner", creator.getUserId());
		
		OperationBoundary createItemOperation = new OperationBoundary("createNewBook", new User(creator.getUserId()), createBookOperationAttr);
		ItemBoundary item = this.restTemplate.postForObject(this.invokeOperationUrl, createItemOperation, ItemBoundary.class);
		return item;
	}
	
	private ItemBoundary deleteItemContentAsManager(ItemBoundary itemToDelete, UserIdBoundary deletorId) {
		ItemBoundary item =  this.restTemplate.postForObject(this.deleteItemsUrl, itemToDelete, ItemBoundary.class, deletorId.getSpace(), deletorId.getEmail());
		return item;
	}
	
	private ItemBoundary deleteItemContentAsPlayer(ItemBoundary itemToDelete, UserBoundary deletor) {
		OperationBoundary deleteItemOperation= new OperationBoundary("deleteBook", new User(deletor.getUserId()), new Item(itemToDelete.getItemId()), null);
		ItemBoundary item =  this.restTemplate.postForObject(this.invokeOperationUrl, deleteItemOperation, ItemBoundary.class);
		return item;
	}
	
	private ItemBoundary swapItemContentAsPlayer(ItemBoundary itemToSwap, UserBoundary swapper, UserIdBoundary otherUserId) {
		OperationBoundary swapPossesionOperation = new OperationBoundary("swapBookPossesion", new User(swapper.getUserId()), 
				new Item(itemToSwap.getItemId()), Collections.singletonMap("newOwnerUserId", otherUserId));
		ItemBoundary item = this.restTemplate.postForObject(this.invokeOperationUrl, swapPossesionOperation, ItemBoundary.class);
		return item;
	}
}
