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

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseTelemetryType;
import com.io7m.repetoir.core.RPServiceType;
import com.io7m.stonesignal.server.errors.StErrorCode;
import com.io7m.stonesignal.server.errors.StException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * The type of server telemetry services.
 */

public interface StTelemetryServiceType
  extends RPServiceType, DDatabaseTelemetryType
{
  /**
   * @return The underlying OpenTelemetry service
   */

  OpenTelemetry openTelemetry();

  /**
   * @return The text map propagator for trace contexts
   */

  TextMapPropagator textMapPropagator();

  /**
   * @return The main tracer
   */

  @Override
  Tracer tracer();

  /**
   * @return The main meter
   */

  @Override
  Meter meter();

  /**
   * @return The main logger
   */

  @Override
  Logger logger();

  /**
   * Set the error kind for the current span.
   *
   * @param errorCode The error kind
   */

  static void setSpanErrorCode(
    final StErrorCode errorCode)
  {
    final var span = Span.current();
    span.setAttribute("stonesignal.errorCode", errorCode.id());
    span.setStatus(StatusCode.ERROR);
  }

  /**
   * Set the error kind for the current span.
   *
   * @param e The error kind
   */

  static void recordSpanException(
    final Throwable e)
  {
    final var span = Span.current();
    if (e instanceof final StException ex) {
      setSpanErrorCode(ex.errorCode());
    }
    if (e instanceof final DDatabaseException ex) {
      setSpanErrorCode(new StErrorCode(ex.errorCode()));
    }
    span.recordException(e);
    span.setStatus(StatusCode.ERROR);
  }

  /**
   * @return {@code true} if this telemetry is in no-op mode
   */

  @Override
  boolean isNoOp();
}
