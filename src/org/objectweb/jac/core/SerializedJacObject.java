/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * This class defines all the needed information when (de)serializing
 * a JAC object.
 *
 * <p>It is used to (de)serialize a JacObject by
 * <code>JacObjectInputStream</code> and
 * <code>JacObjectOutputStream</code>. To ensure the correct use of
 * these streams, use only <code>JacObject.(de)serialize()</code>.
 *
 * <p>Any aspect component can use a <code>SerializedJacObject</code>
 * to add any relevant information during the serialization process
 * (<code>whenSerialized()</code>) or to extract any relevant
 * information during the deserialization process
 * (<code>whenDeserialized()</code>).
 * 
 * @see JacObjectOutputStream
 * @see AspectComponent#whenSerialized(Wrappee,SerializedJacObject)
 * @see JacObjectInputStream
 * @see AspectComponent#whenDeserialized(SerializedJacObject,Wrappee)
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */
 
public class SerializedJacObject implements Serializable {
    static Logger logger = Logger.getLogger("serialization");

    public static final String STATELESS="SerializedJacObject.STATELESS";

   /**
    * Serialize an object into an array of bytes.
    * 
    * <p>If the programmer uses this method, the aspect components are
    * upcalled (<code>whenSerialized()</code>) to parametrize the
    * serialization by filling a <code>SerializedJacObject</code> instance.
    *
    * <p>A symetric process is implemented by <code>deserialize()</code>
    *
    * @param src the object to serialize
    * @return an array of bytes
    * 
    * @see AspectComponent#whenSerialized(Wrappee,SerializedJacObject)
    * @see SerializedJacObject
    * @see #deserialize(byte[])
    * @see JacObjectOutputStream */
    
   public static byte[] serialize(Object src) {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      ObjectOutputStream oos = null;
      byte[] ret = null;

      if(src instanceof Collaboration) {
         Collaboration init=(Collaboration)src;
         Collaboration dest=new Collaboration();

         Iterator it=Collaboration.globalAttributeNames().iterator();
         while(it.hasNext()) {
            String name=(String)it.next();
            if(init.getAttribute(name)!=null) {
               dest.addAttribute(
                  name,
                  init.getAttribute(name));
            }
         }
         logger.debug("serializing "+dest);
         src=dest;
      }

      try {
         oos = (ObjectOutputStream) new JacObjectOutputStream(baos);
         logger.debug("serialize "+src);
         oos.writeObject(src);
         oos.close();
         ret = baos.toByteArray();
         baos.close();
      } catch(Exception e) { 
         e.printStackTrace(); 
      }
      
      return ret;
   }   

    /**
     * Serialize the arguments of a method. */

    public static byte[] serializeArgs(Object[] args,Boolean[] refs) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
        ObjectOutputStream oos = null;
        byte[] ret = null;
        byte[][] tmpret = new byte[args.length][];

        try {
            oos = (ObjectOutputStream) new JacObjectOutputStream(baos);
            
            oos.writeObject(new Integer(args.length));

            for(int i=0;i<args.length;i++) {
             if(refs!=null && refs[i].equals(Boolean.TRUE)) {
            Collaboration.get().addAttribute(SerializedJacObject.STATELESS, Boolean.TRUE);
             }
           oos.writeObject(args[i]);
            }
            
            oos.close();
            ret = baos.toByteArray();
            
           baos.close();
        } catch(Exception e) { 
           e.printStackTrace(); 
        }
      
        return ret;
     }   
   
   /**
    * Deserialize an object from an array of bytes.
    *
    * <p>If the programmer uses this method, the aspect components are
    * upcalled (<code>whenDeserialized()</code> method) to parametrize the
    * deserialization by using a corresponding <code>SerializedJacObject</code>
    * instance.
    *
    * <p>A symetric process is implemented by <code>serialize()</code>
    *
    * @param data      the array of bytes
    * @return the object or null if a error has been encountered
    * 
    * @see AspectComponent#whenDeserialized(SerializedJacObject,Wrappee)
    * @see SerializedJacObject
    * @see #serialize(Object)
    * @see JacObjectInputStream */
    
   public static Object deserialize(byte[] data) {
   
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      
      ObjectInputStream ois = null;
      Object ret = null;

      try {
         ois = (ObjectInputStream)new JacObjectInputStream(bais);
         ret = ois.readObject();
         //Log.trace("serialization",2,"deserialize "+ret);
         ois.close();
         bais.close();
      } catch(Exception e) { 
         e.printStackTrace(); 
      }
      
      return ret;
   }

    /**
     * Deserialize the arguments of a method. */

   public static Object deserializeArgs(byte[] data) {
   
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      
      ObjectInputStream ois = null;
      Object[] ret = null;
        int nbArgs=0;
        try {
         ois = (ObjectInputStream)new JacObjectInputStream(bais);
         nbArgs = ((Integer)ois.readObject()).intValue();
         ret=new Object[nbArgs];
         for(int i=0;i<nbArgs;i++) {
              ret[i] = ois.readObject();
         }
         ois.close();
         bais.close();
 
      } catch(Exception e) { 
         e.printStackTrace(); 
      }
      
      return ret;
   }

   /** The class name of the serialized JAC object. */
   protected String jacObjectClassName;

   /** The serialized fields for this JAC object. */
   protected HashMap fields = new HashMap();

   /** Extra Aspect Component related infos to be serialized. */
   protected HashMap acInfos = new HashMap();

    /** If true, the deserialized object will be a forwarder towards
        the actual object. Default is true. */
    protected boolean forwarder = true;

   /** 
    * The constructor of a serialized JacObject.
    * 
    * <p>The programmer do not need to call this method since it is
    * implicitly called by <code>JacObjectOutputStream</code>.
    * 
    * @param jacObjectClassName the class name of the serialized JAC object
    * @see JacObjectOutputStream */

   public SerializedJacObject ( String jacObjectClassName ) {
      this.jacObjectClassName = jacObjectClassName;
   }
   
   /** 
    * Returns a hashtable that contains the field names to be
    * serialized and their values.
    *
    * @return the serialized fields */

   public HashMap getFields() {
      return fields;
   }
   
   /**
    * Add a field to be serialized.
    *
    * @param name the field name 
    * @param value its value */

   public void addField ( String name, Object value ) {
      if ( name != null )
         fields.put ( name, value );
   }

   /**
    * Get a serialized field value.
    *
    * <p>This value has been added with the <code>addField</code>
    * method.
    * 
    * @param name the name of the serialized field
    * @return its value
    * @see #addField(String,Object) */

   public Object getField ( String name ) {
      if ( name == null ) return null;
      return fields.get ( name );
   }

   /** 
    * Returns the serialized JAC object class name.
    *
    * @return the class name */

   public String getJacObjectClassName() { return jacObjectClassName; }

   /**
    * Add some aspect component related infos to be serialized.
    *
    * <p>This method can be used by an aspect component within the
    * <code>whenSerialized()</code> method. It allows the aspect
    * programmer to add some relevant informations to be serialized
    * (for instance when transmiting JAC objects to remote host or
    * saving them on the disk).
    * 
    * <p>During the deserialization, an aspect component can retrieve
    * these added information by using <code>getACInfos()</code>.
    *
    * @param acName the name of the aspect component that has set this
    * information
    * @param infos any serializable object
    * 
    * @see AspectComponent
    * @see AspectComponent#whenSerialized(Wrappee,SerializedJacObject)
    * @see #getACInfos(String) */

   public void setACInfos ( String acName, Object infos ) {
      if ( acName != null )
         acInfos.put( acName, infos );
   }
   
   /**
    * Get some aspect component related infos to be deserialized.
    *
    * <p>This method can be used by an aspect component within the
    * <code>whenDeserialized()</code> method. It allows the aspect
    * programmer to get some relevant informations to be deserialized
    * (for instance when transmiting JAC objects to remote host or
    * saving them on the disk).
    * 
    * <p>The retrieved informations are usually set during the
    * serialization by using <code>setACInfos()</code>.
    *
    * @param acName the name of the aspect component that has set this
    * information
    * @return the information
    * 
    * @see AspectComponent
    * @see AspectComponent#whenDeserialized(SerializedJacObject,Wrappee)
    * @see #setACInfos(String,Object) 
    */

   public Object getACInfos(String acName) {
      if (acName == null) return null;
      return acInfos.get(acName);
   }

    /**
     * Tells if the serialized object is a forwarder. By default, all
     * the serialized JAC objects are forwarders.
     * 
     * <p>When a serialized JAC object is a forwarder, then if, when
     * deserialized, no name-corresponding JAC object is found, then
     * the deserialized object is wrapped by a binding wrapper so that
     * the binding aspect is able to resolve the actual object later
     * on. Of course, if a corresponding JAC object is found, then the
     * deserialized JAC object is replaced by the actual object.
     *
     * <p>When a serialized JAC object is not a forwarder, then no
     * replacement or binding mechanism occurs.
     *
     * @return true if forwarder
     * 
     * @see org.objectweb.jac.aspects.naming.NamingAC#whenSerialized(Wrappee,SerializedJacObject)
     * @see org.objectweb.jac.aspects.naming.BindingAC#whenDeserialized(SerializedJacObject,Wrappee)
     */

   public boolean isForwarder() {
      return forwarder;
   }

    /**
     * Disable the forwarding.
     *
     * <p>If this method is called, the <code>isForwarder</code>
     * method will return false.
     *
     * @see #isForwarder()
     * @see #enableForwarding() */

    public void disableForwarding() {
       forwarder = false;
    }

    /**
     * Enable the forwarding.
     * 
     * <p>If this method is called, the <code>isForwarder</code>
     * method will return true (default).
     *
     * @see #isForwarder()
     * @see #disableForwarding() */

    public void enableForwarding() {
       forwarder = true;
    }

}




