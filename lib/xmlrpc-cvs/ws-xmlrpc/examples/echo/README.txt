This is a simple example for implementing XML-RPC clients and servers
using Apache XML-RPC.

The client calls the server with a single string parameter, and the
server passes the paramter back to the client. There are two client
implementations: a synchronous one and an asynchronous one.

Compiling and running the demo
------------------------------

Open a shell window and change to the directory containing the demo
files. Make sure that both the current directory and the xmlrpc.jar
file are in your CLASSPATH. Compile the Java files by entering

  javac *.java

If this does not succeed, make sure your CLASSPATH is set correctly
as described above.

Start the the Server by entering

  java Server 8080

where 8080 is the port the Server will listen on.

In a second shell window, run one of the following commands, depending
on whether you want to run the synchronous or the asynchronous client:

  java Client http://localhost:8080/
  java AsyncClient http://localhost:8080/

If everything went ok, each line you enter into the client shell should
be echoed back from the server.