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


package com.io7m.stonesignal.server.http;

import io.helidon.http.HttpException;
import io.helidon.http.HttpMediaType;
import io.helidon.http.ServerRequestHeaders;
import io.helidon.http.Status;

import java.util.Objects;

/**
 * The stonesignal API formats.
 */

public enum StFormat
{
  /**
   * JSON format.
   */

  JSON(HttpMediaType.create("application/stonesignal+json")),

  /**
   * CBOR format.
   */

  CBOR(HttpMediaType.create("application/stonesignal+cbor"));

  private final HttpMediaType mediaType;

  /**
   * @return The HTTP media type
   */

  public HttpMediaType mediaType()
  {
    return this.mediaType;
  }

  StFormat(
    final HttpMediaType httpMediaType)
  {
    this.mediaType =
      Objects.requireNonNull(httpMediaType, "httpMediaType");
  }

  /**
   * Find a format in the given headers.
   *
   * @param headers The headers
   *
   * @return The requested format
   */

  public static StFormat findFormat(
    final ServerRequestHeaders headers)
  {
    final var contentType =
      headers.contentType().orElse(StFormat.JSON.mediaType());

    if (Objects.equals(contentType, StFormat.JSON.mediaType())) {
      return StFormat.JSON;
    } else if (Objects.equals(contentType, StFormat.CBOR.mediaType())) {
      return StFormat.CBOR;
    } else {
      throw new HttpException(
        "Unsupported content type: %s".formatted(contentType),
        Status.BAD_REQUEST_400
      );
    }
  }
}
