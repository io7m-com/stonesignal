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


package com.io7m.stonesignal.protocol.device.v1;

import java.util.Set;

/**
 * The classes present in the V1 protocol.
 */

public final class St1DeviceProtocol
{
  private St1DeviceProtocol()
  {

  }

  /**
   * @return The set of classes present in the protocol
   */

  public static Set<Class<?>> classes()
  {
    return Set.of(
      St1DeviceError.class,
      St1DeviceLocationUpdate.class,
      St1DeviceMessageType.class,
      St1DeviceOK.class,
      String.class,
      double.class,
      int.class
    );
  }
}
