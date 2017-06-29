package com.trunk.metrics.rx;

public interface ObservableMetricsTransformerFactory<T> {
  ObservableMetricsTransformer<T> build();
}
