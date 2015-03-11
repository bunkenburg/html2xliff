# html2xliff

Technical solutions

	- dom4j library is used for parsing because it is flexible, performant and memory-efficient
	- dom4j API for parsing and building documents makes the code human readable and easily maintainable
	- Recursion algorithm is perfect for traversing trees of predictable depth
	
Performance considerations

	- Parsing should require O(n) time and O(n) memory because the tree is traversed systematically from bottom to top after it's created and new memory is allocated only for the XLIFF DOM which takes up roughly the same amount of memory as the original one.
	
Running

	java -jar html2xliff.jar ["This is <b>very</b> important"]
	
Testing

	java -cp html2xliff.jar org.junit.runner.JUnitCore test.leninra.html2xliff.Html2XliffTest

	

