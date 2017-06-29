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
import io.reactivex.netty.protocol.tcp.server.events.TcpServerEventListener;

import java.util.concurrent.TimeUnit;

public class PrometheusTcpServerListener extends TcpServerEventListener {

  private final Gauge connectionOpen;
  private final Counter connectionAccept;

  private final EventMetric connectionHandling;
  private final EventMetric connectionClose;
  private final EventMetric write;
  private final EventMetric flush;

  private final Counter bytesRead;
  private final Counter bytesWritten;

  public PrometheusTcpServerListener(CollectorRegistry registry) {
    connectionOpen = Gauge.build()
      .name("tcp_server_connections_open_count")
      .help("The number of open connections")
      .register(registry);
    connectionAccept = Counter.build()
      .name("tcp_server_connection_accept_total")
      .help("The number of accepted connections.")
      .register(registry);
    connectionHandling = new EventMetric(registry, "tcp_server_connection_handle", "handled connections");
    connectionClose = new EventMetric(registry, "tcp_server_connection_close", "");

    write = new EventMetric(registry, "tcp_server_write", "write operations");
    flush = new EventMetric(registry, "tcp_server_flush", "flush operations");

    bytesWritten = Counter.build()
      .name("tcp_server_bytes_written_total")
      .help("The total number of bytes written.")
      .register(registry);
    bytesRead = Counter.build()
      .name("tcp_server_bytes_read_total")
      .help("The total number of bytes read.")
      .register(registry);
  }

  @Override
  public void onConnectionHandlingFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    connectionHandling.failure(duration, timeUnit);
  }

  @Override
  public void onConnectionHandlingSuccess(long duration, TimeUnit timeUnit) {
    connectionHandling.success(duration, timeUnit);
  }

  @Override
  public void onConnectionHandlingStart(long duration, TimeUnit timeUnit) {
    connectionHandling.start(duration, timeUnit);
  }

  @Override
  public void onConnectionCloseStart() {
    connectionClose.start();
  }

  @Override
  public void onConnectionCloseSuccess(long duration, TimeUnit timeUnit) {
    connectionClose.success(duration, timeUnit);
    connectionOpen.dec();
  }

  @Override
  public void onConnectionCloseFailed(long duration, TimeUnit timeUnit, Throwable throwable) {
    connectionClose.failure(duration, timeUnit);
    connectionOpen.dec();
  }

  @Override
  public void onNewClientConnected() {
    connectionAccept.inc();
    connectionOpen.inc();
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
}
