# Source directories

export NETX_DIR = $(abs_top_builddir)/netx.build
export NETX_SRCDIR = $(abs_top_srcdir)/netx
export NETX_RESOURCE_DIR=$(NETX_SRCDIR)/net/sourceforge/jnlp/resources
export NETX_EXTRA_DIR=$(abs_top_srcdir)/extra/net/sourceforge/javaws/about/resources
export NETX_EXTRA_DIST_DIR=$(abs_top_builddir)/extra-lib/net/sourceforge/javaws/about/resources

export REPORT_STYLES_DIRNAME=report-styles

export TESTS_SRCDIR=$(abs_top_srcdir)/tests
export TESTS_DIR=$(abs_top_builddir)/tests.build

export NETX_UNIT_TEST_SRCDIR=$(TESTS_SRCDIR)/netx/unit
export NETX_UNIT_TEST_DIR=$(TESTS_DIR)/netx/unit

export JUNIT_RUNNER_DIR=$(TESTS_DIR)/junit-runner
export JUNIT_RUNNER_SRCDIR=$(TESTS_SRCDIR)/junit-runner


export JNLP_TESTS_ENGINE_SRCDIR=$(TESTS_SRCDIR)/test-extensions
export JNLP_TESTS_ENGINE_TESTS_SRCDIR=$(TESTS_SRCDIR)/test-extensions-tests
export JNLP_TESTS_SRCDIR=$(TESTS_SRCDIR)/reproducers
export JNLP_TESTS_ENGINE_DIR=$(TESTS_DIR)/jnlp_testsengine
export JNLP_TESTS_ENGINE_TESTS_DIR=$(TESTS_DIR)/netx/jnlp_testsengine_tests
export JNLP_TESTS_SERVER_DEPLOYDIR=$(TESTS_DIR)/jnlp_test_server
export JNLP_TESTS_DIR=$(TESTS_DIR)/jnlp_tests
export PRIVATE_KEYSTORE_NAME=teststore.ks
export PRIVATE_KEYSTORE_PASS=123456789
export EXPORTED_TEST_CERT_PREFIX=icedteatests
export EXPORTED_TEST_CERT_SUFFIX=crt
export TEST_CERT_ALIAS=icedteaweb
export PUBLIC_KEYSTORE=${HOME}/.icedtea/security/trusted.certs
export PUBLIC_KEYSTORE_PASS=changeit

export JUNIT_RUNNER_JAR=$(abs_top_builddir)/junit-runner.jar
export UNIT_CLASS_NAMES = $(abs_top_builddir)/unit_class_names
export REPRODUCERS_CLASS_NAMES = $(abs_top_builddir)/reproducers_class_names
export REPRODUCERS_CLASS_WHITELIST = $(abs_top_srcdir)/netx-dist-tests-whitelist
export EMMA_JAVA_ARGS=-Xmx2G
export EMMA_MODIFIED_FILES=tests-output.xml ServerAccess-logs.xml stdout.log stderr.log all.log
export EMMA_BACKUP_SUFFIX=_noEmma
export EMMA_SUFFIX=_withEmma
export META_MANIFEST = META-INF/MANIFEST.MF
export SIGNED_REPRODUCERS=signed
export SIMPLE_REPRODUCERS=simple
export CUSTOM_REPRODUCERS=custom
export ALL_NONCUSTOM_REPRODUCERS=$(SIMPLE_REPRODUCERS) $(SIGNED_REPRODUCERS)
export ALL_REPRODUCERS=$(ALL_NONCUSTOM_REPRODUCERS) $(CUSTOM_REPRODUCERS)


# linking variables
export PLUGIN_LINK_NAME=libjavaplugin.so
export MOZILLA_LOCAL_PLUGINDIR=${HOME}/.mozilla/plugins
export MOZILLA_GLOBAL64_PLUGINDIR=/usr/lib64/mozilla/plugins
export MOZILLA_GLOBAL32_PLUGINDIR=/usr/lib/mozilla/plugins
export OPERA_GLOBAL64_PLUGINDIR=/usr/lib64/opera/plugins
export OPERA_GLOBAL32_PLUGINDIR=/usr/lib/opera/plugins
export BUILT_PLUGIN_LIBRARY=IcedTeaPlugin.so
export MOZILLA_LOCAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origU
export MOZILLA_GLOBAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origMG
export OPERA_GLOBAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origOG
export MOZILLA_FAMILY_TEST= "$(FIREFOX)" != "" -o "$(CHROMIUM)" != "" -o "$(CHROME)" != "" -o "$(MIDORI)" != "" -o "$(EPIPHANY)" != "" 
# end of linking variables

# Build directories

export BOOT_DIR = $(abs_top_builddir)/bootstrap/jdk1.6.0

if WITH_RHINO
  RHINO_RUNTIME=:$(RHINO_JAR)
else
  RHINO_RUNTIME=
endif

export RUNTIME = $(BOOT_DIR)/jre/lib/rt.jar:$(BOOT_DIR)/jre/lib/jsse.jar$(RHINO_RUNTIME):$(BOOT_DIR)/jre/lib/resources.jar

# Flags
export IT_CFLAGS=$(CFLAGS) $(ARCHFLAG)
export IT_JAVAC_SETTINGS=-g -encoding utf-8 $(JAVACFLAGS) $(MEMORY_LIMIT) $(PREFER_SOURCE)
export IT_LANGUAGE_SOURCE_VERSION=6
export IT_CLASS_TARGET_VERSION=6
export IT_JAVACFLAGS=$(IT_JAVAC_SETTINGS) -source $(IT_LANGUAGE_SOURCE_VERSION) -target $(IT_CLASS_TARGET_VERSION)

#
# We need the jars in bootclasspath for a couple of reasons
#  - we use classes (in the sun.applet package) loaded by the bootclassloader
#    using another classloader to load classes from the same package causes an
#    IllegalAccessException
#  - we want full privileges
#
export LAUNCHER_BOOTCLASSPATH="-Xbootclasspath/a:$(datadir)/$(PACKAGE_NAME)/netx.jar$(RHINO_RUNTIME)"
export PLUGIN_BOOTCLASSPATH='"-Xbootclasspath/a:$(datadir)/$(PACKAGE_NAME)/netx.jar:$(datadir)/$(PACKAGE_NAME)/plugin.jar$(RHINO_RUNTIME)"'

# Fake update version to work with the Deployment Toolkit script used by Oracle
# http://download.oracle.com/javase/tutorial/deployment/deploymentInDepth/depltoolkit_index.html
export JDK_UPDATE_VERSION=50

# Sources list

export PLUGIN_TEST_SRCS = $(abs_top_srcdir)/plugin/tests/LiveConnect/*.java

export NETX_PKGS = javax.jnlp net.sourceforge.nanoxml net.sourceforge.jnlp \
	net.sourceforge.jnlp.cache net.sourceforge.jnlp.config \
	net.sourceforge.jnlp.controlpanel net.sourceforge.jnlp.event \
	net.sourceforge.jnlp.runtime net.sourceforge.jnlp.security \
	net.sourceforge.jnlp.security.viewer net.sourceforge.jnlp.services \
	net.sourceforge.jnlp.tools net.sourceforge.jnlp.util

# Conditional defintions
if ENABLE_PLUGIN
export ICEDTEAPLUGIN_CLEAN = clean-IcedTeaPlugin
export LIVECONNECT_DIR = netscape sun/applet
export PLUGIN_DIR=$(abs_top_builddir)/plugin/icedteanp
export PLUGIN_SRCDIR=$(abs_top_srcdir)/plugin/icedteanp
export LIVECONNECT_SRCS = $(PLUGIN_SRCDIR)/java
export ICEDTEAPLUGIN_TARGET = $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY) stamps/liveconnect-dist.stamp
export PLUGIN_PKGS = sun.applet netscape.security netscape.javascript
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
  RHINO_TESTS=stamps/check-pac-functions.stamp
else
  RHINO_TESTS=
endif

if WITH_JUNIT
  JUNIT_TESTS=stamps/run-netx-unit-tests.stamp
else
  JUNIT_TESTS=
endif


export PLUGIN_VERSION = IcedTea-Web $(FULL_VERSION)

export EXTRA_DIST = $(top_srcdir)/netx $(top_srcdir)/plugin javaws.png javaws.desktop.in extra launcher \
 itweb-settings.desktop.in $(top_srcdir)/tests

# reproducers `D`shortcuts
export DTEST_SERVER=-Dtest.server.dir=$(JNLP_TESTS_SERVER_DEPLOYDIR)
export DJAVAWS_BUILD=-Djavaws.build.bin=$(DESTDIR)$(bindir)/$(javaws)
export DBROWSERS=-Dused.browsers=$(FIREFOX):$(CHROMIUM):$(CHROME):$(OPERA):$(MIDORI):$(EPIPHANY)
export REPRODUCERS_DPARAMETERS= $(DTEST_SERVER) $(DJAVAWS_BUILD) $(DBROWSERS) $(BROWSER_TESTS_MODIFICATION)
# end of `D`shortcuts

#exported autoconf copies
export EXPORTED_JAVAC=$(BOOT_DIR)/bin/javac
#end of exported autoconf copies

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

clean-local: clean-netx clean-plugin clean-liveconnect clean-extra  \
 clean-native-ecj clean-launchers clean-desktop-files clean-docs clean-tests clean-bootstrap-directory
	if [ -e stamps ] ; then \
	  rmdir stamps ; \
	fi

.PHONY: clean-IcedTeaPlugin clean-add-netx clean-add-netx-debug clean-add-plugin clean-add-plugin-debug \
 clean-bootstrap-directory clean-native-ecj clean-desktop-files clean-netx-docs clean-docs clean-plugin-docs \
 clean-tests check-local clean-launchers stamps/check-pac-functions.stamp stamps/run-netx-unit-tests.stamp clean-netx-tests \
 clean-junit-runner clean-netx-unit-tests

install-exec-local:
	${mkinstalldirs} $(DESTDIR)$(bindir) $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/ $(DESTDIR)$(libdir)
if ENABLE_PLUGIN
	${INSTALL_PROGRAM} $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY) $(DESTDIR)$(libdir)
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
	rm -f $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
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

$(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY): $(addprefix $(PLUGIN_DIR)/,$(PLUGIN_OBJECTS))
	cd $(PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  $(PLUGIN_OBJECTS) \
	  $(GLIB_LIBS) \
	  $(GTK_LIBS) \
	  $(MOZILLA_LIBS)\
	  -shared -o $@

clean-IcedTeaPlugin:
	rm -f $(PLUGIN_DIR)/*.o
	rm -f $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY)
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

stamps/check-pac-functions.stamp: stamps/bootstrap-directory.stamp
	./jrunscript $(abs_top_srcdir)/tests/netx/pac/pac-funcs-test.js \
	  $$(readlink -f $(abs_top_srcdir)/netx/net/sourceforge/jnlp/runtime/pac-funcs.js) ; \
	mkdir -p stamps && \
	touch $@

junit-runner-source-files.txt:
	find $(JUNIT_RUNNER_SRCDIR) -name '*.java' | sort > $@

$(JUNIT_RUNNER_JAR): junit-runner-source-files.txt stamps/netx-dist-tests-compile.stamp
	mkdir -p $(JUNIT_RUNNER_DIR) && \
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	  -d $(JUNIT_RUNNER_DIR) \
	  -classpath $(JUNIT_JAR):$(JNLP_TESTS_ENGINE_DIR) \
	  @junit-runner-source-files.txt && \
	$(BOOT_DIR)/bin/jar cf $@  -C $(JUNIT_RUNNER_DIR) .

stamps/junit-jnlp-dist-dirs: junit-jnlp-dist-simple.txt stamps/junit-jnlp-dist-signed.stamp junit-jnlp-dist-custom.txt
	mkdir -p $(JNLP_TESTS_SERVER_DEPLOYDIR)
	mkdir -p $(JNLP_TESTS_DIR)
	touch $@

junit-jnlp-dist-custom.txt:
	cd $(JNLP_TESTS_SRCDIR)/$(CUSTOM_REPRODUCERS)/ ; \
	find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/$@

junit-jnlp-dist-simple.txt:
	cd $(JNLP_TESTS_SRCDIR)/simple/ ; \
	find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/$@

stamps/junit-jnlp-dist-signed.stamp: 
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  pushd $(JNLP_TESTS_SRCDIR)/$$which/ ; \
	  find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" > $(abs_top_builddir)/junit-jnlp-dist-$$which.txt ; \
	  popd ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-prepare-reproducers.stamp: stamps/junit-jnlp-dist-dirs
	types=($(ALL_NONCUSTOM_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    echo "processing: $$dir" ; \
	    mkdir -p "$(JNLP_TESTS_DIR)/$$dir" ; \
	    if [ -e "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/srcs/" ]; then \
	      d=`pwd` ; \
	      cd "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/srcs/" ; \
	      srcFiles=`find . -mindepth 1 -type f -name "*.java" | sed "s/.\/*//"` ; \
	      notSrcFiles=`find . -mindepth 1 -type f \! -name "*.java" | sed "s/.\/*//"` ; \
	      $(BOOT_DIR)/bin/javac -cp $(NETX_DIR)/lib/classes.jar -d "$(JNLP_TESTS_DIR)/$$dir/" $$srcFiles ; \
	      if [ -n "$$notSrcFiles" ] ; then \
	        cp -R --parents $$notSrcFiles "$(JNLP_TESTS_DIR)/$$dir/" ; \
	      fi ; \
	      cd "$(JNLP_TESTS_DIR)/$$dir/" ; \
	      if [ -f $(META_MANIFEST) ]; \
	      then \
	        $(BOOT_DIR)/bin/jar cfm "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" $(META_MANIFEST) * ; \
	      else \
	        $(BOOT_DIR)/bin/jar cf "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" * ; \
	      fi; \
	      cd "$$d" ; \
	    fi; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-sign-some-reproducers.stamp: stamps/netx-dist-tests-prepare-reproducers.stamp
	keystore=$(abs_top_builddir)/$(PRIVATE_KEYSTORE_NAME); \
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  tcaw=$(TEST_CERT_ALIAS)_$$which ; \
	  $(BOOT_DIR)/bin/keytool -genkey -alias $$tcaw -keystore $$keystore -keypass $(PRIVATE_KEYSTORE_PASS) -storepass $(PRIVATE_KEYSTORE_PASS) -dname "cn=$$tcaw, ou=$$tcaw, o=$$tcaw, c=$$tcaw" ; \
	    . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  signedReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	    IFS="$$IFS_BACKUP" ; \
	  for dir in "$${signedReproducers[@]}" ; do \
	   $(BOOT_DIR)/bin/jarsigner -keystore $$keystore -storepass $(PRIVATE_KEYSTORE_PASS) -keypass $(PRIVATE_KEYSTORE_PASS)  "$(JNLP_TESTS_SERVER_DEPLOYDIR)/$$dir.jar"  $$tcaw ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/change-dots-to-paths.stamp: stamps/netx-dist-tests-sign-some-reproducers.stamp
	pushd  $(JNLP_TESTS_SERVER_DEPLOYDIR); \
	types=($(ALL_NONCUSTOM_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    if test "$${dir:0:1}" = "." ; then \
	      echo "reproducer $$dir starts with dot. It is forbidden" ; \
	      exit 5; \
	    fi; \
	    if test "$${dir:(-1)}" = "." ; then \
	      echo "reproducer $$dir ends with dot. It is forbidden" ; \
	      exit 5; \
	    fi; \
	    q=`expr index "$$dir" .`; \
	    r=$$? ; \
	    if [ $$r = 0  ]; then \
	      slashed_dir="./$${dir//.//}" ; \
	      path="`dirname $$slashed_dir`" ; \
	      file="`basename $$slashed_dir`.jar" ; \
	      echo "copying $$dir.jar to $$path as $$file" ; \
	      mkdir --parents $$path ; \
	      cp $$dir".jar" "$$path"/"$$file" ; \
	    fi ; \
	  done ; \
	done ; \
	popd ; \
	mkdir -p stamps && \
	touch $@

#this always tries to remove  previous testcert
#the code is copypasted from netx-dist-tests-remove-cert-from-public, because 
#with depending to not stamped target we always have to rerun reproducers targets
stamps/exported-test-certs.stamp: stamps/change-dots-to-paths.stamp
	-types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  $(BOOT_DIR)/bin/keytool -delete -alias $(TEST_CERT_ALIAS)_$$which -keystore $(PUBLIC_KEYSTORE) -storepass $(PUBLIC_KEYSTORE_PASS) ; \
	done ;
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  keytool -export -alias $(TEST_CERT_ALIAS)_$$which -file $(EXPORTED_TEST_CERT_PREFIX)_$$which.$(EXPORTED_TEST_CERT_SUFFIX) -storepass $(PRIVATE_KEYSTORE_PASS) -keystore $(PRIVATE_KEYSTORE_NAME) ; \
	done ;
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-import-cert-to-public: stamps/exported-test-certs.stamp
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  yes | $(BOOT_DIR)/bin/keytool -import -alias $(TEST_CERT_ALIAS)_$$which -keystore $(PUBLIC_KEYSTORE) -storepass $(PUBLIC_KEYSTORE_PASS) -file $(EXPORTED_TEST_CERT_PREFIX)_$$which.$(EXPORTED_TEST_CERT_SUFFIX) ;\
	done ;
	mkdir -p stamps && \
	touch $@

netx-dist-tests-remove-cert-from-public:
	-types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  $(BOOT_DIR)/bin/keytool -delete -alias $(TEST_CERT_ALIAS)_$$which -keystore $(PUBLIC_KEYSTORE) -storepass $(PUBLIC_KEYSTORE_PASS) ; \
	done ;
	-rm -rf stamps/netx-dist-tests-import-cert-to-public

netx-dist-tests-source-files.txt:
	find $(JNLP_TESTS_ENGINE_SRCDIR) -name '*.java' | sort > $@

stamps/netx-dist-tests-compile.stamp: stamps/netx.stamp \
 stamps/junit-jnlp-dist-dirs netx-dist-tests-source-files.txt
	mkdir -p $(JNLP_TESTS_ENGINE_DIR);
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(JNLP_TESTS_ENGINE_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar \
	 @netx-dist-tests-source-files.txt && \
	mkdir -p stamps && \
	touch $@

netx-dist-tests-tests-source-files.txt:
	find $(JNLP_TESTS_ENGINE_TESTS_SRCDIR) -name '*.java' | sort > $@

stamps/netx-dist-tests-tests-compile.stamp: stamps/junit-jnlp-dist-dirs netx-dist-tests-tests-source-files.txt  stamps/netx-dist-tests-compile.stamp
	mkdir -p $(JNLP_TESTS_ENGINE_TESTS_DIR);
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(JNLP_TESTS_ENGINE_TESTS_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar:$(JNLP_TESTS_ENGINE_DIR) \
	 @netx-dist-tests-tests-source-files.txt && \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-compile-testcases.stamp: stamps/netx.stamp stamps/junit-jnlp-dist-dirs \
 netx-dist-tests-source-files.txt stamps/netx-dist-tests-compile.stamp stamps/netx-dist-tests-tests-compile.stamp
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    $(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	      -d $(JNLP_TESTS_ENGINE_TESTS_DIR) \
	      -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar:$(JNLP_TESTS_ENGINE_DIR) \
	    "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/testcases/"* ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-copy-resources.stamp: stamps/junit-jnlp-dist-dirs
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(abs_top_srcdir)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    cp -R "$(JNLP_TESTS_SRCDIR)/$$which/$$dir/resources/"*  $(JNLP_TESTS_SERVER_DEPLOYDIR)/ ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

$(REPRODUCERS_CLASS_NAMES): $(REPRODUCERS_CLASS_WHITELIST)
	whiteListed=`cat $(REPRODUCERS_CLASS_WHITELIST)`; \
	cd $(JNLP_TESTS_ENGINE_TESTS_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  INLCUDE="NO" ; \
	  for x in $$whiteListed ; do \
	    q=`expr match "$$class_name" "$$x"`; \
	    r=$$? ; \
	    if [ $$r = 0  ]; then \
	      echo "$$class_name will be included in reproducers testcases because of $$x pattern in $(REPRODUCERS_CLASS_WHITELIST). Matching was  $$q"; \
	      INLCUDE="YES" ; \
	    fi; \
	  done; \
	  if [ "$$INLCUDE" = "YES"  ]; then \
	    class_names="$$class_names $$class_name" ; \
	  else \
	    echo "$$class_name had no match in $(REPRODUCERS_CLASS_WHITELIST). Excluding"; \
	  fi; \
	done ; \
	echo $$class_names > $(REPRODUCERS_CLASS_NAMES)

stamps/run-netx-dist-tests.stamp: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp launcher.build/$(javaws) \
 javaws.desktop stamps/docs.stamp launcher.build/$(itweb_settings) itweb-settings.desktop \
 stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/netx-dist-tests-compile.stamp stamps/netx-dist-tests-compile-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/netx-dist-tests-copy-resources.stamp\
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) $(REPRODUCERS_CLASS_NAMES) stamps/process-custom-reproducers.stamp
	cd $(JNLP_TESTS_ENGINE_DIR) ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):.:$(JNLP_TESTS_ENGINE_TESTS_DIR) \
	  $(BOOT_DIR)/bin/java $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath:$(RUNTIME) CommandLine $$class_names
if WITH_XSLTPROC
	$(XSLTPROC)  $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/logs.xsl $(JNLP_TESTS_ENGINE_DIR)/ServerAccess-logs.xml > $(TESTS_DIR)/logs_reproducers.html
	$(XSLTPROC)  --stringparam logs logs_reproducers.html $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(JNLP_TESTS_ENGINE_DIR)/tests-output.xml > $(TESTS_DIR)/index_reproducers.html
endif
	touch $@

stamps/process-custom-reproducers.stamp: stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/netx-dist-tests-compile.stamp stamps/netx-dist-tests-compile-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/netx-dist-tests-copy-resources.stamp\
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) $(REPRODUCERS_CLASS_NAMES)
	. $(abs_top_srcdir)/NEW_LINE_IFS ; \
	customReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-custom.txt `); \
	IFS="$$IFS_BACKUP" ; \
	for dir in "$${customReproducers[@]}" ; do \
	  pushd $(JNLP_TESTS_SRCDIR)/$(CUSTOM_REPRODUCERS)/$$dir/srcs; \
	  $(MAKE) prepare-reproducer ; \
	  popd ; \
	done ; \
	mkdir -p stamps && \
	touch $@

clean-custom-reproducers: junit-jnlp-dist-custom.txt
	. $(abs_top_srcdir)/NEW_LINE_IFS ; \
	customReproducers=(`cat $(abs_top_builddir)/junit-jnlp-dist-custom.txt `); \
	IFS="$$IFS_BACKUP" ; \
	for dir in "$${customReproducers[@]}" ; do \
	  pushd $(JNLP_TESTS_SRCDIR)/custom/$$dir/srcs; \
	  $(MAKE) clean-reproducer ; \
	  popd ; \
	done ; \
	rm -f stamps/process-custom-reproducers.stamp

#for global-links you must be root, for opera there do not exists user-links
#although this targets will indeed create symbolic links to enable 
#icedtea-web plugin inside browser it is intended for testing purposes
if ENABLE_PLUGIN
stamps/user-links.stamp: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp \
 launcher.build/$(javaws) stamps/netx.stamp $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) 
	if [ $(MOZILLA_FAMILY_TEST) ]  ; then  \
	  if [ -e $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) ] ; then \
	    mv -f $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME)  $(MOZILLA_LOCAL_BACKUP_FILE) ; \
	    echo "$(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) backed up as $(MOZILLA_LOCAL_BACKUP_FILE)" ; \
	  else \
	    echo "$(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) doesn't exists, nothing to be backed up to $(MOZILLA_LOCAL_BACKUP_FILE)" ; \
	  fi ; \
	  pushd $(MOZILLA_LOCAL_PLUGINDIR) ; \
	  ln -s $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) $(PLUGIN_LINK_NAME) ; \
	  echo "$(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) linked as  $$PWD/$(PLUGIN_LINK_NAME)" ; \
	  popd ; \
	fi ; \
	touch $@

restore-user-links:
	if [ $(MOZILLA_FAMILY_TEST) ]  ; then  \
	  if [ -e $(MOZILLA_LOCAL_BACKUP_FILE) ] ; then \
	    mv -f  $(MOZILLA_LOCAL_BACKUP_FILE)  $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) ; \
	    echo "$(MOZILLA_LOCAL_BACKUP_FILE) restored as $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME)" ; \
	  else \
	    rm -f $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) ; \
	    echo "$(MOZILLA_LOCAL_BACKUP_FILE) do not exists, nothing to be restored. $(MOZILLA_LOCAL_PLUGINDIR)/$(PLUGIN_LINK_NAME) removed" ; \
	  fi ; \
	fi ;
	if [ -e stamps/user-links.stamp ] ; then \
	  rm -f stamps/user-links.stamp ; \
	fi

stamps/global-links.stamp: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp launcher.build/$(javaws) \
 stamps/netx.stamp $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
	if [ $(MOZILLA_FAMILY_TEST) ]  ; then  \
	    dir="$(MOZILLA_GLOBAL32_PLUGINDIR)"  ; \
	    arch=`arch`  ; \
	    if [ "$$arch" = "x86_64" ]  ; then \
	      dir="$(MOZILLA_GLOBAL64_PLUGINDIR)"  ; \
	    fi ; \
	    if [ -e "$$dir"/$(PLUGIN_LINK_NAME) ] ; then \
	      mv -f "$$dir"/$(PLUGIN_LINK_NAME)  $(MOZILLA_GLOBAL_BACKUP_FILE) ; \
	      echo "$$dir/$(PLUGIN_LINK_NAME) backed up as  $(MOZILLA_GLOBAL_BACKUP_FILE)" ; \
	    else \
	      echo "$$dir/$(PLUGIN_LINK_NAME) do not exists, nothing to be backed up to $(MOZILLA_GLOBAL_BACKUP_FILE)" ; \
	    fi ; \
	    pushd "$$dir"  ; \
	    ln -s $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) $(PLUGIN_LINK_NAME)  ; \
	    echo "$(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) linked as  $$PWD/$(PLUGIN_LINK_NAME)" ; \
	    popd  ; \
	fi ; 
	if [ "$(OPERA)" != "" ]  ; then \
	    dir="$(OPERA_GLOBAL32_PLUGINDIR)"  ; \
	    arch=`arch`  ; \
	    if [ "$$arch" = "x86_64" ] ; then \
	      dir="$(OPERA_GLOBAL64_PLUGINDIR)" ; \
	    fi ; \
	    if [ -e "$$dir"/$(PLUGIN_LINK_NAME) ] ; then \
	      mv -f "$$dir"/$(PLUGIN_LINK_NAME)  $(OPERA_GLOBAL_BACKUP_FILE) ; \
	      echo "$$dir/$(PLUGIN_LINK_NAME) backed up as  $(OPERA_GLOBAL_BACKUP_FILE) "; \
	    else \
	      echo "$$dir/$(PLUGIN_LINK_NAME) do not exists, nothing to be backed up to $(OPERA_GLOBAL_BACKUP_FILE) "; \
	    fi ; \
	    pushd "$$dir"  ; \
	    ln -s $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) $(PLUGIN_LINK_NAME)  ; \
	    echo "$(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) linked as  $$PWD/$(PLUGIN_LINK_NAME)" ; \
	    popd  ; \
	fi   ; \
	touch $@

restore-global-links:
	if [ $(MOZILLA_FAMILY_TEST) ]  ; then  \
	    dir="$(MOZILLA_GLOBAL32_PLUGINDIR)"  ; \
	    arch=`arch`  ; \
	    if [ "$$arch" = "x86_64" ]  ; then \
	      dir="$(MOZILLA_GLOBAL64_PLUGINDIR)"  ; \
	    fi ; \
	    if [ -e $(MOZILLA_GLOBAL_BACKUP_FILE) ] ; then \
	      mv -f $(MOZILLA_GLOBAL_BACKUP_FILE) "$$dir"/$(PLUGIN_LINK_NAME) ; \
	      echo "$(MOZILLA_GLOBAL_BACKUP_FILE) restored as $$dir/$(PLUGIN_LINK_NAME)" ; \
	    else \
	      rm -f "$$dir"/$(PLUGIN_LINK_NAME) ; \
	      echo "$(MOZILLA_GLOBAL_BACKUP_FILE) do not exists, nothing to be restored. $$dir/$(PLUGIN_LINK_NAME) removed" ; \
	    fi ; \
	fi ; 
	if [ "$(OPERA)" != "" ]  ; then \
	    dir="$(OPERA_GLOBAL32_PLUGINDIR)"  ; \
	    arch=`arch`  ; \
	    if [ "$$arch" = "x86_64" ] ; then \
	      dir="$(OPERA_GLOBAL64_PLUGINDIR)" ; \
	    fi ; \
	    if [ -e $(OPERA_GLOBAL_BACKUP_FILE) ] ; then \
	      mv -f $(OPERA_GLOBAL_BACKUP_FILE) "$$dir"/$(PLUGIN_LINK_NAME) ; \
	      echo "$(OPERA_GLOBAL_BACKUP_FILE)  restored as $$dir/$(PLUGIN_LINK_NAME)" ; \
	    else \
	      rm -f "$$dir"/$(PLUGIN_LINK_NAME) ; \
	      echo "$(OPERA_GLOBAL_BACKUP_FILE) do not exist, nothing to be restored. $$dir/$(PLUGIN_LINK_NAME) removed" ; \
	    fi ; \
	fi   ;
	if [ -e stamps/global-links.stamp ] ; then \
	  rm -f stamps/global-links.stamp ; \
	fi
endif

netx-unit-tests-source-files.txt:
	find $(NETX_UNIT_TEST_SRCDIR) -name '*.java' | sort > $@

stamps/netx-unit-tests-compile.stamp: stamps/netx.stamp \
 netx-unit-tests-source-files.txt stamps/netx-dist-tests-compile.stamp
	mkdir -p $(NETX_UNIT_TEST_DIR) && \
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(NETX_UNIT_TEST_DIR) \
	 -classpath $(JUNIT_JAR):$(NETX_DIR)/lib/classes.jar:$(JNLP_TESTS_ENGINE_DIR) \
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
	CLASSPATH=$(NETX_DIR)/lib/classes.jar:$(JUNIT_JAR):$(JUNIT_RUNNER_JAR):$(JNLP_TESTS_ENGINE_DIR):. \
	  $(BOOT_DIR)/bin/java -Xbootclasspath:$(RUNTIME) CommandLine $$class_names 
if WITH_XSLTPROC
	$(XSLTPROC) $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/logs.xsl $(NETX_UNIT_TEST_DIR)/ServerAccess-logs.xml > $(TESTS_DIR)/logs_unit.html
	$(XSLTPROC) --stringparam logs logs_unit.html $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(NETX_UNIT_TEST_DIR)/tests-output.xml > $(TESTS_DIR)/index_unit.html
endif
	mkdir -p stamps && \
	touch $@

#warning,  during this target tests.build/netx/unit/tests-output.xml is backup and rewriten (but not coresponding html file)
#xml results run from emma sandbox, however, can be wrong, co the new tests-output.xml is then renamed and orginal one restored
#you can add -ix "-*Test*" -ix "-*test*"  to ignore all test cases from statistics
stamps/run-unit-test-code-coverage.stamp: stamps/netx-unit-tests-compile.stamp $(JUNIT_RUNNER_JAR)  \
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)  $(UNIT_CLASS_NAMES)
if WITH_EMMA
	cd $(NETX_UNIT_TEST_DIR) ; \
	for file in $(EMMA_MODIFIED_FILES) ; do \
	  mv $(NETX_UNIT_TEST_DIR)/$$file  $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)" ; \
	done ;\
	class_names=`cat $(UNIT_CLASS_NAMES)` ; \
	$(BOOT_DIR)/bin/java $(EMMA_JAVA_ARGS) -Xbootclasspath:$(RUNTIME) -cp $(EMMA_JAR) -Demma.report.html.out.encoding=UTF-8 emmarun \
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
	 -cp $(BOOT_DIR)/jre/lib/resources.jar \
	 -cp $(RHINO_RUNTIME) \
	 -cp $(JNLP_TESTS_ENGINE_DIR) \
	 -cp . \
	 -ix "-org.junit.*" \
	 -ix "-junit.*" \
	 CommandLine $$class_names ;  \
	for file in $(EMMA_MODIFIED_FILES) ; do \
	  mv $(NETX_UNIT_TEST_DIR)/$$file  $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_SUFFIX)" ; \
	  mv $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)"  $(NETX_UNIT_TEST_DIR)/$$file ; \
	done ;
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
	for file in $(EMMA_MODIFIED_FILES) ; do \
	  mv $(JNLP_TESTS_ENGINE_DIR)/$$file $(JNLP_TESTS_ENGINE_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)" ; \
	done ;\
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
	echo "patching $(javaws)" ; \
	cat $$javaws_backup | sed "s,$(LAUNCHER_BOOTCLASSPATH),$$nw_bootclasspath," > $(DESTDIR)$(bindir)/$(javaws) ; \
	chmod 777 $(DESTDIR)$(bindir)/$(javaws) ; \
	testcases_srcs=( ) ; \
	k=0 ; \
	types=($(ALL_REPRODUCERS)); \
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
	 $(EMMA_JAVA_ARGS) \
	  $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath:$(RUNTIME) -cp $(EMMA_JAR) emmarun  \
	   -raw \
	   -cp $(NETX_DIR)/lib/classes.jar \
	   -cp $(JUNIT_JAR) \
	   -cp $(JUNIT_RUNNER_JAR) \
	   -cp $(BOOT_DIR)/jre/lib/rt.jar \
	   -cp $(BOOT_DIR)/jre/lib/jsse.jar \
	   -cp $(BOOT_DIR)/jre/lib/resources.jar \
	   -cp $(RHINO_RUNTIME) \
	   -cp . \
	   -cp $(JNLP_TESTS_ENGINE_TESTS_DIR) \
	   -ix "-org.junit.*" \
	   -ix "-junit.*" \
	   CommandLine $$class_names ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/coverage.ec $(JNLP_TESTS_ENGINE_DIR)/coverageX.ec ; \
	mv $(JNLP_TESTS_ENGINE_DIR)/coverage.es $(JNLP_TESTS_ENGINE_DIR)/coverageX.es ; \
	$(BOOT_DIR)/bin/java $(EMMA_JAVA_ARGS) -cp $(EMMA_JAR) emma merge \
	  -in $(TESTS_DIR)/coverage.em \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverageX.ec \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverageX.es ; \
	$(BOOT_DIR)/bin/java $(EMMA_JAVA_ARGS) -cp $(EMMA_JAR)  -Demma.report.html.out.encoding=UTF-8 emma report \
	  -Dreport.html.out.encoding=UTF-8 \
	  -in $(JNLP_TESTS_ENGINE_DIR)/coverage.es  \
	  -sp $(NETX_SRCDIR) \
	  -sp $(NETX_UNIT_TEST_SRCDIR) \
	  -sp $(JUNIT_RUNNER_SRCDIR) \
	  -sp $(JNLP_TESTS_ENGINE_SRCDIR) \
	  -sp $(JNLP_TESTS_ENGINE_TESTS_SRCDIR) \
	  -r html \
	  -r xml \
	  "$${testcases_srcs[@]}" ; \
	echo "restoring javaws and netx.jar in $(DESTDIR)" ; \
	rm -f $(DESTDIR)$(bindir)/$(javaws) $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar ; \
	rm -f $(DESTDIR)$(bindir)/$(javaws);  \
	mv $$javaws_backup $(DESTDIR)$(bindir)/$(javaws);  \
	mv $$netx_backup $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar ; \
	for file in $(EMMA_MODIFIED_FILES) ; do \
	  mv $(JNLP_TESTS_ENGINE_DIR)/$$file  $(JNLP_TESTS_ENGINE_DIR)/"$$file""$(EMMA_SUFFIX)" ; \
	  mv $(JNLP_TESTS_ENGINE_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)"  $(JNLP_TESTS_ENGINE_DIR)/$$file ; \
	done ;\
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
	types=($(ALL_REPRODUCERS)); \
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
	$(BOOT_DIR)/bin/java $(EMMA_JAVA_ARGS) -cp $(EMMA_JAR) emma merge \
	 -in $(NETX_UNIT_TEST_DIR)/coverage.es \
	 -in $(JNLP_TESTS_ENGINE_DIR)/coverage.es ; \
	$(BOOT_DIR)/bin/java $(EMMA_JAVA_ARGS) -cp $(EMMA_JAR)  -Demma.report.html.out.encoding=UTF-8 emma report \
	 -Dreport.html.out.encoding=UTF-8 \
	 -in $(TESTS_DIR)/coverage.es \
	 -in $(TESTS_DIR)/coverage.em \
	 -sp $(NETX_SRCDIR) \
	 -sp $(NETX_UNIT_TEST_SRCDIR) \
	 -sp $(JUNIT_RUNNER_SRCDIR) \
	 -sp $(JNLP_TESTS_ENGINE_SRCDIR) \
	 -sp $(JNLP_TESTS_ENGINE_TESTS_SRCDIR) \
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
	rm -f stamps/check-pac-functions.stamp
	rm -f stamps/run-netx-unit-tests.stamp
	rm -f stamps/netx-unit-tests-compile.stamp

clean_tests_reports:
	rm -rf  $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)/
	rm -f  $(TESTS_DIR)/*.html

clean-netx-dist-tests: clean_tests_reports netx-dist-tests-remove-cert-from-public clean-custom-reproducers
	rm -f netx-dist-tests-source-files.txt
	rm -rf $(JNLP_TESTS_ENGINE_TESTS_DIR)
	rm -rf $(JNLP_TESTS_DIR)
	rm -rf $(JNLP_TESTS_SERVER_DEPLOYDIR)
	rm -rf $(JNLP_TESTS_ENGINE_DIR)
	rm -f stamps/junit-jnlp-dist-dirs
	rm -f stamps/netx-dist-tests-compile.stamp
	rm -f stamps/netx-dist-tests-tests-compile.stamp
	rm -f stamps/netx-dist-tests-prepare-reproducers.stamp
	rm -f stamps/netx-dist-tests-compile-testcases.stamp
	rm -f stamps/netx-dist-tests-copy-resources.stamp
	rm -f stamps/netx-dist-tests-sign-some-reproducers.stamp
	rm -f stamps/change-dots-to-paths.stamp
	rm -f junit-jnlp-dist-simple.txt
	rm -f junit-jnlp-dist-custom.txt
	rm -f netx-dist-tests-tests-source-files.txt
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  rm -f junit-jnlp-dist-$$which.txt ; \
	  rm -f $(EXPORTED_TEST_CERT_PREFIX)_$$which.$(EXPORTED_TEST_CERT_SUFFIX) ; \
	done ;
	rm -f stamps/exported-test-certs.stamp
	rm -f stamps/junit-jnlp-dist-signed.stamp
	rm -f $(REPRODUCERS_CLASS_NAMES)
	rm -f $(abs_top_builddir)/$(PRIVATE_KEYSTORE_NAME)
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
	if [ -e $(SYSTEM_JRE_DIR)/lib/resources.jar ] ; then \
	  ln -s $(SYSTEM_JRE_DIR)/lib/resources.jar $(BOOT_DIR)/jre/lib ; \
	else \
	  ln -s rt.jar $(BOOT_DIR)/jre/lib/resources.jar ; \
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

check-pac-functions: stamps/check-pac-functions.stamp

run-netx-unit-tests: stamps/run-netx-unit-tests.stamp

links: stamps/global-links.stamp

user-links: stamps/user-links.stamp

run-netx-dist-tests: stamps/run-netx-dist-tests.stamp

run-unit-test-code-coverage: stamps/run-unit-test-code-coverage.stamp

run-reproducers-test-code-coverage: stamps/run-reproducers-test-code-coverage.stamp
