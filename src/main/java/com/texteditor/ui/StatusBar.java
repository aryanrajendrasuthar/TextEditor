package com.texteditor.ui;

import com.texteditor.model.ColorScheme;
import com.texteditor.model.Language;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel posLabel;
    private final JLabel statsLabel;
    private final JLabel langLabel;

    public StatusBar(ColorScheme scheme) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        posLabel   = label("Line 1, Col 1", font, SwingConstants.LEFT);
        statsLabel = label("Words: 0 | Chars: 0", font, SwingConstants.CENTER);
        langLabel  = label("Plain Text", font, SwingConstants.RIGHT);

        add(posLabel,   BorderLayout.WEST);
        add(statsLabel, BorderLayout.CENTER);
        add(langLabel,  BorderLayout.EAST);

        applyScheme(scheme);
    }

    private JLabel label(String text, Font font, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(font);
        l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return l;
    }

    public void update(int line, int col, String text, Language lang) {
        posLabel.setText("Line " + line + ", Col " + col);

        int chars = text.length();
        int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        statsLabel.setText("Words: " + words + " | Chars: " + chars);

        langLabel.setText(lang.getDisplayName());
    }

    public void applyScheme(ColorScheme scheme) {
        Color bg = scheme.lineNumberBg;
        Color fg = scheme.foreground;
        setBackground(bg);
        posLabel.setBackground(bg);   posLabel.setForeground(fg);
        statsLabel.setBackground(bg); statsLabel.setForeground(fg);
        langLabel.setBackground(bg);  langLabel.setForeground(fg);
        setOpaque(true);
        posLabel.setOpaque(true);
        statsLabel.setOpaque(true);
        langLabel.setOpaque(true);
        repaint();
    }
}
