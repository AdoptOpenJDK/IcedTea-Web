TESTNAME=PartiallySignedAppletManifestSpecifiesSandbox

SRC_FILES=PartiallySignedAppletManifestSpecifiesSandboxSigned.java PartiallySignedAppletManifestSpecifiesSandboxUnsigned.java
ENTRYPOINT_CLASSES=PartiallySignedAppletManifestSpecifiesSandboxHelper

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
	cp ../srcs/MANIFEST.MF* $(TMPDIR); \

	cd $(TMPDIR); \
	$(JAR) cfme PartiallySignedAppletManifestSpecifiesSandboxSigned.jar MANIFEST.MF.1 signed.PartiallySignedAppletManifestSpecifiesSandboxSigned signed; \
	$(JAR) cfme PartiallySignedAppletManifestSpecifiesSandbox.jar MANIFEST.MF.1 unsigned.PartiallySignedAppletManifestSpecifiesSandboxUnsigned unsigned; \
        $(JAR) cfme PartiallySignedAppletManifestSpecifiesAllPermissionSigned.jar MANIFEST.MF.2 signed.PartiallySignedAppletManifestSpecifiesSandboxSigned signed; \
	$(JAR) cfme PartiallySignedAppletManifestSpecifiesAllPermission.jar MANIFEST.MF.2 unsigned.PartiallySignedAppletManifestSpecifiesSandboxUnsigned unsigned; \
	cd -; \

	$(JARSIGNER_CMD) -sigfile Alpha $(TMPDIR)/PartiallySignedAppletManifestSpecifiesSandboxSigned.jar $(TEST_CERT_ALIAS)_signed; \
	$(JARSIGNER_CMD) -sigfile Alpha $(TMPDIR)/PartiallySignedAppletManifestSpecifiesAllPermissionSigned.jar $(TEST_CERT_ALIAS)_signed; \

	cp $(TMPDIR)/PartiallySignedAppletManifestSpecifies{Sandbox,AllPermission}{Signed,}.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \

	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR); \
	rm -rf $(TMPDIR); \

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
