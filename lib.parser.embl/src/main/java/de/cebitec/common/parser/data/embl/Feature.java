/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.data.embl;

import com.google.common.collect.ListMultimap;
import de.cebitec.common.sequencetools.intervals.Interval;
import de.cebitec.common.sequencetools.intervals.Intervals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class Feature {

    private FeatureHeader header;
    private ListMultimap<Qualifier, Object> qualifiers;

    public FeatureHeader getHeader() {
        return header;
    }

    public void setHeader(FeatureHeader header) {
        this.header = header;
    }

    public ListMultimap<Qualifier, Object> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(ListMultimap<Qualifier, Object> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public <T> T getSingleQualifierValue(Class<T> type, Qualifier qualifier) {
        Collection<T> get = getQualifierValues(type, qualifier);
        if (get.isEmpty()) {
            return null;
        }
        if (get.size() > 1) {
            throw new UnsupportedOperationException("Qualifier " + qualifier + " has more than one value.");
        }
        return get.iterator().next();
    }

    public <T> Collection<T> getQualifierValues(Class<T> type, Qualifier qualifier) {
        List<Object> get = qualifiers.get(qualifier);
        List<T> list = new LinkedList<>();

        for (Object object : get) {
            if (type.isInstance(object)) {
                list.add(type.cast(object));
            } else {
                throw new UnsupportedOperationException("Value is not of type " + type + ", but of the type " + object.getClass());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "Feature{" + "header=" + header + ", qualifiers=" + qualifiers + '}';
    }

    public static enum Qualifier {

        allele(false),
        altitude(false),
        anticodon(false),
        artificial_location(true),
        bio_material(false),
        bound_moiety(false),
        cell_line(false),
        cell_type(false),
        chromosome(false),
        citation(false),
        clone(false),
        clone_lib(false),
        codon_start(false),
        collected_by(false),
        collection_date(false),
        compare(false),
        country(false),
        cultivar(false),
        culture_collection(false),
        db_xref(false),
        dev_stage(false),
        direction(false),
        EC_number(false),
        ecotype(false),
        environmental_sample(true),
        estimated_length(false),
        exception(false),
        experiment(false),
        focus(true),
        frequency(false),
        function(false),
        gap_type(false),
        gene(false),
        gene_synonym(false),
        germline(true),
        haplogroup(false),
        haplotype(false),
        host(false),
        identified_by(false),
        inference(false),
        isolate(false),
        isolation_source(false),
        lab_host(false),
        lat_lon(false),
        linkage_evidence(false),
        locus_tag(false),
        macronuclear(true),
        map(false),
        mating_type(false),
        mobile_element_type(false),
        mod_base(false),
        mol_type(false),
        ncRNA_class(false),
        note(false),
        number(false),
        old_locus_tag(false),
        operon(false),
        organelle(false),
        organism(false),
        partial(true),
        PCR_conditions(false),
        PCR_primers(false),
        phenotype(false),
        plasmid(false),
        pop_variant(false),
        product(false),
        protein_id(false),
        proviral(true),
        pseudo(true),
        pseudogene(false),
        rearranged(true),
        regulatory_class(true),
        replace(false),
        ribosomal_slippage(true),
        rpt_family(false),
        rpt_type(false),
        rpt_unit_range(false),
        rpt_unit_seq(false),
        satellite(false),
        segment(false),
        serotype(false),
        serovar(false),
        sex(false),
        specimen_voucher(false),
        standard_name(false),
        strain(false),
        sub_clone(false),
        sub_species(false),
        sub_strain(false),
        tag_peptide(false),
        tissue_lib(false),
        tissue_type(false),
        transgenic(true),
        translation(false),
        transl_except(false),
        transl_table(false),
        trans_splicing(true),
        type_material(false),
        variety(false);

        private final boolean booleanQualifier;

        /**
         *
         * @param bool Determines whether the qualifier is a boolean flag.
         */
        private Qualifier(boolean bool) {
            this.booleanQualifier = bool;
        }

        /**
         * Determines whether the qualifier will contain additional data ({@code false}) or is just a flag that contains
         * no additional data ({@code true})
         * @return 
         */
        public boolean isBool() {
            return booleanQualifier;
        }

    }

    public static enum FeatureKey {

        assembly_gap("assembly_gap"),
        attenuator("attenuator"),
        C_region("C_region"),
        CAAT_signal("CAAT_signal"),
        CDS("CDS"),
        centromere("centromere"),
        D_loop("D-loop"),
        D_segment("D_segment"),
        enhancer("enhancer"),
        exon("exon"),
        gap("gap"),
        GC_signal("GC_signal"),
        gene("gene"),
        iDNA("iDNA"),
        intron("intron"),
        J_segment("J_segment"),
        LTR("LTR"),
        mat_peptide("mat_peptide"),
        misc_binding("misc_binding"),
        misc_difference("misc_difference"),
        misc_feature("misc_feature"),
        misc_recomb("misc_recomb"),
        misc_RNA("misc_RNA"),
        misc_signal("misc_signal"),
        misc_structure("misc_structure"),
        mobile_element("mobile_element"),
        modified_base("modified_base"),
        mRNA("mRNA"),
        ncRNA("ncRNA"),
        N_region("N_region"),
        old_sequence("old_sequence"),
        operon("operon"),
        oriT("oriT"),
        polyA_signal("polyA_signal"),
        polyA_site("polyA_site"),
        precursor_RNA("precursor_RNA"),
        prim_transcript("prim_transcript"),
        primer_bind("primer_bind"),
        promoter("promoter"),
        protein_bind("protein_bind"),
        RBS("RBS"),
        repeat_region("repeat_region"),
        rep_origin("rep_origin"),
        rRNA("rRNA"),
        S_region("S_region"),
        sig_peptide("sig_peptide"),
        source("source"),
        stem_loop("stem_loop"),
        STS("STS"),
        TATA_signal("TATA_signal"),
        telomere("telomere"),
        terminator("terminator"),
        tmRNA("tmRNA"),
        transit_peptide("transit_peptide"),
        tRNA("tRNA"),
        unsure("unsure"),
        V_region("V_region"),
        V_segment("V_segment"),
        variation("variation"),
        Three_Prime_UTR("3'UTR"),
        Five_Prime_UTR("5'UTR"),
        Minus_10_signal("-10_signal"),
        Minus_35_signal("-35_signal");

        private final String key;

        private FeatureKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }

        public static FeatureKey fromString(String key) {
            for (FeatureKey featureKey : values()) {
                if (featureKey.getKey().equals(key)) {
                    return featureKey;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    public static class FeatureHeader {

        private FeatureKey key;
        private Location location;

        public FeatureKey getKey() {
            return key;
        }

        public void setKey(FeatureKey key) {
            this.key = key;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.key != null ? this.key.hashCode() : 0);
            hash = 79 * hash + (this.location != null ? this.location.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FeatureHeader other = (FeatureHeader) obj;
            if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
                return false;
            }
            if (this.location != other.location && (this.location == null || !this.location.equals(other.location))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "FeatureHeader{" + "key=" + key + ", location=" + location + '}';
        }
    }

    public static class GenomicInterval implements Interval<Integer> {

        private Interval<java.lang.Integer> interval;
        private boolean leftopen;
        private boolean rightopen;

        public Interval<java.lang.Integer> getInterval() {
            return interval;
        }

        public void setInterval(Interval<java.lang.Integer> interval) {
            this.interval = interval;
        }

        public boolean isLeftopen() {
            return leftopen;
        }

        public void setLeftopen(boolean leftopen) {
            this.leftopen = leftopen;
        }

        public boolean isRightopen() {
            return rightopen;
        }

        public void setRightopen(boolean rightopen) {
            this.rightopen = rightopen;
        }

        @Override
        public Type getType() {
            return interval.getType();
        }

        @Override
        public java.lang.Integer getLength() {
            return interval.getLength();
        }

        @Override
        public java.lang.Integer getStart() {
            return interval.getStart();
        }

        @Override
        public java.lang.Integer getEnd() {
            return interval.getEnd();
        }

        @Override
        public GenomicInterval as(Type newType) {
            return createInterval(interval.as(newType), leftopen, rightopen);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + Objects.hashCode(this.interval);
            hash = 11 * hash + (this.leftopen ? 1 : 0);
            hash = 11 * hash + (this.rightopen ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GenomicInterval other = (GenomicInterval) obj;
            if (!Objects.equals(this.interval, other.interval)) {
                return false;
            }
            if (this.leftopen != other.leftopen) {
                return false;
            }
            if (this.rightopen != other.rightopen) {
                return false;
            }
            return true;
        }

        public static GenomicInterval createInterval(int start, int end) {
            return createInterval(start, end, Type.ZeroOpen);
        }

        public static GenomicInterval createInterval(int start, int end, Type type) {
            Interval<java.lang.Integer> createInterval = Intervals.createInterval(start, end, type);
            return createInterval(createInterval);
        }

        public static GenomicInterval createInterval(int start, int end, Type type, boolean leftOpen, boolean rightOpen) {
            Interval<java.lang.Integer> createInterval = Intervals.createInterval(start, end, type);
            return createInterval(createInterval, leftOpen, rightOpen);
        }

        public static GenomicInterval createInterval(Interval<java.lang.Integer> i) {
            GenomicInterval gi = new GenomicInterval();
            gi.setInterval(i);
            return gi;
        }

        public static GenomicInterval createInterval(Interval<java.lang.Integer> i, boolean leftOpen, boolean rightOpen) {
            GenomicInterval gi = createInterval(i);
            gi.setLeftopen(leftOpen);
            gi.setRightopen(rightOpen);
            return gi;
        }

        public static List<GenomicInterval> createIntervals(List<Interval<java.lang.Integer>> intervals) {
            List<GenomicInterval> list = new ArrayList<>(intervals.size());

            for (Interval<java.lang.Integer> interval : intervals) {
                list.add(createInterval(interval));
            }
            return list;
        }

        @Override
        public boolean isEmpty() {
            return interval.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("%d-%d", getStart(), getEnd());
        }
    }

    public static class Location {

        private List<GenomicInterval> locations;
        private boolean complement;
        private boolean incompleteStart;
        private boolean incompleteEnd;

        public boolean isIncompleteStart() {
            return incompleteStart;
        }

        public void setIncompleteStart(boolean incompleteStart) {
            this.incompleteStart = incompleteStart;
        }

        public boolean isIncompleteEnd() {
            return incompleteEnd;
        }

        public void setIncompleteEnd(boolean incompleteEnd) {
            this.incompleteEnd = incompleteEnd;
        }

        public List<GenomicInterval> getLocations() {
            return locations;
        }

        public void setLocations(List<GenomicInterval> locations) {
            this.locations = locations;
        }

        public boolean isComplement() {
            return complement;
        }

        public void setComplement(boolean complement) {
            this.complement = complement;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (this.locations != null ? this.locations.hashCode() : 0);
            hash = 79 * hash + (this.complement ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Location other = (Location) obj;
            if (this.locations != other.locations && (this.locations == null || !this.locations.equals(other.locations))) {
                return false;
            }
            if (this.complement != other.complement) {
                return false;
            }
            return true;
        }
    }
}
