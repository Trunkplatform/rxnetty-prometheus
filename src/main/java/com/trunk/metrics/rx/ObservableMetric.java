package com.trunk.metrics.rx;

public interface ObservableMetric<T> {
  void start();

  void gotEvent(T t);

  void success();

  void failure(Throwable t);
}
