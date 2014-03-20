TESTNAME=TrustedOnlyAttribute

JARSIGNER=$(BOOT_DIR)/bin/jarsigner
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar

TMPDIR:=$(shell mktemp -d)

prepare-reproducer: 
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	cp MANIFEST.MF $(TMPDIR) ; \
	$(JAVAC) -d $(TMPDIR) $(TESTNAME).java ; \
	
	cd $(TMPDIR) ; \
	$(JAR) cvfm $(TESTNAME)Signed.jar MANIFEST.MF $(TESTNAME).class ; \
	$(JAR) cvfm $(TESTNAME)Unsigned.jar MANIFEST.MF $(TESTNAME).class ; \
	$(BOOT_DIR)/bin/jarsigner -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass  $(PRIVATE_KEYSTORE_PASS)  \
	-keypass $(PRIVATE_KEYSTORE_PASS) "$(TMPDIR)/$(TESTNAME)Signed.jar" $(TEST_CERT_ALIAS)_signed ; \
	
	cd $(TMPDIR); \
	mv $(TESTNAME)Signed.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(TESTNAME)Unsigned.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
