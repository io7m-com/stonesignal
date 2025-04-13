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


package com.io7m.stonesignal.protocol.data.v1;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The classes present in the V1 protocol.
 */

public final class St1DataProtocol
{
  private St1DataProtocol()
  {

  }

  /**
   * @return The set of classes present in the protocol
   */

  public static Set<String> classNames()
  {
    return Stream.concat(
      Stream.of(
        "java.util.Map<java.lang.String,java.lang.String>",
        "java.util.Map<java.util.UUID,java.lang.String>",
        "java.util.Map<java.util.UUID,com.io7m.stonesignal.protocol.data.v1.St1DataLocation>"
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
      St1DataDevice.class,
      St1DataDeviceGetByID.class,
      St1DataDeviceGetResponse.class,
      St1DataDeviceLocationsGet.class,
      St1DataDeviceLocationsGetResponse.class,
      St1DataDevicesGet.class,
      St1DataDevicesGetResponse.class,
      St1DataError.class,
      St1DataLocation.class,
      St1DataMessageType.class,
      OffsetDateTime.class,
      String.class,
      UUID.class,
      double.class,
      int.class
    );
  }
}
