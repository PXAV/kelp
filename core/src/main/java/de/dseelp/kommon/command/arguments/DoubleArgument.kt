package de.dseelp.kommon.command.arguments

import de.dseelp.kommon.command.CommandContext

/**
 * @author DSeeLP
 */
class DoubleArgument<S : Any> @JvmOverloads constructor(
    name: String,
    val completer: CommandContext<S>.() -> Array<Double> = { arrayOf() }
) : Argument<S, Double>(name) {
    override fun get(value: String): Double? = value.toDoubleOrNull()
    override fun getErrorMessage(): String = "%s is not an Double"
    override fun complete(context: CommandContext<S>, value: String): Array<String> =
        completer.invoke(context).map { it.toString() }.filter { value.startsWith(it, true) }.toTypedArray()
}