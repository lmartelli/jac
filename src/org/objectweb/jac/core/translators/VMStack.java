/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.core.translators;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Stack;

/**
 * Represents the VM stack
 */
public class VMStack extends Stack {
    static Logger logger = Logger.getLogger("translator.bytecode");

    public static final ThisPointer thisPointer = new ThisPointer();
    public static final DontCare dontCare = new DontCare();

    static final int UNPREDICTABLE = -1;
    static final int UNDEFINED = -2;

    Vector locals = new Vector(); // Local variables

    ConstantPoolGen cp;
    int[] exceptionHandlers;
    int currentHandler = 0;
    int nbArgs;
    int localsOffest;
    boolean isStatic;
    public VMStack(ConstantPoolGen cp, Code code, int nbArgs, boolean isStatic) {
        this.cp = cp;
        this.nbArgs = nbArgs;
        this.isStatic = isStatic;
        this.localsOffest = nbArgs+(isStatic?1:0);
        if (code!=null) {
            CodeException[] exceptions = code.getExceptionTable();
            exceptionHandlers = new int[exceptions.length];
            for (int i=0; i<exceptions.length; i++) {
                exceptionHandlers[i] = exceptions[i].getHandlerPC();
                logger.debug("Exception handler at "+exceptionHandlers[i]);
            }
            Arrays.sort(exceptionHandlers);
        }
    }

    public void preExecute(InstructionHandle ih) {
        if (currentHandler<exceptionHandlers.length)
            logger.debug("position="+ih.getPosition()+", nextHandler="+exceptionHandlers[currentHandler]);
        if ( currentHandler<exceptionHandlers.length &&
             ih.getPosition()==exceptionHandlers[currentHandler] ) {
            logger.debug("entering exception handler");
            currentHandler++;
            push(dontCare);
        }
    }

    static Set primitiveWrapperTypes = new HashSet();
    static {
        primitiveWrapperTypes.add("java.lang.Float");
        primitiveWrapperTypes.add("java.lang.Double");
        primitiveWrapperTypes.add("java.lang.Integer");
        primitiveWrapperTypes.add("java.lang.Long");
        primitiveWrapperTypes.add("java.lang.Boolean");
        primitiveWrapperTypes.add("java.lang.Character");
        primitiveWrapperTypes.add("java.lang.Byte");
    }

    public void execute(Instruction i, InstructionHandle ih) {
        logger.debug("execute "+i.toString(cp.getConstantPool()));

        if (i instanceof SWAP) {
            swap();
        } else if (i instanceof DUP || i instanceof DUP2) {
            push(peek());
        } else if (i instanceof DUP_X1 || i instanceof DUP2_X2 || 
                   i instanceof DUP2_X1 || i instanceof DUP_X2) {
            swap();
            push(peek(1));
        } else if (i instanceof ALOAD && ((ALOAD)i).getIndex() == 0
                   && !isStatic) {
            push(thisPointer);
        } else if (i instanceof NEW) {         
            if (primitiveWrapperTypes.contains(
                ((NEW)i).getLoadClassType(cp).getClassName())) {
                push(new PrimitiveValue(((NEW)i).getLoadClassType(cp).getClassName()));
            } else {
                push(new Instance(
                    ((NEW)i).getLoadClassType(cp).getClassName(),ih));
            }
        } else if (i instanceof LoadInstruction) {
            LoadInstruction load = (LoadInstruction)i;
            if (load.getIndex()<=nbArgs) {
                push(new Argument(load.getIndex()));
            } else {
                int index = load.getIndex()-nbArgs;
                // We must ensure the size of locals is correct
                // because a LOAD may occur before a STORE in the
                // bytecode (because of a JMP)
                if (locals.size()<index+1)
                    locals.setSize(index+1);
                push(locals.get(index));
            }
        } else if (i instanceof StoreInstruction) {
            StoreInstruction store = (StoreInstruction)i;
            if (store.getIndex()<=nbArgs) {
            } else {
                int index = store.getIndex()-nbArgs;
                if (locals.size()<store.getIndex()+1)
                    locals.setSize(store.getIndex()+1);
                locals.set(store.getIndex(),peek());
            }
            consume(i);
        } else if (i instanceof GETFIELD) {
            Object substance = peek();
            consume(i);
            push(new FieldValue(substance,
                                ((GETFIELD)i).getIndex(),
                                ((GETFIELD)i).getFieldName(cp)));
        } else if (i instanceof GETSTATIC) {
            consume(i);
            push(new FieldValue(null,
                                ((GETSTATIC)i).getIndex(),
                                ((GETSTATIC)i).getFieldName(cp)));
        } else if (i instanceof INVOKESPECIAL && 
                   ((INVOKESPECIAL)i).getMethodName(cp).equals("<init>")) {
            Object invoked = invokedObject((INVOKESPECIAL)i);
            if (invoked instanceof PrimitiveValue) 
                ((PrimitiveValue)invoked).wrappedValue = peek();
            consume(i);
            produce(i);
            /*
        } else if (i instanceof InvokeInstruction) {
            InvokeInstruction invoke = (InvokeInstruction)i;
            if (invoke.getMethodName(cp).equals("iterator")) {
                consume(i);
                push(new IteratorValue(peek()));
            } else if (invoke.getMethodName(cp).equals("next") &&
                       peek() instanceof IteratorValue) {
                consume(i);
                push(new CollectionValue(((IteratorValue)peek()).collection));
            } else {
                consume(i);
                produce(i);
            }
            */
        } else if (i instanceof CHECKCAST || i instanceof ATHROW) {
            // nothing
        } else if (i instanceof ANEWARRAY) {
            // BCEL bug workaround (ANEWARRAY is a StackProducer but not
            // a StackConsumer)
            pop(1);
            push(dontCare);
        } else {
            consume(i);
            produce(i);
        }
        logger.debug("stack: "+this);
    }

    /**
     * Returns the stack element corresponding to the object on which
     * the InvokeInstruction is applied.
     */
    public Object invokedObject(InvokeInstruction i) {
        return peek(i.getArgumentTypes(cp).length);
    }

    /**
     * Consume values from the stack for the given instruction
     */
    public void consume(Instruction i) {
        if (i instanceof StackConsumer)
            pop(getConsumed(i,cp));
    }
    public void produce(Instruction i) {
        if (i instanceof StackProducer) {
            for(int j=0; j<getProduced(i,cp); j++)
                push(dontCare);
        }
    }
    /**
     * Returns the number of elements from the stack consumed by an
     * instruction 
     */
    public static int getConsumed(Instruction i, ConstantPoolGen cp) {
        if (i instanceof INVOKESTATIC) 
            return ((InvokeInstruction)i).getArgumentTypes(cp).length;
        else if (i instanceof InvokeInstruction) 
            return ((InvokeInstruction)i).getArgumentTypes(cp).length+1;
        else if (i instanceof MULTIANEWARRAY) 
            return ((MULTIANEWARRAY)i).getDimensions();
        else
            return CONSUME_STACK[i.getOpcode()];
    }

    /**
     * Returns the number of elements produced on the stack by an instruction 
     */
    public static int getProduced(Instruction i, ConstantPoolGen cp) {
        if (i instanceof InvokeInstruction) {
            return ((InvokeInstruction)i).getReturnType(cp)==Type.VOID ? 0 : 1;
        } else {
            return PRODUCE_STACK[i.getOpcode()];
        }
    }

    /**
     * Gets the value on which a method is invoked
     * @param invoke the invoke instruction
     */
    public Object getSubstance(InvokeInstruction invoke) {
        return peek(invoke.getArgumentTypes(cp).length);
    }

    static final int[] CONSUME_STACK = {
        0/*nop*/, 0/*aconst_null*/, 0/*iconst_m1*/, 0/*iconst_0*/, 0/*iconst_1*/,
        0/*iconst_2*/, 0/*iconst_3*/, 0/*iconst_4*/, 0/*iconst_5*/, 0/*lconst_0*/,
        0/*lconst_1*/, 0/*fconst_0*/, 0/*fconst_1*/, 0/*fconst_2*/, 0/*dconst_0*/,
        0/*dconst_1*/, 0/*bipush*/, 0/*sipush*/, 0/*ldc*/, 0/*ldc_w*/, 0/*ldc2_w*/, 0/*iload*/,
        0/*lload*/, 0/*fload*/, 0/*dload*/, 0/*aload*/, 0/*iload_0*/, 0/*iload_1*/, 0/*iload_2*/,
        0/*iload_3*/, 0/*lload_0*/, 0/*lload_1*/, 0/*lload_2*/, 0/*lload_3*/, 0/*fload_0*/,
        0/*fload_1*/, 0/*fload_2*/, 0/*fload_3*/, 0/*dload_0*/, 0/*dload_1*/, 0/*dload_2*/,
        0/*dload_3*/, 0/*aload_0*/, 0/*aload_1*/, 0/*aload_2*/, 0/*aload_3*/, 2/*iaload*/,
        2/*laload*/, 2/*faload*/, 2/*daload*/, 2/*aaload*/, 2/*baload*/, 2/*caload*/, 2/*saload*/,
        1/*istore*/, 1/*lstore*/, 1/*fstore*/, 1/*dstore*/, 1/*astore*/, 1/*istore_0*/,
        1/*istore_1*/, 1/*istore_2*/, 1/*istore_3*/, 1/*lstore_0*/, 1/*lstore_1*/,
        1/*lstore_2*/, 1/*lstore_3*/, 1/*fstore_0*/, 1/*fstore_1*/, 1/*fstore_2*/,
        1/*fstore_3*/, 1/*dstore_0*/, 1/*dstore_1*/, 1/*dstore_2*/, 1/*dstore_3*/,
        1/*astore_0*/, 1/*astore_1*/, 1/*astore_2*/, 1/*astore_3*/, 3/*iastore*/, 3/*lastore*/,
        3/*fastore*/, 3/*dastore*/, 3/*aastore*/, 3/*bastore*/, 3/*castore*/, 3/*sastore*/,
        1/*pop*/, 1/*pop2*/, 1/*dup*/, 2/*dup_x1*/, 3/*dup_x2*/, 1/*dup2*/, 3/*dup2_x1*/,
        4/*dup2_x2*/, 2/*swap*/, 2/*iadd*/, 2/*ladd*/, 2/*fadd*/, 2/*dadd*/, 2/*isub*/, 2/*lsub*/,
        2/*fsub*/, 2/*dsub*/, 2/*imul*/, 2/*lmul*/, 2/*fmul*/, 2/*dmul*/, 2/*idiv*/, 2/*ldiv*/,
        2/*fdiv*/, 2/*ddiv*/, 2/*irem*/, 2/*lrem*/, 2/*frem*/, 2/*drem*/, 1/*ineg*/, 1/*lneg*/,
        1/*fneg*/, 1/*dneg*/, 2/*ishl*/, 2/*lshl*/, 2/*ishr*/, 2/*lshr*/, 2/*iushr*/, 2/*lushr*/,
        2/*iand*/, 2/*land*/, 2/*ior*/, 2/*lor*/, 2/*ixor*/, 2/*lxor*/, 0/*iinc*/,
        1/*i2l*/, 1/*i2f*/, 1/*i2d*/, 1/*l2i*/, 1/*l2f*/, 1/*l2d*/, 1/*f2i*/, 1/*f2l*/,
        1/*f2d*/, 1/*d2i*/, 1/*d2l*/, 1/*d2f*/, 1/*i2b*/, 1/*i2c*/, 1/*i2s*/, 
        2/*lcmp*/, 2/*fcmpl*/, 2/*fcmpg*/, 2/*dcmpl*/, 2/*dcmpg*/, 1/*ifeq*/, 1/*ifne*/,
        1/*iflt*/, 1/*ifge*/, 1/*ifgt*/, 1/*ifle*/, 2/*if_icmpeq*/, 2/*if_icmpne*/, 2/*if_icmplt*/,
        2 /*if_icmpge*/, 2/*if_icmpgt*/, 2/*if_icmple*/, 2/*if_acmpeq*/, 2/*if_acmpne*/,
        0/*goto*/, 0/*jsr*/, 0/*ret*/, 1/*tableswitch*/, 1/*lookupswitch*/, 1/*ireturn*/,
        1/*lreturn*/, 1/*freturn*/, 1/*dreturn*/, 1/*areturn*/, 0/*return*/, 0/*getstatic*/,
        1,/*putstatic*/ 1/*getfield*/, 2/*putfield*/,
        UNPREDICTABLE/*invokevirtual*/, UNPREDICTABLE/*invokespecial*/,
        UNPREDICTABLE/*invokestatic*/,
        UNPREDICTABLE/*invokeinterface*/, UNDEFINED, 0/*new*/, 1/*newarray*/, 1/*anewarray*/,
        1/*arraylength*/, 1/*athrow*/, 1/*checkcast*/, 1/*instanceof*/, 1/*monitorenter*/,
        1/*monitorexit*/, 0/*wide*/, UNPREDICTABLE/*multianewarray*/, 1/*ifnull*/, 1/*ifnonnull*/,
        0/*goto_w*/, 0/*jsr_w*/, 0/*breakpoint*/, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNPREDICTABLE/*impdep1*/, UNPREDICTABLE/*impdep2*/
    };

    public static final int[] PRODUCE_STACK = {
        0/*nop*/, 1/*aconst_null*/, 1/*iconst_m1*/, 1/*iconst_0*/, 1/*iconst_1*/,
        1/*iconst_2*/, 1/*iconst_3*/, 1/*iconst_4*/, 1/*iconst_5*/, 1/*lconst_0*/,
        1/*lconst_1*/, 1/*fconst_0*/, 1/*fconst_1*/, 1/*fconst_2*/, 1/*dconst_0*/,
        1/*dconst_1*/, 1/*bipush*/, 1/*sipush*/, 1/*ldc*/, 1/*ldc_w*/, 1/*ldc2_w*/, 1/*iload*/,
        1/*lload*/, 1/*fload*/, 1/*dload*/, 1/*aload*/, 1/*iload_0*/, 1/*iload_1*/, 1/*iload_2*/,
        1/*iload_3*/, 1/*lload_0*/, 1/*lload_1*/, 1/*lload_2*/, 1/*lload_3*/, 1/*fload_0*/,
        1/*fload_1*/, 1/*fload_2*/, 1/*fload_3*/, 1/*dload_0*/, 1/*dload_1*/, 1/*dload_2*/,
        1/*dload_3*/, 1/*aload_0*/, 1/*aload_1*/, 1/*aload_2*/, 1/*aload_3*/, 1/*iaload*/,
        1/*laload*/, 1/*faload*/, 1/*daload*/, 1/*aaload*/, 1/*baload*/, 1/*caload*/, 1/*saload*/,
        0/*istore*/, 0/*lstore*/, 0/*fstore*/, 0/*dstore*/, 0/*astore*/, 0/*istore_0*/,
        0/*istore_1*/, 0/*istore_2*/, 0/*istore_3*/, 0/*lstore_0*/, 0/*lstore_1*/,
        0/*lstore_2*/, 0/*lstore_3*/, 0/*fstore_0*/, 0/*fstore_1*/, 0/*fstore_2*/,
        0/*fstore_3*/, 0/*dstore_0*/, 0/*dstore_1*/, 0/*dstore_2*/, 0/*dstore_3*/,
        0/*astore_0*/, 0/*astore_1*/, 0/*astore_2*/, 0/*astore_3*/, 0/*iastore*/, 0/*lastore*/,
        0/*fastore*/, 0/*dastore*/, 0/*aastore*/, 0/*bastore*/, 0/*castore*/, 0/*sastore*/,
        0/*pop*/, 0/*pop2*/, 2/*dup*/, 3/*dup_x1*/, 4/*dup_x2*/, 4/*dup2*/, 5/*dup2_x1*/,
        6/*dup2_x2*/, 2/*swap*/, 1/*iadd*/, 1/*ladd*/, 1/*fadd*/, 1/*dadd*/, 1/*isub*/, 1/*lsub*/,
        1/*fsub*/, 1/*dsub*/, 1/*imul*/, 1/*lmul*/, 1/*fmul*/, 1/*dmul*/, 1/*idiv*/, 1/*ldiv*/,
        1/*fdiv*/, 1/*ddiv*/, 1/*irem*/, 1/*lrem*/, 1/*frem*/, 1/*drem*/, 1/*ineg*/, 1/*lneg*/,
        1/*fneg*/, 1/*dneg*/, 1/*ishl*/, 1/*lshl*/, 1/*ishr*/, 1/*lshr*/, 1/*iushr*/, 1/*lushr*/,
        1/*iand*/, 1/*land*/, 1/*ior*/, 1/*lor*/, 1/*ixor*/, 1/*lxor*/,
        0/*iinc*/, 1/*i2l*/, 1/*i2f*/, 1/*i2d*/, 1/*l2i*/, 1/*l2f*/, 1/*l2d*/, 1/*f2i*/,
        1/*f2l*/, 1/*f2d*/, 1/*d2i*/, 1/*d2l*/, 1/*d2f*/,
        1/*i2b*/, 1/*i2c*/, 1/*i2s*/, 1/*lcmp*/, 1/*fcmpl*/, 1/*fcmpg*/,
        1/*dcmpl*/, 1/*dcmpg*/, 0/*ifeq*/, 0/*ifne*/, 0/*iflt*/, 0/*ifge*/, 0/*ifgt*/, 0/*ifle*/,
        0/*if_icmpeq*/, 0/*if_icmpne*/, 0/*if_icmplt*/, 0/*if_icmpge*/, 0/*if_icmpgt*/,
        0/*if_icmple*/, 0/*if_acmpeq*/, 0/*if_acmpne*/, 0/*goto*/, 1/*jsr*/, 0/*ret*/,
        0/*tableswitch*/, 0/*lookupswitch*/, 0/*ireturn*/, 0/*lreturn*/, 0/*freturn*/,
        0/*dreturn*/, 0/*areturn*/, 0/*return*/, 1/*getstatic*/, 0/*putstatic*/,
        1/*getfield*/, 0/*putfield*/, UNPREDICTABLE/*invokevirtual*/,
        UNPREDICTABLE/*invokespecial*/, UNPREDICTABLE/*invokestatic*/,
        UNPREDICTABLE/*invokeinterface*/, UNDEFINED, 1/*new*/, 1/*newarray*/, 1/*anewarray*/,
        1/*arraylength*/, 1/*athrow*/, 1/*checkcast*/, 1/*instanceof*/, 0/*monitorenter*/,
        0/*monitorexit*/, 0/*wide*/, 1/*multianewarray*/, 0/*ifnull*/, 0/*ifnonnull*/,
        0/*goto_w*/, 1/*jsr_w*/, 0/*breakpoint*/, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
        UNDEFINED, UNPREDICTABLE/*impdep1*/, UNPREDICTABLE/*impdep2*/
    };

    // various classes to represent values on the stack
    public static class ThisPointer implements Serializable { 
        public String toString() { return "This"; } 
    }

    /** Unknown value */
    public static class DontCare implements Serializable { 
        public String toString() { return "???"; } 
    }

    /** Instance of a class */
    public static class Instance implements Serializable { 
        public String type;
        /** the InstructionHandle who created this instance */
        public transient InstructionHandle newHandle;
        public transient InstructionHandle initHandle;
        public Instance(String type, InstructionHandle newHandle) { 
            this.type = type; 
            this.newHandle = newHandle;
        }
        public String toString() { return type; }
    }

    /** An argument */
    public static class Argument implements Serializable {
        public int n;
        public Argument(int n) { this.n = n; }
        public String toString() { return "arg["+n+"]"; }
    }

    /** The value of a field */
    public static class FieldValue implements Serializable {
        int index; // const pool FieldRef index
        String field; // name of field
        Object substance; // owner of the field
        public FieldValue(Object substance, int index, String field) { 
            this.field = field; 
            this.index = index;
            this.substance = substance; 
        }
        public String toString() { return substance+"."+field; }
    }

    /** A primitive value */
    public static class PrimitiveValue implements Serializable {
        public Object wrappedValue;
        public String type;
        public PrimitiveValue(String type) {
            this.type = type;
            this.wrappedValue = null;
        }
        public String toString() {
            return type+"("+wrappedValue+")";
        }
    }

    /** An iterator on a collection */
    public static class IteratorValue {
        public Object collection;
        public IteratorValue(Object collection) {
            this.collection = collection;
        }
        public String toString() {
            return "iterator("+collection.toString()+")";
        }
    }

    /** An item of a collection */
    public static class CollectionValue {
        public Object collection;
        public CollectionValue(Object collection) {
            this.collection = collection;
        }
        public String toString() {
            return "iterator("+collection.toString()+")";
        }
    }
}

