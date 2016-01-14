TESTNAME=NoClassDeff

SRC_FILES=NoClassDeff
ENTRYPOINT_CLASSES=NoClassDeff

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME);
	pwd;
	ls ;
	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) *.java;
	#now the hack, remove inner class so we can later die on CNFE or NCDFE
	ls $(TMPDIR)/
	rm $(TMPDIR)/*\$$* ;  
	ls $(TMPDIR)/
	cp ../resources/* $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	for CLASS in $(ENTRYPOINT_CLASSES); \
	do \
		cd $(TMPDIR); \
		$(JAR) cfe "$$CLASS.jar" "$$CLASS" "$$CLASS.class"; \
		cd -;\
		mv $(TMPDIR)/"$$CLASS.jar" $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	done; \
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR) ; \
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
