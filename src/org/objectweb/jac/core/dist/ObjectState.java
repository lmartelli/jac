
package org.objectweb.jac.core.dist;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.objectweb.jac.core.rtti.ClassRepository;

public class ObjectState {

   public static Field[] getFields(Object object) {
      
      Hashtable fields = ClassRepository.getDirectFieldAccess(object.getClass());
      Object[] res = fields.values().toArray();
      Field[] ares = new Field[res.length];
      System.arraycopy ( res, 0, ares, 0, ares.length );
      return ares;

   }         

   public static Field getField(Object object,String name) {

      Hashtable fields = ClassRepository.getDirectFieldAccess(object.getClass());
      return (Field) fields.get(name);

   }

   public static Object getFieldValue (Object substance,String fieldName) {      
      Hashtable ht = ClassRepository.getDirectFieldAccess(substance.getClass());
      Field field = (Field) ht.get(fieldName);
      if ( field == null ) return null;
      Object ret = null;
      try {
         ret = field.get(substance);
      } catch ( Exception e ) {}
      return ret;
   }

   public static Object[] getState (Object substance) {

      Hashtable fields = ClassRepository.getDirectFieldAccess(substance.getClass());
      Object[] tmp = fields.keySet().toArray();
      String[] names = new String[tmp.length];
      System.arraycopy( tmp, 0, names, 0, tmp.length );
      Object[] values = new Object[tmp.length];
      for ( int i = 0; i < names.length; i ++ ) {
         Field f = (Field) fields.get ( names[i] );
         try {
            values[i] = f.get(substance);
         } catch ( Exception e ) {}
      }

      return new Object[] { names, values };
   }

   public static Object[] getState (Object substance,String[] fieldNames) {

      Hashtable tmpfields = ClassRepository.getDirectFieldAccess(substance.getClass());
      Hashtable fields = new Hashtable();
      for ( int i = 0; i < fieldNames.length; i ++ ) {
         Field f = (Field) tmpfields.get( fieldNames[i] );
         if ( f != null ) {
            fields.put( fieldNames[i], f );
         }
      }
      Object[] tmp = fields.keySet().toArray();
      String[] names = new String[tmp.length];
      System.arraycopy( tmp, 0, names, 0, tmp.length );
      Object[] values = new Object[tmp.length];
      for ( int i = 0; i < names.length; i ++ ) {
         Field f = (Field) fields.get ( names[i] );
         try {
            values[i] = f.get(substance);
         } catch ( Exception e ) {}
      }

      return new Object[] { names, values };
   }

   public static void setState (Object substance, Object[] state ) {

      Hashtable fields = ClassRepository.getDirectFieldAccess(substance.getClass());
      String[] names = (String[])state[0];
      Object[] values = (Object[])state[1];
      for ( int i = 0; i < names.length; i ++ ) {
         Field f = (Field) fields.get ( names[i] );
         try {
            f.set(substance,values[i]);
         } catch ( Exception e ) {}
      }
   }

   public static void setFieldValue (Object substance, String fieldName, Object value) {
      Hashtable ht = ClassRepository.getDirectFieldAccess(substance.getClass());
      Field field = (Field) ht.get( fieldName );
      if ( field == null ) return;
      try {
         field.set(substance, value);
      } catch ( Exception e ) {}
   }

}
