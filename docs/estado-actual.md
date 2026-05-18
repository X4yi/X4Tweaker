# Estado Actual (r1.0.3)

## Resumen rápido

- Estado general: `funcional` con combate y bots estables.
- Configuración persistente: `sí` (JSON).
- ClickGUI: `sí`, con scroll, scissor visual + clipping de input, keybind tab, drag tracking, traducción i18n.
- Automatización/bot: `framework funcional` con AFKBotTask y BotMovementController.
- Pipeline de paquetes: `activo` (PacketEvent.Send/Receive vía Netty).
- Localización: `en_us.lang` y `es_es.lang` completos.

## Cambios en r1.0.3

- Versión unificada a `r1.0.3` en todos los archivos.
- Eliminado `HUDOverlay` (duplicado de `ActiveTweaks`).
- Eliminado `CustomFontRenderer` (dead code con aloca de GPU sin uso).
- UUID protegido en `TargetSelector` ahora es configurable vía builder (eliminado hardcode).
- Corregido bug de coordenadas en `RenderUtils.drawRect` + agregado `pushMatrix/popMatrix`.
- `InputController` ahora bloquea input real del jugador vía `InputUpdateEvent`.
- `Freecam` y `CameraDetach` refactorizados para compartir `DetachedCameraModule` (eliminada duplicación de ~200 LOC).
- Eliminadas allocations por frame en `ActiveTweaks` (buffers reutilizables).
- `EnvironmentScanner.isSolid` ahora usa `getCollisionBoundingBox` para detectar partial blocks.
- Agregadas traducciones de settings de Freecam y CameraDetach.
- `FastCrafting` movido de `random_tweaks` a `tweaks`.
- Calls redundantes de `RaytraceUtil.updateMouseOver` reducidas a una por fase de tick.

## Implementado

### Núcleo

- Inicialización cliente completa (`X4TweakerClient` + 6 managers).
- Pipeline de update y render hacia módulos habilitados vía `ModuleEventDispatcher`.
- Gestión de keybinds global.
- Persistencia de estado de módulos/settings en JSON.
- Settings reactivos con `onSettingChanged()` notificación.
- Settings con visibilidad condicional (`Supplier<Boolean>`).
- PacketEvent pipeline vía Netty handler injection.
- Sistema de incompatibilidades: desactivación automática al activar módulo conflictivo.

### Sistema de combate

- `CombatController` reutilizable (usado por KillAuraLegit).
- `LegitAttackOrchestrator` usando path vanilla (`playerController.attackEntity`). Cooldown alineado con servidor via `getCooledAttackStrength(0.5F)`.
- `TargetSelector` con filtro de ángulo, filtros de tipo de entidad, y lista custom de exclusión.
- `RotationCalculator` con divisor mutable y clamping de pitch.

### Sistema de bots

- `BotTask` con estado explícito (IDLE/RUNNING/COMPLETED/FAILED).
- `BotModule` interface (createTask/isTaskRunning/getBotStatus).
- `AutomationManager` con cola de prioridad, interrupción, y cleanup robusto.
- `AFKBotTask` con máquina de estados de 5 fases (IDLE → CHASING → ATTACKING → RETURNING → RECOVERING).
- Detección de amenaza por transición de `hurtTime` + búsqueda de entidad más cercana.
- Multi-target via stack (máx 3 targets simultáneos).
- `BotMovementController` con simulación de keybinds reales:
  - `KeybindDriver`: simula WASD/Jump/Sprint via `KeyBinding.setKeyBindState()`.
  - `EnvironmentScanner`: raycast de bloques, detección de obstáculos y mobs en camino.
  - `StrafeDecider`: decisión de strafe por prioridades (obstáculo → salto → mobs → periódico).
  - `MovementConfig`: config inmutable via Builder.

### GUI y UX

- ClickGUI con categorías, settings, keybind overlay.
- Drag tracking para sliders (no se mueven al hover con click sostenido desde otro lugar).
- Tooltips de módulos por `.lang` con fallback.
- Botón `[X4]` inyectado en `GuiInventory`.
- Hotfix de render aplicado para `Freecam`/`CameraDetach`: render local estable en cámara desacoplada y sin transparencia accidental.

### Módulos (14 totales)

- `Fullbright`: implementado.
- `PlayerESP`: implementado.
- `ChestESP`: implementado.
- `Lines` de ESP reimplementadas: origen en crosshair de cámara activa con offset de 2 bloques, ruta estable sin jitter.
- `ActiveTweaks`: implementado (HUD Position, Show Title).
- `AutoSprint`: implementado.
- `KillAuraLegit`: implementado (14 settings, reactivos, incompatible con BetterAFK).
- `FastCrafting`: implementado (instantáneo, SPACE y SHIFT + SPACE para crafteo masivo).
- `ClickGUI`: implementado (RSHIFT).
- `BetterAFK`: implementado (13 settings, máquina de estados, movimiento inteligente, multi-target).
- `Freecam`: implementado.
- `CameraDetach`: implementado (Cinematic Drone, Absolute, FollowPlayer, bypass de paquetes servidor).
- `MobInfo`: implementado (Billboard world-space de info de entidades con fade/distancia).
- `ContainerPreview`: implementado (preview automática por apuntado, cache por ticks, layout robusto en pantalla).

### Incompatibilidades activas

- `KillAuraLegit` ↔ `BetterAFK`.
- `BetterAFK` ↔ otros `BotModule`.

### Idiomas

- `en_us.lang`: completo (módulos + settings + descripciones).
- `es_es.lang`: completo (módulos + settings + descripciones).

## Parcial / con deuda técnica

- `AttackOrchestrator`: factory class que solo provee `createLegit()`, sin otras implementaciones.
- `Freecam` / `CameraDetach`: refactorizados para compartir `DetachedCameraModule`, pero aún tienen event handlers duplicados que podrían consolidarse más.

## No implementado todavía

- Bots adicionales (WalkBot, FarmBot).
- Hook de paquetes avanzado (filtrado selectivo, modificación de paquetes).
- Suite de tests automatizados.
- Sistema de perfiles/configs múltiples.
- Búsqueda de módulos en ClickGUI.
- Indicador visual de incompatibilidades en ClickGUI.
