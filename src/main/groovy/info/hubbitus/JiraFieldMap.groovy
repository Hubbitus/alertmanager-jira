package info.hubbitus

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includeNames=true)
class JiraFieldMap {
	String name
	Object rawValue
	Object value
	CimFieldInfo meta
}
