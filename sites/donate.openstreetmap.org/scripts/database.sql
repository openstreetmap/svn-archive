CREATE TABLE IF NOT EXISTS `currency_rates` (
  `currency` char(3) NOT NULL,
  `rate` float NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`currency`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `donations` (
  `uid` int(10) unsigned NOT NULL auto_increment,
  `amount` double(8,2) NOT NULL,
  `currency` char(3) NOT NULL,
  `anonymous` tinyint(1) NOT NULL,
  `processed` tinyint(1) NOT NULL default '0',
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `name` varchar(64) NOT NULL,
  `comment` varchar(255) NOT NULL,
  PRIMARY KEY  (`uid`),
  KEY `processed` (`processed`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=15 ;

CREATE TABLE IF NOT EXISTS `paypal_callbacks` (
  `uid` int(10) unsigned NOT NULL auto_increment,
  `amount` double(8,2) NOT NULL,
  `currency` char(3) NOT NULL,
  `status` varchar(32) NOT NULL,
  `donation_id` int(10) unsigned NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `callback` text NOT NULL,
  PRIMARY KEY  (`uid`),
  KEY `donation_id` (`donation_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=9 ;
