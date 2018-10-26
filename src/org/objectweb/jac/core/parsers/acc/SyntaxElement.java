package org.objectweb.jac.core.parsers.acc;

/**
 * Base class for Terminal and NonTerminal.
 */
public class SyntaxElement {
   /** Element's name */
   String name;
   /** Left position of element in stream */
   int left;
   /** Right position of element in stream */
   int right;

   SyntaxElement parent;

   public SyntaxElement(String name, int left, int right) {
      this.name = name;
      this.left = left;
      this.right = right;
   }
   public String getName() {
      return name;
   }
   public int getLeft() {
      return left;
   }
   public int getRight() {
      return right;
   }
   public void setParent(SyntaxElement parent) {
      this.parent = parent;
   }

   public SyntaxElement getParent() {
      return parent;
   }

   /**
    * Find a parent element of this element with a given name
    * @param searchedName the name to find
    * @return a SyntaxElement who is a parent of this (or this)
    */
   public SyntaxElement findParent(String searchedName) {
      if (name.equals(searchedName))
         return this;
      else if (parent==null)
         return null;
      else
         return parent.findParent(searchedName);
   }

   public String toString() {
      return name+":"+left+"-"+right;
   }
}
