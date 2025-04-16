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

import com.io7m.darco.api.DDatabaseUnit;
import org.jooq.DSLContext;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.io7m.stonesignal.server.database.internal.Tables.DEVICES;

/**
 * devices
 */

public final class StDBDevicesGet
  extends StDatabaseQueryAbstract<DDatabaseUnit, SortedMap<UUID, String>>
  implements StDBDevicesGetType
{
  StDBDevicesGet(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    DDatabaseUnit, SortedMap<UUID, String>, StDBDevicesGetType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBDevicesGetType.class,
      StDBDevicesGet::new
    );
  }

  @Override
  protected SortedMap<UUID, String> onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final DDatabaseUnit ignored)
  {
    final var r =
      context.select(
          DEVICES.DEVICE_ID,
          DEVICES.DEVICE_NAME
        ).from(DEVICES)
        .fetch();

    final var m = new TreeMap<UUID, String>();
    for (final var rec : r) {
      final var id =
        rec.get(DEVICES.DEVICE_ID);
      final var name =
        rec.get(DEVICES.DEVICE_NAME);
      m.put(id, name);
    }
    return m;
  }

  @Override
  protected String name()
  {
    return "DevicesGet";
  }
}
