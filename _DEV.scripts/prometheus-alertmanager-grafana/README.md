# Simple local stand of Prometheus + Alertmanager + Grafana

## How to run
Just simple as:

```shell
./up
```

## Entrypoints

* Grafana: http://localhost:3000/
* Prometheus:
   - Self monitoring: http://localhost:9090/metrics
   - Query/graph interface: http://localhost:9090/graph . E.g. [graph for the `promhttp_metric_handler_requests_total` metric](http://localhost:9090/graph?g0.expr=promhttp_metric_handler_requests_total&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h)
* Alertmanager: http://localhost:9093/#/alerts

## How to trigger alert

Simple alert configured in group BI (see file [conf/_prometheus/alerts/bi.yml]()) with rule:

```yaml
    - alert: DataTest0
      expr: 'promhttp_metric_handler_requests_total > 1'
```
So, to trigger it, just open at least once [query/graph interface](http://localhost:9090/graph)!

## How to see queries what passed into alertmanager plugin

Just navigate to https://webhook.site/#!/af1e1001-3d44-4c6f-bfd0-1ae34bdcd2bb/1acecde8-6e8a-451c-9e38-6368c2997207/1 (configured in [conf/_alertmanager/alertmanager.yml]())
