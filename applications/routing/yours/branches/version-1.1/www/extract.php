<?php
/**
 * Main wiki script; see docs/design.txt
 * @package MediaWiki
 */
$wgRequestTime = microtime(true);

# getrusage() does not exist on the Microsoft Windows platforms, catching this
if ( function_exists ( 'getrusage' ) ) {
	$wgRUstart = getrusage();
} else {
	$wgRUstart = array();
}

unset( $IP );
@ini_set( 'allow_url_fopen', 0 ); # For security...

if ( isset( $_REQUEST['GLOBALS'] ) ) {
	die( '<a href="http://www.hardened-php.net/index.76.html">$GLOBALS overwrite vulnerability</a>');
}

# Valid web server entry point, enable includes.
# Please don't move this line to includes/Defines.php. This line essentially
# defines a valid entry point. If you put it in includes/Defines.php, then
# any script that includes it becomes an entry point, thereby defeating
# its purpose.
define( 'MEDIAWIKI', true );

# Load up some global defines.
# 2006-10-15 Wzl: kPathToMediaWiki must be defined before this line (externally is probably best)
require_once( kPathToMediaWiki.'includes/Defines.php' );

# LocalSettings.php is the per site customization file. If it does not exit
# the wiki installer need to be launched or the generated file moved from
# ./config/ to ./
if( !file_exists( kPathToMediaWiki.'LocalSettings.php' ) ) {
    echo "Can't access LocalSettings.php";
}

# Include this site setttings
require_once( kPathToMediaWiki.'LocalSettings.php' );
# Prepare MediaWiki
require_once( kPathToMediaWiki.'includes/Setup.php' );

# Initialize MediaWiki base class
require_once( kPathToMediaWiki."includes/Wiki.php" );
$mediaWiki = new MediaWiki();

wfProfileIn( 'main-misc-setup' );
OutputPage::setEncodings(); # Not really used yet


$title = kEmbeddedPagePrefix.$page;
$action = 'render'; // (2006-10-15 Wzl) this may be redundant; check.
//$wgTitle = $mediaWiki->checkInitialQueries( $title,$action,$wgOut, $wgRequest, $wgContLang );
$wgTitle = Title::newFromURL( $title );
if ($wgTitle == NULL) {
	unset( $wgTitle );
}
wfProfileOut( 'main-misc-setup' );

# Setting global variables in mediaWiki
$mediaWiki->setVal( 'Server', $wgServer );
$mediaWiki->setVal( 'DisableInternalSearch', $wgDisableInternalSearch );
$mediaWiki->setVal( 'action', $action );
$mediaWiki->setVal( 'SquidMaxage', $wgSquidMaxage );
$mediaWiki->setVal( 'EnableDublinCoreRdf', $wgEnableDublinCoreRdf );
$mediaWiki->setVal( 'EnableCreativeCommonsRdf', $wgEnableCreativeCommonsRdf );
$mediaWiki->setVal( 'CommandLineMode', $wgCommandLineMode );
$mediaWiki->setVal( 'UseExternalEditor', $wgUseExternalEditor );
$mediaWiki->setVal( 'DisabledActions', $wgDisabledActions );

$wgArticle = $mediaWiki->initialize ( $wgTitle, $wgOut, $wgUser, $wgRequest );

// (Wzl 2006-10-15) This bit is from MediaWiki::finalCleanup() in includes/Wiki.php, with one line replaced:
		wfProfileIn( 'MediaWiki::finalCleanup' );
		$mediaWiki->doUpdates( $wgDeferredUpdateList );
		$mediaWiki->doJobs();
		$wgLoadBalancer->saveMasterPos();
		# Now commit any transactions, so that unreported errors after output() don't roll back the whole thing
		$wgLoadBalancer->commitAll();
//		$wgOut->output();
$wgOut->out($wgOut->mBodytext);
		wfProfileOut( 'MediaWiki::finalCleanup' );

# Not sure when $wgPostCommitUpdateList gets set, so I keep this separate from finalCleanup
$mediaWiki->doUpdates( $wgPostCommitUpdateList );

$mediaWiki->restInPeace( $wgLoadBalancer );
?>
