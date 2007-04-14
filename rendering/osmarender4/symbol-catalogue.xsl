<?xml version="1.0" encoding="iso8859-1"?>
<!-- This stylesheet creates a catalogue of all symbols known to Osmarender -->
<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:svg="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="svg">

    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:variable name="columns" select="2"/>
    <xsl:variable name="scale" select="18"/>

    <xsl:template match="/">
        <svg version="1.1" baseProfile="full" width="210mm" height="297mm">
            <xsl:comment> This file is created automatically from symbols.svg. DO NOT CHANGE! </xsl:comment>
            <title>Catalogue of symbols known to Osmarender</title>

            <defs id="def-styles">
                <style id="styles" type="text/css">
<xsl:text>
    .crosshair {
        stroke-width: 0.05;
        stroke: #a0a0a0;
        fill: none;
    }

    .crosshair-fine {
        stroke-width: 0.005;
        stroke: #404040;
        fill: none;
    }

    text.desc {
        font-size: 1;
    }
</xsl:text>
                </style>

            <xsl:apply-templates select="/dir/f[substring-after(@n, '.') = 'svg']" mode="defs">
                <xsl:sort select="@n"/>
            </xsl:apply-templates>
            </defs>

            <g id="catalogue" transform="scale({$scale})">
                <xsl:apply-templates select="/dir/f[substring-after(@n, '.') = 'svg']" mode="draw">
                    <xsl:sort select="@n"/>
                </xsl:apply-templates>
            </g>
        </svg>
    </xsl:template>

    <xsl:template match="f" mode="defs">
        <xsl:copy-of select="document(concat('symbols/', @n))/svg:svg/svg:defs/svg:symbol"/>
    </xsl:template>

    <xsl:template match="f" mode="draw">
        <g id="demo-{substring-before(@n, '.')}" transform="translate({2 + (position()-1) mod $columns * 20}, {2 + floor((position()-1) div $columns) * 3})">
            <line class="crosshair" x1="-1" y1="0" x2="1" y2="0"/>
            <line class="crosshair" x1="0" y1="-1" x2="0" y2="1"/>
            <rect class="crosshair" x="-0.5" y="-0.5" width="1" height="1"/>
            <line class="crosshair-fine" x1="-1" y1="0" x2="1" y2="0"/>
            <line class="crosshair-fine" x1="0" y1="-1" x2="0" y2="1"/>
            <rect class="crosshair-fine" x="-0.5" y="-0.5" width="1" height="1"/>
            <use xlink:href="#symbol-{substring-before(@n, '.')}" width="1" height="1"/>
            <text class="desc" x="3" y="0.4"><xsl:value-of select="substring-before(@n, '.')"/></text>
        </g>
    </xsl:template>

</xsl:stylesheet>
