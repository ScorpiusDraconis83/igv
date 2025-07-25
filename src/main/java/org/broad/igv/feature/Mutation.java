/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.broad.igv.feature;

//~--- non-JDK imports --------------------------------------------------------

import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.logging.*;
import org.broad.igv.feature.aa.AminoAcidSequence;
import org.broad.igv.prefs.PreferencesManager;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.color.ColorTable;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Represents a mutation
 * // TODO -- refactor this to not implement "IGVFeature"
 *
 * @author jrobinso
 */
public class Mutation implements IGVFeature {

    private static Logger log = LogManager.getLogger(Mutation.class);
    private static Map<String, Color> colors;

    private String sampleId;
    private String chr;
    private int start;
    private int end;
    private String name;
    private String omaName;
    private String mutationType;
    private Color color;
    String refAllele;
    String altAllele1;
    String altAllele2;
    private Map<String, String> attributes;
    private String valueString;


    public Mutation(String runId, String chromosome, int start, int end, String type) {
        this.sampleId = runId;
        this.chr = chromosome;
        this.start = start;
        this.end = end;
        this.mutationType = type;
    }

    public Mutation(Mutation mutation) {
        this.sampleId = mutation.sampleId;
        this.chr = mutation.chr;
        this.start = mutation.start;
        this.end = mutation.end;
        this.mutationType = mutation.mutationType;
        this.color = mutation.color;
        this.name = mutation.getName();
        this.omaName = mutation.getOMAName();
    }

    private String getOMAName() {
        if (refAllele == null) return null;
        if (omaName == null) {
            String altAllele = altAllele1;
            if (refAllele.equals(altAllele1)) {
                altAllele = altAllele2;
            }
            String omaChr = chr.replace("chr", "");
            omaName = omaChr + "," + (start + 1) + "," + refAllele + "," + altAllele;
        }
        return omaName;
    }


    public String getOMAUrl() {
        if (refAllele == null) return null;
        String genome = GenomeManager.getInstance().getGenomeId();
        String url = "http://mutationassessor.org/r3/?cm=var&var=" + genome + "," + getOMAName();
        return url;

    }

    public String getCravatLink() {

        String genomeID = GenomeManager.getInstance().getGenomeId();
        if ("hg38".equals(genomeID) || "GRCh38".equals(genomeID)) {
            if (refAllele == null) return null;
            String altAllele = altAllele1;
            if (refAllele.equals(altAllele1)) {
                altAllele = altAllele2;
            }

            //http://www.cravat.us/CRAVAT/variant.html?variant=chr22_40418496_+_A_G
            String cravatChr = chr.startsWith("chr") ? chr : "chr" + chr;
            int position = start + 1;
            return "<a target='_blank' " +
                    "href='http://www.cravat.us/CRAVAT/variant.html?variant=" +
                    cravatChr + "_" + position + "_+_" + refAllele + "_" + altAllele + "'>Cravat " + refAllele + "->" + altAllele + "</a>";
        } else {
            return null;
        }
    }


    public void setChr(String chr) {
        this.chr = chr;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Mutation copy() {
        return new Mutation(this);
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getType() {
        return "mutation";
    }


    public String getMutationType() {
        return mutationType;
    }

    public String getName() {
        if (name == null) {
            StringBuffer buffer = new StringBuffer();
            DecimalFormat format = new DecimalFormat();
            String posString = format.format(start + 1);
            buffer.append(chr + ":" + posString);
            if (end > start + 1) {
                buffer.append("-" + end);
            }
            if (refAllele != null && altAllele1 != null) {
                if (!altAllele1.equals(refAllele)) {
                    buffer.append(" " + refAllele + ">" + altAllele1);
                }
                if (!altAllele1.equals(altAllele2) && !refAllele.equals(altAllele2)) {
                    buffer.append(" " + refAllele + ">" + altAllele2);
                }
            }
            name = buffer.toString();
        }
        return name;
    }

    public String getDescription() {
        StringBuffer desc = new StringBuffer();
        desc.append(getName());
        desc.append("<br>");
        desc.append(mutationType);
        return desc.toString();
    }

    public String getValueString(double position, int mouseX, WindowFunction ignored) {
        if (valueString == null) {
            StringBuffer buf = new StringBuffer();
            buf.append("Type: ");
            buf.append(mutationType);
            if (attributes != null) {
                FormatUtils.printAttributes(attributes, buf, 100);
            }

            if (getOMAName() != null) {
                buf.append("<br/><a href=\"" + getOMAUrl() + "\">Mutation Assessor</a>");
            }

            String cravatLink = getCravatLink();
            if (cravatLink != null) {
                buf.append("<br/>" + cravatLink);
            }

            valueString = buf.toString();
        }
        return valueString;
    }


    public boolean hasScore() {
        return false;
    }

    public Strand getStrand() {
        return Strand.NONE;
    }

    public boolean overlaps(IGVFeature track) {
        return false;
    }

    public String getChr() {
        return chr;
    }

    @Override
    public String getContig() {
        return chr;
    }

    public void setColor(Color color) {

        // Ignore
    }

    public Color getColor() {
        ColorTable colorTable = PreferencesManager.getPreferences().getMutationColorScheme();
        Color c = colorTable.get(getMutationType());
        return c;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public float getScore() {
        return 0;
    }

    public AminoAcidSequence getAminoAcidSequence(int exonIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getCdEnd() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getCdStart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    public void setAltAllele1(String altAllele1) {
        this.altAllele1 = altAllele1;
    }

    public void setAltAllele2(String altAllele2) {
        this.altAllele2 = altAllele2;
    }

}
