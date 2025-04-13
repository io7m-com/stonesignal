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
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDevice;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetByID;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetByKey;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetResponse;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDevicePut;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminError;
import com.io7m.stonesignal.protocol.data.v1.St1DataDevice;
import com.io7m.stonesignal.protocol.data.v1.St1DataDeviceGetByID;
import com.io7m.stonesignal.protocol.data.v1.St1DataDeviceGetResponse;
import com.io7m.stonesignal.protocol.data.v1.St1DataDevicesGet;
import com.io7m.stonesignal.protocol.data.v1.St1DataDevicesGetResponse;
import com.io7m.stonesignal.protocol.data.v1.St1DataError;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StServerType;
import com.io7m.stonesignal.server.StServers;
import com.io7m.stonesignal.server.admin_api_v1.St1AdminProtocolMapperCBOR;
import com.io7m.stonesignal.server.admin_api_v1.St1AdminProtocolMapperJSON;
import com.io7m.stonesignal.server.data_api_v1.St1DataProtocolMapperCBOR;
import com.io7m.stonesignal.server.data_api_v1.St1DataProtocolMapperJSON;
import com.io7m.stonesignal.server.database.StDatabaseConfiguration;
import com.io7m.stonesignal.server.database.StDatabaseFactory;
import com.io7m.stonesignal.server.database.StDatabaseType;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.stonesignal", disabledIfUnsupported = true)
public class StServerDataTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StServerDataTest.class);

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
        Optional.empty(),
        "localhost",
        POSTGRES_FIXTURE.port(),
        "stonesignal",
        true,
        1,
        10
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

  @Test
  public void testDataDevicePutGetJSON()
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

    final var expectedResponse =
      new St1DataDeviceGetResponse(
        Optional.of(
          new St1DataDevice(
            device.id(),
            device.name(),
            device.metadata())
        )
      );

    final var adminMapper =
      St1AdminProtocolMapperJSON.get();
    final var dataMapper =
      St1DataProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-put"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              adminMapper.writeValueAsString(
                new St1AdminDevicePut(
                  new St1AdminDevice(
                    device.id(),
                    device.key().key(),
                    device.name(),
                    device.metadata()
                  )
                )
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testDataDeviceGetMissingJSON()
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

    final var expectedResponse =
      new St1DataDeviceGetResponse(
        Optional.empty()
      );

    final var dataMapper =
      St1DataProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testDataDeviceGetUnauthorizedJSON()
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

    final var dataMapper =
      St1DataProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Authorization", "B")
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }
    }
  }

  @Test
  public void testDataDevicePutGetCBOR()
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

    final var expectedResponse =
      new St1DataDeviceGetResponse(
        Optional.of(
          new St1DataDevice(
            device.id(),
            device.name(),
            device.metadata())
        )
      );

    final var adminMapper =
      St1AdminProtocolMapperCBOR.get();
    final var dataMapper =
      St1DataProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-put"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              adminMapper.writeValueAsBytes(
                new St1AdminDevicePut(
                  new St1AdminDevice(
                    device.id(),
                    device.key().key(),
                    device.name(),
                    device.metadata()
                  )
                )
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testDataDeviceGetMissingCBOR()
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

    final var expectedResponse =
      new St1DataDeviceGetResponse(
        Optional.empty()
      );

    final var dataMapper =
      St1DataProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testDataDeviceGetUnauthorizedCBOR()
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

    final var dataMapper =
      St1DataProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/device-get-by-id"))
            .header("Authorization", "B")
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        dataMapper.readValue(r.body(), St1DataError.class);
      }
    }
  }

  @Test
  public void testDataDevicesJSON()
    throws Exception
  {
    final var devices = new ArrayList<StDevice>();
    for (int index = 0; index < 100; ++index) {
      devices.add(
        new StDevice(
          UUID.randomUUID(),
          StDeviceKey.random(),
          "Device " + index,
          Map.ofEntries(
            Map.entry("x", "y"),
            Map.entry("a", "b")
          )
        )
      );
    }

    final var adminMapper =
      St1AdminProtocolMapperJSON.get();
    final var dataMapper =
      St1DataProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      for (final var device : devices) {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-put"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              adminMapper.writeValueAsString(
                new St1AdminDevicePut(
                  new St1AdminDevice(
                    device.id(),
                    device.key().key(),
                    device.name(),
                    device.metadata()
                  )
                )
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/devices"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              dataMapper.writeValueAsString(new St1DataDevicesGet())
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDevicesGetResponse.class);

        for (final var device : devices) {
          assertEquals(
            device.name(),
            received.devices().get(device.id())
          );
        }
      }
    }
  }

  @Test
  public void testDataDevicesCBOR()
    throws Exception
  {
    final var devices = new ArrayList<StDevice>();
    for (int index = 0; index < 100; ++index) {
      devices.add(
        new StDevice(
          UUID.randomUUID(),
          StDeviceKey.random(),
          "Device " + index,
          Map.ofEntries(
            Map.entry("x", "y"),
            Map.entry("a", "b")
          )
        )
      );
    }

    final var adminMapper =
      St1AdminProtocolMapperCBOR.get();
    final var dataMapper =
      St1DataProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      for (final var device : devices) {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-put"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              adminMapper.writeValueAsBytes(
                new St1AdminDevicePut(
                  new St1AdminDevice(
                    device.id(),
                    device.key().key(),
                    device.name(),
                    device.metadata()
                  )
                )
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10002/1/0/devices"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.dataAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              dataMapper.writeValueAsBytes(new St1DataDevicesGet())
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          dataMapper.readValue(r.body(), St1DataDevicesGetResponse.class);

        for (final var device : devices) {
          assertEquals(
            device.name(),
            received.devices().get(device.id())
          );
        }
      }
    }
  }
}
