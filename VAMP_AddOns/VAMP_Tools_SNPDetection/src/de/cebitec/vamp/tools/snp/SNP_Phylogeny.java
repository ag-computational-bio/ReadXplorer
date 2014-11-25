package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.databackend.dataObjects.SnpI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author jhess
 */
public class SNP_Phylogeny {
    
    public static String FDNAML_PATH = "PathFdnaml";

    InputOutput io;
    SnpDetectionResult snpData;
    HashMap<Integer, HashMap<String, Snp>> sortedSnps;

    public SNP_Phylogeny(SnpDetectionResult snpData) {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(SNP_Phylogeny.class, "SNP_Phylogeny.output.name"), false);
        this.snpData = snpData;
        createAlignment(snpData);
    }

    private void createAlignment(SnpDetectionResult snpData) {

        int numberOfTracks;
        HashMap<Long, HashMap<Integer, String>> bases = new HashMap<>();
        HashMap<Long, String> refBases = new HashMap<>();
        List<SnpI> snps = snpData.getSnpList();
        Map<Integer, PersistantTrack> trackNames = snpData.getTrackMap();
        List<Integer> trackIdsWithSnps = new ArrayList<>();

        for (SnpI snpi : snps) {

            Snp snp = (Snp) snpi;
            
            // get numberOfTracks (with snpData)
            if (!trackIdsWithSnps.contains(snp.getTrackId())) {
                trackIdsWithSnps.add(snp.getTrackId());
            }
//            if (snp.getTrackId() > numberOfTracks) {
//                numberOfTracks = snp.getTrackId();
//            }

            String positionString = snp.getPosition();
            if (positionString.contains("_")) {
                positionString = positionString.substring(0, positionString.length() - 2);
            }

            Long position = Long.parseLong(positionString);

            // save reference base
            if (!refBases.containsKey(position)) {
                refBases.put(position, snp.getRefBase().toUpperCase());
            }

            // save bases per track in a hashmap again hashed to their position
            if (!bases.containsKey(position)) {
                HashMap<Integer, String> track = new HashMap<>();
                bases.put(position, track);
            }
            HashMap<Integer, String> track = bases.get(position);
            track.put(snp.getTrackId(), snp.getBase());
        }

        numberOfTracks = trackIdsWithSnps.size();


        // add reference base to all position maps, where it is missing for at least one track
        Iterator<Entry<Long, HashMap<Integer, String>>> positionIterator = bases.entrySet().iterator();
        while (positionIterator.hasNext()) {

            Map.Entry<Long, HashMap<Integer, String>> posToBaseMap = positionIterator.next();
            HashMap<Integer, String> positionEntry = posToBaseMap.getValue();
            // fill positions without snpData with reference base

            for (int trackId : trackIdsWithSnps) {
                if (!positionEntry.containsKey(trackId)) {
                    positionEntry.put(trackId, String.valueOf(refBases.get(posToBaseMap.getKey())));
                }
            }
        }

        HashMap<Integer, Integer> trackIdToIndex = this.getTrackIdToIndexMap(trackIdsWithSnps);
        String[] alignment = new String[numberOfTracks + 1];
        for (Long l : new TreeSet<>(bases.keySet())) {
            HashMap<Integer, String> snpsAtPosMap = bases.get(l);
            for (Integer trackId : new TreeSet<>(snpsAtPosMap.keySet())) {
                if (alignment[trackIdToIndex.get(trackId)] == null) {
                    alignment[trackIdToIndex.get(trackId)] = ">" + trackNames.get(trackId).getDescription() + System.getProperty("line.separator");
                }
                alignment[trackIdToIndex.get(trackId)] += snpsAtPosMap.get(trackId);
            }
        }

        File alignmentFile = this.createMultipleFastaFile(alignment);
        this.runFdnaml(alignmentFile);

    }

    /**
     * Creates the multiple .fasta file for input in fdnaml
     * @param alignment alignment of snpData to write as multiple fasta
     */
    private File createMultipleFastaFile(String[] alignment) {

        File file;
        FileWriter writer;

        file = new File("./alignment.fasta"); //todo: delete again?
        if (file.exists()) {
            file.delete();
        }
        try {
            // new FileWriter(file) - falls die Datei bereits existiert
            // wird diese überschrieben
            writer = new FileWriter(file, true);
//            int track = 0;
            for (String s : alignment) {
                if (s != null) {
//                    writer.write(">" + track);
//                    writer.write(System.getProperty("line.separator"));
//                    track++;

                    writer.write(s);
                    writer.write(System.getProperty("line.separator"));
                }
            }
            writer.write(System.getProperty("line.separator"));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JPanel(), NbBundle.getMessage(SNP_Phylogeny.class, "SNP_Phylogeny.ERROR.Write_Alignment"),
                    NbBundle.getMessage(SNP_Phylogeny.class, "SNP_Phylogeny.ERROR.Write_File"), JOptionPane.ERROR_MESSAGE);
        }

        return file;
    }

    /**
     * Runs fdnaml to generate the phylogenetic tree from the alignment file
     * @param alignmentFile multiple fasta file to generate phylogenetic tree
     */
    private void runFdnaml(File alignmentFile) {
        // start fdnaml to generate the tree
        try {
            String fdnamlPath = NbPreferences.forModule(SNP_Phylogeny.class).get(SNP_Phylogeny.FDNAML_PATH, "/vol/emboss-6.2/bin/fdnaml");
            ProcessBuilder fdnaml = new ProcessBuilder(fdnamlPath, alignmentFile.toString());

            /*
             * TODO: find tool that prints rooted phylogenetic trees
             *
             * aminosäuren: sauer A, L alipathisch, M Amid-Gruppe, R aromatisch, C basisch, H Hydroxyl-Gruppe,
             * I Imino-Gruppe, S schwefelhaltig
             * 
             * Service Name:	{http://soaplab.org/phylogeny_molecular_sequence/fdnaml.sa}fdnaml.saService
            Port Name:	{http://soaplab.org/phylogeny_molecular_sequence/fdnaml.sa}fdnaml.saPort
            
            Address:	http://www.ebi.ac.uk:80/soaplab/typed/services/phylogeny_molecular_sequence.fdnaml.sa
            WSDL:	http://www.ebi.ac.uk:80/soaplab/typed/services/phylogeny_molecular_sequence.fdnaml.sa?wsdl
            Implementation class:	org.soaplab.typedws.ServiceProvider
             */

            Process fdnamlProcess = fdnaml.start();
            OutputStreamWriter output = new OutputStreamWriter(fdnamlProcess.getOutputStream());
            PrintWriter printOutput = new PrintWriter(output);
            printOutput.println("\n");
            output.close();
            output = new OutputStreamWriter(fdnamlProcess.getOutputStream());
            printOutput = new PrintWriter(output);
            printOutput.println("./alignment.fdnaml");
            output.close();
            //Runtime.getRuntime().exec("/vol/emboss-6.2/bin/fdnaml -sequence " + file + " - outfile " + file + ".fdnaml");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JPanel(), NbBundle.getMessage(SNP_Phylogeny.class, "SNP_Phylogeny.ERROR.Fdnaml"),
                    NbBundle.getMessage(SNP_Phylogeny.class, "SNP_Phylogeny.ERROR.Software"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a mapping from each track id to a corresponding index.
     * @param trackIdList list of used trackIds
     * @return mapping from each track id to a corresponding index.
     */
    private HashMap<Integer, Integer> getTrackIdToIndexMap(List<Integer> trackIdList) {
        HashMap<Integer, Integer> trackIdToIndexMap = new HashMap<>();
        int i = 0;
        for (int trackId : trackIdList) {
            trackIdToIndexMap.put(trackId, i++);
        }
        return trackIdToIndexMap;
    }
}
