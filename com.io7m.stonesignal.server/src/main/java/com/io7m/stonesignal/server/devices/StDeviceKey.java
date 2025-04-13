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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A device key.
 *
 * @param key The device key
 */

public record StDeviceKey(
  String key)
{
  /**
   * The pattern that defines a valid key.
   */

  public static final Pattern VALID_KEY =
    Pattern.compile("[A-F0-9]{64}");

  /**
   * A device key.
   *
   * @param key The device key
   */

  public StDeviceKey
  {
    if (!VALID_KEY.matcher(key).matches()) {
      throw new IllegalArgumentException(
        "Device keys must match %s".formatted(VALID_KEY)
      );
    }
  }

  /**
   * @return A strong random device key
   */

  public static StDeviceKey random()
  {
    final var key = new byte[32];
    try {
      SecureRandom.getInstanceStrong().nextBytes(key);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    return new StDeviceKey(
      HexFormat.of()
        .formatHex(key)
        .toUpperCase(Locale.ROOT)
    );
  }
}
