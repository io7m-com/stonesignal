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

import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryType;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DRoles;
import com.io7m.darco.api.DUsernamePassword;
import com.io7m.darco.postgres.DPQDatabaseConfigurationType;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.stonesignal.server.StConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The database configuration.
 *
 * @param configuration The configuration
 * @param telemetry     The telemetry
 */

public record StDatabaseConfiguration(
  StConfiguration configuration,
  DDatabaseTelemetryType telemetry)
  implements DPQDatabaseConfigurationType
{
  /**
   * The database configuration.
   *
   * @param configuration The configuration
   * @param telemetry     The telemetry
   */

  public StDatabaseConfiguration
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(telemetry, "telemetry");
  }

  @Override
  public Optional<JXEHardenedSAXParsers> saxParsers()
  {
    return Optional.empty();
  }

  @Override
  public DDatabaseCreate create()
  {
    return DDatabaseCreate.DO_NOT_CREATE_DATABASE;
  }

  @Override
  public DDatabaseUpgrade upgrade()
  {
    if (this.configuration.database().upgrade()) {
      return DDatabaseUpgrade.UPGRADE_DATABASE;
    } else {
      return DDatabaseUpgrade.DO_NOT_UPGRADE_DATABASE;
    }
  }

  @Override
  public DRoles roles()
  {
    return DRoles.of(
      List.of(
        this.workerRole(),
        this.ownerRole(),
        this.readerRole(),
        this.deviceRole()
      )
    );
  }

  @Override
  public String databaseAddress()
  {
    return this.configuration.database().address();
  }

  @Override
  public int databasePort()
  {
    return this.configuration.database().port();
  }

  @Override
  public String databaseName()
  {
    return this.configuration.database().databaseName();
  }

  @Override
  public boolean databaseUseTLS()
  {
    return false;
  }

  @Override
  public DUsernamePassword ownerRole()
  {
    return new DUsernamePassword(
      this.configuration.database().ownerRoleName(),
      this.configuration.database().ownerRolePassword()
    );
  }

  @Override
  public DUsernamePassword defaultRole()
  {
    return this.workerRole();
  }

  @Override
  public DUsernamePassword workerRole()
  {
    return new DUsernamePassword(
      "stonesignal",
      this.configuration.database().workerRolePassword()
    );
  }

  /**
   * @return The reader role
   */

  public DUsernamePassword readerRole()
  {
    return new DUsernamePassword(
      "stonesignal_reader",
      this.configuration.database().readerRolePassword()
    );
  }

  /**
   * @return The device role
   */

  public DUsernamePassword deviceRole()
  {
    return new DUsernamePassword(
      "stonesignal_device",
      this.configuration.database().deviceRolePassword()
    );
  }
}
