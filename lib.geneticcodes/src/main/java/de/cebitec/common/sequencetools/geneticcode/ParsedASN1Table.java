package de.cebitec.common.sequencetools.geneticcode;

import java.util.List;

/**
 * @author rhilker
 *
 * Contains a whole parsed ASN1 table.
 */
class ParsedASN1Table {

    private String tableHeader;
    private List<ParsedASN1Entry> entryList;


// Example:
//    name "Standard" ,
//  name "SGC0" ,
//  id 1 ,
//  ncbieaa  "FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
//  sncbieaa "---M---------------M---------------M----------------------------"
//  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG
//  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG
//  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG

    public ParsedASN1Table() {

    }


    public String getTableHeader() {
        return this.tableHeader;
    }

    public List<ParsedASN1Entry> getParsedEntries(){
        return this.entryList;
    }


    public void setTableHeader(String tableHeader) {
        this.tableHeader = tableHeader;
    }

    void setData(List<ParsedASN1Entry> entryList) {
        this.entryList = entryList;
    }



}
