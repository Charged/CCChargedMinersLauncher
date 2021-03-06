package com.chargedminers.launcher.gui;

import com.chargedminers.launcher.LogUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class CutCopyPasteAdapter extends MouseAdapter implements ClipboardOwner {

    public static CutCopyPasteAdapter addToComponent(
            final JTextComponent component, final boolean allowCopy, final boolean allowModification) {
        CutCopyPasteAdapter adapter = new CutCopyPasteAdapter(component, allowCopy, allowModification);
        component.addMouseListener(adapter);
        return adapter;
    }

    private final JTextComponent component;
    private final boolean allowCopy;
    private final boolean allowModification;

    private CutCopyPasteAdapter(
            final JTextComponent component, final boolean allowCopy, final boolean allowModification) {
        if (!allowCopy && !allowModification) {
            throw new IllegalArgumentException("Either allowCopy or allowModification must be set.");
        }
        this.component = component;
        this.allowCopy = allowCopy;
        this.allowModification = allowModification;
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            doPop(e);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            doPop(e);
        }
    }

    private void doPop(final MouseEvent e) {
        final CopyPasteMenu menu = new CopyPasteMenu();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Do nothing.
        // This overload is necessary to be a ClipboardOwner.
    }

    // Menu that provides [Cut], [Copy], and [Paste] buttons for the text box.
    // Isn't it silly that I have to do this manually in Java?
    class CopyPasteMenu extends JPopupMenu {

        CopyPasteMenu() {
            final boolean hasSelectedText
                    = (component.getSelectedText() != null) && component.getSelectedText().length() > 0;
            if (allowCopy && allowModification) {
                final JMenuItem cutMenuItem = new JMenuItem("Cut");
                cutMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setClipboardContents(component.getSelectedText());
                        final int selStart = component.getSelectionStart();
                        final int selEnd = component.getSelectionEnd();
                        String newText = component.getText().substring(0, selStart)
                                + component.getText().substring(selEnd);
                        component.setText(newText);
                        component.setSelectionStart(selStart);
                        component.setSelectionEnd(component.getSelectionStart());
                    }
                });
                cutMenuItem.setEnabled(hasSelectedText);
                add(cutMenuItem);
            }
            if (allowCopy) {
                final JMenuItem copyMenuItem = new JMenuItem("Copy");
                copyMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setClipboardContents(component.getSelectedText());
                    }
                });
                copyMenuItem.setEnabled(hasSelectedText);
                add(copyMenuItem);
            }
            if (allowModification) {
                final JMenuItem pasteMenuItem = new JMenuItem("Paste");
                pasteMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final int selStart = component.getSelectionStart();
                        final int selEnd = component.getSelectionEnd();
                        final String pastedText = getClipboardContents();
                        final String newText = component.getText().substring(0, selStart)
                                + pastedText
                                + component.getText().substring(selEnd);
                        component.setText(newText);
                        component.setSelectionStart(selStart + pastedText.length());
                        component.setSelectionEnd(component.getSelectionStart());
                    }
                });
                add(pasteMenuItem);
            }
        }
    }

    // Places string to clipboard, and makes PromptScreen the clipboard owner
    public void setClipboardContents(String stringToCopy) {
        final StringSelection stringSelection = new StringSelection(stringToCopy);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    // Tries to get string from clipboard. Returns empty string if impossible.
    public String getClipboardContents() {
        String result = "";
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        if ((contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                LogUtil.getLogger().log(Level.WARNING, "Error pasting text from clipboard", ex);
            }
        }
        return result;
    }
}
