SRC 		=	Preparse.java \
			ParseInput.java

OBJ 		= $(SRC:.java=.compiled)

RM		= rm -f

JAVACCMD	?= javac

JAVACMD		?= java

CLASSDIR	?= $(JAC_ROOT)/classes

EXEC		= jac.util.preparse.Preparse

MODIF_ROOT	= $(JAC_ROOT)/modified

TO_MODIF	= AccScanner.java

SUBDIRS		= tmpdir tmpdir2

.SUFFIXES	: .java .compiled

default		: $(OBJ)

.java.compiled	:
	@echo "Compiling $< ..."
	@$(JAVACCMD) -d $(CLASSDIR) -classpath $(CLASSPATH) $<
	@touch $@

clean		:
	$(RM) *.compiled $(CLASSDIR)/org/objectweb/jac/util/preparse/*.class

launch		: default
	@echo "Launching $(EXEC) ..."
	@$(JAVACMD) -classpath $(CLASSPATH) $(EXEC) preparse.cfg $(TO_MODIF)

test		: launch
	@TMP=`echo "$(PWD)" | sed s%"$(JAC_ROOT)/src"%"$(MODIF_ROOT)"%`; \
	mkdir -p $$TMP; \
	rm -f $$TMP/*; \
	mv $(TO_MODIF:.java=.modified) $$TMP; \
	rename 's%\.modified%.java%' $$TMP/*
	for i in $(SUBDIRS); do ( cd $$i; make test; cd - ) ; done
