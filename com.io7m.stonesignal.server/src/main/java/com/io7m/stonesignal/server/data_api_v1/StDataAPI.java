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

package com.io7m.stonesignal.server.data_api_v1;

import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.server.clock.StServerClock;
import com.io7m.stonesignal.server.configuration.StConfigurationServiceType;
import com.io7m.stonesignal.server.http.StHTTPRequestTimeFilter;
import com.io7m.stonesignal.server.telemetry.StMetricsServiceType;
import com.io7m.stonesignal.server.tls.StTLSContextServiceType;
import io.helidon.common.tls.TlsConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;

/**
 * A data API v1 server.
 */

public final class StDataAPI
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StDataAPI.class);

  private StDataAPI()
  {

  }

  /**
   * Create a data API v1 server.
   *
   * @param services The service directory
   *
   * @return A server
   *
   * @throws Exception On errors
   */

  public static WebServer create(
    final RPServiceDirectoryType services)
    throws Exception
  {
    final var configurationService =
      services.requireService(StConfigurationServiceType.class);
    final var tlsService =
      services.requireService(StTLSContextServiceType.class);
    final var configuration =
      configurationService.configuration();
    final var httpConfig =
      configuration.dataAPI();
    final var address =
      InetSocketAddress.createUnresolved(
        httpConfig.host(),
        httpConfig.port()
      );

    final var routingBuilder = HttpRouting.builder();
    routingBuilder.addFilter(
      new StHTTPRequestTimeFilter(
        services.requireService(StMetricsServiceType.class),
        services.requireService(StServerClock.class)
      )
    );

    routingBuilder.get(
      "/1/0/device-get-by-id",
      new St1DataHDeviceGetByID(services)
    );
    routingBuilder.post(
      "/1/0/device-get-by-id",
      new St1DataHDeviceGetByID(services)
    );

    routingBuilder.get(
      "/1/0/devices",
      new St1DataHDevicesGet(services)
    );
    routingBuilder.post(
      "/1/0/devices",
      new St1DataHDevicesGet(services)
    );

    routingBuilder.get(
      "/",
      new StVersions(services, 200)
    );
    routingBuilder.any(new StVersions(services, 404));

    final var webServerBuilder =
      WebServerConfig.builder();

    if (httpConfig.tls().isPresent()) {
      final var tls = httpConfig.tls().get();

      final var tlsContext =
        tlsService.create(
          "DataAPI",
          tls.keyStore(),
          tls.trustStore()
        );

      webServerBuilder.tls(
        TlsConfig.builder()
          .enabled(true)
          .sslContext(tlsContext.context())
          .build()
      );
    }

    final var webServer =
      webServerBuilder
        .port(httpConfig.port())
        .address(InetAddress.getByName(httpConfig.host()))
        .listenerSocketOptions(Map.ofEntries(
          Map.entry(SO_REUSEADDR, Boolean.TRUE),
          Map.entry(SO_REUSEPORT, Boolean.TRUE)
        ))
        .routing(routingBuilder)
        .build();

    webServer.start();
    LOG.info("[{}] Data API server started", address);
    return webServer;
  }
}
