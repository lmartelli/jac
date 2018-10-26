Class:
	Run

How to Launch the sample:
        
        The default configuration of this sample uses distribution
        aspects, so you'll need to start 2 slave servers to try it:

	    go 2

	The "go" script is located in the "scripts" directory.

	Then depending on the GUI you want to use:

	   - Web: Type command : 'jac -W default calcul.jac' in
	   the calcul directory, to launch the program with the
	   web GUI.  Then open a web browser and connect on the
	   URL : 'http://localhost:8088/org/objectweb/jac/default'

	   - Swing: Type command : 'jac -G default photos.jac' in
	   the calcul directory, to launch the program with the
	   swing GUI.

	See section "Using AOP to program distributed applications" of
	the tutotial (http://jac.aopsys.com/doc/tut_programming.html#prog_dist) 
	for more information.


Description:

	The purpose of this very simple sample is to demonstrate JAC's
	distributed aspects capabilities. The default configuration
	shows how to use the load-balancing aspect to split the work
	load on two servers.

	The architecture basically looks like this:

		       	   backends
		      	    ,----.
	     frontend  	,---| s1 |
              ,----.    |   `----'
              | s0 |----+
              `----'    |   ,----.
                        `---| s2 |
                            `----'
         
        The frontend shows a GUI (swing or web) to the user and
        dispatches requests to the backends. The load-balancing aspect
        is used on all methods but modifiers to forward each request
        to one different backend each time. The broadcasting aspect is
        used on modifier methods to call broadcast request on all
        backends so that the state of objects are the same on all of
        them. 

	The tracing aspect allows you to see which methods are called
	on each site.