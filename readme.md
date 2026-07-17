# KStaff

A cross-server Minecraft staff management plugin built for Spigot 1.8+ with Redis-backed inter-server communication.

## Implemented Features

### Staff Mode
- Toggle staff mode with `/staffmode` — saves and restores the player's inventory on enter/exit
- Prevents block breaking, block placing, and item dropping while in staff mode
- Configurable title and sound feedback on toggle
- Blocks configurable commands while in staff mode

### Staff Chat
- Cross-server staff chat via `/staffchat [message]` — toggle or send an inline message
- Messages are published to Redis Streams and consumed by all connected servers
- Staff chat state (on/off) is persisted per player profile

### Global Alerts
- Broadcast server-wide alerts with `/alert <message>`
- Alerts propagate cross-server via Redis Streams
- Configurable format, sound, and optional centering of the message
- Skips processing on the originating server to avoid duplicate delivery

### Staff Scoreboard
- Live scoreboard displayed while a player is in staff mode
- Pluggable provider system — automatically detects and uses the TAB plugin if present
- Shows server name, player name, and online player count
- Ticks on a scheduler and resets cleanly when leaving staff mode

### Custom Item System
- Abstract item framework (`AbstractItem`) for defining right-click items
- Items are identified by string IDs and loaded into a managed `ItemManager`
- **Random Teleport** (`RANDOM_TELEPORT`) — teleports the staff member to a random online player

### Player Profiles
- SQLite-backed persistence storing staff mode state, staff chat state, and saved inventories
- Caffeine in-memory cache for fast lookup; dirty-flag-based saves run on a scheduler
- Profile is loaded on join and unloaded on quit

## Architecture

The project is split into three Gradle modules:

| Module | Description |
|---|---|
| `API` | Core interfaces and abstractions — service registry, module lifecycle, Redis and profile contracts, Protobuf definitions |
| `Utilities` | Shared utilities — config system, item builder, NBT bridge, chat/color helpers, Protobuf-generated types |
| `Bukkit` | Main plugin — feature implementations, commands, listeners, Redis wiring |

### Inter-server Communication
All cross-server messaging (staff chat, global alerts) uses **Redis Streams** with **Protobuf**-serialized event payloads. Events are wrapped in a generic `Event` envelope that carries an `EventType` and raw bytes, allowing new event types to be added without changing the consumer infrastructure.

### Service Registry (`KStaffAPI`)
A lightweight service locator that supports:
- Typed registration (`register(Class, T)`) with `ILoadable`/`IUnloadable` lifecycle callbacks
- Dependency-aware module batches (`registerModules`) loaded in topological order
- Rollback on load failure

## Commands

| Command | Description |
|---|---|
| `/staffmode` | Toggle staff mode on/off |
| `/staffchat [message]` | Toggle staff chat or send a direct message |
| `/alert <message>` | Broadcast a global alert cross-server |

## Dependencies

| Dependency | Version | Notes |
|---|---|---|
| Spigot API | 1.8 | Compile-only |
| Jedis | 6.0.0 | Redis client |
| Protobuf Java | 4.28.2 | Cross-server message serialization |
| SQLite JDBC | 3.47.0.0 | Profile persistence |
| Caffeine | 2.9.3 | In-memory profile cache |
| XSeries | 13.2.0 | Cross-version material/sound support |
| Item NBT API | 2.15.2 | NBT tag handling for custom items |
| Lombok | 1.18.36 | Boilerplate reduction |
| TAB API | 5.0.4 | Optional — scoreboard integration |

## Building

```bash
./gradlew shadowJar
```

Output: `Bukkit/build/libs/KStaff-1.0.jar`

## Configuration

| File | Purpose |
|---|---|
| `config.yml` | Redis connection, server name, alert formatting |
| `lang.yml` | All player-facing messages, prefixes, and titles |
| `items.yml` | Custom item names, lore, materials, and slot assignments |

## License

This project is licensed under the MIT License.
