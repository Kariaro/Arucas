package me.senseiwells.arucas.values;

import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.RuntimeError;
import me.senseiwells.arucas.tokens.Token;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberValue extends Value<Double> {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.############", DecimalFormatSymbols.getInstance(Locale.US));
	
	public NumberValue(double value) {
		super(value);
	}

	@Override
	public NumberValue addTo(Value<?> other, ISyntax syntaxPosition) throws CodeError {
		if (!(other instanceof NumberValue otherValue)) {
			throw new RuntimeError("The 'add' operator cannot be applied to %s and %s".formatted(this, other), syntaxPosition);
		}
		
		return new NumberValue(this.value + otherValue.value);
	}

	public NumberValue subtractBy(NumberValue other) {
		return new NumberValue(this.value - other.value);
	}

	public NumberValue multiplyBy(NumberValue other) {
		return new NumberValue(this.value * other.value);
	}

	public NumberValue divideBy(NumberValue other, ISyntax syntaxPosition) throws RuntimeError {
		if (other.value == 0) {
			throw new RuntimeError("You cannot divide by 0", syntaxPosition);
		}
		
		return new NumberValue(this.value / other.value);
	}

	public NumberValue powerBy(NumberValue other, ISyntax syntaxPosition) throws RuntimeError {
		if (this.value < 0 || (other.value % 1) != 0) {
			throw new RuntimeError("You cannot calculate imaginary numbers", syntaxPosition);
		}
		
		return new NumberValue(Math.pow(this.value, other.value));
	}

	public BooleanValue compareNumber(NumberValue other, Token.Type type) {
		boolean bool = switch (type) {
			case LESS_THAN -> this.value < other.value;
			case MORE_THAN -> this.value > other.value;
			case MORE_THAN_EQUAL -> this.value >= other.value;
			case LESS_THAN_EQUAL -> this.value <= other.value;
			default -> false;
		};
		return new BooleanValue(bool);
	}

	@Override
	public NumberValue copy() {
		return this;
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}
	
	@Override
	public String toString() {
		return DECIMAL_FORMAT.format(this.value);
	}
}
