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
import com.io7m.stonesignal.server.devices.StDeviceLocationUpdate;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.util.Map;

import static com.io7m.stonesignal.server.database.internal.Tables.DEVICE_LOCATION_UPDATES;

/**
 * The device-location-update-put query.
 */

public final class StDBDeviceLocationUpdatePut
  extends StDatabaseQueryAbstract<StDeviceLocationUpdate, DDatabaseUnit>
  implements StDBDeviceLocationUpdatePutType
{
  StDBDeviceLocationUpdatePut(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    StDeviceLocationUpdate, DDatabaseUnit, StDBDeviceLocationUpdatePutType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDeviceLocationUpdatePutType.class,
      StDBDeviceLocationUpdatePut::new
    );
  }

  @Override
  protected DDatabaseUnit onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final StDeviceLocationUpdate update)
    throws Exception
  {
    this.record("DeviceID", update.device());

    context.insertInto(DEVICE_LOCATION_UPDATES)
      .set(DEVICE_LOCATION_UPDATES.DLU_TIME, update.time())
      .set(DEVICE_LOCATION_UPDATES.DLU_DEVICE, update.device())
      .set(DEVICE_LOCATION_UPDATES.DLU_DATA, this.metadataJson(update.data()))
      .execute();

    return DDatabaseUnit.UNIT;
  }

  @Override
  protected String name()
  {
    return "DeviceLocationUpdatePut";
  }

  private JSON metadataJson(
    final Map<String, String> metadata)
    throws JsonProcessingException
  {
    return JSON.json(this.objectMapper().writeValueAsString(metadata));
  }
}
