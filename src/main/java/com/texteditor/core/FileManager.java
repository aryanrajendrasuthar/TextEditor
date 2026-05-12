package com.texteditor.core;

import com.texteditor.model.Language;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileManager {

    private File     currentFile;
    private boolean  unsaved = false;
    private Runnable onTitleChange;

    public FileManager(Runnable onTitleChange) {
        this.onTitleChange = onTitleChange;
    }

    // ── new ───────────────────────────────────────────────────────────────────

    public void newFile(JTextPane textPane) {
        if (!confirmDiscard(textPane)) return;
        textPane.setText("");
        currentFile = null;
        unsaved = false;
        updateTitle();
    }

    // ── open ──────────────────────────────────────────────────────────────────

    public boolean openFile(JFrame parent, JTextPane textPane) {
        if (!confirmDiscard(parent)) return false;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
                "Source Files (*.java, *.py, *.js, *.ts, *.html, *.txt)",
                "java","py","js","ts","html","htm","xml","txt","md"));
        fc.setAcceptAllFileFilterUsed(true);
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return false;

        currentFile = fc.getSelectedFile();
        try {
            byte[] bytes = Files.readAllBytes(currentFile.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            textPane.setText(content);
            textPane.setCaretPosition(0);
            unsaved = false;
            updateTitle();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Cannot open file:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ── save ──────────────────────────────────────────────────────────────────

    public boolean save(JFrame parent, JTextPane textPane) {
        if (currentFile == null) return saveAs(parent, textPane);
        return writeFile(parent, textPane, currentFile);
    }

    public boolean saveAs(JFrame parent, JTextPane textPane) {
        JFileChooser fc = new JFileChooser();
        if (currentFile != null) fc.setSelectedFile(currentFile);
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return false;
        currentFile = fc.getSelectedFile();
        return writeFile(parent, textPane, currentFile);
    }

    private boolean writeFile(JFrame parent, JTextPane textPane, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(textPane.getText().getBytes(StandardCharsets.UTF_8));
            unsaved = false;
            updateTitle();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Cannot save file:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public void markUnsaved() {
        if (!unsaved) { unsaved = true; updateTitle(); }
    }

    public boolean hasUnsavedChanges() { return unsaved; }

    public File getCurrentFile() { return currentFile; }

    public Language detectLanguage() {
        if (currentFile == null) return Language.PLAIN;
        String name = currentFile.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? Language.fromExtension(name.substring(dot + 1)) : Language.PLAIN;
    }

    public String getWindowTitle() {
        String name = currentFile != null ? currentFile.getName() : "Untitled";
        return (unsaved ? "* " : "") + name + " — Text Editor";
    }

    private void updateTitle() {
        if (onTitleChange != null) onTitleChange.run();
    }

    private boolean confirmDiscard(Object parent) {
        if (!unsaved) return true;
        int r = JOptionPane.showConfirmDialog(
                (parent instanceof java.awt.Component ? (java.awt.Component) parent : null),
                "You have unsaved changes. Discard them?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }
}
