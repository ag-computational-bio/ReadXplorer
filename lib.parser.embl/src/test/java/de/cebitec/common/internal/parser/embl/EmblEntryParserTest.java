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

import com.google.common.base.Joiner;
import de.cebitec.common.parser.data.common.DataClass;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.common.Topology;
import de.cebitec.common.parser.data.embl.Accession;
import de.cebitec.common.parser.data.embl.Description;
import de.cebitec.common.parser.data.embl.EmblEntry;
import de.cebitec.common.parser.data.embl.EmblTaxonomicDivision;
import de.cebitec.common.parser.data.embl.Identification;
import de.cebitec.common.parser.data.embl.Keywords;
import de.cebitec.common.parser.data.embl.OrganismClassification;
import de.cebitec.common.parser.data.embl.OrganismSpecies;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class EmblEntryParserTest {

    @Test
    public void testEmblEntryParser() throws URISyntaxException, IOException {
        URL resource = getClass().getResource("test.embl");
        Path get = Paths.get(resource.toURI());
        List<String> readAllLines = Files.readAllLines(get, Charset.defaultCharset());
        String text = Joiner.on("\n").join(readAllLines);
        EmblEntryParser p = new EmblEntryParser();
        EmblEntry parse = p.parse(text);

        assertThat(parse.getIdentification(), equalTo(new Identification("X56734", 1, Topology.linear,
                                                                         MolecularType.mRNA, DataClass.STD,
                                                                         EmblTaxonomicDivision.Plant, 1859)));

        assertThat(parse.getAccession(), equalTo(new Accession(Arrays.asList("X56734", "S46826"))));
        assertThat(parse.getDescription(), equalTo(
                   new Description("Trifolium repens mRNA for non-cyanogenic beta-glucosidase")));
        assertThat(parse.getKeywords(), equalTo(new Keywords(Arrays.asList("beta-glucosidase"))));
        assertThat(parse.getSpecies(), equalTo(new OrganismSpecies("Trifolium repens", "white clover")));
        assertThat(parse.getClassification(), equalTo(new OrganismClassification(
                   Arrays.asList("Eukaryota", "Viridiplantae", "Streptophyta",
                                 "Embryophyta", "Tracheophyta", "Spermatophyta", "Magnoliophyta",
                                 "eudicotyledons", "core eudicotyledons", "rosids", "eurosids I",
                                 "Fabales", "Fabaceae", "Papilionoideae", "Trifolieae", "Trifolium"))));
        assertThat(parse.getFeatureList(), hasSize(3));
        assertThat(parse.getSequence().getSequence().length(), equalTo(1859));

        assertThat(p.write(parse), equalTo(text));

    }
    
      @Test
    public void testSplitLongLine() {
        String text = "Eukaryota; Viridiplantae; Streptophyta; Embryophyta; Tracheophyta;"
            + " Spermatophyta; Magnoliophyta; eudicotyledons; core eudicotyledons; rosids;"
            + " eurosids I; Fabales; Fabaceae; Papilionoideae; Trifolieae; Trifolium.";

        String expected = "Eukaryota; Viridiplantae; Streptophyta; Embryophyta; Tracheophyta;\n"
            + "Spermatophyta; Magnoliophyta; eudicotyledons; core eudicotyledons; rosids;\n"
            + "eurosids I; Fabales; Fabaceae; Papilionoideae; Trifolieae; Trifolium.";

        assertThat(EmblEntryParser.splitToLineLength(text, "[; ]+", 75), equalTo(expected));

        String text2 = "complement(join(471..713,772..908,985..1065,1127..1247,"
            + "1306..1561,1612..1754,1860..1981,2034..2326,2378..2454,"
            + "2519..2726,2782..2891,2944..3019,3253..3355,3405..3539,"
            + "3585..3604,3655..4531,4594..4994,5049..5253,5306..5468))";
        String expected2 = "complement(join(471..713,772..908,985..1065,1127..1247,\n"
            + "1306..1561,1612..1754,1860..1981,2034..2326,2378..2454,\n"
            + "2519..2726,2782..2891,2944..3019,3253..3355,3405..3539,\n"
            + "3585..3604,3655..4531,4594..4994,5049..5253,5306..5468))";

        assertThat(EmblEntryParser.splitToLineLength(text2, ",", 59), equalTo(expected2));

        String text3 = "/translation=\"MDFIVAIFALFVISSFTITSTNAVEASTLLDIGNLSRSSFPRGFI"
            + "FGAGSSAYQFEGAVNEGGRGPSIWDTFTHKYPEKIRDGSNADITVDQYHRYKEDVGIMK"
            + "DQNMDSYRFSISWPRILPKGKLSGGINHEGIKYYNNLINELLANGIQPFVTLFHWDLPQ"
            + "VLEDEYGGFLNSGVINDFRDYTDLCFKEFGDRVRYWSTLNEPWVFSNSGYALGTNAPGR"
            + "CSASNVAKPGDSGTGPYIVTHNQILAHAEAVHVYKTKYQAYQKGKIGITLVSNWLMPLD"
            + "DNSIPDIKAAERSLDFQFGLFMEQLTTGDYSKSMRRIVKNRLPKFSKFESSLVNGSFDF"
            + "IGINYYSSSYISNAPSHGNAKPSYSTNPMTNISFEKHGIPLGPRAASIWIYVYPYMFIQ"
            + "EDFEIFCYILKINITILQFSITENGMNEFNDATLPVEEALLNTYRIDYYYRHLYYIRSA"
            + "IRAGSNVKGFYAWSFLDCNEWFAGFTVRFGLNFVD\"";
        String expected3 = "/translation=\"MDFIVAIFALFVISSFTITSTNAVEASTLLDIGNLSRSSFPRGFI\n"
            + "FGAGSSAYQFEGAVNEGGRGPSIWDTFTHKYPEKIRDGSNADITVDQYHRYKEDVGIMK\n"
            + "DQNMDSYRFSISWPRILPKGKLSGGINHEGIKYYNNLINELLANGIQPFVTLFHWDLPQ\n"
            + "VLEDEYGGFLNSGVINDFRDYTDLCFKEFGDRVRYWSTLNEPWVFSNSGYALGTNAPGR\n"
            + "CSASNVAKPGDSGTGPYIVTHNQILAHAEAVHVYKTKYQAYQKGKIGITLVSNWLMPLD\n"
            + "DNSIPDIKAAERSLDFQFGLFMEQLTTGDYSKSMRRIVKNRLPKFSKFESSLVNGSFDF\n"
            + "IGINYYSSSYISNAPSHGNAKPSYSTNPMTNISFEKHGIPLGPRAASIWIYVYPYMFIQ\n"
            + "EDFEIFCYILKINITILQFSITENGMNEFNDATLPVEEALLNTYRIDYYYRHLYYIRSA\n"
            + "IRAGSNVKGFYAWSFLDCNEWFAGFTVRFGLNFVD\"";
        assertThat(EmblEntryParser.splitToLineLength(text3, "", 59), equalTo(expected3));

        String text4 = "Y00001; X00001-X00005; X00008; Z00001-Z00005; Y00001; X00001-X00005; "
            + "X00008; Z00001-Z00005;";
        String expected4 = "Y00001; X00001-X00005; X00008; Z00001-Z00005; Y00001; X00001-X00005;"
            + "\nX00008; Z00001-Z00005;";
        assertThat(EmblEntryParser.splitToLineLength(text4, "[; ]+", 75), equalTo(expected4));

        String text5 = "Y00001; X00001-X00005; X00008; Z00001-Z00005;";
        assertThat(EmblEntryParser.splitToLineLength(text5, "[; ]+", 75), equalTo(text5));

        String text6 = "/experiment=\"experimental evidence, no additional details recorded\"";
        String expected6 = "/experiment=\"experimental evidence, no additional details\n"
            + "recorded\"";
        assertThat(EmblEntryParser.splitToLineLength(text6, "[; \\-,]+", 59), equalTo(expected6));
    }
}
