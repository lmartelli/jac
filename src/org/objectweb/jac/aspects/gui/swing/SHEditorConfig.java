/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.Color;

/**
 * Holds the configuration of the Java source code editor
 */
public class SHEditorConfig 
{
    Color ignoreColor = new Color(0, 150, 0);
    public Color getIgnoreColor() {
        return ignoreColor;
    }
    public void setIgnoreColor(Color ignoreColor) {
        this.ignoreColor = ignoreColor;
    }

    Color stringColor = new Color(0,100,0);
    public Color getStringColor() {
        return stringColor;
    }
    public void setStringColor(Color stringColor) {
        this.stringColor = stringColor;
    }

    Color textColor = Color.black;
    public Color getTextColor() {
        return textColor;
    }
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    Color backgroundColor = Color.white;
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    Color selectionColor = new Color(210,210,210);
    public Color getSelectionColor() {
        return selectionColor;
    }
    public void setSelectionColor(Color color) {
        this.selectionColor = color;
    }

    Color completionColor = Color.cyan;
    public Color getCompletionColor() {
        return completionColor;
    }
    public void setCompletionColor(Color color) {
        this.completionColor = color;
    }

    Color keywordColor = Color.blue;
    public Color getKeywordColor() {
        return keywordColor;
    }
    public void setKeywordColor(Color keywordColor) {
        this.keywordColor = keywordColor;
    }

    Color modifierColor = Color.magenta;
    public Color getModifierColor() {
        return modifierColor;
    }
    public void setModifierColor(Color modifierColor) {
        this.modifierColor = modifierColor;
    }

    Color typeColor = new Color(255, 69, 0);
    public Color getTypeColor() {
        return typeColor;
    }
    public void setTypeColor(Color typeColor) {
        this.typeColor = typeColor;
    }

    Color clampColor =  new Color(0, 120, 120);
    public Color getClampColor() {
        return clampColor;
    }
    public void setClampColor(Color clampColor) {
        this.clampColor = clampColor;
    }

    Color lineNrBgColor = new Color(180, 180, 180);
    public Color getLineNrBgColor() {
        return lineNrBgColor;
    }
    public void setLineNrBgColor(Color lineNrBgColor) {
        this.lineNrBgColor = lineNrBgColor;
    }

    Color lineNrColor = new Color(200, 0, 0);
    public Color getLineNrColor() {
        return lineNrColor;
    }
    public void setLineNrColor(Color lineNrColor) {
        this.lineNrColor = lineNrColor;
    }
   
    /** number of spaces equivalent to a tab character */
    int tabWidth = 4;
    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }
    public int getTabWidth() {
        return tabWidth;
    }

    /** wether to display line numbers in the left margin */
    boolean showLineNumbers = false;
    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }
    public boolean getShowLineNumbers() {
        return showLineNumbers;
    }

    /** wether to complete on each keystroke */
    boolean autoComplete = true;
    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }
    public boolean isAutoComplete() {
        return autoComplete;
    }
}
