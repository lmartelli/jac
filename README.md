
# WELCOME

Welcome to the JAC project!

JAC (Java Aspect Components) is a project that aims at developping an aspect-oriented middleware layer. It has been developped between 2000 and 2003 by Renaud Pawlak and Laurent Martelli within the AOPSYS company, and is part of the PhD Thesis of Renaud Pawlak.

JAC supports (in aspect components):

- seamless persistence (CMP) that fully handles collections and references
- flexible aspects for distributed programming (customisable broadcast, load-balancing, data-consistency, caching)
- instantaneously defined users and profiles management, access rights checking, and authentication features
- several data integrity features
- RAD (Rapid Application Development):
  - the GUI aspect allows the programmer to develop Swing and WEB applications in a declarative fashion
  - the UML IDE (UML Aspect Factory) allows the programmer to generate running Java applications simply by drawing boxes and by configuring existing aspect components

JAC is Open Source (LGPL) and free of charges! (see the file LICENSE.txt)

# DOCUMENTATION

All documentation is available online at: http://jac.ow2.org/ and in your local distribution, see the doc directory (open index.html).

For a first use of the JAC distribution, first read doc/tutorial.html

For instructions on how to launch the samples, see the the README.md file in the directory of the samples (src/org/objectweb/jac/samples/<sample_name>).

# COMPILING (UNIX)

1. Extract the archive:

```shell
cd <work_dir>
tar zxf jac.tar.gz
```

2. Gets the libs.

JAC depends on a number of librairies in order to compile and run. The ant build script assumes they are in the lib/ directory. The easiest way to get all of them is to download the jac-libs package. 

The jac-libs package can be downloaded from https://download.forge.ow2.org/jac/jac_libs-0.12.tar.gz

*Note: JAC is an old project. It would be interesting to Mavenize it :)*

3. Compile

Just run ant with no arguments: ``ant``.

This will compile everything and build 3 jars: jac.jar, umlaf.jar and samples.jar.

# COPYRIGHTS

JAC is developped by the people mentionned in the AUTHORS file. Each source file is copyrighted by its author(s).
