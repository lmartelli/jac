
package org.objectweb.jac.aspects.gui.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Strings;

/** 
 * @author Lars-Erik H. Bergland <a
//href="http://www.mycgiserver.com/~hbergla/">www.mycgiserver.com/~hbergla</a>
//<a
//href="mailto:vagastorm@microsnyft.com">vagastorm@microsnyft.com</a>
 * @version 1.0
 *
 * The CodeEditor is constructed to display java code with syntax
 * coloring.  It was created to be used in the <a
 * href="http://www.mycgiserver.com/~hbergla/SimpleJavaEditor/about.html">SimpleJavaEditor</a>
 * but it can easily be adjusted so it can be used with any SWING
 * application that needs to display syntax colored java code.  Please
 * use it in any leagal way you whant, but if you find it usefull tell
 * me aboute it, along with any wishes you migt have for changes and
 * I'll see what I can do.  */

public class SHEditor extends JPanel 
    implements KeyListener, MouseListener, MouseMotionListener, 
              Scrollable, ClipboardOwner, FocusListener
{
    static Logger logger = Logger.getLogger("gui.sheditor");
    static Logger loggerComp = Logger.getLogger("completion");
    static Logger loggerClip = Logger.getLogger("clipboard");

    private String text = "";   
    private FontMetrics metrics;
    int lineHeight; // height of line of text

    private int caretPosition = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    /** Whether the selection is some text added by the completion engine */
    protected boolean isSelectionCompletion = false;
    
    /** position of opening char ('(','{' or '[') to highlight */
    private int openPos = -1;
    /** position of closing char (')','}' or ']') to highlight */
    private int closePos = -1;
   
    private int lineToMark = -1;
   
    private boolean showCaret = true;
   
    protected SHEditorConfig conf = new SHEditorConfig();
    public SHEditorConfig getConfig() {
        return conf;
    }
    public void setConfig(SHEditorConfig conf) {
        this.conf = conf;
    }
    
    public static final int DEFAULT = 0;
    public static final int COMMENT = 1;
    public static final int STRING = 2;

    protected int syntaxUnderCaret;
    public int getSyntaxUnderCaret() {
        return syntaxUnderCaret;
    }

    private int mousePressPos = 0;
   
    private Vector doneActions = new Vector();
    private Vector redoneActions = new Vector();
   
    private Rectangle car = new Rectangle(0, 0, 2, 0);
   
    boolean changed = false; 

    private char separators[] = new char[] {
        '\n', ' ', '.' , ',', '(', ')', '{', '}', '[', ']', '/', '-', '+', '*', 
        '<', '>', '=', ';', '"', '\'', '&', '|', '!'
    };

    /**
     * Sets the characters considered as word separators
     */
    public void setWordSeparators(char[] separators) {
        this.separators = separators;
    }

    /** 
     * Constructs a empty SHEditor.
     */
    public SHEditor()
    {
        super();
      
        setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        metrics = getFontMetrics(getFont());
        setCursor(new Cursor(Cursor.TEXT_CURSOR));
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addFocusListener(this);
        if (org.objectweb.jac.core.Jac.getMainJavaVersion().compareTo("1.4")>=0) {
            setFocusTraversalKeysEnabled(false);
        }
        setBackground(Color.white);
    }

    /** 
     * Constructs a SHEditor where the content is set by a string.
     * @param txt A String representing initial java file.
     */
    public SHEditor(String txt)
    {
        this();
        text = txt;
    }

    /** 
     * Constructs a SHEditor and reads the content of a file into it.
     * @param file The initial java file.
     */
    public SHEditor(File file)
    {
        this();
        readFromFile(file);
    }

    Vector caretListeners = new Vector();
    /**
     * Adds a caret listener for notification of any changes
     * to the caret.
     *
     * @param listener the listener to be added
     * @see javax.swing.event.CaretEvent
     */
    public void addCaretListener(CaretListener listener) {
        caretListeners.add(listener);
    }
   
    /**
     * Removes a caret listener.
     *
     * @param listener the listener to be removed
     * @see javax.swing.event.CaretEvent
     */
    public void removeCaretListener(CaretListener listener) {
        caretListeners.remove(listener);
    }

    protected void fireCaretUpdate() {
        CaretEvent e = new MutableCaretEvent(this,caretPosition);
        for (int i = caretListeners.size()-1; i>=0; i--) {
            ((CaretListener)textListeners.get(i)).caretUpdate(e);
        }
    }

    Vector textListeners = new Vector();

    /**
     * Adds a text listener for notification of any changes
     * to the text.
     *
     * @param listener the listener to be added
     * @see javax.swing.event.CaretEvent
     */
    public void addTextListener(TextListener listener) {
        textListeners.add(listener);
    }
   
    /**
     * Removes a text listener.
     *
     * @param listener the listener to be removed
     * @see javax.swing.event.CaretEvent
     */
    public void removeTextListener(TextListener listener) {
        textListeners.remove(listener);
    }

    protected void fireTextUpdate() {
        TextEvent e = new TextEvent(this,TextEvent.TEXT_VALUE_CHANGED);
        for (int i = textListeners.size()-1; i>=0; i--) {
            ((TextListener)textListeners.get(i)).textValueChanged(e);
        }
    }

    /** 
     * Returns the a String containing the java file.
     * @return A string containing the content of the current java file.
     */
    public String getText()
    {
        return text;
    }

    public void setText(String t) {
        text=t;
        fireTextUpdate();
    }

    /**
     * Tells wether the caret is at the end of the text
     */
    boolean eot() {
        return caretPosition >= text.length();
    }

    /**
     * Tells wether the caret is at the end of the selection
     */
    boolean endOfSelection() {
        return caretPosition == selectionEnd;
    }

    /**
     * Tells wether the caret is at the start of the selection
     */
    boolean startOfSelection() {
        return caretPosition == selectionStart;
    }

    /**
     * Sets the start of the selection. It ensures that selectionStart<selectionEnd.
     * @param position new start of selection
     */
    public void setSelectionStart(int position) {
        if (position>selectionEnd) {
            int oldStart = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = position;
            repaintChars(selectionStart,position);
        } else {
            repaintChars(selectionStart,position);
            selectionStart = position;
        }
    }

    /**
     * Sets the end of the selection. It ensures that selectionStart<selectionEnd.
     * @param position new end of selection
     */
    public void setSelectionEnd(int position) {
        if (position<selectionStart) {
            int oldEnd = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = position;
            repaintChars(selectionEnd,position);
        } else {
            repaintChars(selectionEnd,position);
            selectionEnd = position;
        }
    }

    /**
     * Sets the selection
     * @param start position where selection starts
     * @param end position where selection ends
     */ 
    public void setSelection(int start, int end) {
        int oldStart = selectionStart;
        int oldEnd = selectionEnd;
        if (start<=end) {
            selectionStart = start;
            selectionEnd = end;
        } else {
            selectionStart = end;
            selectionEnd = start;
        }
        repaintChars(oldStart,oldEnd);
        repaintChars(selectionStart,selectionEnd);
    }

    /**
     * Updates the selection after a backward move
     * @param select wether the move was a selecting one
     */
    void backwardMove(boolean select) {
        if (select) {
            if (caretPosition<selectionStart)
                setSelectionStart(caretPosition);
            else
                setSelectionEnd(caretPosition);
        } else {
            resetSelection();
        }
    }

    /**
     * Updates the selection after a backward move
     * @param select wether the move was a selecting one
     */
    void forwardMove(boolean select) {
        if (select) {
            if (caretPosition>selectionEnd)
                setSelectionEnd(caretPosition);
            else
                setSelectionStart(caretPosition);
        } else {
            resetSelection();
        }
    }

    /**
     * Moves forward n characters
     * @param n number of characters to move forward
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void forwardChar(int n, boolean select) {
        setCaretPosition(caretPosition + n);
        forwardMove(select);
        positionVisible();
    }
    
    /**
     * Moves backward n characters
     * @param n number of characters to move backward
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void backwardChar(int n, boolean select) {
        setCaretPosition(caretPosition - n);
        backwardMove(select);
        positionVisible();
    }

    /**
     * Moves forward n words
     * @param n number of words to move forward
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void forwardWord(int n, boolean select) {
        boolean endOfSelection = endOfSelection();
        int newPos = caretPosition;
        while(!eot() && isDivider(text.charAt(newPos))) {
            newPos++;
        }
        for (; n>0; n--) {
            while(!eot() && !isDivider(text.charAt(newPos))) {
                newPos++;
            }
        }
        setCaretPosition(newPos);
        forwardMove(select);
        positionVisible();
    }

    /**
     * Moves backward n words
     * @param n number of words to move backward
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void backwardWord(int n, boolean select) {
        int newPos = caretPosition;
        if (newPos>0) {
            newPos--;
        }
        while (newPos>0 && isDivider(text.charAt(newPos))) {
            newPos--;
        }
        for (; n>0; n--) {
            while(newPos>0 && !isDivider(text.charAt(newPos))) {
                newPos--;
            }
        }
        if (!eot() && newPos>0)
            newPos++;
        setCaretPosition(newPos);
        backwardMove(select);
        positionVisible();
    }

    /**
     * Move to the next line 
     * @param n move to previous line n times
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void nextLine(int n, boolean select) 
    {
        boolean moved = false;        
        int newPos = caretPosition;
        for (; n>0; n--) {
            int posInLine = getPosInLine(newPos);
            int nextLineStart = text.indexOf('\n', newPos);
            if (nextLineStart != -1)
            {
                nextLineStart ++;
                int i = 0;
                while(i < posInLine && i + nextLineStart < text.length() && 
                      text.charAt(i + nextLineStart) != '\n')
                {
                    i ++;
                }
                if ((nextLineStart+i) >newPos)
                    moved = true;
                newPos = nextLineStart + i;
            }
        }
        if (moved) {
            setCaretPosition(newPos);
            forwardMove(select);
        }

        positionVisible();
    }

    /**
     * Move to the previous line 
     * @param n move to previous line n times
     * @param select wether to add the text moved over by the caret to
     * the selection
     */
    public void previousLine(int n, boolean select) 
    {
        boolean moved = false;
        int newPos = caretPosition;
        for (; n>0; n--) {
            int posInLine = getPosInLine(newPos);
            int prevLineStart = 
                text.lastIndexOf('\n', newPos - posInLine - 2) + 1;
            int i = 0;
            while(i < posInLine && i + prevLineStart < text.length() && 
                  text.charAt(i + prevLineStart) != '\n')
            {
                i ++;
            }
            if (prevLineStart+i<newPos)
                moved = true;
            newPos = prevLineStart+i;
        }
        if (moved) {
            setCaretPosition(newPos);
            backwardMove(select);
        }

        positionVisible();           
    }

    /**
     * Moves the caret to the beginning of the text.
     * @param select wether to add the text moved over by the caret to
     * the selection      
     */
    public void beginningOfText(boolean select)
    {
        setCaretPosition(0);
        backwardMove(select);
        positionVisible();
    }


    /**
     * Moves the caret to the end of the text.
     * @param select wether to add the text moved over by the caret to
     * the selection      
     */
    public void endOfText(boolean select)
    {
        setCaretPosition(text.length());
        backwardMove(select);
        positionVisible();
    }

    /**
     * Moves the caret to the beginning of the current line.
     * @param select wether to add the text moved over by the caret to
     * the selection      
     */
    public void beginningOfLine(boolean select)
    {
        int newPos = caretPosition;
        if (getPosInLine(newPos) <= getWhiteAtLineStart(newPos)) {
            newPos -= getPosInLine(newPos);
        } else {
            newPos -= 
                getPosInLine(newPos) - 
                getWhiteAtLineStart(newPos);
        }
        setCaretPosition(newPos);
        backwardMove(select);
        positionVisible();

    }

    public void realBeginningOfLine(boolean select)
    {
        int newPos = caretPosition;
        if (getPosInLine(newPos) <= getWhiteAtLineStart(newPos)) {
            newPos -= getPosInLine(newPos);
        }
        setCaretPosition(newPos);
        backwardMove(select);
        positionVisible();
    }

    /**
     * Moves the caret to the end of the current line.
     * @param select wether to add the text moved over by the caret to
     * the selection      
     */
    public void endOfLine(boolean select) {
        setCaretPosition(caretPosition +
            getLineWidth(caretPosition) - getPosInLine(caretPosition));
        forwardMove(select);
        positionVisible();
    }

    /**
     * Sets the caret at the beginning of a line
     * @param lineNumber number of the line (starts with 1)
     */
    public void gotoLine(int lineNumber) {
        beginningOfText(false);
        if (lineNumber>1) {
            nextLine(lineNumber-1,false);
        }
    }

    /**
     * Resets the selection. Sets both its start and end to caretPosition.
     */
    void resetSelection() {
        int oldStart = selectionStart;
        int oldEnd = selectionEnd;
        selectionEnd = caretPosition;
        selectionStart = caretPosition;
        for (int p=oldStart; p<oldEnd; p++)
            repaintCharAt(p);
    }
   
    /**
     * Selects the word around a position
     */
    void selectWord(int position) {
        int oldStart = selectionStart;
        int oldEnd = selectionEnd;

        selectionEnd = caretPosition;
        selectionStart = caretPosition;
        while (selectionEnd<text.length() 
               && Character.isLetterOrDigit(text.charAt(selectionEnd))) {
            selectionEnd++;
        }
        while (selectionStart>=0 && selectionStart<text.length() &&
               Character.isLetterOrDigit(text.charAt(selectionStart))) {
            selectionStart--;
        }
        if (selectionStart>=0 && selectionStart<text.length() &&
            !Character.isLetterOrDigit(text.charAt(selectionStart)))
            selectionStart++;

        for (int p=oldStart; p<oldEnd; p++)
            repaintCharAt(p);
    }

    CompletionEngine completionEngine;

    public void setCompletionEngine(CompletionEngine ce) {
        completionEngine = ce;
    }

    public CompletionEngine getCompletionEngine() {
        return completionEngine;
    }

    void runCompletionEngine(int direction) {

        if (completionEngine!=null) {

            String writtenText = "";
            int pos;
            if (selectionStart!=selectionEnd) {
                pos = selectionStart;
            } else {
                pos = caretPosition;
            }
            int beginWritten = pos;
            StringBuffer currentProposal = new StringBuffer();
            // test if the user has already typed something
            if (pos>0 && !isDivider(text.charAt(pos-1))) {
                loggerComp.debug("written word found");
                beginWritten--;
                // go to the begining of the word
                while (beginWritten>0 && !isDivider(text.charAt(beginWritten-1))) {
                    beginWritten--;
                }
                writtenText = text.substring(beginWritten, pos);
                if (selectionStart!=selectionEnd) {
                    currentProposal.append(writtenText);
                }
            }

            currentProposal.append(text.substring(selectionStart, selectionEnd));
            int initPosition = caretPosition;

            // removes the current proposal
            initPosition = selectionStart;
            remove(selectionStart, selectionEnd - selectionStart);
            resetSelection();
            isSelectionCompletion = true;

            String proposedText = 
                completionEngine.getProposal(
                    text,beginWritten,writtenText,
                    currentProposal.toString(),
                    direction);
            if (proposedText.length()>0) {
                insertString(caretPosition,proposedText.substring(writtenText.length()));
            }

            setSelectionStart(initPosition);
            setSelectionEnd(caretPosition);

        }
    }

    KeyListener toolKeyListener;
    public void toolDone() {
        toolKeyListener = null;
    }

    /** 
     * Key Pressed
     */
    public void keyPressed(KeyEvent e)
    {
        if (toolKeyListener!=null) {
            toolKeyListener.keyPressed(e);
            return;
        }
        if (e.isControlDown())
        {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_C:
                    copy(); 
                    break;
                case KeyEvent.VK_V:
                    paste(); 
                    break;
                case KeyEvent.VK_X:
                    cut(); 
                    break;
                case KeyEvent.VK_Z:
                    undo(); 
                    break;
                case KeyEvent.VK_Y:
                    redo();
                    break;
                case KeyEvent.VK_S:
                    toolKeyListener = new SearchTool(this,caretPosition);
                    break;
                case KeyEvent.VK_RIGHT:
                    forwardWord(1, e.isShiftDown());
                    break;
                case KeyEvent.VK_LEFT:
                    backwardWord(1, e.isShiftDown());
                    break;
                case KeyEvent.VK_HOME:
                    beginningOfText(e.isShiftDown());
                    break;
                case KeyEvent.VK_END:
                    endOfText(e.isShiftDown());
                    break;
                case KeyEvent.VK_SPACE:
                    runCompletionEngine(
                        e.isShiftDown()?CompletionEngine.BACKWARD:CompletionEngine.FORWARD);
                    break;
                default:
            }
        }
        else if ((e.getModifiers() & KeyEvent.ALT_MASK) > 0)
        {
        }
        else if ((e.getModifiers() & KeyEvent.ALT_GRAPH_MASK) > 0)
        {
        }
        else if (e.isShiftDown())
        {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    backwardChar(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_RIGHT:
                    forwardChar(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_UP: 
                    previousLine(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    nextLine(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_HOME:
                    beginningOfLine(e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_END:
                    endOfLine(e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    previousLine(15,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    nextLine(15,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_TAB:
                    {
                        if (selectionStart != selectionEnd)
                        {
                            int start = selectionStart;
                            int end = selectionEnd;
                     
                            for(int pos = end; 
                                pos >= start - getPosInLine(start) && pos >= 0; 
                                pos--)
                            {
                                if (pos == 0 || text.charAt(pos - 1) == '\n')
                                {
                                    for(int i=0; 
                                        i < conf.getTabWidth() && text.charAt(pos) == ' '; 
                                        i++)
                                    {
                                        remove(pos, 1);
                                        end--;
                                    }
                                }
                            }
                            setSelection(start,end);
                        }
                        else
                        {
                        }            
                        e.consume();
                    }
                    break;
                default:
            }
        }
        else
        {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    backwardChar(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_RIGHT:
                    forwardChar(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_UP:
                    previousLine(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    nextLine(1,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_TAB:
                    if (selectionStart == selectionEnd)
                    {
                        insertTab(caretPosition);
                    }
                    else
                    {                  
                        int start = selectionStart;
                        int end = selectionEnd;
                     
                        for(int pos=end; 
                            pos>=start-getPosInLine(start) && pos>=0; 
                            pos--)
                        {
                            if (pos == 0 || text.charAt(pos - 1) == '\n')
                            {
                                end += conf.getTabWidth();
                                insertTab(pos);
                            }
                        }
                        setSelection(start,end);
                    }
                    e.consume();
                    break;
                case KeyEvent.VK_DELETE:
                    if (selectionStart == selectionEnd)
                    {
                        if (caretPosition < text.length()) {
                            remove(caretPosition, 1);
                        }
                    }
                    else
                    {
                        remove(selectionStart, selectionEnd - selectionStart);
                    }
                    resetSelection();
                    positionVisible();
                    e.consume();
                    break;
                case KeyEvent.VK_HOME:
                    beginningOfLine(e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_END:
                    endOfLine(e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    previousLine(15,e.isShiftDown());
                    e.consume();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    nextLine(15,e.isShiftDown());
                    e.consume();
                    break;
                default:
                    e.consume();
            }
        }
    }

    /** 
     * Key Typed
     */
    public void keyTyped(KeyEvent e)
    {
        if (toolKeyListener!=null) {
            toolKeyListener.keyTyped(e);
            return;
        }
        if (e.isControlDown())
        {
        }
        else if ((e.getModifiers() & KeyEvent.ALT_MASK) > 0)
        {
        }
        else
        {
            switch (e.getKeyChar()) {
                case KeyEvent.VK_TAB:
                case KeyEvent.VK_DELETE:
                    e.consume();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    if (selectionStart == selectionEnd && caretPosition > 0)
                    {
                        remove(caretPosition - 1, 1);
                        resetSelection();
                    }
                    else
                    {
                        remove(selectionStart, selectionEnd - selectionStart);
                        resetSelection();
                    }
                    e.consume();
                    break;
                case KeyEvent.VK_ENTER:
                    {
                        if (selectionStart != selectionEnd)
                            remove(selectionStart, selectionEnd - selectionStart);
                        insertReturn();
                        e.consume();
                    }
                    break;
                case '}':
                    {
                        if (selectionStart != selectionEnd)
                            remove(selectionStart, selectionEnd - selectionStart);
                        insertCloseCBracket();
                    }
                    break;
                default:
                    if (selectionStart != selectionEnd && 
                        (!isSelectionCompletion || !isAcceptCompletionChar(e.getKeyChar())))
                        remove(selectionStart, selectionEnd - selectionStart);
                    isSelectionCompletion = false;
                    insertChar(e.getKeyChar());
                    // automatically run completion at the end of words
                    if (!isDivider(e.getKeyChar()) && conf.isAutoComplete() &&
                        syntaxUnderCaret!=COMMENT && syntaxUnderCaret!=STRING &&
                        (eot() || isDivider(text.charAt(caretPosition)))) {
                        runCompletionEngine(CompletionEngine.FORWARD);
                    } else if (completionEngine!=null && 
                               completionEngine.isAutomaticCompletionChar(e.getKeyChar()))
                    {
                        int i=caretPosition;
                        completionEngine.runAutomaticCompletion(
                            this,text,caretPosition,e.getKeyChar());
                        caretPosition=i;
                    }
                    //repaint(); 
                    positionVisible();
            }
        }
    }

    /**
     * Wether to accept proposed completion when a char is typed
     */
    boolean isAcceptCompletionChar(char c) {
        return isDivider(c) || c=='.';
    }

    /**
     * Inserts a closing curly bracket and takes indentation into
     * account. 
     */
    public void insertCloseCBracket() {
        int white = getWhiteAtLineStart(caretPosition);
        int lineWidth = getLineWidth(caretPosition);
        int posInLine = getPosInLine(caretPosition);
        int neededWhite = 0;
      
        int count = 1;
        int pos = getCaretPosition() - 1;
      
        if (pos > 0)
        {
            do
            {
                if (text.charAt(pos) == '}')
                    count ++;
                else if (text.charAt(pos) == '{')
                    count --;                                 
            
                if (count != 0) 
                    pos --;
            }
            while (pos > -1 && count > 0);
        }
         
        // array style (={a,b})
        if (pos==text.length()-1 || text.charAt(pos+1)!='\n') {
            insertChar('}');
            return;
        }
         
        // block style
        while (pos > 0 && text.charAt(pos - 1) != '\n')
        {
            neededWhite ++;
            pos --;
            if(text.charAt(pos)!=' ') neededWhite=0;
        }
      
        if (white == posInLine && white > neededWhite)
        {
            remove(caretPosition - posInLine + neededWhite, 
                   white - neededWhite);
            insertChar('}');
        }
        else if (white == posInLine)
        {
            insertChar('}');
        }
        else
        {
            insertChar('\n');
            for(int i=0; i<neededWhite; i++)
                insertChar(' ');
            insertChar('}');
        }                        
    }

    /**
     * Inserts a carriage return and takes indentation into account if
     * at the end of a line.
     */
    public void insertReturn() {
        int white = 0;
        if (caretPosition==text.length() || text.charAt(caretPosition)=='\n') {
            white = getWhiteAtLineStart(selectionStart);
            if (selectionStart > 0 && text.charAt(selectionStart - 1) == '{') {
                white += 4;
            }
        } else {
            int savedCaret = caretPosition;
            int white2 = getWhiteAtLineStart(caretPosition);
            realBeginningOfLine(false);
            if (caretPosition>0) {
                white = getWhiteAtLineStart(caretPosition-1) - white2;
                if (savedCaret - caretPosition<=white2)
                    white += savedCaret - caretPosition;
            }
            caretPosition = savedCaret;
        }
        String ins = "" + '\n';
        for(int i=0; i<white; i++) ins += " ";
        insertString(caretPosition, ins);
    }
   
    /** 
     * Inserts a white spaces in the text insted of a tab.
     * @param pos The position in the text where the white spaces will be added.
     */
    void insertTab(int pos)
    {
        insertString(pos, Strings.newString(' ',conf.getTabWidth()));
    }

    /** 
     * Key Released
     */
    public void keyReleased(KeyEvent e)
    {
        testOposing();
    }

    /** 
     * Used to get the position of a point in a line.
     * @param pos the position in text to calculate poition from.
     * @return The amount of caracters from the previous \n or start of text to pos
     */
    int getPosInLine(int pos)
    {
        int ret = text.lastIndexOf('\n', pos - 1);
        ret ++;
        ret = pos - ret;   
        return ret;
    }

    /** 
     * Used to get the width of a line.
     * @patam A position in the text, that is in the line.
     * @return the width of the line where the point is located.
     */
    int getLineWidth(int pos)
    {
        int start = 0;
        int end = text.indexOf('\n', pos);
        if (pos > 0) {
            start = text.lastIndexOf('\n',pos-1) + 1;
        }
      
        if (end == -1) 
            end = text.length();
      
        return end - start;
    }


    /** 
     * Used to get the content of a line.
     * @patam A position in the text, that is in the line.
     * @return the line's content
     */
    String getLineText(int pos)
    {
        int start = 0;
        int end = text.indexOf('\n', pos);
        if (pos > 0) {
            start = text.lastIndexOf('\n', pos-1) + 1;
        }
      
        if (end == -1) 
            end = text.length();
      
        return text.substring(start,end);
    }

    /** 
     * Returns the number of white spaces at the begining of a line.
     * @param pos A position in the line where you whant to find the white spaces.
     * @return the number of white spaces at the begining of a line.
     */
    int getWhiteAtLineStart(int pos)
    {
        int ret = 0;
      
        for(int i = pos - getPosInLine(pos); 
            i >= 0 && i < text.length() && text.charAt(i) == ' '; 
            i++)
        {
            ret ++;
        }
      
        return ret;
    }

    /** 
     * Inserts a char at caretPosition.
     */
    void insertChar(char c)
    {
        changed = true;
        doneActions.add(0, new TextAction(TextAction.INSERT, caretPosition, 1, ""));
        text = text.substring(0, caretPosition) + c + 
            text.substring(caretPosition, text.length());
        caretPosition ++;
        resetSelection();

        fireTextUpdate();
        repaint(); 
        positionVisible();
    }

    /** 
     * Inserts a String s into the text at pos.
     * @param pos The position where you whant to insert a String.
     * @param str The String you whant to insert into the text.
     */
    public void insertString(int pos, String str)
    {
        changed = true;
        doneActions.add(0, new TextAction(TextAction.INSERT, pos, str.length(), ""));
        text = text.substring(0, pos) + str + text.substring(pos, text.length());
        if (caretPosition >= pos)
        {
            caretPosition += str.length();
            resetSelection();
        }
        fireTextUpdate();
        repaint(); 
        positionVisible();
    }

    /** 
     * Removes the content of the text from offset to offset + length.
     * @param offset Where to start removing text.
     * @param length length of the text you whant to remove.
     */
    public void remove(int offset, int length)
    {
        changed = true;
        if (offset >= 0 && offset + length <= text.length())
        {
            doneActions.add(
                0, 
                new TextAction(TextAction.REMOVE, offset, length, 
                               text.substring(offset, offset + length)));
         
            text = text.substring(0,offset) + text.substring(offset+length,text.length());
            if (caretPosition > offset && caretPosition <= offset + length)
            {
                setCaretPosition(offset);
            }
            else if (caretPosition > offset)
            {
                setCaretPosition(caretPosition-length);
            }
            repaint();
            fireTextUpdate();
            positionVisible();
        }
    }

    /** 
     * Tels the SHEditor to undo the last action.
     */
    public void undo()
    {
        if (doneActions.size() > 0)
        {
            changed = true;
            TextAction ta = (TextAction)doneActions.remove(0);
            try
            {
                if (ta.action == TextAction.INSERT) {
                    redoneActions.add(
                        0, 
                        new TextAction(TextAction.REMOVE, 
                                       ta.position, 
                                       ta.length, 
                                       text.substring(ta.position, ta.position + ta.length)));
                    text = text.substring(0, ta.position) + 
                        text.substring(ta.position + ta.length, text.length());
                } else if (ta.action == TextAction.REMOVE) {
                    redoneActions.add(
                        0, 
                        new TextAction(TextAction.INSERT, ta.position, ta.length, ""));
                    text = text.substring(0, ta.position) + ta.string + 
                        text.substring(ta.position, text.length());
                }
                caretPosition = ta.position;
            }
            catch(Exception e)
            {
            }
            fireTextUpdate();
            repaint();
        }
    }

    /** 
     * Tels the SHEditor to redo the last undone action.
     */
    public void redo()
    {
        if (redoneActions.size() > 0)
        {
            changed = true;
            TextAction ta = (TextAction)redoneActions.remove(0);
            try
            {
                if (ta.action == TextAction.INSERT)
                {
                    doneActions.add(
                        0, 
                        new TextAction(
                            TextAction.REMOVE, ta.position, ta.length, 
                            text.substring(ta.position, ta.position + ta.length)));
                    text = text.substring(0, ta.position) + 
                        text.substring(ta.position + ta.length, text.length());
                }
                else
                {
                    doneActions.add(
                        0, 
                        new TextAction(TextAction.INSERT, ta.position, ta.length, ""));
                    text = text.substring(0, ta.position) + 
                        ta.string + text.substring(ta.position, text.length());
                }
                caretPosition = ta.position;
            }
            catch(Exception e)
            {
            }
            fireTextUpdate();
            repaint();
        }
    }

    /** 
     * Returns the number of lines to a point.
     * @param pos the position to where you whant to count the lines.
     * @return The numbers of lines to the pos.
     */
    int getLine(int pos)
    {
        int ret = 0;
      
        int brPos = 0;
        boolean done = false;
      
        while (brPos != -1 && !done)
        {
            brPos = text.indexOf('\n', brPos);
         
            if (pos <= brPos)
            {
                done = true;
            }
        }
        return ret;
    }

    /** 
     * Preformes a cut action on the SHEditor.
     */
    public void cut()
    {
        if (selectionStart != selectionEnd)
        {
            try
            {
                Clipboard clipboard = getToolkit().getSystemClipboard();
                String post = text.substring(selectionStart, selectionEnd);
                clipboard.setContents(new StringSelection(post), this);
                remove(selectionStart,selectionEnd-selectionStart);
                resetSelection();
                setCaretPosition(selectionStart);
                positionVisible();
            }
            catch(Exception e)
            {
            }
        }
    }

    /** 
     * Preformes a copy action on the JJAvaPane.
     */
    public void copy()
    {
        if (selectionStart != selectionEnd)
        {
            try
            {
                Clipboard clipboard = getToolkit().getSystemClipboard();
                String post = text.substring(selectionStart, selectionEnd);
                clipboard.setContents(new StringSelection(post), this);
            }
            catch(Exception e)
            {
            }
        }
    }

    /** 
     * Preformes a paste action on the SHEditor.
     */
    public void paste()
    {
        loggerClip.debug("paste...");
        if (selectionStart != selectionEnd)
        {
            remove(selectionStart,selectionEnd-selectionStart);
            resetSelection();
            setCaretPosition(selectionStart);
        }
        Clipboard clipboard = getToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(this);
        if (content != null)
        {
            try
            {
                insertString(caretPosition, 
                             (String)content.getTransferData(DataFlavor.stringFlavor));
            }
            catch (Exception e)
            {            
                loggerClip.error("error in copying",e);
            }
        } else {
            loggerClip.debug("content is null");
        }
        fireTextUpdate();
    }

    /**
     * Returns the number of a char in a the text between to positions.
     * @param c the char to count.
     * @param offset offset where to start looking.
     * @param length length the length of the area to search.
     */
    public int countChar(char c, int offset, int length)
    {
        int ret = 0;
        for (int i=offset; i < offset+length; i++) {
            if (text.charAt(i) == c) 
                ret ++;
        }
        return c;
    }

    public void repaint() {
        super.repaint();
        //logger.debug("repaint",new Exception());
    }

    int getCharWidth(char c) {
        if (Character.isDefined(c))
            return metrics.charWidth(c);
        else 
            return 0;
    }

    /** 
     * Paints the SHEditor. syntaxUnderCaret is updated.
     * @param g the graphics to draw the SHEditor on.
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g.setFont(getFont());                              
        lineHeight = metrics.getHeight();

        Rectangle rect = getVisibleRect();
        logger.debug("SHEditor.paintComponent on "+rect+" / "+g.getClip());
        rect = g.getClipBounds();

        g.setColor(conf.getBackgroundColor());
        g.fillRect(rect.x + getMarginLeft(), rect.y, 
                   rect.width - getMarginLeft(), rect.height);
        g.setColor(new Color(180, 180, 180));
        g.fillRect(rect.x, rect.y, getMarginLeft(), rect.height);
      
        int line = 1;
        int pos = 0;
        boolean done = false;
        boolean ignore = false;

        StringBuffer word = new StringBuffer(); // Current word to be displayed
      
        int posXStart = getMarginLeft();
        int posX = posXStart;
        int maxPosX = posX;
        int posY = lineHeight;

        // Calculate start line and position
        while(posY + lineHeight < rect.y)
        {
            posY += lineHeight;
            if (pos + 1 < text.length()) {
                pos = text.indexOf("\n", pos);
                pos ++;
                line ++;
            } else {
                break;
            }
        }

        if (text.lastIndexOf("/ *", pos) > text.lastIndexOf("* /", pos))
        {
            int lastPos = text.lastIndexOf("/*", pos);
            int posInLine = getPosInLine(lastPos);          
            int count = countChar('"', lastPos - posInLine, posInLine);          
            ignore = (count%2 == 0);
        }          

        syntaxUnderCaret = DEFAULT;
        while (pos < text.length())
        {
            done = false;
            g.setColor(conf.getTextColor());
         
            if (ignore)
            {
                word.append(text.charAt(pos));
                pos ++;
      
                while (pos < text.length() && !done)
                {
                    if (text.charAt(pos) == '/' && text.charAt(pos - 1) == '*') {
                        done = true;
                    }
                    word.append(text.charAt(pos));
                    pos ++;               
                }
            }
            // Double-quoted strings ("blabla")
            else if (word.length() == 0 && text.charAt(pos) == '"')
            {
                int startp = pos;
                word.append(text.charAt(pos));
                pos ++;
            
                while (pos < text.length() && !done)
                {
                    if (text.charAt(pos) == '"') {
                        if (text.charAt(pos - 1) != '\\' || 
                            text.charAt(pos - 2) == '\\') 
                        {
                            done = true;
                        }
                    } else if (text.charAt(pos) == '\n') {
                        // Quoted Strings are not multiline
                        done = true;
                    }
         
                    word.append(text.charAt(pos));
                    pos ++;               
                }
                if (caretPosition<pos && caretPosition>startp)
                    syntaxUnderCaret = STRING;
                g.setColor(conf.getStringColor());
            }
            // Single-quoted strings ('blabla')
            else if (word.length() == 0 && text.charAt(pos) == '\'')
            {
                int startp = pos;
                word.append(text.charAt(pos));
                pos ++;
      
                while (pos < text.length() && !done)
                {
                    if (text.charAt(pos) == '\'') {
                        if (text.charAt(pos - 1) != '\\' || 
                            text.charAt(pos - 2) == '\\') 
                        {
                            done = true;
                        }
                    } else if (text.charAt(pos) == '\n') {
                        // Quoted Strings are not multiline
                        done = true;
                    }
         
                    word.append(text.charAt(pos));
                    pos ++;               
                }
                if (caretPosition<pos && caretPosition>startp)
                    syntaxUnderCaret = STRING;
                g.setColor(conf.getStringColor());
            }
            // Single line comments (// comment blabla)
            else if (text.charAt(pos) == '/' && 
                     pos < text.length() - 1 && 
                     text.charAt(pos + 1) == '/' && 
                     (pos == 0 || text.charAt(pos - 1) != '*'))
            {
                int startp = pos;
                word.append(text.charAt(pos));
                pos ++;
      
                while (pos < text.length() && !done)
                {
                    if (text.charAt(pos) == '\n') {
                        done = true;
                    }
                    word.append(text.charAt(pos));
                    pos ++;               
                }
                if (caretPosition<=pos && caretPosition>startp)
                    syntaxUnderCaret = COMMENT;
                g.setColor(conf.getIgnoreColor());
            }
            // Multiline comments (/* comment blabla */)
            else if (text.charAt(pos) == '/' && 
                     pos < text.length() - 1 && 
                     text.charAt(pos + 1) == '*' && 
                     (pos == 0 || text.charAt(pos - 1) != '/'))
            {
                int startp = pos;
                word.append(text.charAt(pos));
                pos ++;
      
                while (pos < text.length() && !done)
                {
                    if (text.charAt(pos) == '/' && 
                        text.charAt(pos - 1) == '*') 
                    {
                        done = true;
                    }
                    word.append(text.charAt(pos));
                    pos ++;               
                }
                if (caretPosition<pos && caretPosition>startp)
                    syntaxUnderCaret = COMMENT;
                g.setColor(conf.getIgnoreColor());
            }
            // Word separator
            else if (isDivider(text.charAt(pos)))
            {
                if (word.length() == 0) {
                    word.append(text.charAt(pos));
                    pos ++;
                }
                done = true;
            }
            // Normal word
            else
            {          
                while (pos < text.length() && !done)
                {
                    if (isDivider(text.charAt(pos))) {
                        done = true;
                    } else {
                        word.append(text.charAt(pos));
                        pos ++;
                    }
                }
                if (isKeyword(word.toString())) {
                    g.setColor(conf.getKeywordColor());
                } else if (isModifier(word.toString())) {
                    g.setColor(conf.getModifierColor());
                } else if (isType(word.toString())) {
                    g.setColor(conf.getTypeColor());
                } else {
                    g.setColor(conf.getTextColor());
                }
            }

            if (ignore) {
                g.setColor(conf.getIgnoreColor());
                ignore = false;
            } else if (word.toString().equals("{") || 
                       word.toString().equals("}")) {
                g.setColor(conf.getClampColor());
            }

            for(int i=0; i<word.length(); i++)
            {
                char c = word.charAt(i);
                int charWidth = getCharWidth(c);

                if (posY + lineHeight > rect.y && 
                    pos == openPos + 1 || pos == closePos + 1)
                {
                    // Poink background for openin gor closing chars ('(','[',')',']',...)
                    Color tmpColor = g.getColor();
                    g.setColor(Color.pink);
                    if (posX >= rect.x && posX < rect.x + rect.width)
                        g.fillRect(posX, posY - metrics.getAscent(),
                                   charWidth,
                                   metrics.getAscent());
                    g.setColor(tmpColor);
                } 
                else if (posY + lineHeight > rect.y && 
                         pos - word.length() + i < selectionEnd && 
                         pos - word.length() + i >= selectionStart)
                {
                    // draw background for char
                    Color tmpColor = g.getColor();
                    g.setColor(isSelectionCompletion ?
                               conf.getCompletionColor() : conf.getSelectionColor());
                    if (posX >= rect.x && posX < rect.x + rect.width)
                        g.fillRect(posX, posY - metrics.getAscent(), 
                                   charWidth, 
                                   metrics.getAscent());
                    g.setColor(tmpColor);
                }

                if (posY + lineHeight > rect.y && 
                    hasFocus() && 
                    showCaret && 
                    caretPosition == pos - word.length() + i)
                {
                    car.x = posX;
                    car.y = posY - lineHeight;
                    car.height = lineHeight;
                }
                if (c == '\n')
                {
                    Color tmpColor = g.getColor();
                    g.setColor(new Color(180, 180, 180));
                    g.fillRect(rect.x, posY - lineHeight, 
                               getMarginLeft(), lineHeight);
                    g.setColor(new Color(200, 0, 0));
                    g.drawString(
                        "" + line, 
                        rect.x + (getMarginLeft()-10)
                        - metrics.charsWidth(("" + line).toCharArray(), 
                                             0, ("" + line).length()), 
                        posY - metrics.getDescent());
            
                    if (lineToMark == line)
                    {
                        g.setColor(Color.green);
                        g.fillPolygon(
                            new int[] { rect.x + 58, rect.x + 63, rect.x + 58 }, 
                            new int[] { posY - (int)(lineHeight / 1.5d), 
                                        posY - (int)(lineHeight / 3d), 
                                        posY },
                            3);
                    }
                    g.setColor(tmpColor);
            
                    posY += lineHeight;
                    posX = posXStart;
                    line ++;               
                }
                else if (c == '\t')
                {
                    posX += getCharWidth(' ') * conf.getTabWidth();
                }
                else if (charWidth > 0 && posY + lineHeight > rect.y)
                {
                    if ((posX+charWidth) >= rect.x && posX < rect.x + rect.width)
                        g.drawString("" + c, posX, posY - metrics.getDescent());
                    posX += charWidth;
                }           
            }
         
            if (posX > maxPosX) {
                maxPosX = posX;
            }

            // If we're out of the viewport, skip until the end
            if (posY - lineHeight > rect.y + rect.height)
            {
                for(int p = pos; p != -1; p = text.indexOf('\n', p + 1))
                {
                    posY += lineHeight;
               
                    int width = getPixelTextWidth(getLineText(p));
                    if (width > maxPosX) {
                        maxPosX = width;
                    }
                }
            
                pos = text.length();
            }
         
            word.delete(0, word.length());
        }
      
        if (conf.getShowLineNumbers() && 
            (posY - lineHeight <= rect.y + rect.height))
        {
            g.setColor(conf.getLineNrBgColor());
            g.fillRect(rect.x, posY - lineHeight, 
                       getMarginLeft(), lineHeight);
            g.setColor(conf.getLineNrColor());
            g.drawString("" + line, 
                         rect.x + (getMarginLeft()-10) 
                         - metrics.charsWidth(("" + line).toCharArray(), 
                                              0, ("" + line).length()), 
                         posY - metrics.getDescent());
         
            if (lineToMark == line)
            {
                g.setColor(Color.green);
                g.fillPolygon(
                    new int[] { rect.x + 58, rect.x + 63, rect.x + 58 },
                    new int[] { posY - (int)(lineHeight / 1.5d),
                                posY - (int)(lineHeight / 3d),
                                posY },
                    3);
            }
        }
      
        if (hasFocus() && 
            posY - lineHeight <= rect.y + rect.height && 
            showCaret && pos == text.length() && 
            pos == caretPosition)
        {
            car.x = posX;
            car.y = posY - lineHeight;
            car.height = lineHeight;
        }

        if (hasFocus())
            g.setColor(Color.black);
        else
            g.setColor(Color.gray);
        g.fillRect(car.x, car.y, car.width, car.height);
      
        if (posY + 10 != getSize().height || maxPosX + 10 != getSize().width)
        {
            setMinimumSize(
                new Dimension(
                    (maxPosX+10 > getSize().width ? maxPosX+10 : getSize().width),
                    posY+10));
            setPreferredSize(
                new Dimension(maxPosX+10,posY+10));
            logger.debug("revalidate");
            revalidate();
        }
    }

    /**
     * Returns the pixel width of a string
     * @param text the string to get the width of
     * @return the width in number of pixels the text will have when drawn.
     */
    protected int getPixelTextWidth(String text) {
        int width = 0;
        for (int i=0; i<text.length(); i++) {
            if (text.charAt(i)==KeyEvent.VK_TAB)
                width += getCharWidth(' ') * conf.getTabWidth();
            else 
                width += getCharWidth(text.charAt(i));
         
        }
        return width;
    }

    /** 
     * Updates the JJAvaPane (cals paint(Graphics g));
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    /**
     * Sets a line as marked, and scrols to that line if needed.
     */
    public void scrollToLine(int line)
    {         
        lineToMark = line;
      
        int l = 1;
        int pos = 0;
        while (l <= line && pos != -1)
        {
            pos = text.indexOf('\n', pos + 1);
            l ++;
        }
        setCaretPosition(pos);
        requestFocus();
        //repaint();
        positionVisible();
    }    

    /** 
     * Tests oposing (), [] and {}, and makes sure they are marked if
     * the caret is behind one of them.  
     */
    public void testOposing()
    {  
        //unmarkOposing();
        if (getCaretPosition() > -1 && getCaretPosition() - 1 < text.length())
        {
            if (getCaretPosition() > 0)
            {
                char c = text.charAt(getCaretPosition() - 1);
                if (c == '}') {
                    testOpening('{','}');
                } else if (c == '{') {
                    testClosing('{','}');
                } else if (c == ')') {
                    testOpening('(',')');
                } else if (c == '(') {
                    testClosing('(',')');
                } else if (c == ']') {
                    testOpening('[',']');
                } else if (c == '[') {
                    testClosing('[',']');
                } else {
                    unmarkOposing();
                }
            } else {
                repaint();
            }
        }
    }

    public void testClosing(char opening, char closing) {
        if (text.charAt(getCaretPosition() - 1 ) == opening)
        {
            int oldOpenPos = openPos;
            int oldClosePos = closePos;

            openPos = getCaretPosition() - 1;
         
            int count = 1;
            int pos = getCaretPosition() - 1;
            
            while (pos < text.length() - 1 && count > 0) {
                pos ++;
                if (text.charAt(pos) == opening)
                    count ++;
                else if (text.charAt(pos) == closing)
                    count --;
            }
            
            if (count > 0) {
                closePos = -1;
                openPos = -1;
            } else {
                closePos = pos;
            }
         
            repaintCharAt(oldOpenPos);
            repaintCharAt(oldClosePos);
            repaintCharAt(openPos);
            repaintCharAt(closePos);
            repaintCharAt(caretPosition);
        }
    }

    public void testOpening(char opening, char closing) {
        if (text.charAt(getCaretPosition() - 1) == closing)
        {
            int oldOpenPos = openPos;
            int oldClosePos = closePos;

            openPos = getCaretPosition() - 1;
            
            int count = 0;
            int pos = getCaretPosition() - 1;
            
            if (getCaretPosition() > 0) {
                do {
                    if (text.charAt(pos) == closing)
                        count ++;
                    else if (text.charAt(pos) == opening)
                        count --;                                 
                  
                    if (count != 0)
                        pos --;
                } while (pos > -1 && count > 0);
            }

            closePos = pos;
               
            if (closePos == -1)
                openPos = -1;
            
            repaintCharAt(oldOpenPos);
            repaintCharAt(oldClosePos);
            repaintCharAt(openPos);
            repaintCharAt(closePos);
            repaintCharAt(caretPosition);
        }
    }

    /**
     * Unmarks oposing (), {} or [] given by openPos and closePos.
     */
    public void unmarkOposing()
    {
        repaintCharAt(openPos);
        repaintCharAt(closePos);
        openPos = -1;
        closePos = -1;
    }

    /** 
     * Makes sure the caret is visible on screen.
     */
    public void positionVisible()
    {
        positionVisible(caretPosition);
    }

    /** 
     * Makes sure the selection is visible on screen.
     */
    public void selectionVisible()
    {
        positionVisible(selectionStart);
    }

    /**
     * Makes sure a position is visible
     * @param position the position that must be visible
     */
    public void positionVisible(int position)
    {
        // Cast to Object to work around SwingWT inheritance bugs 
        Object parent = getParent();
        if (parent instanceof JViewport)
        {
            Rectangle rect = getVisibleRect();
            Rectangle car = getCaretPos(position);
         
            try
            {
                JScrollPane scrollPane = (JScrollPane)((JViewport)parent).getParent();
                JScrollBar hs = scrollPane.getHorizontalScrollBar();
                JScrollBar vs = scrollPane.getVerticalScrollBar();
            
                int newX = hs.getValue();
                int newY = vs.getValue();
         
                if (rect.x + rect.width - getMarginLeft() <= car.x)
                    newX = car.x + 75 - rect.width;
                else if (rect.x > car.x)
                    newX = car.x - 10;
         
                if (rect.y + rect.height <= car.y + car.height)
                    newY = car.y + 20 + car.height - rect.height;
                else if (rect.y >= car.y) 
                    newY = car.y - 20;
         
                if (newX != hs.getValue()) {
                    hs.setValue(newX);
                }
                if (newY != vs.getValue()) {
                    vs.setValue(newY);
                }
            }
            catch(Exception e)
            {
                logger.error("positionVisible "+position, e);
            }
        } 
    }
   
    /** 
     * Returns a rectangle representation of the caret at a given position.
     * @param position position of the caret
     * @return a Rectangle representation of the caret.
     */
    public Rectangle getCaretPos(int position)
    {
        Rectangle ret = new Rectangle(0, 0, 0 ,0);
      
        if (metrics != null)
        {
            int lineStart = 0;
            int posY = lineHeight;
            int i = 0;
            for(; i < text.length() && i < position; i ++)
            {
                if (text.charAt(i) == '\n')
                {
                    lineStart = i + 1;
                    posY += lineHeight;
                }            
            }
         
            ret.x = metrics.stringWidth(text.substring(lineStart, i));
            ret.y = posY - lineHeight;
            ret.height = lineHeight;
            ret.width = getCharWidth('m');
        }

        return ret;
    }

    /**
     * Sets the font of the SHEditor and repaqints it.
     * @param font the new font.
     */
    public void setFont(Font font)
    {
        super.setFont(font);
        repaint();
    }

    int OFFSET = 35;
    protected int getMarginLeft() {
        return conf.getShowLineNumbers()?OFFSET:0;
    }

    /** 
     * Returns position in the text based on coordinates x and y.
     * @param mousePosX the x coordinate of the position in the text.
     * @param mousePosY the y coordinate of the position in the text.
     * @return the position in the text represented by pint x, y.
     */
    public int getCharPos(int mousePosX, int mousePosY)
    {
        if (metrics != null)
        {
            Rectangle rect = getVisibleRect();
      
            int pos = 0;
            boolean done = false;

            String word = "";
      
            int posXStart = getMarginLeft();
            int posX = posXStart;
            int posY = lineHeight;
         
            while (posY + lineHeight < rect.y)
            {
                posY += lineHeight;
                if (pos + 1 < text.length())
                {
                    pos = text.indexOf('\n', pos);
                    pos ++;
                }
                else
                {
                    break;
                }           
            }
         
            if (mousePosY < posY - lineHeight)
            {
                return -1;
            }
         
            while (pos < text.length()) 
            {
                if (text.charAt(pos) == '\n')
                {
                    if (mousePosX > posX && 
                        mousePosY < posY && 
                        mousePosY >= posY - lineHeight)
                    {
                        return pos;
                    }
                    else if (mousePosX <= posXStart && 
                             mousePosY < posY && 
                             mousePosY >= posY - lineHeight)
                    {
                        if (pos + 1 < text.length())
                        {
                            return pos;
                        }
                    }
                    posY += lineHeight;
                    posX = posXStart;
               
                }
                else if (posX < mousePosX && 
                         posX + (getCharWidth(text.charAt(pos)) / 2) >= mousePosX && 
                         mousePosY < posY && 
                         mousePosY >= posY - lineHeight + 2)
                {
                    return pos;
                }
                else if (posX < mousePosX && 
                         posX + getCharWidth(text.charAt(pos)) >= mousePosX && 
                         mousePosY < posY && 
                         mousePosY >= posY - lineHeight)
                {
                    if (pos + 1 < text.length())
                    {
                        return pos + 1;
                    }
                    posX += getCharWidth(text.charAt(pos));
                }
                else if (mousePosX <= posXStart && 
                         mousePosY < posY && 
                         mousePosY >= posY - lineHeight)
                {
                    if (pos + 1 < text.length())
                    {
                        return pos;
                    }
                    posX += getCharWidth(text.charAt(pos));
                }
                else
                {
                    posX += getCharWidth(text.charAt(pos));
                }
                pos ++;
            }
        }
      
        return text.length();
    }

    /** 
     * Tests if a character is a divider.
     * @param c the char to test.
     * @return true if the char is a divider, meaning it might divide
     * two java words, returns false othervise.  */
    public boolean isDivider(char c)
    {
        for(int i=0; i<separators.length; i++) {
            if (c == separators[i])
                return true;
        }
        return false;
    }

    Set keywords = new HashSet();
    /**
     * Define some new keywords
     * @param addedKeywords the new keywords
     */
    public void addKeywords(String[] addedKeywords) {
        for(int i=0; i<addedKeywords.length; i++) {
            keywords.add(addedKeywords[i]);
        }
    }

    /**
     * Define some new keywords
     * @param addedKeywords the new keywords
     */
    public void addKeywords(Set addedKeywords) {
        keywords.addAll(addedKeywords);
    }

    public void clearKeywords() {
        keywords.clear();
    }

    /**
     * Tells wether a word is keyword or not
     */
    protected boolean isKeyword(String word) {
        return keywords.contains(word);
    }

    Set modifiers = new HashSet();
    /**
     * Define some new keywords
     * @param addedModifiers the new keywords
     */
    public void addModifiers(String[] addedModifiers) {
        for(int i=0; i<addedModifiers.length; i++) {
            modifiers.add(addedModifiers[i]);
        }
    }
    /**
     * Tells wether a word is a modifier or not
     */
    public boolean isModifier(String word) {
        return modifiers.contains(word);
    }

    Set types = new HashSet();
    public void addTypes(String[] addedTypes) {
        for(int i=0; i<addedTypes.length; i++) {
            types.add(addedTypes[i]);
        }      
    }
    /**
     * Tells wether a word is a type or not
     */
    public boolean isType(String word) {
        if (types.contains(word))
            return true;
        int index = word.lastIndexOf('.');
        if (index!=-1) {
            word = word.substring(index+1);
        }
        return word.length()>1 && 
            Character.isUpperCase(word.charAt(0)) && 
            Character.isLowerCase(word.charAt(1));
    }
   
    public void setCaretPosition(int caretPosition) {
        repaintCharAt(this.caretPosition);
        if (caretPosition<0)
            caretPosition = 0;
        else if (caretPosition>text.length())
            caretPosition = text.length();
        this.caretPosition = caretPosition;
        repaintCharAt(caretPosition);
        fireCaretUpdate();
    }

    protected void repaintCharAt(int pos) {
        if (pos!=-1) {
            Rectangle rect = getCaretPos(pos);
            logger.debug("repaintCharAt "+pos+" -> "+rect);
            repaint(rect);
        }
    }

    protected void repaintChars(int start, int end) {
        if (start<end)
            for (int p=start; p<=end; p++) 
                repaintCharAt(p);
        else
            for (int p=end; p<=start; p++) 
                repaintCharAt(p);
    }

    /** 
     * Returns the caretPosition.
     * @return the position of the caret in the text.
     */
    public int getCaretPosition()
    {
        return caretPosition;
    }

    /** 
     * Returns selection start position
     * @return the position of the selectionStart in the text.
     */
    public int getSelectionStart()
    {
        return selectionStart;
    }
   
    /** 
     * Returns selection end position.
     * @return the position of the selectionEnd in the text.
     */
    public int getSelectionEnd()
    {
        return selectionEnd;
    }

    /** 
     * Mouse has been pressed.
     */
    public void mousePressed(MouseEvent e)
    {
        requestFocus();
        int tmp = getCharPos(e.getX(), e.getY());
        mousePressPos = tmp;
        if (tmp != -1)
        {
            isSelectionCompletion = false;
            if ((e.getModifiers() & MouseEvent.SHIFT_MASK) > 0)
            {
                if (caretPosition == selectionStart && tmp <= selectionEnd) {
                    setCaretPosition(tmp);
                    selectionStart = caretPosition;
                } else if (caretPosition == selectionStart) {
                    selectionStart = selectionEnd;
                    setCaretPosition(tmp);
                    selectionEnd = caretPosition;
                } else if (caretPosition == selectionEnd && tmp >= selectionStart) {
                    setCaretPosition(tmp);
                    selectionEnd = caretPosition;
                } else {
                    selectionEnd = selectionStart;
                    setCaretPosition(tmp);
                    selectionStart = caretPosition;
                }            
            }
            else
            {
                setCaretPosition(tmp);
                if (e.getClickCount()==1) {
                    selectionEnd = caretPosition;
                    selectionStart = caretPosition;
                }
            }
         
            repaint(); 
            positionVisible();
        }
    }

    /** 
     * MouseMoved.
     */
    public void mouseMoved(MouseEvent e)
    {
    }

    /** 
     * MouseDragged.
     */
    public void mouseDragged(MouseEvent e)
    {
        int tmp = getCharPos(e.getX(), e.getY());
        if (mousePressPos != -1) {
            if (mousePressPos <= tmp) {
                selectionStart = mousePressPos;
                selectionEnd = tmp;
            } else {
                selectionEnd = mousePressPos;
                selectionStart = tmp;
            }
            setCaretPosition(tmp);
            repaint(); 
            positionVisible();
        } else {
            repaint();
        }
    }

    /** 
     * MouseReleased.
     */
    public void mouseReleased(MouseEvent e)
    {
        mousePressPos = -1;
        testOposing();
    }

    /** 
     * MouseClicked.
     */
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount()==2) {
            selectWord(caretPosition);
        }
    }

    /** 
     * MouseEntered.
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /** 
     * MouseExited.
     */
    public void mouseExited(MouseEvent e)
    {
    }

    protected int minDisplayedLines = 4;
    /**
     * Gets the minimum number of lines to display
     */
    public int getMinDisplayedLines() {
        return minDisplayedLines;
    }
    public void setMinDisplayedLines(int n) {
        this.minDisplayedLines = n;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        int pos = 0;
        int lineStart = 0;
        int width = 30;
        int height;
        int lines = 1;
        
        while (pos < text.length())
        {
            if (text.charAt(pos)=='\n' || pos==(text.length()-1)) {
                int lineWidth =
                    metrics.stringWidth(
                        Strings.replace(text.substring(lineStart,pos),
                                        "\t",
                                        Strings.newString(' ',conf.getTabWidth())))
                    + metrics.charWidth('m');
                if (lineWidth>width)
                    width = lineWidth;
                lineStart = pos+1;
                lines++;
            }
            pos++;
        }
        if (lines<minDisplayedLines)
            lines = minDisplayedLines;
        height = lines * metrics.getHeight();
        return new Dimension(width,height);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) 
    {
        return metrics.getHeight();
    }

    /************************************************** 
     * Copied from JTextComponent.
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        switch(orientation)
        {
            case SwingConstants.VERTICAL:
                return visibleRect.height;
            case SwingConstants.HORIZONTAL:
                return visibleRect.width;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    public boolean getScrollableTracksViewportWidth()
    {   
        // Cast to Object to work around SwingWT inheritance bugs 
        Object parent = getParent();
        return (parent instanceof JViewport) && 
            (((JViewport)parent).getWidth() > getPreferredSize().width);
    }
    public boolean getScrollableTracksViewportHeight()
    {
        // Cast to Object to work around SwingWT inheritance bugs 
        Object parent = getParent();
        return (parent instanceof JViewport) && 
            (((JViewport)parent).getHeight() > getPreferredSize().height);
    }

    /**********************/   
   
    /** 
     * Fiered if the SHEditor loses ownership of a Clipboard
     */
    public void lostOwnership(Clipboard cb, Transferable tr)
    {
    }

    /**
     * The SHEditor has lost focus.
     */
    public void focusLost(FocusEvent e)
    {
        repaintCharAt(caretPosition);
    }

    /**
     * The SHEditor has gained focus.
     */
    public void focusGained(FocusEvent e)
    {
        repaintCharAt(caretPosition);
    }

    /** 
     * Reads a file into SHEditor.
     * @param f the file to read into the SHEditor.
     */
    public void readFromFile(File f)
    {
        try
        {
            StringBuffer tmp = new StringBuffer();
            if (f.exists())
            {
                BufferedReader in = 
                    new BufferedReader(
                        new InputStreamReader(
                            new BufferedInputStream(
                                new FileInputStream(f))));
            
                String read = in.readLine();
                boolean first = true;
            
                while (read != null)
                {
                    if (!first)
                    {
                        tmp.append("\n" + read);
                    }
                    else 
                    {
                        first = false;
                        tmp.append(read);
                    }
                    read = in.readLine();
                }
                in.close();  
                text = tmp.toString();          
            }         
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
        changed = false;
    }

    /** 
     * Saves the file in SHEditor to a file.
     * @param f the file where the content of the SHEditor should be saved.
     */
    public void saveToFile(File f)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(f));
            out.print(text);
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
        changed = false;
    }

    /** 
     * This class is a text action, it is used to undo redo actions..
     */
    static class TextAction
    {
        /**
         * The action is a remove action.
         */
        public final static int REMOVE = 0;
        /**
         * The action is a insert action.
         */
        public final static int INSERT = 1;
      
        int action;
        int position;
        int length;
        String string;
      
        /** 
         * Constructs a new TextActiont.
         * @param a action type REMOVE or INSERT.
         * @param p the position of the action.
         * @param l the length of the action, only needed by a insert action.
         * @param s the string content of the action, only needed by a remove action.
         */
        TextAction(int a, int p, int l, String s)
        {
            action = a;
            position = p;
            length = l;
            string = s;
        }
    }

    static class MutableCaretEvent extends CaretEvent {
        public MutableCaretEvent(Object source, int caretPosition) {
            super(source);
            this.caretPosition = caretPosition;
        }
        int caretPosition;
        public int getDot() {
            return caretPosition;
        }
        public int getMark() {
            return -1;
        }
    }
}
