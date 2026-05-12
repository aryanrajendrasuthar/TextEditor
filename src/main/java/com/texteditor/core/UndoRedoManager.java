package com.texteditor.core;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoRedoManager {

    private final UndoManager mgr = new UndoManager();

    /** Max undo steps. */
    public UndoRedoManager() { mgr.setLimit(1000); }

    public UndoManager getUndoManager() { return mgr; }

    public void undo() {
        if (mgr.canUndo()) {
            try { mgr.undo(); } catch (CannotUndoException ignored) {}
        }
    }

    public void redo() {
        if (mgr.canRedo()) {
            try { mgr.redo(); } catch (CannotRedoException ignored) {}
        }
    }

    public boolean canUndo() { return mgr.canUndo(); }
    public boolean canRedo() { return mgr.canRedo(); }
}
