package com.texteditor.core;

import com.texteditor.model.ColorScheme;
import com.texteditor.model.Language;
import com.texteditor.model.Token;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.List;

public class SyntaxHighlighter {

    private final Tokenizer   tokenizer = new Tokenizer();
    private final JTextPane   textPane;
    private       Language    language  = Language.PLAIN;
    private       ColorScheme scheme;
    private       boolean     busy      = false;

    public SyntaxHighlighter(JTextPane textPane, ColorScheme scheme) {
        this.textPane = textPane;
        this.scheme   = scheme;
    }

    public void setLanguage(Language lang)   { this.language = lang; }
    public void setColorScheme(ColorScheme s) { this.scheme  = s; }

    /** Schedule a highlight pass on a background thread with EDT-safe apply. */
    public void highlight() {
        if (busy || language == Language.PLAIN) return;
        busy = true;

        final String text = textPane.getText();

        SwingWorker<List<Token>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Token> doInBackground() {
                return tokenizer.tokenize(text, language);
            }

            @Override
            protected void done() {
                try {
                    applyStyles(get(), text.length());
                } catch (Exception ignored) {
                } finally {
                    busy = false;
                }
            }
        };
        worker.execute();
    }

    private void applyStyles(List<Token> tokens, int docLen) {
        StyledDocument doc = textPane.getStyledDocument();

        // reset to default foreground
        SimpleAttributeSet def = new SimpleAttributeSet();
        StyleConstants.setForeground(def, scheme.foreground);
        StyleConstants.setBold(def, false);
        StyleConstants.setItalic(def, false);
        doc.setCharacterAttributes(0, docLen, def, true);

        for (Token t : tokens) {
            if (t.start < 0 || t.end > docLen || t.start >= t.end) continue;

            SimpleAttributeSet a = new SimpleAttributeSet();
            switch (t.type) {
                case KEYWORD:
                    StyleConstants.setForeground(a, scheme.keywordColor);
                    StyleConstants.setBold(a, true);
                    break;
                case STRING:
                    StyleConstants.setForeground(a, scheme.stringColor);
                    break;
                case COMMENT:
                    StyleConstants.setForeground(a, scheme.commentColor);
                    StyleConstants.setItalic(a, true);
                    break;
                case NUMBER:
                    StyleConstants.setForeground(a, scheme.numberColor);
                    break;
                case ANNOTATION:
                    StyleConstants.setForeground(a, scheme.annotationColor);
                    break;
                default:
                    continue;
            }
            doc.setCharacterAttributes(t.start, t.end - t.start, a, false);
        }
    }
}
