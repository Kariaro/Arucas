package me.senseiwells.arucas.values.functions;

import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.RuntimeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.NullValue;
import me.senseiwells.arucas.values.Value;

import java.util.List;

public abstract class AbstractBuiltInFunction<T extends AbstractBuiltInFunction<?>> extends FunctionValue {
	public final FunctionDefinition<T> function;

	private int parameterIndex;

	public AbstractBuiltInFunction(String name, List<String> argumentNames, FunctionDefinition<T> function, String isDeprecated) {
		super(name, ISyntax.emptyOf("Arucas/" + name), argumentNames, isDeprecated);
		this.function = function;
	}

	public Value<?> getParameterValue(Context context, int index) {
		Value<?> param = context.getVariable(this.argumentNames.get(index));
		return param == null ? NullValue.NULL : param;
	}

	public void checkDeprecated(Context context) {
		if (this.deprecatedMessage != null && !context.isSuppressDeprecated()) {
			context.printDeprecated("The function %s() is deprecated and will be removed in the future! %s".formatted(this.value, this.deprecatedMessage));
		}
	}

	public <E extends Value<?>> E getFirstParameter(Context context, Class<E> clazz) throws CodeError {
		if (this.getParameterCount() == 0) {
			throw new RuntimeError("Function doesn't have parameters", this.syntaxPosition, context);
		}
		this.parameterIndex = 1;
		return this.getParameterValueOfType(context, clazz, 0);
	}

	public <E extends Value<E>> E getNextParameter(Context context, Class<E> clazz) throws CodeError {
		if (this.parameterIndex >= this.getParameterCount()) {
			throw new RuntimeError("Next parameter is out of bounds", this.syntaxPosition, context);
		}
		E value = this.getParameterValueOfType(context, clazz, this.parameterIndex);
		this.parameterIndex++;
		return value;
	}

	public boolean hasNext() {
		return this.parameterIndex < this.getParameterCount();
	}

	public <E extends Value<?>> E getParameterValueOfType(Context context, Class<E> clazz, int index) throws CodeError {
		return this.getParameterValueOfType(context, clazz, index, null);
	}

	public <E extends Value<?>> E getParameterValueOfType(Context context, Class<E> clazz, int index, String additionalInfo) throws CodeError {
		Value<?> value = this.getParameterValue(context, index);
		if (!clazz.isInstance(value)) {
			throw this.throwInvalidParameterError("Must pass %s into parameter %d for %s()%s".formatted(
				clazz.getSimpleName(), index + 1, this.value, additionalInfo == null ? "" : ("\n" + additionalInfo)
			), context);
		}
		return clazz.cast(value);
	}
}
