package com.trunk.metrics.rxnetty;

import io.prometheus.client.CollectorRegistry;
import io.reactivex.netty.protocol.http.server.events.HttpServerEventsListener;

import java.util.concurrent.TimeUnit;

public class PrometheusHttpServerListener extends HttpServerEventsListener {

  private final EventMetric requestRead;
  private final EventMetric requestProcessing;
  private final EventMetric responseWrite;

  private final PrometheusTcpServerListener tcpDelegate;

  public PrometheusHttpServerListener(CollectorRegistry registry) {
    requestRead = new EventMetric(registry, "http_request_read", "requests received");
    requestProcessing = new EventMetric(registry, "http_request_processing", "requests processed");
    responseWrite = new EventMetric(registry, "http_response", "responses written");
    tcpDelegate = new PrometheusTcpServerListener(registry);
  }

  @Override
  public void onRequestHandlingFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    requestProcessing.failure(duration, timeUnit);
  }

  @Override
  public void onRequestHandlingSuccess(long duration, TimeUnit timeUnit) {
    requestProcessing.success(duration, timeUnit);
  }

  @Override
  public void onResponseWriteSuccess(long duration, TimeUnit timeUnit, int responseCode) {
    responseWrite.success(duration, timeUnit);
  }

  @Override
  public void onResponseWriteFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    responseWrite.failure(duration, timeUnit);
  }

  @Override
  public void onRequestReceiveComplete(long duration, TimeUnit timeUnit) {
    requestRead.success(duration, timeUnit);
  }

  @Override
  public void onRequestHandlingStart(long duration, TimeUnit timeUnit) {
    requestProcessing.start(duration, timeUnit);
  }

  @Override
  public void onRequestHeadersReceived() {
    requestRead.start();
  }

  @Override
  public void onResponseWriteStart() {
    responseWrite.start();
  }

  @Override
  public void onConnectionHandlingFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    tcpDelegate.onConnectionHandlingFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onConnectionHandlingSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onConnectionHandlingSuccess(duration, timeUnit);
  }

  @Override
  public void onConnectionHandlingStart(long duration, TimeUnit timeUnit) {
    tcpDelegate.onConnectionHandlingStart(duration, timeUnit);
  }

  @Override
  public void onConnectionCloseStart() {
    tcpDelegate.onConnectionCloseStart();
  }

  @Override
  public void onConnectionCloseSuccess(long duration, TimeUnit timeUnit) {
    tcpDelegate.onConnectionCloseSuccess(duration, timeUnit);
  }

  @Override
  public void onConnectionCloseFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    tcpDelegate.onConnectionCloseFailed(duration, timeUnit, throwable);
  }

  @Override
  public void onNewClientConnected() {
    tcpDelegate.onNewClientConnected();
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
}
