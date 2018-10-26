Class:
	Run

How to Launch sample:
	- Web:
	   - Unix:
	   Type command : 'java -jar jac.jar -R . -C src:samples.jar -W
	   web src/org/objectweb/jac/samples/photos/photos.jac' in the jac root
	   directory, to launch the program with the web GUI.

	   - Windows:
	   Double-click on 'photos-web' to launch the web GUI.

	You must then open a web browser and connect on the URL :
	'http://localhost:8088/org/objectweb/jac/web'
	
	- Swing:
	   - Unix:
	   Type command : 'java -jar jac.jar -R . -C src:samples.jar -G
	   main src/org/objectweb/jac/samples/photos/photos.jac' in the jac root
	   directory, to launch the program with the swing GUI.

	   - Windows:
	   Double-click on 'photos-swing' to launch the Swing GUI.

Description:

	This sample is a photo album, used to stock and visualize photos.


Users guide:

	This sample uses the authentication and user aspects, so
	you'll have to enter a user and a password in order to be
	allowed to use it. In order to logg in as the administrator,
	use "admin" and "admin" as login and password. You will then
	be able to create new users: select the "users" entry from the
	"View" menu, and click on the add button.

	All users have a profile. The "Profiles" entry in the "View"
	menu allows the administrator to configure the rights granted
	to users of a profile. See the UserAC's documentation for more
	infos. 

	The default configuration allows the administrator to do
	anything. Users can view and add pictures, and the owner of a
	picture can modify it.

---

Class:
	Bench

How to launch sample:
	Just type command :
	'java -classpath $CLASSPATH jac.samples.photos.Bench'

Description:
	A benchmark which creates lots of new photos and displays
	execution time used to do it.
	It then iterates on these photos and displays execution time.
