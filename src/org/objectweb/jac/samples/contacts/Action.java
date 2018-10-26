
package org.objectweb.jac.samples.contacts;

import java.util.Date;

public class Action {
   String description;
   Date date;

   /**
    * Get the value of date.
    * @return value of date.
    */
   public Date getDate() {
      return date;
   }
   
   /**
    * Set the value of date.
    * @param v  Value to assign to date.
    */
   public void setDate(Date  v) {
      this.date = v;
   }

   /**
    * Get the value of description.
    * @return value of description.
    */
   public String getDescription() {
      return description;
   }
   
   /**
    * Set the value of description.
    * @param v  Value to assign to description.
    */
   public void setDescription(String  v) {
      this.description = v;
   }
   
   public Action(String description, Date date) {
      this.description = description;
      this.date = date;
   }
   
}
	
