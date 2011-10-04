<Style name="wifi-area">

    <!-- wifi area: none -->
    <Rule>
      <Filter>[cond] = 'none'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&wa_none;</CssParameter>
        <CssParameter name="fill-opacity">&wa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&wa_none_stroke;</CssParameter>
        <CssParameter name="stroke-width">&wa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&wa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>

    <!-- wifi area: fee -->
    <Rule>
      <Filter>[cond] = 'fee'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&wa_fee;</CssParameter>
        <CssParameter name="fill-opacity">&wa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&wa_fee_stroke;</CssParameter>
        <CssParameter name="stroke-width">&wa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&wa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>

    <!-- wifi area: customers -->
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&wa_customers;</CssParameter>
        <CssParameter name="fill-opacity">&wa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&wa_customers_stroke;</CssParameter>
        <CssParameter name="stroke-width">&wa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&wa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>

    <!-- wifi area: free -->
    <Rule>
      <Filter>[cond] = 'free'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&wa_free;</CssParameter>
        <CssParameter name="fill-opacity">&wa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&wa_free_stroke;</CssParameter>
        <CssParameter name="stroke-width">&wa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&wa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>

    <Rule>
      <ElseFilter/>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&wa_unknown;</CssParameter>
        <CssParameter name="fill-opacity">&wa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&wa_unknown_stroke;</CssParameter>
        <CssParameter name="stroke-width">&wa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&wa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
</Style>

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

