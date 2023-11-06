package info.hubbitus

import com.atlassian.jira.rest.client.api.domain.BasicComponent
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import info.hubbitus.DTO.Alert
import info.hubbitus.DTO.AlertRequest
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.hamcrest.Matcher
import org.hamcrest.collection.IsIterableContainingInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat

@QuarkusTest
class JiraServiceTest {
	@Inject
	JiraService jiraService

	@Inject
	ObjectMapper objectMapper

	@BeforeEach
	void setupTest() {
		objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
	}

//	@BeforeAll
//	static void setup() {
//		JiraService mock = Mockito.mock(JiraService.class)
//		Mockito.when(
//			mock.createIssue(any(AlertContext.class))
//		).thenReturn(
//			new BasicIssue(null, 'TEST-0', 0)
//		)
//		QuarkusMock.installMockForType(mock, JiraService.class)
//	}

	@Test
	void testNameNormalize(){
		assertThat(AlertContext.nameNormalize('Labels'), equalTo('labels'))
		assertThat(AlertContext.nameNormalize('Component/s'), equalTo('component_s'))
		assertThat(AlertContext.nameNormalize('Тест'), equalTo('____'))
	}

	@Test
	void testSimpleJiraClient() throws IOException {
		AlertRequest alertRequest = objectMapper.readValue(contentResourceFile('/alert-sample.json5'), AlertRequest)
		assertThat('Alert must be read from JSON resource file', alertRequest, notNullValue())

		Alert alert0 = alertRequest.alerts.find{'DataTest0' == it.labels.alertname} // order is not fixed
		assertThat('0 alert must be not null', alert0, notNullValue())
		assertThat('0 alert name should be DataTest0 alert', alert0.labels.alertname, equalTo('DataTest0'))
		assertThat(alert0.status, equalTo('firing'))

		Set<AlertContext> alertContexts = []
		List<IssueInput> issuesToCreate = alertRequest.alerts.collect{ Alert alert ->
			AlertContext alerting = new AlertContext(
				alert: alert,
				jiraProject: jiraService.getProjectByCode(alert.params.jira__project_key),
				jiraService: jiraService
			)
			alertContexts.add(alerting) // To check parsing
			jiraService.createIssueInput(alerting) // TODO also updates
		}

		assertThat(issuesToCreate, notNullValue())
		assertThat(issuesToCreate.size(), equalTo(2))

		assertThat(alertContexts, notNullValue())
		assertThat(alertContexts.size(), equalTo(2))

		// 0 alert:
		AlertContext alertContext0 = alertContexts.find{'DataTest0' == it.alert.params.alertname }
		assertThat(alertContext0, notNullValue())
		assertThat(alertContext0.jiraFields.size(), equalTo(4))

		assertThat(alertContext0.jiraFields.Priority.name, equalTo('Priority'))
		assertThat(alertContext0.jiraFields.Priority.rawValue, equalTo('High'))
		assertThat(alertContext0.jiraFields.Priority.value, equalTo('High'))

		assertThat(alertContext0.jiraFields.Assignee.name, equalTo('Assignee'))
		assertThat(alertContext0.jiraFields.Assignee.rawValue, equalTo('plalexeev'))
		assertThat(alertContext0.jiraFields.Assignee.value, equalTo('plalexeev'))

		assertThat(alertContext0.jiraFields.'Component/s'.name, equalTo('Component/s'))
		assertThat(alertContext0.jiraFields.'Component/s'.rawValue, equalTo('DQ-support, DevOps-infrastructure'))
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[0], instanceOf(BasicComponent.class))
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[0].id, notNullValue())
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[0].name, equalTo('DevOps-infrastructure'))
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[1], instanceOf(BasicComponent.class))
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[1].name, equalTo('DQ-support'))
		assertThat(alertContext0.jiraFields.'Component/s'.value.sort()[1].id, notNullValue())

		assertThat(alertContext0.jiraFields.Labels.name, equalTo('Labels'))
		assertThat(alertContext0.jiraFields.Labels.rawValue, equalTo('label_one, labelTwo, label:three'))
		assertThat(alertContext0.jiraFields.Labels.value, equalTo(['label_one', 'label:three', 'alert(-560575869)', 'labelTwo'] as Set))


		// 1 alert:
		AlertContext alertContext1 = alertContexts.find{'DataTest1' == it.alert.params.alertname }
		assertThat(alertContext1, notNullValue())
		assertThat(alertContext1.jiraFields.size(), equalTo(3))

		assertThat(alertContext1.jiraFields.Priority.name, equalTo('Priority'))
		assertThat(alertContext1.jiraFields.Priority.rawValue, equalTo('High'))
		assertThat(alertContext1.jiraFields.Priority.value, equalTo('High'))

		assertThat(alertContext1.jiraFields.Assignee.name, equalTo('Assignee'))
		assertThat(alertContext1.jiraFields.Assignee.rawValue, equalTo('plalexeev'))
		assertThat(alertContext1.jiraFields.Assignee.value, equalTo('plalexeev'))

		assertThat(alertContext1.jiraFields.Labels.name, equalTo('Labels'))
		assertThat(alertContext1.jiraFields.Labels.rawValue, nullValue())
		assertThat(alertContext1.jiraFields.Labels.value, equalTo(['alert(753956055)'] as Set))
	}

	/* *********************
 	* Helper methods
 	********************* */

	private String contentResourceFile(String name) throws IOException {
		return this.getClass().getResource(name).text
	}
}
