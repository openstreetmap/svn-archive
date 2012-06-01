How to use:

Tasks:
1) Getting new translation catalog file:
Call "ant pot"
Upload the new file to Launchpad (see below)

2) Updating translations to current state (from daily updated Bazar repository):
Call "ant update" or "ant updatecore" when plugins are unwanted

3) Update translations from Launchpad download:
Call "./launchpad.pl <url>", where <url> is the supplied URL from launchpad.
This call always builds core and plugins, but safely can be aborted after core is completed.

4) Check whether translations have string errors (e.g. wrong ' count or illegal {})
Translation data (.po) must exist!
Call "ant test".

5) Upload POT file to launchpad:
Call "./launchpad.pl upload". You need to set login information (see file how to do) or enter them when running.

6) Request launchpad po file download:
Call "./launchpad.pl download". You need to set login information (see file how to do) or enter them when running.

NOTE:
a) Your repositories should not have any local modifications which modify string elements!
b) Always do an "svn up" for core and plugins before starting!
