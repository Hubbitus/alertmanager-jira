package info.hubbitus.DTO

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Project
import com.atlassian.jira.rest.client.api.domain.SearchResult
import groovy.text.SimpleTemplateEngine
import groovy.transform.Canonical
import groovy.transform.Memoized
import groovy.transform.ToString
import info.hubbitus.JiraService

import static info.hubbitus.DTO.OptionsFields.*

/**
* Context of alerting.
* Class to collect around information like: Alert, jira project, jira issue type and so on
**/
@Canonical
@ToString(includeNames=true, includePackage=false)
class AlertContext {
	public static final String JIRA_FIELD_KEY_PREFIX = 'jira__field__'

	public Alert alert

	private JiraService jiraService

	@Lazy
	public SearchResult jiraPresentIssues = {
		String jql = field(
			JIRA__JQL_TO_FIND_ISSUE_FOR_UPDATE.key
			,JIRA__JQL_TO_FIND_ISSUE_FOR_UPDATE.defaultValue
		)
		if (jql){
			return jiraService.searchClient.searchJql(jql).claim()
		}
		else {
			return null
		}
	}()

	@Lazy
	public Project jiraProject = {
		jiraService.getProjectByCode(field(JIRA__PROJECT_KEY.key))
	}()

	@Lazy
	public IssueType jiraIssueType = {
		(IssueType)jiraProject.getIssueTypes().find{ IssueType type ->
			type.name == field(JIRA__ISSUE_TYPE_NAME.key)
		}
	}()

	@Lazy
	Iterable<CimFieldInfo> jiraMetaFields = {
		jiraService.getIssueFieldsMetadata(jiraProject.key, jiraIssueType)
	}()

	@Lazy
	public Map<String, JiraFieldMap> jiraFields = {
		parseJiraFields()
	}()

	/**
	* Parse all `jira__` prefixed labels and annotations in the that order (so, last will override previous) to the fields specification.
	* The most important which must be set for rule:
	*
	* `jira__project_key` - the project name in which issue creation is supposed to be (e.g. `DATA`).
	* `jira__issue_type_name` - the type of issue (e.g. `Task`).
	* `jira__field__*` - all fields which we are best trying to set in target issue. For examples: `jira__field__assignee: plalexeev`, `jira__field__priority: High`.
	* Please note, for values takes array, please provide it as comma-separated string (), like: `jira__field__labels: 'label_one, labelTwo, label:three'`
	* `jira__field__name__<n>`/`jira__field__value__<n>` pairs. See notes below about possible variants of quoting and names providing
	*
	* #### Field names normalization
	*
	* Due to the alertmanager YAML schema binding, all labels and annotations must be valid identifiers!
	* So, unfortunately **you can't set something like**:
	* ```yaml
	* annotations:
	* "jira__field__Component/s": 'DQ-issues+alerts, DevOps+infrastructure'
	* "jira__field__Target start": '2023-11-06'
	* "jira__field__Итоговый результат": 'Some result description (описание результата)'
	* ```
	* And you are have 3 options there (starting from most recommended)
	*
	* ###### 1) Replace all non identifier literals by _
	*
	* Names may be passed in lowercase and all non-identifier symbols (by regexp: [^0-9a-zA-Z_]) replaced by _.
	* For example:
	* ```yaml
	* annotations:
	* jira__field__component_s: 'DQ-issues+alerts, DevOps+infrastructure'
	* jira__field__target_start: '2023-11-06'
	* ```
	*
	* ###### 2) Use pair jira__field__name__<n>/jira__field__value__<n>
	*
	* Continue example:
	* ```yaml
	* annotations:
	* jira__field__name__1: 'Component/s'
	* jira__field__value__1: 'DQ-issues+alerts, DevOps+infrastructure'
	* jira__field__name__2: 'Итоговый результат'
	* jira__field__value__2: 'Some result description (описание результата)'
	* ```
	*
	* > *Note*. There is really have no matter in <n> values. That ma by any string same for the pair and distinct from others!
	* > So, in this example it may be good idea use e.g. `jira__field__name__result`/`jira__field__value__result`
	*
	* ###### 3) Use customId identifier for custom fields
	*
	* ```yaml
	* annotations:
	*   # Field "Итоговый результат"
	*   jira__field__customId__10217: 'Some result description (описание результата)'
	* ```
	* @param alert
	* @return Map of parsed fields with name in key
	**/
	private Map<String, JiraFieldMap> parseJiraFields(){
		Map<String, JiraFieldMap> res = alert.params
			.findAll{it.key.startsWith(JIRA_FIELD_KEY_PREFIX) }
			.collectEntries { param ->
				JiraFieldMap field = new JiraFieldMap(name: param.key - JIRA_FIELD_KEY_PREFIX)

				switch (true) {
					case field.name.startsWith('name__'): // Name/value pair. E.g. jira__field__name__2: 'Итоговый результат'/jira__field__value__2: 'Some result description (описание результата)'
						String valueKey = "${JIRA_FIELD_KEY_PREFIX}value__${field.name - 'name__'}"
						field.name = param.value
						field.rawValue = alert.params[valueKey]
						field.meta = (CimFieldInfo)jiraMetaFields.find{ CimFieldInfo it -> field.name == it.name}
						if (!field.meta) {
							throw new IllegalStateException("No metadata field found for this pair! Name/value pair used to get field: {name=[${field.name}], value=[${field.value}]}! Please check specification")
						}
						break
					case field.name.startsWith('value__'): // Pair to the 'name__', skipping
						return

					case field.name.startsWith('customId'):
						def customFieldId = field.name - 'customId__'
						field.rawValue = param.value
						field.meta = (CimFieldInfo)jiraMetaFields.find{ CimFieldInfo it-> customFieldId as Long == it.schema.customId }
						if (!field.meta) {
							throw new IllegalStateException("No metadata field found for this pair! CustomId scheme used to specify field {id=[${customFieldId}], value=[${field.value}]}! Please check specification")
						}
						else {
							field.name = field.meta.getName()
						}
						break

					default: // Assume simple variant with identifiers and _ replacements
						field.name = nameNormalize(field.name)
						field.rawValue = param.value
						field.meta = (CimFieldInfo)jiraMetaFields.find{ CimFieldInfo it -> nameNormalize(it.name) == nameNormalize(field.name)}
						if (!field.meta) {
							throw new IllegalStateException("No metadata field found for this pair! Default identifier _ substitution scheme used to specify field {name=[${field.name}], value=[${field.value}]}! Please check specification")
						}
						else {
							field.name = field.meta.getName()
						}
				}

				handleTargetMetaType(field)
				return [ (field.name): field ]
			}

		addAlertHashCode(res)
		return res as Map<String, JiraFieldMap>
	}

	private void handleTargetMetaType(JiraFieldMap field){
		field.value = field.rawValue = new SimpleTemplateEngine().createTemplate(field.rawValue as String).make([context: this]).toString()
		if (field.meta.schema.type == 'array') {
			//See IDEA bug https://youtrack.jetbrains.com/issue/IDEA-179791
			//noinspection GroovyAssignabilityCheck
			field.value = field.rawValue.split(/\s*,\s*/) as Set
			if (field.meta.allowedValues) {
				field.value = field.value.collect {String it ->
					def value = field.meta.allowedValues.find{ metaVal ->
						metaVal.name == it
					}
					if (!value) {
						throw new IllegalArgumentException("You provided value [${it}] for field [${field.meta.name}] but it is not allowed. Allowed values (searched by name): ${field.meta.allowedValues}")
					}
					return value
				}
			}
		}
	}

	void addAlertHashCode(Map<String, JiraFieldMap> fields){
		if (fields.Labels){
			fields.Labels.value += issueIdentificationLabel()
		}
		else {
			fields.Labels = new JiraFieldMap(
				name: 'Labels',
				value: [issueIdentificationLabel()] as Set,
				meta: (CimFieldInfo)jiraMetaFields.find{ CimFieldInfo it -> 'Labels' == it.name}
			)
		}
	}

	@Memoized
	static String nameNormalize(String input){
		return input.toLowerCase().replaceAll(/[^0-9a-zA-Z_]/, '_')
	}

	/**
	* Just fast access to do not check always in labels and annotations.
	* Also handling templating by <a href="https://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html#_simpletemplateengine">SimpleTemplateEngine</a>
	* @link template()
	**/
	@Memoized
	String field(String name, String defaultValue = null){
		return template(((alert.labels + alert.annotations)[name] ?: defaultValue))
	}

	private String issueIdentificationLabel(){
		field(JIRA__ALERT_IDENTIFY_LABEL.key, JIRA__ALERT_IDENTIFY_LABEL.defaultValue)
	}

	/**
	* Suppose you have in alert definition where you want reference other fields and expressions:
	* <code>
	* labels:
	*   severity: warning
	* annotations:
	*   jira__field__labels: 'label_one, labelTwo, label:three, severity:${context.field("severity")}'
	* </code>
	* See more details and examples in readme.
	**/
	@Memoized
	String template(String text){
		if (!text)
			return text
		return new SimpleTemplateEngine().createTemplate(text).make([context: this]).toString()
	}
}
