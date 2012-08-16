Script: 		Ajax Availability Calendar
Script Web: 	www.ajaxavailabilitycalendar.com
Author: 		Chris Bolson
Autor Web: 		www.cbolson.com

Version: 		Version 3.03.07
Date Release:	2012-03-08
File: 			Readme

Instructions:
1. Unzip the file (done if you are reading this)
2. Upload all the files to your server, possibly in a subdirectory called "calendar"
3. Open your browser to the calendar directory and run the install.php file.
3.1 Note - the ac-config.inc.file will need write permissions (chmod 777) - alternatively you 
can modify it manually on your server.
4. Follow the install instructions. This file preforms the following actions:
 - Check db connection - if fails, it checks write permission on ac-config.inc.php file 
 then shows a form for indicating your db connection data
 - Once config.inc.php has been configured, it writes the db tables and default data to the database.
 - Shows basic calendar configuration parameters (url, title, num months etc) - these can be changed from the admin panel.
 
5.  Once install is complete, remove the instal.php file from the ftp.
6. Login to admin (http://www.your_url.com/calendar/ac-admin/)
 	user: admin
 	password: demo
7. define your calendar items.
8. If you are happy and you like it, feel free to make a donation :)
9. Lang folder and files need write permissions (chmod 777) to be able to be administrated via admin


How the calendar works:

UPDATING DATE STATES
In the administration version you can update the individual dates by clicking on them.
Clicking on a data calls a php script via ajax that updates the date to the next state in 
the order as defined in the "states" admin section (see below).
Alternatively, you can define a "default" state to be set as each date is clicked.  
This allows you to update the states quicker as it avoids having to cycle through all the states.


POSSIBLE DATE STATES
You can define as many "states" (booked, booked am, special offer etc) as required.
These can be ordered, this order is used both in the key and in the order in which the state is 
changed when you cycle through the states (see above)
With each state you must assign a "class" - you then need to modify the calendar css file 
to add this class and define the background color and image if required. 
NOTE - in future versions I plan to add the possibility to define the background color in admin.
The states can also be "deactivated" if you would do this rather than delete them completely from the database.


CALENDAR ITEMS (ie your calendar)
You can define as many items as you require.
Note - the "bookings" table only holds information for dates that have a 
booking state - ie "available" days are not stored in the database.
For ease of use, you need to assign a "friendly" name to your item.  
Ideally this should reflect the actual item in your web 
- eg.  If your calendar is for your mobile home on a camp site, you should call your 
calendar "mobile home" (obvious, but thought I would mention it)


USER PROFILE
Basic admin access data - your username and password.


CALENDAR CONFIGURATION
Here you can define the various calendar options such as:
 - Calendar Title
 - Calendar Url
 - Number of months to show (max 12 but you can hack the code to show more)
 - Start Display Day (sunday or monday)
 - Set if previous dates can be modified - if set to "no" this also "fades out" 
 the date previous to the current dates on the calendar display)
 - Default language (see below for language details)
 
AC_LANGUAGES
NOTE - Languages MUST be added via admin as the code needs to add language columns to the database.
This script can work with any number of languages.
The field columns and a new language file are created automatically (lang dir needs write permissions)
You can also edit the languages files via admin or directly in the file.


ADMIN USERS
From Admin you can add as many users as you like.
These users will NOT be able to modify calendar states (booked, provisional etc)
They will ONLY be able to create, see and modify their own calendars. 

MODIFYING COLORS
For all color changes you must make them in the avail-calendar.css file. 

INCLUDING CALENDAR IN YOUR WEB
There are many ways you could include the calendar in your web.
Perhaps the simplest methods would be to use a modal or pop-up window to show it on request or to use an iframe to embed it in your website.
eg. 
<iframe src ="/calendar/index.php" width="600" height="300"></iframe>

eg. for specific calendar item:
<iframe src ="/calendar/index.php?id_item=4" width="600" height="300"></iframe>

eg specific item and number of months (overrides default setting):
<iframe src ="/calendar/index.php?id_item=4&num_months=5" width="600" height="300"></iframe>

However, the ideal method would be to include the code directly into your source code via a php include.
If you opt for this method you will need a minimum knowledge of php.