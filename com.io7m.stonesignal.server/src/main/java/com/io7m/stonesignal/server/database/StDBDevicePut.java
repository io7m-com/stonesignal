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
import com.io7m.stonesignal.server.devices.StDevice;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.io7m.stonesignal.server.database.internal.Tables.AUDIT;
import static com.io7m.stonesignal.server.database.internal.Tables.DEVICES;

/**
 * The device-put query.
 */

public final class StDBDevicePut
  extends StDatabaseQueryAbstract<StDevice, DDatabaseUnit>
  implements StDBDevicePutType
{
  StDBDevicePut(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    StDevice, DDatabaseUnit, StDBDevicePutType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDevicePutType.class,
      StDBDevicePut::new
    );
  }

  @Override
  protected DDatabaseUnit onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final StDevice device)
    throws Exception
  {
    this.record("DeviceID", device.id());
    this.record("DeviceName", device.name());

    context.insertInto(DEVICES)
      .set(DEVICES.DEVICE_ID, device.id())
      .set(DEVICES.DEVICE_KEY, device.key().key())
      .set(DEVICES.DEVICE_METADATA, this.metadataJson(device.metadata()))
      .set(DEVICES.DEVICE_NAME, device.name())
      .onConflict(DEVICES.DEVICE_ID)
      .doUpdate()
      .set(DEVICES.DEVICE_KEY, device.key().key())
      .set(DEVICES.DEVICE_METADATA, this.metadataJson(device.metadata()))
      .set(DEVICES.DEVICE_NAME, device.name())
      .execute();

    context.insertInto(AUDIT)
      .set(AUDIT.AU_TIME, OffsetDateTime.now())
      .set(AUDIT.AU_TYPE, "DeviceUpdated")
      .set(AUDIT.AU_DATA, this.metadataJson(Map.ofEntries(
        Map.entry("DeviceID", device.id().toString()),
        Map.entry("DeviceName", device.name())
      )))
      .execute();

    return DDatabaseUnit.UNIT;
  }

  @Override
  protected String name()
  {
    return "DevicePut";
  }

  private JSON metadataJson(
    final Map<String, String> metadata)
    throws JsonProcessingException
  {
    return JSON.json(this.objectMapper().writeValueAsString(metadata));
  }
}
