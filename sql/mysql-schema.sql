-- MySQL dump 10.9
--
-- Host: localhost    Database: openstreetmap
-- ------------------------------------------------------
-- Server version	4.1.11-Debian_4-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `area`
--

DROP TABLE IF EXISTS `area`;
CREATE TABLE `area` (
  `uid` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `timestamp` bigint(32) default NULL,
  `node_a` bigint(20) default NULL,
  `node_b` bigint(20) default NULL,
  `visible` tinyint(1) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `area_meta_table`
--

DROP TABLE IF EXISTS `area_meta_table`;
CREATE TABLE `area_meta_table` (
  `uid` bigint(20) NOT NULL auto_increment,
  `user_uid` bigint(20) default NULL,
  `timestamp` bigint(32) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `area_values`
--

DROP TABLE IF EXISTS `area_values`;
CREATE TABLE `area_values` (
  `user_uid` bigint(20) default NULL,
  `area_uid` bigint(20) default NULL,
  `key_uid` bigint(20) default NULL,
  `val` varchar(255) default NULL,
  `timestamp` bigint(32) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `gpx_to_insert`
--

DROP TABLE IF EXISTS `gpx_to_insert`;
CREATE TABLE `gpx_to_insert` (
  `originalname` varchar(255) default NULL,
  `tmpname` varchar(255) default NULL,
  `user_uid` bigint(20) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `key_meta_table`
--

DROP TABLE IF EXISTS `key_meta_table`;
CREATE TABLE `key_meta_table` (
  `uid` bigint(64) NOT NULL auto_increment,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `node_meta_table`
--

DROP TABLE IF EXISTS `node_meta_table`;
CREATE TABLE `node_meta_table` (
  `uid` bigint(64) NOT NULL auto_increment,
  `timestamp` bigint(32) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `nodes`
--

DROP TABLE IF EXISTS `nodes`;
CREATE TABLE `nodes` (
  `uid` bigint(64) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `timestamp` bigint(32) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  `tags` text NOT NULL,
  KEY `nodes_uid_idx` (`uid`),
  KEY `nodes_latlon_idx` (`latitude`,`longitude`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `osmKeys`
--

DROP TABLE IF EXISTS `osmKeys`;
CREATE TABLE `osmKeys` (
  `uid` bigint(64) default NULL,
  `name` varchar(255) default NULL,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `osmLineValues`
--

DROP TABLE IF EXISTS `osmLineValues`;
CREATE TABLE `osmLineValues` (
  `line_uid` bigint(64) default NULL,
  `key_uid` bigint(64) default NULL,
  `value` varchar(255) default NULL,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `poi_values`
--

DROP TABLE IF EXISTS `poi_values`;
CREATE TABLE `poi_values` (
  `user_uid` bigint(20) default NULL,
  `poi_uid` bigint(20) default NULL,
  `key_uid` bigint(20) default NULL,
  `val` varchar(255) default NULL,
  `timestamp` bigint(32) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `point_of_interest`
--

DROP TABLE IF EXISTS `point_of_interest`;
CREATE TABLE `point_of_interest` (
  `uid` bigint(20) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `points_meta_table`
--

DROP TABLE IF EXISTS `points_meta_table`;
CREATE TABLE `points_meta_table` (
  `uid` bigint(64) NOT NULL auto_increment,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) NOT NULL default '1',
  `name` varchar(255) NOT NULL default '',
  `size` bigint(20) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `points_of_interest_meta_table`
--

DROP TABLE IF EXISTS `points_of_interest_meta_table`;
CREATE TABLE `points_of_interest_meta_table` (
  `uid` bigint(20) NOT NULL auto_increment,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_meta_table`
--

DROP TABLE IF EXISTS `street_meta_table`;
CREATE TABLE `street_meta_table` (
  `uid` bigint(20) NOT NULL auto_increment,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_segment_meta_table`
--

DROP TABLE IF EXISTS `street_segment_meta_table`;
CREATE TABLE `street_segment_meta_table` (
  `uid` bigint(64) NOT NULL auto_increment,
  `timestamp` bigint(32) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_segment_values`
--

DROP TABLE IF EXISTS `street_segment_values`;
CREATE TABLE `street_segment_values` (
  `user_uid` bigint(20) default NULL,
  `street_segment_uid` bigint(20) default NULL,
  `key_uid` bigint(20) default NULL,
  `val` varchar(255) default NULL,
  `timestamp` bigint(20) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_segments`
--

DROP TABLE IF EXISTS `street_segments`;
CREATE TABLE `street_segments` (
  `uid` bigint(64) default NULL,
  `node_a` bigint(64) default NULL,
  `node_b` bigint(64) default NULL,
  `timestamp` bigint(32) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  `tags` text NOT NULL,
  KEY `street_segments_nodea_idx` (`node_a`),
  KEY `street_segments_nodeb_idx` (`node_b`),
  KEY `street_segment_uid_idx` (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_table`
--

DROP TABLE IF EXISTS `street_table`;
CREATE TABLE `street_table` (
  `uid` bigint(20) default NULL,
  `segment_uid` bigint(20) default NULL,
  `timestamp` bigint(20) default NULL,
  `user_uid` bigint(20) default NULL,
  `visible` tinyint(1) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `street_values`
--

DROP TABLE IF EXISTS `street_values`;
CREATE TABLE `street_values` (
  `user_uid` bigint(20) default NULL,
  `street_uid` bigint(20) default NULL,
  `key_uid` bigint(20) default NULL,
  `val` varchar(255) default NULL,
  `timestamp` bigint(20) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `streets`
--

DROP TABLE IF EXISTS `streets`;
CREATE TABLE `streets` (
  `name` varchar(255) character set latin1 default NULL,
  `uid` bigint(20) NOT NULL auto_increment,
  `timestamp` bigint(20) default NULL,
  `user_uid` mediumint(9) default NULL,
  `visible` tinyint(1) default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `tempPoints`
--

DROP TABLE IF EXISTS `tempPoints`;
CREATE TABLE `tempPoints` (
  `altitude` float default NULL,
  `timestamp` bigint(20) default NULL,
  `uid` mediumint(9) default NULL,
  `hor_dilution` float default NULL,
  `vert_dilution` float default NULL,
  `trackid` int(11) default NULL,
  `quality` tinyint(3) unsigned default NULL,
  `satellites` tinyint(3) unsigned default NULL,
  `last_time` bigint(20) default NULL,
  `visible` tinyint(4) NOT NULL default '0',
  `dropped_by` mediumint(9) NOT NULL default '0',
  `latitude` double default NULL,
  `longitude` double default NULL,
  `gpx_id` bigint(20) default NULL,
  KEY `points_idx` (`latitude`,`longitude`,`timestamp`,`uid`),
  KEY `points_uid_idx` (`uid`),
  KEY `points_gpxid_idx` (`gpx_id`),
  KEY `points_time_idx` (`timestamp`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user` varchar(255) default NULL,
  `uid` mediumint(9) NOT NULL auto_increment,
  `timeout` bigint(20) default NULL,
  `token` varchar(255) default NULL,
  `active` int(11) NOT NULL default '0',
  `pass_crypt` varchar(255) default NULL,
  `creation_time` datetime default NULL,
  PRIMARY KEY  (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

