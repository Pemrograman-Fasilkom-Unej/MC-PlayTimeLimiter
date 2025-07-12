# ⏳ PlayTimeLimiter

A flexible Minecraft plugin that limits daily playtime, tracks total usage, rewards active players, and penalizes deaths — fully configurable and optimized for **Paper 1.21+**.

---

## 🔧 Features

- ⏱️ **Daily Playtime Limits** — configurable by permissions
- 📊 **Total Playtime Tracking** — across all sessions
- 🎁 **Reward System** — for daily or total playtime (commands, messages, broadcasts)
- ☠️ **Death Penalties** — remove time on death; optionally reward killers
- 🖥️ **Customizable HUD Display** — per-player (actionbar, tablist, scoreboard)
- 🏆 **Top Playtime Leaderboard** — via `/playtime top`
- 🔄 **Daily Reset** — clears daily tracked data every new login day
- ⚙️ **YAML-Based Configs** — intuitive config, reward, and message management
- 🔌 Built for **Minecraft 1.21.7+** with **Paper** (or forks)

---

## 📦 Installation

1. Download the plugin `.jar` and place it in your server’s `plugins/` folder.
2. Start the server to generate configuration files.
3. Edit `config.yml` and `rewards.yml` to match your server needs.
4. Reload with `/playtime reload` or restart the server.

---

## 📜 Commands

| Command                     | Description                                  | Permission               |
|----------------------------|----------------------------------------------|--------------------------|
| `/playtime`                | View your current and total playtime         | `playtimelimiter.use`    |
| `/playtime hud`            | Show your current HUD type                   | `playtimelimiter.use`    |
| `/playtime hud <type>`     | Set HUD display (actionbar/tablist/scoreboard) | `playtimelimiter.use`    |
| `/playtime top`            | Show the top 10 players by total playtime    | `playtimelimiter.use`    |
| `/playtime reload`         | Reload all config files                      | `playtimelimiter.admin`  |
| `/playtime set <player> <minutes>` | Set a player’s daily playtime manually | `playtimelimiter.admin`  |
| `/playtime reset`          | Reset all daily playtime                     | `playtimelimiter.admin`  |

---

## 🔐 Permissions

| Node                          | Description                                   |
|------------------------------|-----------------------------------------------|
| `playtimelimiter.use`        | Allows player to use base playtime commands   |
| `playtimelimiter.admin`      | Grants access to administrative commands      |
| `playtime.limit.-1`          | Unlimited daily playtime                      |
| `playtime.limit.<minutes>`  | Sets max daily limit for player/groups        |

---

## ⚙️ Configuration Overview

- `config.yml`  
  Configure:
  - Time limits (via permissions)
  - Warnings and messages
  - HUD defaults and update intervals
  - Reward and penalty rules

- `rewards.yml`  
  Define time-based rewards for **daily** and **total** playtime:
  - Run server commands
  - Send messages
  - Optionally broadcast to others

- `plugins/PlayTimeLimiter/data/*.yml`  
  Individual player data (auto-saved):
  - Daily usage and claims
  - Total playtime
  - HUD display preference

---

## 💡 Reward Example (`rewards.yml`)

```yaml
daily:
  60:
    command: "give %player% golden_apple 1"
    message: "You've earned a Golden Apple for 1 hour of play!"
    broadcast: false

total:
  600:
    command: "lp user %player% permission set playtime.rewarded"
    message: "Congrats! You've reached 10 hours total playtime!"
    broadcast: true
