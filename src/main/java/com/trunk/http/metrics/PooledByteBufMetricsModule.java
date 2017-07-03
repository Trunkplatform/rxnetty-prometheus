package com.trunk.http.metrics;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.netty.buffer.PooledByteBufAllocator;
import io.prometheus.client.CollectorRegistry;

import javax.inject.Inject;

public class PooledByteBufMetricsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PooledByteBufMetricsRegistrar.class).asEagerSingleton();
  }

  @Singleton
  @Provides
  public static PooledByteBufMetrics pooledByteBufMetrics() {
    return new PooledByteBufMetrics(PooledByteBufAllocator.DEFAULT);
  }

  @Singleton
  public static class PooledByteBufMetricsRegistrar {
    @Inject
    public PooledByteBufMetricsRegistrar(CollectorRegistry registry, PooledByteBufMetrics pooledByteBufMetrics) {
      pooledByteBufMetrics.register(registry);
    }
  }
}
