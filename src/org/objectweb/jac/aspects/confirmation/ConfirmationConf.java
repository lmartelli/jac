/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.confirmation;

/**
 * The confirmation aspect implementation (allows the user to add
 * confirmation popups before committing. 
 */
public interface ConfirmationConf {

    /**
     * Allows the user to set some methods to be confirmed.
     *
     * @param classes the classes the methods belongs to
     * @param methods a method-pointcut expression denoting sets of methods
     * @param objects the instances the poincut applies to 
     */
    void confirm(String classes, String methods, String objects);
    
    /**
     * Tells that some methods should be confirmed, with a custom message.
     *
     * @param classes the classes the methods belongs to (a class-pointcut expression) 
     * @param methods a method-pointcut expression denoting sets of methods
     * @param objects the instances the poincut applies to 
     * @param message the custom confirmation message
     */
    void confirm(String classes, String methods, String objects, String message);
}
