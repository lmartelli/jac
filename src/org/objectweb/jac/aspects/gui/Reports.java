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

package org.objectweb.jac.aspects.gui;

import dori.jasper.engine.JRDataSource;
import dori.jasper.engine.JRException;
import dori.jasper.engine.JasperCompileManager;
import dori.jasper.engine.JasperExportManager;
import dori.jasper.engine.JasperFillManager;
import dori.jasper.engine.JasperPrint;
import dori.jasper.engine.JasperReport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.reports.JacDataSource;
import org.objectweb.jac.core.rtti.ClassRepository;

public class Reports {
    static Logger logger = Logger.getLogger("report");

    /**
     * Generate a PDF report
     * @param reportDef resource name of the XML report definition file
     * @param pdfFile file where to store the resulting PDF document
     */
    public static void genReport(String reportDef, File pdfFile) 
        throws JRException 
    {
        genReport(reportDef,pdfFile,new HashMap());
    }

    /**
     * Generate a PDF report
     * @param reportDef resource name of the XML report definition file
     * @param pdfFile file where to store the resulting PDF document
     * @param parameters the parameters to fill the report with
     */
    public static void genReport(String reportDef, File pdfFile, Map parameters) 
        throws JRException 
    {
        JasperReport report = getJasperReport(reportDef);
        logger.debug("Filling report "+report);
        JasperPrint print = 
            JasperFillManager.fillReport(
                report,
                parameters,
                new JacDataSource(ClassRepository.get().getClass(Class.class)));
        logger.debug("Exporting report "+print);
        JasperExportManager.exportReportToPdfFile(print,pdfFile.getPath());
        logger.debug("Done");
    }

    /**
     * Generate a PDF report
     * @param reportDef resource name of the XML report definition file
     * @param out file where to store the resulting PDF document
     * @param parameters the parameters to fill the report with
     * @param dataSource the data source
     */
    public static void genReport(String reportDef, OutputStream out, 
                                 Map parameters, JRDataSource dataSource) 
        throws JRException 
    {
        JasperReport report = getJasperReport(reportDef);
        logger.debug("Filling report "+report);
        JasperPrint print = 
            JasperFillManager.fillReport(
                report,
                parameters,
                dataSource);
        logger.debug("Exporting report "+print);
        JasperExportManager.exportReportToPdfStream(print,out);
        logger.debug("Done");
    }

    public static JasperReport getJasperReport(String reportDef) throws JRException {
        logger.debug("Compiling report file "+reportDef);
        return
            JasperCompileManager.compileReport(
                Actions.class.getClassLoader().getResourceAsStream(reportDef));
    }
}
