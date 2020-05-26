/*
 * Copyright (C) 2015 Lukas Jelonek <Lukas.Jelonek at computational.bio.uni-giessen.de>
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

import de.cebitec.common.parser.data.common.DataClass;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.common.Topology;
import de.cebitec.common.parser.data.embl.EmblTaxonomicDivision;
import de.cebitec.common.parser.data.embl.Identification;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class IdentificationParserTest {

    @Test
    public void testIdentificationParser() {
        String idline = "X56734; SV 1; linear; mRNA; STD; PLN; 1859 BP.";
        EmblIdentificationParser p = new EmblIdentificationParser();
        Identification parse = p.parse(idline);
        assertThat(parse.getPrimaryAccession(), equalTo("X56734"));
        assertThat(parse.getSequenceVersion(), equalTo(1));
        assertThat(parse.getTopology(), equalTo(Topology.linear));
        assertThat(parse.getMolecular_type(), equalTo(MolecularType.mRNA));
        assertThat(parse.getData_class(), equalTo(DataClass.STD));
        assertThat((EmblTaxonomicDivision) parse.getTaxonomy_division(), equalTo(EmblTaxonomicDivision.Plant));
        assertThat(parse.getSequence_length(), equalTo(1859));

        assertThat(p.write(parse), equalTo(idline));
    }

}
