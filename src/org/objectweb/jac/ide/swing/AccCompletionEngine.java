/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.swing.DefaultCompletionEngine;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.parsers.acc.NonTerminal;
import org.objectweb.jac.core.parsers.acc.SyntaxElement;
import org.objectweb.jac.core.parsers.acc.Terminal;
import org.objectweb.jac.core.parsers.acc.ToolParserWrapper;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Field;
import org.objectweb.jac.ide.Method;
import org.objectweb.jac.ide.Package;
import org.objectweb.jac.ide.Project;
import org.objectweb.jac.ide.RelationRole;

/**
 * This class implements a completion engine for method acc
 * configuration code of the UMLAF IDE.  
 */
public class AccCompletionEngine extends DefaultCompletionEngine {
    static final Logger logger = Logger.getLogger("completion");

    Project project;
    /**
     * Creates a new AccCompletionEngine using a given parser.
     * @param parser the parser to use
     */
    public AccCompletionEngine(ToolParserWrapper parser, Project project) {
        this.project = project;
        this.parser = parser;
    }

    SyntaxElement currentSyntaxElement;
    public SyntaxElement getCurrentSyntaxElement() {
        return currentSyntaxElement;
    }

    AspectComponent aspectInstance;
    /**
     * Sets the aspect instance associated with the completion engine
     * @param instance the AspectComponent instance
     */
    public void setAspectInstance(AspectComponent instance) {
        this.aspectInstance = instance;
    }

    ToolParserWrapper parser;
    public List getContextualChoices(String text, int position, 
                                     String writtenText) {
        NonTerminal elements = parser.parse(new StringReader(text),"");
        if (writtenText.length()>0)
            position = position+writtenText.length()-1;
        logger.debug("Syntax elements = "+elements);
        logger.debug("position = "+position);
        logger.debug("writtenText = "+writtenText);

        SyntaxElement se = parser.getSyntaxElementAt(position);
        currentSyntaxElement = se;
        logger.debug("Syntax element = "+se);
        if (se!=null && (se.getName().equals("CONF_METHOD") || 
                         se.getName().equals("EOL") ||
                         se.getName().equals("class_block"))) {
            logger.debug("Completing!");
            NonTerminal block = (NonTerminal)se.findParent("class_block");
            if (block!=null) {
                Terminal keyword = (Terminal)block.getChild("BLOCK_KEYWORD");
                if (keyword!=null) {
                    // Completion inside a block
                    if (keyword.getValue().equals("attribute")) {
                        logger.debug("  Completing in attribute block");
                        return aspectInstance.getConfigurationMethodsName(
                            FieldItem.class);
                    } else if (keyword.getValue().equals("method")) {
                        logger.debug("  Completing in method block");
                        return aspectInstance.getConfigurationMethodsName(
                            AbstractMethodItem.class);
                    } else if (keyword.getValue().equals("member")) {
                        logger.debug("  Completing in member block");
                        return aspectInstance.getConfigurationMethodsName(
                            MemberItem.class);
                    } else if (keyword.getValue().equals("class")) {
                        logger.debug("  Completing in class block");
                        Vector result = new Vector();
                        result.addAll(
                            aspectInstance.getConfigurationMethodsName(ClassItem.class));
                        result.addAll(
                            aspectInstance.getConfigurationMethodsName(MemberItem.class));
                        result.addAll(Arrays.asList(
                            new String[] {"attribute","method","member"}));
                        return result;
                    } else {
                        logger.debug("  Unknown block keyword");
                        return baseWords;
                    }
                } else {
                    logger.debug("  No keyword in block");
                    return baseWords;
                }
            } else {
                logger.debug("  No block keyword");
                return baseWords;
            }
        } else {
            Terminal term = parser.getTerminalAt(position);
            logger.debug("  SyntaxElement before: "+term);
            if (term!=null && term.getName().equals("BLOCK_PARAM")) {
                return completeBlockParam(term);
            } else if (se==null) {
                logger.debug("  No current syntax element");
                return baseWords;
            } else {
                NonTerminal confMethod = (NonTerminal)se.findParent("conf_method");
                if (confMethod!=null) {
                    String methodName = confMethod.getChild(0).getName();
                    logger.debug("  completing arg of method "+methodName);
                    String className = getClassName(confMethod);
                    if (className!=null) {
                        logger.debug("   className = "+className);                        
                    }
                }
            }
        }
        return new Vector();
    }

    protected List completeConfMethodParam() {
        return null;
    }

    /**
     * Gets completion for a block parameter
     * @param term the Terminal syntax element to complete
     */
    protected List completeBlockParam(Terminal term) {
        logger.debug("complete block param "+term);
        Vector result = new Vector();
        NonTerminal block = (NonTerminal)term.findParent("class_block");
        if (block!=null) {
            Terminal keyword = (Terminal)block.getChild("BLOCK_KEYWORD");
            if (keyword!=null) {
                if (keyword.getValue().equals("attribute")) {
                    String className = getClassName(block);
                    if (className!=null) {
                        logger.debug("completing attribute for class "+className);
                        Class cl = project.findClass(className);
                        if (cl!=null) {
                            completeAttributeName(term.getValue(),cl,result);
                        }
                    }
                } else if (keyword.getValue().equals("method")) {
                    String className = getClassName(block);
                    if (className!=null) {
                        logger.debug("completing method for class "+className);
                        Class cl = project.findClass(className);
                        if (cl!=null) {
                            completeMethodName(term.getValue(),cl,result);
                        }
                    }
                } else if (keyword.getValue().equals("member")) {
                    String className = getClassName(block);
                    if (className!=null) {
                        logger.debug("completing member for class "+className);
                        Class cl = project.findClass(className);
                        if (cl!=null) {
                            completeAttributeName(term.getValue(),cl,result);
                            completeMethodName(term.getValue(),cl,result);
                        } else {
                            logger.warn("No such class in project: "+className);
                        }
                    }
                } else if (keyword.getValue().equals("class")) {
                    completeClassName(term.getValue(),result);
                } else {
                    logger.debug("BLOCK_KEYWORD is "+keyword.getValue());
                }
            } else {
                logger.debug("No child BLOCK_KEYWORD "+term);
            }
        } else {
            logger.debug("No parent class_block "+term);
        }

        return result;
    } 

    /**
     * Gets the className for a member,method or attribute block
     * @param block Non terminal of the block keyword
     * @return the class name, or null if it cannot be computed
     */
    protected String getClassName(NonTerminal block) {
        String className = null;
        block = (NonTerminal)block.getParent().findParent("class_block");
        if (block!=null) {
            Terminal keyword = (Terminal)block.getChild("BLOCK_KEYWORD");
            if (keyword!=null && keyword.getValue().equals("class")) {
                NonTerminal blockParams = 
                    (NonTerminal)block.getChild("block_params");
                if (blockParams!=null) {
                    Terminal param = (Terminal)blockParams.getChild(0);
                    className = param.getValue();
                }
            }
        }
        return className;
    }

    protected String getMemberName(NonTerminal block) {
        String memberName = null;
        block = (NonTerminal)block.getParent().findParent("class_block");
        if (block!=null) {
            Terminal keyword = (Terminal)block.getChild("BLOCK_KEYWORD");
            if (keyword!=null && 
                (keyword.getValue().equals("method") || 
                 keyword.getValue().equals("attribute"))) {
                NonTerminal blockParams = 
                    (NonTerminal)block.getChild("block_params");
                if (blockParams!=null) {
                    Terminal param = (Terminal)blockParams.getChild(0);
                    memberName = param.getValue();
                }
            }
        }
        return memberName;        
    }

    protected void completeAttributeName(String start, Class cl, List result) {
        Iterator it = cl.getAllFields().iterator();
        while(it.hasNext()) {
            Field field = (Field)it.next();
            if (field.getName().startsWith(start)) {
                result.add(field.getGenerationName());
            }
        }
        it = cl.getAllNavigableRoles().iterator();
        while(it.hasNext()) {
            RelationRole role = (RelationRole)it.next();
            if (role.getGenerationName().startsWith(start)) {
                result.add(role.getGenerationName());
            }
        }
    }

    protected void completeMethodName(String start, Class cl, List result) {
        Iterator it = cl.getMethods().iterator();
        while(it.hasNext()) {
            Method method = (Method)it.next();
            if (method.getName().startsWith(start)) {
                result.add(method.getGenerationName());
            }
        }
    }

    protected void completeClassName(String start, List result) {
        logger.debug("completing class name "+start);
        int dot = start.lastIndexOf('.');
        if (dot==-1) {
            Iterator it = project.getPackages().iterator();
            while (it.hasNext()) {
                Package pkg = (Package)it.next();
                if (pkg.getGenerationName().startsWith(start)) {
                    result.add(pkg.getPPath());
                }
            }
         
        } else {
            String end = start.substring(dot+1);
            Package pkg = project.findPackage(start.substring(0,dot));

            if (pkg!=null) {
                logger.debug("Looking for "+end+" in package "+pkg.getPPath());
                Iterator it = pkg.getClasses().iterator();
                while (it.hasNext()) {
                    Class cl = (Class)it.next();
                    if (cl.getGenerationName().startsWith(end)) {
                        logger.debug("Found class "+cl.getFullName());
                        result.add(cl.getGenerationFullName());
                    }
                }
                it = pkg.getSubPackages().iterator();
                while (it.hasNext()) {
                    Package subPkg = (Package)it.next();
                    if (subPkg.getGenerationName().startsWith(end)) {
                        logger.debug("Found package "+subPkg.getPPath());
                        result.add(subPkg.getPPath());
                    }
                }
            }
        }
    }
}
