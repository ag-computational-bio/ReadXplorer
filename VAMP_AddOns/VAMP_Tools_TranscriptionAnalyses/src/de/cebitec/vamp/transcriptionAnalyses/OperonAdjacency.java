package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author MKD
 */
public class OperonAdjacency {

    private PersistantFeature operonFeature1;
    private PersistantFeature operonFeature2;
    private int read_cover_Gen1 = 0;
    private int read_cover_Gen1_and_Gen2 = 0;
    private int read_cover_Gen2 = 0;
    private int read_cover_none = 0;

    public OperonAdjacency(PersistantFeature operonFeature1, PersistantFeature operonFeature2) {
        this.operonFeature1 = operonFeature1;
        this.operonFeature2 = operonFeature2;

    }

    /**
     * @return the operonFeature
     */
    public PersistantFeature getOperonFeature() {
        return operonFeature1;
    }

    /**
     * @param operonFeature the operonFeature to set
     */
    public void setOperonFeature(PersistantFeature operonFeature) {
        this.operonFeature1 = operonFeature;
    }

    /**
     * @return the operonFeature2
     */
    public PersistantFeature getOperonFeature2() {
        return operonFeature2;
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
