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

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseUnit;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.database.StDBAuditSearchParameters;
import com.io7m.stonesignal.server.database.StDBAuditSearchType;
import com.io7m.stonesignal.server.database.StDBDeviceGetByIDType;
import com.io7m.stonesignal.server.database.StDBDeviceGetByKeyType;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdatePutType;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdateSearchParameters;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdateSearchType;
import com.io7m.stonesignal.server.database.StDBDeviceLocationsGetType;
import com.io7m.stonesignal.server.database.StDBDevicePutType;
import com.io7m.stonesignal.server.database.StDatabaseConfiguration;
import com.io7m.stonesignal.server.database.StDatabaseFactory;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.devices.StDevice;
import com.io7m.stonesignal.server.devices.StDeviceKey;
import com.io7m.stonesignal.server.devices.StDeviceLocation;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.stonesignal.server.errors.StStandardErrorCodes.DEVICE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.stonesignal", disabledIfUnsupported = true)
public class StDatabaseTest
{
  private static StPostgresFixture POSTGRES_FIXTURE;
  private StDatabaseFactory databases;
  private StDatabaseType database;

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

    final var deviceAPI =
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

    this.database =
      this.databases.open(
        new StDatabaseConfiguration(
          new StConfiguration(
            dbConfig,
            Optional.empty(),
            deviceAPI,
            adminAPI,
            dataAPI
          ),
          DDatabaseTelemetryNoOp.get()
        ),
        event -> {

        }
      );
  }

  @Test
  public void testDevicePutGet()
    throws DDatabaseException
  {
    final var deviceBefore =
      new StDevice(
        UUID.randomUUID(),
        StDeviceKey.random(),
        "Device0",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    final var deviceAfter =
      new StDevice(
        deviceBefore.id(),
        StDeviceKey.random(),
        "Device1",
        Map.ofEntries(
          Map.entry("z", "q"),
          Map.entry("b", "c")
        ));

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDevicePutType.class);
      final var get0 = t.query(StDBDeviceGetByKeyType.class);
      final var get1 = t.query(StDBDeviceGetByIDType.class);

      p.execute(deviceBefore);
      t.commit();
      assertEquals(
        deviceBefore,
        get0.execute(deviceBefore.key()).orElseThrow());
      assertEquals(deviceBefore, get1.execute(deviceBefore.id()).orElseThrow());
      p.execute(deviceAfter);
      t.commit();
      assertEquals(deviceAfter, get0.execute(deviceAfter.key()).orElseThrow());
      assertEquals(deviceAfter, get1.execute(deviceAfter.id()).orElseThrow());

      assertEquals(
        Optional.empty(),
        get0.execute(StDeviceKey.random())
      );
      assertEquals(
        Optional.empty(),
        get1.execute(UUID.randomUUID())
      );
    }

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBAuditSearchType.class);
      final var e =
        p.execute(new StDBAuditSearchParameters(
          Optional.of("DeviceUpdated"),
          OffsetDateTime.now().minus(Duration.ofHours(1L)),
          1000
        ));
      assertEquals(2, e.size());
    }
  }

  @Test
  public void testDevicePutKeyConflict()
    throws DDatabaseException
  {
    final var device0 =
      new StDevice(
        UUID.randomUUID(),
        StDeviceKey.random(),
        "Device0",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    final var device1 =
      new StDevice(
        UUID.randomUUID(),
        device0.key(),
        "Device1",
        Map.ofEntries(
          Map.entry("x", "y"),
          Map.entry("a", "b")
        ));

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDevicePutType.class);
      p.execute(device0);
      t.commit();

      final var ex =
        assertThrows(DDatabaseException.class, () -> p.execute(device1));
      assertEquals(DEVICE_DUPLICATE.id(), ex.errorCode());
    }
  }

  @Test
  public void testDevicePutLocationUpdates()
    throws DDatabaseException
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
      final var q = t.query(StDBDeviceLocationUpdatePutType.class);
      final var s = t.query(StDBDeviceLocationUpdateSearchType.class);

      p.execute(device);

      OffsetDateTime first = null;
      OffsetDateTime time;

      for (int index = 0; index < 2500; ++index) {
        time = OffsetDateTime.now();
        if (first == null) {
          first = time;
        }

        q.execute(
          StDeviceLocation.fromMap(
            0L,
            device.id(),
            time,
            Map.of("Speed", "1.0")
          )
        );
      }

      t.commit();

      OffsetDateTime timeSearch = first;

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(600, r.size());
        timeSearch = r.getLast().time();
      }

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(600, r.size());
        timeSearch = r.getLast().time();
      }

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(600, r.size());
        timeSearch = r.getLast().time();
      }

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(600, r.size());
        timeSearch = r.getLast().time();
      }

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(104, r.size());
        timeSearch = r.getLast().time();
      }

      {
        final var r =
          s.execute(new StDBDeviceLocationUpdateSearchParameters(
            device.id(),
            timeSearch,
            600
          ));
        assertEquals(1, r.size());
        timeSearch = r.getLast().time();
      }
    }
  }

  @Test
  public void testDeviceLocationsGet()
    throws DDatabaseException
  {
    final var devices = new ArrayList<StDevice>();
    for (int index = 0; index < 5; ++index) {
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

    final var highestTime =
      new HashMap<UUID, OffsetDateTime>();
    final var highestData =
      new HashMap<UUID, String>();

    var time =
      OffsetDateTime.now()
        .withNano(0);

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDevicePutType.class);
      final var q = t.query(StDBDeviceLocationUpdatePutType.class);
      final var r = t.query(StDBDeviceLocationsGetType.class);

      for (final var device : devices) {
        p.execute(device);

        for (int index = 0; index < 100; ++index) {
          final var data = Integer.toString(100 - index);
          q.execute(StDeviceLocation.fromMap(
            0L,
            device.id(),
            time,
            Map.of("Speed", data)
          ));
          highestData.put(device.id(), data);
          highestTime.put(device.id(), time);
          time = time.plusSeconds(1L);
        }
      }

      t.commit();

      final var l = r.execute(DDatabaseUnit.UNIT);
      assertEquals(devices.size(), l.size());

      for (final var device : devices) {
        assertEquals(
          highestTime.get(device.id()),
          l.get(device.id()).time()
        );
        assertEquals(
          Double.parseDouble(highestData.get(device.id())),
          l.get(device.id()).speed()
        );
      }
    }
  }
}
