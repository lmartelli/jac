/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import java.util.Collection;

public class RelationLink extends Link {

    public RelationLink() {
    }

    public RelationLink(Class start, Class end) {
        initRoles();
        setStart(start);
        setEnd(end);
    }

    /**
     * Instantiates a start role and an end role.
     */
    public void initRoles() {
        startRole = new RelationRole(this);
        endRole = new RelationRole(this);
    }

    public RelationRole startRole() {
        return (RelationRole)startRole;
    }

    public RelationRole endRole() {
        return (RelationRole)endRole;
    }

    public static final int ORIENTATION_BOTH = 0;
    public static final int ORIENTATION_STRAIGHT = 1;
    public static final int ORIENTATION_REVERSE = -1;

    int orientation = ORIENTATION_BOTH;

    /**
     * Get the value of orientation.
     * @return value of orientation.
     */
    public int getOrientation() {
        return orientation;
    }
    /**
     * Set the value of orientation.
     * @param v  Value to assign to orientation.
     */
    public void setOrientation(int  v) {
        this.orientation = v;
    }

    boolean aggregation;
    /**
     * Get the value of aggregation.
     * @return value of aggregation.
     */
    public boolean isAggregation() {
        return aggregation;
    }   
    /**
     * Set the value of aggregation.
     * @param v  Value to assign to aggregation.
     */
    public void setAggregation(boolean  v) {
        this.aggregation = v;
    }

    boolean calculated;
    /**
     * Get the value of aggregation.
     * @return value of aggregation.
     */
    public boolean isCalculated() {
        return calculated;
    }   
    /**
     * Set the value of aggregation.
     * @param v  Value to assign to aggregation.
     */
    public void setCalculated(boolean  v) {
        this.calculated = v;
    }

    public static Collection endChoices(Object substance) {
        return ObjectRepository.getObjects(ClassRepository.get().getClass(Class.class));
    }

    /**
     * Swaps start and end classes. (Does not work well with diagrams)
     */
    public void reverse() {
        RelationRole start = (RelationRole)startRole;
        RelationRole end = (RelationRole)endRole;
        startRole = end;
        endRole = start;
    }
}
