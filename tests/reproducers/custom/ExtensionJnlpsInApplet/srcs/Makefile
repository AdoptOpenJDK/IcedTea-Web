TESTNAME=ExtensionJnlpsInApplet

SRC_FILES=ExtensionJnlpHelper.java ExtensionJnlpTestApplet.java
RESOURCE_FILES=ExtensionJnlpTest.html ExtensionJnlpTestApplet.jnlp ExtensionJnlpHelper.jnlp
ENTRYPOINT_CLASSES=ExtensionJnlpHelper ExtensionJnlpTestApplet

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
	ls; \
	for CLASS in $(ENTRYPOINT_CLASSES); \
	do \
		cd $(TMPDIR); \
		$(JAR) cfe "$$CLASS.jar" "$$CLASS" "$$CLASS.class"; \
		cd -;\
		mv $(TMPDIR)/"$$CLASS.jar" $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	done; \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
