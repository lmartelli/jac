/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.ide;

public class PointcutLink extends RelationLink {

    public PointcutLink() {
        super();
    }
   
    public PointcutLink(Class start, Class end) {
        super(start,end);
    }

    String methodPCD;
   
    /**
     * Get the value of methodPCD.
     * @return value of methodPCD.
     */
    public String getMethodPCD() {
        return methodPCD;
    }
   
    /**
     * Set the value of methodPCD.
     * @param v  Value to assign to methodPCD.
     */
    public void setMethodPCD(String  v) {
        this.methodPCD = v;
    }
   
    String hostPCD;
   
    /**
     * Get the value of hostPCD.
     * @return value of hostPCD.
     */
    public String getHostPCD() {
        return hostPCD;
    }
   
    /**
     * Set the value of hostPCD.
     * @param v  Value to assign to hostPCD.
     */
    public void setHostPCD(String  v) {
        this.hostPCD = v;
    }

    /*
   public String getStartRole() {
      return aspectRole;
   }

   public void setStartRole(String role) {
      aspectRole=role;
   }

   public String getEndRole() {
      return getMethodPCD()+":"+getHostPCD();
   }

   public void setEndRole(String role) {
      if(role==null) return;
      int i=role.indexOf(":");
      if(i==-1) {
         setMethodPCD(role);
      } else {
         setMethodPCD(role.substring(0,i));
         setHostPCD(role.substring(i+1));
      }
   }
   */
   
    String aspectRole;
   
    /**
     * Get the value of aspectRole.
     * @return value of aspectRole.
     */
    public String getAspectRole() {
        return aspectRole;
    }
   
    /**
     * Set the value of aspectRole.
     * @param v  Value to assign to aspectRole.
     */
    public void setAspectRole(String  v) {
        this.aspectRole = v;
    }

}
