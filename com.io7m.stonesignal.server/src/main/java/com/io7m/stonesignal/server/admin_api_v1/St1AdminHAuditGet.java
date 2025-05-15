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
import com.io7m.stonesignal.protocol.admin.v1.St1AdminAuditGet;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminAuditGetResponse;
import com.io7m.stonesignal.protocol.admin.v1.St1AuditEvent;
import com.io7m.stonesignal.server.audit.StAuditEvent;
import com.io7m.stonesignal.server.database.StDBAuditSearchParameters;
import com.io7m.stonesignal.server.database.StDBAuditSearchType;
import com.io7m.stonesignal.server.database.StDatabaseType;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;

/**
 * audit-get
 */

public final class St1AdminHAuditGet
  extends St1BearerHandler
{
  private final StDatabaseType database;

  St1AdminHAuditGet(
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
    final var state =
      request.context()
        .get(St1AdminState.class)
        .orElseThrow();

    final var data =
      this.read(state, request, St1AdminAuditGet.class);

    final List<StAuditEvent> events;
    try (final var t = this.database.openTransaction()) {
      final var p = t.query(StDBAuditSearchType.class);
      events = p.execute(
        new StDBAuditSearchParameters(
          data.type(),
          data.timeStart(),
          data.count()
        )
      );
      t.commit();
    } catch (final DDatabaseException e) {
      response.status(Status.INTERNAL_SERVER_ERROR_500);
      throw e;
    }

    response.status(200);
    this.send(
      state,
      response,
      new St1AdminAuditGetResponse(
        events.stream()
          .map(e -> new St1AuditEvent(e.id(), e.type(), e.time(), e.data()))
          .toList()
      )
    );
  }

  @Override
  protected String name()
  {
    return "AdminAuditGet";
  }
}
