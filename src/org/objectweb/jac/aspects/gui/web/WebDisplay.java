/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;
import org.mortbay.util.MultiException;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.ExtBoolean;
import org.objectweb.jac.util.Strings;

/**
 * This class provides a server for web clients using a thin client
 * protocol and implements the <code>Display</code> interface to
 * assume data inputs and outputs between JAC objects and web clients.
 *
 * <p>This inferface is typically used by Java web clients and more
 * specifically by servlets. The implementation provided by JAC is
 * <code>JacServlet</code>.
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> 
 */
public class WebDisplay implements CustomizedDisplay {
    static Logger logger = Logger.getLogger("display");
    static Logger loggerWeb = Logger.getLogger("web");
    static Logger loggerViews = Logger.getLogger("web.views");
    static Logger loggerRequest = Logger.getLogger("web.request");
    static Logger loggerHtml = Logger.getLogger("web.html");
    static Logger loggerEditor = Logger.getLogger("gui.editor");


    ViewFactory factory;
    // customizedID -> customized
    Hashtable frames = new Hashtable();

    /* The session */
    Session session;

    /**
     * Create a new web display
     * @param factory the ViewFactory of the display
     * @param sessionId the sessionId of the display
     */
    public WebDisplay(ViewFactory factory, String sessionId) {
        this.factory = factory;
        session = new Session(sessionId);
        Collaboration.get().addAttribute( 
            SessionAC.SESSION_ID, sessionId );
    }

    public static final String RESPONSE = "gui.web.response";
    public static final String REQUEST = "gui.web.request";
    public static final String ON_ENTER_ACTION = "GuiAC.web.ON_ENTER_ACTION";

    /**
     * Set the servlet response for the current collaboration
     * @param response the servlet response object
     */
    public static void setResponse(HttpServletResponse response) {
        Collaboration.get().addAttribute(RESPONSE,response);
    }
    /**
     * Returnsx the servlet response of the current collaboration
     * @return the servlet response of the current collaboration
     */
    public static HttpServletResponse getResponse() {
        return (HttpServletResponse)Collaboration.get().getAttribute(RESPONSE);
    }

    /**
     * Sets the current JacRequest in the context.
     * @param request the JacRequets
     */ 
    public static void setRequest(JacRequest request) {
        loggerRequest.debug("Setting request for "+Strings.hex(Collaboration.get())+
                            ": "+request);
        Collaboration.get().addAttribute(REQUEST,request);
    }

    /**
     * Returns the current JacRequest contained in the context.
     * @return the current JacRequest contained in the context
     */
    public static JacRequest getRequest() {
        return (JacRequest)Collaboration.get().getAttribute(REQUEST);
    }

    protected static void grabResponse() {
        setResponse(null);
        //getRequest().setResponse();
        //setResponse(null);
    }

    /**
     * Read values of field editors from http request
     * @param editors field editors for which to read the value
     * @param request the http request where to read values from
     * @param commit if true, commit() will be called on the field
     * editors after readValue() 
     * @see HTMLEditor#readValue(Object)
     * @see HTMLEditor#commit()
     */
    public static void readValues(DisplayContext editors, JacRequest request, 
                                  boolean commit) 
    {
        loggerEditor.debug("reading values from context "+editors);
        Iterator i = editors.getEditors().iterator();
        while (i.hasNext()) {
            View editor = (View)i.next();
            try {
                Object parameter = request.getParameter(editor.getLabel());
                loggerEditor.debug("reading value for "+editor.getLabel());
                if (editor instanceof JacRequestReader) {
                    ((JacRequestReader)editor).readValue(request);
                } else {
                    if (((FieldEditor)editor).isEnabled()) {
                        HTMLEditor htmlEditor = (HTMLEditor)editor;
                        if (htmlEditor.readValue(parameter) && commit) {
                            try {
                                htmlEditor.commit();
                            } catch (Exception e) {
                                throw new CommitException(
                                    e,
                                    ((FieldEditor)editor).getSubstance(),
                                    ((FieldEditor)editor).getField());
                            }
                        }
                    }
                }
            } catch (CommitException e) {
                throw e;
            } catch(Exception e) {
                loggerEditor.error("Failed to readValue for "+editor+"/"+editor.getLabel(),e);
            }
        }
    }

    public static void readValuesAndRefresh(
        DisplayContext editors, JacRequest request, 
        boolean commit) 
    {
        try {
            readValues(editors,request,commit);
            editors.getDisplay().refresh();
        } catch(CommitException e) {
            editors.getDisplay().showError(
                "Commit error",
                "Failed to set value of "+e.getField()+
                " on "+GuiAC.toString(e.getObject())+
                ": "+e.getNested().getMessage());
        }
    }

    public void fullRefresh() {
        Iterator it = frames.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            View frame = (View)entry.getValue();
            CustomizedGUI customized = ((CustomizedView)frame).getCustomizedGUI();
            frame.close(true);
            View newframe = factory.createView(
                customized.getTitle(),"Customized",
                new Object[] {customized,null},
                new DisplayContext(this,null));
            logger.debug("frame created "+newframe);
            frames.put(entry.getKey(),newframe);
            session.newRequest(new Request(newframe));
        }
    }

    public void showCustomized(String id, Object object) {
        showCustomized(id,object,null);
    }

    public void showCustomized(String id, Object object, Map panels) {
        logger.debug("showCustomized("+id+","+object+")");
        try {
            CustomizedGUI customized = (CustomizedGUI)object;
            if (frames.get(id)!=null) {
                if (panels!=null) {
                    CustomizedView frame = (CustomizedView)frames.get(id);
                    GenericFactory.initCustomized(frame.getFactory(), 
                                                  frame.getContext(), 
                                                  frame.getPanelView(), 
                                                  customized, panels);         
                }
                refresh();
            } else {
                HTMLViewer frame = (HTMLViewer)factory.createView(
                    customized.getTitle(),"Customized",
                    new Object[] {customized,panels},
                    new DisplayContext(this,null));
                logger.debug("frame created");
                frames.put(id,frame);
                session.newRequest(new Request((View)frame));
                refresh();
            }
        } catch(Exception e) {
            e.printStackTrace();
            showError("showCustomized error",e.toString());
        }
    }

    public void show(Object object) {
        logger.debug("show("+object+")");
        show(object,false);
    }

    public void show(Object object,
                     String viewType, Object[] viewParams) {
        logger.debug("show("+object+","+viewType+","+
                  (viewParams!=null?Arrays.asList(viewParams):null)+")");
        show(object,viewType,viewParams,false);
    }
    
    protected void show(Object object, boolean newWindow) {
        show(object,
             "Object",new String[] {GuiAC.DEFAULT_VIEW},
             newWindow);
    }

    protected void show(Object object,
                        String viewType, Object[] viewParams,
                        boolean newWindow) {
        if (object==null) {
            refresh();
            /*
              } else if (object instanceof Throwable) {
              showError("Exception",object.toString());
              ((Throwable)object).printStackTrace();
            */
        } else if (object instanceof InputStream) {
            try {
                byte[] buffer = new byte[4096];
                InputStream input = (InputStream)object;
                int length;
                OutputStream output = getResponse().getOutputStream();
                while ((length=input.read(buffer))!=-1) {
                    output.write(buffer,0,length);
                }
            } catch (IOException e) {
                logger.error("Failed to output stream",e);
            } finally {
                getRequest().setResponse();
            }
        } else if (object instanceof Attachment) {
            try {
                DisplayContext context = new DisplayContext(this,null);
                HttpServletResponse response = getResponse();
                response.setContentType(((Attachment)object).getMimeType());
                response.getOutputStream().write(((Attachment)object).getData());
            } catch (IOException e) {
                logger.error("Failed to output stream",e);
            } finally {
                getRequest().setResponse();
            }
        } else {
            DisplayContext context = new DisplayContext(this,null);
            View objectView = 
                factory.createView("object",
                                   viewType,ExtArrays.add(object,viewParams),
                                   context);
            String title;
            if (object!=null) {
                Class substance_type = object.getClass();
                String tn = substance_type.getName();
            
                title = tn.substring( tn.lastIndexOf('.') + 1) + " " +
                    GuiAC.toString(object);
            } else {
                title= "<null>";
            }

            View page = factory.createView(
                title,"Window",
                new Object[] {objectView,ExtBoolean.valueOf(newWindow)},
                context);
            context.setWindow(page);
            session.newRequest(new Request(page));
            refresh();
        }
    }

    public void openView(Object object) {
        logger.debug("openView("+object+")");
        show(object,true);
    }

    public boolean showModal(Object object, String title, String header, 
                             Object parent,
                             boolean okButton, boolean cancelButton, 
                             boolean closeButton)
    {
        logger.debug("showModal("+object+","+title+")");
        DisplayContext context = new DisplayContext(this,null);
        logger.debug("new context "+context);
        View objectView = factory.createObjectView("object",object,context);

        return showModal(objectView,context,
                         title,header,parent,
                         okButton,cancelButton,closeButton);
    }

    public boolean showModal(Object object, 
                             String viewType, Object[] viewParams,
                             String title, String header, 
                             Object parent,
                             boolean okButton, boolean cancelButton, 
                             boolean closeButton)
    {
        logger.debug("showModal("+object+","+title+","+viewType+")");
        DisplayContext context = new DisplayContext(this,null);
        View objectView = factory.createView("object",viewType,
                                             ExtArrays.add(object,viewParams),context);

        return showModal(objectView,context,
                         title,header,parent,
                         okButton,cancelButton,closeButton);
    }

    public boolean showModal(View objectView, DisplayContext context,
                             String title, String header, 
                             Object parent,
                             boolean okButton, boolean cancelButton, 
                             boolean closeButton) 
    {
        logger.debug("objectView = "+objectView+", context="+Strings.hex(context));
        if (objectView instanceof org.objectweb.jac.aspects.gui.EditorContainer)
            ((org.objectweb.jac.aspects.gui.EditorContainer)objectView).setShowButtons(false);
         
        DialogView page = 
            (DialogView)factory.createView(
                title,"Dialog",
                new Object[] {objectView,parent,title,header},
                context);
        context.setWindow(page);
        session.newRequest(new Request(page));
        refresh();

        try {
            boolean result = page.waitForClose();
            WebDisplay.setResponse(((Dialog)page).getResponse());
            WebDisplay.setRequest(((Dialog)page).getRequest());
            return result;
        } catch (TimeoutException timeout) {
            addTimedoutDialog(page);
            throw timeout;
        }
    }

    public boolean showInput(Object substance, AbstractMethodItem method, 
                             Object[] parameters) 
    {
        logger.debug("showInput("+substance+","+method+","+
                  Arrays.asList(parameters)+")");
        DisplayContext dc = (DisplayContext)Collaboration.get()
            .getAttribute(GuiAC.DISPLAY_CONTEXT);
        //if (dc==null) {
        dc = new DisplayContext(this,null);
        //}
        DialogView page = GenericFactory.createInputDialog(substance,
                                                           method,parameters,dc);
        dc.setWindow(page);
        session.newRequest(new Request(page));
        refresh();
        try {
            if (page.waitForClose()) {
                EditorContainer inputView = (EditorContainer)page.getContentView();
                Iterator it = inputView.getEditors().iterator();
                int i=0;
                JacRequest request = getRequest();
                while (it.hasNext()) {
                    if (method.getParameterTypes()[i] != DisplayContext.class) {
                        FieldEditor editor = (FieldEditor)it.next();
                        method.setParameter(parameters,i,editor.getValue());
                    }
                    i++;
                }
                setResponse(((Dialog)page).getResponse());
                setRequest(((Dialog)page).getRequest());
                return true;
            } else {
                setResponse(((Dialog)page).getResponse());
                setRequest(((Dialog)page).getRequest());
                return false;
            }
        } catch (TimeoutException timeout) {
            addTimedoutDialog(page);
            throw timeout;
        }
    }

    public boolean showMessage(String message, String title,
                               boolean okButton, 
                               boolean cancelButton, 
                               boolean closeButton ) {
        logger.debug("showMessage("+message+","+title+")");
        return showModal(null,title,message,null,okButton,cancelButton,closeButton);
    }

    protected View buildMessage(String title, String message) {
        DisplayContext context=(DisplayContext)Collaboration.get()
            .getAttribute(GuiAC.DISPLAY_CONTEXT);
        if(context==null) {
            context=new DisplayContext(this,null);
        }
        View label = factory.createView("message","text",
                                        new Object[] {message,null,null},context);
        View page = factory.createView("Object view","Window",
                                       new Object[] {label,Boolean.FALSE},context);
        return page;
    }

    public void showMessage(String title, String message) {
        logger.debug("showMessage("+title+","+message+")");
        try {
            View page = buildMessage(title,message);
            session.newRequest(new Request(page));
        } finally {
            refresh();
        }
    }

    public Object showRefreshMessage(String title, String message) {
        View page = null;
        try {
            DisplayContext context=(DisplayContext)Collaboration.get()
                .getAttribute(GuiAC.DISPLAY_CONTEXT);
            if (context==null) {
                context=new DisplayContext(this,null);
            }
            View label = factory.createView("message","text",
                                            new Object[] {message,null,null},context);
            page = factory.createView("Object view","RefreshWindow",
                                           new Object[] {label},context);
            session.newRequest(new Request(page));
        } finally {
            refresh();
        }
        return page;
    }

    public void showError(String title, String message) {
        showMessage(title,message);
    }

    public void showStatus(String message) {
    }

    public void applicationStarted() {
    }
   

    public CustomizedView getCustomizedView(String customizedID) {
        return (CustomizedView)frames.get(customizedID);
    }
    public Collection getCustomizedViews() {
        return frames.values();
    }
    public ViewFactory getFactory() {
        return factory;
    }

    String displayID;

    public String getDisplayID() {
        return displayID;
    }

    public void setDisplayID(String displayID) {
        this.displayID = displayID;
    }

    public void close() {
        // close all customized guis
        Iterator i = frames.values().iterator();
        while(i.hasNext()) {
            View view = (View)i.next();
            view.close(true);
        }
    }   

    // definition of AbstractServer abstract methods

    public Object[] buildParameterValues(AbstractMethodItem method, String[] params) {
        Object[] result = new Object[params.length];
        Class[] pts = method.getParameterTypes();
        for(int i=0; i<params.length; i++)
        {
            //         HTMLEditor editor = getEditorComponent(null,method,i);
            //         result[i] = editor.getValue(params[i]);
        }
        return result;
    }

    public void refresh() {
        if (getResponse()==null) {
            logger.debug("refresh ignored since response==null");
            return;
        }
        logger.debug("refresh");
        Object view = session.getCurrentRequest().getView();
        if (view == null) {
            showError("Display error","Nothing to refresh yet");
            logger.debug("Nothing to refresh yet");
        } else {
            try {
                loggerHtml.debug("genHTML on "+view+"("+Strings.hex(getResponse())+")");
                ((HTMLViewer)view).genHTML(getResponse().getWriter());
            } catch(Exception e) {
                logger.error("refresh failed",e);
            } finally {
                getRequest().setResponse();
            } 
        }      
    }

    static HttpServer httpServer;
    public static HttpServer getHttpServer() {
        if (httpServer==null) {
            httpServer = new HttpServer();
        }
        return httpServer;
    } 

    /** Alreeady started GUIs */
    static HashSet startedGuis = new HashSet();
    /** TCP Ports the servlet engine listens to */
    static HashSet openedPorts = new HashSet();

    /**
     * Start JAC's internal webserver. You can then interact with the
     * application through an URL like this: 
     * http://<hostname>:<port>/<application>/<guiID>
     *
     * @param application name of the application
     * @param guiIDs name of windows. gui_name[:port]
     * @param defaultPort default TCP port on which to listen */
    public static void startWebServer(String application, String[] guiIDs, int defaultPort) 
        throws IOException, MultiException
    {
        loggerWeb.debug("startWebServer on port "+defaultPort);
        httpServer = getHttpServer();
        HttpContext context = httpServer.getContext("/jac");

        ServletHandler servletHandler = new ServletHandler();
        for (int i=0; i<guiIDs.length;i++) {
            if (startedGuis.contains(guiIDs[i])) {
                loggerWeb.debug("skipping already registered GUI "+guiIDs[i]);
            } else {
                loggerWeb.debug("register GUI "+guiIDs[i]);

                String split[] = Strings.split(guiIDs[i],":");
                String gui = split[0];
                int port = split.length>=2 ? Integer.parseInt(split[1]) : defaultPort;
            
                if (!openedPorts.contains(new Integer(port))) {
                    httpServer.addListener(new InetAddrPort(port));
                    openedPorts.add(new Integer(port));
                }
            
                // Make sure the servlet will have the JAC ClassLoader
                // or it won't get the right NameRepository
                context.setClassLoader(WebDisplay.class.getClassLoader());
         
                servletHandler.addServlet("Jac","/"+gui,
                                          "org.objectweb.jac.aspects.gui.web.JacLocalServlet");
            }
        }
        context.addHandler(servletHandler);

        context = httpServer.getContext("/jac/resources");
        context.setBaseResource(new ClasspathResource());
        context.setClassLoader(WebDisplay.class.getClassLoader());
        context.addHandler(new ResourceHandler());

        startWebServer();
    }

    /**
     * Start the web server if it is not already started
     */
    public static synchronized void startWebServer() throws MultiException {
        if (httpServer.isStarted()) {
            try {
                httpServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        httpServer.start();
    }

    // View -> (String)id
    Hashtable ids = new Hashtable();
    // (String)id -> View
    Hashtable views = new Hashtable();

    /**
     * Compute an id for a view and register it.
     * @param view the view to register
     * @return the id of the view
     * @see #unregisterView(View)
     * @see #getView(String)
     */
    public String registerView(View view) {
        String id = (String)ids.get(view);
        if (id==null) {
            id = Integer.toString(view.hashCode());
            ids.put(view,id);
            views.put(id,view);
            loggerViews.debug(toString()+".registerView("+view+") -> "+id);
        }
        return id;
    }

    /**
     * Unregister a view.
     * @param view the view to register
     * @see #registerView(View)
     */
    public void unregisterView(View view) { 
        Object id = ids.get(view);
        loggerViews.debug(toString()+".unregisterView("+view+") "+id);
        ids.remove(view);
        if (id!=null) {
            views.remove(id);
        }
    }

    /**
     * Returns the view registered with a given id
     * @param id the id of the view
     * @return the registered view with that id, null if there is none
     * @see #registerView(View)
     */
    public View getView(String id) {
        loggerViews.debug(toString()+".getView("+id+")");
        return (View)views.get(id);
    }

    /**
     * Gets the ID of a registered view.
     *
     * @param view the view
     * @return the ID of the view 
     */
    public String getViewID(View view) {
        return (String)ids.get(view);
    }

    // Strings
    HashSet timedoutDialogs = new HashSet();
    public void addTimedoutDialog(DialogView dialog) {
        timedoutDialogs.add(getViewID(dialog));
    }
    /**
     * Tells wether a view ID corresponds to a timedout dialog
     */
    public boolean isTimedout(String viewID) {
        return timedoutDialogs.contains(viewID);
    }

    String servletName;
    /**
     * Set the name of the servlet
     * @param name the name of the servlet
     */
    public void setServletName(String name) {
        servletName = name;
    }
    /**
     * Returns the name of the servlet
     */
    public String getServletName() {
        return servletName;
    }

    public void closeWindow(View window, boolean validate) {
        loggerWeb.debug("closeWindow "+window);
        Iterator i = session.getRequests().iterator();
        Request request = null;
        while (i.hasNext()) {
            request = (Request)i.next();
            if (request.getView().equals(window)) {
                // we MUST do the close() before remove() because if
                // close() fails with an exception, we do not want to
                // remove.
                ((View)request.getView()).close(validate);
                session.getRequests().remove(request);
                break;
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public boolean fillParameters(AbstractMethodItem method, Object[] parameters) {
        Class[] paramTypes = method.getParameterTypes();
        for (int i=0; i<paramTypes.length; i++) {
            if (Writer.class.isAssignableFrom(paramTypes[i]) 
                && parameters[i]==null) 
            {
                String type = (String)method.getAttribute(GuiAC.MIME_TYPE);
                if (type!=null) {
                    logger.info("Setting mime-type: "+type);
                    getResponse().setContentType(type+"; "+GuiAC.getEncoding());
                }
                try {
                    parameters[i] = 
                        new OutputStreamWriter(
                            getResponse().getOutputStream(),
                            GuiAC.getEncoding());
                    grabResponse(); // So that no one else can used it
                } catch(Exception e) {
                    logger.error("Failed to set Writer parameter "+i+
                                 " for "+method.getLongName(),e);
                }
                return paramTypes.length<=1;
            } else if (OutputStream.class.isAssignableFrom(paramTypes[i]) 
                       && parameters[i]==null) {
                String type = (String)method.getAttribute(GuiAC.MIME_TYPE);
                if (type!=null) {
                    logger.info("Setting mime-type: "+type);
                    getResponse().setContentType(type+"; "+GuiAC.getEncoding());
                }
                try {
                    parameters[i] = getResponse().getOutputStream();
                    grabResponse(); // So that no one else can used it
                } catch(Exception e) {
                    logger.error("Failed to set OutputStream parameter "+i+
                                 " for "+method.getLongName(),e);
                }
                return paramTypes.length<=1;
            }
        }

        return false;
    }

    /**
     * Ensure that we will send any HTML output to the currently connection.
     */
    public void onInvocationReturn(Object substance, AbstractMethodItem method) {
        Class[] paramTypes = method.getParameterTypes();
        for (int i=0; i<paramTypes.length; i++) {
            // TODO: only call setResponse() for the invocation for
            // which the HttpResponse was grabbed
            if (Writer.class.isAssignableFrom(paramTypes[i]) || 
                OutputStream.class.isAssignableFrom(paramTypes[i]))
            {
                getRequest().setResponse();
            }
        }
    }

}
