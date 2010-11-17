
package vamp.view.dataVisualisation.readPosition;

/**
 *
 * @author jstraube
 */
public class Read {
    private String readname;
    private int position;
    private int errors;
    private int isBestMapping;

    public Read(String readname, int position, int errors, int isBestMapping){
        this.readname = readname;
        this.position = position;
        this.errors = errors;
        this.isBestMapping = isBestMapping;
    }

    public String getReadname() {
        return readname;
    }

    public int getErrors() {
        return errors;
    }

    public int getPosition() {
        return position;
    }

    public int getisBestMapping() {
        return isBestMapping;
    }

    @Override
    public String toString(){
        return "read: "+readname+"\tposition: "+position+"\terrors: "+errors+"%\tis best mapping.: "+isBestMapping;
    }

}
