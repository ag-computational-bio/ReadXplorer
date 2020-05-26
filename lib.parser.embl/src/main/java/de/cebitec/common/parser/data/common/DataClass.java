/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.common.parser.data.common;

/**
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public enum DataClass {
    CON, //		 Entry constructed from segment entry sequences; if unannotated,
    //annotation may be drawn from segment entries
    PAT, //            Patent
    EST, //            Expressed Sequence Tag
    GSS, //            Genome Survey Sequence
    HTC, //        High Thoughput CDNA sequencing
    HTG, //        High Thoughput Genome sequencing
    MGA, //        Mass Genome Annotation
    WGS, //        Whole Genome Shotgun
    TSA, //        Transcriptome Shotgun Assembly
    STS, //        Sequence Tagged Site
    STD
    
}
