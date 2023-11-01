package ru.gid.tracker;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.core.Is.is;

/**
 * Unfortunately Spock is not supported yet, so use JUnit5.
 * @link https://github.com/quarkusio/quarkus/issues/6506
 * @link https://github.com/quarkusio/quarkus/issues/30221
 */
@QuarkusTest
class AlertControllerTest {

	@Test
	public void testPingEndpoint() {
		given()
			.when()
				.get("/ping")
			.then()
				.statusCode(200)
				.body(is("pong"));
	}

	@Test
	public void testSimpleJiraClient() {
		given()
			.contentType("application/json")
			.request().body("{\"receiver\": \"jira-data\",\"status\": \"firing\"}")
			.when()
				.post("/alert")
			.then()
				.statusCode(200);
	}
}
