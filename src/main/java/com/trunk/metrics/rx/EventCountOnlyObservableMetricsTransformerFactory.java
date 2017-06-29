package com.trunk.metrics.rx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class EventCountOnlyObservableMetricsTransformerFactory implements ObservableMetricsTransformerFactory<Object> {
  private final Counter events;

  public EventCountOnlyObservableMetricsTransformerFactory(CollectorRegistry registry, String name, String help) {
    events = Counter.build()
      .name(name + "_events_total")
      .help("The count of " + help)
      .labelNames("state")
      .register(registry);
  }

  @Override
  public ObservableMetricsTransformer<Object> build() {
    return new ObservableMetricsTransformer<>(
      new ObservableMetric<Object>() {

        @Override
        public void start() {
        }

        @Override
        public void gotEvent(Object t) {
          events.inc();
        }

        @Override
        public void success() {
        }

        @Override
        public void failure(Throwable throwable) {
        }
      }
    );
  }
}
