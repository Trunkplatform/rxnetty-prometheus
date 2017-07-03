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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

@Singleton
public class MetricsHandler implements RequestHandler<ByteBuf, ByteBuf> {
  private static final Logger log = LoggerFactory.getLogger(com.trunk.metrics.MetricsHandler.class);

  private final CollectorRegistry registry;

  @Inject
  public MetricsHandler(CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return Observable.fromCallable(
      () -> {
        ByteBuf buffer = Unpooled.buffer();
        try (BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new ByteBufOutputStream(buffer)))) {
          TextFormat.write004(bufWriter, registry.metricFamilySamples());
          return buffer;
        }
      }
    )
      .flatMap(
        buffer -> {
          response.setStatus(HttpResponseStatus.OK);
          response.addHeader(HttpHeaderNames.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
          return response.write(Observable.just(buffer));
        }
      )
      .doOnError(throwable -> log.warn("Error gathering metrics", throwable))
      .onErrorResumeNext(
        throwable ->
          Observable.just("ERROR")
            .flatMap(
              msg -> {
                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                return response.writeString(Observable.just(msg));
              }
            )
      );

  }
}
