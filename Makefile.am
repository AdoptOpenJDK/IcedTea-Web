# Source directories

NETX_DIR = $(abs_top_builddir)/netx.build
NETX_SRCDIR = $(abs_top_srcdir)/netx
NETX_RESOURCE_DIR=$(NETX_SRCDIR)/net/sourceforge/jnlp/resources
NETX_EXTRA_DIR=$(abs_top_srcdir)/extra/net/sourceforge/javaws/about/resources
NETX_EXTRA_DIST_DIR=$(abs_top_builddir)/extra-lib/net/sourceforge/javaws/about/resources

REPORT_STYLES_DIRNAME=report-styles

TESTS_SRCDIR=$(abs_top_srcdir)/tests
TESTS_DIR=$(abs_top_builddir)/tests.build

NETX_UNIT_TEST_SRCDIR=$(TESTS_SRCDIR)/netx/unit
NETX_UNIT_TEST_DIR=$(TESTS_DIR)/netx/unit

JUNIT_RUNNER_DIR=$(TESTS_DIR)/junit-runner
JUNIT_RUNNER_SRCDIR=$(TESTS_SRCDIR)/junit-runner

JNLP_TESTS_ENGINE_SRCDIR=$(TESTS_SRCDIR)/netx/jnlp_testsengine
JNLP_TESTS_SRCDIR=$(TESTS_SRCDIR)/jnlp_tests
JNLP_TESTS_ENGINE_DIR=$(TESTS_DIR)/netx/jnlp_testsengine
JNLP_TESTS_SERVER_DEPLOYDIR=$(TESTS_DIR)/jnlp_test_server
JNLP_TESTS_DIR=$(TESTS_DIR)/jnlp_tests

JUNIT_RUNNER_JAR=$(abs_top_builddir)/junit-runner.jar

# Build directories

BOOT_DIR = $(abs_top_builddir)/bootstrap/jdk1.6.0

if WITH_RHINO
  RHINO_RUNTIME=:$(RHINO_JAR)
else
  RHINO_RUNTIME=
endif

RUNTIME = $(BOOT_DIR)/jre/lib/rt.jar:$(BOOT_DIR)/jre/lib/jsse.jar$(RHINO_RUNTIME)

# Flags
IT_CFLAGS=$(CFLAGS) $(ARCHFLAG)
IT_JAVAC_SETTINGS=-g -encoding utf-8 $(JAVACFLAGS) $(MEMORY_LIMIT) $(PREFER_SOURCE)
IT_LANGUAGE_SOURCE_VERSION=6
IT_CLASS_TARGET_VERSION=6
IT_JAVACFLAGS=$(IT_JAVAC_SETTINGS) -source $(IT_LANGUAGE_SOURCE_VERSION) -target $(IT_CLASS_TARGET_VERSION)

#
# We need the jars in bootclasspath for a couple of reasons
#  - we use classes (in the sun.applet package) loaded by the bootclassloader
#    using another classloader to load classes from the same package causes an
#    IllegalAccessException
#  - we want full privileges
#
JRE='"$(SYSTEM_JDK_DIR)/jre"'
LAUNCHER_BOOTCLASSPATH="-Xbootclasspath/a:$(datadir)/$(PACKAGE_NAME)/netx.jar$(RHINO_RUNTIME)"
PLUGIN_BOOTCLASSPATH='"-Xbootclasspath/a:$(datadir)/$(PACKAGE_NAME)/netx.jar:$(datadir)/$(PACKAGE_NAME)/plugin.jar$(RHINO_RUNTIME)"'

# Fake update version to work with the Deployment Toolkit script used by Oracle
# http://download.oracle.com/javase/tutorial/deployment/deploymentInDepth/depltoolkit_index.html
JDK_UPDATE_VERSION=50

# Sources list

PLUGIN_TEST_SRCS = $(abs_top_srcdir)/plugin/tests/LiveConnect/*.java

NETX_PKGS = javax.jnlp net.sourceforge.nanoxml net.sourceforge.jnlp \
	net.sourceforge.jnlp.cache net.sourceforge.jnlp.config \
	net.sourceforge.jnlp.controlpanel net.sourceforge.jnlp.event \
	net.sourceforge.jnlp.runtime net.sourceforge.jnlp.security \
	net.sourceforge.jnlp.security.viewer net.sourceforge.jnlp.services \
	net.sourceforge.jnlp.tools net.sourceforge.jnlp.util

# Conditional defintions
if ENABLE_PLUGIN
ICEDTEAPLUGIN_CLEAN = clean-IcedTeaPlugin
LIVECONNECT_DIR = netscape sun/applet
PLUGIN_DIR=$(abs_top_builddir)/plugin/icedteanp
PLUGIN_SRCDIR=$(abs_top_srcdir)/plugin/icedteanp
LIVECONNECT_SRCS = $(PLUGIN_SRCDIR)/java
ICEDTEAPLUGIN_TARGET = $(PLUGIN_DIR)/IcedTeaPlugin.so stamps/liveconnect-dist.stamp
PLUGIN_PKGS = sun.applet netscape.security netscape.javascript
endif

if CP_SUPPORTS_REFLINK
  REFLINK = --reflink=auto
endif

if SRC_DIR_HARDLINKABLE
  SRC_DIR_LINK = -l
else
  SRC_DIR_LINK = $(REFLINK)
endif

if ENABLE_DOCS
JAVADOC_OPTS=-use -keywords -encoding UTF-8 -splitIndex \
 -bottom '<font size="-1"> <a href="http://icedtea.classpath.org/bugzilla">Submit a bug or feature</a></font>'
if JAVADOC_SUPPORTS_J_OPTIONS
JAVADOC_MEM_OPTS=-J-Xmx1024m -J-Xms128m -J-XX:PermSize=32m -J-XX:MaxPermSize=160m
endif
endif

if WITH_RHINO
  RHINO_TESTS=check-pac-functions
else
  RHINO_TESTS=
endif

if WITH_JUNIT
  JUNIT_TESTS=run-netx-unit-tests
else
  JUNIT_TESTS=
endif


PLUGIN_VERSION = IcedTea-Web $(FULL_VERSION)

EXTRA_DIST = $(top_srcdir)/netx $(top_srcdir)/plugin javaws.png javaws.desktop.in extra launcher \
 itweb-settings.desktop.in $(top_srcdir)/tests

# binary names
javaws:= $(shell echo javaws | sed '@program_transform_name@')
itweb_settings:= $(shell echo itweb-settings | sed '@program_transform_name@')

# the launcher needs to know $(bindir) and $(datadir) which can be different at
# make-time from configure-time
edit_launcher_script = sed \
  -e 's|[@]LAUNCHER_BOOTCLASSPATH[@]|$(LAUNCHER_BOOTCLASSPATH)|g' \
  -e 's|[@]JAVAWS_BIN_LOCATION[@]|$(bindir)/$(javaws)|g' \
  -e 's|[@]ITWEB_SETTINGS_BIN_LOCATION[@]|$(bindir)/$(itweb_settings)|g' \
  -e 's|[@]JAVA[@]|$(JAVA)|g' \
  -e 's|[@]JRE[@]|$(JRE)|g'

# Top-Level Targets
# =================

all-local: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp launcher.build/$(javaws) \
 javaws.desktop stamps/docs.stamp launcher.build/$(itweb_settings) itweb-settings.desktop

check-local: $(RHINO_TESTS) $(JUNIT_TESTS)

clean-local: clean-netx clean-plugin clean-liveconnect clean-extra clean-bootstrap-directory \
 clean-native-ecj clean-launchers clean-desktop-files clean-docs clean-tests
	if [ -e stamps ] ; then \
	  rmdir stamps ; \
	fi

.PHONY: clean-IcedTeaPlugin clean-add-netx clean-add-netx-debug clean-add-plugin clean-add-plugin-debug \
 clean-bootstrap-directory clean-native-ecj clean-desktop-files clean-netx-docs clean-docs clean-plugin-docs \
 clean-tests check-local clean-launchers check-pac-functions run-netx-unit-tests clean-netx-tests \
 clean-junit-runner clean-netx-unit-tests

install-exec-local:
	${mkinstalldirs} $(DESTDIR)$(bindir) $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/ $(DESTDIR)$(libdir)
if ENABLE_PLUGIN
	${INSTALL_PROGRAM} $(PLUGIN_DIR)/IcedTeaPlugin.so $(DESTDIR)$(libdir)
	${INSTALL_DATA} $(abs_top_builddir)/liveconnect/lib/classes.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/plugin.jar
endif
	${INSTALL_DATA} $(NETX_DIR)/lib/classes.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar
	${INSTALL_PROGRAM} launcher.build/$(javaws) $(DESTDIR)$(bindir)
	${INSTALL_DATA} extra-lib/about.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/about.jar
	${INSTALL_PROGRAM} launcher.build/$(itweb_settings) $(DESTDIR)$(bindir)

install-data-local:
	${mkinstalldirs} -d $(DESTDIR)$(mandir)/man1
	${INSTALL_DATA} $(NETX_SRCDIR)/javaws.1 $(DESTDIR)$(mandir)/man1
	${INSTALL_DATA} $(NETX_RESOURCE_DIR)/about.jnlp $(DESTDIR)$(datarootdir)/$(PACKAGE_NAME)
if ENABLE_DOCS
	${mkinstalldirs} $(DESTDIR)$(htmldir)
	(cd ${abs_top_builddir}/docs/netx; \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} $(DESTDIR)$(htmldir)/netx/$${files}; \
	 done)
if ENABLE_PLUGIN
	(cd ${abs_top_builddir}/docs/plugin; \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} $(DESTDIR)$(htmldir)/plugin/$${files}; \
	 done)
endif
endif

uninstall-local:
	rm -f $(DESTDIR)$(libdir)/IcedTeaPlugin.so
	rm -f $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/plugin.jar
	rm -f $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar
	rm -f $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/about.jnlp
	rm -f $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/about.jar
	rm -f $(DESTDIR)$(mandir)/man1/javaws.1
	rm -f $(DESTDIR)$(bindir)/$(javaws)
	rm -f $(DESTDIR)$(bindir)/$(itweb_settings)
	rm -rf $(DESTDIR)$(htmldir)

# Plugin

if ENABLE_PLUGIN
# IcedTeaPlugin.so.
# Separate compile and link invocations to ensure intermediate object
# is listed before -l options.  See:
# http://developer.mozilla.org/en/docs/XPCOM_Glue

PLUGIN_SRC=IcedTeaNPPlugin.cc IcedTeaScriptablePluginObject.cc \
        IcedTeaJavaRequestProcessor.cc IcedTeaPluginRequestProcessor.cc \
		IcedTeaPluginUtils.cc

PLUGIN_OBJECTS=IcedTeaNPPlugin.o IcedTeaScriptablePluginObject.o \
        IcedTeaJavaRequestProcessor.o IcedTeaPluginRequestProcessor.o \
		IcedTeaPluginUtils.o

$(PLUGIN_DIR)/%.o: $(PLUGIN_SRCDIR)/%.cc
	mkdir -p $(PLUGIN_DIR) && \
	cd $(PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  -DJDK_UPDATE_VERSION="\"$(JDK_UPDATE_VERSION)\"" \
	  -DPLUGIN_NAME="\"IcedTea-Web Plugin\"" \
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DPACKAGE_URL="\"$(PACKAGE_URL)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
	  -DICEDTEA_WEB_JRE=$(JRE) \
	  -DPLUGIN_BOOTCLASSPATH=$(PLUGIN_BOOTCLASSPATH) \
	  $(GLIB_CFLAGS) \
	  $(GTK_CFLAGS) \
	  $(MOZILLA_CFLAGS) \
	  -fPIC -o $@ -c $<

$(PLUGIN_DIR)/IcedTeaPlugin.so: $(addprefix $(PLUGIN_DIR)/,$(PLUGIN_OBJECTS))
	cd $(PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  $(PLUGIN_OBJECTS) \
	  $(GLIB_LIBS) \
	  $(GTK_LIBS) \
	  $(MOZILLA_LIBS)\
	  -shared -o $@

clean-IcedTeaPlugin:
	rm -f $(PLUGIN_DIR)/*.o
	rm -f $(PLUGIN_DIR)/IcedTeaPlugin.so
	if [ $(abs_top_srcdir) != $(abs_top_builddir) ]; then \
	  if [ -e $(abs_top_builddir)/plugin/icedteanp ] ; then \
	    rmdir $(abs_top_builddir)/plugin/icedteanp ; \
	    rmdir $(abs_top_builddir)/plugin ; \
	  fi ; \
	fi
endif

stamps/plugin.stamp: $(ICEDTEAPLUGIN_TARGET)
	mkdir -p stamps
	touch stamps/plugin.stamp

clean-plugin: $(ICEDTEAPLUGIN_CLEAN)
	rm -f stamps/plugin.stamp

liveconnect-source-files.txt:
	if test "x${LIVECONNECT_DIR}" != x; then \
	  find $(LIVECONNECT_SRCS) -name '*.java' | sort > $@ ; \
	fi
	touch $@

stamps/liveconnect.stamp: liveconnect-source-files.txt stamps/netx.stamp
	if test "x${LIVECONNECT_DIR}" != x; then \
	  mkdir -p $(abs_top_builddir)/liveconnect && \
	  $(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	      -d $(abs_top_builddir)/liveconnect \
	      -bootclasspath $(NETX_DIR):$(RUNTIME) \
	      -sourcepath $(LIVECONNECT_SRCS) \
	      @liveconnect-source-files.txt ; \
	fi
	mkdir -p stamps
	touch $@

stamps/liveconnect-dist.stamp: stamps/liveconnect.stamp
	if test "x${LIVECONNECT_DIR}" != x; then \
	  (cd $(abs_top_builddir)/liveconnect ; \
	   mkdir -p lib ; \
	   $(BOOT_DIR)/bin/jar cf lib/classes.jar $(LIVECONNECT_DIR) ; \
	   cp -pPR $(SRC_DIR_LINK) $(LIVECONNECT_SRCS) src; \
	   find src -type f -exec chmod 640 '{}' ';' -o -type d -exec chmod 750 '{}' ';'; \
	   cd src ; \
	   $(ZIP) -qr $(abs_top_builddir)/liveconnect/lib/src.zip $(LIVECONNECT_DIR) ) ; \
	fi
	mkdir -p stamps
	touch $@

clean-liveconnect:
	rm -rf $(abs_top_builddir)/liveconnect
	rm -f stamps/liveconnect-dist.stamp
	rm -f liveconnect-source-files.txt
	rm -f stamps/liveconnect.stamp

# NetX
# requires availability of OpenJDK source code including
# a patch applied to sun.plugin.AppletViewerPanel and generated sources

netx-source-files.txt:
	find $(NETX_SRCDIR) -name '*.java' | sort > $@
if !WITH_RHINO
	sed -i '/RhinoBasedPacEvaluator/ d' $@
endif

stamps/netx.stamp: netx-source-files.txt stamps/bootstrap-directory.stamp
	mkdir -p $(NETX_DIR)
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	    -d $(NETX_DIR) \
	    -sourcepath $(NETX_SRCDIR) \
	    -bootclasspath $(RUNTIME) \
	    @netx-source-files.txt
	(cd $(NETX_RESOURCE_DIR); \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} \
	   $(NETX_DIR)/net/sourceforge/jnlp/resources/$${files}; \
	 done)
	cp -a $(NETX_SRCDIR)/net/sourceforge/jnlp/runtime/pac-funcs.js \
	  $(NETX_DIR)/net/sourceforge/jnlp/runtime
	cp -a build.properties $(NETX_DIR)/net/sourceforge/jnlp/
	mkdir -p stamps
	touch $@

stamps/netx-dist.stamp: stamps/netx.stamp $(abs_top_builddir)/netx.manifest
	(cd $(NETX_DIR) ; \
	 mkdir -p lib ; \
	 $(BOOT_DIR)/bin/jar cfm lib/classes.jar \
	  $(abs_top_builddir)/netx.manifest javax/jnlp net ; \
	 cp -pPR $(SRC_DIR_LINK) $(NETX_SRCDIR) src; \
	 find src -type f -exec chmod 640 '{}' ';' -o -type d -exec chmod 750 '{}' ';'; \
	 cd src ; \
	 $(ZIP) -qr $(NETX_DIR)/lib/src.zip javax net )
	mkdir -p stamps
	touch $@

clean-netx:
	rm -rf $(NETX_DIR)
	rm -f stamps/netx-dist.stamp
	rm -f netx-source-files.txt
	rm -f stamps/netx.stamp

clean-desktop-files:
	rm -f javaws.desktop
	rm -f itweb-settings.desktop

# extras -- used to create about.jar for javaws.
extra-source-files.txt:
	find $(abs_top_srcdir)/extra -name '*.java' | sort > $@

stamps/extra-class-files.stamp: extra-source-files.txt stamps/netx.stamp
	mkdir -p extra-lib
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) -d extra-lib \
          -sourcepath $(abs_top_srcdir)/extra -cp netx.build \
          -bootclasspath $(RUNTIME) @extra-source-files.txt
	(cd $(NETX_EXTRA_DIR); \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} \
	   $(NETX_EXTRA_DIST_DIR)/$${files}; \
	 done)
	mkdir -p stamps
	touch $@

clean-extra:
	rm -rf extra-lib
	rm -f stamps/extra-class-files.stamp
	rm -f extra-source-files.txt

extra-lib/about.jar: stamps/extra-class-files.stamp
	$(BOOT_DIR)/bin/jar cf $@ -C extra-lib net ;

launcher.build/$(javaws): launcher/javaws.in
	mkdir -p launcher.build
	$(edit_launcher_script) < $< > $@

launcher.build/$(itweb_settings): launcher/itweb-settings.in
	mkdir -p launcher.build
	$(edit_launcher_script) < $< > $@

clean-launchers:
	rm -f launcher.build/$(javaws)
	rm -f launcher.build/$(itweb_settings)
	if [ -e launcher.build ] ; then \
	  rmdir launcher.build ; \
	fi

javaws.desktop: javaws.desktop.in
	sed "s#PATH_TO_JAVAWS#$(bindir)/$(javaws)#" < $(srcdir)/javaws.desktop.in > javaws.desktop

itweb-settings.desktop: $(srcdir)/itweb-settings.desktop.in
	sed "s#PATH_TO_ITWEB_SETTINGS#$(bindir)/$(itweb_settings)#" \
	  < $(srcdir)/itweb-settings.desktop.in > itweb-settings.desktop

# documentation

stamps/docs.stamp: stamps/netx-docs.stamp stamps/plugin-docs.stamp
	touch stamps/docs.stamp

clean-docs: clean-netx-docs clean-plugin-docs
	if [ -e ${abs_top_builddir}/docs ] ; then \
	  rmdir ${abs_top_builddir}/docs ; \
	fi
	rm -f stamps/docs.stamp

stamps/netx-docs.stamp: stamps/bootstrap-directory.stamp
if ENABLE_DOCS
	$(BOOT_DIR)/bin/javadoc $(JAVADOC_MEM_OPTS) $(JAVADOC_OPTS) \
	 -d ${abs_top_builddir}/docs/netx -sourcepath $(NETX_SRCDIR) \
	 -doctitle 'IcedTea-Web: NetX API Specification' \
	 -windowtitle 'IcedTea-Web: NetX ' \
	 -header '<strong>IcedTea-Web<br/>NetX</strong>' \
	 $(NETX_PKGS)
endif
	mkdir -p stamps
	touch stamps/netx-docs.stamp

clean-netx-docs:
	rm -rf ${abs_top_builddir}/docs/netx
	rm -f stamps/netx-docs.stamp

stamps/plugin-docs.stamp: stamps/bootstrap-directory.stamp
if ENABLE_DOCS
if ENABLE_PLUGIN
	$(BOOT_DIR)/bin/javadoc $(JAVADOC_MEM_OPTS) $(JAVADOC_OPTS) \
	 -d ${abs_top_builddir}/docs/plugin -sourcepath $(NETX_SRCDIR):$(LIVECONNECT_SRCS) \
	 -doctitle 'IcedTea-Web: Plugin API Specification' \
	 -windowtitle 'IcedTea-Web: Plugin ' \
	 -header '<strong>IcedTea-Web<br/>Plugin</strong>' \
	 $(PLUGIN_PKGS)
endif
endif
	mkdir -p stamps
	touch stamps/plugin-docs.stamp

clean-plugin-docs:
	rm -rf ${abs_top_builddir}/docs/plugin
	rm -f stamps/plugin-docs.stamp


# check
# ==========================

clean-tests: clean-netx-tests
	if [ -e $(TESTS_DIR) ]; then \
		rmdir $(TESTS_DIR) ; \
	fi

check-pac-functions: stamps/bootstrap-directory.stamp
	./jrunscript $(abs_top_srcdir)/tests/netx/pac/pac-funcs-test.js \
	  $$(readlink -f $(abs_top_srcdir)/netx/net/sourceforge/jnlp/runtime/pac-funcs.js)

junit-runner-source-files.txt:
	find $(JUNIT_RUNNER_SRCDIR) -name '*.java' | sort > $@

$(JUNIT_RUNNER_JAR): junit-runner-source-files.txt
	mkdir -p $(JUNIT_RUNNER_DIR) && \
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	  -d $(JUNIT_RUNNER_DIR) \
	  -classpath $(JUNIT_JAR) \
	  @junit-runner-source-files.txt && \
	$(BOOT_DIR)/bin/jar cf $@  -C $(JUNIT_RUNNER_DIR) .

junit-jnlp-dist-dirs.txt:
	mkdir -p $(JNLP_TESTS_SERVER_DEPLOYDIR)
	mkdir -p $(JNLP_TESTS_DIR)
	mkdir -p $(JNLP_TESTS_ENGINE_DIR)
	cd $(JNLP_TESTS_SRCDIR)/simple/ ; \
	find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/$@

stamps/netx-dist-tests-prepare-reproducers.stamp: junit-jnlp-dist-dirs.txt
	simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-dirs.txt `); \
	for dir in "$${simpleReproducers[@]}" ; do \
	  mkdir -p $(JNLP_TESTS_DIR)/$$dir ; \
	  $(BOOT_DIR)/bin/javac -d  $(JNLP_TESTS_DIR)/$$dir/ $(JNLP_TESTS_SRCDIR)/simple/$$dir/srcs/* ; \
	  d=`pwd` ; \
	  cd $(JNLP_TESTS_DIR)/$$dir/ ; \
	    $(BOOT_DIR)/bin/jar cf $(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar * ; \
	  cd $$d ; \
	  cp -R $(JNLP_TESTS_SRCDIR)/simple/$$dir/resources/*  $(JNLP_TESTS_SERVER_DEPLOYDIR)/ ; \
	done ; \
	mkdir -p stamps && \
	touch $@

netx-dist-tests-source-files.txt:
	find $(JNLP_TESTS_ENGINE_SRCDIR) -name '*.java' | sort > $@

stamps/netx-dist-tests-compile.stamp: stamps/netx.stamp \
 junit-jnlp-dist-dirs.txt netx-dist-tests-source-files.txt
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(JNLP_TESTS_ENGINE_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar \
	 @netx-dist-tests-source-files.txt && \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-compile-testcases.stamp: stamps/netx.stamp junit-jnlp-dist-dirs.txt\
 netx-dist-tests-source-files.txt stamps/netx-dist-tests-compile.stamp
	simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-dirs.txt `); \
	for dir in "$${simpleReproducers[@]}" ; do \
	  $(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	  -d $(JNLP_TESTS_ENGINE_DIR) \
	  -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar:$(JNLP_TESTS_ENGINE_DIR) \
	  $(JNLP_TESTS_SRCDIR)/simple/$$dir/testcases/* ; \
	done ; \
	mkdir -p stamps && \
	touch $@

run-netx-dist-tests: all-local stamps/netx.stamp junit-jnlp-dist-dirs.txt stamps/netx-dist-tests-prepare-reproducers.stamp \
 stamps/netx-dist-tests-compile.stamp stamps/netx-dist-tests-compile-testcases.stamp $(JUNIT_RUNNER_JAR) $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)
	cd $(JNLP_TESTS_ENGINE_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  class_names="$$class_names $$class_name" ; \
	done ; \
	echo $$class_names ; \
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):. \
	  $(BOOT_DIR)/bin/java -Dtest.server.dir=$(JNLP_TESTS_SERVER_DEPLOYDIR) -Djavaws.build.bin=$(DESTDIR)$(bindir)/javaws \
	 -Xbootclasspath:$(RUNTIME) CommandLine $$class_names \
	  > stdout.log 2> stderr.log ; \
	 cat stdout.log ; \
	 cat stderr.log >&2
	-xsltproc $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml > $(TESTS_DIR)/index_reproducers.html

netx-unit-tests-source-files.txt:
	find $(NETX_UNIT_TEST_SRCDIR) -name '*.java' | sort > $@

stamps/netx-unit-tests-compile.stamp: stamps/netx.stamp \
 netx-unit-tests-source-files.txt
	mkdir -p $(NETX_UNIT_TEST_DIR) && \
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(NETX_UNIT_TEST_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar \
	 @netx-unit-tests-source-files.txt && \
	mkdir -p stamps && \
	touch $@

$(TESTS_DIR)/$(REPORT_STYLES_DIRNAME):
	mkdir $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)
	cp $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/*.css $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)/
	cp $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/*.js $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)/

run-netx-unit-tests: stamps/netx-unit-tests-compile.stamp \
 $(JUNIT_RUNNER_JAR) $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) 
	filename=" " ; \
	cd $(NETX_UNIT_TEST_SRCDIR) ; \
	for file in `find . -type f \! -iname "*.java"`; do\
		filename=`echo $$file `; \
		cp --parents $$filename $(NETX_UNIT_TEST_DIR) ; \
	done ; \
	cd $(NETX_UNIT_TEST_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  class_names="$$class_names $$class_name" ; \
	done ; \
	echo $$class_names ; \
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):. \
	  $(BOOT_DIR)/bin/java -Xbootclasspath:$(RUNTIME) CommandLine $$class_names \
	  > stdout.log 2> stderr.log ; \
	cat stdout.log ; \
	cat stderr.log >&2
	-xsltproc $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(NETX_UNIT_TEST_DIR)/tests-output.xml > $(TESTS_DIR)/index_unit.html

clean-netx-tests: clean-netx-unit-tests clean-junit-runner clean-netx-dist-tests
	if [ -e $(TESTS_DIR)/netx ]; then \
	  rmdir $(TESTS_DIR)/netx ; \
	fi

clean-junit-runner:
	rm -f junit-runner-source-files.txt
	rm -rf $(JUNIT_RUNNER_DIR)
	rm -f $(JUNIT_RUNNER_JAR)

clean-netx-unit-tests: clean_tests_reports
	rm -f netx-unit-tests-source-files.txt
	rm -rf $(NETX_UNIT_TEST_DIR)
	rm -f stamps/netx-unit-tests-compile.stamp

clean_tests_reports:
	rm -rf  $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)/
	rm -f  $(TESTS_DIR)/index*.html

clean-netx-dist-tests: clean_tests_reports
	rm -f netx-dist-tests-source-files.txt
	rm -rf $(JNLP_TESTS_DIR)
	rm -rf $(JNLP_TESTS_SERVER_DEPLOYDIR)
	rm -rf $(JNLP_TESTS_ENGINE_DIR)
	rm -f stamps/netx-dist-tests-compile.stamp
	rm -f stamps/netx-dist-tests-prepare-reproducers.stamp
	rm -f stamps/netx-dist-tests-compile-testcases.stamp
	rm -f junit-jnlp-dist-dirs.txt

# plugin tests

if ENABLE_PLUGIN
stamps/plugin-tests.stamp: $(PLUGIN_TEST_SRCS) stamps/plugin.stamp
	mkdir -p plugin/tests/LiveConnect
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d plugin/tests/LiveConnect \
	 -classpath liveconnect/lib/classes.jar \
	 $(PLUGIN_TEST_SRCS) ;
	$(BOOT_DIR)/bin/jar cf plugin/tests/LiveConnect/PluginTest.jar \
	  plugin/tests/LiveConnect/*.class ;
	cp -pPR $(SRC_DIR_LINK) $(abs_top_srcdir)/plugin/tests/LiveConnect/*.{js,html} \
	  plugin/tests/LiveConnect ;
	echo "Done. Now launch \"firefox file://`pwd`/index.html\"" ;
	mkdir -p stamps
	touch stamps/plugin-tests.stamp
endif

# Bootstrap Directory Targets
# ===========================

stamps/native-ecj.stamp: 
	mkdir -p stamps ; \
	if test "x$(GCJ)" != "xno"; then \
	  $(GCJ) $(IT_CFLAGS) -Wl,-Bsymbolic -findirect-dispatch -o native-ecj \
	    --main=org.eclipse.jdt.internal.compiler.batch.Main ${ECJ_JAR} ; \
	fi ; \
	touch stamps/native-ecj.stamp

clean-native-ecj:
	rm -f native-ecj
	rm -rf stamps/native-ecj.stamp

# bootstrap
stamps/bootstrap-directory.stamp: stamps/native-ecj.stamp
	mkdir -p $(BOOT_DIR)/bin stamps/
	ln -sf $(JAVA) $(BOOT_DIR)/bin/java
	ln -sf $(JAR) $(BOOT_DIR)/bin/jar
	ln -sf $(abs_top_builddir)/javac $(BOOT_DIR)/bin/javac
	ln -sf $(JAVADOC) $(BOOT_DIR)/bin/javadoc
	mkdir -p $(BOOT_DIR)/jre/lib && \
	ln -s $(SYSTEM_JDK_DIR)/jre/lib/rt.jar $(BOOT_DIR)/jre/lib && \
	if [ -e $(SYSTEM_JDK_DIR)/jre/lib/jsse.jar ] ; then \
	  ln -s $(SYSTEM_JDK_DIR)/jre/lib/jsse.jar $(BOOT_DIR)/jre/lib ; \
	else \
	  ln -s rt.jar $(BOOT_DIR)/jre/lib/jsse.jar ; \
	fi
	ln -sf $(SYSTEM_JDK_DIR)/jre/lib/$(JRE_ARCH_DIR) \
	  $(BOOT_DIR)/jre/lib/ && \
	if ! test -d $(BOOT_DIR)/jre/lib/$(INSTALL_ARCH_DIR); \
	  then \
	  ln -sf ./$(JRE_ARCH_DIR) \
	    $(BOOT_DIR)/jre/lib/$(INSTALL_ARCH_DIR); \
	fi;
	mkdir -p $(BOOT_DIR)/include && \
	for i in $(SYSTEM_JDK_DIR)/include/*; do \
	  test -r $$i | continue; \
	  i=`basename $$i`; \
	  rm -f $(BOOT_DIR)/include/$$i; \
	  ln -s $(SYSTEM_JDK_DIR)/include/$$i $(BOOT_DIR)/include/$$i; \
	done
	mkdir -p stamps
	touch stamps/bootstrap-directory.stamp

clean-bootstrap-directory:
	rm -rf $(BOOT_DIR)
	if [ -e ${abs_top_builddir}/bootstrap ] ; then \
	  rmdir ${abs_top_builddir}/bootstrap ; \
	fi
	rm -f stamps/bootstrap-directory.stamp

# Target Aliases
# ===============

add-netx: stamps/add-netx.stamp

add-netx-debug: stamps/add-netx-debug.stamp

netx: stamps/netx.stamp

netx-dist: stamps/netx-dist.stamp

plugin: stamps/plugin.stamp

plugin-tests: stamps/plugin-tests.stamp
