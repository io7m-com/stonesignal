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

package com.io7m.stonesignal.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The server configuration.
 */

// CHECKSTYLE:OFF

@JsonDeserialize
@JsonSerialize
public record StConfiguration(
  @JsonProperty(value = "Database", required = true)
  @JsonPropertyDescription("The database configuration.")
  Database database,

  @JsonProperty(value = "OpenTelemetry")
  @JsonPropertyDescription("The OpenTelemetry configuration.")
  Optional<OpenTelemetry> openTelemetry,

  @JsonProperty(value = "DeviceAPI", required = true)
  @JsonPropertyDescription("The device API configuration.")
  DeviceAPI deviceAPI,

  @JsonProperty(value = "AdminAPI", required = true)
  @JsonPropertyDescription("The admin API configuration.")
  AdminAPI adminAPI,

  @JsonProperty(value = "DataAPI", required = true)
  @JsonPropertyDescription("The data API configuration.")
  DataAPI dataAPI)
{
  public StConfiguration
  {
    Objects.requireNonNull(database, "database");
    Objects.requireNonNull(openTelemetry, "openTelemetry");
    Objects.requireNonNull(deviceAPI, "deviceAPI");
    Objects.requireNonNull(adminAPI, "adminAPI");
    Objects.requireNonNull(dataAPI, "dataAPI");
  }

  @JsonDeserialize
  @JsonSerialize
  public enum DatabaseKind
  {
    POSTGRESQL
  }

  @JsonDeserialize
  @JsonSerialize
  public record Database(
    @JsonProperty(value = "Kind", required = true)
    @JsonPropertyDescription("The database kind.")
    DatabaseKind kind,

    @JsonProperty(value = "OwnerRole", required = true)
    @JsonPropertyDescription("The database owner role.")
    String ownerRoleName,

    @JsonProperty(value = "OwnerPassword", required = true)
    @JsonPropertyDescription("The database owner password.")
    String ownerRolePassword,

    @JsonProperty(value = "WorkerPassword", required = true)
    @JsonPropertyDescription("The database worker password.")
    String workerRolePassword,

    @JsonProperty(value = "ReaderPassword")
    @JsonPropertyDescription("The database reader password.")
    Optional<String> readerRolePassword,

    @JsonProperty(value = "Address", required = true)
    @JsonPropertyDescription("The database address.")
    String address,

    @JsonProperty(value = "Port", required = true)
    @JsonPropertyDescription("The database port.")
    int port,

    @JsonProperty(value = "Name", required = true)
    @JsonPropertyDescription("The database name.")
    String databaseName,

    @JsonProperty(value = "Upgrade", required = true)
    @JsonPropertyDescription("Upgrade the database?")
    boolean upgrade,

    @JsonProperty(value = "MinimumConnections", required = true)
    @JsonPropertyDescription("The minimum connections for pooling.")
    int minimumConnections,

    @JsonProperty(value = "MaximumConnections", required = true)
    @JsonPropertyDescription("The maximum connections for pooling.")
    int maximumConnections)
  {
    public Database
    {
      Objects.requireNonNull(kind, "kind");
      Objects.requireNonNull(ownerRoleName, "ownerRoleName");
      Objects.requireNonNull(ownerRolePassword, "ownerRolePassword");
      Objects.requireNonNull(workerRolePassword, "workerRolePassword");
      Objects.requireNonNull(readerRolePassword, "readerRolePassword");
      Objects.requireNonNull(address, "address");
      Objects.requireNonNull(databaseName, "databaseName");
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public record TLSStore(
    @JsonProperty(value = "Type", required = true)
    @JsonPropertyDescription("The store type.")
    String storeType,

    @JsonProperty(value = "Provider", required = true)
    @JsonPropertyDescription("The store provider.")
    String storeProvider,

    @JsonProperty(value = "Password", required = true)
    @JsonPropertyDescription("The store password.")
    String storePassword,

    @JsonProperty(value = "Path", required = true)
    @JsonPropertyDescription("The store path.")
    Path storePath)
  {
    public TLSStore
    {
      Objects.requireNonNull(storeType, "storeType");
      Objects.requireNonNull(storeProvider, "storeProvider");
      Objects.requireNonNull(storePassword, "storePassword");
      Objects.requireNonNull(storePath, "storePath");
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public record TLS(
    @JsonProperty(value = "KeyStore", required = true)
    @JsonPropertyDescription("The key store.")
    TLSStore keyStore,

    @JsonProperty(value = "TrustStore", required = true)
    @JsonPropertyDescription("The trust store.")
    TLSStore trustStore)
  {
    public TLS
    {
      Objects.requireNonNull(keyStore, "keyStore");
      Objects.requireNonNull(trustStore, "trustStore");
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public record DeviceAPI(
    @JsonProperty(value = "Host", required = true)
    @JsonPropertyDescription("The host address to which to bind.")
    String host,

    @JsonProperty(value = "Port", required = true)
    @JsonPropertyDescription("The host port to which to bind.")
    int port,

    @JsonProperty(value = "TLS")
    @JsonPropertyDescription("The TLS configuration.")
    Optional<TLS> tls)
  {
    public DeviceAPI
    {
      Objects.requireNonNull(host, "host");
      Objects.requireNonNull(tls, "tls");
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public record AdminAPI(
    @JsonProperty(value = "Host", required = true)
    @JsonPropertyDescription("The host address to which to bind.")
    String host,

    @JsonProperty(value = "Port", required = true)
    @JsonPropertyDescription("The host port to which to bind.")
    int port,

    @JsonProperty(value = "APIKey", required = true)
    @JsonPropertyDescription("The API key.")
    String apiKey,

    @JsonProperty(value = "TLS")
    @JsonPropertyDescription("The TLS configuration.")
    Optional<TLS> tls)
  {
    public AdminAPI
    {
      Objects.requireNonNull(host, "host");
      Objects.requireNonNull(tls, "tls");
      Objects.requireNonNull(apiKey, "apiKey");
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public record DataAPI(
    @JsonProperty(value = "Host", required = true)
    @JsonPropertyDescription("The host address to which to bind.")
    String host,

    @JsonProperty(value = "Port", required = true)
    @JsonPropertyDescription("The host port to which to bind.")
    int port,

    @JsonProperty(value = "APIKey", required = true)
    @JsonPropertyDescription("The API key.")
    String apiKey,

    @JsonProperty(value = "TLS")
    @JsonPropertyDescription("The TLS configuration.")
    Optional<TLS> tls)
  {
    public DataAPI
    {
      Objects.requireNonNull(host, "host");
      Objects.requireNonNull(tls, "tls");
      Objects.requireNonNull(apiKey, "apiKey");
    }
  }

  @JsonSerialize
  @JsonDeserialize
  public record OpenTelemetry(
    @JsonProperty(value = "ServiceName", required = true)
    String logicalServiceName,
    @JsonProperty(value = "Logs")
    Optional<Logs> logs,
    @JsonProperty(value = "Metrics")
    Optional<Metrics> metrics,
    @JsonProperty(value = "Traces")
    Optional<Traces> traces)
  {
    /**
     * Configuration information for OpenTelemetry.
     *
     * @param logicalServiceName The logical service name
     * @param logs               The configuration for OTLP logs
     * @param metrics            The configuration for OTLP metrics
     * @param traces             The configuration for OTLP traces
     */

    public OpenTelemetry
    {
      Objects.requireNonNull(logicalServiceName, "logicalServiceName");
      Objects.requireNonNull(logs, "logs");
      Objects.requireNonNull(metrics, "metrics");
      Objects.requireNonNull(traces, "traces");
    }

    /**
     * The protocol used to deliver OpenTelemetry data.
     */

    @JsonSerialize
    @JsonDeserialize
    public enum OTLPProtocol
    {
      /**
       * gRPC
       */

      GRPC,

      /**
       * HTTP(s)
       */

      HTTP
    }

    /**
     * Metrics configuration.
     *
     * @param endpoint The endpoint to which OTLP metrics data will be sent.
     * @param protocol The protocol used to deliver OpenTelemetry data.
     */

    @JsonSerialize
    @JsonDeserialize
    public record Metrics(
      @JsonProperty(value = "Endpoint", required = true)
      URI endpoint,
      @JsonProperty(value = "Protocol", required = true)
      OTLPProtocol protocol)
    {
      /**
       * Metrics configuration.
       */

      public Metrics
      {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(protocol, "protocol");
      }
    }

    /**
     * Trace configuration.
     *
     * @param endpoint The endpoint to which OTLP trace data will be sent.
     * @param protocol The protocol used to deliver OpenTelemetry data.
     */

    @JsonSerialize
    @JsonDeserialize
    public record Traces(
      @JsonProperty(value = "Endpoint", required = true)
      URI endpoint,
      @JsonProperty(value = "Protocol", required = true)
      OTLPProtocol protocol)
    {
      /**
       * Trace configuration.
       */

      public Traces
      {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(protocol, "protocol");
      }
    }

    /**
     * Logs configuration.
     *
     * @param endpoint The endpoint to which OTLP log data will be sent.
     * @param protocol The protocol used to deliver OpenTelemetry data.
     */

    @JsonSerialize
    @JsonDeserialize
    public record Logs(
      @JsonProperty(value = "Endpoint", required = true)
      URI endpoint,
      @JsonProperty(value = "Protocol", required = true)
      OTLPProtocol protocol)
    {
      /**
       * Logs configuration.
       */

      public Logs
      {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(protocol, "protocol");
      }
    }
  }
}
