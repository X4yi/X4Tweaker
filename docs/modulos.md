# Módulos del Proyecto

## VISUALS

### Fullbright

- Clase: `module.visuals.Fullbright`
- Estado: Implementado
- Función: fuerza gamma alta al activar y restaura al desactivar.
- Settings: ninguno.

### PlayerESP

- Clase: `module.visuals.PlayerESP`
- Estado: Implementado
- Función: render de cajas/líneas para jugadores.
- Base común: hereda de `ESPBase`.
- Settings:
  - `Style` (Boxes, Lines, Lines&Boxes)
  - `X-Ray Entity` (boolean)

### ChestESP

- Clase: `module.visuals.ChestESP`
- Estado: Implementado
- Función: ESP para cofres (`TileEntityChest`) y chest minecarts.
- Base común: hereda de `ESPBase`.
- Settings:
  - `Style` (Boxes, Lines, Lines&Boxes)
  - `X-Ray Entity` (boolean)

### ActiveTweaks

- Clase: `module.visuals.ActiveTweaks`
- Estado: Implementado
- Función: HUD que muestra módulos activos en pantalla.
- Settings:
  - `HUD Position` (mode)
  - `Show Title` (boolean)

## COMBAT

### KillAuraLegit

- Clase: `module.combat.KillAuraLegit`
- Estado: Implementado
- Función: selección de target + rotación suave + ataque vanilla legítimo via `CombatController`.
- Settings reactivos: cambios se aplican inmediatamente via dirty flag + `onSettingChanged()`.
- Incompatible con: `BetterAFK`.
- Settings:
  - `Attack Range` (1.0-6.0, default 3.5)
  - `Rotation Speed` (1.0-10.0, default 2.0)
  - `Target Priority` (Distance, Health, Angle)
  - `Aim Precision` (1.0-45.0°, default 15.0)
  - `FOV Filter` (1.0-360.0°, default 180.0, visible solo en prioridad Angle)
  - `Skip Players` (boolean, default false)
  - `Skip Monsters` (boolean, default false)
  - `Skip Animals` (boolean, default false)
  - `Skip Slimes` (boolean, default false)
  - `Skip Pets` (boolean, default true)
  - `Skip Flying` (boolean, default false)
  - `Skip Sleeping` (boolean, default true)
  - `Skip Enderman` (boolean, default false)
  - `Ignore List` (lista de nombres de entidades)

## TWEAKS

### AutoSprint

- Clase: `module.tweaks.AutoSprint`
- Estado: Implementado
- Función: sprint automático al avanzar. Condiciones: moveForward > 0, no sneaking, no colisión horizontal, hambre > 6.
- Settings: ninguno.

## RANDOM_TWEAKS

### FastCrafting

- Clase: `module.random_tweaks.FastCrafting`
- Estado: Rehecho (instantáneo)
- Función: rellena y craftea de forma instantánea usando la última receta detectada.
- Interacción: tecla configurable (default `SPACE`), `SHIFT + tecla` para crafteo masivo hasta agotar materiales.
- Setting:
  - `Trigger Key` (keycode, default `SPACE`)

## BOTS

### BetterAFK

- Clase: `module.bots.BetterAFK`
- Estado: Implementado
- Implementa: `BotModule`
- Función: Bot AFK con máquina de estados (IDLE → CHASING → ATTACKING → RETURNING → RECOVERING).
- Detección de amenaza: transición de `hurtTime` (0 → >0) + entidad más cercana en 8 bloques.
- Multi-target: stack de targets (máx 3), retargetea al más cercano al recibir golpe, vuelve al anterior cuando muere.
- Movimiento: `BotMovementController` con simulación de keybinds reales (W/A/S/D/Space/Sprint).
- Combate: `LegitAttackOrchestrator` con cooldown vanilla + `RotationCalculator` para giros suaves.
- Incompatible con: `KillAuraLegit`, otros `BotModule`.
- Settings:
  - `Check Interval` (5-100 ticks, default 20)
  - `Auto Eat` (boolean, default true)
  - `Hunger Threshold` (1-18, default 12, visible si Auto Eat)
  - `Eat Duration` (10-100 ticks, default 40, visible si Auto Eat)
  - `Auto Defend` (boolean, default false)
  - `Attack Range` (1.0-6.0, default 3.5, visible si Auto Defend)
  - `Rotation Speed` (1.0-10.0, default 3.0, visible si Auto Defend)
  - `Sprint While Chasing` (boolean, default true, visible si Auto Defend)
  - `Jump While Attacking` (boolean, default true, visible si Auto Defend)
  - `Strafe In Combat` (boolean, default true, visible si Auto Defend)
  - `Strafe Interval` (5-60 ticks, default 15, visible si Auto Defend + Strafe)
  - `Max Chase Distance` (5-50 bloques, default 20, visible si Auto Defend)
  - `Avoid Mobs In Path` (boolean, default true, visible si Auto Defend)

## CLIENT

### ClickGUI

- Clase: `module.client.ClickGUIModule`
- Estado: Implementado
- Función: abre `ClickGUI`.
- Keybind default: `RSHIFT`.
- También accesible via botón `[X4]` en inventario.

## UTILITY

### Freecam

- Clase: `module.utility.Freecam`
- Estado: Implementado
- Función: cámara libre no clip con render opcional del jugador.

### CameraDetach

- Clase: `module.utility.CameraDetach`
- Estado: Implementado
- Función: desacopla cámara con modos de bloqueo y movimiento cinemático.

### MobInfo

- Clase: `module.utility.MobInfo`
- Estado: Implementado
- Función: etiquetas world-space tipo billboard con nombre, mod, vida y armor.

### ContainerPreview

- Clase: `module.utility.ContainerPreview`
- Estado: Implementado
- Función: preview visual automática al apuntar contenedores, con pipeline por ticks y cache de snapshot.
- Settings:
  - `Position` (LEFT_CROSSHAIR, RIGHT_CROSSHAIR, BOTTOM_RIGHT, CUSTOM)
  - `Scale`
  - `Opacity`
  - `Fade Animation`
  - `Blur Background`
  - `Show Delay` (ticks)
  - `Refresh Rate` (ticks)
  - `Custom X` / `Custom Y` (si Position = CUSTOM)
