<Style name="wifi-area">

    <!-- wifi area: none -->
    <Rule>
      <Filter>[cond] = 'none'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="&wa_none;" fill-opacity="&wa_fill_opacity;"/>
      <LineSymbolizer stroke="&wa_none_stroke;" stroke-width="&wa_stroke_width;" stroke-opacity="&wa_stroke_opacity;"/>
    </Rule>

    <!-- wifi area: fee -->
    <Rule>
      <Filter>[cond] = 'fee'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="&wa_fee;" fill-opacity="&wa_fill_opacity;"/>
      <LineSymbolizer stroke="&wa_fee_stroke;" stroke-width="&wa_stroke_width;" stroke-opacity="&wa_stroke_opacity;"/>
    </Rule>

    <!-- wifi area: customers -->
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="&wa_customers;" fill-opacity="&wa_fill_opacity;"/>
      <LineSymbolizer stroke="&wa_customers_stroke;" stroke-width="&wa_stroke_width;" stroke-opacity="&wa_stroke_opacity;"/>
    </Rule>

    <!-- wifi area: free -->
    <Rule>
      <Filter>[cond] = 'free'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="&wa_free;" fill-opacity="&wa_fill_opacity;"/>
      <LineSymbolizer stroke="&wa_free_stroke;" stroke-width="&wa_stroke_width;" stroke-opacity="&wa_stroke_opacity;"/>
    </Rule>

    <Rule>
      <ElseFilter/>
      &maxscale_zoom10;
      <PolygonSymbolizer fill="&wa_unknown;" fill-opacity="&wa_fill_opacity;"/>
      <LineSymbolizer stroke="&wa_unknown_stroke;" stroke-width="&wa_stroke_width;" stroke-opacity="&wa_stroke_opacity;"/>
    </Rule>
</Style>

<Layer name="wifi-area" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,amenity,name,(tags->'vending') as "vending",(tags->'fee') as "fee",
        CASE /* wifi-internetaccess= */ LOWER(SUBSTRING(COALESCE(tags->'wifi',tags->'wlan','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access:fee','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access:access','--'),1,2))
        WHEN 'ye------' THEN 'unknown'
        WHEN 'no------' THEN 'none'
        WHEN 'fe------' THEN 'fee'
        WHEN 'pa------' THEN 'fee'
        WHEN 'fr------' THEN 'free'
        WHEN 'cu------' THEN 'customers'
        WHEN '--ye----' THEN 'unknown' /* yes */
        WHEN '--yeno--' THEN 'unknown' /* no fee can be either public or customers */
        WHEN '--yenopu' THEN 'free' /* no fee for public */
        WHEN '--yenocu' THEN 'customers' /* no fee only for customers */
        WHEN '--yeye--' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--yeyepu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--yeyecu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wl----' THEN 'unknown' /* wlan */
        WHEN '--wlno--' THEN 'unknown' /* no fee can be either public or customers */
        WHEN '--wlnopu' THEN 'free' /* no fee for public */
        WHEN '--wlnocu' THEN 'customers' /* no fee only for customers */
        WHEN '--wlye--' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wlyepu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wlyecu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--no----' THEN 'none'
        WHEN '--wi----' THEN 'none' /* wired */
        WHEN '--te----' THEN 'none' /* terminal */
        WHEN '--pu----' THEN 'free' /* public (assuming public wlan) */
        WHEN '--puno--' THEN 'free' /* public (assuming public wlan) */
        WHEN '--punopu' THEN 'free' /* public (assuming public wlan) */
	    ELSE 'unknown'
        END AS "cond"
       from &prefix;_polygon
       where 
          (tags ? 'wifi') OR (tags ? 'internet_access')
      ) as wifi
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="wifi-area-top" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,amenity,name,(tags->'vending') as "vending",(tags->'fee') as "fee",
        CASE /* wifi-internetaccess= */ LOWER(SUBSTRING(COALESCE(tags->'wifi',tags->'wlan','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access:fee','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access:access','--'),1,2))
        WHEN 'ye------' THEN 'unknown'
        WHEN 'no------' THEN 'none'
        WHEN 'fe------' THEN 'fee'
        WHEN 'pa------' THEN 'fee'
        WHEN 'fr------' THEN 'free'
        WHEN 'cu------' THEN 'customers'
        WHEN '--ye----' THEN 'unknown' /* yes */
        WHEN '--yeno--' THEN 'unknown' /* no fee can be either public or customers */
        WHEN '--yenopu' THEN 'free' /* no fee for public */
        WHEN '--yenocu' THEN 'customers' /* no fee only for customers */
        WHEN '--yeye--' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--yeyepu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--yeyecu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wl----' THEN 'unknown' /* wlan */
        WHEN '--wlno--' THEN 'unknown' /* no fee can be either public or customers */
        WHEN '--wlnopu' THEN 'free' /* no fee for public */
        WHEN '--wlnocu' THEN 'customers' /* no fee only for customers */
        WHEN '--wlye--' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wlyepu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--wlyecu' THEN 'fee' /* fee. doesnt matter whether public or customers */
        WHEN '--no----' THEN 'none'
        WHEN '--wi----' THEN 'none' /* wired */
        WHEN '--te----' THEN 'none' /* terminal */
        WHEN '--pu----' THEN 'free' /* public (assuming public wlan) */
        WHEN '--puno--' THEN 'free' /* public (assuming public wlan) */
        WHEN '--punopu' THEN 'free' /* public (assuming public wlan) */
	    ELSE 'unknown'
        END AS "cond"
       from &prefix;_polygon
       where 
          (tags ? 'wifi') OR (tags ? 'internet_access')
      ) as wifi
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>







<!-- 
<Layer name="wifi-area" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,(tags->'amenity') AS "amenity",(tags->'tourism') AS "tourism",(tags->'name') AS "name",(tags->'fee') AS "fee",(tags->'access') AS "access",(tags->'parking:condition:area') AS "parking:condition:area",
        CASE LOWER(tags->'wifi')
        WHEN 'yes' THEN 'unknown'
        WHEN 'no' THEN 'none'
        WHEN 'fee' THEN 'fee'
        WHEN 'pay' THEN 'fee'
        WHEN 'paid' THEN 'fee'
        WHEN 'customers' THEN 'customers'
	    ELSE 'unknown'
        END AS "cond"
      from &prefix;_polygon
      where 
          (tags ? 'wifi')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="wifi-area-top" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,amenity,tourism,name,(tags->'fee') AS "fee",
        CASE LOWER(tags->'wifi')
        WHEN 'yes' THEN 'unknown'
        WHEN 'no' THEN 'none'
        WHEN 'fee' THEN 'fee'
        WHEN 'pay' THEN 'fee'
        WHEN 'paid' THEN 'fee'
        WHEN 'customers' THEN 'customers'
	    ELSE 'unknown'
        END AS "cond"
      from &prefix;_polygon
      where 
          (tags ? 'wifi')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
-->

