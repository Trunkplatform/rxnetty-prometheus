package com.trunk.http.metrics;

import com.google.common.collect.Lists;
import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
import io.netty.buffer.PoolArenaMetric;
import io.netty.buffer.PoolChunkMetric;
import io.netty.buffer.PoolSubpageMetric;
import io.netty.buffer.PooledByteBufAllocator;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import static fj.data.List.iterableList;

public class PooledByteBufMetrics extends Collector {

  private final PooledByteBufAllocator pooledByteBufAllocator;

  public PooledByteBufMetrics(PooledByteBufAllocator pooledByteBufAllocator) {
    this.pooledByteBufAllocator = pooledByteBufAllocator;
  }

  @Override
  public java.util.List<MetricFamilySamples> collect() {
    return collectFor(
      "direct",
      iterableList(pooledByteBufAllocator.directArenas())
    )
      .append(
        collectFor(
          "heap",
          iterableList(pooledByteBufAllocator.heapArenas())
        )
      )
      .toJavaList();
  }

  private List<MetricFamilySamples> collectFor(String name, List<PoolArenaMetric> poolArenaMetrics) {
    return collectTinySubPageFamiliesFor(name, poolArenaMetrics)
      .append(collectSmallSubPageFamiliesFor(name, poolArenaMetrics))
      .append(collectChunkFamiliesFor(name, poolArenaMetrics));
  }

  private List<MetricFamilySamples> collectTinySubPageFamiliesFor(String name, List<PoolArenaMetric> poolArenaMetrics) {
    GaugeMetricFamily chunkCountMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_tinysubpage_count",
      "Decile chunked chunk counts in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily freeBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_tinysubpage_free_bytes",
      "Decile chunked free bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily allocatedBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_tinysubpage_allocated_bytes",
      "Decile chunked allocated bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );

    addMetrics(
      calculateSubpagePercentiles(
        poolArenaMetrics
          .bind(poolArenaMetric -> iterableList(poolArenaMetric.tinySubpages()))
      ),
      chunkCountMetricFamily,
      freeBytesMetricFamily,
      allocatedBytesMetricFamily
    );

    return List.list(chunkCountMetricFamily, freeBytesMetricFamily, allocatedBytesMetricFamily);
  }

  private List<MetricFamilySamples> collectSmallSubPageFamiliesFor(String name, List<PoolArenaMetric> poolArenaMetrics) {
    GaugeMetricFamily chunkCountMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_smallsubpage_count",
      "Decile chunked chunk counts in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily freeBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_smallsubpage_free_bytes",
      "Decile chunked free bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily allocatedBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_smallsubpage_allocated_bytes",
      "Decile chunked allocated bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );

    addMetrics(
      calculateSubpagePercentiles(
        poolArenaMetrics
          .bind(poolArenaMetric -> iterableList(poolArenaMetric.smallSubpages()))
      ),
      chunkCountMetricFamily,
      freeBytesMetricFamily,
      allocatedBytesMetricFamily
    );

    return List.list(chunkCountMetricFamily, freeBytesMetricFamily, allocatedBytesMetricFamily);
  }

  private List<MetricFamilySamples> collectChunkFamiliesFor(String name, List<PoolArenaMetric> poolArenaMetrics) {
    GaugeMetricFamily chunkCountMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_chunk_count",
      "Decile chunked chunk counts in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily freeBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_free_bytes",
      "Decile chunked free bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );
    GaugeMetricFamily allocatedBytesMetricFamily = new GaugeMetricFamily(
      "netty_pooledbytebufallocator_" + name + "_allocated_bytes",
      "Decile chunked allocated bytes in " + name + " arenas by percent free space",
      Lists.newArrayList("percentile")
    );


    List<PoolChunkMetric> chunkMetrics = poolArenaMetrics
      .bind(poolArenaMetric -> iterableList(poolArenaMetric.chunkLists()))
      .bind(List::iterableList);

    addMetrics(
      calculateChunkPercentiles(chunkMetrics),
      chunkCountMetricFamily,
      freeBytesMetricFamily,
      allocatedBytesMetricFamily
    );

    return List.list(chunkCountMetricFamily, freeBytesMetricFamily, allocatedBytesMetricFamily);
  }

  private void addMetrics(
    TreeMap<Integer, AllocationBucket> metrics,
    GaugeMetricFamily countMetricFamily,
    GaugeMetricFamily freeBytesMetricFamily,
    GaugeMetricFamily tinySubpageAllocatedBytesMetricFamily
  ) {
    metrics
      .forEach(
        integerChunkBucketP2 -> {
          String percentile = integerChunkBucketP2._1().toString();
          AllocationBucket bucket = integerChunkBucketP2._2();

          countMetricFamily.addMetric(Lists.newArrayList(percentile), bucket.allocationCount);
          freeBytesMetricFamily.addMetric(Lists.newArrayList(percentile), bucket.freeBytes);
          tinySubpageAllocatedBytesMetricFamily.addMetric(Lists.newArrayList(percentile), bucket.allocatedBytes);
        }
      );
  }

  private TreeMap<Integer, AllocationBucket> calculateChunkPercentiles(List<PoolChunkMetric> chunkMetrics) {
    return chunkMetrics.foldLeft(
      (percentiles, chunkMetric) -> {
        int usage = chunkMetric.usage();
        int percentile = Math.min(100, usage - (usage % 10) + 10);
        int freeBytes = chunkMetric.freeBytes();
        int allocatedBytes = chunkMetric.chunkSize();
        return percentiles.set(
          percentile,
          percentiles.get(percentile)
            .map(allocationBucket -> allocationBucket.add(freeBytes, allocatedBytes))
            .orSome(new AllocationBucket(freeBytes, allocatedBytes))
        );
      },
      TreeMap.treeMap(Ord.intOrd)
    );
  }

  private TreeMap<Integer, AllocationBucket> calculateSubpagePercentiles(List<PoolSubpageMetric> subpageMetrics) {
    return subpageMetrics.foldLeft(
      (percentiles, subpageMetric) -> {
        int usage = usage(subpageMetric);
        int percentile = Math.min(100, usage - (usage % 10) + 10);
        int freeBytes = subpageMetric.numAvailable() * subpageMetric.elementSize();
        int allocatedBytes = subpageMetric.pageSize();
        return percentiles.set(
          percentile,
          percentiles.get(percentile)
            .map(allocationBucket -> allocationBucket.add(freeBytes, allocatedBytes))
            .orSome(new AllocationBucket(freeBytes, allocatedBytes))
        );
      },
      TreeMap.treeMap(Ord.intOrd)
    );
  }

  private int usage(PoolSubpageMetric subpageMetric) {
    final int freeElements = subpageMetric.numAvailable();
    if (freeElements == 0) {
      return 100;
    }

    int freePercentage = (int) (freeElements * 100L / subpageMetric.maxNumElements());
    if (freePercentage == 0) {
      return 99;
    }
    return 100 - freePercentage;
  }

  private class AllocationBucket {
    private final int allocationCount;
    private final long freeBytes;
    private final long allocatedBytes;

    AllocationBucket(int allocationCount, long freeBytes, long allocatedBytes) {
      this.allocationCount = allocationCount;
      this.freeBytes = freeBytes;
      this.allocatedBytes = allocatedBytes;
    }

    AllocationBucket(long freeBytes, long allocatedBytes) {
      this(1, freeBytes, allocatedBytes);
    }

    AllocationBucket add(long freeBytes, long allocatedBytes) {
      return new AllocationBucket(
        this.allocationCount + 1,
        this.freeBytes + freeBytes,
        this.allocatedBytes + allocatedBytes
      );
    }
  }
}
