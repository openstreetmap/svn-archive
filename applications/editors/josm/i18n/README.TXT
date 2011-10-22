How to use:

Tasks:
1) Getting new translation catalog file:
Call "ant pot"
Upload the new file to Launchpad

2) Updating translations to current state (from daily updated Bazar repository):
Call "ant update" or "ant updatecore" when plugins are unwanted

3) Update translations from Launchpad download:
Call "./launchpad.pl <url>", wehre <url> is the supplied URL from launchpad.
This call always builds core and plugins, but safely can be aborted after core is completed.

NOTE:
a) Your repositories should not have any local modifications which modify string elements!
b) Always do an "svn up" for core and plugins before starting!
