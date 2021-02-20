package dts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // causes the server to go up
public class AdminTests {
	private int port;
	private String spaceName;
	private String testUserMail;
	
	private String deleteAllUsersUrl;
	private String deleteAllItemsUrl;
	private String deleteAllOperationsUrl;
	
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

		this.deleteAllUsersUrl = "http://localhost:" + port + "/dts/admin/users/{adminSpace}/{adminEmail}";
		this.deleteAllItemsUrl = "http://localhost:" + port + "/dts/admin/items/{adminSpace}/{adminEmail}";
		this.deleteAllOperationsUrl = "http://localhost:" + port + "/dts/admin/operations/{adminSpace}/{adminEmail}";
		this.restTemplate = new RestTemplate();
//		this.exportAllUsersUrl = "http://localhost:" + port + "/dts/admin/users/{adminSpace}/{adminEmail}"; // unimplemented yet
//		this.exportAllOps = "http://localhost:" + port + "/dts/admin/operations/{adminSpace}/{adminEmail}"; // unimplemented yet
	}

	// cleanup the database before each test
	@BeforeEach
	public void setup() {
		this.restTemplate.delete(this.deleteAllUsersUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteAllItemsUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteAllOperationsUrl, this.spaceName, this.testUserMail);

	}

	// cleanup the database after each test
	@AfterEach
	public void tearDown() {
		this.restTemplate.delete(this.deleteAllUsersUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteAllItemsUrl, this.spaceName, this.testUserMail);
		this.restTemplate.delete(this.deleteAllOperationsUrl, this.spaceName, this.testUserMail);
	}

	@Test
	public void contextLoads() {

	}

	@Test
	public void testDeleteAllUsersWithEmptyDatabase() {
		assertDoesNotThrow(() -> this.restTemplate.delete(this.deleteAllUsersUrl, this.spaceName, this.testUserMail));
	}

	@Test
	public void testDeleteAllItemsWithEmptyDatabase() {
		assertDoesNotThrow(() -> this.restTemplate.delete(this.deleteAllItemsUrl, this.spaceName, this.testUserMail));
	}

	@Test
	public void testDeleteAllOpsWithEmptyDatabase() {
		assertDoesNotThrow(() -> this.restTemplate.delete(this.deleteAllOperationsUrl, this.spaceName, this.testUserMail));
	}

}