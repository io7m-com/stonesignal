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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Get the location history of a device.
 */

// CHECKSTYLE:OFF

@JsonSerialize
@JsonDeserialize
public record St1DataDeviceLocationHistoryGet(
  @JsonProperty(value = "DeviceID", required = true)
  @JsonPropertyDescription("The device ID.")
  UUID deviceId,

  @JsonProperty(value = "TimeStart", required = true)
  @JsonPropertyDescription("The start time.")
  OffsetDateTime timeStart,

  @JsonProperty(value = "Count", required = true)
  @JsonPropertyDescription("The maximum number of events to return.")
  int count)
  implements St1DataMessageType
{
  public St1DataDeviceLocationHistoryGet
  {
    Objects.requireNonNull(deviceId, "deviceId");
    Objects.requireNonNull(timeStart, "timeStart");
  }
}
