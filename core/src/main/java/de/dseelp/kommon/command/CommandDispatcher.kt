package de.dseelp.kommon.command

import de.dseelp.kommon.command.arguments.ParsedArgument
import java.util.*

/**
 * @author DSeeLP
 */
class CommandDispatcher<S : Any> {
    private val nodes = mutableListOf<CommandNode<S>>()

    fun register(node: CommandNode<S>) {
        if (node.name == null) throw IllegalArgumentException("The root node must have a name!")
        nodes.add(node)
    }

    fun register(name: String, block: CommandBuilder<S>.() -> Unit) {
        register(literal(name, block))
    }

    fun register(builder: JavaCommandBuilder<S>) {
        register(builder.build())
    }

    fun unregister(name: String) {
        val node = getNode(name) ?: return
        nodes.remove(node)
    }

    fun getNode(name: String, useAliases: Boolean = false): CommandNode<S>? {
        val lowercaseName = name.lowercase(Locale.getDefault())
        for (node in nodes) {
            if (node.name!!.lowercase(Locale.getDefault()) == lowercaseName) return node
            if (useAliases) {
                for (alias in node.aliases) {
                    if (alias.lowercase(Locale.getDefault()) == lowercaseName) return node
                }
            }
        }
        return null
    }

    private fun recursiveParse(
        parent: CommandNode<S>,
        args: Array<String>,
        currentResult: ParsedResult<S>
    ): ParsedResult<S> {
        if (args.isEmpty()) return currentResult.copy(node = parent)

        val copiedArgs = if (args.size > 1) args.copyOfRange(1, args.size) else arrayOf()
        val currentArg = args[0]

        //val child = getNode(currentArg, true, parent)
        //child?.let { return recursiveParse(it as CommandNode<T>, copiedArgs, currentResult) }
        for (child in parent.childs) {
            val endArgs = copiedArgs
            if (child.name?.equals(currentArg, child.ignoreCase) == true)
                return recursiveParse(
                    child.target ?: child,
                    endArgs,
                    currentResult.copy(node = child, context = currentResult.context.updateContext(child.mappers))
                )
            for (alias in child.aliases) {
                if (alias.equals(currentArg, child.ignoreCase))
                    return recursiveParse(
                        child.target ?: child,
                        endArgs,
                        currentResult.copy(node = child, context = currentResult.context.updateContext(child.mappers))
                    )
            }
            val idArg = child.argumentIdentifier
            val value = idArg?.get(currentArg) ?: continue
            //val shortend = (endArgs.toList()-endArgs[0]).toTypedArray()
            return recursiveParse(
                child.target ?: child,
                endArgs,
                copy(
                    currentResult, mapOf(idArg.name to ParsedArgument(idArg.name, false, value))
                ).let {
                    it.copy(context = it.context.updateContext(child.mappers))
                }
            )
        }
        val parseArgs = parseArgs(parent, args)
        if (parseArgs.ok && parseArgs.noArg && parent.arguments.isNotEmpty()) return currentResult.copy(node = parent)
        if (parent.executor != null) return currentResult.copy(node = parent)
        return currentResult.copy(failed = true, cause = ParsedResult.FailureCause.USAGE)
    }


    private fun CommandContext<S>.updateContext(mappers: Map<String, CommandContext<S>.(input: Any) -> Any?>) =
        this.copy(mappers = this.mappers + mappers)

    private fun copy(result: ParsedResult<S>, parseArgs: Map<String, ParsedArgument<*>>) =
        result.copy(context = result.context.copy(args = result.context.args + parseArgs))

    fun parse(command: String): ParsedResult<S>? {
        val splitted = parseRaw(command)
        if (splitted.isEmpty()) {
            return null
        }
        val name = splitted[0]
        val node = getNode(name, useAliases = true) ?: return null
        @Suppress("UNCHECKED_CAST")
        return recursiveParse(
            node,
            if (splitted.size == 1) arrayOf() else splitted.copyOfRange(1, splitted.size),
            ParsedResult(
                node,
                CommandContext(
                    mapOf(),
                    mapOf(),
                    mapOf()
                ), failed = false
            )
        )
    }

    private data class ParseArgsResult(
        val ok: Boolean,
        val newArgs: Array<String> = arrayOf(),
        val parsed: Map<String, ParsedArgument<*>> = mapOf(),
        val failedArg: Boolean = false,
        val usage: Boolean = false,
        val noArg: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ParseArgsResult

            if (ok != other.ok) return false
            if (!newArgs.contentEquals(other.newArgs)) return false
            if (parsed != other.parsed) return false
            if (failedArg != other.failedArg) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ok.hashCode()
            result = 31 * result + newArgs.contentHashCode()
            result = 31 * result + parsed.hashCode()
            result = 31 * result + failedArg.hashCode()
            return result
        }
    }

    private fun <T : Any> parseArgs(
        node: CommandNode<T>,
        args: Array<String>
    ): ParseArgsResult {
        val parsed = mutableListOf<ParsedArgument<*>>()
        val nArgs = node.arguments
        var usedIndex = -1

        for (index in args.indices) {
            val s = args[index]
            if (nArgs.size <= index) break
            val arg = nArgs[index]
            val value = arg.get(s)
            if (value != null) {
                usedIndex = index
                parsed.add(ParsedArgument(arg.name, false, value))
            } else {
                //if (arg.optional) continue
                //TODO: Error Handling
                return ParseArgsResult(false, usage = true)
            }

        }
        if (usedIndex != nArgs.lastIndex) {
            for (index in (if (usedIndex == -1) 0 else usedIndex)..nArgs.lastIndex) {
//                if (!nArgs[index].optional) return ParseArgsResult(false, failedArg = true)
                return ParseArgsResult(false, failedArg = true)
            }
        }

        if (usedIndex == -1) return ParseArgsResult(true, noArg = true)

        val data = if (args.isNotEmpty() && usedIndex > -1)
            args.copyOfRange(usedIndex + 1, args.lastIndex + 1)
        else
            arrayOf()

        return ParseArgsResult(true, data, parsed.map { it.name to it }.toMap())
    }

    private fun parseRaw(value: String): Array<String> {
        val rawSplitted = value.split(' ')
        val splitted = mutableListOf<String>()
        var inS = false
        var raw = ""
        for (s in rawSplitted) {
            if (!inS && s.startsWith('\'')) {
                if (s == "''") continue
                inS = true
                raw = if (s == "' ") " "
                else "${s.replaceFirst("'", "")} "
                continue
            }
            if (inS) {
                if (s.endsWith('\'')) {
                    inS = false
                    if (s.length in 1..1) {
                        raw += " "
                    } else if (s.length > 1) {
                        raw += s.substring(0 until s.lastIndex)
                    }
                    splitted.add(raw)
                    raw = ""
                    continue
                }
                raw += "$s "
            } else splitted.add(s)
        }
        if (inS) splitted.add(raw.substring(0 until raw.lastIndex))
        return splitted.toTypedArray()
    }


    fun complete(sender: S, command: String): Array<String> {
        val raw = parseRaw(command)
        if (raw.isEmpty()) {
            return arrayOf()
        }
        val name = raw[0]
        val node = getNode(name, useAliases = true) ?: return arrayOf()
        val args = if (raw.size == 1) arrayOf() else raw.copyOfRange(1, raw.size)
        return recursiveComplete(node, CommandContext<S>(mapOf(), mapOf(), mapOf()).apply { this.sender = sender }, args)
    }

    private fun recursiveComplete(
        node: CommandNode<S>,
        context: CommandContext<S>,
        args: Array<String>
    ): Array<String> {
        val newArgs = if (args.isEmpty()) arrayOf() else args.copyOfRange(1, args.size)
        val current = if (args.isEmpty()) "" else args[0]
        if (args.isNotEmpty() && args[0] != "") {
            for (child in node.childs) {
                if (child.name?.equals(current, child.ignoreCase) == true) {
                    return recursiveComplete(child, context, newArgs)
                }
                if (args[args.lastIndex] != "") continue
                val idArg = child.argumentIdentifier
                val value = idArg?.get(current) ?: continue
                return recursiveComplete(
                    child,
                    context.copy(context.args.plus(idArg.name to ParsedArgument(idArg.name, false, value))),
                    newArgs
                )
            }
        }
        if (newArgs.isEmpty()) {
            val strings = mutableSetOf<String>()
            node.childs
                .filter { it.checkAccess.invoke(context) }
                .mapNotNull { it.argumentIdentifier }
                .map { it.complete(context, current) }
                .forEach { strings.addAll(it) }
            node.childs.filter { it.checkAccess.invoke(context) }.filter { it.argumentIdentifier == null }.forEach { strings.addAll(it.aliases + it.name!!) }
            return strings.toTypedArray()
        }
        return arrayOf()
    }
}