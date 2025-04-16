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

package com.io7m.stonesignal.server.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io7m.darco.api.DDatabaseUnit;
import com.io7m.stonesignal.server.devices.StDeviceLocation;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.io7m.stonesignal.server.database.internal.Tables.DEVICE_LOCATION_UPDATES;

/**
 * device-locations
 */

public final class StDBDeviceLocationsGet
  extends StDatabaseQueryAbstract<DDatabaseUnit, SortedMap<UUID, StDeviceLocation>>
  implements StDBDeviceLocationsGetType
{
  StDBDeviceLocationsGet(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    DDatabaseUnit, SortedMap<UUID, StDeviceLocation>, StDBDeviceLocationsGetType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDeviceLocationsGetType.class,
      StDBDeviceLocationsGet::new
    );
  }

  @Override
  protected SortedMap<UUID, StDeviceLocation> onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final DDatabaseUnit ignored)
    throws JsonProcessingException
  {
    final var r =
      context.selectDistinct(
          DEVICE_LOCATION_UPDATES.DLU_ID,
          DEVICE_LOCATION_UPDATES.DLU_DATA,
          DEVICE_LOCATION_UPDATES.DLU_TIME,
          DEVICE_LOCATION_UPDATES.DLU_DEVICE
        ).on(DEVICE_LOCATION_UPDATES.DLU_DEVICE)
        .from(DEVICE_LOCATION_UPDATES)
        .orderBy(
          DEVICE_LOCATION_UPDATES.DLU_DEVICE,
          DEVICE_LOCATION_UPDATES.DLU_TIME.desc())
        .fetch();

    final var m = new TreeMap<UUID, StDeviceLocation>();
    for (final var rec : r) {
      final var id =
        rec.get(DEVICE_LOCATION_UPDATES.DLU_ID);
      final var device =
        rec.get(DEVICE_LOCATION_UPDATES.DLU_DEVICE);
      final var time =
        rec.get(DEVICE_LOCATION_UPDATES.DLU_TIME);
      final var data =
        rec.get(DEVICE_LOCATION_UPDATES.DLU_DATA);

      m.put(
        device,
        StDeviceLocation.fromMap(
          id,
          device,
          time,
          metadataJson(data)
        )
      );
    }
    return m;
  }

  @Override
  protected String name()
  {
    return "DeviceLocationsGet";
  }

  private Map<String, String> metadataJson(
    final JSON metadata)
    throws JsonProcessingException
  {
    return this.objectMapper().readValue(
      metadata.data(),
      Map.class
    );
  }
}
