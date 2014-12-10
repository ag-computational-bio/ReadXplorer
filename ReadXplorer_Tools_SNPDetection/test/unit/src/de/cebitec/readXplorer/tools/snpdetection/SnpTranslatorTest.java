/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readXplorer.tools.snpdetection;


import de.cebitec.readxplorer.databackend.dataObjects.CodonSnp;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.Snp;
import de.cebitec.readxplorer.tools.snpdetection.SnpTranslator;
import de.cebitec.readxplorer.utils.SequenceComparison;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 *
 * @author rhilker
 */
public class SnpTranslatorTest {

    public SnpTranslatorTest() {
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of checkCoveredByFeature method, of class SnpTranslator.
     */
    @Test
    public void testCheckCoveredByFeature() {
//        System.out.println("checkCoveredByFeature");
//        String position = "";
//        SnpTranslator instance = null;
//        List expResult = null;
//        List result = instance.checkCoveredByFeature(position);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }


    /**
     * Test of checkForFeature method, of class SnpTranslator.
     */
    @Test
    public void testCheckForFeature() {
//        System.out.println("checkForFeature");
//        Snp snp = null;
//        SnpTranslator instance = null;
//        instance.checkForFeature(snp);
        // TODO review the generated test code and remove the default call to fail.
    }


    @Test
    public void testCalcSnpList() {
        System.out.println( "CalcSnpListTest" );
        PersistentFeature feat1 = new PersistentFeature( 1, 1, "0", "ec1", "locus1", "product1", 1, 205, true, FeatureType.CDS, "name1" );
        PersistentFeature feat2 = new PersistentFeature( 2, 2, "0", "ec2", "locus2", "product2", 2, 320, false, FeatureType.CDS, "name2" );
        PersistentFeature feat3 = new PersistentFeature( 3, 3, "0", "ec3", "locus3", "product3", 430, 650, false, FeatureType.CDS, "name3" );
        PersistentFeature feat4 = new PersistentFeature( 4, 4, "0", "ec4", "locus4", "product4", 570, 810, true, FeatureType.CDS, "name4" );
        PersistentFeature feat5 = new PersistentFeature( 5, 5, "0", "ec5", "miRnaL5", "miRnaP5", 1000, 1100, true, FeatureType.MIRNA, "miRnaN5" );
        PersistentFeature feat6 = new PersistentFeature( 6, 6, "0", "ec6", "locus6", "product6", 11000, 11130, true, FeatureType.CDS, "name6" );
        PersistentFeature feat7 = new PersistentFeature( 7, 7, "0", "ec7", "locus7", "product7", 10800, 11129, false, FeatureType.CDS, "name7" );
//        PersistentSubFeature subfeat1 = new PersistentSubFeature(1, 1, 7, FeatureType.EXON); //ttt aaa g
//        PersistentSubFeature subfeat2 = new PersistentSubFeature(1, 10, 100, FeatureType.EXON); //ac cgg cga ttc tag tga aat cga acg ggc agg tca att tcc aac cag cga tga cgt aat aga tag ata caa gga agt cat ttt tct ttt aa
//        PersistentSubFeature subfeat3 = new PersistentSubFeature(2, 2, 100, FeatureType.EXON); //current snp base is not incorporated in snp calculation a att tct ctg gcc gct aag atc act tta gct tgc ccg tcc agt taa agg ttg gtc gct act gca tta tct atc tat gtt cct tca gta aaa aga aaa tt
//        PersistentSubFeature subfeat4 = new PersistentSubFeature(2, 200, 260, FeatureType.EXON); //t ctt ggc cac agc tcc gac aaa gga agg act cgc ttc gga ccc cta ctt gct cta cca ata
//        PersistentSubFeature subfeat5 = new PersistentSubFeature(3, 499, 530, FeatureType.EXON); //cc gtc gtc acg cac cta gaa gag gcg cta ctc
//        PersistentSubFeature subfeat6 = new PersistentSubFeature(3, 550, 560, FeatureType.EXON); //tgt gga cct ag
//        PersistentSubFeature subfeat7 = new PersistentSubFeature(3, 580, 620, FeatureType.EXON); //g gct tcc gct gct taa cgc aca cat acg tgg gtt ggc aaa g
//        PersistentSubFeature subfeat7b = new PersistentSubFeature(3, 625, 630, FeatureType.EXON); //ag cta a
//        PersistentSubFeature subfeat8 = new PersistentSubFeature(3, 640, 650, FeatureType.EXON); //tc ttt atg gag
//        PersistentSubFeature subfeat9 = new PersistentSubFeature(4, 570, 590, FeatureType.EXON); //cag gtc gaa gcc gaa ggc gac
//        PersistentSubFeature subfeat10 = new PersistentSubFeature(4, 600, 621, FeatureType.EXON); //gtg tat gca ccc aac cgt ttc g
//        PersistentSubFeature subfeat11 = new PersistentSubFeature(4, 751, 810, FeatureType.EXON); //tc gtc cca tcg cag acc cac gtg gct ccc ccg cct ccg gtt gct ccg ccg ccg gcg cca g
//        PersistentSubFeature subfeat12 = new PersistentSubFeature(5, 1000, 1020, FeatureType.EXON); //tga agc aca cca gct atc tca
//        PersistentSubFeature subfeat13 = new PersistentSubFeature(5, 1070, 1100, FeatureType.EXON); //ggc ccg cgc cgc cgc ctg gca ggt ggc gga c
//        PersistentSubFeature subfeat14 = new PersistentSubFeature(6, 11000, 11050, FeatureType.EXON); //cta cca ggt cca ggt cga gct gct tct cga tga gga tgc gca gca cgc cca
//        PersistentSubFeature subfeat15 = new PersistentSubFeature(6, 11100, 11130, FeatureType.EXON); //gcc gat gcc gaa gat acc gac cag ggt atc g
//        PersistentSubFeature subfeat16 = new PersistentSubFeature(7, 10800, 11000, FeatureType.EXON); //gacctgccggacatgcgcgaccagcttcaggtcgccgctcgaaccgaactcgcgcgcgtggctgaccatgtgccggtgcaggtgcggaagcaggagcatcgcgcgcgcgtccgccagcttgtgcttcaggtcgtggacgagccggtccggccgccggaactggaacagcggcataacgagctgccgccgcaactggcg ccg
//        PersistentSubFeature subfeat17 = new PersistentSubFeature(7, 11100, 11129, FeatureType.EXON); //cgg cta cgg ctt cta tgg ctg gtc cca tag
//        feat1.addSubFeature(subfeat1);
//        feat1.addSubFeature(subfeat2);
//        feat2.addSubFeature(subfeat3);
//        feat2.addSubFeature(subfeat4);
//        feat3.addSubFeature(subfeat5);
//        feat3.addSubFeature(subfeat6);
//        feat3.addSubFeature(subfeat7);
//        feat3.addSubFeature(subfeat7b);
//        feat3.addSubFeature(subfeat8);
//        feat4.addSubFeature(subfeat9);
//        feat4.addSubFeature(subfeat10);
//        feat4.addSubFeature(subfeat11);
//        feat5.addSubFeature(subfeat12);
//        feat5.addSubFeature(subfeat13);
//        feat6.addSubFeature(subfeat14);
//        feat6.addSubFeature(subfeat15);
//        feat7.addSubFeature(subfeat16);
//        feat7.addSubFeature(subfeat17);
        List<PersistentFeature> featuresFound = new ArrayList<>();
        featuresFound.add( feat1 );
        featuresFound.add( feat2 );
        featuresFound.add( feat3 );
        featuresFound.add( feat4 );
        featuresFound.add( feat5 );
        featuresFound.add( feat6 );
        featuresFound.add( feat7 );           //A, C, G, T, N, Gap, cov, freq
        Snp snp1 = new Snp( 1, 1, 1, 'A', 'T', 20, 0, 0, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp2 = new Snp( 2, 1, 1, 'G', 'T', 0, 0, 30, 5, 0, 0, 35, 83, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp3 = new Snp( 3, 1, 1, 'C', 'T', 0, 20, 0, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp4 = new Snp( 10, 1, 1, 'T', 'A', 0, 0, 0, 25, 0, 0, 25, 100, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp5 = new Snp( 11, 1, 1, 'A', 'C', 20, 5, 0, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp6 = new Snp( 500, 1, 1, 'A', 'G', 20, 0, 0, 0, 0, 0, 20, 100, SequenceComparison.SUBSTITUTION, 37, 37 ); //right base of triplet, test for suspended snp
        Snp snp7 = new Snp( 501, 1, 1, 'A', 'C', 20, 5, 0, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp8 = new Snp( 620, 1, 1, 'A', 'C', 20, 5, 0, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp8b = new Snp( 630, 1, 1, 'C', 'T', 0, 20, 0, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp8c = new Snp( 751, 1, 1, 'C', 'T', 0, 20, 0, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp9 = new Snp( 1000, 1, 1, 'A', 'T', 20, 0, 5, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp10 = new Snp( 1001, 1, 1, 'T', 'G', 0, 0, 5, 15, 0, 0, 20, 75, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp11 = new Snp( 1002, 1, 1, 'T', 'A', 5, 0, 0, 15, 0, 0, 25, 75, SequenceComparison.SUBSTITUTION, 37, 37 ); //to high coverage
        Snp snp12 = new Snp( 1003, 1, 1, 'G', 'A', 5, 0, 20, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp13 = new Snp( 1098, 1, 1, 'A', 'T', 20, 0, 0, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp14 = new Snp( 1099, 1, 1, 'G', 'A', 5, 0, 20, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp15 = new Snp( 1100, 1, 1, 'A', 'C', 20, 5, 0, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp16 = new Snp( 1101, 1, 1, 'T', 'A', 0, 0, 0, 25, 0, 0, 25, 100, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp17 = new Snp( 11127, 1, 1, 'T', 'A', 5, 0, 0, 20, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp18 = new Snp( 11128, 1, 1, 'N', 'T', 0, 0, 0, 5, 20, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 ); //contains an N
        Snp snp19 = new Snp( 11129, 1, 1, 'G', 'C', 0, 0, 20, 5, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 );
        Snp snp20 = new Snp( 11130, 1, 1, '-', 'G', 20, 0, 5, 0, 0, 0, 25, 80, SequenceComparison.SUBSTITUTION, 37, 37 ); //contains a _

        List<Snp> snps = new ArrayList<>();
        snps.add( snp1 );
        snps.add( snp2 );
        snps.add( snp3 );
        snps.add( snp4 );
        snps.add( snp5 );
        snps.add( snp6 );
        snps.add( snp7 );
        snps.add( snp8 );
        snps.add( snp8b );
        snps.add( snp8c );
        snps.add( snp9 );
        snps.add( snp10 );
        snps.add( snp11 );
        snps.add( snp12 );
        snps.add( snp13 );
        snps.add( snp14 );
        snps.add( snp15 );
        snps.add( snp16 );
        snps.add( snp17 );
        snps.add( snp18 );
        snps.add( snp19 );
        snps.add( snp20 );

//        Set<FeatureType> featureTypes = new HashSet<>();
//        featureTypes.add(FeatureType.CDS);
//        featureTypes.add(FeatureType.EXON);
//        featureTypes.add(FeatureType.MIRNA);
        SnpTranslator snpTranslator = new SnpTranslator( featuresFound,
                                                         new PersistentChromosome( 1, 1, 1, "genome", 1 ),
                                                         new PersistentReference( 1, "test", "test", null, new File( "" ) ) );
        for( Snp snp : snps ) {
            snpTranslator.checkForFeature( snp );
        }

        for( Snp snp : snps ) {
            System.out.println( "Snp at : " + snp.getPosition() );
            for( CodonSnp codon : snp.getCodons() ) {
                System.out.println( "Refseq: " + codon.getTripletRef() );
                System.out.println( "Snpseq: " + codon.getTripletSnp() );
            }
        }

        List<CodonSnp> codons1 = snp1.getCodons();
        List<CodonSnp> codons2 = snp2.getCodons();
        List<CodonSnp> codons3 = snp3.getCodons();
        List<CodonSnp> codons4 = snp4.getCodons();
        List<CodonSnp> codons5 = snp5.getCodons();
        List<CodonSnp> codons6 = snp6.getCodons();
        List<CodonSnp> codons7 = snp7.getCodons();
        List<CodonSnp> codons8 = snp8.getCodons();
        List<CodonSnp> codons8b = snp8b.getCodons();
        List<CodonSnp> codons8c = snp8c.getCodons();
        List<CodonSnp> codons9 = snp9.getCodons();
        List<CodonSnp> codons10 = snp10.getCodons();
        List<CodonSnp> codons11 = snp11.getCodons();
        List<CodonSnp> codons12 = snp12.getCodons();
        List<CodonSnp> codons13 = snp13.getCodons();
        List<CodonSnp> codons14 = snp14.getCodons();
        List<CodonSnp> codons15 = snp15.getCodons();
        List<CodonSnp> codons16 = snp16.getCodons();
        List<CodonSnp> codons17 = snp17.getCodons();
        List<CodonSnp> codons18 = snp18.getCodons();
        List<CodonSnp> codons19 = snp19.getCodons();
        List<CodonSnp> codons20 = snp20.getCodons();
        assertTrue( codons1.size() == 1 ); // 1
        assertTrue( codons1.get( 0 ).getTripletRef().equals( "TTT" ) ); //Ttt aaa g -> Att aaa g
        assertTrue( codons1.get( 0 ).getTripletSnp().equals( "ATT" ) );
        assertTrue( codons2.size() == 1 ); // 2
        assertTrue( codons2.get( 0 ).getTripletRef().equals( "TTT" ) ); //tTt aaa g -> tGt aaa g
        assertTrue( codons2.get( 0 ).getTripletSnp().equals( "TGT" ) ); //scnd feature missing because: a att tct ... = incomplete triplet
        assertTrue( codons3.size() == 2 ); // 3
        assertTrue( codons3.get( 0 ).getTripletRef().equals( "TTT" ) ); //ttT aaa g -> ttC aaa g
        assertTrue( codons3.get( 0 ).getTripletSnp().equals( "TTC" ) );
        assertTrue( codons3.get( 1 ).getTripletRef().equals( "TTA" ) ); //a Att tct ... -> a Gtt tct -> read triplets backwards!
        assertTrue( codons3.get( 1 ).getTripletSnp().equals( "TTG" ) );
        assertTrue( codons4.size() == 2 ); //10
        assertTrue( codons4.get( 0 ).getTripletRef().equals( "GAC" ) ); // ttt aaa g Ac cgg -> ttt aaa g Tc cgg
        assertTrue( codons4.get( 0 ).getTripletSnp().equals( "GTC" ) );
        assertTrue( codons4.get( 1 ).getTripletRef().equals( "GTC" ) ); // a att tct cTg gcc -> a att tct cAg gcc
        assertTrue( codons4.get( 1 ).getTripletSnp().equals( "GAC" ) );
        assertTrue( codons5.size() == 2 ); //11
        assertTrue( codons5.get( 0 ).getTripletRef().equals( "GAC" ) ); // ttt aaa g aC cgg -> ttt aaa g aA cgg
        assertTrue( codons5.get( 0 ).getTripletSnp().equals( "GAA" ) );
        assertTrue( codons5.get( 1 ).getTripletRef().equals( "GTC" ) ); // a att tct ctG gcc -> a att tct ctT gcc
        assertTrue( codons5.get( 1 ).getTripletSnp().equals( "TTC" ) );
        assertTrue( codons6.isEmpty() ); //500 // cC gtc gtc -> triplet incomplete
        assertTrue( codons7.size() == 1 ); // 501
        assertTrue( codons7.get( 0 ).getTripletRef().equals( "CTG" ) ); // cc Gtc gtc -> cc Ttc gtc
        assertTrue( codons7.get( 0 ).getTripletSnp().equals( "CTT" ) );
        assertTrue( codons8.size() == 2 ); //620
        assertTrue( codons8.get( 0 ).getTripletRef().equals( "GAG" ) ); // ggc aaa G ag cta a -> ggc aaa T ag cta a
        assertTrue( codons8.get( 0 ).getTripletSnp().equals( "GAT" ) );
        assertTrue( codons8.get( 1 ).getTripletRef().equals( "TTC" ) ); // cgt ttc g -> cgt ttA g
        assertTrue( codons8.get( 1 ).getTripletSnp().equals( "TTA" ) );
        assertTrue( codons8b.size() == 1 ); //630
        assertTrue( codons8b.get( 0 ).getTripletRef().equals( "CTA" ) ); // cta A tc ttt -> cta G tc ttt
        assertTrue( codons8b.get( 0 ).getTripletSnp().equals( "CTG" ) );
        assertTrue( codons8c.size() == 1 ); //751
        assertTrue( codons8c.get( 0 ).getTripletRef().equals( "GTC" ) ); // ttc g Tc gtc -> ttc g Cc gtc
        assertTrue( codons8c.get( 0 ).getTripletSnp().equals( "GCC" ) );
        assertTrue( codons9.size() == 1 ); //1000
        assertTrue( codons9.get( 0 ).getTripletRef().equals( "TGA" ) ); // Tga agc aca -> Aga agc aca
        assertTrue( codons9.get( 0 ).getTripletSnp().equals( "AGA" ) );
        assertTrue( codons10.size() == 1 ); //1001
        assertTrue( codons10.get( 0 ).getTripletRef().equals( "TGA" ) ); // tGa agc aca -> tTa agc aca
        assertTrue( codons10.get( 0 ).getTripletSnp().equals( "TTA" ) );
        assertTrue( codons11.size() == 1 ); //1002
        assertTrue( codons11.get( 0 ).getTripletRef().equals( "TGA" ) ); // tgA agc aca -> tgT agc aca
        assertTrue( codons11.get( 0 ).getTripletSnp().equals( "TGT" ) );
        assertTrue( codons12.size() == 1 ); //1003
        assertTrue( codons12.get( 0 ).getTripletRef().equals( "AGC" ) ); // tga Agc aca -> tga Ggc aca
        assertTrue( codons12.get( 0 ).getTripletSnp().equals( "GGC" ) );
        assertTrue( codons13.size() == 1 ); //1098
        assertTrue( codons13.get( 0 ).getTripletRef().equals( "GGA" ) ); // ggc gGa c -> ggc gAa c // 21 + (ab 70) 29 = 50 mod 3 = 2
        assertTrue( codons13.get( 0 ).getTripletSnp().equals( "GAA" ) );
        assertTrue( codons14.size() == 1 ); //1099
        assertTrue( codons14.get( 0 ).getTripletRef().equals( "GGA" ) ); // ggc ggA c -> ggc ggG c
        assertTrue( codons14.get( 0 ).getTripletSnp().equals( "GGG" ) );
        assertTrue( codons15.isEmpty() ); //1100 // ggc gga c -> incomplete triplet
        assertTrue( codons16.isEmpty() ); //1101 // not in a subfeature, but feature has subfeatures, so no translation
        assertTrue( codons17.size() == 2 ); //11127
        assertTrue( codons17.get( 0 ).getTripletRef().equals( "ATC" ) ); // ggt Atc g -> ggt Ttc g // 51 + (ab 100) 28 = 79 mod 3 = 1
        assertTrue( codons17.get( 0 ).getTripletSnp().equals( "TTC" ) );
        assertTrue( codons17.get( 1 ).getTripletRef().equals( "GAT" ) ); // gtc cca Tag -> gtc cca Aag
        assertTrue( codons17.get( 1 ).getTripletSnp().equals( "GAA" ) );
        assertTrue( codons18.isEmpty() ); //11128 //snp with N = kicked
        assertTrue( codons19.size() == 2 );//11129
        assertTrue( codons19.get( 0 ).getTripletRef().equals( "ATC" ) ); // ggt atC g -> ggt atG g // 51 + (ab 100) 30 = 81 mod 3 = 0
        assertTrue( codons19.get( 0 ).getTripletSnp().equals( "ATG" ) );
        assertTrue( codons19.get( 1 ).getTripletRef().equals( "GAT" ) ); // gtc cca taG -> gtc cca taC
        assertTrue( codons19.get( 1 ).getTripletSnp().equals( "CAT" ) );
        assertTrue( codons20.isEmpty() ); //11130 //snp with _ = kicked //new triplet = at border or needs base from right side
    }


    //pseudomonas aeruginosa PAO1 first 11130 bp
    private static final String refSeq = "TTTAAAGAGACCGGCGATTCTAGTGAAATCGAACGGGCAGGTCAATTTCCAACCAGCGATGACGTAATAG"
                                   + "ATAGATACAAGGAAGTCATTTTTCTTTTAAAGGATAGAAACGGTTAATGCTCTTGGGACGGCGCTTTTCT"
                                   + "GTGCATAACTCGATGAAGCCCAGCAATTGCGTGTTTCTCCGGCAGGCAAAAGGTTGTCGAGAACCGGTGT"
                                   + "CGAGGCTGTTTCCTTCCTGAGCGAAGCCTGGGGATGAACGAGATGGTTATCCACAGCGGTTTTTTCCACA"
                                   + "CGGCTGTGCGCAGGGATGTACCCCCTTCAAAGCAAGGGTTATCCACAAAGTCCAGGACGACCGTCCGTCG"
                                   + "GCCTGCCTGCTTTTATTAAGGTCTTGATTTGCTTGGGGCCTCAGCGCATCGGCATGTGGATAAGTCCGGC"
                                   + "CCGTCCGGCTACAATAGGCGCTTATTTCGTTGTGCCGCCTTTCCAATCTTTGGGGGATATCCGTGTCCGT"
                                   + "GGAACTTTGGCAGCAGTGCGTGGATCTTCTCCGCGATGAGCTGCCGTCCCAACAATTCAACACCTGGATC"
                                   + "CGTCCCTTGCAGGTCGAAGCCGAAGGCGACGAATTGCGTGTGTATGCACCCAACCGTTTCGTCCTCGATT"
                                   + "GGGTGAACGAGAAATACCTCGGTCGGCTTCTGGAACTGCTCGGTGAACGCGGCGAGGGTCAGTTGCCCGC"
                                   + "GCTTTCCTTATTAATAGGCAGCAAGCGTAGCCGTACGCCGCGCGCCGCCATCGTCCCATCGCAGACCCAC"
                                   + "GTGGCTCCCCCGCCTCCGGTTGCTCCGCCGCCGGCGCCAGTGCAGCCGGTATCGGCCGCGCCCGTGGTAG"
                                   + "TGCCACGTGAAGAGCTGCCGCCAGTGACGACGGCTCCCAGCGTGTCGAGCGATCCCTACGAGCCGGAAGA"
                                   + "ACCCAGCATCGATCCGCTGGCCGCCGCCATGCCGGCTGGAGCAGCGCCTGCGGTGCGCACCGAGCGCAAC"
                                   + "GTCCAGGTCGAAGGTGCGCTGAAGCACACCAGCTATCTCAACCGTACCTTCACCTTCGAGAACTTCGTCG"
                                   + "AGGGCAAGTCCAACCAGTTGGCCCGCGCCGCCGCCTGGCAGGTGGCGGACAACCTCAAGCACGGCTACAA"
                                   + "CCCGCTGTTCCTCTACGGTGGCGTCGGTCTGGGCAAGACCCACCTGATGCATGCGGTGGGCAACCACCTG"
                                   + "CTGAAGAAGAACCCGAACGCCAAGGTGGTCTACCTGCATTCGGAACGTTTCGTCGCGGACATGGTGAAGG"
                                   + "CCTTGCAGCTCAACGCCATCAACGAATTCAAGCGCTTCTACCGCTCGGTGGACGCACTGTTGATCGACGA"
                                   + "CATCCAGTTCTTCGCCCGTAAGGAGCGCTCCCAGGAGGAGTTCTTCCACACCTTCAATGCCCTTCTCGAA"
                                   + "GGCGGCCAGCAGGTGATCCTCACCAGCGACCGCTATCCGAAGGAAATCGAAGGCCTGGAAGAGCGGCTGA"
                                   + "AATCCCGCTTCGGCTGGGGCCTGACGGTGGCCGTCGAGCCGCCGGAACTGGAAACCCGGGTGGCGATCCT"
                                   + "GATGAAGAAGGCCGAGCAGGCGAAGATCGAGCTGCCGCACGATGCGGCCTTCTTCATCGCCCAGCGCATC"
                                   + "CGTTCCAACGTGCGTGAACTGGAAGGTGCGCTGAAGCGGGTGATCGCCCACTCGCACTTCATGGGCCGGC"
                                   + "CGATCACCATCGAGCTGATTCGCGAGTCGCTGAAGGACCTGTTGGCCCTTCAGGACAAGCTGGTCAGCAT"
                                   + "CGACAACATCCAGCGCACCGTCGCCGAGTACTACAAGATCAAGATATCCGATCTGTTGTCCAAGCGGCGT"
                                   + "TCGCGCTCGGTGGCGCGCCCGCGCCAGGTGGCCATGGCGCTCTCCAAGGAGCTGACCAACCACAGCCTGC"
                                   + "CGGAGATCGGCGTGGCCTTCGGCGGTCGGGATCACACCACGGTGTTGCACGCCTGTCGTAAGATCGCTCA"
                                   + "ACTTAGGGAATCCGACGCGGATATCCGCGAGGACTACAAGAACCTGCTGCGTACCCTGACAACCTGACGC"
                                   + "AGCCCACGAGGCAAGGGACTAGACCATGCATTTCACCATTCAACGCGAAGCCCTGTTGAAACCGCTGCAA"
                                   + "CTGGTCGCCGGCGTCGTGGAACGCCGCCAGACATTGCCGGTTCTCTCCAACGTCCTGCTGGTGGTCGAAG"
                                   + "GCCAGCAACTGTCGCTGACCGGCACCGACCTCGAAGTCGAGCTGGTTGGTCGCGTGGTACTGGAAGATGC"
                                   + "CGCCGAACCCGGCGAGATCACCGTACCGGCGCGCAAGCTGATGGACATCTGCAAGAGCCTGCCGAACGAC"
                                   + "GTGCTGATCGACATCCGTGTCGAAGAGCAGAAACTTCTGGTGAAGGCCGGGCGTAGCCGCTTCACCCTGT"
                                   + "CCACCCTGCCGGCCAACGATTTCCCCACCGTAGAGGAAGGTCCCGGCTCGCTGAACTTCAGCATTGCCCA"
                                   + "GAGCAAGCTGCGTCGCCTGATCGACCGCACCAGCTTCGCCATGGCCCAGCAGGACGTGCGTTACTACCTC"
                                   + "AACGGCATGCTGCTGGAAGTGAACGGCGGCACCCTGCGCTCCGTCGCCACCGACGGCCACCGACTGGCCA"
                                   + "TGTGCTCGCTGGATGCGCAGATCCCGTCGCAGGACCGCCACCAGGTGATCGTGCCGCGCAAAGGCATCCT"
                                   + "CGAACTGGCTCGTCTGCTCACCGAGCAGGACGGCGAAGTCGGCATCGTCCTGGGCCAGCACCATATCCGT"
                                   + "GCCACCACTGGCGAATTCACCTTCACTTCGAAGCTGGTGGACGGCAAGTTCCCGGACTACGAGCGTGTAC"
                                   + "TGCCGCGCGGTGGCGACAAGCTGGTGGTCGGTGACCGCCAGCAACTGCGCGAAGCCTTCAGCCGTACCGC"
                                   + "GATCCTCTCCAACGAGAAGTACCGCGGCATTCGCCTGCAGCTTTCCAACGGTTTGCTGAAAATCCAGGCG"
                                   + "AACAACCCGGAGCAGGAAGAGGCCGAGGAAGAAGTGCAGGTCGAGTACAACGGCGGCAACCTGGAGATAG"
                                   + "GCTTCAACGTCAGTTACCTGCTCGACGTGCTGGGTGTGATCGGTACCGAGCAGGTCCGCTTCATCCTTTC"
                                   + "CGATTCCAACAGCAGCGCCCTGGTCCACGAGGCCGACAATGACGATTCTGCCTATGTCGTCATGCCGATG"
                                   + "CGCCTCTAAACATACTGAATGTCCCTGACCCGCGTTTCGGTCACCGCGGTGCGCAACCTGCACCCGGTGA"
                                   + "CCCTCTCCCCCTCCCCCCGCATCAACATCCTCTACGGCGACAACGGCAGCGGCAAGACCAGCGTGCTCGA"
                                   + "AGCCATCCACCTGCTGGGCCTGGCGCGTTCATTCCGCAGTGCGCGCTTGCAGCCGGTGATCCAGTATGAG"
                                   + "GAAGCGGCCTGCACCGTATTCGGCCAGGTGATGTTGGCCAACGGCATCGCCAGCAACCTGGGGATTTCCC"
                                   + "GTGAGCGCCAGGGCGAGTTCACCATCCGCATCGATGGGCAGAACGCCCGGAGTGCGGCTCAATTGGCGGA"
                                   + "AACTCTCCCACTGCAACTGATCAACCCGGACAGCTTTCGGTTGCTCGAGGGAGCGCCGAAGATCCGGCGA"
                                   + "CAGTTCCTCGATTGGGGAGTGTTCCACGTGGAACCTCGGTTTCTGCCCGTCTGGCAGCGCCTGCAGAAGG"
                                   + "CGCTGCGCCAGCGGAACTCCTGGCTCCGGCATGGTAAACTGGACCCCGCGTCGCAAGCGGCCTGGGACCG"
                                   + "GGAATTGAGCCTGGCCAGCGATGAGATCGATGCCTACCGCAGAAGCTATATCCAGGCGTTGAAACCGGTA"
                                   + "TTCGAGGAAACACTCGCCGAATTGGTTTCACTGGATGACCTGACCCTTAGCTACTACCGAGGCTGGGACA"
                                   + "AGGACCGGGACCTCCTGGAGGTTCTGGCTTCCAGCCTGTTGCGCGACCAGCAGATGGGCCACACCCAGGC"
                                   + "GGGACCGCAGCGTGCGGATCTTCGCATACGGTTGGCAGGTCATAACGCCGCGGAGATTCTCTCGCGCGGT"
                                   + "CAGCAGAAGCTGGTGGTATGCGCCCTGCGCATCGCCCAAGGCCATCTGATCAATCGCGCCAAGCGCGGAC"
                                   + "AGTGCGTCTACCTGGTGGACGACCTGCCCTCGGAACTGGATGAGCAGCATCGAATGGCTCTTTGCCGCTT"
                                   + "GCTTGAAGATTTGGGTTGCCAGGTATTCATCACCTGCGTGGACCCGCAACTATTGAAAGACGGCTGGCGC"
                                   + "ACGGATACGCCGGTATCCATGTTCCACGTGGAACATGGAAAAGTCTCTCAGACCACGACCATCGGGAGTG"
                                   + "AAGCATGAGCGAGAACAACACGTACGACTCTTCCAGCATCAAGGTGCTGAAGGGGCTGGATGCCGTACGC"
                                   + "AAGCGCCCCGGCATGTACATCGGCGACACCGACGATGGCACCGGTCTGCACCACATGGTGTTCGAGGTGG"
                                   + "TGGATAACTCCATCGACGAAGCGCTGGCCGGTTACTGCAGCGAAATCAGCATCACCATCCATACGGATGA"
                                   + "GTCGATCACTGTCCGCGACAATGGACGCGGTATTCCGGTGGATATCCACAAGGAAGAAGGGGTTTCTGCG"
                                   + "GCGGAAGTGATCATGACCGTCCTCCACGCCGGCGGCAAGTTCGACGACAACACCTACAAGGTGTCCGGCG"
                                   + "GCTTGCACGGTGTGGGCGTCTCGGTGGTGAACGCGCTGTCCCATGAACTACGCCTGACCATCCGTCGCCA"
                                   + "CAACAAGGTCTGGGAACAGGTCTACCACCACGGCGTTCCGCAGTTCCCACTGCGCGAAGTGGGCGAGACC"
                                   + "GATGGCTCCGGCACCGAAGTTCACTTCAAGCCGTCCCCGGAGACCTTCAGCAACATCCACTTCAGTTGGG"
                                   + "ACATCCTGGCCAAGCGCATCCGCGAGCTGTCCTTCCTCAACTCCGGCGTCGGCATCCTGCTGCGCGACGA"
                                   + "GCGTACCGGCAAGGAGGAGCTGTTCAAGTACGAAGGCGGTCTGAAGGCCTTCGTCGAGTACCTGAACACC"
                                   + "AACAAGACCGCGGTGAACGAGGTATTCCACTTCAACGTCCAGCGTGAAGAGGACGGCGTGGGTGTGGAAG"
                                   + "TCGCCTTGCAGTGGAACGACAGCTTCAACGAGAACCTGCTCTGCTTCACCAACAACATCCCGCAGCGTGA"
                                   + "CGGCGGCACCCACCTGGCCGGTTTCCGTTCGGCGCTGACGCGTAACCTGAACAACTACATCGAGGCCGAA"
                                   + "GGCCTGGCGAAGAAGTTCAAGATCGCCACCACCGGCGACGATGCCCGCGAAGGCCTCACCGCGATCATCT"
                                   + "CGGTGAAGGTACCGGACCCGAAGTTCAGCTCGCAGACCAAGGACAAGCTGGTCTCCTCCGAGGTGAAGAC"
                                   + "TGCGGTGGAACAGGAGATGGGCAAGTACTTCGCCGACTTCCTGCTGGAGAATCCCAACGAAGCCAAGGCC"
                                   + "GTGGTCGGCAAGATGATCGACGCCGCCCGTGCCCGCGAGGCCGCGCGCAAGGCGCGCGAGATGACCCGCC"
                                   + "GCAAGGGCGCGCTGGACATCGCCGGCCTGCCCGGCAAACTGGCCGATTGCCAGGAAAAGGACCCGGCGCT"
                                   + "CTCCGAACTGTACATCGTGGAGGGTGACTCCGCGGGCGGTTCCGCCAAGCAGGGCCGCAATCGCCGGACC"
                                   + "CAGGCGATCCTGCCGCTCAAGGGCAAGATCCTCAACGTCGAAAAGGCGCGCTTCGACAAGATGCTCTCCT"
                                   + "CCCAGGAGGTCGGTACGCTGATCACCGCCCTGGGCTGTGGCATCGGCCGCGAGGAATACAACATCGACAA"
                                   + "GCTGCGCTACCACAACATCATCATCATGACCGATGCTGACGTCGACGGTTCGCACATCCGCACCCTGCTG"
                                   + "TTGACCTTCTTCTTCCGCCAGATGCCCGAGCTGATCGAGCGTGGCTACATCTACATCGCCCAGCCCCCGT"
                                   + "TGTACAAGGTCAAGCGCGGCAAGCAGGAGCAGTACATCAAGGACGACCAGGCCATGGAAGAGTACATGAC"
                                   + "CCAGTCGGCCCTGGAAGACGCCAGCCTGCACGTCAACGAGCACGCTCCGGGCCTGTCCGGGGCGGCGCTG"
                                   + "GAGAAACTGGTCAACGAGTATCGCGGGGTGATCGCCACCCTCAAGCGCCTGTCGCGCCTGTACCCCCAGG"
                                   + "AGCTGACCGAGCACTTCATCTACCTGCCTACCGTGTCGGTGGACGACCTGGCTAACGAGTCGGCCATGCA"
                                   + "GGGCTGGTTGGAGAAGTTCCAGGCGCGCCTGACCGCCGCCGAGAAGTCCGGCCTGACCTACAAGGCCAGC"
                                   + "CTGCGCGAAGACCGCGAGCGCCACCTGTGGCTGCCCGAGGTGGAACTGGTGGCCCACGGCCTGTCCAGCT"
                                   + "ACGTCACCTTCAACCGTGACTTCTTCGCCAGCAATGACTACCGCTCGGTGTCGCTGCTCGGCGACCAGCT"
                                   + "GAACAGCCTGCTGGAAGACGGCGCCTACGTGCAGAAGGGTGAGCGCAAGCGCCCGATCAGCGCCTTCAAG"
                                   + "GACGGCCTGGACTGGCTGATGGCCGAAGGTACCAAGCGCCACAGCATCCAGCGATACAAGGGGCTGGGCG"
                                   + "AGATGAACCCTGAGCAGCTGTGGGAAACCACCATGGATCCGAACGTCCGGCGCATGCTCAAGGTGACCAT"
                                   + "CGAGGATGCCATCGCCGCCGACCAGATCTTCAACACCCTGATGGGCGATGCCGTGGAGCCGCGCCGCGAC"
                                   + "TTCATCGAAAGCAACGCGCTGGCGGTGTCGAACCTGGACGTGTGACAGGTCGGCAGACCGACCCTCATGG"
                                   + "AAACCCCGGCCTGGCGCCGGGGTTTTCTTTTTGCGCCAGGTAGCCTGGATACGGGCGCCAGGGGTGCCTT"
                                   + "GCCAAGGTGTGGCCGAGCCCCGGTTACTCGCCGGCATTGGCACCCTGGACCCGGGCCTCCTGGCGGCGTG"
                                   + "GCACCGCTTCTGTGGGCAGCGGTCGCCGGTCGGTTCGGTCGATCGTTGCCAGCCGGCCTTGCTTTGCGCC"
                                   + "TCGCGTGAAAACAAGAAGCCCCGCTATGGCGGGGCTTCTTTTATCGAATCGGCGCACAGTACGCCTTGCA"
                                   + "GGGGGCTTTGCGGCCCTTCACGAGACCACCGACGGCTCCGGATGGCTGACCCGCTGCTGGATGGGGCTGA"
                                   + "TCTCGGCCATGGTCTCGCTGACCCAGGCTTCGGCGCGCTGGTTTAGCTCGGCGATGGCGCGCGGGCCTTC"
                                   + "GCCTTCGGCGTGCATGGCCGGGCCGATCACCACCTGGATGGTGCCCGGGTACTTGGCCCAGCCGGCCTTG"
                                   + "GGCCAATACTGCCCGGCGTTGTGGGCGATCGGCAGTACCGGTAGCCCGGCGTTGACCGCCAGGGCGGTGC"
                                   + "CGCCGCGGGAGAACTTGCCCATCTGCCCCACCGGAATACGCGTGCCTTCCGGGAAGATCAGCACCCAGGC"
                                   + "GCCTTTCTTCAGGCACTCGTCGCCCTGCTTGGCCAGTTGCTTGAGGGCCAGCTTGGGCTGGCTGCGGTCG"
                                   + "ATGGCGATGGGCTTGAGCAGGGCCAGGGCCCAGCCGAAGAACGGCACGTAGAGCAGCTCGCGCTTGAGTA"
                                   + "CCTGGCTGAGTGGCTCGAAGAAGCCGGAGAGGAAGAAGGTTTCCCAGGTGCTCTGGTGCTTGGAGAGGAT"
                                   + "CACGCAGGGCTTTTCCGGGATGTTCTCCAGTCCGCGCACCTCGTAGCGGATGCCGGCGACCACGCGGGTC"
                                   + "AGCCAGATCGCGAAGCGGCACCAGTTCTGTACCACGAAGCGGTAGCGGGCGCGGAACGGCAGGATCGGCG"
                                   + "CGATGAAGAAGCTGAGGGTGCCCCAGACGAACGCGCTGGCGGACAGCAGCAGGTAAAAGAGGACGGTTCT"
                                   + "GATGGCCTGCACTGTCGACATGTATTCTGACCTTACTGAAGTAATGCGCTGGCGACTGCCGCCAGATCGT"
                                   + "CGAATATCAGGGTGCCCTCTGGCAAGGGCTTGCCCAGCGTACGTACACCTTTTCCGGTCTTTACCAATAC"
                                   + "CGGCTGACAATCGACGGCCCGCGCCGCCTCCAGGTCACCGATGCTGTCGCCGACGAACCAGATACCCGAC"
                                   + "AGATCGACCCCGTAGTGCTCGCCGATCTGCCGCAGCATACCCGGCTTCGGCTTGCGGCAGTCGCAACCGT"
                                   + "CGTCCGGTCCATGCGGACAATAGACGATGAGGCCGACCTCGCCGCCCTGCTCCGCGACCAGTTCGCGCAA"
                                   + "GCGCGCATGCATGGCCTCGAGCACTGCCAGGTCGTAATAGCCACGGGCGATGCCGGACTGGTTGGTAGCC"
                                   + "ACCGCGACGGTCCAGCCGGCCTGGCTCAGGCGGGCGATGGCCTCGATCGAGCTGGGGATGGGGATCCACT"
                                   + "CGTCGAGGGTCTTGATGTAATCGTCGGAGTCGAGGTTGATGACTCCATCGCGGTCGAGAATCAGCAGGGA"
                                   + "ACGGGACATCGATGCGCAGTTGGCCATGAACGGAAAGGATCGATTCTACCTCAGCCAGCCGGAGCATCGC"
                                   + "GGATGGATCGACGAGCCCCCTACGCTGGACAATCTGTACTAGATTCAATAATGGCAATGGAATGGCAATC"
                                   + "CCAGGCCAGGGAGTCATTTGGCACGCAGCAGTGGGTGTCTCGGAGTACGTGCGAACCCCTTGGCGGGTGG"
                                   + "ATCACCGGAGTAACGCAGTAAGCGCCTATCGCATGCCAGCACGGCCGACCGGCCGTGTTTCCACCACAGG"
                                   + "TGGAGCGGGCGTGACGACGGGGAGTTCCGGGGCGTCTGCCCGTTCCCAAGGACGGTAACCGTGAAACGCC"
                                   + "TGAAAAAGACACTGCACCTTTCAAGCTTGTCCCTCGCTTCCCTGGCTCTTTCTTCCGCCGCCCTGGCGGC"
                                   + "CGCTCCGGTCATGCTCGACCAGGGCAAGGAATGGACCGAAAGCCACCGCCAGGACTTCTACAGCCGCGAC"
                                   + "CAGGGCTCGCAGGTGATGCCCCTGCCCTGGCTCAAGGCGTTGCGACAGCCGGATGGAACGCCTTTCCTCG"
                                   + "CCGACAGCCTGGCCCGCTACGGCTATTTGCCCAACCCCAAGGCGCCCGCGGAAGGCCTGCCGGTGGGCTT"
                                   + "CACCGTAGCCGGCACGGGCGCCCGGCAGATGGTCGGCATGACCTGTTCGGCCTGCCATACCCGGCAGATC"
                                   + "GAGGTGAAGGGCACTGCCTATCGGATCGACGGCGGTCCGGCGATCGTCGACTTCCAGGCATTCCTCGCCG"
                                   + "ACCTCGATCGGGCCGTGGGACCGCTGACCAGCGATGACGCCGCCTTCGACGCCTTCGCCAAGCCGATCCT"
                                   + "CGGGGCCAATCCGCCTCCCGGTGCGCGCGACGCTCTGCTCGCGGCGGTGAAGGAATGGTACGAGCCCTAT"
                                   + "CACACGCTGATCGAGCGCGCGCTGCCCAAGGACACCTGGGGACCGGCGCGGCTGGACGCGGTATCGATGA"
                                   + "TCTTCAACCGCCTTACCGGGCTGGATATCGGCACCGCGCCGCCCTACCTGATTCCCGACAACATCAAGGC"
                                   + "GGCCGATGCGCCGGTGCGCTATCCGTTCCTGTGGAACGCGGCGCGGCAGAACAAGACCCAGTGGCCCGGC"
                                   + "TTCGCCGCCAACGGCAACGACCTGCTCGGCCTGGCGCGCAATGTCGGCGAGGTCTACGGGGTGTTCGCCA"
                                   + "CCTTCCACCCGCAGAAGAGCAAGTTCCACCTGCTGGGCATGGACTACCTGAAGATCAACTCGGCCAACTT"
                                   + "CCACGGGCTGGGCAAGCTGGAAGACCTGATCAAGAAGATCGGCCCGCCGAAGTGGCCCTGGGCGGTGGAC"
                                   + "AAGCACCTGGCCAGGAAAGGCGCGCTGATCTTCGCCCGCAAGACCGACGAAGGTGGCTGCGTGGAGTGCC"
                                   + "ACGGCATCCGGATCAAGGACCTGGTGCTTTGGGACACTCCGCTGAGGGACGTCGGCAGCGACAGCCGCCA"
                                   + "GCACGCCATCCTCGATGGCCAGGTGCAGACCGGCGTGATGGAGGGCGCGCGGATGCCGTTCGGCCAGCCG"
                                   + "CTGAAGGCGACCGACGGAGCCTTCGATGTACTCGCCGTAGCGGTGGCCGGTTCGATCCTGCAGCACTTCG"
                                   + "TGCCGATCCTCGGTGAGAAGCACGATGCCAAGGCGGCGGCGGTCAAGCCGGAAAGCGTGATGACCGACGA"
                                   + "AACCCGGCAACTGCTGACCGCCTTCCAGAAGCCGGTGCGTACCCAGGCCGACCCCTACCCCTACGAGTCG"
                                   + "CGGGTCCTGCAGGGGATCTGGGCAGCGGCGCCGTACCTGCACAACGGCTCGGTGCCGACCCTGGAAGAGT"
                                   + "TGCTGAAGCCGGCCGCGGAGCGGGTGGAATCCTTCCCGGTGGGCTCGGCCTACGACGTGGACAAGGTCGG"
                                   + "CCTCGCCGCCCAGCAGACCCAATTCGGCAGCTATGTGCTGAAGACCACCGGCTGCGAGCAGCGTGATTCC"
                                   + "GGCAACAGCCGCTGCGGCCATGAGTACGGCACCAGCCTGTCGGCCGAGGAGAAGCGTGCGCTGCTGGAGT"
                                   + "ATCTGAAGGTCCTGTAGTGAAAAAGGCCCGGTGTCGCGAGGACGCCGGGCCTTTTCTTCGAGGCGGGACT"
                                   + "GGCTCAGCCGAGCAGCGAGATGTCCGCCACCCCGAGGAACGACCCGCGCAGCTTGGCCAGCAGCGCATAG"
                                   + "CGGTTGGCGCGCACCGCCGCGTCGTCGACATTGACCATCACATCGGCGAAGAACGTATCCACCGGCTCGC"
                                   + "GCAAGGCCGCCAGGCGGGCCAGCGCGGCGCGATAGTCGCGTGCCGCTGCCAGCGGCGCGACTTCGCTTTC"
                                   + "GGCGTTCGCCACGGCGCTGCCCAGGGCCTTCTCGGCGGCTTCCACCAGCAGGCTGGCATCCACGTTCGGC"
                                   + "GGAACCTCGTCCTCGGACTTGGCGAGAATATTCGACACCCGTTTGTTCGCCGCGGCCAGGGCCTCGGCTT"
                                   + "CAGGCAACTGGCGGAAGGCCTGGACGGCCTGTACGCGCTGGTCGAAGTCCAGCGGCGAGCTTGGCTTGAG"
                                   + "CGCGCGCACCGACTGGTACACGGCCACGTCCACGCCTTCGTCCTCGTAGCGCGCGCGCAGGCGGTCGAAC"
                                   + "ACGAAGTCCAGCACCTGCTCGGCCAGGCCGGCGGCCTTGACCTTGTCGCCGTATTGCTCGACGGCGGCGT"
                                   + "TGACCGCGGCTACCAGGTCCAGGTCGAGCTGCTTCTCGATGAGGATGCGCAGCACGCCCAGCGCAGCGCG"
                                   + "GCGCAGCGCGTAGGGGTCCTTGCTGCCGGTGGGAAGCATGCCGATGCCGAAGATACCGACCAGGGTATCG";

}
