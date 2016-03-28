# Socket-Programming

1.Brief Description of My Code

There are 5 classes. Server, ServerThread, BlacklistTimer, Client, ClientStreamingReadingThread.
Server: This is the main thread of the server program. The main method is an object of ServerSocket class and it listens on a specific port. Once it receive a request for new connection, it would create a new thread which handles all the following communications and the main thread continues listening to new request. Shutdown hook is implemented in the code to take care of all the business after the control-c is triggered. Basically it tells all the connected clients to close the communications and then close all the server side sockets. 

ServerThread: This is the program that operates communications with clients. This is the new thread mentioned above. Basically it first ask the client to login. There are many cases here such as invalid username, wrong password, ip-username pair already blocked, if the passwords are wrong for 3 consecutive times. I have corresponding logic to tackle each situation. Then there would be the offline message part upon successful, which would be explained in the extra feature part. Then there would be the command part. All 6 commands are supported. The first 5 commands are parsed by regular expressions and the last command “logout” is implemented with the loop conditions. If the command sent by client doesn’t match any supported commands, a remainder of all the commands and corresponding grammars are displayed for the client.

BlacklistTime: This is the class, which extends the TimerTask, implements the blacklist for ip-username pair. 

Client: This is main program for client side. Basically it constructs a client side socket to connect to the server. This is the main thread which takes care of the output stream of the client socket. This program could be terminated by two conditions either the client type “logout” or the server sends “logout”. The latter case is when the server is terminated by control-c. Like the Server class, a shutdown hook is implemented here to handle the situation when the client terminal is terminated by control-c. It would let the Server to log out by sending the “logout” command and then close the client side socket.

ClientStreamingReadingThread: This is the class that implements another thread on the client side. This thread takes care of the incoming stream of the client side socket. If income stream and outcome stream are both handled by the main thread, there would be some problems such as the outcome stream can only display one line each time and waits for the user keyboard input. With this extra thread, messages from the server can be displayed at once how many lines they contain without the constraint by the keyboard stream.


2.Details on Deployment environment

Since the program is written in Java, the deployment environment is JVM. JDK version is 1.6.


3.Instructions on how to run my code

First use ‘make’ to compile all the files.
Then ‘java Server <port number>’ to run the server.

‘java Client <server ip address> <port number>’ to run the client.


4.Sample commands to invoke my code

After successfully login, some commands are those 6 supported commands.
All the first 5 commands could handle some extra space. For example, in “send <username> <message>”, before send there could be multiple space, between send and <username> and between <username> and <message> there could be multiple space. The “logout” command should be exactly as it spells without any space to make sure it is not easily invoked and is only invoked when the user is cautious about it. I also decide to allow users to send message to themselves, which may give them a chance to see a message look like without any command keyword. 

5. Extra Feature:


I implemented the offline message service. Basically this service is automatically invoked upon successful login for any user. If there is no offline message at all, nothing would be displayed. If there are offline messages, the user would receive something like: “Here are all the offline messages for you. Some messages. End of offline message.”
Offline messages don’t include those broadcast messages but only those messages sent by two send commend. I have this thought because: 1. When people are broadcasting, since they know who is online by using command ‘who’, they may be not intended to send the message to this offline user at the very beginning. 2. Another concern is that saving messages are potentially expensive, this may not be a wise way to save so many messages. 3. The last concern is for the user who just get online. If so many messages just pump in front of you, it may not be a pleasant experience. 
