package me.senseiwells.arucas.values;

import java.util.Objects;

public abstract class Value<T> implements ValueOperations {
	public final T value;
	
	public Value(T value) {
		this.value = value;
	}
	
	// Shallow copy
	public abstract Value<T> copy();

	// Deep copy
	public Value<T> newCopy() {
		return this.copy();
	}

	/**
	 * We only care about comparing the value not the position
	 * So overriding the equals and hashCode methods for maps
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Value<?> otherValue)) {
			return false;
		}
		
		// Object.equals takes null values into perspective.
		return Objects.equals(this.value, otherValue.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
