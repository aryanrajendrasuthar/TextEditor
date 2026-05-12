package com.texteditor.model;

public enum Language {
    JAVA, PYTHON, JAVASCRIPT, HTML, PLAIN;

    public static Language fromExtension(String ext) {
        if (ext == null) return PLAIN;
        switch (ext.toLowerCase()) {
            case "java":            return JAVA;
            case "py":              return PYTHON;
            case "js": case "ts":   return JAVASCRIPT;
            case "html": case "htm": case "xml": return HTML;
            default:                return PLAIN;
        }
    }

    public String getDisplayName() {
        switch (this) {
            case JAVA:       return "Java";
            case PYTHON:     return "Python";
            case JAVASCRIPT: return "JavaScript";
            case HTML:       return "HTML";
            default:         return "Plain Text";
        }
    }
}
