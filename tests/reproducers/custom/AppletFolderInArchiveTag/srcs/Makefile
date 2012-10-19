TESTNAME=AppletFolderInArchiveTag
ARCHIVE_TEST_FOLDER=archive_tag_folder_test
JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
DEPLOY_SUBDIR=$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/$(ARCHIVE_TEST_FOLDER)
INDEX_HTML_BODY="<html><body><h1>Required to recognize folder structure</h1></body></html>"

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)
	mkdir -p $(DEPLOY_SUBDIR)
	echo $(INDEX_HTML_BODY) > $(DEPLOY_SUBDIR)/index.html
	$(EXPORTED_JAVAC) -classpath $(JAVAC_CLASSPATH) -d $(DEPLOY_SUBDIR) $(TESTNAME).java
	echo PREPARED REPRODUCER $(TESTNAME)

clean-reproducer:
	echo CLEANING REPRODUCER $(TESTNAME)
	rm -rf $(DEPLOY_SUBDIR)
	echo CLEANED REPRODUCER $(TESTNAME)

