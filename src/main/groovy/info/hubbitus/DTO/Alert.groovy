package info.hubbitus.DTO

import groovy.transform.ToString
import java.time.ZonedDateTime

/**
 * See example of alert in _DEV.scripts/alert-sample.json
 */
@ToString(includeNames = true)
class Alert {
	String receiver
	String status

	Set<AlertItem> alerts
	Labels groupLabels
	Labels commonLabels

	int truncatedAlerts

	String groupKey
	String version
	String externalURL
	Annotations commonAnnotations

	static class AlertItem {
		String status

		Labels labels
		Annotations annotations

		String fingerprint
		String generatorURL
		ZonedDateTime endsAt
		ZonedDateTime startsAt
	}

	/**
	 * There are "well known" items "alertname", "code", "severity", and others (variable amount) by user.
	 */
	static class Labels extends LinkedHashMap<String, String>{
	}

	/**
	 * There are will be at least items "summary", "description", and others (variable amount) by user.
	 */
	static class Annotations extends LinkedHashMap<String, String> {
	}
}
