package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff3.GFF3DocumentHandler;
import org.biojava.bio.program.gff3.GFF3Parser;
import org.biojava.bio.program.gff3.GFF3Record;
import org.biojava.ontology.Ontology;
import org.biojava.utils.ParserException;

/**
 * Parser to fetch all available sequence identifiers from a GFF 3 file.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class BioJavaGff3IdParser implements IdParserI {
    
    private ArrayList<Observer> observers = new ArrayList<>();
    private List<String> seqIds;

    /**
     * Fetches all available sequence identifiers from a GFF 3 file.
     * @param gff3File the GFF 3 file to read from
     * @return the list of sequence identifiers contained in the given file
     * @throws ParsingException 
     */
    @Override
    public List<String> getSequenceIds(final File gff3File) throws ParsingException {
        seqIds = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(gff3File))) {
            
            GFF3Parser gff3Parser = new GFF3Parser();
            GFF3Handler handler = new GFF3Handler();
            gff3Parser.parse(reader, handler, new Ontology.Impl("Ontologyname", "name of ontology"));
            
        } catch (IOException | BioException | ParserException ex) {
            this.notifyObservers(ex);
        }

        return seqIds;
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }
    
    private class GFF3Handler implements GFF3DocumentHandler {

        @Override
        public void startDocument(String string) {
        }

        @Override
        public void endDocument() {
        }

        @Override
        public void commentLine(String string) {
        }

        @Override
        public void recordLine(GFF3Record gffr) {
            if (!seqIds.contains(gffr.getSequenceID())) {
                seqIds.add(gffr.getSequenceID());
            }
        }
    }
}