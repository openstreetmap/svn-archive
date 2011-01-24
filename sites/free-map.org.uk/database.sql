CREATE TABLE annotatednodes (id serial,x INT, y INT, type VARCHAR(255), name VARCHAR(255), description TEXT);
CREATE TABLE annotations (id serial, wayid INT, text VARCHAR(255), dir INT default 0, xy geometry, type VARCHAR(255), authorised INT default 0);
CREATE TABLE panoramas (id serial, authorised INT default 0,direction FLOAT, time INT, photosession INT, userid INT, xy geometry);
CREATE TABLE users (id serial, username VARCHAR(255), password VARCHAR(255), email VARCHAR(255), k INT, active INT default 0, isadmin INT default 0);
CREATE TABLE photosessions (id serial, userid int, t int);
CREATE TABLE routes (id serial, route geometry, userid int);
