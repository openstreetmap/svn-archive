<?xml version="1.0" encoding="UTF-8"?>
<xslout:stylesheet xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0" xmlns:xslout="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xslout:key name="fromto2segment" match="/osm/segment" use="concat(@from, ' ', @to)"/>
  <xslout:key name="tofrom2segment" match="/osm/segment" use="concat(@to, ' ', @from)"/>
  <xslout:key name="nodesbycoordinates" match="/osm/node" use="concat(@lon,' ', @lat)"/>
  <xslout:key name="segment2way" match="/osm/way" use="seg/@id"/>
  <xslout:key name="node-from" match="/osm/segment" use="@from"/>
  <xslout:key name="node-to" match="/osm/segment" use="@to"/>
  <xslout:key name="segment" match="/osm/segment" use="@id"/>
  <xslout:template name="all-tests">
    <maplint:test agent="xsltests" group="base" id="empty-tag-key" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="empty-tag-value" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="base" id="multiple-segments-on-same-nodes" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="nodes-on-same-spot" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="segment-with-from-equals-to" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="segment-without-way" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="base" id="tagged-segment" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="untagged-unconnected-node" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="base" id="ways-with-unordered-segments" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="main" id="deprecated-tags" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="main" id="motorway-without-ref" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="main" id="residential-without-name" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="strict" id="unknown-tags" version="1" severity="notice"/>
  </xslout:template>
  <xslout:template name="call-tests-any">
    <xslout:call-template name="test-base-empty-tag-key-any"/>
    <xslout:call-template name="test-base-empty-tag-value-any"/>
    <xslout:call-template name="test-main-deprecated-tags-any"/>
  </xslout:template>
  <xslout:template name="call-tests-node">
    <xslout:call-template name="test-base-nodes-on-same-spot-node"/>
    <xslout:call-template name="test-base-untagged-unconnected-node-node"/>
    <xslout:call-template name="test-strict-unknown-tags-node"/>
  </xslout:template>
  <xslout:template name="call-tests-segment">
    <xslout:call-template name="test-base-multiple-segments-on-same-nodes-segment"/>
    <xslout:call-template name="test-base-segment-with-from-equals-to-segment"/>
    <xslout:call-template name="test-base-segment-without-way-segment"/>
    <xslout:call-template name="test-base-tagged-segment-segment"/>
  </xslout:template>
  <xslout:template name="call-tests-way">
    <xslout:call-template name="test-base-ways-with-unordered-segments-way"/>
    <xslout:call-template name="test-main-motorway-without-ref-way"/>
    <xslout:call-template name="test-main-residential-without-name-way"/>
    <xslout:call-template name="test-strict-unknown-tags-way"/>
  </xslout:template>
  <xslout:template name="test-base-empty-tag-key-any">
        <xslout:if test="tag[@k='']">
            <maplint:result ref="empty-tag-key">Value=<xslout:value-of select="tag[@k='']/@v"/></maplint:result>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-empty-tag-value-any">
        <xslout:if test="tag[@v='']">
            <maplint:result ref="empty-tag-value">Key=<xslout:value-of select="tag[@v='']/@k"/></maplint:result>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-main-deprecated-tags-any">
        <xslout:if test="tag/@k='class'">
            <maplint:result ref="deprecated-tags">class</maplint:result>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-nodes-on-same-spot-node">
        <xslout:variable name="nodes" select="key('nodesbycoordinates', concat(@lon, ' ', @lat))"/>
        <xslout:variable name="nid" select="@id"/>

        <xslout:if test="count($nodes) != 1">
            <maplint:result ref="nodes-on-same-spot">
                <xslout:text>Nodes:</xslout:text>
                <xslout:for-each select="$nodes">
                    <xslout:if test="@id != $nid">
                        <xslout:value-of select="concat(' ', @id)"/>
                    </xslout:if>
                </xslout:for-each>
            </maplint:result>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-untagged-unconnected-node-node">
        <xslout:if test="not(tag[@k != 'created_by'] or key('node-from', @id) or key('node-to', @id))">
            <maplint:result ref="untagged-unconnected-node"/>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-strict-unknown-tags-node">
        <xslout:for-each select="tag">
            <xslout:if test="(@k!='created_by') and                     (@k!='highway') and                     (@k!='railway') and                     (@k!='waterway') and                     (@k!='amenity') and                     (@k!='dispensing') and                     (@k!='religion') and                     (@k!='military') and                     (@k!='denomination') and                     (@k!='leisure') and                     (@k!='recycling:glass') and                     (@k!='recycling:batteries') and                     (@k!='recycling:clothes') and                     (@k!='recycling:paper') and                     (@k!='tourism') and                     (@k!='ele') and                     (@k!='man_made') and                     (@k!='sport') and                     (@k!='place') and                     (@k!='note') and                     (@k!='historic') and                     (@k!='layer') and                     (@k!='source') and                     (@k!='access') and                     (@k!='foot') and                     (@k!='bicycle') and                     (@k!='motorcycle') and                     (@k!='horse') and                     (@k!='time') and                     (@k!='name')">
                <maplint:result ref="unknown-tags"><xslout:value-of select="concat(@k, '=', @v)"/></maplint:result>
            </xslout:if>
        </xslout:for-each>
    </xslout:template>
  <xslout:template name="test-base-multiple-segments-on-same-nodes-segment">
        <xslout:variable name="segment-samedir" select="key('fromto2segment', concat(@from, ' ', @to))"/>
        <xslout:variable name="segment-otherdir" select="key('tofrom2segment', concat(@to, ' ', @from))"/>
        <xslout:variable name="sid" select="@id"/>
        <xslout:if test="count($segment-samedir) &gt; 1">
            <maplint:result ref="multiple-segments-on-same-nodes">
                <xslout:text>Segments with same @from/@to:</xslout:text>
                <xslout:for-each select="$segment-samedir">
                    <xslout:if test="@id != $sid">
                        <xslout:value-of select="concat(' ', @id)"/>
                    </xslout:if>
                </xslout:for-each>
            </maplint:result>
        </xslout:if>
        <xslout:if test="count($segment-otherdir) &gt; 1">
                <xslout:text>Segments with @from/@to reversed:</xslout:text>
                <xslout:for-each select="$segment-otherdir">
                    <xslout:if test="@id != $sid">
                        <xslout:value-of select="concat(' ', @id)"/>
                    </xslout:if>
                </xslout:for-each>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-segment-with-from-equals-to-segment">
        <xslout:if test="@from=@to">
            <maplint:result ref="segment-with-from-equals-to"/>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-segment-without-way-segment">
        <xslout:if test="not(key('segment2way', @id))">
            <maplint:result ref="segment-without-way"/>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-tagged-segment-segment">
        <xslout:if test="tag[@k!='created_by']">
            <maplint:result ref="tagged-segment">
                <xslout:for-each select="tag[@k!='created_by']">
                    <xslout:value-of select="concat(@k,'=',@v,' ')"/>
                </xslout:for-each>
            </maplint:result>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-base-ways-with-unordered-segments-way">
        <xslout:variable name="error">
            <xslout:for-each select="seg">
                <xslout:if test="position() != last()">
                    <xslout:variable name="thissegment" select="key('segment',@id)"/>
                    <xslout:variable name="next" select="position()+1"/>
                    <xslout:variable name="nextsegment" select="key('segment',../seg[$next]/@id)"/>
                    <xslout:variable name="to" select="$thissegment/@to"/>
                    <xslout:variable name="from" select="$nextsegment/@from"/>
                    <xslout:if test="$to != $from">
                        <xslout:text>fail</xslout:text>
                    </xslout:if>
                </xslout:if>
            </xslout:for-each>
        </xslout:variable>

        <xslout:if test="$error != ''">
            <maplint:result ref="ways-with-unordered-segments"/>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-main-motorway-without-ref-way">
        <xslout:if test="tag[@k='highway' and @v='motorway']">
            <xslout:if test="not(tag[@k='ref'])">
                <maplint:result ref="motorway-without-ref"/>
            </xslout:if>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-main-residential-without-name-way">
        <xslout:if test="(tag[@k='highway' and @v='residential']) and not(tag[@k='name'])">
            <maplint:result ref="residential-without-name"/>
        </xslout:if>
    </xslout:template>
  <xslout:template name="test-strict-unknown-tags-way">
        <xslout:for-each select="tag">
            <xslout:if test="(@k!='created_by') and                     (@k!='highway') and                     (@k!='railway') and                     (@k!='waterway') and                     (@k!='amenity') and                     (@k!='tourism') and                     (@k!='ele') and                     (@k!='man_made') and                     (@k!='sport') and                     (@k!='place') and                     (@k!='note') and                     (@k!='historic') and                     (@k!='landuse') and                     (@k!='oneway') and                     (@k!='bridge') and                     (@k!='tunnel') and                     (@k!='leisure') and                     (@k!='junction') and                     (@k!='ref') and                     (@k!='int_ref') and                     (@k!='nat_ref') and                     (@k!='natural') and                     (@k!='layer') and                     (@k!='source') and                     (@k!='time') and                     (@k!='abutters') and                     (@k!='name')">

                <maplint:result ref="unknown-tags"><xslout:value-of select="concat(@k, '=', @v)"/></maplint:result>
            </xslout:if>
        </xslout:for-each>
    </xslout:template>
</xslout:stylesheet>
