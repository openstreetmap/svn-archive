== Compiling Potlatch ==

=== Requirements ===

You will need Ming (www.libming.net) and the Perl bindings.

The unit used by Ming to specify line widths has changed in recent versions. You will need to alter the first line of potlatch_assets.pl (all the sprites) depending which version you are using.

If you are using current Ming:		$cw=1/20;
If you are using an old Ming:		$cw=1;

=== Against a local installation ===

If you have the Rails port (full OSM codebase) set up on your machine, it's best to run Potlatch directly against that.

To compile Potlatch, simply run:
	perl potlatch.pl /path/to/rails_port/public/potlatch/potlatch.swf
	
If you'd like to enable a debug window, add --trace:
	perl potlatch.pl --debug /path/to/rails_port/public/potlatch/potlatch.swf

You can write to the debug window from the Potlatch source like this:
	_root.chat.text="Some text";
	
=== Against the main OSM server ===

You can also run Potlatch locally, talking to the main openstreetmap.org server. In this case, to compile it, run:
	perl potlatch.pl --absolute potlatch.swf
	
This will compile the SWF to the current source directory. Then, to run Potlatch, open test.html in your web browser.

Note that Flash Player has (really annoying) security restrictions that may prevent your local machine talking to a remote server, and vice versa. So on my OS X machine:
	
	file:///Users/richard/Sites/potlatch/test.html - DOESN'T WORK
	http://127.0.0.1/~richard/potlatch/test.html - works
	http://localhost/~richard/potlatch/test.html - works
	

== Authentication ==

When Potlatch is run on the OSM site (or your local install of the same), it gets a token from Rails which it uses to authenticate.

If you're running it locally without a full install of the Rails port, you won't have a token. Happily, Potlatch can also authenticate by user ID and password.

To do this, find the reference to 'token' in test.html:
    fo.addVariable('token','VeSiWDkofUzgLLx5DgAU4rh1Wn23ro');

and change it to:
    fo.addVariable('token','your_username:your_password');



Richard Fairhurst
richard@systemeD.net
