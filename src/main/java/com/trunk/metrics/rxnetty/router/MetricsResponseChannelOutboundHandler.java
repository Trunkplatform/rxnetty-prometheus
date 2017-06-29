package com.trunk.metrics.rxnetty.router;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

public class MetricsResponseChannelOutboundHandler extends ChannelOutboundHandlerAdapter {
  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof HttpServerResponse) {
      Channel channel = ctx.channel();
      HttpServerResponse response = (HttpServerResponse) msg;
      channel.attr(AttributeKeys.RESPONSE_STATUS).set(response.getStatus().codeAsText().toString());
      channel.attr(AttributeKeys.RESPONSE_CONTENT_TYPE).set(response.getHeader("content-type", ""));
      channel.attr(AttributeKeys.RESPONSE_ENCODING).set(response.getHeader("encoding", ""));
    }
    if (msg instanceof HttpResponse) {
      Channel channel = ctx.channel();
      HttpResponse response = (HttpResponse) msg;
      channel.attr(AttributeKeys.RESPONSE_STATUS).set(response.status().codeAsText().toString());
      channel.attr(AttributeKeys.RESPONSE_PROTOCOL).set(response.protocolVersion().text());
      channel.attr(AttributeKeys.RESPONSE_CONTENT_TYPE).set(response.headers().get("content-type", ""));
      channel.attr(AttributeKeys.RESPONSE_ENCODING).set(response.headers().get("encoding", ""));
    }
    super.write(ctx, msg, promise);
  }
}
