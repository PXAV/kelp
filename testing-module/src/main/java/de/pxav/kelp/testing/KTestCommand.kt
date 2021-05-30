package de.pxav.kelp.testing

import de.dseelp.kommon.command.CommandNode
import de.dseelp.kommon.command.arguments.BooleanArgument
import de.dseelp.kommon.command.command
import de.pxav.kelp.core.command.CreateDeclarativeCommand
import de.pxav.kelp.core.command.DeclarativeKelpCommand
import de.pxav.kelp.core.command.KelpCommandSender
import de.pxav.kelp.core.player.KelpPlayer

@CreateDeclarativeCommand
class KTestCommand: DeclarativeKelpCommand<KelpCommandSender<*>> {
  override fun getCommandNode(): CommandNode<KelpCommandSender<*>> = command("ktest") {
    execute {
      sender.sendMessage("This is a TestCommand written with KommonCommand in Kotlin")
    }
    argument(BooleanArgument("test")) {
      execute {
        println("TestCommand with argument ${get<Boolean>("test")}")
      }
    }
  }
}