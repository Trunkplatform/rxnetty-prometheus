package com.trunk.metrics.rxnetty;

import io.prometheus.client.Collector;

import java.util.concurrent.TimeUnit;

public class Utils {

  public static double toSeconds(long duration, TimeUnit timeUnit) {
    return timeUnit.toNanos(duration) / Collector.NANOSECONDS_PER_SECOND;
  }
}
