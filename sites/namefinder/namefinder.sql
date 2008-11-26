-- phpMyAdmin SQL Dump
-- version 2.10.0.2
-- http://www.phpmyadmin.net
-- 
-- Host: 127.0.0.1:3305
-- Generation Time: Nov 26, 2008 at 08:11 PM
-- Server version: 5.0.22
-- PHP Version: 5.2.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

-- 
-- Database: `nf`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `canonical`
-- 

CREATE TABLE `canonical` (
  `canonical` varchar(255) character set utf8 NOT NULL,
  `region` int(11) NOT NULL,
  PRIMARY KEY  (`canonical`,`region`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `changedid`
-- 

CREATE TABLE `changedid` (
  `id` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `lock`
-- 

CREATE TABLE `lock` (
  `id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `named`
-- 

CREATE TABLE `named` (
  `id` bigint(20) NOT NULL,
  `region` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `name` varchar(255) character set utf8 NOT NULL,
  `canonical` varchar(255) character set utf8 NOT NULL,
  `category` varchar(255) character set utf8 NOT NULL,
  `is_in` varchar(255) character set utf8 NOT NULL,
  `rank` tinyint(4) NOT NULL,
  `info` text character set utf8 NOT NULL,
  KEY `id` (`id`),
  KEY `canonical` (`canonical`,`region`),
  KEY `region` (`region`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `node`
-- 

CREATE TABLE `node` (
  `id` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `options`
-- 

CREATE TABLE `options` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=275 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `placeindex`
-- 

CREATE TABLE `placeindex` (
  `id` int(11) NOT NULL,
  `region` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `rank` tinyint(4) NOT NULL,
  UNIQUE KEY `id` (`id`),
  KEY `region` (`region`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `postcodeprefix`
-- 

CREATE TABLE `postcodeprefix` (
  `prefix` varchar(255) NOT NULL,
  `placename` varchar(255) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  PRIMARY KEY  (`prefix`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `relation_node`
-- 

CREATE TABLE `relation_node` (
  `relation_id` bigint(20) NOT NULL,
  `node_id` bigint(20) NOT NULL,
  `role` varchar(255) character set utf8 NOT NULL,
  KEY `node_id` (`node_id`),
  KEY `relation_id` (`relation_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `relation_relation`
-- 

CREATE TABLE `relation_relation` (
  `relation_id` bigint(20) NOT NULL default '0',
  `other_relation_id` bigint(20) NOT NULL default '0',
  KEY `relation_id` (`relation_id`),
  KEY `other_relation_id` (`other_relation_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `relation_way`
-- 

CREATE TABLE `relation_way` (
  `relation_id` bigint(20) NOT NULL,
  `way_id` bigint(20) NOT NULL,
  `role` varchar(255) character set utf8 NOT NULL,
  KEY `way_id` (`way_id`),
  KEY `relation_id` (`relation_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `way_node`
-- 

CREATE TABLE `way_node` (
  `way_id` bigint(20) NOT NULL,
  `node_id` bigint(20) NOT NULL,
  KEY `node_id` (`node_id`),
  KEY `way_id` (`way_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `word`
-- 

CREATE TABLE `word` (
  `word` varchar(255) NOT NULL,
  `ordinal` tinyint(4) NOT NULL default '0',
  `firstword` tinyint(1) NOT NULL default '0',
  `lastword` tinyint(1) NOT NULL default '0',
  `region` int(11) NOT NULL default '0',
  `id` bigint(20) NOT NULL default '0',
  KEY `region` (`region`),
  KEY `id` (`id`),
  KEY `word` (`word`,`region`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
