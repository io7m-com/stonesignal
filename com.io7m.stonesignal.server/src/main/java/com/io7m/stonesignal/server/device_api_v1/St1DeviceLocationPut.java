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


package com.io7m.stonesignal.server.device_api_v1;

import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceLocationUpdate;
import com.io7m.stonesignal.protocol.device.v1.St1DeviceOK;
import com.io7m.stonesignal.server.clock.StServerClock;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdatePutType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import com.io7m.stonesignal.server.devices.StDeviceLocationUpdate;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.IOException;

/**
 * device-location-put
 */

public final class St1DeviceLocationPut
  extends St1BearerHandler
{
  private final StDatabaseType database;
  private final StServerClock clock;

  /**
   * device-location-put
   *
   * @param inServices The services
   */

  public St1DeviceLocationPut(
    final RPServiceDirectoryType inServices)
  {
    super(inServices);

    this.database =
      inServices.requireService(StDatabaseType.class);
    this.clock =
      inServices.requireService(StServerClock.class);
  }

  @Override
  protected void handleAuthenticated(
    final ServerRequest request,
    final ServerResponse response)
    throws IOException, DDatabaseException
  {
    final St1DeviceLocationUpdate data;
    try {
      data = this.read(request, St1DeviceLocationUpdate.class);
    } catch (final IOException e) {
      response.status(Status.BAD_REQUEST_400);
      throw e;
    }

    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDeviceLocationUpdatePutType.class);
      p.execute(new StDeviceLocationUpdate(
        0L,
        this.device().id(),
        this.clock.nowPrecise(),
        data.toMap()
      ));
      t.commit();
    } catch (final DDatabaseException e) {
      response.status(Status.INTERNAL_SERVER_ERROR_500);
      throw e;
    }

    response.status(200);
    this.send(response, new St1DeviceOK());
  }

  @Override
  protected String name()
  {
    return "DeviceLocationPut";
  }
}
