<Style name="rail-lowzoom">
    <!-- RAIL -->
    <Rule>
      &min_z6;
      &max_z8;
      <Filter>[railway] = 'rail'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="0.6"/>
    </Rule>
    <Rule>
      &min_z9;
      &max_z9;
      <Filter>[railway] = 'rail' and not [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="1"/>
    </Rule>
    <Rule>
      &min_z9;
      &max_z9;
      <Filter>[railway] = 'rail' and [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="1" stroke-dasharray="5,2"/>
    </Rule>
    <Rule>
      &min_z8;
      &max_z9;
      <Filter>[railway] = 'tram' or [railway] = 'light_rail' or [railway] = 'narrow_gauge' or [railway]='funicular'</Filter>
      <LineSymbolizer stroke="#ccc" stroke-width="1"/>
    </Rule>
    <Rule>
      &min_z10;
      &max_z12;
      <Filter>[railway] = 'rail' and not [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="2"/>
    </Rule>
    <Rule>
      &min_z10;
      &max_z12;
      <Filter>[railway] = 'rail' and [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="2" stroke-dasharray="5,2"/>
    </Rule>
    <Rule>
      &min_z10;
      &max_z12;
      <Filter>[railway] = 'tram' or [railway] = 'light_rail' or [railway] = 'narrow_gauge' or [railway]='funicular'</Filter>
      <LineSymbolizer stroke="#aaa" stroke-width="1"/>
    </Rule>
    <Rule>
      &min_z12;
      &max_z12;
      <Filter>[railway] = 'preserved'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#aaa" stroke-width="1.5"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>[railway] = 'preserved'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#999999" stroke-width="3"/>
      <LineSymbolizer stroke-linejoin="round" stroke="white" stroke-width="1" stroke-dasharray="0,1,8,1"/>
    </Rule>
    <Rule>
      &min_z12;
      &max_z12;
      <Filter>[railway] = 'INT-preserved-ssy'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#aaa" stroke-width="1"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>[railway] = 'INT-preserved-ssy'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#999999" stroke-width="2"/>
      <LineSymbolizer stroke-linejoin="round" stroke="white" stroke-width="0.8" stroke-dasharray="0,1,8,1"/>
    </Rule>
    <Rule>
      &min_z14;
      &max_z18;
      <Filter>[railway]='monorail'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#fff" stroke-width="4" stroke-linecap="round" stroke-opacity="0.4"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#777" stroke-width="3.0" stroke-dasharray="2,3" stroke-linecap="round"/>
    </Rule>
</Style>
<Layer name="rail-lowzoom" status="on" srs="&osm2pgsql_projection;">
    <StyleName>rail-lowzoom</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,
       case when tunnel in ('yes','true','1') then 'yes'::text else tunnel end as tunnel,
       case when railway='preserved' and service in ('spur','siding','yard') then 'INT-preserved-ssy'::text else railway end as railway
       from &prefix;_roads
       where (railway is not null and railway!='preserved' and (service is null or service not in ('spur','siding','yard')))
          or railway='preserved'
       order by z_order
      ) as roads
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>

<Style name="railway">
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>[railway] = 'rail' and [tunnel] = 'yes'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#ffffff" stroke-width="3" stroke-dasharray="1,9"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#fdfdfd" stroke-width="3" stroke-dasharray="0,1,1,8"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#ececec" stroke-width="3" stroke-dasharray="0,2,1,7"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#cacaca" stroke-width="3" stroke-dasharray="0,3,1,6"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#afafaf" stroke-width="3" stroke-dasharray="0,4,1,5"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#a1a1a1" stroke-width="3" stroke-dasharray="0,5,1,4"/>
      <LineSymbolizer stroke-linejoin="round" stroke="#9b9b9b" stroke-width="3" stroke-dasharray="0,6,1,3"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z13;
      <Filter>[railway] = 'rail' and not [tunnel] = 'yes'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#999999" stroke-width="3"/>
      <LineSymbolizer stroke-linejoin="round" stroke="white" stroke-width="1" stroke-dasharray="8,12"/>
    </Rule>
    <Rule>
      &min_z14;
      &max_z18;
      <Filter>[railway] = 'rail' and not [tunnel]='yes'</Filter>
      <LineSymbolizer stroke-linejoin="round" stroke="#999999" stroke-width="3"/>
      <LineSymbolizer stroke-linejoin="round" stroke="white" stroke-width="1" stroke-dasharray="0,11,8,1"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>([railway]='narrow_gauge' or [railway]='funicular') and not [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#666" stroke-width="2"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>([railway]='narrow_gauge' or [railway]='funicular') and [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#666" stroke-width="5" stroke-dasharray="5,3"/>
      <LineSymbolizer stroke="#fff" stroke-width="4"/>
      <LineSymbolizer stroke="#aaa" stroke-width="1.5"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z14;
      <Filter>[railway]='tram' and [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#444" stroke-width="1" stroke-dasharray="5,3"/>
    </Rule>
    <Rule>
      &min_z15;
      &max_z18;
      <Filter>[railway]='tram' and [tunnel]='yes'</Filter>
      <LineSymbolizer stroke="#444" stroke-width="2" stroke-dasharray="5,3"/>
    </Rule>
    <Rule>
      &min_z13;
      &max_z18;
      <Filter>[railway]='light_rail' and [tunnel] = 'yes'</Filter>
      <LineSymbolizer stroke="#666" stroke-width="2" stroke-dasharray="5,3"/>
    </Rule>
     <Rule>
      &min_z13;
      &max_z18;
      <Filter>[railway]='light_rail' and not [tunnel] = 'yes'</Filter>
      <LineSymbolizer stroke="#666" stroke-width="2"/>
    </Rule>
</Style>
<Layer name="railway" status="on" srs="&osm2pgsql_projection;">
    <StyleName>railway</StyleName>
    <Datasource>
      <Parameter name="table">
      (select way,horse,bicycle,foot,construction,
       case when tunnel in ('yes','true','1') then 'yes'::text else tunnel end as tunnel,
       case when bridge in ('yes','true','1','viaduct') then 'yes'::text else bridge end as bridge,
       case when railway in ('spur','siding')
              or (railway='rail' and service in ('spur','siding','yard'))
            then 'spur-siding-yard'::text else railway end as railway
       from &prefix;_line
       where railway in ('light_rail','narrow_gauge','funicular','rail','subway','tram','spur','siding','platform','disused','abandoned','construction','miniature','turntable')
       order by z_order) as roads
      </Parameter>
      &datasource-settings;
    </Datasource>
</Layer>
