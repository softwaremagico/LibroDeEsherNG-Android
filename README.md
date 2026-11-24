# Libro de Esher NG — Android

[![Licencia GNU GPL 3.0](https://img.shields.io/badge/licencia-GNU_GPL_3.0-brightgreen.svg)](LICENSE)
[![Plataforma Android](https://img.shields.io/badge/plataforma-Android-3DDC84.svg?logo=android&logoColor=white)]()
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=OpenJDK&logoColor=white)]()

> Los latidos de su corazón retumbaban en sus oídos mientras corría a través de los largos
> corredores. «Pixie, no..., sílfide no..., gnomo, no...», gesticulaban sus labios en voz baja
> mientras pasaba las hojas de ese viejo libro que ahora tenía entre sus manos.
>
> «Troll de guerra», dijo otra vez, cada vez más alto, como si saboreara las palabras.
>
> — extracto de la introducción original de *El Libro de Esher*

## Acerca de Libro de Esher

El Libro de Esher es una aplicación que facilita la creación de fichas de personajes para tus
jugadores o la creación de personajes no jugadores en unos pocos segundos. Permite además
personalizar razas, culturas y profesiones, y ofrece la flexibilidad necesaria para cualquier tipo
de partida de **Rolemaster / El Libro de Esher**.

Fue desarrollado originalmente por Jorge Hortelano Otero como una aplicación de escritorio en Java,
distribuida como software libre bajo licencia GNU. Este repositorio es la **versión Android**,
construida sobre [LibroDeEsherNG](https://github.com/softwaremagico/LibroDeEsherNG): el mismo motor
de reglas y modelo de creación de personajes, modernizado como biblioteca Java sin dependencias de
AWT/Swing, y preparado para ejecutarse en un teléfono o tableta.

Es una herramienta **no oficial**, no afiliada a los titulares de los derechos de Rolemaster.

## Funcionalidades

### Creación y desarrollo de personajes

- Creación paso a paso con nombre, sexo, raza, cultura, profesión, nivel y edad.
- Generación aleatoria configurable, con especialización, talentos, adiestramientos preferidos y
  rangos sugeridos.
- Creación guiada de cultura y profesión: idiomas raciales/regionales, aficiones, orden de armas
  típicas, rangos de adolescencia, reinos de magia y habilidades comunes/profesionales/restringidas.
- Desarrollo completo: características, habilidades, listas de conjuros, dotes, adiestramientos,
  categorías, costes de armas, resistencias, equipo y trasfondo.
- Asistente de creación para quien prefiere seguir el flujo original ventana a ventana.

### Hoja, guardado e intercambio

- Hoja de texto y exportación a PDF en cuatro diseños: estándar, una columna, dos columnas y
  habilidades combinadas (legacy).
- Compartir la hoja de texto o el PDF directamente desde la app.
- Guardado local de varios personajes con Room; carga, eliminación y cambio de personaje.
- Importación/exportación de personajes mediante el selector de archivos de Android (SAF). El
  formato es JSON y acepta también las instantáneas `.rlm` JSON del modelo original.
- Importación/exportación de una **subida de nivel** aislada, con validación contra el personaje del
  nivel anterior.

### Opciones y módulos

- Panel de opciones de reglas: armas de fuego, poderes Chi, magia, listas oscuras, dotes que
  consumen puntos de trasfondo, coste máximo por categoría y otros detalles del reglamento.
- Activación/desactivación de módulos de reglas y selección del diseño de PDF.
- Idioma del catálogo y de las reglas (español/inglés) desde Ajustes.
- Ayuda integrada con el catálogo de razas, culturas, profesiones, habilidades, dotes y hechizos.

## Requisitos previos

- **JDK 17** (el wrapper de Gradle 9.6 está incluido en el repositorio).
- **Android SDK 37** instalado y `local.properties` apuntando a él (`sdk.dir=...`).
- Dispositivo o emulador con **API 31 o superior** para ejecutar la app y los tests instrumentados.
- **Maven 3.9+** para publicar los módulos de LibroDeEsherNG en el repositorio Maven local.

La app depende de `librodeesher-rules`, `librodeesher-random` y `librodeesher-pdf`, que se resuelven
desde `mavenLocal()`. Además, OpenPDF 1.x necesita en Android las dependencias `android-awt` y StAX,
ya declaradas en `app/build.gradle`.

## Compilación e instalación

Primero publica el motor de reglas NG en tu Maven local (se recomienda mantener ambos repositorios
como hermanos):

```bash
cd ../LibroDeEsherNG
mvn install
```

Después compila e instala la app:

```bash
cd ../LibroDeEsherNG-Android
./gradlew assembleDebug        # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala en el dispositivo/emulador conectado
```

Para generar un APK de release (sin ofuscar, `minifyEnabled=false`):

```bash
./gradlew assembleRelease
```

## Ejecución

Abre la app y usa la barra inferior para alternar entre **Personaje**, **Asistente**, **Aleatorio**,
**Características**, **Hoja** y **Ayuda**. Desde el menú superior puedes crear, guardar y cargar personajes, acceder al
panel de Desarrollo (cultura, profesión, adiestramientos, habilidades, conjuros, dotes, historial,
equipo, categorías y costes de armas), cambiar las opciones, el idioma o ver «Acerca de».

La pestaña **Personaje** muestra también el resto de ventanas como pestañas deslizables, para que
puedas revisar y editar todas las secciones sin salir del personaje.

## Subida de nivel remota

Igual que en la aplicación original de escritorio, el jugador y el director de juego pueden seguir
un flujo seguro para subir de nivel en casa:

1. El DJ exporta el personaje actual y se lo envía al jugador.
2. El jugador sube el nivel con la app y exporta **solamente la subida** (un archivo JSON de nivel).
   No debe enviar el personaje completo.
3. El DJ importa esa subida sobre su copia del personaje. La app reconstruye el personaje del nivel
   anterior y valida que el archivo corresponde exactamente a esa base antes de aplicarlo.
4. Para la siguiente subida, el DJ debe volver a enviar su personaje actualizado al jugador.

Nunca se debe sustituir la copia del DJ por la copia del jugador: la primera es la referencia de
confianza, exactamente como se describe en el manual original.

## Pruebas

```bash
./gradlew testDebugUnitTest      # tests JVM (persistencia, gestor de personajes, exportación)
./gradlew connectedDebugAndroidTest  # tests instrumentados UI-only (requiere dispositivo)
```

Los tests instrumentados comprueban flujos completos exclusivamente a través de la interfaz:
creación de los siete personajes de referencia y revisión de **todas** sus ventanas, verificación
del PDF exportado, subida de nivel, importación/exportación, opciones, historial y dotes. La
generación aleatoria y el renderizado de PDF son lentos en emulador; los tests amplían los timeouts
de Espresso hasta 15 minutos por este motivo.

La cobertura JVM incluye regresión de reglas y PDF frente a las fichas legacy del motor NG.

## Arquitectura

- Proyecto Gradle de un único módulo Android (`app`), Kotlin-free, Java 17 con desugaring.
- UI construida con Fragmentos, Navigation Component, ViewPager2, Material Components y Room.
- `CharacterManager` mantiene el personaje seleccionado en sesión; `OptionsHandler` persiste las
  opciones de reglas y módulos en `SharedPreferences`.
- `CharacterFileHelper` centraliza la importación/exportación de personajes y niveles mediante
  `ActivityResultContracts` (SAF).
- Todo el cálculo de reglas, generación aleatoria y exportación a PDF se delega a los módulos NG;
  la app no reimplementa reglas.

## Historial de versiones

### 1.0.0 (en desarrollo)

- Paridad con la aplicación original: opciones de reglas y módulos, creación guiada de cultura y
  profesión, selector de adiestramientos e historial/dotes interactivos.
- Importación/exportación de personajes y de subidas de nivel, con el flujo seguro DJ/jugador.
- Cuatro diseños de PDF, compartición de hoja y PDF, guardado múltiple con Room.
- Cobertura UI-only de los personajes de referencia y regresión de PDF frente a las fichas legacy.

## Notas

- Iconos y fuentes incluidos en el proyecto bajo sus licencias correspondientes.
- Rolemaster es una marca registrada de Iron Crown Enterprises / Aurigas Aldebaron.
- Licencia GNU General Public License v3; el texto completo está en [`LICENSE`](LICENSE).

## Más información

- Aplicación original: https://github.com/softwaremagico/LibroDeEsher
- Motor de reglas NG: https://github.com/softwaremagico/LibroDeEsherNG
- Esta app Android: https://github.com/softwaremagico/LibroDeEsherNG-Android
- Bugs, ideas y sugerencias: softwaremagico@gmail.com
