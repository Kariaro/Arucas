package me.senseiwells.arucas.values.classes;

import me.senseiwells.arucas.nodes.Node;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.ThrowValue;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.functions.ClassMemberFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArucasClassDefinition {
	// TODO: A class definition needs to only have one static instance of itself
	private final String name;
	private final List<ClassMemberFunction> constructors;
	private final List<ClassMemberFunction> methods;
	private final List<ClassMemberFunction> staticMethods;
	private final Map<String, Node> memberVariables;
	private final Map<String, Node> staticMemberVariables;
	
	public ArucasClassDefinition(String name) {
		this.name = name;
		this.staticMethods = new ArrayList<>();
		this.constructors = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.memberVariables = new HashMap<>();
		this.staticMemberVariables = new HashMap<>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addConstructor(Object obj) {
	
	}
	
	public void addMethod(boolean isStatic, ClassMemberFunction method) {
		if (isStatic) {
			this.staticMethods.add(method);
		}
		else {
			this.methods.add(method);
		}
	}
	
	public void addMemberVariable(boolean isStatic, String name, Node value) {
		if (isStatic) {
			this.staticMemberVariables.put(name, value);
		}
		else {
			this.memberVariables.put(name, value);
		}
	}
	
	public ArucasClassValue createNewDefinition(Context context, List<Value<?>> parameters) throws CodeError, ThrowValue {
		ArucasClassValue value = new ArucasClassValue(this);
		// Add methods
		for (ClassMemberFunction function : this.methods) {
			value.addMethod(function.copy(value));
		}
		
		// Add member variables
		for (Map.Entry<String, Node> entry : this.memberVariables.entrySet()) {
			value.addMemberVariable(entry.getKey(), entry.getValue().visit(context));
		}
		
		// TODO: Find constructor with the correct amount of parameters
		
		
		return value;
	}
}
