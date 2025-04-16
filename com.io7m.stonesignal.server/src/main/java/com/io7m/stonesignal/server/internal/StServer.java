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

package com.io7m.stonesignal.server.internal;

import com.io7m.darco.api.DDatabaseException;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StServerType;
import com.io7m.stonesignal.server.admin_api_v1.StAdminAPI;
import com.io7m.stonesignal.server.clock.StServerClock;
import com.io7m.stonesignal.server.configuration.StConfigurationService;
import com.io7m.stonesignal.server.configuration.StConfigurationServiceType;
import com.io7m.stonesignal.server.data_api_v1.StDataAPI;
import com.io7m.stonesignal.server.database.StDatabaseConfiguration;
import com.io7m.stonesignal.server.database.StDatabaseFactory;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.device_api_v1.StDeviceAPI;
import com.io7m.stonesignal.server.errors.StErrorCode;
import com.io7m.stonesignal.server.errors.StException;
import com.io7m.stonesignal.server.telemetry.StMetricsService;
import com.io7m.stonesignal.server.telemetry.StMetricsServiceType;
import com.io7m.stonesignal.server.telemetry.StTelemetryNoOp;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceFactoryType;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceType;
import com.io7m.stonesignal.server.tls.StTLSContextService;
import com.io7m.stonesignal.server.tls.StTLSContextServiceType;
import com.io7m.stonesignal.server.tls.StTLSReloader;
import io.opentelemetry.api.trace.SpanKind;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.toUnsignedString;

/**
 * The basic server frontend.
 */

public final class StServer implements StServerType
{
  private final StConfiguration configuration;
  private final AtomicBoolean stopped;
  private final Clock clock;
  private CloseableCollectionType<StException> resources;
  private StTelemetryServiceType telemetry;
  private StDatabaseType database;

  /**
   * The basic server frontend.
   *
   * @param inClock         The clock
   * @param inConfiguration The server configuration
   */

  public StServer(
    final Clock inClock,
    final StConfiguration inConfiguration)
  {
    this.clock =
      Objects.requireNonNull(inClock, "clock");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.resources =
      createResourceCollection();
    this.stopped =
      new AtomicBoolean(true);
  }

  private static CloseableCollectionType<StException> createResourceCollection()
  {
    return CloseableCollection.create(
      () -> {
        return new StException(
          "Server creation failed.",
          new StErrorCode("server-creation"),
          Map.of(),
          Optional.empty()
        );
      }
    );
  }

  @Override
  public void start()
    throws StException
  {
    try {
      if (this.stopped.compareAndSet(true, false)) {
        this.resources = createResourceCollection();
        this.telemetry = this.createTelemetry();

        final var startupSpan =
          this.telemetry.tracer()
            .spanBuilder("StServer.start")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try {
          this.database =
            this.resources.add(this.createDatabase());
          final var services =
            this.resources.add(this.createServiceDirectory(this.database));

          final var publicAPI = StDeviceAPI.create(services);
          this.resources.add(publicAPI::stop);
          final var adminAPI = StAdminAPI.create(services);
          this.resources.add(adminAPI::stop);
          final var dataAPI = StDataAPI.create(services);
          this.resources.add(dataAPI::stop);
        } catch (final Exception e) {
          startupSpan.recordException(e);

          try {
            this.close();
          } catch (final StException ex) {
            e.addSuppressed(ex);
          }
          throw new StException(
            e.getMessage(),
            e,
            new StErrorCode("startup"),
            Map.of(),
            Optional.empty()
          );
        } finally {
          startupSpan.end();
        }
      }
    } catch (final Throwable e) {
      this.close();
      throw e;
    }
  }

  private RPServiceDirectoryType createServiceDirectory(
    final StDatabaseType newDatabase)
  {
    final var services = new RPServiceDirectory();

    services.register(StTelemetryServiceType.class, this.telemetry);
    services.register(StDatabaseType.class, newDatabase);

    final var metrics = new StMetricsService(this.telemetry);
    services.register(StMetricsServiceType.class, metrics);

    final var configService = new StConfigurationService(this.configuration);
    services.register(StConfigurationServiceType.class, configService);

    final var clockService = new StServerClock(this.clock);
    services.register(StServerClock.class, clockService);

    final var tls = StTLSContextService.createService(services);
    services.register(StTLSContextServiceType.class, tls);
    services.register(StTLSReloader.class, StTLSReloader.create(tls));
    return services;
  }

  private StDatabaseType createDatabase()
    throws DDatabaseException
  {
    final var databases =
      new StDatabaseFactory();

    final var databaseConfiguration =
      new StDatabaseConfiguration(
        this.configuration,
        this.telemetry
      );

    return databases.open(
      databaseConfiguration,
      s -> {

      });
  }

  private StTelemetryServiceType createTelemetry()
  {
    return this.configuration.openTelemetry()
      .flatMap(config -> {
        final var loader = ServiceLoader.load(StTelemetryServiceFactoryType.class);
        return loader.findFirst().map(f -> f.create(config));
      }).orElseGet(StTelemetryNoOp::noop);
  }

  @Override
  public StDatabaseType database()
  {
    if (this.stopped.get()) {
      throw new IllegalStateException("Server is not started.");
    }

    return this.database;
  }

  @Override
  public StConfiguration configuration()
  {
    return this.configuration;
  }

  @Override
  public boolean isClosed()
  {
    return this.stopped.get();
  }


  @Override
  public void close()
    throws StException
  {
    if (this.stopped.compareAndSet(false, true)) {
      this.resources.close();
    }
  }

  @Override
  public String toString()
  {
    return "[StServer 0x%s]".formatted(toUnsignedString(this.hashCode(), 16));
  }
}
