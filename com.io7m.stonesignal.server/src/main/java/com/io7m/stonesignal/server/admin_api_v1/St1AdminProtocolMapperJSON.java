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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.io7m.dixmont.core.DmJsonRestrictedDeserializers;
import com.io7m.stonesignal.protocol.admin.v1.St1AdminProtocol;

/**
 * The v1 JSON mapper.
 */

public final class St1AdminProtocolMapperJSON
{
  private static final JsonMapper MAPPER =
    createJsonMapper();

  private St1AdminProtocolMapperJSON()
  {

  }

  /**
   * @return The mapper
   */

  public static JsonMapper get()
  {
    return MAPPER;
  }

  private static JsonMapper createJsonMapper()
  {
    final var builder =
      DmJsonRestrictedDeserializers.builder();

    builder.allowClassNames(St1AdminProtocol.classNames());

    final var serializers =
      builder.build();

    final var simpleModule = new SimpleModule();
    simpleModule.setDeserializers(serializers);

    return JsonMapper.builder()
      .addModule(simpleModule)
      .addModule(new Jdk8Module())
      .addModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
      .build();
  }
}
