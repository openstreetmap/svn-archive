<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Templates to process line, circle, text, etc. instructions -->

<xsl:stylesheet 
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Each template is passed a variable containing the set of elements that need to
	     be processed.  The set of elements is already determined by the rules, so 
	     these templates don't need to know anything about the rules context they are in. -->

	<!-- Process a <line> instruction -->
	<xsl:template match='line'>
		<xsl:param name='elements' />
		<xsl:param name='layer' />
		<xsl:param name='classes' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

		<g>
			<xsl:apply-templates select='@*' mode='copyAttributes'> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->
                <xsl:with-param name="classes" select="$classes"/>
			</xsl:apply-templates>

			<!-- For each segment and way -->
			<xsl:apply-templates select='$elements' mode='line'>
				<xsl:with-param name='instruction' select='$instruction' />
				<xsl:with-param name='layer' select='$layer' />
				<xsl:with-param name='classes' select='$classes' />
			</xsl:apply-templates>

		</g>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='line'/>
	
	
	<!-- Draw lines for a segment -->
	<xsl:template match='segment' mode='line'>
		<xsl:param name='instruction' />
		<xsl:param name='classes' />

		<xsl:call-template name='drawLine'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='segment' select='.'/>
			<xsl:with-param name='classes' select='$classes' />
		</xsl:call-template>

	</xsl:template>


	<!-- Draw lines for a way (draw all the segments that belong to the way) -->
	<xsl:template match='way' mode='line'>
		<xsl:param name='instruction' />
		<xsl:param name='layer' />
		<xsl:param name='classes' />

		<!-- The current <way> element -->
		<xsl:variable name='way' select='.' />
		
		<xsl:call-template name='drawWay'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='way' select='$way'/>
			<xsl:with-param name='layer' select='$layer' />
			<xsl:with-param name='classes' select='$classes' />
		</xsl:call-template>

	</xsl:template>
	

	<!-- Process an <area> instruction -->
	<xsl:template match='area'>
		<xsl:param name='elements' />
		<xsl:param name='classes' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

		<g>
			<xsl:apply-templates select='@*' mode='copyAttributes'/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

			<!-- For each segment and way -->
			<xsl:apply-templates select='$elements' mode='area'>
				<xsl:with-param name='instruction' select='$instruction' />
			</xsl:apply-templates>

		</g>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='area'/>
	
	
	<!-- Draw area for a <way> -->
	<xsl:template match='way' mode='area'>
		<xsl:param name='instruction' />

		<xsl:call-template name='generateAreaPath' />

		<xsl:call-template name='renderArea'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("area_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Process circle instruction -->
	<xsl:template match='circle'>
		<xsl:param name='elements'/>
		<xsl:param name='classes' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />

		<xsl:for-each select='$elements[name()="node"]'>
			<xsl:call-template name='drawCircle'>
				<xsl:with-param name='instruction' select='$instruction'/>
			</xsl:call-template>					
		</xsl:for-each>
	</xsl:template>


	<!-- Process a symbol instruction -->
	<xsl:template match='symbol'>
		<xsl:param name='elements'/>
		<xsl:param name='classes' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />

		<xsl:for-each select='$elements[name()="node"]'>
			<xsl:call-template name='drawSymbol'>
				<xsl:with-param name='instruction' select='$instruction'/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>	


	<!-- Process a <text> instruction -->
	<xsl:template match='text'>
		<xsl:param name='elements'/>
		<xsl:param name='classes' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />
		
		<!-- Select all <node> elements that have a key that matches the k attribute of the text instruction -->
		<xsl:for-each select='$elements[name()="node"][tag[@k=$instruction/@k]]'>
				<xsl:call-template name='renderText'>
					<xsl:with-param name='instruction' select='$instruction'/>
				</xsl:call-template>					
		</xsl:for-each>

		<!-- Select all <segment> and <way> elements that have a key that matches the k attribute of the text instruction -->
		<xsl:apply-templates select='$elements[name()="segment" or name()="way"][tag[@k=$instruction/@k]]' mode='textPath'>
			<xsl:with-param name='instruction' select='$instruction' />
		</xsl:apply-templates>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='textPath'/>


	<!-- Render textPaths for a segment -->
	<xsl:template match='segment' mode='textPath'>
		<xsl:param name='instruction' />
		
		<!-- The current <segment> element -->
		<xsl:variable name='segment' select='.' />

		<!-- Generate the path for the segment -->
		<!-- Text on segments should be relatively uncommon so only generate a <path> when one is needed -->
		<xsl:call-template name='generateSegmentPath' />

		<xsl:call-template name='renderTextPath'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("segment_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Render textPaths for a way -->
	<xsl:template match='way' mode='textPath'>
		<xsl:param name='instruction' />

		<!-- The current <way> element -->
		<xsl:variable name='way' select='.' />

		<xsl:call-template name='renderTextPath'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("way_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Generate a way path for the current segment -->
	<xsl:template name='generateSegmentPath'>
		<xsl:variable name='pathData'>
			<xsl:choose>
				<xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1" or (key("nodeById",@from)/@lon &gt; key("nodeById",@to)/@lon)'>
					<xsl:call-template name='segmentMoveToEnd'/>
					<xsl:call-template name='segmentLineToStart'/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name='segmentMoveToStart'/>
					<xsl:call-template name='segmentLineToEnd'/>			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<path id="segment_{@id}" d="{$pathData}"/>

	</xsl:template>


	<!-- Generate a way path for the current way element -->
	<xsl:template name='generateWayPath'>
		
		<!-- Generate the path for the way -->
		<xsl:variable name='pathData'>
			<xsl:choose>
<!--				<xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1" or (key("nodeById",key("segmentById",seg[1]/@id)/@from)/@lon &gt; key("nodeById",key("segmentById",seg[last()]/@id)/@to)/@lon)'>-->
<!--				<xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>-->
                <xsl:when test='(tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1") != (key("nodeById",key("segmentById",seg [1]/@id)/@from)/@lon &lt; key("nodeById",key("segmentById",seg[last()]/@id)/@to)/@lon)'>
					<xsl:for-each select='seg[key("segmentById",@id)]'>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=key("segmentById",preceding-sibling::seg[1]/@id)/@to'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:if test='not($linkedSegment)'>
								<xsl:call-template name='segmentMoveToStart'/>				
							</xsl:if>
								<xsl:call-template name='segmentLineToEnd'/>							
						</xsl:for-each>
					</xsl:for-each>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select='seg'>
						<xsl:sort select='position()' data-type='number' order='descending'/>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='linkedSegment' select='key("segmentById",following-sibling::seg[1]/@id)/@from=key("segmentById",@id)/@to'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:if test='not($linkedSegment)'>
								<xsl:call-template name='segmentMoveToEnd'/>
							</xsl:if>
								<xsl:call-template name='segmentLineToStart'/>
						</xsl:for-each>
					</xsl:for-each>				
				</xsl:otherwise>			
			</xsl:choose>
		</xsl:variable>

		<path id="way_{@id}t" d="{$pathData}"/>

		<!-- Generate the path for the way -->
		<xsl:variable name='pathDataFixed'>
            <xsl:for-each select='seg[key("segmentById",@id)]'>
                <xsl:variable name='segmentId' select='@id'/>
                <xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=key("segmentById",preceding-sibling::seg[1]/@id)/@to'/>
                <xsl:for-each select='key("segmentById",$segmentId)'>
                    <xsl:if test='not($linkedSegment)'>
                        <xsl:call-template name='segmentMoveToStart'/>				
                    </xsl:if>
                        <xsl:call-template name='segmentLineToEnd'/>							
                </xsl:for-each>
            </xsl:for-each>				
		</xsl:variable>

		<path id="way_{@id}" d="{$pathDataFixed}"/>

	</xsl:template>


	<!-- Generate an area path for the current way or area element -->
	<xsl:template name='generateAreaPath'>

		<!-- Generate the path for the area -->
		<xsl:variable name='pathData'>
			<xsl:for-each select='seg[key("segmentById",@id)]'>
				<xsl:variable name='segmentId' select='@id'/>
				<xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=key("segmentById",preceding-sibling::seg[1]/@id)/@to'/>
				<xsl:variable name='segmentSequence' select='position()'/>
				<xsl:for-each select='key("segmentById",$segmentId)'>
                    <xsl:if test='$segmentSequence=1'>
                        <xsl:call-template name='segmentMoveToStart'/>				
                    </xsl:if>
                    <xsl:if test='not($linkedSegment)'>
                        <xsl:call-template name='segmentLineToStart'/>				
                    </xsl:if>
                    <xsl:call-template name='segmentLineToEnd'/>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:text>Z</xsl:text>
		</xsl:variable>

		<path id="area_{@id}" d="{$pathData}"/>

	</xsl:template>


	<!-- Generate a MoveTo command for a segment start -->
	<xsl:template name='segmentMoveToStart'>
		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>

		<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
		<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>M</xsl:text>
		<xsl:value-of select='$x1'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y1'/>
	</xsl:template>

		
	<!-- Generate a LineTo command for a segment start -->
	<xsl:template name='segmentLineToStart'>
		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>

		<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
		<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>L</xsl:text>
		<xsl:value-of select='$x1'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y1'/>
	</xsl:template>


	<!-- Generate a MoveTo command for a segment end -->
	<xsl:template name='segmentMoveToEnd'>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>

		<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)'/>
		<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>M</xsl:text>
		<xsl:value-of select='$x2'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y2'/>
	</xsl:template>


	<!-- Generate a LineTo command for a segment end -->
	<xsl:template name='segmentLineToEnd'>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>

		<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)'/>
		<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>L</xsl:text>
		<xsl:value-of select='$x2'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y2'/>
	</xsl:template>

</xsl:stylesheet>
