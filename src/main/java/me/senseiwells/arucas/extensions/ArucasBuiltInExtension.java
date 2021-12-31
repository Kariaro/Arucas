package me.senseiwells.arucas.extensions;

import me.senseiwells.arucas.api.IArucasExtension;
import me.senseiwells.arucas.core.Run;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.RuntimeError;
import me.senseiwells.arucas.throwables.ThrowStop;
import me.senseiwells.arucas.throwables.ThrowValue;
import me.senseiwells.arucas.utils.ArucasValueList;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.utils.ExceptionUtils;
import me.senseiwells.arucas.values.*;
import me.senseiwells.arucas.values.functions.BuiltInFunction;
import me.senseiwells.arucas.values.functions.FunctionValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ArucasBuiltInExtension implements IArucasExtension {
	private final Scanner scanner = new Scanner(System.in);
	private final Random random = new Random();
	
	@Override
	public String getName() {
		return "BuiltInExtension";
	}
	
	@Override
	public Set<BuiltInFunction> getDefinedFunctions() {
		return this.builtInFunctions;
	}

	private final Set<BuiltInFunction> builtInFunctions = Set.of(
		new BuiltInFunction("run", "path", this::run),
		new BuiltInFunction("stop", this::stop),
		new BuiltInFunction("sleep", "milliseconds", this::sleep),
		new BuiltInFunction("print", "printValue", this::print),
		new BuiltInFunction("input", "prompt", this::input),
		new BuiltInFunction("debug", "boolean", this::debug),
		new BuiltInFunction("suppressDeprecated", "boolean", this::suppressDeprecated),
		new BuiltInFunction("random", "bound", this::random),
		new BuiltInFunction("getTime", (context, function) -> new StringValue(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now()))),
		new BuiltInFunction("getDirectory", (context, function) -> new StringValue(System.getProperty("user.dir"))),
		new BuiltInFunction("len", "value", this::len),
		new BuiltInFunction("runThreaded", List.of("function", "parameters"), this::runThreaded),
		new BuiltInFunction("readFile", "path", this::readFile),
		new BuiltInFunction("writeFile", List.of("path", "string"), this::writeFile),
		new BuiltInFunction("createDirectory", "path", this::createDirectory),
		new BuiltInFunction("doesFileExist", "path", this::doesFileExist),
		new BuiltInFunction("throwRuntimeError", "message", this::throwRuntimeError),
		new BuiltInFunction("callFunctionWithList", List.of("function", "argList"), this::callFunctionWithList),
		new BuiltInFunction("runFromString", "string", this::runFromString),

		// Math functions
		new BuiltInFunction("sin", "value", this::sin),
		new BuiltInFunction("cos", "value", this::cos),
		new BuiltInFunction("tan", "value", this::tan),
		new BuiltInFunction("arcsin", "value", this::arcsin),
		new BuiltInFunction("arccos", "value", this::arccos),
		new BuiltInFunction("arctan", "value", this::arctan),
		new BuiltInFunction("cosec", "value", this::cosec),
		new BuiltInFunction("sec", "value", this::sec),
		new BuiltInFunction("cot", "value", this::cot)
	);

	private Value<?> run(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		String filePath = new File(stringValue.value).getAbsolutePath();
		try {
			Context childContext = context.createChildContext(filePath);
			String fileContent = Files.readString(Path.of(filePath));
			return Run.run(childContext, filePath, fileContent);
		}
		catch (IOException | OutOfMemoryError | InvalidPathException e) {
			throw new RuntimeError("Failed to execute script '%s' \n%s".formatted(filePath, e), function.syntaxPosition, context);
		}
	}

	private Value<?> stop(Context context, BuiltInFunction function) throws CodeError {
		throw new ThrowStop();
	}

	private Value<?> sleep(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		try {
			Thread.sleep(numberValue.value.longValue());
		}
		catch (InterruptedException e) {
			throw new CodeError(CodeError.ErrorType.INTERRUPTED_ERROR, "", function.syntaxPosition);
		}
		return new NullValue();
	}

	private Value<?> print(Context context, BuiltInFunction function) {
		context.getOutput().println(function.getParameterValue(context, 0));
		return new NullValue();
	}

	private synchronized Value<?> input(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		context.getOutput().println(stringValue.value);
		return new StringValue(this.scanner.nextLine());
	}

	private Value<?> debug(Context context, BuiltInFunction function) throws CodeError {
		context.setDebug(function.getParameterValueOfType(context, BooleanValue.class, 0).value);
		return new NullValue();
	}

	private Value<?> suppressDeprecated(Context context, BuiltInFunction function) throws CodeError {
		context.setSuppressDeprecated(function.getParameterValueOfType(context, BooleanValue.class, 0).value);
		return new NullValue();
	}

	private Value<?> random(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(this.random.nextInt(numValue.value.intValue()));
	}

	private Value<?> len(Context context, BuiltInFunction function) throws CodeError {
		Value<?> value = function.getParameterValue(context, 0);
		if (value instanceof ListValue listValue) {
			return new NumberValue(listValue.value.size());
		}
		if (value instanceof StringValue stringValue) {
			return new NumberValue(stringValue.value.length());
		}
		if (value instanceof MapValue mapValue) {
			return new NumberValue(mapValue.value.size());
		}
		throw new RuntimeError("Cannot pass %s into len()".formatted(value), function.syntaxPosition, context);
	}

	// This should be overwritten if you are implementing the language
	private Value<?> runThreaded(Context context, BuiltInFunction function) throws CodeError {
		FunctionValue functionValue = function.getParameterValueOfType(context, FunctionValue.class, 0);
		ArucasValueList list = function.getParameterValueOfType(context, ListValue.class, 1).value;
		Context functionContext = context.createBranch();

		Thread thread = new Thread(() -> {
			try {
				functionValue.call(functionContext, list);
			}
			catch (CodeError e) {
				context.getOutput().printf("An error occurred in a separate thread: %s\n", e.toString(functionContext));
			}
			catch (ThrowValue ignored) { }
		});
		thread.setDaemon(true);
		thread.start();
		return new NullValue();
	}

	private Value<?> readFile(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		
		try {
			String fileContent = Files.readString(Path.of(stringValue.value));
			return new StringValue(fileContent);
		}
		catch (OutOfMemoryError e) {
			throw new RuntimeError("Out of Memory - The file you are trying to read is too large", function.syntaxPosition);
		}
		catch (InvalidPathException e) {
			throw new RuntimeError(e.getMessage(), function.syntaxPosition);
		}
		catch (IOException e) {
			throw new RuntimeError(
				"There was an error reading the file: \"%s\"\n%s".formatted(stringValue.value, ExceptionUtils.getStackTrace(e)),
				function.syntaxPosition,
				context
			);
		}
	}

	private Value<?> writeFile(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		StringValue writeValue = function.getParameterValueOfType(context, StringValue.class, 1);
		String filePath = stringValue.value;

		try (PrintWriter printWriter = new PrintWriter(filePath)) {
			printWriter.println(writeValue.value);
			return new NullValue();
		}
		catch (FileNotFoundException e) {
			throw new RuntimeError(
				"There was an error writing the file: \"%s\"\n%s".formatted(stringValue.value, ExceptionUtils.getStackTrace(e)),
				function.syntaxPosition,
				context
			);
		}
	}

	private Value<?> createDirectory(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		try {
			return new BooleanValue(Path.of(stringValue.value).toFile().mkdirs());
		}
		catch (InvalidPathException e) {
			throw new RuntimeError(e.getMessage(), function.syntaxPosition);
		}
	}

	private Value<?> doesFileExist(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		try {
			return new BooleanValue(Path.of(stringValue.value).toFile().exists());
		}
		catch (InvalidPathException e) {
			throw new RuntimeError(e.getMessage(), function.syntaxPosition);
		}
	}

	private Value<?> throwRuntimeError(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		throw new RuntimeError(stringValue.value, function.syntaxPosition, context);
	}

	private Value<?> callFunctionWithList(Context context, BuiltInFunction function) throws CodeError {
		FunctionValue functionValue = function.getParameterValueOfType(context, FunctionValue.class, 0);
		ArucasValueList listValue = function.getParameterValueOfType(context, ListValue.class, 1).value;
		try {
			return functionValue.call(context, listValue);
		}
		catch (ThrowValue throwValue) {
			throw new CodeError(CodeError.ErrorType.ILLEGAL_OPERATION_ERROR, "Cannot break or continue in a function", function.syntaxPosition);
		}
	}

	private Value<?> runFromString(Context context, BuiltInFunction function) throws CodeError {
		StringValue stringValue = function.getParameterValueOfType(context, StringValue.class, 0);
		return Run.run(context, "string-run", stringValue.value);
	}

	private Value<?> sin(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.sin(numberValue.value));
	}

	private Value<?> cos(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.cos(numberValue.value));
	}

	private Value<?> tan(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.tan(numberValue.value));
	}

	private Value<?> arcsin(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.asin(numberValue.value));
	}

	private Value<?> arccos(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.acos(numberValue.value));
	}

	private Value<?> arctan(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(Math.atan(numberValue.value));
	}

	private Value<?> cosec(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(1 / Math.sin(numberValue.value));
	}

	private Value<?> sec(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(1 / Math.cos(numberValue.value));
	}

	private Value<?> cot(Context context, BuiltInFunction function) throws CodeError {
		NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 0);
		return new NumberValue(1 / Math.tan(numberValue.value));
	}
}
