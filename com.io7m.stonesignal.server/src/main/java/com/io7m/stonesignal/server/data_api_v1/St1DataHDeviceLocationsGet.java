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


package com.io7m.stonesignal.server.data_api_v1;

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseUnit;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationsGetResponse;
import com.io7m.stonesignal.protocol.data.v1.St1DataDevicesGet;
import com.io7m.stonesignal.protocol.data.v1.St1DataLocation;
import com.io7m.stonesignal.server.database.StDBDeviceLocationsGetType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.devices.StDeviceLocation;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * device-locations
 */

public final class St1DataHDeviceLocationsGet
  extends St1BearerHandler
{
  private final StDatabaseType database;

  St1DataHDeviceLocationsGet(
    final RPServiceDirectoryType inServices)
  {
    super(inServices);

    this.database =
      inServices.requireService(StDatabaseType.class);
  }

  @Override
  protected void handleAuthenticated(
    final ServerRequest request,
    final ServerResponse response)
    throws DDatabaseException
  {
    this.read(request, St1DataDevicesGet.class);

    final Map<UUID, StDeviceLocation> locations;
    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDeviceLocationsGetType.class);
      locations = p.execute(DDatabaseUnit.UNIT);
    } catch (final DDatabaseException e) {
      response.status(Status.INTERNAL_SERVER_ERROR_500);
      throw e;
    }

    final var data = transformLocations(locations);
    response.status(200);
    this.send(response, new St1DataDeviceLocationsGetResponse(data));
  }

  private static Map<UUID, St1DataLocation> transformLocations(
    final Map<UUID, StDeviceLocation> locations)
  {
    return locations.entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), transformLocation(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static St1DataLocation transformLocation(
    final StDeviceLocation value)
  {
    return new St1DataLocation(
      value.time(),
      value.accuracy(),
      value.altitude(),
      value.bearing(),
      value.bearingAccuracy(),
      value.latitude(),
      value.longitude(),
      value.mslAltitude(),
      value.mslAltitudeAccuracy(),
      value.speed(),
      value.speedAccuracy()
    );
  }

  @Override
  protected String name()
  {
    return "DataDevices";
  }
}
