package dev.samplespace.blockshuffle.game;

import dev.samplespace.blockshuffle.BlockShuffle;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class BlockShuffleEvents implements Listener {

    public static final Component DEFAULT_TITLE = Component.text("Block Shuffle").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
    public static final List<Component> DEFAULT_LINES = List.of(Component.empty(), Component.text("You are not in a game!").color(NamedTextColor.GRAY), Component.empty());

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BlockShuffleGame game = BlockShuffle.get().getGame().orElse(null);
        if (game == null || game.getCompleted().isEmpty() || game.getBlocks().isEmpty()) {
            return;
        }
        Player player = event.getPlayer();

        boolean completed = game.getCompleted().get(player.getUniqueId());
        if (completed) {
            return;
        }

        Material material = game.getBlocks().get(player.getUniqueId());
        if (event.getTo().clone().subtract(0, 1, 0).getBlock().getType().equals(material)) {
            game.getCompleted().put(player.getUniqueId(), true);
            player.sendMessage(Component.text("Well done! You found your block.").color(NamedTextColor.GREEN));
            player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BlockShuffleGame game = BlockShuffle.get().getGame().orElse(null);
        if (game == null) {
            return;
        }

        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        game.getScoreboards().put(player.getUniqueId(), board);

        if (!game.getPlayers().contains(player.getUniqueId())) {
            board.updateTitle(DEFAULT_TITLE);
            board.updateLines(DEFAULT_LINES);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BlockShuffleGame game = BlockShuffle.get().getGame().orElse(null);
        if (game == null) {
            return;
        }

        Player player = event.getPlayer();
        game.getScoreboards().remove(player.getUniqueId());
    }
}
