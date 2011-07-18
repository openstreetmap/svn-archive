<Style name="parking-points">
    <!-- parking points: ticket vending machines -->
    <Rule>
      <Filter>[amenity] = 'vending_machine' and [vending]='parking_tickets'</Filter>
      &maxscale_zoom16;
      <PointSymbolizer file="&symbols;/parking-vending.png" />
    </Rule>
</Style>

<Layer name="parking-points" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-points</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,(tags->'amenity') as "amenity",(tags->'name') as "name",(tags->'vending') as "vending",(tags->'fee') as "fee"
       from &prefix;_point
       where 
          (tags ? 'amenity') AND (tags->'amenity') in ('vending_machine')
      ) as points
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
