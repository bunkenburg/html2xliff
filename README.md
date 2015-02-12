# score-server

Performance considerations

	- Unnecessary iteration and looping is avoided in sorting the highscores list.
	- Unnecessary copying of data is avoided in concurrency strategies.
	
Concurrence considerations

	- Highscores list is being kept in order in asynchronous mode, ie. it is always in correct order.
	- Iterators are avoided to make sure ConcurrentModificationException would not occur.
	- Concurrent methods are marked as 'synchronous' to ensure thread safe data access.
	
Running

	java -jar score-server.jar
	
Other

	

