package com.darkun7.limiter.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    public UUID uuid;
    public int dailyUsed = 0;
    public int dailyExtra = 0;
    public int totalUsed = 0;
    public boolean hudEnabled = true;
    public String hudType = "tablist"; // default value
    public int dailyDeath = 0;

    public Set<Integer> dailyClaimed = new HashSet<>();
    public Set<Integer> totalClaimed = new HashSet<>();

    public String lastLoginDate = "";

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }
}
