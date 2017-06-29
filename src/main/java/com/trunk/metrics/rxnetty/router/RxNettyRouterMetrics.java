package com.trunk.metrics.rxnetty.router;

import com.google.inject.Singleton;
import io.prometheus.client.*;

import javax.inject.Inject;

@Singleton
public class RxNettyRouterMetrics {
  public static final String[] LABEL_NAMES = {
    "method",
    "path",
    "request_content_type",
    "request_encoding",
    "response_status",
    "response_protocol",
    "response_content_type",
    "response_encoding"
  };

  public final Counter requestCount;
  public final Counter unhandledRequestCounter;
  public final Counter responseCount;
  public final Counter failedResponseCount;
  public final Histogram timeToLastByte;
  public final Histogram timeToFirstByte;
  public final Histogram readTime;
  public final Histogram writeTime;
  public final Summary bytesRead;
  public final Summary bytesWritten;
  public final Gauge lastRequest;

  @Inject
  public RxNettyRouterMetrics(CollectorRegistry registry) {
    requestCount = Counter.build().name("http_router_request_total")
      .help("Count of all requests received by the underlying Netty server.")
      .labelNames(LABEL_NAMES).register(registry);
    unhandledRequestCounter = Counter.build().name("http_router_unhandled_request_total")
      .help("Count of all requests not handled by the RxNetty server.")
      .labelNames(LABEL_NAMES).register(registry);
    responseCount = Counter.build().name("http_router_response_total")
      .help("Count of all requests handled by the RxNetty server.")
      .labelNames(LABEL_NAMES).register(registry);
    failedResponseCount = Counter.build().name("http_router_failed_response_total")
      .help("Count of all requests handled by the RxNetty server with no byte written.")
      .labelNames(LABEL_NAMES).register(registry);
    timeToLastByte = Histogram.build().name("http_router_request_time_to_last_byte_seconds")
      .help("Time in seconds from first byte received by Netty to the connection being closed or disconnected.")
      .labelNames(LABEL_NAMES).register(registry);
    timeToFirstByte = Histogram.build().name("http_router_request_time_to_first_byte_seconds")
      .help("Time in seconds from first byte received by Netty to the first byte being sent by Netty.")
      .labelNames(LABEL_NAMES).register(registry);
    readTime = Histogram.build().name("http_router_request_read_time_seconds")
      .help("Time in seconds from the first byte received by Netty to the last byte received.")
      .labelNames(LABEL_NAMES).register(registry);
    writeTime = Histogram.build().name("http_router_request_write_time_seconds")
      .help("Time in seconds from the first byte written by Netty to the connection being closed or disconnected.")
      .labelNames(LABEL_NAMES).register(registry);
    bytesRead = Summary.build().name("http_router_request_data_read_bytes")
      .help("Amount of data received from the request in bytes.")
      .labelNames(LABEL_NAMES).register(registry);
    bytesWritten = Summary.build().name("http_router_request_data_written_bytes")
      .help("Amount of data sent responding to the request in bytes.")
      .labelNames(LABEL_NAMES).register(registry);
    lastRequest = Gauge.build().name("http_router_last_request_timestamp_seconds")
      .help("The timestamp when a request was last handled.")
      .labelNames(LABEL_NAMES).register(registry);
  }
}
