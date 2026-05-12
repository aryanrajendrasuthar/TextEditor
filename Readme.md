# Text Editor — Project 18

> Premium Java Swing desktop text editor with syntax highlighting, themes, find & replace, and more.

## Tech Stack

![Java](https://img.shields.io/badge/Java-11+-ED8B00?logo=java)
![Java Swing](https://img.shields.io/badge/Java%20Swing-AWT-blue)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven)

**Resume Skills Covered:** Java, Object-Oriented Design, Algorithms, Data Structures  
**Additional Skills:** Custom tokenizer for syntax highlighting, Command pattern (Undo/Redo)

---

## Features

| Feature | Details |
|---------|---------|
| File Operations | New, Open, Save, Save As with unsaved-changes guard |
| Syntax Highlighting | Java, Python, JavaScript, HTML — custom regex tokenizer, no external deps |
| Auto-Indentation | Enter copies leading whitespace of current line |
| Bracket Auto-Close | `(`, `[`, `{`, `"`, `'` — auto-inserts matching close char |
| Find & Replace | Regex support, match-case toggle, highlight all matches, Previous/Next cycling |
| Undo / Redo | Unlimited depth via Java `UndoManager` (1 000 steps) |
| Line Numbers | Custom `JComponent` gutter panel, stays in sync with scroll |
| Status Bar | `Line X, Col Y | Words: N | Chars: N | Language` |
| Themes | Light, Dark (IntelliJ-inspired), Solarized |
| No word wrap | Horizontal scroll enabled for wide code lines |

---

## Architecture

```
src/main/java/com/texteditor/
├── TextEditorApp.java          <- JFrame, menu bar, key bindings, app entry point
├── ui/
│   ├── EditorPanel.java        <- JTextPane (no-wrap), line numbers, debounced highlight
│   ├── LineNumberPanel.java    <- JComponent gutter using modelToView
│   ├── StatusBar.java          <- live line/col/word/char count
│   ├── Toolbar.java            <- quick-access buttons
│   └── FindReplaceDialog.java  <- regex find/replace with highlight cycling
├── core/
│   ├── SyntaxHighlighter.java  <- SwingWorker-based, 350ms debounce
│   ├── Tokenizer.java          <- char-by-char lexer per language
│   ├── FileManager.java        <- open/save/new with JFileChooser
│   ├── ThemeManager.java       <- ColorScheme factory (Light / Dark / Solarized)
│   └── UndoRedoManager.java    <- wraps javax.swing.undo.UndoManager
└── model/
    ├── Language.java           <- enum + extension detection
    ├── Token.java              <- type, value, start, end
    └── ColorScheme.java        <- immutable color bundle
```

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+N` | New file |
| `Ctrl+O` | Open file |
| `Ctrl+S` | Save |
| `Ctrl+Shift+S` | Save As |
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `Ctrl+F` | Find |
| `Ctrl+H` | Find & Replace |
| `Ctrl+A` | Select All |
| `Enter`  | Auto-indent |

---

## Build & Run

### Prerequisites
- Java 11 or later
- Maven 3.6+

### Build executable JAR
```bash
mvn package
java -jar target/TextEditor.jar
```

### Run directly with Maven
```bash
mvn compile exec:java -Dexec.mainClass=com.texteditor.TextEditorApp
```

### IDE
Import as a Maven project in IntelliJ IDEA or Eclipse and run `TextEditorApp.main()`.

---

## 4-Phase Development Summary

| Phase | Focus | Deliverables |
|-------|-------|-------------|
| Phase 1 | Core Editor Setup | JFrame, file I/O, menu bar, line numbers |
| Phase 2 | Syntax Highlighting | Custom tokenizer for Java/Python/JS/HTML |
| Phase 3 | Advanced Features | Find & Replace (regex), Undo/Redo, auto-indent, bracket auto-close |
| Phase 4 | Themes & Polish | Light/Dark/Solarized themes, status bar, keyboard shortcuts, JAR packaging |
