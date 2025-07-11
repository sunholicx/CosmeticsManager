package me.sunrise.cosmeticsmanager.utils;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class GradientInputManager {

    private final Set<Player> awaitingInput = new HashSet<>();

    public void add(Player player) {
        awaitingInput.add(player);
    }

    public void remove(Player player) {
        awaitingInput.remove(player);
    }

    public boolean isAwaiting(Player player) {
        return awaitingInput.contains(player);
    }
}
