package dev.samplespace.blockshuffle.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.samplespace.blockshuffle.BlockShuffle;
import dev.samplespace.blockshuffle.game.BlockShuffleGame;
import dev.samplespace.blockshuffle.game.PlayerState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Objects;

public class BlockShuffleCommands {

    public static final CommandAPICommand ADD_COMMAND = new CommandAPICommand("add")
            .withPermission(CommandPermission.OP)
            .withArguments(new PlayerArgument("player"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                BlockShuffleGame game = BlockShuffle.get().getGame();
                if (game.addPlayer(player)) {
                    sender.sendMessage(player.displayName().color(NamedTextColor.AQUA)
                            .append(Component.text(" was added to the game.").color(NamedTextColor.GREEN))
                    );
                    game.sendMessage(player.displayName().color(NamedTextColor.AQUA).append(Component.text(" joined the game!").color(NamedTextColor.YELLOW)));
                } else {
                    sender.sendMessage(player.displayName().color(NamedTextColor.AQUA)
                            .append(Component.text(" is already in the game.").color(NamedTextColor.RED)));
                }
            });

    public static final CommandAPICommand JOIN_COMMAND = new CommandAPICommand("join")
            .executesPlayer((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                if (game.addPlayer(sender)) {
                    sender.sendMessage(Component.text("You joined the game!").color(NamedTextColor.GREEN));
                    game.sendMessage(sender.displayName().color(NamedTextColor.AQUA).append(Component.text(" joined the game!").color(NamedTextColor.YELLOW)));
                } else {
                    sender.sendMessage(Component.text("You are already in the game!").color(NamedTextColor.RED));
                }
            });

    public static final CommandAPICommand REMOVE_COMMAND = new CommandAPICommand("remove")
            .withPermission(CommandPermission.OP)
            .withArguments(new PlayerArgument("player"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                BlockShuffleGame game = BlockShuffle.get().getGame();
                if (game.removePlayer(player)) {
                    sender.sendMessage(player.displayName().color(NamedTextColor.AQUA)
                            .append(Component.text(" was removed from the game.").color(NamedTextColor.GREEN)));
                    game.sendMessage(player.displayName().color(NamedTextColor.AQUA).append(Component.text(" left the game.").color(NamedTextColor.YELLOW)));
                } else {
                    sender.sendMessage(player.displayName().color(NamedTextColor.AQUA)
                            .append(Component.text(" is not in the game.").color(NamedTextColor.RED)));
                }
            });

    public static final CommandAPICommand LEAVE_COMMAND = new CommandAPICommand("leave")
            .executesPlayer((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                if (game.removePlayer(sender)) {
                    sender.sendMessage(Component.text("You left the game!").color(NamedTextColor.GREEN));
                    game.sendMessage(sender.displayName().color(NamedTextColor.AQUA).append(Component.text(" joined the game!").color(NamedTextColor.YELLOW)));
                } else {
                    sender.sendMessage(Component.text("You are not in the game!").color(NamedTextColor.RED));
                }
            });

    public static final CommandAPICommand LIST_COMMAND = new CommandAPICommand("list")
            .executes((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                Component playerList = Component.text(String.join(", ", game.getPlayers().stream()
                        .map(PlayerState::asPlayer).filter(Objects::nonNull)
                        .map(Player::displayName)
                        .map(component -> (TextComponent) component)
                        .map(TextComponent::content)
                        .toList()));

                sender.sendMessage(Component.text("Players: ").color(NamedTextColor.AQUA).append(playerList.color(NamedTextColor.GREEN)));
            });

    public static final CommandAPICommand START_COMMAND = new CommandAPICommand("start")
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                if (game.getPlayers().size() < 2) {
                    sender.sendMessage(Component.text("There are not enough players to start the game!").color(NamedTextColor.RED));
                    return;
                }

                sender.sendMessage(Component.text("Starting game...").color(NamedTextColor.GREEN));
                game.start();
            });

    public static final CommandAPICommand STOP_COMMAND = new CommandAPICommand("stop")
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                if (game.getPlayers().size() <= 1) {
                    sender.sendMessage(Component.text("There is no game running!").color(NamedTextColor.RED));
                    return;
                }

                sender.sendMessage(Component.text("Game stopped.").color(NamedTextColor.RED));
                game.stop();
            });

    public static final CommandAPICommand SKIP_COMMAND = new CommandAPICommand("skip")
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                BlockShuffleGame game = BlockShuffle.get().getGame();

                for (PlayerState state : game.getPlayers()) {
                    state.setCompleted(true);
                }
            });

}
