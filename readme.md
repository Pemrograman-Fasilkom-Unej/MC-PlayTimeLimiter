# ⏳ PlayTimeLimiter

A flexible Minecraft plugin that limits daily playtime, tracks total usage, rewards active players, and penalizes deaths — fully configurable and optimized for **Paper 1.21+**.

---

## 🔧 Features

- 🎮 **Multi Platform, Single Record** — Shared progress while has same username (sensitive case) while using multi platform support like Geyser!
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

- `plugins/PlayTimeLimiter/data/*.yml`  
  Individual player data (auto-saved):
  - Daily usage and claims
  - Total playtime
  - HUD display preference

---

## 🔥 API

```
getDailyUsed([UUID])
setDailyUsed([UUID[], [int] minutes)
getTotalUsed([UUID])
reduceDailyUsed([UUID], [int] minutes)
setDailyDeath([UUID], [int] count)
```

### How to use?

Create folder named libs then put `PlayTimeLimiter.jar` on that folder.
Update your `build.gradle` like this:
```
repositories {
    ...
    flatDir {
        dirs 'libs'
    }
}
dependencies {
    ...
    compileOnly name: 'PlayTimeLimiter' // Without .jar
}
```

## 💡 Config Example (`config.yml`)

```yaml
limits:
  default: 120
  weekend: 240

hud:
  interval-seconds: 10
  scoreboard:
    title: "&6⏳ PlayTime"
    lines:
      - "&fUsed: &e{used} &f/ &e{limit} min"
      - "&aRemaining: &f{remaining} min"
      - "&6Percent: &f{percent}%%"

death:
  penalty-minutes: 1
  steal-minutes: 5
  progressive: true
  progressive-increment: 1
  progressive-limit: 5

rewards:
  enabled: true

  total:
    "5":
      command: "give %player% iron_ingot 3"
      message: "You’ve received 3 Iron for playing 5 minutes!"
    "60":
      command: "give %player% golden_apple 1"
      broadcast: "%player% has played 1 hours total!"
    "600":
      command: "give %player% totem_of_undying 1"
      broadcast: "%player% just receive 1 Totem of Undying for 10 hour of total playtime!"

  daily:
    "5":
      command: "give %player% potato 5"
      message: "You’ve received 5 potatos for playing 5 minutes!"

messages:
  hud: "&ePlaytime: &f{used}/{limit} min ({percent}%%) - &a{remaining} min left"
  kick: "&cYou have reached your daily playtime limit of &e{minutes}&c minutes."
  warn75: "&6⚠️ You have used &f75%&6 of your daily playtime limit!"
  warn90: "&6⏳ You are at &f90%&6 of your daily playtime limit, be prepared for real life!."
  kill-reward: "&aYou gained &e{drain} &aminutes from killing {victim}."
  kill-loss: "&cYou lost &e{drain} &cminutes to {killer}."
  killed: "&cYou lost &e{reduce} &cminutes as a death penalty"