-- MySQL dump 10.11
--
-- Host: localhost    Database: tagstat
-- ------------------------------------------------------
-- Server version	5.0.67

--
-- Table structure for table `tagcomments`
--

DROP TABLE IF EXISTS `tagcomments`;
CREATE TABLE `tagcomments` (
  `id` int(11) NOT NULL auto_increment,
  `tag` text,
  `comment` text,
  `score` int(11) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

--
-- Table structure for table `tagpairs`
--

DROP TABLE IF EXISTS `tagpairs`;
CREATE TABLE `tagpairs` (
  `id` int(11) NOT NULL auto_increment,
  `tag` text,
  `value` text,
  `count` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `tag_idx` (`tag`(12),`value`(12)),
  KEY `count_ind` (`count`)
) ENGINE=MyISAM AUTO_INCREMENT=4633729 DEFAULT CHARSET=utf8;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `tag` text NOT NULL,
  `uses` int(11) default NULL,
  `uniq_values` int(11) default NULL,
  PRIMARY KEY  (`tag`(255)),
  KEY `uses_ind` (`uses`),
  FULLTEXT KEY `tag` (`tag`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
