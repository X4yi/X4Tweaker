# Roadmap

## r1.0 (actual) — Completado

1. Refactor del sistema de combate (`LegitAttackOrchestrator` alineado con path vanilla).
2. Fix de daño de arma (cooldown `getCooledAttackStrength(0.5F)` sincronizado con servidor).
3. Framework de bots escalable con `BotModule` interface.
4. `BotMovementController` modular: `KeybindDriver` + `EnvironmentScanner` + `StrafeDecider`.
5. `AFKBotTask` con 5 fases (IDLE/EATING/CHASING/ATTACKING/RETURNING) y multi-target via stack.
6. Settings renombrados para claridad (Attack Range, Rotation Speed, FOV Filter, etc.).
7. 13 settings configurables en BetterAFK (sprint, jump, strafe, chase distance, mob avoidance).
8. Localización completa en `en_us.lang` y `es_es.lang`.
9. Fix de slider drag tracking en ClickGUI.
10. Sistema de incompatibilidades entre módulos.
11. PacketEvent pipeline activo vía Netty.
12. Documentación actualizada.

## Objetivos beta v1.3 (corto plazo)

1. Implementar bots adicionales:
   - `WalkBot`: pathfinding a coordenadas.
   - `FarmBot`: cosecha automática.
2. `InputController` funcional: bloquear input real del jugador durante ejecución de bots.
3. Editor de tema (colores) con persistencia.
4. Mejoras de ClickGUI:
   - Búsqueda de módulos.
   - Orden/sort por nombre o estado.
   - Indicador visual de incompatibilidades.
   - Tooltips para settings individuales (con keys `.desc` del `.lang`).

## Objetivos técnicos (mediano/largo plazo)

1. Diseñar API interna limpia para módulos con dependencias y conflictos.
2. Añadir tests de regresión para lógica crítica:
   - Persistencia de settings.
   - Render/state cleanup (GL states).
   - Filtros y targeting de `KillAuraLegit`.
   - Máquina de estados de `AFKBotTask`.
   - `EnvironmentScanner` y `StrafeDecider`.
3. Sistema de perfiles/configs múltiples.
4. Hook avanzado de paquetes (filtrado selectivo, modificación).
5. Expandir `AttackOrchestrator` con variantes (packet-only, delayed, etc.).
