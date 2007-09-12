<?xml version="1.0" encoding="UTF-8"?>
<xslout:stylesheet xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0" xmlns:xslout="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="nodesbycoordinates" match="/osm/node" use="concat(@lon,' ', @lat)"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="nodeId" match="/osm/node" use="@id"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="wayId" match="/osm/way" use="@id"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="relId" match="/osm/relation" use="@id"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="fromto2segment" match="/osm/segment" use="concat(@from, ' ', @to)"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="tofrom2segment" match="/osm/segment" use="concat(@to, ' ', @from)"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segment2way" match="/osm/way" use="seg/@id"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="node-from" match="/osm/segment" use="@from"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="node-to" match="/osm/segment" use="@to"/>
  <xsl:key xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segment" match="/osm/segment" use="@id"/>
  <xslout:template name="all-tests">
    <maplint:test agent="xsltests" group="base" id="empty-tag-key" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="empty-tag-value" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="base" id="nodes-on-same-spot" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="base" id="untagged-way" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="main" id="bridge-or-tunnel-without-layer" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="main" id="deprecated-tags" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="main" id="motorway-without-ref" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="main" id="place-of-worship-without-religion" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="main" id="poi-without-name" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="main" id="residential-without-name" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="relations" id="member-missing" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="segments" id="multiple-segments-on-same-nodes" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="segments" id="segment-with-from-equals-to" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="segments" id="segment-without-way" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="segments" id="tagged-segment" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="segments" id="untagged-unconnected-node" version="1" severity="warning"/>
    <maplint:test agent="xsltests" group="segments" id="ways-with-unordered-segments" version="1" severity="error"/>
    <maplint:test agent="xsltests" group="strict" id="unknown-tags" version="1" severity="notice"/>
  </xslout:template>
  <xslout:template name="call-tests-any">
    <xslout:call-template name="test-base-empty-tag-key-any"/>
    <xslout:call-template name="test-base-empty-tag-value-any"/>
    <xslout:call-template name="test-main-deprecated-tags-any"/>
  </xslout:template>
  <xslout:template name="call-tests-node">
    <xslout:call-template name="test-base-nodes-on-same-spot-node"/>
    <xslout:call-template name="test-main-place-of-worship-without-religion-node"/>
    <xslout:call-template name="test-main-poi-without-name-node"/>
    <xslout:call-template name="test-segments-untagged-unconnected-node-node"/>
    <xslout:call-template name="test-strict-unknown-tags-node"/>
  </xslout:template>
  <xslout:template name="call-tests-segment">
    <xslout:call-template name="test-segments-multiple-segments-on-same-nodes-segment"/>
    <xslout:call-template name="test-segments-segment-with-from-equals-to-segment"/>
    <xslout:call-template name="test-segments-segment-without-way-segment"/>
    <xslout:call-template name="test-segments-tagged-segment-segment"/>
  </xslout:template>
  <xslout:template name="call-tests-way">
    <xslout:call-template name="test-base-untagged-way-way"/>
    <xslout:call-template name="test-main-bridge-or-tunnel-without-layer-way"/>
    <xslout:call-template name="test-main-motorway-without-ref-way"/>
    <xslout:call-template name="test-main-residential-without-name-way"/>
    <xslout:call-template name="test-segments-ways-with-unordered-segments-way"/>
    <xslout:call-template name="test-strict-unknown-tags-way"/>
  </xslout:template>
  <xslout:template name="call-tests-relation">
    <xslout:call-template name="test-relations-member-missing-relation"/>
  </xslout:template>
  <xslout:template name="test-base-empty-tag-key-any">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="tag[@k='']">
            <maplint:result ref="empty-tag-key">Value=<xsl:value-of select="tag[@k='']/@v"/></maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-base-empty-tag-value-any">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="tag[@v='']">
            <maplint:result ref="empty-tag-value">Key=<xsl:value-of select="tag[@v='']/@k"/></maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-deprecated-tags-any">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="tag/@k='class'">
            <maplint:result ref="deprecated-tags">class</maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-base-nodes-on-same-spot-node">
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="nodes" select="key('nodesbycoordinates', concat(@lon, ' ', @lat))"/>
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="nid" select="@id"/>

        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="count($nodes) != 1">
            <maplint:result ref="nodes-on-same-spot">
                <xsl:text>Nodes:</xsl:text>
                <xsl:for-each select="$nodes">
                    <xsl:if test="@id != $nid">
                        <xsl:value-of select="concat(' ', @id)"/>
                    </xsl:if>
                </xsl:for-each>
            </maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-place-of-worship-without-religion-node">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="(tag[@k='amenity' and @v='place_of_worship']) and not(tag[@k='religion'])">
            <maplint:result ref="place-of-worship-without-religion"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-poi-without-name-node">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="(tag[@k='amenity' and (@v='place_of_worship' or @v='cinema' or @v='pharmacy' or @v='pub' or @v='restaurant' or @v='school' or @v='university' or @v='hospital' or @v='library' or @v='theatre' or @v='courthouse' or @v='bank')]) and not(tag[@k='name'])">
            <maplint:result ref="poi-without-name">
                <xsl:text>amenity=</xsl:text>
                <xsl:value-of select="tag[@k='amenity']/@v"/>
            </maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-segments-untagged-unconnected-node-node">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="not(tag[@k != 'created_by'] or key('node-from', @id) or key('node-to', @id))">
            <maplint:result ref="untagged-unconnected-node"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-strict-unknown-tags-node">
        <xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
            <xsl:if test="(@k!='created_by') and                     not(starts-with(@k, 'tiger:')) and                     (@k!='converted_by') and                     (@k!='todo') and                     (@k!='landuse') and                     (@k!='note') and                     (@k!='highway') and                     (@k!='railway') and                     (@k!='waterway') and                     (@k!='amenity') and                     (@k!='dispensing') and                     (@k!='religion') and                     (@k!='military') and                     (@k!='denomination') and                     (@k!='leisure') and                     (@k!='recycling:glass') and                     (@k!='recycling:batteries') and                     (@k!='recycling:clothes') and                     (@k!='recycling:paper') and                     (@k!='recycling:green_waste') and                     (@k!='tourism') and                     (@k!='int_name') and                     (@k!='nat_name') and                     (@k!='reg_name') and                     (@k!='loc_name') and                     (@k!='old_name') and                     (@k!='int_ref') and                     (@k!='nat_ref') and                     (@k!='reg_ref') and                     (@k!='loc_ref') and                     (@k!='old_ref') and                     (@k!='ncn_ref') and                     (@k!='ele') and                     (@k!='man_made') and                     (@k!='sport') and                     (@k!='place') and                     (@k!='historic') and                     (@k!='natural') and                     (@k!='layer') and                     (@k!='religion') and                     (@k!='denomination') and                     (@k!='source') and                     (@k!='source:ref') and                     (@k!='source:name') and                     (@k!='is_in') and                     (@k!='time') and                     (@k!='access') and                     (@k!='name')">
                <maplint:result ref="unknown-tags"><xsl:value-of select="concat(@k, '=', @v)"/></maplint:result>
            </xsl:if>
        </xsl:for-each>
    </xslout:template>
  <xslout:template name="test-segments-multiple-segments-on-same-nodes-segment">
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segment-samedir" select="key('fromto2segment', concat(@from, ' ', @to))"/>
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segment-otherdir" select="key('tofrom2segment', concat(@to, ' ', @from))"/>
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="sid" select="@id"/>
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="count($segment-samedir) &gt; 1">
            <maplint:result ref="multiple-segments-on-same-nodes">
                <xsl:text>Segments with same @from/@to:</xsl:text>
                <xsl:for-each select="$segment-samedir">
                    <xsl:if test="@id != $sid">
                        <xsl:value-of select="concat(' ', @id)"/>
                    </xsl:if>
                </xsl:for-each>
            </maplint:result>
        </xsl:if>
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="count($segment-otherdir) &gt; 1">
                <xsl:text>Segments with @from/@to reversed:</xsl:text>
                <xsl:for-each select="$segment-otherdir">
                    <xsl:if test="@id != $sid">
                        <xsl:value-of select="concat(' ', @id)"/>
                    </xsl:if>
                </xsl:for-each>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-segments-segment-with-from-equals-to-segment">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="@from=@to">
            <maplint:result ref="segment-with-from-equals-to"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-segments-segment-without-way-segment">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="not(key('segment2way', @id))">
            <maplint:result ref="segment-without-way"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-segments-tagged-segment-segment">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="tag[(@k!='created_by') and (@k!='converted_by')]">
            <maplint:result ref="tagged-segment">
                <xsl:for-each select="tag[(@k!='created_by') and (@k!='converted_by')]">
                    <xsl:value-of select="concat(@k,'=',@v,' ')"/>
                </xsl:for-each>
            </maplint:result>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-base-untagged-way-way">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="not(tag[@k != 'created_by'])">
            <maplint:result ref="untagged-way"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-bridge-or-tunnel-without-layer-way">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="(tag[(@k='bridge' or @k='tunnel') and @v='true']) and not(tag[@k='layer'])">
            <maplint:result ref="bridge-or-tunnel-without-layer"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-motorway-without-ref-way">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="tag[@k='highway' and @v='motorway']">
            <xsl:if test="not(tag[@k='ref'])">
                <maplint:result ref="motorway-without-ref"/>
            </xsl:if>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-main-residential-without-name-way">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="(tag[@k='highway' and @v='residential']) and not(tag[@k='name'])">
            <maplint:result ref="residential-without-name"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-segments-ways-with-unordered-segments-way">
        <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="error">
            <xsl:for-each select="seg">
                <xsl:if test="position() != last()">
                    <xsl:variable name="thissegment" select="key('segment',@id)"/>
                    <xsl:variable name="next" select="position()+1"/>
                    <xsl:variable name="nextsegment" select="key('segment',../seg[$next]/@id)"/>
                    <xsl:variable name="to" select="$thissegment/@to"/>
                    <xsl:variable name="from" select="$nextsegment/@from"/>
                    <xsl:if test="$to != $from">
                        <xsl:text>fail</xsl:text>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="$error != ''">
            <maplint:result ref="ways-with-unordered-segments"/>
        </xsl:if>
    </xslout:template>
  <xslout:template name="test-strict-unknown-tags-way">
        <xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
            <xsl:if test="(@k!='created_by') and                     not(starts-with(@k, 'tiger:')) and                     (@k!='converted_by') and                     (@k!='highway') and                     (@k!='railway') and                     (@k!='waterway') and                     (@k!='amenity') and                     (@k!='tourism') and                     (@k!='ele') and                     (@k!='man_made') and                     (@k!='sport') and                     (@k!='place') and                     (@k!='note') and                     (@k!='historic') and                     (@k!='landuse') and                     (@k!='oneway') and                     (@k!='bridge') and                     (@k!='tunnel') and                     (@k!='leisure') and                     (@k!='junction') and                     (@k!='ref') and                     (@k!='int_name') and                     (@k!='nat_name') and                     (@k!='reg_name') and                     (@k!='loc_name') and                     (@k!='old_name') and                     (@k!='int_ref') and                     (@k!='nat_ref') and                     (@k!='reg_ref') and                     (@k!='loc_ref') and                     (@k!='old_ref') and                     (@k!='ncn_ref') and                     (@k!='natural') and                     (@k!='layer') and                     (@k!='source') and                     (@k!='source:ref') and                     (@k!='source:name') and                     (@k!='time') and                     (@k!='abutters') and                     (@k!='maxspeed') and                     (@k!='access') and                     (@k!='foot') and                     (@k!='bicycle') and                     (@k!='motorcycle') and                     (@k!='motorcar') and                     (@k!='horse') and                     (@k!='surface') and                     (@k!='osmarender:renderName') and                     (@k!='osmarender:nameDirection') and                     (@k!='name')">

                <maplint:result ref="unknown-tags"><xsl:value-of select="concat(@k, '=', @v)"/></maplint:result>
            </xsl:if>
        </xsl:for-each>
    </xslout:template>
  <xslout:template name="test-relations-member-missing-relation">
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="member[(@type='way') and not(key('wayId', @ref))]">
            <maplint:result ref="member-missing"/>
        </xsl:if>
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="member[(@type='node') and not(key('nodeId', @ref))]">
            <maplint:result ref="member-missing"/>
        </xsl:if>
        <xsl:if xmlns:xsl="http://www.w3.org/1999/XSL/Transform" test="member[(@type='relation') and not(key('relId', @ref))]">
            <maplint:result ref="member-missing"/>
        </xsl:if>
    </xslout:template>
</xslout:stylesheet>
