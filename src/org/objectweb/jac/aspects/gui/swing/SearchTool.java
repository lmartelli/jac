/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A search tool for the SHEditor. 
 */
public class SearchTool implements KeyListener {

    SHEditor editor;
    int savedPosition;
    int searchFrom;
 
    /**
     * @param editor search in this editor's text
     * @param searchFrom position to start searching from
     */
    public SearchTool(SHEditor editor, int searchFrom) {
        this.editor = editor;
        this.searchFrom = searchFrom;
        this.savedPosition = editor.getCaretPosition();
    }

    /** The text to find */
    String searchedText = "";

    /**
     * Finds the next occurrence 
     */
    protected void searchNext() {
        int found = editor.getText().indexOf(searchedText,searchFrom+1);
        if (found!=-1) {
            found(found);
        }
    }

    /**
     * Finds an occurence
     */
    protected void search() {
        int found = editor.getText().indexOf(searchedText,searchFrom);
        if (found!=-1) {
            found(found);
        }
    }

    /**
     * Repaint editor when an occurrence is found
     */
    protected void found(int found) {
        searchFrom = found;
        editor.setSelection(found,found+searchedText.length());
        editor.selectionVisible();
        editor.repaint();
    }

    /**
     * Quit search tool, sets the position at the beginning of the
     * found occurrence.
     */
    protected void done() {
        editor.setCaretPosition(searchFrom);
        editor.toolDone();
    }

    /**
     * Quit search tool, reset position to saved one.
     */
    protected void abort() {
        editor.setCaretPosition(savedPosition);
        editor.resetSelection();
        editor.toolDone();
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_S:
                    searchNext();
                    e.consume();
                    break;
                case KeyEvent.VK_ESCAPE:
                    e.consume();
                    abort();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_END:
                case KeyEvent.VK_HOME:
                    e.consume();
                    done();
                    break;
                default:
                    e.consume();
                    return;
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    e.consume();
                    break;
                case KeyEvent.VK_ESCAPE:
                    e.consume();
                    abort();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_END:
                case KeyEvent.VK_HOME:
                    e.consume();
                    done();
                    break;
                default:
            }
        }
    }

    boolean isFirst = true;
    public void keyTyped(KeyEvent e)
    {
        if (isFirst) {
            // Skip first event (it's the Ctrl+S that triggered the search tool)
            isFirst = false;
            return;
        }
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_S:
                    searchNext();
                    e.consume();
                    break;
                default:
                    e.consume();
                    return;
            }
        } else {
            switch (e.getKeyChar()) {
                case KeyEvent.VK_ENTER:
                    e.consume();
                    done();
                    break;
                default:
                    searchedText += e.getKeyChar();
                    search();
                    e.consume();
            }
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }

}
