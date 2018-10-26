/*
  Copyright (C) 2001 Renaud Pawlak

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.tracing;

import org.objectweb.jac.core.*;
import java.util.*;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * This class is able to record a set of methods call and replay them.
 */

public class Recorder {

   static Recorder recorder;
   Vector calls = new Vector(); 
   Vector newObjectsClasses = new Vector();
   Hashtable newObjectsIndexes = new Hashtable();
   boolean recording = false;
   int newObjectsCount = 0;

   public void printRecordState() {
      Iterator it = calls.iterator();
      while( it.hasNext() ) {
         Object[] call = (Object[])it.next();
         System.out.println("- "+call[0]+"."+call[1]+
                            Arrays.asList((Object[])call[2]));
      }
   }

   public static Recorder get() {      
      return recorder;
   }

   public boolean isRecording() {
      return recording;
   }

   public void start() {
      calls.clear();
      newObjectsClasses.clear();
      newObjectsIndexes.clear();
      newObjectsCount = 0;
      recording = true;
   }

   public void stop() {
      recording = false;
   }

   public Vector getCalls() {
      return calls;
   }

   public Vector getNewObjectsClasses() {
      return newObjectsClasses;
   }

   public void replay( Vector newObjectsClasses, Vector calls ) {
      if( recording ) return;
      // create the objects that have been instanciated
      // during the record
      Vector createdObjects = new Vector();
      Iterator it = newObjectsClasses.iterator();
      while( it.hasNext() ) {
         Class newObjectClass = (Class)it.next();
         try {
            Object o = newObjectClass.newInstance();
            createdObjects.add(o);
            System.out.println("REPLAY: creating new object "+o);
         } catch( Exception e ) {
            System.out.println("FATAL ERROR: replay failed!!");
            e.printStackTrace();
            return;
         }
      }
      // invoke the same method that have been called during
      // the record
      it = calls.iterator();
      while( it.hasNext() ) {
         Object[] call = (Object[])it.next();
         Wrappee o;
         if( call[0] instanceof Integer ) {
            // resolve a created object
            o = (Wrappee)createdObjects.get( ((Integer)call[0]).intValue() );
         } else {
            // resolve a pre-exisisting object
            o = (Wrappee)NameRepository.get().getObject( (String)call[0] );
         }
         System.out.println("REPLAY: calling recorded "+o+"."+
                            (String)call[1]+Arrays.asList((Object[])call[2]));
         ClassRepository.get().getClass(o).getMethod((String)call[1]).invoke( 
            o, (Object[])call[2] );
      }
   }

   public void recordMethodCall(Object o, String methodName, Object[] args) {
      if( isNewObject(o) ) {
         calls.add( new Object[] { newObjectsIndexes.get(o),
                                   methodName, args } );
      } else {
         calls.add( new Object[] { NameRepository.get().getName(o), 
                                   methodName, args } );
      }
   }

   public void recordNewObject(Object o) {
      newObjectsClasses.add(o.getClass());
      newObjectsIndexes.put(o,new Integer(newObjectsCount++));
   }

   boolean isNewObject(Object o) {
      if( newObjectsIndexes.get(o) == null ) {
         return false;
      } else {
         return true;
      }
   }

}


   





