/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.Writer;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;
import org.objectweb.jac.aspects.gui.CustomizedGUI;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.PanelContent;
import org.objectweb.jac.aspects.gui.TableCellViewer;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Strings;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * This servlet does the same thing as the JacServlet, but it does not
 * use RMI. It is meant to be used with the integrated http server
 * (Jetty).
 */

public class JacLocalServlet extends HttpServlet {
    static final Logger logger = Logger.getLogger("web.servlet");
    static final Logger loggerEvents = Logger.getLogger("gui.events");
    static final Logger loggerPerf = Logger.getLogger("perf");
    static final Logger loggerThreads = Logger.getLogger("threads");

    // SessionID -> WebDisplay
    Hashtable displays = new Hashtable();

    // list of possible events
    static Hashtable eventList = new Hashtable();
    static {
        eventList.put("onDirectInvoke", new Integer(1));
        eventList.put("onView", new Integer(2));
        eventList.put("onTableInvoke", new Integer(3));
        eventList.put("onSelect", new Integer(4));
        eventList.put("onInvoke", new Integer(5));
        eventList.put("onSelection", new Integer(6));
        eventList.put("onCellSelection", new Integer(7));
        eventList.put("onHeaderClick", new Integer(8));
        eventList.put("onClose", new Integer(9));
        eventList.put("onValidate", new Integer(10));
        eventList.put("onOK", new Integer(11));
        eventList.put("onCancel", new Integer(12));
        eventList.put("onAddToCollection", new Integer(13));
        eventList.put("onRemoveFromCollection", new Integer(14));
        eventList.put("onNext", new Integer(15));
        eventList.put("onPrevious", new Integer(16));
        eventList.put("onSelectNode", new Integer(17));
        eventList.put("onExpandNode", new Integer(18));
        eventList.put("onCollapseNode", new Integer(19));
        eventList.put("onMenuClick", new Integer(20));
        eventList.put("onCreateObject", new Integer(21));
        eventList.put("onLoadAttachment", new Integer(22));
        eventList.put("onPreviousInCollection", new Integer(23));
        eventList.put("onNextInCollection", new Integer(24));
        eventList.put("onBackToCollection", new Integer(25));
        eventList.put("onRemoveInCollection", new Integer(26));
        eventList.put("onRemove", new Integer(27));
        eventList.put("onFirst", new Integer(28));
        eventList.put("onLast", new Integer(29));
        eventList.put("onAddEmbedded", new Integer(30));
        eventList.put("onAddExistingToCollection", new Integer(31));
        eventList.put("onSetDefaults", new Integer(32));
        eventList.put("onViewObject", new Integer(33));
        eventList.put("onRefresh", new Integer(34));
        eventList.put("onRefreshCollection", new Integer(35));
    }

    public JacLocalServlet() {
    }

    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException 
    {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException 
    {
        //ejp.tracer.TracerAPI.enableTracing();

        logger.info("query -> "
                    + request.getQueryString() + " -> " + Strings.hex(response));
        logger.debug("request -> " + request);

        long start = System.currentTimeMillis();

        Collaboration.get().reset();
        HttpSession session = request.getSession(true);
        String sid = session.getId();

        String encoding = GuiAC.getEncoding();
        if (request.getCharacterEncoding()==null) {
            logger.info("Setting character encoding to "+encoding);
            request.setCharacterEncoding(encoding);
        }

        response.setContentType("text/html; charset="+encoding);
        response.setHeader("Cache-Control", "no-cache");

        String contentType = request.getHeader("Content-type");
        JacRequest jacRequest;
        if (contentType != null
            && contentType.startsWith("multipart/form-data")) {
            MultiPartRequest parts = new MultiPartRequest(request,encoding);
            jacRequest = new MultiPartJacRequest(parts, request);
        } else {
            jacRequest = new MultiMapJacRequest(request);
        }

        String guiID = request.getServletPath().substring(1);

        WebDisplay display = (WebDisplay) displays.get(sid+":"+guiID);
        if (display == null) {
            logger.info("new Display for session " + sid);
            display = new WebDisplay(GuiAC.getViewFactory("web"), sid);
            display.setServletName(guiID);
            displays.put(sid+":"+guiID, display);
        }
        logger.info(sid + " -> " + display);
        WebDisplay.setResponse(response);
        WebDisplay.setRequest(jacRequest);

        String event = (String) jacRequest.getParameter("event");
        String eventAndAction =
            (String) jacRequest.getParameter("eventAndAction");
        if (eventAndAction != null) {
            MultiMap params = new MultiMap();
            logger.debug("eventAndAction = " + eventAndAction);
            UrlEncoded.decodeTo(eventAndAction, params);
            logger.debug("params = " + params);

            String source = (String) jacRequest.getParameter("source");
            View dest = null;
            if (source == null) {
                source = (String)params.get("source");
                dest = display.getView(source);
                if (dest == null) {
                    error(display, jacRequest, "ERROR: no such view "+source);
                    return;
                }
                Object window = dest.getContext().getWindow();
                if (!(window instanceof WindowListener)) {
                    logger.warn("Window of "+source+" is not a WindowListener ("+window+")");
                    dest = null;
                } else {
                    dest = (View)window;
                }
            } else {
                if (source.equals(params.get("source"))) {
                    // In this case, the action will do the validationx
                    logger.debug("Skipping onValidate for "+source);
                } else {
                    dest = display.getView(source);
                    if (dest == null) {
                        error(display, jacRequest, "ERROR: no such view "+source);
                        return;
                    }
                }
            }

            if (dest != null) {
                logger.info("event = onValidate -> " + dest + "(" + source + ")");
                try {
                    loggerEvents.debug(dest+".onCancel");
                    ((WindowListener)dest).onValidate(jacRequest);
                } catch(Exception e) {
                    logger.error("Dialog validation failed for "+dest,e);
                }
            } else {
                logger.info("No source for eventAndAction");
            }
            jacRequest = new MultiMapJacRequest(params, request, jacRequest); 
            WebDisplay.setRequest(jacRequest);
            handleEvent(
                jacRequest,
                response,
                display,
                sid,
                guiID,
                GuiAC.getCustomized(guiID));
        } else {
            CustomizedGUI cgui = GuiAC.getCustomized(guiID);
            if (event == null) {
                logger.info("No event, looking for generic action");
                logger.info("  request="+jacRequest);                
                if (jacRequest.contains("showObject")) {
                    logger.info("  showObject");                
                    String name = // name of object to display
                        (String)jacRequest.getParameter("object");
                    if (name == null) {
                        error(display, jacRequest,
                              "PROTOCOL ERROR: missing object argument for showObject");
                    } else {
                        Object object = NameRepository.get().getObject(name);
                        String viewName = (String)jacRequest.getParameter("viewName");
                        if (viewName!=null) {
                            display.show(object,"Object",new Object[]{viewName});
                        } else {
                            display.show(object);
                        }
                        jacRequest.waitForResponse();                                
                    }                            
                } else {
                    HashMap panels = null;
                    for (int i = 0; i < 3; i++) {
                        String panel =
                            (String) jacRequest.getParameter("panel[" + i + "]");
                        if (panel != null) {
                            if (panels == null)
                                panels = new HashMap();
                            panels.put(
                                Integer.toString(i),
                                new PanelContent("Object", new String[] { panel }));
                        }
                    }
                    if (cgui == null) {
                        error(
                            display,
                            jacRequest,
                            "No such customized GUI: " + guiID);
                    } else {
                        new ShowThread(
                            display,
                            sid,
                            guiID,
                            cgui,
                            panels,
                            jacRequest,
                            response)
                            .start();
                        if (!jacRequest.waitForResponse()) {
                            showTimeout(response);
                        }
                    }
                }
            } else {
                handleEvent(jacRequest, response, display, sid, guiID, cgui);
            }
        }

        //ejp.tracer.TracerAPI.disableTracing();

        logger.info(
            "request completed " + request.getQueryString()
            + " -> " + Strings.hex(response));
        loggerPerf.info("page generated in " + (System.currentTimeMillis() - start) + "ms");
    }

    void showTimeout(HttpServletResponse response) throws IOException {
        Writer output = response.getWriter();
        output.write("A timeout occurred.");
    }

    void handleEvent(
        JacRequest jacRequest,
        HttpServletResponse response,
        WebDisplay display,
        String sid,
        String guiID,
        CustomizedGUI cgui) throws IOException 
    {
        String event = (String) jacRequest.getParameter("event");
        logger.info("handleEvent "+event);

        DisplayContext context = new DisplayContext(display, null);
        Collaboration.get().addAttribute(GuiAC.DISPLAY_CONTEXT,context);
        Collaboration.get().addAttribute(SessionAC.SESSION_ID, sid);

        // get Integer associated with the event, see in constructor
        Integer eventID = (Integer) eventList.get(event);

        if (eventID == null) {
            error(
                display,
                jacRequest,
                "PROTOCOL ERROR: unknown event " + event);
            return;
        }

        // simple invocation event (with no GUI interaction and
        // only string parameters supported)
        if (eventID.intValue() == 1) // onDirectInvoke
        {
            String jid = (String) jacRequest.getParameter("jid");
            String methodName = (String) jacRequest.getParameter("method");
            String strParam = (String) jacRequest.getParameter("param");
            if (jid == null) {
                error(
                    display,
                    jacRequest,
                    "PROTOCOL ERROR: missing jid argument for event " + event);
                return;
            }
            if (methodName == null) {
                error(
                    display,
                    jacRequest,
                    "PROTOCOL ERROR: missing method argument for event "
                    + event);
                return;
            }
            Object object = NameRepository.get().getObject(jid);
            if (object == null) {
                error(
                    display,
                    jacRequest,
                    "PROTOCOL ERROR: jid " + jid + " does not exist");
                return;
            } else {
                MethodItem method =
                    ClassRepository.get().getClass(object).getMethod(methodName);
                if (strParam == null) {
                    EventHandler.get().onInvoke(
                        context, 
                        new InvokeEvent(null, object, method));
                } else {
                    EventHandler.get().onInvoke(
                        context, 
                        new InvokeEvent(null, object, method, new Object[] { strParam }), 
                        false, 
                        null, null);
                }
                jacRequest.waitForResponse();
                return;
            }
        }

        String source = (String) jacRequest.getParameter("source");
        if (source == null) {
            error(
                display,
                jacRequest,
                "PROTOCOL ERROR: missing source argument for event " + event);
            return;
        }
        logger.info("  source="+source);

        if (display.isTimedout(source)) {
            display.closeWindow(display.getView(source),false);
            error(display, jacRequest, "ERROR: dialog time out");
            return;
        }
        View dest = display.getView(source);
        if (dest == null) {
            error(display, jacRequest, "ERROR: no such view " + source);
            return;
        }
        logger.info("  dest="+dest);
        logger.info("event = " + event + " -> " + dest + "(" + source + ")");

        try {
            String tableEventSource =
                (String) jacRequest.getParameter("tableEventSource");
            if (tableEventSource != null) {
                View tableDest = display.getView(tableEventSource);
                if (tableDest == null) {
                    error(
                        display,
                        jacRequest,
                        "ERROR: no such view " + tableEventSource);
                    return;
                }
                logger.info("tableEvent = " + tableEventSource + " -> " + tableDest);
                String row = (String) jacRequest.getParameter("row");
                if (row == null) {
                    error(
                        display,
                        jacRequest,
                        "PROTOCOL ERROR: missing row argument for tableEvent");
                    return;
                }
                String col = (String) jacRequest.getParameter("col");
                if (col == null) {
                    error(
                        display,
                        jacRequest,
                        "PROTOCOL ERROR: missing col argument for tableEvent");
                    return;
                }
                if (dest instanceof TableCellViewer) {
                    dest =
                        ((TableListener) tableDest).onRowEvent(
                            new Integer(row).intValue(),
                            new Integer(col).intValue());
                }
            }

            new ActionThread(
                display,
                sid,
                guiID,
                cgui,
                jacRequest,
                response,
                event,
                dest)
                .start();
            if (!jacRequest.waitForResponse()) {
                showTimeout(response);
            }

        } catch (WrappedThrowableException wrapped) {
            Throwable e = wrapped.getWrappedThrowable();
            if (e instanceof EmptyStackException) {
                new ShowThread(
                    display,
                    sid,
                    guiID,
                    cgui,
                    null,
                    jacRequest,
                    response)
                    .start();
                if (!jacRequest.waitForResponse()) {
                    showTimeout(response);
                }
            } else {
                display.showError("Exception", "Caught exception: " + e);
                if (!jacRequest.waitForResponse()) {
                    showTimeout(response);
                }
                logger.error("handleEvent("+jacRequest+")",e);
            }
        } catch (Throwable e) {
            display.showError("Exception", "Caught exception: " + e);
            if (!jacRequest.waitForResponse()) {
                showTimeout(response);
            }
            logger.error("handleEvent("+jacRequest+")",e);
        }

    }

    class ShowThread extends Thread {
        WebDisplay display;
        String guiID;
        CustomizedGUI cgui;
        String sid;
        JacRequest request;
        HttpServletResponse response;
        Map panels;

        public ShowThread(
            WebDisplay display,
            String sid,
            String guiID,
            CustomizedGUI cgui,
            Map panels,
            JacRequest request,
            HttpServletResponse response) {
            this.display = display;
            this.guiID = guiID;
            this.cgui = cgui;
            this.sid = sid;
            this.request = request;
            this.response = response;
            this.panels = panels;
        }

        public void run() {
            loggerThreads.debug("starting ShowThread " + this);
            try {
                Collaboration.get().addAttribute(
                    GuiAC.DISPLAY_CONTEXT,
                    new DisplayContext(display, null));
                Collaboration.get().addAttribute(SessionAC.SESSION_ID, sid);
                WebDisplay.setRequest(request);
                WebDisplay.setResponse(response);
                try {
                    display.showCustomized(guiID, cgui, panels);
                } catch (Exception e) {
                    logger.error("showThread "+guiID,e);
                    display.showError("Error", e.getMessage());
                    if (!request.waitForResponse()) {
                        showTimeout(response);
                    }
                }
            } catch (IOException e) {
                logger.error("Caught exception in ShowThread",e);
            } finally {
                loggerThreads.debug("ShowThread " + this +" terminated");
            }
        }
    }

    public class ActionThread extends Thread {
        WebDisplay display;
        String guiID;
        CustomizedGUI cgui;
        String sid;
        JacRequest jacRequest;
        HttpServletResponse response;
        String event;
        int eventID;
        View dest;

        public ActionThread(
            WebDisplay display,
            String sid,
            String guiID,
            CustomizedGUI cgui,
            JacRequest request,
            HttpServletResponse response,
            String event,
            View dest) 
        {
            this.display = display;
            this.guiID = guiID;
            this.cgui = cgui;
            this.sid = sid;
            this.jacRequest = request;
            this.response = response;
            this.event = event;
            this.eventID = ((Integer) eventList.get(event)).intValue();
            this.dest = dest;
        }

        public void run() {
            loggerThreads.debug("starting ActionThread " + this +" eventID=" + eventID);
            try {
                Collaboration collab = Collaboration.get();
                collab.addAttribute(
                    GuiAC.DISPLAY_CONTEXT,
                    new DisplayContext(display, null));
                collab.addAttribute(SessionAC.SESSION_ID, sid);
                WebDisplay.setRequest(jacRequest);
                WebDisplay.setResponse(response);
                View root = dest.getRootView();
                if (root instanceof DialogListener)
                    ((DialogListener) root).restoreContext();
                handleAction();
            } finally {
                cleanup();
                loggerThreads.debug("ActionThread " + this +" terminated");
            }
        }

        /**
         * Cleanup instance variables so that no views are kept i memory
         */
        public void cleanup() {
            dest = null;
            display = null;
            jacRequest = null;
            response = null;
            guiID = null;
            cgui = null;
            sid = null;
            event = null;
        }

        public void handleAction() {

            try {
                switch (eventID) {
                    case 2 :
                        { // onView
                            String index =
                                (String) jacRequest.getParameter("index");
                            if (index == null) {
                                index =
                                    (String)jacRequest.getParameter(
                                        "index_" + display.getViewID(dest));
                                if (index == null) {
                                    error(display,jacRequest,
                                          "PROTOCOL ERROR: missing index argument for event "+event);
                                }
                            }
                            if (index != null) {
                                ((CollectionListener)dest).onView(
                                    Integer.parseInt(index));
                            }
                        }
                        break;
                    case 33 :
                        { // onViewObject
                            String name =
                                (String)jacRequest.getParameter("object");
                            if (name == null) {
                                error(display, jacRequest,
                                      "PROTOCOL ERROR: missing object argument for event "+event);
                            } else {
                                logger.info(dest+".onViewObject("+name+")");
                                ((CollectionListener)dest).onViewObject(name);
                            }
                        }
                        break;
                    case 27 :
                        { // onRemove
                            String index =
                                (String)jacRequest.getParameter("index");
                            if (index == null) {
                                error(display, jacRequest,
                                      "PROTOCOL ERROR: missing index argument for event "+event);
                            } else {
                                ((CollectionListener) dest).onRemove(
                                    Integer.parseInt(index));
                            }
                        }
                        break;
                    case 3 :
                        { // onTableInvoke
                            String index =
                                (String) jacRequest.getParameter("index");
                            String method =
                                (String) jacRequest.getParameter("method");
                            if (index == null) {
                                error(display,jacRequest,
                                      "PROTOCOL ERROR: missing index argument for event "+event);
                            } else if (method == null) {
                                error(display,jacRequest,
                                      "PROTOCOL ERROR: missing method argument for event "+event);
                            } else {
                                ((CollectionListener) dest).onTableInvoke(
                                    Integer.parseInt(index),
                                    method);
                            }
                        }
                        break;
                    case 4 :
                        { // onSelect
                            String index =
                                (String) jacRequest.getParameter("index");
                            if (index == null) {
                                error(display,jacRequest,
                                      "PROTOCOL ERROR: missing index argument for event "+event);
                            } else {
                                logger.info("TabsListener.onSelect(" + index + ")");
                                ((TabsListener) dest).onSelect(
                                    Integer.parseInt(index));
                            }
                        }
                        break;
                    case 5 : // onInvoke
                        loggerEvents.debug(dest+".onInvoke");
                        ((MethodListener) dest).onInvoke();
                        break;
                    case 6 : // onSelection
                        ((SelectionListener) dest).onSelection();
                        break;
                    case 7 : // onCellSelection
                        String row = (String) jacRequest.getParameter("row");
                        if (row == null) {
                            error(display, jacRequest,
                                  "PROTOCOL ERROR: missing row argument for event "+event);
                        } else {
                            String col =
                                (String) jacRequest.getParameter("col");
                            if (row == null) {
                                error(display,jacRequest,
                                      "PROTOCOL ERROR: missing row argument for event "+event);
                            } else {
                                ((TableListener) dest).onCellSelection(
                                    Integer.parseInt(row),
                                    Integer.parseInt(col));
                            }
                        }

                        break;
                    case 8 : // onHeaderClick
                        String col = (String) jacRequest.getParameter("col");
                        if (col == null) {
                            error(display,jacRequest,
                                  "PROTOCOL ERROR: missing col argument for event "+event);
                        } else {
                            ((TableListener) dest).onHeaderClick(
                                Integer.parseInt(col));
                        }
                        break;
                    case 9 : // onClose
                        if ((String)jacRequest.getParameter("onOK") != null) {
                            loggerEvents.debug(dest+".onOK");
                            ((WindowListener)dest).onOK(jacRequest);
                        } else if ((String)jacRequest.getParameter("onCancel") != null) {
                            loggerEvents.debug(dest+".onCancel");
                            ((WindowListener)dest).onCancel();
                        } else {
                            loggerEvents.debug(dest+".onOK");
                            ((WindowListener)dest).onOK(jacRequest);
                        }
                        break;
                    case 10 : // onValidate
                        if ((String) jacRequest.getParameter("onOK") != null) {
                            loggerEvents.debug(dest+".onOK");
                            ((DialogListener) dest).onOK(jacRequest);
                        } else if ((String)jacRequest.getParameter("onCancel") != null) {
                            loggerEvents.debug(dest+".onCancel");
                            ((DialogListener) dest).onCancel();
                        }
                        break;
                    case 11 : // onOK
                        ((WindowListener) dest).onOK(jacRequest);
                        break;
                    case 12 : // onCancel
                        ((WindowListener) dest).onCancel();
                        break;
                    case 34 : // onRefresh
                        loggerEvents.debug(dest+".onRefresh");
                        ((WindowListener)dest).onRefresh(jacRequest);
                        break;
                    case 13 : // onAddToCollection
                        ((CollectionListener)dest).onAddToCollection();
                        break;
                    case 31 : // onAddExistingToCollection
                        ((CollectionListener)dest).onAddExistingToCollection();
                        break;
                    case 14 : // onRemoveFromCollection
                        ((CollectionListener)dest).onRemoveFromCollection();
                        break;
                    case 15 : // onNext
                        ((CollectionListener)dest).onNext();
                        break;
                    case 29 : // onLast
                        ((CollectionListener)dest).onLast();
                        break;
                    case 16 : // onPrevious
                        ((CollectionListener)dest).onPrevious();
                        break;
                    case 28 : // onFirst
                        ((CollectionListener)dest).onFirst();
                        break;
                    case 35: // onRefreshCollection
                        ((CollectionListener)dest).onRefreshCollection();
                        break;  
                    case 17 : // onSelectNode
                        loggerEvents.debug(dest+".onSelectNode");
                        ((TreeListener) dest).onSelectNode(
                            (String) jacRequest.getParameter("nodePath"));
                        break;
                    case 18 : // onExpandNode
                        loggerEvents.debug(dest+".onExpandNode");
                        ((TreeListener) dest).onExpandNode(
                            (String) jacRequest.getParameter("nodePath"));
                        break;
                    case 19 : // onCollapseNode
                        loggerEvents.debug(dest+".onCollapseNode");
                        ((TreeListener) dest).onCollapseNode(
                            (String) jacRequest.getParameter("nodePath"));
                        break;
                    case 20 : // onMenuClick
                        loggerEvents.debug(dest+".onMenuClick");
                        ((MenuListener) dest).onMenuClick(
                            (String) jacRequest.getParameter("item"));
                        break;
                    case 21 : // onCreateObject
                        ((ChoiceListener) dest).onCreateObject();
                        break;
                    case 22 : // onLoadAttachment
                        ((AttachmentListener) dest).onLoadAttachment();
                        break;
                    case 23 : // onPreviousInCollection
                        ((CollectionItemViewListener) dest)
                            .onPreviousInCollection();
                        break;
                    case 24 : // onNextInCollection
                        ((CollectionItemViewListener) dest)
                            .onNextInCollection();
                        break;
                    case 25 : // onBackToCollection
                        ((CollectionItemViewListener) dest)
                            .onBackToCollection();
                        break;
                    case 26 : // onRemoveInCollection
                        ((CollectionItemViewListener) dest)
                            .onRemoveInCollection();
                        break;
                    case 30 : // onAddEmbedded
                        ((TableListener) dest).onEmbeddedAddToCollection();
                        break;
                    case 32 : // onSetDefaults
                        ((TableListener) dest).onSetDefaults();
                        break;
                    default :
                        throw new Exception(
                            "Unknown event " + event + "(" + eventID + ")");
                }

            } catch (Throwable e) {
                logger.error("handleAction(eventID="+eventID+",event="+event+
                             ",sid="+sid+") failed",e);
                display.show(e);
            }
        }

    }

    protected void error(
        WebDisplay display,
        JacRequest jacRequest,
        String message) 
    {
        logger.error("message",new Exception());
        display.showError("Servlet error", message);
        jacRequest.waitForResponse();
    }

}
