stonesignal
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.stonesignal/com.io7m.stonesignal.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.stonesignal%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.stonesignal/com.io7m.stonesignal?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/stonesignal/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/stonesignal.svg?style=flat-square)](https://codecov.io/gh/io7m-com/stonesignal)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.stonesignal](./src/site/resources/stonesignal.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/stonesignal/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/stonesignal/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/stonesignal/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/stonesignal/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/stonesignal/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/stonesignal/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/stonesignal/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/stonesignal/actions?query=workflow%3Amain.windows.temurin.lts)|

## stonesignal

The `stonesignal` package provides a server for receiving and storing location
data from devices.

## Features

* Accept location data from devices using a trivial JSON and/or CBOR API.
* Complete audit log; every operation that changes the state of the system is
  logged in an append-only log.
* Fully instrumented with [OpenTelemetry](https://opentelemetry.io/).
* A small, easily auditable codebase with a heavy use of modularity for
  correctness.
* An extensive automated test suite with high coverage.
* Platform independence. No platform-dependent code is included in any form,
  and installations can largely be carried between platforms without changes.
* Extensive documentation including information on installation, a setup
  tutorial, a theory of operation, maintenance and monitoring information,
  information on security properties, and full API documentation.
* [OCI](https://opencontainers.org/)-ready: Ready to run as an immutable,
  stateless, read-only, unprivileged container for maximum security and
  reliability.
* [OSGi](https://www.osgi.org/)-ready.
* [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System)-ready.
* ISC license.

## Usage

See the [documentation](https://www.io7m.com/software/stonesignal).

