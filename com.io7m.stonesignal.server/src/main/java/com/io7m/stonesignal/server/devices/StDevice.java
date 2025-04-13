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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A device.
 *
 * @param id       The device ID
 * @param key      The device key
 * @param name     The device name
 * @param metadata The device metadata
 */

public record StDevice(
  UUID id,
  StDeviceKey key,
  String name,
  Map<String, String> metadata)
{
  /**
   * A device.
   *
   * @param id       The device ID
   * @param key      The device key
   * @param name     The device name
   * @param metadata The device metadata
   */

  public StDevice
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(name, "name");
    metadata = Map.copyOf(metadata);
  }
}
