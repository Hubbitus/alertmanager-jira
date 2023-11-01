package info.hubbitus

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import io.quarkiverse.test.spock.QuarkusSpockTest
import jakarta.enterprise.context.Dependent
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
class AlertSimpleSpec extends Specification {

	def "Spock works!"() {
		expect:
			3 + 4 == 7
	}

//	def testSimpleJiraClient() {
//		expect:
//			JiraRestClient restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
//				new URI('https://jira-lab.gid.team'), 'plalexeev', 'pere-Turb@Tr0s7')
//	}
}