package de.cebitec.readXplorer.parser.reference;

import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.util.Observer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.utils.ParserException;


/**
 * Parser to fetch all available sequence identifiers from a GFF 2 file.
 * 
 * @author marie-theres, rhilker
 */
public class BioJavaGff2IdParser implements IdParserI {
    
    
    private List<Observer> observers = new ArrayList<>();
    private List<String> seqIds;
    
    /**
     * Fetches all available sequence identifiers from a GFF 2 file.
     * @param gff2File the GFF 2 file to read from
     * @return the list of sequence identifiers contained in the given file
     * @throws ParsingException
     */
    @Override
    public List<String> getSequenceIds(File gff2File) throws ParsingException {
        seqIds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(gff2File))) {

            GFFParser gff2Parser = new GFFParser();
            GFFHandler handler = new GFFHandler();

            gff2Parser.parse(reader, handler);

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

    public class GFFHandler implements GFFDocumentHandler {

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
        public void recordLine(GFFRecord gffr) {
            if (!seqIds.contains(gffr.getSeqName())) {
                seqIds.add(gffr.getSeqName());
            }
        }
    }
}




