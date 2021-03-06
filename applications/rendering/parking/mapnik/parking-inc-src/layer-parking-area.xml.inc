<Style name="parking-area">

    <!-- parking area: ticket -->
    <Rule>
      <Filter>[cond] = 'ticket'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_fee;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_fee_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'ticket' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_fee.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'ticket' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_fee.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'ticket' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_fee.png"/>
    </Rule>

    <!-- parking area: disc -->
    <Rule>
      <Filter>[cond]='disc'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_disc;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_disc_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond]='disc' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_disc.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'disc' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_disc.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'disc' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_disc.png"/>
    </Rule>

    <!-- parking area: customers -->
    <Rule>
      <Filter>[cond]='customers'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_customers;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_customers_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond]='customers' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_cust.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='customers' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_cust.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='customers' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_cust.png"/>
    </Rule>

    <!-- parking area: residents -->
    <Rule>
      <Filter>[cond]='residents'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_residents;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_residents_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond]='residents' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_resi.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='residents' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_resi.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='residents' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_resi.png"/>
    </Rule>

    <!-- parking area: private -->
    <Rule>
      <Filter>[cond]='private'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_private;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_private_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond]='private' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_priv.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='private' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_priv.png"/>
    </Rule>
    <Rule>
      <Filter>[cond]='private' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_priv.png"/>
    </Rule>

    <!-- parking area: free -->
    <Rule>
      <Filter>[cond] = 'free'</Filter>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_free;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_free_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'free' and [parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_free.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'free' and [parking] = 'multistorey'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_multistorey_free.png"/>
    </Rule>
    <Rule>
      <Filter>[cond] = 'free' and [parking] = 'underground'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_underground_free.png"/>
    </Rule>

    <Rule>
      <ElseFilter/>
      &maxscale_zoom10;
      &minscale_zoom15;
      <PolygonSymbolizer fill="&pa_unknown;" fill-opacity="&pa_fill_opacity;"/>
      <LineSymbolizer stroke="&pa_unknown_stroke;" stroke-width="&pa_stroke_width;" stroke-opacity="&pa_stroke_opacity;"/>
    </Rule>
    <Rule>
      <ElseFilter/>
      <Filter>[parking] = 'surface'</Filter>
      &maxscale_zoom16;
      <PolygonPatternSymbolizer file="&symbols;/parking_area_unkn.png"/>
    </Rule>
</Style>

<!--
fee [y,n,-] = SUBSTRING(COALESCE(tags->fee,'-'),1,1)
access [Private,Residents,Customers,-] = SUBSTRING(COALESCE(tags->access,'-'),1,1)
condition [Ticket,Disk,Free,Private,Residents,Customers,-] = SUBSTRING(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'),1,1)

FAC = SUBSTRING(COALESCE(tags->fee,'-'),1,1) || SUBSTRING(COALESCE(tags->access,'-'),1,1) || SUBSTRING(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'),1,1)

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
      (SELECT way,amenity,name,
        CASE /* parking= */ LOWER(COALESCE(tags->'parking','surface'))
          WHEN 'surface' THEN 'surface'
          WHEN 'multi-storey' THEN 'multistorey'
          WHEN 'underground' THEN 'underground'
          WHEN 'sheds' THEN 'surface'
          WHEN 'carports' THEN 'surface'
          WHEN 'garages' THEN 'surface'
	      ELSE 'surface'
        END AS "parking",
        CASE /* fee-access-condition= */ (SUBSTRING("feeX",1,1) || SUBSTRING("accessX",1,1) || SUBSTRING("conditionX",1,1))
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
        END AS "cond",tags
      from (
        /* subselect cleans following values: access->accessX, parking:condition->conditionX, fee->feeX */
        SELECT osm_id,way,amenity,name,tags,
          CASE /* feeX = */ LOWER(COALESCE(tags->'fee','-'))
            WHEN '-' THEN '-'
            ELSE LOWER(COALESCE(tags->'fee','-'))
          END AS "feeX",
          CASE /* accessX = */ LOWER(COALESCE(access,'-'))
            WHEN 'permit' THEN 'residentsX'
            WHEN 'permissive' THEN 'customersX'
            WHEN 'destination' THEN 'customersX'
            WHEN 'yes' THEN '-'
            ELSE LOWER(COALESCE(access,'-'))
          END AS "accessX",
          CASE /* conditionX = */ LOWER(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'))
            WHEN 'destination' THEN 'customersX'
            WHEN 'permissive' THEN 'customersX'
            WHEN 'visitors' THEN 'customersX'
            ELSE LOWER(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'))
          END AS "conditionX"
          from &prefix;_polygon
        ) as subselect
      where 
          amenity in ('parking')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="parking-area-top" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,amenity,name,
        CASE /* parking= */ LOWER(COALESCE(tags->'parking','surface'))
          WHEN 'surface' THEN 'surface'
          WHEN 'multi-storey' THEN 'multistorey'
          WHEN 'underground' THEN 'underground'
          WHEN 'sheds' THEN 'surface'
          WHEN 'carports' THEN 'surface'
          WHEN 'garages' THEN 'surface'
	      ELSE 'surface'
        END AS "parking",
        CASE /* fee-access-condition= */ (SUBSTRING("feeX",1,1) || SUBSTRING("accessX",1,1) || SUBSTRING("conditionX",1,1))
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
        END AS "cond",tags
      from (
        /* subselect cleans following values: access->accessX, parking:condition->conditionX, fee->feeX */
        SELECT osm_id,way,amenity,name,tags,
          CASE /* feeX = */ LOWER(COALESCE(tags->'fee','-'))
            WHEN '-' THEN '-'
            ELSE LOWER(COALESCE(tags->'fee','-'))
          END AS "feeX",
          CASE /* accessX = */ LOWER(COALESCE(access,'-'))
            WHEN 'permit' THEN 'residentsX'
            WHEN 'permissive' THEN 'customersX'
            WHEN 'destination' THEN 'customersX'
            WHEN 'yes' THEN '-'
            ELSE LOWER(COALESCE(access,'-'))
          END AS "accessX",
          CASE /* conditionX = */ LOWER(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'))
            WHEN 'destination' THEN 'customersX'
            WHEN 'permissive' THEN 'customersX'
            WHEN 'visitors' THEN 'customersX'
            ELSE LOWER(COALESCE(tags->'parking:condition:area',tags->'parking:condition','-'))
          END AS "conditionX"
          from &prefix;_polygon
        ) as subselect
      where 
          amenity in ('parking')
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
      <TextSymbolizer fontset-name="book-fonts" size="8" fill="#101010" dy="0" halo-radius="1" wrap-width="20">[parkname]</TextSymbolizer>
    </Rule>
</Style>

<Layer name="parking-area-text" status="on" srs="&osm2pgsql_projection;">
    <StyleName>parking-area-text</StyleName>
    <Datasource>
      <Parameter name="table">
      (SELECT way,amenity,name,
        (COALESCE(name,tags->'parking:condition:area:customers',tags->'parking:condition:customers',operator,'')
        ) AS "parkname" 
      from &prefix;_polygon
      where
          amenity in ('parking')
      ) as parking
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
