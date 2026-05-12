package com.texteditor;

import com.texteditor.core.FileManager;
import com.texteditor.core.ThemeManager;
import com.texteditor.core.UndoRedoManager;
import com.texteditor.model.Language;
import com.texteditor.ui.EditorPanel;
import com.texteditor.ui.FindReplaceDialog;
import com.texteditor.ui.StatusBar;
import com.texteditor.ui.Toolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TextEditorApp extends JFrame {

    private final ThemeManager    themeManager  = new ThemeManager();
    private final UndoRedoManager undoManager   = new UndoRedoManager();
    private final FileManager     fileManager;
    private final EditorPanel     editorPanel;
    private final StatusBar       statusBar;
    private       FindReplaceDialog findDialog;

    public TextEditorApp() {
        super("Untitled — Text Editor");

        fileManager = new FileManager(() ->
                SwingUtilities.invokeLater(() ->
                        setTitle(fileManager.getWindowTitle())));

        statusBar   = new StatusBar(themeManager.getCurrent());
        editorPanel = new EditorPanel(themeManager.getCurrent(),
                                      fileManager, undoManager, statusBar);

        // ── layout ───────────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        Toolbar toolbar = new Toolbar(
                e -> fileNew(), e -> fileOpen(),
                e -> fileSave(), e -> undo(),
                e -> redo(),    e -> showFind());
        add(toolbar,     BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
        add(statusBar,   BorderLayout.SOUTH);

        // ── menu bar ─────────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── keyboard shortcuts ────────────────────────────────────────────────
        bindKeys();

        // ── frame setup ───────────────────────────────────────────────────────
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitApp(); }
        });

        setPreferredSize(new Dimension(1000, 700));
        pack();
        setLocationRelativeTo(null);
    }

    // ── menu bar ──────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(fileMenu());
        mb.add(editMenu());
        mb.add(viewMenu());
        mb.add(formatMenu());
        return mb;
    }

    private JMenu fileMenu() {
        JMenu m = new JMenu("File");
        m.setMnemonic(KeyEvent.VK_F);
        m.add(mi("New",     KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
                 e -> fileNew()));
        m.add(mi("Open…",   KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
                 e -> fileOpen()));
        m.addSeparator();
        m.add(mi("Save",    KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                 e -> fileSave()));
        m.add(mi("Save As…",
                 KeyStroke.getKeyStroke(KeyEvent.VK_S,
                         InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                 e -> fileSaveAs()));
        m.addSeparator();
        m.add(mi("Exit",    null, e -> exitApp()));
        return m;
    }

    private JMenu editMenu() {
        JMenu m = new JMenu("Edit");
        m.setMnemonic(KeyEvent.VK_E);
        m.add(mi("Undo",     KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                 e -> undo()));
        m.add(mi("Redo",     KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
                 e -> redo()));
        m.addSeparator();
        m.add(mi("Find…",    KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
                 e -> showFind()));
        m.add(mi("Replace…", KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK),
                 e -> showFind()));
        m.addSeparator();
        m.add(mi("Select All",
                 KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
                 e -> editorPanel.getTextPane().selectAll()));
        return m;
    }

    private JMenu viewMenu() {
        JMenu m = new JMenu("View");
        m.setMnemonic(KeyEvent.VK_V);

        // Theme sub-menu
        JMenu themeMenu = new JMenu("Theme");
        for (String name : new String[]{"Light", "Dark", "Solarized"}) {
            JMenuItem item = new JMenuItem(name);
            item.addActionListener(e -> applyTheme(name));
            themeMenu.add(item);
        }
        m.add(themeMenu);

        // Font size sub-menu
        JMenu fontMenu = new JMenu("Font Size");
        for (int size : new int[]{11, 12, 13, 14, 16, 18, 20, 24}) {
            int sz = size;
            JMenuItem fi = new JMenuItem(size + "pt");
            fi.addActionListener(e -> setFontSize(sz));
            fontMenu.add(fi);
        }
        m.add(fontMenu);
        return m;
    }

    private JMenu formatMenu() {
        JMenu m = new JMenu("Language");
        m.setMnemonic(KeyEvent.VK_L);
        for (Language lang : Language.values()) {
            JMenuItem item = new JMenuItem(lang.getDisplayName());
            item.addActionListener(e -> setLanguage(lang));
            m.add(item);
        }
        return m;
    }

    private JMenuItem mi(String label, KeyStroke ks,
                         java.awt.event.ActionListener al) {
        JMenuItem item = new JMenuItem(label);
        if (ks != null) item.setAccelerator(ks);
        item.addActionListener(al);
        return item;
    }

    // ── key bindings (in addition to menu accelerators) ───────────────────────

    private void bindKeys() {
        JRootPane rp    = getRootPane();
        int       ctrl  = InputEvent.CTRL_DOWN_MASK;
        int       shift = InputEvent.SHIFT_DOWN_MASK;

        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrl),       "new",      e -> fileNew());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl),       "open",     e -> fileOpen());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl),       "save",     e -> fileSave());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl|shift), "saveas",   e -> fileSaveAs());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl),       "undo",     e -> undo());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl),       "redo",     e -> redo());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl),       "find",     e -> showFind());
        bind(rp, KeyStroke.getKeyStroke(KeyEvent.VK_H, ctrl),       "replace",  e -> showFind());
    }

    private void bind(JRootPane rp, KeyStroke ks, String name,
                      java.awt.event.ActionListener al) {
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
        rp.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { al.actionPerformed(e); }
        });
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void fileNew() {
        fileManager.newFile(editorPanel.getTextPane());
        setLanguage(Language.PLAIN);
    }

    private void fileOpen() {
        if (fileManager.openFile(this, editorPanel.getTextPane())) {
            Language detected = fileManager.detectLanguage();
            setLanguage(detected);
        }
    }

    private void fileSave()   { fileManager.save(this, editorPanel.getTextPane()); }
    private void fileSaveAs() { fileManager.saveAs(this, editorPanel.getTextPane()); }

    private void undo() { undoManager.undo(); }
    private void redo() { undoManager.redo(); }

    private void showFind() {
        if (findDialog == null) {
            findDialog = new FindReplaceDialog(this, editorPanel.getTextPane());
        }
        findDialog.open();
    }

    private void applyTheme(String name) {
        themeManager.setTheme(themeManager.byName(name));
        editorPanel.applyScheme(themeManager.getCurrent());
    }

    private void setFontSize(int size) {
        Font f = editorPanel.getTextPane().getFont();
        editorPanel.getTextPane().setFont(f.deriveFont((float) size));
    }

    private void setLanguage(Language lang) {
        editorPanel.setLanguage(lang);
    }

    private void exitApp() {
        if (fileManager.hasUnsavedChanges()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Exit anyway?",
                    "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }
        System.exit(0);
    }

    // ── entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new TextEditorApp().setVisible(true);
        });
    }
}
