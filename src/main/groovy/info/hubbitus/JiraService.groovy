package info.hubbitus

import com.atlassian.jira.rest.client.api.*
import com.atlassian.jira.rest.client.api.domain.BasicIssue
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Project
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.google.common.base.Function
import com.google.common.collect.Iterables
import groovy.transform.Memoized
import info.hubbitus.DTO.Alert
import info.hubbitus.DTO.AlertRequest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

//@CompileStatic
@ApplicationScoped
class JiraService {
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

	@Lazy
	IssueRestClient issueClient = {
		jiraRestClient.getIssueClient()
	}()

	@Lazy
	SearchRestClient searchClient = {
		jiraRestClient.getSearchClient()
	}()

	@Lazy
	MetadataRestClient metadataClient = {
		jiraRestClient.getMetadataClient()
	}()

	@Lazy
	ProjectRestClient projectClient = {
		jiraRestClient.getProjectClient()
	}()

	@Memoized
	Project getProjectByCode(String code){
		projectClient.getProject(code).claim()
	}

	@Memoized
	Iterable<CimFieldInfo> getIssueFieldsMetadata(String projectIdOrKey, IssueType issueType){
		issueClient.getCreateIssueMetaFields(projectIdOrKey, issueType.id as String, 0, 1000).claim().values
	}



	List<BasicIssue> process(AlertRequest alertRequest){
//        IssueRestClient issueClient = jira.jiraRestClient.getIssueClient()
//        jira.jiraRestClient.getSearchClient()
//		Promise<Issue> promise = issueClient.getIssue('DATA-1')
//		Issue issue = promise.claim()

//		def searchClient = jiraRestClient.getSearchClient()
//		SearchResult res = searchClient.searchJql("labels = alert(${alert.hashCode})").claim()
		return alertRequest.alerts.collect{ Alert alert ->
			AlertContext alerting = new AlertContext(
				alert: alert,
				jiraProject: getProjectByCode(alert.params.jira__project_key),
				jiraService: this
			)
			createIssue(alerting) // TODO also updates
		}
	}

	BasicIssue createIssue(AlertContext alerting){
		BasicIssue res = issueClient.createIssue(createIssueInput(alerting)).claim()
		log.info("Issue created: ${res.key}")
		return res
	}

	/**
	* Method to create new issue by alert and its mapping
	* @param alerting
	* @return
	**/
	private static IssueInput createIssueInput(AlertContext alerting){
		IssueInputBuilder builder = new IssueInputBuilder(alerting.jiraProject, alerting.jiraIssueType)
			.setSummary(alerting.alert.params.summary)
			.setDescription(alerting.alert.params.description)

		alerting.jiraFields
			.each { String key, JiraFieldMap field ->
				if ('array' == field.meta.schema.type){
					if ('string' == field.meta.schema.items){ // Plain arrays like Labels: builder.setFieldValue(IssueFieldId.LABELS_FIELD.id, ['one', 'two'])
						builder.setFieldInput(
							new FieldInput ((field.meta.schema.system ?: key.toLowerCase()), field.value)
						)
					}
					else{ // Complex values like Component/s: builder.setComponentsNames(['DQ-support', 'DevOps-infrastructure']) // Works
						builder.setFieldInput(
							new FieldInput (
								(field.meta.schema.system ?: key.toLowerCase()),
								toListOfComplexIssueInputFieldValueWithSingleKey(
									(Iterable)field.value*.name,
									'name'
								)
							)
						)
					}
				}
				else{
					builder.setFieldInput(
						new FieldInput (
							(field.meta.schema.system ?: key.toLowerCase()),
							ComplexIssueInputFieldValue.with('name', field.value)
						)
					)
				}
			}

		return builder.build()
	}

	/**
	* @link com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder#toListOfComplexIssueInputFieldValueWithSingleKey(java.lang.Iterable, java.lang.String). Copy/paste. Unfortunately that is provate
	**/
	private static <T> Iterable<ComplexIssueInputFieldValue> toListOfComplexIssueInputFieldValueWithSingleKey(final Iterable<T> items, final String key) {
		return Iterables.transform(items, new Function<T, ComplexIssueInputFieldValue>() {

			@Override
			ComplexIssueInputFieldValue apply(T value) {
				return ComplexIssueInputFieldValue.with(key, value)
			}
		})
	}
}
