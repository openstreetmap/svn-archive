-- MySQL dump 10.11
--
-- Host: localhost    Database: tagstat
-- ------------------------------------------------------
-- Server version	5.0.67

USE tagstat;

--
-- Table structure for table `tagcomments`
--

DROP TABLE IF EXISTS `tagcomments`;
CREATE TABLE `tagcomments` (
  `id` int(11) NOT NULL auto_increment,
  `tag` text,
  `comment` text,
  `score` int(11) default 0,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `tagpairs`
--

DROP TABLE IF EXISTS `tagpairs`;
CREATE TABLE `tagpairs` (
  `id` int(11) NOT NULL auto_increment,
  `tag` text,
  `value` text,
  `c_node` int(11) default 0,
  `c_way` int(11) default 0,
  `c_relation` int(11) default 0,
  `c_other` int(11) default 0,
  `c_total` int(11) default 0,
  PRIMARY KEY  (`id`),
  KEY `tag_idx` (`tag`(12),`value`(12)),
  KEY `c_node_ind` (`c_node`),
  KEY `c_way_ind` (`c_way`),
  KEY `c_relation_ind` (`c_relation`),
  KEY `c_total_ind` (`c_total`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `tag` text NOT NULL,
  `uses` int(11) default 0,
  `uniq_values` int(11) default 0,
  PRIMARY KEY  (`tag`(255)),
  KEY `uses_ind` (`uses`),
  FULLTEXT KEY `tag` (`tag`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
