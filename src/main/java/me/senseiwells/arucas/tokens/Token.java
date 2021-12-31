package me.senseiwells.arucas.tokens;

import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.utils.Position;

import java.util.Set;

public class Token {
	public final Type type;
	public final String content;
	public final ISyntax syntaxPosition;
	
	public Token(Type type, String content, Position startPos, Position endPos) {
		this.type = type;
		this.content = content;
		this.syntaxPosition = ISyntax.of(startPos, endPos);
	}
	
	public Token(Type type, String content, ISyntax syntaxPosition) {
		this(type, content, syntaxPosition.getStartPos(), syntaxPosition.getEndPos());
	}
	
	public Token(Type type, ISyntax startPos, ISyntax endPos) {
		this(type, "", startPos.getStartPos(), endPos.getEndPos());
	}
	
	public Token(Type type, ISyntax syntaxPosition) {
		this(type, "", syntaxPosition.getStartPos(), syntaxPosition.getEndPos());
	}
	
	@Override
	public String toString() {
		return "Token{type=%s, content='%s'}".formatted(this.type, this.content);
	}
	
	public enum Type {
		// Delimiters
		WHITESPACE,
		SEMICOLON,
		COLON,
		IDENTIFIER,
		COMMA,
		FINISH,
		
		// Atoms
		NUMBER,
		BOOLEAN,
		STRING,
		NULL,
		LIST,
		MAP,
		SCOPE,
		
		// Arithmetics
		PLUS,
		MINUS,
		MULTIPLY,
		DIVIDE,
		POWER,
		
		// Boolean operators
		NOT,
		AND,
		OR,
		
		// Brackets
		LEFT_BRACKET,
		RIGHT_BRACKET,
		LEFT_SQUARE_BRACKET,
		RIGHT_SQUARE_BRACKET,
		LEFT_CURLY_BRACKET,
		RIGHT_CURLY_BRACKET,
		
		// Memory Operator
		ASSIGN_OPERATOR,
		INCREMENT,
		DECREMENT,
		
		// Comparisons
		EQUALS,
		NOT_EQUALS,
		LESS_THAN,
		MORE_THAN,
		LESS_THAN_EQUAL,
		MORE_THAN_EQUAL,
		
		// Statements
		IF,
		WHILE,
		ELSE,
		CONTINUE,
		BREAK,
		VAR,
		RETURN,
		FUN,
		TRY,
		CATCH,
		FOREACH,
		SWITCH,
		CASE,
		DEFAULT,
		CLASS,
		THIS,
		NEW,
		STATIC,

		// Dot
		DOT,
		POINTER
		;

		public static Set<Type> comparisonTokens = Set.of(
			EQUALS,
			NOT_EQUALS,
			LESS_THAN,
			MORE_THAN,
			LESS_THAN_EQUAL,
			MORE_THAN_EQUAL
		);
	}
}
