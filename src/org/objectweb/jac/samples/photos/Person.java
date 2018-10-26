/*
  Copyright (C) AOPSYS (http://www.aopsys.com)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.samples.photos;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.jac.aspects.user.Profile;

public class Person {

   /**
    * the last name of the person
    */
   String lastName = "";
   public void setLastName(String value) {
      this.lastName=value;
   }
   public String getLastName() {
      return lastName;
   }

   /**
    * the first name of the person
    */
   String firstName = "";
   public void setFirstName(String value) {
      this.firstName=value;
   }
   public String getFirstName() {
      return firstName;
   }

   /**
    * the email address of the person
    */
   String email;
   public void setEmail(String value) {
      this.email=value;
   }
   public String getEmail() {
      return email;
   }

   /**
    * the password of the person
    */
   String password;
   public String getPassword() {
      return password;
   }
   public void setPassword(String  v) {
      this.password = v;
   }

   Profile profile;
   public Profile getProfile() {
      return profile;
   }
   public void setProfile(Profile  v) {
      this.profile = v;
   }
   
   public Person() {
   }

   public Person(String firstName, String lastName,
                 String email, String password) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.email = email;
      this.password = password;
   }

   HashSet photos = new HashSet();

   /**
    * Get the value of photos
    * @return value of photos
    */
   public Set getPhotos() {
      return photos;
   }

   /**
    * Add a photo to photos
    * @param photo the photo to add
    */
   public void addPhoto(Photo photo) {
      photos.add(photo);
   }

   /**
    * Remove a photo from photos
    * @param photo the photo to remove
    */
   public void removePhoto(Photo photo) {
      photos.remove(photo);
   }

}
