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

import com.io7m.stonesignal.server.devices.StDeviceLocation;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.io7m.stonesignal.server.database.internal.Tables.DEVICE_LOCATION_UPDATES;

/**
 * The device-location-update-search query.
 */

public final class StDBDeviceLocationUpdateSearch
  extends StDatabaseQueryAbstract<
  StDBDeviceLocationUpdateSearchParameters,
  List<StDeviceLocation>>
  implements StDBDeviceLocationUpdateSearchType
{
  StDBDeviceLocationUpdateSearch(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    StDBDeviceLocationUpdateSearchParameters,
    List<StDeviceLocation>,
    StDBDeviceLocationUpdateSearchType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDeviceLocationUpdateSearchType.class,
      StDBDeviceLocationUpdateSearch::new
    );
  }

  @Override
  protected List<StDeviceLocation> onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final StDBDeviceLocationUpdateSearchParameters search)
    throws Exception
  {
    final var matchDevice =
      DEVICE_LOCATION_UPDATES.DLU_DEVICE.eq(search.device());
    final var matchTime =
      DEVICE_LOCATION_UPDATES.DLU_TIME.ge(search.timeStart());
    final var conditions =
      DSL.and(matchDevice, matchTime);

    final var r =
      context.select(
          DEVICE_LOCATION_UPDATES.DLU_ID,
          DEVICE_LOCATION_UPDATES.DLU_DEVICE,
          DEVICE_LOCATION_UPDATES.DLU_TIME,
          DEVICE_LOCATION_UPDATES.DLU_DATA
        )
        .from(DEVICE_LOCATION_UPDATES)
        .where(conditions)
        .limit(search.count())
        .fetch();

    final var results =
      new ArrayList<StDeviceLocation>(r.size());

    final var mapper =
      this.objectMapper();

    for (final var rec : r) {
      results.add(
        StDeviceLocation.fromMap(
          rec.get(DEVICE_LOCATION_UPDATES.DLU_ID),
          rec.get(DEVICE_LOCATION_UPDATES.DLU_DEVICE),
          rec.get(DEVICE_LOCATION_UPDATES.DLU_TIME),
          mapper.readValue(
            rec.get(DEVICE_LOCATION_UPDATES.DLU_DATA).data(),
            Map.class
          )
        )
      );
    }

    return List.copyOf(results);
  }

  @Override
  protected String name()
  {
    return "DeviceLocationUpdateSearch";
  }

}
