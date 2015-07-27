TESTNAME=AppletExtendsFromOutsideJar

SRC_FILES=AppletReferenceInSameJar.java AppletReferenceOutOfJar.java Referenced.java
JAR_FILES=AppletReferenceInSameJar.class Referenced.class
OUTER_FILE=AppletReferenceOutOfJar.class

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(SRC_FILES)
	
	cd $(TMPDIR); \
	$(JAR) cvf $(TESTNAME).jar $(JAR_FILES); \
	mv $(OUTER_FILE) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	mv $(TESTNAME).jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); 
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
