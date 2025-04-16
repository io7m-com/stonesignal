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

package com.io7m.stonesignal.server.database;

import com.io7m.stonesignal.server.audit.StAuditEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.io7m.stonesignal.server.database.internal.Tables.AUDIT;

/**
 * The audit-search query.
 */

public final class StDBAuditSearch
  extends StDatabaseQueryAbstract<
  StDBAuditSearchParameters,
  List<StAuditEvent>>
  implements StDBAuditSearchType
{
  StDBAuditSearch(final StDatabaseTransactionType t)
  {
    super(t);
  }

  /**
   * @return The query provider
   */

  public static StDatabaseQueryProviderType<
    StDBAuditSearchParameters,
    List<StAuditEvent>,
    StDBAuditSearchType>
  provider()
  {
    return StDatabaseQueryProvider.provide(
      StDBAuditSearchType.class,
      StDBAuditSearch::new
    );
  }

  @Override
  protected List<StAuditEvent> onExecuteWithContext(
    final StDatabaseTransactionType transaction,
    final DSLContext context,
    final StDBAuditSearchParameters search)
    throws Exception
  {
    final var matchType =
      search.type()
        .map(AUDIT.AU_TYPE::eq)
        .orElse(DSL.trueCondition());
    final var matchTime =
      AUDIT.AU_TIME.ge(search.timeStart());
    final var conditions =
      DSL.and(matchType, matchTime);

    final var r =
      context.select(
          AUDIT.AU_ID,
          AUDIT.AU_TIME,
          AUDIT.AU_TYPE,
          AUDIT.AU_DATA
        )
        .from(AUDIT)
        .where(conditions)
        .limit(search.count())
        .fetch();

    final var results =
      new ArrayList<StAuditEvent>(r.size());

    final var mapper =
      this.objectMapper();

    for (final var rec : r) {
      results.add(
        new StAuditEvent(
          rec.get(AUDIT.AU_ID),
          rec.get(AUDIT.AU_TYPE),
          rec.get(AUDIT.AU_TIME),
          mapper.readValue(
            rec.get(AUDIT.AU_DATA).data(),
            Map.class
          )
        )
      );
    }

    return List.copyOf(results);
  }

  @Override
  protected String name()
  {
    return "AuditSearch";
  }
}
