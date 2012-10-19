TESTNAME=AdditionalJarsInMetaInfIndexList
ARCHIVE_TEST_FOLDER=archive_tag_folder_test

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
KEYTOOL=$(BOOT_DIR)/bin/keytool
JARSIGNER=$(BOOT_DIR)/bin/jarsigner
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar

# File used because the 'jar' command does not accept an empty file
DUMMY_FILE=jar_dummy_content

# Index jar causes main class jar to load
INDEX_JAR_UNSIGNED=AdditionalJarsInMetaInfIndexListUnsigned.jar
INDEX_JAR_SIGNED=AdditionalJarsInMetaInfIndexListSigned.jar

MAINCLASS=LoadedViaMetaInfIndexList

MAINCLASS_JAR_UNSIGNED=LoadedViaMetaInfIndexListUnsigned.jar
MAINCLASS_JAR_SIGNED=LoadedViaMetaInfIndexListSigned.jar

TMPDIR:=$(shell mktemp -d)

prepare-reproducer: 
	echo PREPARING REPRODUCER $(TESTNAME) in $(TMPDIR)
	
	$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) $(MAINCLASS).java
	
	# Create the jars which have INDEX.LIST
	cd $(TMPDIR) ; \
	echo "This file exists because jar command does not take 0 args" > $(DUMMY_FILE) ; \
	$(JAR) cvf $(INDEX_JAR_UNSIGNED) $(DUMMY_FILE)	; \
	$(JAR) cvf $(INDEX_JAR_SIGNED) $(DUMMY_FILE) ;
	
	# Create the jar which has the main-class
	# and update INDEX_JAR_*'s index
	cd $(TMPDIR) ; \
	$(JAR) cvf $(MAINCLASS_JAR_UNSIGNED) $(MAINCLASS).class ; \
	$(JAR) cvf $(MAINCLASS_JAR_SIGNED) $(MAINCLASS).class  ; \
	$(JAR) i $(INDEX_JAR_UNSIGNED) $(MAINCLASS_JAR_UNSIGNED) ; \
	$(JAR) i $(INDEX_JAR_SIGNED) $(MAINCLASS_JAR_SIGNED) ; 
	
	# Sign some of the jars for the signed jar test
	cd $(TMPDIR) ; \
	for jar_to_sign in $(MAINCLASS_JAR_SIGNED) $(INDEX_JAR_SIGNED); do \
	   $(BOOT_DIR)/bin/jarsigner -keystore $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME) -storepass  $(PRIVATE_KEYSTORE_PASS)  \
	   -keypass $(PRIVATE_KEYSTORE_PASS) "$$jar_to_sign" $(TEST_CERT_ALIAS)_signed   ; \
	done
	
	# Move jars into deployment directory
	cd $(TMPDIR); \
	mv $(INDEX_JAR_UNSIGNED) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(INDEX_JAR_SIGNED) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(MAINCLASS_JAR_UNSIGNED) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; \
	mv $(MAINCLASS_JAR_SIGNED) $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR) ; 
	
	echo PREPARED REPRODUCER $(TESTNAME), removing $(TMPDIR)
	rm -rf $(TMPDIR)

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
