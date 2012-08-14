TESTNAME=UnsignedContentInMETAINF
JAVAC_CLASSPATH=$(JNLP_TESTS_ENGINE_DIR):$(NETX_DIR)/lib/classes.jar
DEPLOY_DIR=$(JNLP_TESTS_SERVER_DEPLOYDIR)
JAVAC=$(BOOT_DIR)/bin/javac
JAR=$(BOOT_DIR)/bin/jar
ABS_SRC_PATH=$(JNLP_TESTS_SRCDIR)/custom/$(TESTNAME)/srcs

prepare-reproducer:
	echo PREPARING REPRODUCER $(TESTNAME)
	echo "USING ABSPATH = " $(ABS_SRC_PATH)
	cp $(DEPLOY_DIR)/ReadPropertiesSigned.jar $(DEPLOY_DIR)/UnsignedContentInMETAINF.jar
	# Place an unsigned file in the META-INF folder
	cd $(ABS_SRC_PATH)
	$(JAR) uf $(DEPLOY_DIR)/UnsignedContentInMETAINF.jar META-INF/
	echo PREPARED REPRODUCER $(TESTNAME)

clean-reproducer:
	echo CLEANING REPRODUCER $(TESTNAME)
	rm -f UnsignedContentInMETAINF.jar
	echo CLEANED REPRODUCER $(TESTNAME)

