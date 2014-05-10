package de.cebitec.readXplorer.view.dataVisualisation.readPairViewer;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReadPair;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReadPairGroup;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;

/**
 * An extension of JPopupMenu for displaying information regarding a sequence pair.
 * It shows all details of the sequence pair and all mappings belonging to that pair.
 * Furthermore it offers the possibility to jump to a certain mapping by clicking on it.
 * 
 * @author Rolf Hilker
 */
public class ReadPairPopup extends JPopupMenu {
    
    private static final long serialVersionUID = 1L;
    
    private final AbstractViewer parentViewer;
    private final String pairType;
    private final BlockPair block;
    private final ArrayList<Color> pairColors;
    
    /**
     * Displays information regarding a sequence pair in a popup.
     * It shows all details of the sequence pair and all mappings belonging to that pair.
     * Furthermore it offers the possibility to jump to a certain mapping by clicking on it.
     * @param parentViewer the parent viewer
     * @param pairType the type of the sequence pair already in user readable string format
     * @param pairColors 
     * @param block the sequence pair block
     */
    public ReadPairPopup(AbstractViewer parentViewer, String pairType, ArrayList<Color> pairColors, BlockPair block) {
        this.parentViewer = parentViewer;
        this.pairType = pairType;
        this.block = block;
        this.pairColors = pairColors;
        this.initDataAndComponents();
    }
    
    @NbBundle.Messages({"ReadPairDetails=Read Pair Details",
                        "ReadPair=Read Pair",
                        "Type=Type:",
                        "Replicates=Replicates:",
                        "Distance=Distance:"})
    private void initDataAndComponents() {
        PersistantReadPairGroup readPairData = this.getReadPairInfo(); //TODO: get infos from elswhere
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        TitledBorder title = BorderFactory.createTitledBorder(Bundle.ReadPairDetails());
        title.setTitleJustification(TitledBorder.CENTER);
        contentPanel.setBorder(title);
        JScrollPane contentScrollpane = new JScrollPane(contentPanel);
        this.add(contentScrollpane);

        //handle read pair data
        List<PersistantReadPair> readPairs = readPairData.getReadPairs();
        List<PersistantMapping> singleMappings = readPairData.getSingleMappings();
        PersistantReadPair readPair;
        PersistantMapping mapping;
        JPanel readPairMappingInfoPanel = null;

        for (int i = 0; i < readPairs.size(); ++i) {
            JPanel seqPairInfoPanel = new JPanel();
            readPairMappingInfoPanel = new JPanel();
            readPairMappingInfoPanel.setLayout(new BoxLayout(readPairMappingInfoPanel, BoxLayout.Y_AXIS));
            readPairMappingInfoPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            contentPanel.add(seqPairInfoPanel);
            contentPanel.add(readPairMappingInfoPanel);
            
            readPair = readPairs.get(i);
            ReadPairType type = readPair.getReadPairType();
            String hex = Integer.toHexString(PersistantReadPair.determineReadPairColor(type).getRGB());
            hex = hex.substring(2, hex.length());
            
            long distance = Math.abs(readPair.getStop() - readPair.getStart());
            JLabel readPairLabel = new JLabel("<html>".concat(Bundle.ReadPair()).
                    concat(" ").concat(String.valueOf(i + 1)).concat("<br>").
                    concat(Bundle.Type()).
                    concat("</b> <font bgcolor=").concat(hex).concat("> ").
                    concat(type.getTypeString()).concat("</font><br> ").
                    concat(Bundle.Replicates()).
                    concat(" ").concat(String.valueOf(readPair.getReadPairReplicates())).concat("<br> ").
                    concat(Bundle.Distance()).
                    concat(" ").concat(String.valueOf(distance)).concat("</html>"));
            seqPairInfoPanel.add(readPairLabel);

            //handle mappings of pair
            mapping = readPair.getVisibleMapping();
            String mapping1Description = this.getPairMappingString("Mapping1", mapping);
            String mapping2Description = null;
            if (readPair.hasVisibleMapping2()) {
                mapping = readPair.getVisibleMapping2();
                mapping2Description = this.getPairMappingString("Mapping2", mapping);
            }

            //create JList with content for all paired mappings of the pair
            final JList<Object> contentList = new javax.swing.JList<>();
            readPairMappingInfoPanel.add(contentList);
            contentList.setModel(new MappingListModel());
            contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            ArrayList<String> mappingList = new ArrayList<>();
            mappingList.add(mapping1Description);
            if (mapping2Description != null) {
                mappingList.add(mapping2Description);
            }
            ((MappingListModel) contentList.getModel()).setContent(mappingList);
            
            contentList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    int[] selectedRows = contentList.getSelectedIndices();
                    if (selectedRows.length > 0) {
                        int selectedRow = selectedRows[0];
                        String rowEntry = (String) contentList.getModel().getElementAt(selectedRow);
                        int startI = rowEntry.indexOf("Start:") + 8;
                        int stopI = rowEntry.indexOf(",   - Stop");
                        long start = Long.valueOf(rowEntry.substring(startI, stopI));
                        parentViewer.getBoundsInformationManager().navigatorBarUpdated((int) start);
                        contentList.clearSelection();
                    }
                }
            });

        }

        ArrayList<String> singleMappingList = new ArrayList<>();
        for (int i = 0; i < singleMappings.size(); ++i) {
            mapping = singleMappings.get(i);
            singleMappingList.add(this.getSingleMappingString("SingleMapping", i+1, mapping));
        }

        //create JList with content for all single mappings of the pair
        if (!singleMappingList.isEmpty()) {
            
            if (readPairMappingInfoPanel != null){
                JPanel placeholder = new JPanel();
                placeholder.setPreferredSize(new Dimension(6, 6));
                contentPanel.add(placeholder);
            }
            
            JPanel mappingInfoPanel = new JPanel();
            mappingInfoPanel.setLayout(new BoxLayout(mappingInfoPanel, BoxLayout.Y_AXIS));
            mappingInfoPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));//createLoweredBevelBorder());
            contentPanel.add(mappingInfoPanel);
            final JList<Object> contentList = new javax.swing.JList<>();
            mappingInfoPanel.add(contentList);
            contentList.setModel(new MappingListModel());
            ((MappingListModel) contentList.getModel()).setContent(singleMappingList);

            contentList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    int[] selectedRows = contentList.getSelectedIndices();
                    if (selectedRows.length > 0) {
                        int selectedRow = selectedRows[0];
                        String rowEntry = (String) contentList.getModel().getElementAt(selectedRow);
                        int startI = rowEntry.indexOf("Start:") + 8;
                        int stopI = rowEntry.indexOf(",   - Stop");
                        long start = Long.valueOf(rowEntry.substring(startI, stopI));
                        parentViewer.getBoundsInformationManager().navigatorBarUpdated((int) start);
                        contentList.clearSelection();
                    }
                }
            });
            
            if (readPairMappingInfoPanel != null){
                readPairMappingInfoPanel.setPreferredSize(new Dimension(
                        mappingInfoPanel.getPreferredSize().width, readPairMappingInfoPanel.getPreferredSize().height));
            }
        }
        


//        sb.append(createTableRow("Mismatches", String.valueOf(seqPair.getDifferences())));
//        this.appendDiffs(seqPair, sb);
//        this.appendGaps(seqPair, sb);

//        sb.append("List of other mappings with same pair id add database querry");
//        sb.append("</html>")


        //adapt popup window size
        Dimension size = this.getPreferredSize();
        if (size.width < 200) {
            size = new Dimension(200, size.height);
        }
        this.setSize(size);
    }
    
    /**
     * @return all information about this components sequence pair from the DB
     * to show in the popup. If the parent viewer ist not a ReadPairViewer
     * <code>null</code> is returned.
     */
    private PersistantReadPairGroup getReadPairInfo() {
        PersistantReadPairGroup seqPairGroup = null;
        if (parentViewer instanceof ReadPairViewer){
            ReadPairViewer viewer = (ReadPairViewer) this.parentViewer;
            if (viewer.isDbViewer()) {
                ((ReadPairViewer) this.parentViewer).getReadPairInfoFromDB(this.block.getSeqPairId());
            } else {
                seqPairGroup = (PersistantReadPairGroup) this.block.getPersistantObject();
            }
        }
        return seqPairGroup;
    }
    
    
    /**
     * Formats a string containing information for a pair mapping
     * @param bundleString bundle string to retrieve mapping identifier from Bundle.properties
     * @param type mapping type string
     * @param mapping pair mapping itself for start and stop positions
     * @return correctly formatted String.
     */
    private String getPairMappingString(String bundleString, PersistantMapping mapping){
        return NbBundle.getMessage(BlockComponentPair.class, bundleString).concat("   - ").
                    concat(this.getMappingInfos(mapping));
    }
    
    /**
     * Formats a string containing information for a single mapping
     * @param bundleString bundle string to retrieve mapping identifier from Bundle.properties
     * @param type mapping type string
     * @param mapping single mapping itself for start and stop positions
     * @return correctly formatted String.
     */
    private String getSingleMappingString(String bundleString, int count, PersistantMapping mapping){
        return NbBundle.getMessage(BlockComponentPair.class, bundleString).concat(" ").
                    concat(String.valueOf(count)).concat(",   ").
                    concat(this.getMappingInfos(mapping));
    }
    
    private String getMappingInfos(PersistantMapping mapping){
        String strand = mapping.isFwdStrand() ? 
                    NbBundle.getMessage(BlockComponentPair.class, "Fwd") : 
                    NbBundle.getMessage(BlockComponentPair.class, "Rev");
        return NbBundle.getMessage(BlockComponentPair.class, "Type").concat(" ").
                    concat(this.getMappingTypeString(mapping)).
                    concat(NbBundle.getMessage(BlockComponentPair.class, "Start")).concat(" ").
                    concat(String.valueOf(mapping.getStart())).
                    concat(NbBundle.getMessage(BlockComponentPair.class, "Stop")).concat(" ").
                    concat(String.valueOf(mapping.getStop())).
                    concat(NbBundle.getMessage(BlockComponentPair.class, "Orientation")).concat(" ").
                    concat(strand);
    }
    
    /**
     * @param mapping whose type should be determined
     * @return the type of the mapping (Perfect Match, Best Match or Common Match)
     */
    private String getMappingTypeString(PersistantMapping mapping) {
        String mappingType;
        if (mapping.getDifferences() == 0) {
            mappingType = "Perfect Match";
        } else if (mapping.isBestMatch()) {
            mappingType = "Best Match";
        } else {
            mappingType = "Common Match";
        }
        return mappingType;
    }
    
    /**
     * Adds a setContent method to the functionality of the AbstractListModel,
     * which is the displayable content of the ListModel.
     */
    private class MappingListModel extends AbstractListModel<Object> {
        private static final long serialVersionUID = 1L;

        String content[] = new String[1];
        
        public void setContent(ArrayList<String> content) {
            this.content = content.toArray(this.content);
        }

        @Override
        public int getSize() {
            return content.length;
        }

        @Override
        public Object getElementAt(int i) {
            return content[i];
        }
    }
    
}
