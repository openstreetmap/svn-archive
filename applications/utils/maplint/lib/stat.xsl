<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="iso-8859-1"/>

    <xsl:key name="maplint" match="/osm/maplint:test" use="@id"/>

    <xsl:variable name="nodes_all" select="count(/osm/node)"/>
    <xsl:variable name="segments_all" select="count(/osm/segment)"/>
    <xsl:variable name="ways_all" select="count(/osm/way)"/>
    <xsl:variable name="rels_all" select="count(/osm/relation)"/>

    <xsl:variable name="nodes_with_problem" select="count(/osm/node[maplint:result])"/>
    <xsl:variable name="segments_with_problem" select="count(/osm/segment[maplint:result])"/>
    <xsl:variable name="ways_with_problem" select="count(/osm/way[maplint:result])"/>
    <xsl:variable name="rels_with_problem" select="count(/osm/relation[maplint:result])"/>

    <xsl:variable name="nodes_with_error" select="count(/osm/node[key('maplint', maplint:result/@ref)/@severity = 'error'])"/>
    <xsl:variable name="segments_with_error" select="count(/osm/segment[key('maplint', maplint:result/@ref)/@severity = 'error'])"/>
    <xsl:variable name="ways_with_error" select="count(/osm/way[key('maplint', maplint:result/@ref)/@severity = 'error'])"/>
    <xsl:variable name="rels_with_error" select="count(/osm/relation[key('maplint', maplint:result/@ref)/@severity = 'error'])"/>

    <xsl:variable name="nodes_with_warning" select="count(/osm/node[key('maplint', maplint:result/@ref)/@severity = 'warning'])"/>
    <xsl:variable name="segments_with_warning" select="count(/osm/segment[key('maplint', maplint:result/@ref)/@severity = 'warning'])"/>
    <xsl:variable name="ways_with_warning" select="count(/osm/way[key('maplint', maplint:result/@ref)/@severity = 'warning'])"/>
    <xsl:variable name="rels_with_warning" select="count(/osm/relation[key('maplint', maplint:result/@ref)/@severity = 'warning'])"/>

    <xsl:variable name="nodes_with_notice" select="count(/osm/node[key('maplint', maplint:result/@ref)/@severity = 'notice'])"/>
    <xsl:variable name="segments_with_notice" select="count(/osm/segment[key('maplint', maplint:result/@ref)/@severity = 'notice'])"/>
    <xsl:variable name="ways_with_notice" select="count(/osm/way[key('maplint', maplint:result/@ref)/@severity = 'notice'])"/>
    <xsl:variable name="rels_with_notice" select="count(/osm/relation[key('maplint', maplint:result/@ref)/@severity = 'notice'])"/>

    <xsl:template match="/">
        <xsl:text>DATA TYPE: ALL: PROBLEMS (%), ERRORS (%), WARNINGS (%), NOTICES (%)
</xsl:text>
Nodes:     <xsl:value-of select="$nodes_all"/>: <xsl:value-of select="$nodes_with_problem"/> (<xsl:value-of select="format-number($nodes_with_problem div $nodes_all, '#0.0%')"/>), <xsl:value-of select="$nodes_with_error"/> (<xsl:value-of select="format-number($nodes_with_error div $nodes_all, '#0.0%')"/>), <xsl:value-of select="$nodes_with_warning"/> (<xsl:value-of select="format-number($nodes_with_warning div $nodes_all, '#0.0%')"/>), <xsl:value-of select="$nodes_with_notice"/> (<xsl:value-of select="format-number($nodes_with_notice div $nodes_all, '#0.0%')"/>)
        <xsl:if test="/osm/segment">
Segments:  <xsl:value-of select="$segments_all"/>: <xsl:value-of select="$segments_with_problem"/> (<xsl:value-of select="format-number($segments_with_problem div $segments_all, '#0.0%')"/>), <xsl:value-of select="$segments_with_error"/> (<xsl:value-of select="format-number($segments_with_error div $segments_all, '#0.0%')"/>), <xsl:value-of select="$segments_with_warning"/> (<xsl:value-of select="format-number($segments_with_warning div $segments_all, '#0.0%')"/>), <xsl:value-of select="$segments_with_notice"/> (<xsl:value-of select="format-number($segments_with_notice div $segments_all, '#0.0%')"/>)
        </xsl:if>
Ways:      <xsl:value-of select="$ways_all"/>: <xsl:value-of select="$ways_with_problem"/> (<xsl:value-of select="format-number($ways_with_problem div $ways_all, '#0.0%')"/>), <xsl:value-of select="$ways_with_error"/> (<xsl:value-of select="format-number($ways_with_error div $ways_all, '#0.0%')"/>), <xsl:value-of select="$ways_with_warning"/> (<xsl:value-of select="format-number($ways_with_warning div $ways_all, '#0.0%')"/>), <xsl:value-of select="$ways_with_notice"/> (<xsl:value-of select="format-number($ways_with_notice div $ways_all, '#0.0%')"/>)
        <xsl:if test="/osm/relation">
Relations: <xsl:value-of select="$rels_all"/>: <xsl:value-of select="$rels_with_problem"/> (<xsl:value-of select="format-number($rels_with_problem div $rels_all, '#0.0%')"/>), <xsl:value-of select="$rels_with_error"/> (<xsl:value-of select="format-number($rels_with_error div $rels_all, '#0.0%')"/>), <xsl:value-of select="$rels_with_warning"/> (<xsl:value-of select="format-number($rels_with_warning div $rels_all, '#0.0%')"/>), <xsl:value-of select="$rels_with_notice"/> (<xsl:value-of select="format-number($rels_with_notice div $rels_all, '#0.0%')"/>)
        </xsl:if>
        <xsl:text>
</xsl:text>

        <xsl:text>Errors:
</xsl:text>
        <xsl:apply-templates select="/osm/maplint:test[@severity='error']">
            <xsl:sort select="@id"/>
        </xsl:apply-templates>

        <xsl:text>Warnings:
</xsl:text>
        <xsl:apply-templates select="/osm/maplint:test[@severity='warning']">
            <xsl:sort select="@id"/>
        </xsl:apply-templates>

        <xsl:text>Notices:
</xsl:text>
        <xsl:apply-templates select="/osm/maplint:test[@severity='notice']">
            <xsl:sort select="@id"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="maplint:test">
        <xsl:variable name="tid" select="@id"/>
        <xsl:value-of select="concat('  ', @id, ' (', @group, '): ', count(/osm/*/maplint:result[@ref = $tid]))"/>
        <xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>
