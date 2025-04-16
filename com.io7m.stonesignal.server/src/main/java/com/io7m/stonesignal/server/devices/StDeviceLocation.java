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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A location.
 */

// CHECKSTYLE:OFF

@JsonSerialize
@JsonDeserialize
public record StDeviceLocation(
  long id,
  UUID device,
  OffsetDateTime time,

  @JsonProperty(value = "Accuracy", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated horizontal accuracy radius in meters of this location.")
  double accuracy,

  @JsonProperty(value = "Altitude", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The altitude of this location in meters above the WGS84 reference ellipsoid.")
  double altitude,

  @JsonProperty(value = "Bearing", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The bearing at the time of this location in degrees. ")
  double bearing,

  @JsonProperty(value = "BearingAccuracy", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated bearing accuracy in degrees of this location.")
  double bearingAccuracy,

  @JsonProperty(value = "Latitude", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The latitude in degrees.")
  double latitude,

  @JsonProperty(value = "Longitude", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The longitude in degrees.")
  double longitude,

  @JsonProperty(value = "MSLAltitude", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The Mean Sea Level altitude of this location in meters.")
  double mslAltitude,

  @JsonProperty(value = "MSLAltitudeAccuracy", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated Mean Sea Level altitude accuracy in meters.")
  double mslAltitudeAccuracy,

  @JsonProperty(value = "Speed", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The speed at the time of this location in meters per second.")
  double speed,

  @JsonProperty(value = "SpeedAccuracy", defaultValue = "0.0")
  @JsonPropertyDescription(
    "The estimated speed accuracy in meters per second of this location.")
  double speedAccuracy)
{
  /**
   * A device location.
   *
   * @param id     The location update ID
   * @param device The location device
   * @param time   The location update time
   */

  public StDeviceLocation
  {
    Objects.requireNonNull(device, "device");
    Objects.requireNonNull(time, "time");
  }

  public static StDeviceLocation fromMap(
    final long id,
    final UUID device,
    final OffsetDateTime time,
    final Map<String, String> data)
  {
    return new StDeviceLocation(
      id,
      device,
      time,
      doubleOf(data, "Accuracy"),
      doubleOf(data, "Altitude"),
      doubleOf(data, "Bearing"),
      doubleOf(data, "BearingAccuracy"),
      doubleOf(data, "Latitude"),
      doubleOf(data, "Longitude"),
      doubleOf(data, "MSLAltitude"),
      doubleOf(data, "MSLAltitudeAccuracy"),
      doubleOf(data, "Speed"),
      doubleOf(data, "SpeedAccuracy")
    );
  }

  private static double doubleOf(
    final Map<String, String> data,
    final String name)
  {
    return Double.parseDouble(data.getOrDefault(name, "0.0"));
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
