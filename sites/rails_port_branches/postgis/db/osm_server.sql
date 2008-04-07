--
-- Table structure for table "area_segments"
--

DROP TABLE "area_segments" CASCADE;
CREATE TABLE "area_segments" (
  "id" bigint NOT NULL default '0',
  "segment_id" int default NULL,
  "version" bigint NOT NULL default '0',
  "sequence_id" serial NOT NULL,
  PRIMARY KEY  ("id","version","sequence_id")
);

create index "area_segments_id_idx" on area_segments ("id");
create index "area_segments_segment_id_idx" on area_segments ("segment_id");
create index "area_segments_id_version_idx" on area_segments ("id","version");

--
-- Table structure for table "area_tags"
--

DROP TABLE "area_tags" CASCADE;
CREATE TABLE "area_tags" (
  "id" bigint NOT NULL default '0',
  "k" varchar(255) default NULL,
  "v" varchar(255) default NULL,
  "version" bigint NOT NULL default '0',
  "sequence_id" serial NOT NULL,
  PRIMARY KEY  ("id","version","sequence_id")
);

--
-- Table structure for table "areas"
--

DROP TABLE "areas" CASCADE;
CREATE TABLE "areas" (
  "id" bigint NOT NULL default '0',
  "user_id" bigint default NULL,
  "timestamp" timestamp default NULL,
  "version" serial NOT NULL,
  "visible" bool NOT NULL default true,
  PRIMARY KEY  ("id","version")
);

--
-- Table structure for table "current_nodes"
--

DROP TABLE "current_nodes" CASCADE;
CREATE TABLE "current_nodes" (
  "id" serial NOT NULL,
  "latitude" float default NULL,
  "longitude" float default NULL,
  "user_id" bigint default NULL,
  "visible" bool NOT NULL default true,
  "tags" text NOT NULL,
  "timestamp" timestamp default NULL,
  PRIMARY KEY  ("id")
);

create index "current_nodes_lat_lon_idx" on current_nodes ("latitude","longitude");

--
-- Table structure for table "current_segments"
--

DROP TABLE "current_segments" CASCADE;
CREATE TABLE "current_segments" (
  "id" serial NOT NULL,
  "node_a" bigint default NULL,
  "node_b" bigint default NULL,
  "user_id" bigint default NULL,
  "visible" bool NOT NULL default true,
  "tags" text NOT NULL,
  "timestamp" timestamp default NULL,
  PRIMARY KEY  ("id")
);

create index "current_segments_a_idx" on current_segments ("node_a");
create index "current_segments_b_idx" on current_segments ("node_b");

--
-- Table structure for table "current_way_segments"
--

DROP TABLE "current_way_segments" CASCADE;
CREATE TABLE "current_way_segments" (
  "id" bigint default NULL,
  "segment_id" bigint default NULL,
  "sequence_id" bigint default NULL
);
create index  "current_way_segments_seg_idx" on current_way_segments ("segment_id");
create index  "current_way_segments_id_idx" on current_way_segments ("id");

--
-- Table structure for table "current_way_tags"
--

DROP TABLE "current_way_tags" CASCADE;
CREATE TABLE "current_way_tags" (
  "id" bigint default NULL,
  "k" varchar(255) NOT NULL default '',
  "v" varchar(255) NOT NULL default ''
);

create index "current_way_tags_id_idx" on current_way_tags ("id");
create index "current_way_tags_v_idx" on current_way_tags ("v"); -- FULLTEXT

--
-- Table structure for table "current_ways"
--

DROP TABLE "current_ways" CASCADE;
CREATE TABLE "current_ways" (
  "id" serial NOT NULL,
  "user_id" bigint default NULL,
  "timestamp" timestamp default NULL,
  "visible" bool NOT NULL default true,
  PRIMARY KEY  ("id")
);

--
-- Table structure for table "diary_entries"
--

DROP TABLE "diary_entries" CASCADE;
CREATE TABLE "diary_entries" (
  "id" serial NOT NULL,
  "user_id" bigint NOT NULL,
  "title" varchar(255) default NULL,
  "body" text,
  "created_at" timestamp default NULL,
  "updated_at" timestamp default NULL,
  PRIMARY KEY  ("id")
);

--
-- Table structure for table "friends"
--

DROP TABLE "friends" CASCADE;
CREATE TABLE "friends" (
  "id" serial NOT NULL,
  "user_id" bigint NOT NULL,
  "friend_user_id" bigint NOT NULL,
  PRIMARY KEY  ("id")
);

create index "user_id_idx" on friends ("friend_user_id");

--
-- Table structure for table "gps_points"
--

DROP TABLE "gps_points" CASCADE;
CREATE TABLE "gps_points" (
  "altitude" float default NULL,
  "user_id" bigint default NULL,
  "trackid" int default NULL,
  "latitude" float default NULL,
  "longitude" float default NULL,
  "gpx_id" bigint default NULL,
  "timestamp" timestamp default NULL
);

create index "points_idx" on gps_points ("latitude","longitude","user_id");
create index "points_uid_idx" on gps_points ("user_id");
create index "points_gpxid_idx" on gps_points ("gpx_id");
create index "gps_points_timestamp_idx" on gps_points ("timestamp");

--
-- Table structure for table "gpx_file_tags"
--

DROP TABLE "gpx_file_tags" CASCADE;
CREATE TABLE "gpx_file_tags" (
  "gpx_id" bigint NOT NULL default '0',
  "tag" varchar(255) default NULL,
  "id" serial NOT NULL,
  PRIMARY KEY  ("id")
);
create index  "gpx_file_tags_gpxid_idx" on gpx_file_tags ("gpx_id");

--
-- Table structure for table "gpx_files"
--

DROP TABLE "gpx_files" CASCADE;
CREATE TABLE "gpx_files" (
  "id" serial NOT NULL,
  "user_id" bigint default NULL,
  "visible" bool NOT NULL default true,
  "name" varchar(255) NOT NULL default '',
  "size" bigint default NULL,
  "latitude" float default NULL,
  "longitude" float default NULL,
  "timestamp" timestamp default NULL,
  "public" bool NOT NULL default '1',
  "description" varchar(255) default '',
  "inserted" bool default NULL,
  PRIMARY KEY  ("id")
);
create index "gpx_files_visible_public_idx" on gpx_files ("visible","public");

--
-- Table structure for table "gpx_pending_files"
--

DROP TABLE "gpx_pending_files" CASCADE;
CREATE TABLE "gpx_pending_files" (
  "originalname" varchar(255) default NULL,
  "tmpname" varchar(255) default NULL,
  "user_id" bigint default NULL
);



--
-- Table structure for table "messages"
--

DROP TABLE "messages" CASCADE;
CREATE TABLE "messages" (
  "id" serial NOT NULL,
  "user_id" bigint NOT NULL,
  "from_user_id" bigint NOT NULL,
  "from_display_name" varchar(255) default '',
  "title" varchar(255) default NULL,
  "body" text,
  "sent_on" timestamp default NULL,
  "message_read" boolean NOT NULL default FALSE,
  "to_user_id" bigint NOT NULL,
  PRIMARY KEY  ("id")
);

create index "from_name_idx" on messages ("from_display_name");

--
-- Table structure for table "meta_areas"
--

DROP TABLE "meta_areas" CASCADE;
CREATE TABLE "meta_areas" (
  "id" serial NOT NULL,
  "user_id" bigint default NULL,
  "timestamp" timestamp default NULL,
  PRIMARY KEY  ("id")
);

--
-- Table structure for table "nodes"
--

DROP TABLE "nodes" CASCADE;
CREATE TABLE "nodes" (
  "id" bigint default NULL,
  "latitude" float default NULL,
  "longitude" float default NULL,
  "user_id" bigint default NULL,
  "visible" bool NOT NULL default true,
  "tags" text NOT NULL,
  "timestamp" timestamp default NULL
);
create index "nodes_uid_idx" on nodes ("id");
create index "nodes_latlon_idx" on nodes  ("latitude","longitude");

--
-- Table structure for table "segments"
--

DROP TABLE "segments" CASCADE;
CREATE TABLE "segments" (
  "id" bigint default NULL,
  "node_a" bigint default NULL,
  "node_b" bigint default NULL,
  "user_id" bigint default NULL,
  "visible" bool NOT NULL default true,
  "tags" text NOT NULL,
  "timestamp" timestamp default NULL
);
create index  "street_segments_nodea_idx" on segments ("node_a");
create index  "street_segments_nodeb_idx" on segments ("node_b");
create index  "street_segment_uid_idx" on segments ("id");

--
-- Table structure for table "users"
--

DROP TABLE "users" CASCADE;
CREATE TABLE "users" (
  "email" varchar(255) default NULL,
  "id" serial NOT NULL,
  "token" varchar(255) default NULL,
  "active" boolean NOT NULL default FALSE,
  "pass_crypt" varchar(255) default NULL,
  "creation_time" timestamp default NULL,
  "timeout" timestamp default NULL,
  "display_name" varchar(255) default '',
  "preferences" text,
  "data_public" bool default '0',
  "description" text,
  "home_lat" float default NULL,
  "home_lon" float default NULL,
  PRIMARY KEY  ("id")
);
create index "users_email_idx" on users ("email");
create index "users_display_name_idx" on users ("display_name");



--
-- Table structure for table "way_segments"
--

DROP TABLE "way_segments" CASCADE;
CREATE TABLE "way_segments" (
  "id" bigint NOT NULL default '0',
  "segment_id" int default NULL,
  "version" bigint NOT NULL default '0',
  "sequence_id" serial NOT NULL,
  PRIMARY KEY  ("id","version","sequence_id")
);

--
-- Table structure for table "way_tags"
--

DROP TABLE "way_tags" CASCADE;
CREATE TABLE "way_tags" (
  "id" bigint NOT NULL default '0',
  "k" varchar(255) default NULL,
  "v" varchar(255) default NULL,
  "version" bigint default NULL
);
create index  "way_tags_id_version_idx" on "way_tags" ("id","version");

--
-- Table structure for table "ways"
--

DROP TABLE "ways" CASCADE;
CREATE TABLE "ways" (
  "id" bigint NOT NULL default '0',
  "user_id" bigint default NULL,
  "timestamp" timestamp default NULL,
  "version" serial NOT NULL,
  "visible" bool NOT NULL default true,
  PRIMARY KEY  ("id","version")
);
create index  "ways_id_version_idx" on ways ("id");
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

