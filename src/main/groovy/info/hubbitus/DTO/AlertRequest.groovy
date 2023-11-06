package info.hubbitus.DTO

import com.fasterxml.jackson.annotation.JsonManagedReference
import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.ToString

/**
 * See example of alert in _DEV.scripts/alert-sample.json
 */
@Canonical
@AutoClone
@ToString(includeNames = true)
class AlertRequest {
	String receiver
	String status

	@JsonManagedReference
	Set<Alert> alerts

	Labels groupLabels
	Labels commonLabels

	int truncatedAlerts

	String groupKey
	String version
	String externalURL
	Annotations commonAnnotations

	/**
	 * There are "well known" items "alertname", "code", "severity", and others (variable amount) by user.
	 */
	static class Labels extends LinkedHashMap<String, String>{}

	/**
	 * There are will be at least items "summary", "description", and others (variable amount) by user.
	 */
	static class Annotations extends LinkedHashMap<String, String> {}
}
