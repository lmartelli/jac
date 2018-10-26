/*
  Copyright (C) 2001-2003 Renaud Pawlak, Lionel Seinturier.

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

/**
 * <code>WrappeeTranslator</code> translates a regular java class into
 * a wrappable one.
 * 
 * <p>This translation is usually done at <i>load time</i>. After the
 * translation, the class subclasses the <code>JacObject</code> class.
 *
 * @author Renaud Pawlak
 * @author Lionel Seinturier 
 * @author Fabrice Legond-Aubry
 */

public interface WrappeeTranslator {
   /**
    * Translate a class
    * @param name the name of the class to translate
    * @return the byte code or null i the class was not translated
    */
   byte[] translateClass (String name) throws Exception;


   /**
    * Computes RTTI info for a class and gets its bytecode
    * @param aClass name of the class analyze
    * @return bytecode of the class
    */
   byte[] fillClassRTTI(String aClass) throws Exception;
}
