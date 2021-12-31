package me.senseiwells.arucas.values.functions;

import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.ThrowValue;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.classes.ArucasClassValue;

import java.util.List;

public class ClassMemberFunction extends UserDefinedFunction {
	private final ArucasClassValue thisValue;
	
	public ClassMemberFunction(ArucasClassValue thisValue, String name, List<String> argumentNames, ISyntax syntaxPosition) {
		super(name, argumentNames, syntaxPosition);
		this.thisValue = thisValue;
	}
	
	public ClassMemberFunction(String name, List<String> argumentNames, ISyntax syntaxPosition) {
		this(null, name, argumentNames, syntaxPosition);
	}
	
	public ClassMemberFunction copy(ArucasClassValue value) {
		ClassMemberFunction copy = new ClassMemberFunction(value, this.getName(), this.argumentNames, this.syntaxPosition);
		copy.bodyNode = this.bodyNode;
		return copy;
	}
	
	@Override
	public Value<?> execute(Context context, List<Value<?>> arguments) throws CodeError, ThrowValue {
		// This values is always added as the first parameter.
		arguments.add(0, this.thisValue);
		return super.execute(context, arguments);
	}
	
	@Override
	public String toString() {
		return "<class %s::%s@%x>".formatted(this.thisValue.getName(), this.getName(), this.hashCode());
	}
}
