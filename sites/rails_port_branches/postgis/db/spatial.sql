-- NODES

select addgeometrycolumn('public','current_nodes', 'geom', -1, 'POINT', 2);

create index current_nodes_geom_idx on current_nodes using gist (geom
    gist_geometry_ops);

create or replace function update_current_nodes_geom () returns trigger as '
begin
    new.geom := ''POINT('' || new.longitude || '' ''
                           || new.latitude  || '')'';
    return new;
end;
' language plpgsql;

create trigger current_nodes_geom_trigger before insert or update
    on current_nodes for each row
    execute procedure update_current_nodes_geom();

-- SEGMENTS

select addgeometrycolumn('public','current_segments', 'bbox', -1, 'POLYGON', 2);

create index current_segments_bbox_idx on current_segments using gist
        (bbox gist_geometry_ops);

create or replace function
    update_current_segments_bbox () returns trigger as '
declare
    box geometry;
    node_a bigint;
    node_b bigint;
begin
    node_a := new.node_a;
    node_b := new.node_b;
    select into box envelope(collect(cn.geom))
            from current_nodes cn where cn.id = node_a or cn.id = node_b;
    new.bbox := box;
    return new;
end;
' language plpgsql;

create trigger current_segments_bbox_trigger before insert or update
    on current_segments for each row
    execute procedure update_current_segments_bbox();

-- REBUILD SEGMENTS FROM NODES

create or replace function
    rebuild_matching_segments (bigint) returns void as '
declare
    node_id alias for $1;
begin
    update current_segments set bbox = NULL
        where node_a = node_id or node_b = node_id;
end;
' language plpgsql;

create or replace function
    on_update_current_nodes () returns trigger as '
declare
    node_id bigint;
begin
    node_id := new.id;
    perform rebuild_matching_segments(node_id);
    return new;
end;
' language plpgsql;

create or replace function
    on_delete_current_nodes () returns trigger as '
declare
    node_id bigint;
begin
    node_id := old.id;
    perform rebuild_matching_segments(node_id);
end;
' language plpgsql;

create trigger on_update_current_nodes_trigger after update
    on current_nodes for each row
    execute procedure on_update_current_nodes();

create trigger on_delete_current_nodes_trigger after delete
    on current_nodes for each row
    execute procedure on_delete_current_nodes();

-- WAYS

select addgeometrycolumn('public','current_ways', 'bbox', -1, 'GEOMETRY', 2);

create or replace function update_current_ways_bbox () returns trigger as '
declare
    box geometry;
    way_id bigint;
begin
    way_id := new.id;
    select into box envelope(collect(cs.bbox))
        from current_way_segments cws, current_segments cs
        where cws.id = way_id and cws.segment_id = cs.id;
    new.bbox := box;
    return new;
end;
' language plpgsql;

create trigger current_ways_bbox_trigger before insert or update
    on current_ways for each row
    execute procedure update_current_ways_bbox();

create index current_ways_geom_idx 
    on current_ways using gist (bbox gist_geometry_ops);

-- SEGMENTS -> WAYS

create or replace function
    rebuild_matching_ways (bigint) returns void as '
declare
    seg_id alias for $1;
begin
    update current_ways set bbox = NULL
                from current_way_segments cws
                where segment_id = seg_id and current_ways.id = cws.id;
end;
' language plpgsql;

create or replace function on_update_current_segments () returns trigger as '
declare
    segment_id bigint;
begin
    segment_id := new.id;
    perform rebuild_matching_ways(segment_id);
    return new;
end;
' language plpgsql;

create or replace function on_delete_current_segments () returns trigger as '
declare
    segment_id bigint;
begin
    segment_id := old.id;
    perform rebuild_matching_ways(segment_id);
end;
' language plpgsql;

create trigger on_update_current_segments_trigger after update
    on current_segments for each row
    execute procedure on_update_current_segments();

create trigger on_delete_current_segments_trigger after delete
    on current_segments for each row
    execute procedure on_delete_current_segments();

-- WAY_SEGMENTS

create or replace function
    rebuild_way_by_segment (bigint) returns void as '
declare
    way_id alias for $1;
begin
    update current_ways set bbox = NULL where current_ways.id = way_id;
end;
' language plpgsql;

create or replace function on_insert_current_way_segments () returns trigger as '
declare
    way_id bigint;
begin
    way_id := new.id;
    perform rebuild_way_by_segment(way_id);
    return new;
end;
' language plpgsql;

create or replace function on_update_current_way_segments () returns trigger as '
declare
    way_id bigint;
begin
    way_id := old.id;
    perform rebuild_way_by_segment(way_id);
    if way_id <> new.id then
        way_id := new.id;
        perform rebuild_way_by_segment(way_id);
    end if;
    return new;
end;
' language plpgsql;

create or replace function on_delete_current_way_segments () returns trigger as '
declare
    way_id bigint;
begin
    way_id := old.id;
    perform rebuild_way_by_segment(way_id);
end;
' language plpgsql;

create trigger on_insert_current_way_segments_trigger after insert
    on current_way_segments for each row
    execute procedure on_insert_current_way_segments();

create trigger on_update_current_way_segments_trigger after update
    on current_way_segments for each row
    execute procedure on_update_current_way_segments();

create trigger on_delete_current_way_segments_trigger after delete
    on current_way_segments for each row
    execute procedure on_delete_current_way_segments();
