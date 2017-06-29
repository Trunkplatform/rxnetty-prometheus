package com.trunk.metrics.rxnetty;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class ResponseCodesHolder {

  private final Counter.Child response5xx;
  private final Counter.Child response4xx;
  private final Counter.Child response3xx;
  private final Counter.Child response2xx;
  private final Counter.Child response1xx;

  public ResponseCodesHolder(CollectorRegistry registry) {
    Counter baseCounter = Counter.build()
      .name("response_code_total")
      .help("The count of response code classes")
      .labelNames("state")
      .register(registry);
    
    response1xx = baseCounter.labels("1xx");
    response2xx = baseCounter.labels("2xx");
    response3xx = baseCounter.labels("3xx");
    response4xx = baseCounter.labels("4xx");
    response5xx = baseCounter.labels("5xx");
  }

  public void update(int responseCode) {
    int firstDigit = responseCode / 100;
    switch (firstDigit) {
      case 1:
        response1xx.inc();
        break;
      case 2:
        response2xx.inc();
        break;
      case 3:
        response3xx.inc();
        break;
      case 4:
        response4xx.inc();
        break;
      case 5:
        response5xx.inc();
        break;
    }
  }
}
