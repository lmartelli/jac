/*
  Copyright (C) 2002-2003 Fabrice Legond-Aubry, Renaud Pawlak, 
  Lionel Seinturier, Laurent Martelli

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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.JacLoader;
import org.objectweb.jac.core.JacPropLoader;
import org.objectweb.jac.core.WrappeeTranslator;
import org.objectweb.jac.core.rtti.InvokeInfo;
import org.objectweb.jac.core.rtti.LoadtimeRTTI;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Stack;

public  class WrappeeTranslator_BCEL implements WrappeeTranslator {
    static Logger logger = Logger.getLogger("translator");
    static Logger loggerRtti = Logger.getLogger("rtti.detect");
    static Logger loggerBytecode = Logger.getLogger("translator.bytecode");

    LoadtimeRTTI rtti;

    /**
     * Translator initializator. */

    public WrappeeTranslator_BCEL(LoadtimeRTTI rtti) {
        this.rtti = rtti;
    }

    private static final String nextWrapper_signature = 
    "(Lorg/objectweb/jac/core/Interaction;)Ljava/lang/Object;";

    private static final String newInteraction_signature = 
    "(Lorg/objectweb/jac/core/WrappingChain;Lorg/objectweb/jac/core/Wrappee;"+
    "Lorg/objectweb/jac/core/rtti/AbstractMethodItem;"+
    "[Ljava/lang/Object;)V";

    private static final String getWrappingChain_signature = 
    "(Lorg/objectweb/jac/core/Wrappee;Lorg/objectweb/jac/core/rtti/AbstractMethodItem;)Lorg/objectweb/jac/core/WrappingChain;";

    private String primitiveTypeName(Type t)
    {
        if (t==Type.BOOLEAN)
            return "boolean";
        if (t==Type.BYTE)
            return "byte";
        if (t==Type.INT)
            return "int";
        if (t==Type.LONG)
            return "long";
        if (t==Type.SHORT)
            return "short";
        if (t==Type.FLOAT)
            return "float";
        if (t==Type.CHAR)
            return "char";
        if (t==Type.DOUBLE)
            return "double";
        return null;
    }

    private String primitiveTypeAsObject(Type t)
    {
        if (t==Type.BOOLEAN)
            return "java.lang.Boolean";
        if (t==Type.BYTE)
            return "java.lang.Byte";
        if (t==Type.INT)
            return "java.lang.Integer";
        if (t==Type.LONG)
            return "java.lang.Long";
        if (t==Type.SHORT)
            return "java.lang.Short";
        if (t==Type.FLOAT)
            return "java.lang.Float";
        if (t==Type.CHAR)
            return "java.lang.Character";
        if (t==Type.DOUBLE)
            return "java.lang.Double";
        return null;
    }
   
    private void generateStubMethod(ClassGen classGen, 
                                    ConstantPoolGen constPool,
                                    Method method, String gSNewName, 
                                    int staticFieldIndex,
                                    List staticFieldIndexes,
                                    int wrappingChainIndex,
                                    List wrappingChainIndexes, 
                                    InstructionList callSuper)
    {
        logger.debug("Generating stub method "+
                     method.getName()+method.getSignature());
        InstructionList il = new InstructionList();
        InstructionFactory ifactory = new InstructionFactory(classGen);
        //create the stub method

        Type[] argumentTypes = Type.getArgumentTypes(method.getSignature());
        Type returnType = Type.getReturnType(method.getSignature());
        MethodGen stubMethod = 
            new MethodGen(
                method.getAccessFlags(),
                returnType,argumentTypes,
                null,
                method.getName(),classGen.getClassName(), 
                il,constPool);
        int lineNumber = 0;
        // generate the super call when its a constructor
        if (stubMethod.getName().equals("<init>")) {
            logger.debug("Generating stub method "+
                         classGen.getClassName()+"."+
                         method.getName()+method.getSignature());
            if (callSuper!=null) {
                logger.debug("   insert call to super");
                il.append(callSuper);
            } else {
                logger.debug("   insert super()");
                il.append(new ALOAD(0));
                il.append(
                    ifactory.createInvoke(classGen.getSuperclassName(), "<init>", 
                                          Type.VOID, 
                                          emptyTypeArray, 
                                          Constants.INVOKESPECIAL));
            }
            // initialize the wrapping chains
            for(int i=0;i<wrappingChainIndexes.size();i++) {
                if(wrappingChainIndexes.get(i)==null) continue;
                il.append(InstructionFactory.createThis());
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                il.append(InstructionFactory.createThis());
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                il.append(new GETSTATIC(
                    ((Integer)staticFieldIndexes.get(i)).intValue()));
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                il.append(ifactory.createInvoke(
                    "org.objectweb.jac.core.Wrapping", "getWrappingChain", 
                    Type.getReturnType(getWrappingChain_signature), 
                    Type.getArgumentTypes(getWrappingChain_signature), 
                    Constants.INVOKESTATIC));
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                WCIndex chainIndex = (WCIndex)wrappingChainIndexes.get(i);
                if (chainIndex.isStatic) {
                    il.append(new PUTSTATIC(chainIndex.index));
                } else {
                    il.append(new PUTFIELD(chainIndex.index));
                }
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
            }
        }      

        ///////////////////////////////////
        // LOOK AT THIS ! NEED TEST
        ///////////////////////////////////////
        stubMethod.removeExceptionHandlers();

        // create an Interaction object
        il.append(ifactory.createNew("org.objectweb.jac.core.Interaction"));
        stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        il.append(new DUP());
        stubMethod.addLineNumber(il.getEnd(),il.size()-1);

        // get the wrapping chain
        if (method.isStatic()) {
            il.append(new GETSTATIC(wrappingChainIndex));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        } else {
            il.append(InstructionFactory.createThis());
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
            il.append(new GETFIELD(wrappingChainIndex));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        }
      
        // push the ref on this object in case of a non-static method on stack 
        if( !stubMethod.isStatic() ) {
            il.append(InstructionFactory.createThis());
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        } else {
            il.append(new ACONST_NULL());
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        }

        // push the static field containing the AbstractMethodItem on the stack
        il.append(new GETSTATIC(staticFieldIndex));
        stubMethod.addLineNumber(il.getEnd(),il.size()-1);

        //create an array of Objects that are the parameters for the
        //original method
        if (argumentTypes.length==0) {
            il.append(
                ifactory.createGetStatic(
                    "org.objectweb.jac.core.Wrapping",
                    "emptyArray",
                    Type.getType("[Ljava.lang.Object;")));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        } else {
            il.append(new PUSH(constPool, argumentTypes.length));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
            //create a array and put its ref on the stack
            il.append((Instruction)ifactory.createNewArray(Type.OBJECT,(short)1));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
            int j = (stubMethod.isStatic())?0:1; // index of argument on the stack
            for (int i=0; i<argumentTypes.length; i++)
            {
                //duplicate the ref on the array to keep it for next operation
                il.append(InstructionFactory.createDup(1));
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                //get the index [in the array] in which we will store the ref 
                il.append(new PUSH(constPool, i));
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                // is the parameter an object ?
                if (!Utils.isPrimitive(argumentTypes[i])) {
                    //get the ref of the j st parameter of the local function
                    il.append(InstructionFactory.createLoad(Type.OBJECT,j));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    //effectively store the ref in the array
                    il.append(InstructionFactory.createArrayStore(Type.OBJECT));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    //increment the counter for the next object
                    j++;
                } else {
                    //create a new object similar to the primitive type
                    String objectType = primitiveTypeAsObject(argumentTypes[i]);
                    il.append (ifactory.createNew(objectType));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    //call the constructor of the new object with the primitive value
                    il.append(InstructionFactory.createDup(1));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    il.append(InstructionFactory.createLoad(argumentTypes[i],j));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    il.append(ifactory.createInvoke(objectType, "<init>", Type.VOID, 
                                                    new Type[] {argumentTypes[i]}, 
                                                    Constants.INVOKESPECIAL));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    //store the new object in the array
                    il.append(InstructionFactory.createArrayStore(Type.OBJECT));
                    stubMethod.addLineNumber(il.getEnd(),il.size()-1);
                    j++;
                    if (argumentTypes[i]==Type.LONG ||
                        argumentTypes[i]==Type.DOUBLE)
                        // long and double take 2 slots on the stack
                        j++;
                }
            
            }
        }

        // <init> the Interaction
        il.append(
            ifactory.createInvoke(
                "org.objectweb.jac.core.Interaction", "<init>", 
                Type.getReturnType(newInteraction_signature), 
                Type.getArgumentTypes(newInteraction_signature), 
                Constants.INVOKESPECIAL));      
        stubMethod.addLineNumber(il.getEnd(),il.size()-1);

        //make invocation of "nextWrapper"
        if (method.isStatic()||method.getName().equals("<init>")) {
            il.append(
                ifactory.createInvoke(
                    "org.objectweb.jac.core.Wrapping", "nextWrapper", 
                    Type.getReturnType(nextWrapper_signature), 
                    Type.getArgumentTypes(nextWrapper_signature), 
                    Constants.INVOKESTATIC));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        } else {
            il.append(
                ifactory.createInvoke(
                    "org.objectweb.jac.core.Wrapping", "methodNextWrapper", 
                    Type.getReturnType(nextWrapper_signature), 
                    Type.getArgumentTypes(nextWrapper_signature), 
                    Constants.INVOKESTATIC));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        }

        //parse the return value if it is a primitive one
        if (Utils.isPrimitive(returnType)) 
        {
            //ouch ! the return primitive types are also wrapped.
            //test the cast for the object returned ....
            //is this really usefull ?
				
            il.append(
                ifactory.createCheckCast(
                    (ReferenceType)Type.getReturnType(
                        "()L"+primitiveTypeAsObject(returnType).replace('.','/')+";")));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
				
            //get the value wrapped in the object
            il.append(
                ifactory.createInvoke(
                    primitiveTypeAsObject(returnType), 
                    primitiveTypeName(returnType)+"Value",
                    stubMethod.getReturnType(), emptyTypeArray,
                    Constants.INVOKEVIRTUAL));
            stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        }
        // else make a simple checkcast (avoid the checkcast on the VOID type)
        //is this also really useful ?
        else 
            if (stubMethod.getReturnType()!=Type.VOID) {
                il.append (
                    ifactory.createCheckCast((ReferenceType)returnType));
                stubMethod.addLineNumber(il.getEnd(),il.size()-1);
            }
        // finally return ! HOURRA !
        il.append (InstructionFactory.createReturn(returnType));
        stubMethod.addLineNumber(il.getEnd(),il.size()-1);
        // compile all this stuff, generate the method
        stubMethod.setMaxLocals();
        stubMethod.setMaxStack();
        //System.out.println ("il=\n"+il);
        classGen.addMethod(stubMethod.getMethod());
    }

    /**
     * Generate a default constructor --which takes no argument-- which
     * just calls super().
     * 
     * !!! IT SHOULD ALSO INITIALIZE FIELDS !!!
     * We could use the "this" method generated by jikes-1.18
     */
    private void generateDefaultConstructor(ClassGen classGen)
    {
        logger.debug("class "+classGen.getClassName()+
                  " ==> Generating default constructor");
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionList instructions = new InstructionList();
        InstructionFactory ifactory = new InstructionFactory(classGen);

        instructions.append(new ALOAD(0));
        instructions.append(ifactory.createInvoke(classGen.getSuperclassName(), 
                                                  "<init>", 
                                                  Type.VOID, 
                                                  emptyTypeArray, 
                                                  Constants.INVOKESPECIAL));
      
        instructions.append(new RETURN());

        MethodGen constructor = new MethodGen(Modifier.PUBLIC, 
                                              Type.VOID,
                                              new Type[] {}, new String[] {},
                                              "<init>", classGen.getClassName(), 
                                              instructions, constPool);

        constructor.setMaxLocals();
        constructor.setMaxStack();
        classGen.addMethod(constructor.getMethod());
    }

    /**
     * Remove call to super()
     * Replace collection attributes with the wrappable org.objectweb.jac.lib version
     *
     * @param classGen the class of the constructor
     * @param origConstructor 
     * @param constructor the constructor to translate
     * @param callSuper store bytecodes that do the call to the super
     * constructor in it 
     * @param removeSuperCall wether to remove the call to super bytecodes
     */
    private void translateConstructor(ClassGen classGen, 
                                      Method origConstructor,
                                      MethodGen constructor,
                                      InstructionList callSuper,
                                      boolean removeSuperCall) {
        logger.debug("constructor translation of "+
                     constructor.getName()+constructor.getSignature());
        InstructionList instructions = constructor.getInstructionList();
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionFactory ifactory = new InstructionFactory(classGen);
        instructions.setPositions();
        int i=0;
        // Represents the state of the JVM stack 
        // (contains ThisPointer or DontCare)
        boolean superRemoved = false;
        VMStack stack = new VMStack(constPool,origConstructor.getCode(),
                                    constructor.getArgumentTypes().length,false);
        InstructionHandle first = null;
        Iterator it = instructions.iterator();
        while(it.hasNext()) {
            InstructionHandle ih = (InstructionHandle)it.next();
            InstructionHandle next = ih.getNext();
            if (first==null)
                first = ih;
            Instruction instruction = ih.getInstruction();
            stack.preExecute(ih);
            if (!superRemoved && !constructor.getName().equals("this")) {
                if (callSuper!=null) {
                    if (instruction instanceof BranchInstruction) {
                        callSuper.append((BranchInstruction)instruction);
                    } else {
                        callSuper.append(instruction);
                    }
                }
                // remove call to super <init>
                if (instruction instanceof INVOKESPECIAL && 
                    stack.getSubstance((INVOKESPECIAL)instruction)==VMStack.thisPointer) 
                {
                    superRemoved = true;
                    INVOKESPECIAL invoke = (INVOKESPECIAL)instruction;
                    if (removeSuperCall) {
                        if (!invoke.getClassName(constPool).equals(classGen.getClassName()))
                        {
                            try {
                                logger.debug("deleting call to super, callSuper = \n"+callSuper);
                                instructions.delete(first,ih);
                            } catch (TargetLostException e) {
                                logger.debug("TargetLostException..., callSuper = \n"+callSuper);
                                InstructionHandle[] targets = e.getTargets();
                                for(int j=0; j<targets.length; j++) {
                                    InstructionTargeter[] targeters = 
                                        targets[j].getTargeters();
                                    for(int k=0; k<targeters.length; k++)
                                        targeters[k].updateTarget(targets[j], next);
                                }
                            }
                        } else {
                            //if(classGen.containsMethod(
                            //   invoke.getMethodName(constPool),
                            //   invoke.getSignature(constPool)).isPublic())
                            //{
                            // if it calls another constructor of the same class,
                            // replace it with a call to the renamed constructor
                            ih.setInstruction(
                                ifactory.createInvoke(
                                    classGen.getClassName(),
                                    prefix+classGen.getClassName()
                                    .substring(classGen.getClassName().lastIndexOf(".")+1),
                                    Type.VOID,
                                    invoke.getArgumentTypes(constPool),
                                    Constants.INVOKEVIRTUAL
                                )
                            );
                            //}
                        }
                    }
                }
                stack.execute(instruction,ih);
            } else if (JacPropLoader.translateFields(constructor.getClassName())) {

                // Replace java.util. with org.objectweb.jac.lib.java.util
                // for collections

                if (instruction instanceof PUTFIELD &&
                    stack.peek(1) == VMStack.thisPointer &&
                    stack.peek() instanceof VMStack.Instance &&
                    isCollection(((VMStack.Instance)stack.peek()).type) ) {
                    // this.putfield(...)
                    PUTFIELD putfield = (PUTFIELD)instruction;
                    if (!classGen.containsField(putfield.getFieldName(constPool)).isTransient()) {
                        VMStack.Instance collection = (VMStack.Instance)stack.peek();
                        /*
                          System.out.println("collection: "+collection.type+"( new="+
                          collection.newHandle+",init="+
                          collection.initHandle+")");
                        */
                        collection.newHandle.setInstruction(
                            ifactory.createNew("org.objectweb.jac.lib."+collection.type));
                        collection.initHandle.setInstruction(
                            ifactory.createInvoke(
                                "org.objectweb.jac.lib."+collection.type,
                                "<init>",
                                Type.VOID,
                                ((INVOKESPECIAL)collection.initHandle.getInstruction()).getArgumentTypes(constPool),
                                Constants.INVOKESPECIAL));
                        logger.debug("Found collection field initialization: "+
                                  putfield.getFieldName(constPool));
                    }
                } else if (instruction instanceof INVOKESPECIAL) {
                    INVOKESPECIAL invoke = (INVOKESPECIAL)instruction;
                    Object substance = 
                        stack.peek(VMStack.getConsumed(invoke,constPool)-1);
                    if (substance instanceof VMStack.Instance) {
                        logger.debug("Found collection <init> for "+substance);
                        ((VMStack.Instance)substance).initHandle = ih;
                    }
                } 
                stack.execute(instruction,ih);
            }
            i++;
        }
        logger.debug("callSuper = \n"+callSuper);
        instructions.setPositions();
        constructor.setInstructionList(instructions);
        constructor.removeLineNumbers();
    }

    /**
     * Rename the method "xxx" in "_org_xxx". 
     *
     * <p>If the method is a constructor, it's translated.</p>
     *
     * @param classGen the class of the method to rename
     * @param constPool 
     * @param method the method to rename
     * @param newProposedName new name for the translated method
     * @param callSuper store bytecodes that do the call to the super
     * constructor in this list
     *
     * @see #translateConstructor(ClassGen,Method,MethodGen,InstructionList)
     */
    private String renameMethod(ClassGen classGen, ConstantPoolGen constPool,
                                Method method, String newProposedName,
                                InstructionList callSuper)
    {
        logger.debug("Rename "+method.getName()+" -> "+newProposedName);
        // handles constructors specific translations
        if (method.getName().equals("<init>")) {
            MethodGen newNamedMethod = 
                new MethodGen(method, classGen.getClassName(), constPool);
            newNamedMethod.setName(newProposedName);
            translateConstructor(classGen,method,newNamedMethod,callSuper,true);
            //why the hell is all this stuff necessary ?!
            newNamedMethod.removeLocalVariables();
            //      newNamedMethod.setMaxLocals();
            //      newNamedMethod.setMaxStack();
            //move the original method into the new "_org_xxx" method
            classGen.replaceMethod(method, newNamedMethod.getMethod());
        } else {
            Method newMethod = new Method(method);
            newMethod.setNameIndex(constPool.addUtf8(newProposedName));
            classGen.replaceMethod(method,newMethod);
        }
        return newProposedName;
    }

    /**
     * Generate RTTI information for a method.
     *
     * @param classGen the class of the method
     * @param constPool the constant pool of the method
     * @param method the method
     * @param passive if true, do not perform any translation
     */
    Method fillRTTI(ClassGen classGen, ConstantPoolGen constPool, Method method,
                    boolean passive) 
    {
        String className = classGen.getClassName(); 
        String methodName = method.getName();
        MethodGen methodGen = new MethodGen(method,className,constPool);
        Iterator instructions = methodGen.getInstructionList().iterator();
        String methodSign = null;
        if (methodName.startsWith(prefix)) {
            methodName = methodName.substring(prefix.length());
        }
        methodSign = className+"."+getMethodFullName(method);
        VMStack stack = new VMStack(constPool,method.getCode(),
                                    methodGen.getArgumentTypes().length,method.isStatic());
        loggerRtti.debug("detecting RTTI for "+methodSign);
        while (instructions.hasNext()) {
            InstructionHandle ih=(InstructionHandle)instructions.next();
            Instruction instruction = ih.getInstruction();
            loggerBytecode.debug("offset: "+ih.getPosition());
            stack.preExecute(ih);
         
            if (instruction instanceof PUTFIELD && 
                stack.peek(1)==VMStack.thisPointer) {
                // setters and modifiers
                PUTFIELD putfield = (PUTFIELD)instruction;
                String fieldName = putfield.getFieldName(constPool);
                if (!isSystemField(fieldName)) {
                    if (stack.peek() instanceof VMStack.Argument) {
                        loggerRtti.debug("  sets field "+fieldName);
                        rtti.addltSetField(className,methodSign,fieldName);
                        // TODO: check if the type is translated collection
                    } 
                    loggerRtti.debug("  modifies field "+fieldName);
                    rtti.addltModifiedField(className,methodSign,fieldName);
                }
            } else if (instruction instanceof PUTSTATIC && 
                       ((PUTSTATIC)instruction).getClassName(constPool).equals(classGen.getClassName())) {
                // static setters and modifiers
                PUTSTATIC putfield = (PUTSTATIC)instruction;
                String fieldName = putfield.getFieldName(constPool);
                if (!isSystemField(fieldName)) {
                    if (stack.peek() instanceof VMStack.Argument) {
                        loggerRtti.debug(methodSign+" sets static field "+fieldName);
                        rtti.addltSetField(className,methodSign,fieldName);
                        // TODO: check if the type is translated collection
                    } 
                    loggerRtti.debug("  modifies static field "+fieldName);
                    rtti.addltModifiedField(className,methodSign,fieldName);
                }
            } else if (instruction instanceof ReturnInstruction && 
                       !(instruction instanceof RETURN)) {
                if (stack.peek() instanceof VMStack.FieldValue) {
                    // *the* getter
                    VMStack.FieldValue fieldValue = (VMStack.FieldValue)stack.peek();
                    if (!isSystemField(fieldValue.field)) {
                        if (fieldValue.substance==VMStack.thisPointer) {
                            loggerRtti.debug("  returns field "+fieldValue.field);
                            rtti.addltReturnedField(className,methodSign,fieldValue.field);
                        } else {
                            loggerRtti.debug("  returns "+stack.peek());
                            rtti.addltReturnedField(className,methodSign,null);
                            rtti.setltIsGetter(className,methodSign,false);
                        }
                    }
                } else {
                    loggerRtti.debug("  returns "+stack.peek());
                    rtti.setltIsGetter(className,methodSign,false);               
                }
            } else if (instruction instanceof GETFIELD &&
                       stack.peek()==VMStack.thisPointer) {
                // getters
                String fieldName = ((GETFIELD)instruction).getFieldName(constPool);
                if (!isSystemField(fieldName)) {
                    loggerRtti.debug("  accesses field "+fieldName);
                    rtti.addltAccessedField(className,methodSign,fieldName);
                }
            } else if (instruction instanceof GETSTATIC &&
                       ((GETSTATIC)instruction).getClassName(constPool).equals(classGen.getClassName())) {
                // getters
                String fieldName = ((GETSTATIC)instruction).getFieldName(constPool);
                if (!isSystemField(fieldName)) {
                    loggerRtti.debug("  accesses static field "+fieldName);
                    rtti.addltAccessedField(className,methodSign,fieldName);
                }
            } else if ((instruction instanceof INVOKEVIRTUAL || 
                        instruction instanceof INVOKEINTERFACE) && 
                       !className.startsWith("org.objectweb.jac.lib.java")) {
                // adders and removers
                InvokeInstruction invoke = (InvokeInstruction)instruction;
                String invokedClass = invoke.getClassName(constPool);
                String invokedMethodName = invoke.getMethodName(constPool);
                int numArgs = invoke.getArgumentTypes(constPool).length;
                Object substance = stack.invokedObject(invoke);
                loggerBytecode.info("substance="+substance);
                if (substance instanceof VMStack.FieldValue) {
                    VMStack.FieldValue fieldValue = (VMStack.FieldValue)substance;
                    loggerBytecode.info("detected INVOKE on field "+substance);
                    ConstantFieldref fieldref = 
                        (ConstantFieldref)constPool.getConstant(fieldValue.index);
                    ConstantNameAndType nameAndType = 
                        (ConstantNameAndType)constPool.getConstant(
                            fieldref.getNameAndTypeIndex());
                    String signature = 
                        nameAndType.getSignature(constPool.getConstantPool());
                    String fieldName = 
                        nameAndType.getName(constPool.getConstantPool());
                    loggerBytecode.debug("field signature: "+signature);
                    loggerBytecode.debug("invoked class name: "+invokedClass);
                    if (signature.startsWith("Lorg/objectweb/jac/lib/java") && 
                        !invokedClass.startsWith("org.objectweb.jac.lib.java")) {
                        loggerBytecode.info("FIXING INCOMPATIBLE TYPES "+
                                  signature+" AND "+invokedClass+
                                  " for "+nameAndType);
                        loggerBytecode.info("  ==> "+
                                  signature.substring(1,signature.length()-1));
                        invoke.setIndex(
                            constPool.addMethodref(
                                signature.substring(1,signature.length()-1),
                                invoke.getName(constPool),
                                invoke.getSignature(constPool)));
                    }

                    // check that the field value belongs to "this"
                    if (fieldValue.substance==VMStack.thisPointer && 
                        (invoke.getClassType(constPool).isCastableTo(Type.getType(Collection.class)) ||
                         invoke.getClassType(constPool).isCastableTo(Type.getType(Map.class)))) 
                    {
                        if (invokedMethodName.equals("add")) {
                            if ((numArgs==1 && areArguments(stack,numArgs)) ||
                                (numArgs==2 && isArgument(stack,0))) {
                                loggerRtti.debug(methodSign+" is adder for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltAddedCollection(className,methodSign,fieldName);
                            } else {
                                loggerRtti.debug(methodSign+" is modifier for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltModifiedCollection(className,methodSign,fieldName);
                            }
                            if (numArgs==2) {
                                VMStack.Argument arg = getArgument(stack,1);
                                int n;
                                if (arg!=null) {
                                    n = arg.n - (method.isStatic() ? 0 : 1);
                                    loggerRtti.debug("  has collectionIndexArgument "+n);
                                    rtti.setCollectionIndexArgument(className,methodSign,n);
                                }
                                arg = getArgument(stack,0);
                                if (arg!=null) {
                                    n = arg.n - (method.isStatic() ? 0 : 1);
                                    loggerRtti.debug("  has collectionItemArgument "+n);
                                    rtti.setCollectionItemArgument(className,methodSign,n);
                                }
                            }
                        } else if (invokedMethodName.equals("put")) {
                            if (isArgument(stack,0)) {
                                loggerRtti.debug("  is putter for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltAddedCollection(className,methodSign,fieldName);
                            } else {
                                loggerRtti.debug("  is modifier for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltModifiedCollection(className,methodSign,fieldName);
                            }
                        } else if (invokedMethodName.equals("remove")) {
                            if (areArguments(stack,numArgs)) {
                                loggerRtti.debug("  is remover for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltRemovedCollection(className,methodSign,fieldName);
                            } else {
                                loggerRtti.debug("  is modifier for "+
                                          nameAndType.getName(constPool.getConstantPool()));
                                rtti.addltModifiedCollection(className,methodSign,fieldName);
                            }
                        } else if (invokedMethodName.equals("set") ||
                                   invokedMethodName.equals("clear") ||
                                   invokedMethodName.equals("addAll") ||
                                   invokedMethodName.equals("removeAll") ||
                                   invokedMethodName.equals("retainAll")) {
                            loggerRtti.debug("  is modifier for "+
                                      nameAndType.getName(constPool.getConstantPool()));
                            rtti.addltModifiedCollection(className,methodSign,fieldName);
                            if (numArgs==2 && isArgument(stack,1)) {
                                loggerRtti.debug("  has collectionIndexArgument 0");
                                rtti.setCollectionIndexArgument(className,methodSign,0);
                            }
                        }
                    }
                }

                if (JacLoader.classIsToBeAdapted(invokedClass) && 
                    !(invokedMethodName.equals(methodName) && 
                      invokedClass.equals(className)))
                    rtti.addInvokedMethod(className,methodSign,
                                          new InvokeInfo(
                                              stack.getSubstance(invoke),
                                              invokedClass,invokedMethodName));
            } else if (instruction instanceof INVOKESPECIAL) {
                INVOKESPECIAL invoke = (INVOKESPECIAL)instruction;
                Object substance = stack.getSubstance(invoke);
                loggerRtti.debug("  invokespecial "+
                                 invoke.getMethodName(constPool)+" on "+substance);
                if (substance==VMStack.thisPointer &&
                    invoke.getMethodName(constPool).equals(methodName) &&
                    Arrays.equals(invoke.getArgumentTypes(constPool),method.getArgumentTypes())) 
                {
                    loggerRtti.debug("  calls super");
                    rtti.setCallSuper(className,methodSign);
                }
            }
            stack.execute(instruction,ih);
        }
        return methodGen.getMethod();
    }

    static boolean isSystemField(String fieldName) {
        return fieldName.indexOf('$')!=-1;
    }

    /**
     * Returns true if the n top elements of the stack are arguments
     * @param stack a stack
     * @param n number of elements to check
     * @return true if the n top elements of the stack are arguments
     */
    static boolean areArguments(Stack stack, int n) {
        for (;n>0; n--) {
            if (!isArgument(stack,n-1))
                return false;
        }
        return true;
    }

    /**
     * Returns true if the nth top element of the stack is an argument
     *
     * @param stack a stack
     * @param n index element to check
     * @return true if stack.peek(n) is an argument
     */
    static boolean isArgument(Stack stack, int n) {
        return !( !(stack.peek(n) instanceof VMStack.Argument) &&
                  !(stack.peek(n) instanceof VMStack.PrimitiveValue &&
                    ((VMStack.PrimitiveValue)stack.peek(n)).wrappedValue 
                    instanceof VMStack.Argument) );
    }

    static VMStack.Argument getArgument(Stack stack, int n) {
        Object elt = stack.peek(n);
        if (elt instanceof VMStack.Argument) {
            return (VMStack.Argument)elt;
        } else if (elt instanceof VMStack.PrimitiveValue &&
                   ((VMStack.PrimitiveValue)elt).wrappedValue 
                   instanceof VMStack.Argument) {
            return (VMStack.Argument)((VMStack.PrimitiveValue)elt).wrappedValue;
        }
        return null;
    }

    /**
     * Change the type of collection fields to use org.objectweb.jac.lib types
     * @param classGen the class holding the field
     * @param field the field whose type to change
     */
    private void translateField(ClassGen classGen, Field field)
    {
        String type = Type.getType(field.getSignature()).toString();
        String translatedType = (String)collectionTypes.get(type);
        if (translatedType != null) {
            logger.info("field "+
                      field.getName()+" "+type+
                      " -> "+translatedType);
            ConstantPoolGen constPool = classGen.getConstantPool();
            FieldGen newField = new FieldGen(field,constPool);
            newField.setType(Type.getType(translatedType));
            classGen.replaceField(field,newField.getField());
            // replace the Fieldref entry in the constant pool
            constPool.setConstant(
                constPool.lookupFieldref(classGen.getClassName(),
                                         field.getName(),
                                         field.getSignature()),
                new ConstantFieldref(
                    classGen.getClassNameIndex(),
                    constPool.addNameAndType(field.getName(),
                                             translatedType)));
        }
    }

    static final String prefix="_org_";

    /**
     * Translate a method (rename it and generate a stub).
     * 
     * @param classGen the ClassGen holding the method
     * @param constPool the constant pool 
     * @param method the method to translate
     * @param staticFieldIndex index of the static field holding the
     * reference to the MethodItem of the method
     * @param wrappingChainIndex index of the static field holding the
     * wrapping chain for that method.
     * @param wrappingChainIndexes 
     */
    private void translateMethod(ClassGen classGen, ConstantPoolGen constPool,
                                 Method method, int staticFieldIndex,
                                 List staticFieldIndexes, 
                                 int wrappingChainIndex,
                                 List wrappingChainIndexes)
    {
        logger.debug("Translating method "+
                     method.getName()+"("+method.getNameIndex()+")");
        // get all methods
        String tmpname;
		
        tmpname = method.getName();
        //rename the method "xxx" in "_org_xxx" (except for
        //constructors, where it's called _org_<class_name>)
        String newName = null;
        InstructionList callSuper;
        String className = classGen.getClassName()
            .substring(classGen.getClassName().lastIndexOf(".")+1);
        if (tmpname.equals("<init>")) {
            if (!method.isPublic()) {
                // We keep a copy of non public constructors because
                // they may be called from other constructors and we
                // won't generate a stub for them
                MethodGen copyMethod = 
                    new MethodGen(method,classGen.getClassName(),constPool);
                translateConstructor(classGen,method,copyMethod,null,false);
                classGen.addMethod(copyMethod.getMethod());
            }
            callSuper = new InstructionList();
            newName = renameMethod(
                classGen, constPool, method,
                prefix+className,
                callSuper);
            logger.debug("callSuper = "+callSuper);
            callSuper.setPositions();
        } else {
            callSuper = null;
            classGen.removeMethod(method);
            if (!classGen.getClassName().startsWith("org.objectweb.jac.lib.java")) {
                method = fillRTTI(classGen,constPool,method,false);
            }
            newName = renameMethod(classGen, constPool, method, 
                                   prefix+tmpname+
                                // Renamed methods should not override each other
                                   (method.isStatic()?"":"_"+className),
                                   callSuper);
        }
        if (!tmpname.equals("<init>") || method.isPublic()) {
            generateStubMethod(classGen,constPool,method,newName,
                               staticFieldIndex,staticFieldIndexes,
                               wrappingChainIndex,wrappingChainIndexes, 
                               callSuper);
        } else {
            logger.debug("skipping stub method generation for "+method);
        }
    }

    /**
     * Returns true is the method should be translated
     * @param className name of the class
     * @param method the method
     */
    boolean isTranslatable(String className,Method method) {
        String methodName = method.getName();
        if ((!method.isPublic()) ||
            method.isAbstract() || 
            method.isInterface()) return false;
        if (method.isStatic()
            && (methodName.equals("get") ||
                methodName.equals("main") ||
                methodName.equals("<clinit>"))) 
            return false;
        if ( (methodName.equals("toString") && 
              method.getSignature().startsWith("()")) ||
             (methodName.equals("equals") && 
              method.getSignature().startsWith("(Ljava/lang/Object;)")) ||
             (methodName.equals("hashCode") && 
              method.getSignature().startsWith("()")) )
            return false;
        Map wrappableMethods = JacPropLoader.wrappableMethods;
        if(!wrappableMethods.containsKey(className)) {
            logger.debug(className+" has all methods wrappable");
            return true;
        }
        // constructors should always be wrappable
        if (methodName.equals("<init>"))
            return true;
        List methods = (List)wrappableMethods.get(className);
        Iterator it = methods.iterator();
        while(it.hasNext()) {
            String cur = (String)it.next();
            if (cur.equals(methodName)) {
                logger.debug(methodName+" is wrappable");
                return true;
            }
        }
        logger.debug(methodName+" is not wrappable");
         
        return false;
    }

    /**
     * Create a static field containing a reference to an
     * AbstractMethodItem for method.
     * @return the index in the constant pool of created field 
     */
    int createMethodStaticField(ClassGen classGen, ConstantPoolGen cp, 
                                String fieldName) {
        FieldGen fieldGen = 
            new FieldGen(Constants.ACC_STATIC | 
                         Constants.ACC_PUBLIC | 
                         Constants.ACC_TRANSIENT |
                         Constants.ACC_FINAL,
                         Type.getType("Lorg/objectweb/jac/core/rtti/AbstractMethodItem;"),
                         fieldName,
                         cp);
        Field field = fieldGen.getField();
        //      int fieldIndex = field.getConstantValue().getConstantValueIndex();
        classGen.addField(field);
        return cp.addFieldref(classGen.getClassName(),
                              field.getName(),
                              field.getSignature());
    }

    int createWrappingChainField(ClassGen classGen, 
                                 ConstantPoolGen constPool,
                                 String fieldName,
                                 boolean isStatic) {
        FieldGen fieldGen=new FieldGen(Constants.ACC_PUBLIC | 
                                       Constants.ACC_TRANSIENT |
                                       Constants.ACC_FINAL | 
                                       (isStatic?Constants.ACC_STATIC:0),
                                       Type.getType("Lorg.objectweb.jac.core.WrappingChain;"),
                                       fieldName,
                                       constPool);
        Field field = fieldGen.getField();
        classGen.addField(field);
        return constPool.addFieldref(
            classGen.getClassName(),
            field.getName(),field.getSignature());
    }

    /**
     * Setup class before really translating it.
     *
     * <p>The Wrappee interface is added, and the __JAC_TRANSLATED
     * field is added too. A default constructor is created if none
     * exists.</p>
     * @param classGen translated class */
    private void startingTranslation(ClassGen classGen)
    {
		
        ConstantPoolGen constPool = classGen.getConstantPool();
        int objectIndexRef = constPool.lookupClass("java.lang.Object");

        // Add the __JAC_TRANSLATED field
        FieldGen fgJT = new FieldGen (
            Constants.ACC_STATIC | 
            Constants.ACC_PUBLIC | 
            (classGen.isInterface() ? Constants.ACC_FINAL : Constants.ACC_TRANSIENT), 
            Type.BOOLEAN, 
            "__JAC_TRANSLATED", 
            constPool);
        classGen.addField (fgJT.getField());
		
        classGen.addInterface("org.objectweb.jac.core.Wrappee");
      
        if (!classGen.isInterface() && 
            classGen.containsMethod("<init>","()V")==null) {
            generateDefaultConstructor(classGen);
        }
    }

    /**
     * Returns true if the class is already translated (ie it contains
     * a field named __JAC_TRANSLATED)
     */
    protected boolean isTranslated(JavaClass javaClass) {
        Field[] fields = javaClass.getFields();
        for (int i=0; (i<fields.length); i++)
            if (fields[i].getName().equals("__JAC_TRANSLATED"))
                return true;
        return false;
    }

    /**
     * Returns the full name of a method as required by RTTI
     * @param method the method
     * @return full name of the method (something like
     * myMethod(java.lang.String,int) */
    String getMethodFullName(Method method) {
        String fullName=method.getName()+"(";
        Type[] argumentTypes = Type.getArgumentTypes(method.getSignature());      
        for(int i=0;i<argumentTypes.length;i++) {
            fullName+=argumentTypes[i];
            if (i<argumentTypes.length-1) 
                fullName+=",";
        }
        fullName+=")";
        return fullName;
    }

    /**
     * Create code to initialize static fields that hold MethodItems
     * and wrapping chains.
     *
     * @param classGen the class to build static fields initialization for
     * @param fields a list of StaticField
     * @param clinit the static class initialization method if one
     * already exists
     */
    void initStaticFields(ClassGen classGen, List fields, Method clinit) {
        //search for the static {} method
        Method[] methods = classGen.getMethods();
        ConstantPoolGen constPool = classGen.getConstantPool();
        MethodGen methodGen;
        InstructionFactory ifactory = new InstructionFactory (classGen);
        logger.info("initStaticFields for "+
                  classGen.getClassName());
        // If there is no static method 
        // then we will need to build one from scratch
        if (clinit==null)
        {
            methodGen = new MethodGen (Constants.ACC_STATIC, 
                                       Type.VOID, emptyTypeArray, 
                                       ExtArrays.emptyStringArray,
                                       Constants.STATIC_INITIALIZER_NAME,
                                       classGen.getClassName(), 
                                       new InstructionList(), 
                                       constPool);
        } else {
            //else we need to update the already existing one
            if (!clinit.getName().equals(Constants.STATIC_INITIALIZER_NAME)) {
                logger.error("This is not a class initializer: "+clinit.getName());
            }
            methodGen = new MethodGen (clinit,
                                       classGen.getClassName(), 
                                       constPool);
        }
        InstructionList il = new InstructionList();
      
        // ClassRepository.get() ...
        il.append(
            ifactory.createInvoke("org.objectweb.jac.core.rtti.ClassRepository","get",
                                  Type.getType("Lorg/objectweb/jac/core/rtti/ClassRepository;"),
                                  emptyTypeArray,
                                  Constants.INVOKESTATIC));

        // ... .getClass(<className>)
        int classNameIndex = constPool.addString(classGen.getClassName());
        il.append(new LDC(classNameIndex));
        il.append(
            ifactory.createInvoke("org.objectweb.jac.core.rtti.ClassRepository","getClass",
                                  Type.getType("Lorg/objectweb/jac/core/rtti/ClassItem;"),
                                  new Type[] {Type.STRING},
                                  Constants.INVOKEVIRTUAL));

        // We now have the ClassItem on the stack
        // Let's call getMethod on it.
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            StaticField field = (StaticField)it.next();
            logger.info("initStaticField("+
                        field.fieldName+","+field.methodName+")");

            il.append(new DUP());
            il.append(new LDC(constPool.addString(field.methodName)));
            il.append(
                ifactory.createInvoke(
                    "org.objectweb.jac.core.rtti.ClassItem","getAbstractMethod",
                    Type.getType("Lorg/objectweb/jac/core/rtti/AbstractMethodItem;"),
                    new Type[] {Type.STRING},
                    Constants.INVOKEVIRTUAL));
            il.append(new PUTSTATIC(field.fieldIndex));
        }
        il.append(new POP());
        il.append (methodGen.getInstructionList());
        if (clinit==null) {
            il.append (InstructionFactory.createReturn(Type.VOID));
        }

        methodGen.setInstructionList(il);
        methodGen.removeNOPs();
        methodGen.removeLocalVariables();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
		
        if (clinit!=null)
            classGen.removeMethod(clinit);
        classGen.addMethod (methodGen.getMethod());
			
    }

    static class StaticField {
        public String fieldName;
        public String methodName;
        public int fieldIndex;
        public StaticField(String fieldName, String methodName, int fieldIndex) {
            this.fieldName = fieldName;
            this.methodName = methodName;
            this.fieldIndex = fieldIndex;
        }
    }

    /**
     * Translate a class
     * @param aClass name of the class to translate
     * @return bytecode of the translated class
     */
    public byte[] translateClass(String aClass) throws Exception 
    {
        JavaClass originalClass = Repository.lookupClass(aClass); 
        if (originalClass==null) {
            logger.info("CLASS NOT FOUND "+aClass);
            throw new ClassNotFoundException("Class not found: "+aClass);
        }
		
        // test if it is public (for security resons, protected and
        // private classes are not translated).
        //if (!originalClass.isPublic()) {
        //   logger.info("skipping non public class "+aClass);
        //   return null;
        //}

        // test if the class is already translated/adapted.
        if (!isTranslated(originalClass))
        {
            /** None of the above mentionned cases are verified. Adapt the class. */
         
            logger.info("TRANSLATING "+aClass);
         
            // Mark it as translated
            ClassGen  newClassGen = new ClassGen (originalClass); 
            startingTranslation(newClassGen);

            // test if a real class (not an interface)
            if (originalClass.isInterface()) {
                logger.info("skipping interface "+aClass);
                return newClassGen.getJavaClass().getBytes();
            }

            Method clinit = newClassGen.containsMethod("<clinit>","()V");
            if (clinit!=null)
                logger.debug("clinit = "+clinit+"("+clinit.getNameIndex()+")");
            else
                logger.debug("no <clinit>");
         
            // translate fields 
            // (java.util.<collection> -> org.objectweb.jac.lib.java.util.<collection>)
            Field[] fields = newClassGen.getFields();
            if (JacPropLoader.translateFields(newClassGen.getClassName())) {
                for (int i=0; i<fields.length; i++) {
                    if (!fields[i].isTransient() && !fields[i].isStatic())
                        translateField(newClassGen, fields[i]);
                }
            }

            ConstantPoolGen constPool = newClassGen.getConstantPool();
         
            // get all methods and translate them
            Method[] methods = newClassGen.getMethods();
            LinkedList staticFields = new LinkedList();
            List staticFieldIndexes = new Vector();
            List wrappingChainIndexes = new Vector();

            // first pass to get the field indexes
            for (int i=0; i<methods.length; i++) {
                if (isTranslatable(newClassGen.getClassName(),methods[i])) { 
                    // create a static field containing a ref to the
                    // AbstractMethodItem
                    staticFieldIndexes.add(new Integer(
                        createMethodStaticField(newClassGen,constPool,
                                                "__JAC_method_"+i)));
                    wrappingChainIndexes.add(new WCIndex(
                        methods[i].isStatic(),
                        createWrappingChainField(newClassGen,
                                                 constPool,"__JAC_wc_"+i,
                                                 methods[i].isStatic())));
                } else {
                    staticFieldIndexes.add(null);
                    wrappingChainIndexes.add(null);
                }
            }
               
            for (int i=0; i<methods.length; i++) {
                if (isTranslatable(newClassGen.getClassName(),methods[i])) { 
               
                    try {
                        translateMethod(
                            newClassGen,constPool,
                            methods[i],
                            ((Integer)staticFieldIndexes.get(i)).intValue(),
                            staticFieldIndexes,
                            ((WCIndex)wrappingChainIndexes.get(i)).index,
                            wrappingChainIndexes);
                        
                        staticFields.add(new StaticField(
                            "__JAC_method_"+i,
                            getMethodFullName(methods[i]),
                            ((Integer)staticFieldIndexes.get(i)).intValue()));
                    } catch(Exception e) {
                        logger.error("translateClass: failed to translate method "+
                                     newClassGen.getClassName()+"."+methods[i]+": "+e);
                        throw e;
                    }

                } else {

                    // translate private or protected contructors (not
                    // stored in rtti)
                    if (methods[i].getName().equals("<init>") && 
                        !methods[i].isPublic())
                    {
                        translateMethod(
                            newClassGen,constPool,
                            methods[i],
                            0,null,0,null);
                    } else if (methods[i].getName().equals("this")) {
                        MethodGen methodGen = 
                            new MethodGen(methods[i], newClassGen.getClassName(), constPool);
                        translateConstructor(newClassGen,methods[i],methodGen,null,true);
                        newClassGen.replaceMethod(methods[i],methodGen.getMethod());
                    }

                    logger.debug("skipping "+methods[i]+
                              "("+methods[i].getNameIndex()+")");
                    if (methods[i].getName().startsWith("<init>")) {
                        logger.warn("Constructor not translated: "+aClass+"."+methods[i]);
                    }
                }
            }

            // initialize static fields
            initStaticFields(newClassGen,staticFields,clinit);

            return newClassGen.getJavaClass().getBytes();
			
        } else {

            logger.info("ALREADY TRANSLATED "+aClass);
            ConstantPoolGen constPool = 
                new ConstantPoolGen(originalClass.getConstantPool());
            Method[] methods = originalClass.getMethods();
            /*
              for (int i=0; i<methods.length; i++) {
              if (methods[i].getName().startsWith(prefix))
              fillRTTI(originalClass.getClassName(), constPool, methods[i]);
              }
            */         
            return originalClass.getBytes();
        }

    }

    /**
     * Computes RTTI info for a class and gets its bytecode
     * @param aClass name of the class analyze
     * @return bytecode of the class
     */
    public byte[] fillClassRTTI(String aClass) throws Exception 
    {
        JavaClass originalClass = Repository.lookupClass(aClass); 
        if (originalClass==null) {
            logger.info("CLASS NOT FOUND "+aClass);
            throw new ClassNotFoundException("Class not found: "+aClass);
        }
		
        // test if a real class (not an interface)
        if (originalClass.isInterface()) {
            logger.info("skipping interface "+aClass);
            return null;
        }

        /** None of the above mentionned cases are verified. Load the class. */
         
        logger.info("LOADING "+aClass);
         
        // Mark it as translated
        ClassGen  newClassGen = new ClassGen(originalClass); 
        ConstantPoolGen constPool = newClassGen.getConstantPool();
         
        // get all methods and translate them
        Method[] methods = newClassGen.getMethods();

        for (int i=0; i<methods.length; i++) {
            logger.info("Analyzing "+methods[i]);
            fillRTTI(newClassGen,constPool,methods[i],true);
        }

        return newClassGen.getJavaClass().getBytes();

    }

    static public boolean isCollection(String type) {
        return collectionTypes.containsKey(type);
    }

    static class WCIndex {
        boolean isStatic;
        int index;
        public WCIndex(boolean isStatic, int index) {
            this.isStatic = isStatic;
            this.index = index;
        }
    }

    static HashMap collectionTypes = new HashMap();
    {
        collectionTypes.put("java.util.Vector","Lorg/objectweb/jac/lib/java/util/Vector;");
        collectionTypes.put("java.util.Hashtable","Lorg/objectweb/jac/lib/java/util/Hashtable;");
        collectionTypes.put("java.util.HashMap","Lorg/objectweb/jac/lib/java/util/HashMap;");
        collectionTypes.put("java.util.HashSet","Lorg/objectweb/jac/lib/java/util/HashSet;");
    }

    static final Type[] emptyTypeArray = new Type[0];
}

