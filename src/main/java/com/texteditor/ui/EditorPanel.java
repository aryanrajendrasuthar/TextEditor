package com.texteditor.ui;

import com.texteditor.core.FileManager;
import com.texteditor.core.SyntaxHighlighter;
import com.texteditor.core.UndoRedoManager;
import com.texteditor.model.ColorScheme;
import com.texteditor.model.Language;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class EditorPanel extends JPanel {

    private final JTextPane      textPane;
    private final LineNumberPanel lineNumbers;
    private final StatusBar      statusBar;
    private final SyntaxHighlighter highlighter;
    private final FileManager    fileManager;
    private final UndoRedoManager undoManager;
    private       Language        language = Language.PLAIN;

    // 350ms debounce timer for syntax highlighting
    private final Timer highlightTimer;

    public EditorPanel(ColorScheme scheme, FileManager fileManager,
                       UndoRedoManager undoManager, StatusBar statusBar) {
        super(new BorderLayout());
        this.fileManager  = fileManager;
        this.undoManager  = undoManager;
        this.statusBar    = statusBar;

        // ── text pane (no word wrap) ─────────────────────────────────────────
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                // allow horizontal scroll (disables word wrap)
                return getUI().getPreferredSize(this).width
                       <= getParent().getSize().width;
            }
        };
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        applySchemeToPane(scheme);

        // ── syntax highlighter ───────────────────────────────────────────────
        highlighter  = new SyntaxHighlighter(textPane, scheme);
        highlightTimer = new Timer(350, e -> highlighter.highlight());
        highlightTimer.setRepeats(false);

        // ── undo manager ─────────────────────────────────────────────────────
        textPane.getDocument().addUndoableEditListener(
                e -> undoManager.getUndoManager().addEdit(e.getEdit()));

        // ── document listener ────────────────────────────────────────────────
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onDocChange(); }
            @Override public void removeUpdate(DocumentEvent e)  { onDocChange(); }
            @Override public void changedUpdate(DocumentEvent e) { /* styles */ }
        });

        // ── caret listener for status bar ────────────────────────────────────
        textPane.addCaretListener((CaretEvent e) -> updateStatus());

        // ── key listener: auto-indent + bracket auto-close ───────────────────
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    handleEnter();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == '(' || c == '[' || c == '{' || c == '"' || c == '\'') {
                    e.consume();
                    insertPair(c);
                }
            }
        });

        // ── layout ───────────────────────────────────────────────────────────
        lineNumbers = new LineNumberPanel(textPane, scheme);
        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setRowHeaderView(lineNumbers);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        add(scroll, BorderLayout.CENTER);
    }

    // ── public API ────────────────────────────────────────────────────────────

    public JTextPane getTextPane()   { return textPane; }
    public Language  getLanguage()   { return language; }

    public void setLanguage(Language lang) {
        this.language = lang;
        highlighter.setLanguage(lang);
        highlightTimer.restart();
        updateStatus();
    }

    public void applyScheme(ColorScheme scheme) {
        applySchemeToPane(scheme);
        lineNumbers.setColorScheme(scheme);
        highlighter.setColorScheme(scheme);
        statusBar.applyScheme(scheme);
        highlightTimer.restart();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void applySchemeToPane(ColorScheme scheme) {
        textPane.setBackground(scheme.background);
        textPane.setForeground(scheme.foreground);
        textPane.setCaretColor(scheme.caretColor);
        textPane.setSelectionColor(scheme.selectionColor);
    }

    private void onDocChange() {
        fileManager.markUnsaved();
        highlightTimer.restart();
        updateStatus();
    }

    private void updateStatus() {
        try {
            int caret = textPane.getCaretPosition();
            Element root  = textPane.getDocument().getDefaultRootElement();
            int     line  = root.getElementIndex(caret) + 1;
            int     lineStart = root.getElement(line - 1).getStartOffset();
            int     col   = caret - lineStart + 1;
            statusBar.update(line, col, textPane.getText(), language);
        } catch (Exception ignored) {}
    }

    // ── auto-indent on Enter ──────────────────────────────────────────────────

    private void handleEnter() {
        try {
            int    caret  = textPane.getCaretPosition();
            Element root  = textPane.getDocument().getDefaultRootElement();
            int    lineIdx = root.getElementIndex(caret);
            Element lineEl = root.getElement(lineIdx);
            String  lineText = textPane.getDocument()
                                       .getText(lineEl.getStartOffset(),
                                                lineEl.getEndOffset() - lineEl.getStartOffset());

            // compute leading whitespace of current line
            StringBuilder indent = new StringBuilder();
            for (char ch : lineText.toCharArray()) {
                if (ch == ' ' || ch == '\t') indent.append(ch);
                else break;
            }

            textPane.getDocument().insertString(caret, "\n" + indent, null);
        } catch (BadLocationException ignored) {}
    }

    // ── bracket / quote auto-close ────────────────────────────────────────────

    private void insertPair(char open) {
        char close = closingChar(open);
        try {
            int caret = textPane.getCaretPosition();
            // if text is selected, wrap selection
            String sel = textPane.getSelectedText();
            if (sel != null && !sel.isEmpty()) {
                int start = textPane.getSelectionStart();
                textPane.getDocument().remove(start, sel.length());
                textPane.getDocument().insertString(start,
                        open + sel + close, null);
                textPane.setCaretPosition(start + sel.length() + 1);
            } else {
                textPane.getDocument().insertString(caret,
                        String.valueOf(open) + close, null);
                textPane.setCaretPosition(caret + 1);
            }
        } catch (BadLocationException ignored) {}
    }

    private static char closingChar(char open) {
        switch (open) {
            case '(': return ')';
            case '[': return ']';
            case '{': return '}';
            default:  return open;   // same char for quotes
        }
    }
}
