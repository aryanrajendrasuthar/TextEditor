package com.texteditor.ui;

import com.texteditor.model.ColorScheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;

public class LineNumberPanel extends JPanel implements DocumentListener {

    private final JTextPane   textPane;
    private       ColorScheme scheme;

    private static final int PADDING    = 6;
    private static final int MIN_DIGITS = 3;

    public LineNumberPanel(JTextPane textPane, ColorScheme scheme) {
        this.textPane = textPane;
        this.scheme   = scheme;

        setOpaque(true);
        setBackground(scheme.lineNumberBg);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, textPane.getFont().getSize()));

        textPane.getDocument().addDocumentListener(this);

        // Keep font in sync
        textPane.addPropertyChangeListener("font", e -> {
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, textPane.getFont().getSize()));
            refreshWidth();
            repaint();
        });

        refreshWidth();
    }

    public void setColorScheme(ColorScheme scheme) {
        this.scheme = scheme;
        setBackground(scheme.lineNumberBg);
        repaint();
    }

    // ── DocumentListener ─────────────────────────────────────────────────────

    @Override public void insertUpdate(DocumentEvent e)  { refreshWidth(); repaint(); }
    @Override public void removeUpdate(DocumentEvent e)  { refreshWidth(); repaint(); }
    @Override public void changedUpdate(DocumentEvent e) { /* style, ignore */ }

    // ── painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont());

        g2.setColor(scheme.lineNumberBg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm      = g2.getFontMetrics();
        Element     root    = textPane.getDocument().getDefaultRootElement();
        int         lineCount = root.getElementCount();

        Rectangle visible = textPane.getVisibleRect();
        int startY = visible.y;
        int endY   = visible.y + visible.height;

        for (int line = 0; line < lineCount; line++) {
            Element el = root.getElement(line);
            try {
                Rectangle rect = textPane.modelToView(el.getStartOffset());
                if (rect == null) continue;
                if (rect.y + rect.height < startY) continue;
                if (rect.y > endY) break;

                String num    = String.valueOf(line + 1);
                int    drawX  = getWidth() - fm.stringWidth(num) - PADDING;
                int    drawY  = rect.y + fm.getAscent()
                                      + (rect.height - fm.getHeight()) / 2;

                g2.setColor(scheme.lineNumberFg);
                g2.drawString(num, drawX, drawY);
            } catch (BadLocationException ignored) {}
        }

        // right separator line
        g2.setColor(scheme.lineNumberFg);
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    }

    private void refreshWidth() {
        int    digits = Math.max(MIN_DIGITS,
                String.valueOf(textPane.getDocument().getDefaultRootElement()
                                       .getElementCount()).length());
        FontMetrics fm  = getFontMetrics(getFont());
        int         w   = fm.stringWidth("0".repeat(digits)) + PADDING * 2 + 4;
        Dimension   dim = new Dimension(w, 0);
        if (!dim.equals(getPreferredSize())) {
            setPreferredSize(dim);
            revalidate();
        }
    }
}
