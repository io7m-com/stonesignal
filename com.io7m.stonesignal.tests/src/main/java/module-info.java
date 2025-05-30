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


open module com.io7m.stonesignal.tests
{
  requires com.io7m.stonesignal.protocol.admin;
  requires com.io7m.stonesignal.protocol.data;
  requires com.io7m.stonesignal.protocol.device;
  requires com.io7m.stonesignal.server;

  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.cbor;
  requires com.io7m.darco.api;
  requires com.io7m.ervilla.api;
  requires com.io7m.ervilla.postgres;
  requires com.io7m.ervilla.test_extension;
  requires com.io7m.percentpass.extension;
  requires com.io7m.zelador.test_extension;
  requires java.net.http;
  requires org.junit.jupiter.api;
  requires org.junit.jupiter.engine;
  requires org.junit.platform.commons;
  requires org.junit.platform.engine;
  requires org.junit.platform.launcher;
  requires org.slf4j;
}