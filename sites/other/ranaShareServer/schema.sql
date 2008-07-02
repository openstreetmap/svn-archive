-- Schema for ranaShareServer
--
-- Database: `ojw`
-- 

-- --------------------------------------------------------

-- 
-- Groups
-- 

CREATE TABLE `pos_groups` (
  `id` bigint(20) NOT NULL auto_increment,
  `owner` bigint(20) NOT NULL,
  `name` char(10) NOT NULL,
  `pin_write` bigint(20) NOT NULL,
  `pin_read` bigint(20) NOT NULL,
  `timeout` float NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

-- --------------------------------------------------------

-- 
-- Position reports
-- 

CREATE TABLE `pos_reports` (
  `user` bigint(20) NOT NULL,
  `group` bigint(20) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`user`,`group`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Nicknames
-- 

CREATE TABLE `pos_text` (
  `user` bigint(20) NOT NULL,
  `group` bigint(20) NOT NULL,
  `type` smallint(6) NOT NULL COMMENT '1=nickname',
  `text` varchar(40) NOT NULL,
  PRIMARY KEY  (`user`,`group`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Users
-- 

CREATE TABLE `pos_users` (
  `id` bigint(20) NOT NULL auto_increment,
  `pin` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=16 ;
