package com.texteditor.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FindReplaceDialog extends JDialog {

    private final JTextPane   textPane;
    private final JTextField  findField;
    private final JTextField  replaceField;
    private final JCheckBox   regexCb;
    private final JCheckBox   matchCaseCb;
    private final JLabel      statusLabel;

    // all match ranges for cycling
    private final List<int[]> matches     = new ArrayList<>();
    private       int         matchIndex  = -1;

    private static final Color HIGHLIGHT_COLOR = new Color(0xFF, 0xD7, 0x00, 180);

    public FindReplaceDialog(JFrame owner, JTextPane textPane) {
        super(owner, "Find & Replace", false);
        this.textPane = textPane;

        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Find:"), gc);
        gc.gridx = 1; gc.weightx = 1.0;
        findField = new JTextField(22);
        form.add(findField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Replace:"), gc);
        gc.gridx = 1; gc.weightx = 1.0;
        replaceField = new JTextField(22);
        form.add(replaceField, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        regexCb    = new JCheckBox("Regex");
        matchCaseCb = new JCheckBox("Match case");
        opts.add(regexCb);
        opts.add(Box.createHorizontalStrut(12));
        opts.add(matchCaseCb);
        form.add(opts, gc);

        add(form, BorderLayout.NORTH);

        // buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 10));
        JButton findAllBtn     = btn("Find All",      e -> findAll());
        JButton prevBtn        = btn("Previous",      e -> step(-1));
        JButton nextBtn        = btn("Next",           e -> step(+1));
        JButton replaceBtn     = btn("Replace",        e -> replaceCurrent());
        JButton replaceAllBtn  = btn("Replace All",    e -> replaceAll());
        JButton closeBtn       = btn("Close",          e -> setVisible(false));
        buttons.add(findAllBtn);
        buttons.add(prevBtn);
        buttons.add(nextBtn);
        buttons.add(replaceBtn);
        buttons.add(replaceAllBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        add(statusLabel, BorderLayout.SOUTH);

        // Escape closes
        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Enter in findField triggers find-all
        findField.addActionListener(e -> findAll());

        pack();
        setResizable(false);
    }

    public void open() {
        if (!isVisible()) {
            setLocationRelativeTo(getOwner());
        }
        setVisible(true);
        findField.requestFocusInWindow();
        findField.selectAll();
    }

    // ── find ─────────────────────────────────────────────────────────────────

    private void findAll() {
        clearHighlights();
        matches.clear();
        matchIndex = -1;

        Pattern p = buildPattern();
        if (p == null) return;

        String text = textPane.getText();
        Matcher m   = p.matcher(text);

        Highlighter hl     = textPane.getHighlighter();
        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT_COLOR);

        while (m.find()) {
            int[] range = {m.start(), m.end()};
            matches.add(range);
            try { hl.addHighlight(m.start(), m.end(), painter); }
            catch (BadLocationException ignored) {}
        }

        status(matches.isEmpty() ? "No matches found." : matches.size() + " match(es) found.");
        if (!matches.isEmpty()) step(+1);
    }

    private void step(int direction) {
        if (matches.isEmpty()) { findAll(); return; }
        matchIndex = (matchIndex + direction + matches.size()) % matches.size();
        int[] range = matches.get(matchIndex);
        textPane.setCaretPosition(range[0]);
        textPane.moveCaretPosition(range[1]);
        textPane.requestFocusInWindow();
        status("Match " + (matchIndex + 1) + " of " + matches.size());
    }

    // ── replace ───────────────────────────────────────────────────────────────

    private void replaceCurrent() {
        if (matches.isEmpty() || matchIndex < 0) { findAll(); return; }
        int[] range = matches.get(matchIndex);
        try {
            textPane.getDocument().remove(range[0], range[1] - range[0]);
            textPane.getDocument().insertString(range[0], replaceField.getText(), null);
        } catch (BadLocationException ignored) {}
        findAll();
    }

    private void replaceAll() {
        Pattern p = buildPattern();
        if (p == null) return;

        String text    = textPane.getText();
        String newText = p.matcher(text).replaceAll(
                Matcher.quoteReplacement(replaceField.getText()));
        textPane.setText(newText);
        clearHighlights();
        matches.clear();
        matchIndex = -1;
        status("Replaced all occurrences.");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Pattern buildPattern() {
        String q = findField.getText();
        if (q.isEmpty()) { status("Enter a search term."); return null; }
        try {
            int flags = matchCaseCb.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;
            return Pattern.compile(regexCb.isSelected() ? q : Pattern.quote(q), flags);
        } catch (PatternSyntaxException e) {
            status("Invalid regex: " + e.getDescription());
            return null;
        }
    }

    private void clearHighlights() {
        textPane.getHighlighter().removeAllHighlights();
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    private JButton btn(String text, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        return b;
    }
}
