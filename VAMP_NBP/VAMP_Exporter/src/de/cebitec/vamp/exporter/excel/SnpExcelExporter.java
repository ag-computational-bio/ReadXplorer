package de.cebitec.vamp.exporter.excel;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.vamp.databackend.dataObjects.CodonSnp;
import de.cebitec.vamp.exporter.ExporterI;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.databackend.dataObjects.SnpData;
import de.cebitec.vamp.databackend.dataObjects.SnpI;
import de.cebitec.vamp.util.SequenceComparison;
import jxl.write.Label;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.NumberFormats;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.openide.util.NbBundle;

/**
 * @author jstraube, rhilker
 * 
 * Allows to export SnpData into an excel sheet.
 */
public class SnpExcelExporter implements ExporterI {

    private SnpData snpData;

    @Override
    public boolean readyToExport() {
        if (snpData.getSnpList().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public File writeFile(File tempDir, String name) throws FileNotFoundException, IOException {
        File tempFile = new File(tempDir + "\\" + name);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting to write Excel file...{0}", tempFile.getAbsolutePath());

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = Workbook.createWorkbook(tempFile, wbSettings);

        WritableSheet snpSheet = null;
        int currentPage = 0;
        if (!snpData.getSnpList().isEmpty()) {
            snpSheet = workbook.createSheet("SNPs", currentPage++);
        }
        try {
            if (snpSheet != null) {
                fillSheet(snpSheet, snpData);
            }
            workbook.write();
            workbook.close();

            String msg = NbBundle.getMessage(SnpExcelExporter.class, "SnpExcelExporter.SuccessMsg", "SNP data successfully stored!");
            String header = NbBundle.getMessage(SnpExcelExporter.class, "SnpExcelExporter.SuccessHeader", "Information Message");
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.INFORMATION_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished writing Excel file!");
        } catch (WriteException ex) {
            String msg = NbBundle.getMessage(SnpExcelExporter.class, "SnpExcelExporter.FailMsg", 
                    "A write error occured during saving progress!");
            String header = NbBundle.getMessage(SnpExcelExporter.class, "SnpExcelExporter.FailHeader", "Failure");
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
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
            cellvalue = cellvalue instanceof Character ? String.valueOf(cellvalue) : cellvalue;
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

    public void fillSheet(WritableSheet sheet, SnpData snpData) throws WriteException {
        int row = 0;

        addColumn(sheet, "LABEL", "Position", 0, row);
        addColumn(sheet, "LABEL", "Track", 1, row);
        addColumn(sheet, "LABEL", "Base", 2, row);
        addColumn(sheet, "LABEL", "Reference", 3, row);
        addColumn(sheet, "LABEL", "A", 4 , row);
        addColumn(sheet, "LABEL", "C", 5 , row);
        addColumn(sheet, "LABEL", "G", 6 , row);
        addColumn(sheet, "LABEL", "T", 7 , row);
        addColumn(sheet, "LABEL", "N", 8 , row);
        addColumn(sheet, "LABEL", "_", 9 , row);
        addColumn(sheet, "LABEL", "Coverage", 10 , row);
        addColumn(sheet, "LABEL", "Frequency", 11 , row);
        addColumn(sheet, "LABEL", "Type", 12 , row);
        addColumn(sheet, "LABEL", "Amino Snp", 13 , row);
        addColumn(sheet, "LABEL", "Amino Ref", 14 , row);
        addColumn(sheet, "LABLE", "Effect on AA", 15, row);
        addColumn(sheet, "LABEL", "Features (Genes)", 16 , row);
        row++;

        List<SnpI> snps = snpData.getSnpList();
        Map<Integer,String> trackNames = snpData.getTrackNames();
        
        for (SnpI snpi : snps) {
            
            Snp snp = (Snp) snpi;
            
            addColumn(sheet, "STRING", snp.getPosition(), 0, row);
            addColumn(sheet, "STRING", trackNames.get(snp.getTrackId()), 1, row);
            addColumn(sheet, "STRING", snp.getBase().toUpperCase(), 2, row);
            addColumn(sheet, "STRING", snp.getRefBase().toUpperCase(), 3, row);
            addColumn(sheet, "INTEGER", snp.getARate(), 4, row);
            addColumn(sheet, "INTEGER", snp.getCRate(), 5, row);
            addColumn(sheet, "INTEGER", snp.getGRate(), 6, row);
            addColumn(sheet, "INTEGER", snp.getTRate(), 7, row);
            addColumn(sheet, "INTEGER", snp.getNRate(), 8, row);
            addColumn(sheet, "INTEGER", snp.getGapRate(), 9, row);
            addColumn(sheet, "INTEGER", snp.getCoverage(), 10, row);
            addColumn(sheet, "INTEGER", snp.getFrequency(), 11, row);
            addColumn(sheet, "STRING", snp.getType().getType(), 12, row);
            
            List<CodonSnp> codons = snp.getCodons();
            String noGene = "No gene";
            String aminoAcidsSnp = "";
            String aminoAcidsRef = "";
            if (codons.isEmpty()){
                aminoAcidsSnp = noGene;
                aminoAcidsRef = noGene;
            }
            String effect = "";
            String geneId = "";
            char aminoSnp;
            char aminoRef;
            for (CodonSnp codon : codons) {
                aminoSnp = codon.getAminoSnp();
                aminoRef = codon.getAminoRef();
                aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA(aminoSnp) + ")\n";
                aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA(aminoRef) + ")\n";
                effect += codon.getEffect().getType() + "\n";
                geneId += codon.getGeneId() + "\n";
            }
            
            //determine effect on amino acid sequence in case its not a substitution
            if (!aminoAcidsSnp.equals(noGene) && effect.equals("")) { //only if there is at least on gene here
                effect += snp.getType().getType(); //it will be identical to type in this case
            }
            
            addColumn(sheet, "STRING", aminoAcidsSnp, 13, row);
            addColumn(sheet, "STRING", aminoAcidsRef, 14, row);
            addColumn(sheet, "STRING", effect, 15, row);
            addColumn(sheet, "STRING", geneId, 16, row);
            row++;
        }
    }

    public void setSNPs(SnpData snpData) {
        this.snpData = snpData;
    }
}
