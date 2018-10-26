/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
                          Renaud Pawlak <renaud@aopsys.com>

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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.authentication.AuthenticationAC;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.AttributeController;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ObjectArray;

/**
 * This aspect handles users within an application.
 *
 * <p>Any class of the application can be declared as a user
 * representation. The aspect configurator should then declare which
 * are the fields of this class that corresponds to the user's id
 * (that is used to login) and to the password (not required). Users
 * can then be bounded to profiles that define what are the elements
 * of the application that have the right to access or not.
 *
 * <p>A profile has a list of rules associated to it. When the aspect
 * needs to know if a user with a given profile is allowed to acess
 * resource, it inspects the rules of the profile in the order in
 * their declaration order, and as soon as a rule matches the
 * resource, this rule determines if the user is granted access to the
 * resource. A resource is a field or a method of a class. If the
 * profile inherits another profile, the rules of the inherited
 * profile are examined first.</p>
 *
 * @see #setUserClass(ClassItem,String,String,String)
 * @see #declareProfile(String) 
 * @see #declareProfile(String,String) 
 * @see Rule
 * @see Profile
 */

public class UserAC
	extends AspectComponent
	implements UserConf, AttributeController 
{
    public static final Logger logger = Logger.getLogger("user");
    public static final Logger loggerAuth = Logger.getLogger("auth");
    public static final Logger loggerProfile = Logger.getLogger("profile");
    public static final Logger loggerFilter = Logger.getLogger("profile.filter");

	public static final String USER = "UserAC.USER";
	public static final String CONTEXTUAL_PROFILE = "UserAC.CONTEXTUAL_PROFILE";
	public static final String HABILITATION = "UserAC.HABILITATION";
	public static final String FILTER = "UserAC.FILTER"; // MethodItem

	/**
	 * The default controller registers its
	 * <code>controlAttribute</code> method as an access controller for
	 * the RTTI. */

	public UserAC() {
		blockKeywords = new String[] { "profile" };
		try {
			MetaItem.registerAccessController(this);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void setContextualProfile(ClassItem cl,
                                     String field,
                                     String profile)
    {
		loggerProfile.debug(
			"defined contextual profile: " + field + " <--> " + profile);
		cl.getField(field).setAttribute(CONTEXTUAL_PROFILE, profile);
	}

	/**
	 * Adds a contextually profiled user.
	 *
	 * @param substance object whose field(s) to set
	 * @param user user object
	 * @param profile set fields tagged with this profile
	 */

	public static void addContextualProfiledUser(
		Object substance,
		Object user,
		Profile profile) {

		Collection c =
			ClassRepository.get().getClass(substance).getTaggedMembers(
				CONTEXTUAL_PROFILE,
				profile.getName());

		loggerProfile.debug("found contextual profiles for: " + c);
		Iterator it = c.iterator();
		while (it.hasNext()) {
			MemberItem member = (MemberItem) it.next();
			try {
				if (member instanceof CollectionItem) {
					((CollectionItem) member).addThroughAdder(substance, user);
				} else if (member instanceof FieldItem) {
					((FieldItem) member).setThroughWriter(substance, user);
				}
			} catch (Exception e) {
				logger.error(
					"Failed to set field "+ substance + "." + member
						+ " with " + user
                    , e);
			}
		}
	}

	public Profile getProfileFromUser(Object user) {
		return (Profile) profileField.getThroughAccessor(user);
	}

	public void setProfileToUser(Object user, Profile profile) {
		try {
			profileField.setThroughWriter(user, profile);
		} catch (Exception e) {
			logger.error(
				"Failed to set profile of user ("
                + user + "." + profileField + ") with " + profile, 
                e);
		}
	}

	/**
	 * Gets the profiles of a user for checking access rights for an
	 * object.
	 *
	 * <p>If the user is the owner of the checked object, the "owner"
	 * profile is returned in addition to the user's profile.</p> 
	 *
	 * @param authuser the user's name
	 * @param substance the checked object
	 */

	protected List getProfiles(String authuser, Object substance) {
		Vector profiles = new Vector();
		Object user = getUserFromLogin(authuser);
		Profile profile = getProfileFromUser(user);
		if (profile != null)
			profiles.add(profile);
		if (substance != null) {
			Object owner = getOwner(substance);
			// We consider that everybody is the owner of an object with
			// no owner so that new objects whose owner is not set yet
			// can be edited
			if (owner == user || owner == null) {
				profile = userManager.getProfile("owner");
				if (profile != null)
					profiles.add(profile);
			}

			// gets the contextual profiles
			Collection fs =
				ClassRepository.get().getClass(substance).getTaggedFields(
					CONTEXTUAL_PROFILE,
					false);
			if (fs.size() > 0) {
				Iterator it = fs.iterator();
				while (it.hasNext()) {
					FieldItem field = (FieldItem) it.next();
					if ((field instanceof CollectionItem
						&& ((CollectionItem) field).getActualCollection(
							substance).contains(
							user))
						|| (field.isReference()
							&& field.getThroughAccessor(substance) == user)) {
						profile =
							userManager.getProfile(
								(String) field.getAttribute(
									CONTEXTUAL_PROFILE));
						if (profile != null)
							profiles.add(profile);
					}
				}
			}
		}

		loggerProfile.debug("Profiles=" + profiles);
		return profiles;
	}

	Hashtable usersCache = new Hashtable();

	/**
	 * Invalidate controlAttribute's cache.
	 *
	 * @see #controlAttribute(Object,MetaItem,String,Object)
	 */
	public void invalidateCache() {
		usersCache.clear();
	}

	static class BooleanThreadLocal extends ThreadLocal {
		protected Object initialValue() {
			return Boolean.FALSE;
		}
	}

	private BooleanThreadLocal inHabilitation = new BooleanThreadLocal();

	/**
	 * This method controls the access to a given meta item of the RTTI.
	 *
	 * <p>The profile of the current user is fetched and the
	 * permissions are checked against this profile. If the user owns
	 * the object being controlled, the "owner" profile is checked
	 * first.
	 *
	 * @param substance 
	 * @param item the meta item that is currently accessed
	 * @param attrName the attribute that is asked on <code>item</code>
	 * @param value the already fetched value (can be overriden or
	 * returned as is)
	 * @return the value that will finally be returned by the RTTI 
	 *
	 * @see org.objectweb.jac.core.rtti.MetaItem#getAttribute(String) 
	 * @see #invalidateCache() */

	public Object controlAttribute(
		Object substance,
		MetaItem item,
		String attrName,
		Object value) 
    {
		loggerProfile.debug(
			"controlAttribute(" + item + "," + attrName + "," + value + ")");
		// Avoid infinite recursion when displaying an error box.
		if (inHabilitation.equals(Boolean.TRUE)) {
			return value;
		}
		String authuser = (String) attr(AuthenticationAC.USER);
		if (authuser == null) {
			loggerProfile.debug(
				"user not defined, no access check for "
					+ item + "." + attrName);
			return value;
		}

		loggerProfile.debug("1");

		ClassItem cli = null;
		if (substance != null)
			cli = ClassRepository.get().getClass(substance);
		MethodItem classHabilitation = null;
		if (cli != null)
			classHabilitation = (MethodItem) cli.getAttribute(HABILITATION);
		if (classHabilitation == null)
			classHabilitation = habilitation;
		if (classHabilitation != null) {
			loggerProfile.debug("Calling habilitation " + classHabilitation);
			Object savedInhabilitation = inHabilitation.get();
			try {
				inHabilitation.set(Boolean.TRUE);
				if (attrName.equals(GuiAC.VISIBLE)
					|| attrName.equals(GuiAC.EDITABLE)
					|| attrName.equals(GuiAC.ADDABLE)
					|| attrName.equals(GuiAC.REMOVABLE)) {
					value =
						classHabilitation.invokeStatic(
							new Object[] {
								substance,
								item,
								getUserFromLogin(authuser),
								attrName });
				}
			} finally {
				inHabilitation.set(savedInhabilitation);
			}
			return value;
		}

		loggerProfile.debug("3");

		Map userCache = (HashMap) usersCache.get(authuser);
		if (userCache == null) {
			userCache = new HashMap();
			usersCache.put(authuser, userCache);
		}
		ObjectArray key =
			new ObjectArray(new Object[] { substance, item, attrName });
		//Log.trace("profile.cache." + attrName, "key = " + key);
		if (userCache.containsKey(key)) {
			/*
              Log.trace(
				"profile.cache." + attrName,
				"return cached value " + userCache.get(key));
            */
			return userCache.get(key);
		}

		if (attrName.equals(GuiAC.VISIBLE)) {
			Collection profiles = getProfiles(authuser, substance);
			if (profiles.isEmpty()) {
				;
			} else if (!Profile.isReadable(profiles, item)) {
				loggerProfile.debug(
					"overriding " + attrName + " for " + item + " -> FALSE");
				value = Boolean.FALSE;
				// do not show adders if isAddable==false
			} else if (
				item instanceof MethodItem && ((MethodItem) item).isAdder()) {
				CollectionItem[] addedColls =
					((MethodItem) item).getAddedCollections();
				for (int i = 0; i < addedColls.length; i++) {
					if (!Profile.isAddable(profiles, addedColls[i])) {
						loggerProfile.debug(
							"overriding " + attrName
                            + " for " + item + " -> FALSE");
						value = Boolean.FALSE;
						break;
					}
				}
				// do not show removers if isRemovable==false
			} else if (
				item instanceof MethodItem
					&& ((MethodItem) item).isRemover()) {
				CollectionItem[] removedColls =
					((MethodItem) item).getRemovedCollections();
				for (int i = 0; i < removedColls.length; i++) {
					if (!Profile.isRemovable(profiles, removedColls[i])) {
						loggerProfile.debug(
							"overriding " + attrName
								+ " for " + item + " -> FALSE");
						value = Boolean.FALSE;
						break;
					}
				}
			}
		} else if (attrName.equals(GuiAC.EDITABLE)) {
			Collection profiles = getProfiles(authuser, substance);
			if (profiles.isEmpty()) {
				;
			} else if (!Profile.isWritable(profiles, item)) {
				loggerProfile.debug(
					"overriding " + attrName + " for " + item + " -> FALSE");
				value = Boolean.FALSE;
			}
		} else if (attrName.equals(GuiAC.ADDABLE)) {
			Collection profiles = getProfiles(authuser, substance);
			if (profiles.isEmpty()) {
				;
			} else if (!Profile.isAddable(profiles, item)) {
				loggerProfile.debug(
					"overriding " + attrName + " for " + item + " -> FALSE");
				value = Boolean.FALSE;
			}
		} else if (attrName.equals(GuiAC.REMOVABLE)) {
			Collection profiles = getProfiles(authuser, substance);
			if (profiles.isEmpty()) {
				;
			} else if (!Profile.isRemovable(profiles, item)) {
				loggerProfile.debug(
					"overriding " + attrName + " for " + item + " -> FALSE");
				value = Boolean.FALSE;
			}
		} else if (attrName.equals(GuiAC.CREATABLE)) {
			Collection profiles = getProfiles(authuser, substance);
			if (profiles.isEmpty()) {
				;
			} else if (!Profile.isCreatable(profiles, item)) {
				loggerProfile.debug(
					"overriding " + attrName + " for " + item + " -> FALSE");
				value = Boolean.FALSE;
			}
		}
		userCache.put(key, value);
		return value;
	}

	/**
	 * Returns the user that is currently logged in.
	 *
	 * @return the user */

	public Object getCurrentUser() {
		String authuser =
			(String) Collaboration.get().getAttribute(AuthenticationAC.USER);
		if (authuser != null) {
			return getUserFromLogin(authuser);
		}
		return null;
	}

	/** login -> User */
	Hashtable cachedUsers = new Hashtable();

	/**
	 * Gets a user from its login as defined in <code>setUserClass</code>.
	 *
	 * @param login the user's id
	 * @return the user (instance of the user's class)
	 * @see #setUserClass(ClassItem,String,String,String)
	 * @see #getUserLogin(Object)
	 * @see #getUserPassword(Object) */

	public Object getUserFromLogin(String login) {
		Object user = null;
		user = cachedUsers.get(login);
		if (user == null) {
			Collection users = ObjectRepository.getObjects(userClass);
			Iterator i = users.iterator();
			// Find a user whose loginField matches the authenticated username
			while (i.hasNext()) {
				Object testedUser = i.next();
				Object id = loginField.getThroughAccessor(testedUser);
				if (login.equals(id)) {
					user = testedUser;
					break;
				}
			}
			if (login != null && user != null)
				cachedUsers.put(login, user);
		}
		return user;
	}

	/**
	 * Gets the login value for a given user.
	 *
	 * @param user the user object
	 * @return its login value (<code>null<code> if the user is
	 * <code>null</code> or if <code>setUserClass</code> is not
	 * correctly defined)
	 * @see #setUserClass(ClassItem,String,String,String)
	 * @see #getUserFromLogin(String) */

	public String getUserLogin(Object user) {
		if (user == null)
			return null;
		if (loginField == null)
			return null;
		return (String) loginField.getThroughAccessor(user);
	}

	/**
	 * Gets the password value for a given user.
	 *
	 * @param user the user object
	 * @return its password value (<code>null<code> if the user is
	 * <code>null</code> or if <code>setUserClass</code> is not
	 * correctly defined)
	 * @see #setUserClass(ClassItem,String,String,String)
	 * @see #getUserFromLogin(String) */

	public String getUserPassword(Object user) {
		if (user == null)
			return null;
		if (passwordField == null)
			return null;
		return (String) passwordField.getThroughAccessor(user);
	}

	/**
	 * Gets the login for the currently logged user.
	 *
	 * @return the login
	 * @see #getCurrentUser() */

	public String getCurrentUserLogin() {
		Object currentUser = getCurrentUser();
		if (currentUser == null)
			return null;
		if (loginField == null)
			return null;
		return (String) loginField.getThroughAccessor(currentUser);
	}

	/**
	 * Gets the password for the currently logged user.
	 *
	 * @return the password
	 * @see #getCurrentUser() */

	public String getCurrentUserPassword() {
		Object currentUser = getCurrentUser();
		if (currentUser == null)
			return null;
		if (passwordField == null)
			return null;
		return (String) passwordField.getThroughAccessor(currentUser);
	}

	/**
	 * This controlling method can be used by the authentification
	 * aspect to control that the authenticated user is valid.
	 *
	 * @param username the username that is given by the authenticator
	 * @param wrappee the object that is currently accessed
	 * @param method the method that is currently called
	 * @see org.objectweb.jac.aspects.authentication.AuthenticationAC
	 * @see org.objectweb.jac.aspects.authentication.AuthenticationAC#setController(String,String,MethodItem)
	 */
	public static boolean userController(
		String username,
		Object wrappee,
		MethodItem method) 
    {
		loggerAuth.debug("userController(" + username + "...)");
		if (username == null)
			return false;
		UserAC userAC = (UserAC) ACManager.getACM().getAC("user");
		return username.equals(userAC.getCurrentUserLogin());
	}

	public ClassItem getUserClass() {
		return userClass;
	}

	public FieldItem getLoginField() {
		return loginField;
	}

	// UserConf interface

	UserWrapper wrapper = new UserWrapper(this);

	ClassItem userClass;
	FieldItem loginField;
	FieldItem passwordField;
	FieldItem profileField;

	public void setUserClass(
		ClassItem userClass,
		String loginField,
		String passwordField,
		String profileField) {
		/*if(!User.class.isAssignableFrom(userClass.getActualClass())) {
		   Log.error(userClass+" MUST implement org.objectweb.jac.aspects.user.User.\n"+
		             "Please correct this and start the application again.");
		   System.exit(-1);
		   }*/
		this.userClass = userClass;
		this.loginField = userClass.getField(loginField);
		if (passwordField != null) {
			this.passwordField = userClass.getField(passwordField);
		}
		if (profileField != null) {
			this.profileField = userClass.getField(profileField);
		}
	}

	public void defineAdministrator(String login, String password) {
		if (userClass == null) {
			logger.error(
				"cannot define administrator if no user class is defined before.");
			return;
		}
		if (getUserFromLogin(login) != null) {
			logger.debug("Admin user `" + login + "' already defined");
			// admin already defined
			return;
		}
		Object admin = null;
		try {
			admin = userClass.newInstance();
		} catch (Exception e) {
			logger.error("administrator instantiation failed",e);
			return;
		}
		try {
			loginField.setThroughWriter(admin, login);
		} catch (Exception e) {
			logger.error(
				"Failed to set login of admin ("
					+ admin + "." + loginField + ") with " + login
                , e);
		}
		try {
			passwordField.setThroughWriter(admin, password);
		} catch (Exception e) {
			logger.error(
				"Failed to set password of admin ("
					+ admin + "." + passwordField + ") "
					, e);
		}
		setProfileToUser(admin, userManager.getProfile("administrator"));
		userManager.setDefaultAdmin((Wrappee) admin);
		logger.debug("Created admin user " + admin);
	}

	public void autoInitClasses(String classExpr) {
		logger.debug("autoInitClasses " + classExpr);
		pointcut(
			"ALL",
			classExpr,
			"CONSTRUCTORS",
			wrapper,
			null);
	}

	public void autoInitClasses(ClassItem cl,
                                String triggerClassExpr,
                                String triggerMethodExpr) 
    {
		logger.debug(
			"autoInitClasses " + cl + 
            " on " + triggerClassExpr + "." + triggerMethodExpr);
		wrapper.addClass(cl);
		pointcut(
			"ALL",
			triggerClassExpr,
			triggerMethodExpr,
			wrapper,
			null);
	}

	UserManager userManager = new UserManager();

	public UserManager getUserManager() {
		return userManager;
	}

	public void declareProfile(String name) {
		Collection profiles =
			ObjectRepository.getObjects(Profile.class);
		Iterator it = profiles.iterator();
		while (it.hasNext()) {
			Profile cur = (Profile) it.next();
			if (cur.getName().equals(name)) {
				loggerProfile.info("profile " + name + " already defined");
				cur.setIsNew(false);
                userManager.addProfile(cur);
				//cur.clear();
				return;
			}
		}
		Profile profile = new Profile(name);
		userManager.addProfile(profile);
	}

	public void declareProfile(String name, String parent) {
		Collection profiles =
			ObjectRepository.getObjects(Profile.class);
		Iterator it = profiles.iterator();
		while (it.hasNext()) {
			Profile cur = (Profile) it.next();
			if (cur.getName().equals(name)) {
				loggerProfile.info("profile " + name + " already defined");
				cur.setIsNew(false);
                userManager.addProfile(cur);
				//cur.clear();
				return;
			}
		}
		Profile parentProfile = userManager.getProfile(parent);
		Profile profile = new Profile(name,parentProfile);
		userManager.addProfile(profile);
	}

	public Profile getProfile(String name) {
		//(Profile)ObjectRepository.getObjects("org.objectweb.jac.aspects.user.Profile","name='"+name+"'");
		Profile profile = userManager.getProfile(name);
		if (profile == null)
			throw new RuntimeException("No such profile " + name);
		return profile;
	}

	/**
	 * Use this config method to clear a profile so that it can be
	 * reinitialized from the config file.
	 * @param name name of the profile to clear
	 */
	public void clearProfile(String name) {
		Profile profile = getProfile(name);
		profile.clear();
		profile.setIsNew(true);
	}

	public void addReadable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addReadable(resourceExpr);
	}

	public void addUnreadable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addUnreadable(resourceExpr);
	}

	public void addWritable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addWritable(resourceExpr);
	}

	public void addUnwritable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addUnwritable(resourceExpr);
	}

	public void addRemovable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addRemovable(resourceExpr);
	}

	public void addUnremovable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addUnremovable(resourceExpr);
	}

	public void addAddable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addAddable(resourceExpr);
	}

	public void addCreatable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addCreatable(resourceExpr);
	}

	public void addUnaddable(String profile, String resourceExpr) {
		if (getProfile(profile).isNew())
			getProfile(profile).addUnaddable(resourceExpr);
	}

	public MethodItem habilitation;

	public void defineHabilitation(MethodItem habilitation) {
		this.habilitation = habilitation;
	}

	public void defineHabilitation(ClassItem cli, MethodItem habilitation) {
		cli.setAttribute(HABILITATION, habilitation);
	}

	public void addOwnerFilter(
		String profile,
		ClassItem cl,
		String collectionName) {
		pointcut(
			"ALL",
			cl.getName(),
			"GETTERS(" + collectionName + ")",
			"org.objectweb.jac.aspects.user.UserAC$OwnerFilterWrapper",
			new Object[] { profile },
			null,
			SHARED);
	}

	public void addFilter(CollectionItem collection, MethodItem filter) {
		collection.setAttribute(FILTER, filter);
		pointcut(
			"ALL",
			collection.getClassItem().getName(),
			"GETTERS(" + collection.getName() + ")",
			"org.objectweb.jac.aspects.user.UserAC$FilterWrapper",
			null,
			SHARED);
	}

	public class FilterWrapper extends Wrapper {
		public FilterWrapper(AspectComponent ac) {
			super(ac);
		}
		public Object filterResult(Interaction interaction) {
			Object result = proceed(interaction);
			String authuser = (String) this.attr(AuthenticationAC.USER);
			if (authuser == null) {
				logger.debug(
					"user not defined, cannot filter " + interaction.method);
				return result;
			}
            FieldItem returnedField = ((MethodItem)interaction.method).getReturnedField();
            if (!(returnedField instanceof CollectionItem)) {
                logger.warn("Cannot filter non collection field "+returnedField+
                            " returned by "+interaction.method);
                return result;
            }
            CollectionItem collection = (CollectionItem)returnedField;
			if (collection == null) {
				logger.debug(
					"no returned collection for " + interaction.method);
				return result;
			}
			logger.debug(
				"filtering collection " + collection + ", user=" + authuser);
			MethodItem filter = (MethodItem) collection.getAttribute(FILTER);
			if (filter == null) {
				logger.debug("no filter for " + collection);
				return result;
			}
			return filter.invokeStatic(
				new Object[] {
					result,
					interaction.wrappee,
					collection,
					getUserFromLogin(authuser)});
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			return filterResult((Interaction) invocation);
		}
	}

	public class OwnerFilterWrapper extends Wrapper {
		public OwnerFilterWrapper(AspectComponent ac) {
			super(ac);
		}

		public OwnerFilterWrapper(AspectComponent ac, String profileName) {
			super(ac);
			this.profileName = profileName;
		}

		String profileName;

		/**
		 * Filters the result of a collection's getter to keep only the
		 * object that are owned by the currently logged user. 
		 */
		public Object filterResult(Interaction interaction) {
			loggerFilter.debug("filterResult("+interaction.method+")");
			Collection c = (Collection) proceed(interaction);
			UserAC ac = (UserAC) getAspectComponent();
			if (!ac
				.getProfileFromUser(ac.getCurrentUser())
				.getName()
				.equals(profileName)) {
				return c;
			}
			loggerFilter.debug("filtering...");
			Vector result = new Vector();
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				Object owner = ac.getOwner(o);
				if (owner == ac.getCurrentUser()) {
					result.add(o);
				}
			}
			loggerFilter.debug("returning " + result);
			return result;
		}
		public Object invoke(MethodInvocation invocation) throws Throwable {
			return filterResult((Interaction) invocation);
		}
	}

	/**
	 * <p>Returns the owner of an object.</p>
	 * 
	 * <p>The owner of an object is defined as the value of a field
	 * whose type is the type defined by <code>setUserClass</code>
	 *
	 * @param object the object
	 * @return the owner of the object, or null if the object does not
	 * have a owner.
	 */
	public Object getOwner(Object object) {
		ClassItem classItem = ClassRepository.get().getClass(object);
		FieldItem[] fields = classItem.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getTypeItem() == userClass) {
				return fields[i].getThroughAccessor(object);
			}
		}
		return null;
	}

	public String[] getDefaultConfigs() {
		return new String[] { "org/objectweb/jac/aspects/user/user.acc" };
	}

	/**
	 * Display the profiles.
	 *
	 * <p>This method can be used as a menu callback by applications.
	 */
	public static void viewProfiles(DisplayContext context, String panelID) {
		org.objectweb.jac.aspects.gui.Actions.viewObject(
			context,
			"usermanager#0",
			panelID);
	}

	public static UserManager getProfiles() {
		return (UserManager) NameRepository.get().getObject("usermanager#0");
	}

}

