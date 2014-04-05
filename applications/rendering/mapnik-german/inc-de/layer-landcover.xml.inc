<Style name="landcover">
     <Rule>
      <Filter>[leisure] = 'swimming_pool'</Filter>
      &maxscale_zoom14;
      <PolygonSymbolizer fill="#b5d0d0"/>
      <LineSymbolizer stroke="blue" stroke-width="0.5"/>
    </Rule>
    <Rule>
      <Filter>[leisure] = 'playground'</Filter>
      &maxscale_zoom13;
      <PolygonSymbolizer fill="#ccfff1"/>
      <LineSymbolizer stroke="#666" stroke-width="0.3"/>
    </Rule>
    <Rule>
      <Filter>[tourism] = 'camp_site' or [tourism]='caravan_site' or [tourism] = 'picnic_site'</Filter>
      &maxscale_zoom13;
      <PolygonSymbolizer fill-opacity="0.5" fill="#ccff99"/>
      <LineSymbolizer stroke="#666" stroke-width="0.3"/>
    </Rule>        
    <Rule>
      <Filter>[tourism] = 'attraction'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#f2caea"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'quarry'</Filter>
      &maxscale_zoom11;
      <PolygonPatternSymbolizer file="&symbols;/quarry2.png" />
      <LineSymbolizer stroke="grey" stroke-width="0.5"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'vineyard'</Filter>
      &maxscale_zoom11;
      &minscale_zoom12;
      <PolygonSymbolizer fill="#abdf96"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'orchard'</Filter>
      &maxscale_zoom10;
      <PolygonPatternSymbolizer file="&symbols;/orchard.png" />
    </Rule>
    <Rule>
      <Filter>[landuse] = 'vineyard'</Filter>
      &maxscale_zoom13;
      <PolygonPatternSymbolizer file="&symbols;/vineyard.png" />
    </Rule>
    <Rule>
      &maxscale_zoom11;
      &minscale_zoom14;
      <Filter>[landuse] = 'cemetery' or [landuse] = 'grave_yard' or [amenity]='grave_yard'</Filter>
      <PolygonSymbolizer fill="#aacbaf"/>
    </Rule>
    <Rule>
      &maxscale_zoom11;
      &minscale_zoom14;
      <Filter>[landuse] = 'residential'</Filter>
      <PolygonSymbolizer fill="#e9e9e9"/>
    </Rule>
    <Rule>
      &maxscale_zoom15;
      &minscale_zoom18;
      <Filter>[landuse] = 'residential'</Filter>
      <PolygonSymbolizer fill="#ddd"/>
    </Rule>
    <Rule>
      &maxscale_zoom12;
      &minscale_zoom18;
      <Filter>[landuse] = 'garages'</Filter>
      <PolygonSymbolizer fill-opacity="0.2" fill="#996"/>
    </Rule>
    <Rule>
      &maxscale_zoom11;
      &minscale_zoom18;
      <Filter>[military] = 'barracks'</Filter>
      <PolygonSymbolizer fill="#ffb5b5"/>
    </Rule>
    <Rule>
      &maxscale_zoom10;
      &minscale_zoom13;
      <Filter>[landuse]='field' or [natural]='field'</Filter>
      <PolygonSymbolizer fill-opacity="0.2" fill="#666600"/>
    </Rule>
    <Rule>
      &maxscale_zoom14;
      &minscale_zoom18;
      <Filter>[landuse]='field' or [natural]='field'</Filter>
      <PolygonSymbolizer fill-opacity="0.2" fill="#666600"/>
      <LineSymbolizer stroke="#660" stroke-width="0.3" stroke-opacity="0.4"/>
    </Rule>
    <Rule>
      &maxscale_zoom10;
      &minscale_zoom10;
      <Filter>[military] = 'danger_area'</Filter>
      <PolygonSymbolizer fill-opacity="0.3" fill="pink"/>
    </Rule>
    <Rule>
      &maxscale_zoom11;
      &minscale_zoom18;
      <Filter>[military] = 'danger_area'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/danger.png" />
    </Rule>
    <Rule>
      &maxscale_zoom14;
      <Filter>([landuse] = 'cemetery' or [landuse] = 'grave_yard' or [amenity]='grave_yard') and [religion] = 'jewish'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/cemetery_jewish.18.png" />
    </Rule>
    <Rule>
      &maxscale_zoom14;
      <Filter>([landuse] = 'cemetery' or [landuse] = 'grave_yard' or [amenity]='grave_yard') and [religion] = 'christian'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/grave_yard.png" />
    </Rule>
    <Rule>
      &maxscale_zoom14;
      <Filter>([landuse] = 'cemetery' or [landuse] = 'grave_yard' or [amenity]='grave_yard') and [religion] = 'INT-generic'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/grave_yard_generic.png" />
    </Rule>
    <Rule>
      &maxscale_zoom10;
      <Filter>[landuse] = 'meadow' or [landuse] = 'grass'</Filter>
      <PolygonSymbolizer fill="#cfeca8"/>
    </Rule>
    <Rule>
      <Filter>[leisure] = 'park' or [leisure] = 'recreation_ground'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill-opacity="0.6" fill="#b6fdb6"/>
    </Rule>
    <Rule>
      <Filter>[tourism] = 'zoo'</Filter>
      &maxscale_zoom10;
      <PolygonPatternSymbolizer file="&symbols;/zoo.png" />
    </Rule>
    <Rule>
      <Filter>[leisure] = 'common'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#cfeca8"/>
    </Rule>
    <Rule>
      <Filter>[leisure] = 'garden'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#cfeca8"/>
    </Rule>
    <Rule>
      <Filter>[leisure] = 'golf_course'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#b5e3b5"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'allotments'</Filter>
      &maxscale_zoom11;
      &minscale_zoom13;
      <PolygonSymbolizer fill="#ead8bd"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'allotments'</Filter>
      &maxscale_zoom14;
      <PolygonPatternSymbolizer file="&symbols;/allotments.png" />
    </Rule>
    <Rule>
      <Filter>[landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood'</Filter>
      &maxscale_zoom7;
      &minscale_zoom7;
      <PolygonSymbolizer fill="#c6ddb5" fill-opacity="0.6"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood'</Filter>
      &maxscale_zoom8;
      &minscale_zoom9;
      <PolygonSymbolizer fill="#8dc56c" fill-opacity="0.4"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood'</Filter>
      &maxscale_zoom10;
      &minscale_zoom10;
      <PolygonSymbolizer fill="#8dc56c" fill-opacity="0.5"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood'</Filter>
      &maxscale_zoom11;
      &minscale_zoom12;
      <PolygonSymbolizer fill="#8dc56c" fill-opacity="0.6"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood'</Filter>
      &maxscale_zoom13;
      &minscale_zoom13;
      <PolygonSymbolizer fill="#8dc56c" fill-opacity="0.9"/>
    </Rule>
    <Rule>
      <Filter>([landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood') and [wood] = 'deciduous'</Filter>
      &maxscale_zoom14;
      <PolygonPatternSymbolizer file="&symbols;/wood_deciduous.png"/>
    </Rule>
    <Rule>
      <Filter>([landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood') and [wood] = 'mixed'</Filter>
      &maxscale_zoom14;
      <PolygonPatternSymbolizer file="&symbols;/wood_mixed.png"/>
    </Rule>
    <Rule>
      <Filter>([landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood') and [wood] = 'coniferous'</Filter>
      &maxscale_zoom14;
      <PolygonPatternSymbolizer file="&symbols;/wood_coniferous.png"/>
    </Rule>
    <Rule>
         <Filter>([landuse] = 'forest' or [landuse] = 'wood' or [natural]='wood') and not ([wood] = 'deciduous' or [wood] = 'mixed' or [wood] = 'coniferous')</Filter>
     &maxscale_zoom14;
     <PolygonSymbolizer fill="#8dc56c" fill-opacity="1"/>
    </Rule>
    <Rule>
      <Filter>[landuse]='farmyard'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#ead8bd" fill-opacity="0.9"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'farm' or [landuse]='farmland'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="#f2e9d7" fill-opacity="0.7"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'recreation_ground' or [landuse] = 'conservation'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#cfeca8"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'village_green'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#cfeca8"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'retail'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="#f1dada"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'retail'</Filter>
      &maxscale_zoom15;
      <LineSymbolizer stroke="red" stroke-width="0.3"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'industrial' or [landuse]='railway'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="#dfd1d6"/>
    </Rule>
    <Rule>
      <Filter>[power] = 'station' or [power]='generator'</Filter>
      &maxscale_zoom11;
      &minscale_zoom11;
      <PolygonSymbolizer fill="#bbb"/>
    </Rule>
    <Rule>
      <Filter>[power] = 'station' or [power]='generator'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#bbb"/>
      <LineSymbolizer stroke="#555" stroke-width="0.4"/>
    </Rule>
    <Rule>
      <Filter>[power] = 'sub_station'</Filter>
      &maxscale_zoom13;
      <PolygonSymbolizer fill="#bbb"/>
      <LineSymbolizer stroke="#555" stroke-width="0.4"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'commercial'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#efc8c8"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'brownfield' or [landuse]='landfill' or [landuse] = 'greenfield' or [landuse]='construction'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill-opacity="0.7" fill="#9d9d6c"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'desert'</Filter>
      &maxscale_zoom8;
      <PolygonSymbolizer fill="#e3b57a"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'sand'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="#ffdf88"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'heath'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#d6d99f"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'grassland'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="#c6e4b4"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'scrub'</Filter>
      &maxscale_zoom10;
      &minscale_zoom13;
      <PolygonSymbolizer fill="#b5e3b5"/>
    </Rule>
    <Rule>
      &maxscale_zoom14;
      <Filter>[natural] = 'scrub'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/scrub.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'university' or [amenity] = 'college' or [amenity] = 'school' or [amenity]='hospital' or [amenity]='kindergarten'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#f0f0d8"/>
    </Rule>
    <Rule>
      <Filter>[amenity] = 'university' or [amenity] = 'college' or [amenity] = 'school' or [amenity]='hospital' or [amenity]='kindergarten'</Filter>
      &maxscale_zoom12;
      <LineSymbolizer stroke="brown" stroke-width="0.3"/>
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking'</Filter>
      &maxscale_zoom11;
      <PolygonSymbolizer fill="#f7efb7"/>
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking'</Filter>
      &maxscale_zoom15;
      <LineSymbolizer stroke="#eeeed1" stroke-width="0.3"/>
    </Rule>
    <Rule>
    <Filter>[amenity] = 'prison'</Filter>
      &maxscale_zoom14;
      <PolygonPatternSymbolizer file="&symbols;/amenity_prison.png" />
      <LineSymbolizer stroke="#555" stroke-width="0.3"/>
    </Rule>
    <Rule>
      <Filter>[aeroway] = 'apron'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#e9d1ff"/>
    </Rule>
    <Rule>
      <Filter>[aeroway] = 'aerodrome'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill-opacity="0.2" fill="#ccc"/>
      <LineSymbolizer stroke="#555" stroke-width="0.2"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'beach'</Filter>
      &maxscale_zoom13;
      <PolygonPatternSymbolizer file="&symbols;/beach.png" />
    </Rule>
    <Rule>
      <Filter>[highway] = 'services' or [highway] = 'rest_area'</Filter>
      &maxscale_zoom14;
      <PolygonSymbolizer fill="#efc8c8"/>
    </Rule>
</Style>
<Style name="landcover_line">
    <Rule>
      &maxscale_zoom14;
      &minscale_zoom15;
      <LineSymbolizer stroke-linejoin="round" stroke="#f2efe9" stroke-width="3" stroke-linecap="square"/>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <LineSymbolizer stroke-linejoin="round" stroke="#f2efe9" stroke-width="6" stroke-linecap="square"/>
    </Rule>
</Style>
<Style name="sports_grounds">
    <Rule>
      <Filter>[leisure] = 'sports_centre' or [leisure] = 'stadium'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#33cc99"/>
    </Rule>
    <Rule>
      <Filter>[leisure]='track'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#74dcba"/>
      <LineSymbolizer stroke="#888" stroke-width="0.5"/>
    </Rule>
    <Rule>
      <Filter>[leisure] = 'pitch'</Filter>
      &maxscale_zoom12;
      <PolygonSymbolizer fill="#8ad3af"/>
      <LineSymbolizer stroke="#888" stroke-width="0.5"/>
    </Rule>
</Style>
<Style name="sports" filter-mode="first">
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(2.4)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2.4)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d12] &gt; 50) and ([d12] &lt; 100) and ([d23] &gt; 120) and ([d23] &lt; 200)</Filter>
    <PointSymbolizer file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(2.4)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2.4)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([d23] &gt; 50) and ([d23] &lt; 100) and ([d12] &gt; 120) and ([d12] &lt; 200)</Filter>
    <PointSymbolizer file="symbols/sports/sports-foot-us.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d12] &gt; 75) and ([d12] &lt; 150) and ([d23] &gt; 150) and ([d23] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(4)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.5)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.25)" file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([d23] &gt; 75) and ([d23] &lt; 150) and ([d12] &gt; 150) and ([d12] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-rugby.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d23] &gt; 30) and ([d23] &lt; 40) and ([d12] &gt; 15) and ([d12] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d23] &gt; 30) and ([d23] &lt; 40) and ([d12] &gt; 15) and ([d12] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d23] &gt; 30) and ([d23] &lt; 40) and ([d12] &gt; 15) and ([d12] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d23] &gt; 30) and ([d23] &lt; 40) and ([d12] &gt; 15) and ([d12] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.075)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d23] &gt; 30) and ([d23] &lt; 40) and ([d12] &gt; 15) and ([d12] &lt; 25)</Filter>
    <PointSymbolizer file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d12] &gt; 30) and ([d12] &lt; 40) and ([d23] &gt; 15) and ([d23] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d12] &gt; 30) and ([d12] &lt; 40) and ([d23] &gt; 15) and ([d23] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d12] &gt; 30) and ([d12] &lt; 40) and ([d23] &gt; 15) and ([d23] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d12] &gt; 30) and ([d12] &lt; 40) and ([d23] &gt; 15) and ([d23] &lt; 25)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.075)" file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'basketball') and ([way_area] &lt; 1000) and ([d13] &gt; 30) and ([d13] &lt; 50) and ([d12] &gt; 30) and ([d12] &lt; 40) and ([d23] &gt; 15) and ([d23] &lt; 25)</Filter>
    <PointSymbolizer file="symbols/sports/sports-basketball.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 12000) and ([way_area] &lt; 17000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(2.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.7)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.35)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.175)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.7)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.35)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.175)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.9)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.45)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.225)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.1125)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.9)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.45)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.225)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.1125)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d12] &gt; 130) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000) and ([d23] &lt; 160)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(2.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.4)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.7)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.35)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.175)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &gt;= 17000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.8)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.9)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.45)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.225)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.1125)" file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([way_area] &lt; 12000)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d23] &lt; 70) and ([d12] &gt; 20) and ([d12] &lt; 40)</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]+90) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d23] &gt; 38) and ([d12] &lt; 40) and ([d12] &gt; 38) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40) and ([access] = 'private')</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom20;
    &minscale_zoom20;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(1.2)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom19;
    &minscale_zoom19;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.6)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom18;
    &minscale_zoom18;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.3)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    &minscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.15)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    &minscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer transform="rotate([angle]) scale(0.075)" file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom21;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([d12] &gt; 38) and ([d12] &lt; 70) and ([d23] &gt; 20) and ([d23] &lt; 40)</Filter>
    <PointSymbolizer file="symbols/sports/sports-tennis.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d23] &gt; 130) and ([d23] &lt; 200) and ([d12] &gt; 68) and ([d12] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([d12] &gt; 130) and ([d12] &lt; 200) and ([d23] &gt; 68) and ([d23] &lt; 160) and ([d13] &gt; 150) and ([d13] &lt; 250)</Filter>
    <PointSymbolizer file="symbols/sports/sports-soccer.svg" ignore-placement="true" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([way_area] &gt; 8000) and ([way_area] &lt; 20000) and ([d13] &gt; 120) and ([d13] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([way_area] &gt; 15000) and ([way_area] &lt; 25000) and ([d13] &gt; 150) and ([d13] &lt; 250) and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([way_area] &lt; 2500) and ([d13] &gt; 37) and ([d13] &lt; 80) and ([access] = 'private')</Filter>
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'american_football') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_league') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'rugby_union') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom17;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([access] = 'private')</Filter>
    <LineSymbolizer stroke-width="4" stroke="#efa9a9" stroke-dasharray="6, 8" stroke-opacity="0.5" stroke-linejoin="round" stroke-linecap="round" />
  </Rule>
</Style>
<Style name="sports-surface" filter-mode="first">
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'soccer') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([surface] = 'grass')</Filter>
    <PolygonSymbolizer fill="#54a854" />
  </Rule>
  <Rule>
    &maxscale_zoom16;
    <Filter>([angle_diff] &gt; 85) and ([angle_diff] &lt; 95) and ([sport] = 'tennis') and ([surface] = 'clay')</Filter>
    <PolygonSymbolizer fill="#cc7e66" />
  </Rule>
</Style>
<Layer name="landcover" status="on" srs="&osm2pgsql_projection;">
    <StyleName>landcover</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,aeroway,amenity,landuse,leisure,man_made,military,"natural",wood,power,tourism,&get_name;,highway,
       case when religion in ('christian','jewish') then religion else 'INT-generic'::text end as religion
       from &prefix;_polygon
       where landuse is not null
          or leisure is not null
          or aeroway in ('apron','aerodrome')
          or amenity in ('parking','university','college','school','hospital','kindergarten','grave_yard','prison')
          or military in ('barracks','danger_area')
          or "natural" in ('field','beach','desert','heath','mud','grassland','wood','sand','scrub')
          or power in ('station','sub_station','generator')
          or tourism in ('attraction','camp_site','caravan_site','picnic_site','zoo')
          or highway in ('services','rest_area')
       order by z_order,way_area desc
      ) as leisure
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
<Layer name="landcover_line" status="on" srs="&osm2pgsql_projection;">
    <StyleName>landcover_line</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way
       from &prefix;_line
       where man_made='cutline'
      ) as leisure
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
<Layer name="sports_grounds" status="on" srs="&osm2pgsql_projection;">
    <StyleName>sports_grounds</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,leisure,
       case when leisure='pitch' then 2
            when leisure='track' then 1
            else 0 end as prio
       from &prefix;_polygon
       where leisure in ('sports_centre','stadium','pitch','track')
       order by z_order,prio,way_area desc
      ) as sports_grounds
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
<Layer name="sports" srs="&osm2pgsql_projection;">
    <StyleName>sports-surface</StyleName>
    <StyleName>sports</StyleName>
    <Datasource>
      <Parameter name="table">
      (select *, abs(a12-a23) as angle_diff, (a12+a23+90)/2 as angle
      from (select *, st_npoints(way2) as nb,
                   ST_Distance(st_pointn(way2,1),st_pointn(way2,2)) as d12, ST_Distance(st_pointn(way2,3),st_pointn(way2,2)) as d23,
                   ST_Distance(st_pointn(way2,1),st_pointn(way2,3)) as d13, degrees(st_azimuth(st_pointn(way2,1),st_pointn(way2,2))) as a12,
                   degrees(st_azimuth(st_pointn(way2,2),st_pointn(way2,3))) as a23 from (select *, st_area(way) as way_area,
                   ST_ExteriorRing(ST_SimplifyPreserveTopology(way,100)) as way2 from (select (st_dump(way)).geom as way, sport, surface, access
            from &prefix;_polygon
            <![CDATA[where sport in ('tennis','soccer','basketball','rugby','rugby_union','rugby_league','american_football') AND way && !bbox!) as dump) as simplified) as simplified2]]>
      ) as sports
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
