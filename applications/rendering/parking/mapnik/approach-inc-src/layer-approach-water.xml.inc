<!--  &layer-water; -->
<Style name="water_areas">
    <Rule>
      <Filter>[waterway] = 'dock' or [waterway] = 'mill_pond' or [waterway]='canal'</Filter>
      &min_z9;
      <PolygonSymbolizer fill="&approach_water;"/>
    </Rule>
    <Rule>
      <Filter>[landuse] = 'basin'</Filter>
      &min_z7;
      <PolygonSymbolizer fill="&approach_water;"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'lake' or [natural] = 'water' or [landuse] = 'reservoir' or [waterway] = 'riverbank' or [landuse] = 'water' or [natural]='bay'</Filter>
      &min_z6;
      <PolygonSymbolizer fill="&approach_water;"/>
    </Rule>
    <Rule>
      <Filter>[natural] = 'land'</Filter>
      &min_z10;
      <PolygonSymbolizer fill="&approach_background;"/>
    </Rule>
</Style>

<Style name="water-lines-casing">
    <Rule>
      <Filter>[waterway]='stream' or [waterway]='ditch' or [waterway]='drain'</Filter>
      &min_z13;
      &max_z14;
      <LineSymbolizer stroke="white" stroke-width="1.5"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='stream'</Filter>
      &min_z15;
      &max_z18;
      <LineSymbolizer stroke="white" stroke-width="2.5"/>
    </Rule>
</Style>

<Style name="water-lines-low-zoom">
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z8;
      &max_z8;
      <LineSymbolizer stroke="&approach_water;" stroke-width="0.7"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z9;
      &max_z9;
      <LineSymbolizer stroke="&approach_water;" stroke-width="1.2"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z10;
      &max_z11;
      <LineSymbolizer stroke="&approach_water;" stroke-width="1.6"/>
    </Rule>
</Style>

<Style name="water_lines">
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z12;
      &max_z12;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="2" stroke-linecap="round"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z13;
      &max_z13;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="3" stroke-linecap="round"/>
      <TextSymbolizer size="9" fill="&approach_water;" placement="line" spacing="400" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z14;
      &max_z14;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="5" stroke-linecap="round"/>
      <TextSymbolizer size="10" fill="#6699cc" placement="line" spacing="400" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule><Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z15;
      &max_z16;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="6" stroke-linecap="round"/>
      <TextSymbolizer size="10" fill="#6699cc" placement="line" spacing="400" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z17;
      &max_z17;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="10" stroke-linecap="round"/>
      <TextSymbolizer size="10" fill="#6699cc" placement="line" spacing="400" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='river'</Filter>
      &min_z18;
      &max_z18;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="12" stroke-linecap="round"/>
      <TextSymbolizer size="10" fill="#6699cc" placement="line" spacing="400" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='canal' and not [disused]='yes'</Filter>
      &min_z12;
      &max_z12;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="3" stroke-linecap="round"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='canal' and not [disused]='yes'</Filter>
      &min_z13;
      &max_z13;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="4" stroke-linecap="round"/>
      <TextSymbolizer size="8" fill="#6699cc" placement="line" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='stream' or [waterway]='ditch' or [waterway]='drain'</Filter>
      &min_z13;
      &max_z14;
      <LineSymbolizer stroke="&approach_water;" stroke-width="1"/>
    </Rule>
    <Rule>
      <Filter>[waterway]='canal' and not ([tunnel] = 'yes' or [disused]='yes')</Filter>
      &min_z14;
      &max_z16;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="7" stroke-linecap="round"/>
      <TextSymbolizer size="9" fill="#6699cc" placement="line" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='canal' and not ([tunnel] = 'yes' or [disused]='yes' or [lock]='yes')</Filter>
      &min_z17;
      &max_z18;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="11" stroke-linecap="round"/>
      <TextSymbolizer size="9" fill="#6699cc" placement="line" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='canal' and not ([tunnel] = 'yes' or [disused]='yes') and [lock]='yes'</Filter>
      &min_z17;
      &max_z18;
      <LineSymbolizer stroke-linejoin="round" stroke="&approach_water;" stroke-width="7" stroke-linecap="round"/>
      <TextSymbolizer size="9" fill="#6699cc" fontset-name="book-fonts" halo-radius="1" wrap-width="20">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>[waterway]='stream' and not [tunnel]='yes'</Filter>
      &min_z15;
      &max_z18;
      <LineSymbolizer stroke="&approach_water;" stroke-width="2"/>
      <TextSymbolizer size="8" fill="#6699cc" spacing="600" placement="line" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
    <Rule>
      <Filter>([waterway]='drain' or [waterway]='ditch') and not [tunnel]='yes'</Filter>
      &min_z15;
      &max_z18;
      <LineSymbolizer stroke="&approach_water;" stroke-width="1"/>
      <TextSymbolizer size="8" fill="#6699cc" spacing="600" placement="line" fontset-name="book-fonts" halo-radius="1">[name]</TextSymbolizer>
    </Rule>
</Style>

<Layer name="water-lines-casing" status="on" srs="&osm2pgsql_projection;">
    <StyleName>water-lines-casing</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,waterway
      from &prefix;_line
      where waterway in ('stream','drain','ditch')
        and (tunnel is null or tunnel != 'yes')
      ) as water_lines</Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
<Layer name="water_areas" status="on" srs="&osm2pgsql_projection;">
    <StyleName>water_areas</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,"natural",waterway,landuse,name
      from &prefix;_polygon
      where (waterway in ('dock','mill_pond','riverbank','canal')
         or landuse in ('reservoir','water','basin')
         or "natural" in ('lake','water','land','glacier','mud','bay'))
         and building is null
      order by z_order,way_area desc
      ) as water_areas</Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="water-lines-low-zoom" status="on" srs="&osm2pgsql_projection;">
    <StyleName>water-lines-low-zoom</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,waterway
      from &prefix;_line
      where waterway='river'
      ) as water_lines</Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Layer name="water_lines" status="on" srs="&osm2pgsql_projection;">
    <StyleName>water_lines</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,waterway,disused,lock,name,
      case when tunnel in ('yes','true','1') then 'yes'::text else tunnel end as tunnel
      from &prefix;_line
      where waterway in ('weir','river','canal','derelict_canal','stream','drain','ditch','wadi')
        and (bridge is null or bridge not in ('yes','true','1','aqueduct'))
      order by z_order
      ) as water_lines</Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<!-- &layer-water_features; -->
<!-- weggelassen -->
