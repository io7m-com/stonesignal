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


package com.io7m.stonesignal.server.device_api_v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceError;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceMessageType;
import com.io7m.stonesignal.server.StVersion;
import com.io7m.stonesignal.server.database.StDBDeviceGetByKeyType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.devices.StDeviceKey;
import com.io7m.stonesignal.server.http.StFormat;
import com.io7m.stonesignal.server.http.StHTTPServerRequestContextExtractor;
import com.io7m.stonesignal.server.telemetry.StMetricsServiceType;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceType;
import io.helidon.http.HeaderNames;
import io.helidon.http.HttpException;
import io.helidon.http.ServerRequestHeaders;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The v1 bearer-token authenticated handler.
 */

abstract class St1BearerHandler implements Handler
{
  private static final Logger LOG =
    LoggerFactory.getLogger(St1BearerHandler.class);

  private static final Pattern PATTERN =
    Pattern.compile("^Bearer ([A-F0-9]{64})$");

  private final StDatabaseType database;
  private final StTelemetryServiceType telemetry;
  private final StMetricsServiceType metrics;

  protected St1BearerHandler(
    final RPServiceDirectoryType inServices)
  {
    Objects.requireNonNull(inServices, "services");

    this.database =
      inServices.requireService(StDatabaseType.class);
    this.telemetry =
      inServices.requireService(StTelemetryServiceType.class);
    this.metrics =
      inServices.requireService(StMetricsServiceType.class);
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

  @Override
  public final void handle(
    final ServerRequest request,
    final ServerResponse response)
  {
    final var context = request.context();
    final var state = new St1DeviceState();
    context.register(state);

    this.metrics.onHttpRequested();

    final var span =
      this.createSpan(state, request);

    try {
      final var headers = request.headers();
      state.setFormat(StFormat.findFormat(headers));

      response.header("Content-Type", state.format().mediaType().text());
      this.findAuthorization(state, headers);
      this.checkAuthorization(state);

      response.header(
        "Server",
        String.format(
          "stonesignal %s %s",
          StVersion.version(),
          StVersion.build()
        )
      );

      this.handleAuthenticated(request, response);
    } catch (final HttpException e) {
      response.status(e.status());
      span.recordException(e);
      this.sendErrorHTTP(state, e, response);
    } catch (final DDatabaseException e) {
      span.recordException(e);
      this.sendErrorDatabase(state, e, response);
    } catch (final Throwable e) {
      span.recordException(e);
      this.sendErrorAny(state, e, response);
    } finally {
      this.recordMetrics(response);
      span.end();
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
    final St1DeviceState state,
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
      .setAttribute("http.request_id", state.requestId().toString())
      .setAttribute("stonesignal.endpoint", this.name())
      .startSpan();
  }

  private void checkAuthorization(
    final St1DeviceState state)
    throws DDatabaseException
  {
    try (final var transaction = this.database.openTransaction()) {
      final var q =
        transaction.query(StDBDeviceGetByKeyType.class);
      final var dOpt =
        q.execute(state.token());

      if (dOpt.isEmpty()) {
        throw new HttpException(
          Status.UNAUTHORIZED_401.reasonPhrase(),
          Status.UNAUTHORIZED_401
        );
      }
      state.setDevice(dOpt.get());
    }
  }

  protected abstract void handleAuthenticated(
    final ServerRequest request,
    final ServerResponse response)
    throws IOException, DDatabaseException;

  protected abstract String name();

  private static void setStatusIfNecessary(
    final ServerResponse response)
  {
    if (response.status().code() < 400) {
      response.status(500);
    }
  }

  private void sendErrorDatabase(
    final St1DeviceState state,
    final DDatabaseException exception,
    final ServerResponse response)
  {
    setStatusIfNecessary(response);
    this.send(
      state,
      response,
      new St1DeviceError(
        exception.errorCode(),
        exception.getMessage()
      )
    );
  }

  private void sendErrorHTTP(
    final St1DeviceState state,
    final HttpException exception,
    final ServerResponse response)
  {
    this.send(
      state,
      response,
      new St1DeviceError(
        "error-" + response.status().code(),
        exception.getMessage()
      )
    );
  }

  private void sendErrorAny(
    final St1DeviceState state,
    final Throwable exception,
    final ServerResponse response)
  {
    final var message =
      Objects.requireNonNullElse(exception.getMessage(), "I/O error");

    setStatusIfNecessary(response);
    this.send(
      state,
      response,
      new St1DeviceError("error-general", message)
    );
  }

  protected final <C extends St1DeviceMessageType> C read(
    final St1DeviceState state,
    final ServerRequest request,
    final Class<C> clazz)
    throws IOException
  {
    try (final var stream = request.content().inputStream()) {
      return switch (state.format()) {
        case JSON -> St1DeviceProtocolMapperJSON.get().readValue(stream, clazz);
        case CBOR -> St1DeviceProtocolMapperCBOR.get().readValue(stream, clazz);
      };
    }
  }

  private void findAuthorization(
    final St1DeviceState state,
    final ServerRequestHeaders headers)
  {
    try {
      final var bearer = headers.get(HeaderNames.AUTHORIZATION);
      for (final var value : bearer.allValues()) {
        final var matcher = PATTERN.matcher(value);
        if (matcher.matches()) {
          state.setToken(new StDeviceKey(matcher.group(1)));
          return;
        }
      }
      throw new HttpException(
        Status.UNAUTHORIZED_401.reasonPhrase(),
        Status.UNAUTHORIZED_401
      );
    } catch (final NoSuchElementException e) {
      throw new HttpException(
        Status.UNAUTHORIZED_401.reasonPhrase(),
        Status.UNAUTHORIZED_401
      );
    }
  }

  protected final void send(
    final St1DeviceState state,
    final ServerResponse response,
    final St1DeviceMessageType message)
  {
    try {
      response.send(
        switch (state.format()) {
          case JSON ->
            St1DeviceProtocolMapperJSON.get().writeValueAsBytes(message);
          case CBOR ->
            St1DeviceProtocolMapperCBOR.get().writeValueAsBytes(message);
        }
      );
    } catch (final JsonProcessingException e) {
      LOG.debug("Error sending response: ", e);
    }
  }
}
