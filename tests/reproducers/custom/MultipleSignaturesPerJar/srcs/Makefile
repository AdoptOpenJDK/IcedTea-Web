TESTNAME=MultipleSignaturesPerJar

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
KEYTOOL=$(EXPORTED_KEYTOOL)
JARSIGNER=$(EXPORTED_JARSIGNER)
JARSIGNER_CMD=$(JARSIGNER) -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass  $(PRIVATE_KEYSTORE_PASS) -keypass $(PRIVATE_KEYSTORE_PASS) 
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)

# Index jar causes main class jar to load

TMPDIR:=$(shell mktemp -d)

prepare-reproducer: 
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) somecrazytestpackage/MultipleSignaturesPerJarMain.java
	
	# Extract ReadPropertiesSigned.class for our usage
	cd $(TMPDIR) ; \
	$(JAR) xf $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/ReadPropertiesSigned.jar ReadPropertiesSigned.class ; 
	
	# Create jars *testname*_A_and_B.jar, *testname*_A_only.jar, *testname*_B_only.jar 
	# These are signed by signatures A and B, A only, B only, respectively.
	
	# *testname*_A_and_B.jar as well as *testname*_B_only.jar contain ReadPropertiesSigned.class, which exercises the signing.
	# *testname*_A_only.jar contains MultipleSignaturesTest.class, the (reused) main class for this reproducer.
	
	cd $(TMPDIR) ; \
	$(JAR) cvf $(TESTNAME)_B_only.jar ReadPropertiesSigned.class ; \
	cp $(TESTNAME)_B_only.jar $(TESTNAME)_A_and_B.jar ; \
	$(JAR) cvf $(TESTNAME)_A_only.jar somecrazytestpackage ;
	
	# Sign with signature 'A', the signature used in the 'signed' reproducer group
	cd $(TMPDIR) ; \
	for jar_to_sign in $(TESTNAME)_A_only.jar $(TESTNAME)_A_and_B.jar; do \
	   $(JARSIGNER_CMD) -sigfile Alpha "$$jar_to_sign" $(TEST_CERT_ALIAS)_signed  ; \
	done
	
	# Sign with signature 'B', the signature used in the 'signed2' reproducer group
	cd $(TMPDIR) ; \
	for jar_to_sign in $(TESTNAME)_B_only.jar $(TESTNAME)_A_and_B.jar; do \
	   $(JARSIGNER_CMD) -sigfile Beta "$$jar_to_sign" $(TEST_CERT_ALIAS)_signed2  ; \
	done
	
	# Move jars into deployment directory
	cd $(TMPDIR); \
	mv $(TESTNAME)_B_only.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(TESTNAME)_A_only.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(TESTNAME)_A_and_B.jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ;
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)