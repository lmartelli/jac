/*
  Copyright (C) 2001 Zachary Medico

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.util;

import java.io.*;
import java.net.*;

/**
 *  Here is  an object for InputStream creation
 *  It lets you access the URL that corresponds to the created InputStream
 *
 */
public class URLInputStream {

   private InputStream inputStream;
   private URL url;
    
   public URLInputStream(String fileLocation) throws Exception {        

      try {
         inputStream = new FileInputStream(fileLocation);
         url=new URL("file:"+fileLocation);
         //url = new File(fileLocation).toURL();
      } catch ( FileNotFoundException e ) {
         try {
            url = getClass().getClassLoader().getResource( fileLocation );
            inputStream = url.openStream();
         } catch (Exception e2 ) {
            // uncaught MalformedURLException
            url = new URL(fileLocation);
            inputStream = url.openStream();
         }
      }
   }

   public URL getURL() {
      return url;
   }

   public InputStream getInputStream() {
      return inputStream;
   }
    
   public static void main(String[] args) throws Exception {
      if ( args.length < 1 ) {
         System.err.println("usage: java InputStreamTest <file_path>");
      } else {
         for ( int i=0; i<args.length; i++) {
            try {
               URLInputStream urlInputStream = new URLInputStream( args[i] );
               System.out.println( urlInputStream.getURL().toExternalForm());
               urlInputStream.getInputStream().close();
            } catch( Exception e ) {
               System.out.println(e.getMessage());
            }
            System.out.println();
         }
      }
        
      System.exit(0);        
   }    

}
