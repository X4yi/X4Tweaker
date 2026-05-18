# Roadmap

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
