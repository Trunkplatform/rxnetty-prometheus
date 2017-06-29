package com.trunk.metrics.rxnetty;

import io.prometheus.client.CollectorRegistry;
import io.reactivex.netty.protocol.http.client.events.HttpClientEventsListener;

import java.util.concurrent.TimeUnit;

public class PrometheusHttpClientListener extends HttpClientEventsListener {
  private final EventMetric requestWrite;
  private final EventMetric requestProcessing;
  private final EventMetric response;

  private final ResponseCodesHolder responseCodesHolder;
  private final PrometheusTcpClientListener tcpDelegate;

  public PrometheusHttpClientListener(CollectorRegistry registry) {
    requestWrite = new EventMetric(registry, "http_client_request_write", "requests written");
    requestProcessing = new EventMetric(registry, "http_client_request_processing", "requests processed");
    response = new EventMetric(registry, "http_client_response", "responses received");

    responseCodesHolder = new ResponseCodesHolder(registry);
    tcpDelegate = new PrometheusTcpClientListener(registry);
  }

  @Override
  public void onRequestProcessingComplete(long duration, TimeUnit timeUnit) {
    requestProcessing.success(duration, timeUnit);
  }

  @Override
  public void onResponseHeadersReceived(int responseCode, long duration, TimeUnit timeUnit) {
    responseCodesHolder.update(responseCode);
  }

  @Override
  public void onResponseReceiveComplete(long duration, TimeUnit timeUnit) {
    response.success(duration, timeUnit);
  }

  @Override
  public void onRequestWriteStart() {
    requestWrite.start();
  }

  @Override
  public void onResponseFailed(Throwable throwable) {
    response.failure();
  }

  @Override
  public void onRequestWriteComplete(long duration, TimeUnit timeUnit) {
    requestWrite.success(duration, timeUnit);
  }

  @Override
  public void onRequestWriteFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    requestWrite.failure(duration, timeUnit);
  }

  @Override
  public void onRequestSubmitted() {
    requestProcessing.start();
  }

  @Override
  public void onByteRead(long bytesRead) {
    tcpDelegate.onByteRead(bytesRead);
  }

  @Override
  public void onByteWritten(long bytesWritten) {
    tcpDelegate.onByteWritten(bytesWritten);
  }

  @Override
  public void onFlushComplete(long duration, TimeUnit timeUnit) {
    tcpDelegate.onFlushComplete(duration, timeUnit);
  }

  @Override
  public void onFlushStart() {
    tcpDelegate.onFlushStart();
  }

  @Override
  public void onWriteFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    tcpDelegate.onWriteFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onWriteSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onWriteSuccess(duration, timeUnit);
  }

  @Override
  public void onWriteStart() {
    tcpDelegate.onWriteStart();
  }

  @Override
  public void onPoolReleaseFailed(long duration, TimeUnit timeUnit,
                                  Throwable throwable) {
    tcpDelegate.onPoolReleaseFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onPoolReleaseSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onPoolReleaseSuccess(duration, timeUnit);
  }

  @Override
  public void onPoolReleaseStart() {
    tcpDelegate.onPoolReleaseStart();
  }

  @Override
  public void onPooledConnectionEviction() {
    tcpDelegate.onPooledConnectionEviction();
  }

  @Override
  public void onPooledConnectionReuse() {
    tcpDelegate.onPooledConnectionReuse();
  }

  @Override
  public void onPoolAcquireFailed(long duration, TimeUnit timeUnit,
                                  Throwable throwable) {
    tcpDelegate.onPoolAcquireFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onPoolAcquireSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onPoolAcquireSuccess(duration, timeUnit);
  }

  @Override
  public void onPoolAcquireStart() {
    tcpDelegate.onPoolAcquireStart();
  }

  @Override
  public void onConnectionCloseFailed(long duration, TimeUnit timeUnit,
                                      Throwable throwable) {
    tcpDelegate.onConnectionCloseFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onConnectionCloseSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onConnectionCloseSuccess(duration, timeUnit);
  }

  @Override
  public void onConnectionCloseStart() {
    tcpDelegate.onConnectionCloseStart();
  }

  @Override
  public void onConnectFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    tcpDelegate.onConnectFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onConnectSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onConnectSuccess(duration, timeUnit);
  }

  @Override
  public void onConnectStart() {
    tcpDelegate.onConnectStart();
  }
}
