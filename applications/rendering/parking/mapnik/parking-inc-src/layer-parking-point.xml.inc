<Style name="parking-points">
    <!-- parking points: ticket vending machines -->
    <Rule>
      <Filter>[amenity] = 'vending_machine' and [vending]='parking_tickets'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking-vending.png" />
    </Rule>

    <!-- parking nodes -->
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='ticket'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_fee.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='disc'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_disc.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='customers'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_cust.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='residents'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_resi.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='private'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_priv.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='free'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_free.png" />
    </Rule>
    <Rule>
      <Filter>[amenity] = 'parking' and [cond]='unknown'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking_node_unkn.png" />
    </Rule>
</Style>

<Layer name="parking-points" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-points</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,amenity,name,(tags->'vending') as "vending",(tags->'fee') as "fee",
        CASE /* fee-access-condition= */ LOWER(SUBSTRING(COALESCE(tags->'fee','-'),1,1) || SUBSTRING(COALESCE(tags->'access','-'),1,1) || SUBSTRING(COALESCE(tags->'parking:condition:area','-'),1,1))
        WHEN '--t' THEN 'ticket'
        WHEN 'y--' THEN 'ticket'
        WHEN 'yc-' THEN 'ticket' /* access=customers fee=yes */
        WHEN 'y-c' THEN 'ticket' /* access=customers fee=yes */
        WHEN 'ycc' THEN 'ticket' /* access=customers fee=yes */
        WHEN 'y-t' THEN 'ticket'
        WHEN '--d' THEN 'disc'
        WHEN 'n-d' THEN 'disc'
        WHEN '--c' THEN 'customers'
        WHEN '-c-' THEN 'customers'
        WHEN '-cc' THEN 'customers'
        WHEN 'n-c' THEN 'customers'
        WHEN 'nc-' THEN 'customers'
        WHEN 'ncc' THEN 'customers'
        WHEN '-r-' THEN 'residents'
        WHEN '--r' THEN 'residents'
        WHEN '-rr' THEN 'residents'
        WHEN 'nr-' THEN 'residents'
        WHEN 'n-r' THEN 'residents'
        WHEN 'nrr' THEN 'residents'
        WHEN '-p-' THEN 'private'
        WHEN '--p' THEN 'private'
        WHEN '-pp' THEN 'private'
        WHEN 'np-' THEN 'private'
        WHEN 'n-p' THEN 'private'
        WHEN 'npp' THEN 'private'
        WHEN '--f' THEN 'free'
        WHEN 'n--' THEN 'free'
        WHEN 'n-f' THEN 'free'
	    ELSE 'unknown'
        END AS "cond"
       from &prefix;_point
       where 
          amenity in ('vending_machine','parking')
      ) as points
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
