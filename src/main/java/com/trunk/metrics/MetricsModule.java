package com.trunk.metrics;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.trunk.metrics.rxnetty.router.MetricsRouter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.*;

import javax.inject.Singleton;

public class MetricsModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  protected CollectorRegistry getRegistry() {
    CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    new StandardExports().register(registry);
    new MemoryPoolsExports().register(registry);
    new GarbageCollectorExports().register(registry);
    new ThreadExports().register(registry);
    new ClassLoadingExports().register(registry);

    return registry;
  }

  @Provides
  @Singleton
  protected MetricsRouter getRouter() {
    return new MetricsRouter();
  }
}
