package com.texteditor.model;

public class Token {

    public enum Type {
        KEYWORD, STRING, COMMENT, NUMBER, ANNOTATION, IDENTIFIER, OTHER
    }

    public final Type   type;
    public final String value;
    public final int    start;
    public final int    end;

    public Token(Type type, String value, int start, int end) {
        this.type  = type;
        this.value = value;
        this.start = start;
        this.end   = end;
    }
}
