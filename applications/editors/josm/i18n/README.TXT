How to use:

Prerequisites:
a) You must have installed ant, perl, libwww-perl, libterm-readkey-perl, gettext, bzr
b) You must register your SSH key with Launchpad. See https://launchpad.net/people/+me
c) You must inform bzr of your Launchpad ID. See "bzr help launchpad-login".

Tasks:
1) Get new translation catalog file:
Call "ant pot"
Afterwards the new file should be uploaded to Launchpad (see (5) below)

2) Update translations to current state (from daily updated Bazaar repository):
Call "ant update" or "ant updatecore" when plugins are unwanted
NOTE: The Bazaar repository is updated once a day. For most recent data try (6) and (3) instead.

3) Update translations from Launchpad download file:
Call "./launchpad.pl <url>", where <url> is the supplied URL from launchpad.
This call always builds core and plugins, but safely can be aborted after core is completed.

4) Check whether translations have string errors (e.g. wrong ' count or illegal {})
Translation data (.po) must exist!
Call "ant test".

5) Upload POT file to launchpad:
Call "./launchpad.pl upload". You need to set login information (see file how to do) or enter them when running.
Can also be issued by hand on the Launchpad web page.

6) Request launchpad po file download:
Call "./launchpad.pl download". You need to set login information (see file how to do) or enter them when running.
Can also be issued by hand on the Launchpad web page.
After some time a mail is sent with a URL. With this URL step (3) can be done.

NOTE:
a) Your repositories should not have any local modifications which modify string elements!
b) Always do an "svn up" for core and plugins before starting!
