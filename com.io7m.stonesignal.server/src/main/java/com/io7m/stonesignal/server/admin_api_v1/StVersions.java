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


package com.io7m.stonesignal.server.admin_api_v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminError;
import com.io7m.stonesignal.server.StVersion;
import com.io7m.stonesignal.server.http.StFormat;
import com.io7m.stonesignal.server.http.StHTTPServerRequestContextExtractor;
import com.io7m.stonesignal.server.telemetry.StMetricsServiceType;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceType;
import com.io7m.ventrad.core.VProtocol;
import com.io7m.ventrad.core.VProtocols;
import io.helidon.http.HeaderNames;
import io.helidon.http.HttpException;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * The default versions handler.
 */

public final class StVersions implements Handler
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StVersions.class);

  private final StTelemetryServiceType telemetry;
  private final StMetricsServiceType metrics;
  private final int defaultStatus;
  private StFormat format;
  private UUID requestId;

  /**
   * The default versions handler.
   *
   * @param inServices      The services
   * @param inDefaultStatus The default status
   */

  public StVersions(
    final RPServiceDirectoryType inServices,
    final int inDefaultStatus)
  {
    this.telemetry =
      inServices.requireService(StTelemetryServiceType.class);
    this.metrics =
      inServices.requireService(StMetricsServiceType.class);
    this.defaultStatus =
      inDefaultStatus;
    this.format =
      StFormat.JSON;
  }

  @Override
  public void handle(
    final ServerRequest request,
    final ServerResponse response)
  {
    this.requestId = UUID.randomUUID();
    this.metrics.onHttpRequested();

    final var span =
      this.createSpan(request);

    try {
      this.format = StFormat.findFormat(request.headers());

      response.header("Content-Type", this.format.mediaType().text());
      response.header(
        "Server",
        String.format(
          "stonesignal %s %s",
          StVersion.version(),
          StVersion.build()
        )
      );

      this.sendVersions(response);
      this.recordMetrics(response);
    } catch (final HttpException e) {
      span.recordException(e);
      this.sendErrorHTTP(e, response);
    } finally {
      span.end();
    }
  }

  private void sendVersions(
    final ServerResponse response)
  {
    final var protocols =
      new VProtocols(
        List.of(
          new VProtocol(
            "com.io7m.stonesignal.admin",
            BigInteger.ONE,
            BigInteger.ZERO,
            URI.create("/1/0/"),
            "Stonesignal 1.0 Admin API"
          )
        )
      );

    try {
      response.status(this.defaultStatus);

      switch (this.format) {
        case JSON -> {
          response.send(new ObjectMapper().writeValueAsBytes(protocols));
        }
        case CBOR -> {
          response.send(new CBORMapper().writeValueAsBytes(protocols));
        }
      }
    } catch (final JsonProcessingException e) {
      LOG.debug("Error sending response: ", e);
    }
  }

  private void recordMetrics(
    final ServerResponse response)
  {
    this.recordResponseCode(response);
    this.recordResponseSize(response);
  }

  private void recordResponseSize(
    final ServerResponse response)
  {
    this.metrics.onHttpResponseSize(response.bytesWritten());
  }

  private void recordResponseCode(
    final ServerResponse response)
  {
    final var code = response.status().code();
    if (code >= 200 && code < 300) {
      this.metrics.onHttp2xx();
    }
    if (code >= 400 && code < 500) {
      this.metrics.onHttp4xx();
    }
    if (code >= 500) {
      this.metrics.onHttp5xx();
    }
  }

  private Span createSpan(
    final ServerRequest request)
  {
    final var context =
      this.telemetry.textMapPropagator()
        .extract(
          Context.current(),
          request,
          StHTTPServerRequestContextExtractor.instance()
        );

    final var tracer =
      this.telemetry.tracer();

    return tracer.spanBuilder(request.path().path())
      .setParent(context)
      .setStartTimestamp(Instant.now())
      .setSpanKind(SpanKind.SERVER)
      .setAttribute("http.client_ip", remoteAddress(request))
      .setAttribute("http.method", request.prologue().method().text())
      .setAttribute("http.request_path", request.path().path())
      .setAttribute("http.request_id", this.requestId.toString())
      .startSpan();
  }

  private static String remoteAddress(
    final ServerRequest request)
  {
    try {
      final var headers = request.headers();
      return headers.get(HeaderNames.X_FORWARDED_HOST).getString();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      final var peer = request.remotePeer();
      return "%s:%d".formatted(peer.host(), Integer.valueOf(peer.port()));
    }
  }

  private void sendErrorHTTP(
    final HttpException exception,
    final ServerResponse response)
  {
    try {
      final var code = exception.status().code();
      if (code >= 400) {
        if (code >= 500) {
          this.metrics.onHttp5xx();
        } else {
          this.metrics.onHttp4xx();
        }
      }

      switch (this.format) {
        case JSON -> {
          response.send(
            St1AdminProtocolMapperJSON.get()
              .writeValueAsBytes(
                new St1AdminError(
                  "error-" + code,
                  exception.getMessage()
                )
              )
          );
        }
        case CBOR -> {
          response.send(
            St1AdminProtocolMapperCBOR.get()
              .writeValueAsBytes(
                new St1AdminError(
                  "error-" + code,
                  exception.getMessage()
                )
              )
          );
        }
      }
    } catch (final JsonProcessingException e) {
      LOG.debug("Error sending response: ", e);
    }
  }
}
