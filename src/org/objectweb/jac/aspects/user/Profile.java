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

package org.objectweb.jac.aspects.user;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Stack;
import java.util.List;

public class Profile {

    public Profile() {}

    public Profile(String name) {
        this.name = name;
    }

    public Profile(String name, Profile parent) {
        this.name = name;
        this.parent = parent;
    }

    String name;
   
    /**
     * Get the value of name.
     * @return value of name.
     */
    public String getName() {
        return name;
    }
   
    /**
     * Set the value of name.
     * @param v  Value to assign to name.
     */
    public void setName(String  v) {
        this.name = v;
    }
   
    /** inherited profile */
    Profile parent = null;

    public void setParent(Profile parent) {
        this.parent = parent;
        invalidateCache();
    }
    public Profile getParent() {
        return parent;
    }

    transient boolean isNew = true;
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    public boolean isNew() {
        return isNew;
    }

    /**
     * Clear all rules
     */
    public void clear() {
        readRules.clear();
        writeRules.clear();
        addRules.clear();
        createRules.clear();
        removeRules.clear();
        invalidateCache();
    }
   
    Vector readRules = new Vector();
    Vector writeRules = new Vector();
    Vector addRules = new Vector();
    Vector removeRules = new Vector();
    Vector createRules = new Vector();

    public List getReadRules() {
        return readRules;
    }
    public void addReadRule(Rule rule) {
        readRules.add(rule);
        invalidateCache();
    }
    public void removeReadRule(Rule rule) {
        readRules.remove(rule);
        invalidateCache();
    }

    public List getWriteRules() {
        return writeRules;
    }
    public void addWriteRule(Rule rule) {
        writeRules.add(rule);
        invalidateCache();
    }
    public void removeWriteRule(Rule rule) {
        writeRules.remove(rule);
        invalidateCache();
    }

    public List getAddRules() {
        return addRules;
    }
    public void addAddRule(Rule rule) {
        addRules.add(rule);
        invalidateCache();
    }
    public void removeAddRule(Rule rule) {
        addRules.remove(rule);
        invalidateCache();
    }

    public List getCreateRules() {
        return createRules;
    }
    public void addCreateRule(Rule rule) {
        createRules.add(rule);
        invalidateCache();
    }
    public void removeCreateRule(Rule rule) {
        createRules.remove(rule);
        invalidateCache();
    }

    public List getRemoveRules() {
        return removeRules;
    }
    public void addRemoveRule(Rule rule) {
        removeRules.add(rule);
        invalidateCache();
    }
    public void removeRemoveRule(Rule rule) {
        removeRules.remove(rule);
        invalidateCache();
    }

    public void addReadable(String resourceExpr) {
        readRules.add(new Rule(Rule.ALLOW,resourceExpr));
        invalidateCache();
    }

    public void addUnreadable(String resourceExpr) {
        readRules.add(new Rule(Rule.DENY,resourceExpr));
        invalidateCache();
    }

    public void addWritable(String resourceExpr) {
        writeRules.add(new Rule(Rule.ALLOW,resourceExpr));
        invalidateCache();
    }

    public void addUnwritable(String resourceExpr) {
        writeRules.add(new Rule(Rule.DENY,resourceExpr));
        invalidateCache();
    }

    public void addAddable(String resourceExpr) {
        addRules.add(new Rule(Rule.ALLOW,resourceExpr));
        invalidateCache();
    }

    public void addCreatable(String resourceExpr) {
        createRules.add(new Rule(Rule.ALLOW,resourceExpr));
        invalidateCache();
    }

    public void addUnaddable(String resourceExpr) {
        addRules.add(new Rule(Rule.DENY,resourceExpr));
        invalidateCache();
    }

    public void addRemovable(String resourceExpr) {
        removeRules.add(new Rule(Rule.ALLOW,resourceExpr));
        invalidateCache();
    }

    public void addUnremovable(String resourceExpr) {
        removeRules.add(new Rule(Rule.DENY,resourceExpr));
        invalidateCache();
    }

    public Stack getProfileStack() {
        Stack profiles = new Stack();
        Profile profile = this;
        while (profile!=null) {
            profiles.push(profile);
            profile = profile.getParent();
        }
        return profiles;
    }

    public boolean isReadable(MetaItem item) {
        Stack profiles = getProfileStack();
        while(!profiles.empty()) {
            Profile profile = (Profile)profiles.pop();
            Collection rules = profile.getReadRules();
            Iterator it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                if (rule.match(item))
                    return rule.getAllow();
            }
        }
        return false;
    }

    public boolean isWritable(MetaItem item) {
        Log.trace("profile",2,"isWritable "+item);
        Stack profiles = getProfileStack();
        while(!profiles.empty()) {
            Profile profile = (Profile)profiles.pop();
            Log.trace("profile",2,"Current profile "+profile);
            Collection rules = profile.getWriteRules();
            Iterator it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                if (rule.match(item))
                    return rule.getAllow();
            }
        }
        return false;
    }

    public boolean isAddable(MetaItem item) {
        Stack profiles = getProfileStack();
        while(!profiles.empty()) {
            Profile profile = (Profile)profiles.pop();
            Collection rules = profile.getAddRules();
            Iterator it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                if (rule.match(item))
                    return rule.getAllow();
            }
        }
        return false;
    }

    public boolean isCreatable(MetaItem item) {
        Stack profiles = getProfileStack();
        while (!profiles.empty()) {
            Profile profile = (Profile)profiles.pop();
            Collection rules = profile.getCreateRules();
            Iterator it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                if (rule.match(item))
                    return rule.getAllow();
            }
        }
        return false;
    }

    public boolean isRemovable(MetaItem item) {
        Stack profiles = getProfileStack();
        while(!profiles.empty()) {
            Profile profile = (Profile)profiles.pop();
            Collection rules = profile.getRemoveRules();
            Iterator it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                if (rule.match(item))
                    return rule.getAllow();
            }
        }
        return false;
    }

    public static boolean isReadable(Collection profiles,MetaItem item) {
        Iterator it = profiles.iterator();
        while(it.hasNext()) {
            Profile profile = (Profile)it.next();
            if (profile.isReadable(item))
                return true;
        }
        return false;
    }

    public static boolean isWritable(Collection profiles,MetaItem item) {
        Iterator it = profiles.iterator();
        while(it.hasNext()) {
            Profile profile = (Profile)it.next();
            if (profile.isWritable(item))
                return true;
        }
        return false;
    }

    public static boolean isAddable(Collection profiles,MetaItem item) {
        Iterator it = profiles.iterator();
        while(it.hasNext()) {
            Profile profile = (Profile)it.next();
            if (profile.isAddable(item))
                return true;
        }
        return false;
    }

    public static boolean isCreatable(Collection profiles,MetaItem item) {
        Iterator it = profiles.iterator();
        while(it.hasNext()) {
            Profile profile = (Profile)it.next();
            if (profile.isCreatable(item))
                return true;
        }
        return false;
    }

    public static boolean isRemovable(Collection profiles,MetaItem item) {
        Iterator it = profiles.iterator();
        while(it.hasNext()) {
            Profile profile = (Profile)it.next();
            if (profile.isRemovable(item))
                return true;
        }
        return false;
    }

    protected void invalidateCache() {
        UserAC ac = ((UserAC)ACManager.getACM().getAC("user"));
        if (ac!=null)
            ac.invalidateCache();
    }

    public String toString() {
        return name;
    }
}
