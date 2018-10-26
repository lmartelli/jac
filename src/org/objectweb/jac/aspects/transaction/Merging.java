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

package org.objectweb.jac.aspects.transaction;



import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Log;

public class Merging {

    static Logger logger = Logger.getLogger("transaction");

    public static void merge(Wrappee receptor, 
                             Wrappee original, 
                             Wrappee modified) throws Exception {

        logger.debug("merging "+modified+" with "+receptor);
      
        Vector peerChangedFields = diffFields(receptor,original);
      
        Vector changedFields = diffFields(original,modified);

        Iterator it = changedFields.iterator();
        while(it.hasNext()) {
            FieldItem field = (FieldItem)it.next();
            if( peerChangedFields.contains( field ) ) {
                throw new RuntimeException("concurrent modification during transaction on "
                                           +field);
            }
            if( field instanceof CollectionItem ) {
                // merge the collections
            } else {
                // we use the setter so that the other aspects are 
                // warned of the change
                field.setThroughWriter(receptor,field.get(modified));
            }
        }
    }

    public static Vector diffFields(Wrappee o1, Wrappee o2) {
      
        Vector result = new Vector();
        ClassItem cli = ClassRepository.get().getClass(o1.getClass());
        FieldItem[] fields = cli.getPrimitiveFields();
      
        // diff the primitive fields
        for( int i=0; i<fields.length; i++ ) {
            if(!fields[i].get(o1)
               .equals(fields[i].get(o2))) {
                logger.debug(fields[i]+" differs after transaction");
                result.add(fields[i]);
            }
        }

        CollectionItem[] collections = cli.getCollections();

        // diff the collections (to be implemeted)
        /*for( int i=0; i<collections.length; i++ ) {
          if(!collections[i].getActualCollection(o1)
          .equals(collections[i].getActualCollection(o2))) {
          result.add(fields[i]);
          }
          }*/

        return result;
     
    }

} 


