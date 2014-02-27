TESTNAME=SignedAppletExternalMainClass

JARSIGNER=$(BOOT_DIR)/bin/jarsigner
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar

TMPDIR:=$(shell mktemp -d)

prepare-reproducer: 
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	$(JAVAC) -d $(TMPDIR) $(TESTNAME).java $(TESTNAME)Helper.java
	
	cd $(TMPDIR) ; \
	$(JAR) cvf $(TESTNAME)Helper.jar $(TESTNAME)Helper.class ; \
	$(BOOT_DIR)/bin/jarsigner -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass  $(PRIVATE_KEYSTORE_PASS)  \
	-keypass $(PRIVATE_KEYSTORE_PASS) "$(TMPDIR)/$(TESTNAME)Helper.jar" $(TEST_CERT_ALIAS)_signed   ; \
	
	cd $(TMPDIR); \
	mv $(TESTNAME).class $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(TESTNAME)Helper.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ;
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
