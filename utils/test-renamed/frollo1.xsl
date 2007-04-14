<?xml version="1.0" encoding="UTF-8"?>
<!--
==============================================================================

Frollo - Osmarender pre-processor
         Part 1, count the number of segments that connect to each end of
         each segment in a way.  Output two attributes, osma:fromCount
         and osma:toCount for each <seg> in a <way>.

==============================================================================

Copyright (C) 2007 Etienne Cherdlu

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA

==============================================================================
-->
<xsl:stylesheet version="1.0"
  xmlns:osma="http://wiki.openstreetmap.org/index.php/Osmarender/Frollo/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>


    <xsl:key name="segmentById" match="/osm/segment" use="@id"/>
    
    <xsl:template match='/osm'>
		<osm>
			<xsl:apply-templates select='@*|node()'/>
		</osm>
    </xsl:template>

	<xsl:template match='seg'>
		<seg>
			<xsl:apply-templates select='@*'/>

			<!-- Count the number of segments *in this way* that connect to the from node of this segment -->
			<xsl:variable name='currentSegmentFromNodeId' select='key("segmentById",@id)/@from' />
			<xsl:if test='$currentSegmentFromNodeId'>
				<xsl:variable name='fromCount' 
					select='count(../seg[key("segmentById",@id)/@to=$currentSegmentFromNodeId])
					        + count(../seg[key("segmentById",@id)/@from=$currentSegmentFromNodeId]) 
					        - 1' />
				<xsl:attribute name='osma:fromCount'><xsl:value-of select='$fromCount'/></xsl:attribute>					
			</xsl:if>

			<!-- Count the number of segments *in this way* that connect to the to node of this segment -->
			<xsl:variable name='currentSegmentToNodeId' select='key("segmentById",@id)/@to' />
			<xsl:if test='$currentSegmentToNodeId'>
				<xsl:variable name='toCount' 
					select='count(../seg[key("segmentById",@id)/@to=$currentSegmentToNodeId])
					        + count(../seg[key("segmentById",@id)/@from=$currentSegmentToNodeId]) 
					        - 1' />
				<xsl:attribute name='osma:toCount'><xsl:value-of select='$toCount'/></xsl:attribute>					
			</xsl:if>

		</seg>
	</xsl:template>



	<!-- Identity Transform -->
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
