package dev.samplespace.blockshuffle;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.samplespace.blockshuffle.command.BlockShuffleCommands;
import dev.samplespace.blockshuffle.game.BlockShuffleEvents;
import dev.samplespace.blockshuffle.game.BlockShuffleGame;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class BlockShuffle extends JavaPlugin {

    private static BlockShuffle instance;
    private final BlockShuffleGame game = new BlockShuffleGame();

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);

        instance = this;

        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new BlockShuffleEvents(), this);

        new CommandAPICommand("blockshuffle")
                .withAliases("bs")
                .withSubcommand(BlockShuffleCommands.ADD_COMMAND)
                .withSubcommand(BlockShuffleCommands.JOIN_COMMAND)
                .withSubcommand(BlockShuffleCommands.REMOVE_COMMAND)
                .withSubcommand(BlockShuffleCommands.LEAVE_COMMAND)
                .withSubcommand(BlockShuffleCommands.LIST_COMMAND)
                .withSubcommand(BlockShuffleCommands.START_COMMAND)
                .withSubcommand(BlockShuffleCommands.STOP_COMMAND)
                .withSubcommand(BlockShuffleCommands.SKIP_COMMAND)
                .register();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    public @NotNull BlockShuffleGame getGame() {
        return this.game;
    }

    public static BlockShuffle get() {
        return instance;
    }
}
