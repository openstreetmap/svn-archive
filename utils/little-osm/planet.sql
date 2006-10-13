CREATE DATABASE /*!32312 IF NOT EXISTS*/ `little-osm`;
USE `little-osm`;

DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `uid` int(10) unsigned NOT NULL default '0',
  `tags` text,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `reference` text,
  `minlat` float NOT NULL default '0',
  `minlon` float NOT NULL default '0',
  `maxlat` float NOT NULL default '0',
  `maxlon` float NOT NULL default '0',
  KEY `Index_1` (`uid`)
  KEY `Index_2` (`minlat`,`minlon`,`maxlat`,`maxlon`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
