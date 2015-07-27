TESTNAME=PackGZip

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)
JARSIGNER=$(EXPORTED_JARSIGNER)
PACKER=$(EXPORTED_PACK200)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)

	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(TESTNAME).java; \
	
	cp ../resources/* $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	cd $(TMPDIR); \
	$(JAR) cf $(TESTNAME).jar $(TESTNAME).class; \
	cd $(TMPDIR); \
    $(PACKER) $(TESTNAME).jar.pack.gz $(TESTNAME).jar; \
	cp $(TMPDIR)/$(TESTNAME).jar.pack.gz $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR); \
	rm -rf $(TMPDIR); \

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
