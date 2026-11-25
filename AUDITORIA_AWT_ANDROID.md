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
- El resto de diferencias son de plataforma/UX o funcionalidad **adicional** de Advisor; no implican
  pérdida de resultados AWT.

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
| Guardar | `action_save_character` | Sin “Guardar como”; `Remove` solo quita de sesión, `Delete` sí borra de BD y su Undo no restaura Room |
| Compartir | botones de compartir TXT/PDF | No existe en AWT; es una conveniencia móvil |
| Asistente | `CharacterAssistantFragment` | Función adicional de Android |
| Preferencias aleatorias | `action_random_preferences` | Función adicional de Android, persistida |
| Idioma | selector de idioma en ajustes | Función adicional de Android |
| Clonación | `action_clone_character` (`#25`) | Copia sin guardar en la sesión, como en Advisor |
| QR | no implementado en Android | Función de Advisor no presente en AWT; equivaldría a importar/exportar comprimido por imagen y no afecta a resultados AWT |
| Indicador de cambios sin guardar | marcador `*` en el título y confirmaciones (`#24`) | Equivalente a Advisor; `Guardar` y `Cargar` limpian el marcador |

## 4. Evidencias

- Baterías UI-only previas: `#22` → `OK (20 tests)` (`/tmp/opencode/final-all-ui-22.log`);
  `#23` → `OK (20 tests)` (`/tmp/opencode/issue23-all-ui-2.log`), ambas con origen publicado.
- `#24`/`#25`/`#26`: `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest` y
  `lintDebug` completados correctamente; la batería instrumentada completa con cobertura sigue
  pendiente de verificación. El último intento completo anterior registró fallos y no constituye
  evidencia de una batería verde.
- Tests JVM añadidos: serialización JSON, entidad Room, manager de sesión (dirty tracking,
  clonación, cambio/cierre), validador, resumen y fechas. Tests instrumentados añadidos:
  `UnsavedChangesUiTest`, `AdditionalScreensUiTest` (asistente, wiki, aleatorio),
  `DevelopmentScreensUiTest` (habilidades, historial, equipo, conjuros, opciones, etc.),
  `SheetActionsUiTest` (guardar, cargar, PDF, compartir, subir de nivel, importar/exportar) y
  `UiCoverageSweepTest` (lista de personajes, detalles, características, dotes, compartir/importar,
  idioma y wiki).
- Cobertura: JaCoCo combinatorial (`testDebugUnitTest` + `createDebugCoverageReport`), informe en
  `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.csv`. Las clases generadas por
  Android/Room se excluyen del denominador. La última medición disponible fue 2668/4589 líneas
  (58,1 %); falta verificar una medición actualizada y alcanzar el objetivo del 80 %.
- Nihal: el test UI con ocho subidas automáticas y comprobación del PDF pasó en 616,6 segundos
  (`/tmp/opencode/nihal-optimized2.log`). La ejecución histórica de 887,5 segundos no es una
  comparación controlada. Las subidas requieren aproximadamente 56–67 segundos cada una.
- Estado pendiente de cierre: Android `HEAD == origin/main` en `8c8a4d5` antes de actualizar este
  informe; existe el directorio no rastreado `images/`, que se ha preservado. NG permanece sin
  cambios en `a29d114` (`origin/master`). Las incidencias `#24`–`#26` siguen abiertas hasta
  completar la verificación.
- PDF: memories `.serena/memories/librodeesher-ng-pdf-parity.md` y
  `.serena/memories/android/nihal-e2e-verification.md`. Los artefactos crudos de `.parity/out/`
  están desactualizados y no deben usarse como evidencia.
- Personajes de referencia cubiertos en NG: Nihal, Morticia, Ophiliaglii, Kara, Fthagtli, Gorju y
  Skaag.

## 5. Diferencias no bloqueantes

- AWT no tiene toolbar global, atajos globales, “Compartir”, “Asistente” ni preferencias aleatorias
  persistentes; Android las añade sin alterar el cálculo del personaje.
- Android no incorpora el QR de Advisor (sí la clonación desde `#25`). El intercambio de personajes
  se hace mediante ficheros `.rlm` y el sistema de compartir de Android.
- La paginación/orden por defecto de algunos PDFs depende del flag `sortPdfSkills` guardado en el
  personaje; no es un defecto de render.
- El catálogo NG tiene 65 categorías frente a 58 del legacy; la última fila de la tabla de valores
  puede diferir por recorte de catálogo, no por el exportador.
