/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.stonesignal.server.telemetry;

import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StVersion;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * An OpenTelemetry service factory.
 */

public final class StTelemetryServices
  implements StTelemetryServiceFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StTelemetryServices.class);

  /**
   * An OpenTelemetry service factory.
   */

  public StTelemetryServices()
  {

  }

  /**
   * Create a telemetry service.
   *
   * @param telemetryConfiguration The server configuration
   *
   * @return The service
   */

  @Override
  public StTelemetryServiceType create(
    final StConfiguration.OpenTelemetry telemetryConfiguration)
  {
    Objects.requireNonNull(telemetryConfiguration, "configuration");

    final var metricsOpt =
      telemetryConfiguration.metrics();
    final var tracesOpt =
      telemetryConfiguration.traces();
    final var logsOpt =
      telemetryConfiguration.logs();

    if (metricsOpt.isEmpty() && tracesOpt.isEmpty() && logsOpt.isEmpty()) {
      LOG.warn(
        "No metrics, logs, or trace configurations were provided; no telemetry will be sent!");
      return StTelemetryNoOp.noop();
    }

    final var resource =
      Resource.getDefault()
        .merge(Resource.create(
          Attributes.builder()
            .put("service.name", telemetryConfiguration.logicalServiceName())
            .put("service.version", StVersion.version())
            .build()
        ));

    final var builder =
      OpenTelemetrySdk.builder();

    metricsOpt.ifPresent(metrics -> {
      builder.setMeterProvider(createMeterProvider(resource, metrics));
    });

    tracesOpt.ifPresent(traces -> {
      builder.setTracerProvider(createTracerProvider(resource, traces));
    });

    logsOpt.ifPresent(logs -> {
      builder.setLoggerProvider(createLoggerProvider(resource, logs));
    });

    final var contextPropagators =
      ContextPropagators.create(W3CTraceContextPropagator.getInstance());

    final OpenTelemetry openTelemetry =
      builder.setPropagators(contextPropagators)
        .build();

    final var tracer =
      openTelemetry.getTracer(
        "com.io7m.stonesignal",
        StVersion.version()
      );

    final var meter =
      openTelemetry.getMeter(
        "com.io7m.stonesignal"
      );

    final var logger =
      openTelemetry.getLogsBridge()
        .loggerBuilder("com.io7m.stonesignal")
        .build();

    return new StTelemetryService(
      openTelemetry,
      tracer,
      meter,
      logger,
      openTelemetry.getPropagators().getTextMapPropagator()
    );
  }

  private static SdkMeterProvider createMeterProvider(
    final Resource resource,
    final StConfiguration.OpenTelemetry.Metrics metrics)
  {
    final var endpoint = metrics.endpoint().toString();
    LOG.info(
      "Metrics data will be sent to {} using {}",
      endpoint,
      metrics.protocol()
    );

    final var metricExporter =
      switch (metrics.protocol()) {
        case HTTP -> {
          yield OtlpHttpMetricExporter.builder()
            .setEndpoint(endpoint)
            .build();
        }
      };

    final var periodicMetricReader =
      PeriodicMetricReader.builder(metricExporter)
        .setInterval(1L, TimeUnit.SECONDS)
        .build();

    return SdkMeterProvider.builder()
      .registerMetricReader(periodicMetricReader)
      .setResource(resource)
      .build();
  }

  private static SdkLoggerProvider createLoggerProvider(
    final Resource resource,
    final StConfiguration.OpenTelemetry.Logs logs)
  {
    final var endpoint = logs.endpoint().toString();
    LOG.info(
      "Log data will be sent to {} using {}",
      endpoint,
      logs.protocol()
    );

    final var logExporter =
      switch (logs.protocol()) {
        case HTTP -> {
          yield OtlpHttpLogRecordExporter.builder()
            .setEndpoint(endpoint)
            .build();
        }
      };

    final var processor =
      BatchLogRecordProcessor.builder(logExporter)
        .build();

    return SdkLoggerProvider.builder()
      .addLogRecordProcessor(processor)
      .setResource(resource)
      .build();
  }

  private static SdkTracerProvider createTracerProvider(
    final Resource resource,
    final StConfiguration.OpenTelemetry.Traces traces)
  {
    final var endpoint = traces.endpoint().toString();
    LOG.info(
      "Trace data will be sent to {} using {}",
      endpoint,
      traces.protocol()
    );

    final var spanExporter =
      switch (traces.protocol()) {
        case HTTP -> {
          yield OtlpHttpSpanExporter.builder()
            .setEndpoint(endpoint)
            .build();
        }
      };

    final var batchSpanProcessor =
      BatchSpanProcessor.builder(spanExporter)
        .build();

    return SdkTracerProvider.builder()
      .addSpanProcessor(batchSpanProcessor)
      .setResource(resource)
      .build();
  }
}
