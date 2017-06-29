package com.trunk.metrics.rx;

public class NoopObservableMetricsTransformerFactory<T> implements ObservableMetricsTransformerFactory<T> {

  private static final NoopObservableMetricsTransformerFactory INSTANCE = new NoopObservableMetricsTransformerFactory();

  public static <T> NoopObservableMetricsTransformerFactory<T> instance() {
    return INSTANCE;
  }

  private static final ObservableMetricsTransformer TRANSFORMER = new ObservableMetricsTransformer(
    new ObservableMetric() {
      @Override
      public void start() {

      }

      @Override
      public void gotEvent(Object o) {

      }

      @Override
      public void success() {

      }

      @Override
      public void failure(Throwable t) {

      }
    }
  );

  private NoopObservableMetricsTransformerFactory() {
  }

  @Override
  public ObservableMetricsTransformer<T> build() {
    return TRANSFORMER;
  }
}
