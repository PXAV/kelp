package de.dseelp.kommon.command.arguments

import de.dseelp.kommon.command.CommandContext

/**
 * @author DSeeLP
 */
class StringArgument<S: Any>(name: String, val completer: CommandContext<S>.() -> Array<String> = { arrayOf() }) : Argument<S, String>(name) {
    constructor(name: String): this(name, { arrayOf() })

    override fun get(value: String): String = value
    override fun getErrorMessage(): String = "This should not happen!"
    override fun complete(context: CommandContext<S>, value: String): Array<String> = completer.invoke(context).filterPossibleMatches(value)
}