package de.pxav.kelp.implementation1_8.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import de.dseelp.kommon.command.CommandDispatcher;
import de.dseelp.kommon.command.CommandNode;
import de.dseelp.kommon.command.ParsedResult;
import de.pxav.kelp.core.command.*;
import de.pxav.kelp.core.command.version.CommandRegistryVersionTemplate;
import de.pxav.kelp.core.player.KelpPlayer;
import de.pxav.kelp.core.player.KelpPlayerRepository;
import de.pxav.kelp.core.version.Versioned;
import kotlin.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A class description goes here.
 *
 * @author pxav
 */
@Versioned
public class VersionedCommandRegistry extends CommandRegistryVersionTemplate {

  private KelpConsoleSenderFactory consoleSenderFactory;
  private KelpPlayerRepository playerRepository;
  private JavaPlugin kelpPlugin;
  private CommandDispatcher<? extends KelpCommandSender<?>> commandDispatcher;

  @Inject
  public VersionedCommandRegistry(KelpConsoleSenderFactory consoleSenderFactory, KelpPlayerRepository playerRepository, JavaPlugin kelpPlugin, CommandDispatcher<? extends KelpCommandSender<?>> commandDispatcher) {
    this.consoleSenderFactory = consoleSenderFactory;
    this.playerRepository = playerRepository;
    this.kelpPlugin = kelpPlugin;
    this.commandDispatcher = commandDispatcher;
  }

  @Override
  public void registerCommand(KelpCommand command, CreateCommand commandAnnotation) {
    Command bukkitCommand =
      new Command(commandAnnotation.name()) {

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
          if (args.length == 0) {
            return checkExecutorsAndExecute(sender, args, command, commandAnnotation.executorType());
          }

          KelpCommand parentCommand = command;
          int lastCommandArgument = 0;

          for (int i = 1; i < args.length + 1; i++) {
            for (Map.Entry<KelpCommand, CreateSubCommand> subCommandEntry : parentCommand.getSubCommands().entrySet()) {
              KelpCommand subCommand = subCommandEntry.getKey();
              CreateSubCommand subCommandAnnotation = subCommandEntry.getValue();
              String subCommandName = args[i - 1];
              if (subCommandAnnotation.name().equalsIgnoreCase(subCommandName)) {
                if (i == args.length) {
                  String[] newArgs = args;
                  if (subCommand.shouldArgumentsStartFromZero()) {
                    newArgs = new String[] {};
                  }
                  return checkExecutorsAndExecute(sender, newArgs, subCommand, subCommandAnnotation.executorType());
                } else {
                  lastCommandArgument = i;
                  parentCommand = subCommand;
                }
              }
            }
          }

          if (parentCommand.customParametersAllowed()) {
            ExecutorType executorType;
            String[] newArgs = args;
            if (parentCommand.equals(command)) {
              CreateCommand annotation = parentCommand.getClass().getAnnotation(CreateCommand.class);
              executorType = annotation.executorType();
            } else {
              CreateSubCommand annotation = parentCommand.getClass().getAnnotation(CreateSubCommand.class);
              executorType = annotation.executorType();
            }
            if (parentCommand.shouldArgumentsStartFromZero()) {
              newArgs = Arrays.copyOfRange(args, lastCommandArgument, args.length);
            }
            return checkExecutorsAndExecute(sender, newArgs, parentCommand, executorType);
          }

          return false;
        }
      }.setAliases(Lists.newArrayList(command.getAliases()));

    registerInCommandMap(commandAnnotation.name(), bukkitCommand);
  }

  @Override
  public void registerCommand(DeclarativeKelpCommand<? extends KelpCommandSender<?>> command, CreateDeclarativeCommand commandAnnotation) {
    final String commandName = command.getCommandNode().getName();
    addToDispatcher(command.getCommandNode());
    Command bukkitCommand = new BukkitCommand(commandName) {
      @Override
      public boolean execute(CommandSender sender, String label, String[] args) {
        String joinedArguments = String.join(" ", args);
        final ParsedResult<? extends KelpCommandSender<?>> parsed = commandDispatcher.parse(label + " " + joinedArguments);
        if (parsed == null) {
          sender.sendMessage("Failed to parse command. Please contact an admin.");
          return true;
        }
        Pair<KelpCommandSender, Boolean> senderPair = getKelpCommandSender(sender);
        if (senderPair == null) {
          return true;
        }
        try {
          parsed.getClass().getDeclaredMethod("execute", Object.class, boolean.class).invoke(parsed, senderPair.component1(), senderPair.component2());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          e.printStackTrace();
        }
        return true;
      }

      @Nullable
      public Pair<KelpCommandSender, Boolean> getKelpCommandSender(CommandSender sender) {
        ExecutorType executorType = commandAnnotation.executorType();
        KelpCommandSender<?> kelpSender;
        boolean isConsoleSender = false;
        if ((sender instanceof Player && executorType != ExecutorType.CONSOLE_ONLY)) {
          kelpSender = playerRepository.getKelpPlayer((Player) sender);
        }else if ((sender instanceof ConsoleCommandSender && executorType != ExecutorType.PLAYER_ONLY)) {
          kelpSender = consoleSenderFactory.newKelpConsoleSender(sender);
          isConsoleSender = true;
        }else {
          sender.sendMessage("Command executed with unknown CommandSender.");
          return null;
        }
        return new Pair<>(kelpSender, isConsoleSender);
      }

      @Override
      public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Pair<KelpCommandSender, Boolean> senderPair = getKelpCommandSender(sender);
        if (senderPair == null || args.length == 0) {
          return ImmutableList.of();
        }

        String joinedArguments = String.join(" ", args);
        try {
          return ImmutableList.copyOf((String[]) commandDispatcher.getClass().getDeclaredMethod("complete", Object.class, String.class).invoke(commandDispatcher, senderPair.component1(), alias + " " + joinedArguments));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          e.printStackTrace();
          return ImmutableList.of();
        }
      }
    }.setAliases(Collections.singletonList(commandName));
    registerInCommandMap(commandName, bukkitCommand);
  }

  private void addToDispatcher(CommandNode<? extends KelpCommandSender<?>> node) {
    try {
      commandDispatcher.getClass().getDeclaredMethod("register", CommandNode.class).invoke(commandDispatcher, node);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private void registerInCommandMap(String name, Command bukkitCommand) {
    CommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();

    if (commandMap.getCommand(name) != null) {
      commandMap.getCommand(name).unregister(commandMap);
    }

    try {
      final Field f = commandMap.getClass().getDeclaredField("knownCommands");
      f.setAccessible(true);
      Map<String, Command> knownCommands = (Map<String, Command>) f.get(commandMap);
      if (knownCommands.containsKey(bukkitCommand)) {
        knownCommands.remove(bukkitCommand);
        f.set(commandMap, knownCommands);
      }
    } catch (IllegalAccessException | NoSuchFieldException ex) {
      return;
    }

    commandMap.register(kelpPlugin.getName(), bukkitCommand);
  }


  private boolean checkExecutorsAndExecute(CommandSender sender, String[] args, KelpCommand command, ExecutorType executorType) {
    if (executorType == ExecutorType.CONSOLE_ONLY && sender instanceof Player) {
      if (command.getNoConsoleMessage() == null) {
        sender.sendMessage("Default no console message");
      } else {
        sender.sendMessage(command.getNoConsoleMessage());
      }
      return false;
    } else if(executorType == ExecutorType.CONSOLE_ONLY) {
      command.onCommand(consoleSenderFactory.newKelpConsoleSender(sender), args);
      return true;
    }

    if (executorType == ExecutorType.PLAYER_ONLY && !(sender instanceof Player)) {
      if (command.getNoPlayerMessage() == null) {
        sender.sendMessage("Default no player message");
      } else {
        sender.sendMessage(command.getNoPlayerMessage());
      }
      return false;
    }

    if (executorType == ExecutorType.PLAYER_ONLY) {
      return this.checkPlayerPermissionsAndExecute(sender, args, command, ExecutorType.PLAYER_ONLY);
    }

    if (executorType == ExecutorType.PLAYER_AND_CONSOLE) {
      if (sender instanceof Player) {
        return this.checkPlayerPermissionsAndExecute(sender, args, command, ExecutorType.PLAYER_AND_CONSOLE);
      }
      command.onCommand(consoleSenderFactory.newKelpConsoleSender(sender), args);
      return true;
    }
    return false;
  }

  private boolean checkPlayerPermissionsAndExecute(CommandSender sender, String[] args, KelpCommand command, ExecutorType executorType) {
    Player bukkitPlayer = (Player) sender;
    KelpPlayer player = playerRepository.getKelpPlayer(bukkitPlayer);
    if (command.getPermission() != null) {
      if (bukkitPlayer.hasPermission(command.getPermission())) {
        if (executorType == ExecutorType.PLAYER_AND_CONSOLE) {
          if (command.shouldDelegateToConsole()) {
            command.onCommand(KelpConsoleSender.create(player.getBukkitPlayer()), args);
          } else {
            command.onCommand(player, args);
          }
        } else if (executorType == ExecutorType.PLAYER_ONLY) {
          command.onCommand(player, args);
        }
        return true;
      } else if (command.getNoPermissionMessage() != null) {
        player.sendMessage(command.getNoPermissionMessage());
      } else {
        player.sendMessage("Default no permission message.");
      }
      return false;
    } else {
      if (executorType == ExecutorType.PLAYER_AND_CONSOLE) {
        if (command.shouldDelegateToConsole()) {
          command.onCommand(KelpConsoleSender.create(player.getBukkitPlayer()), args);
        } else {
          command.onCommand(player, args);
        }
      } else if (executorType == ExecutorType.PLAYER_ONLY) {
        command.onCommand(player, args);
      }
      return true;
    }
  }

}
