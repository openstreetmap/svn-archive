<?xml version="1.0" encoding="UTF-8"?>
<!--
==============================================================================

Frollo - Osmarender pre-processor
         Part 2, for each way sort the segments into a series of best-fit linear
         paths.  Annotate <seg> elements with osma:reverse=1 where the segment
         is pointing in the wrong direction and annotate with osma:sub-path=1
         when the segemnts are not contiguous.

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

<!-- Strategy.  Find a loose end.  Follow that loose end until we reach the end or another segment that has
                already been processed.  Repeat until all loose ends have been processed.  Finally pick
                up any unprocessed segments and follow them.  Repeat until all segments processed.

				A loose end is any segment in a way that does not have any other segments (in that way) leading
				to it.  We will also look for segments that have no other segments leading from them and process
				these in the reverse direction.
				
				We follow the path of a way along its segments using the following rules in this order:
				1) If the to end of the current segment connect to the from end of the next segment in the 
				   way then use that segment
				2) If the to end of the current segment connect to the to end of the next segment in the way 
				   then use that segment in reverse
				3) If the to end of the current segment connects to the from end of any other segments in the 
				   way then use the first such segment
				4) If the to end of the current segment connects to the to end of any other segments in the way 
				   then use the first such segment in reverse
				5) Find the next loose end, if there is one, and start a new sub-path
				6) Find the next unprocessed segment, if there is one, and start a new sub-path

				In all cases, if the segment is being processed in reverse then treat the from end as the to end and
				vice-versa.
			
-->

<xsl:stylesheet version="1.0"
  xmlns:osma="http://wiki.openstreetmap.org/index.php/Osmarender/Frollo/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes='osma'>

    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

	<!-- Some xsl processors are a bit recursion challenged (eg xalan-j).  This variable enables you to restrict the number of segs that
	     get sorted.  If a way has more than this number of segs then the rest just get output in their original order.  -->
	<xsl:variable name='maximumNumberOfSegs' select='"800"'/>
		

	<!-- Keys -->
	<!-- Lookup for segments by segment id -->
    <xsl:key name="segmentById" match="/osm/segment" use="@id"/>
    
    
    <!-- Add an attribute to the osm element to indicate that this file has been frolloised -->
    <xsl:template match='/osm'>
		<osm>
			<xsl:attribute name='osma:frollo'>1</xsl:attribute>
			<xsl:apply-templates select='@*|node()'/>
		</osm>
    </xsl:template>
    
    
    <!-- Process each way -->
	<xsl:template match='way'>
		<way>
			<xsl:apply-templates select='@*'/>

			<!-- If there are any segs, then make the first seg the context node and then find the next loose end -->
			<xsl:for-each select='seg[1]'>
				<xsl:call-template name='nextSeg'>
					<xsl:with-param name='processedSegs' select='"|"'/>
					<xsl:with-param name='debug' select='"firstSeg"'/>
				</xsl:call-template>			
			</xsl:for-each>

			<xsl:apply-templates select='*'/>
		</way>
	</xsl:template>    
    
    
	<!-- Process the next most suitable seg.  This will either be the first loose end, or, if there are no more unprocessed
	     loose ends then it will be the next unprocessed seg in original seg order. -->
	<xsl:template name='nextSeg'>
		<xsl:param name='processedSegs'/>
		<xsl:param name='debug'/>
		
		<!-- A loose-end is any seg that has no other segs connecting to/from it in this way.  We prefer "from" loose-ends but
		     will be happy with "to" loose-ends if necessary.  If there are no loose-ends (eg a roundabout) then use the
		     first unprocessed seg in the set.  -->
		<xsl:variable name='fromLooseEnds' select='../seg[not(contains($processedSegs,concat("|",@id,"|")))][@osma:fromCount="0"]'/>
		<xsl:variable name='toLooseEnds' select='../seg[not(contains($processedSegs,concat("|",@id,"|")))][@osma:toCount="0"]'/>

		<xsl:choose>
			<!-- are there any "from" loose ends? -->
			<xsl:when test='$fromLooseEnds'>
				<xsl:for-each select='$fromLooseEnds[1]'>
					<xsl:call-template name='processSeg'>
						<xsl:with-param name='currentSeg' select='.'/>
						<xsl:with-param name='processedSegs' select='$processedSegs'/>
						<xsl:with-param name='subPath' select='true()'/>
						<xsl:with-param name='debug' select='"fromLooseEnd"'/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:when>
			<!-- are there any "to" loose ends? -->
			<xsl:when test='$toLooseEnds'>
				<xsl:for-each select='$toLooseEnds[1]'>
					<xsl:call-template name='processSeg'>
						<xsl:with-param name='currentSeg' select='.'/>
						<xsl:with-param name='processedSegs' select='$processedSegs'/>
						<xsl:with-param name='subPath' select='true()'/>
						<xsl:with-param name='reverse' select='true()'/>
						<xsl:with-param name='debug' select='"toLooseEnd"'/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<!-- otherwise use the first unprocessed seg in the set -->
				<xsl:for-each select='../seg[not(contains($processedSegs,concat("|",@id,"|")))][1]'>
					<xsl:call-template name='processSeg'>
						<xsl:with-param name='currentSeg' select='.'/>
						<xsl:with-param name='processedSegs' select='$processedSegs'/>
						<xsl:with-param name='subPath' select='true()'/>
						<xsl:with-param name='debug' select='"firstSegment"'/>
					</xsl:call-template>
				</xsl:for-each>									
			</xsl:otherwise>
		
		</xsl:choose>
	</xsl:template>    
     
    
	<!-- Process a seg.  First output the seg, then add it to the processed segs list and finally start searching
	     for the next seg that needs processing -->
   	<xsl:template name='processSeg'>
		<xsl:param name='currentSeg'/>
		<xsl:param name='reverse'/>
		<xsl:param name='subPath'/>
		<xsl:param name='processedSegs'/>
		<xsl:param name='debug'/>

		<!-- Output current seg -->
		<seg>
			<xsl:apply-templates select='$currentSeg/@*|$currentSeg'/>			
			<!--<xsl:attribute name='id'><xsl:value-of select='$currentSeg/@id'/></xsl:attribute>-->
			<xsl:if test='$reverse'>
				<xsl:attribute name='osma:reverse'>1</xsl:attribute>
			</xsl:if>
			<xsl:if test='$subPath'>
				<xsl:attribute name='osma:sub-path'>1</xsl:attribute>
			</xsl:if>
			<!-- <xsl:attribute name='osma:debug'><xsl:value-of select='$debug'/></xsl:attribute> -->
		</seg>

		<!-- Add current seg to the processed segs list -->
		<xsl:variable name='newProcessedSegs' select='concat($processedSegs,$currentSeg/@id,"|")'/>
	
		<xsl:choose>
			<xsl:when test='string-length(translate($processedSegs,"-0123456789",""))&lt;$maximumNumberOfSegs'>
				<!-- Start searching for next seg -->
				<xsl:variable name='nextSeg' select='$currentSeg/following-sibling::seg[1]'/>
				<xsl:variable name='alreadyProcessed' select='contains($newProcessedSegs,concat("|",$nextSeg/@id,"|"))'/>

				<xsl:variable name='currentSegToNodeId' select="key('segmentById',$currentSeg/@id)/@to" />
				<xsl:variable name='currentSegFromNodeId' select="key('segmentById',$currentSeg/@id)/@from" />
				<xsl:variable name='nextSegToNodeId' select="key('segmentById',$nextSeg/@id)/@to" />
				<xsl:variable name='nextSegFromNodeId' select="key('segmentById',$nextSeg/@id)/@from" />


				<!-- If there is another segment -->
				<xsl:choose>
					<xsl:when test='$nextSeg and not($alreadyProcessed)'>
						<xsl:choose>
							<xsl:when test='not($reverse)'>
								<xsl:choose>
									<xsl:when test='$currentSegToNodeId=$nextSegFromNodeId'>
										<xsl:call-template name='processSeg'>
											<xsl:with-param name='currentSeg' select='$nextSeg'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
											<xsl:with-param name='debug' select='"currentSegTo=nextSegFrom"'/>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test='$currentSegToNodeId=$nextSegToNodeId'>
										<xsl:call-template name='processSeg'>
											<xsl:with-param name='currentSeg' select='$nextSeg'/>
											<xsl:with-param name='reverse' select='true()'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
											<xsl:with-param name='debug' select='"currentSegTo=nextSegTo"'/>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise> <!-- not connected to the next seg -->
										<xsl:call-template name='linkedSeg'>
											<xsl:with-param name='currentSeg' select='$currentSeg'/>
											<xsl:with-param name='reverse' select='$reverse'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
										</xsl:call-template>							
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise> <!-- $reverse -->
								<xsl:choose>
									<xsl:when test='$currentSegFromNodeId=$nextSegFromNodeId'>
										<xsl:call-template name='processSeg'>
											<xsl:with-param name='currentSeg' select='$nextSeg'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
											<xsl:with-param name='debug' select='"currentSegFrom=nextSegFrom"'/>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test='$currentSegFromNodeId=$nextSegToNodeId'>
										<xsl:call-template name='processSeg'>
											<xsl:with-param name='currentSeg' select='$nextSeg'/>
											<xsl:with-param name='reverse' select='true()'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
											<xsl:with-param name='debug' select='"currentSegFrom=nextSegTo"'/>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise> <!-- not connected to the next seg -->
										<xsl:call-template name='linkedSeg'>
											<xsl:with-param name='currentSeg' select='$currentSeg'/>
											<xsl:with-param name='reverse' select='$reverse'/>
											<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
										</xsl:call-template>	
									</xsl:otherwise>
								</xsl:choose>				
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise> <!-- there's no next seg that has not already been processed -->
						<xsl:call-template name='linkedSeg'>
							<xsl:with-param name='currentSeg' select='$currentSeg'/>
							<xsl:with-param name='reverse' select='$reverse'/>
							<xsl:with-param name='processedSegs' select='$newProcessedSegs'/>
						</xsl:call-template>
					</xsl:otherwise>		
				</xsl:choose>			
			</xsl:when>
			<xsl:otherwise> <!-- If there are too many segments, output the remainder in original order -->
				<xsl:for-each select='../seg[not(contains($newProcessedSegs,concat("|",@id,"|")))]'>
					<seg>
						<xsl:apply-templates select='@*|node()'/>			
					</seg>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- Find any linked segment -->
	<!-- The next seg was not connected to the current seg, so now we need to look for any other seg that is connected to
	     the current seg.  If we can't find one then we'll just take the next loose-end.  -->
	<xsl:template name='linkedSeg'>
		<xsl:param name='currentSeg'/>
		<xsl:param name='reverse'/>
		<xsl:param name='processedSegs'/>

		<xsl:variable name='currentSegToNodeId' select="key('segmentById',$currentSeg/@id)/@to" />
		<xsl:variable name='currentSegFromNodeId' select="key('segmentById',$currentSeg/@id)/@from" />

		<!-- Find the any segment that matches now -->
		<xsl:variable name='unprocessedSegs' select='../seg[not($currentSeg/@id=@id)][not(contains($processedSegs,concat("|",@id,"|")))]'/>
		<xsl:variable name='linkedToFromSeg' select='$unprocessedSegs[$currentSegToNodeId=key("segmentById",@id)/@from]' />
		<xsl:variable name='linkedToToSeg' select='$unprocessedSegs[$currentSegToNodeId=key("segmentById",@id)/@to]' />
		<xsl:variable name='linkedFromFromSeg' select='$unprocessedSegs[$currentSegFromNodeId=key("segmentById",@id)/@from]' />
		<xsl:variable name='linkedFromToSeg' select='$unprocessedSegs[$currentSegFromNodeId=key("segmentById",@id)/@to]' />

		<xsl:choose>
			<xsl:when test='not($reverse)'>
				<xsl:choose>
					<xsl:when test='$linkedToFromSeg'>
						<xsl:call-template name='processSeg'>
							<xsl:with-param name='currentSeg' select='$linkedToFromSeg'/>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"linkedToFromSeg"'/>
						</xsl:call-template>	
					</xsl:when>
					<xsl:when test='$linkedToToSeg'>
						<xsl:call-template name='processSeg'>
							<xsl:with-param name='currentSeg' select='$linkedToToSeg'/>
							<xsl:with-param name='reverse' select='true()'/>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"linkedToToSeg"'/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise> <!-- not linked -->
						<xsl:call-template name='nextSeg'>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"nextSeg-ToToSeg"'/>
						</xsl:call-template>					
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise> <!-- $reverse -->
				<xsl:choose>
					<xsl:when test='$linkedFromToSeg'>
						<xsl:call-template name='processSeg'>
							<xsl:with-param name='currentSeg' select='$linkedFromToSeg'/>
							<xsl:with-param name='reverse' select='true()'/>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"linkedFromToSeg"'/>
						</xsl:call-template>	
					</xsl:when>
					<xsl:when test='$linkedFromFromSeg'>
						<xsl:call-template name='processSeg'>
							<xsl:with-param name='currentSeg' select='$linkedFromFromSeg'/>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"linkedFromFromSeg"'/>
						</xsl:call-template>	
					</xsl:when>
					<xsl:otherwise> <!-- not linked -->
						<xsl:call-template name='nextSeg'>
							<xsl:with-param name='processedSegs' select='$processedSegs'/>
							<xsl:with-param name='debug' select='"nextSeg-FromFromSeg"'/>
						</xsl:call-template>					
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:template>


	<!-- Suppress original seg elements -->
	<xsl:template match='seg' />

	<!-- Suppress created by tags -->
	<xsl:template match="tag[@k='created_by']" />

	<!-- Suppress timestamp attributes -->
	<xsl:template match="@timestamp" />

	<!-- Identity Transform -->
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
