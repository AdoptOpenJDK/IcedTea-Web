TESTNAME=SignedAppletCodebaseLoading

SRC_FILES=SignedAppletCodebaseLoading.java SignedAppletCodebaseLoadingHelper.java

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar
JARSIGNER=$(BOOT_DIR)/bin/jarsigner
JARSIGNER_CMD=$(JARSIGNER) -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass $(PRIVATE_KEYSTORE_PASS) -keypass $(PRIVATE_KEYSTORE_PASS)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)

	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(SRC_FILES); \

	cp ../resources/* $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	cd $(TMPDIR); \
	$(JAR) cfe SignedAppletCodebaseLoading.jar signed.SignedAppletCodebaseLoading signed; \
	cd -; \

	$(JARSIGNER_CMD) -sigfile Alpha $(TMPDIR)/SignedAppletCodebaseLoading.jar $(TEST_CERT_ALIAS)_signed; \

	cp $(TMPDIR)/SignedAppletCodebaseLoading.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	cp -r $(TMPDIR)/helper $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR); \
	rm -rf $(TMPDIR); \

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
