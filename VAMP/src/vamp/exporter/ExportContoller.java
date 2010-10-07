/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *   This file is part of ProSE.
 *   Copyright (C) 2007-2010 CeBiTec, Bielefeld University
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package vamp.exporter;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.spi.DirectoryManager;
import vamp.view.dataVisualisation.snpDetection.Snp;
import javax.swing.SwingWorker;
/**
 *
 * @author jstraube
 */
public class ExportContoller implements ActionListener {



    private File exportFile;
    private File tempFile;
    private String filename;
    private String contentType;
    private List<Snp> snps = new ArrayList<Snp>();
    private ExcelExporter exporter = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand.startsWith("export_")) {

            String exportTypeName = actionCommand.substring(7).toUpperCase();
            try {
                EnumExportType exportType = EnumExportType.getExporters(exportTypeName);
                notifyExportEvent(exportType, "text/plain");
                setContentType("text/plain");

            } catch (IllegalArgumentException iae) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Type does not match any exporter!");
            }
        }
    }

    public enum EnumExportType {

        EXCEL;

        /**
         * Get the EnumExportType of a given name
         * @param exportTypeName
         * @return enumExportType
         */
        public static EnumExportType getExporters(String exportTypeName) {
            return EnumExportType.valueOf(exportTypeName);
        }
    }

    /**
     * Notifies an export event and starts the appropriate exporter.
     * @param exportType The enum for the wanted exporter.
     * @param contentType The content type of the output file.
     */
    public void notifyExportEvent(
            EnumExportType exportType,
            final String contentType) {


       

        try {
            /* initialise correct exporter */
            switch (exportType) {
                case EXCEL:
                    exporter = new ExcelExporter();
                    exporter.setSNPs(snps);
                    break;
            }
        } catch (NoClassDefFoundError def) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Unknown exporter! Export failed!");
        }

        /* start exporter */
        if (exporter != null) {
            if (exporter.readyToExport()) {

               
                try {
                   
                 File tempDir = new File(System.getProperty("java.io.tmpdir"));
   
                     exportFile = exporter.writeFile(tempFile, filename);

                    // FileWriter f = new FileWriter(exportFile);
                  //    BufferedWriter writter = new BufferedWriter(f);

                   //   writter.write(cbuf, off, len);


                } catch (FileNotFoundException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!");
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!");
                    ex.printStackTrace();
                }
            } else {
                // Exporter is not ready!
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Exporter is not ready now");
            }
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return exportFile.getName();
    }

    public int getSize() {
        return (int) exportFile.length();
    }

    public void writeFile(FileWriter fileWriter)
            throws IOException {
        byte[] content = new byte[(int) exportFile.length()];
        FileInputStream inputStream = new FileInputStream(
                exportFile);
        final int bytesRead = inputStream.read(content);
        inputStream.close();
        fileWriter.write("");
    }

    public void setSnpData(List<Snp> snps){
    this.snps = snps;
    }

    public void setFile(String path){
        tempFile = new File(path);
    }

    public void setName(String name){
        this.filename = name;
    }

}
