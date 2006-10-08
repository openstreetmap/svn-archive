CREATE TABLE `places2` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `type` varchar(20) NOT NULL default '',
  `lat` float NOT NULL default '0',
  `lon` float NOT NULL default '0',
  `osm_size` float NOT NULL default '0' COMMENT 'KiB',
  `osm_date` int(11) NOT NULL default '0' COMMENT 'days since epoch',
  `img_exists` tinyint(4) NOT NULL default '0',
  `renderer` varchar(20) NOT NULL default '' COMMENT 'username of person who last rendered the image',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
