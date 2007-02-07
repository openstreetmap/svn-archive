<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Rule processing engine -->

<!-- 

    Calls all templates inside <rule> tags (including itself, if there are nested rules).

    If the global var withOSMLayers is 'no', we don't care about layers and draw everything
    in one go. This is faster and is sometimes useful. For normal maps you want withOSMLayers
    to be 'yes', which is the default.

-->

<xsl:stylesheet 
    version="1.0"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:set="http://exslt.org/sets"
    extension-element-prefixes="set"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="processRules">
      
        <xsl:choose>

            <!-- Process all the rules, one layer at a time -->
            <xsl:when test="$withOSMLayers='yes'">
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"-5"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"-4"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"-3"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"-2"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"-1"' /></xsl:call-template>
                <xsl:call-template name="processLayer">
                    <xsl:with-param name='layer' select='"0"' />
                    <xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and count(tag[@k="layer"])=0 or tag[@k="layer" and @v="0"]]' />
                </xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"1"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"2"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"3"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"4"' /></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name='layer' select='"5"' /></xsl:call-template>
            </xsl:when>

            <!-- Process all the rules, without looking at the layers -->
            <xsl:otherwise>
                <xsl:apply-templates select='/rules/rule'>
                    <xsl:with-param name='elements' select='$data/osm/*[not(@action="delete")]' />
                    <xsl:with-param name='layer' select='"0"' />
                    <xsl:with-param name='classes' select="''" />
                </xsl:apply-templates>
            </xsl:otherwise>

        </xsl:choose>
	</xsl:template>

    <xsl:template name='processLayer'>
        <xsl:param name="layer" />
        <xsl:param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v=$layer]]' />

        <g inkscape:groupmode="layer" id="layer{$layer}" inkscape:label="Layer {$layer}">
            <xsl:apply-templates select='/rules/rule'>
                <xsl:with-param name='elements' select='$elements' />
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='classes' select="''" />
            </xsl:apply-templates>
        </g>
    </xsl:template>
            
    <xsl:template match='rule'>
        <xsl:param name='elements' />
        <xsl:param name='layer' />
        <xsl:param name='classes' />

        <!-- This is the rule currently being processed -->
        <xsl:variable name='rule' select='.'/>

        <!-- Make list of elements that this rule should be applied to -->
        <xsl:variable name='eBare'>
            <xsl:choose>
                <xsl:when test='$rule/@e="*"'>node|segment|way</xsl:when>
                <xsl:when test='$rule/@e'><xsl:value-of select='$rule/@e'/></xsl:when>
                <xsl:otherwise>node|segment|way</xsl:otherwise>
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
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$elementsWithNoTags'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test='contains($v,"|*|")'>
                        <xsl:variable name='allElements' select='$selectedElements'/>
                        <xsl:call-template name='processElements'>
                            <xsl:with-param name='eBare' select='$eBare'/>
                            <xsl:with-param name='kBare' select='$kBare'/>
                            <xsl:with-param name='vBare' select='$vBare'/>
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$allElements'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name='allElementsWithValue' select='$selectedElements[tag[contains($v,concat("|",@v,"|"))]]'/>
                        <xsl:call-template name='processElements'>
                            <xsl:with-param name='eBare' select='$eBare'/>
                            <xsl:with-param name='kBare' select='$kBare'/>
                            <xsl:with-param name='vBare' select='$vBare'/>
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$allElementsWithValue'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
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
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$elementsWithoutKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test='contains($v,"|*|")'>
                <xsl:variable name='allElementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|"))]]'/>
                <xsl:call-template name='processElements'>
                    <xsl:with-param name='eBare' select='$eBare'/>
                    <xsl:with-param name='kBare' select='$kBare'/>
                    <xsl:with-param name='vBare' select='$vBare'/>
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$allElementsWithKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name='elementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|")) and contains($v,concat("|",@v,"|"))]]'/>
                <xsl:call-template name='processElements'>
                    <xsl:with-param name='eBare' select='$eBare'/>
                    <xsl:with-param name='kBare' select='$kBare'/>
                    <xsl:with-param name='vBare' select='$vBare'/>
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$elementsWithKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Process a set of elements selected by a rule at a specific layer -->
    <xsl:template name='processElements'>
        <xsl:param name='eBare'/>
        <xsl:param name='kBare'/>
        <xsl:param name='vBare'/>
        <xsl:param name='layer'/>
        <xsl:param name='elements'/>
        <xsl:param name='rule'/>
        <xsl:param name='classes'/>
        
        <xsl:if test='$elements'>
            <xsl:message>
Processing &lt;rule e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt; 
Matched by <xsl:value-of select='count($elements)'/> elements for layer <xsl:value-of select='$layer'/>.
            </xsl:message>

            <xsl:apply-templates select='*'>
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='elements' select='$elements' />
                <xsl:with-param name='rule' select='$rule'/>
                <xsl:with-param name='classes' select='$classes'/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>


    <xsl:template match='addclass'>
        <xsl:param name='elements' />
        <xsl:param name='layer' />
        <xsl:param name='classes' />

        <!-- This is the rule currently being processed -->
        <xsl:variable name='rule' select='.'/>

        <!-- Additional classes from class attribute of this rule -->
        <xsl:variable name='addclasses' select="@class"/>

        <!-- Make list of elements that this rule should be applied to -->
        <xsl:variable name='eBare'>
            <xsl:choose>
                <xsl:when test='$rule/@e="*"'>node|segment|way</xsl:when>
                <xsl:when test='$rule/@e'><xsl:value-of select='$rule/@e'/></xsl:when>
                <xsl:otherwise>node|segment|way</xsl:otherwise>
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
                        <xsl:call-template name='processElementsForAddClass'>
                            <xsl:with-param name='eBare' select='$eBare'/>
                            <xsl:with-param name='kBare' select='$kBare'/>
                            <xsl:with-param name='vBare' select='$vBare'/>
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$elementsWithNoTags'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
                            <xsl:with-param name='addclasses' select='$addclasses'/>
                            <xsl:with-param name='allelements' select='$elements'/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test='contains($v,"|*|")'>
                        <xsl:variable name='allElements' select='$selectedElements'/>
                        <xsl:call-template name='processElementsForAddClass'>
                            <xsl:with-param name='eBare' select='$eBare'/>
                            <xsl:with-param name='kBare' select='$kBare'/>
                            <xsl:with-param name='vBare' select='$vBare'/>
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$allElements'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
                            <xsl:with-param name='addclasses' select='$addclasses'/>
                            <xsl:with-param name='allelements' select='$elements'/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name='allElementsWithValue' select='$selectedElements[tag[contains($v,concat("|",@v,"|"))]]'/>
                        <xsl:call-template name='processElementsForAddClass'>
                            <xsl:with-param name='eBare' select='$eBare'/>
                            <xsl:with-param name='kBare' select='$kBare'/>
                            <xsl:with-param name='vBare' select='$vBare'/>
                            <xsl:with-param name='layer' select='$layer'/>
                            <xsl:with-param name='elements' select='$allElementsWithValue'/>
                            <xsl:with-param name='rule' select='$rule'/>
                            <xsl:with-param name='classes' select='$classes'/>
                            <xsl:with-param name='addclasses' select='$addclasses'/>
                            <xsl:with-param name='allelements' select='$elements'/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test='contains($v,"|~|")'>
                <xsl:variable name='elementsWithoutKey' select='$selectedElements[count(tag[contains($k,concat("|",@k,"|"))])=0]'/>
                <xsl:call-template name='processElementsForAddClass'>
                    <xsl:with-param name='eBare' select='$eBare'/>
                    <xsl:with-param name='kBare' select='$kBare'/>
                    <xsl:with-param name='vBare' select='$vBare'/>
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$elementsWithoutKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                    <xsl:with-param name='addclasses' select='$addclasses'/>
                    <xsl:with-param name='allelements' select='$elements'/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test='contains($v,"|*|")'>
                <xsl:variable name='allElementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|"))]]'/>
                <xsl:call-template name='processElementsForAddClass'>
                    <xsl:with-param name='eBare' select='$eBare'/>
                    <xsl:with-param name='kBare' select='$kBare'/>
                    <xsl:with-param name='vBare' select='$vBare'/>
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$allElementsWithKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                    <xsl:with-param name='addclasses' select='$addclasses'/>
                    <xsl:with-param name='allelements' select='$elements'/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name='elementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|")) and contains($v,concat("|",@v,"|"))]]'/>
                <xsl:call-template name='processElementsForAddClass'>
                    <xsl:with-param name='eBare' select='$eBare'/>
                    <xsl:with-param name='kBare' select='$kBare'/>
                    <xsl:with-param name='vBare' select='$vBare'/>
                    <xsl:with-param name='layer' select='$layer'/>
                    <xsl:with-param name='elements' select='$elementsWithKey'/>
                    <xsl:with-param name='rule' select='$rule'/>
                    <xsl:with-param name='classes' select='$classes'/>
                    <xsl:with-param name='addclasses' select='$addclasses'/>
                    <xsl:with-param name='allelements' select='$elements'/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- Process a set of elements selected by a rule at a specific layer -->
    <xsl:template name='processElementsForAddClass'>
        <xsl:param name='eBare'/>
        <xsl:param name='kBare'/>
        <xsl:param name='vBare'/>
        <xsl:param name='layer'/>
        <xsl:param name='elements'/>
        <xsl:param name='allelements'/>
        <xsl:param name='rule'/>
        <xsl:param name='classes'/>
        <xsl:param name='addclasses'/>
        
        <xsl:variable name='newclasses' select="concat($classes,' ',$addclasses)"/>
        <xsl:variable name='otherelements' select="set:difference($allelements, $elements)"/>

        <xsl:if test='$elements'>
            <xsl:message>
Processing &lt;addclass e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt; 
Positive match by <xsl:value-of select='count($elements)'/> elements for layer <xsl:value-of select='$layer'/>.
            </xsl:message>

            <xsl:apply-templates select='*'>
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='elements' select='$elements' />
                <xsl:with-param name='rule' select='$rule'/>
                <xsl:with-param name='classes' select='$newclasses'/>
            </xsl:apply-templates>
        </xsl:if>

        <xsl:if test='$otherelements'>
            <xsl:message>
Processing &lt;addclass e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt; 
Negative match by <xsl:value-of select='count($otherelements)'/> elements for layer <xsl:value-of select='$layer'/>.
            </xsl:message>
            <xsl:apply-templates select='*'>
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='elements' select='$otherelements' />
                <xsl:with-param name='rule' select='$rule'/>
                <xsl:with-param name='classes' select='$classes'/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
