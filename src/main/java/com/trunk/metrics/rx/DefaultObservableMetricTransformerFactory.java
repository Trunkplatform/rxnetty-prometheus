package com.trunk.metrics.rx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;

public class DefaultObservableMetricTransformerFactory<T> implements ObservableMetricsTransformerFactory<T> {
  private final Counter start;

  private final Counter events;

  private final Counter.Child success;
  private final Summary.Child successLatency;

  private final Counter.Child failed;
  private final Summary.Child failureLatency;

  public DefaultObservableMetricTransformerFactory(CollectorRegistry registry, String name, String help) {
    Counter baseCompletionCounter = Counter.build()
      .name(name + "_completion_total")
      .help("The count of " + help)
      .labelNames("state")
      .register(registry);
    Summary baseLatencySummary = Summary.build()
      .name(name + "_latency_seconds")
      .help("The latency in seconds of " + help)
      .labelNames("state")
      .register(registry);

    start = Counter.build()
      .name(name + "_start_total")
      .help("The count of " + help)
      .register(registry);

    events = Counter.build()
      .name(name + "_events_total")
      .help("The count of " + help)
      .register(registry);

    success = baseCompletionCounter.labels("success");
    successLatency = baseLatencySummary.labels("success");
    failed = baseCompletionCounter.labels("failed");
    failureLatency = baseLatencySummary.labels("failed");

  }

  @Override
  public ObservableMetricsTransformer<T> build() {
    return new ObservableMetricsTransformer<>(
      new ObservableMetric<T>() {

        private volatile SimpleTimer timer;

        @Override
        public void start() {
          timer = new SimpleTimer();
          start.inc();
        }

        @Override
        public void gotEvent(T t) {
          events.inc();
        }

        @Override
        public void success() {
          success.inc();
          successLatency.observe(timer.elapsedSeconds());
        }

        @Override
        public void failure(Throwable t) {
          failed.inc();
          failureLatency.observe(timer.elapsedSeconds());
        }
      }
    );
  }
}
