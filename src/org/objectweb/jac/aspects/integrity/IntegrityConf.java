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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.integrity;

import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

public interface IntegrityConf {

    /**
     * Tells that the integrity aspect should maintain integrity
     * between the roles of associations.
     *
     * <p>For instance, if you have a Customer class and an Order
     * class:</p>
     *
     * <pre>
     *    ,----------. 1    n ,-------.
     *    | Customer |--------| Order |
     *    `----------'        `-------'
     * </pre>
     *
     * <p>You can declare an association made of Cutomer.orders and
     * Order.customer, so that setting the cutomer of an order will
     * automatically add this order in the customer's list of
     * orders. And vice-versa.</p>
     *
     * @see org.objectweb.jac.core.rtti.RttiAC#declareAssociation(FieldItem,FieldItem) 
     */
    void updateAssociations();

    /**
     * This method declares a repository collection.
     *
     * <p>When an object is added to a relation, it will be
     * automatically added to the collection of the repository.
     *
     * @param repositoryName the JAC object's name of the repository
     * @param collection the collection to add into (on the instance
     * given as the first parameter)
     * @param field objects that are set or added to this field are
     * added to the repository */
    void declareRepository(String repositoryName,
                           CollectionItem collection,
                           FieldItem field);

    /**
     * <p>Declare a referential integrity contraint.</p>
     *
     * <p>When an object is removed from the target collection, it will
     * be checked wether it can be allowed.</p>
     *
     * <p>Suppose you have Customer class and an Invoice class :</p>
     * <pre>
     *  ,-----------. 1   * ,----------. 1       * ,---------.
     *  | Customers |-------| Customer |-----------| Invoice |
     *  `-----------'       `----------'           `---------'
     * </pre>
     *
     * <p>You do not want to allow the removal of a customer from the
     * Customers repository if there are invoices for that customer. So
     * you would add the following constraint:</p>
     *    
     * <code>declareConstraint Invoice.customer Customers.customers FORBIDDEN;</code>
     *
     * @param relation
     * @param target the collection on which checking will occur on
     * remove, or the reference on which checking will occur when
     * setting another value.
     * @param constraint the type of the constraint. It may be
     * "DELETE_CASCADE" (delete the object holding the reference on the
     * object to be deleted), "SET_NULL" (set the reference to null, or
     * remove the object from the collection), FORBIDDEN" (raise an
     * exception).
     */
    void declareConstraint(FieldItem relation,
                           FieldItem target, String constraint);

    /**
     * Use this configuration method to add a precondition on a
     * object's field.
     *
     * <p>It means that the initial value of the field will be tested
     * with the added constraint and if it is not valid, it will be
     * rejected.</p>
     *
     * <p>Constraint methods must return a Boolean that is Boolean.TRUE
     * if the test has been validated (passed), Boolean.FALSE else. The
     * class <code>org.objectweb.jac.aspects.integrity.GenericConditions</code>
     * contains basic tests such as <code>forbiddenValues</code> or
     * <code>authorizedValues</code>.</p>
     *
     * @param field the field to constrain
     * @param constraint the constraint method used to check the
     * field's value
     * @param params the parameters passed to the contraint method
     * @param errorMsg the error message displayed if the checking has
     * not been passed
     *
     * @see #addPostCondition(FieldItem,MethodItem,Object[],String)
     */
    void addPreCondition(FieldItem field,
                         MethodItem constraint,
                         Object[] params,
                         String errorMsg);

    /**
     * Use this configuration method to add a postcondition on a
     * object's field.
     *
     * <p>It means that the final value of the field will be tested
     * with the added constraint and if it is not valid, it will be
     * rejected.</p>
     *
     * <p>Constraint methods must return a Boolean that is Boolean.TRUE
     * if the test has been validated (passed), Boolean.FALSE else. The
     * class <code>org.objectweb.jac.aspects.integrity.GenericConditions</code>
     * contains basic tests such as <code>forbiddenValues</code> or
     * <code>authorizedValues</code>.</p>
     *
     * @param field the field to constrain
     * @param constraint the constraint method used to check the
     * field's value
     * @param params the parameters passed to the contraint method
     * @param errorMsg the error message displayed if the checking has
     * not been passed
     *
     * @see #addPreCondition(FieldItem,MethodItem,Object[],String)
     */
    void addPostCondition(FieldItem field,
                          MethodItem constraint,
                          Object[] params,
                          String errorMsg);

}

