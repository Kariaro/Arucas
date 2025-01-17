package me.senseiwells.arucas.builtin

import me.senseiwells.arucas.api.docs.ClassDoc
import me.senseiwells.arucas.api.docs.ConstructorDoc
import me.senseiwells.arucas.api.docs.FunctionDoc
import me.senseiwells.arucas.classes.ClassInstance
import me.senseiwells.arucas.classes.CreatableDefinition
import me.senseiwells.arucas.core.Interpreter
import me.senseiwells.arucas.core.Type
import me.senseiwells.arucas.exceptions.runtimeError
import me.senseiwells.arucas.utils.*
import me.senseiwells.arucas.utils.Util.Types.BOOLEAN
import me.senseiwells.arucas.utils.Util.Types.LIST
import me.senseiwells.arucas.utils.Util.Types.NUMBER
import me.senseiwells.arucas.utils.Util.Types.OBJECT
import me.senseiwells.arucas.utils.Util.Types.STRING
import me.senseiwells.arucas.utils.impl.ArucasList
import java.util.*
import java.util.regex.PatternSyntaxException

@ClassDoc(
    name = STRING,
    desc = [
        "This class represents an array of characters to form a string.",
        "This class cannot be instantiated directly, instead use the literal",
        "by using quotes. Strings are immutable in Arucas."
    ]
)
class StringDef(interpreter: Interpreter): CreatableDefinition<String>(STRING, interpreter) {
    private val pool = HashMap<String, ClassInstance>()

    fun literal(value: String) = this.create(StringUtils.unescapeString(value.substring(1, value.length - 1)))

    override fun create(value: String) = this.pool.getOrPut(value) { super.create(value) }

    override fun canConstructDirectly() = false

    override fun plus(instance: ClassInstance, interpreter: Interpreter, other: ClassInstance, trace: LocatableTrace): String {
        return instance.toString(interpreter, trace) + other.toString(interpreter, trace)
    }

    override fun compare(instance: ClassInstance, interpreter: Interpreter, type: Type, other: ClassInstance, trace: LocatableTrace): Any? {
        val otherString = other.getPrimitive(this) ?: return super.compare(instance, interpreter, type, other, trace)
        return when (type) {
            Type.LESS_THAN -> instance.asPrimitive(this) < otherString
            Type.LESS_THAN_EQUAL -> instance.asPrimitive(this) <= otherString
            Type.MORE_THAN -> instance.asPrimitive(this) > otherString
            Type.MORE_THAN_EQUAL -> instance.asPrimitive(this) >= otherString
            else -> super.compare(instance, interpreter, type, other, trace)
        }
    }

    override fun compare(instance: ClassInstance, interpreter: Interpreter, other: ClassInstance, trace: LocatableTrace): Int {
        val otherString = other.getPrimitive(this) ?: return super.compare(instance, interpreter, other, trace)
        return instance.asPrimitive(this).compareTo(otherString)
    }

    override fun bracketAccess(instance: ClassInstance, interpreter: Interpreter, index: ClassInstance, trace: LocatableTrace): ClassInstance {
        val i = index.getPrimitive(NumberDef::class)?.toInt() ?: runtimeError("Expected number to index string", trace)
        return this.create(instance.asPrimitive(this)[i].toString())
    }

    override fun toString(instance: ClassInstance, interpreter: Interpreter, trace: LocatableTrace): String {
        return instance.asPrimitive(this)
    }

    override fun defineConstructors(): List<ConstructorFunction> {
        return listOf(
            ConstructorFunction.of(1, this::construct)
        )
    }

    @ConstructorDoc(
        desc = [
            "This creates a new string object, not from the string pool, with the given string.",
            "This cannot be called directly, only from child classes"
        ],
        examples = [
            """
            class ChildString: String {
                ChildString(): super("example");
            }
            """
        ]
    )
    private fun construct(arguments: Arguments) {
        val instance = arguments.next()
        val string = arguments.nextPrimitive(StringDef::class)
        instance.setPrimitive(this, string)
    }

    override fun defineMethods(): List<MemberFunction> {
        return listOf(
            MemberFunction.of("toList", this::toList, "Use '<String>.chars()' instead"),
            MemberFunction.of("chars", this::chars),
            MemberFunction.of("length", this::length),
            MemberFunction.of("uppercase", this::uppercase),
            MemberFunction.of("lowercase", this::lowercase),
            MemberFunction.of("capitalize", this::capitalize),
            MemberFunction.of("reverse", this::reverse),
            MemberFunction.of("contains", 1, this::contains),
            MemberFunction.of("startsWith", 1, this::startsWith),
            MemberFunction.of("endsWith", 1, this::endsWith),
            MemberFunction.arb("format", this::format),
            MemberFunction.of("toNumber", this::toNumber),
            MemberFunction.of("strip", this::strip),
            MemberFunction.of("subString", 2, this::subString),
            MemberFunction.of("split", 1, this::split),
            MemberFunction.of("matches", 1, this::matches),
            MemberFunction.of("find", 1, this::find),
            MemberFunction.of("findAll", 1, this::findAll),
            MemberFunction.of("replaceAll", 2, this::replaceAll),
            MemberFunction.of("replaceFirst", 2, this::replaceFirst),
        )
    }

    @FunctionDoc(
        deprecated = ["Use '<String>.chars()' instead"],
        name = "toList",
        desc = ["This makes a list of all the characters in the string"],
        returns = [LIST, "the list of characters"],
        examples = ["'hello'.toList(); // [h, e, l, l, o]"]
    )
    private fun toList(arguments: Arguments): ArucasList {
        return this.chars(arguments)
    }

    @FunctionDoc(
        name = "chars",
        desc = ["This makes a list of all the characters in the string"],
        returns = [LIST, "the list of characters"],
        examples = ["'hello'.chars(); // [h, e, l, l, o]"]
    )
    private fun chars(arguments: Arguments): ArucasList {
        val string = arguments.next().asPrimitive(this)
        val list = ArucasList()
        for (i in string.indices) {
            list[i] = this.create(string[i].toString())
        }
        return list
    }

    @FunctionDoc(
        name = "length",
        desc = ["This returns the length of the string"],
        returns = [NUMBER, "the length of the string"],
        examples = ["'hello'.length(); // 5"]
    )
    private fun length(arguments: Arguments): Int {
        return arguments.nextPrimitive(this).length
    }

    @FunctionDoc(
        name = "uppercase",
        desc = ["This returns the string in uppercase"],
        returns = [STRING, "the string in uppercase"],
        examples = ["'hello'.uppercase(); // 'HELLO'"]
    )
    private fun uppercase(arguments: Arguments): String {
        return arguments.nextPrimitive(this).uppercase(Locale.UK)
    }

    @FunctionDoc(
        name = "lowercase",
        desc = ["This returns the string in lowercase"],
        returns = [STRING, "the string in lowercase"],
        examples = ["'HELLO'.lowercase(); // 'hello'"]
    )
    private fun lowercase(arguments: Arguments): String {
        return arguments.nextPrimitive(this).lowercase(Locale.UK)
    }

    @FunctionDoc(
        name = "capitalize",
        desc = ["This returns the string in capitalized form"],
        returns = [STRING, "the string in capitalized form"],
        examples = ["'hello'.capitalize(); // 'Hello'"]
    )
    private fun capitalize(arguments: Arguments): String {
        return arguments.nextPrimitive(this).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.UK) else it.toString()
        }
    }

    @FunctionDoc(
        name = "reverse",
        desc = ["This returns the string in reverse"],
        returns = [STRING, "the string in reverse"],
        examples = ["'hello'.reverse(); // 'olleh'"]
    )
    private fun reverse(arguments: Arguments): String {
        return arguments.nextPrimitive(this).reversed()
    }

    @FunctionDoc(
        name = "contains",
        desc = ["This returns whether the string contains the given string"],
        params = [STRING, "string", "the string to check"],
        returns = [BOOLEAN, "whether the string contains the given string"],
        examples = ["'hello'.contains('lo'); // true"]
    )
    private fun contains(arguments: Arguments): Boolean {
        val string = arguments.nextPrimitive(this)
        val substring = arguments.nextPrimitive(this)
        return string.contains(substring)
    }

    @FunctionDoc(
        name = "startsWith",
        desc = ["This returns whether the string starts with the given string"],
        params = [STRING, "string", "the string to check"],
        returns = [BOOLEAN, "whether the string starts with the given string"],
        examples = ["'hello'.startsWith('he'); // true"]
    )
    private fun startsWith(arguments: Arguments): Boolean {
        val string = arguments.nextPrimitive(this)
        val substring = arguments.nextPrimitive(this)
        return string.startsWith(substring)
    }

    @FunctionDoc(
        name = "endsWith",
        desc = ["This returns whether the string ends with the given string"],
        params = [STRING, "string", "the string to check"],
        returns = [BOOLEAN, "whether the string ends with the given string"],
        examples = ["'hello'.endsWith('lo'); // true"]
    )
    private fun endsWith(arguments: Arguments): Boolean {
        val string = arguments.nextPrimitive(this)
        val substring = arguments.nextPrimitive(this)
        return string.endsWith(substring)
    }

    @FunctionDoc(
        isVarArgs = true,
        name = "format",
        desc = [
            "This formats the string using the given arguments.",
            "This internally uses the Java String.format() method.",
            "For how to use see here: https://www.javatpoint.com/java-string-format"
        ],
        params = [OBJECT, "objects...", "the objects to insert"],
        returns = [STRING, "the formatted string"],
        examples = ["'%s %s'.format('hello', 'world'); // 'hello world'"]
    )
    private fun format(arguments: Arguments): String {
        val string = arguments.nextPrimitive(this)
        try {
            return string.format(*arguments.getRemaining().map { it.toString(arguments.interpreter) }.toTypedArray())
        } catch (e: IllegalFormatException) {
            runtimeError("Couldn't format string: '$string'", e)
        }
    }

    @FunctionDoc(
        name = "toNumber",
        desc = [
            "This tries to convert the string to a number.",
            "This method can convert hex or denary into numbers.",
            "If the string is not a number, it will throw an error"
        ],
        returns = [NUMBER, "the number"],
        examples = ["'99'.toNumber(); // 99"]
    )
    private fun toNumber(arguments: Arguments): Number {
        val string = arguments.nextPrimitive(this)
        return try {
            StringUtils.parseNumber(string)
        } catch (e: NumberFormatException) {
            runtimeError("Couldn't convert string to number: '$string'", e)
        }
    }

    @FunctionDoc(
        name = "strip",
        desc = ["This strips the whitespace from the string"],
        returns = [STRING, "the stripped string"],
        examples = ["'  hello  '.strip(); // 'hello'"]
    )
    private fun strip(arguments: Arguments): String {
        val string = arguments.nextPrimitive(this)
        return string.trim()
    }

    @FunctionDoc(
        name = "subString",
        desc = ["This returns a substring of the string"],
        params = [
            NUMBER, "from", "the start index (inclusive)",
            NUMBER, "to", "the end index (exclusive)"
        ],
        returns = [STRING, "the substring"],
        examples = ["'hello'.subString(1, 3); // 'el'"]
    )
    private fun subString(arguments: Arguments): String {
        val string = arguments.nextPrimitive(this)
        val from = arguments.nextPrimitive(NumberDef::class).toInt()
        val to = arguments.nextPrimitive(NumberDef::class).toInt()
        return string.substring(from, to)
    }

    @FunctionDoc(
        name = "split",
        desc = ["This splits the string into a list of strings based on a regex"],
        params = [STRING, "regex", "the regex to split the string with"],
        returns = [LIST, "the list of strings"],
        examples = ["'foo/bar/baz'.split('/');"]
    )
    private fun split(arguments: Arguments): List<String> {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        return string.split(this.safeRegex(regex))
    }

    @FunctionDoc(
        name = "matches",
        desc = ["This returns whether the string matches the given regex"],
        params = [STRING, "regex", "the regex to match the string with"],
        returns = [BOOLEAN, "whether the string matches the given regex"],
        examples = ["'foo'.matches('f.*'); // true"]
    )
    private fun matches(arguments: Arguments): Boolean {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        return string.matches(this.safeRegex(regex))
    }

    @FunctionDoc(
        name = "find",
        desc = [
            "This finds all matches of the regex in the string,",
            "this does not find groups, for that use `<String>.findGroups(regex)`"
        ],
        params = [STRING, "regex", "the regex to search the string with"],
        returns = [LIST, "the list of all instances of the regex in the string"],
        examples = ["'102i 1i'.find('([\\\\d+])i'); // ['2i', '1i']"]
    )
    private fun find(arguments: Arguments): ArucasList {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        val list = ArucasList()
        for (match in this.safeRegex(regex).findAll(string)) {
            list.add(this.create(match.value))
        }
        return list
    }

    @FunctionDoc(
        name = "findAll",
        desc = [
            "This finds all matches and groups of a regex in the matches in the string",
            "the first group of each match will be the complete match and following",
            "will be the groups of the regex, a group may be empty if it doesn't exist"
        ],
        params = [STRING, "regex", "the regex to search the string with"],
        returns = [LIST, "the list of lists containg the matches"],
        examples = ["'102i 1i'.find('([\\\\d+])i'); // [['2i', '2', 'i'], ['1i', '1', 'i']]"]
    )
    private fun findAll(arguments: Arguments): ArucasList {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        val list = ArucasList()
        for (match in this.safeRegex(regex).findAll(string)) {
            val subList = ArucasList()
            for (group in match.groupValues) {
                subList.add(this.create(group))
            }
            list.add(arguments.interpreter.create(ListDef::class, subList))
        }
        return list
    }

    @FunctionDoc(
        name = "replaceAll",
        desc = ["This replaces all the instances of a regex with the replace string"],
        params = [
            STRING, "regex", "the regex you want to replace",
            STRING, "replacement", "the string you want to replace it with"
        ],
        returns = [STRING, "the modified string"],
        examples = ["'hello'.replaceAll('l', 'x'); // 'hexxo'"]
    )
    private fun replaceAll(arguments: Arguments): String {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        val replacement = arguments.nextPrimitive(this)
        return string.replace(this.safeRegex(regex), replacement)
    }

    @FunctionDoc(
        name = "replaceFirst",
        desc = ["This replaces the first instance of a regex with the replace string"],
        params = [
            STRING, "regex", "the regex you want to replace",
            STRING, "replacement", "the string you want to replace it with"
        ],
        returns = [STRING, "the modified string"],
        examples = ["'hello'.replaceFirst('l', 'x'); // 'hexlo'"]
    )
    private fun replaceFirst(arguments: Arguments): String {
        val string = arguments.nextPrimitive(this)
        val regex = arguments.nextPrimitive(this)
        val replacement = arguments.nextPrimitive(this)
        return string.replaceFirst(this.safeRegex(regex), replacement)
    }

    private fun safeRegex(string: String): Regex {
        return try {
            Regex(string)
        } catch (e: PatternSyntaxException) {
            runtimeError("Invalid regex: '$string'", e)
        }
    }
}