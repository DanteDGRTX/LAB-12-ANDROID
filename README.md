# LAB 12 Android — Google Maps con Jetpack Compose

Aplicación Android básica que muestra Google Maps centrado en Arequipa, Perú, con un marcador.

## Requisitos

- Android Studio (versión reciente)
- JDK 17
- Android SDK 35
- Una API key habilitada para Maps SDK for Android y con facturación configurada en Google Cloud

## Configurar la API key local

1. Copia `secrets.properties.example` como `secrets.properties` en la raíz del proyecto.
2. Reemplaza `YOUR_API_KEY` por una clave nueva. No subas `secrets.properties` a Git.
3. Abre el proyecto en Android Studio y espera la sincronización de Gradle.
4. Ejecuta la configuración `app` en un emulador o dispositivo.

La clave se lee con Secrets Gradle Plugin y se inyecta como `MAPS_API_KEY` en el manifiesto. Restringe la clave en Google Cloud al nombre del paquete de esta app y a Maps SDK for Android.
