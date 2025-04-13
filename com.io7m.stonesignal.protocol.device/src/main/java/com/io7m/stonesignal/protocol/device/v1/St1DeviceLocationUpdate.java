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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

/**
 * A location update.
 */

// CHECKSTYLE:OFF

@JsonSerialize
@JsonDeserialize
public record St1DeviceLocationUpdate(
  @JsonProperty(value = "ACC", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated horizontal accuracy radius in meters of this location.")
  double accuracy,

  @JsonProperty(value = "ALT", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The altitude of this location in meters above the WGS84 reference ellipsoid.")
  double altitude,

  @JsonProperty(value = "BE", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The bearing at the time of this location in degrees. ")
  double bearing,

  @JsonProperty(value = "BEACC", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated bearing accuracy in degrees of this location.")
  double bearingAccuracy,

  @JsonProperty(value = "LAT", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The latitude in degrees.")
  double latitude,

  @JsonProperty(value = "LONG", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The longitude in degrees.")
  double longitude,

  @JsonProperty(value = "MSLALT", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The Mean Sea Level altitude of this location in meters.")
  double mslAltitude,

  @JsonProperty(value = "MSLALTACC", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated Mean Sea Level altitude accuracy in meters.")
  double mslAltitudeAccuracy,

  @JsonProperty(value = "SP", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The speed at the time of this location in meters per second.")
  double speed,

  @JsonProperty(value = "SPACC", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated speed accuracy in meters per second of this location.")
  double speedAccuracy)
  implements St1DeviceMessageType
{
  /**
   * A location update.
   */

  public St1DeviceLocationUpdate
  {

  }

  /**
   * @return The location data as a string map
   */

  public Map<String, String> toMap()
  {
    return Map.ofEntries(
      Map.entry("Accuracy", Double.toString(this.accuracy)),
      Map.entry("Altitude", Double.toString(this.altitude)),
      Map.entry("Bearing", Double.toString(this.bearing)),
      Map.entry("BearingAccuracy", Double.toString(this.bearingAccuracy)),
      Map.entry("Latitude", Double.toString(this.latitude)),
      Map.entry("Longitude", Double.toString(this.longitude)),
      Map.entry("MSLAltitude", Double.toString(this.mslAltitude)),
      Map.entry("MSLAltitudeAccuracy", Double.toString(this.mslAltitudeAccuracy)),
      Map.entry("Speed", Double.toString(this.speed)),
      Map.entry("SpeedAccuracy", Double.toString(this.speedAccuracy))
    );
  }
}
