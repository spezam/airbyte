/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.metrics.lib;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

public class OpenTelemetryMetricClient implements MetricClient {

  private Meter meter;

  @Override
  public void count(MetricsRegistry metric, double val, String... tags) {
    LongCounter counter = meter
        .counterBuilder(metric.getMetricName())
        .setDescription(metric.getMetricDescription())
        .setUnit("1")
        .build();

    AttributesBuilder attributesBuilder = Attributes.builder();
    for (String tag : tags) {
      attributesBuilder.put(stringKey(tag), tag);
    }

    counter.add((long) val, attributesBuilder.build());
  }

  @Override
  public void gauge(MetricsRegistry metric, double val, String... tags) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (String tag : tags) {
      attributesBuilder.put(stringKey(tag), tag);
    }
    meter.gaugeBuilder(metric.getMetricName()).setDescription(metric.getMetricDescription())
        .buildWithCallback(measurement -> measurement.record(val, attributesBuilder.build()));
  }

  @Override
  public void distribution(MetricsRegistry metric, double val, String... tags) {
    DoubleHistogram histogramMeter = meter.histogramBuilder(metric.getMetricName()).setDescription(metric.getMetricDescription()).build();
    AttributesBuilder attributesBuilder = Attributes.builder();

    for (String tag : tags) {
      attributesBuilder.put(stringKey(tag), tag);
    }
    histogramMeter.record(val, attributesBuilder.build());
  }

  public void initialize(MetricEmittingApp metricEmittingApp) {
    OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
    meter = openTelemetry.meterBuilder(metricEmittingApp.getApplicationName())
        .build();
  }

}
