/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.trunk.metrics.rxnetty;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.reactivex.netty.protocol.tcp.client.events.TcpClientEventListener;

import java.util.concurrent.TimeUnit;

public class PrometheusTcpClientListener extends TcpClientEventListener {
  private final Gauge connectionOpen;
  private final EventMetric connection;
  private final EventMetric connectionClose;
  private final EventMetric poolAcquire;
  private final EventMetric poolRelease;
  private final Counter poolEvictions;
  private final Counter poolReuse;

  private final EventMetric write;
  private final EventMetric flush;

  private final Counter bytesRead;
  private final Counter bytesWritten;

  public PrometheusTcpClientListener(CollectorRegistry registry) {
    connectionOpen = Gauge.build()
      .name("tcp_client_connections_open_count")
      .help("The number of open connections")
      .register(registry);

    connection = new EventMetric(registry, "tcp_client_connection_connect", "connections made");
    connectionClose = new EventMetric(registry, "tcp_client_connection_handle",  "connections handled");
    poolAcquire = new EventMetric(registry, "tcp_client_connection_pool_acquire", "connection pool members acquired");
    poolRelease = new EventMetric(registry, "tcp_client_connection_pool_release", "connection pool members released");

    poolEvictions = Counter.build()
      .name("tcp_client_connection_pool_evict_total")
      .help("Number of connection pool members evicted")
      .register(registry);
    poolReuse = Counter.build()
      .name("tcp_client_connection_pool_reuse_total")
      .help("Number of connection pool members reused")
      .register(registry);

    write = new EventMetric(registry, "tcp_client_write", "write events");
    flush = new EventMetric(registry, "tcp_client_flush", "flush events");

    bytesWritten = Counter.build()
      .name("bytes_written_total")
      .help("Total number of bytes written")
      .register(registry);
    bytesRead = Counter.build()
      .name("bytes_read_total")
      .help("Total number of bytes read")
      .register(registry);
  }

  @Override
  public void onByteRead(long bytesRead) {
    this.bytesRead.inc(bytesRead);
  }

  @Override
  public void onByteWritten(long bytesWritten) {
    this.bytesWritten.inc(bytesWritten);
  }

  @Override
  public void onFlushComplete(long duration, TimeUnit timeUnit) {
    flush.success(duration, timeUnit);
  }

  @Override
  public void onFlushStart() {
    flush.start();
  }

  @Override
  public void onWriteFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    write.failure(duration, timeUnit);
  }

  @Override
  public void onWriteSuccess(long duration, TimeUnit timeUnit) {
    write.success(duration, timeUnit);
  }

  @Override
  public void onWriteStart() {
    write.start();
  }

  @Override
  public void onPoolReleaseFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    poolRelease.failure(duration, timeUnit);
  }

  @Override
  public void onPoolReleaseSuccess(long duration, TimeUnit timeUnit) {
    poolRelease.success(duration, timeUnit);
  }

  @Override
  public void onPoolReleaseStart() {
    poolRelease.start();
  }

  @Override
  public void onPooledConnectionEviction() {
    poolEvictions.inc();
  }

  @Override
  public void onPooledConnectionReuse() {
    poolReuse.inc();
  }

  @Override
  public void onPoolAcquireFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    poolAcquire.failure(duration, timeUnit);
  }

  @Override
  public void onPoolAcquireSuccess(long duration, TimeUnit timeUnit) {
    poolAcquire.success(duration, timeUnit);
  }

  @Override
  public void onPoolAcquireStart() {
    poolAcquire.start();
  }

  @Override
  public void onConnectionCloseFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    connectionClose.failure(duration, timeUnit);
    connectionOpen.dec();
  }

  @Override
  public void onConnectionCloseSuccess(long duration, TimeUnit timeUnit) {
    connectionClose.success(duration, timeUnit);
    connectionOpen.dec();
  }

  @Override
  public void onConnectionCloseStart() {
    connectionClose.start();
  }

  @Override
  public void onConnectFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    connection.failure(duration, timeUnit);
  }

  @Override
  public void onConnectSuccess(long duration, TimeUnit timeUnit) {
    connection.success(duration, timeUnit);
  }

  @Override
  public void onConnectStart() {
    connection.start();
    connectionOpen.inc();
  }
}
