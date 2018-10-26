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

import java.util.Date;


/**
 * A comment on a photo
 */
public class Comment {
   /**
    * the author of the comment
    */
   Person author;
   public Person getAuthor() {
      return author;
   }
   public void setAuthor(Person author) {
      this.author = author;
   }

   /**
    * the text of the comment
    */
   String comment;
   public String getComment() {
      return comment;
   }
   public void setComment(String comment) {
      this.comment = comment;
   }

   Date date = new Date();
   public Date getDate() {
      return date;
   }

   /**
    * the commented photo
    */
   Photo photo;
   public Photo getPhoto() {
      return photo;
   }
   public void setPhoto(Photo photo) {
      this.photo = photo;
   }
}
