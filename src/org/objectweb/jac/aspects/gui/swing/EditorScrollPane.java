
package org.objectweb.jac.aspects.gui.swing;

import javax.swing.*;
import java.io.*;

public class EditorScrollPane extends JScrollPane
{
   public SHEditor editor;
   
   EditorScrollPane() {    
      editor = new SHEditor();
      this.setViewportView(editor);
   }
   
   public void showLineNumbering(boolean show) {
   }
   
   public void scrollToLine(int line) {
         editor.scrollToLine(line);
   }
   
   public void loadFromFile(File f) {
      editor.readFromFile(f);
   }
   
   public void saveToFile(File f) {
      editor.saveToFile(f);
   }
   
   public boolean isSaved() {
      return !editor.changed;
   }
   
   public void cut() {
      editor.cut();
   }
   
   public void copy() {
      editor.copy();
   }
   
   public void paste() {
      editor.paste();
   }
}
