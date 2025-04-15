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


package com.io7m.stonesignal.tests;

import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.percentpass.extension.MinimumPassing;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceError;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceLocationUpdate;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StServerType;
import com.io7m.stonesignal.server.StServers;
import com.io7m.stonesignal.server.admin_api_v1.St1AdminProtocolMapperJSON;
import com.io7m.stonesignal.server.database.StDBDevicePutType;
import com.io7m.stonesignal.server.database.StDatabaseConfiguration;
import com.io7m.stonesignal.server.database.StDatabaseFactory;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.device_api_v1.St1DeviceProtocolMapperCBOR;
import com.io7m.stonesignal.server.device_api_v1.St1DeviceProtocolMapperJSON;
import com.io7m.stonesignal.server.devices.StDevice;
import com.io7m.stonesignal.server.devices.StDeviceKey;
import com.io7m.stonesignal.server.errors.StException;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.stonesignal", disabledIfUnsupported = true)
public class StServerDeviceTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StServerDeviceTest.class);

  private static StPostgresFixture POSTGRES_FIXTURE;
  private StDatabaseFactory databases;
  private StDatabaseType database;
  private StServerType server;
  private StConfiguration configuration;

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType containers)
    throws Exception
  {
    POSTGRES_FIXTURE =
      StFixtures.postgres(StFixtures.pod(containers));
  }

  @BeforeEach
  public void setup()
    throws Exception
  {
    POSTGRES_FIXTURE.reset();

    this.databases =
      new StDatabaseFactory();

    final var dbConfig =
      new StConfiguration.Database(
        StConfiguration.DatabaseKind.POSTGRESQL,
        "postgresql",
        "12345678",
        "12345678",
        "12345678",
        "12345678",
        "localhost",
        POSTGRES_FIXTURE.port(),
        "stonesignal",
        true
      );

    final var publicAPI =
      new StConfiguration.DeviceAPI(
        "localhost",
        10000,
        Optional.empty()
      );

    final var adminAPI =
      new StConfiguration.AdminAPI(
        "localhost",
        10001,
        "2E166AC102F43DAB23E7A291138C6D0EDE0D59B1AAA9B21F44443D4DE819AB04",
        Optional.empty()
      );

    final var dataAPI =
      new StConfiguration.DataAPI(
        "localhost",
        10002,
        "FB241A9C3009F0A73C09114780275EEF9B825F23317E9C3F783FECA3A0698D52",
        Optional.empty()
      );

    final var databaseConfiguration =
      new StDatabaseConfiguration(
        new StConfiguration(
          dbConfig,
          Optional.empty(),
          publicAPI,
          adminAPI,
          dataAPI
        ),
        DDatabaseTelemetryNoOp.get()
      );

    this.database =
      this.databases.open(
        databaseConfiguration,
        event -> {

        }
      );

    this.configuration =
      new StConfiguration(
        dbConfig,
        Optional.empty(),
        publicAPI,
        adminAPI,
        dataAPI
      );

    final var servers = new StServers();
    this.server = servers.createServer(Clock.systemUTC(), configuration);
    this.server.start();

    Thread.sleep(250L);
  }

  @AfterEach
  public void tearDown()
    throws StException
  {
    this.server.close();
  }

  @MinimumPassing(executionCount = 5, passMinimum = 2)
  public void testDeviceUpdateWorkflow()
    throws Exception
  {
    final var device =
      new StDevice(
        UUID.randomUUID(),
        StDeviceKey.random(),
        "Device0",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDevicePutType.class);
      p.execute(device);
      t.commit();
    }

    LOG.debug("Waiting for server to settle...");
    Thread.sleep(1000L);
    LOG.debug("Making request...");

    final var mapper =
      St1AdminProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      final var request =
        HttpRequest.newBuilder(
            URI.create("http://localhost:10000/1/0/device-location-put"))
          .header("Authorization", "Bearer " + device.key().key())
          .POST(
            HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1DeviceLocationUpdate(
                  1.0,
                  2.0,
                  3.0,
                  4.0,
                  5.0,
                  6.0,
                  7.0,
                  8.0,
                  9.0,
                  10.0
                )
              )
            ))
          .build();

      final var r = client.send(request, BodyHandlers.discarding());
      LOG.debug("Got: {}", r.statusCode());
      assertEquals(200, r.statusCode());
    }
  }

  @Test
  public void testDeviceUpdateUnauthorizedJSON()
    throws Exception
  {
    final var device =
      new StDevice(
        UUID.randomUUID(),
        StDeviceKey.random(),
        "Device0",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    final var mapper =
      St1DeviceProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      final var cmd =
        new St1DeviceLocationUpdate(
        1.0,
        2.0,
        3.0,
        4.0,
        5.0,
        6.0,
        7.0,
        8.0,
        9.0,
        10.0
      );

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .header("Authorization", "Bearer " + device.key().key())
            .POST(
              HttpRequest.BodyPublishers.ofString(
                mapper.writeValueAsString(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .header("Authorization", "Bearer ")
            .POST(
              HttpRequest.BodyPublishers.ofString(
                mapper.writeValueAsString(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .POST(
              HttpRequest.BodyPublishers.ofString(
                mapper.writeValueAsString(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }
    }
  }

  @Test
  public void testDeviceUpdateUnauthorizedCBOR()
    throws Exception
  {
    final var device =
      new StDevice(
        UUID.randomUUID(),
        StDeviceKey.random(),
        "Device0",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    final var mapper =
      St1DeviceProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      final var cmd =
        new St1DeviceLocationUpdate(
          1.0,
          2.0,
          3.0,
          4.0,
          5.0,
          6.0,
          7.0,
          8.0,
          9.0,
          10.0
        );

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header("Authorization", "Bearer " + device.key().key())
            .POST(
              HttpRequest.BodyPublishers.ofByteArray(
                mapper.writeValueAsBytes(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header("Authorization", "Bearer ")
            .POST(
              HttpRequest.BodyPublishers.ofByteArray(
                mapper.writeValueAsBytes(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10000/1/0/device-location-put"))
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(
              HttpRequest.BodyPublishers.ofByteArray(
                mapper.writeValueAsBytes(cmd)
              ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}", r.statusCode());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1DeviceError.class);
      }
    }
  }

}
