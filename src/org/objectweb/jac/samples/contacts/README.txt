Class:
	Run

How to Launch sample:
	- Web:   
	Type command : 'jac -W contacts-gui contacts.jac'
	in the contacts directory, to launch the program with the web GUI.
	Then open a web browser and connect on the URL :
	'http://localhost:8088/org/objectweb/jac/contacts-gui', where 'localhost' is
	your ip.
	
	- Swing:
	Type command : 'jac -G contacts-gui contacts.jac' in the
	contacts directory, to launch the program with the swing GUI.

Description:
	This sample is a contact list, showing how the GUI, persistence
	and session components can work.

---

Class:
	Create

How to launch sample:
	Just type command :
	'java -classpath $CLASSPATH jac.samples.contacts.Create <n>'.
	<n> is the number of contacts to create.

Description:
	A benchmark to create a bunch of contacts easily.
	Useful if you don't want to create contacts one by one to test
	the sample.