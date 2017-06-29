package com.trunk.metrics.rxnetty.router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsLastByteChannelOutboundHandler extends ChannelOutboundHandlerAdapter {
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean finished = new AtomicBoolean(false);
  private final AtomicLong bytesWritten = new AtomicLong(0L);
  private SimpleTimer writeTimer;
  private Double timeToFirstByte = null;
  private final RxNettyRouterMetrics rxNettyRouterMetrics;

  public MetricsLastByteChannelOutboundHandler(RxNettyRouterMetrics rxNettyRouterMetrics) {
    this.rxNettyRouterMetrics = rxNettyRouterMetrics;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (!started.getAndSet(true)) {
      writeTimer = new SimpleTimer();
      SimpleTimer requestFirstByteReceivedTimer = ctx.channel().attr(AttributeKeys.REQUEST_FIRST_BYTE_RECEIVED_TIMER).get();
      timeToFirstByte = requestFirstByteReceivedTimer != null ? requestFirstByteReceivedTimer.elapsedSeconds() : null;
    }
    if (msg instanceof ByteBuf) {
      bytesWritten.getAndUpdate(l -> l + ((ByteBuf) msg).readableBytes());
    }
    super.write(ctx, msg, promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    logStatistics(ctx);
    super.close(ctx, promise);
  }

  @Override
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    logStatistics(ctx);
    super.disconnect(ctx, promise);
  }

  private void logStatistics(ChannelHandlerContext ctx) {
    if (finished.getAndSet(true)) {
      return;
    }
    Channel channel = ctx.channel();

    String[] labels = {
      getAttr(channel, AttributeKeys.METHOD, ""),
      getAttr(channel, AttributeKeys.PATH, ""),
      getAttr(channel, AttributeKeys.REQUEST_ENCODING, ""),
      getAttr(channel, AttributeKeys.REQUEST_CONTENT_TYPE, ""),
      getAttr(channel, AttributeKeys.RESPONSE_STATUS, ""),
      getAttr(channel, AttributeKeys.RESPONSE_PROTOCOL, ""),
      getAttr(channel, AttributeKeys.RESPONSE_CONTENT_TYPE, ""),
      getAttr(channel, AttributeKeys.RESPONSE_ENCODING, ""),
    };


    Counter.Child responseCounter = rxNettyRouterMetrics.responseCount.labels(labels);
    Counter.Child failedResponseCounter = rxNettyRouterMetrics.failedResponseCount.labels(labels);
    if (channel.attr(AttributeKeys.PATH).get() == null) {
      rxNettyRouterMetrics.requestCount.inc();
      rxNettyRouterMetrics.unhandledRequestCounter.inc();
    } else if (!started.get()) {
      failedResponseCounter.inc();
    } else {
      responseCounter.inc();
    }

    Histogram.Child timeToFirstByteHistogram = rxNettyRouterMetrics.timeToFirstByte.labels(labels);
    if (timeToFirstByte != null) {
      timeToFirstByteHistogram.observe(timeToFirstByte);
    }

    Histogram.Child timeToLastByteHistogram = rxNettyRouterMetrics.timeToLastByte.labels(labels);
    SimpleTimer requestFirstByteReceivedTimer = channel.attr(AttributeKeys.REQUEST_FIRST_BYTE_RECEIVED_TIMER).get();
    Double timeToLastByte = requestFirstByteReceivedTimer != null ? requestFirstByteReceivedTimer.elapsedSeconds() : null;
    if (timeToLastByte != null) {
      timeToLastByteHistogram.observe(timeToLastByte);
    }

    Histogram.Child readTimeHistogram = rxNettyRouterMetrics.readTime.labels(labels);
    Double readTime = channel.attr(AttributeKeys.TOTAL_READ_TIME).get();
    if (readTime != null) {
      readTimeHistogram.observe(readTime);
    }

    Histogram.Child writeTimeHistogram = rxNettyRouterMetrics.writeTime.labels(labels);
    Double writeTime = writeTimer.elapsedSeconds();
    writeTimeHistogram.observe(writeTime);

    Summary.Child bytesReadHistogram = rxNettyRouterMetrics.bytesRead.labels(labels);
    Long bytesRead = channel.attr(AttributeKeys.BYTES_READ).get();
    if (bytesRead != null) {
      bytesReadHistogram.observe(bytesRead);
    }

    Summary.Child bytesWrittenHistogram = rxNettyRouterMetrics.bytesWritten.labels(labels);
    Long bytesWritten = this.bytesWritten.get();
    bytesWrittenHistogram.observe(bytesWritten);
  }

  private <T> T getAttr(Channel channel, AttributeKey<T> key, T defaultValue) {
    T t = channel.attr(key).get();
    return t != null ? t : defaultValue;
  }
}
