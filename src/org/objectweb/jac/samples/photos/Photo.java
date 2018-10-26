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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.samples.photos;

import org.objectweb.jac.lib.Attachment;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Photo {
   
   public Photo() {  
      title = "untitled";
      image = null;
   }

   public Photo (String title, Attachment image) {
      this.title = title;
      this.image = image;
   }

   /**
    * the author of the photo
    */
   Person author;
   public Person getAuthor() {
      return author;
   }
   public void setAuthor(Person author) {
      this.author = author;
   }

   /**
    * image of the photo
    */
   Attachment image;
   public void setImage(Attachment image) {
      this.image = image;      
   }
   public Attachment getImage() {
      return image;
   }

   /**
    * the title of the photo
    */
   String title = "";
   public String getTitle() {
      return title;
   }
   public void setTitle( String title ) {
      this.title = title;      
   }

   /**
    * the date the photo was added
    */
   Date date = new Date();
   public Date getDate() {
      return date;
   }
   public void setDate(Date  v) {
      this.date = v;
   }

   /**
    * the rate of the photo
    */
   int rate = 5;
   public int getRate() {
      return rate;
   }
   public void setRate(int rate) {
      this.rate = rate;
   }

   /**
    * comments on the photo
    */
   List comments = new Vector();
   public List getComments() {
      return comments;
   }
   public void addComment(Comment comment) {
      comments.add(comment);
   }
}
