package de.cebitec.vamp.view.dialogMenus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * A standard JPanel with functionality to handle multiple files, a list of 
 * mapping files and manage a used directory.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FileSelectionPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private boolean useMultipleImport = false;
    private List<File> mappingFiles = new ArrayList<>();

    /**
     * A standard JPanel with functionality to handle multiple files
     * and manage a used directory.
     */
    public FileSelectionPanel() {
    }
    
    /**
     * Adds a single file to the list of mapping files.
     * @param file the file to add to the list
     * @param mappingFileField the field, which should display the file name
     */
    public void addFile(File file, JTextField mappingFileField) {
        if (file.canRead()) {
            addMappingFile(file);
            mappingFileField.setText(file.getAbsolutePath());
        } else {
            Logger.getLogger(ImportTrackBasePanel.class.getName()).log(Level.WARNING, "Couldn't read file");
        }
    }
    
    /**
     * @return true, if multiple files arre imported at once, false otherwise
     */
    public boolean useMultipleImport() {
        return useMultipleImport;
    }

    /**
     * Sets if mutliple tracks should be imported at once
     *
     * @param useMultipleImport true, if multiple files arre imported at once,
     * false otherwise
     */
    protected void setUseMultipleImport(boolean useMultipleImport) {
        this.useMultipleImport = useMultipleImport;
    }

    /**
     * @return The single mapping file to import
     */
    public File getMappingFile() {
        if (getMappingFiles().isEmpty()) {
            return null;
        } else {
            return getMappingFiles().get(0);
        }
    }

    /**
     * @return The complete list of mapping files1 to import for multiple data
     * set import with the same parameters at once.
     */
    public List<File> getMappingFiles() {
        return mappingFiles;
    }

    /**
     * Sets the mapping files for multiple track import at once.
     *
     * @param mappingFiles The list of mapping files to import at once
     */
    protected void setMappingFiles(List<File> mappingFiles) {
        this.mappingFiles = mappingFiles;
    }

    /**
     * Adds a single mapping file to the list.
     *
     * @param mappingFile The mapping file to add to the list
     */
    protected void addMappingFile(File mappingFile) {
        this.mappingFiles.add(mappingFile);
    }

    /**
     * Fills the table showing all tracks selected for a multiple track at once
     * import.
     * @param model the model to which the data should be added
     * @param mappingFiles the files whose names should be appended to the model
     * @param title the title above the file names to add
     */
    protected void fillMultipleImportTable(DefaultListModel<String> model, List<File> mappingFiles, String title) {
        model.addElement(title);
        for (File mappingFile : mappingFiles) {
            model.addElement(mappingFile.getName());
        }
        if (mappingFiles.isEmpty()) {
            model.addElement("-");
        }
    }
    
    /**
     * Updates the gui for multiple or single file handling.
     * @param multipleFileCheckBox the checkbox defining whether multiple file
     * handling is enabled
     * @param multiTrackScrollPane scrollpane to display multiple files
     * @param multiTrackList list which actually displays the multiple files on
     * the scrollpane
     * @param multiTrackListLabel the label which should only be visible for
     * multiple file handling
     * @param fileTextField text field displaying either the file path for
     * single files or the count of files to handle for multiple file handling
     */
    public void updateGuiForMultipleFiles(JCheckBox multipleFileCheckBox, JScrollPane multiTrackScrollPane, JList<String> multiTrackList,
            JLabel multiTrackListLabel, JTextField fileTextField) {
        this.setUseMultipleImport(multipleFileCheckBox.isSelected());
        if (this.useMultipleImport()) {
            multiTrackScrollPane.setVisible(true);
            multiTrackList.setVisible(true);
            multiTrackListLabel.setVisible(true);
            fileTextField.setText(getMappingFiles().size() + " tracks to import");
            DefaultListModel<String> model = new DefaultListModel<>();
            fillMultipleImportTable(model, getMappingFiles(), "Mapping file list:");
            multiTrackList.setModel(model);
        } else {
            multiTrackScrollPane.setVisible(false);
            multiTrackList.setVisible(false);
            multiTrackListLabel.setVisible(false);
            fileTextField.setText("");
            getMappingFiles().clear();
            multiTrackList.setModel(new DefaultListModel<String>());
        }
    }
}
