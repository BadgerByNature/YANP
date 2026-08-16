# A Groovy-based World of Warcraft Login Server

Designed to work with CMaNGOS TBC (https://github.com/cmangos/mangos-tbc)

This started out as a potential WoW Server, but after getting a working Login server I realized
what a ridiculous amount of work it would be to even begin getting a whole server up and running.
CMaNGOS has _individual files_ with over 10,000 lines. There must be hundreds of thousands
of lines of actual server code. It would be a fool's errand to try to port the rest of this to Groovy,
and the benefits to doing so are fairly small.

I've changed the repo to show that it is
just a Login Server (Auth Server) before making it publicly available so that anyone curious what
a Java/Groovy version of the current WoW systems would look like has something to play with. Enjoy!

### Resources Referenced

* https://github.com/JavaWoW/JavaWoW/
* https://github.com/vmangos/core
* https://github.com/cmangos/mangos-tbc
* https://github.com/azerothcore/azerothcore-wotlk/


License Information
=====================
This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see http://www.gnu.org/licenses/.