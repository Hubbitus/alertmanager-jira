package info.hubbitus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
* Unfortunately, Spock is not supported yet, so use JUnit5.
* @link <a href="https://github.com/quarkusio/quarkus/issues/6506">Quarkus issue for that</a>
* @link <a href="https://github.com/quarkusio/quarkus/issues/30221">Quarkus spock extension proposal</a>
**/
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
	void testJiraCreateSimpleAlert() throws IOException {
		given()
			.contentType("application/json")
			.request().body(contentResourceFile("/alert-sample.small.json5"))
			.when()
				.post("/alert")
			.then()
				.log().ifValidationFails(LogDetail.BODY)
				.statusCode(200)
				.body("content.result", is("ok"))
				.body("content.affected_issues", not(emptyArray()))
		;
	}

	/**
	* Just similar to {@see testJiraClientSmall}, but in JSON used pair:
	*  "jira__field__name__1": "Component/s",
	*  "jira__field__value__1": "DQ-issues+alerts, DevOps+infrastructure",
	* instead of simple:
	* 	"jira__field__component_s": "DQ-issues+alerts, DevOps+infrastructure"
	* @throws IOException because resource may be unavailable theoretically. Have no matter for the test
	**/
	@Test
	void testJiraCreateSimpleAlert_FieldPairs() throws IOException {
		given()
			.contentType("application/json")
			.request().body(contentResourceFile("/alert-sample.small.field_pairs.json5"))
			.when()
				.post("/alert")
			.then()
				.log().ifValidationFails(LogDetail.BODY)
				.statusCode(200)
				.body("content.result", is("ok"))
				.body("content.affected_issues", not(emptyArray()))
		;
	}

	/**
    * Alertmanager sometimes sent requests where $ is present.
    * Issue <a href="https://jira.gid.team/browse/DATA-6503">DATA-6503</a>
    **/
	@Test
	void testJiraCreateAlertWith$InInput() throws IOException {
		given()
			.contentType("application/json")
			.request().body(contentResourceFile("/alert-sample.with$.json5"))
			.when()
				.post("/alert")
			.then()
				.log().ifValidationFails(LogDetail.BODY)
				.statusCode(200)
				.body("content.result", is("ok"))
				.body("content.affected_issues", not(emptyArray()))
		;
	}

	/* *********************
	* Helper methods
	********************* */
	private String contentResourceFile(String name) throws IOException {
		try (var stream = Objects.requireNonNull(this.getClass().getResource(name)).openStream()){
			return new String(stream.readAllBytes(), UTF_8);
		}
	}
}
