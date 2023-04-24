package dev.samplespace.blockshuffle.game;

import dev.samplespace.blockshuffle.BlockShuffle;
import dev.samplespace.blockshuffle.Locale;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class BlockShuffleGame {

    private final @NotNull Set<UUID> players;
    private final @NotNull Map<UUID, Material> blocks;
    private final @NotNull Map<UUID, Boolean> completed;
    private final @NotNull Map<UUID, FastBoard> scoreboards;
    private final ArrayList<Material> materials;

    private BukkitTask game;
    private int seconds = 300;

    public BlockShuffleGame() {
        this.players = new HashSet<>();
        this.blocks = new HashMap<>();
        this.completed = new HashMap<>();
        this.scoreboards = new HashMap<>();

        this.materials = new ArrayList<>();

        // Load the list of blocks to memory
        FileConfiguration config = BlockShuffle.get().getConfig();
        for (String s : config.getStringList("blocks")) {
            Material material = Material.valueOf(s.toUpperCase().trim());
            this.materials.add(material);
        }
    }

    public void start() {
        this.players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
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

            this.scoreboards.put(uuid, new FastBoard(player));

            // Assign a new random block
            this.assignBlock(uuid);
        });

        this.game = new BukkitRunnable() {
            @Override
            public void run() {
                for (FastBoard board : BlockShuffleGame.this.scoreboards.values()) {
                    BlockShuffleGame.this.updateBoard(board);
                }

                int completedCount = 0;
                for (Map.Entry<UUID, Boolean> entry : BlockShuffleGame.this.completed.entrySet()) {
                    if (entry.getValue()) {
                        completedCount++;
                    }
                }
                if (completedCount == BlockShuffleGame.this.players.size()) {
                    // Assign new blocks to the remaining players
                    for (UUID uuid : BlockShuffleGame.this.players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) {
                            return;
                        }

                        BlockShuffleGame.this.assignBlock(uuid);
                        BlockShuffleGame.this.seconds = 300;
                    }
                }

                // When there are 0 seconds left
                if (BlockShuffleGame.this.seconds == 0) {
                    List<Map.Entry<UUID, Boolean>> failedPlayers = BlockShuffleGame.this.completed.entrySet().stream()
                            .filter(entry -> !entry.getValue())
                            .toList();

                    // If everyone failed
                    if (failedPlayers.size() == BlockShuffleGame.this.players.size()) {
                        for (UUID uuid : BlockShuffleGame.this.players) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player == null) {
                                continue;
                            }

                            player.sendMessage(Component.text("No one found their block! Game over.").color(NamedTextColor.RED));
                        }
                        this.cancel();
                        BlockShuffleGame.this.stop();
                        return;
                    }
                    failedPlayers.forEach(entry -> BlockShuffleGame.this.players.remove(entry.getKey()));

                    // If at least one person failed
                    if (failedPlayers.size() > 0) {
                        String players = BlockShuffleGame.this.niceList(failedPlayers.stream()
                                .map(Map.Entry::getKey).map(Bukkit::getPlayer)
                                .filter(Objects::nonNull)
                                .map(Player::displayName).map(component -> (TextComponent) component).map(TextComponent::content)
                                .toList());

                        for (UUID uuid : BlockShuffleGame.this.players) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player == null) {
                                continue;
                            }
                            player.sendMessage(Component.text(players + " failed to find their block! They have been eliminated.").color(NamedTextColor.RED));
                        }
                    }

                    // If there is one player remaining
                    if (BlockShuffleGame.this.players.size() == 1) {
                        UUID uuid = BlockShuffleGame.this.players.toArray(UUID[]::new)[0];
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
                    for (UUID uuid : BlockShuffleGame.this.players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) {
                            return;
                        }

                        BlockShuffleGame.this.assignBlock(uuid);
                        BlockShuffleGame.this.seconds = 300;
                    }
                }

                BlockShuffleGame.this.seconds--;
            }
        }.runTaskTimer(BlockShuffle.get(), 0, 20);
    }

    public void stop() {
        this.game.cancel();
        this.players.clear();
        this.completed.clear();
        this.blocks.clear();
    }

    private void updateBoard(FastBoard board) {
        UUID uuid = board.getPlayer().getUniqueId();
        String block = Locale.getKey(this.blocks.get(uuid));
        boolean completed = this.completed.get(uuid);

        if (!this.players.contains(board.getPlayer().getUniqueId())) {
            board.updateTitle(BlockShuffleEvents.DEFAULT_TITLE);
            board.updateLines(BlockShuffleEvents.DEFAULT_LINES);
            return;
        }

        board.updateTitle(Component.text("Block Shuffle").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        board.updateLines(
                Component.empty(),
                Component.text("Your block: ").color(NamedTextColor.GRAY).append(Component.translatable(block).color(NamedTextColor.GOLD)),
                Component.text("Completed: ").color(NamedTextColor.GRAY)
                        .append(Component.text(completed ? "✓" : "✕").color(completed ? NamedTextColor.GREEN : NamedTextColor.RED)),
                Component.empty(),
                Component.text("%02d:%02d".formatted(this.seconds / 60, this.seconds % 60)).color(NamedTextColor.YELLOW)
        );
    }

    private void assignBlock(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        this.assignBlock(player);
    }
    private void assignBlock(Player player) {
        Material randomBlock = this.getRandomBlock();
        player.sendMessage(Component.text("Your block is: ").color(NamedTextColor.GRAY)
                .append(Component.translatable(Locale.getKey(randomBlock)).color(NamedTextColor.GOLD)));

        player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER.key(), Sound.Source.MASTER, 1.0f, 2.0f));
        this.blocks.put(player.getUniqueId(), randomBlock);
        this.completed.put(player.getUniqueId(), false);
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

    public @NotNull Set<UUID> getPlayers() {
        return this.players;
    }

    public @NotNull Map<UUID, Material> getBlocks() {
        return this.blocks;
    }

    public @NotNull Map<UUID, Boolean> getCompleted() {
        return this.completed;
    }

    public @NotNull Map<UUID, FastBoard> getScoreboards() {
        return this.scoreboards;
    }
}
