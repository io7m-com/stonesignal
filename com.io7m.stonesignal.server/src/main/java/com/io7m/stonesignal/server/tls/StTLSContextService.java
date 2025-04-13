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

package com.io7m.stonesignal.server.tls;

import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.errors.StException;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.io7m.stonesignal.server.errors.StStandardErrorCodes.IO_ERROR;
import static com.io7m.stonesignal.server.telemetry.StTelemetryServiceType.recordSpanException;

/**
 * The TLS context service.
 */

public final class StTLSContextService
  implements StTLSContextServiceType
{
  private final ConcurrentHashMap.KeySetView<StTLSContext, Boolean> contexts;
  private final StTelemetryServiceType telemetry;

  private StTLSContextService(
    final StTelemetryServiceType inTelemetry)
  {
    this.telemetry =
      Objects.requireNonNull(inTelemetry, "telemetry");
    this.contexts =
      ConcurrentHashMap.newKeySet();
  }

  @Override
  public String toString()
  {
    return "[StTLSContextService 0x%x]"
      .formatted(Integer.valueOf(this.hashCode()));
  }

  /**
   * @param services The service directory
   *
   * @return A new TLS context service
   */

  public static StTLSContextServiceType createService(
    final RPServiceDirectoryType services)
  {
    return new StTLSContextService(
      services.requireService(StTelemetryServiceType.class)
    );
  }

  @Override
  public StTLSContext create(
    final String user,
    final StConfiguration.TLSStore keyStoreConfiguration,
    final StConfiguration.TLSStore trustStoreConfiguration)
    throws StException
  {
    try {
      final var newContext =
        StTLSContext.create(
          user,
          keyStoreConfiguration,
          trustStoreConfiguration
        );
      this.contexts.add(newContext);
      return newContext;
    } catch (final IOException e) {
      throw errorIO(e);
    } catch (final GeneralSecurityException e) {
      throw errorSecurity(e);
    }
  }

  @Override
  public void reload()
  {
    final var span =
      this.telemetry.tracer()
        .spanBuilder("ReloadTLSContexts")
        .startSpan();

    try (final var ignored = span.makeCurrent()) {
      for (final var context : this.contexts) {
        this.reloadContext(context);
      }
    } finally {
      span.end();
    }
  }

  private void reloadContext(
    final StTLSContext context)
  {
    final var span =
      this.telemetry.tracer()
        .spanBuilder("ReloadTLSContext")
        .startSpan();

    try (final var ignored = span.makeCurrent()) {
      context.reload();
    } catch (final Throwable e) {
      recordSpanException(e);
    } finally {
      span.end();
    }
  }

  @Override
  public String description()
  {
    return "The TLS context service.";
  }

  private static StException errorIO(
    final IOException e)
  {
    return new StException(
      "I/O exception",
      e,
      IO_ERROR,
      Map.of(),
      Optional.empty()
    );
  }

  private static StException errorSecurity(
    final GeneralSecurityException e)
  {
    return new StException(
      e.getMessage(),
      e,
      IO_ERROR,
      Map.of(),
      Optional.empty()
    );
  }
}
