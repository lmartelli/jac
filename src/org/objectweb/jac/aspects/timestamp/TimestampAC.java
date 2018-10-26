/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.timestamp;

import java.util.Iterator;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.*;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.ExtBoolean;

public class TimestampAC extends AspectComponent implements TimestampConf {
    static final Logger logger = Logger.getLogger("timestamp");
    
    public static final String FOLLOW = "TimestampAC.FOLLOW";

    public void declareTimestampedClasses(String classExpr, 
                                          String wrappeeExpr,
                                          String repositoryName) 
    {
        pointcut("ALL",classExpr,"MODIFIERS",
                 new Wrapper(this,repositoryName),null);
    }

    public void followLink(FieldItem link, boolean follow) {
        logger.info("Setting follow on "+link);
        link.setAttribute(FOLLOW,ExtBoolean.valueOf(follow));
    }

    public static class Wrapper extends org.objectweb.jac.core.Wrapper {
        public Wrapper(AspectComponent ac, String stampsName) {
            super(ac);
            this.stampsName = stampsName;
        }

        String stampsName;
        Timestamps stamps; 

        void setStamps() {
            stamps = (Timestamps)NameRepository.get().getObject(stampsName);
        }

        public Object invoke(MethodInvocation invocation) throws Throwable {
            Object res = invocation.proceed();
            setStamps();
            if (stamps!=null) {
                Interaction interaction = (Interaction)invocation;
                touch(interaction.wrappee,interaction.getClassItem());
            }
            return res;
        }

        void touch(Object object, ClassItem cl) {
            logger.info("touch "+object);
            stamps.touch(object);
            if (cl==null) {
                cl = ClassRepository.get().getClass(object);
            }
            FieldItem[] fields = cl.getFields();
            for (int i=0; i<fields.length; i++) {
                FieldItem field= fields[i];
                logger.debug("Testing field "+field);
                boolean followed = false;
                if (field.isReference()) {
                    FieldItem opposite = (FieldItem)field.getOppositeRole();
                    if (opposite!=null && opposite.isAggregation()) {
                        logger.debug("  opposite is aggregation");
                        followed = true;
                        Object touched = fields[i].getThroughAccessor(object);
                        if (touched!=null) 
                            touch(touched,null);
                    }
                } 

                if (!followed && field.getBoolean(FOLLOW, false)) {
                    logger.debug("  must be followed");
                    if (field instanceof CollectionItem) {
                        Iterator it = ((CollectionItem)field).getActualCollectionThroughAccessor(object).iterator();
                        while (it.hasNext()) {
                            Object touched = it.next();
                            if (touched!=null) 
                                touch(touched,null);
                            else
                                logger.debug("  Not touching null value in "+field.getLongName());
                        }
                    } else {
                        Object touched = field.getThroughAccessor(object);
                        if (touched!=null)
                            touch(touched,null);
                        else
                            logger.info("  Not touching null value "+field.getLongName());
                    }
                }
            }
        }

        public Object construct(ConstructorInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }
}
