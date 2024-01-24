# alertmanager-jira

Prometheus alertmanager plugin which creates and manages JIRA issues by alerts.

## Configuration

Sample configuration how to run in `podman-compose` full stack prometheus+alertmanager+grafana see in directory [_DEV.scripts/prometheus-alertmanager-grafana]()

There we will start just from alert rule configuration example. Let it be defined like:

[alertmanager.yml](_DEV.scripts/prometheus-alertmanager-grafana/conf/_alertmanager/alertmanager.yml) pert:
```yaml
receivers:
  - name: 'jira-data'
    webhook_configs:
      - url: 'https://alertmanager-jira.url'
        send_resolved: true
```

```yaml
- name: BI
  rules:
    - alert: DataTest0
      expr: 'promhttp_metric_handler_requests_total > 1'
      # for: 10s
      labels:
        severity: warning
        jira__field__severity: High
        value: '{{$value}}'
      annotations:
        jira__field__project_key: DATA
        jira__field__issue_type_name: Alert
        jira__field__assignee: plalexeev
        summary: DataTest0 summary
        description: |
          Some description QAZ2
          of DataTest0 alert
          VALUE: {{$value}}
```

When such alert fired, that will be sent to `url` configured above ('https://alertmanager-jira.url') [in JSON form](https://prometheus.io/docs/alerting/latest/configuration/#webhook_config).
Please look description of [data structures in documentation](https://prometheus.io/docs/alerting/latest/notifications/#data-structures).
Will not provide that for simplicity. But you may look on it in files [alert-sample.json5](src/test/resources/alert-sample.json5) and [alert-sample.small.json5](src/test/resources/alert-sample.small.json5).

### 'jira__' fields for the alerting control

Please pay attention to the labels and annotations starting from `jira__` prefix.
They control how to alert will be turned into JIRA issue and other behaviour.

> *Note* to do not repeat each time some defaults, you may use [alert_relabel_configs](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#alert_relabel_configs) ([sample](https://gitlab.com/gitlab-org/omnibus-gitlab/-/issues/4332)) like:
> ```yaml
> alerting:
>   alert_relabel_configs:
>     - target_label: jira__project_key
>       replacement: DATA
>     - target_label: jira__issue_type_name
>       replacement: Alert
> ```

The most important which must be set for rule:

* `jira__project_key` - the project name in which issue creation is supposed to be (e.g. `DATA`).
* `jira__issue_type_name` - the type of issue (e.g. `Task`).
* `jira__field__*` - all fields which we are best trying to set in target issue. For examples: `jira__field__assignee: plalexeev`, `jira__field__priority: Hight`.
  * Please note, for values takes array, please provide it as comma-separated string (), like: `jira__field__labels: 'label_one, labelTwo, label:three'`
* `jira__field__name__<n>`/`jira__field__value__<n>` pairs. See notes below about possible variants of quoting and names providing
* `jira__alert_identify_label` - template (as described later) of additional label to identify issue update (or resolving). By default, `alert{${context.alert.hashCode()}}` 
* `jira__jql_to_find_issue_for_update`. By default `labels = "alert{${context.alert.hashCode()}}"`. Provide false or empty value to do not search previous issues
* `jira__comment_in_present_issues` - template to use for comment issue, if that already present. Be careful - all issues by `JQL` from `jira__jql_to_find_issue_for_update` will be commented!

#### Field names normalization

Due to the alertmanager YAML schema binding, all labels and annotations must be valid identifiers!
So, unfortunately **you can't set something like**:
```yaml
annotations:
  "jira__field__Component/s": 'DQ-issues+alerts, DevOps+infrastructure'
  "jira__field__Target start": '2023-11-06'
  "jira__field__Итоговый результат": 'Some result description (описание результата)'
```
And you are have 3 options there (starting from most recommended)

###### 1) Replace all non identifier literals by _

Names may be passed in lowercase and all non-identifier symbols (by regexp: [^0-9a-zA-Z_]) replaced by _.
For example:
```yaml
annotations:
  jira__field__component_s: 'DQ-issues+alerts, DevOps+infrastructure'
  jira__field__target_start: '2023-11-06'
```

###### 2) Use pair jira__field__name__<n>/jira__field__value__<n>

Continue example:
```yaml
annotations:
  jira__field__name__1: 'Component/s'
  jira__field__value__1: 'DQ-issues+alerts, DevOps+infrastructure'
  jira__field__name__2: 'Итоговый результат'
  jira__field__value__2: 'Some result description (описание результата)'
```

> *Note*. There is really have no matter in <n> values. That ma by any string same for the pair and distinct from others!
> So, in this example it may be good idea use e.g. `jira__field__name__result`/`jira__field__value__result` 

###### 3) Use customId identifier for custom fields

```yaml
annotations:
  # Field "Итоговый результат"
  jira__field__customId__10217: 'Some result description (описание результата)'
```

#### Values templating

Suppose you have in alert definition:
```yaml
  labels:
    severity: warning
  annotations:
    jira__field__labels: 'label_one, labelTwo, label:three, severity:${context.field("severity")}'
```

For the values `context` see class [AlertContext](src/main/groovy/info/hubbitus/AlertContext.groovy). There are many interesting fields for use, like:
* `alert` - [Alert](src/main/groovy/info/hubbitus/DTO/Alert.groovy) object of incoming data
* `jiraPresentIssues` - search result of found by alert code issues, created early (we automatically create label `Alert(<hashCode>)` to identify updates).
* `jiraProject` - information and metadata of target JIRA project where task should be created (see `jira__project_key` description before)
* `jiraIssueType` - information and metadata of target JIRA IssueType (see `jira__issue_type_name` early)
* `jiraFields` - jira fields, parsed by rules and heuristics described  in previous section. There also metadata for each field present for introspection and validation

## Tech overview
This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell
./gradlew quarkusDev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

### Packaging and running the application

The application can be packaged using:
```shell
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

### Creating a native executable

You can create a native executable using: 
```shell
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/alertmanager-jira-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

### Creating a native executable and pack into container

```shell
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```
or:
```shell
./gradlew imageBuild -Dquarkus.package.type=native
```

See [documentation](https://quarkus.io/guides/building-native-image#creating-a-container) for available customizations.

### Related Guides

- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging

### Running in a container

```shell
podman run -it --rm --name alertmanajer-jira-manual \
    -p 8080:8080 \
    -v /home/pasha/@Projects/@DATA/kafka.scripts/conf/DEV/:/work/_conf:Z,ro \
        localhost/local/alertmanager-jira:latest
```
