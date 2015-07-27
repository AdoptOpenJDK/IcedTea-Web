TESTNAME=JNLPClassLoaderDeadlock

SRC_FILES=JNLPClassLoaderDeadlock_1.java JNLPClassLoaderDeadlock_2.java
RESOURCE_FILES=JNLPClassLoaderDeadlock.html
ENTRYPOINT_CLASSES=JNLPClassLoaderDeadlock_1 JNLPClassLoaderDeadlock_2

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)

	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(SRC_FILES)

	cd ../resources; \
	cp $(RESOURCE_FILES) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	cd -; \
	for CLASS in $(ENTRYPOINT_CLASSES); \
	do \
		mv $(TMPDIR)/"$$CLASS.class" $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	done; \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
