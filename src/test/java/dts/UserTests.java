package dts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import dts.logic.boundaries.NewUserDetails;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // causes the server to go up
public class UserTests {
	private int port;
	private String spaceName;
	private String testUserName;
	private String testUserMail;
	private String testUserAvatar;
	
	private String createUserUrl;
	private String deleteUserUrl;
	private String loginUserUrl;
	private String updateUserUrl;
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
		this.testUserName = "testUserName";
		this.testUserMail = "testUser@Mail.com";
		this.testUserAvatar = "testUserAvatar";

		this.createUserUrl = "http://localhost:" + this.port + "/dts/users";
		this.deleteUserUrl = "http://localhost:" + this.port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.loginUserUrl = "http://localhost:" + this.port + "/dts/users/login/{userSpace}/{userEmail}";
		this.updateUserUrl = "http://localhost:" + this.port + "/dts/users/{userSpace}/{userEmail}";
		this.restTemplate = new RestTemplate();
	}

	// cleanup the database before each test
	@BeforeEach
	public void setup() {
		this.restTemplate.delete(this.deleteUserUrl, this.spaceName, this.testUserMail);
	}

	// cleanup the database after each test
	@AfterEach
	public void tearDown() {
		this.restTemplate.delete(this.deleteUserUrl, this.spaceName, this.testUserMail);
	}

	@Test
	public void contextLoads() {

	}

	@Test
	public void testCreateUserWithEmptyDatabase() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// AND the database is empty

		// WHEN I POST at /dts/users
		UserBoundary actual = this.restTemplate.postForObject(this.createUserUrl,
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class);

		// THEN the result HTTP STATUS 2XX
		// AND all NewUser fields are not null
		// AND the server returns the JSON {“id”: “not null”}
		// AND the server contains a new entity
		
		// null checks
		assertThat(actual.getUserId()).isNotNull();

		assertThat(actual.getRole()).isNotNull();
		
		// compare checks
		assertThat(actual).extracting("username", "avatar").containsExactly(this.testUserName, this.testUserAvatar);
	}

	@Test
	public void testCreateUserWithValidInputAndInvalidUrl() {
		// GIVEN the server is up (automatically by SpringBootTest)
		// WHEN I POST at /dts/ (an invalid url)
		// THEN the result HTTP STATUS NOT 2XX
		assertThrows(NotFound.class, () -> this.restTemplate.postForObject("http://localhost:" + port + "/dts/",
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class));
	}

	@Test
	public void testCreateUserWhileUserExist() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// WHEN I POST at
		// And I POST again the same user at /dts/users/
		// THEN the result HTTP STATUS 2XX
		// And the server returns the JSON {“id”: “not null”}
		this.restTemplate.postForObject(this.createUserUrl,
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class);

		assertThrows(RuntimeException.class, () -> this.restTemplate.postForObject(this.createUserUrl,
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class));
	}

	@Test
	public void testLoginUserAndRecieveDetailsWhileUserExist() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// And the DB has a user in the url /dts/users/login/{userSpace}/{userEmail}
		@SuppressWarnings("unused")
		UserBoundary actual = this.restTemplate.postForObject(this.createUserUrl,
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class);

		// WHEN I GET /dts/users/login/{userSpace}/{userEmail}
		UserBoundary loggedin = this.restTemplate.getForObject(this.loginUserUrl, UserBoundary.class, this.spaceName,
				this.testUserMail);

		// THEN the result HTTP STATUS 2XX
		// And the system is logged in
		assertThat(loggedin.getUserId()).isNotNull();
	}

	@Test
	public void testLoginUserAndRecieveDetailsWhileUserNotExist() {
		// GIVEN the server is up (automatically by SpringBootTest)
		// WHEN I get /dts/users/login/{userSpace}/{userEmail} that does not exist
		// THEN the result is HTTP STATUS NOT 2XX
		assertThrows(NotFound.class, () -> this.restTemplate.getForObject(this.loginUserUrl, UserBoundary.class,
				this.spaceName, this.testUserMail));
	}

	@Test
	public void testLoginUserAndRecieveDetailsWithValidInputAndInvalidUrl() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// WHEN I get /dts/users/login/{userSpace}/{userEmail} (which is an invalid url)
		// THEN the result HTTP STATUS NOT 2XX
		assertThrows(NotFound.class,
				() -> this.restTemplate.getForObject("http://localhost:" + port + "/dts/users/login",
						UserBoundary.class, this.spaceName, this.testUserMail));

	}

	@Test
	public void testUpdateUserWhileUserExist() {
		UserRoleBoundary newRole = UserRoleBoundary.MANAGER;
		String newUserName = "newTestUserName";
		String newTestAvatar = "newTestUserAvatar";

		// GIVEN the server is up (Automatically by SpringBootTest)
		// And the DB has a user in the url /dts/users/login/{userSpace}/{userEmail}
		UserBoundary actual = this.restTemplate.postForObject(this.createUserUrl,
				new NewUserDetails(this.testUserMail, UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar),
				UserBoundary.class);

		// WHEN I GET /dts/users/login/{userSpace}/{userEmail}
		@SuppressWarnings("unused")
		UserBoundary loggedin = this.restTemplate.getForObject(this.loginUserUrl, UserBoundary.class, this.spaceName,
				this.testUserMail);

		// WHEN I PUT /dts/users/{userSpace}/{userEmail}
		this.restTemplate.put(this.updateUserUrl,
				new UserBoundary(new NewUserDetails(this.testUserMail, newRole, newUserName, newTestAvatar)),
				this.spaceName, this.testUserMail);

		UserBoundary updated = this.restTemplate.getForObject(this.loginUserUrl, UserBoundary.class, this.spaceName,
				this.testUserMail);

		// THEN the result HTTP STATUS 2XX
		// And the user is updated

		// null checks
		assertThat(actual.getUserId()).isNotNull();

		assertThat(updated.getUserId()).isNotNull();
		
		// compare checks
		assertThat(updated.getUserId()).extracting("space", "email").containsExactly(actual.getUserId().getSpace(),
				actual.getUserId().getEmail());

		assertThat(updated.getRole().toString()).isEqualTo((newRole.toString()));

		assertThat(updated).extracting("username", "avatar").containsExactly(newUserName, newTestAvatar);
	}

	@Test
	public void testUpdateWhileUserNotExist() {
		// GIVEN the server is up (Automatically by SpringBootTest)
		// WHEN I put /dts/users/{userSpace}/{userEmail} while the user does not exist
		// THEN the result HTTP STATUS NOT 2XX
		assertThrows(NotFound.class,
				() -> this.restTemplate.put(
						this.updateUserUrl, new UserBoundary(new NewUserDetails(this.testUserMail,
								UserRoleBoundary.PLAYER, this.testUserName, this.testUserAvatar)),
						this.spaceName, this.testUserMail));
	}
}