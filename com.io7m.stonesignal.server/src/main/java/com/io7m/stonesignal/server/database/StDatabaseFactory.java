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

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.postgres.DPQDatabaseFactory;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.jooq.SQLDialect.POSTGRES;

/**
 * The main database factory.
 */

public final class StDatabaseFactory
  extends DPQDatabaseFactory<
  StDatabaseConfiguration,
  StDatabaseConnectionType,
  StDatabaseTransactionType,
  StDatabaseQueryProviderType<?, ?, ?>,
  StDatabaseType>
  implements StDatabaseFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StDatabaseFactory.class);

  /**
   * The main database factory.
   */

  public StDatabaseFactory()
  {

  }

  @Override
  protected RDottedName applicationId()
  {
    return new RDottedName("com.io7m.stonesignal");
  }

  @Override
  protected Logger logger()
  {
    return LOG;
  }

  @Override
  protected StDatabaseType onCreateDatabase(
    final StDatabaseConfiguration configuration,
    final DataSource source,
    final List<StDatabaseQueryProviderType<?, ?, ?>> queryProviders,
    final CloseableCollectionType<DDatabaseException> resources)
  {
    return new StDatabase(
      configuration,
      source,
      queryProviders,
      resources
    );
  }

  @Override
  protected InputStream onRequireDatabaseSchemaXML()
  {
    return StDatabaseFactory.class.getResourceAsStream(
      "/com/io7m/stonesignal/server/database/database.xml"
    );
  }

  @Override
  protected void onEvent(
    final String message)
  {

  }

  @Override
  protected DataSource onTransformDataSourceForSetup(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected DataSource onTransformDataSourceForUse(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected void onPostUpgrade(
    final StDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    updateWorkerRolePassword(configuration, connection);
    updateReaderRolePassword(configuration, connection);
    updateDeviceRolePassword(configuration, connection);
  }

  @Override
  protected List<StDatabaseQueryProviderType<?, ?, ?>> onRequireDatabaseQueryProviders()
  {
    return ServiceLoader.load(StDatabaseQueryProviderType.class)
      .stream()
      .map(ServiceLoader.Provider::get)
      .map(x -> (StDatabaseQueryProviderType<?, ?, ?>) x)
      .collect(Collectors.toList());
  }

  private static void updateRole(
    final Connection connection,
    final String passwordText,
    final String role)
    throws SQLException
  {
    final var settings =
      new Settings().withRenderNameCase(RenderNameCase.LOWER);
    final var dslContext =
      DSL.using(connection, POSTGRES, settings);

    final var safePass =
      dslContext.render(DSL.inline(passwordText));
    final var sql =
      "ALTER ROLE %s WITH PASSWORD %s"
        .formatted(
          role,
          safePass
        );

    dslContext.execute(sql);

    try (final var st = connection.createStatement()) {
      st.execute("ALTER ROLE %s LOGIN".formatted(role));
    }
  }

  /**
   * Update the worker role password.
   */

  private static void updateWorkerRolePassword(
    final StDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    LOG.debug("Updating worker role");

    updateRole(
      connection,
      configuration.configuration().database().workerRolePassword(),
      StDatabaseRoles.worker()
    );
  }

  /**
   * Update the reader role password.
   */

  private static void updateReaderRolePassword(
    final StDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    LOG.debug("Updating reader role");

    updateRole(
      connection,
      configuration.configuration().database().readerRolePassword(),
      StDatabaseRoles.reader()
    );
  }

  /**
   * Update the device role password.
   */

  private static void updateDeviceRolePassword(
    final StDatabaseConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    LOG.debug("Updating device role");

    updateRole(
      connection,
      configuration.configuration().database().deviceRolePassword(),
      StDatabaseRoles.device()
    );
  }
}
