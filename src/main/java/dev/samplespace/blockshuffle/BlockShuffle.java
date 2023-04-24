package dev.samplespace.blockshuffle;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.samplespace.blockshuffle.command.BlockShuffleCommands;
import dev.samplespace.blockshuffle.game.BlockShuffleEvents;
import dev.samplespace.blockshuffle.game.BlockShuffleGame;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class BlockShuffle extends JavaPlugin {

    private static BlockShuffle instance;
    private final AtomicReference<BlockShuffleGame> game = new AtomicReference<>();

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);

        instance = this;

        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new BlockShuffleEvents(), this);
        this.setGame(new BlockShuffleGame());

        new CommandAPICommand("blockshuffle")
                .withSubcommand(BlockShuffleCommands.ADD_COMMAND)
                .withSubcommand(BlockShuffleCommands.JOIN_COMMAND)
                .withSubcommand(BlockShuffleCommands.REMOVE_COMMAND)
                .withSubcommand(BlockShuffleCommands.LEAVE_COMMAND)
                .withSubcommand(BlockShuffleCommands.LIST_COMMAND)
                .withSubcommand(BlockShuffleCommands.START_COMMAND)
                .withSubcommand(BlockShuffleCommands.STOP_COMMAND)
                .withSubcommand(BlockShuffleCommands.SKIP_COMMAND)
                .withAliases("bs")
                .register();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    public Optional<BlockShuffleGame> getGame() {
        return Optional.ofNullable(this.game.get());
    }

    public void setGame(BlockShuffleGame game) {
        this.game.set(game);
    }

    public static BlockShuffle get() {
        return instance;
    }
}
