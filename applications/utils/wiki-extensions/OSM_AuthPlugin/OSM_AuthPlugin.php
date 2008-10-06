<?php
/**
 * OSM_AuthPlugin - OpenStreetMap API Mediawiki Authentication Plugin
 *
 * Authenticate users against the OpenStreetMap user database instead 
 * of or in addition to the MediaWiki internal user database.
 * 
 * Initially by Chris Jackson, except for doc comments.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

if ( !defined( 'MEDIAWIKI' ) ) {
	die( 'This file is a MediaWiki extension.' );
}

require( 'AuthPlugin.php' );  	// API to extend
require( 'HTTP/Request.php' );	// PEAR include - adjust to suit

$wgExtensionCredits['other'][] = array(
	'name'			=> 'OpenStreetMap authentication',
	'author'		=> 'Chris Jackson',
	'svn-date'		=> '$LastChangedDate: 2008-07-23 22:20:05 +0100 (Wed, 23 Jul 2008) $',
	'svn-revision'	=> '$LastChangedRevision: 37977 $',
	'url'			=> 'http://wiki.openstreetmap.org/index.php/OSM_AuthPlugin',
	'description'	=> 'Allows shared sign-on with OpenStreetMap. Or so we hope.'
);

class OSMAuthPlugin extends AuthPlugin {
	/**
	 * Check whether there exists a user account with the given name.
	 * The name will be normalized to MediaWiki's requirements, so
 	 * you might need to munge it (for instance, for lowercase initial
 	 * letters).
 	 *
 	 * @param $username String: username.
 	 * @return bool
 	 * @public
 	 */
 	function userExists( $username ) {
		// 0929-4 authenticate() depends on this function
 		// and is not called unless this returns true.
 		// Until we have a meaningful way of establishing
 		// whether or not a user account eixists,  we must:  
 		return true; 
 		// return false;
 	}
 
 	/**
 	 * Check if a username+password pair is a valid login.
 	 * The name will be normalized to MediaWiki's requirements, so
 	 * you might need to munge it (for instance, for lowercase initial
 	 * letters).
 	 *
 	 * @param $username String: username.
 	 * @param $password String: user password.
 	 * @return bool
 	 * @public
 	 */
 	function authenticate( $username, $password ) {
 		
		$auth_url = 'http://www.openstreetmap.org/api/0.5/user/details';
		$params = array ( "method" => "HEAD",
 						  "allowRedirects" => TRUE );

  		$req = new HTTP_Request( $auth_url, $params );
 		$req->setBasicAuth( $username, $password );
		$req->sendRequest();
 
		// Request will return 200 if successfull, and generally 401 if it fails
		$code = $req->getResponseCode();
		$body = $req->getResponseBody(); 
 		if ( $code == 200 ) {
			// XML body extract email
			
			return true;
		} else {
 			return false;
		}
  		return false;
 	}
 
 	/**
 	 * Modify options in the login template.
 	 *
 	 * @param $template UserLoginTemplate object.
 	 * @public
 	 */
 	function modifyUITemplate( &$template ) {
 		# Override this!
 		$template->set( 'usedomain', false );
 	}   
 
 	/**
 	 * Set the domain this plugin is supposed to use when authenticating.
 	 *
 	 * @param $domain String: authentication domain.
 	 * @public
 	 */
 	function setDomain( $domain ) {
 		$this->domain = $domain;
 	}
 
 	/**
 	 * Check to see if the specific domain is a valid domain.
 	 *
 	 * @param $domain String: authentication domain.
 	 * @return bool
 	 * @public
 	 */
 	function validDomain( $domain ) {
 		# Override this!
 		return true;
 	} 
 
 	/**
 	 * When a user logs in, optionally fill in preferences and such.
 	 * For instance, you might pull the email address or real name from the
 	 * external user database.
 	 *
 	 * The User object is passed by reference so it can be modified; don't
 	 * forget the & on your function declaration.
 	 *
 	 * @param User $user
 	 * @public
 	 */
 	function updateUser( &$user ) {
 		# Override this and do something
		
		// Can I override mName? with the display_name from OSM Auth API?
		
 		return true;
 	}
 
 	/**
 	 * Return true if the wiki should create a new local account automatically
 	 * when asked to login a user who doesn't exist locally but does in the
 	 * external auth database.
 	 *
 	 * If you don't automatically create accounts, you must still create
 	 * accounts in some way. It's not possible to authenticate without
 	 * a local account.
 	 *
 	 * This is just a question, and shouldn't perform any actions.
 	 *
 	 * @return bool
 	 * @public
 	 */
 	function autoCreate() {
 		return true;
 	}
 
 	/**
 	 * Can users change their passwords?
 	 *
 	 * @return bool
 	 */
 	function allowPasswordChange() {
 		return false;
 	}
 
 	/**
 	 * Set the given password in the authentication database.
 	 * As a special case, the password may be set to null to request
 	 * locking the password to an unusable value, with the expectation
 	 * that it will be set later through a mail reset or other method.
 	 *
 	 * Return true if successful.
 	 *
 	 * @param $user User object.
 	 * @param $password String: password.
 	 * @return bool
 	 * @public
 	 */
 	function setPassword( $user, $password ) {
 		return true;
 	}
 
 	/**
 	 * Update user information in the external authentication database.
 	 * Return true if successful.
 	 *
 	 * @param $user User object.
 	 * @return bool
 	 * @public
 	 */
 	function updateExternalDB( $user ) {
 		return true;
 	}
 
 	/**
 	 * Check to see if external accounts can be created.
 	 * Return true if external accounts can be created.
 	 * @return bool
 	 * @public
 	 */
 	function canCreateAccounts() {
 		return false;
 	}
 
 	/**
 	 * Add a user to the external authentication database.
 	 * Return true if successful.
 	 *
 	 * @param User $user - only the name should be assumed valid at this point
 	 * @param string $password
 	 * @param string $email
 	 * @param string $realname
 	 * @return bool
 	 * @public
 	 */
 	function addUser( $user, $password, $email='', $realname='' ) {
 		return true;
 	}
 
 
 	/**
 	 * Return true to prevent logins that don't authenticate here from being
 	 * checked against the local database's password fields.
 	 *
 	 * This is just a question, and shouldn't perform any actions.
 	 *
 	 * @return bool
 	 * @public
 	 */
 	function strict() {
 		return false;
 	}
 
 	/**
 	 * Check if a user should authenticate locally if the global authentication fails.
 	 * If either this or strict() returns true, local authentication is not used.
 	 *
 	 * @param $username String: username.
 	 * @return bool
 	 * @public
 	 */
 	function strictUserAuth( $username ) {
 		return false;
 	}
 
 	/**
 	 * When creating a user account, optionally fill in preferences and such.
 	 * For instance, you might pull the email address or real name from the
 	 * external user database.
 	 *
 	 *
 	 * The User object is passed by reference so it can be modified; don't
 	 * forget the & on your function declaration.
 	 *
 	 * @param $user User object.
 	 * @param $autocreate bool True if user is being autocreated on login
 	 * @public
 	 */
 	function initUser( &$user, $autocreate=false ) {
 		# Override this to do something.
 	}
 
 	/**
 	 * If you want to munge the case of an account name before the final
 	 * check, now is your chance.
 	 */
 	function getCanonicalName( $username ) {
		return $username;
	}
}
?>