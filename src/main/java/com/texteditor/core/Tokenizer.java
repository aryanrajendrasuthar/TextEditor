package com.texteditor.core;

import com.texteditor.model.Language;
import com.texteditor.model.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tokenizer {

    // ── keyword sets ──────────────────────────────────────────────────────────

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
        "abstract","assert","boolean","break","byte","case","catch","char","class",
        "const","continue","default","do","double","else","enum","extends","final",
        "finally","float","for","goto","if","implements","import","instanceof","int",
        "interface","long","native","new","package","private","protected","public",
        "return","short","static","strictfp","super","switch","synchronized","this",
        "throw","throws","transient","try","void","volatile","while",
        "true","false","null","var","record","sealed","permits","yield"
    ));

    private static final Set<String> PYTHON_KEYWORDS = new HashSet<>(Arrays.asList(
        "and","as","assert","async","await","break","class","continue","def","del",
        "elif","else","except","False","finally","for","from","global","if","import",
        "in","is","lambda","None","nonlocal","not","or","pass","raise","return",
        "True","try","while","with","yield","self","cls"
    ));

    private static final Set<String> JS_KEYWORDS = new HashSet<>(Arrays.asList(
        "break","case","catch","class","const","continue","debugger","default",
        "delete","do","else","export","extends","finally","for","function","if",
        "import","in","instanceof","let","new","null","return","static","super",
        "switch","this","throw","true","false","try","typeof","undefined","var",
        "void","while","with","yield","async","await","of","from","as"
    ));

    private static final Set<String> HTML_TAGS = new HashSet<>(Arrays.asList(
        "html","head","body","div","span","p","a","img","ul","ol","li","table","tr",
        "td","th","form","input","button","script","style","link","meta","title",
        "h1","h2","h3","h4","h5","h6","section","article","header","footer","nav",
        "main","aside","canvas","video","audio","br","hr","pre","code","em","strong",
        "select","option","textarea","label","fieldset","legend","iframe","figure"
    ));

    // ── public API ────────────────────────────────────────────────────────────

    public List<Token> tokenize(String text, Language lang) {
        switch (lang) {
            case JAVA:       return tokenizeJava(text);
            case PYTHON:     return tokenizePython(text);
            case JAVASCRIPT: return tokenizeJS(text);
            case HTML:       return tokenizeHTML(text);
            default:         return new ArrayList<>();
        }
    }

    // ── Java ──────────────────────────────────────────────────────────────────

    private List<Token> tokenizeJava(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);

            // line comment
            if (i + 1 < n && c == '/' && s.charAt(i + 1) == '/') {
                int start = i;
                while (i < n && s.charAt(i) != '\n') i++;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            // block comment
            if (i + 1 < n && c == '/' && s.charAt(i + 1) == '*') {
                int start = i; i += 2;
                while (i + 1 < n && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) i++;
                if (i + 1 < n) i += 2;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            // string
            if (c == '"') {
                int start = i; i++;
                while (i < n && s.charAt(i) != '"' && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < n && s.charAt(i) == '"') i++;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            // char literal
            if (c == '\'') {
                int start = i; i++;
                while (i < n && s.charAt(i) != '\'' && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < n && s.charAt(i) == '\'') i++;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            // annotation
            if (c == '@') {
                int start = i; i++;
                while (i < n && isIdentPart(s.charAt(i))) i++;
                tokens.add(tok(Token.Type.ANNOTATION, s, start, i));
                continue;
            }
            // number
            if (Character.isDigit(c)) {
                int start = i;
                while (i < n && isNumberPart(s.charAt(i))) i++;
                tokens.add(tok(Token.Type.NUMBER, s, start, i));
                continue;
            }
            // identifier / keyword
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && isIdentPart(s.charAt(i))) i++;
                String word = s.substring(start, i);
                tokens.add(tok(JAVA_KEYWORDS.contains(word) ? Token.Type.KEYWORD : Token.Type.IDENTIFIER,
                               s, start, i));
                continue;
            }
            i++;
        }
        return tokens;
    }

    // ── Python ────────────────────────────────────────────────────────────────

    private List<Token> tokenizePython(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);

            // comment
            if (c == '#') {
                int start = i;
                while (i < n && s.charAt(i) != '\n') i++;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            // triple-quoted string
            if (i + 2 < n && (c == '"' || c == '\'')
                    && s.charAt(i + 1) == c && s.charAt(i + 2) == c) {
                int start = i; char q = c; i += 3;
                while (i + 2 < n
                        && !(s.charAt(i) == q && s.charAt(i+1) == q && s.charAt(i+2) == q)) i++;
                if (i + 2 < n) i += 3;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            // single-quoted string
            if (c == '"' || c == '\'') {
                int start = i; char q = c; i++;
                while (i < n && s.charAt(i) != q && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < n && s.charAt(i) == q) i++;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            // decorator
            if (c == '@') {
                int start = i; i++;
                while (i < n && (isIdentPart(s.charAt(i)) || s.charAt(i) == '.')) i++;
                tokens.add(tok(Token.Type.ANNOTATION, s, start, i));
                continue;
            }
            // number
            if (Character.isDigit(c)) {
                int start = i;
                while (i < n && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.'
                                 || s.charAt(i) == 'j' || s.charAt(i) == '_')) i++;
                tokens.add(tok(Token.Type.NUMBER, s, start, i));
                continue;
            }
            // identifier / keyword
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && isIdentPart(s.charAt(i))) i++;
                String word = s.substring(start, i);
                tokens.add(tok(PYTHON_KEYWORDS.contains(word) ? Token.Type.KEYWORD : Token.Type.IDENTIFIER,
                               s, start, i));
                continue;
            }
            i++;
        }
        return tokens;
    }

    // ── JavaScript ───────────────────────────────────────────────────────────

    private List<Token> tokenizeJS(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);

            if (i + 1 < n && c == '/' && s.charAt(i + 1) == '/') {
                int start = i;
                while (i < n && s.charAt(i) != '\n') i++;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            if (i + 1 < n && c == '/' && s.charAt(i + 1) == '*') {
                int start = i; i += 2;
                while (i + 1 < n && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) i++;
                if (i + 1 < n) i += 2;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            // template literal
            if (c == '`') {
                int start = i; i++;
                while (i < n && s.charAt(i) != '`') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < n) i++;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            if (c == '"' || c == '\'') {
                int start = i; char q = c; i++;
                while (i < n && s.charAt(i) != q && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < n && s.charAt(i) == q) i++;
                tokens.add(tok(Token.Type.STRING, s, start, i));
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;
                while (i < n && isNumberPart(s.charAt(i))) i++;
                tokens.add(tok(Token.Type.NUMBER, s, start, i));
                continue;
            }
            if (Character.isLetter(c) || c == '_' || c == '$') {
                int start = i;
                while (i < n && (isIdentPart(s.charAt(i)) || s.charAt(i) == '$')) i++;
                String word = s.substring(start, i);
                tokens.add(tok(JS_KEYWORDS.contains(word) ? Token.Type.KEYWORD : Token.Type.IDENTIFIER,
                               s, start, i));
                continue;
            }
            i++;
        }
        return tokens;
    }

    // ── HTML ─────────────────────────────────────────────────────────────────

    private List<Token> tokenizeHTML(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);

            // HTML comment
            if (i + 3 < n && s.startsWith("<!--", i)) {
                int start = i; i += 4;
                while (i + 2 < n && !s.startsWith("-->", i)) i++;
                if (i + 2 < n) i += 3;
                tokens.add(tok(Token.Type.COMMENT, s, start, i));
                continue;
            }
            // tag
            if (c == '<') {
                int tagStart = i; i++;
                if (i < n && s.charAt(i) == '/') i++;   // closing slash
                int nameStart = i;
                while (i < n && Character.isLetterOrDigit(s.charAt(i))) i++;
                String tagName = s.substring(nameStart, i).toLowerCase();
                if (HTML_TAGS.contains(tagName)) {
                    tokens.add(tok(Token.Type.KEYWORD, s, tagStart, i));
                }
                // attributes inside the tag
                while (i < n && s.charAt(i) != '>') {
                    char ac = s.charAt(i);
                    if (ac == '"' || ac == '\'') {
                        int astart = i; char q = ac; i++;
                        while (i < n && s.charAt(i) != q) i++;
                        if (i < n) i++;
                        tokens.add(tok(Token.Type.STRING, s, astart, i));
                    } else {
                        i++;
                    }
                }
                if (i < n) i++;
                continue;
            }
            i++;
        }
        return tokens;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Token tok(Token.Type type, String s, int start, int end) {
        return new Token(type, s.substring(start, end), start, end);
    }

    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isNumberPart(char c) {
        return Character.isDigit(c) || c == '.' || c == 'L' || c == 'f' || c == 'x'
                || c == 'n' || c == '_'
                || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
