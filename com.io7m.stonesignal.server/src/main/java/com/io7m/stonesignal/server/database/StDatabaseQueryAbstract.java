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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseQueryAbstract;
import org.jooq.DSLContext;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.jooq.SQLDialect.POSTGRES;

/**
 * An abstract query for the database.
 *
 * @param <P> The query parameters
 * @param <R> The query results
 */

public abstract class StDatabaseQueryAbstract<P, R>
  extends DDatabaseQueryAbstract<StDatabaseTransactionType, P, R>
{
  private static final Settings SETTINGS =
    new Settings().withRenderNameCase(RenderNameCase.LOWER);

  private final HashMap<String, String> attributes;

  protected StDatabaseQueryAbstract(
    final StDatabaseTransactionType t)
  {
    super(t);
    this.attributes = new HashMap<String, String>();
  }

  protected final DSLContext context(
    final Connection connection)
  {
    return DSL.using(connection, POSTGRES, SETTINGS);
  }

  protected final DSLContext context(
    final StDatabaseTransactionType transaction)
  {
    return this.context(transaction.connection());
  }

  protected final ObjectMapper objectMapper()
  {
    return new ObjectMapper();
  }

  protected final void record(
    final String key,
    final Object value)
  {
    this.attributes.put(
      Objects.requireNonNull(key, "key"),
      Objects.requireNonNull(value.toString(), "value")
    );
  }

  protected final Map<String, String> attributes()
  {
    return this.attributes;
  }

  @Override
  protected final R onExecute(
    final StDatabaseTransactionType transaction,
    final P parameters)
    throws DDatabaseException
  {
    final var querySpan =
      transaction.createSubSpan(this.name());

    try {
      return this.onExecuteWithContext(
        transaction,
        this.context(transaction),
        parameters
      );
    } catch (final Exception e) {
      querySpan.recordException(e);
      throw StDatabaseExceptions.handle(e, this.attributes());
    } finally {
      querySpan.end();
    }
  }

  protected abstract R onExecuteWithContext(
    StDatabaseTransactionType transaction,
    DSLContext context,
    P parameters)
    throws Exception;

  protected abstract String name();
}
