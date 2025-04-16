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
import com.io7m.stonesignal.server.devices.StDevice;
import com.io7m.stonesignal.server.devices.StDeviceKey;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;

import java.util.Map;
import java.util.Optional;

import static com.io7m.stonesignal.server.database.internal.Tables.DEVICES;

/**
 * device-get-by-id
 */

public final class StDBDeviceGetByID
  extends StDatabaseQueryAbstract<StDBDeviceGetByIDParameters, Optional<StDevice>>
  implements StDBDeviceGetByIDType
{
  StDBDeviceGetByID(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    StDBDeviceGetByIDParameters, Optional<StDevice>, StDBDeviceGetByIDType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDeviceGetByIDType.class,
      StDBDeviceGetByID::new
    );
  }

  @Override
  protected Optional<StDevice> onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final StDBDeviceGetByIDParameters device)
    throws Exception
  {
    this.record("DeviceID", device);

    final SelectSelectStep<?> baseSelect;
    if (device.includeKey()) {
      baseSelect =
        context.select(
          DEVICES.DEVICE_ID,
          DEVICES.DEVICE_KEY,
          DEVICES.DEVICE_METADATA,
          DEVICES.DEVICE_NAME
        );
    } else {
      baseSelect =
        context.select(
          DEVICES.DEVICE_ID,
          DSL.inline(StDeviceKey.redactedKey().key()).as(DEVICES.DEVICE_KEY),
          DEVICES.DEVICE_METADATA,
          DEVICES.DEVICE_NAME
        );
    }

    final var opt =
      baseSelect.from(DEVICES)
        .where(
          DEVICES.DEVICE_ID.eq(device.deviceId())
            .and(DEVICES.DEVICE_DELETED.eq(false)))
        .fetchOptional();

    if (opt.isPresent()) {
      final var v = opt.get();
      return Optional.of(
        new StDevice(
          v.get(DEVICES.DEVICE_ID),
          new StDeviceKey(v.get(DEVICES.DEVICE_KEY)),
          v.get(DEVICES.DEVICE_NAME),
          this.metadataJson(v.get(DEVICES.DEVICE_METADATA))
        )
      );
    }
    return Optional.empty();
  }

  @Override
  protected String name()
  {
    return "DeviceGetByID";
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
