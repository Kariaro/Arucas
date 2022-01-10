package me.senseiwells.arucas.values;

import me.senseiwells.arucas.api.ArucasClassExtension;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.RuntimeError;
import me.senseiwells.arucas.utils.ArucasFunctionMap;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.utils.impl.ArucasValueListCustom;
import me.senseiwells.arucas.values.functions.MemberFunction;

import java.util.List;

public class ListValue extends Value<ArucasValueListCustom> {

	public ListValue(ArucasValueListCustom value) {
		super(value);
	}

	@Override
	public ListValue copy() {
		return new ListValue(this.value);
	}

	@Override
	public ListValue newCopy() {
		return new ListValue(new ArucasValueListCustom(this.value));
	}
	
	@Override
	public int getHashCode(Context context) throws CodeError {
		return this.value.getHashCode(context);
	}
	
	@Override
	public String getStringValue(Context context) throws CodeError {
		return this.value.getStringValue(context);
	}
	
	@Override
	public boolean isEquals(Context context, Value<?> other) throws CodeError {
		if (!(other instanceof ListValue otherList)) {
			return false;
		}
		return this.value.isEquals(context, otherList.value);
	}
	
	public static class ArucasListClass extends ArucasClassExtension {
		public ArucasListClass() {
			super("List");
		}

		@Override
		public Class<ListValue> getValueClass() {
			return ListValue.class;
		}

		@Override
		public ArucasFunctionMap<MemberFunction> getDefinedMethods() {
			return ArucasFunctionMap.of(
				new MemberFunction("getIndex", "index", this::getListIndex, "Use '<List>.get(index)'"),
				new MemberFunction("get", "index", this::getListIndex),
				new MemberFunction("removeIndex", "index", this::removeListIndex, "Use '<List>.remove(index)'"),
				new MemberFunction("remove", "index", this::removeListIndex),
				new MemberFunction("append", "value", this::appendList),
				new MemberFunction("insert", List.of("value", "index"), this::insertList),
				new MemberFunction("concat", "otherList", this::concatList),
				new MemberFunction("contains", "value", this::listContains),
				new MemberFunction("containsAll", "otherList", this::containsAll),
				new MemberFunction("isEmpty", this::isEmpty),
				new MemberFunction("clear", this::clear)
			);
		}

		private synchronized Value<?> getListIndex(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 1);
			int index = numberValue.value.intValue();
			if (index >= thisValue.value.size() || index < 0) {
				throw function.throwInvalidParameterError("Index is out of bounds", context);
			}
			return thisValue.value.get(index);
		}

		private synchronized Value<?> removeListIndex(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			NumberValue numberValue = function.getParameterValueOfType(context, NumberValue.class, 1);
			int index = numberValue.value.intValue();
			if (index >= thisValue.value.size() || index < 0) {
				throw function.throwInvalidParameterError("Index is out of bounds", context);
			}
			return thisValue.value.remove(index);
		}

		private synchronized Value<?> appendList(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			Value<?> value = function.getParameterValue(context, 1);
			thisValue.value.add(value);
			return thisValue;
		}

		private synchronized Value<?> insertList(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			Value<?> value = function.getParameterValue(context, 1);
			int index = function.getParameterValueOfType(context, NumberValue.class, 2).value.intValue();
			int len = thisValue.value.size();
			if (index > len || index < 0) {
				throw new RuntimeError("Index is out of bounds", function.syntaxPosition, context);
			}
			thisValue.value.add(index, value);
			return thisValue;
		}

		private synchronized Value<?> concatList(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			ListValue list2 = function.getParameterValueOfType(context, ListValue.class, 1);
			thisValue.value.addAll(list2.value);
			return thisValue;
		}

		private synchronized BooleanValue listContains(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			Value<?> value = function.getParameterValue(context, 1);
			return BooleanValue.of(thisValue.value.contains(context, value));
		}

		private synchronized BooleanValue containsAll(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			ListValue otherList = function.getParameterValueOfType(context, ListValue.class, 1);
			return BooleanValue.of(thisValue.value.containsAll(context, otherList.value));
		}

		private synchronized BooleanValue isEmpty(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			return BooleanValue.of(thisValue.value.isEmpty());
		}

		private synchronized Value<?> clear(Context context, MemberFunction function) throws CodeError {
			ListValue thisValue = function.getThis(context, ListValue.class);
			thisValue.value.clear();
			return NullValue.NULL;
		}
	}
}
