package de.dseelp.kommon.command

import de.dseelp.kommon.command.arguments.Argument

/**
 * @author DSeeLP
 */
class CommandBuilder<S: Any>(
    val name: String? = null,
    val argument: Argument<S, *>? = null,
    val aliases: Array<String> = arrayOf(),
) {

    constructor(name: String, aliases: Array<String>): this(name, null, aliases)
    constructor(argument: Argument<S, *>): this(name = null, argument = argument)

    var target: CommandNode<S>? = null
        private set
    var arguments: Array<Argument<S, *>> = arrayOf()
        private set
    var childs: Array<CommandNode<S>> = arrayOf()
        private set

    private var executeBlock: (CommandContext<S>.() -> Unit)? = null
    private var noAccessBlock: (CommandContext<S>.(node: CommandNode<S>) -> Unit)? = null
    private var checkAccessBlock: (CommandContext<S>.() -> Boolean) = { true }

    private var parameters = mapOf<String, Any>()

    fun noAccess(block: CommandContext<S>.(node: CommandNode<S>) -> Unit) {
        noAccessBlock = block
    }

    fun checkAccess(block: CommandContext<S>.() -> Boolean) {
        checkAccessBlock = block
    }

    fun execute(block:  CommandContext<S>.() -> Unit) {
        executeBlock = block
    }

    fun forward(node: CommandNode<S>) {
        target = node
    }

    operator fun Pair<String, Any>.unaryPlus() {
        parameters = parameters + this
    }

    @Deprecated("Use literal() instead", ReplaceWith("literal(name, aliases) {block}"))
    fun node(name: String, aliases: Array<String> = arrayOf(), block: CommandBuilder<S>.() -> Unit) = literal(name, aliases, block)

    fun node(builder: CommandBuilder<S>) {
        childs += builder.build()
    }

    fun node(node: CommandNode<S>) {
        childs += node
    }

    fun literal(name: String, aliases: Array<String> = arrayOf(), block: CommandBuilder<S>.() -> Unit) {
        childs += CommandBuilder<S>(name, aliases = aliases).apply(block).build()
    }

    fun argument(argument: Argument<S, *>, block: CommandBuilder<S>.() -> Unit) {
        childs += CommandBuilder<S>(argument = argument).apply(block).build()
    }

    fun build(): CommandNode<S> {
        if (name == null && argument == null) throw IllegalStateException("An command node must have a name or an argument!")
        if (target != null && childs.isNotEmpty()) throw IllegalStateException("Cannot forward a node with children")
        if (target != null && arguments.isNotEmpty()) throw IllegalStateException("Cannot forward a node with arguments")
        return CommandNode(name, aliases, argument, target, arrayOf(), childs, executeBlock, checkAccess = checkAccessBlock, noAccess = noAccessBlock, parameters = parameters, mappers = mappers)
    }

    operator fun invoke(block: CommandBuilder<S>.() -> Unit) {
        this.apply(block)
    }

    fun <I : Any, O : Any?> map(name: String, mapper: CommandContext<S>.(input: I) -> O) {
        @Suppress("UNCHECKED_CAST")
        mappers = mappers + (name to mapper as CommandContext<S>.(input: Any) -> Any?)
    }

    var mappers: Map<String, CommandContext<S>.(input: Any) -> Any?> = mapOf()
        private set
}

@Deprecated(
    message = "This deprecated in favor of literal and will be removed in the future",
    replaceWith = ReplaceWith("literal(name, block)")
)
fun <S : Any> command(name: String, block: CommandBuilder<S>.() -> Unit): CommandNode<S> = literal(name, block)

fun <S : Any> literal(name: String, block: CommandBuilder<S>.() -> Unit): CommandNode<S> =
    CommandBuilder<S>(name).apply(block).build()

fun <T : Any> argument(argument: Argument<T, *>, block: CommandBuilder<T>.() -> Unit): CommandNode<T> =
    CommandBuilder<T>(argument = argument).apply(block).build()