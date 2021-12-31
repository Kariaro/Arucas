package me.senseiwells.arucas.utils;

import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.classes.ArucasClassDefinition;

import java.util.*;

public class StackTable {
	protected final Map<String, Value<?>> symbolMap;
	protected final Map<String, ArucasClassDefinition> classDefinitions;
	private final StackTable parentTable;
	private final ISyntax syntaxPosition;
	
	protected final boolean canContinue;
	protected final boolean canBreak;
	protected final boolean canReturn;
	
	public StackTable(StackTable parent, ISyntax syntaxPosition, boolean canBreak, boolean canContinue, boolean canReturn) {
		this.symbolMap = new HashMap<>();
		this.classDefinitions = new HashMap<>();
		this.parentTable = parent;
		this.syntaxPosition = syntaxPosition;
		this.canContinue = canContinue;
		this.canReturn = canReturn;
		this.canBreak = canBreak;
	}

	public StackTable() {
		this(null, ISyntax.empty(), false, false, false);
	}
	
	/**
	 * Returns the scope position that created this table.
	 */
	public final ISyntax getPosition() {
		return this.syntaxPosition;
	}
	
	/**
	 * Returns the value of the variable name.
	 */
	public Value<?> get(String name) {
		Value<?> value = this.symbolMap.get(name);
		if (value != null) {
			return value;
		}
		
		if (this.parentTable != null) {
			return this.parentTable.get(name);
		}
		
		return null;
	}
	
	/**
	 * Change the value of a variable called name.
	 */
	public void set(String name, Value<?> value) {
		StackTable parentTable = this.getParent(name);
		if (parentTable != null) {
			parentTable.symbolMap.put(name, value);
		}
		else {
			this.symbolMap.put(name, value);
		}
	}
	
	/**
	 * Change the value of a local variable called name.
	 */
	public void setLocal(String name, Value<?> value) {
		this.symbolMap.put(name, value);
	}
	
	/**
	 * Returns the first parent that contains the value name.
	 */
	public StackTable getParent(String name) {
		if (this.parentTable != null) {
			if (this.parentTable.symbolMap.get(name) != null) {
				return this.parentTable;
			}
			else {
				return this.parentTable.getParent(name);
			}
		}
		
		return null;
	}
	
	public ArucasClassDefinition getClassDefinition(String name) {
		ArucasClassDefinition definition = this.classDefinitions.get(name);
		if (definition != null) {
			return definition;
		}
		
		return this.parentTable != null ? this.parentTable.getClassDefinition(name) : null;
	}
	
	public void addClassDefinition(ArucasClassDefinition definition) {
		this.classDefinitions.put(definition.getName(), definition);
	}
	
	/**
	 * Returns the root table.
	 */
	public StackTable getRoot() {
		return this.parentTable != null ? this.parentTable.getRoot():this;
	}
	
	public StackTable getParentTable() {
		return this.parentTable;
	}
	
	public StackTable getReturnScope() {
		return this.canReturn ? this : this.parentTable != null ? this.parentTable.getReturnScope() : null;
	}
	
	public StackTable getBreakScope() {
		return this.canBreak ? this : this.parentTable != null ? this.parentTable.getBreakScope() : null;
	}
	
	public StackTable getContinueScope() {
		return this.canContinue ? this : this.parentTable != null ? this.parentTable.getContinueScope() : null;
	}
	
	public Iterator<StackTable> iterator() {
		return new Iterator<>() {
			private StackTable object = StackTable.this;
			
			@Override
			public boolean hasNext() {
				return this.object != null;
			}
			
			@Override
			public StackTable next() {
				StackTable current = this.object;
				this.object = this.object.parentTable;
				return current;
			}
		};
	}
	
	@Override
	public String toString() {
		return "%s%s".formatted(this.parentTable == null ? "RootTable":"StackTable", this.symbolMap);
	}
}
