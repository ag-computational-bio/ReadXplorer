package de.cebitec.readXplorer.differentialExpression;

/**
 *
 * @author kstaderm
 */
public class Group {
    
    private Integer[] integerRepresentation;
    private final int id;
    private String stringRepresentation;
    
    private static int nextUnusedID = 0;
    
    public Group(final Integer[] integerRepresentation, final String stringRepresentation){
        id=nextUnusedID++;
        this.integerRepresentation=integerRepresentation;
        this.stringRepresentation=stringRepresentation;
    }

    public int getId() {
        return id;
    }
    
    public int getGnuRID() {
        return (id+1);
    }

    public Integer[] getIntegerRepresentation() {
        return integerRepresentation;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
