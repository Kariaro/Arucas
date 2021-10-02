package me.senseiwells.arucas.values;

import me.senseiwells.arucas.throwables.Error;
import me.senseiwells.arucas.throwables.ErrorRuntime;
import me.senseiwells.arucas.core.Run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class BuiltInFunctionValue extends BaseFunctionValue {

    public BuiltInFunctionValue(String name) {
        super(name);
    }

    @Override
    public Value<?> execute(List<Value<?>> arguments) throws Error {
        BuiltInFunction function = BuiltInFunction.stringToFunction(this.value);
        this.context = this.generateNewContext();
        Value<?> returnValue = new NullValue();
        if (function == null)
            throw new ErrorRuntime("Function " + this.value + " is not defined", this.startPos, this.endPos, this.context);
        this.checkAndPopulateArguments(arguments, function.argumentNames, this.context);
        switch (function) {
            case RUN -> this.run();
            case DEBUG -> this.toggleDebug();
            case PRINT -> this.print();
            case SLEEP -> this.sleep();
            case RANDOM -> returnValue = this.random();
            case IS_NUMBER -> returnValue = this.isType(NumberValue.class);
            case IS_STRING -> returnValue = this.isType(StringValue.class);
            case IS_BOOLEAN -> returnValue = this.isType(BooleanValue.class);
            case IS_FUNCTION -> returnValue = this.isType(BaseFunctionValue.class);
            case IS_LIST -> returnValue = this.isType(ListValue.class);
            case GET_INDEX -> returnValue = this.modifyListIndex(false);
            case REMOVE_INDEX -> returnValue = this.modifyListIndex(true);
            case APPEND -> returnValue = this.appendList();
            case CONCAT -> returnValue = this.concatList();
        }
        return returnValue;
    }

    private void run() throws Error {
        Value<?> value = this.context.symbolTable.get(BuiltInFunction.RUN.getArgument(0));
        if (!(value instanceof StringValue stringValue))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass an string (file path) to function", this.startPos, this.endPos);
        String fileName = stringValue.value;
        try {
            String fileContent = Files.readString(Path.of(fileName));
            Run.run(fileName, fileContent);
        }
        catch (IOException | InvalidPathException e) {
            throw new ErrorRuntime("Failed to execute script '" + fileName + "' \n" + e, this.startPos, this.endPos, this.context);
        }
    }

    private void print() {
        System.out.println(this.context.symbolTable.get(BuiltInFunction.PRINT.getArgument(0)));
    }

    private void sleep() throws Error {
        Value<?> numValue = this.context.symbolTable.get(BuiltInFunction.SLEEP.getArgument(0));
        if (!(numValue instanceof NumberValue timeValue))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass an integer into function", this.startPos, this.endPos);
        try {
            Thread.sleep(timeValue.value.longValue());
        }
        catch (InterruptedException e) {
            throw new Error(Error.ErrorType.RUNTIME_ERROR, "An error occurred while trying to call 'sleep()'", this.startPos, this.endPos);
        }
    }

    private NumberValue random() throws Error {
        Value<?> numValue = this.context.symbolTable.get(BuiltInFunction.RANDOM.getArgument(0));
        if (!(numValue instanceof NumberValue bound))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass an integer (bound) into function", this.startPos, this.endPos);
        return new NumberValue(new Random().nextInt(bound.value.intValue()));
    }

    private BooleanValue isType(Class<?> classInstance) {
        return new BooleanValue(classInstance.isInstance(this.context.symbolTable.get(BuiltInFunction.IS_STRING.getArgument(0))));
    }

    private Value<?> modifyListIndex(boolean delete) throws Error {
        Value<?> value = this.context.symbolTable.get(BuiltInFunction.GET_INDEX.getArgument(0));
        Value<?> numValue = this.context.symbolTable.get(BuiltInFunction.GET_INDEX.getArgument(1));
        if (!(value instanceof ListValue listValue))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass a list into parameter 1", this.startPos, this.endPos);
        if (!(numValue instanceof NumberValue numberValue))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass an integer into parameter 2", this.startPos, this.endPos);
        int index = numberValue.value.intValue();
        if (index >= listValue.value.size() || index < 0)
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Parameter 2 is out of bounds", this.startPos, this.endPos);
        return delete ? listValue.value.remove(index) : listValue.value.get(index);
    }

    private Value<?> appendList() throws Error {
        Value<?> listValue = this.context.symbolTable.get(BuiltInFunction.APPEND.getArgument(0));
        Value<?> value = this.context.symbolTable.get(BuiltInFunction.APPEND.getArgument(1));
        if (!(listValue instanceof ListValue list))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Must pass a list into parameter 1", this.startPos, this.endPos);
        list.value.add(value);
        return list;
    }

    private Value<?> concatList() throws Error {
        Value<?> list1 = this.context.symbolTable.get(BuiltInFunction.CONCAT.getArgument(0));
        Value<?> list2 = this.context.symbolTable.get(BuiltInFunction.CONCAT.getArgument(1));
        if (!(list1 instanceof ListValue listValue1) || !(list2 instanceof ListValue listValue2))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Parameters must both be lists", this.startPos, this.endPos);
        listValue1.value.addAll(listValue2.value);
        return listValue1;
    }

    private void toggleDebug() throws Error {
        Value<?> value = this.context.symbolTable.get(BuiltInFunction.DEBUG.getArgument(0));
        if (!(value instanceof BooleanValue booleanValue))
            throw new Error(Error.ErrorType.ILLEGAL_SYNTAX_ERROR, "Cannot pass " + value.value + "to debug()", this.startPos, this.endPos);
        Run.debug = booleanValue.value;
    }

    @Override
    public Value<?> copy() {
        return new BuiltInFunctionValue(this.value).setPos(this.startPos, this.endPos).setContext(this.context);
    }

    public enum BuiltInFunction {
        //general functions
        RUN("run", List.of("path")),
        DEBUG("debug", List.of("boolean")),
        PRINT("print", List.of("printValue")),
        SLEEP("sleep", List.of("time")),
        RANDOM("random", List.of("bound")),
        IS_NUMBER("isNumber", List.of("value")),
        IS_STRING("isString", List.of("value")),
        IS_BOOLEAN("isBoolean", List.of("value")),
        IS_FUNCTION("isFunction", List.of("value")),
        IS_LIST("isList", List.of("value")),

        //list functions
        GET_INDEX("getIndex", List.of("list", "index")),
        REMOVE_INDEX("removeIndex", List.of("list", "index")),
        APPEND("append", List.of("list", "value")),
        CONCAT("concat", List.of("list", "otherList"));

        public String name;
        List<String> argumentNames;

        BuiltInFunction(String name, List<String> argumentNames) {
            this.name = name;
            this.argumentNames = argumentNames;
        }

        public static BuiltInFunction stringToFunction(String word) {
            for (BuiltInFunction value : BuiltInFunction.values()) {
                if (word.equals(value.name))
                    return value;
            }
            return null;
        }

        private String getArgument(int index) {
            return this.argumentNames.get(index);
        }
    }
}