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


package com.io7m.stonesignal.server.devices;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A device location update.
 *
 * @param id     The location update ID
 * @param device The location device
 * @param time   The location update time
 * @param data   The location update data
 */

public record StDeviceLocationUpdate(
  long id,
  UUID device,
  OffsetDateTime time,
  Map<String, String> data)
{
  /**
   * A device location update.
   *
   * @param id     The location update ID
   * @param device The location device
   * @param time   The location update time
   * @param data   The location update data
   */

  public StDeviceLocationUpdate
  {
    Objects.requireNonNull(device, "device");
    Objects.requireNonNull(time, "time");
    data = Map.copyOf(data);
  }
}
