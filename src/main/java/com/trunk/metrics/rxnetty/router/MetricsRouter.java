package com.trunk.metrics.rxnetty.router;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.pk11.rxnetty.router.Router;

@Singleton
public class MetricsRouter extends Router<ByteBuf, ByteBuf> {

  @Override
  public Router<ByteBuf, ByteBuf> pattern(HttpMethod method, String path, RequestHandler<ByteBuf, ByteBuf> handler) {
    return super.pattern(method, path, RxNettyMetricsHandler.from(method, path, handler));
  }

  @Override
  public Router<ByteBuf, ByteBuf> patternFirst(HttpMethod method, String path, RequestHandler<ByteBuf, ByteBuf> handler) {
    return super.patternFirst(method, path, RxNettyMetricsHandler.from(method, path, handler));
  }

  @Override
  public Router<ByteBuf, ByteBuf> patternLast(HttpMethod method, String path, RequestHandler<ByteBuf, ByteBuf> handler) {
    return super.patternLast(method, path, RxNettyMetricsHandler.from(method, path, handler));
  }
}
