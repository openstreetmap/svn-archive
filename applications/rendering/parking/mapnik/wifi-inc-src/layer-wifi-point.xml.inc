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
</Style>

<Layer name="wifi-points" status="on" srs="&osm2pgsql_projection;">
    <StyleName>wifi-points</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,amenity,name,(tags->'vending') as "vending",(tags->'fee') as "fee",
        CASE /* wifi-internetaccess= */ LOWER(SUBSTRING(COALESCE(tags->'wifi',tags->'wlan','--'),1,2) || SUBSTRING(COALESCE(tags->'internet_access','--'),1,2))
        WHEN 'ye--' THEN 'unknown'
        WHEN 'no--' THEN 'none'
        WHEN 'fe--' THEN 'fee'
        WHEN 'pa--' THEN 'fee'
        WHEN 'fr--' THEN 'free'
        WHEN 'cu--' THEN 'customers'
        WHEN '--wl' THEN 'unknown' /* wlan */
        WHEN '--no' THEN 'none'
        WHEN '--wi' THEN 'none' /* wired */
        WHEN '--te' THEN 'none' /* terminal */
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
