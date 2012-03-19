package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.parser.reference.Filter.AnnotationFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedReference {

    private ArrayList<ParsedAnnotation> annotations;
    private String sequence;
    private String description;
    private String name;
    private AnnotationFilter filter;
    private Timestamp timestamp;
    private int id;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ParsedReference(){
        annotations = new ArrayList<ParsedAnnotation>();
        filter = new AnnotationFilter();
    }

    public void setAnnotationFilter(AnnotationFilter filter){
        this.filter = filter;
    }

    public AnnotationFilter getAnnotationFilter(){
        return filter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getLength(){
        return sequence.length();
    }

    public void addAnnotation(ParsedAnnotation annotation){
        // only add valid annotations according to specified filterrules
        if(filter.isValidAnnotation(annotation)){
            annotations.add(annotation);
        }
    }

    public List<ParsedAnnotation> getAnnotations(){
        return annotations;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID(){
        return id;
    }

    /**
     * Adds sub annotations to their parent annotations. Should only be called after all
     * regular annotations have been imported. If a parent annotation cannot cannot be found 
     * the sub annotation becomes a regular annotation. So they all should contain as much 
     * information as a regular annotation, if possible.
     * @param sub annotations the sub annotations to add to their parent annotation for this genome.
     */
    public void addSubAnnotations(List<ParsedAnnotation> subAnnotations) {
        int lastIndex = 0;
        boolean added = false;
        for (ParsedAnnotation subAnnotation : subAnnotations){
            //since the annotations are sorted in this.annotations we can do this in linear time
            for (int i = lastIndex; i<this.annotations.size(); ++i){
                ParsedAnnotation annotation = this.annotations.get(i);
                if (annotation.getStrand() == subAnnotation.getStrand() && annotation.getStart() <= subAnnotation.getStart()
                        && annotation.getStop() >= subAnnotation.getStop()) {
                    annotation.addSubAnnotation(new ParsedSubAnnotation(subAnnotation.getStart(), subAnnotation.getStop(), subAnnotation.getType()));
                    added = true;
                    lastIndex = i == 0 ? 0 : i-1;
                    break;
                }
            }
            if (!added){ //if there is no parent annotation for the sub annotation it becomes an ordinary annotation
                this.annotations.add(subAnnotation);
            }
        }
    }

}
