# X4Tweaker

Mod cliente para Minecraft `1.12.2` (Forge `14.23.5.2864`) enfocado en tweaks de utilidad, visuales, combate legítimo y automatización AFK.

## Estado del proyecto

- Versión actual: r1.0.3b1
- Estado general: funcional y usable
- Licencia: **MIT**

## Características principales

- Sistema modular con toggles, keybinds y settings reactivos.
- Persistencia por módulo en JSON con migración automática desde config legacy.
- ClickGUI con categorías, sliders, keybind overlay y traducción por `.lang`.
- Pipeline de eventos internos (`Update`, `Render2D`, `Render3D`, `Key`, `Packet`).
- Inyección de `PacketHandler` en Netty al conectar.
- Sistema de incompatibilidades entre módulos conflictivos.
- Framework de bots con cola de prioridad y tareas interrumpibles.

## Arquitectura resumida

Flujo de arranque:

1. `X4Tweaker` delega en `ClientProxy`.
2. `ClientProxy.preInit()` llama `X4TweakerClient.start()`.
3. Se inicializan managers: `EventManager`, `ThemeManager`, `ModuleManager`, `ConfigManager`, `KeybindManager`, `AutomationManager`.
4. `ModuleManager` registra módulos.
5. `ConfigManager` carga configs desde `x4tweaker/modules/*.json`.
6. En tick cliente, `EventManager` inyecta `PacketHandler` cuando hay conexión activa.

Componentes núcleo:

- `core/`: cliente principal e interceptor de paquetes.
- `manager/`: eventos, módulos, keybinds, config, tema.
- `module/`: módulos agrupados por categoría.
- `combat/`: selector de targets, rotación y orquestación de ataque legítimo.
- `automation/`: scheduler de tareas y control de movimiento/input para bots.

## Módulos implementados (14)

### VISUALS (4)

- `Fullbright`
- `ChestESP`
- `PlayerESP`
- `MobESP`

### COMBAT (1)

- `KillAuraLegit`

### TWEAKS (2)

- `AutoSprint`
- `FastCrafting`

### UTILITY (6)

- `ActiveTweaks` (HUD de módulos activos)
- `ClickGUI` (keybind por defecto: `RSHIFT`)
- `Freecam`
- `CameraDetach`
- `MobInfo`

### BOTS (1)

- `BetterAFK`

## Módulos incompatibles

- `KillAuraLegit` <-> `BetterAFK`
- `BetterAFK` <-> cualquier otro `BotModule`

## Configuración y archivos generados

- `x4tweaker/theme.json`
- `x4tweaker/modules/<ModuleName>.json`

Notas:

- Config version actual: `1.0`.

## Build y desarrollo

Requisitos:

- JDK `8`
- ForgeGradle 1.12.2
Comandos:

```bash
./gradlew build
```

Artifact:

- JAR reobfuscado por `reobfJar` al finalizar `jar`.

## Roadmap corto (v1.1)

- Nuevos bots (`WalkBot`, `FarmBot`)
- Input lock real integrado en Tweaks correspondientes
- Mejoras de ClickGUI (búsqueda, orden, indicador de incompatibilidades)

## Documentación extendida

- [Arquitectura](./docs/arquitectura.md)
- [Estado actual](./docs/estado-actual.md)
- [Módulos](./docs/modulos.md)
- [Roadmap](./docs/roadmap.md)

## Revisión de documentación pendiente detectada

Se encontraron puntos donde la documentación interna aún no refleja el código actual:

- `MobESP` está implementado y debe figurar en todos los listados.
- `ClickGUI` y `ActiveTweaks` están en categoría `UTILITY`.
- `FastCrafting` usa categoría `TWEAKS`.
- La persistencia actual es modular (`modules/*.json`), no archivo único activo.
