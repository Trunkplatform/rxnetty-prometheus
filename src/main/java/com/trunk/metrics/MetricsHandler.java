package com.trunk.metrics;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

@Singleton
public class MetricsHandler implements RequestHandler<ByteBuf, ByteBuf> {

  private final CollectorRegistry registry;

  @Inject
  public MetricsHandler(CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    ByteBuf buffer = Unpooled.buffer();
    try (BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new ByteBufOutputStream(buffer)))) {
      TextFormat.write004(bufWriter, registry.metricFamilySamples());
    } catch (IOException e) {
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      return response.writeString(Observable.just("ERROR"));
    }

    response.setStatus(HttpResponseStatus.OK);
    response.addHeader(HttpHeaderNames.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
    return response.write(Observable.just(buffer));
  }
}
