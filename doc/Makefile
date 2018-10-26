include ../Make.rules

JAC_ROOT=..

JAVADOCCMD = javadoc -quiet -windowtitle "JAC documentation" -footer "Contact \
JAC development team:<br> <a href=mailto:pawlak@cnam.fr>Renaud Pawlak</a><br>\
<a href=mailto:Lionel.Senturier@lip6.fr>Lionel Seinturier</a><br>\
<a href=mailto:laurent@aopsys.com>Laurent Martelli</a>" \
-header "JAC project<br>AOPSYS<br>CEDRIC & LIP6 labs" -version -author \
-classpath $(CLASSPATH) -sourcepath ../src -encoding UTF-8 \
-link http://java.sun.com/j2se/1.3/docs/api/ \
-link http://www.cacas.org/java/gnu/regexp/api \
-link http://jetty.mortbay.org/javadoc/ \
-link http://jakarta.apache.org/bcel/apidocs/


JAVADOCNOLINKCMD = javadoc -windowtitle "JAC documentation" -footer "Contact \
JAC development team:<br> <a href=mailto:pawlak@cnam.fr>Renaud Pawlak</a><br>\
<a href=mailto:Lionel.Senturier@lip6.fr>Lionel Seinturier</a><br>\
<a href=mailto:laurent@aopsys.com>Laurent Martelli</a>" \
-header "JAC project<br>AOPSYS<br>CEDRIC & LIP6 labs" -version -author \
-classpath $(CLASSPATH) -sourcepath ../src \
-stylesheetfile ../basic.css

J2HCMD = j2h

JDKAPI = /usr/doc/jdk-docs-1.2/api

JACSRCDIR = $(JAC_ROOT)/src
JAVADOCDIR = $(JAC_ROOT)/doc/javadoc
J2HDIR1 = $(JAC_ROOT)/doc/j2h.nojdk
J2HDIR2 = $(JAC_ROOT)/doc/j2h.jdk
IMGDIR = $(JAC_ROOT)/doc/images

HTMLDOCS=$(wildcard *.html)
PDFDOCS=$(wildcard *.pdf)
FIGURES = framework.png

SUBPACKAGES = org.objectweb.jac.util:org.objectweb.jac.core:org.objectweb.jac.wrappers:org.objectweb.jac.aspects:jac.ide
EXCLUDED_PACKAGES = org.objectweb.jac.util.ant:org.objectweb.jac.aspects.distribution.thinclient:org.objectweb.jac.core.dist.corba:org.objectweb.jac.aspects.speedo

default: javadoc.doc j2h1.doc

javadoc.doc:
	rm -fr $(JAVADOCDIR)
	mkdir -p $(JAVADOCDIR)
	cd $(JACSRCDIR); \
	$(JAVADOCCMD) -d $(JAVADOCDIR) -subpackages $(SUBPACKAGES) -exclude $(EXCLUDED_PACKAGES)

javadoc.doc.nolinks:
	rm -fr $(JAVADOCDIR)
	mkdir -p $(JAVADOCDIR)
	cd $(JACSRCDIR); \
	$(JAVADOCNOLINKCMD) -d $(JAVADOCDIR) $(PACKAGES)

j2h1.doc:
	rm -fr $(J2HDIR1)
	mkdir -p $(J2HDIR1)
	$(J2HCMD) -d $(J2HDIR1) -js $(JACSRCDIR)

j2h2.doc:
	rm -fr $(J2HDIR2)
	mkdir -p $(J2HDIR2)
	$(J2HCMD) -d $(J2HDIR2) -js $(JACSRCDIR) -jd $(JDKAPI)

figures: $(FIGURES)

html: tutorial.html aspects_config.html

install: $(FIGURES) html
#	rm -rf $(DOCDIR)/*
	rm -f $(WWWROOT)/basic.css
	cp ../basic.css $(WWWROOT)
	rm -f $(WWWROOT)/toc.css
	cp ../toc.css $(WWWROOT)
	cp $(HTMLDOCS) $(DOCDIR)
	cp $(PDFDOCS) $(DOCDIR)
	cp $(FIGURES) $(DOCDIR)
	cp -r $(IMGDIR) $(DOCDIR)
	cp -r $(J2HDIR1) $(DOCDIR)
	cp -dR $(JAVADOCDIR) $(DOCDIR)

clean:
	rm -fr $(JAVADOCDIR) $(J2HDIR1) $(J2HDIR2)
