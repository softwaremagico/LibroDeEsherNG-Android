# Auditoría AWT ⇄ Android de LibroDeEsher

Fecha: 2026-11-24. Alcance: equivalencia funcional y de resultados entre la UI AWT original
(`LibroDeEsher`) y la app Android (`LibroDeEsherNG-Android`), más las acciones generales de
`ThinkMachine-4E-Advisor` usadas como referencia de UX. Los tests instrumentados son **UI-only**:
no invocan APIs internas de la librería desde el test; verifican pantallas, botones y diálogos.

## 1. Resultado ejecutivo

- La app Android cubre los flujos de creación, edición, subida de nivel, persistencia, importación,
  exportación y PDF de los siete personajes de referencia.
- La paridad de valores PDF legacy ⇄ NG está validada para Nihal N9 (13 correcciones confirmadas),
  Kara V2.2.1 y los export de una columna de Fthagtli/Gorju; las diferencias residuales conocidas
  son de catálogo/versión de datos, no de render.
- La auditoría encontró **tres huecos reales** de la UI AWT: aleatorizar características, resolver
  opciones de dotes (`PerkChoiceGrant`) y emparejar defectos/weakness. Se corrigieron en el issue
  `#22` (`0a04c15`, commit publicado en `origin/main`) y se cubren con tests UI-only nuevos. La issue
  está cerrada con comentario de evidencia: https://github.com/softwaremagico/LibroDeEsherNG-Android/issues/22.
- `#23` cerró el mismo flujo para dotes aleatorias (`abe119c`, batería UI-only 20/20).
- `#24` añade el indicador de cambios sin guardar y las confirmaciones de Advisor en las acciones
  que descartan trabajo; `#25` añade la clonación de personaje en sesión, otra acción general de
  Advisor que faltaba.
- `#26` corrige un cuarto hueco real detectado por la batería UI-only: la pantalla “Aleatorio”
  entregaba al generador un personaje sin raza/cultura/profesión, algo que AWT impedía con
  `isEverythingSelectedCorrectly()` y que NG documenta como requisito previo. Ahora se siembra la
  selección actual (o entradas aleatorias del catálogo) antes de `createRandomValues()`.
- Las pantallas de generación y preferencias aleatorias se han rediseñado para parecerse a
  `ThinkMachine-4E-Advisor`: cabecera con pestañas, títulos de sección, filas «etiqueta a la
  izquierda / valor a la derecha», FAB con dado e `ExtendedFloatingActionButton`, y overlay de
  progreso. La funcionalidad y todos los IDs usados por los tests UI-only se conservan.
- El resto de diferencias son de plataforma/UX o funcionalidad **adicional** de Advisor; no implican
  pérdida de resultados AWT.
- Auditoría de rendimiento: la librería releía el catálogo XML en cada acceso (2832 lecturas por
  generación aleatoria) y Espresso esperaba 60 s por cada búsqueda de diálogo inexistente. `edf5b34`
  (NG) cachea los elementos hasta el cambio de módulos y `474183d` (Android) sustituye esa búsqueda
  por una inspección rápida de ventanas; la generación baja de 12,475 s a 1,969 s en JVM y el barrido
  `CoverageExpansionUiTest` de 1009 s a 110 s.

## 2. Matriz AWT ⇄ Android

| AWT (`MainMenu` / ventanas) | Android | Estado |
| --- | --- | --- |
| Archivo › Nuevo | `action_new_character` + asistente de creación | Equivalente |
| Archivo › Cargar | `action_load_character` (selector de fichero/sesión) | Equivalente |
| Archivo › Salvar | `action_save_character` (Room/JSON) | Equivalente |
| Archivo › Exportar (personaje, nivel, TXT, TXT resumen, PDF estándar/combinada 1/2 col.) | `SheetFragment`: compartir TXT, TXT abreviado, exportar PDF, compartir PDF | Equivalente en resultados; el “compartir” sustituye al guardado de fichero de escritorio |
| Archivo › Importar (personaje/nivel) | `CharacterFileHelper` + diálogo de carga | Equivalente con restricción Android (`content://` requiere resolver el Uri) |
| Personaje › Características | pestaña `Características` | Equivalente; `#22` añade `Aleatorio` |
| Personaje › Cultura / Profesión | pestaña `Personaje` y pestañas de detalle | Equivalente |
| Personaje › Adiestramientos | pestaña `Adiestramientos` | Equivalente |
| Personaje › Habilidades / Rangos | pestaña `Habilidades` + diálogos de rangos | Equivalente; uso de `setCurrentLevelCategoryRanks` cerró el hueco histórico de categorías |
| Personaje › Talentos / Opciones del talento | pestaña `Dotes` | Equivalente tras `#22` (diálogos de opciones y defecto) |
| Personaje › Historial / Trasfondo | pestaña `Historial` | Equivalente |
| Personaje › Subir Nivel | flujo de subida de nivel | Equivalente |
| Insertar › Equipo / Objeto mágico / Edad | pestaña `Equipo` + `action_insert_age` | Equivalente |
| Aleatorio › Nombre / Personaje aleatorio | asistente aleatorio + preferencias aleatorias persistentes | Equivalente y ampliado; `#26` restaura el requisito AWT de raza/cultura/profesión previas |
| Configuración › Opciones / Debug | `OptionsFragment` + `SettingsHandler` | Equivalente |
| Ayuda › Acerca de | `action_about` | Equivalente |

### Huecos cerrados en `#22`

1. **Características aleatorias**: botón `Aleatorio` en `fragment_characteristics.xml`, cableado en
   `CharacteristicsFragment` a `RandomCharacterPlayer.setRandomCharacteristics(character, 0)`;
   desaparece cuando el personaje ya confirmó características.
2. **Opciones de dotes**: `PerksFragment` ofrece un botón `Opciones` en cada dote seleccionada con
   concesiones pendientes y resuelve `PerkChoiceGrant` de forma secuencial (fijos, elegir N, y
   elegir al menos uno para `optionsToChoose == -1`) mediante `CharacterPlayer.applyPerkChoiceGrant`.
3. **Defectos/weakness**: con `Las dotes consumen puntos de trasfondo` activo, al añadir una dote de
   coste positivo se ofrecen los defectos compatibles (`getAvailableWeaknessIds`) o `Ninguna`, y se
   aplica `addWeakness`. La fila muestra el defecto emparejado.

## 3. Acciones generales: ThinkMachine-Advisor ⇄ Android

| Acción Advisor | Equivalente Android | Observación |
| --- | --- | --- |
| Cargar fichero de personaje | `action_load_character` | Advisor usa `uri.getPath()`, Android tiene el mismo riesgo con `content://` |
| Nuevo personaje | asistente de creación | Android añade asistente guiado |
| Guardar | `action_save_character`, `action_save_as_character` (#29) | Guardar como crea una copia con nombre sin cambiar la selección; `Remove` solo quita de sesión y `Delete` ofrece `Deshacer` que reinserta la fila Room |
| Compartir | botones de compartir TXT/PDF | No existe en AWT; es una conveniencia móvil |
| Asistente | `CharacterAssistantFragment` | Función adicional de Android |
| Preferencias aleatorias | `action_random_preferences` | Función adicional de Android, persistida; UI rediseñada con la gramática visual de Advisor |
| Idioma | selector de idioma en ajustes | Función adicional de Android |
| Clonación | `action_clone_character` (`#25`) | Copia sin guardar en la sesión, como en Advisor |
| QR | no implementado en Android | Función de Advisor no presente en AWT; equivaldría a importar/exportar comprimido por imagen y no afecta a resultados AWT |
| Indicador de cambios sin guardar | marcador `*` en el título y confirmaciones (`#24`) | Equivalente a Advisor; `Guardar` y `Cargar` limpian el marcador |

## 4. Evidencias

- Baterías UI-only previas: `#22` → `OK (20 tests)` (`/tmp/opencode/final-all-ui-22.log`);
  `#23` → `OK (20 tests)` (`/tmp/opencode/issue23-all-ui-2.log`), ambas con origen publicado.
- `ded810e` refuerza el requisito UI-only: las comprobaciones de reglas que llamaban directamente a
  la librería se movieron al source set JVM (`app/src/test`), y las comprobaciones de persistencia y
  PDF ya presentes en `SheetActionsUiTest` y los E2E se eliminaron de `app/src/androidTest`. Tras este
  cambio, los tests instrumentados solo impulsan la interfaz y validan ficheros exportados.
- La librería NG pasa de `a29d114` a `edf5b34` (`origin/master`): `XmlFactory.readXml()` sirve una
  copia de la caché de elementos fusionados y solo la reconstruye tras
  `ModuleManager.resetModules()`. `mvn test` pasó con 92 tests (incluido el nuevo test de caché) y
  la regresión PDF se ejecutó en la misma corrida. Android consume esa versión instalada vía
  `mavenLocal` y sus APK se recompilaron con `--refresh-dependencies`.
- `#24`/`#25`/`#26`: `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest` y
  `lintDebug` completados correctamente. Tras la batería completa que detectó diez fallos por
  esperas de render y un diálogo de ajustes pendiente, se publicaron `a594720` (esperas y cierre de
  diálogos), `312c740` (confirmación obligatoria en la preparación del test de cambios sin guardar)
  y `ddfb69b` (espera a que la pestaña termine de renderizar antes de verificarla).
- `134c4fc` publica la política de privacidad en el repositorio; `880c994` y `b78720d` integran los
  cambios remotos (banner y README). `9b29c98` configura JaCoCo y el timeout de instrumentación.
- Rediseño Advisor-like de las pantallas aleatorias: `8d205cf` introduce el shell
  CoordinatorLayout/AppBarLayout/TabLayout, los títulos `RandomSectionTitle`, las filas
  etiqueta/valor, el FAB con `@drawable/ic_random_icon`, el `ExtendedFloatingActionButton` de
  preferencias y el overlay de progreso; `f8ae6d1`, `562080a`, `3430db7` y `b131ff3` ajustan
  el ancho y desplazamiento de etiquetas, la visibilidad del resumen y los botones, y las
  interacciones de los tests afectadas por esperas de idle de Espresso. Se conservan los IDs de
  los tests instrumentados; la causa de las esperas largas se determinó después: el `RootViewPicker` de Espresso aguarda 60 s
   cuando `inRoot(isDialog())` no encuentra ningún diálogo; `474183d` evita esa búsqueda comprobando
   el tipo de ventana de cada root.
- Tests focalizados del rediseño: `/tmp/opencode/random-ui-focused6.log` → `OK (3 tests)`,
  `Time: 1.737,558 s` (`RandomPreferencesUiTest` completo y
  `AdditionalScreensUiTest#randomScreenGeneratesSavesAndExportsACharacter`). Los logs intermedios
  (`final-full-ui4.log`, `random-ui-focused.log` a `random-ui-focused5.log`) registran los fallos ya
  corregidos.
- Tests JVM añadidos: serialización JSON, entidad Room, manager de sesión (dirty tracking,
  clonación, cambio/cierre), validador, resumen y fechas. Tests instrumentados añadidos:
  `UnsavedChangesUiTest`, `AdditionalScreensUiTest` (asistente, wiki, aleatorio),
  `DevelopmentScreensUiTest` (habilidades, historial, equipo, conjuros, opciones, etc.),
  `SheetActionsUiTest` (guardar, cargar, PDF, compartir, subir de nivel, importar/exportar) y
  `UiCoverageSweepTest` (lista de personajes, detalles, características, dotes, compartir/importar,
  idioma y wiki).
- Cobertura: JaCoCo combinatorial (`testDebugUnitTest` + `coverage.ec` de la batería con
  `-e coverage true`), informe en
  `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.csv`. Las clases generadas por
  Android/Room se excluyen del denominador. La medición parcial intermedia fue 1992/4631 líneas
  (43,0 %); la medición final tras la batería completa se documenta al cierre de esta sección.
- Nihal: el test UI con ocho subidas automáticas y comprobación del PDF pasó en 616,6 segundos
  (`/tmp/opencode/nihal-optimized2.log`). La ejecución histórica de 887,5 segundos no es una
  comparación controlada. Las subidas requieren aproximadamente 56–67 segundos cada una.
- Estado: las incidencias `#24`–`#26` están cerradas tras las pruebas focalizadas documentadas en
  sus comentarios. La batería integrada final y su medición de cobertura se registran al final de
  esta sección. Existe el directorio no rastreado `images/`, preservado. NG cerró en `cb72356`
  (`origin/master`) tras corregir la caché del catálogo (`edf5b34`) y los ajustes documentales
  de #1/#2.
- PDF: memories `.serena/memories/librodeesher-ng-pdf-parity.md` y
  `.serena/memories/android/nihal-e2e-verification.md`. Los artefactos crudos de `.parity/out/`
  están desactualizados y no deben usarse como evidencia.
- Personajes de referencia cubiertos en NG: Nihal, Morticia, Ophiliaglii, Kara, Fthagtli, Gorju y
  Skaag.

### Auditoría de rendimiento (cerrada)

- El cuello de botella del catálogo estaba en `XmlFactory.readXml()`: cada `getElements()` volvía a
  abrir y parsear los XML de todos los módulos. Una generación aleatoria JVM hacía 2832 lecturas
  (`skills.xml` 2764 veces) y tardaba 12,475 s. `edf5b34` cachea los elementos fusionados, devuelve
  una lista nueva a cada llamador y solo invalida la caché desde `ModuleManager.resetModules()`; la
  misma generación tardó 1,969 s con 9 lecturas
  (`/tmp/opencode/random-generation.log` ⇄ `/tmp/opencode/random-generation-committed.log`).
- `mvn test` en NG: 92 tests verdes, incluido `repeatedGetElementsReusesTheCachedElements`, que
  comprueba listas distintas con elementos compartidos. La batería genera los siete PDF de
  referencia en la misma ejecución, sin diferencias de paridad.
- Con las APK recompiladas contra la librería corregida, la pantalla aleatoria bajó de 279 s a 95 s
  y el asistente de 47 s a 12 s (`/tmp/opencode/focused-after-cache.log`). El barrido
  `CoverageExpansionUiTest` aún tardaba 1009 s: el coste ya no era XML sino Espresso.
- El desglose mostró ~60 s por acción aleatoria a pesar de que el código de la pantalla terminaba en
  207 ms. `UiDialogs.dismissAnyDialogIfPresent()` usaba `inRoot(isDialog())`; el `RootViewPicker` de
  Espresso 3.7.0 espera 60 s cuando no hay ningún root de diálogo. `474183d` inspecciona el tipo de
  las ventanas raíz (diálogo = `TYPE_APPLICATION`) con el root matcher por defecto y solo acude a
  Espresso cuando existe un diálogo. El barrido pasó en 109,825 s
  (`/tmp/opencode/coverage-after-dialog-fix.log`) y los 18 tests focalizados
  (`StandaloneScreensUiTest`, `UnsavedChangesUiTest`, `CoverageExpansionUiTest` y
  `SheetActionsUiTest`) en 592,163 s (`/tmp/opencode/focused-dialog-root.log`).

### Batería final y cobertura (cerrada)

- La ejecución `/tmp/opencode/final-full-ui5.log` fue interrumpida para comparar aceleración gráfica.
  La primera con GPU (`/tmp/opencode/final-full-gpu.log`) también se interrumpió al alcanzar el
  proceso del emulador su límite de una hora. Ninguna constituye una batería completa válida.
  `/tmp/opencode/final-full-gpu2.log` detectó una aserción prematura en la pestaña de habilidades;
  `c1d4934` añade la espera a que se pueble y su test focalizado pasó (`OK (3 tests)`,
  753,557 s). `/tmp/opencode/final-full-gpu3.log` terminó con `Tests run: 44, Failures: 2`: la dote
  «Adicción Mínima» no siempre está disponible (`caa64ac` usa la primera acción habilitada) y las
  acciones de adiestramiento dependen del personaje aleatorio (`dc317db` verifica el marcador con
  una edición estable tras la subida de nivel). `365e989` excluye las clases generadas por Room del
  denominador de JaCoCo, como indicaba esta auditoría. La repetición focalizada destapó diálogos
  encadenados al añadir dotes y pantallas que se pueblan de forma asíncrona; `bb088d3` y `e1bc006`
  cierran los diálogos, esperan las acciones habilitadas y usan una edición determinista como
  reserva, sin llamar a APIs internas. `bb088d3` añade `StandaloneScreensUiTest`, que abre
  `SkillsFragment` desde el submenú «Desarrollo», recorre sus botones y diálogos y verifica la
  pantalla de carga de personajes guardados. `de58982` añade `CoverageExpansionUiTest` (controles
  aleatorios, bonus de equipo y acciones de la hoja) y `667fc9b` corrige la edición de texto
  cuando el campo está fuera de pantalla. La repetición de `StandaloneScreensUiTest` sufrió un ANR
  al usar `onData(...).perform(click())` en una lista de diálogo; `4b3e2ec` sustituye esas pulsaciones
  por `UiViews.clickFirstDialogRow()`, que dispara `performItemClick` directamente y evita eventos
  táctiles y esperas de idle. La siguiente repetición destapó un fallo real: al introducir un
  identificador de especialización no válido, `SkillsFragment` propagaba la
  `IllegalArgumentException` del motor de reglas y cerraba la app; `1bc06a3` la captura y muestra
  `skills_error`, como ya hacía con `InvalidXmlElementException`.
- A petición del usuario se acerca la interacción a `ThinkMachine-4E-Advisor`: las acciones
  aleatorias de `Características`, `Cultura detallada`, `Profesión detallada`, `Historial`, `Dotes`
  y `Habilidades` son ahora un `FloatingActionButton` abajo a la derecha con `ic_random_icon` y
  overlay de progreso, como en `random_preferences_fragment.xml` de Advisor. Se eliminan los
  botones «Aceptar» de esas pantallas: las decisiones se aplican al seleccionar cada spinner
  (`CultureDetailsFragment`, `ProfessionDetailsFragment`), el historial se guarda mientras se escribe
  y las características se confirman automáticamente al agotar los puntos. Los identificadores
  usados por los tests (`characteristics_random`, `culture_random`, `profession_random`,
  `history_random`, `perks_random`, `skills_random`, `characteristics_confirm`, `history_apply`)
  se conservan; los dos últimos quedan como hooks internos no visibles. El barrido aleatorio debía
  abrir además el destino `Características` antes de esperar sus controles (`3d58b0e`). Con el
  rediseño, `StandaloneScreensUiTest` y las 14 pruebas de `UnsavedChangesUiTest` pasaron
  (`/tmp/opencode/focused-advisor-style.log`). El barrido `CoverageExpansionUiTest` pasó aislado
  en 879,396 s (`/tmp/opencode/coverage-expansion-focused2.log`). La batería completa
  `/tmp/opencode/final-full-advisor.log` terminó con `Tests run: 46, Failures: 1`: el hook oculto
  `characteristics_confirm` seguía habilitado después de la confirmación automática; `9cdcc5a` lo
  deshabilita. `7d26a5f` refuerza los recorridos UI-only de hoja, adiestramientos y equipo para
  reducir líneas sin cubrir.
- El emulador utiliza KVM (`-accel on`) y GPU NVIDIA RTX 4060 Ti (`-gpu host`), verificado mediante
  `dumpsys SurfaceFlinger`. El test `specializationIsSavedAndShownAgain` pasó en 58,426 s con
  SwiftShader y en 6,875 s y 5,382 s con GPU; esta comparación de un solo test no demuestra una
  mejora equivalente para toda la batería.
- Cobertura de la batería con dos fallos ya corregidos: 3080/4656 líneas (66,15 %) antes de
  excluir Room; tras excluir las clases generadas, 2949/4331 (68,09 %). Se añaden tests UI-only
  para las pantallas alcanzables solo por el submenú «Desarrollo», en particular `SkillsFragment`
  (248 líneas sin cubrir) y la pantalla de carga de personajes.
- Con la caché XML publicada y el arreglo de diálogos, `/tmp/opencode/final-full-after-cache.log`
  cerró `OK (46 tests)` en 1385,392 s, frente a 10.893 s antes de ambas correcciones. Se añadieron
  `CoverageTopUpUiTest`, `SheetFileResultsUiTest` (SAF con `espresso-intents`),
  `DevelopmentRanksUiTest`, `AdditionalScreensUiTest` y `CoveragePushUiTest` (generación aleatoria
  desde creación, dotes con opciones, adiestramientos, objeto mágico y edición de características).
  `MainActivityMenuUiTest` se descartó: abrir el menú overflow derribaba el emulador con
  `gfxstream ... error null ctx` bajo `-gpu host` y además usaba una API no disponible de
  `ActivityScenarioRule`.
- Batería final definitiva `/tmp/opencode/final-full-414d95d-coverage.log`: `OK (68 tests)` en
  2160,882 s con `Motoroal_G73` (`-accel on -gpu host`). La instrumentación manual **debe** pasar
  `-e coverage true`: los `testInstrumentationRunnerArgument` de `build.gradle` solo se aplican
  cuando ejecuta Gradle, y sin esa opción el `coverage.ec` del dispositivo queda sin actualizar.
  El `coverage.ec` resultante (`/tmp/opencode/final-full-414d95d-coverage.ec`) y
  `./gradlew jacocoTestReport` dan **3596/4465 líneas (80,54 %)**, 16538/20010 instrucciones
  (82,65 %) y 1079/1670 ramas (64,61 %). Se supera el objetivo de ≥80 % de líneas con tests
  exclusivamente UI-only: ningún test invoca APIs internas de la librería; se validan los siete
  personajes de referencia, los PDF exportados y la regresión PDF legacy⇄NG. Los cuatro tests
  añadidos tras la medición anterior (`AssistantReviewUiTest`, `DisabledModulesUiTest`,
  `RandomPreferenceOriginUiTest` y `SaveAsUndoUiTest`) quedan incluidos en esta medición.

## 5. Diferencias no bloqueantes

- AWT no tiene toolbar global, atajos globales, “Compartir”, “Asistente” ni preferencias aleatorias
  persistentes; Android las añade sin alterar el cálculo del personaje.
- Android no incorpora el QR de Advisor (sí la clonación desde `#25`). El intercambio de personajes
  se hace mediante ficheros `.rlm` y el sistema de compartir de Android.
- La paginación/orden por defecto de algunos PDFs depende del flag `sortPdfSkills` guardado en el
  personaje; no es un defecto de render.
- El catálogo NG tiene 65 categorías frente a 58 efectivas del legacy; la última fila de la tabla
  de valores puede diferir por divergencia de datos, no por el exportador. Decisión #1: conservar
  el catálogo NG completo; se acepta esa diferencia de fila y no se recorta ni fusiona ninguna
  categoría al renderizar. La paridad byte a byte de esa fila queda fuera de alcance.

## 6. Trabajo futuro documentado

No afecta a la equivalencia AWT/Android ya verificada y queda registrado para implementaciones
posteriores:

- Librería `LibroDeEsherNG`: [#1](https://github.com/softwaremagico/LibroDeEsherNG/issues/1)
  (catálogo de categorías) y [#2](https://github.com/softwaremagico/LibroDeEsherNG/issues/2)
  (Javadoc/comentarios obsoletos) quedaron cerrados como trabajo documental; la librería cerró en
  `cb72356` (`origin/master`; la caché se introdujo en `edf5b34`) con `mvn test` verde (458/92/92).
- App Android: todos los issues de la auditoría están cerrados. #27 (asistente con pantalla de
  revisión, validación y resumen de equipo; `AssistantReviewUiTest`), #28 (raza, cultura, profesión
  y rangos de características configurables; `RandomPreferenceOriginUiTest`), #29 (Guardar como y
  `Deshacer` del borrado; `SaveAsUndoUiTest`), #30 (SAF `content://`,
  `SheetFileResultsUiTest`), #31 (política de backup explícita, `f6ebd85`), #32 (prueba UI-only
  `DisabledModulesUiTest` de módulos desactivados) y #33 (memorias históricas corregidas).
- El QR de Advisor queda fuera de alcance por acuerdo: no existe en AWT y no altera resultados.
