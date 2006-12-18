<?xml version='1.0' encoding='iso-8859-1' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:key name='nodeById' match='/osm/node' use='@id'/>
	<xsl:key name='segmentById' match='/osm/segment' use='@id'/>
	<xsl:key name='segmentByFromNode' match='/osm/segment' use='@from'/>
	<xsl:key name='segmentByToNode' match='/osm/segment' use='@to'/>
	<xsl:key name='wayBySegment' match='/osm/way' use='seg/@id'/>
	
	<xsl:variable name='data' select='document(/rules/@data)'/>
	<xsl:variable name='defs' select='/rules/defs'/>
	<xsl:variable name='overlays' select='/rules/overlays'/>

    <xsl:template match="/">
        <kml>
            <Document>
                <name>OpenStreetMap</name>
                <description>OpenStreetMap Data. See http://www.openstreetmap.org/</description>

                <xsl:copy-of select="$defs/*"/>
                <xsl:copy-of select="$overlays/*"/>

                <xsl:apply-templates select="/rules/rule">
                    <xsl:with-param name='elements' select='$data/osm/*' />
                </xsl:apply-templates>
            </Document>
        </kml>
            
    </xsl:template>

    <xsl:template match="rule">
		<xsl:param name='elements'/>

		<!-- This is the rule currently being processed -->
		<xsl:variable name='rule' select='.'/>

		<!-- Make list of elements that this rule should be applied to -->
		<xsl:variable name='eBare'>
			<xsl:choose>
				<xsl:when test='$rule/@e="*"'>node|segment|way|area</xsl:when>
				<xsl:when test='$rule/@e'><xsl:value-of select='$rule/@e'/></xsl:when>
				<xsl:otherwise>node|segment|way|area</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- List of keys that this rule should be applied to -->
		<xsl:variable name='kBare' select='$rule/@k' />

		<!-- List of values that this rule should be applied to -->
		<xsl:variable name='vBare' select='$rule/@v' />

		<!-- Top'n'tail selectors with | for contains usage -->
		<xsl:variable name='e'>|<xsl:value-of select='$eBare'/>|</xsl:variable>
		<xsl:variable name='k'>|<xsl:value-of select='$kBare'/>|</xsl:variable>
		<xsl:variable name='v'>|<xsl:value-of select='$vBare'/>|</xsl:variable>


		<xsl:variable name='selectedElements' select='$elements[contains($e,concat("|",name(),"|"))or (contains($e,"|waysegment|") and name()="segment" and key("wayBySegment",@id))]'/>

		<xsl:choose>
			<xsl:when test='contains($k,"|*|")'>
				<xsl:choose>
					<xsl:when test='contains($v,"|~|")'>
						<xsl:variable name='elementsWithNoTags' select='$selectedElements[count(tag)=0]'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='elements' select='$elementsWithNoTags'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test='contains($v,"|*|")'>
						<xsl:variable name='allElements' select='$selectedElements'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='elements' select='$allElements'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name='allElementsWithValue' select='$selectedElements[tag[contains($v,concat("|",@v,"|"))]]'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='elements' select='$allElementsWithValue'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test='contains($v,"|~|")'>
				<xsl:variable name='elementsWithoutKey' select='$selectedElements[count(tag[contains($k,concat("|",@k,"|"))])=0]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='elements' select='$elementsWithoutKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test='contains($v,"|*|")'>
				<xsl:variable name='allElementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|"))]]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='elements' select='$allElementsWithKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name='elementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|")) and contains($v,concat("|",@v,"|"))]]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='elements' select='$elementsWithKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>

	<!-- Process a set of elements selected by a rule  -->
	<xsl:template name='processElements'>
		<xsl:param name='eBare'/>
		<xsl:param name='kBare'/>
		<xsl:param name='vBare'/>
		<xsl:param name='elements'/>
		<xsl:param name='rule'/>
		
		<xsl:if test='$elements'>
			<xsl:message>
Processing &lt;rule e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt; 
Matched by <xsl:value-of select='count($elements)'/> elements.
			</xsl:message>

			<xsl:apply-templates select='*'>
				<xsl:with-param name='elements' select='$elements' />
				<xsl:with-param name='rule' select='$rule'/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

    <xsl:template match="point|city">
		<xsl:param name='elements'/>
		<xsl:param name='rule'/>

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

        <!-- For each point or city -->
        <xsl:apply-templates select='$elements' mode='point'>
            <xsl:with-param name='instruction' select='$instruction' />
        </xsl:apply-templates>
	</xsl:template>

    <xsl:template match="folder">
		<xsl:param name='elements'/>
        <Folder>
            <name><xsl:value-of select="@name"/></name>
            <xsl:apply-templates>
		        <xsl:with-param name='elements' select="$elements"/>
            </xsl:apply-templates>
        </Folder> 
	</xsl:template>

    <xsl:template match="polyline|polygon">
		<xsl:param name='elements'/>
		<xsl:param name='rule'/>

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

        <Placemark>
            <xsl:if test="$instruction/@name">
                <name><xsl:value-of select="$instruction/@name"/></name>
            </xsl:if>
            <styleUrl>#<xsl:value-of select="$instruction/@class"/></styleUrl>
            <MultiGeometry>
                <!-- For each segment and way -->
                <xsl:apply-templates select='$elements' mode='line'>
                    <xsl:with-param name='instruction' select='$instruction' />
                </xsl:apply-templates>
            </MultiGeometry>
        </Placemark>
	</xsl:template>

	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='line'/>
	<xsl:template match='*' mode='point'/>

	<!-- Draw points -->
    <xsl:template match="node" mode="point">
		<xsl:param name='instruction' />

        <Placemark>
            <xsl:if test="tag[(@k='name')and(@v!='')]">
                <name><xsl:value-of select="tag[@k='name']/@v"/></name>
            </xsl:if>
            <styleUrl>#<xsl:value-of select="$instruction/@class"/></styleUrl>
            <Point>
                <coordinates>
                    <xsl:value-of select="@lon"/><xsl:text>,</xsl:text>
                    <xsl:value-of select="@lat"/><xsl:text>,0
</xsl:text>
                </coordinates>
            </Point>
        </Placemark>
	</xsl:template>

	<!-- Draw lines for a segment -->
    <xsl:template match="segment" mode="line">
		<xsl:param name='instruction' />

		<xsl:call-template name='drawLine'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='segment' select='.'/>
		</xsl:call-template>
	</xsl:template>

	<!-- Draw lines for a way (draw all the segments that belong to the way) -->
	<xsl:template match='way' mode='line'>
		<xsl:param name='instruction' />

		<!-- The current <way> element -->
		<xsl:variable name='way' select='.' />
		
		<xsl:call-template name='drawWay'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='way' select='$way'/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- Drawing templates                                                             -->
	<!-- ============================================================================= -->

	<!-- Draw a line for the current <segment> element using the formatting of the current <line> instruction -->
	<xsl:template name='drawLine'>
		<xsl:param name='instruction'/>
		<xsl:param name='segment'/> <!-- The current segment element -->
		<xsl:param name='way'/>  <!-- The current way element if applicable -->

		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>

            <LineString>
                <coordinates>
                    <xsl:value-of select="$fromNode/@lon"/>,<xsl:value-of select="$fromNode/@lat"/>
                    <xsl:text>,0
</xsl:text>
                    <xsl:value-of select="$toNode/@lon"/>,<xsl:value-of select="$toNode/@lat"/>
                    <xsl:text>,0
</xsl:text>
                </coordinates>
            </LineString>
	</xsl:template>

	<!-- Draw a line for the current <way> element using the formatting of the current <line> instruction -->	
	<xsl:template name='drawWay'>
		<xsl:param name='instruction'/>
		<xsl:param name='way'/>  <!-- The current way element if applicable -->

        <xsl:choose>
            <xsl:when test="name($instruction)='polyline'">
                <LineString>
                    <xsl:call-template name="coordinates">
                        <xsl:with-param name="way" select="$way"/>
                    </xsl:call-template>
                </LineString>
            </xsl:when>
            <xsl:otherwise>
                <Polygon>
                    <altitudeMode>relativeToGround</altitudeMode>
                    <outerBoundaryIs>
                        <LinearRing>
                            <xsl:call-template name="coordinates">
                                <xsl:with-param name="way" select="$way"/>
                            </xsl:call-template>
                        </LinearRing>
                    </outerBoundaryIs>
                </Polygon>
            </xsl:otherwise>
        </xsl:choose>

	</xsl:template>

    <xsl:template name="coordinates">
		<xsl:param name='way'/>  <!-- The current way element if applicable -->
        <coordinates>
            <xsl:for-each select="$way/seg">
                <xsl:variable name="segment" select='key("segmentById", @id)'/>
                <xsl:variable name='from' select='$segment/@from'/>
                <xsl:variable name='to' select='$segment/@to'/>
                <xsl:variable name='fromNode' select='key("nodeById",$from)'/>
                <xsl:variable name='toNode' select='key("nodeById",$to)'/>
                <xsl:if test="$fromNode/@lat!=''">
                    <xsl:value-of select="$fromNode/@lon"/>,<xsl:value-of select="$fromNode/@lat"/>
            <xsl:text>,0
</xsl:text>
                    <xsl:value-of select="$toNode/@lon"/>,<xsl:value-of select="$toNode/@lat"/>
            <xsl:text>,0
</xsl:text>
                </xsl:if>
            </xsl:for-each>

        </coordinates>
	</xsl:template>

</xsl:stylesheet>
