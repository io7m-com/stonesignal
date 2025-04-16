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


package com.io7m.stonesignal.protocol.admin.v1;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The classes present in the V1 protocol.
 */

public final class St1AdminProtocol
{
  private St1AdminProtocol()
  {

  }

  /**
   * @return The set of classes present in the protocol
   */

  public static Set<String> classNames()
  {
    return Stream.concat(
      Stream.of(
        "java.util.Map<java.lang.String,java.lang.String>"
      ),
      classes()
        .stream()
        .map(Class::getCanonicalName)
    ).collect(Collectors.toSet());
  }

  /**
   * @return The set of classes present in the protocol
   */

  private static Set<Class<?>> classes()
  {
    return Set.of(
      St1AdminDevice.class,
      St1AdminDeviceGetByID.class,
      St1AdminDeviceGetByKey.class,
      St1AdminDeviceGetResponse.class,
      St1AdminDevicePut.class,
      St1AdminError.class,
      St1AdminMessageType.class,
      St1AdminOK.class,
      String.class,
      UUID.class,
      int.class
    );
  }
}
