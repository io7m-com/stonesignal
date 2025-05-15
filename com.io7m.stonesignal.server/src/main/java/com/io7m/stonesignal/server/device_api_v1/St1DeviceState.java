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


package com.io7m.stonesignal.server.device_api_v1;

import com.io7m.stonesignal.server.devices.StDevice;
import com.io7m.stonesignal.server.devices.StDeviceKey;
import com.io7m.stonesignal.server.http.StFormat;

import java.util.Objects;
import java.util.UUID;

/**
 * The state carried through a device API request.
 */

final class St1DeviceState
{
  private StFormat format;
  private StDeviceKey token;
  private UUID requestId;
  private StDevice device;

  St1DeviceState()
  {
    this.requestId = UUID.randomUUID();
    this.format = StFormat.JSON;
  }

  /**
   * @return The device
   */

  public StDevice device()
  {
    return Objects.requireNonNull(this.device, "this.device");
  }

  /**
   * @return The chosen data format
   */

  public StFormat format()
  {
    return this.format;
  }

  /**
   * Set the data format.
   *
   * @param newFormat The format
   */

  public void setFormat(
    final StFormat newFormat)
  {
    this.format = Objects.requireNonNull(newFormat, "newFormat");
  }

  /**
   * @return The authentication token
   */

  public StDeviceKey token()
  {
    return Objects.requireNonNull(this.token, "this.token");
  }

  /**
   * @return The request ID
   */

  public UUID requestId()
  {
    return this.requestId;
  }

  /**
   * Set the authentication token.
   *
   * @param newToken The token
   */

  public void setToken(
    final StDeviceKey newToken)
  {
    this.token = Objects.requireNonNull(newToken, "token");
  }

  /**
   * Set the device.
   *
   * @param newDevice The device
   */

  public void setDevice(
    final StDevice newDevice)
  {
    this.device = Objects.requireNonNull(newDevice, "stDevice");
  }
}
