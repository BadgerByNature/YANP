# CHANGELOG

## 2026-03-01
* Added verification of build hash from client vs expected build hash on server

## 2026-02-28
* Additional auth security added via more fine-grained AuthStatus control
* Added support for client reconnect - if client is logged in, then uses `/logout` then selects Change Realm you hit this logic.
Found that this logic is broken in CMangos which added some complexity.

## 2026-02-26
* Server now handles client login up through realmlist, which means clients can be succesfully routed to
a CMangos server via our auth.
  * We are writing the SessionKey to the database, so the CMangos server Auth does accept us - tested and proven
  * All realmlist fields are included, such as build info if realmflags have it enabled.

## 2026-02-20
* Server currently handles client login up through proof validation using a real TBC client against a real in-use CMangos database 
  * Real account salts/verifiers are being used
  * SRP6 M1 and M2 values match CMangos. M1 is being successfully verified server-side
  * The client attempts to make a Realm List request, proving that our M2 is being accepted
* Tons of cleanup and refactoring is needed already. A lot of the code taken from the JavaWoW repo could use some TLC,
and a lot of the code written here so far was Minimum Viable Product style.
