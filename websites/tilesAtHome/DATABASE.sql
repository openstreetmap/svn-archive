
-- 
-- Table structure for table `tiles`
-- 

CREATE TABLE `tiles` (
  `id` bigint(20) NOT NULL auto_increment COMMENT 'temporary key',
  `x` bigint(20) NOT NULL,
  `y` bigint(20) NOT NULL,
  `z` smallint(6) NOT NULL,
  `exists` smallint(6) NOT NULL,
  `size` float NOT NULL,
  `user` varchar(20) character set utf8 collate utf8_unicode_ci NOT NULL,
  `date` int(11) NOT NULL,
  `tile` longblob NOT NULL COMMENT 'Storage for the tile image in PNG format',
  `to_import` tinyint(4) NOT NULL default '0' COMMENT 'Set this to 1 if the tile needs to be copied from bandnet (for use by import scripts)',
  `todo` tinyint(4) NOT NULL default '0' COMMENT 'If this is 1, the associated tileset needs rendering. only for z=12 tiles',
  PRIMARY KEY  (`x`,`y`,`z`),
  UNIQUE KEY `id` (`id`),
  KEY `exists` (`exists`),
  KEY `todo` (`todo`),
  KEY `user` (`user`),
  KEY `z` (`z`),
  KEY `size` (`size`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=15022300 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `tiles_log`
-- 

CREATE TABLE `tiles_log` (
  `id` bigint(20) NOT NULL auto_increment COMMENT 'just used as index',
  `user` varchar(50) collate utf8_unicode_ci NOT NULL COMMENT 'who uploaded',
  `size` bigint(20) NOT NULL COMMENT 'zip filesize bytes',
  `time` bigint(20) NOT NULL COMMENT 'unix timestamp',
  `filename` varchar(200) collate utf8_unicode_ci NOT NULL COMMENT 'remote filename',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=147240 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `tiles_misc`
-- 

CREATE TABLE `tiles_misc` (
  `last_12x` bigint(20) NOT NULL,
  `last_12y` bigint(20) NOT NULL,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `last_user` varchar(30) collate utf8_unicode_ci NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Table structure for table `tiles_msg`
-- 

CREATE TABLE `tiles_msg` (
  `id` bigint(20) NOT NULL auto_increment,
  `text` varchar(200) collate utf8_unicode_ci NOT NULL,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `priority` smallint(6) NOT NULL default '4' COMMENT '1=high,4=low',
  PRIMARY KEY  (`id`),
  KEY `date` (`date`,`priority`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='text logs, messages' AUTO_INCREMENT=2677 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `tiles_queue`
-- 

CREATE TABLE `tiles_queue` (
  `x` int(11) NOT NULL COMMENT 'x (z=12)',
  `y` int(11) NOT NULL COMMENT 'y (z=12)',
  `date` datetime NOT NULL COMMENT 'date request was created',
  `sent` tinyint(4) NOT NULL default '0' COMMENT 'if 1, the request has been taken by someone',
  `src` varchar(10) collate utf8_unicode_ci NOT NULL default '-',
  `priority` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`x`,`y`,`sent`),
  KEY `date` (`date`),
  KEY `sent` (`sent`),
  KEY `priority` (`priority`),
  KEY `src` (`src`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='list of tilesets that need rendering';
