/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 *
 * @author MKD
 */
public class PutativOperon {

    private PersistantFeature genFeature1;
    private PersistantFeature genFeature2;
    private int read_cover_Gen1;
    private int read_cover_Gen1_and_Gen2;
    private int read_cover_Gen2;
    private int read_cover_none;
    public PutativOperon(PersistantFeature genFeature1, PersistantFeature genFeature2) {
        this.genFeature1 = genFeature1;
        this.genFeature2 = genFeature2;
    }

    /**
     * @return the genFeature1
     */
    public PersistantFeature getGenFeature1() {
        return genFeature1;
    }

    /**
     * @return the genFeature2
     */
    public PersistantFeature getGenFeature2() {
        return genFeature2;
    }

    /**
     * @return the read_cover_Gen1
     */
    public int getRead_cover_Gen1() {
        return read_cover_Gen1;
    }

    /**
     * @param read_cover_Gen1 the read_cover_Gen1 to set
     */
    public void setRead_cover_Gen1(int read_cover_Gen1) {
        this.read_cover_Gen1 = read_cover_Gen1;
    }

    /**
     * @return the read_cover_Gen1_and_Gen2
     */
    public int getRead_cover_Gen1_and_Gen2() {
        return read_cover_Gen1_and_Gen2;
    }

    /**
     * @param read_cover_Gen1_and_Gen2 the read_cover_Gen1_and_Gen2 to set
     */
    public void setRead_cover_Gen1_and_Gen2(int read_cover_Gen1_and_Gen2) {
        this.read_cover_Gen1_and_Gen2 = read_cover_Gen1_and_Gen2;
    }

    /**
     * @return the read_cover_Gen2
     */
    public int getRead_cover_Gen2() {
        return read_cover_Gen2;
    }

    /**
     * @param read_cover_Gen2 the read_cover_Gen2 to set
     */
    public void setRead_cover_Gen2(int read_cover_Gen2) {
        this.read_cover_Gen2 = read_cover_Gen2;
    }

    /**
     * @return the read_cover_none
     */
    public int getRead_cover_none() {
        return read_cover_none;
    }

    /**
     * @param read_cover_none the read_cover_none to set
     */
    public void setRead_cover_none(int read_cover_none) {
        this.read_cover_none = read_cover_none;
    }
}
