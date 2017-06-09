
—————————————————

How to run
—————————————————
use javac to compile java files in folder P2, and then use java P2 host1 (host1 is the hostName) to run program.

———————————————
Input format
———————————————
java P2 host1  

add (host1, 172.21.68.102, 65022)(…..)(….) 

add command: which is used to add hosts into serverList. 
“add” must be in lowercase. All the parameters have to be wrapped inside a pair of parentheses. “add” can have one or more parameters. 

out(“abc”, 3)   

out command: store tuple(“abc”, 3) into tuple space.
“out” must be lowercase. and the data type of parameters can be string, integer, float. It can also have variables

in(“abc”, 3) 
in command: match and remove tuple in tuple space. 
The parameters following after “in” must match requirement as above.

rd(“abc”, 3) 
rd command: find tuple in tuple space. (Not remove)

delete(host1, host2,...) 
delete command: delete the host, then data on this host will be cleared. User can also add this host later. Make sure there are at least two hosts when you delete host.

kill(host1) only one
kill command: make host1 crash

reboot(host1) only one
reboot command: reboot the host which is crashed.

—————————————————
Project introduction: 
—————————————————
This is a distributed Linda platform implemented in java. Linda provides a conceptually “global” tuple space (TS) which remote processes can access the matched tuples in TS by atomic operations(in, rd, out). 

A tuple is an ordered list of values with types(integer, float, string). A match can be exact match (value only) or variable match (in the form of ?variable_name:type) for all operations. 
.
All the tuple have a backup been stored in another host.

The “out” simply put tuples in TS, The “in” will match and remove tuples from TS, but the “rd” is not destructive. If multiple tuples are available to an “in” or “rd” call, one is selected non-deterministically. If no tuple is available, the “in” or “rd” call blocks.

user can delete and add host whenever he want.

Also, situitation that the host may be crashed has been covered.

—————————————————
Modular design
—————————————————
This project contains the following classes: P1, Client, Server,  Util

P1 class: It is the entry of the whole project. It first accept P1 command from terminal, and then start Server class and Client class. It can deal with the command

Client class: This class is used to communicate with server, send the command to the server, then server can execute and give the user what they want

Server class: Accept request from Client, and execute different commands and give client the result.

Util class: This class contains method that can be used by other three classes. This class is used for reduce redundant codes.

ConsistentHash class: This class used to deal with situation that total number of hosts is changed.

P2 class: This class is the main class, deal with the user's input.

———————————————
file/directory organizing 
———————————————
nets file: Store in path /tmp/<login>/linda/<name>/nets, which store all the information about Servers, including hostName, IP address, port number. 

tuples file: Store in path  /tmp/<login>/linda/<name>/tuples, which store all the tuples in that host. 

