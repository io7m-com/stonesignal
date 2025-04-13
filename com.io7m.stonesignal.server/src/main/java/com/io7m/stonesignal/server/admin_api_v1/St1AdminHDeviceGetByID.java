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


package com.io7m.stonesignal.server.admin_api_v1;

import com.io7m.darco.api.DDatabaseException;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDevice;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetByID;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetResponse;
import com.io7m.stonesignal.server.database.StDBDeviceGetByIDType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.Optional;

/**
 * device-get-by-id
 */

public final class St1AdminHDeviceGetByID
  extends St1BearerHandler
{
  private final StDatabaseType database;

  St1AdminHDeviceGetByID(
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
    final var data =
      this.read(request, St1AdminDeviceGetByID.class);

    final Optional<St1AdminDevice> device;
    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBDeviceGetByIDType.class);
      device =
        p.execute(data.deviceId())
          .map(x -> {
            return new St1AdminDevice(
              x.id(),
              x.key().key(),
              x.name(),
              x.metadata()
            );
          });
    } catch (final DDatabaseException e) {
      response.status(Status.INTERNAL_SERVER_ERROR_500);
      throw e;
    }

    response.status(200);
    this.send(response, new St1AdminDeviceGetResponse(device));
  }

  @Override
  protected String name()
  {
    return "AdminDeviceGetByID";
  }
}
