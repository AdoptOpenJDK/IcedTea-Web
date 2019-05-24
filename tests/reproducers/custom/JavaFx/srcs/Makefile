TESTNAME=JavaFx

JAVAC_CLASSPATH=$(TEST_EXTENSIONS_DIR):$(NETX_DIR)/lib/classes.jar
JAVAC=$(EXPORTED_JAVAC)
JAR=$(EXPORTED_JAR)
JARSIGNER=$(EXPORTED_JARSIGNER)

ifeq ($(OS), Windows_NT)
	TMPDIR:=$(shell cygpath -p -m $(shell mktemp -d))
else
	TMPDIR:=$(shell mktemp -d)
endif

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)
	#this test contains prebuild binary, as javafx, when installed on computer, preven ITW from building
	#due to JSObject differennt implementation
	#$(JAVAC) -d $(TMPDIR) -classpath $(JAVAC_CLASSPATH) Controller.java Main.java; \
	#cp helloworld.fxml $(TMPDIR) ; \
	#pushd $(TMPDIR); \
	#$(JAR) cf $(TESTNAME).jar *.class *.fxml; \
	#popd
	#cp $(TMPDIR)/$(TESTNAME).jar $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	cp ../resources/* $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	rm -rf $(TMPDIR); \
	echo PREPARED REPRODUCER $(TESTNAME); \

clean-reproducer:
	echo NOTHING TO CLEAN FOR $(TESTNAME)
