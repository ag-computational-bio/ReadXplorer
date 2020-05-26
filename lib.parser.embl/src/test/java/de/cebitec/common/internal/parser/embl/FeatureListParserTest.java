/*
 * Copyright (C) 2015 Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.parser.data.embl.Feature;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class FeatureListParserTest {

    @Test
    public void testFeatureListParser() {
        String text
            = "gene            complement(1226208..1226306)\n"
            + "                /locus_tag=\"ACP_1063\"\n"
            + "CDS             complement(1226208..1226306)\n"
            + "                /codon_start=1\n"
            + "                /transl_table=11\n"
            + "                /locus_tag=\"ACP_1063\"\n"
            + "                /product=\"hypothetical protein\"\n"
            + "                /note=\"identified by glimmer; putative\"\n"
            + "                /db_xref=\"UniProtKB/TrEMBL:C1F436\"\n"
            + "                /protein_id=\"ACO33551.1\"\n"
            + "                /translation=\"MPLSWQECEGFLYQGTASAVPLSLQKEAGFSP\"\n";

        FeatureListParser p = new FeatureListParser();
        List<Feature> parse = p.parse(text);
        assertThat(parse, hasSize(2));
        Feature gene = parse.get(0);
        assertThat(gene.getHeader().getKey(), equalTo(Feature.FeatureKey.gene));
        assertTrue(gene.getQualifiers().containsEntry(Feature.Qualifier.locus_tag, "ACP_1063"));

        Feature cds = parse.get(1);
        assertThat(cds.getHeader().getKey(), equalTo(Feature.FeatureKey.CDS));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.codon_start, 1));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.transl_table, 11));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.locus_tag, "ACP_1063"));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.product, "hypothetical protein"));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.note, "identified by glimmer; putative"));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.db_xref, "UniProtKB/TrEMBL:C1F436"));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.protein_id, "ACO33551.1"));
        assertTrue(cds.getQualifiers().containsEntry(Feature.Qualifier.translation, "MPLSWQECEGFLYQGTASAVPLSLQKEAGFSP"));

        assertThat(p.write(parse), equalTo(text));

        String text2 = "CDS             complement(3710..4477)\n"
            + "                /codon_start=1\n"
            + "                /transl_table=11\n"
            + "                /locus_tag=\"ACP_0005\"\n"
            + "                /product=\"hypothetical protein\"\n"
            + "                /note=\"identified by glimmer; putative\"\n"
            + "                /db_xref=\"UniProtKB/TrEMBL:C1F7W2\"\n"
            + "                /protein_id=\"ACO31873.1\"\n"
            + "                /translation=\"MHLSLAARVLWAAGFTEQAALLLVLLLRKRFRAFPIFTIWIAFLV\n"
            + "                LKNVVLFAVLGSLREYYYTYWLAEIIDLLLQIGVIYEICRNVLRPTGTWVRSALRSFLV\n"
            + "                FGVAGIVLAAGLAWLAHPAESSFLGTWIERGRLFSSLITLELFVAMGFSSTRLGLVWRN\n"
            + "                HVMAITTGWALWAAIGFAEELASAYHGPDYHGIVLDQIRIFSTQAVTVYWVAMFWLNEP\n"
            + "                AERKLSPDMQIYLSNMQRRLESDAEILSNLRKP\"\n";

        assertThat(p.write(p.parse(text2)), equalTo(text2));

        String text3 = "CDS             complement(join(471..713,772..908,985..1065,1127..1247,\n"
            + "                1306..1561,1612..1754,1860..1981,2034..2326,2378..2454,\n"
            + "                2519..2726,2782..2891,2944..3019,3253..3355,3405..3539,\n"
            + "                3585..3604,3655..4531,4594..4994,5049..5253,5306..5468))\n";
        List<Feature> parse3 = p.parse(text3);
        assertThat(parse3.size(), equalTo(1));
        Feature cds3 = parse3.get(0);
        assertThat(cds3.getHeader(), notNullValue());
        assertThat(cds3.getHeader().getKey(), equalTo(Feature.FeatureKey.CDS));
        assertThat(p.write(p.parse(text3)), equalTo(text3));
    }

}
