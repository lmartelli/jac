
package org.objectweb.jac.samples.calcul;

public class Op {

   String name;
   
   /**
    * Get the value of name.
    * @return value of name.
    */
   public String getName() {
      return name;
   }
   
   /**
    * Set the value of name.
    * @param v  Value to assign to name.
    */
   public void setName(String  v) {
      this.name = v;
   }
   
   String arg;
   
   /**
    * Get the value of arg.
    * @return value of arg.
    */
   public String getArg() {
      return arg;
   }
   
   /**
    * Set the value of arg.
    * @param v  Value to assign to arg.
    */
   public void setArg(String  v) {
      this.arg = v;
   }
   

   public Op(String name,String arg) {
      this.name=name;
      this.arg=arg;
   }

}
