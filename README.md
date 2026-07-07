# Restricted Operator

---

### Usage

---
**Restricted Operator disables most harmful commands enabled by ```/op```.**

**Admins must add themselves to ```bypass-usernames``` in the plugin's ```config.yml``` to bypass these restrictions.**

Alternatively, using the server's console only, the following commands may be run to modify the config without opening the file:
* ```/bypass add Player```
* ```/notify add Player```

### Build Instructions:

---
1. Clone repository using Git or downloading the zip
2. Download JDK 25 at one of the links below:
   * [Windows](https://www.oracle.com/java/technologies/downloads/#jdk25-windows)
   * [MacOS](https://www.oracle.com/java/technologies/downloads/#jdk25-mac)
   * [Linux](https://www.oracle.com/java/technologies/downloads/#jdk25-linux)
3. Run one of the following commands inside the repo:
   * Windows: ```.\gradle.bat build```
   * MacOS/Linux: ```./gradlew build```
4. The compiled plugin jar file will be located at: ```build/libs/restricted-operator-1.0-SNAPSHOT.jar```