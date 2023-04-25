package dev.samplespace.blockshuffle.game;

import dev.samplespace.blockshuffle.Locale;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerState {

    private final UUID uuid;
    private Material material;
    private boolean completed;
    private FastBoard board;


    public PlayerState(UUID uuid) {
        this.uuid = uuid;
    }

    public @Nullable Player asPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public Material getMaterial() {
        return this.material;
    }

    private void setMaterial(Material material) {
        this.material = material;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public FastBoard getBoard() {
        return this.board;
    }

    public void setBoard(FastBoard board) {
        this.board = board;
    }

    public void updateBoard(int seconds) {
        String block = Locale.getKey(this.material);

//        if (!this.players.contains(board.getPlayer().getUniqueId())) {
//            board.updateTitle(BlockShuffleEvents.DEFAULT_TITLE);
//            board.updateLines(BlockShuffleEvents.DEFAULT_LINES);
//            return;
//        }

        this.board.updateTitle(Component.text("Block Shuffle").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        this.board.updateLines(
                Component.empty(),
                Component.text("Your block: ").color(NamedTextColor.GRAY).append(Component.translatable(block).color(NamedTextColor.GOLD)),
                Component.text("Completed: ").color(NamedTextColor.GRAY)
                        .append(Component.text(this.completed ? "✓" : "✕").color(this.completed ? NamedTextColor.GREEN : NamedTextColor.RED)),
                Component.empty(),
                Component.text("%02d:%02d".formatted(seconds / 60, seconds % 60)).color(NamedTextColor.YELLOW)
        );
    }

    public void assignBlock(Material material) {
        Player player = this.asPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(Component.text("Your block is: ").color(NamedTextColor.GRAY)
                .append(Component.translatable(Locale.getKey(material)).color(NamedTextColor.GOLD)));

        player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER.key(), Sound.Source.MASTER, 1.0f, 2.0f));

        this.setMaterial(material);
        this.setCompleted(false);
    }
}
