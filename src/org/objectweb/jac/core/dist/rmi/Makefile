JAC_ROOT=../../../../../../..
include $(JAC_ROOT)/Make.rules
vpath %.class $(CLASSDIR)/org/objectweb/jac/core/dist/rmi

#
# Source files
#

JAVA = RMIRemoteContainerInterf.java RMIRemoteContainer.java RMIDistd.java \
       RMIRemoteContainerStub.java RMINaming.java RMIRemoteRef.java

#
# Generic production rules
#

default: $(JAVA:.java=.class)

notrace: default

all:
	@echo "Compiling RMIRemoteContainerInterf.java RMIRemoteContainer.java with $(JAVACCMD)"
	@$(JAVACCMD) $(JAVAC_OPTS) RMIRemoteContainerInterf.java RMIRemoteContainer.java
	@echo "Compiling org.objectweb.jac.core.dist.rmi.RMIRemoteContainer with $(RMICCMD)"
	@$(RMICCMD) $(RMIC_OPTS) org.objectweb.jac.core.dist.rmi.RMIRemoteContainer
	@echo "Compiling $(JAVA) with $(JAVACCMD)"
	@$(JAVACCMD) $(JAVAC_OPTS) $(JAVA)

#
# Production rules specific to the current directory
#

RMIRemoteContainer.class: RMIRemoteContainer.java RMIRemoteContainerInterf.java
	@echo "Compiling RMIRemoteContainer.java with $(JAVACCMD)"
	@$(JAVACCMD) $(JAVAC_OPTS) RMIRemoteContainer.java
	@echo "Compiling org.objectweb.jac.core.dist.rmi.RMIRemoteContainer with $(RMICCMD)"
	@$(RMICCMD) $(RMIC_OPTS) org.objectweb.jac.core.dist.rmi.RMIRemoteContainer

RMIRemoteRef.class: RMIRemoteRef.java RMIRemoteContainer.java
RMIRemoteContainerStub.class: RMIRemoteContainerStub.java RMIRemoteContainer.java

#
# Clean up rule
#

clean:
	rm -fr $(CLASSDIR)/org/objectweb/jac/core/dist/rmi/*






