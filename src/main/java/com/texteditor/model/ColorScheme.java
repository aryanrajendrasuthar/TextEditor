package com.texteditor.model;

import java.awt.Color;

public class ColorScheme {

    public final String name;
    public final Color  background;
    public final Color  foreground;
    public final Color  keywordColor;
    public final Color  stringColor;
    public final Color  commentColor;
    public final Color  numberColor;
    public final Color  annotationColor;
    public final Color  lineNumberBg;
    public final Color  lineNumberFg;
    public final Color  selectionColor;
    public final Color  caretColor;

    public ColorScheme(String name,
                       Color background,    Color foreground,
                       Color keywordColor,  Color stringColor,
                       Color commentColor,  Color numberColor,
                       Color annotationColor,
                       Color lineNumberBg,  Color lineNumberFg,
                       Color selectionColor, Color caretColor) {
        this.name            = name;
        this.background      = background;
        this.foreground      = foreground;
        this.keywordColor    = keywordColor;
        this.stringColor     = stringColor;
        this.commentColor    = commentColor;
        this.numberColor     = numberColor;
        this.annotationColor = annotationColor;
        this.lineNumberBg    = lineNumberBg;
        this.lineNumberFg    = lineNumberFg;
        this.selectionColor  = selectionColor;
        this.caretColor      = caretColor;
    }
}
