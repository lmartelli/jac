/*

  Copyright (C) AOPSYS (http://www.aopsys.com)

  $licence$ */

package org.objectweb.jac.samples.photos;

import org.objectweb.jac.aspects.user.Profile;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class PhotoRepository 
{

   Set photos = new HashSet();

   public PhotoRepository () {
   }

   /**
    * Get the value of photos.
    * @return value of photos.
    */
   public Set getPhotos() {
      return photos;
   }

   /**
    * Search for a photo with its title.
    * @param title the title of the photo.
    * @return photos found.
    */
   public List searchTitle(String title) 
   {
      Iterator i = photos.iterator();
      Vector result = new Vector();
      while ( i.hasNext() ) {
         Photo curphoto = (Photo)i.next();
         if (curphoto.getTitle()!=null) {
            if ( curphoto.getTitle().indexOf( title ) != -1 ) {
               result.add ( curphoto );
            }
         }
      }
      /*
      Photo[] res = new Photo[result.size()];
      System.arraycopy(result.toArray(),0,res,0,result.size());
      */
      
      return result;
   }

   /**
    * Add a photo to photos
    * @param photo the photo to add
    */
   public void addPhoto (Photo photo) {
      photos.add(photo);
   }

   /**
    * Remove a photo from photos
    * @param photo the photo to remove
    */
   public void delPhoto(Photo photo) {
      photos.remove(photo);
   }

   /**
    * Remove all photos.
    */
   public void clearPhotos() {
      photos.clear();
   }

   /**
    * Filter out "owner" and "default" profile
    */
   public static Collection getProfiles(Object substance) {
      Collection profiles = 
         ObjectRepository.getObjects(
            ClassRepository.get().getClass(Profile.class));
      Vector result = new Vector(profiles.size());
      Iterator it=profiles.iterator();
      while(it.hasNext()) {
         Profile cur=(Profile)it.next();
         if (!cur.getName().equals("owner") &&
             !cur.getName().equals("default")) {
            result.add(cur);
         }
      }
      return result;
   }

   /*
    * GUI methods
    */

   /*
   public static Person selectionHandler(org.objectweb.jac.aspects.gui.SwingCustomizedGUI gui,
                                         org.objectweb.jac.core.rtti.CollectionItem collection,
                                         Photo photo) {
      System.out.println("selectionHandler("+photo+")");
      return photo.getAuthor();
   }
   */   
}

// Local Variables: ***
// c-basic-offset:3 ***
// End: **

