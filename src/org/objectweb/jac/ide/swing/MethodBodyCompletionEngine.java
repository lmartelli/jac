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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.swing.CompletionEngine;
import org.objectweb.jac.aspects.gui.swing.SHEditor;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.CodeGeneration;
import org.objectweb.jac.ide.Field;
import org.objectweb.jac.ide.Method;
import org.objectweb.jac.ide.Parameter;
import org.objectweb.jac.ide.RelationRole;
import org.objectweb.jac.ide.Role;
import org.objectweb.jac.ide.Type;

/**
 * This class implements a completion engine for method bodies Java
 * editors of the UMLAF IDE. */

public class MethodBodyCompletionEngine extends CompletionEngine {
    static final Logger logger = Logger.getLogger("completion");
    static final Logger loggerPerf = Logger.getLogger("perf");

    Method method;
    SHEditor editor;

    /**
     * Creates a completion engine for a given method. */

    public MethodBodyCompletionEngine(Method method, SHEditor editor) {
        this.method = method;
        addBaseWords(buildThisWords());
        logger.info("baseWords = "+baseWords);
        this.editor = editor;
    }

    /**
     * Get the contextual possible choices.
     *
     * <p>Supported contextual completions are like
     * <code>class_typed_symbol.{methods}</code>. In the long term, any
     * typed expression should be supported even if it is not that
     * important in clean developments (because of the Demeter's
     * Law!!).
     *
     * @param text the editor's full text
     * @param position the cursor position
     * @param writtenText the already written text */

    public List getContextualChoices(String text, int position, 
                                     String writtenText) {
        long start = System.currentTimeMillis();
      
        List result;
        int pos = position-1;
        if (pos>0 && text.charAt(pos)=='.') {
            // Here we should be able to guess types of more complex
            // lines that symbols (see getSymbolType). This would need a
            // real parser.
            while (pos>0 && !editor.isDivider(text.charAt(--pos))) {
            }
            String symbol = text.substring(pos+1,position-1);
            logger.info("found parent symbol "+symbol);
            Type t = getSymbolType(symbol);
            if (t!=null) {
                result = buildTypeWords(t);
            } else {
                result = new Vector();
            }
        } else {
            result = getBaseWords();
        }
        loggerPerf.info("getContextualChoices("+writtenText+"): "+
                        (System.currentTimeMillis()-start)+"ms");

        logger.info( "getContextualChoices("+writtenText+") -> "+result);
        return result;
    }

    /**
     * Builds the list of method and field names that are directly
     * accessible within the class of the method and the parameters of
     * the method, as well as the names of the types of those fields.
     */
    public List buildThisWords() {
        List ret = buildClassWords(method.getParent());
        Iterator it = method.getParameters().iterator();
        while(it.hasNext()) {
            Parameter p = (Parameter)it.next();
            String name = p.getGenerationName();
            if (!ret.contains(name)) {
                ret.add(name);
            }
        }
        return ret;
    }

    /**
     * Get all the accessible words in the context of a given type.
     *
     * <p>If type is an internal IDE class, all the method plus the
     * generated methods (getters, setters, adders, removers, clearers)
     * are returned. If type is an external library class, all the
     * public methods are returned.
     *
     * @see #buildClassWords(Class) */

    public List buildTypeWords(Type type) {
        logger.debug("buildTypeWords("+type.getFullName()+")");
        if (type==null) {
            return new Vector();
        } else if (type instanceof Class) {
            return buildClassWords((Class)type);
        } else {
            logger.debug("found external type");         
            List ret = new Vector();
            ClassItem c = ClassRepository.get().getClass(type.getFullName());
            Iterator it = c.getAllMethods().iterator();
            while(it.hasNext()) {
                String name = ((AbstractMethodItem)it.next()).getName();
                logger.debug("external method candidate: "+name);         
                if (!ret.contains(name)) {
                    ret.add(name);
                }
            }
            return ret;
        }
    }

    /**
     * Get all the accessible words in the context of a given IDE
     * class (fields,methods). 
     *
     * @param c the class for which to get accessible words
     */
    public List buildClassWords(Class c) {
        if (c==null)
            return new Vector();
        if (classWords.containsKey(c)) {
            return (List)classWords.get(c);
        }
        logger.debug("buildClassWords("+c.getFullName()+")");
        List ret = new Vector();
        Iterator it = c.getMethods().iterator();
        while (it.hasNext()) {
            String name = ((Method)it.next()).getGenerationName();
            if (!ret.contains(name)) {
                ret.add(name);
            }
        }
        it = c.getFields().iterator();
        while (it.hasNext()) {
            Field field = (Field)it.next();
            String name = field.getGenerationName();         
            if (!ret.contains(name)) {
                ret.add(name);
            }
            Type type = field.getType();
            if (type instanceof Class) {
                name = type.getGenerationName();
                if (!ret.contains(name))
                    ret.add(name);
            }
            // add also the setter
            String tmpName = CodeGeneration.getSetterName(name);
            if (!ret.contains(tmpName)) {
                ret.add(tmpName);
            }
            // and the getter
            tmpName = CodeGeneration.getGetterName(name);
            if (!ret.contains(tmpName)) {
                ret.add(tmpName);
            }
         
        }

        List rs = c.getLinks();
        for (int i=0; i<rs.size(); i++) {
            Role role = (Role)rs.get(i);
            if (role instanceof RelationRole) {
                RelationRole end = (RelationRole)role;

                logger.debug("  role "+end.getGenerationName());
                String name = end.getGenerationName();
                if (!ret.contains(name)) {
                    ret.add(name);
                }
                name = ((Class)end.getEnd()).getGenerationName();
                logger.debug("  Classname of "+end.getGenerationName()+": "+name);
                if (!ret.contains(name))
                    ret.add(name);

                String tmpName = CodeGeneration.getSetterName(name);
                if (!ret.contains(tmpName)) {
                    ret.add(tmpName);
                }
                tmpName = CodeGeneration.getGetterName(name);
                if (!ret.contains(tmpName)) {
                    ret.add(tmpName);
                }
                if (end.isMultiple()) {
                    tmpName = CodeGeneration.getAdderName(name);
                    if (!ret.contains(tmpName)) {
                        ret.add(tmpName);
                    }
                    tmpName = CodeGeneration.getRemoverName(name);
                    if (!ret.contains(tmpName)) {
                        ret.add(tmpName);
                    }
                    tmpName = CodeGeneration.getClearerName(name);
                    if (!ret.contains(tmpName)) {
                        ret.add(tmpName);
                    }
                }
            }
        }         

        classWords.put(c,ret);
        return ret;
    }

    HashMap classWords = new HashMap();

    /**
     * Returns the type of a symbol. Any kind of expression should be
     * supported one day... 
     * @param name the symbol's name
     */
    Type getSymbolType(String name) {
        if (symbolTypes.containsKey(name)) {
            return (Type)symbolTypes.get(name);
        }
        Type type = null;
        Iterator it = method.getParameters().iterator();
        while (it.hasNext()) {
            Parameter p = (Parameter)it.next();
            if (p.getGenerationName().equals(name)) {
                type = p.getType();
                symbolTypes.put(name,type);
                return type;
            }
        }
        Class c = method.getParent();
        if (c!=null) {
            it = c.getFields().iterator();
            while(it.hasNext()) {
                Field f = (Field)it.next();
                if (f.getGenerationName().equals(name)) {
                    type = f.getType();
                    symbolTypes.put(name,type);
                    return type;
                }
            }
        }
        symbolTypes.put(name,null);
        return null;
    }

    HashMap symbolTypes = new HashMap();

    /**
     * Returns true if <code>c=='('</code>. */

    public boolean isAutomaticCompletionChar(char c) {
        logger.debug("isAutomaticCompletionChar("+c+")");
        return c=='(';
    }

    /**
     * Help the programmer to write useful control structure such as
     * <code>for</code> or <code>while</code>. */

    public void runAutomaticCompletion(SHEditor editor,
                                       String text, 
                                       int position, 
                                       char c) {
        String word = getPreviousWord(text,position);
        logger.debug("getAutomaticCompletion("+word+")");
        if(c=='(') {
            if(word.equals("for")) {
                editor.insertString(position,";;) {");
                editor.insertReturn();
                editor.insertReturn();
                editor.insertCloseCBracket();
            } else if(word.equals("while")) {
                editor.insertString(position,") {");
                editor.insertReturn();
                editor.insertReturn();
                editor.insertCloseCBracket();
            }
        }
    }

    String getPreviousWord(String text, int position) {
        int pos2 = position-1;
        int pos1 = pos2;
        while (pos1>0 && !editor.isDivider(text.charAt(--pos1))) {
        }
        return text.substring(pos1,pos2).trim(); 
    }
   
}

