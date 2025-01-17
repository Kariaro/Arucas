package me.senseiwells.arucas.extensions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import me.senseiwells.arucas.api.docs.ClassDoc
import me.senseiwells.arucas.api.docs.FunctionDoc
import me.senseiwells.arucas.builtin.FileDef
import me.senseiwells.arucas.builtin.StringDef
import me.senseiwells.arucas.classes.ClassInstance
import me.senseiwells.arucas.classes.CreatableDefinition
import me.senseiwells.arucas.core.Interpreter
import me.senseiwells.arucas.exceptions.runtimeError
import me.senseiwells.arucas.utils.Arguments
import me.senseiwells.arucas.utils.BuiltInFunction
import me.senseiwells.arucas.utils.MemberFunction
import me.senseiwells.arucas.utils.Util
import me.senseiwells.arucas.utils.Util.Types.FILE
import me.senseiwells.arucas.utils.Util.Types.JSON
import me.senseiwells.arucas.utils.Util.Types.LIST
import me.senseiwells.arucas.utils.Util.Types.MAP
import me.senseiwells.arucas.utils.Util.Types.OBJECT
import me.senseiwells.arucas.utils.Util.Types.STRING
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files

@ClassDoc(
    name = JSON,
    desc = [
        "This class allows you to create and manipulate JSON objects.",
        "This class cannot be instantiated or extended"
    ],
    importPath = "util.Json"
)
class JsonDef(interpreter: Interpreter): CreatableDefinition<JsonElement>(JSON, interpreter) {
    private companion object {
        val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create()
    }

    override fun canExtend() = false

    override fun defineStaticMethods(): List<BuiltInFunction> {
        return listOf(
            BuiltInFunction.of("fromString", 1, this::fromString),
            BuiltInFunction.of("fromList", 1, this::fromList),
            BuiltInFunction.of("fromMap", 1, this::fromMap),
            BuiltInFunction.of("fromFile", 1, this::fromFile)
        )
    }

    @FunctionDoc(
        isStatic = true,
        name = "fromString",
        desc = [
            "This converts a string into a Json provided it is formatted correctly,",
            "otherwise throwing an error"
        ],
        params = [STRING, "string", "the string that you want to parse into a Json"],
        returns = [JSON, "the Json parsed from the string"],
        examples = ["Json.fromString('{\"key\":\"value\"}');"]
    )
    private fun fromString(arguments: Arguments): ClassInstance {
        val string = arguments.nextPrimitive(StringDef::class)
        return try {
            this.create(GSON.fromJson(string, JsonElement::class.java))
        } catch (e: JsonSyntaxException) {
            runtimeError("Json could not be parsed", e)
        }
    }

    @FunctionDoc(
        isStatic = true,
        name = "fromList",
        desc = [
            "This converts a list into a Json, an important thing to note is that",
            "any values that are not Numbers, Booleans, Lists, Maps, or Null will use their",
            "toString() member to convert them to a string"
        ],
        params = [LIST, "list", "the list that you want to parse into a Json"],
        returns = [JSON, "the Json parsed from the list"],
        examples = ["Json.fromList(['value', 1, true]);"]
    )
    private fun fromList(arguments: Arguments): ClassInstance {
        val list = arguments.nextList()
        return this.create(Util.Json.fromInstance(arguments.interpreter, list, 100))
    }

    @FunctionDoc(
        isStatic = true,
        name = "fromFile",
        desc = ["This will read a file and parse it into a Json"],
        params = [FILE, "file", "the file that you want to parse into a Json"],
        returns = [JSON, "the Json parsed from the file"],
        examples = ["Json.fromFile(new File('this/path/is/an/example.json'));"]
    )
    private fun fromFile(arguments: Arguments): ClassInstance {
        val file = arguments.nextPrimitive(FileDef::class)
        try {
            val content = Files.readString(file.toPath())
            return this.create(GSON.fromJson(content, JsonElement::class.java))
        } catch (e: IOException) {
            runtimeError("Could not read file '$file'", e)
        }
    }

    @FunctionDoc(
        isStatic = true,
        name = "fromMap",
        desc = [
            "This converts a map into a Json, an important thing to note is that",
            "any values that are not Numbers, Booleans, Lists, Maps, or Null will use their",
            "toString() member to convert them to a string"
        ],
        params = [MAP, "map", "the map that you want to parse into a Json"],
        returns = [JSON, "the Json parsed from the map"],
        examples = ["Json.fromMap({'key': ['value1', 'value2']});"]
    )
    private fun fromMap(arguments: Arguments): ClassInstance {
        val map = arguments.nextMap()
        return this.create(Util.Json.fromInstance(arguments.interpreter, map, 100))
    }

    override fun defineMethods(): List<MemberFunction> {
        return listOf(
            MemberFunction.of("getValue", this::getValue),
            MemberFunction.of("writeToFile", 1, this::writeToFile)
        )
    }

    @FunctionDoc(
        name = "getValue",
        desc = ["This converts the Json back into an object"],
        returns = [OBJECT, "the Value parsed from the Json"],
        examples = ["json.getValue();"]
    )
    private fun getValue(arguments: Arguments): ClassInstance {
        val instance = arguments.nextPrimitive(this)
        return Util.Json.toInstance(arguments.interpreter, instance)
    }

    @FunctionDoc(
        name = "writeToFile",
        desc = [
            "This writes the Json to a file",
            "if the file given is a directory or cannot be",
            "written to, an error will be thrown"
        ],
        params = [FILE, "file", "the file that you want to write to"],
        examples = ["json.writeToFile(new File('D:/cool/realDirectory'));"]
    )
    private fun writeToFile(arguments: Arguments) {
        val instance = arguments.nextPrimitive(this)
        val file = arguments.nextPrimitive(FileDef::class)
        try {
            file.writeText(GSON.toJson(instance))
        } catch (e: FileNotFoundException) {
            runtimeError("Could not write to file '$file'", e)
        }
    }
}