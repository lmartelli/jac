
WELCOME
=======

Welcome to the JAC project!

JAC (Java Aspect Components) is a very ambitious project consisting in
developing an aspect-oriented middleware layer.

Current application servers relying on J2EE does not always provide
satisfying means to separate technical concerns from the application
code. Since JAC uses aspect-orientation, the complex EJBs components
are replaced by POJOs (Plain Old Java Objects) and technical concerns
implementations that are usually wired deep into the containers
implementations are replaced by loosely-coupled, dynamically pluggable
aspect components.

JAC supports (in aspect components):

    * seamless persistence (CMP) that fully handles collections and
      references

    * flexible clustering features (customisable broadcast,
      load-balancing, data-consistency, caching)

    * instantaneously defined users and profiles management, access
      rights checking, and authentication features

    * several data integrity features

    * RAD (Rapid Application Development):

          o the GUI aspect allows the programmer to develop Swing and
            WEB applications at incredible speeds

          o the UML IDE (UML Aspect Factory) allows the programmer to
            generate running Java applications simply by drawing boxes
            and by configuring existing aspect components

    * and many more features!!!

All this software is Open Source (LGPL) and free of charges! (see the
file LICENSE.txt)


DOCUMENTATION
=============

All documentation is available online at: http://jac.objectweb.org and
in your local distribution, see the doc directory (open index.html).

For a first use of the JAC distribution, first read doc/tutorial.html

For instructions on how to launch the samples, see the the README.txt
file in the directory of the samples (src/org/objectweb/jac/samples/<sample_name>).

COMPILING (UNIX)
================

1. Extract the archive:

	cd <work_dir>
	tar zxf jac.tar.gz


2. Gets the libs.

JAC depends on a number of librairies in order to compile and run. The
ant build script assumes they are in the lib/ directory. The easiest
way to get all of them is to download the jac-libs package. 

[TODO]

This script will try to find the required jar files in /usr/share/java
and /usr/local/share/java and install links to them in lib and
lib/opt. If it does not find the jars, you'll have to either change
the CLASSPATH in scripts/init_env or manually put the jars in lib or
lib/opt. 

The jars expected in lib (gnu-regexp.jar cup.jar bcel.jar log4j) are
mandatory to build and run the JAC core. Those in opt are required by
some aspects or the IDE (jhotdraw.jar). 

NOTE: You'll need our patched jhotdraw in order to build the IDE. You
can fetch it from http://jac.objectweb.org/downloads.html


3. Compile

Just run ant with no arguments:

	ant

This will compile everything and build 3 jars: jac.jar, umlaf.jar and
samples.jar.


COPYRIGHTS
==========

JAC is developped by the people mentionned in the AUTHORS file.
Each source file is copyrighted by its author(s).
