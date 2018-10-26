
package org.objectweb.jac.samples.document;

import org.objectweb.jac.lib.java.util.*;

public class DocumentServer {
   Vector documents = new Vector();
   public Vector search(String expression) {
      return documents;
   }
   public void addDocument(Document doc) {
      documents.add(doc);
   }
   public void removeDocument(Document doc) {
      documents.remove(doc);
   }

}
