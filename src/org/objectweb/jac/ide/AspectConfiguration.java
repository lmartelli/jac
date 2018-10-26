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

import java.lang.Class;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Jac;
import org.objectweb.jac.util.File;

public class AspectConfiguration extends ModelElement {

    public AspectConfiguration() {
    }

    public AspectConfiguration(String aspectName) {
        this.name = aspectName;
    }

    public String toString() {
        String _name=null;
        if (name != null) {
            _name=name;
        } else if(aspect!=null) {
            _name=aspect.getName();
        }
        if (_name!=null) {
            if(woven)
                return _name+" *";
            else
                return _name;
        } else {
            return super.toString();
        }
    }

    Application application;

    /**
     * Get the value of application.
     * @return value of application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Set the value of application.
     * @param v  Value to assign to application.
     */
    public void setApplication(Application  v) {
        this.application = v;
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

    public static Set getDeclaredAspects(Object substance) {
        return ACManager.getACM().getDeclaredACs();
    }

    Aspect aspect;

    /**
     * Get the value of aspect.
     * @return value of aspect.
     */
    public Aspect getAspect() {
        return aspect;
    }

    /**
     * Set the value of aspect.
     * @param v  Value to assign to aspect.
     */
    public void setAspect(Aspect  v) {
        this.aspect = v;
    }

    boolean woven = true;

    /**
     * Get the value of woven.
     * @return value of woven.
     */
    public boolean isWoven() {
        return woven;
    }

    /**
     * Set the value of woven.
     * @param v  Value to assign to woven.
     */
    public void setWoven(boolean  v) {
        this.woven = v;
    }

    public boolean canReload() {
        return application!=null && application.isStarted() && application.isDistributionEnabled();
    }

    /**
     * Reloads the configuration in the running process.
     */
    public void reload() throws Throwable/*IOException*/ {
        if (!application.isStarted()) {
            org.objectweb.jac.aspects.gui.Actions.showStatus(
                "Application is not started: cannot reload configuration");
            return;
        }

        org.objectweb.jac.aspects.gui.Actions.showStatus("Reloading aspect configuration '"+
                                                         name+"'...");

        Jac.remoteReloadAspect(getApplication().getName(),
                               getApplication().getServerName(),name);
    }

    String configurationCode = "";

    /**
     * Get the value of configurationCode.
     * @return value of configurationCode.
     */
    public String getConfigurationCode() {
        return configurationCode;
    }

    /**
     * Set the value of configurationCode.
     * @param v  Value to assign to configurationCode.
     */
    public void setConfigurationCode(String  v) {
        this.configurationCode = v;
    }

    String defaultConfigurationCode = "";

    /**
     * Get the value of defaultConfigurationCode.
     * @return value of defaultConfigurationCode.
     */
    public String getDefaultConfigurationCode() {
        return defaultConfigurationCode;
    }

    /**
     * Set the value of defaultConfigurationCode.
     * @param v  Value to assign to defaultConfigurationCode.
     */
    public void setDefaultConfigurationCode(String  v) {
        this.defaultConfigurationCode = v;
    }


    private List configItems=new Vector();

    /**
     * add a new ConfigItem on this Element
     * @param config the new ConfigItem
     */
    public void addConfigItem(ConfigItem config){
        configItems.add(config);
    }

    /**
     * remove an ConfigItem
     * @param config the ConfigItem
     */
    public void removeConfigItem(ConfigItem config){
        configItems.remove(config);
    }

    /**
     * Returns all the configuration methods of the aspect
     * @return a collection of java.lang.reflect.Method
     */
    public Collection getConfigurationMethods() 
    {
        String acClassName = ACManager.getACM().getACPathFromName(name);
        try {
            Class acClass = Class.forName(acClassName);
            AspectComponent acInstance = (AspectComponent)acClass.newInstance();
            return acInstance.getConfigurationMethods();
        } catch (Exception e) {
        }
        return new Vector();
    }
}
