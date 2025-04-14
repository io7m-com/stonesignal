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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A device.
 */

// CHECKSTYLE:OFF

@JsonSerialize
@JsonDeserialize
public record St1AdminDevice(
  @JsonProperty(value = "DeviceID", required = true)
  @JsonPropertyDescription("The device ID.")
  UUID deviceId,

  @JsonProperty(value = "DeviceKey", required = true)
  @JsonPropertyDescription("The device key.")
  String deviceKey,

  @JsonProperty(value = "Name", required = true)
  @JsonPropertyDescription("The device name.")
  String name,

  @JsonProperty(value = "Metadata")
  @JsonPropertyDescription("The device metadata.")
  Map<String, String> metadata)
{
  public St1AdminDevice
  {
    Objects.requireNonNull(deviceId, "deviceId");
    Objects.requireNonNull(deviceKey, "deviceKey");
    Objects.requireNonNull(name, "name");
    metadata = Map.copyOf(metadata);
  }
}
