package me.senseiwells.arucas.values;

public abstract class MemberValue<T> extends Value<T> {

	public MemberValue(T value) {
		super(value);
	}
	
	/**
	 * Returns if this object contains the specified member
	 */
	public abstract boolean hasMember(String name);
	
	/**
	 * Returns if this object contains the member with the specified parameters
	 */
	public abstract boolean hasMember(String name, int parameters);
	
	/**
	 * Returns if the specified member is allowed to change value
	 */
	public abstract boolean isAssignable(String name);
	
	/**
	 * Change the value of a member inside this object
	 */
	public abstract boolean setMember(String name, Value<?> value);
	
	/**
	 * Returns a member of this object
	 */
	public abstract Value<?> getMember(String name);
	
	/**
	 * Returns a member of this object with the specified amount of parameters
	 */
	public abstract Value<?> getMember(String name, int parameters);
	
	@Override
	public Value<T> copy() {
		return this;
	}

	@Override
	public String toString() {
		return "<%s@%x>".formatted(this.getClass().getSimpleName(), this.hashCode());
	}
}
