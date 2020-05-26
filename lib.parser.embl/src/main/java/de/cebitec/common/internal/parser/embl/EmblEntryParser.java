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
package de.cebitec.common.internal.parser.embl;

import com.google.common.base.Joiner;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.parser.data.embl.EmblEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class EmblEntryParser extends AbstractStringWriter<EmblEntry> implements Parser<EmblEntry> {

    private static final Logger logger = Logger.getLogger(EmblEntryParser.class.getName());

    private final EmblIdentificationParser idParser = new EmblIdentificationParser();
    private final AccessionParser accessionParser = new AccessionParser();
    private final ProjectParser projectParser = new ProjectParser();
    private final KeywordsParser keywordsParser = new KeywordsParser();
    private final OrganismSpeciesParser organismSpeciesParser = new OrganismSpeciesParser();
    private final OrganismClassificationParser organismClassificationParser = new OrganismClassificationParser();
    private final DescriptionParser descriptionParser = new DescriptionParser();
    private final FeatureListParser featureListParser = new FeatureListParser();
    private final SequenceParser sequenceParser = new SequenceParser();
    private final DateParser dateParser = new DateParser();
    private final CommentParser commentParser = new CommentParser();

    @Override
    public EmblEntry parse(CharSequence data) {
        String[] lines = data.toString().split("\n");
        EmblEntry entry = new EmblEntry();
        String currentPrefix = null;
        List<String> linesForPrefix = null;
        for (String line : lines) {
            // trim first 5 characters
            String prefix;
            String suffix;
            if (line.length() > 5) {
                prefix = line.substring(0, 5).trim();
                suffix = line.substring(5);
            } else {
                prefix = line;
                suffix = "";
            }
            if (prefix.equals(currentPrefix) || ("SQ".equals(currentPrefix) && prefix.isEmpty())) {
                linesForPrefix.add(suffix);
            } else {
                if (currentPrefix != null && linesForPrefix != null) {
                    processDataset(currentPrefix, linesForPrefix, entry);
                }
                currentPrefix = prefix;
                linesForPrefix = new LinkedList<>();
                linesForPrefix.add(suffix);
            }
        }
        return entry;
    }

    @Override
    public String write(EmblEntry data) {
        StringBuilder sb = new StringBuilder();
        addAllLines(sb, "ID", idParser.write(data.getIdentification()));
        addAllLines(sb, "AC", accessionParser.write(data.getAccession()));
        addAllLines(sb, "DT", dateParser.write(data.getDate()));
        if (data.getProject() != null) {
            addAllLines(sb, "PR", projectParser.write(data.getProject()));
        }
        addAllLines(sb, "DE", descriptionParser.write(data.getDescription()));
        addAllLines(sb, "KW", keywordsParser.write(data.getKeywords()));
        addAllLines(sb, "OS", organismSpeciesParser.write(data.getSpecies()), false);
        addAllLines(sb, "OC", organismClassificationParser.write(data.getClassification()));
        if (data.getComment() != null) {
            addAllLines(sb, "CC", commentParser.write(data.getComment()));
        }
        addAllLines(sb, "FH", "Key             Location/Qualifiers", false);
        sb.append("FH\n");
        addAllLines(sb, "FT", featureListParser.write(data.getFeatureList()));
        sb.append(String.format("%-5s", "SQ")).append(sequenceParser.write(data.getSequence()));
        sb.append("\n");
        sb.append("//");
        return sb.toString();
    }

    public void addAllLines(StringBuilder sb, String tag, String lines) {
        addAllLines(sb, tag, lines, 80, true);
    }

    public void addAllLines(StringBuilder sb, String tag, String lines, boolean endWithXX) {
        addAllLines(sb, tag, lines, 80, endWithXX);
    }

    public void addAllLines(StringBuilder sb, String tag, String lines, int lineWidth, boolean endWithXX) {
        String prefix = String.format("%-5s", tag);
        int availableCharacters = lineWidth - prefix.length();
        String[] split = lines.split("\n");
        for (String line : split) {
            if (line.length() > availableCharacters) {
                String text = splitToLineLength(line, "[,;:\\-\\s]", availableCharacters);
                for (String l : text.split("\n")) {
                    sb.append(prefix).append(l).append("\n");
                }
            } else {
                sb.append(prefix).append(line).append("\n");
            }
        }
        if (endWithXX) {
            sb.append("XX\n");
        }
    }

    public static String splitToLineLength(String s, String regex, int lineLength) {
        if (s.length() < lineLength) {
            return s;
        }
        StringBuilder textBuilder = new StringBuilder();
        int lastStart = 0;
        if (regex == null || regex.isEmpty() || !Pattern.compile(regex).matcher(s).find()) {
            // split after lineLength characters
            int start = 0;
            int newStart = lineLength;
            while (newStart < s.length()) {
                textBuilder.append(s.substring(start, newStart)).append("\n");
                start = newStart;
                newStart += lineLength;
            }
            if (start < s.length()) {
                textBuilder.append(s.substring(start));
            }
        } else {
            // split after pattern match
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(s);
            StringBuilder lineBuilder = new StringBuilder(lineLength);
            matcher.reset();
            while (matcher.find()) {
                String word = s.substring(lastStart, matcher.end());
                if (lineBuilder.length() + word.length() > lineLength) {
                    textBuilder.append(lineBuilder.toString().trim()).append("\n");
                    lineBuilder.delete(0, lineBuilder.length());
                }
                lineBuilder.append(word);
                lastStart = matcher.end();
            }
            if (s.length() > lastStart) {
                String word = s.substring(lastStart);
                if (lineBuilder.length() + word.length() > lineLength) {
                    textBuilder.append(lineBuilder.toString().trim()).append("\n");
                    lineBuilder.delete(0, lineBuilder.length());
                }
                lineBuilder.append(word);
            }
            if (lineBuilder.length() > 0) {
                textBuilder.append(lineBuilder);
            }
        }
        return textBuilder.toString();
    }

    private void processDataset(String prefix, List<String> linesForPrefix, EmblEntry entry) {
        String join;

        Set<String> spaceDelimeted = new HashSet<>();
        spaceDelimeted.addAll(Arrays.asList("ID", "AC", "PR", "KW", "OS", "OC", "DE"));
        Set<String> newLineDelimeted = new HashSet<>();
        newLineDelimeted.addAll(Arrays.asList("FT", "SQ", "DT", "CC"));

        if (spaceDelimeted.contains(prefix)) {
            join = Joiner.on(" ").join(linesForPrefix);
        } else {
            join = Joiner.on("\n").join(linesForPrefix);
        }

        if (null != prefix) {
            switch (prefix) {
                case "ID":
                    entry.setIdentification(idParser.parse(join));
                    break;
                case "AC":
                    entry.setAccession(accessionParser.parse(join));
                    break;
                case "PR":
                    entry.setProject(projectParser.parse(join));
                    break;
                case "KW":
                    entry.setKeywords(keywordsParser.parse(join));
                    break;
                case "OS":
                    entry.setSpecies(organismSpeciesParser.parse(join));
                    break;
                case "OC":
                    entry.setClassification(organismClassificationParser.parse(join));
                    break;
                case "DE":
                    entry.setDescription(descriptionParser.parse(join));
                    break;
                case "FT":
                    entry.setFeatureList(featureListParser.parse(join));
                    break;
                case "SQ":
                    entry.setSequence(sequenceParser.parse(join));
                    break;
                case "DT":
                    entry.setDate(dateParser.parse(join));
                    break;
                case "CC":
                    entry.setComment(commentParser.parse(join));
                    break;
                case "XX":
                case "FH":
                    // ignore empty lines and static content
                    break;
                default:
                    logger.log(Level.WARNING, "Unhandled embl lines: {0}", prefix);
                    break;
            }
        }
    }
}
