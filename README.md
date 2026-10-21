# SetSignal

SetSignal es un cronómetro de entrenamiento independiente para Wear OS con un solo trabajo:
terminar la serie, iniciar el descanso y volver a la siguiente serie sin tener que sacar el
teléfono. Es un MVP local-first con tres rutinas de ejemplo, un temporizador de descanso real basado
en marcas de tiempo, resúmenes de sesión e historial persistente.

La aplicación es intencionalmente pequeña. No hay aplicación acompañante en el teléfono, cuentas,
sincronización en la nube, acceso a internet, integración con plataformas de salud, entrada de
sensores, IA ni métricas de recuperación/calorías inventadas. Todos los datos permanecen en el
reloj.

> Las rutinas incluidas son ejemplos de configuración, no consejo médico ni un plan de
> entrenamiento personalizado. Ajusta el esfuerzo a tu nivel y consulta a un profesional si lo
> necesitas.

## Qué hace

- **Tres rutinas integradas** – fuerza de cuerpo completo, entrenamiento por intervalos y
  calistenia suave.
- **Flujo de series en vivo** – marca una serie como hecha, avanza automáticamente a la
  siguiente serie/ejercicio y termina la sesión en la última serie.
- **Cuenta atrás de descanso basada en marcas de tiempo** – el tiempo restante se deriva de una
  fecha límite del reloj monótono, no de un estado de UI que se decrementa. Pausar congela tanto el
  tiempo de entrenamiento como el de descanso.
- **Controles de descanso** – añade el tiempo extra configurado, salta el descanso o continúa
  cuando la cuenta atrás llega a cero. Se produce una vibración cuando termina el descanso mientras
  el proceso de la app está en ejecución.
- **Pausa/reanudación y cancelación segura** – la pausa está disponible durante el ejercicio y el
  descanso; cancelar pide confirmación y guarda el trabajo realizado hasta el momento como sesión
  cancelada.
- **Resumen de sesión** – distingue sesiones completadas y canceladas y muestra tiempo activo,
  series/ejercicios completados, descanso usado y resultados por ejercicio.
- **Historial local** – las sesiones completadas y canceladas sobreviven a los reinicios de la app
  y se pueden borrar con una segunda confirmación.
- **Preferencias persistentes** – el descanso por defecto, la duración del descanso extra y los
  hápticos se almacenan en DataStore.
- **UI pensada para el reloj** – paleta oscura AMOLED, texto blanco cálido/gris, acento lima,
  números de cronómetro grandes, anillo de progreso con marcas de tick personalizado, transiciones
  cortas y diseños aptos para pantallas redondas.

## Pantallas y flujo

| Pantalla | Propósito |
| --- | --- |
| Inicio | Última sesión, recuento de sesiones completadas, accesos a historial/ajustes |
| Rutinas | Explorar las tres rutinas locales |
| Detalle de rutina | Revisar ejercicios, ajustar el descanso en pasos de 15 segundos, empezar el entrenamiento |
| Overlay de entrenamiento | Estados de ejercicio, descanso, descanso completado, pausado, guardando y error de guardado |
| Resumen | Resultados de una sesión completada o cancelada |
| Historial | Lista de sesiones agrupadas por día, con totales completadas/canceladas |
| Ajustes | Descanso por defecto, descanso extra, hápticos, borrar historial, información de la app |

Un entrenamiento es un overlay de pantalla completa a nivel de actividad, no un destino de
navegación. El gesto de deslizar hacia atrás no puede descartar ni perder una sesión activa; el
gesto de retroceso del sistema abre la confirmación de cancelación. Una vez guardada la sesión, el
overlay se cierra y se abre el resumen.

## Arquitectura

```
app/src/main/java/com/example/setsignal/
├── data/
│   ├── datastore/        Repositorio de preferencias con DataStore
│   ├── local/            Base de datos Room, DAO, entidades, relación
│   └── repository/       Repositorio de sesiones respaldado por Room
├── domain/
│   ├── data/             Catálogo de rutinas (tres ejemplos locales)
│   ├── model/            Modelos de ejercicio, rutina, sesión y ajustes
│   ├── repository/       Contratos de los repositorios
│   └── workout/          Máquina de estados pura del entrenamiento y mapeo de resultados
├── feature/              ViewModels de inicio, rutinas, entrenamiento, historial, resumen y ajustes
├── platform/             Fuente de tiempo monótono y adaptador de vibración
├── ui/                   Tema, componentes reutilizables, formato, pantallas
├── AppContainer.kt       Contenedor manual de dependencias
├── SetSignalApplication.kt
└── MainActivity.kt       Host de navegación y overlay de entrenamiento
```

Decisiones clave:

- **Motor puro primero.** `WorkoutEngine` gestiona las transiciones de serie/descanso/pausa y
  acepta valores de `elapsedRealtime()` proporcionados por el llamador. No tiene dependencias de
  Android ni de Compose, lo que mantiene la lógica de tiempos determinista y testeable con unit
  tests.
- **Solo se persisten sesiones terminadas.** Una sesión se escribe una única vez, después de
  completarla o cancelarla. El estado en curso nunca entra en la base de datos, así que el
  historial no puede contener filas parciales o duplicadas.
- **Estado unidireccional.** Los ViewModels exponen `StateFlow`; las pantallas de Compose los
  recolectan con conciencia del ciclo de vida. Los ViewModels de funcionalidad usan fábricas
  manuales pequeñas y `AppContainer` en lugar de un framework de inyección de dependencias.
- **Los datos permanecen locales.** Room almacena sesiones/ejercicios; DataStore almacena
  preferencias. El manifiesto solo solicita `VIBRATE` y no declara permiso de red.
- **Navegación.** Wear Compose Navigation (`SwipeDismissableNavHost`) gestiona la biblioteca, el
  historial, los ajustes y los resúmenes. El entrenamiento en vivo se sitúa deliberadamente fuera
  de ese grafo.

### Semántica de tiempos

- El tiempo transcurrido de entrenamiento usa `SystemClock.elapsedRealtime()` e excluye los
  intervalos en pausa; el tiempo de descanso cuenta como tiempo de entrenamiento.
- El descanso restante es `restEndsAtElapsedMillis - now`, de modo que la recomposición, los
  cambios de ciclo de vida y los periodos con pantalla apagada no acumulan desfase.
- Completar una serie inicia un descanso o avanza inmediatamente si el descanso está a cero.
- Completar la última serie termina sin un descanso final.
- En `REST_COMPLETE`, el motor devuelve exactamente un evento de transición para que el ViewModel
  pueda disparar una única háptica; después el usuario toca **SEGUIR** para continuar.

## Stack tecnológico

Las versiones se declaran en `gradle/libs.versions.toml`:

- Kotlin 2.2.10, AGP 9.4.1, Gradle 9.6.0
- Jetpack Compose para Wear OS 1.5.6 (Material 3 Wear, Foundation, Navigation)
- Compose BOM 2025.06.01, Activity Compose 1.10.1
- Lifecycle 2.9.4, Navigation Compose (Wear) 1.5.6
- Room 2.8.5 con KSP 2.2.10-2.0.2
- DataStore Preferences 1.2.1
- JUnit 4.13.2, Robolectric 4.16.1, coroutines-test 1.10.2

Compile/target SDK 37, min SDK 30, app de reloj independiente, application ID `com.example.setsignal`.

## Compilar y ejecutar

Requisitos:

- Android Studio con Android SDK 37
- JDK compatible con la cadena de herramientas Gradle/AGP del proyecto
- Un emulador de Wear OS o un reloj con API 30+

Desde la raíz del repositorio (Linux/macOS):

```bash
./gradlew :app:assembleDebug
```

En Windows:

```bat
gradlew.bat :app:assembleDebug
```

Instalar en un reloj/emulador conectado:

```bash
./gradlew :app:installDebug
```

El APK de debug se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Pruebas y comprobaciones

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

Resultados locales actuales:

- **28 unit tests JVM pasan** (motor de entrenamiento, repositorio Room con Robolectric,
  persistencia/límites de DataStore, helpers de formato/fechas).
- **`:app:lintDebug` termina con 0 errores** (15 avisos informativos, en su mayoría
  actualizaciones opcionales de dependencias y dos atributos de manifiesto/backup).
- **`:app:assembleDebug` finaliza correctamente** y produce `app-debug.apk`.

La suite de pruebas cubre:

- Plazos de descanso, transiciones de completado, salto, descanso a cero, pausa/reanudación,
  descanso extra, cancelación y validación de resultados.
- Round-trip de sesiones completadas/canceladas, orden de ejercicios, historial del más reciente al
  más antiguo, recuento de completadas y borrado del historial.
- Valores por defecto de preferencias, límites de validación y persistencia en un archivo de
  DataStore reabierto.
- Formato de tiempo transcurrido/reloj/fechas y etiquetas de día del historial.

### Verificación en emulador

La build de debug se instaló y se probó en un emulador Android Wear OS (AVD `SetSignalWear`,
Wear OS 7, pantalla redonda de 454×454). Lo siguiente se confirmó inspeccionando la app en
ejecución:

- Arranque en frío hasta Inicio: el logotipo, el eslogan y la acción principal **ELEGIR RUTINA**
  se renderizan en la primera pantalla sin scroll, recorte ni desbordamiento.
- Lista de rutinas: las tres tarjetas de rutina se renderizan con títulos y metadatos completos.
- Detalle de rutina: descripción, métricas y la acción en línea **EMPEZAR** se renderizan en la
  primera pantalla.
- Entrenamiento en vivo: completar una serie avanza el contador de series y el anillo de progreso;
  la pantalla de descanso cuenta atrás desde la duración elegida, y pausar/reanudar congela y
  reanuda la cuenta atrás.
- **+15 s** añade el descanso extra configurado y **SALTAR** salta el descanso.
- Terminar la última serie guarda la sesión y abre el resumen, que informa de la rutina, la fecha,
  el tiempo activo y el total de series completadas.
- Cancelar pide confirmación; confirmar guarda la sesión como cancelada, y la sesión cancelada
  aparece en el historial local con su propio estado.
- El historial conserva la sesión a través de un reinicio de la app.

No verificado en el emulador: la vibración, porque el emulador no tiene hápticos significativos. La
acción en línea **VOLVER** de la pantalla de resumen y su panel de métricas se confirmaron se
renderizan sin solaparse en la pantalla redonda.

## Limitaciones conocidas

- **No está implementada la recuperación de sesión activa.** Si Android mata el proceso a mitad de
  un entrenamiento, se pierde el estado de serie/descanso en curso. Solo las sesiones terminadas y
  canceladas son duraderas.
- **No hay servicio de alarma en segundo plano.** Las hápticas de fin de descanso se disparan
  mientras el proceso de la app y su coroutine de ticks siguen vivos. El comportamiento con la
  pantalla apagada depende de que el sistema operativo del dispositivo mantenga el proceso en
  ejecución; no se garantizan hápticos tras la muerte del proceso ni con restricciones agresivas en
  segundo plano.
- **No hay notificaciones de descanso en segundo plano.** La cuenta atrás es visible en la app; no
  hay notificación del sistema ni complicación permanente.
- **Las rutinas son ejemplos fijos.** No hay editor de rutinas, programación, progresión ni
  biblioteca de ejercicios en este MVP.
- **La UI está solo en español.** Las cadenas de texto están incrustadas actualmente en los
  composables; todavía no hay un conjunto de recursos localizado.
- **Defecto visual conocido en la tarjeta «Última sesión» del Inicio.** Cuando `SessionCard` se
  usa en modo detallado, el recuento de series completadas y el anillo de progreso no caben en la
  fila: el recuento salta un carácter por línea y el nombre de la rutina se trunca. Las filas de
  rutinas, el overlay de entrenamiento y la pantalla de resumen no se ven afectados. Necesita una
  corrección de maquetación en `ui/components/SessionCards.kt` (dar su propia línea a la métrica o
  quitar el anillo en modo detallado).
- La app intencionalmente no lee la frecuencia cardíaca, los pasos ni otros datos de sensores, y
  no estima calorías ni recuperación.

## Notas de configuración del proyecto

- KSP y AGP 9 requieren `android.disallowKotlinSourceSets=false` en `gradle.properties` hasta que
  KSP adopte el DSL de source sets integrado de AGP.
- Los esquemas de Room se exportan a `app/schemas/` mediante el argumento `room.schemaLocation` de
  KSP.
- Las copias de seguridad están desactivadas (`android:allowBackup="false"`); el historial es
  local a la app instalada en el reloj.
