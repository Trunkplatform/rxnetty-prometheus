package com.trunk.metrics.rxnetty.router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.prometheus.client.SimpleTimer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsFirstByteChannelInboundHandler extends ChannelInboundHandlerAdapter {
  private final SimpleTimer startTime;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicLong bytesRead = new AtomicLong(0L);

  public MetricsFirstByteChannelInboundHandler() {
    startTime = new SimpleTimer();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!started.getAndSet(true)) {
      ctx.channel().attr(AttributeKeys.REQUEST_FIRST_BYTE_RECEIVED_TIMER).set(startTime);
    }
    if (msg instanceof ByteBuf) {
      ctx.channel().attr(AttributeKeys.BYTES_READ).set(
        bytesRead.updateAndGet(l -> ((ByteBuf) msg).readableBytes())
      );
    }
    super.channelRead(ctx, msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.channel().attr(AttributeKeys.TOTAL_READ_TIME).set(startTime.elapsedSeconds());
    super.channelReadComplete(ctx);
  }
}
