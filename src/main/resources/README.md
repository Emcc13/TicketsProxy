# Ticket Plugin Proxy v0.6.3 for BungeeCoord 1.16-R0.3

A plugin to manage player tickets. Requires Ticket Plugin Server v0.6.0 or above on every server in the network

## Commands

- ticketsreload: reload config for proxy plugin
- ticketscleandb: remove closed and read tickets and renumber remaining tickets

## Config

- channel: channel to communicate with the server plugin, needs to be the same in the server config (default: channel:name)
- db
  - host: mysql server ip (default: 127.0.0.1)
  - port: mysql server port (default: 3306)
  - database: database name (default: minecraft)
  - table: table name for the tickets (default: tickets)
  - username: user to connect with the database (default: root)
  - password: password for given user (default: 123456)
  - updated: table updated to scheme of v0.6.0 or higher (default: false, should be set to true by hand if the database is already updated)
- ticket_type_notify: player is notified if the type contained is this setting (default: "p")
- ticket_count_ignore: ticket types ignored when notifying team members about open tickets (default: "h")
- db.failure_message: shown if the connection to the database is broken (certain commands trigger a reconnect otherwise a reconnect is triggered after sometime)
- coordinate_format: format to round coordinates (default: %.1f)
- tickets
  - format: how to display tickets for the tickets command
    - open: 
    - claimed
    - closed
    - read
  - page
    - limit: maximum amount of tickets shown at once
    - header: message shown above each 'page'
  - PlayerPage.header: message shown above each 'page' of tickets for given player
  - info
    - create: notification that a player has created a ticket
    - claim: notification that a ticket has been claimed
    - unclaim: notification that a ticket has been refused
    - close: notification that a ticket has been closed
    - closeFail: notification that a ticket with given id is already closed or does not exist
    - spam: notification that someone tries to spam the ticket system
    - read: notification that a player has read the answer to a closed ticket

- ticket
  - format: how to display ticketsfor the ticket command
    - open
    - read
    - tickettype: replacement for the ticket types in the ticket formats
    - link_regex: regular expression to detect urls in ticket texts and create openurl actions for the on click event
  - color.char: ColorCode char
  - page.info: header to switch between 'pages' of /ticket list
  - info
    - create: notification message for a player that a ticket has been created
    - close: notification that a ticket has been closed
    - read: notification that the answer of the ticket has been read
  - spam_distance: minimum distance between two tickets with the same text

- config
  - reload
    - op: list of UUIDs which are allowed to reload the config
  - cleandb
    - op: list of UUIDs which are allowed to clean the database

- no_ticket_with_id: player tried to see ticket with id # which does not exist
- join.ticket: notification on join event
  - open: about open tickets
  - unread: about unread tickets


## Databse

Table update:
  - ALTER TABLE tickets CHANGE living ticketType CHAR(1) NOT NULL;
  - UPDATE tickets SET ticketType="p" WHERE ticketType=1;
  - UPDATE tickets SET ticketType="c" WHERE ticketType=0;
  - UPDATE tickets SET ticketType="h" WHERE ticketType=2;

-----

CREATE TABLE IF NOT EXISTS tickets  
(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  
enquirer CHAR(36) NOT NULL,  
ticketType CHAR(1) NOT NULL,  
request TEXT NOT NULL,  
requestDate TIMESTAMP,

server CHAR(255) NOT NULL,  
world CHAR(255) NOT NULL,  
X DOUBLE NOT NULL,  
Y DOUBLE NOT NULL,  
Z DOUBLE NOT NULL,  
yaw FLOAT NOT NULL,  
pitch FLOAT NOT NULL,

claimedBy CHAR(36),  
claimingD TIMESTAMP,  
responder CHAR(36),  
answer TEXT,  
answerD TIMESTAMP,  
readD TIMESTAMP);

