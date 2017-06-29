package com.trunk.metrics.rx;

import rx.Observable;

public class ObservableMetricsTransformer<T> implements Observable.Transformer<T, T> {

  private final ObservableMetric<? super T> metric;

  public ObservableMetricsTransformer(ObservableMetric<? super T> metric) {
    this.metric = metric;
  }

  @Override
  public Observable<T> call(Observable<T> upstream) {
    return upstream
      .doOnSubscribe(this::onSubscribe)
      .doOnNext(this::onNext)
      .doOnUnsubscribe(this::onUnsubscribe)
      .doOnCompleted(this::onCompleted)
      .doOnError(this::onError);
  }

  private void onSubscribe() {
    metric.start();
  }

  private void onNext(T t) {
    metric.gotEvent(t);
  }

  private void onUnsubscribe() {
    metric.success();
  }

  private void onCompleted() {
    metric.success();
  }

  private void onError(Throwable throwable) {
    metric.failure(throwable);
  }
}
