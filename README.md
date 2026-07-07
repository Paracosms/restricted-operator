# Restricted Operator

---

**Restricted Operator disables most harmful commands enabled by ```/op```.**

**This plugin may be used to teach players about admin commands and command blocks in a safe server environment without giving unrestricted access to harmful commands.**

### Usage

---

**The restricted commands, namespaces, and selectors can be modified in the plugin's ```config.yml``` file.**

**Admins must add themselves to ```bypass-usernames``` in the plugin's ```config.yml``` to bypass these restrictions.**

Alternatively, using the server's console only, the following commands may be run to modify the config without opening the file:
* ```/bypass add Player```: Unrestricts Player's command use. This is equivalent to giving ```/op``` permissions without this plugin.
* ```/notify add Player```: Player will receive notifications about anyone attempting to run restricted commands.

**Admins with bypass permissions have access to the following commands:**
* ```/unrestrict x y z```: Trusts the command block's current command at that location.
If the command is changed later, the block is monitored again until it is unrestricted again.
* ```/restrictedoperator config```: Main command for modifying ```config.yml``` in game.

### Default Restrictions

---

The following is an excerpt from ```config.yml``` that contains the default command restrictions.

```
# Disables any command starting with these roots.
blocked-roots:
- stop
- restart
- reload
- rl
- op
- deop
- ban
- ban-ip
- kick
- whitelist
- kill
- clear
- execute
- function
- schedule
- data
- damage
- worldborder
- difficulty
- gamerule
- plugins
- pl

# Disables all commands starting with these namespaces (e.g. any command starting with "minecraft:" will be blocked).
blocked-namespaces:
- minecraft
- bukkit
- spigot
- paper

# Disables any command containing these selectors.
blocked-selectors:
- "@e"
- "@a"
- "@r"
```

### Build Instructions

---
1. Clone repository using Git or by downloading the zip directly through GitHub
2. Download JDK 25 at one of the links below:
   * [Windows](https://www.oracle.com/java/technologies/downloads/#jdk25-windows)
   * [MacOS](https://www.oracle.com/java/technologies/downloads/#jdk25-mac)
   * [Linux](https://www.oracle.com/java/technologies/downloads/#jdk25-linux)
3. Run one of the following commands inside the repo:
   * Windows: ```.\gradle.bat build```
   * MacOS/Linux: ```./gradlew build```
4. The compiled plugin jar file will be located at: ```build/libs/restricted-operator-1.0-SNAPSHOT.jar```
