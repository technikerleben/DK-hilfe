# Klexikon-App für Android

Eine bewusst eingeschränkte WebView-App für Kinder. Sie öffnet ausschließlich
`https://klexikon.zum.de/` und blockiert fremde Navigationen sowie fremde
Netzwerkressourcen, Downloads, Pop-ups, Datei- und Inhaltszugriffe.

## Build

```bash
gradle :app:assembleRelease
```

Die APK entsteht unter `app/build/outputs/apk/release/app-release.apk`.
