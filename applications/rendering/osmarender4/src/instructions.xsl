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


    <!-- Process a <tunnel> instruction -->
    <xsl:template match='tunnel'>
        <xsl:param name='elements' />
        <xsl:param name='layer' />
        <xsl:param name='classes' />

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name='instruction' select='.'/>

        <g>
            <xsl:apply-templates select='@*' mode='copyAttributes'> <!-- Add all the svg attributes of the <tunnel> instruction to the <g> element -->
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>

            <!-- For each segment and way -->
            <xsl:apply-templates select='$elements' mode='tunnel'>
                <xsl:with-param name='instruction' select='$instruction' />
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='classes' select='$classes' />
            </xsl:apply-templates>
        </g>
    </xsl:template>

    <!-- Draw tunnel for a way (draw all the segments that belong to the way) -->
    <xsl:template match='way' mode='tunnel'>
        <xsl:param name='instruction' />
        <xsl:param name='layer' />
        <xsl:param name='classes' />

        <!-- The current <way> element -->
        <xsl:variable name='way' select='.' />

        <xsl:call-template name='drawTunnel'>
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
            <xsl:with-param name='pathId' select='concat("way_",@id,"t")'/>
        </xsl:call-template>

    </xsl:template>


    <!-- Generate a way path for the current segment -->
    <xsl:template name='generateSegmentPath'>
        <xsl:variable name='pathData'>
            <xsl:choose>
				<!-- Manual override -->
                <xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
                    <xsl:call-template name='segmentMoveToEnd'/>
                    <xsl:call-template name='segmentLineToStart'/>
                </xsl:when>
                <xsl:when test='tag[@k="name_direction"]/@v="1" or tag[@k="osmarender:nameDirection"]/@v="1"'>
                    <xsl:call-template name='segmentMoveToStart'/>
                    <xsl:call-template name='segmentLineToEnd'/>
                </xsl:when>
                <!-- Automatic direction -->
                <xsl:when test='(key("nodeById",@from)/@lon &gt; key("nodeById",@to)/@lon)'>
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

        <!-- Generate the path for the way that will be used by the street
        name rendering. This is horribly inefficient, because we will later
        also have the path used for the rendering of the path itself. So
        each path is twice in the SVG file. But this path here needs to
        have the right direction for the names to render right way up
        and the other path needs to be the right direction for rendering
        the oneway arrows. This can probably be done better, but currently
        I don't know how. -->
        <xsl:variable name='pathData'>
            <xsl:choose>
				<!-- Manual override, reverse direction -->
                <xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
					<xsl:call-template name='generateWayPathReverse'/>
                </xsl:when>
				<!-- Manual override, normal direction -->
                <xsl:when test='tag[@k="name_direction"]/@v="1" or tag[@k="osmarender:nameDirection"]/@v="1"'>
					<xsl:call-template name='generateWayPathNormal'/>
                </xsl:when>
				<!-- Automatic, reverse direction -->
                <xsl:when test='(key("nodeById",key("segmentById",seg[1]/@id)/@from)/@lon &gt; key("nodeById",key("segmentById",seg[last()]/@id)/@to)/@lon)'>
					<xsl:call-template name='generateWayPathReverse'/>
                </xsl:when>
				<!-- Automatic, normal direction -->
                <xsl:otherwise>
					<xsl:call-template name='generateWayPathNormal'/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <path id="way_{@id}t" d="{$pathData}"/>

        <!-- Generate the path for the way itself. Used for rendering the
        way and, possibly, oneway arrows. -->
        <xsl:variable name='pathDataFixed'>
			<xsl:call-template name='generateWayPathNormal'/>
        </xsl:variable>

        <path id="way_{@id}" d="{$pathDataFixed}"/>

    </xsl:template>
    

    <!-- Generate a way path in the normal order of the segments in the way -->
    <xsl:template name="generateWayPathNormal">
		<xsl:choose>
			<xsl:when test='$bFrollo'>			
				<xsl:for-each select='seg[key("segmentById",@id)]'>
				    <xsl:variable name='segmentId' select='@id'/>
					<xsl:variable name='bReverseSeg' select='@osma:reverse="1"'/>
					<xsl:variable name='bSubPath' select='(position()=1) or (@osma:sub-path="1")'/>
				    <xsl:for-each select='key("segmentById",$segmentId)'>
						<xsl:choose>
							<xsl:when test='$bReverseSeg'>
								<xsl:if test='$bSubPath'>
								    <xsl:call-template name='segmentMoveToEnd'/>
								</xsl:if>
								<xsl:call-template name='segmentLineToStart'/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test='$bSubPath'>
								    <xsl:call-template name='segmentMoveToStart'/>
								</xsl:if>
								<xsl:call-template name='segmentLineToEnd'/>
							</xsl:otherwise>				
						</xsl:choose>
				    </xsl:for-each>
				</xsl:for-each>				
			</xsl:when>
			<xsl:otherwise> <!-- Not pre-processed by frollo -->
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
			</xsl:otherwise>		
		</xsl:choose>
    </xsl:template>


    <!-- Generate a way path in the reverse order of the segments in the way -->
    <xsl:template name="generateWayPathReverse">
		<xsl:choose>
			<xsl:when test='$bFrollo'>			
				<xsl:for-each select='seg'>
				    <xsl:sort select='position()' data-type='number' order='descending'/>
				    <xsl:variable name='segmentId' select='@id'/>
					<xsl:variable name='bReverseSeg' select='@osma:reverse="1"'/>
					<xsl:variable name='bSubPath' select='(position()=1) or (preceding-sibling::seg/@osma:sub-path="1")'/>
				    <xsl:for-each select='key("segmentById",$segmentId)'>
						<xsl:choose>
							<xsl:when test='$bReverseSeg'>
								<xsl:if test='$bSubPath'>
								    <xsl:call-template name='segmentMoveToStart'/>
								</xsl:if>
								<xsl:call-template name='segmentLineToEnd'/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test='$bSubPath'>
								    <xsl:call-template name='segmentMoveToEnd'/>
								</xsl:if>
								<xsl:call-template name='segmentLineToStart'/>
							</xsl:otherwise>				
						</xsl:choose>
				    </xsl:for-each>
				</xsl:for-each>    
			</xsl:when>
			<xsl:otherwise> <!-- Not pre-processed by frollo -->
				<xsl:for-each select='seg'>
				    <xsl:sort select='position()' data-type='number' order='descending'/>
				    <xsl:variable name='segmentId' select='@id'/>
				    <xsl:variable name='linkedSegment' select='key("segmentById",following-sibling::seg[1]/@id)/@from=key("segmentById",@id)/@to'/>
				    <xsl:for-each select='key("segmentById",$segmentId)'>
						<xsl:if test='not($linkedSegment)'>
							<xsl:call-template name='segmentMoveToStart'/>
						</xsl:if>
						<xsl:call-template name='segmentLineToEnd'/>
				    </xsl:for-each>
				</xsl:for-each>	
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>


	<!-- Generate an area path for the current way or area element -->
	<xsl:template name='generateAreaPath'>

		<!-- Generate the path for the area -->
		<xsl:variable name='pathData'>
			<xsl:choose>
				<xsl:when test='$bFrollo'>			
					<xsl:for-each select='seg[key("segmentById",@id)]'>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='bReverseSeg' select='@osma:reverse="1"'/>
						<xsl:variable name='bSubPath' select='@osma:sub-path="1"'/>						
						<xsl:variable name='fromCount' select='@osma:fromCount'/>						
						<xsl:variable name='segmentSequence' select='position()'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:choose>
								<xsl:when test='not($bReverseSeg)'>
									<xsl:choose>
										<!-- If this is the start of the way then we always have to move to the start of the segment. -->
										<xsl:when test='$segmentSequence=1'>
											<xsl:call-template name='segmentMoveToStart'/>				
										</xsl:when>
										
										<!-- If the segment is connected to another segment (at the from end) and is the start of a new
											 sub-path then start a new sub-path -->
										<xsl:when test='$fromCount>0 and $bSubPath'>
											<xsl:text>Z</xsl:text>
											<xsl:call-template name='segmentMoveToStart'/>				
										</xsl:when>
										
										<!-- If the segment is the start of a new sub-path, but is not connected to any previous
										     segment then draw an artificial line.  Typically this happens when a tile boundary chops
										     through the middle of an area or a river is segmented into manageable chunks. -->
										<xsl:otherwise>
											<xsl:call-template name='segmentLineToStart'/>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:call-template name='segmentLineToEnd'/>								
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<!-- If this is the start of the way then we always have to move to the start of the segment. -->
										<xsl:when test='$segmentSequence=1'>
											<xsl:call-template name='segmentMoveToEnd'/>				
										</xsl:when>
										
										<!-- If the segment is connected to another segment (at the from end) and is the start of a new
											 sub-path then start a new sub-path -->
										<xsl:when test='$fromCount>0 and $bSubPath'>
											<xsl:text>Z</xsl:text>
											<xsl:call-template name='segmentMoveToEnd'/>				
										</xsl:when>
										
										<!-- If the segment is the start of a new sub-path, but is not connected to any previous
										     segment then draw an artificial line.  Typically this happens when a tile boundary chops
										     through the middle of an area or a river is segmented into manageable chunks. -->
										<xsl:otherwise>
											<xsl:call-template name='segmentLineToEnd'/>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:call-template name='segmentLineToStart'/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:for-each>
					<xsl:text>Z</xsl:text>
				</xsl:when>
				<xsl:otherwise> <!-- Not pre-processed by frollo -->
					<xsl:for-each select='seg[key("segmentById",@id)]'>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='currentSegmentToNodeId' select='key("segmentById",@id)/@to' />
						<xsl:variable name='currentSegmentFromNodeId' select='key("segmentById",@id)/@from' />
						<xsl:variable name='previousSegmentToNodeId' select='key("segmentById",preceding-sibling::seg[1]/@id)/@to' />
						
						<!-- The linkedSegment flag indicates whether the previous segment is connected to the current segment.  If it isn't
						     then we will need to draw an additional line (segmentLineToStart) from the end of the previous segment to the
						     start of the current segment. 
						-->
						<xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=$previousSegmentToNodeId'/>
		
						<!--  Now we count the number of segments in this way that have a to node that is equal to the current segment's from node.
						      We do this to find out if the current segment is connected from some other segment in the way.  If it is, and it
						      is not linked to the current segment then we can assume we have the start of a new sub-path.  In this case we shouldn't
						      draw an additional line between the end of the previous segment and the start of the current segment.
						-->
						<xsl:variable name='connectedSegmentCount' select='count(../*[key("segmentById",@id)/@to=$currentSegmentFromNodeId])' />
						
						<xsl:variable name='segmentSequence' select='position()'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:choose>
								<!-- If this is the start of the way then we always have to move to the start of the segment. -->
								<xsl:when test='$segmentSequence=1'>
									<xsl:call-template name='segmentMoveToStart'/>				
								</xsl:when>
								<!-- If the segment is "connected" to another segment (at the from end) but is not linked to the
									 previous segment, then start a new sub-path -->
								<xsl:when test='$connectedSegmentCount>0 and not($linkedSegment)'>
									<xsl:text>Z</xsl:text>
									<xsl:call-template name='segmentMoveToStart'/>				
								</xsl:when>
								<!-- If the previous segment is not linked to this one we need to draw an artificial line -->
								<xsl:when test='not($linkedSegment)'>
									<xsl:call-template name='segmentLineToStart'/>				
								</xsl:when>
							</xsl:choose>
							<xsl:call-template name='segmentLineToEnd'/>
						</xsl:for-each>
					</xsl:for-each>
					<xsl:text>Z</xsl:text>	
				</xsl:otherwise>
			</xsl:choose>
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
