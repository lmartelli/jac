package org.objectweb.jac.util.ant;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Cup extends Task {
   private File cupFile;
   private String options;
   private String symbols;
   private String parser;
   
   private List split(String str) {
      int begin = 0;
      int end = 0;
      Vector list = new Vector();

      while (end < str.length())
      {
         while ((end < str.length()) && (str.charAt(end) != ' '))
            end++;
         list.add(str.substring(begin, end));
         end = end + 1;
         begin = end;
      }

      return list;
   }

   public void execute() throws BuildException {
      try
      {
         if (parser==null) {
            System.err.println("No parser class specified");
            throw new Exception();
         }
         List opt = split(options);
         opt.add("-parser");
         opt.add(parser);
         File directory = cupFile.getParentFile();
         File target = new File(directory,parser+".java");
         File symTarget = new File(directory,symbols+".java");
         boolean skip = false;
         if (target.exists()
             && cupFile.lastModified()<=target.lastModified()) {
            skip = true;
         }

         if (symbols!=null) {
            opt.add("-symbols");
            opt.add(symbols);
            if (skip && symTarget.exists() 
                && cupFile.lastModified()<=symTarget.lastModified()) {
               return;
            }
         }

         System.out.println("target="+target);
         System.out.println("Opening "+cupFile);
         System.out.println("Options "+options);
         FileInputStream theFileStream = new FileInputStream(cupFile);
         System.setIn(theFileStream);
         java_cup.Main.main((String[])opt.toArray(new String[] {}));

         // Move file generated in current directory
         target.delete();
         new File(parser+".java").renameTo(target);
         if (symbols!=null) {
            target = new File(directory,symbols+".java");
            new File(symbols+".java").renameTo(target);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("it does not work");
         throw new BuildException("Parser generation failed");
      }
   }
   
   public void setFilename(File file) {
      this.cupFile = file;
   }
   
   public void setOptions(String options) {
      this.options = options;
   }

   public void setParser(String parser) {
      this.parser = parser;
   }

   public void setSymbols(String symbols) {
      this.symbols = symbols;
   }
}
