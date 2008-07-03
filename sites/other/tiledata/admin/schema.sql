
-- 
-- Node positions
-- 

CREATE TABLE `nodepos` (
  `id` bigint(20) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `tile` char(11) character set ascii collate ascii_bin NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `tile` (`tile`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

-- 
-- Way data
-- 

CREATE TABLE `waydata` (
  `id` bigint(20) NOT NULL,
  `data` blob NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COLLATE=ascii_bin;

-- --------------------------------------------------------

-- 
-- Way locations
-- 

CREATE TABLE `wayloc` (
  `way` bigint(20) NOT NULL,
  `tile` char(11) character set ascii collate ascii_bin NOT NULL,
  KEY `tile` (`tile`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
