include Make.rules

# Directory where to put .class files
CLASSES = $(JAC_ROOT)/classes

# Directory where to put .class files of libraries
CLASSES_LIB = $(JAC_ROOT)/classes_lib

# Root of source files
SRC = $(JAC_ROOT)/src

JAC_NAME = jac

JAR_FILE = $(JAC_NAME).jar

MKDIR 	= mkdir -p
RM 	= rm -f

JARS = $(JAR_FILE) samples.jar

# build a jar of jac.core. and jac.aspects.*
.PHONY: doc $(JARS) all clean almost_clean

all:
	make -C src/org/objectweb/jac all

incr:
	make -C src/org/objectweb/jac

jar: $(JARS)

# files .acc copied in the jar file
ACC = 	\
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/core/rtti/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/persistence/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/authentication/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/web/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/swing/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/user/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/session/*.acc) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/ide/*.acc)

# additional resources used by the web-gui aspect
WEB =	\
	org/objectweb/jac/aspects/gui/web/style.css \
	org/objectweb/jac/aspects/gui/web/style2.css \
	org/objectweb/jac/aspects/gui/web/ie.css \
	org/objectweb/jac/aspects/gui/web/script.js

# Directories of libs to include in $(JAR_FILE)
LIBS =  CH \
	gnu \
	java_cup \
	javax \
	org \
	com/lowagie \
	dori/jasper

# resource files used by the samples
SAMPLES_RESOURCES=\
	$(foreach sample,$(SAMPLES_PACKAGES), $(shell cd $(JAC_ROOT)/src; ls $(sample)/*.acc)) \
	$(foreach sample,$(SAMPLES_PACKAGES), $(shell cd $(JAC_ROOT)/src; ls $(sample)/*.jac))


ADDITION_FOR_RELEASE = \
	$(JARS) \
	jac.prop \
	jac.policy \
	basic.css \
	toc.css \
	icons \
	scripts \
	LICENSE.txt \
	README.txt \
	samples \
	UML_AF.bat \
	UML_AF.pif \
	UML_AF.lnk \
	UML_AF.sh \
	doc

RUNTIME_FILES = \
	$(JAR_FILE) \
	jac.prop \
	jac.policy \
	scripts/init_env \
	scripts/jac \
	scripts/go \
	scripts/libs.sh \
	lib \
	LICENSE.txt \
	README.txt

DEMO_FILES = \
	$(RUNTIME_FILES) \
	samples.jar \
	samples \


# All the resources to include in $(JAR_FILE)
RESOURCES=\
	$(ACC) \
	$(WEB) \
	org/objectweb/jac/util/mime.types \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/resources/base/*.*) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/resources/hotdraw/*.*) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/aspects/gui/resources/*.*) \
	$(shell cd $(JAC_ROOT)/src; ls org/objectweb/jac/ide/icons/*.*)

# Packages to include in jar file
PACKAGES=\
	org/objectweb/jac/core \
	org/objectweb/jac/aspects \
	org/objectweb/jac/util \
	org/objectweb/jac/lib \
	org/objectweb/jac/wrappers \
	org/objectweb/jac/ide

# Packages to include in "light" runtime jar file
RUNTIME_PACKAGES=\
	org/objectweb/jac/core \
	org/objectweb/jac/aspects \
	org/objectweb/jac/util \
	org/objectweb/jac/lib \
	org/objectweb/jac/wrappers \
	org/jutils

# samples directories
SAMPLES_PACKAGES = \
	org/objectweb/jac/samples/bench \
	org/objectweb/jac/samples/calcul \
	org/objectweb/jac/samples/contacts \
	org/objectweb/jac/samples/photos

# Create jar file with all jac classes and resources, and all the libraries
$(JAR_FILE): all
	@echo "--- Creating $@ ---"
	@$(JARCMD) cfm $@ jac.mf jac.policy
	@if [ -e $(CLASSES_LIB) ]; then rm -rf $(CLASSES_LIB); fi
	@mkdir -p $(CLASSES_LIB)
	@cd $(CLASSES_LIB); \
	  for i in ../lib/opt/*.jar; do $(JARCMD) xf $$i; done; \
	  for i in ../lib/*.jar; do $(JARCMD) xf $$i; done;
	cd $(CLASSES); \
	  $(JARCMD) uf ../$@ $(PACKAGES)
	cd $(CLASSES_LIB); \
	  $(JARCMD) uf ../$@ $(LIBS)
	@cd src; $(JARCMD) uf ../$@ $(RESOURCES)
	@echo "--- done : $@ ---"

# Create jar file with only jac classes and resource

jar_light: all jar_light_no_build

jar_light_no_build:
	@echo "--- Creating \"light\" jac.jar ---"
	@$(JARCMD) cfm jac.jar jac.mf jac.policy
	@cd $(CLASSES); \
	  $(JARCMD) uf ../jac.jar $(RUNTIME_PACKAGES)
	@cd src; $(JARCMD) uf ../jac.jar $(RESOURCES)
	@echo "--- done : \"light\" $@ ---"

# update existing jac.jar with classes and resources
update_jar:
	@echo "--- Updating $(JAR_FILE) ---"
	@cd $(CLASSES); \
	$(JARCMD) uf ../$(JAR_FILE) $(PACKAGES)
	@cd src; $(JARCMD) uf ../$(JAR_FILE) $(RESOURCES)

samples.jar:
	@echo "--- Creating $@ ---"
	@jar=`pwd`/$@; \
	  cd $(CLASSES); $(JARCMD) cf $$jar $(SAMPLES_PACKAGES); \
	  cd $(SRC); $(JARCMD) uf $$jar $(SAMPLES_RESOURCES)
	@echo "--- done : $@ ---"


# Build tar.gz for release
release: jar doc
	@echo "--- Making release ---"
	@$(MKDIR) tmp; \
	VERSION=`cat .version`; \
	cd tmp; \
	TMP_DIR=$(JAC_NAME); \
	$(MKDIR) $$TMP_DIR; \
	for i in $(ADDITION_FOR_RELEASE); do cp -rf ../$$i $$TMP_DIR; done; \
	$(MKDIR) $$TMP_DIR/src/org/objectweb/jac/samples; \
	make -C ../src/org/objectweb/jac/samples distclean; \
	for i in $(SAMPLES_PACKAGES); do \
	    cp -rf ../src/$$i $$TMP_DIR/src/org/objectweb/jac/samples; \
	done; \
	ARCHIVE=$(JAC_NAME)-$$VERSION.tar.gz; \
	tar czf $$ARCHIVE $$TMP_DIR; \
	mv $$ARCHIVE ..; \
	cd ..; \
	$(RM) -r tmp; \
	echo "--- done : $$ARCHIVE ---"

# Build tar.gz for release
release_runtime: all release_runtime_no_build

release_runtime_no_build: jar_light_no_build
	@echo "--- Making runtime release ---"
	@$(MKDIR) tmp; \
	cd tmp; \
	TMP_DIR=$(JAC_NAME); \
	$(MKDIR) $$TMP_DIR; \
	for f in $(RUNTIME_FILES); do \
		$(MKDIR) -p $$TMP_DIR/`dirname $$f`; \
		cp -rf ../$$f $$TMP_DIR/`dirname $$f`; \
	done; \
	ARCHIVE=$(JAC_NAME).tar.gz; \
	tar czhf $$ARCHIVE $$TMP_DIR; \
	mv $$ARCHIVE ..; \
	cd ..; \
	$(RM) -r tmp; \
	echo "--- done : $$ARCHIVE ---"

release_demo: jar_light_no_build samples.jar
	@ARCHIVE=$(JAC_NAME)-demo.tar.gz; \
	echo "--- Making demo release $$ARCHIVE ---"; \
	$(MKDIR) tmp; \
	cd tmp; \
	TMP_DIR=$(JAC_NAME); \
	$(MKDIR) $$TMP_DIR; \
	for f in $(DEMO_FILES); do \
		$(MKDIR) -p $$TMP_DIR/`dirname $$f`; \
		cp -rf ../$$f $$TMP_DIR/`dirname $$f`; \
	done; \
	tar czhf $$ARCHIVE \
	mv $$ARCHIVE ..; \
	cd ..; \
	$(RM) -r tmp; \
	echo "--- done : $$ARCHIVE ---"

release_doc: doc
	@ARCHIVE=$(JAC_NAME)-doc.tar.gz; \
	echo "--- Making doc release $$ARCHIVE ---"; \
	$(MKDIR) tmp; \
	cd tmp; \
	TMP_DIR=$(JAC_NAME); \
	$(MKDIR) $$TMP_DIR; \
	mv ../doc/javadoc $$TMP_DIR; \
	tar zcf $$ARCHIVE  $$TMP_DIR; \
	mv $$ARCHIVE ..; \
	cd ..; \
	$(RM) -r tmp; \
	echo "--- done : $$ARCHIVE ---"

# Build tar.bz2 for release
release_bz2: jar doc
	@echo "--- Making release ---"
	@$(MKDIR) tmp; \
	VERSION=`cat .version`; \
	cd tmp; \
	TMP_DIR=$(JAC_NAME); \
	$(MKDIR) $$TMP_DIR; \
	for i in $(ADDITION_FOR_RELEASE); do cp -rf ../$$i $$TMP_DIR; done; \
	$(MKDIR) $$TMP_DIR/src/org/objectweb/jac/samples; \
	make -C ../src/org/objectweb/jac/samples clean; \
	for i in $(SAMPLES_PACKAGES); do \
	    cp -rf ../src/$$i $$TMP_DIR/src/org/objectweb/jac/samples; \
	done; \
	ARCHIVE=$(JAC_NAME)-$$VERSION.tar.bz2; \
	tar cjf $$ARCHIVE $$TMP_DIR; \
	mv $$ARCHIVE ..; \
	cd ..; \
	$(RM) -r tmp; \
	echo "--- done : $$ARCHIVE ---"

doc:
	$(MAKE) -C doc javadoc.doc; \

tag:
	cvs tag -F v`tr . _ < .version`;

# update version number in java source files
# and change old version to the current version
version:
	@echo changing version from `cat .version.old` to `cat .version`
	scripts/updateVerNum `cat .version.old` `cat .version`
	cat .version > .version.old

CVSROOT=cvs.jac.forge.objectweb.org:/cvsroot/jac

# Make a tar.gz of cvs export for current version
tarball:
	VERSION=`cat .version`; \
	mkdir tmp; cd tmp; \
	cvs -d $(CVSROOT) export -r v`echo $$VERSION | tr . _` jac; \
	TMP_DIR=jac-$$VERSION; \
	mv jac $$TMP_DIR; \
	ARCHIVE=jac_src-$$VERSION.tar.gz; \
	tar zcf $$ARCHIVE $$TMP_DIR; \
	mv $$ARCHIVE ..; \
        cd ..; rm -rf tmp

# Make a tar.gz of cvs export for current version
tarball_bz2:
	VERSION=`cat .version`; \
	mkdir tmp; cd tmp; \
	cvs -d $(CVSROOT) export -r v`echo $$VERSION | tr . _` jac; \
	TMP_DIR=jac-$$VERSION; \
	mv jac $$TMP_DIR; \
	ARCHIVE=jac_src-$$VERSION.tar.bz2; \
	tar jcf $$ARCHIVE $$TMP_DIR; \
	mv $$ARCHIVE ..; \
        cd ..; rm -rf tmp

almost_clean:
	$(MAKE) -C src/org/objectweb/jac clean
	rm -rf classes/*

clean: almost_clean
	rm -f $(JARS) 
