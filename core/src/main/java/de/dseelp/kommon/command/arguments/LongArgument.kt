package de.dseelp.kommon.command.arguments

import de.dseelp.kommon.command.CommandContext

/**
 * @author DSeeLP
 */
class LongArgument<S : Any> @JvmOverloads constructor(
    name: String,
    val completer: CommandContext<S>.() -> Array<Long> = { arrayOf() }
) : Argument<S, Long>(name) {
    override fun get(value: String): Long? = value.toLongOrNull()
    override fun getErrorMessage(): String = "%s is not an Long"
    override fun complete(context: CommandContext<S>, value: String): Array<String> =
        completer.invoke(context).map { it.toString() }.filter { value.startsWith(it, true) }.toTypedArray()
}