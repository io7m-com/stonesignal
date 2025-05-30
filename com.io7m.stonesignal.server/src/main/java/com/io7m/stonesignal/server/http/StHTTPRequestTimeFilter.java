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

import com.io7m.stonesignal.server.clock.StServerClock;
import com.io7m.stonesignal.server.telemetry.StMetricsServiceType;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

import java.time.Duration;
import java.util.Objects;

/**
 * A filter that tracks request times.
 */

public final class StHTTPRequestTimeFilter
  implements io.helidon.webserver.http.Filter
{
  private final StServerClock clock;
  private final StMetricsServiceType metrics;

  /**
   * A filter that tracks request times.
   *
   * @param inMetrics The metrics
   * @param inClock   The clock
   */

  public StHTTPRequestTimeFilter(
    final StMetricsServiceType inMetrics,
    final StServerClock inClock)
  {
    this.metrics =
      Objects.requireNonNull(inMetrics, "inMetrics");
    this.clock =
      Objects.requireNonNull(inClock, "clock");
  }

  @Override
  public String toString()
  {
    return "[StHTTPRequestTimeFilter 0x%s]"
      .formatted(Long.toUnsignedString(this.hashCode(), 16));
  }

  @Override
  public void filter(
    final io.helidon.webserver.http.FilterChain chain,
    final RoutingRequest req,
    final RoutingResponse res)
  {
    final var timeThen = this.clock.nowPrecise();
    try {
      chain.proceed();
    } finally {
      final var timeNow = this.clock.nowPrecise();
      this.metrics.onHttpResponseTime(
        Duration.between(timeThen, timeNow)
      );
    }
  }
}
