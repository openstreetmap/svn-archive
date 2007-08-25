-- phpMyAdmin SQL Dump
-- version 2.10.0.2
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: Aug 21, 2007 at 05:34 PM
-- Server version: 5.0.37
-- PHP Version: 5.2.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

-- 
-- Database: `osmplaces`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `canon`
-- 

CREATE TABLE `canon` (
  `named_id` int(11) NOT NULL,
  `canon` varchar(255) character set utf8 NOT NULL,
  KEY `canon` (`canon`),
  KEY `named_id` (`named_id`)
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
  `id` int(11) NOT NULL auto_increment,
  `region` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `name` varchar(255) character set utf8 NOT NULL,
  `category` varchar(255) character set utf8 NOT NULL,
  `is_in` varchar(255) character set utf8 NOT NULL,
  `rank` tinyint(4) NOT NULL,
  `info` text character set utf8 NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `region` (`region`),
  KEY `category` (`category`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=352023472 ;

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=83 ;

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
-- Table structure for table `segment`
-- 

CREATE TABLE `segment` (
  `id` int(11) NOT NULL,
  `from` int(11) NOT NULL,
  `to` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
