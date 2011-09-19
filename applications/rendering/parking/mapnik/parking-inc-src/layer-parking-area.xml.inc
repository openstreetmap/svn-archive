<Style name="parking-area">

    <!-- parking area: ticket -->
    <Rule>
      <Filter>[cond] = 'ticket'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond] = 'ticket'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_fee.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: disc -->
    <Rule>
      <Filter>[cond]='disc'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond]='disc'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_disc.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: customers -->
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_cust.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: residents -->
    <Rule>
      <Filter>[cond]='residents'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond]='residents'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_resi.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: private -->
    <Rule>
      <Filter>[cond]='private'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond]='private'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_priv.png" type="png" width="16" height="16"/>
    </Rule>

    <!-- parking area: free -->
    <Rule>
      <Filter>[cond] = 'free'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <Filter>[cond] = 'free'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_free.png" type="png" width="16" height="16"/>
    </Rule>

    <Rule>
      <ElseFilter/>
      &maxscale_zoom10;
      &minscale_zoom15;
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
      <ElseFilter/>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_unkn.png" type="png" width="16" height="16"/>
    </Rule>
</Style>

<!--
fee [y,n,-] = SUBSTRING(COALESCE(tags->fee,'-'),1,1)
access [Private,Residents,Customers,-] = SUBSTRING(COALESCE(tags->access,'-'),1,1)
condition [Ticket,Disk,Free,Private,Residents,Customers,-] = SUBSTRING(COALESCE(tags->'parking:condition:area','-'),1,1)

FAC = SUBSTRING(COALESCE(tags->fee,'-'),1,1) || SUBSTRING(COALESCE(tags->access,'-'),1,1) || SUBSTRING(COALESCE(tags->'parking:condition:area','-'),1,1)

fee (new) - [parking:condition:area] = 'ticket'
fee (old) - [amenity] = 'parking' and [fee]='yes'
disc - [parking:condition:area]='disc'
customers - [amenity]='parking' and ([parking:condition:area]='customers' or [access]='customers')
residents - [amenity] = 'parking' and ([parking:condition:area]='residents' or [access]='residents')
private - [amenity] = 'parking' and ([parking:condition:area]='private' or [access]='private' or [access]='permissive')
free (new) - [parking:condition:area] = 'free'
free (old) - [amenity] = 'parking' and [fee]='no'
ElseFilter

Fehlt noch: maxstay auswerten -> disc
Für inline string-replace nutzen: overlay(string placing string from int [for int])
-->

<Layer name="parking-area" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,(tags->'amenity') AS "amenity",(tags->'tourism') AS "tourism",(tags->'name') AS "name",(tags->'fee') AS "fee",(tags->'access') AS "access",(tags->'parking:condition:area') AS "parking:condition:area",
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
      from &prefix;_polygon
      where 
          (tags ? 'amenity') AND (tags->'amenity') in ('parking')
          or (tags ? 'tourism') AND (tags->'tourism') in ('camp_site','caravan_site','picnic_site')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="parking-area-top" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,(tags->'amenity') AS "amenity",(tags->'tourism') AS "tourism",(tags->'name') AS "name",(tags->'fee') AS "fee",(tags->'access') AS "access",(tags->'parking:condition:area') AS "parking:condition:area",
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
      from &prefix;_polygon
      where 
          (tags ? 'amenity') AND (tags->'amenity') in ('parking')
          or (tags ? 'tourism') AND (tags->'tourism') in ('camp_site','caravan_site','picnic_site')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Style name="parking-area-text">
    <Rule>
      <Filter>[amenity] = 'parking'</Filter>
      &maxscale_zoom14;
      &minscale_zoom18;
      <TextSymbolizer name="fac" fontset_name="book-fonts" size="8" fill="#101010" dy="0" halo_radius="1" wrap_width="20"/>
    </Rule>
</Style>

<Layer name="parking-area-text" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area-text</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,(tags->'amenity') AS "amenity",(tags->'tourism') AS "tourism",(tags->'name') AS "name",(tags->'fee') AS "fee",(tags->'access') AS "access",(tags->'parking:condition:area') AS "parking:condition:area",
        LOWER(SUBSTRING(COALESCE(tags->'fee','-'),1,1) || SUBSTRING(COALESCE(tags->'access','-'),1,1) || SUBSTRING(COALESCE(tags->'parking:condition:area','-'),1,1)) AS "fac"
      from &prefix;_polygon
      where 
          (tags ? 'amenity') AND (tags->'amenity') in ('parking')
          or (tags ? 'tourism') AND (tags->'tourism') in ('camp_site','caravan_site','picnic_site')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
