<?php
 /* 
  * OSMAuthPlugin
  * Authenticate users against the OpenStreetMap user database instead 
  * of or in addition to the MediaWiki internal user database.
  * 
  * Initially by Chris Jackson, except for doc comments.  Licensed as appropriate.
  * Wiki-formatted version may have broken indentation
  */
 require( 'AuthPlugin.php' );  	// API to extend
 require( 'HTTP/Request.php' );	// PEAR include - adjust to suit
 
 if( !defined( 'MEDIAWIKI' ) )
 die();
 
 $wgExtensionCredits['other'][] = array(
     'name' 			=> 'OpenStreetMap authentication',
     'description'	=> 'Allows shared sign-on with OpenStreetMap.  Or so we hope.',
 	'version' 		=> '20080929-5',
     'author' 		=> 'Chris Jackson',
     'url'			=> 'http://wiki.openstreetmap.org/index.php/OSM_AuthPlugin'
 );
 
 // helper function for installations where lcfirst() does not exist
 // doc comments from PHP manual
 
 if( !function_exists( 'lcfirst' ) ) {
     /**
      * Make a string's first character lowercase 
      *
      * @param string $str The input string.
      * @return string the resulting string.
      */
     function lcfirst( $str ) {
         $str[0] = strtolower( $str[0] );
         return (string) $str;
     }
 }
 
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
 
 		$params = array ( "method" => "HEAD",
 						  "user" => $username,
 						  "pass" => $password );
 
 		$auth_url = "http://www.openstreetmap.org/api/0.5/user/details";
 
 		$req = new HTTP_Request( $auth_url, $params );
 		$req->sendRequest();
 
 		// Request will return 200 if successfull, and generally 401 if it fails	
 		$code = $req->getResponseCode();
 		$body = $req->getResponseBody(); 
 
 		if ( $code == 200 ) {
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