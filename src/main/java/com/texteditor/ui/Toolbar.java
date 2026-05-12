package com.texteditor.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Toolbar extends JToolBar {

    public Toolbar(ActionListener fileNew,  ActionListener fileOpen,
                   ActionListener fileSave, ActionListener undo,
                   ActionListener redo,     ActionListener find) {
        setFloatable(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        add(toolBtn("New",     "Ctrl+N", fileNew));
        add(toolBtn("Open",    "Ctrl+O", fileOpen));
        add(toolBtn("Save",    "Ctrl+S", fileSave));
        addSeparator();
        add(toolBtn("Undo",    "Ctrl+Z", undo));
        add(toolBtn("Redo",    "Ctrl+Y", redo));
        addSeparator();
        add(toolBtn("Find",    "Ctrl+F", find));
    }

    private JButton toolBtn(String label, String tip, ActionListener al) {
        JButton b = new JButton(label);
        b.setToolTipText(tip);
        b.setFocusable(false);
        b.addActionListener(al);
        return b;
    }
}
