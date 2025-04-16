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


package com.io7m.stonesignal.documentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.io7m.stonesignal.server.StConfiguration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static com.github.victools.jsonschema.module.jackson.JacksonOption.INCLUDE_ONLY_JSONPROPERTY_ANNOTATED_METHODS;
import static com.github.victools.jsonschema.module.jackson.JacksonOption.RESPECT_JSONPROPERTY_REQUIRED;

public final class SchemaGeneration
{
  private SchemaGeneration()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var module =
      new JacksonModule(
        INCLUDE_ONLY_JSONPROPERTY_ANNOTATED_METHODS,
        RESPECT_JSONPROPERTY_REQUIRED
      );

    final var builder =
      new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12);

    builder.with(module);
    builder.with(Option.ADDITIONAL_FIXED_TYPES);
    builder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
    builder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES);
    builder.with(Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS);
    builder.with(Option.FLATTENED_ENUMS);
    builder.with(Option.FLATTENED_OPTIONALS);
    builder.with(Option.SCHEMA_VERSION_INDICATOR);
    builder.without(Option.NONPUBLIC_STATIC_FIELDS);
    builder.without(Option.NULLABLE_ALWAYS_AS_ANYOF);
    builder.without(Option.NULLABLE_FIELDS_BY_DEFAULT);

    builder.forFields()
      .withNullableCheck(x -> false);
    builder.forMethods()
      .withNullableCheck(x -> false);

    builder.forMethods()
      .withPropertyNameOverrideResolver(target -> {
        final var annotation = target.getAnnotation(JsonProperty.class);
        if (annotation != null) {
          return annotation.value();
        }
        return null;
      });

    final var config =
      builder.build();

    final var generator =
      new SchemaGenerator(config);

    final var mapper =
      JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build();

    final var classes = Set.of(
      StConfiguration.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminAuditGetResponse.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminAuditGet.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AuditEvent.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminDevice.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetByID.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetByKey.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminDeviceGetResponse.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminDevicePut.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminError.class,
      com.io7m.stonesignal.protocol.admin.v1.St1AdminOK.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDevice.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceGetByID.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceGetResponse.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationHistoryGet.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationHistoryGetResponse.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationsGet.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDeviceLocationsGetResponse.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDevicesGet.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataDevicesGetResponse.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataError.class,
      com.io7m.stonesignal.protocol.data.v1.St1DataLocation.class,
      com.io7m.stonesignal.protocol.device.v1.St1DeviceError.class,
      com.io7m.stonesignal.protocol.device.v1.St1DeviceLocationUpdate.class,
      com.io7m.stonesignal.protocol.device.v1.St1DeviceOK.class
    );

    for (final var clazz: classes) {
      final var file =
        Paths.get("schema-" + clazz.getSimpleName() + ".schema.json");
      try (final var out = Files.newOutputStream(file)) {
        mapper.writeValue(out, generator.generateSchema(clazz));
      }
    }
  }
}
