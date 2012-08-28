TESTNAME=AppletExtendsFromOutsideJar

SRC_FILES=AppletReferenceInSameJar.java AppletReferenceOutOfJar.java Referenced.java
JAR_FILES=AppletReferenceInSameJar.class Referenced.class
OUTER_FILE=AppletReferenceOutOfJar.class

JAVAC_CLASSPATH=$(JNLP_TESTS_ENGINE_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(SRC_FILES)
	
	cd $(TMPDIR); \
	$(JAR) cvf $(TESTNAME).jar $(JAR_FILES); \
	mv $(OUTER_FILE) $(JNLP_TESTS_SERVER_DEPLOYDIR); \
	mv $(TESTNAME).jar $(JNLP_TESTS_SERVER_DEPLOYDIR); 
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)