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


package com.io7m.stonesignal.server.tls;

import com.io7m.repetoir.core.RPServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service that periodically reloads TLS contexts.
 */

public final class StTLSReloader
  implements AutoCloseable, RPServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(StTLSReloader.class);

  private final StTLSContextServiceType tls;
  private final ScheduledExecutorService executor;

  private StTLSReloader(
    final StTLSContextServiceType inTls)
  {
    this.tls =
      Objects.requireNonNull(inTls, "tls");

    this.executor =
      Executors.newSingleThreadScheduledExecutor(r -> {
        final var thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(
          "com.io7m.stonesignal.server.tls.StTLSReloader[%s]"
            .formatted(thread.threadId()));
        return thread;
      });

    this.executor.scheduleAtFixedRate(
      this::run,
      0L,
      30L,
      TimeUnit.MINUTES
    );
  }

  private void run()
  {
    LOG.info("Reloading TLS certificates.");
    this.tls.reload();
  }

  /**
   * Create a new TLS reloader.
   *
   * @param inTls The TLs contexts
   *
   * @return A TLS reloader
   */

  public static StTLSReloader create(
    final StTLSContextServiceType inTls)
  {
    return new StTLSReloader(inTls);
  }

  @Override
  public void close()
  {
    this.executor.shutdown();
  }

  @Override
  public String description()
  {
    return "TLS reloading service.";
  }
}
