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
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationHistoryGet;
import com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationHistoryGetResponse;
import com.io7m.stonesignal.protocol.data.v1.St1DataLocation;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdateSearchParameters;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdateSearchType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.devices.StDeviceLocation;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;

import static com.io7m.stonesignal.server.database.StDatabaseRoles.reader;

/**
 * device-location-history
 */

public final class St1DataHDeviceLocationHistory
  extends St1BearerHandler
{
  private final StDatabaseType database;

  St1DataHDeviceLocationHistory(
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
    final var command =
      this.read(request, St1DataDeviceLocationHistoryGet.class);

    final List<StDeviceLocation> locations;
    try (final var t = this.database.openTransactionWithRole(reader())) {
      final var p = t.query(StDBDeviceLocationUpdateSearchType.class);
      locations = p.execute(
        new StDBDeviceLocationUpdateSearchParameters(
          command.deviceId(),
          command.timeStart(),
          command.count()
        )
      );
    } catch (final DDatabaseException e) {
      response.status(Status.INTERNAL_SERVER_ERROR_500);
      throw e;
    }

    final var data = transformLocations(locations);
    response.status(200);
    this.send(response, new St1DataDeviceLocationHistoryGetResponse(data));
  }

  private static List<St1DataLocation> transformLocations(
    final List<StDeviceLocation> locations)
  {
    return locations.stream()
      .map(St1DataHDeviceLocationHistory::transformLocation)
      .toList();
  }

  private static St1DataLocation transformLocation(
    final StDeviceLocation value)
  {
    return new St1DataLocation(
      value.id(),
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
    return "DeviceLocationHistory";
  }
}
