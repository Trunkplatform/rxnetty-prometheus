package com.trunk.metrics.rxnetty.router;

import com.google.inject.Singleton;
import io.netty.util.AttributeKey;
import io.prometheus.client.SimpleTimer;

@Singleton
public class AttributeKeys {
  // First Byte Keys
  public static final AttributeKey<SimpleTimer> REQUEST_FIRST_BYTE_RECEIVED_TIMER = AttributeKey.newInstance("requestFirstByteReceivedTimer");
  public static final AttributeKey<Double> TOTAL_READ_TIME = AttributeKey.newInstance("totalReadTime");
  public static final AttributeKey<Long> BYTES_READ = AttributeKey.newInstance("bytesRead");

  // Request Keys
  public static final AttributeKey<String> METHOD = AttributeKey.newInstance("method");
  public static final AttributeKey<String> PATH = AttributeKey.newInstance("path");
  public static final AttributeKey<String> REQUEST_CONTENT_TYPE = AttributeKey.newInstance("requestContentType");
  public static final AttributeKey<String> REQUEST_ENCODING = AttributeKey.newInstance("requestEncoding");

  // Response Keys
  public static final AttributeKey<String> RESPONSE_CONTENT_TYPE = AttributeKey.newInstance("responseContentType");
  public static final AttributeKey<String> RESPONSE_ENCODING = AttributeKey.newInstance("responseEncoding");
  public static final AttributeKey<String> RESPONSE_STATUS = AttributeKey.newInstance("responseStatus");
  public static final AttributeKey<String> RESPONSE_PROTOCOL = AttributeKey.newInstance("responseProtocol");
}
