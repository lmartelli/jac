Class:
	Run

How to Launch sample:
	- Web:
	   - Unix:
	   Type command : 'java -jar jac.jar -R . -C src:samples.jar -W
	   contacts-gui src/org/objectweb/jac/samples/contacts/contacts.jac' in the
	   jac root directory, to launch the program with the web GUI.

	   - Windows:
	   Double-click on 'contacts-web' to launch the web GUI.

	You must then open a web browser and connect on the URL :
	'http://localhost:8088/org/objectweb/jac/contacts-gui', where 'localhost' is
	your ip.
	
	- Swing:
	   - Unix:
	   Type command : 'java -jar jac.jar -R . -C src:samples.jar -G
	   contacts-gui src/org/objectweb/jac/samples/contacts/contacts.jac' in the
	   jac root directory, to launch the program with the swing GUI.

	   - Windows:
	   Double-click on 'contacts-swing' to launch the Swing GUI.

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

