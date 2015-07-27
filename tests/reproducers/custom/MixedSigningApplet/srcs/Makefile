TESTNAME=MixedSigningApplet

SRC_FILES=MixedSigningAppletSigned.java MixedSigningAppletHelper.java
ENTRYPOINT_CLASSES=MixedSigningApplet

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)
JARSIGNER=$(EXPORTED_JARSIGNER)
JARSIGNER_CMD=$(JARSIGNER) -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass $(PRIVATE_KEYSTORE_PASS) -keypass $(PRIVATE_KEYSTORE_PASS)

TMPDIR:=$(shell mktemp -d)

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)

	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(SRC_FILES); \

	cp ../resources/* $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	cd $(TMPDIR); \
	$(JAR) cfe MixedSigningAppletSigned.jar signed.MixedSigningAppletSigned signed; \
	$(JAR) cf MixedSigningApplet.jar helper; \
	cd -; \

	$(JARSIGNER_CMD) -sigfile Alpha $(TMPDIR)/MixedSigningAppletSigned.jar $(TEST_CERT_ALIAS)_signed; \

	cp $(TMPDIR)/MixedSigningApplet{Signed,}.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR); \
	rm -rf $(TMPDIR); \

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
