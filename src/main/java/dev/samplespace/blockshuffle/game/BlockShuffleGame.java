package dev.samplespace.blockshuffle.game;

import dev.samplespace.blockshuffle.BlockShuffle;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BlockShuffleGame implements ForwardingAudience {

    private final @NotNull Set<PlayerState> players;
    private final List<Material> materials;

    private BukkitTask game;
    private int seconds = 300;

    public BlockShuffleGame() {
        this.players = new HashSet<>();
        this.materials = new ArrayList<>();

        // Load the list of blocks to memory
        FileConfiguration config = BlockShuffle.get().getConfig();
        for (String s : config.getStringList("blocks")) {
            Material material = Material.valueOf(s.toUpperCase().trim());
            this.materials.add(material);
        }
    }

    public void start() {
        this.players.forEach(state -> {
            Player player = state.asPlayer();
            if (player == null) {
                return;
            }

            // Set world settings to defaults
            World world = player.getWorld();
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);

            // Set player settings to defaults
            player.setFoodLevel(20);
            player.setSaturation(20);
            //noinspection DataFlowIssue
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            player.getInventory().clear();

            player.sendMessage(Component.text("Block Shuffle is starting! Get ready!").color(NamedTextColor.YELLOW));

            // Assign a new random block and scoreboard to the player state
            state.setBoard(new FastBoard(player));
            state.assignBlock(this.getRandomBlock());
        });

        this.game = new BukkitRunnable() {
            @Override
            public void run() {
                int completed = 0;
                for (PlayerState state : BlockShuffleGame.this.players) {
                    // Update scoreboards
                    state.updateBoard(BlockShuffleGame.this.seconds);

                    // Count players who have found their block
                    if (state.isCompleted()) {
                        completed++;
                    }
                }

                if (completed == BlockShuffleGame.this.players.size()) {
                    // Assign new blocks to the remaining players
                    for (PlayerState state : BlockShuffleGame.this.players) {
                        Player player = state.asPlayer();
                        if (player == null) {
                            return;
                        }

                        state.assignBlock(BlockShuffleGame.this.getRandomBlock());
                        BlockShuffleGame.this.seconds = 300;
                    }
                }

                // When there are 0 seconds left
                if (BlockShuffleGame.this.seconds == 0) {
                    Set<PlayerState> failedPlayers = BlockShuffleGame.this.players.stream()
                            .filter(state -> !state.isCompleted())
                            .collect(Collectors.toSet());

                    // If everyone failed
                    if (failedPlayers.size() == BlockShuffleGame.this.players.size()) {
                        BlockShuffleGame.this.sendMessage(Component.text("No one found their block! Game over.").color(NamedTextColor.RED));
                        this.cancel();
                        BlockShuffleGame.this.stop();
                        return;
                    }

                    failedPlayers.forEach(BlockShuffleGame.this.players::remove);

                    // If at least one person failed
                    if (failedPlayers.size() > 0) {
                        String players = BlockShuffleGame.this.niceList(failedPlayers.stream()
                                .map(PlayerState::asPlayer)
                                .filter(Objects::nonNull)
                                .map(Player::displayName).map(component -> (TextComponent) component).map(TextComponent::content)
                                .toList());

                        BlockShuffleGame.this.sendMessage(Component.text(players + " failed to find their block! They have been eliminated.")
                                .color(NamedTextColor.RED));
                    }

                    // If there is one player remaining
                    if (BlockShuffleGame.this.players.size() == 1) {
                        UUID uuid = BlockShuffleGame.this.players.toArray(PlayerState[]::new)[0].getUniqueId();
                        Player winner = Bukkit.getPlayer(uuid);
                        if (winner != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.sendMessage(winner.displayName().color(NamedTextColor.GOLD).append(Component.text(" is the winner!").color(NamedTextColor.YELLOW)));
                                player.showTitle(Title.title(
                                        Component.text("Winner: ").color(NamedTextColor.YELLOW).append(winner.displayName().color(NamedTextColor.GOLD)),
                                        Component.empty(),
                                        Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(250)))
                                );
                            }
                        }
                        this.cancel();
                        BlockShuffleGame.this.stop();
                        return;
                    }

                    // Assign new blocks to the remaining players
                    for (PlayerState state : BlockShuffleGame.this.players) {
                        Player player = state.asPlayer();
                        if (player == null) {
                            return;
                        }

                        state.assignBlock(BlockShuffleGame.this.getRandomBlock());
                        BlockShuffleGame.this.seconds = 300;
                    }
                }

                BlockShuffleGame.this.seconds--;
            }
        }.runTaskTimer(BlockShuffle.get(), 0, 20);

        for (PlayerState state : this.players) {
            this.checkBlock(state);
        }
    }

    public void stop() {
        this.game.cancel();
        this.players.clear();
    }

    public void checkBlock(PlayerState state) {
        Player player = state.asPlayer();
        if (player == null) {
            return;
        }
        this.checkBlock(state, player.getLocation());
    }

    public void checkBlock(PlayerState state, Location location) {
        Material material = state.getMaterial();
        if (location.getBlock().getType().equals(material)) {
            state.setCompleted(true);

            Player player = state.asPlayer();
            if (player == null) {
                return;
            }

            this.sendMessage(player.displayName().color(NamedTextColor.AQUA).append(Component.text(" found their block!").color(NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("Well done! You found your block.").color(NamedTextColor.GREEN));
            player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    public boolean addPlayer(Player player) {
        return this.players.add(new PlayerState(player.getUniqueId()));
    }

    public boolean removePlayer(Player player) {
        return this.players.removeIf(state -> state.getUniqueId().equals(player.getUniqueId()));
    }

    private Material getRandomBlock() {
        return this.materials.get(ThreadLocalRandom.current().nextInt(0, this.materials.size() - 1));
    }

    private String niceList(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i)).append(i < strings.size() - 2 ? ", " : i == strings.size() - 2 ? " and " : "");
        }
        return sb.toString();
    }

    public @NotNull Set<PlayerState> getPlayers() {
        return this.players;
    }

    public @Nullable PlayerState getPlayerState(Player player) {
        for (PlayerState state : this.players) {
            if (state.getUniqueId().equals(player.getUniqueId())) {
                return state;
            }
        }
        return null;
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return this.players.stream().map(PlayerState::asPlayer).filter(Objects::nonNull).toList();
    }
}
