# Libro de Esher NG — Android

[![GNU GPL 3.0 License](https://img.shields.io/badge/license-GNU_GPL_3.0-brightgreen.svg)](LICENSE)
[![Android platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android&logoColor=white)]()
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=OpenJDK&logoColor=white)]()

> His heartbeat pounded in his ears as he ran through the long corridors. "Pixie, no...
> sylph, no... gnome, no..." his lips mouthed in a low voice as he turned the pages of that old
> book he had found long ago and now held in his hands. He knew these corridors like the back of
> his hand, which let him find what he needed while fleeing toward the heart of his lair...
> "Goblin, no... orc, maybe, though I need something bigger...". His fingers kept turning until,
> finally, they stopped on one page: "War troll". His lips repeated it again, "War troll", and
> again, and again, as if trying to make the idea real through the sound of his own voice alone.
>
> — excerpt from the introduction to *El Libro de Esher*

## About Libro de Esher

Libro de Esher is an application that makes it easy to create character sheets for your players,
or to create non-player characters in just a few seconds. It also lets you customize races,
cultures and professions, giving you all the flexibility needed for any kind of
**Rolemaster / El Libro de Esher** game.

It was originally developed by Jorge Hortelano Otero as a free-software desktop application,
distributed under the GNU license. This repository is the **Android version**, built on top of
[LibroDeEsherNG](https://github.com/softwaremagico/LibroDeEsherNG): the same rules engine and
character creation model, modernized as a Java library without AWT/Swing dependencies and ready to
run on a phone or tablet.

It is an **unofficial** tool, not affiliated with the holders of the Rolemaster rights.

## Features

### Character creation and development

- Step-by-step creation: name, sex, race, culture, profession, level and age.
- Configurable random generation, including specialization, talents, preferred trainings and
  suggested ranks.
- Guided culture and profession creation: racial/regional languages, hobbies, typical weapon
  order, adolescent ranks, spell realms, and common/professional/restricted skills.
- Full development: characteristics, skills, spell lists, perks, trainings, categories, weapon
  costs, resistances, equipment and background points.
- A creation assistant for those who prefer to follow the original window-by-window flow.

### Sheet, saving and sharing

- Text sheet and PDF export in four layouts: standard, one column, two columns and combined
  skills (legacy).
- Share the text sheet or the PDF straight from the app.
- Local storage of multiple characters with Room; load, delete and switch characters.
- Character import/export through the Android Storage Access Framework (SAF). The format is JSON
  and the legacy JSON `.rlm` snapshots are accepted as well.
- Import/export of an isolated **level-up**, validated against the character at the previous
  level.

### Options and modules

- Rules options panel: firearms, Chi powers, magic, dark spell lists, perks that consume
  background points, maximum category cost and other rule details.
- Enable/disable rule modules and choose the PDF layout.
- Catalog and rules language (Spanish/English) from Settings.
- Built-in wiki with the catalog of races, cultures, professions, skills, perks and spells.

## Requirements

- **JDK 17** (the Gradle 9.6 wrapper is included in the repository).
- **Android SDK 37** installed and `local.properties` pointing to it (`sdk.dir=...`).
- A device or emulator with **API 31 or higher** to run the app and the instrumented tests.
- **Maven 3.9+** to publish the LibroDeEsherNG modules to the local Maven repository.

The app depends on `librodeesher-rules`, `librodeesher-random` and `librodeesher-pdf`, resolved
from `mavenLocal()`. In addition, OpenPDF 1.x needs the `android-awt` and StAX dependencies on
Android; both are already declared in `app/build.gradle`.

## Building and installing

First publish the NG rules engine to your local Maven repository (keeping both repositories as
siblings is recommended):

```bash
cd ../LibroDeEsherNG
mvn install
```

Then build and install the app:

```bash
cd ../LibroDeEsherNG-Android
./gradlew assembleDebug        # produces app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # installs it on the connected device/emulator
```

To build a release APK (not obfuscated, `minifyEnabled=false`):

```bash
./gradlew assembleRelease
```

## Usage

Open the app and use the bottom bar to switch between **Character**, **Assistant**, **Random**,
**Characteristics**, **Sheet** and **Help**. The top menu lets you create, save and load
characters, open the Development panel (culture, profession, trainings, skills, spells, perks,
history, equipment, categories and weapon costs), change the options, switch the language or read
the About screen.

The **Character** tab also shows the rest of the sections as swipeable tabs, so you can review and
edit everything without leaving your character.

## Remote level-up

As in the original desktop application, the player and the game master can follow a safe workflow
to level up at home:

1. The GM exports the current character and sends it to the player.
2. The player levels up in the app and exports **only the level-up** (a level JSON file). The full
   character must not be sent back.
3. The GM imports that level-up on top of their copy of the character. The app rebuilds the
   previous-level character and verifies that the file matches that exact base before applying it.
4. For the next level-up, the GM must send their updated character to the player again.

The GM's copy must never be replaced with the player's copy: the former is the trusted reference,
exactly as described in the original manual.

## Tests

```bash
./gradlew testDebugUnitTest      # JVM tests (persistence, character manager, export)
./gradlew connectedDebugAndroidTest  # UI-only instrumented tests (requires a device)
```

The instrumented tests exercise complete flows exclusively through the UI: creation of the seven
reference characters with a review of **every** window, verification of the exported PDF,
level-up, import/export, options, history and perks. Random generation and PDF rendering are slow
in an emulator, so Espresso timeouts are extended to 15 minutes.

JVM coverage includes rules and PDF regression against the legacy sheets of the NG engine.

## Architecture

- Single-module Android Gradle project (`app`), Kotlin-free, Java 17 with desugaring.
- UI built with Fragments, Navigation Component, ViewPager2, Material Components and Room.
- `CharacterManager` keeps the selected character in session; `OptionsHandler` persists rules and
  module options in `SharedPreferences`.
- `CharacterFileHelper` centralizes character/level import and export through
  `ActivityResultContracts` (SAF).
- All rules calculation, random generation and PDF export are delegated to the NG modules; the app
  does not reimplement any rules.

## Version history

### 1.0.0 (in development)

- Parity with the original application: rules/module options, guided culture and profession
  creation, training selector, and interactive history/perks.
- Character and level-up import/export, with the safe GM/player workflow.
- Four PDF layouts, text/PDF sharing and multiple-character storage with Room.
- UI-only coverage of the reference characters and PDF regression against the legacy sheets.

## Notes

- Icons and fonts are included in the project under their respective licenses.
- Rolemaster is a registered trademark of Iron Crown Enterprises / Aurigas Aldebaron.
- Licensed under the GNU General Public License v3; the full text is in [`LICENSE`](LICENSE).

## More information

- Original application: https://github.com/softwaremagico/LibroDeEsher
- NG rules engine: https://github.com/softwaremagico/LibroDeEsherNG
- This Android app: https://github.com/softwaremagico/LibroDeEsherNG-Android
- Bugs, ideas and suggestions: softwaremagico@gmail.com
