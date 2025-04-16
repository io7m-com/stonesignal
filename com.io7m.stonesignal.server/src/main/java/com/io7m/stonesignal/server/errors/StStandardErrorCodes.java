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

package com.io7m.stonesignal.server.errors;

/**
 * Standard error codes.
 */

public final class StStandardErrorCodes
{
  /**
   * An attempt was made to create a duplicate device.
   */

  public static final StErrorCode DEVICE_DUPLICATE =
    new StErrorCode("error-device-duplicate");

  /**
   * An API is being called incorrectly.
   */

  public static final StErrorCode API_MISUSE_ERROR =
    new StErrorCode("error-api-misuse");

  /**
   * JSON was malformed.
   */

  public static final StErrorCode JSON_ERROR =
    new StErrorCode("error-json");

  /**
   * SQL issue.
   */

  public static final StErrorCode SQL_ERROR =
    new StErrorCode("error-sql");

  /**
   * IO issue.
   */

  public static final StErrorCode IO_ERROR =
    new StErrorCode("error-io");

  private StStandardErrorCodes()
  {

  }
}
