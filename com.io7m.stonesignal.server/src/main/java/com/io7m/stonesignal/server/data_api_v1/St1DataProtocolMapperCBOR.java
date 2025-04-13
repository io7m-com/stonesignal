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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.io7m.dixmont.core.DmJsonRestrictedDeserializers;
import com.io7m.stonesignal.protocol.data.v1.St1DataProtocol;

/**
 * The v1 CBOR mapper.
 */

public final class St1DataProtocolMapperCBOR
{
  private static final CBORMapper MAPPER =
    createCborMapper();

  private St1DataProtocolMapperCBOR()
  {

  }

  /**
   * @return The mapper
   */

  public static CBORMapper get()
  {
    return MAPPER;
  }

  private static CBORMapper createCborMapper()
  {
    final var builder =
      DmJsonRestrictedDeserializers.builder();

    builder.allowClassNames(St1DataProtocol.classNames());

    final var serializers =
      builder.build();

    final var simpleModule = new SimpleModule();
    simpleModule.setDeserializers(serializers);

    return CBORMapper.builder()
      .addModule(simpleModule)
      .addModule(new Jdk8Module())
      .enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
      .build();
  }
}
