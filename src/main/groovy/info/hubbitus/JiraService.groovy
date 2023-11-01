package info.hubbitus

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory

//import com.atlassian.jira.rest.client.api.JiraRestClient
//import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
//@CompileStatic
public class JiraService {
	@Inject
	Logger log

	@ConfigProperty(name="jira.URL")
	public URI jiraURL

	@ConfigProperty(name="jira.username")
	public String username

	@ConfigProperty(name="jira.password")
	String password

	@Lazy
	public JiraRestClient jiraRestClient = {
		log.debug("Lazy init; jiraURL=${jiraURL}, username=${username}")
		return new AsynchronousJiraRestClientFactory()
			.createWithBasicHttpAuthentication(jiraURL, this.username, this.password)
	}()
}
