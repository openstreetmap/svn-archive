<Style name="wifi-points">
    <!-- wifi points: ticket vending machines -->
    <Rule>
      <Filter>[amenity] = 'vending_machine' and [vending]='wifi_tickets'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi-vending.png" />
    </Rule>

    <!-- wifi nodes -->
    <Rule>
      <Filter>[cond]='none'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi_node_none.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='fee'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi_node_fee.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi_node_cust.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='free'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi_node_free.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='unknown'</Filter>
      &maxscale_zoom13;
      <PointSymbolizer file="&symbols;/wifi_node_unkn.png" />
    </Rule>

    <Rule>
      <Filter>[cond]='none'</Filter>
      &maxscale_zoom4;
      &minscale_zoom12;
      <PointSymbolizer file="&symbols;/wifi_node_6_none.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='fee'</Filter>
      &maxscale_zoom4;
      &minscale_zoom12;
      <PointSymbolizer file="&symbols;/wifi_node_6_fee.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom4;
      &minscale_zoom12;
      <PointSymbolizer file="&symbols;/wifi_node_6_cust.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='free'</Filter>
      &maxscale_zoom4;
      &minscale_zoom12;
      <PointSymbolizer file="&symbols;/wifi_node_6_free.png" />
    </Rule>
    <Rule>
      <Filter>[cond]='unknown'</Filter>
      &maxscale_zoom4;
      &minscale_zoom12;
      <PointSymbolizer file="&symbols;/wifi_node_6_unkn.png" />
    </Rule>
</Style>

<Layer name="wifi-points" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-points</StyleName>
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
       from &prefix;_point
       where 
          (tags ? 'wifi') OR (tags ? 'internet_access')
      ) as points
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
