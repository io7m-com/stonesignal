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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.stonesignal.server.errors.StStandardErrorCodes;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.postgresql.util.PSQLException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.io7m.stonesignal.server.database.internal.Keys.DEVICE_KEY_UNIQUE;

final class StDatabaseExceptions
{
  private interface StExConstructorType
  {
    DDatabaseException execute(
      IntegrityConstraintViolationException e,
      Map<String, String> attributes
    );
  }

  private static final HashMap<String, StExConstructorType> INTEGRITY_CONSTRAINT_ERRORS;

  static {
    INTEGRITY_CONSTRAINT_ERRORS = new HashMap<>();
    INTEGRITY_CONSTRAINT_ERRORS.put(
      DEVICE_KEY_UNIQUE.getName(),
      (e, attributes) -> {
        return new DDatabaseException(
          "A device already exists with the given key.",
          e,
          StStandardErrorCodes.DEVICE_DUPLICATE.id(),
          attributes,
          Optional.empty()
        );
      }
    );
  }

  private StDatabaseExceptions()
  {

  }

  public static DDatabaseException handle(
    final Exception e,
    final Map<String, String> attributes)
  {
    return switch (e) {
      case final JsonProcessingException j -> {
        yield new DDatabaseException(
          e.getMessage(),
          e,
          StStandardErrorCodes.JSON_ERROR.id(),
          attributes,
          Optional.empty()
        );
      }
      case final DataAccessException x -> {
        yield handleDataAccessException(x, attributes);
      }
      default -> {
        yield new DDatabaseException(
          e.getMessage(),
          e,
          StStandardErrorCodes.SQL_ERROR.id(),
          attributes,
          Optional.empty()
        );
      }
    };
  }

  private static DDatabaseException handleDataAccessException(
    final DataAccessException e,
    final Map<String, String> attributes)
  {
    return switch (e) {
      case final IntegrityConstraintViolationException x -> {
        yield handleIntegrityConstraintViolation(x, attributes);
      }
      default -> {
        yield new DDatabaseException(
          e.getMessage(),
          e,
          StStandardErrorCodes.SQL_ERROR.id(),
          attributes,
          Optional.empty()
        );
      }
    };
  }

  private static DDatabaseException handleIntegrityConstraintViolation(
    final IntegrityConstraintViolationException e,
    final Map<String, String> attributes)
  {
    final var psql =
      (PSQLException) e.getCause();
    final var em =
      psql.getServerErrorMessage();

    final var constructor =
      INTEGRITY_CONSTRAINT_ERRORS.get(
        em.getConstraint().toUpperCase(Locale.ROOT)
      );

    return constructor.execute(
      e,
      attributes
    );
  }
}
