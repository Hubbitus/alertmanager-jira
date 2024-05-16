package info.hubbitus.DTO

/**
* Also parsed dynamic valies of fields:
* `jira__field__*` - all fields which we are best trying to set in target issue. For examples: `jira__field__assignee: plalexeev`, `jira__field__priority: Hight`.
* Please note, for values takes array, please provide it as comma-separated string (), like: `jira__field__labels: 'label_one, labelTwo, label:three'`
* `jira__field__name__<n>`/`jira__field__value__<n>` pairs. See notes below about possible variants of quoting and names providing
**/
enum OptionsFields {
	JIRA__PROJECT_KEY('jira__project_key', 'The project name in which issue creation is supposed to be (e.g. `DATA`)', null),
	JIRA__ISSUE_TYPE_NAME('jira__issue_type_name', 'The type of issue (e.g. `Task`)', null),
	JIRA__ALERT_IDENTIFY_LABEL('jira__alert_identify_label', 'Template of additional label to identify issue update (or resolving). By default, `alert[${context.alert.hashCode()}]', 'alert[${context.alert.hashCode()}]'),
	JIRA__JQL_TO_FIND_ISSUE_FOR_UPDATE('jira__jql_to_find_issue_for_update', 'By default `labels = "alert[${context.alert.hashCode()}]"`. Provide false or empty value to do not search previous issues', '(labels = "alert[${context.alert.hashCode()}]" AND statusCategory != Done)'),
	JIRA__COMMENT_IN_PRESENT_ISSUES('jira__comment_in_present_issues', 'Template to use for comment issue, if that already present. Be careful - all issues by `JQL` will be commented!', null)

	public final String key
	public final String description
	public final String defaultValue

	OptionsFields(String key, String description, String defaultValue) {
		this.key = key
		this.description = description
		this.defaultValue = defaultValue
	}
}
