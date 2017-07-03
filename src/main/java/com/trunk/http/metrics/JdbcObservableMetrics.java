package com.trunk.http.metrics;

import com.google.inject.Singleton;
import com.trunk.metrics.rx.ObservableMetric;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;

import javax.inject.Inject;

@Singleton
public class JdbcObservableMetrics {

  public static final String SELECT = "select";
  public static final String INSERT = "insert";
  public static final String UPDATE = "update";
  public static final String STORED_PROC = "stored_proc";
  public static final String COMPOUND = "compound";

  private final Counter baseCallsCounter;
  private final Counter baseEventsCounter;
  private final Counter baseCompletionCounter;
  private final Summary baseLatencySummary;

  @Inject
  public JdbcObservableMetrics(CollectorRegistry registry) {
    baseCallsCounter = Counter.build()
      .name("database_calls_total")
      .help("The number of database requests made")
      .labelNames("name", "type")
      .register(registry);

    baseEventsCounter = Counter.build()
      .name("database_events_total")
      .help("The number of result events emitted")
      .labelNames("name", "type")
      .register(registry);

    baseCompletionCounter = Counter.build()
      .name("database_call_completion_total")
      .help("The number of database calls completed")
      .labelNames("name", "type", "state")
      .register(registry);
    baseLatencySummary = Summary.build()
      .name("database_call_latency_seconds")
      .help("The latency in seconds of database calls")
      .labelNames("name", "type", "state")
      .register(registry);

  }

  public <T> ObservableMetric<T> getFor(String name, String type) {
    Counter.Child callsCounter = baseCallsCounter.labels(name, type);
    Counter.Child eventCounter = baseEventsCounter.labels(name, type);

    Counter.Child success = baseCompletionCounter.labels(name, type, "success");
    Summary.Child successLatency = baseLatencySummary.labels(name, type, "success");
    Counter.Child failed = baseCompletionCounter.labels(name, type, "failed");
    Summary.Child failureLatency = baseLatencySummary.labels(name, type, "failed");

    return new ObservableMetric<T>() {

      private volatile SimpleTimer timer;

      @Override
      public void start() {
        timer = new SimpleTimer();
        callsCounter.inc();
      }

      @Override
      public void gotEvent(T t) {
        eventCounter.inc();
      }

      @Override
      public void success() {
        success.inc();
        successLatency.observe(timer.elapsedSeconds());
      }

      @Override
      public void failure(Throwable throwable) {
        failed.inc();
        failureLatency.observe(timer.elapsedSeconds());
      }
    };
  }
}
