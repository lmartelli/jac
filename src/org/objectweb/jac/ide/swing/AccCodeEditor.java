/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>
  
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

package org.objectweb.jac.ide.swing;

import java.awt.BorderLayout;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.swing.AbstractCodeEditor;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.parsers.acc.NonTerminal;
import org.objectweb.jac.core.parsers.acc.SyntaxElement;
import org.objectweb.jac.core.parsers.acc.Terminal;
import org.objectweb.jac.core.parsers.acc.ToolParserWrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NoSuchMethodException;
import org.objectweb.jac.ide.Application;
import org.objectweb.jac.ide.Projects;
import org.objectweb.jac.ide.AspectConfiguration;
import org.objectweb.jac.util.Strings;

/**
 * A Java source code editor
 */
public class AccCodeEditor extends AbstractCodeEditor
{
    static final Logger logger = Logger.getLogger("ide.editor");

    JLabel statusLine = new JLabel();
    AccCodeStatus codeStatus;
    ToolParserWrapper parser;
    AccCompletionEngine ce;

    public AccCodeEditor(Object substance, FieldItem field) {
        super(substance,field);
        editor.setConfig(Projects.prefs.getEditorPrefs());
        editor.setMinDisplayedLines(10);
        editor.setWordSeparators(
            new char [] { '\n', ' ', ',', '(', ')', '{', '}', '[', ']', '/', '-', '+', '*', 
                          '<', '>', '=', ';', '"', '\'', '&', '|', '!' });
        add(BorderLayout.SOUTH,statusLine);
        parser = new ToolParserWrapper();
        Application application = ((AspectConfiguration)getSubstance()).getApplication();
        if (application==null) {
            application = (Application)Collaboration.get().getAttribute(GuiAC.SUBSTANCE);
        }

        if (application!=null) {
            ce = new AccCompletionEngine(parser,application.getProject());
            editor.setCompletionEngine(ce);
        }
        editor.addCaretListener(new AccCodeStatus());
        init();
        editor.addTextListener(
            new TextListener() {
                    public void textValueChanged(TextEvent e) {
                        logger.debug("textValueChanged");
                        parser.parse(new StringReader(editor.getText()),"");
                        updateStatusLine();
                    }
                });
    }  

    static String[] defaultBlockKeywords = 
        new String[] {"class","member","attribute","method","block"};

    protected void init() {
        editor.clearKeywords();
        editor.addKeywords(defaultBlockKeywords);
        if (getSubstance() instanceof AspectConfiguration) {
            // instanciate the aspect so that we can know its block keywords
            AspectConfiguration config = (AspectConfiguration)getSubstance();
            try {
                if (!Strings.isEmpty(config.getName())) {
                    String acClassName = 
                        ACManager.getACM().getACPathFromName(config.getName());
                    Class acClass = Class.forName(acClassName);
                    aspectClass = ClassRepository.get().getClass(acClassName);
                    AspectComponent acInstance = (AspectComponent)acClass.newInstance();
                    Set keywords = acInstance.getBlockKeywords();
                    logger.debug("keywords for "+config.getName()+": "+keywords);
                    editor.addKeywords(keywords);

                    if (parser!=null)
                        parser.setBlockKeywords(keywords);
                    if (ce!=null) {
                        ce.setAspectInstance(acInstance);
                        ce.clearBaseWords();
                        ce.addBaseWords(acInstance.getConfigurationMethodsName());
                        ce.addBaseWords(acInstance.getBlockKeywords());
                        ce.addBaseWords(Arrays.asList(defaultBlockKeywords));
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to init the code editor",e);
            }
        }
    }

    /*
      public void setValue(Object value) {
      super.setValue(value);
      init();
      if (parser!=null) {
      parser.parse(new StringReader(editor.getText()),"");
      }
      }
    */

    ClassItem aspectClass;

    /**
     * Updates the "modeline" to display the current configuration method
     */
    void updateStatusLine() {
        SyntaxElement currentElement = parser.getSyntaxElementAt(editor.getCaretPosition());
        logger.debug("currentElement = "+currentElement);
        String newStatus = " ";
        NonTerminal parent=null;
        if (currentElement!=null)
            parent = (NonTerminal)currentElement.findParent("conf_method");
        if (parent!=null) {
            logger.debug("parent = "+parent);
            String currentName = parent.getName();
            Terminal confMethod = 
                (Terminal)parent.getChild("CONF_METHOD");
            if (confMethod!=null) {
                if (aspectClass!=null) {
                    try {
                        MethodItem[] methods = 
                            aspectClass.getMethods(confMethod.getValue());
                        if (methods.length==1) {
                            newStatus = methods[0].getCompactFullName();
                        } else {
                            newStatus = confMethod.getValue()+"(...)";
                        }
                    } catch (NoSuchMethodException e) {
                        newStatus = "???";
                    }
                } else {
                    newStatus = confMethod.getValue();
                }
            } else {
                newStatus = currentElement.toString();
            }
        } else if (currentElement!=null) {
            newStatus = currentElement.toString();
        }
        statusLine.setText(newStatus);
    }

    class AccCodeStatus implements CaretListener {
        public AccCodeStatus() {
        }
        public void caretUpdate(CaretEvent event) {
            updateStatusLine();
        }
    }

    public void fieldUpdated(Object object, FieldItem field, 
                             Object value, Object param) {
        init();
        super.fieldUpdated(object,field,value,param);
    }
}

