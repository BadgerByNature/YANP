# CHANGELOG

## 2026-02-20
* Server currently handles client login up through proof validation using a real TBC client against a real in-use CMangos database 
  * Real account salts/verifiers are being used
  * SRP6 M1 and M2 values match CMangos. M1 is being successfully verified server-side
  * The client attempts to make a Realm List request, proving that our M2 is being accepted


Tons of cleanup and refactoring is needed already. A lot of the code taken from the JavaWoW repo could use some TLC,
and a lot of the code written here so far was Minimum Viable Product style.
