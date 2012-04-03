package de.cebitec.vamp.databackend.dataObjects;

/**
 * @author -Rolf Hilker-
 * 
 * Contains the data structure for temporary storing a distribution of 
 * counting values. The counts of the input are assigned into groups. The groups
 * get more voluminous with growing number size.
 * The distribution looks as follows:
 * index countedValue
 * 0 = 1
 * 1 = 2
 * 2 = 3-4
 * 3 = 5-6
 * 4 = 7-8
 * 5 = 9-10
 * 6 = 11-13
 * 7 = 14-16
 * 8 = 17-19
 * 9 = 20-22
 * 10 = 23-25
 * 11 = 26-28
 * 12 = 29-31
 * 13 = 32-34
 * 14 = 35-38
 * 15 = 39-42
 * 16 = 43-46
 * 17 = 47-50
 * 18 = 51-55
 * 19 = 56-60
 * 20 = 61-70
 * 21 = 71-80
 * 22 = 81-90
 * 23 = 91-100
 * 24 = 101-140
 * 25 = 141-180
 * 26 = 181-240
 * 27 = 241-300
 * 28 = 301-400
 * 29 = 401-500
 * 30 = 501-600
 * 31 = 601-700
 * 32 = 701-850
 * 33 = 851-100
 * 34 = 1001+ 
 */
public class DiscreteCountingDistribution {
    
    
    private static final int nbDistributionFields = 35;
    
    private static final int idx0 = 0;
    private static final int idx1 = 1;
    private static final int idx2 = 2;
    private static final int idx3 = 3;
    private static final int idx4 = 4;
    private static final int idx5 = 5;
    private static final int idx6 = 6;
    private static final int idx7 = 7;
    private static final int idx8 = 8;
    private static final int idx9 = 9;
    private static final int idx10 = 10;
    private static final int idx11 = 11;
    private static final int idx12 = 12;
    private static final int idx13 = 13;
    private static final int idx14 = 14;
    private static final int idx15 = 15;
    private static final int idx16 = 16;
    private static final int idx17 = 17;
    private static final int idx18 = 18;
    private static final int idx19 = 19;
    private static final int idx20 = 20;
    private static final int idx21 = 21;
    private static final int idx22 = 22;
    private static final int idx23 = 23;
    private static final int idx24 = 24;
    private static final int idx25 = 25;
    private static final int idx26 = 26;
    private static final int idx27 = 27;
    private static final int idx28 = 28;
    private static final int idx29 = 29;
    private static final int idx30 = 30;
    private static final int idx31 = 31;
    private static final int idx32 = 32;
    private static final int idx33 = 33;
    private static final int idx34 = 34;
    
    private int[] DiscreteCountingDistribution;
    private byte type;
    private long totalCount;

    /**
     * Contains the data structure for temporary storing a distribution of 
     * counting values. The counts of the input are assigned into groups. The groups
     * get more voluminous with growing number size.
     * The distribution looks as follows:
     * index countedValue
     * 0 = 1
     * 1 = 2
     * 2 = 3-4
     * 3 = 5-6
     * 4 = 7-8
     * 5 = 9-10
     * 6 = 11-13
     * 7 = 14-16
     * 8 = 17-19
     * 9 = 20-22
     * 10 = 23-25
     * 11 = 26-28
     * 12 = 29-31
     * 13 = 32-34
     * 14 = 35-38
     * 15 = 39-42
     * 16 = 43-46
     * 17 = 47-50
     * 18 = 51-55
     * 19 = 56-60
     * 20 = 61-70
     * 21 = 71-80
     * 22 = 81-90
     * 23 = 91-100
     * 24 = 101-140
     * 25 = 141-180
     * 26 = 181-240
     * 27 = 241-300
     * 28 = 301-400
     * 29 = 401-500
     * 30 = 501-600
     * 31 = 601-700
     * 32 = 701-850
     * 33 = 851-100
     * 34 = 1001+ 
     */
    public DiscreteCountingDistribution() {
        this.DiscreteCountingDistribution = new int[nbDistributionFields];
        this.totalCount = 0;
    }

    /**
     * Contains the data structure for temporary storing a distribution of 
     * counting values. The counts of the input are assigned into groups. The groups
     * get more voluminous with growing number size.
     * The distribution looks as follows:
     * index countedValue
     * 0 = 1
     * 1 = 2
     * 2 = 3-4
     * 3 = 5-6
     * 4 = 7-8
     * 5 = 9-10
     * 6 = 11-13
     * 7 = 14-16
     * 8 = 17-19
     * 9 = 20-22
     * 10 = 23-25
     * 11 = 26-28
     * 12 = 29-31
     * 13 = 32-34
     * 14 = 35-38
     * 15 = 39-42
     * 16 = 43-46
     * 17 = 47-50
     * 18 = 51-55
     * 19 = 56-60
     * 20 = 61-70
     * 21 = 71-80
     * 22 = 81-90
     * 23 = 91-100
     * 24 = 101-140
     * 25 = 141-180
     * 26 = 181-240
     * 27 = 241-300
     * 28 = 301-400
     * 29 = 401-500
     * 30 = 501-600
     * 31 = 601-700
     * 32 = 701-850
     * 33 = 851-100
     * 34 = 1001+ 
     * @param increase the currently detected increase at a position
     * in the genome.
     */
    public void increaseDistribution(int increase) {
        if (increase < 0) {
            return;
        } else 
        if (increase == 1) {
            ++this.DiscreteCountingDistribution[idx0];
        } else
        if (increase == 2) {
            ++this.DiscreteCountingDistribution[idx1];
        } else
        if (increase <= 4) {
            ++this.DiscreteCountingDistribution[idx2];
        } else
        if (increase <= 6) {
            ++this.DiscreteCountingDistribution[idx3];
        } else
        if (increase <= 8) {
            ++this.DiscreteCountingDistribution[idx4];
        } else
        if (increase <= 10) {
            ++this.DiscreteCountingDistribution[idx5];
        } else
        if (increase <= 13) {
            ++this.DiscreteCountingDistribution[idx6];
        } else
        if (increase <= 16) {
            ++this.DiscreteCountingDistribution[idx7];
        } else
        if (increase <= 19) {
            ++this.DiscreteCountingDistribution[idx8];
        } else
        if (increase <= 22) {
            ++this.DiscreteCountingDistribution[idx9];
        } else
        if (increase <= 25) {
            ++this.DiscreteCountingDistribution[idx10];
        } else
        if (increase <= 28) {
            ++this.DiscreteCountingDistribution[idx11];
        } else
        if (increase <= 31) {
            ++this.DiscreteCountingDistribution[idx12];
        } else
        if (increase <= 34) {
            ++this.DiscreteCountingDistribution[idx13];
        } else
        if (increase <= 38) {
            ++this.DiscreteCountingDistribution[idx14];
        } else
        if (increase <= 42) {
            ++this.DiscreteCountingDistribution[idx15];
        } else
        if (increase <= 46) {
            ++this.DiscreteCountingDistribution[idx16];
        } else
        if (increase <= 50) {
            ++this.DiscreteCountingDistribution[idx17];
        } else
        if (increase <= 55) {
            ++this.DiscreteCountingDistribution[idx18];
        } else
        if (increase <= 60) {
            ++this.DiscreteCountingDistribution[idx19];
        } else
        if (increase <= 70) {
            ++this.DiscreteCountingDistribution[idx20];
        } else
        if (increase <= 80) {
            ++this.DiscreteCountingDistribution[idx21];
        } else
        if (increase <= 90) {
            ++this.DiscreteCountingDistribution[idx22];
        } else
        if (increase <= 100) {
            ++this.DiscreteCountingDistribution[idx23];
        } else
        if (increase <= 140) {
            ++this.DiscreteCountingDistribution[idx24];
        } else
        if (increase <= 180) {
            ++this.DiscreteCountingDistribution[idx25];
        } else
        if (increase <= 240) {
            ++this.DiscreteCountingDistribution[idx26];
        } else
        if (increase <= 300) {
            ++this.DiscreteCountingDistribution[idx27];
        } else
        if (increase <= 400) {
            ++this.DiscreteCountingDistribution[idx28];
        } else
        if (increase <= 500) {
            ++this.DiscreteCountingDistribution[idx29];
        } else
        if (increase <= 600) {
            ++this.DiscreteCountingDistribution[idx30];
        } else
        if (increase <= 700) {
            ++this.DiscreteCountingDistribution[idx31];
        } else
        if (increase <= 850) {
            ++this.DiscreteCountingDistribution[idx32];
        } else
        if (increase <= 1000) {
            ++this.DiscreteCountingDistribution[idx33];
        } else 
        if (increase > 1000) {
            ++this.DiscreteCountingDistribution[idx34];
        } 
        ++this.totalCount;
    }
    
    /**
     * Sets the total counts for a given index.
     * @param index the index for which the count should be set
     * @param count the total count for a given index of the distribution
     */
    public void setCountForIndex(int index, int count) {
        this.totalCount += count - this.DiscreteCountingDistribution[index];
        this.DiscreteCountingDistribution[index] = count;
    }

    /**
     * @return the array containing the discrete counting distribution.
     */
    public int[] getDiscreteCountingDistribution() {
        return this.DiscreteCountingDistribution;
    }
    
    /**
     * @return true, if the distribution only contains 0 entries, false otherwise
     */
    public boolean isEmpty() {
        for (int i : this.DiscreteCountingDistribution) {
            if (i > 0 ) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return The total count of entries for this data set.
     */
    public long getTotalCount() {
        return this.totalCount;
    }

    /**
     * @return the type of this distribution: Either Properties.COVERAGE_INCREASE_DISTRIBUTION
     * or Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     */
    public byte getType() {
        return this.type;
    }

    /**
     * Set the type of this distribution.
     * @param type of this distribution: Either Properties.COVERAGE_INCREASE_DISTRIBUTION
     * or Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     */
    public void setType(byte type) {
        this.type = type;
    }
    
    
    
    /**
     * Contains the data structure for temporary storing a distribution of 
     * counting values. The counts of the input are assigned into groups. The groups
     * get more voluminous with growing number size.
     * The distribution looks as follows:
     * index countedValue
     * 0 = 1
     * 1 = 2
     * 2 = 3-4
     * 3 = 5-6
     * 4 = 7-8
     * 5 = 9-10
     * 6 = 11-13
     * 7 = 14-16
     * 8 = 17-19
     * 9 = 20-22
     * 10 = 23-25
     * 11 = 26-28
     * 12 = 29-31
     * 13 = 32-34
     * 14 = 35-38
     * 15 = 39-42
     * 16 = 43-46
     * 17 = 47-50
     * 18 = 51-55
     * 19 = 56-60
     * 20 = 61-70
     * 21 = 71-80
     * 22 = 81-90
     * 23 = 91-100
     * 24 = 101-140
     * 25 = 141-180
     * 26 = 181-240
     * 27 = 241-300
     * 28 = 301-400
     * 29 = 401-500
     * 30 = 501-600
     * 31 = 601-700
     * 32 = 701-850
     * 33 = 851-100
     * 34 = 1001+ 
     * @param count the count value for which the index is needed.
     */
    public int getIndexForCountValue(int count) {
        if (count == 1) {
            return idx0;
        } else
        if (count == 2) {
            return idx1;
        } else
        if (count <= 4) {
            return idx2;
        } else
        if (count <= 6) {
            return idx3;
        } else
        if (count <= 8) {
            return idx4;
        } else
        if (count <= 10) {
            return idx5;
        } else
        if (count <= 13) {
            return idx6;
        } else
        if (count <= 16) {
            return idx7;
        } else
        if (count <= 19) {
            return idx8;
        } else
        if (count <= 22) {
            return idx9;
        } else
        if (count <= 25) {
            return idx10;
        } else
        if (count <= 28) {
            return idx11;
        } else
        if (count <= 31) {
            return idx12;
        } else
        if (count <= 34) {
            return idx13;
        } else
        if (count <= 38) {
            return idx14;
        } else
        if (count <= 42) {
            return idx15;
        } else
        if (count <= 46) {
            return idx16;
        } else
        if (count <= 50) {
            return idx17;
        } else
        if (count <= 55) {
            return idx18;
        } else
        if (count <= 60) {
            return idx19;
        } else
        if (count <= 70) {
            return idx20;
        } else
        if (count <= 80) {
            return idx21;
        } else
        if (count <= 90) {
            return idx22;
        } else
        if (count <= 100) {
            return idx23;
        } else
        if (count <= 140) {
            return idx24;
        } else
        if (count <= 180) {
            return idx25;
        } else
        if (count <= 240) {
            return idx26;
        } else
        if (count <= 300) {
            return idx27;
        } else
        if (count <= 400) {
            return idx28;
        } else
        if (count <= 500) {
            return idx29;
        } else
        if (count <= 600) {
            return idx30;
        } else
        if (count <= 700) {
            return idx31;
        } else
        if (count <= 850) {
            return idx32;
        } else
        if (count <= 1000) {
            return idx33;
        } else { // meaning covIncrease > 1000
            return idx34;
        }
    }
    
    /**
     * Contains the data structure for temporary storing a distribution of 
     * counting values. The counts of the input are assigned into groups. The groups
     * get more voluminous with growing number size.
     * The distribution looks as follows:
     * index countedValue
     * 0 = 1
     * 1 = 2
     * 2 = 3-4
     * 3 = 5-6
     * 4 = 7-8
     * 5 = 9-10
     * 6 = 11-13
     * 7 = 14-16
     * 8 = 17-19
     * 9 = 20-22
     * 10 = 23-25
     * 11 = 26-28
     * 12 = 29-31
     * 13 = 32-34
     * 14 = 35-38
     * 15 = 39-42
     * 16 = 43-46
     * 17 = 47-50
     * 18 = 51-55
     * 19 = 56-60
     * 20 = 61-70
     * 21 = 71-80
     * 22 = 81-90
     * 23 = 91-100
     * 24 = 101-140
     * 25 = 141-180
     * 26 = 181-240
     * 27 = 241-300
     * 28 = 301-400
     * 29 = 401-500
     * 30 = 501-600
     * 31 = 601-700
     * 32 = 701-850
     * 33 = 851-100
     * 34 = 1001+ 
     * @param index the coverage increase for which the index is needed.
     */
    public int getMinCountForIndex(int index) {
        switch (index) {
            case idx0 :
                return 1;
            case idx1 :
                return 2;
            case idx2 :
                return 3;
            case idx3 :
                return 5;
            case idx4 :
                return 7;
            case idx5 :
                return 9;
            case idx6 :
                return 11;
            case idx7 :
                return 14;
            case idx8 :
                return 17;
            case idx9 :
                return 20;
            case idx10 :
                return 23;
            case idx11 :
                return 26;
            case idx12 :
                return 29;
            case idx13 :
                return 32;
            case idx14 :
                return 35;
            case idx15 :
                return 39;
            case idx16 :
                return 43;
            case idx17 :
                return 47;
            case idx18 :
                return 51;
            case idx19 :
                return 56;
            case idx20 :
                return 61;
            case idx21 :
                return 71;
            case idx22 :
                return 81;
            case idx23 :
                return 91;
            case idx24 :
                return 101;
            case idx25 :
                return 141;
            case idx26 :
                return 181;
            case idx27 :
                return 241;
            case idx28 :
                return 301;
            case idx29 :
                return 401;
            case idx30 :
                return 501;
            case idx31 :
                return 601;
            case idx32 :
                return 701;
            case idx33 :
                return 851;
            case idx34 :
                return 1001;
            default:
                return 1001;
        }
    }
    
}
