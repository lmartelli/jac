
package org.objectweb.jac.samples.contacts;

import java.net.URL;

public class Company {

   public Company(String name,String address,
                  String phone,String fax,URL webSiteURL) {
      this.name = name;
      this.address = address;
      this.phone = phone;
      this.fax = fax;
      this.webSiteURL = webSiteURL;
   }

   public String toString() {
      return name;
   }

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
   
   String address;
   
   /**
    * Get the value of address.
    * @return value of address.
    */
   public String getAddress() {
      return address;
   }

   /**
    * Set the value of address.
    * @param v  Value to assign to address.
    */
   public void setAddress(String  v) {
      this.address = v;
   }
   

   String phone;
   
   /**
    * Get the value of phone.
    * @return value of phone.
    */
   public String getPhone() {
      return phone;
   }
   
   /**
    * Set the value of phone.
    * @param v  Value to assign to phone.
    */
   public void setPhone(String  v) {
      this.phone = v;
   }
   

   URL webSiteURL;
   
   /**
    * Get the value of webSiteURL.
    * @return value of webSiteURL.
    */
   public URL getWebSiteURL() {
      return webSiteURL;
   }
   
   /**
    * Set the value of webSiteURL.
    * @param v  Value to assign to webSiteURL.
    */
   public void setWebSiteURL(URL  v) {
      this.webSiteURL = v;
   }
   

   String notes;
   
   /**
    * Get the value of notes.
    * @return value of notes.
    */
   public String getNotes() {
      return notes;
   }
   
   /**
    * Set the value of notes.
    * @param v  Value to assign to notes.
    */
   public void setNotes(String  v) {
      this.notes = v;
   }

   String fax;
   
   /**
    * Get the value of fax.
    * @return value of fax.
    */
   public String getFax() {
      return fax;
   }
   
   /**
    * Set the value of fax.
    * @param v  Value to assign to fax.
    */
   public void setFax(String  v) {
      this.fax = v;
   }
   
   
}
	
