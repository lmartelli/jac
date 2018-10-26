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

package org.objectweb.jac.aspects.gui.swing;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * This class must be subclassed to implement specific completion
 * engines for code editors. 
 */
public abstract class CompletionEngine {
    static Logger logger = Logger.getLogger("completion");

    public static final int BACKWARD=-1;
    public static final int FORWARD=1;

    protected List baseWords = new Vector();

   /**
    * Returns the list of the words that are potential completions for
    * a given context. Implement this methods for specific language
    * editors.
    *
    * @param text the editor's full text
    * @param position the cursor position
    * @param writtenText the already written text 
    * @return a list of strings which <b>must not contain duplicates</b>
    */
    public abstract List getContextualChoices(String text, int position, 
                                              String writtenText);
   
    /**
    * Returns a proposal from a current text's state.
    *
    * @param text the editor's full text
    * @param position the cursor position
    * @param writtenText the already written text of the
    * completionable word if any
    * @param currentProposal the proposal that is currently made to
    * the user ("" if none)
    * @param direction BACKWARD || FORWARD 
    * @return the proposed completion, starting with writtenText
    */
    public String getProposal(String text, 
                              int position,
                              String writtenText,
                              String currentProposal,
                              int direction) {
        logger.info("getProposal("+position+","+
                     ","+writtenText+","+currentProposal+","+direction+")");
        String ret = "";
        List words = getContextualChoices(text,position,writtenText);
        logger.debug("Proposals = "+words);
        if (words.size()==0) 
            return ret;
        if (currentProposal.equals("") && writtenText.equals("")) {
            ret = (String)words.get(0);
        } else {
            // Finds the position of currentProposal in words
            // We cannot use List.indexOf() because we want case insensitiveness
            int i = -1;
            int pos = 0;
            Iterator it = words.iterator();
            while (it.hasNext() && i==-1) {
                if (currentProposal.compareToIgnoreCase((String)it.next())==0)
                    i = pos;
                pos++;
            }
            if (i!=-1 || currentProposal.equals("")) {
                if (writtenText.equals("")) {
                    ret = (String) words.get(next(words,i,direction));
                } else {
                    int count = 0;
                    logger.debug("before search: "+i);
                    while(count <= words.size() && 
                          !((String)words.get(i=next(words,i,direction))).toLowerCase()
                          .startsWith(writtenText.toLowerCase())) {
                        count++;
                    }
                    logger.debug("after search: "+i);
                    if (((String)words.get(i)).toLowerCase()
                        .startsWith(writtenText.toLowerCase())) {
                        ret = ((String)words.get(i));//.substring(writtenText.length());
                    }
                }
            }
        }
        return ret;
    }

    public abstract void runAutomaticCompletion(SHEditor editor,
                                                String text, 
                                                int position,
                                                char c);
   
    public abstract boolean isAutomaticCompletionChar(char c);

    int next(List l,int i, int direction) {
        if(direction==FORWARD) {
            if(i==l.size()-1)
                return 0;
            else
                return i+1;
        } else if (direction==BACKWARD) {
            if(i==0)
                return l.size()-1;
            else
                return i-1;
        } else {
            return i;
        }
    }

    public List getBaseWords() {
        return baseWords;
    }

    public void addBaseWord(String baseWord) {
        if (!baseWords.contains(baseWord)) {
            baseWords.add(baseWord);
        }
    }

    public void addBaseWords(Collection baseWords) {
        logger.debug("addBaseWords "+baseWords);
        Iterator it = baseWords.iterator();
        while (it.hasNext()) {
            Object cur = it.next();
            if (!this.baseWords.contains(cur)) {
                this.baseWords.add(cur);
            }
        }
    }

    public void clearBaseWords() {
        baseWords.clear();
    }

}
