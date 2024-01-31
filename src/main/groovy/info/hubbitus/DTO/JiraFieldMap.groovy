package info.hubbitus.DTO

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includeNames=true, includePackage=false)
class JiraFieldMap {
	String name
	Object rawValue
	Object value
//	@Lazy
//	Object value = {
//		if (meta.schema.type == 'array') {
//			value = rawValue.split(/\s*,\s*/) as Set
//			if (meta.allowedValues) {
//				value = value.collect {String val ->
//					def value = meta.allowedValues.find{ metaVal ->
//						metaVal.name == val
//					}
//					if (!value) {
//						throw new IllegalArgumentException("You provided value [${it}] for field [${meta.name}] but it is not allowed. Allowed values (searched by name): ${meta.allowedValues}")
//					}
//					return value
//				}
//			}
//		}
//	}()
	CimFieldInfo meta
}
