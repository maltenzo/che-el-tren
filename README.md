# Che, el tren 🚆

> 🤖 **Desarrollado con IA.** Este proyecto —código, iconos, GTFS procesado y
> documentación— fue construido en colaboración con un asistente de IA
> (Claude Code).

App para **Wear OS** (reloj) que muestra los **próximos trenes en vivo** de cualquier
estación de la red de Trenes Argentinos (SOFSE), con auto-refresh y favoritos.

> App **no oficial**, sin relación con SOFSE / Trenes Argentinos. Usa datos
> públicos de su API de arribos.

## Features

- Arribos en tiempo real por estación (minutos al próximo tren + siguientes).
- Pager horizontal: próximo / siguiente / 3º.
- Elegís **ramal → estación** desde el reloj.
- **Favoritos** (ramales y estaciones): mantené apretado para marcar/desmarcar;
  quedan arriba y se guardan en el reloj.
- Auto-refresh cada 60s; tap en el fondo para refrescar al toque.
- Color por sentido/destino.

## Build

```bash
# APK de debug (sideload por ADB)
./gradlew assembleDebug

# Release firmada: creá keystore.properties (ver abajo)
./gradlew assembleRelease     # APK
./gradlew bundleRelease       # AAB para Google Play
```

### Firma

Creá `keystore.properties` en la raíz (NO se commitea):

```properties
storeFile=keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Generá el keystore con `keytool -genkeypair -keystore keystore.jks -alias <alias> ...`.

## Instalar en el reloj (Wear OS, por WiFi)

```bash
adb pair   192.168.x.x:PUERTO_EMPAREJAMIENTO   # primera vez (código del reloj)
adb connect 192.168.x.x:PUERTO
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Datos

- Arribos: `https://api-servicios.sofse.gob.ar/v1/arribos/estacion/{id}` (público).
- Catálogo de estaciones y ramales: precalculado del **GTFS estático** de Trenes
  Argentinos → `app/src/main/assets/lines.json`.

## Stack

Kotlin · Jetpack Compose (Wear) · OkHttp · minSdk 30.

## Licencia

MIT — ver [LICENSE](LICENSE). El nombre, logo y datos de Trenes Argentinos / SOFSE
pertenecen a sus titulares; este proyecto no los incluye ni los representa.
