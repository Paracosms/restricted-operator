# Restricted Operator

---

**Restricted Operator disables most harmful commands enabled by ```/op```.**

**This plugin may be used to teach players about admin commands and command blocks in a safe server environment without giving unrestricted access to harmful commands.**

### Installation

Restricted Operator currently supports Paper servers. If you have not set up a Paper server yet, start with the [Paper getting started guide](https://docs.papermc.io/paper/getting-started/).

1. [Download the latest release](https://github.com/Paracosms/restricted-operator/releases/latest) for your server version.
2. Place the `restricted-operator-<version>.jar` file in your server's `plugins` directory.
3. Start or restart the server.
4. Check the server log to confirm the plugin loaded successfully.

After startup, players with `/op` will still have operator status, but commands matching this plugin's restrictions will be blocked until you adjust the configuration.

### Usage

---

**The restricted commands, namespaces, and selectors can be modified in the plugin's ```config.yml``` file.**

**Admins must add themselves to ```bypass-usernames``` in the plugin's ```config.yml``` to bypass these restrictions.**

Alternatively, using the server's console only, the following commands may be run to modify the config without opening the file:
* ```/bypass add Player```: Unrestricts Player's command use. This is equivalent to using ```/op``` without this plugin.
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

# Disables all commands starting with these namespaces.
# (e.g. any command starting with "minecraft:" will be blocked)
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
### Gallery

---

**What ```/op``` users see when they try to execute a restricted command:**

Note: This text can be modified inside the plugin's ```config.yml```.


![blocked_chat_command.png](screenshots/blocked_chat_command.png)

![blocked_command_block.png](screenshots/blocked_command_block.png)
---

**What admins with ```notify``` see when ```/op``` users execute a restricted command:**

![notify_command_block.png](screenshots/notify_command_block.png)
---

**Example of the ```/unrestrict x y z``` command:**

![unrestricted_command_block.png](screenshots/unrestricted_command_block.png)

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
