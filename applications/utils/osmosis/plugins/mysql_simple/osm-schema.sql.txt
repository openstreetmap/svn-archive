-- phpMyAdmin SQL Dump
-- version 3.1.2deb1ubuntu0.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 16. Oktober 2009 um 19:33
-- Server Version: 5.0.75
-- PHP-Version: 5.2.6-3ubuntu4.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Datenbank: `osm`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `member_node`
--

CREATE TABLE `member_node` (
  `nodeid` int(11) NOT NULL,
  `relid` int(11) NOT NULL,
  `role` varchar(255) NOT NULL,
  KEY `wayid` (`nodeid`,`relid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `member_relation`
--

CREATE TABLE `member_relation` (
  `relid2` int(11) NOT NULL,
  `relid` int(11) NOT NULL,
  `role` varchar(255) NOT NULL,
  KEY `wayid` (`relid2`,`relid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `member_way`
--

CREATE TABLE `member_way` (
  `wayid` int(11) NOT NULL,
  `relid` int(11) NOT NULL,
  `role` varchar(255) default NULL,
  KEY `wayid` (`wayid`,`relid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `nodes`
--

CREATE TABLE `nodes` (
  `id` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `visible` tinyint(1) default NULL,
  `user` char(50) default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `node_tags`
--

CREATE TABLE `node_tags` (
  `id` int(11) NOT NULL,
  `k` varchar(50) NOT NULL,
  `v` varchar(255) NOT NULL,
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `relations`
--

CREATE TABLE `relations` (
  `id` int(11) NOT NULL,
  `visible` tinyint(4) default NULL,
  `user` varchar(50) default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `relation_tags`
--

CREATE TABLE `relation_tags` (
  `id` int(11) NOT NULL,
  `k` varchar(50) NOT NULL,
  `v` varchar(255) NOT NULL,
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ways`
--

CREATE TABLE `ways` (
  `id` int(11) NOT NULL,
  `visible` tinyint(4) default NULL,
  `user` varchar(50) default NULL,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ways_nodes`
--

CREATE TABLE `ways_nodes` (
  `nodeid` int(11) NOT NULL,
  `wayid` int(11) NOT NULL,
  `sequence` int(11) NOT NULL,
  KEY `nodeid` (`nodeid`,`wayid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `way_tags`
--

CREATE TABLE `way_tags` (
  `id` int(11) NOT NULL,
  `k` varchar(50) NOT NULL,
  `v` varchar(255) NOT NULL,
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

