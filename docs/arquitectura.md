# Arquitectura del Proyecto

## Stack y entorno

- Minecraft: `1.12.2`
- Forge: `14.23.5.2864`
- Java: `8`
- Mod principal: `com.x4yi.x4tweaker.X4Tweaker`

## Flujo de inicio

1. `X4Tweaker` delega a `ClientProxy`.
2. `ClientProxy.preInit()` llama `X4TweakerClient.start()`.
3. `X4TweakerClient` inicializa:
   - `EventManager`
   - `ThemeManager`
   - `ModuleManager`
   - `ConfigManager`
   - `KeybindManager`
   - `AutomationManager`
4. `ModuleManager.init()` registra módulos.
5. `ConfigManager.init()` carga `modules.json`.
6. En primer tick con jugador conectado, `EventManager` inyecta `PacketHandler` en el canal Netty.

## Componentes principales

### Core

- `X4TweakerClient`: singleton thread-safe (double-checked locking) de orquestación general.
- `PacketHandler`: interceptor Netty (`ChannelDuplexHandler`) para emitir `PacketEvent.Send/Receive`.

### Managers

- `ModuleManager`: registro, consulta por nombre/clase/categoría, e incompatibilidades de módulos. Al activar un módulo, desactiva automáticamente los incompatibles.
- `EventManager`: puente de eventos Forge → módulos (`onUpdate`, `onRender2D`, `onRender3D`, `onEvent`). Inyecta `PacketHandler` en conexión. Añade botón `[X4]` en `GuiInventory`.
- `ModuleEventDispatcher`: despacha eventos internos a módulos habilitados.
- `KeybindManager`: toggles por tecla.
- `ConfigManager`: persistencia en `mcDataDir/x4tweaker/modules.json`.
- `ThemeManager`: colores del cliente.
- `AutomationManager`: scheduler de tareas de bot con cola de prioridad (`PriorityQueue<TaskTicket>`) y lifecycle management.

### Sistema de módulos

- Clase base: `Module` — nombre, descripción, categoría, keybind, toggle, visibility, implemented flag.
- Categorías: `VISUALS`, `COMBAT`, `UTILITY`, `TWEAKS`, `RANDOM_TWEAKS`, `CLIENT`, `BOTS`, `KEYBINDS`.
- Ajustes: `Setting<T>` con notificación a módulo vía `onSettingChanged()` y visibilidad condicional via `Supplier<Boolean>`.
- Derivados: `BooleanSetting`, `NumberSetting`, `ModeSetting`, `StringListSetting`, `ColorSetting`, `StringSetting`.
- Persistencia: `loadFromJson()` / `saveToJson()` por cada tipo de setting.
- i18n: nombres de módulos y settings resueltos por `.lang` files con fallback a strings Java.

### Sistema de combate

- `CombatController`: orquestador de alto nivel (target → rotate → attack). Usado por `KillAuraLegit`.
- `TargetSelector`: selección con filtros y prioridad (Distance/Health/Angle). Builder inmutable con opciones: maxRange, maxAngle, ignorePlayers/Monsters/Animals/Slimes/Pets/Flying/Sleeping/Enderman, customIgnoreList.
- `RotationCalculator`: interpolación suave de yaw/pitch hacia target con divisor mutable y clamping de pitch (-90/+90).
- `IAttackOrchestrator`: interfaz strategy para ejecución de ataques (`attack`, `canAttack`, `onUpdate`, `reset`, `getAimThreshold`).
- `LegitAttackOrchestrator`: implementación vanilla vía `playerController.attackEntity()`. Cooldown check con `getCooledAttackStrength(0.5F)` alineado al servidor.
- `AttackOrchestrator`: factory simplificada (`createLegit()`).

### Sistema de bots/automatización

- `BotTask`: tarea base con estado explícito (`IDLE`, `RUNNING`, `COMPLETED`, `FAILED`).
- `BotModule`: interfaz para módulos bot (`createTask()`, `isTaskRunning()`, `getBotStatus()`).
- `AutomationManager`: scheduler con cola de prioridad, interrupción por prioridad, y cleanup en shutdown.
- `AutomationContext` + `PlayerStateSnapshot`: captura y restaura estado del jugador.
- Controladores:
  - `InputController`: flag de `inputLocked`.
  - `InventoryController`: selección de comida en hotbar y restauración de slot.
  - `KeybindDriver`: simulación de teclas de movimiento vía `KeyBinding.setKeyBindState()`. Exclusividad mutua forward/back, left/right.
  - `EnvironmentScanner`: análisis de terreno con `world.getBlockState()`. Detecta path libre, necesidad de salto, obstáculos entre entidades, y mobs en el camino.
  - `StrafeDecider`: decisión de dirección de strafe por prioridades (obstáculo → salto bloqueado → mobs en camino → periódico). Alternación de dirección configurable.
  - `BotMovementController`: orquestador de movimiento que combina `KeybindDriver` + `EnvironmentScanner` + `StrafeDecider`. APIs: `moveToward()` y `maintainCombatDistance()`.
  - `MovementConfig`: configuración inmutable vía Builder (sprint, jump, strafe, attackRange, maxChaseDistance, avoidMobsInPath).
- Tareas implementadas:
  - `AFKBotTask`: máquina de estados (IDLE → CHASING → ATTACKING → RETURNING → RECOVERING) con multi-target via stack.
  - `EatFromHotbarTask`: come comida del hotbar.
  - `WaitTask`: espera configurable.

### GUI/HUD

- `ClickGUI`: interfaz principal con categorías, toggles, settings, keybind overlay, scroll con scissor. Drag tracking para sliders.
- `HUDOverlay`: overlay base.
- `ActiveTweaks`: HUD de módulos activos configurable.
- Botón `[X4]` inyectado en `GuiInventory`.

## Eventos internos

Clases evento internas:

- `UpdateEvent`: tick del cliente.
- `Render2DEvent`: render de overlay (con partialTicks).
- `Render3DEvent`: render de mundo (con partialTicks).
- `KeyEvent`: tecla presionada.
- `PacketEvent` (`.Send` y `.Receive`): paquetes Netty interceptados. Cancelables.

## Incompatibilidades de módulos

- `KillAuraLegit` ↔ `BetterAFK`: conflicto de rotación/ataque.
- `BetterAFK` ↔ cualquier otro `BotModule`: solo un bot activo a la vez.

## Localización

- `en_us.lang`: inglés completo (módulos, settings, descripciones).
- `es_es.lang`: español completo (módulos, settings, descripciones).
- Resolución: ClickGUI busca key `.lang` primero, fallback a string Java.
