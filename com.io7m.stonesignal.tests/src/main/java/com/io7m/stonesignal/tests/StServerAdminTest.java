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
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StServerType;
import com.io7m.stonesignal.server.StServers;
import com.io7m.stonesignal.server.admin_api_v1.St1AdminProtocolMapperCBOR;
import com.io7m.stonesignal.server.admin_api_v1.St1AdminProtocolMapperJSON;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.stonesignal", disabledIfUnsupported = true)
public class StServerAdminTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StServerAdminTest.class);

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

  @Test
  public void testAdminDevicePutGetJSON()
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
      new St1AdminDeviceGetResponse(
        Optional.of(
          new St1AdminDevice(
            device.id(),
            device.key().key(),
            device.name(),
            device.metadata())
        )
      );

    final var mapper =
      St1AdminProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-put"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
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
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testAdminDeviceGetMissingJSON()
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
      new St1AdminDeviceGetResponse(
        Optional.empty()
      );

    final var mapper =
      St1AdminProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testAdminDeviceGetUnauthorizedJSON()
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
      St1AdminProtocolMapperJSON.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Authorization", "B")
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Authorization", "B")
            .POST(HttpRequest.BodyPublishers.ofString(
              mapper.writeValueAsString(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofString());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }
    }
  }

  @Test
  public void testAdminDevicePutGetCBOR()
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
      new St1AdminDeviceGetResponse(
        Optional.of(
          new St1AdminDevice(
            device.id(),
            device.key().key(),
            device.name(),
            device.metadata())
        )
      );

    final var mapper =
      St1AdminProtocolMapperCBOR.get();

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
              mapper.writeValueAsBytes(
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
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testAdminDeviceGetMissingCBOR()
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
      new St1AdminDeviceGetResponse(
        Optional.empty()
      );

    final var mapper =
      St1AdminProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer " + this.configuration.adminAPI().apiKey())
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(200, r.statusCode());

        final var received =
          mapper.readValue(r.body(), St1AdminDeviceGetResponse.class);
        assertEquals(expectedResponse, received);
      }
    }
  }

  @Test
  public void testAdminDeviceGetUnauthorizedCBOR()
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
      St1AdminProtocolMapperCBOR.get();

    try (final var client = HttpClient.newHttpClient()) {
      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Content-Type", "application/stonesignal+cbor")
            .header(
              "Authorization",
              "Bearer F2AEC3AC60DBEB7D43B0C5F68453D515BF99EE3E6E888275D39AE1A9EFD522B2")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-id"))
            .header("Authorization", "B")
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(new St1AdminDeviceGetByID(device.id()))
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }

      {
        final var request =
          HttpRequest.newBuilder(
              URI.create("http://localhost:10001/1/0/device-get-by-key"))
            .header("Authorization", "B")
            .header("Content-Type", "application/stonesignal+cbor")
            .POST(HttpRequest.BodyPublishers.ofByteArray(
              mapper.writeValueAsBytes(
                new St1AdminDeviceGetByKey(device.key().key())
              )
            ))
            .build();

        final var r = client.send(request, BodyHandlers.ofByteArray());
        LOG.debug("Got: {}: {}", r.statusCode(), r.body());
        assertEquals(401, r.statusCode());
        mapper.readValue(r.body(), St1AdminError.class);
      }
    }
  }

  @Test
  public void testAdminDevicePutMalformedMetadataJSON()
    throws Exception
  {
    final var text = """
{
  "Device": {
    "DeviceID": "f057fc17-07ef-4b89-8aa1-004eb992a364",
    "DeviceKey": "ACCD3D5D5C3864083E371BD1E161C6071C3A847943CCCA6A833CECC1FA67D30C",
    "Name": "Fake",
    "Metadata": {
      "Manufacturer": 23,
      "What?": {}
    }
  }
}
      """;

    try (final var client = HttpClient.newHttpClient()) {
      final var request =
        HttpRequest.newBuilder(
            URI.create("http://localhost:10001/1/0/device-put"))
          .header(
            "Authorization",
            "Bearer " + this.configuration.adminAPI().apiKey())
          .POST(HttpRequest.BodyPublishers.ofString(text))
          .build();

      final var r = client.send(request, BodyHandlers.ofString());
      LOG.debug("Got: {}: {}", r.statusCode(), r.body());
      assertEquals(400, r.statusCode());
    }
  }
}
