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
import java.util.Iterator;

public class Bench {

    /**
     * A benchmark which creates lots of new photos and displays
     * execution time used to do it. It then iterates on these photos
     * and displays execution time.
     */

    public static void main(String[] args) throws Exception
    {
        //PhotoRepository rep = new PhotoRepository();

        /*
        System.out.println("Iterating on photos...");
        Iterator i = rep.getPhotos().iterator();
        while (i.hasNext()) {
            System.out.print(".");
            Photo p = (Photo)i.next();
            p.getTitle();
            p.getDate();
            p.getRate();
            p.getImage();
            p.getAuthor().getFirstName();
        }
        System.out.println("benchmark runned in "+
                           (System.currentTimeMillis()-_start_time)+"ms");
        */
    }

    static public void createPhotos(int n) throws Throwable {
        System.out.println("Creating photos "+n+"...");
        /*
        if (!ejp.tracer.TracerAPI.enableTracing()) {
            throw ejp.tracer.TracerAPI.getInitializationError();
        }
        */
        long _start_time = System.currentTimeMillis();
        for (int i=0; i<n; i++) {
            System.out.print(".");
            Photo photo = 
                new Photo("Test"+i,
                          new Attachment("Hello".getBytes(),"text/plain","hello"));
            //rep.addPhoto(photo);
            photo.setAuthor(new Person());
        }
        System.out.println("object instanciations runned in "+
                           (System.currentTimeMillis()-_start_time)+"ms");
        /*
        if (!ejp.tracer.TracerAPI.disableTracing()) {
            throw ejp.tracer.TracerAPI.getInitializationError();
        }
        */
        _start_time = System.currentTimeMillis();
    }

    static void printUsage() {
        System.out.println("Usage: jac bench.jac <nbPhotos>");
        System.out.println("	<nbPhotos>: number of photos to create");
    }
}

