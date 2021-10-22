package me.senseiwells.arucas.values.functions;

import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.ErrorRuntime;
import me.senseiwells.arucas.throwables.ThrowValue;
import me.senseiwells.arucas.values.Value;

import java.util.List;

public abstract class FunctionValue extends Value<String> {
	public FunctionValue(String name) {
		super(name);
	}
	
	private void checkArguments(Context context, List<Value<?>> arguments, List<String> argumentNames) throws ErrorRuntime {
		int argumentSize = arguments == null ? 0 : arguments.size();
		if (argumentSize > argumentNames.size())
			throw new ErrorRuntime("%s too many arguments passed into %s".formatted(arguments.size() - argumentNames.size(), this.value), this.startPos, this.endPos, context);
		if (argumentSize < argumentNames.size())
			throw new ErrorRuntime("%s too few arguments passed into %s".formatted(argumentNames.size() - argumentSize, this.value), this.startPos, this.endPos, context);
	}

	private void populateArguments(Context context, List<Value<?>> arguments, List<String> argumentNames) {
		for (int i = 0; i < argumentNames.size(); i++) {
			String argumentName = argumentNames.get(i);
			Value<?> argumentValue = arguments.get(i);
			context.setLocal(argumentName, argumentValue);
		}
	}

	public void checkAndPopulateArguments(Context context, List<Value<?>> arguments, List<String> argumentNames) throws ErrorRuntime {
		this.checkArguments(context, arguments, argumentNames);
		this.populateArguments(context, arguments, argumentNames);
	}

	public CodeError throwInvalidParameterError(String details) {
		return new CodeError(CodeError.ErrorType.ILLEGAL_SYNTAX_ERROR, details, this.startPos, this.endPos);
	}

	protected abstract Value<?> execute(Context context, List<Value<?>> arguments) throws CodeError, ThrowValue;
	
	public final Value<?> call(Context context, List<Value<?>> arguments) throws CodeError, ThrowValue {
		context.pushFunctionScope(this.startPos);
		try {
			Value<?> value = this.execute(context, arguments);
			context.popScope();
			return value;
		}
		catch (ThrowValue.Return tv) {
			context.moveScope(context.getSymbolTable().getReturnScope());
			context.popScope();
			return tv.returnValue;
		}
	}
	
	@Override
	public abstract Value<?> copy();

	@Override
	public String toString() {
		return "<function %s>".formatted(this.value);
	}
}