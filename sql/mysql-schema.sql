-- MySQL dump 10.9
--
-- Host: localhost    Database: openstreetmap
-- ------------------------------------------------------
-- Server version	4.1.15-Debian_1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `gps_points`
--

DROP TABLE IF EXISTS `gps_points`;
CREATE TABLE `gps_points` (
  `altitude` float default NULL,
  `user_id` bigint(20) default NULL,
  `trackid` int(11) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `gpx_id` bigint(20) default NULL,
  `timestamp` datetime default NULL,
  KEY `points_idx` (`latitude`,`longitude`,`user_id`),
  KEY `points_uid_idx` (`user_id`),
  KEY `points_gpxid_idx` (`gpx_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `gpx_files`
--

DROP TABLE IF EXISTS `gpx_files`;
CREATE TABLE `gpx_files` (
  `id` bigint(64) NOT NULL auto_increment,
  `user_id` bigint(20) default NULL,
  `visible` tinyint(1) NOT NULL default '1',
  `name` varchar(255) NOT NULL default '',
  `size` bigint(20) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `gpx_pending_files`
--

DROP TABLE IF EXISTS `gpx_pending_files`;
CREATE TABLE `gpx_pending_files` (
  `originalname` varchar(255) default NULL,
  `tmpname` varchar(255) default NULL,
  `user_id` bigint(20) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `meta_nodes`
--

DROP TABLE IF EXISTS `meta_nodes`;
CREATE TABLE `meta_nodes` (
  `id` bigint(64) NOT NULL auto_increment,
  `user_id` bigint(20) default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `meta_segments`
--

DROP TABLE IF EXISTS `meta_segments`;
CREATE TABLE `meta_segments` (
  `id` bigint(64) NOT NULL auto_increment,
  `user_id` bigint(20) default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `nodes`
--

DROP TABLE IF EXISTS `nodes`;
CREATE TABLE `nodes` (
  `id` bigint(64) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `user_id` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  `tags` text NOT NULL,
  `timestamp` datetime default NULL,
  KEY `nodes_uid_idx` (`id`),
  KEY `nodes_latlon_idx` (`latitude`,`longitude`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `segments`
--

DROP TABLE IF EXISTS `segments`;
CREATE TABLE `segments` (
  `id` bigint(64) default NULL,
  `node_a` bigint(64) default NULL,
  `node_b` bigint(64) default NULL,
  `user_id` bigint(20) default NULL,
  `visible` tinyint(1) default NULL,
  `tags` text NOT NULL,
  `timestamp` datetime default NULL,
  KEY `street_segments_nodea_idx` (`node_a`),
  KEY `street_segments_nodeb_idx` (`node_b`),
  KEY `street_segment_uid_idx` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `email` varchar(255) default NULL,
  `id` bigint(20) NOT NULL auto_increment,
  `token` varchar(255) default NULL,
  `active` int(11) NOT NULL default '0',
  `pass_crypt` varchar(255) default NULL,
  `creation_time` datetime default NULL,
  `timeout` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

