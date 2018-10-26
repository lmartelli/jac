/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.user;

import gnu.regexp.RE;
import gnu.regexp.REException;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * Rules for profiles : allow/deny reading, allow/deny writing, etc ...
 *
 * @see Profile
 * @see UserAC
 */

public class Rule {
    public static final boolean ALLOW = true;
    public static final boolean DENY = false;

    boolean allow;
    String resourceExpression;
    transient RE regexp;

    /**
     * @param allow if <code>true</code> : allowing rule, if
     * <code>false</code> : denying rule
     * @param resourceExpression regular expression indicating which
     * fields or methods this rule aplies to.
     */
    public Rule(boolean allow, String resourceExpression) {
        this.allow = allow;
        this.resourceExpression = resourceExpression;
        try {
            this.regexp = new RE(resourceExpression);
        } catch (REException e) {
            throw new RuntimeException("Caught regular expression exception: "+e);
        }
    }

    /**
     * Get rule mode (allow or deny).
     *
     * @return the rule's mode.
     */
    public boolean getAllow() {
        return allow;
    }

    /**
     * Set rule mode (allow or deny).
     *
     * @param allow mode (<code>true</code> for allow,
     * <code>false</code> for deny).
     */
    public void setAllow(boolean allow) {
        this.allow = allow;
        invalidateCache();
    }

    /**
     * Set regular expression indicating which fields to apply rule for.
     *
     * @param resourceExpression the regular expression.
     */
    public void setResourceExpression(String resourceExpression) {
        this.resourceExpression = resourceExpression;
        invalidateCache();
    }

    public String getResourceExpression() {
        return resourceExpression;
    }

    /**
     * Returns a regular expression object for the resource expression.
     */
    protected RE getRegexp() {
        if (regexp==null) 
            try { 
                regexp = new RE(resourceExpression==null?"":resourceExpression);
            } catch (REException e) {
            }
        return regexp;
    }

    /**
     * Tells if a <code>MetaItem</code> matches the rule.
     *
     * @param item the MetaItem
     */
    public boolean match(MetaItem item) {
        String resourceDescr;
        if (item instanceof AbstractMethodItem) {         
            AbstractMethodItem method = (AbstractMethodItem)item;
            resourceDescr = method.getParent().getName()+
                "."+method.getFullName();
        } else if (item instanceof MemberItem) {         
            resourceDescr = ((MemberItem)item).getParent().getName()+
                "."+item.getName();
        } else {
            resourceDescr = item.toString();
        }
        boolean result = getRegexp().isMatch(resourceDescr);
        if (!result && (item instanceof AbstractMethodItem)) {
            resourceDescr = ((MemberItem)item).getParent().getName()+
                "."+item.getName();
            result = getRegexp().isMatch(resourceDescr);
        }
        Log.trace("profile.rule",resourceExpression+
                  (result?" matches ":" does not match ")+resourceDescr);
        return result;
    }

    /**
     * Invalidates the UserAC.controlAttribute's cache.
     *
     * <p>It merely calls invalidateCache() on UserAC.</p>
     *
     * @see UserAC#controlAttribute(Object,MetaItem,String,Object)
     * @see UserAC#invalidateCache()
     */
    protected void invalidateCache() {
        UserAC ac = (UserAC)ACManager.getACM().getAC("user");
        if (ac!=null)
            ac.invalidateCache();
    }
}
