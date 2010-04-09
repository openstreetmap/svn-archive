<Style name="parking-area">
    <!-- parking area: fee (new) -->
    <Rule>
      <Filter>[parking:condition:area] = 'ticket'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_fee;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_fee_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[parking:condition:area] = 'ticket'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_fee.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: fee (old) -->
    <Rule>
      <Filter>[amenity] = 'parking' and [fee]='yes'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_fee;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_fee_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[amenity] = 'parking' and [fee]='yes'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_fee.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: free (new) -->
    <Rule>
      <Filter>[parking:condition:area] = 'free'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_free;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_free_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[parking:condition:area] = 'free'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_free.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: free (old) -->
    <Rule>
      <Filter>[amenity] = 'parking' and [fee]='no'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_free;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_free_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[amenity] = 'parking' and [fee]='no'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_free.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: disc -->
    <Rule>
      <Filter>[parking:condition:area]='disc'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_customers;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_customers_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[parking:condition:area]='disc'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_disc.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: customers -->
    <Rule>
      <Filter>[parking:condition:area]='customers'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_customers;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_customers_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[parking:condition:area]='customers'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_cust.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: residents -->
    <Rule>
      <Filter>[amenity] = 'parking' and [parking:condition:area]='residents'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_residents;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_residents_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[amenity] = 'parking' and [parking:condition:area]='residents'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_resi.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: private -->
    <Rule>
      <Filter>[amenity] = 'parking' and [parking:condition:area]='private'</Filter>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_private;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_private_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <Filter>[amenity] = 'parking' and [parking:condition:area]='private'</Filter>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_priv.png" type="png" width="16" height="16"/>
    </Rule>

    <Rule>
      <ElseFilter/>
      &maxscale_zoom10;
      <PolygonSymbolizer>
        <CssParameter name="fill">&pa_unknown;</CssParameter>
        <CssParameter name="fill-opacity">&pa_fill_opacity;</CssParameter>
      </PolygonSymbolizer>
      <LineSymbolizer>
        <CssParameter name="stroke">&pa_unknown_stroke;</CssParameter>
        <CssParameter name="stroke-width">&pa_stroke_width;</CssParameter>
        <CssParameter name="stroke-opacity">&pa_stroke_opacity;</CssParameter>
      </LineSymbolizer>
    </Rule>
    <Rule>
      &maxscale_zoom16;
      <ElseFilter/>
      <PolygonPatternSymbolizer file="&symbols;/parking_area_unkn.png" type="png" width="16" height="16"/>
    </Rule>
</Style>

<Layer name="parking-area" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,tags->'amenity',tags->'tourism',tags->'name',tags->'fee',tags->'parking:condition:area'
       from &prefix;_polygon
       where 
          (tags ? 'amenity') AND (tags->'amenity') in ('parking')
          or (tags ? 'tourism') AND (tags->'tourism') in ('camp_site','caravan_site','picnic_site')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
