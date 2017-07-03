package com.trunk.http.metrics;

import com.google.inject.Singleton;
import com.trunk.metrics.rx.ObservableMetric;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

import javax.inject.Inject;

@Singleton
public class JsonObservableMetrics {

  private final Counter baseCounter;

  @Inject
  public JsonObservableMetrics(CollectorRegistry registry) {
    baseCounter = Counter.build()
      .name("tokens_emitted_total")
      .help("The number of JSON emitted")
      .labelNames("method", "path")
      .register(registry);
  }

  public <T> ObservableMetric<T> getFor(String method, String path) {
    Counter.Child eventCounter = baseCounter.labels(method, path);
    return new ObservableMetric<T>() {
      @Override
      public void start() {}

      @Override
      public void gotEvent(T t) {
        eventCounter.inc();
      }

      @Override
      public void success() { }

      @Override
      public void failure(Throwable throwable) {}
    };
  }
}
