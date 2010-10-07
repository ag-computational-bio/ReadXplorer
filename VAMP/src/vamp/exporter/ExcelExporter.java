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

import jxl.write.Label;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.NumberFormats;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import vamp.view.dataVisualisation.snpDetection.Snp;

/**
 *
 * @author jstraube
 */
public class ExcelExporter implements ExporterI {

    private List<Snp> snps;

    @Override
    public boolean readyToExport() {
        if (snps.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public File writeFile(File tempDir, String name) throws FileNotFoundException, IOException {
        File tempFile = File.createTempFile(name, ".xls", tempDir);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting to write Excel file..." + tempFile.getAbsolutePath());


        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = Workbook.createWorkbook(tempFile, wbSettings);

        WritableSheet snpSheet = null;
        int currentPage = 0;
        if (!snps.isEmpty()) {
            snpSheet = workbook.createSheet("SNPs", currentPage++);
        }
        try {
            if (snpSheet != null) {
                fillSheet(snpSheet, snps);
            }
            workbook.write();
            workbook.close();

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished writing Excel file!");
        } catch (WriteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error writing file" + ex.getMessage());
        }

        return tempFile;
    }

    @Override
    public void addColumn(WritableSheet sheet, String celltype, Object cellvalue, int column, int row) throws WriteException {
        WritableFont arialbold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
        if (cellvalue == null) {
            Label label = new Label(column, row, "n/a");
            sheet.addCell(label);
        } else if (celltype.equals("LABEL")) {
            WritableCellFormat header = new WritableCellFormat(arialbold);
            Label label = new Label(column, row, (String) cellvalue, header);
            sheet.addCell(label);
        } else if (celltype.equals("STRING")) {
            WritableCellFormat string = new WritableCellFormat(arial);
            Label label = new Label(column, row, (String) cellvalue, string);
            sheet.addCell(label);
        } else if (celltype.equals("INTEGER")) {
            WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);
            Double value = Double.parseDouble(cellvalue.toString());
            Number number = new Number(column, row, value, integerFormat);
            sheet.addCell(number);
        }

    }

    public void fillSheet(WritableSheet sheet, List<Snp> snps) throws WriteException {
        int row = 0;

        addColumn(sheet, "LABEL", "Position", 0, row);
        addColumn(sheet, "LABEL", "Base", 1, row);
        addColumn(sheet, "LABEL", "Count", 2, row);
        addColumn(sheet, "LABEL", "%", 3, row);
        addColumn(sheet, "LABEL", "% variation at position", 4, row);
        row++;

        for (Snp snp : snps) {
            addColumn(sheet, "INTEGER", snp.getPosition(), 0, row);
            addColumn(sheet, "STRING", snp.getBase(), 1, row);
            addColumn(sheet, "INTEGER", snp.getCount(), 2, row);
            addColumn(sheet, "INTEGER", snp.getPercentage(), 3, row);
            addColumn(sheet, "INTEGER", snp.getVariationPercentag(), 4, row);
            row++;
        }
    }

    public void setSNPs(List<Snp> snps) {
        this.snps = snps;
    }
}
