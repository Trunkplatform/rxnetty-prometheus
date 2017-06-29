package com.trunk.metrics.rxnetty;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

import java.util.concurrent.TimeUnit;

import static com.trunk.metrics.rxnetty.Utils.toSeconds;

public class EventMetric {
  private final Counter.Child start;
  private final Summary.Child startLatency;

  private final Counter.Child success;
  private final Summary.Child successLatency;

  private final Counter.Child failed;
  private final Summary.Child failureLatency;

  public EventMetric(CollectorRegistry registry, String name, String help) {
    Counter baseCounter = Counter.build()
      .name(name + "_total")
      .help("The count of " + help)
      .labelNames("state")
      .register(registry);
    Summary baseHistogram = Summary.build()
      .name(name + "_latency_seconds")
      .help("The latency in seconds of " + help)
      .labelNames("state")
      .register(registry);

    start = baseCounter.labels("start");
    startLatency = baseHistogram.labels("start");
    success = baseCounter.labels("success");
    successLatency = baseHistogram.labels("success");
    failed = baseCounter.labels("failed");
    failureLatency = baseHistogram.labels("failed");
  }

  public void start() {
    start.inc();
  }

  public void start(long duration, TimeUnit timeUnit) {
    start.inc();
    startLatency.observe(toSeconds(duration, timeUnit));
  }

  public void success() {
    success.inc();
  }

  public void success(long duration, TimeUnit timeUnit) {
    success.inc();
    successLatency.observe(toSeconds(duration, timeUnit));
  }

  public void failure() {
    failed.inc();
  }

  public void failure(long duration, TimeUnit timeUnit) {
    failed.inc();
    failureLatency.observe(toSeconds(duration, timeUnit));
  }}
