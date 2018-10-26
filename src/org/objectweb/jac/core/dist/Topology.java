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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.core.dist;

import gnu.regexp.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACConfiguration;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.Application;
import org.objectweb.jac.core.ApplicationRepository;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.util.ExtArrays;

/**
 * This class defines a generic topology where nodes are a
 * set of reachable containers.
 *
 * <p>It is used by the distribution aspect to define distribution,
 * deployment, and replication schemes.<p>
 *
 * @see org.objectweb.jac.core.dist.RemoteContainer */

public class Topology implements Serializable {
    static Logger logger = Logger.getLogger("topology");
    static Logger loggerDist = Logger.getLogger("dist");
    static Logger loggerApp = Logger.getLogger("application");
    static Logger loggerAspects = Logger.getLogger("aspects");

    public boolean bootstrapFlag = false;

    /** The name of the property that defines the initial global
        topology in the org.objectweb.jac.prop file. */
	//protected static String topologyProp = "org.objectweb.jac.topology";

    /** Store the gobal topology of the JAC distributed system. */
    protected static Topology globalTopology = null;

    static Hashtable applicationsTopologies = new Hashtable();

    /**
     * Returns the topology defined for the given application.
     *
     * @param application the application
     * @return the corresponding topology 
     */
    public static Topology getTopology(Application application) {
        if( application == null ) return null;
        return (Topology)applicationsTopologies.get(application);
    }

    /**
     * Sets the topology for a given application.
     *
     * @param application the application
     * @param topology the corresponding topology
     */
    public static void setTopology(Application application, 
                                   Topology topology) {
        if(application == null || topology == null) return;
        loggerApp.debug("setting "+topology+" to "+application); 
        applicationsTopologies.put(application,topology);
    }

    /**
     * This method returns the gobal topology of the JAC distributed
     * middleware.<p>
     *
     * This instance of topology is set in strong consistency so that
     * all the container of the distributed name/aspect space are
     * notified when a container is added or removed.<p>
     *
     * @return the global topology
     */
    public static Topology get() {
        if (globalTopology == null) {

            // creates the global topology 
            new Topology();
        }
        return globalTopology;
    }  

    /**
     * Resets the global topology by re-resolving the different
     * containers.  
     */
    public static void reset() {
        globalTopology=null;
        get();
    }

    /**
     * This method returns a topology with all the containers that
     * match the regular expression within the global topology of org.objectweb.jac.
     *
     * @param regexp a regular expression on the container names
     * @return a partial topology
     * @see #getPartialTopology(String) 
     */
    public static Topology getPartialTopology(RE regexp) {
        return new Topology(get().getContainers(regexp)); 
    }

    /**
     * This method returns the first container that matches the host
     * expression. 
     */
    public RemoteContainer getFirstContainer(String sregexp) {
        RE regexp = null;
        try {
            regexp = new RE(sregexp);
        } catch(Exception e) {
            return null;
        }
        for (int i=0; i<containers.size(); i++) {
            if (regexp.isMatch(((RemoteContainer)containers.get(i)).getName())) {
                return (RemoteContainer)containers.get(i);
            }
        }
        return null;
    }

    /**
     * This method returns a topology with all the containers that
     * match the regular expression within the global topology of org.objectweb.jac.
     *
     * @param regexp a regular expression (as a string) on the
     * container names
     * @return a partial topology 
     */
    public static Topology getPartialTopology(String regexp) {
        if (globalTopology == null) 
            return null;
        RE re = null;
        try {
            re = new RE(regexp);
        } catch(Exception e) {
            logger.error("getPartialTopology "+regexp,e);
            return null;
        }
        //Log.trace("topology","get partial topology "+regexp+"(global topology="+globalTopology);
        RemoteContainer[] containers = globalTopology.getContainers(re);
        logger.debug("get partial topology found "+Arrays.asList(containers));
        return new Topology(containers);
    }

    /** Store the containers of the topology. */
    public List containers = new Vector();

    /** Store the remote references on the name repository for
        optimization matter. */
    public List nameReps = new Vector();

    /**
     * This constructor builds a topology and set it to global if a
     * global topology does not exist yet.
     *
     * @see Topology#get() 
     */
    public Topology() {
        logger.debug("Creating Topology...");
        if (globalTopology == null) {
            NameRepository.get().register("JAC_topology", this);
            globalTopology = this;
        }
        createNameReps();
        logger.debug("Topology created");
    }

    /**
     * Builds a topology with the given container identifiers. The
     * identifier is protocol dependent.<p>
     *
     * This method transforms the names into remote containers
     * references by calling <code>resolve</code>.
     *
     * @param someNames a set of container names
     * @see org.objectweb.jac.core.dist.RemoteContainer#resolve(String) 
     */
    public Topology(String[] someNames) {
        for(int i=0; i<someNames.length; i++) {
            containers.add(RemoteContainer.resolve(someNames[i]));
        }
        createNameReps();
    }

    /**
     * Creates the remote references on the name repositories.<p>
     *
     * This method is used for optimization reasons. Indeed, it allows
     * the topology to avoid the dynamic resolution of the containers
     * when accessing them.<p>
     *
     * @see #getContainers()
     * @see #getContainer(int)
     * @see #getDistContainers() 
     */
    public void createNameReps() {
        nameReps.clear();
        for (int i=0; i<containers.size(); i++) {
            RemoteContainer cc = (RemoteContainer) containers.get(i);
            RemoteRef rr = null;
            if (!cc.isLocal()) {
                rr = cc.bindTo ("JAC_name_repository");
            } else {
                rr = RemoteRef.create("JAC_name_repository", 
                                      (Wrappee)NameRepository.get());
            }
            nameReps.add(rr);
        }
    }

    /**
     * Builds a topology from a set of containers.
     *
     * @param someContainers the container of the new topology
     *
     * @see #getContainers()
     * @see #getContainer(int)
     * @see #getDistContainers() 
     */
    public Topology(RemoteContainer[] someContainers) {
        containers.addAll(Arrays.asList(someContainers));
    }

    /**
     * Returns all the containers of the topology.<p>
     *
     * @return an array of containers
     * 
     * @see #getContainer(int)
     * @see #getDistContainers() 
     */
    public RemoteContainer[] getContainers() {
        RemoteContainer[] res = new RemoteContainer[containers.size()];
        System.arraycopy(containers.toArray(), 0, res, 0, res.length);
        return res;
    }

    /**
     * Returns a given container.<p>
     *
     * @param index a valid index
     * @return the container that corresponds to the index
     *
     * @see #getContainers()
     * @see #getDistContainers()
     */
    public RemoteContainer getContainer(int index) {
        return (RemoteContainer)containers.get(index);
    }

    /**
     * Returns the container of the topology minus the local one if
     * any.<p>
     *
     * This method is typically used to deploy objects on a topology
     * (when you do not want the object to be also depoyed on the local
     * container).<p>
     * 
     * @return an array of containers
     *
     * @see org.objectweb.jac.core.dist.RemoteContainer#isLocal() 
     * @see #getContainers()
     * @see #getContainer(int)
     */
    public RemoteContainer[] getDistContainers() {
        Vector ret = new Vector();
        for ( int i=0; i<containers.size(); i++) {
            RemoteContainer cc = (RemoteContainer)containers.get(i);
            if (!cc.isLocal()) {
                ret.add (cc);
            }
        }
        RemoteContainer[] rcs = new RemoteContainer[ret.size()];
        System.arraycopy(ret.toArray(), 0, rcs, 0, ret.size());
        return rcs;
    }

    /**
     * Tells if the current topology contains the local container.
     *
     * @return true if the local container is included in the topology
     */
    public boolean containsLocal() {
        for (int i=0; i<containers.size(); i++) {
            RemoteContainer cc = (RemoteContainer)containers.get(i);
            if (cc.isLocal()) {
                return true;
            }
        }
        return false;
    }
   
    /**
     * Gets a set of remote references on all the remote replicas of a
     * local object for the current topology.
     *
     * @param localObject the local object of which the replicas are
     * seeked
     * @return the replicas references 
     */
    public Vector getReplicas(Wrappee localObject) {
        String name = NameRepository.get().getName( localObject );
        Vector vRes = new Vector();
        for ( int i = 0; i < countContainers(); i++ ) {
            logger.debug("try to find "+name+" on "+getContainer(i)+
                      " -- local container is: "+Distd.getLocalContainerName());         
            RemoteRef rr=null;
            if(!getContainer(i).getName().equals(Distd.getLocalContainerName())) {
                rr=getContainer(i).bindTo(name);
            }
            if( rr != null ) {
                vRes.add( rr );
            } else {
                logger.debug("remote object not found!");
            }
        }
        return vRes;
    }

    /**
     * Says if an object exist on one of the container of the topology
     * (excluding the local one).<p>
     *
     * @param name the name of the object to check
     * @return true if the object has been found on one of the
     * containers of the topology 
     */
    public boolean exist(String name) {
        if ( nameReps == null || nameReps.size() == 0 ) {
            createNameReps();
        }
        //      System.out.print ( " TEST THE EXISTANCE OF " + name + ".... "  );
        try {
            for ( int i = 0; i < nameReps.size(); i++ ) {
                RemoteRef nr = (RemoteRef) nameReps.get(i);
                if ( ! nr.getRemCont().isLocal() ) {
                    if ( ((Boolean)nr.invoke ( "isRegistered", 
                                               new Object[] { name } ))
                         . booleanValue() ) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            return true;
        }
        return false;
    }

    /**
     * Get the index of a container.<p>
     *
     * @param container a container
     * @return the index of the container, -1 if not part of this
     * topology
     * @see #getContainer(int)
     * @see #getContainerIndex(String)
     */
    public int getContainerIndex(RemoteContainer container) {
        return containers.indexOf(container);
    }

    /**
     * Get the index of a container from its name.<p>
     *
     * @param name a container name
     * @return the index of the container, -1 if not part of this
     * topology
     * @see #getContainer(int)
     * @see #getContainerIndex(RemoteContainer)
     * @see #getContainerIndexes(RE)
     * @see #getContainerIndexes(String[])
     */
    public int getContainerIndex(String name) {
        for ( int i = 0; i < containers.size(); i++ ) {
            if ( ((RemoteContainer)containers.get( i )).getName().equals ( name ) ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the indexes of some container regarding a regular expression
     * on the name.
     *
     * @param regexp a regular expression
     * @return a set of matching containers indexes
     *
     * @see #getContainer(int)
     * @see #getContainerIndex(String)
     * @see #getContainerIndexes(String[])
     */
    public int[] getContainerIndexes(RE regexp) {
        if ( regexp == null ) {
            return null;
        }
        Vector temp = new Vector();
        for ( int i = 0; i < containers.size(); i++ ) {
            if ( regexp.isMatch( ((RemoteContainer)containers.get( i )).getName() ) ) {
                temp.add( new Integer( i ) );
            }
        }
        int[] res = new int[temp.size()];
        for ( int i = 0; i < res.length; i++ ) {
            res[i] = ((Integer)temp.get(i)).intValue();
        }
        return res;
    }

    /**
     * Get some containers regarding a regular expression on the name.
     *
     * @param regexp a regular expression
     * @return a set of matching containers
     *
     * @see #getContainer(int)
     * @see #getContainerIndex(String)
     * @see #getContainerIndexes(String[])
     * @see #getContainerIndexes(RE) 
     */
    public RemoteContainer[] getContainers(RE regexp) {
        if ( regexp == null ) {
            return null;
        }
        Vector temp = new Vector();
        for ( int i = 0; i < containers.size(); i++ ) {
            if ( regexp.isMatch( ((RemoteContainer)containers.get(i)).getName() ) ) {
                temp.add(containers.get( i ));
            }
        }
        RemoteContainer[] res = new RemoteContainer[temp.size()];
        for ( int i = 0; i < res.length; i++ ) {
            res[i] = (RemoteContainer)temp.get(i);
        }
        return res;
    }

    /**
     * Get the indexes of some container names.<p>
     *
     * @param names an array containing the names
     * @return the array of the corresponding indexes
     *
     * @see #getContainer(int)
     * @see #getContainerIndex(RemoteContainer)
     * @see #getContainerIndexes(RE)
     */
    public int[] getContainerIndexes(String[] names) {
        int[] res = null;
        Vector vres = new Vector();
        if ( names != null && names.length != 0 ) {
            for ( int i = 0; i < names.length; i++ ) {
                int index = getContainerIndex( 
                    RemoteContainer.resolve( names[i] ) );
                if ( index != -1 ) {
                    vres.add( new Integer( index ) );
                }
            }
            res = new int[vres.size()];
            for ( int i = 0; i < res.length; i++ ) {
                res[i] = ((Integer)vres.get(i)).intValue();
            }
        }
        return res;
    }

    /**
     * Add a container to the topology if not already present.
     *
     * @param container the container to add
     *
     * @see #addContainers(RemoteContainer[])
     */
    public void addContainer(RemoteContainer container) {
        if( containers == null ) return;
        if( containers.contains( container ) ) return; 
        containers.add(container);
        fireTopologyChangeEvent();
    }
   
    /**
     * Adds a container from its name.
     *
     * @param container the container name 
     */
    public void addContainer(String container) {
        RemoteContainer rc = RemoteContainer.resolve( container ); 
        addContainer( rc );
    }

    /**
     * Add a set of containers to the topology.<p>
     *
     * @param someContainers a set of containers to add
     *
     * @see #addContainer(RemoteContainer)
     */
    public void addContainers(RemoteContainer[] someContainers) {
        if( containers == null ) return;
        for( int i=0; i < someContainers.length; i++ ) {
            addContainer(someContainers[i]);
        }
    }

    /**
     * Remove a container from the topology.<p>
     *
     * @param container the container to be removed 
     */
    public void removeContainer(RemoteContainer container) {
        containers.remove(container);
        fireTopologyChangeEvent();
    }

    /**
     * Check if the current topology contains the given container.
     *
     * @param container the container to check
     * @return true if the topology contains this container, false
     * otherwise 
     */
    public boolean isContainer(RemoteContainer container) {
        return containers.contains(container);
    }

    /**
     * Replace a container at a given index.<p>
     *
     * @param index the index where the container has to be substituted
     * @param newContainer the container to set
     *
     * @see #replaceContainer(RemoteContainer,RemoteContainer)
     */
    public void replaceContainer(int index, RemoteContainer newContainer) {
        if( newContainer == null ) return;
        if( index < 0 || index >= containers.size() ) return;
        if( containers.contains(newContainer) ) {
            containers.remove(index);
            fireTopologyChangeEvent();
            return;
        }
        containers.remove(index);
        containers.add(index,newContainer);
        fireTopologyChangeEvent();
    }

    /**
     * Replace a container by another container.<p>
     *
     * @param oldContainer the container to replace (must be in the topology)
     * @param newContainer the replacing container
     *
     * @see #replaceContainer(int,RemoteContainer)
     */
    public void replaceContainer(RemoteContainer oldContainer,
                                 RemoteContainer newContainer) {
        replaceContainer(containers.indexOf(oldContainer),newContainer);
    }

    /**
     * Gets a container from its name.
     *
     * @param name the name to seek
     * @return the container, null if not found in the topology 
     */
    public RemoteContainer getContainer(String name) {
        RemoteContainer rc = RemoteContainer.resolve(name);
        int index = containers.indexOf(rc);
        if( index == -1 ) return null;
        return (RemoteContainer)containers.get(index);
    }

    /**
     * Returns the number of containers in this topology.<p>
     *
     * @return the containers count 
     */
    public int countContainers() {
        return containers.size();
    }

    /**
     * Notifies everybody that the topology changed.
     */
    protected void fireTopologyChangeEvent() {
        if( bootstrapFlag == false && this == Topology.get() ) {
            loggerDist.debug("topology changed");
            // notify all the system objects
            Wrapping.invokeRoleMethod((Wrappee)ApplicationRepository.get(),
                                      /* org.objectweb.jac.aspects.distribution.consistency.ConsistencyWrapper, */
                                      "invalidateTopology",ExtArrays.emptyObjectArray);
            Wrapping.invokeRoleMethod((Wrappee)Topology.get(),
                                      /* org.objectweb.jac.aspects.distribution.consistency.ConsistencyWrapper, */
                                      "invalidateTopology",ExtArrays.emptyObjectArray);
            // notify the aspects
            ((ACManager)ACManager.get()).whenTopologyChanged();
        }
    }


    /**
     * Dump the containers of the topology.
     */
    public void dump () {
        System.out.println ("Dumping the topology:");
        System.out.println (containers);
    }

    /**
     * Launches the administration GUI on the local container. 
     */
    public void launchGUI(String programName) {
        Object guiAC = ACManager.get().getObject("gui");
        Object display = null;
        try {
            display = guiAC.getClass().getMethod("createSwingDisplay", 
                                                 new Class[] { Class.class } )
                .invoke(guiAC, 
                        new Object[] {Class.forName("org.objectweb.jac.aspects.gui.ProgramView")});
            display.getClass().getMethod("setProgram",new Class[] {String.class})
                .invoke( display, new Object[] {programName});
        } catch (Exception e) {
            logger.error("launchGUI "+programName,e);
        }
    }

    /**
     * Start the admin GUI
     */
    public void launchGUI() {
        Object guiAC = ACManager.get().getObject("gui");
        Object display = null;
        try {
            display = guiAC.getClass().getMethod("createSwingDisplays", 
                                                 new Class[] { String[].class })
                .invoke(guiAC, new Object[] {new String[] {"admin"}});
        } catch (Exception e) {
            logger.error("launchGUI",e);
        }
    }

    /**
     * Start some swing GUIs for an application.
     *
     * @param application the name of the application for which to
     * start the GUIs
     * @param guiNames the names of the GUI windows to start
     */
    public void startSwingGUI(String application, String[] guiNames) {
        loggerAspects.debug(
            "Start Swing GUI: "+application+" "+Arrays.asList(guiNames));
        ACManager acm = (ACManager)ACManager.get();
        Object guiAC = acm.getObject(application+".gui");
        // We use reflection here because we do not want to depend on
        // the GuiAC
        try {
            Class guiACClass = Class.forName("org.objectweb.jac.aspects.gui.GuiAC");
            Method method = guiACClass.getMethod("createSwingDisplays",
                                                 new Class[] {String[].class});
            method.invoke(guiAC,new Object[] {guiNames});
        } catch (Exception e) {
            logger.error("startSwingGUI "+application+","+Arrays.asList(guiNames),e);
        }
    }

    /** 
     * Reload the configuration of an aspect 
     */ 
    public void reloadAspect(String applicationName, String aspectName) {
        ACManager.getACM().reloadAspect(applicationName,aspectName); 
    }

	public void unweaveAspect(String applicationName, String aspectName) {
		//Application app=ApplicationRepository.get().getApplication(applicationName);
		ACManager.getACM().unregister(applicationName+"."+aspectName); 
	}

	public void weaveAspect(String applicationName, String aspectClassName, String configPath) {
		Application app=ApplicationRepository.get().getApplication(applicationName);
		new ACConfiguration(app,aspectClassName,configPath,true).weave();
	}

    /**
     * Set a trace logging level
     * @see org.objectweb.jac.util.Log#trace(String,int,String)
     */
    public void setTrace(String application, String category, int level) {
        loggerAspects.debug("Setting trace "+category+"="+level); 
        Logger.getLogger(category).setLevel(Level.toLevel(level));      
    }

    /**
     * Get a textual representation of the topology.
     *
     * @return a string describing the topology */

    public String toString() {
        if( containers == null ) return "null";
        return containers.toString();
    }
   
}
