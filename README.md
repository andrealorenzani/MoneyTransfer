![Upgraded with Claude](https://img.shields.io/badge/Upgraded%20with-Claude-D97757)

# MoneyTransfer

A small REST service that keeps users with an amount of money in memory and lets you add money to a user or transfer
money between two users. It started as the solution to a Java/Scala developer test
(see `Java%2FScala Developer Test - Revolut.pdf`) where the point was *the technologies and how they are plugged
together*, not the algorithm.

## Upgraded after 9 years

The code was written in 2017 and upgraded in 2026 with Claude. The old stack could not simply be bumped: Finagle's
last release (24.2) only exists for Scala 2.13, Scala 2.11 is long gone, and Cucumber for Scala and the sbt cucumber
plugin were abandoned. So this is a rewrite that keeps the same design and the same HTTP API.

| Area | Before (2017) | Now |
|------|---------------|-----|
| Language | Scala 2.11.11 | Scala 3.10 (enums, `Either`, indentation syntax, `@main`) |
| Build | sbt 0.13.16 + cucumber plugin from an HTTP repository | sbt 1.12 |
| HTTP server | Finagle 6.45 | `com.sun.net.httpserver` from the JDK, on virtual threads (Java 21+) |
| JSON | Jackson 2.8 + `ScalaObjectMapper` | jsoniter-scala 2.42 (compile-time codecs, no reflection) |
| Tests | ScalaTest 3.0 + Mockito 1.9 + Cucumber 1.2 | munit 1.3, real fakes instead of mocks, 28 tests |
| Java | 8 | 21 or newer |

What changed in the code:

* **Money is `BigDecimal`**, not `Double`, so `0.1 + 0.2` is exactly `0.3`.
* **Errors are values**: the data store returns `Either[StoreError, A]` (an `enum`) instead of throwing exceptions and
  wrapping them in `Try`.
* **The routes are a plain function** (`Api.handle(method, path)`), tested without any server. `RestServer` is a thin
  adapter to the JDK server. The `RestService` / `RequestHandler` / `TransferHandler` / `UserHandler` hierarchy and the
  global `DataStore` singleton are gone; the store is passed in explicitly.
* **Insertion order** is now a guarantee (`VectorMap`) instead of a coincidence of `HashMap`.
* **Behaviour fixes** (all of these used to misbehave):
  * invalid amounts (`abc`, negative, zero) answer `400` instead of `500`/silently succeeding;
  * a transfer from an account to itself is rejected (it used to credit money out of thin air);
  * an unknown user answers `404` instead of `400`; a created user answers `201`;
  * a request with a wrong method or path answers `404`.
* **Tests**: the Cucumber scenarios (which needed a special plugin and a sleep-based setup) are now an ordinary
  end-to-end suite over real HTTP on a random port. Added tests for fractional amounts, invalid input, duplicate ids,
  insertion order and heavy concurrent transfers.

## Usage

Requirements: JDK 21+ and [sbt](https://www.scala-sbt.org/).

```bash
sbt run     # starts the server on port 8080 (set PORT to change it)
sbt test    # unit and end-to-end tests
```

### API

| Request | Effect |
|---------|--------|
| `GET /` | all users |
| `GET /size` | `{"size": <number of users>}` |
| `GET /user/{id}` | one user, `404` if unknown |
| `POST /user/{name}/{id}` | create a user with zero availability (`201`), `400` if the id is taken |
| `POST /user/{name}` | same, using the current number of users as id |
| `PUT /add/{id}/{money}` | add money to a user |
| `PUT /move/{idSource}/{money}/{idDest}` | transfer money, `400` with an explanation if it is not possible |

```bash
curl -X POST http://localhost:8080/user/Thomas/id1
curl -X POST http://localhost:8080/user/Brunone/id2
curl -X PUT  http://localhost:8080/add/id1/500
curl -X PUT  http://localhost:8080/move/id1/100/id2
curl         http://localhost:8080/
```

## Design notes (from the original solution)

* The in-memory store only holds what the test needs: users and their available money. APIs to create users and add
  money exist so the service can be exercised.
* Users from outside the company, deleting users and similar topics were deliberately left out of scope.
* Everything that depends on the outside world (the store) is injected, with no injection framework.
* `DataStore` is a trait: another implementation can be plugged in without touching the routes.
* Writes to `InMemoryDataStore` are serialised so a transfer is atomic; reads use an immutable snapshot.

## License

[Apache License 2.0](LICENSE), Copyright 2017-2026 Andrea Lorenzani. The PDF with the original assignment is not part of
this license and belongs to its authors.
