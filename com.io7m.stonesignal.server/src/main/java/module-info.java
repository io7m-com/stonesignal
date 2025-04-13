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

import com.io7m.stonesignal.server.StServerFactoryType;
import com.io7m.stonesignal.server.StServers;
import com.io7m.stonesignal.server.database.StDBAuditSearch;
import com.io7m.stonesignal.server.database.StDBDeviceGetByID;
import com.io7m.stonesignal.server.database.StDBDeviceGetByKey;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdatePut;
import com.io7m.stonesignal.server.database.StDBDeviceLocationUpdateSearch;
import com.io7m.stonesignal.server.database.StDBDeviceLocationsGet;
import com.io7m.stonesignal.server.database.StDBDevicePut;
import com.io7m.stonesignal.server.database.StDBDevicesGet;
import com.io7m.stonesignal.server.database.StDatabaseQueryProviderType;
import com.io7m.stonesignal.server.telemetry.StTelemetryServiceFactoryType;

/**
 * Position recorder (Server)
 */

module com.io7m.stonesignal.server
{
  requires static org.osgi.annotation.versioning;
  requires static org.osgi.annotation.bundle;

  requires com.io7m.stonesignal.protocol.admin;
  requires com.io7m.stonesignal.protocol.device;
  requires com.io7m.stonesignal.protocol.data;

  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.cbor;
  requires com.fasterxml.jackson.datatype.jdk8;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires com.io7m.canonmill.core;
  requires com.io7m.darco.api;
  requires com.io7m.darco.postgres;
  requires com.io7m.dixmont.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires com.io7m.quarrel.core;
  requires com.io7m.quarrel.ext.logback;
  requires com.io7m.repetoir.core;
  requires com.io7m.seltzer.api;
  requires com.io7m.ventrad.core;
  requires io.helidon.common.tls;
  requires io.helidon.webserver;
  requires io.opentelemetry.api;
  requires io.opentelemetry.context;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.sdk.logs;
  requires io.opentelemetry.sdk.metrics;
  requires io.opentelemetry.sdk.trace;
  requires io.opentelemetry.sdk;
  requires java.sql;
  requires jul.to.slf4j;
  requires org.jooq;
  requires org.postgresql.jdbc;
  requires org.slf4j;

  exports com.io7m.stonesignal.server.audit;
  exports com.io7m.stonesignal.server.devices;
  exports com.io7m.stonesignal.server.errors;
  exports com.io7m.stonesignal.server;

  exports com.io7m.stonesignal.server.database
    to com.io7m.stonesignal.tests;
  exports com.io7m.stonesignal.server.admin_api_v1
    to com.io7m.stonesignal.tests;
  exports com.io7m.stonesignal.server.device_api_v1
    to com.io7m.stonesignal.tests;
  exports com.io7m.stonesignal.server.data_api_v1
    to com.io7m.stonesignal.tests;
  exports com.io7m.stonesignal.server.internal;

  provides StDatabaseQueryProviderType with
    StDBAuditSearch,
    StDBDeviceGetByID,
    StDBDeviceGetByKey,
    StDBDeviceLocationUpdatePut,
    StDBDeviceLocationUpdateSearch,
    StDBDeviceLocationsGet,
    StDBDevicePut,
    StDBDevicesGet;

  provides StServerFactoryType with
    StServers;

  uses StDatabaseQueryProviderType;
  uses StTelemetryServiceFactoryType;
  uses StServerFactoryType;
}