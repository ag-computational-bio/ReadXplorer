/*
 * NewTrackDialogPanel.java
 *
 * Created on 13.01.2011, 15:18:28
 */
package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.mappings.SAMBAMParser;
import de.cebitec.vamp.parser.mappings.JokParser;
import de.cebitec.vamp.parser.mappings.MappingParserI;
import de.cebitec.vamp.parser.mappings.SamBamStepParser;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import org.openide.util.NbBundle;

/**
 *
 * @author jwinneba
 */
public class NewTrackDialogPanel extends javax.swing.JPanel implements NewJobDialogI {

    private static final long serialVersionUID = 774275254;
    private File mappingFile;
    private ReferenceJob[] refGenJobs;
    private MappingParserI[] parsers = new MappingParserI[]{new JokParser(), new SAMBAMParser(), new SamBamStepParser()};
    private MappingParserI currentParser;
    private int stepSize = 0;
    private static final int maxVal = 1000000000;
    private static final int minVal = 10000;
    private static final int step = 1000;
    private static final int defaultVal = 300000;

    /** Creates new form NewTrackDialogPanel */
    public NewTrackDialogPanel() {
        refGenJobs = this.getRefGenJobs();
        initComponents();
        // choose the default parser. first entry is shown in combobox by default
        this.currentParser = this.parsers[0];
        this.setStepwiseField(false);
        setJSpinner();

    }

    @Override
    public boolean isRequiredInfoSet() {
        if (mappingFile == null || refGenBox.getSelectedItem() == null || descriptionField.getText().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public File getMappingFile() {
        return mappingFile;
    }

    public String getDescription() {
        return descriptionField.getText();
    }

    public ReferenceJob getReferenceJob() {
        return (ReferenceJob) refGenBox.getSelectedItem();
    }

    public MappingParserI getParser() {
        return currentParser;
    }

    public boolean isFileSorted() {
        return fileSorted.isSelected();
    }

    public Integer getstepSize() {
        stepSize = (Integer) stepSizeSpinner.getValue();

        return stepSize;
    }

    private void setJSpinner() {
        stepSizeSpinner.setModel(new SpinnerNumberModel(defaultVal, minVal, maxVal, step));
        JFormattedTextField txt = ((JSpinner.NumberEditor) stepSizeSpinner.getEditor()).getTextField();
        ((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);

    }

    private void setStepwiseField(boolean setFields) {
        stepSizeLabel.setVisible(setFields);
        stepSizeSpinner.setVisible(setFields);
        fileSorted.setVisible(setFields);
    }

    public void setReferenceJobs(List<ReferenceJob> jobs) {
        List<ReferenceJob> list = new ArrayList<ReferenceJob>();

        try {
            List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
            for (Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext();) {
                PersistantReference r = it.next();
                list.add(new ReferenceJob(r.getId(), null, null, r.getDescription(), r.getName(), r.getTimeStamp()));
            }
        } catch (OutOfMemoryError e) {
            String msg = NbBundle.getMessage(NewPositionTableDialog.class, "OOM_Message",
                    "An out of memory error occured during fetching the references. Please restart the software with more memory.");
            String title = NbBundle.getMessage(NewPositionTableDialog.class, "OOM_Header", "Restart Software");
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

        list.addAll(jobs);

        ReferenceJob[] gens = new ReferenceJob[list.size()];
        for (int i = 0; i < list.size(); i++) {
            gens[i] = list.get(i);
        }

        refGenBox.setModel(new DefaultComboBoxModel(gens));
    }

    private ReferenceJob[] getRefGenJobs() {
        List<ReferenceJob> list = new ArrayList<ReferenceJob>();
        
        try {
            List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
            for (Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext();) {
                PersistantReference r = it.next();
                list.add(new ReferenceJob(r.getId(), null, null, r.getDescription(), r.getName(), r.getTimeStamp()));
            }
        } catch (OutOfMemoryError e) {
            String msg = NbBundle.getMessage(NewPositionTableDialog.class, "OOM_Message",
                    "An out of memory error occured during fetching the references. Please restart the software with more memory.");
            String title = NbBundle.getMessage(NewPositionTableDialog.class, "OOM_Header", "Restart Software");
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

        ReferenceJob[] gens = new ReferenceJob[list.size()];
        for (int i = 0; i < list.size(); i++) {
            gens[i] = list.get(i);
        }

        return gens;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        mappingFileLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        refGenLabel = new javax.swing.JLabel();
        refGenBox = new javax.swing.JComboBox(refGenJobs);
        mappingFileField = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();
        descriptionField = new javax.swing.JTextField();
        outputTypeCombo = new javax.swing.JComboBox(parsers);
        stepSizeLabel = new javax.swing.JLabel();
        stepSizeSpinner = new javax.swing.JSpinner();
        fileSorted = new javax.swing.JCheckBox();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.jLabel1.text")); // NOI18N

        mappingFileLabel.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.mappingFileLabel.text")); // NOI18N

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.descriptionLabel.text")); // NOI18N

        refGenLabel.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.refGenLabel.text")); // NOI18N

        mappingFileField.setEditable(false);
        mappingFileField.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.mappingFileField.text")); // NOI18N

        chooseButton.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.chooseButton.text")); // NOI18N
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });

        outputTypeCombo.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                if(value instanceof ParserI){
                    return super.getListCellRendererComponent(list, ((ParserI) value).getParserName(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        outputTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputTypeComboActionPerformed(evt);
            }
        });

        stepSizeLabel.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.stepSizeLabel.text")); // NOI18N

        fileSorted.setSelected(true);
        fileSorted.setText(org.openide.util.NbBundle.getMessage(NewTrackDialogPanel.class, "NewTrackDialogPanel.fileSorted.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refGenLabel))
                    .addComponent(stepSizeLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(mappingFileLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputTypeCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 311, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mappingFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chooseButton))
                    .addComponent(descriptionField, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                    .addComponent(refGenBox, 0, 311, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stepSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileSorted)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mappingFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseButton)
                    .addComponent(mappingFileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptionLabel))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stepSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileSorted)
                    .addComponent(stepSizeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refGenBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refGenLabel))
                .addContainerGap(43, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(currentParser.getInputFileDescription(), currentParser.getFileExtensions()));
        Preferences prefs2 = Preferences.userNodeForPackage(NewReferenceDialogPanel.class);

        String path = prefs2.get("RefGenome.Filepath", null);
        if (path != null) {
            fc.setCurrentDirectory(new File(path));
        }
        int result = fc.showOpenDialog(this);

        File file = null;

        if (result == 0) {
            // file chosen
            file = fc.getSelectedFile();

            if (file.canRead()) {
                mappingFile = file;
                mappingFileField.setText(mappingFile.getAbsolutePath());
                descriptionField.setText(mappingFile.getName());
                Preferences prefs = Preferences.userNodeForPackage(NewReferenceDialogPanel.class);
                prefs.put("RefGenome.Filepath", mappingFile.getAbsolutePath());
                try {
                    prefs.flush();
                } catch (BackingStoreException ex) {
                    Logger.getLogger(NewTrackDialogPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(NewTrackDialogPanel.class.getName()).log(Level.WARNING, "Couldnt read file");
            }
        }
}//GEN-LAST:event_chooseButtonActionPerformed

    private void outputTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputTypeComboActionPerformed
        MappingParserI newparser = (MappingParserI) this.outputTypeCombo.getSelectedItem();
        if (this.currentParser != newparser) {
            this.currentParser = newparser;
            this.mappingFile = null;
            this.mappingFileField.setText("");
            this.descriptionField.setText("");
            this.setStepwiseField(false);
            if (newparser instanceof SamBamStepParser) {
                this.setStepwiseField(true);
                this.fileSorted.requestFocus();
                JOptionPane.showMessageDialog(this,
                        "Please make sure that your file is sorted by read sequence. \n If not, deselect the checkbox!",
                        "Stepwise parser",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
}//GEN-LAST:event_outputTypeComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseButton;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JCheckBox fileSorted;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField mappingFileField;
    private javax.swing.JLabel mappingFileLabel;
    private javax.swing.JComboBox outputTypeCombo;
    private javax.swing.JComboBox refGenBox;
    private javax.swing.JLabel refGenLabel;
    private javax.swing.JLabel stepSizeLabel;
    private javax.swing.JSpinner stepSizeSpinner;
    // End of variables declaration//GEN-END:variables
}
