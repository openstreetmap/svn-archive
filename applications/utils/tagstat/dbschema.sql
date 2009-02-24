-- MySQL dump 10.11
--
-- Host: localhost    Database: tagstat
-- ------------------------------------------------------
-- Server version	5.0.67

USE tagstat;

DROP TABLE IF EXISTS `tagpairs`;
CREATE TABLE `tagpairs` (
  `id` int(11) NOT NULL auto_increment,
  `tag` varchar(166) default NULL,
  `value` varchar(166) default NULL,
  `count` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `tag_idx` (`tag`,`value`)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `tag` varchar(256) default NULL,
  `uses` int(11) default NULL,
  `uniq_values` int(11) default NULL,
  PRIMARY KEY (`tag`)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;
