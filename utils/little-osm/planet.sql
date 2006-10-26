CREATE DATABASE /*!32312 IF NOT EXISTS*/ `little-osm`;
USE `little-osm`;

DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `uid` int(10) unsigned NOT NULL default '0',
  `tags` text,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `reference` text,
  `min` point NOT NULL,
  `max` point NOT NULL,
  KEY `Index_1` (`uid`),
  spatial (`min`),
  spatial (`max`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
