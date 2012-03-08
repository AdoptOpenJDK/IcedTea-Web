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
KEYSTORE_NAME=teststore.ks

JUNIT_RUNNER_JAR=$(abs_top_builddir)/junit-runner.jar
UNIT_CLASS_NAMES = $(abs_top_builddir)/unit_class_names
REPRODUCERS_CLASS_NAMES = $(abs_top_builddir)/reproducers_class_names
META_MANIFEST = META-INF/MANIFEST.MF

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
  JUNIT_TESTS=stamps/run-netx-unit-tests.stamp
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
  -e 's|[@]JRE[@]|$(SYSTEM_JRE_DIR)|g'

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
 clean-tests check-local clean-launchers check-pac-functions stamps/run-netx-unit-tests.stamp clean-netx-tests \
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
	   $(DEFS) $(VERSION_DEFS) \
	  -DJDK_UPDATE_VERSION="\"$(JDK_UPDATE_VERSION)\"" \
	  -DPLUGIN_NAME="\"IcedTea-Web Plugin\"" \
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DPACKAGE_URL="\"$(PACKAGE_URL)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
	  -DICEDTEA_WEB_JRE="\"$(SYSTEM_JRE_DIR)\"" \
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

stamps/junit-jnlp-dist-dirs: junit-jnlp-dist-simple.txt junit-jnlp-dist-signed.txt
	mkdir -p $(JNLP_TESTS_SERVER_DEPLOYDIR)
	mkdir -p $(JNLP_TESTS_DIR)
	mkdir -p $(JNLP_TESTS_ENGINE_DIR)
	touch $@

junit-jnlp-dist-simple.txt:
	cd $(JNLP_TESTS_SRCDIR)/simple/ ; \
	find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/$@

junit-jnlp-dist-signed.txt: 
	cd $(JNLP_TESTS_SRCDIR)/signed/ ; \
	find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/$@

stamps/netx-dist-tests-prepare-reproducers.stamp: stamps/junit-jnlp-dist-dirs
	types=(simple signed); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    echo "processing: $$dir" ; \
	    mkdir -p "$(JNLP_TESTS_DIR)/$$dir" ; \
	    d=`pwd` ; \
	    cd "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/srcs/" ; \
	    srcFiles=`find . -mindepth 1 -type f -name "*.java" | sed "s/.\/*//"` ; \
	    notSrcFiles=`find . -mindepth 1 -type f \! -name "*.java" | sed "s/.\/*//"` ; \
	    $(BOOT_DIR)/bin/javac -d  "$(JNLP_TESTS_DIR)/$$dir/" $$srcFiles ; \
	    if [ -n "$$notSrcFiles" ] ; then \
	      cp -R --parents "$$notSrcFiles" "$(JNLP_TESTS_DIR)/$$dir/" ; \
	    fi ; \
	    cd "$(JNLP_TESTS_DIR)/$$dir/" ; \
	    if [ -f $(META_MANIFEST) ]; \
	    then \
	      $(BOOT_DIR)/bin/jar cfm "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" $(META_MANIFEST) * ; \
	    else \
	      $(BOOT_DIR)/bin/jar cf "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" * ; \
	    fi; \
	    cd "$$d" ; \
	    cp -R "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/resources/"*  $(JNLP_TESTS_SERVER_DEPLOYDIR)/ ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-sign-some-reproducers.stamp: stamps/netx-dist-tests-prepare-reproducers.stamp
	alias=icedteaweb; \
	keystore=$(abs_top_builddir)/$(KEYSTORE_NAME); \
	pass=123456789; \
	$(BOOT_DIR)/bin/keytool -genkey -alias $$alias -keystore $$keystore -keypass $$pass -storepass $$pass -dname "cn=$$alias, ou=$$alias, o=$$alias, c=$$alias" ; \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	signedReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-signed.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	for dir in "$${signedReproducers[@]}" ; do \
	 $(BOOT_DIR)/bin/jarsigner -keystore $$keystore -storepass $$pass -keypass $$pass  "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar"  $$alias ; \
 	done ; \
 	mkdir -p stamps && \
 	touch $@

netx-dist-tests-source-files.txt:
	find $(JNLP_TESTS_ENGINE_SRCDIR) -name '*.java' | sort > $@

stamps/netx-dist-tests-compile.stamp: stamps/netx.stamp \
 stamps/junit-jnlp-dist-dirs netx-dist-tests-source-files.txt
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(JNLP_TESTS_ENGINE_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar \
	 @netx-dist-tests-source-files.txt && \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-compile-testcases.stamp: stamps/netx.stamp stamps/junit-jnlp-dist-dirs \
 netx-dist-tests-source-files.txt stamps/netx-dist-tests-compile.stamp
	types=(simple signed); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    $(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	    -d $(JNLP_TESTS_ENGINE_DIR) \
	    -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar:$(JNLP_TESTS_ENGINE_DIR) \
	    "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/testcases/"* ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

$(REPRODUCERS_CLASS_NAMES):
	cd $(JNLP_TESTS_ENGINE_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  class_names="$$class_names $$class_name" ; \
	done ; \
	echo $$class_names > $(REPRODUCERS_CLASS_NAMES)

stamps/run-netx-dist-tests.stamp: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp launcher.build/$(javaws) \
 javaws.desktop stamps/docs.stamp launcher.build/$(itweb_settings) itweb-settings.desktop \
 stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-sign-some-reproducers.stamp \
 stamps/netx-dist-tests-compile.stamp stamps/netx-dist-tests-compile-testcases.stamp $(JUNIT_RUNNER_JAR) \
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) $(REPRODUCERS_CLASS_NAMES)
	cd $(JNLP_TESTS_ENGINE_DIR) ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):. \
	  $(BOOT_DIR)/bin/java -Dtest.server.dir=$(JNLP_TESTS_SERVER_DEPLOYDIR) -Djavaws.build.bin=$(DESTDIR)$(bindir)/javaws \
	 -Xbootclasspath:$(RUNTIME) CommandLine $$class_names \
	  > stdout.log 2> stderr.log ; \
	 cat stdout.log ; \
	 cat stderr.log >&2
if WITH_XSLTPROC
	$(XSLTPROC) $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml > $(TESTS_DIR)/index_reproducers.html
endif
	touch $@

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

$(UNIT_CLASS_NAMES):
	cd $(NETX_UNIT_TEST_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  class_names="$$class_names $$class_name" ; \
	done ; \
	echo $$class_names > $(UNIT_CLASS_NAMES);

stamps/run-netx-unit-tests.stamp: stamps/netx-unit-tests-compile.stamp $(JUNIT_RUNNER_JAR)  \
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)  $(UNIT_CLASS_NAMES)
	filename=" " ; \
	cd $(NETX_UNIT_TEST_SRCDIR) ; \
	for file in `find . -type f \! -iname "*.java"`; do\
		filename=`echo $$file `; \
		cp --parents $$filename $(NETX_UNIT_TEST_DIR) ; \
	done ; \
	cd $(NETX_UNIT_TEST_DIR) ; \
	class_names=`cat $(UNIT_CLASS_NAMES)` ; \
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):. \
	  $(BOOT_DIR)/bin/java -Xbootclasspath:$(RUNTIME) CommandLine $$class_names \
	  > stdout.log 2> stderr.log ; \
	cat stdout.log ; \
	cat stderr.log >&2
if WITH_XSLTPROC
	$(XSLTPROC) $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(NETX_UNIT_TEST_DIR)/tests-output.xml > $(TESTS_DIR)/index_unit.html
endif
	touch $@

#warning,  during this target tests.build/netx/unit/tests-output.xml is backup and rewriten (but not coresponding html file)
#xml results run from emma sandbox, however, can be wrong, co the new tests-output.xml is then renamed and orginal one restored
#you can add -ix "-*Test*" -ix "-*test*"  to ignore all test cases from statistics
stamps/run-unit-test-code-coverage.stamp: check  $(UNIT_CLASS_NAMES);
if WITH_EMMA
	mv $(NETX_UNIT_TEST_DIR)/tests-output.xml  $(NETX_UNIT_TEST_DIR)/tests-output.xml_noEmma ; \
	cd $(NETX_UNIT_TEST_DIR) ; \
	class_names=`cat $(UNIT_CLASS_NAMES)` ; \
	$(BOOT_DIR)/bin/java -Xbootclasspath:$(RUNTIME) -cp $(EMMA_JAR) -Demma.report.html.out.encoding=UTF-8 emmarun \
	 -Dreport.html.out.encoding=UTF-8 \
	 -raw \
	 -sp $(NETX_SRCDIR) \
	 -sp $(NETX_UNIT_TEST_SRCDIR) \
	 -sp $(JUNIT_RUNNER_SRCDIR) \
	 -r html \
	 -r xml \
	 -cp $(NETX_DIR)/lib/classes.jar \
	 -cp $(JUNIT_JAR) \
	 -cp $(JUNIT_RUNNER_JAR) \
	 -cp $(BOOT_DIR)/jre/lib/rt.jar \
	 -cp $(BOOT_DIR)/jre/lib/jsse.jar \
	 -cp $(RHINO_RUNTIME) \
	 -cp . \
	 -ix "-org.junit.*" \
	 -ix "-junit.*" \
	 CommandLine $$class_names ;  \
	mv $(NETX_UNIT_TEST_DIR)/tests-output.xml  $(NETX_UNIT_TEST_DIR)/tests-output_withEmma.xml ; \
	mv $(NETX_UNIT_TEST_DIR)/tests-output.xml_noEmma  $(NETX_UNIT_TEST_DIR)/tests-output.xml ; 
else 
	echo "Sorry, coverage report cant be run without emma installed. Try install emma or specify with-emma value" ;
	exit 5
endif 
	touch $@

#warning,  during this target tests.build/netx/jnlp_testsengine/tests-output.xml is backup and rewriten (but not coresponding html file)
#xml results run from emma sandbox, however, can be wrong, co the new tests-output.xml is then renamed and orginal one restored
stamps/run-reproducers-test-code-coverage.stamp: stamps/run-netx-dist-tests.stamp $(REPRODUCERS_CLASS_NAMES)
if WITH_EMMA
	cd $(TESTS_DIR) ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml  $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml_noEmma ; \
	echo "backuping javaws and netx.jar in $(DESTDIR)" ; \
	netx_backup=$(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx_backup.jar ; \
	javaws_backup=$(DESTDIR)$(bindir)/javaws_backup ; \
	mv $(DESTDIR)$(bindir)/javaws $$javaws_backup ;  \
	mv $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar $$netx_backup ; \
	nw_bootclasspath="$(LAUNCHER_BOOTCLASSPATH):$(EMMA_JAR):$$netx_backup" ; \
	instructed_dir=$(TESTS_DIR)/instr ; \
	echo "instrumenting netx.jar from $$netx_backup through $$instructed_dir" ; \
	$(BOOT_DIR)/bin/java -cp  $(EMMA_JAR) emma instr -d $$instructed_dir -ip $$netx_backup ; \
	pushd $$instructed_dir ; \
	$(BOOT_DIR)/bin/jar -cf $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar * ; \
	popd ; \
	rm -rf $$instructed_dir ; \
	echo "patching javaws" ; \
	cat $$javaws_backup | sed "s,$(LAUNCHER_BOOTCLASSPATH),$$nw_bootclasspath," > $(DESTDIR)$(bindir)/javaws ; \
	chmod 777 $(DESTDIR)$(bindir)/javaws ; \
	testcases_srcs=( ) ; \
	k=0 ; \
	types=(simple signed); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    testcases_srcs[k]="-sp" ; \
	    k=$$((k+1)) ; \
	    testcases_srcs[k]="$(JNLP_TESTS_SRCDIR)/$$which/$$dir/testcases/" ; \
	    k=$$((k+1)) ; \
	    done ; \
	  done ; \
	cd $(JNLP_TESTS_ENGINE_DIR) ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	$(BOOT_DIR)/bin/java  \
	 -Dtest.server.dir=$(JNLP_TESTS_SERVER_DEPLOYDIR) \
	 -Djavaws.build.bin=$(DESTDIR)$(bindir)/javaws \
	 -Xbootclasspath:$(RUNTIME) -cp $(EMMA_JAR) emmarun  \
	   -raw \
	   -cp $(NETX_DIR)/lib/classes.jar \
	   -cp $(JUNIT_JAR) \
	   -cp $(JUNIT_RUNNER_JAR) \
	   -cp $(BOOT_DIR)/jre/lib/rt.jar \
	   -cp $(BOOT_DIR)/jre/lib/jsse.jar \
	   -cp $(RHINO_RUNTIME) \
	   -cp . \
	   -ix "-org.junit.*" \
	   -ix "-junit.*" \
	   CommandLine $$class_names ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/coverage.ec $(JNLP_TESTS_ENGINE_DIR)/coverageX.ec ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/coverage.es $(JNLP_TESTS_ENGINE_DIR)/coverageX.es ; \
	$(BOOT_DIR)/bin/java -cp $(EMMA_JAR) emma merge \
	  -in $(TESTS_DIR)/coverage.em \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverageX.ec \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverageX.es ; \
	$(BOOT_DIR)/bin/java -cp $(EMMA_JAR)  -Demma.report.html.out.encoding=UTF-8 emma report \
	  -Dreport.html.out.encoding=UTF-8 \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverage.es  \
	  -sp $(NETX_SRCDIR) \
	  -sp $(NETX_UNIT_TEST_SRCDIR) \
	  -sp $(JUNIT_RUNNER_SRCDIR) \
	  -sp $(JNLP_TESTS_ENGINE_SRCDIR) \
	  -r html \
	  -r xml \
	  "$${testcases_srcs[@]}" ; \
	echo "restoring javaws and netx.jar in $(DESTDIR)" ; \
	rm -f $(DESTDIR)$(bindir)/javaws $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar ; \
	rm -f $(DESTDIR)$(bindir)/javaws;  \
	mv $$javaws_backup $(DESTDIR)$(bindir)/javaws;  \
	mv $$netx_backup $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml  $(JNLP_TESTS_ENGINE_DIR)/tests-output_withEmma.xml ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml_noEmma  $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml ; \
	rm $(JNLP_TESTS_ENGINE_DIR)/coverage.txt ; 
else
	echo "Sorry, coverage report cant be run without emma installed. Try install emma or specify with-emma value" ;
	exit 5
endif
	touch $@

run-test-code-coverage: run-unit-test-code-coverage run-reproducers-test-code-coverage
if WITH_EMMA
	cd $(TESTS_DIR) ; \
	k=0 ; \
	types=(simple signed); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    testcases_srcs[k]="-sp" ; \
	    k=$$((k+1)) ; \
	    testcases_srcs[k]="$(JNLP_TESTS_SRCDIR)/$$which/$$dir/testcases/" ; \
	    k=$$((k+1)) ; \
	  done ; \
	done ; \
	$(BOOT_DIR)/bin/java -cp $(EMMA_JAR) emma merge \
	 -in $(NETX_UNIT_TEST_DIR)/coverage.es \
	 -in $(JNLP_TESTS_ENGINE_DIR)/coverage.es ; \
	$(BOOT_DIR)/bin/java -cp $(EMMA_JAR)  -Demma.report.html.out.encoding=UTF-8 emma report \
	 -Dreport.html.out.encoding=UTF-8 \
	 -in $(TESTS_DIR)/coverage.es \
	 -in $(TESTS_DIR)/coverage.em \
	 -sp $(NETX_SRCDIR) \
	 -sp $(NETX_UNIT_TEST_SRCDIR) \
	 -sp $(JUNIT_RUNNER_SRCDIR) \
	 -sp $(JNLP_TESTS_ENGINE_SRCDIR) \
	 "$${testcases_srcs[@]}" \
	 -r html \
	 -r xml ; 
else
	echo "Sorry, coverage report cant be run without emma installed. Try install emma or specify with-emma value" ;
	exit 5
endif

clean-netx-tests: clean-netx-unit-tests clean-junit-runner clean-netx-dist-tests clean-test-code-coverage
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
	rm -f $(UNIT_CLASS_NAMES)
	rm -f stamps/run-netx-unit-tests.stamp
	rm -f stamps/netx-unit-tests-compile.stamp

clean_tests_reports:
	rm -rf  $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)/
	rm -f  $(TESTS_DIR)/index*.html

clean-netx-dist-tests: clean_tests_reports
	rm -f netx-dist-tests-source-files.txt
	rm -rf $(JNLP_TESTS_DIR)
	rm -rf $(JNLP_TESTS_SERVER_DEPLOYDIR)
	rm -rf $(JNLP_TESTS_ENGINE_DIR)
	rm -f stamps/junit-jnlp-dist-dirs
	rm -f stamps/netx-dist-tests-compile.stamp
	rm -f stamps/netx-dist-tests-prepare-reproducers.stamp
	rm -f stamps/netx-dist-tests-compile-testcases.stamp
	rm -f stamps/netx-dist-tests-sign-some-reproducers.stamp
	rm -f junit-jnlp-dist-simple.txt
	rm -f junit-jnlp-dist-signed.txt
	rm -f $(REPRODUCERS_CLASS_NAMES)
	rm -f $(abs_top_builddir)/$(KEYSTORE_NAME)
	rm -f stamps/run-netx-dist-tests.stamp

clean-unit-test-code-coverage:
	if [ -e stamps/run-unit-test-code-coverage.stamp ]; then \
	  rm -rf $(NETX_UNIT_TEST_DIR)/coverage ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/coverage.xml ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/coverageX.es ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/coverageX.ec ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/coverage.es ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/tests-output_withEmma.xml ; \
	  rm -f stamps/run-unit-test-code-coverage.stamp ; \
	fi

clean-reproducers-test-code-coverage:
	if [ -e stamps/run-reproducers-test-code-coverage.stamp ]; then \
	 rm -rf $(JNLP_TESTS_ENGINE_DIR)/coverage ; \
	 rm -f $(JNLP_TESTS_ENGINE_DIR)/coverage.xml ; \
	 rm -f $(JNLP_TESTS_ENGINE_DIR)/coverage.es ; \
	 rm -f $(JNLP_TESTS_ENGINE_DIR)/tests-output_withEmma.xml ; \
	 rm -f stamps/run-reproducers-test-code-coverage.stamp ; \
	fi

clean-test-code-coverage: clean-unit-test-code-coverage clean-reproducers-test-code-coverage
	if [ -e $(TESTS_DIR)/coverage.xml ]; then \
	  rm -rf $(TESTS_DIR)/coverage  ; \
	  rm -f $(TESTS_DIR)/coverage.xml  ; \
	  rm -f $(TESTS_DIR)/coverage.es  ; \
	  rm -f $(TESTS_DIR)/coverage.em ; \
	fi


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
	if [ -e "$(KEYTOOL)" ] ; then \
	  ln -sf $(KEYTOOL) $(BOOT_DIR)/bin/keytool ;\
	else \
	  echo "#! /bin/sh" > $(BOOT_DIR)/bin/keytool ;\
	  echo "echo \"keytool not exist on your system, signed part of reproducers test will fail\"" >> $(BOOT_DIR)/bin/keytool ;\
	  chmod 777 $(BOOT_DIR)/bin/keytool ;\
	fi
	if [ -e "$(JARSIGNER)" ] ; then \
	  ln -sf $(JARSIGNER) $(BOOT_DIR)/bin/jarsigner ;\
	else \
	  echo "#! /bin/sh" > $(BOOT_DIR)/bin/jarsigner ;\
	  echo "echo \"jarsigner not exist on your system, signed part of reproducers test will fail\"" >> $(BOOT_DIR)/bin/jarsigner ;\
	  chmod 777 $(BOOT_DIR)/bin/jarsigner ;\
	fi
	mkdir -p $(BOOT_DIR)/jre/lib && \
	ln -s $(SYSTEM_JRE_DIR)/lib/rt.jar $(BOOT_DIR)/jre/lib && \
	if [ -e $(SYSTEM_JRE_DIR)/lib/jsse.jar ] ; then \
	  ln -s $(SYSTEM_JRE_DIR)/lib/jsse.jar $(BOOT_DIR)/jre/lib ; \
	else \
	  ln -s rt.jar $(BOOT_DIR)/jre/lib/jsse.jar ; \
	fi
	ln -sf $(SYSTEM_JRE_DIR)/lib/$(JRE_ARCH_DIR) \
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

run-netx-unit-tests: stamps/run-netx-unit-tests.stamp

run-netx-dist-tests: stamps/run-netx-dist-tests.stamp

run-unit-test-code-coverage: stamps/run-unit-test-code-coverage.stamp

run-reproducers-test-code-coverage: stamps/run-reproducers-test-code-coverage.stamp
