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
    <maplint:test agent="xsltests" group="strict" id="not-in-map_features" version="1" severity="notice"/>
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
    <xslout:call-template name="test-strict-not-in-map_features-node"/>
  </xslout:template>
  <xslout:template name="call-tests-segment">
    <xslout:call-template name="test-segments-multiple-segments-on-same-nodes-segment"/>
    <xslout:call-template name="test-segments-segment-with-from-equals-to-segment"/>
    <xslout:call-template name="test-segments-segment-without-way-segment"/>
    <xslout:call-template name="test-segments-tagged-segment-segment"/>
    <xslout:call-template name="test-strict-not-in-map_features-segment"/>
  </xslout:template>
  <xslout:template name="call-tests-way">
    <xslout:call-template name="test-base-untagged-way-way"/>
    <xslout:call-template name="test-main-bridge-or-tunnel-without-layer-way"/>
    <xslout:call-template name="test-main-motorway-without-ref-way"/>
    <xslout:call-template name="test-main-residential-without-name-way"/>
    <xslout:call-template name="test-segments-ways-with-unordered-segments-way"/>
    <xslout:call-template name="test-strict-not-in-map_features-way"/>
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
  <xslout:template name="test-strict-not-in-map_features-node">
<xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
<xsl:choose>
<xsl:when test="starts-with(@k, 'tiger:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND_')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'gns:')">
</xsl:when>
<xsl:when test="@k='aeroway'">
<xsl:choose>
<xsl:when test="@v='aerodrome'"/>
<xsl:when test="@v='helipad'"/>
<xsl:when test="@v='terminal'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='amenity'">
<xsl:choose>
<xsl:when test="@v='arts_centre'"/>
<xsl:when test="@v='atm'"/>
<xsl:when test="@v='bank'"/>
<xsl:when test="@v='bicycle_parking'"/>
<xsl:when test="@v='biergarten'"/>
<xsl:when test="@v='bus_station'"/>
<xsl:when test="@v='cafe'"/>
<xsl:when test="@v='cinema'"/>
<xsl:when test="@v='college'"/>
<xsl:when test="@v='courthouse'"/>
<xsl:when test="@v='fast_food'"/>
<xsl:when test="@v='fire_station'"/>
<xsl:when test="@v='fuel'"/>
<xsl:when test="@v='grave_yard'"/>
<xsl:when test="@v='hospital'"/>
<xsl:when test="@v='library'"/>
<xsl:when test="@v='parking'"/>
<xsl:when test="@v='pharmacy'"/>
<xsl:when test="@v='place_of_worship'"/>
<xsl:when test="@v='police'"/>
<xsl:when test="@v='post_box'"/>
<xsl:when test="@v='post_office'"/>
<xsl:when test="@v='prison'"/>
<xsl:when test="@v='pub'"/>
<xsl:when test="@v='public_building'"/>
<xsl:when test="@v='recycling'"/>
<xsl:when test="@v='restaurant'"/>
<xsl:when test="@v='school'"/>
<xsl:when test="@v='telephone'"/>
<xsl:when test="@v='theatre'"/>
<xsl:when test="@v='toilets'"/>
<xsl:when test="@v='townhall'"/>
<xsl:when test="@v='university'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="contains(@k, 'name:')">

</xsl:when>
<xsl:when test="@k='created_by'">

</xsl:when>
<xsl:when test="@k='description'">

</xsl:when>
<xsl:when test="@k='ele'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='highway'">
<xsl:choose>
<xsl:when test="@v='bus_stop'"/>
<xsl:when test="@v='cattle_grid'"/>
<xsl:when test="@v='crossing'"/>
<xsl:when test="@v='ford'"/>
<xsl:when test="@v='gate'"/>
<xsl:when test="@v='incline'"/>
<xsl:when test="@v='incline_steep'"/>
<xsl:when test="@v='mini_roundabout'"/>
<xsl:when test="@v='motorway_junction'"/>
<xsl:when test="@v='services'"/>
<xsl:when test="@v='stile'"/>
<xsl:when test="@v='stop'"/>
<xsl:when test="@v='toll_booth'"/>
<xsl:when test="@v='traffic_signals'"/>
<xsl:when test="@v='viaduct'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='historic'">
<xsl:choose>
<xsl:when test="@v='archaeological_site'"/>
<xsl:when test="@v='castle'"/>
<xsl:when test="@v='icon'"/>
<xsl:when test="@v='memorial'"/>
<xsl:when test="@v='monument'"/>
<xsl:when test="@v='museum'"/>
<xsl:when test="@v='ruins'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='iata'">

</xsl:when>
<xsl:when test="@k='icao'">

</xsl:when>
<xsl:when test="@k='image'">
<xsl:choose>
<xsl:when test="@v='URI'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='int_name'">

</xsl:when>
<xsl:when test="@k='int_ref'">

</xsl:when>
<xsl:when test="@k='is_in'">

</xsl:when>
<xsl:when test="@k='landuse'">
<xsl:choose>
<xsl:when test="@v='allotments'"/>
<xsl:when test="@v='basin'"/>
<xsl:when test="@v='brownfield'"/>
<xsl:when test="@v='cemetery'"/>
<xsl:when test="@v='commercial'"/>
<xsl:when test="@v='farm'"/>
<xsl:when test="@v='forest'"/>
<xsl:when test="@v='greenfield'"/>
<xsl:when test="@v='industrial'"/>
<xsl:when test="@v='landfill'"/>
<xsl:when test="@v='quarry'"/>
<xsl:when test="@v='recreation_ground'"/>
<xsl:when test="@v='reservoir'"/>
<xsl:when test="@v='residential'"/>
<xsl:when test="@v='retail'"/>
<xsl:when test="@v='village_green'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='leisure'">
<xsl:choose>
<xsl:when test="@v='common'"/>
<xsl:when test="@v='fishing'"/>
<xsl:when test="@v='garden'"/>
<xsl:when test="@v='golf_course'"/>
<xsl:when test="@v='marina'"/>
<xsl:when test="@v='nature_reserve'"/>
<xsl:when test="@v='park'"/>
<xsl:when test="@v='pitch'"/>
<xsl:when test="@v='playground'"/>
<xsl:when test="@v='slipway'"/>
<xsl:when test="@v='sports_centre'"/>
<xsl:when test="@v='stadium'"/>
<xsl:when test="@v='track'"/>
<xsl:when test="@v='water_park'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='loc_name'">

</xsl:when>
<xsl:when test="@k='loc_ref'">

</xsl:when>
<xsl:when test="@k='man_made'">
<xsl:choose>
<xsl:when test="@v='beacon'"/>
<xsl:when test="@v='gasometer'"/>
<xsl:when test="@v='lighthouse'"/>
<xsl:when test="@v='power_fossil'"/>
<xsl:when test="@v='power_hydro'"/>
<xsl:when test="@v='power_nuclear'"/>
<xsl:when test="@v='power_wind'"/>
<xsl:when test="@v='reservoir_covered'"/>
<xsl:when test="@v='survey_point'"/>
<xsl:when test="@v='tower'"/>
<xsl:when test="@v='water_tower'"/>
<xsl:when test="@v='windmill'"/>
<xsl:when test="@v='works'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='military'">
<xsl:choose>
<xsl:when test="@v='airfield'"/>
<xsl:when test="@v='barracks'"/>
<xsl:when test="@v='bunker'"/>
<xsl:when test="@v='danger_area'"/>
<xsl:when test="@v='range'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='name'">

</xsl:when>
<xsl:when test="@k='nat_name'">

</xsl:when>
<xsl:when test="@k='nat_ref'">

</xsl:when>
<xsl:when test="@k='natural'">
<xsl:choose>
<xsl:when test="@v='bay'"/>
<xsl:when test="@v='beach'"/>
<xsl:when test="@v='cliff'"/>
<xsl:when test="@v='coastline'"/>
<xsl:when test="@v='fell'"/>
<xsl:when test="@v='heath'"/>
<xsl:when test="@v='land'"/>
<xsl:when test="@v='marsh'"/>
<xsl:when test="@v='mud'"/>
<xsl:when test="@v='peak'"/>
<xsl:when test="@v='scree'"/>
<xsl:when test="@v='scrub'"/>
<xsl:when test="@v='spring'"/>
<xsl:when test="@v='water'"/>
<xsl:when test="@v='wood'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='note'">

</xsl:when>
<xsl:when test="@k='old_name'">

</xsl:when>
<xsl:when test="@k='old_ref'">

</xsl:when>
<xsl:when test="@k='osmarender:render'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderName'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderRef'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='place'">
<xsl:choose>
<xsl:when test="@v='city'"/>
<xsl:when test="@v='continent'"/>
<xsl:when test="@v='country'"/>
<xsl:when test="@v='county'"/>
<xsl:when test="@v='hamlet'"/>
<xsl:when test="@v='island'"/>
<xsl:when test="@v='region'"/>
<xsl:when test="@v='state'"/>
<xsl:when test="@v='suburb'"/>
<xsl:when test="@v='town'"/>
<xsl:when test="@v='village'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='place_numbers'">

</xsl:when>
<xsl:when test="@k='postal_code'">

</xsl:when>
<xsl:when test="@k='power'">
<xsl:choose>
<xsl:when test="@v='tower'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='railway'">
<xsl:choose>
<xsl:when test="@v='crossing'"/>
<xsl:when test="@v='halt'"/>
<xsl:when test="@v='level_crossing'"/>
<xsl:when test="@v='station'"/>
<xsl:when test="@v='viaduct'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='ref'">

</xsl:when>
<xsl:when test="@k='reg_name'">

</xsl:when>
<xsl:when test="@k='reg_ref'">

</xsl:when>
<xsl:when test="@k='shop'">
<xsl:choose>
<xsl:when test="@v='bakery'"/>
<xsl:when test="@v='butcher'"/>
<xsl:when test="@v='chandler'"/>
<xsl:when test="@v='supermarket'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='source'">
<xsl:choose>
<xsl:when test="@v='extrapolation'"/>
<xsl:when test="@v='historical'"/>
<xsl:when test="@v='image'"/>
<xsl:when test="@v='knowledge'"/>
<xsl:when test="@v='survey'"/>
<xsl:when test="@v='voice'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='source ref'">

</xsl:when>
<xsl:when test="@k='source_ref'">

</xsl:when>
<xsl:when test="@k='sport'">
<xsl:choose>
<xsl:when test="@v='10pin'"/>
<xsl:when test="@v='athletics'"/>
<xsl:when test="@v='baseball'"/>
<xsl:when test="@v='basketball'"/>
<xsl:when test="@v='bowls'"/>
<xsl:when test="@v='climbing'"/>
<xsl:when test="@v='cricket'"/>
<xsl:when test="@v='cricket_nets'"/>
<xsl:when test="@v='croquet'"/>
<xsl:when test="@v='cycling'"/>
<xsl:when test="@v='dog_racing'"/>
<xsl:when test="@v='equestrian'"/>
<xsl:when test="@v='football'"/>
<xsl:when test="@v='golf'"/>
<xsl:when test="@v='gymnastics'"/>
<xsl:when test="@v='hockey'"/>
<xsl:when test="@v='horse_racing'"/>
<xsl:when test="@v='motor'"/>
<xsl:when test="@v='multi'"/>
<xsl:when test="@v='pelota'"/>
<xsl:when test="@v='racquet'"/>
<xsl:when test="@v='rugby'"/>
<xsl:when test="@v='skateboard'"/>
<xsl:when test="@v='skating'"/>
<xsl:when test="@v='skiing'"/>
<xsl:when test="@v='soccer'"/>
<xsl:when test="@v='swimming'"/>
<xsl:when test="@v='table_tennis'"/>
<xsl:when test="@v='tennis'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='toll'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='tourism'">
<xsl:choose>
<xsl:when test="@v='attraction'"/>
<xsl:when test="@v='camp_site'"/>
<xsl:when test="@v='caravan_site'"/>
<xsl:when test="@v='guest_house'"/>
<xsl:when test="@v='hostel'"/>
<xsl:when test="@v='hotel'"/>
<xsl:when test="@v='information'"/>
<xsl:when test="@v='motel'"/>
<xsl:when test="@v='picnic_site'"/>
<xsl:when test="@v='theme_park'"/>
<xsl:when test="@v='viewpoint'"/>
<xsl:when test="@v='zoo'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='waterway'">
<xsl:choose>
<xsl:when test="@v='aqueduct'"/>
<xsl:when test="@v='boatyard'"/>
<xsl:when test="@v='lock_gate'"/>
<xsl:when test="@v='mooring'"/>
<xsl:when test="@v='turning_point'"/>
<xsl:when test="@v='waste_disposal'"/>
<xsl:when test="@v='water_point'"/>
<xsl:when test="@v='weir'"/>

</xsl:choose>
</xsl:when>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Unknown key: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
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
        <xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
            <xsl:choose>
                <xsl:when test="starts-with(@k, 'tiger:')">
                </xsl:when>
                <xsl:when test="starts-with(@k, 'AND_')">
                </xsl:when>
                <xsl:when test="starts-with(@k, 'AND:')">
                </xsl:when>
                <xsl:when test="starts-with(@k, 'gns:')">
                </xsl:when>
                <xsl:when test="@k='created_by'">
                </xsl:when>
                <xsl:when test="@k='converted_by'">
                </xsl:when>
                <xsl:otherwise>
                    <maplint:result ref="tagged-segment">
                        <xsl:value-of select="concat(@k,'=',@v,' ')"/>
                    </maplint:result>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xslout:template>
  <xslout:template name="test-strict-not-in-map_features-segment">
<xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
<xsl:choose>
<xsl:when test="starts-with(@k, 'tiger:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND_')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'gns:')">
</xsl:when>
<xsl:when test="@k='created_by'">

</xsl:when>
<xsl:when test="@k='osmarender:render'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderName'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderRef'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Unknown key: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:for-each>
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
  <xslout:template name="test-strict-not-in-map_features-way">
<xsl:for-each xmlns:xsl="http://www.w3.org/1999/XSL/Transform" select="tag">
<xsl:choose>
<xsl:when test="starts-with(@k, 'tiger:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND_')">
</xsl:when>
<xsl:when test="starts-with(@k, 'AND:')">
</xsl:when>
<xsl:when test="starts-with(@k, 'gns:')">
</xsl:when>
<xsl:when test="@k='abutters'">
<xsl:choose>
<xsl:when test="@v='commercial'"/>
<xsl:when test="@v='industrial'"/>
<xsl:when test="@v='mixed'"/>
<xsl:when test="@v='residential'"/>
<xsl:when test="@v='retail'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='access'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='aerialway'">
<xsl:choose>
<xsl:when test="@v='cable_car'"/>
<xsl:when test="@v='chair_lift'"/>
<xsl:when test="@v='drag_lift'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='aeroway'">
<xsl:choose>
<xsl:when test="@v='apron'"/>
<xsl:when test="@v='runway'"/>
<xsl:when test="@v='taxiway'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='amenity'">
<xsl:choose>
<xsl:when test="@v='bicycle_parking'"/>
<xsl:when test="@v='college'"/>
<xsl:when test="@v='grave_yard'"/>
<xsl:when test="@v='hospital'"/>
<xsl:when test="@v='parking'"/>
<xsl:when test="@v='public_building'"/>
<xsl:when test="@v='school'"/>
<xsl:when test="@v='townhall'"/>
<xsl:when test="@v='university'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='area'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='bicycle'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='boat'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='boundary'">
<xsl:choose>
<xsl:when test="@v='administrative'"/>
<xsl:when test="@v='civil'"/>
<xsl:when test="@v='national_park'"/>
<xsl:when test="@v='political'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='bridge'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="contains(@k, 'name:')">

</xsl:when>
<xsl:when test="@k='created_by'">

</xsl:when>
<xsl:when test="@k='cutting'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='cycleway'">
<xsl:choose>
<xsl:when test="@v='lane'"/>
<xsl:when test="@v='opposite'"/>
<xsl:when test="@v='opposite_lane'"/>
<xsl:when test="@v='opposite_track'"/>
<xsl:when test="@v='track'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='date_off'">
<xsl:choose>
<xsl:when test="@v='Date'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='date_on'">
<xsl:choose>
<xsl:when test="@v='Date'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='day_off'">
<xsl:choose>
<xsl:when test="@v='Day of Week'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='day_on'">
<xsl:choose>
<xsl:when test="@v='Day of Week'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='depth'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='description'">

</xsl:when>
<xsl:when test="@k='embankment'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='end_date'">
<xsl:choose>
<xsl:when test="@v='Date'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='est_depth'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='est_width'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='fenced'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='foot'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='goods'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='hgv'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='highway'">
<xsl:choose>
<xsl:when test="@v='bridleway'"/>
<xsl:when test="@v='cycleway'"/>
<xsl:when test="@v='footway'"/>
<xsl:when test="@v='motorway'"/>
<xsl:when test="@v='motorway_link'"/>
<xsl:when test="@v='pedestrian'"/>
<xsl:when test="@v='primary'"/>
<xsl:when test="@v='primary_link'"/>
<xsl:when test="@v='residential'"/>
<xsl:when test="@v='secondary'"/>
<xsl:when test="@v='service'"/>
<xsl:when test="@v='steps'"/>
<xsl:when test="@v='tertiary'"/>
<xsl:when test="@v='track'"/>
<xsl:when test="@v='trunk'"/>
<xsl:when test="@v='trunk_link'"/>
<xsl:when test="@v='unclassified'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='historic'">
<xsl:choose>
<xsl:when test="@v='archaeological_site'"/>
<xsl:when test="@v='ruins'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='horse'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='hour_off'">
<xsl:choose>
<xsl:when test="@v='Time'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='hour_on'">
<xsl:choose>
<xsl:when test="@v='Time'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='iata'">

</xsl:when>
<xsl:when test="@k='icao'">

</xsl:when>
<xsl:when test="@k='image'">
<xsl:choose>
<xsl:when test="@v='URI'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='int_name'">

</xsl:when>
<xsl:when test="@k='int_ref'">

</xsl:when>
<xsl:when test="@k='is_in'">

</xsl:when>
<xsl:when test="@k='junction'">
<xsl:choose>
<xsl:when test="@v='roundabout'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='landuse'">
<xsl:choose>
<xsl:when test="@v='allotments'"/>
<xsl:when test="@v='basin'"/>
<xsl:when test="@v='brownfield'"/>
<xsl:when test="@v='cemetery'"/>
<xsl:when test="@v='commercial'"/>
<xsl:when test="@v='farm'"/>
<xsl:when test="@v='forest'"/>
<xsl:when test="@v='greenfield'"/>
<xsl:when test="@v='industrial'"/>
<xsl:when test="@v='landfill'"/>
<xsl:when test="@v='quarry'"/>
<xsl:when test="@v='recreation_ground'"/>
<xsl:when test="@v='reservoir'"/>
<xsl:when test="@v='residential'"/>
<xsl:when test="@v='retail'"/>
<xsl:when test="@v='village_green'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='lanes'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='layer'">
<xsl:choose>
<xsl:when test="@v &gt; -5 and @v &lt; 5"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='lcn_ref'">

</xsl:when>
<xsl:when test="@k='leisure'">
<xsl:choose>
<xsl:when test="@v='common'"/>
<xsl:when test="@v='fishing'"/>
<xsl:when test="@v='garden'"/>
<xsl:when test="@v='golf_course'"/>
<xsl:when test="@v='marina'"/>
<xsl:when test="@v='nature_reserve'"/>
<xsl:when test="@v='park'"/>
<xsl:when test="@v='pitch'"/>
<xsl:when test="@v='playground'"/>
<xsl:when test="@v='sports_centre'"/>
<xsl:when test="@v='stadium'"/>
<xsl:when test="@v='track'"/>
<xsl:when test="@v='water_park'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='lit'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='loc_name'">

</xsl:when>
<xsl:when test="@k='loc_ref'">

</xsl:when>
<xsl:when test="@k='man_made'">
<xsl:choose>
<xsl:when test="@v='pier'"/>
<xsl:when test="@v='reservoir_covered'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='maxheight'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='maxlength'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='maxspeed'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='maxweight'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='maxwidth'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='military'">
<xsl:choose>
<xsl:when test="@v='airfield'"/>
<xsl:when test="@v='barracks'"/>
<xsl:when test="@v='danger_area'"/>
<xsl:when test="@v='range'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='minspeed'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='motorboat'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='motorcar'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='motorcycle'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='name'">

</xsl:when>
<xsl:when test="@k='nat_name'">

</xsl:when>
<xsl:when test="@k='nat_ref'">

</xsl:when>
<xsl:when test="@k='natural'">
<xsl:choose>
<xsl:when test="@v='bay'"/>
<xsl:when test="@v='beach'"/>
<xsl:when test="@v='cliff'"/>
<xsl:when test="@v='coastline'"/>
<xsl:when test="@v='fell'"/>
<xsl:when test="@v='heath'"/>
<xsl:when test="@v='land'"/>
<xsl:when test="@v='marsh'"/>
<xsl:when test="@v='mud'"/>
<xsl:when test="@v='scree'"/>
<xsl:when test="@v='scrub'"/>
<xsl:when test="@v='water'"/>
<xsl:when test="@v='wood'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='ncn_ref'">

</xsl:when>
<xsl:when test="@k='noexit'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='note'">

</xsl:when>
<xsl:when test="@k='old_name'">

</xsl:when>
<xsl:when test="@k='old_ref'">

</xsl:when>
<xsl:when test="@k='oneway'">
<xsl:choose>
<xsl:when test="@v='-1'"/>
<xsl:when test="@v='1'"/>
<xsl:when test="@v='false'"/>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='true'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:nameDirection'">
<xsl:choose>
<xsl:when test="@v='1'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:render'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderName'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='osmarender:renderRef'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='place'">
<xsl:choose>
<xsl:when test="@v='city'"/>
<xsl:when test="@v='continent'"/>
<xsl:when test="@v='country'"/>
<xsl:when test="@v='county'"/>
<xsl:when test="@v='hamlet'"/>
<xsl:when test="@v='island'"/>
<xsl:when test="@v='region'"/>
<xsl:when test="@v='state'"/>
<xsl:when test="@v='suburb'"/>
<xsl:when test="@v='town'"/>
<xsl:when test="@v='village'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='place_name'">

</xsl:when>
<xsl:when test="@k='place_numbers'">

</xsl:when>
<xsl:when test="@k='postal_code'">

</xsl:when>
<xsl:when test="@k='power'">
<xsl:choose>
<xsl:when test="@v='line'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='psv'">
<xsl:choose>
<xsl:when test="@v='no'"/>
<xsl:when test="@v='permissive'"/>
<xsl:when test="@v='private'"/>
<xsl:when test="@v='unknown'"/>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='railway'">
<xsl:choose>
<xsl:when test="@v='abandoned'"/>
<xsl:when test="@v='disused'"/>
<xsl:when test="@v='light_rail'"/>
<xsl:when test="@v='monorail'"/>
<xsl:when test="@v='narrow_gauge'"/>
<xsl:when test="@v='preserved'"/>
<xsl:when test="@v='rail'"/>
<xsl:when test="@v='subway'"/>
<xsl:when test="@v='tram'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='rcn_ref'">

</xsl:when>
<xsl:when test="@k='ref'">

</xsl:when>
<xsl:when test="@k='reg_name'">

</xsl:when>
<xsl:when test="@k='reg_ref'">

</xsl:when>
<xsl:when test="@k='route'">
<xsl:choose>
<xsl:when test="@v='bus'"/>
<xsl:when test="@v='ferry '"/>
<xsl:when test="@v='flight'"/>
<xsl:when test="@v='ncn'"/>
<xsl:when test="@v='pub_crawl'"/>
<xsl:when test="@v='ski'"/>
<xsl:when test="@v='subsea'"/>
<xsl:when test="@v='tour'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='shop'">
<xsl:choose>
<xsl:when test="@v='supermarket'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='source'">
<xsl:choose>
<xsl:when test="@v='extrapolation'"/>
<xsl:when test="@v='historical'"/>
<xsl:when test="@v='image'"/>
<xsl:when test="@v='knowledge'"/>
<xsl:when test="@v='survey'"/>
<xsl:when test="@v='voice'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='source ref'">

</xsl:when>
<xsl:when test="@k='source_ref'">

</xsl:when>
<xsl:when test="@k='sport'">
<xsl:choose>
<xsl:when test="@v='10pin'"/>
<xsl:when test="@v='athletics'"/>
<xsl:when test="@v='baseball'"/>
<xsl:when test="@v='basketball'"/>
<xsl:when test="@v='bowls'"/>
<xsl:when test="@v='climbing'"/>
<xsl:when test="@v='cricket'"/>
<xsl:when test="@v='cricket_nets'"/>
<xsl:when test="@v='croquet'"/>
<xsl:when test="@v='cycling'"/>
<xsl:when test="@v='dog_racing'"/>
<xsl:when test="@v='equestrian'"/>
<xsl:when test="@v='football'"/>
<xsl:when test="@v='golf'"/>
<xsl:when test="@v='gymnastics'"/>
<xsl:when test="@v='hockey'"/>
<xsl:when test="@v='horse_racing'"/>
<xsl:when test="@v='motor'"/>
<xsl:when test="@v='multi'"/>
<xsl:when test="@v='pelota'"/>
<xsl:when test="@v='racquet'"/>
<xsl:when test="@v='rugby'"/>
<xsl:when test="@v='skateboard'"/>
<xsl:when test="@v='skating'"/>
<xsl:when test="@v='skiing'"/>
<xsl:when test="@v='soccer'"/>
<xsl:when test="@v='swimming'"/>
<xsl:when test="@v='table_tennis'"/>
<xsl:when test="@v='tennis'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='start_date'">
<xsl:choose>
<xsl:when test="@v='Date'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='surface'">
<xsl:choose>
<xsl:when test="@v='paved'"/>
<xsl:when test="@v='unpaved'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='toll'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='tourism'">
<xsl:choose>
<xsl:when test="@v='attraction'"/>
<xsl:when test="@v='camp_site'"/>
<xsl:when test="@v='caravan_site'"/>
<xsl:when test="@v='picnic_site'"/>
<xsl:when test="@v='theme_park'"/>
<xsl:when test="@v='zoo'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='tracktype'">
<xsl:choose>
<xsl:when test="@v='grade1'"/>
<xsl:when test="@v='grade2'"/>
<xsl:when test="@v='grade3'"/>
<xsl:when test="@v='grade4'"/>
<xsl:when test="@v='grade5'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='tunnel'">
<xsl:choose>
<xsl:when test="@v='yes'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@k='waterway'">
<xsl:choose>
<xsl:when test="@v='canal'"/>
<xsl:when test="@v='dock'"/>
<xsl:when test="@v='drain'"/>
<xsl:when test="@v='river'"/>
<xsl:when test="@v='stream'"/>

</xsl:choose>
</xsl:when>
<xsl:when test="@k='width'">
<xsl:choose>
<xsl:when test="string(number(@v)) != 'NaN'"/>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Value not in map features: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:otherwise>
<maplint:result ref="not-in-map_features"><xsl:value-of select="concat('Unknown key: ', @k, '=', @v)"/></maplint:result>
</xsl:otherwise>
</xsl:choose>
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
