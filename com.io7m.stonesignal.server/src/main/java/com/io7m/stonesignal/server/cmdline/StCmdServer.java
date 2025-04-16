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


package com.io7m.stonesignal.server.cmdline;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.io7m.canonmill.core.CMKeyStoreProvider;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.stonesignal.server.StConfiguration;
import com.io7m.stonesignal.server.StServerFactoryType;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.nio.file.Path;
import java.security.Security;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;

/**
 * The "server" command.
 */

public final class StCmdServer implements QCommandType
{
  private static final QParameterNamed1<Path> CONFIGURATION_FILE =
    new QParameterNamed1<>(
      "--configuration",
      List.of(),
      new QConstant("The configuration file."),
      Optional.empty(),
      Path.class
    );

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public StCmdServer()
  {
    this.metadata = new QCommandMetadata(
      "server",
      new QConstant("Start the server."),
      Optional.empty()
    );
  }

  private static IllegalStateException noService()
  {
    return new IllegalStateException(
      "No services available of %s".formatted(StServerFactoryType.class)
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(CONFIGURATION_FILE),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    Security.addProvider(new CMKeyStoreProvider());

    QLogback.configure(context);

    final var configurationFile =
      context.parameterValue(CONFIGURATION_FILE);

    final var objectMapper =
      JsonMapper.builder()
        .addModule(new Jdk8Module())
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .build();

    final var configuration =
      objectMapper.readValue(configurationFile.toFile(), StConfiguration.class);

    final var servers =
      ServiceLoader.load(StServerFactoryType.class)
        .findFirst()
        .orElseThrow(StCmdServer::noService);

    try (final var server =
           servers.createServer(Clock.systemUTC(), configuration)) {
      server.start();

      while (true) {
        try {
          Thread.sleep(1_000L);
        } catch (final InterruptedException e) {
          break;
        }
      }
    }

    return SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
