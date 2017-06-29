package com.trunk.metrics.rxnetty.router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.pk11.rxnetty.router.Route;
import rx.Observable;

import java.util.Map;

class RxNettyMetricsHandler implements Route<ByteBuf, ByteBuf> {
  private final HttpMethod method;
  private final String path;
  private final RequestHandler<ByteBuf, ByteBuf> delegate;

  public static RxNettyMetricsHandler from(
    HttpMethod method,
    String path,
    RequestHandler<ByteBuf, ByteBuf> delegate
  ) {
    return new RxNettyMetricsHandler(method, path, delegate);
  }

  private RxNettyMetricsHandler(HttpMethod method, String path, RequestHandler<ByteBuf, ByteBuf> delegate) {
    this.method = method;
    this.path = path;
    this.delegate = delegate;
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {

    Channel channel = response.unsafeNettyChannel();
    channel.attr(AttributeKeys.METHOD).set(method.name());
    channel.attr(AttributeKeys.PATH).set(path);
    channel.attr(AttributeKeys.REQUEST_CONTENT_TYPE).set(request.getHeader("Content-Type", ""));
    channel.attr(AttributeKeys.REQUEST_ENCODING).set(request.getHeader("Encoding", ""));

    return delegate.handle(request, response);
  }

  @Override
  public Observable<Void> handle(Map<String, String> params, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    Channel channel = response.unsafeNettyChannel();
    channel.attr(AttributeKeys.METHOD).set(method.name());
    channel.attr(AttributeKeys.PATH).set(path);
    channel.attr(AttributeKeys.REQUEST_CONTENT_TYPE).set(request.getHeader("Content-Type", ""));
    channel.attr(AttributeKeys.REQUEST_ENCODING).set(request.getHeader("Encoding", ""));

    if (delegate instanceof Route) {
      return ((Route<ByteBuf, ByteBuf>) delegate).handle(params, request, response);
    }
    return delegate.handle(request, response);
  }
}
