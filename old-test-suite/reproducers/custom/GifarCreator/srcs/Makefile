DEPLOY_SUBDIR=$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)
GIF=$(DEPLOY_SUBDIR)/happyNonAnimated.gif
JAR=$(DEPLOY_SUBDIR)/GifarBase.jar
RESULT1=$(DEPLOY_SUBDIR)/Gifar.jar
RESULT2=$(DEPLOY_SUBDIR)/Gifar.gif

#this is dependent on reproducers/signed/GifarBase

prepare-reproducer:
	cat $(GIF) > $(RESULT1)
	cat $(JAR) >> $(RESULT1)
	cp $(RESULT1) $(RESULT2)

clean-reproducer:
	rm -rf $(RESULT1)
	rm -rf $(RESULT2)

