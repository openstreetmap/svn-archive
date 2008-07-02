Concepts:

To create a user account on the server, specify a PIN that will be your password, and it will return a user ID.

To create a group, specify the name, and two PINs:
 * read PIN - anyone who knows this will be able to view the position of people in the group
 * write PIN - anyone who knows this will be able to publish their position info on the group

To make a group public, set read PIN to 0

To upload your position, send your own ID/PIN, the group number to publish to, and the group's write PIN.  You must pubish "to a group" (create one if necessary)

To set your nickname within a group, send your own ID/PIN, the group number this nickname applies to, and the group's write PIN.  So you can publish to several groups using several identities, using the same user account.

To download a group, send the group number and the group's read PIN.  It will return a list of User,Lat,Lon.  Currently this returns user IDs, but it should return nicknames only.

That is all. Enjoy.  Future concepts are in roadmap.txt.  API is best explored by going to the website and playing with the HTTP requests used by the forms:

http://dev.openstreetmap.org/~ojw/pos/


Install guide: just put into a website with PHP, modify the line which includes connect.php so that it connects to your MySQL database, and use the attached schema.sql to generate the blank database.
