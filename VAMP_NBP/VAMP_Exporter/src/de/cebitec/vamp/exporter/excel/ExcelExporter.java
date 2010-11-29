package de.cebitec.vamp.exporter.excel;

import de.cebitec.vamp.exporter.ExporterI;
import de.cebitec.vamp.objects.Snp;
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

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting to write Excel file...{0}", tempFile.getAbsolutePath());

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
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error writing file{0}", ex.getMessage());
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
