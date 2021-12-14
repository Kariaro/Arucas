package me.senseiwells.arucas.values.classes;

import me.senseiwells.arucas.utils.StackTable;
import me.senseiwells.arucas.values.MemberValue;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.functions.ClassMemberFunction;

import java.util.ArrayList;
import java.util.List;

public class ArucasClassValue extends MemberValue<ArucasClassDefinition> {
	private final List<ClassMemberFunction> methods;
	private final StackTable members;
	
	public ArucasClassValue(ArucasClassDefinition arucasClass) {
		super(arucasClass);
		this.members = new StackTable();
		this.methods = new ArrayList<>();
	}
	
	public String getName() {
		return this.value.getName();
	}
	
	protected void addMethod(ClassMemberFunction method) {
		this.methods.add(method);
	}
	
	protected void addMemberVariable(String name, Value<?> value) {
		this.members.setLocal(name, value);
	}
	
	private ClassMemberFunction getDelegate(String name) {
		ClassMemberFunction memberFunction = null;
		for (ClassMemberFunction method : this.methods) {
			if (method.getName().equals(name)) {
				// We can only delegate methods that are not overloaded
				if (memberFunction != null) {
					return null;
				}
				
				memberFunction = method;
			}
		}
		
		return memberFunction;
	}
	
	@Override
	public boolean hasMember(String name) {
		return this.getMember(name) != null;
	}
	
	@Override
	public Value<?> getMember(String name) {
		Value<?> member = this.members.get(name);
		if (member == null) {
			return this.getDelegate(name);
		}
		
		return member;
	}
	
	@Override
	public boolean hasMember(String name, int parameters) {
		return this.getMember(name, parameters) != null;
	}
	
	@Override
	public ClassMemberFunction getMember(String name, int parameters) {
		for (ClassMemberFunction method : this.methods) {
			if (method.getName().equals(name) && method.getParameterCount() == parameters) {
				return method;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isAssignable(String name) {
		// Only member variables are modifiable
		return this.members.get(name) != null;
	}
	
	@Override
	public boolean setMember(String name, Value<?> value) {
		if (!this.isAssignable(name)) {
			return false;
		}
		
		this.members.set(name, value);
		return true;
	}
	
	@Override
	public ArucasClassValue copy() {
		// You should not be able to
		return this;
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}
	
	@Override
	public String toString() {
		// If 'toString' is overwritten we should return that value here
		return "<class %s@%x>".formatted(this.getName(), this.hashCode());
	}
}
