# TODO
Todo tracking visible inside the code, avoiding a public Trello or Jira board

* Fix how expected size of login challenge request is handled since username can vary in length
* Timeout the auth connection if socket opens but no commands received (30s)
* Handle Authenticator (Not planning to handle PIN for Classic client)
* Cache the Realm List so we aren't querying _all realms_ on every login
* Hunt down a bunch of in-code TODOs
* Refactor entire project into multi-module in prep for Game Server. Common/Auth/Game/Other - account management? Website? API?