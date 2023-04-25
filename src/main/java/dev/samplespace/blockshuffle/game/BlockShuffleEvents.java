package dev.samplespace.blockshuffle.game;

import dev.samplespace.blockshuffle.BlockShuffle;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class BlockShuffleEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BlockShuffleGame game = BlockShuffle.get().getGame();
        if (game.getPlayers().isEmpty()) {
            return;
        }

        PlayerState state = game.getPlayerState(event.getPlayer());
        if (state == null || state.isCompleted()) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ()) {
            return;
        }

        game.checkBlock(state, to.clone().subtract(0, 1, 0));
    }
}
