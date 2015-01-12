/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.featuretableexport;


/**
 * Feature keys reference
 * <p>
 * The following has been organized according to the following format:
 * Feature Key the feature key name
 * Definition the definition of the key
 * Mandatory qualifiers qualifiers required with the key; if there are no
 * mandatory qualifiers, this field is omitted.
 * Optional qualifiers optional qualifiers associated with the key
 * Organism scope valid organisms for the key; if the scope is any
 * organism, this field is omitted.
 * Molecule scope valid molecule types; if the scope is any molecule
 * type, this field is omitted.
 * References citations of published reports, usually supporting the
 * feature consensus sequence
 * Comment comments and clarifications
 * Abbreviations:
 * accnum an entry primary accession number
 * <amino_acid> abbreviation for amino acid
 * <base_range> location descriptor for a simple range of bases
 * <bool> Boolean truth value. Valid values are yes and no
 * <integer> unsigned integer value
 * <location> general feature location descriptor
 * <modified_base> abbreviation for modified nucleotide base
 * [number] integer representing number of citation in entry's
 * reference list
 * <repeat_type> value indicating the organization of a repeated
 * sequence.
 * "text" any text or character string. Since the string is
 * delimited by double quotes, double quotes may only
 * appear as part of the string if they appear in pairs.
 * For example, the sentence:
 * <p>
 * The "label" qualifier is no longer legal.
 * <p>
 * would be formatted thus:
 * <p>
 * "The ""label"" qualifier is no longer legal."
 * <p>
 * @author jritter
 */
public enum FeatureKey {


//Feature Key           assembly_gap
//Definition            gap between two components of a genome or transcriptome assembly;
//Mandatory qualifiers  /estimated_length=unknown or <integer>
//		      /gap_type="TYPE"
//                      /linkage_evidence="TYPE" (Note: Mandatory only if the
//                      /gap_type is "within scaffold" or "repeat within
//                      scaffold".If there are multiple types of linkage_evidence
//                      they will appear as multiple /linkage_evidence="TYPE"
//                      qualifiers. For all other types of assembly_gap
//                      features, use of the /linkage_evidence qualifier is
//                      invalid.)
//                      Mandatory qualifiers under assembly_gap feature for transcriptome
//                      shotgun assemblies (TSA):
//                      /estimated_length=<integer>
//                      /gap_type="within scaffold" and /linkage_evidence="TYPE" where TYPE
//                      can not be "unspecified";
//
//Comment               the location span of the assembly_gap feature for an unknown gap has
//                      to be specified by the submitter; the specified gap length has to be
//                      reasonable (less or = 1000) and will be indicated as "n"'s in sequence.
//                      However, the value for the estimated_length of assembly_gap features
//                      within a single (non-CON) transcriptome record must be an integer
//                      and can not be "unknown";
    ASSEMBLY_GAP,
    //Feature Key           attenuator
    //Definition            1) region of DNA at which regulation of termination of
    //                         transcription occurs, which controls the expression
    //                         of some bacterial operons;
    //                      2) sequence segment located between the promoter and the
    //                         first structural gene that causes partial termination
    //                         of transcription
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /phenotype="text"
    //
    //Organism scope        prokaryotes
    //
    //Molecule scope        DNA

    ATTENUATOR,
    //Feature Key           C_region
    //Definition            constant region of immunoglobulin light and heavy
    //                      chains, and T-cell receptor alpha, beta, and gamma
    //                      chains; includes one or more exons depending on the
    //                      particular chain
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"

    C_REGION,
    //Parent Key            CDS
    //Organism scope        eukaryotes
    //Feature Key           CAAT_signal
    //Definition            CAAT box; part of a conserved sequence located about 75
    //                      bp up-stream of the start point of eukaryotic
    //                      transcription units which may be involved in RNA
    //                      polymerase binding; consensus=GG(C or T)CAATCT [1,2].
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Organism scope        eukaryotes and eukaryotic viruses
    //
    //Molecule scope        DNA
    //
    //References            [1]  Efstratiadis, A.  et al.  Cell 21, 653-668 (1980)
    //                      [2]  Nevins, J.R.  "The pathway of eukaryotic mRNA formation"
    //                           Ann Rev Biochem 52, 441-466 (1983)

    CAAT_SIGNAL,
    //Feature Key           CDS
    //
    //Definition            coding sequence; sequence of nucleotides that
    //                      corresponds with the sequence of amino acids in a
    //                      protein (location includes stop codon);
    //                      feature includes amino acid conceptual translation.
    //
    //Optional qualifiers   /allele="text"
    //                      /artificial_location="[artificial_location_value]"
    //                      /citation=[number]
    //                      /codon_start=<1 or 2 or 3>
    //                      /db_xref="<database>:<identifier>"
    //                      /EC_number="text"
    //                      /exception="[exception_value]"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /number=unquoted text (single token)
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /protein_id="<identifier>"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /ribosomal_slippage
    //                      /standard_name="text"
    //                      /translation="text"
    //                      /transl_except=(pos:<base_range>,aa:<amino_acid>)
    //                      /transl_table =<integer>
    //                      /trans_splicing
    //
    //Comment               /codon_start has valid value of 1 or 2 or 3, indicating
    //                      the offset at which the first complete codon of a coding
    //                      feature can be found, relative to the first base of
    //                      that feature;
    //                      /transl_table defines the genetic code table used if
    //                      other than the universal genetic code table;
    //                      genetic code exceptions outside the range of the specified
    //                      tables is reported in /transl_except qualifier;
    //                      /protein_id consists of a stable ID portion (3+5 format
    //                      with 3 position letters and 5 numbers) plus a version
    //                      number after the decimal point; when the protein
    //                      sequence encoded by the CDS changes, only the version
    //                      number of the /protein_id value is incremented; the
    //                      stable part of the /protein_id remains unchanged and as
    //                      a result will permanently be associated with a given
    //                      protein;

    CDS,
    //Feature Key           centromere
    //Definition            region of biological interest identified as a centromere and
    //                      which has been experimentally characterized;
    //
    //Optional qualifiers   /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    // 		      /inference="[CATEGORY:]TYPE[(same species)][:EVIDENCE_BASIS]"
    //                      /note="text"
    //                      /standard_name="text"
    //
    //Comment               the centromere feature describes the interval of DNA
    //                      that corresponds to a region where chromatids are held
    //                      and a kinetochore is formed

    CENTROMERE,
    //Feature Key           D-loop
    //Definition            displacement loop; a region within mitochondrial DNA in
    //                      which a short stretch of RNA is paired with one strand
    //                      of DNA, displacing the original partner DNA strand in
    //                      this region; also used to describe the displacement of a
    //                      region of one strand of duplex DNA by a single stranded
    //                      invader in the reaction catalyzed by RecA protein
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Molecule scope        DNA

    D_LOOP,
    //Feature Key           D_segment
    //
    //
    //Definition            Diversity segment of immunoglobulin heavy chain, and
    //                      T-cell receptor beta chain;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            CDS
    //
    //Organism scope        eukaryotes

    D_SEGMENT,
    //Feature Key           enhancer
    //
    //
    //Definition            a cis-acting sequence that increases the utilization of
    //                      (some)  eukaryotic promoters, and can function in either
    //                      orientation and in any location (upstream or downstream)
    //                      relative to the promoter;
    //
    //Optional qualifiers   /allele="text"
    //                      /bound_moiety="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //Organism scope        eukaryotes and eukaryotic viruses

    ENHANCER,
    //Feature Key           exon
    //
    //
    //Definition            region of genome that codes for portion of spliced mRNA,
    //                      rRNA and tRNA; may contain 5'UTR, all CDSs and 3' UTR;
    //
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /EC_number="text"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /number=unquoted text (single token)
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing

    EXON,
    //Feature Key           gap
    //
    //Definition            gap in the sequence
    //
    //Mandatory qualifiers  /estimated_length=unknown or <integer>
    //
    //Optional qualifiers   /experiment="[CATEGORY:]text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /map="text"
    //                      /note="text"
    //
    //Comment               the location span of the gap feature for an unknown
    //                      gap is 100 bp, with the 100 bp indicated as 100 "n"'s in
    //                      the sequence.  Where estimated length is indicated by
    //                      an integer, this is indicated by the same number of
    //                      "n"'s in the sequence.
    //                      No upper or lower limit is set on the size of the gap.

    GAP,
    //Feature Key           GC_signal
    //
    //
    //Definition            GC box; a conserved GC-rich region located upstream of
    //                      the start point of eukaryotic transcription units which
    //                      may occur in multiple copies or in either orientation;
    //                      consensus=GGGCGG;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Organism scope        eukaryotes and eukaryotic viruses

    GC_SIGNAL,
    //Feature Key           gene
    //
    //
    //Definition            region of biological interest identified as a gene
    //                      and for which a name has been assigned;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /phenotype="text"
    //                      /standard_name="text"
    //                      /trans_splicing
    //
    //
    //Comment               the gene feature describes the interval of DNA that
    //                      corresponds to a genetic trait or phenotype; the feature is,
    //                      by definition, not strictly bound to it's positions at the
    //                      ends;  it is meant to represent a region where the gene is
    //                      located.

    GENE,
    //Feature Key           iDNA
    //
    //
    //Definition            intervening DNA; DNA which is eliminated through any of
    //                      several kinds of recombination;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /number=unquoted text (single token)
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //Molecule scope        DNA
    //
    //Comment               e.g., in the somatic processing of immunoglobulin genes.

    I_DNA,
    //Feature Key           intron
    //
    //
    //Definition            a segment of DNA that is transcribed, but removed from
    //                      within the transcript by splicing together the sequences
    //                      (exons) on either side of it;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /number=unquoted text (single token)
    //                      /old_locus_tag="text" (single token)
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing

    INTRON,
    //Feature Key           J_segment
    //
    //
    //Definition            joining segment of immunoglobulin light and heavy
    //                      chains, and T-cell receptor alpha, beta, and gamma
    //                      chains;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            CDS
    //
    //Organism scope        eukaryotes

    J_SEGMENT,
    //Feature Key           LTR
    //
    //
    //Definition            long terminal repeat, a sequence directly repeated at
    //                      both ends of a defined sequence, of the sort typically
    //                      found in retroviruses;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"

    LTR,
    //Feature Key           mat_peptide
    //
    //
    //Definition            mature peptide or protein coding sequence; coding
    //                      sequence for the mature or final peptide or protein
    //                      product following post-translational modification; the
    //                      location does not include the stop codon (unlike the
    //                      corresponding CDS);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /EC_number="text"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"

    MAT_PEPTIDE,
    //Feature Key           misc_binding
    //
    //
    //Definition            site in nucleic acid which covalently or non-covalently
    //                      binds another moiety that cannot be described by any
    //                      other binding key (primer_bind or protein_bind);
    //
    //Mandatory qualifiers  /bound_moiety="text"
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Comment               note that the key RBS is used for ribosome binding sites

    MISC_BINDING,
    //Feature Key           misc_difference
    //
    //
    //Definition            feature sequence is different from that presented
    //                      in the entry and cannot be described by any other
    //                      Difference key (unsure, old_sequence,
    //                      variation, or modified_base);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /clone="text"
    //                      /compare=[accession-number.sequence-version]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /phenotype="text"
    //                      /replace="text"
    //                      /standard_name="text"
    //
    //Comment               the misc_difference feature key should be used to
    //                      describe variability that arises as a result of
    //                      genetic manipulation (e.g. site directed mutagenesis);
    //                      use /replace="" to annotate deletion, e.g.
    //                      misc_difference 412..433
    //                                      /replace=""
    MISC_DIFFERENCE,
    //Feature Key           misc_feature
    //
    //
    //Definition            region of biological interest which cannot be described
    //                      by any other feature key; a new or rare feature;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /number=unquoted text (single token)
    //                      /old_locus_tag="text" (single token)
    //                      /phenotype="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Comment               this key should not be used when the need is merely to
    //                      mark a region in order to comment on it or to use it in
    //                      another feature's location

    MISC_FEATURE,
    //Feature Key           misc_recomb
    //
    //Definition            site of any generalized, site-specific or replicative
    //                      recombination event where there is a breakage and
    //                      reunion of duplex DNA that cannot be described by other
    //                      recombination keys or qualifiers of source key
    //                      (/proviral);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //Molecule scope        DNA

    MISC_RECOMB,
    //Feature Key           misc_RNA
    //
    //
    //Definition            any transcript or RNA product that cannot be defined by
    //                      other RNA keys (prim_transcript, precursor_RNA, mRNA,
    //                      5'UTR, 3'UTR, exon, CDS, sig_peptide, transit_peptide,
    //                      mat_peptide, intron, polyA_site, ncRNA, rRNA and tRNA);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing

    MISC_RNA,
    //Feature Key           misc_signal
    //
    //
    //Definition            any region containing a signal controlling or altering
    //                      gene function or expression that cannot be described by
    //                      other signal keys (promoter, CAAT_signal, TATA_signal,
    //                      -35_signal, -10_signal, GC_signal, RBS, polyA_signal,
    //                      enhancer, attenuator, terminator, and rep_origin).
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /phenotype="text"
    //                      /standard_name="text"
    MISC_SIGNAL,
    //Feature Key           misc_structure
    //
    //
    //Definition            any secondary or tertiary nucleotide structure or
    //                      conformation that cannot be described by other Structure
    //                      keys (stem_loop and D-loop);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"

    MISC_STRUCTURE,
    //Feature Key           mobile_element
    //
    //
    //Definition            region of genome containing mobile elements;
    //
    //Mandatory qualifiers  /mobile_element_type="<mobile_element_type>
    //                      [:<mobile_element_name>]"
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /rpt_family="text"
    //                      /rpt_type=<repeat_type>
    //                      /standard_name="text"

    MOBILE_ELEMENT,
    //Feature Key           modified_base
    //
    //
    //Definition            the indicated nucleotide is a modified nucleotide and
    //                      should be substituted for by the indicated molecule
    //                      (given in the mod_base qualifier value)
    //
    //Mandatory qualifiers  /mod_base=<modified_base>
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /frequency="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Comment               value is limited to the restricted vocabulary for
    //                      modified base abbreviations;

    MODIFIED_BASE,
    //Feature Key           mRNA
    //
    //
    //Definition            messenger RNA; includes 5'untranslated region (5'UTR),
    //                      coding sequences (CDS, exon) and 3'untranslated region
    //                      (3'UTR);
    //
    //Optional qualifiers   /allele="text"
    //                      /artificial_location="[artificial_location_value]"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing
    MRNA,
    //Feature Key           ncRNA
    //
    //Definition            a non-protein-coding gene, other than ribosomal RNA and
    //                      transfer RNA, the functional molecule of which is the RNA
    //                      transcript;
    //
    //Mandatory qualifiers  /ncRNA_class="TYPE"
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing
    //
    //Example               /ncRNA_class="miRNA"
    //                      /ncRNA_class="siRNA"
    //                      /ncRNA_class="scRNA"
    //
    //Comment               the ncRNA feature is not used for ribosomal and transfer
    //                      RNA annotation, for which the rRNA and tRNA feature keys
    //                      should be used, respectively;

    NC_RNA,
    //Feature Key           N_region
    //
    //
    //Definition            extra nucleotides inserted between rearranged
    //                      immunoglobulin segments.
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            CDS
    //
    //Organism scope        eukaryotes

    N_REGION,
    //Feature Key           old_sequence
    //
    //
    //Definition            the presented sequence revises a previous version of the
    //                      sequence at this location;
    //
    //Mandatory qualifiers  /citation=[number]
    //                      Or
    //                      /compare=[accession-number.sequence-version]
    //
    //Optional qualifiers   /allele="text"
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /replace="text"
    //
    //Comment               /replace="" is used to annotate deletion, e.g.
    //                      old_sequence 12..15
    //                      /replace=""
    //                      NOTE: This feature key is not valid in entries/records
    //                      created from 15-Oct-2007.

    OLD_SEQUENCE,
    //Feature Key           operon
    //
    //Definition            region containing polycistronic transcript including a cluster of
    //                      genes that are under the control of the same regulatory sequences/promotor
    //                      and in the same biological pathway
    //
    //Mandatory qualifiers  /operon="text"
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /map="text"
    //                      /note="text"
    //                      /phenotype="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"

    OPERON,
    //Feature Key           oriT
    //Definition            origin of transfer; region of a DNA molecule where transfer is
    //                      initiated during the process of conjugation or mobilization
    //
    //Optional qualifiers   /allele="text"
    //                      /bound_moiety="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /direction=value
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /rpt_family="text"
    //                      /rpt_type=<repeat_type>
    //                      /rpt_unit_range=<base_range>
    //                      /rpt_unit_seq="text"
    //                      /standard_name="text"
    //
    //Molecule Scope        DNA
    //
    //Comment               rep_origin should be used for origins of replication;
    //                      /direction has legal values RIGHT, LEFT and BOTH, however only
    //                      RIGHT and LEFT are valid when used in conjunction with the oriT
    //                      feature;
    //                      origins of transfer can be present in the chromosome;
    //                      plasmids can contain multiple origins of transfer

    ORI_T,
    //Feature Key           polyA_signal
    //
    //
    //Definition            recognition region necessary for endonuclease cleavage
    //                      of an RNA transcript that is followed by polyadenylation;
    //                      consensus=AATAAA [1];
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Organism scope        eukaryotes and eukaryotic viruses
    //
    //References            [1] Proudfoot, N. and Brownlee, G.G. Nature 263, 211-214
    //                      (1976)
    POLY_A_SIGNAL,
    //Feature Key           polyA_site
    //
    //
    //Definition            site on an RNA transcript to which will be added adenine
    //                      residues by post-transcriptional polyadenylation;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Organism scope        eukaryotes and eukaryotic viruses

    POLY_A_SITE,
    //Feature Key           precursor_RNA
    //
    //
    //Definition            any RNA species that is not yet the mature RNA product;
    //                      may include 5' untranslated region (5'UTR), coding
    //                      sequences (CDS, exon), intervening sequences (intron)
    //                      and 3' untranslated region (3'UTR);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /standard_name="text"
    //                      /trans_splicing
    //
    //Comment               used for RNA which may be the result of
    //                      post-transcriptional processing;  if the RNA in question
    //                      is known not to have been processed, use the
    //                      prim_transcript key.
    PRECURSER_RNA,
    //Feature Key           prim_transcript
    //
    //
    //Definition            primary (initial, unprocessed) transcript;  includes 5'
    //                      untranslated region (5'UTR), coding sequences
    //                      (CDS, exon), intervening sequences (intron) and 3'
    //                      untranslated region (3'UTR);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"

    PRIM_TRANSCRIPT,
    //Feature Key           primer_bind
    //
    //
    //Definition            non-covalent primer binding site for initiation of
    //                      replication, transcription, or reverse transcription;
    //                      includes site(s) for synthetic e.g., PCR primer elements;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //                      /PCR_conditions="text"
    //
    //Comment               used to annotate the site on a given sequence to which a primer
    //                      molecule binds - not intended to represent the sequence of the
    //                      primer molecule itself; PCR components and reaction times may
    //                      be stored under the "/PCR_conditions" qualifier;
    //                      since PCR reactions most often involve pairs of primers,
    //                      a single primer_bind key may use the order() operator
    //                      with two locations, or a pair of primer_bind keys may be
    //                      used.

    PRIM_BIND,
    //Feature Key           promoter
    //
    //
    //Definition            region on a DNA molecule involved in RNA polymerase
    //                      binding to initiate transcription;
    //
    //Optional qualifiers   /allele="text"
    //                      /bound_moiety="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /phenotype="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Molecule scope        DNA

    PROMOTER,
    //Feature Key           protein_bind
    //
    //
    //Definition            non-covalent protein binding site on nucleic acid;
    //
    //Mandatory qualifiers  /bound_moiety="text"
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"
    //
    //Comment               note that RBS is used for ribosome binding sites.

    PROTEIN_BIND,
    //Feature Key           RBS
    //
    //
    //Definition            ribosome binding site;
    //
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //References            [1] Shine, J. and Dalgarno, L.  Proc Natl Acad Sci USA
    //                          71, 1342-1346 (1974)
    //                      [2] Gold, L. et al.  Ann Rev Microb 35, 365-403 (1981)
    //
    //Comment               in prokaryotes, known as the Shine-Dalgarno sequence: is
    //                      located 5 to 9 bases upstream of the initiation codon;
    //                      consensus GGAGGT [1,2].

    RBS,
    //Feature Key           repeat_region
    //
    //
    //Definition            region of genome containing repeating units;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /rpt_family="text"
    //                      /rpt_type=<repeat_type>
    //                      /rpt_unit_range=<base_range>
    //                      /rpt_unit_seq="text"
    //                      /satellite="<satellite_type>[:<class>][ <identifier>]"
    //                      /standard_name="text"

    REPEAT_REGION,
    //Feature Key           rep_origin
    //
    //
    //Definition            origin of replication; starting site for duplication of
    //                      nucleic acid to give two identical copies;
    //
    //Optional Qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /direction=value
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //Comment               /direction has valid values: RIGHT, LEFT, or BOTH.

    REP_ORIGIN,
    //Feature Key           rRNA
    //
    //
    //Definition            mature ribosomal RNA; RNA component of the
    //                      ribonucleoprotein particle (ribosome) which assembles
    //                      amino acids into proteins.
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Comment               rRNA sizes should be annotated with the /product
    //                      Qualifier.

    R_RNA,
    //Feature Key           S_region
    //
    //
    //Definition            switch region of immunoglobulin heavy chains;
    //                      involved in the rearrangement of heavy chain DNA leading
    //                      to the expression of a different immunoglobulin class
    //                      from the same B-cell;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            misc_signal
    //
    //Organism scope        eukaryotes

    S_REGION,
    //Feature Key           sig_peptide
    //
    //
    //Definition            signal peptide coding sequence; coding sequence for an
    //                      N-terminal domain of a secreted protein; this domain is
    //                      involved in attaching nascent polypeptide to the
    //                      membrane leader sequence;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"

    SIG_PEPTIDE,
    //Feature Key           source
    //
    //
    //Definition            identifies the biological source of the specified span of
    //                      the sequence; this key is mandatory; more than one source
    //                      key per sequence is allowed; every entry/record will have, as a
    //                      minimum, either a single source key spanning the entire
    //                      sequence or multiple source keys, which together, span the
    //                      entire sequence.
    //
    //Mandatory qualifiers  /organism="text"
    //                      /mol_type="genomic DNA", "genomic RNA", "mRNA", "tRNA",
    //                                "rRNA", "other RNA", "other DNA", "transcribed
    //                                RNA", "viral cRNA", "unassigned DNA",
    //                                "unassigned RNA"
    //
    //
    //Optional qualifiers   /altitude="text"
    //                      /bio_material="[<institution-code>:[<collection-code>:]]<material_id>"
    //                      /cell_line="text"
    //                      /cell_type="text"
    //                      /chromosome="text"
    //                      /citation=[number]
    //                      /clone="text"
    //                      /clone_lib="text"
    //                      /collected_by="text"
    //                      /collection_date="text"
    //                      /country="<country_value>[:<region>][, <locality>]"
    //                      /cultivar="text"
    //                      /culture_collection="<institution-code>:[<collection-code>:]<culture_id>"
    //                      /db_xref="<database>:<identifier>"
    //                      /dev_stage="text"
    //                      /ecotype="text"
    //                      /environmental_sample
    //                      /focus
    //                      /germline
    //                      /haplogroup="text"
    //                      /haplotype="text"
    //                      /host="text"
    //                      /identified_by="text"
    //                      /isolate="text"
    //                      /isolation_source="text"
    //                      /lab_host="text"
    //                      /lat_lon="text"
    //                      /macronuclear
    //                      /map="text"
    //                      /mating_type="text"
    //                      /note="text"
    //                      /organelle=<organelle_value>
    //                      /PCR_primers="[fwd_name: XXX, ]fwd_seq: xxxxx,
    //                      [rev_name: YYY, ]rev_seq: yyyyy"
    //                      /plasmid="text"
    //                      /pop_variant="text"
    //                      /proviral
    //                      /rearranged
    //                      /segment="text"
    //                      /serotype="text"
    //                      /serovar="text"
    //                      /sex="text"
    //                      /specimen_voucher="[<institution-code>:[<collection-code>:]]<specimen_id>"
    //                      /strain="text"
    //                      /sub_clone="text"
    //                      /sub_species="text"
    //                      /sub_strain="text"
    //                      /tissue_lib="text"
    //                      /tissue_type="text"
    //                      /transgenic
    //                      /type_material="<type-of-type> of <organism name>"
    //                      /variety="text"
    //
    //Molecule scope        any
    //
    //Comment               transgenic sequences must have at least two source feature
    //                      keys; in a transgenic sequence the source feature key
    //                      describing the organism that is the recipient of the DNA
    //                      must span the entire sequence;
    //                      see Appendix IV /organelle for a list of <organelle_value>

    SOURCE,
    //Feature Key           stem_loop
    //
    //
    //Definition            hairpin; a double-helical region formed by base-pairing
    //                      between adjacent (inverted) complementary sequences in a
    //                      single strand of RNA or DNA.
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"

    STEM_LOOP,
    //Feature Key           STS
    //
    //Definition            sequence tagged site; short, single-copy DNA sequence
    //                      that characterizes a mapping landmark on the genome and
    //                      can be detected by PCR; a region of the genome can be
    //                      mapped by determining the order of a series of STSs;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //
    //Molecule scope        DNA
    //
    //Parent key            misc_binding
    //
    //Comment               STS location to include primer(s) in primer_bind key or
    //                      primers.

    STS,
    //Feature Key           TATA_signal
    //
    //
    //Definition            TATA box; Goldberg-Hogness box; a conserved AT-rich
    //                      septamer found about 25 bp before the start point of
    //                      each eukaryotic RNA polymerase II transcript unit which
    //                      may be involved in positioning the enzyme  for correct
    //                      initiation; consensus=TATA(A or T)A(A or T) [1,2];
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //
    //Organism scope        eukaryotes and eukaryotic viruses
    //
    //Molecule scope        DNA
    //
    //References            [1] Efstratiadis, A.  et al.  Cell 21, 653-668 (1980)
    //                      [2] Corden, J., et al.  "Promoter sequences of
    //                          eukaryotic protein-encoding genes"  Science 209,
    //                          1406-1414 (1980)
    TATA_SIGNAL,
    //Feature Key           telomere
    //
    //Definition            region of biological interest identified as a telomere
    //                      and which has been experimentally characterized;
    //
    //Optional qualifiers   /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"/note="text"
    //                      /inference="[CATEGORY:]TYPE[(same species)][:EVIDENCE_BASIS]"
    //                      /note="text"
    //                      /rpt_type=<repeat_type>
    //                      /rpt_unit_range=<base_range>
    //                      /rpt_unit_seq="text"
    //                      /standard_name="text"
    //
    //Comment               the telomere feature describes the interval of DNA
    //                      that corresponds to a specific structure at the end of
    //                      the linear eukaryotic chromosome which is required for
    //		      the integrity and maintenance of the end; this region
    //                      is unique compared to the rest of the chromosome and
    //                      represent the physical end of the chromosome;

    TELOMERE,
    //Feature Key           terminator
    //
    //
    //Definition            sequence of DNA located either at the end of the
    //                      transcript that causes RNA polymerase to terminate
    //                      transcription;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"
    //
    //Molecule scope        DNA

    TERMINATOR,
    //Feature Key           tmRNA
    //
    //Definition            transfer messenger RNA; tmRNA acts as a tRNA first,
    //                      and then as an mRNA that encodes a peptide tag; the
    //                      ribosome translates this mRNA region of tmRNA and attaches
    //                      the encoded peptide tag to the C-terminus of the
    //                      unfinished protein; this attached tag targets the protein for
    //                      destruction or proteolysis;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /tag_peptide=<base_range>

    TM_RNA,
    //Feature Key           transit_peptide
    //
    //
    //Definition            transit peptide coding sequence; coding sequence for an
    //                      N-terminal domain of a nuclear-encoded organellar
    //                      protein; this domain is involved in post-translational
    //                      import of the protein into the organelle;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"

    TRANSIT_PEPTIDE,
    //Feature Key           tRNA
    //
    //
    //Definition            mature transfer RNA, a small RNA molecule (75-85 bases
    //                      long) that mediates the translation of a nucleic acid
    //                      sequence into an amino acid sequence;
    //
    //Optional qualifiers   /allele="text"
    //                      /anticodon=(pos:<location>,aa:<amino_acid>,seq:<text>)
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //                      /trans_splicing

    T_RNA,
    //Feature Key           unsure
    //
    //
    //Definition            author is unsure of exact sequence in this region;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /compare=[accession-number.sequence-version]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /replace="text"
    //
    //Comment               use /replace="" to annotate deletion, e.g.
    //                      Unsure      11..15
    //                                  /replace=""

    UNSURE,
    //Feature Key           V_region
    //
    //
    //Definition            variable region of immunoglobulin light and heavy
    //                      chains, and T-cell receptor alpha, beta, and gamma
    //                      chains;  codes for the variable amino terminal portion;
    //                      can be composed of V_segments, D_segments, N_regions,
    //                      and J_segments;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            CDS
    //
    //Organism scope        eukaryotes

    V_REGION,
    //Feature Key           V_segment
    //
    //
    //Definition            variable segment of immunoglobulin light and heavy
    //                      chains, and T-cell receptor alpha, beta, and gamma
    //                      chains; codes for most of the variable region (V_region)
    //                      and the last few amino acids of the leader peptide;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /product="text"
    //                      /pseudo
    //                      /pseudogene="TYPE"
    //                      /standard_name="text"
    //
    //Parent Key            CDS
    //
    //Organism scope        eukaryotes

    V_SEGMENT,
    //Feature Key           variation
    //
    //Definition            a related strain contains stable mutations from the same
    //                      gene (e.g., RFLPs, polymorphisms, etc.) which differ
    //                      from the presented sequence at this location (and
    //                      possibly others);
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /compare=[accession-number.sequence-version]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /frequency="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /phenotype="text"
    //                      /product="text"
    //                      /replace="text"
    //                      /standard_name="text"
    //
    //Comment               used to describe alleles, RFLP's,and other naturally occurring
    //                      mutations and  polymorphisms; variability arising as a result
    //                      of genetic manipulation (e.g. site directed mutagenesis) should
    //                      be described with the misc_difference feature;
    //                      use /replace="" to annotate deletion, e.g.
    //                      variation   4..5
    //                                  /replace=""

    VARIATION,
    //Feature Key           3'UTR
    //
    //
    //Definition            1) region at the 3' end of a mature transcript (following
    //                      the stop codon) that is not translated into a protein;
    //                      2) region at the 3' end of an RNA virus (following the last stop
    //                      codon) that is not translated into a protein;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //                      /trans_splicing

    ThreeUTR,
    //Feature Key           5'UTR
    //
    //
    //Definition            1) region at the 5' end of a mature transcript (preceding
    //                      the initiation codon) that is not translated into a protein;
    //                      2) region at the 5' end of an RNA virus genome (preceding the first
    //                      initiation codon) that is not translated into a protein;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /function="text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /standard_name="text"
    //                      /trans_splicing

    FiveUTR,
    //Feature Key           -10_signal
    //
    //
    //Definition            Pribnow box; a conserved region about 10 bp upstream of
    //                      the start point of bacterial transcription units which
    //                      may be involved in binding RNA polymerase;
    //                      consensus=TAtAaT [1,2,3,4];
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"
    //
    //Organism scope        prokaryotes
    //
    //Molecule scope        DNA
    //
    //References            [1] Schaller, H., Gray, C., and Hermann, K.  Proc Natl
    //                          Acad Sci USA 72, 737-741 (1974)
    //                      [2] Pribnow, D.  Proc Natl Acad Sci USA 72, 784-788 (1974)
    //                      [3] Hawley, D.K. and McClure, W.R.  "Compilation and
    //                          analysis of Escherichia coli promoter DNA sequences"
    //                          Nucl Acid Res 11, 2237-2255 (1983)
    //                      [4] Rosenberg, M. and Court, D.  "Regulatory sequences
    //                          involved in the promotion and termination of RNA
    //                          transcription"  Ann Rev Genet 13, 319-353 (1979)

    MINUS_TEN_SIGNAL,
    //Feature Key           -35_signal
    //
    //
    //Definition            a conserved hexamer about 35 bp upstream of the start
    //                      point of bacterial transcription units; consensus=TTGACa
    //                      or TGTTGACA;
    //
    //Optional qualifiers   /allele="text"
    //                      /citation=[number]
    //                      /db_xref="<database>:<identifier>"
    //                      /experiment="[CATEGORY:]text"
    //                      /gene="text"
    //                      /gene_synonym="text"
    //                      /inference="[CATEGORY:]TYPE[ (same species)][:EVIDENCE_BASIS]"
    //                      /locus_tag="text" (single token)
    //                      /map="text"
    //                      /note="text"
    //                      /old_locus_tag="text" (single token)
    //                      /operon="text"
    //                      /standard_name="text"
    //
    //Organism scope        prokaryotes
    //
    //Molecule scope        DNA
    //
    //References            [1] Takanami, M., et al.  Nature 260, 297-302 (1976)
    //                      [2] Moran, C.P., Jr., et al.  Molec Gen Genet 186,
    //                          339-346 (1982)
    //                      [3] Maniatis, T., et al.  Cell 5, 109-113 (1975)

    MINUS_THIRTYFIVE_SIGNAL,

}
