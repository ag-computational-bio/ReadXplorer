/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.api.objects;

/**
 *
 * @author msmith
 */
public class Snp454 extends Snp{
    
    private String refBase;
    
    public Snp454(int count, int position, String base, int percentage, int positionVariation, String refBase) {
        super(count, position, base, percentage, positionVariation);
        this.refBase = refBase;
    }
    
    public String getRefBase() {
        return this.refBase;
    }
}
