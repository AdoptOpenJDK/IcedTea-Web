if WINDOWS
    export CLASSPATH_SEPARATOR = ;
    export ICON = javaws.ico
else
    export CLASSPATH_SEPARATOR = :
    export ICON = javaws.png
endif

# Functions to compose path/classpath

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
define joinsegments
$(subst $(SPACE),$(CLASSPATH_SEPARATOR),$(strip $1))
endef
define composeclasspath
$(if $(call joinsegments,$1),-classpath "$(call joinsegments,$1)",)
endef

# Source directories
if WINDOWS
	export TOP_BUILD_DIR := $(shell cygpath -p -m "$(abs_top_builddir)")
	export TOP_SRC_DIR := $(shell cygpath -p -m "$(abs_top_srcdir)")
	export prefix := $(shell cygpath -p -m "$(prefix)")
	export CPA="-v"
else
	export TOP_BUILD_DIR = $(abs_top_builddir)
	export TOP_SRC_DIR = $(abs_top_srcdir)
	export CPA="-av"
	WIN_LAUNCHERS=
endif


MODULARJDK_ARGS_FILE=itw-modularjdk.args

if WINDOWS_OR_INCLUDE_BATS
	WIN_LAUNCHERS=launcher.build/$(javaws).bat launcher.build/$(itweb_settings).bat  launcher.build/$(policyeditor).bat
else
	WIN_LAUNCHERS=
endif

if ENABLE_SHELL_LAUNCHERS
	SHELL_LAUNCHERS=$(WIN_LAUNCHERS) launcher.build/$(MODULARJDK_ARGS_FILE) launcher.build/$(javaws).sh launcher.build/$(itweb_settings).sh launcher.build/$(policyeditor).sh
else
	SHELL_LAUNCHERS=
endif


if ENABLE_NATIVE_LAUNCHERS
	NATIVE_LAUNCHERS=launcher.build/$(MODULARJDK_ARGS_FILE) launcher.build/$(javaws) launcher.build/$(itweb_settings) launcher.build/$(policyeditor)
	DESKTOP_SUFFIX=
else
	NATIVE_LAUNCHERS=
	DESKTOP_SUFFIX=.sh
endif

# javaws is program name, JAVAWS is used executable name. in 99.9% those are equals.
# in unsupported case (no rust, and enforced shell launchers), it will become javaws.sh.
# similar case is for other two binaries. 
JAVAWS=$(javaws)$(DESKTOP_SUFFIX)
ITWEB_SETTINGS=$(itweb_settings)$(DESKTOP_SUFFIX)
POLICYEDITOR=$(policyeditor)$(DESKTOP_SUFFIX)

LAUNCHERS=$(NATIVE_LAUNCHERS) $(SHELL_LAUNCHERS)
DESKTOP_FILES=javaws.desktop itweb-settings.desktop policyeditor.desktop
DESKTOP_IN_FILES=javaws.desktop.in itweb-settings.desktop.in policyeditor.desktop.in

export WIN_INSTALLER_DIR = $(TOP_BUILD_DIR)/win-installer.build
export WIN_INSTALLER_SRC_DIR = $(TOP_SRC_DIR)/win-installer
export NETX_DIR = $(TOP_BUILD_DIR)/netx.build
export DOCS_DIR=$(TOP_BUILD_DIR)/icedtea-web-docs/$(FULL_VERSION)
export NETX_SRCDIR = $(TOP_SRC_DIR)/netx
export NETX_RESOURCE_DIR=$(NETX_SRCDIR)/net/sourceforge/jnlp/resources
export ICONS_DEST_DIR=$(DESTDIR)$(datadir)/pixmaps
export BASH_CMPL_DEST_DIR=$(DESTDIR)@bashcompdir@

export WIN_ALL_DEPS=$(DESTDIR)$(prefix)/win-deps-all
export WIN_RUN_DEPS=$(DESTDIR)$(prefix)/win-deps-runtime
export LINUX_BIN_DIST_DEPS=$(DESTDIR)$(prefix)/linux-deps-runtime
export MACOS_BIN_DIST_DEPS=$(DESTDIR)$(prefix)/macos-deps-runtime

export REPORT_STYLES_DIRNAME=report-styles

export TESTS_SRCDIR=$(TOP_SRC_DIR)/tests
export TESTS_DIR=$(TOP_BUILD_DIR)/tests.build

export NETX_UNIT_TEST_SRCDIR=$(TESTS_SRCDIR)/netx/unit
export NETX_TEST_DIR=$(TESTS_DIR)/netx
export NETX_UNIT_TEST_DIR=$(NETX_TEST_DIR)/unit

export JUNIT_RUNNER_DIR=$(TESTS_DIR)/junit-runner
export JUNIT_RUNNER_SRCDIR=$(TESTS_SRCDIR)/junit-runner

export JACOCO_OPERATOR_DIR=$(TESTS_DIR)/jacoco-operator
export JACOCO_OPERATOR_SRCDIR=$(TESTS_SRCDIR)/jacoco-operator

export TEST_EXTENSIONS_SRCDIR=$(TESTS_SRCDIR)/test-extensions
export TEST_EXTENSIONS_TESTS_SRCDIR=$(TESTS_SRCDIR)/test-extensions-tests
export REPRODUCERS_TESTS_SRCDIR=$(TESTS_SRCDIR)/reproducers
export TEST_EXTENSIONS_DIR=$(TESTS_DIR)/test-extensions
export CPP_UNITTEST_FRAMEWORK_SRCDIR=$(TESTS_SRCDIR)/UnitTest++
export CPP_UNITTEST_SRCDIR=$(TESTS_SRCDIR)/cpp-unit-tests
export CPP_UNITTEST_DIR=$(TESTS_DIR)/cpp-unit-tests
export TEST_EXTENSIONS_COMPATIBILITY_SYMLINK=$(TESTS_DIR)/netx/jnlp_testsengine
export TEST_EXTENSIONS_TESTS_DIR=$(TESTS_DIR)/test-extensions-tests
export REPRODUCERS_TESTS_SERVER_DEPLOYDIR=$(TESTS_DIR)/reproducers_test_server_deploydir
export REPRODUCERS_BUILD_DIR=$(TESTS_DIR)/reproducers.classes
export PRIVATE_KEYSTORE_NAME=teststore.ks
export PRIVATE_KEYSTORE_PASS=123456789
export EXPORTED_TEST_CERT_PREFIX=icedteatests
export EXPORTED_TEST_CERT_SUFFIX=crt
export TEST_CERT_ALIAS=icedteaweb
export PUBLIC_KEYSTORE_STUB=icedtea-web/security/trusted.certs
export PUBLIC_KEYSTORE_PASS=changeit

export JUNIT_RUNNER_JAR=$(TOP_BUILD_DIR)/junit-runner.jar
export UNIT_CLASS_NAMES = $(TOP_BUILD_DIR)/unit_class_names
export REPRODUCERS_CLASS_NAMES = $(TOP_BUILD_DIR)/reproducers_class_names
export REPRODUCERS_CLASS_WHITELIST = $(TOP_SRC_DIR)/netx-dist-tests-whitelist
export COVERAGE_JAVA_ARGS=-Xmx2G
export COVERAGE_MODIFIED_FILES=tests-output.xml ServerAccess-logs.xml stdout.log stderr.log all.log
export EMMA_BACKUP_SUFFIX=_noEmma
export EMMA_SUFFIX=_withEmma
export META_MANIFEST = META-INF/MANIFEST.MF
export SIGNED_REPRODUCERS=signed signed2
export SIMPLE_REPRODUCERS=simple
export CUSTOM_REPRODUCERS=custom
export ALL_NONCUSTOM_REPRODUCERS=$(SIMPLE_REPRODUCERS) $(SIGNED_REPRODUCERS)
export ALL_REPRODUCERS=$(ALL_NONCUSTOM_REPRODUCERS) $(CUSTOM_REPRODUCERS)

export JACOCO_PATH:=$(shell dirname "$(JACOCO_JAR)")
export JACOCO_AGENT=org.jacoco.agent.jar
export JACOCO_ANT=org.jacoco.ant.jar
export JACOCO_REPORT=org.jacoco.report.jar
export JACOCO_AGENTRT=org.jacoco.agent.rt.jar
export JACOCO_CORE=org.jacoco.core.jar
export JACOCO_JAVAWS_RESULTS=$(TEST_EXTENSIONS_DIR)/jacoco_javaws.exec
export JACOCO_PLUGIN_RESULTS=$(TEST_EXTENSIONS_DIR)/jacoco_plugin.exec
# space separated fragments for composeclasspath
export JACOCO_CLASSPATH=$(JACOCO_PATH)/$(JACOCO_CORE) $(JACOCO_PATH)/$(JACOCO_AGENT) $(JACOCO_PATH)/$(JACOCO_REPORT) $(JACOCO_PATH)/$(JACOCO_AGENTRT) $(JACOCO_PATH)/$(JACOCO_ANT) $(ASM_JAR)
export JACOCO_AGENT_SWITCH_BODY=-javaagent:$(JACOCO_PATH)/$(JACOCO_AGENTRT)
export JACOCO_BASE_EXCLUDE=org.junit.*:junit.*
export JACOCO_ADVANCED_EXCLUDE=:*jacoco*:java.lang.*:java.reflect.*:java.util.*:sun.reflect.*:javax.swing.*
export JACOCO_AGENT_SWITCH="$(JACOCO_AGENT_SWITCH_BODY)=excludes=$(JACOCO_BASE_EXCLUDE)$(JACOCO_ADVANCED_EXCLUDE),inclbootstrapclasses=true"
export JACOCO_AGENT_JAVAWS_SWITCH=\"$(JACOCO_AGENT_SWITCH),destfile=$(JACOCO_JAVAWS_RESULTS)\"
export JACOCO_AGENT_PLUGIN_SWITCH=\"$(JACOCO_AGENT_SWITCH),destfile=$(JACOCO_PLUGIN_RESULTS)\"
export JACOCO_OPERATOR_EXEC=$(SYSTEM_JRE_DIR)/bin/java $(COVERAGE_JAVA_ARGS) -cp $(call joinsegments, $(JACOCO_OPERATOR_DIR) $(JACOCO_CLASSPATH) .) org.jacoco.operator.Main

# linking variables
export PLUGIN_LINK_NAME=libjavaplugin.so
export MOZILLA_LOCAL_PLUGINDIR=${HOME}/.mozilla/plugins
export MOZILLA_GLOBAL64_PLUGINDIR=/usr/lib64/mozilla/plugins
export MOZILLA_GLOBAL32_PLUGINDIR=/usr/lib/mozilla/plugins
export OPERA_GLOBAL64_PLUGINDIR=/usr/lib64/opera/plugins
export OPERA_GLOBAL32_PLUGINDIR=/usr/lib/opera/plugins
export BUILT_PLUGIN_LIBRARY=IcedTeaPlugin.so
export CPP_UNITTEST_FRAMEWORK_BUILDDIR=$(CPP_UNITTEST_DIR)/UnitTest++
export CPP_UNITTEST_FRAMEWORK_LIB_NAME=libUnitTest++.a
export CPP_UNITTEST_FRAMEWORK_LIB=$(CPP_UNITTEST_FRAMEWORK_BUILDDIR)/$(CPP_UNITTEST_FRAMEWORK_LIB_NAME)
export CPP_UNITTEST_EXECUTABLE=$(CPP_UNITTEST_DIR)/IcedTeaPluginUnitTests
export MOZILLA_LOCAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origU
export MOZILLA_GLOBAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origMG
export OPERA_GLOBAL_BACKUP_FILE=${HOME}/$(PLUGIN_LINK_NAME).origOG
export MOZILLA_FAMILY_TEST= "$(FIREFOX)" != "" -o "$(CHROMIUM)" != "" -o "$(CHROME)" != "" -o "$(MIDORI)" != "" -o "$(EPIPHANY)" != "" 
# end of linking variables

# Build directories

# note this is *space* separated list, as composeclasspath is called on them in classpath usage
JUNIT_RUNTIME:=$(JUNIT_JAR) $(HAMCREST_JAR)

# support for javaws -html needs plugin.jar on classpath. rest of javaws works of course without it.
# plugin jar is split into plugin.jar and jsobject.jar so it can run with modular (9+) jdk
if ENABLE_PLUGINJAR
  PLUGIN_JAR=$(datadir)/$(PACKAGE_NAME)/plugin.jar
  JSOBJECT_JAR=$(datadir)/$(PACKAGE_NAME)/jsobject.jar
endif

NETX_JAR=$(datadir)/$(PACKAGE_NAME)/javaws.jar
SPLASH_PNG=$(datadir)/$(PACKAGE_NAME)/javaws_splash.png
MODULARJDK_ARGS_LOCATION=$(DESTDIR)$(MODULARJDK_ARGS_DIR)/$(MODULARJDK_ARGS_FILE)


# Conditional defintions
if HAVE_TAGSOUP
if HAVE_MSLINKS
  NETX_EXCLUDE_SRCS=
else
  NETX_EXCLUDE_SRCS=net.sourceforge.jnlp.util.WindowsDesktopEntry.java
endif
else
if HAVE_MSLINKS
  NETX_EXCLUDE_SRCS=net.sourceforge.jnlp.MalformedXMLParser.java
else
  NETX_EXCLUDE_SRCS=net.sourceforge.jnlp.MalformedXMLParser.java net.sourceforge.jnlp.util.WindowsDesktopEntry.java
endif
endif
if !ENABLE_PLUGINJAR
  NETX_EXCLUDE_SRCS+= net.sourceforge.jnlp.NetxPanel sun.applet.AppletViewerPanelAccess.java
endif

# Flags
# macOS requires this flag, see https://youtrack.jetbrains.com/issue/IDEA-72010
if MACOS
	export JAVACFLAGS="-XDignore.symbol.file"
endif
export IT_CFLAGS=$(CFLAGS) $(ARCHFLAG)
export IT_JAVAC_SETTINGS=-g -encoding utf-8 $(JAVACFLAGS) $(MEMORY_LIMIT) $(PREFER_SOURCE)
export IT_LANGUAGE_SOURCE_VERSION=8
export IT_CLASS_TARGET_VERSION=8
export IT_JAVACFLAGS=$(IT_JAVAC_SETTINGS) -source $(IT_LANGUAGE_SOURCE_VERSION) -target $(IT_CLASS_TARGET_VERSION)

#
# We need the jars in bootclasspath for a couple of reasons
#  - we use classes (in the sun.applet package) loaded by the bootclassloader
#    using another classloader to load classes from the same package causes an
#    IllegalAccessException
#  - we want full privileges
#
export UNIFIED_CLASSPATH_SEGMENTS=$(NETX_JAR) $(PLUGIN_JAR) $(JSOBJECT_JAR) $(RHINO_JAR) $(TAGSOUP_JAR) $(MSLINKS_JAR)
export UNIFIED_JACOCO_CLASSPATH_SEGMENTS=$(UNIFIED_CLASSPATH_SEGMENTS) $(JACOCO_CLASSPATH)
export UNIFIED_CLASSPATH=$(call joinsegments, $(UNIFIED_CLASSPATH_SEGMENTS))
export UNIFIED_JACOCO_CLASSPATH=$(call joinsegments, $(UNIFIED_JACOCO_CLASSPATH_SEGMENTS))
export UNIFIED_BOOTCLASSPATH=-Xbootclasspath/a:$(UNIFIED_CLASSPATH)
# note javaws, itwebsettings and policyeditor (== LAUNCHER_BOOTCLASSPATH) must be always able to compile and run *without* plugin
export LAUNCHER_BOOTCLASSPATH=$(UNIFIED_BOOTCLASSPATH)
export PLUGIN_BOOTCLASSPATH='"$(UNIFIED_BOOTCLASSPATH)"'
export PLUGIN_COVERAGE_BOOTCLASSPATH='"-Xbootclasspath/a:$(UNIFIED_JACOCO_CLASSPATH)"'

# Fake update version to work with the Deployment Toolkit script used by Oracle
# http://download.oracle.com/javase/tutorial/deployment/deploymentInDepth/depltoolkit_index.html
export JDK_UPDATE_VERSION=50

# Sources list

export PLUGIN_TEST_SRCS = $(TOP_SRC_DIR)/plugin/tests/LiveConnect/*.java

export NETX_PKGS = javax.jnlp net.sourceforge.nanoxml net.sourceforge.jnlp \
	net.sourceforge.jnlp.about \
	net.sourceforge.jnlp.cache net.sourceforge.jnlp.config \
	net.sourceforge.jnlp.controlpanel net.sourceforge.jnlp.event \
	net.sourceforge.jnlp.runtime net.sourceforge.jnlp.security \
	net.sourceforge.jnlp.security.viewer net.sourceforge.jnlp.services \
	net.sourceforge.jnlp.tools net.sourceforge.jnlp.util

if ENABLE_PLUGINJAR
export ICEDTEAPLUGIN_TARGET = stamps/liveconnect-dist.stamp
export PLUGIN_DIR=$(TOP_BUILD_DIR)/plugin/icedteanp
export PLUGIN_SRCDIR=$(TOP_SRC_DIR)/plugin/icedteanp
export JSOBJECT_DIRS = netscape
export SUN_DIR = sun
export SUN_APPLET_DIRS = sun/applet
export SUN_APPLET_PKGS = sun.applet
export PLUGIN_PKGS = sun.applet netscape.security netscape.javascript
export LIVECONNECT_SRCS = $(PLUGIN_SRCDIR)/java
export LIVECONNECT_DIR = $(JSOBJECT_DIRS) $(SUN_APPLET_DIRS)
endif
#overriding ICEDTEAPLUGIN_TARGET
if ENABLE_NATIVE_PLUGIN
export ICEDTEAPLUGIN_TARGET = $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY) stamps/liveconnect-dist.stamp
export ICEDTEAPLUGIN_CLEAN = clean-IcedTeaPlugin
#this is for plugin testcoverage
export COVERABLE_PLUGIN_DIR=$(TESTS_DIR)/icedteanp-build-with-jacoco
endif

if ENABLE_NATIVE_PLUGIN
  CPPUNIT_IMPL = run-cpp-unit-tests-impl
  CPPUNIT_CLEAN_IMPL = clean-cpp-unit-tests-impl
  CPPUNIT_FRAMEWORK_CLEAN_IMPL = clean-unittest++-impl
else
  CPPUNIT_IMPL = run-cpp-unit-tests-impl-placeholder
  CPPUNIT_CLEAN_IMPL = clean-cpp-unit-tests-placeholder
  CPPUNIT_FRAMEWORK_CLEAN_IMPL = clean-unittest++-placeholder
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
if HAVE_JAVA9
JAVADOC_OPTS+=-source $(IT_LANGUAGE_SOURCE_VERSION) --ignore-source-errors
endif
if JAVADOC_SUPPORTS_J_OPTIONS
JAVADOC_MEM_OPTS=-J-Xmx1024m -J-Xms128m
endif
endif

# if you create netx-dist-tests-whitelist in top directory, and place "expr match" regexes
# (separate by one of IFS's values (most commonly space)) into it, you can control reproducers which compile or/xor run.
# if you need declare space in your regex, use \s as described in "expr match" documentation
ECHO_WHITELIST=if [ -f $(REPRODUCERS_CLASS_WHITELIST) ] ; then cat $(REPRODUCERS_CLASS_WHITELIST) ; else echo ".*" ; fi
if ENABLE_WHITELIST
COMPILE_WHITELIST_FILTER=`$(ECHO_WHITELIST)`
RUN_WHITELIST_FILTER=.*
else
COMPILE_WHITELIST_FILTER=.*
RUN_WHITELIST_FILTER=`$(ECHO_WHITELIST)`
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

export EXTRA_DIST = $(MODULARJDK_ARGS_FILE) $(top_srcdir)/netx $(top_srcdir)/plugin ${ICON} $(DESKTOP_IN_FILES) $(top_srcdir)/completion \
  shell-launcher rust-launcher $(top_srcdir)/tests html-gen.sh NEW_LINE_IFS $(top_srcdir)/metadata.in $(top_srcdir)/win-installer

# reproducers `D`shortcuts
export DTEST_SERVER=-Dtest.server.dir=$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)
export DJAVAWS_BUILD=-Djavaws.build.bin=$(DESTDIR)$(bindir)/$(JAVAWS)
export DBROWSERS=-Dused.browsers=$(FIREFOX):$(CHROMIUM):$(CHROME):$(OPERA):$(MIDORI):$(EPIPHANY)
export REPRODUCERS_DPARAMETERS= $(DTEST_SERVER) $(DJAVAWS_BUILD) $(DBROWSERS) $(BROWSER_TESTS_MODIFICATION)
# end of `D`shortcuts

#exported autoconf copies
export EXPORTED_JAVAC=$(SYSTEM_JDK_DIR)/bin/javac
export EXPORTED_JAR=$(SYSTEM_JDK_DIR)/bin/jar
export EXPORTED_KEYTOOL=$(SYSTEM_JDK_DIR)/bin/keytool
export EXPORTED_JARSIGNER=$(SYSTEM_JDK_DIR)/bin/jarsigner
export EXPORTED_PACK200=$(SYSTEM_JDK_DIR)/bin/pack200
#end of exported autoconf copies

# binary names
javaws:= $(shell echo javaws | sed '@program_transform_name@')
itweb_settings:= $(shell echo itweb-settings | sed '@program_transform_name@')
policyeditor:= $(shell echo policyeditor | sed '@program_transform_name@')

if WINDOWS_OR_INCLUDE_BATS
# convert Unix newlines (LF) to DOS format:
  fix_CRLF=-e "s/\$$/\r/"
else
  fix_CRLF=
endif


# the launcher needs to know $(bindir) and $(datadir) which can be different at
# make-time from configure-time
edit_launcher_script = sed \
  -e "s|[@]LAUNCHER_BOOTCLASSPATH[@]|$(LAUNCHER_BOOTCLASSPATH)|g" \
  -e "s|[@]JAVAWS_SPLASH_LOCATION[@]|$(SPLASH_PNG)|g" \
  -e "s|[@]JAVA[@]|$(JAVA)|g" \
  -e "s|[@]JRE[@]|$(SYSTEM_JRE_DIR)|g" \
  -e "s|[@]MAIN_CLASS[@]|$${MAIN_CLASS}|g" \
  -e "s|[@]BIN_LOCATION[@]|$${BIN_LOCATION}|g" \
  -e "s|[@]NETX_JAR[@]|$(NETX_JAR)|g" \
  -e "s|[@]PLUGIN_JAR[@]|$(PLUGIN_JAR)|g" \
  -e "s|[@]JSOBJECT_JAR[@]|$(JSOBJECT_JAR)|g" \
  -e "s|[@]MSLINKS_JAR[@]|$(MSLINKS_JAR)|g" \
  -e "s|[@]TAGSOUP_JAR[@]|$(TAGSOUP_JAR)|g" \
  -e "s|[@]RHINO_JAR[@]|$(RHINO_JAR)|g" \
  -e "s|[@]PROGRAM_NAME[@]|$${PROGRAM_NAME}|g" \
  -e "s|[@]MODULARJDK_ARGS_LOCATION[@]|$(MODULARJDK_ARGS_LOCATION)|g"


# Top-Level Targets
# =================

all-local: stamps/netx-dist.stamp stamps/plugin.stamp stamps/docs.stamp \
 $(LAUNCHERS) $(DESKTOP_FILES)


check-local: $(RHINO_TESTS) $(JUNIT_TESTS)

clean-local: clean-netx clean-plugin clean-liveconnect \
 clean-native-ecj clean-launchers clean-desktop-files clean-docs clean-generated-docs clean-metadata  clean-icedteaweb-completion clean-tests clean-bootstrap-directory clean-win-installer
	if [ -e stamps ] ; then \
	  rmdir stamps ; \
	fi

.PHONY: clean-IcedTeaPlugin clean-add-netx clean-add-netx-debug clean-add-plugin clean-add-plugin-debug \
 clean-bootstrap-directory clean-native-ecj clean-desktop-files clean-netx-docs clean-docs clean-plugin-docs clean-generated-docs clean-icedteaweb-completion\
 clean-tests check-local clean-launchers stamps/check-pac-functions.stamp stamps/run-netx-unit-tests.stamp clean-netx-tests \
 clean-junit-runner clean-netx-unit-tests clean-metadata win-installer clean-win-installer

install-exec-local:
	${mkinstalldirs} $(DESTDIR)$(bindir) $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/ $(DESTDIR)$(libdir) $(ICONS_DEST_DIR) $(BASH_CMPL_DEST_DIR)
if ENABLE_NATIVE_PLUGIN
	${INSTALL_PROGRAM} $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY) $(DESTDIR)$(libdir)
endif
if ENABLE_PLUGINJAR
	${INSTALL_DATA} $(TOP_BUILD_DIR)/liveconnect/lib/classes-applet.jar   $(DESTDIR)$(PLUGIN_JAR)
	${INSTALL_DATA} $(TOP_BUILD_DIR)/liveconnect/lib/classes-jsobject.jar $(DESTDIR)$(JSOBJECT_JAR)
endif
	${INSTALL_DATA} $(NETX_DIR)/lib/classes.jar $(DESTDIR)$(NETX_JAR)
	${INSTALL_DATA} $(NETX_SRCDIR)/javaws_splash.png $(DESTDIR)$(SPLASH_PNG)
	${INSTALL_DATA} $(TOP_SRC_DIR)/${ICON} $(ICONS_DEST_DIR)/
	mkdir -p $(DESTDIR)$(MODULARJDK_ARGS_DIR)
	cp launcher.build/$(MODULARJDK_ARGS_FILE) $(MODULARJDK_ARGS_LOCATION)
if ENABLE_NATIVE_LAUNCHERS
	${INSTALL_PROGRAM} launcher.build/$(javaws) $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(itweb_settings) $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(policyeditor) $(DESTDIR)$(bindir)
endif
if ENABLE_SHELL_LAUNCHERS
	${INSTALL_PROGRAM} launcher.build/$(javaws).sh $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(itweb_settings).sh $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(policyeditor).sh $(DESTDIR)$(bindir)
if WINDOWS_OR_INCLUDE_BATS
	${INSTALL_PROGRAM} launcher.build/$(javaws).bat $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(itweb_settings).bat $(DESTDIR)$(bindir)
	${INSTALL_PROGRAM} launcher.build/$(policyeditor).bat $(DESTDIR)$(bindir)
endif
endif
	${INSTALL_DATA} $(TOP_BUILD_DIR)/completion/itweb-settings.bash $(BASH_CMPL_DEST_DIR)/
	${INSTALL_DATA} $(TOP_BUILD_DIR)/completion/javaws.bash $(BASH_CMPL_DEST_DIR)/
	${INSTALL_DATA} $(TOP_BUILD_DIR)/completion/policyeditor.bash $(BASH_CMPL_DEST_DIR)/

# all generated manpages are installed in swarm
# all windows depndences are copied for windows build, and known one are copied/removed as necessary
# the makefile variables are used in favor of global variables, but maybe they will need to get mixed together
install-data-local: printvarsfile
	${mkinstalldirs} -d $(DESTDIR)$(mandir)
	cp -r "$(DOCS_DIR)/man/"* $(DESTDIR)$(mandir)/
if ENABLE_DOCS
	${mkinstalldirs} $(DESTDIR)$(htmldir)
	(cd ${TOP_BUILD_DIR}/docs/netx; \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} $(DESTDIR)$(htmldir)/netx/$${files}; \
	 done)
if ENABLE_PLUGINJAR
	(cd ${TOP_BUILD_DIR}/docs/plugin; \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} $(DESTDIR)$(htmldir)/plugin/$${files}; \
	 done)
endif
endif
if WINDOWS_OR_INCLUDE_BATS
	mkdir -p $(WIN_ALL_DEPS)
	mkdir -p $(WIN_RUN_DEPS)
	JARSVARS=` ( set -o posix ; set ) | sed "s/.*=//"  | grep \\.jar$$ | grep \\.jar.exe$$ ` ; \
	JARSMAKE=` cat $(TOP_BUILD_DIR)/printvarsfile | sed  "s/.*=//"  |  grep \\.jar$$ ` ; \
	for JAR in $$JARSMAKE ; do \
	  if [ -f $ $$JAR ] ; then \
	    cp -v  $$JAR $(WIN_ALL_DEPS)/ ; \
	  fi ; \
	done ;
	rm -f $(WIN_ALL_DEPS)/jar ;
	for JAR in `ls $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/*.jar` ; do \
	  rm -fv $(WIN_ALL_DEPS)/`basename $$JAR` ; \
	done;
if WITH_RHINO
	filteredName=`basename $(RHINO_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(RHINO_JAR) $(WIN_RUN_DEPS)/$$filteredName
endif
if HAVE_TAGSOUP
	filteredName=`basename $(TAGSOUP_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(TAGSOUP_JAR) $(WIN_RUN_DEPS)/$$filteredName
endif
if HAVE_MSLINKS
	filteredName=`basename $(MSLINKS_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(MSLINKS_JAR) $(WIN_RUN_DEPS)/$$filteredName
endif
endif

#no npapi plugin at all!
linux-bin-dist: install
	rm -f $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
	mkdir -p $(LINUX_BIN_DIST_DEPS)
if WITH_RHINO
	filteredName=`basename $(RHINO_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(RHINO_JAR) $(LINUX_BIN_DIST_DEPS)/$$filteredName
endif
if HAVE_TAGSOUP
	filteredName=`basename $(TAGSOUP_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(TAGSOUP_JAR) $(LINUX_BIN_DIST_DEPS)/$$filteredName
endif
	cp $(NETX_DIR)/lib/src.zip $(DESTDIR)$(prefix)/netx.src.zip
if ENABLE_PLUGINJAR
	cp $(TOP_BUILD_DIR)/liveconnect/lib/src.zip $(DESTDIR)$(prefix)/plugin.src.zip
endif
if ENABLE_DOCS
	cp -r $(DOCS_DIR)/html $(DESTDIR)$(prefix)/icedtea-web-docs
endif
	if [ ! -e $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ] ; then cp $(MODULARJDK_ARGS_LOCATION) $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ; fi
	(cd $(DESTDIR)$(prefix)/.. && $(ZIP) -qr $(TOP_BUILD_DIR)/$(distdir).linux.bin.zip `basename $(DESTDIR)$(prefix)` );

macos-bin-dist: install
	rm -f $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
	mkdir -p $(MACOS_BIN_DIST_DEPS)
if WITH_RHINO
	filteredName=`basename $(RHINO_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(RHINO_JAR) $(MACOS_BIN_DIST_DEPS)/$$filteredName
endif
if HAVE_TAGSOUP
	filteredName=`basename $(TAGSOUP_JAR) | sed "s/[^a-zA-Z]//g" | sed "s/jar$$/.jar/"` ; \
	cp -v $(TAGSOUP_JAR) $(MACOS_BIN_DIST_DEPS)/$$filteredName
endif
	cp $(NETX_DIR)/lib/src.zip $(DESTDIR)$(prefix)/netx.src.zip
if ENABLE_PLUGINJAR
	cp $(TOP_BUILD_DIR)/liveconnect/lib/src.zip $(DESTDIR)$(prefix)/plugin.src.zip
endif
if ENABLE_DOCS
	cp -r $(DOCS_DIR)/html $(DESTDIR)$(prefix)/icedtea-web-docs
endif
	if [ ! -e $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ] ; then cp $(MODULARJDK_ARGS_LOCATION) $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ; fi
	(cd $(DESTDIR)$(prefix)/.. && $(ZIP) -qr $(TOP_BUILD_DIR)/$(distdir).macos.bin.zip `basename $(DESTDIR)$(prefix)` );


win-bin-dist: win-only-image
	(cd $(DESTDIR)$(prefix)/.. && $(ZIP) -qr $(TOP_BUILD_DIR)/$(distdir).win.bin.zip `basename $(DESTDIR)$(prefix)` );

if WINDOWS
# strips cygwin and other linux-like blobs. keeping only windows-usefull stuff
win-only-image: install uninstall-linux-only remove-empty-linux-only-dirs
	rm -rf $(WIN_ALL_DEPS)
	if [ ! -e $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ] ; then cp $(MODULARJDK_ARGS_LOCATION) $(DESTDIR)$(bindir)/$(MODULARJDK_ARGS_FILE) ; fi
if ENABLE_DOCS
	cp -r $(DOCS_DIR)/html $(DESTDIR)$(prefix)/icedtea-web-docs
endif
	cp $(NETX_DIR)/lib/src.zip $(DESTDIR)$(prefix)/netx.src.zip
if ENABLE_PLUGINJAR
	cp $(TOP_BUILD_DIR)/liveconnect/lib/src.zip $(DESTDIR)$(prefix)/plugin.src.zip
endif

clean-win-installer:
	rm -rf $(WIN_INSTALLER_DIR)

$(WIN_INSTALLER_DIR)/itw-installer.json: clean-win-installer
	mkdir $(WIN_INSTALLER_DIR)
	sed \
	-e "s|../win-installer|$(WIN_INSTALLER_SRC_DIR)|g" \
	-e "s|[@]MAJOR_VERSION[@]|$(MAJOR_VERSION)|g" \
	-e "s|[@]MINOR_VERSION[@]|$(MINOR_VERSION)|g" \
	-e "s|[@]MICRO_VERSION[@]|$(MICRO_VERSION)|g" \
	-e s/[@]PACKAGE_VERSION[@]/$(PACKAGE_VERSION)/g $(WIN_INSTALLER_SRC_DIR)/installer.json.in > $@

win-installer: win-only-image $(WIN_INSTALLER_DIR)/itw-installer.json
	"$(JAVA)" -jar "$(WIXGEN_JAR)" "$(DESTDIR)$(prefix)" -c $(WIN_INSTALLER_DIR)/itw-installer.json -o $(WIN_INSTALLER_DIR)/itw-installer.wxs
	cd $(WIN_INSTALLER_DIR) && "$(WIX_TOOLSET_DIR)"/candle.exe /nologo itw-installer.wxs
	cd $(WIN_INSTALLER_DIR) && "$(WIX_TOOLSET_DIR)"/light.exe /nologo -sval -ext WixUIExtension itw-installer.wixobj
	cd $(WIN_INSTALLER_DIR) && mv itw-installer.msi $(distdir).msi
endif

# note that this is called only from windows specific target (hidden otherwise)
remove-empty-linux-only-dirs:
	echo "*************************************************************************" ; \
	echo "* If run without prefix or on non-windows machine, may harm your system *" ; \
	echo "*************************************************************************" ; \
	MAN_DIRS_TO_CHECK_AND_CLEAN="de/man1 pl/man1 cs/man1 de pl cs man1 " ; \
	for manDirName in $$MAN_DIRS_TO_CHECK_AND_CLEAN ; do \
	  dirCandidate=$(DESTDIR)$(mandir)/$$manDirName ; \
	  if ls -1qA $$dirCandidate | grep -q . ; then echo "$$dirCandidate not empty"; else rmdir -v $$dirCandidate ; fi ; \
	done
	dirCandidate=$(DESTDIR)$(mandir)/$$manDirName ; \
	if ls -1qA $$dirCandidate | grep -q . ; then echo "$$dirCandidate not empty"; else rmdir -v $$dirCandidate ; fi ;
	DEFAULT_CMPL_DIRS_TO_CHECK_AND_CLEAN="$(BASH_CMPL_DEST_DIR) `dirname $(BASH_CMPL_DEST_DIR)` $$(dirname $$(dirname $(BASH_CMPL_DEST_DIR)))" ; \
	for cmplDirName in $$DEFAULT_CMPL_DIRS_TO_CHECK_AND_CLEAN ; do \
	  dirCandidate=$$cmplDirName ; \
	  if ls -1qA $$dirCandidate | grep -q . ; then echo "$$dirCandidate not empty"; else rmdir -v $$dirCandidate ; fi ; \
	done
	dirCandidate=$(DESTDIR)$(libdir)/ ; \
	if ls -1qA $$dirCandidate | grep -q . ; then echo "$$dirCandidate not empty"; else rmdir -v $$dirCandidate ; fi ;

# all generated manpages must be removed one by one
# shell (only shell!) launchers must go away
uninstall-linux-only:
	KNOWN_MANS="icedtea-web.1  icedtea-web-plugin.1  itweb-settings.1  javaws.1  policyeditor.1" ; \
	KNOWN_DIRS="man1 de/man1 pl/man1 cs/man1" ; \
	for file in $$KNOWN_MANS ; do \
	  for dir in $$KNOWN_DIRS ; do \
	    rm -f $(DESTDIR)$(mandir)/$$dir/$$file ; \
	  done ; \
	done
	rm -f $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
	rm -f $(DESTDIR)$(bindir)/$(javaws).sh
	rm -f $(DESTDIR)$(bindir)/$(itweb_settings).sh
	rm -f $(DESTDIR)$(bindir)/$(policyeditor).sh
	rm -f $(BASH_CMPL_DEST_DIR)/itweb-settings.bash
	rm -f $(BASH_CMPL_DEST_DIR)/javaws.bash
	rm -f $(BASH_CMPL_DEST_DIR)/policyeditor.bash

uninstall-local: uninstall-linux-only
	rm -f $(DESTDIR)$(PLUGIN_JAR)
	rm -f $(DESTDIR)$(JSOBJECT_JAR)
	rm -f $(DESTDIR)$(NETX_JAR)
	rm -r $(DESTDIR)$(SPLASH_PNG)
	rm -r $(ICONS_DEST_DIR)/${ICON}
	rm -rf $(DESTDIR)$(htmldir)
if WINDOWS_OR_INCLUDE_BATS
	rm -rf $(WIN_ALL_DEPS)
	rm -rf $(WIN_RUN_DEPS)
	rm -f $(DESTDIR)$(bindir)/$(javaws).bat
	rm -f $(DESTDIR)$(bindir)/$(itweb_settings).bat
	rm -f $(DESTDIR)$(bindir)/$(policyeditor).bat
endif

# Plugin

if ENABLE_NATIVE_PLUGIN
# IcedTeaPlugin.so.
# Separate compile and link invocations to ensure intermediate object
# is listed before -l options.  See:
# http://developer.mozilla.org/en/docs/XPCOM_Glue

PLUGIN_SRC=IcedTeaNPPlugin.cc IcedTeaScriptablePluginObject.cc \
        IcedTeaJavaRequestProcessor.cc IcedTeaPluginRequestProcessor.cc \
		IcedTeaPluginUtils.cc IcedTeaParseProperties.cc

PLUGIN_OBJECTS=IcedTeaNPPlugin.o IcedTeaScriptablePluginObject.o \
        IcedTeaJavaRequestProcessor.o IcedTeaPluginRequestProcessor.o \
		IcedTeaPluginUtils.o IcedTeaParseProperties.o

$(PLUGIN_DIR)/%.o: $(PLUGIN_SRCDIR)/%.cc
	mkdir -p $(PLUGIN_DIR) && \
	cd $(PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	   $(DEFS) $(VERSION_DEFS) \
	  -DJDK_UPDATE_VERSION="\"$(JDK_UPDATE_VERSION)\"" \
	  -DPLUGIN_NAME="\"IcedTea-Web Plugin\"" \
	  -DJAVAWS_BIN="\"$(bindir)/$(JAVAWS)\"" \
	  -DJAVAWS_NAME="\"$(javaws)\"" \
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DPACKAGE_URL="\"$(PACKAGE_URL)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
	  -DICEDTEA_WEB_JRE="\"$(SYSTEM_JRE_DIR)\"" \
	  -DPLUGIN_BOOTCLASSPATH=$(PLUGIN_BOOTCLASSPATH) \
	  -DNETX_JAR="\"$(NETX_JAR)\"" \
	  -DPLUGIN_JAR="\"$(PLUGIN_JAR)\"" \
	  -DJSOBJECT_JAR="\"$(JSOBJECT_JAR)\""\
	  $(GLIB_CFLAGS) \
	  $(MOZILLA_CFLAGS) \
  	  -fvisibility=hidden \
	  -fPIC -o $@ -c $<

$(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY): $(addprefix $(PLUGIN_DIR)/,$(PLUGIN_OBJECTS))
	cd $(PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  $(PLUGIN_OBJECTS) \
	  $(GLIB_LIBS) \
	  $(MOZILLA_LIBS) \
	  -shared -o $@

# Start of CPP Unit test targets

# Note that UnitTest++ has its own makefile, however this is avoided because it creates an in-source build.
$(CPP_UNITTEST_FRAMEWORK_LIB): $(CPP_UNITTEST_FRAMEWORK_SRCDIR)
	mkdir -p $(CPP_UNITTEST_FRAMEWORK_BUILDDIR) && \
	pushd $(CPP_UNITTEST_FRAMEWORK_SRCDIR) && \
	for cppfile in $$(find $(CPP_UNITTEST_FRAMEWORK_SRCDIR) -name '*.cpp') ; \
	do \
	  objfile="$(CPP_UNITTEST_FRAMEWORK_BUILDDIR)/$$(basename $${cppfile%.cpp}).o" ; \
	  $(CXX) $(CXXFLAGS) -c $$cppfile -o $$objfile || exit 1 ; \
	done ; \
	ar cr $(CPP_UNITTEST_FRAMEWORK_LIB) $(CPP_UNITTEST_FRAMEWORK_BUILDDIR)/*.o ; \
	popd

clean-unittest++-impl:
	rm -f $(CPP_UNITTEST_FRAMEWORK_BUILDDIR)/*.o
	rm -f $(CPP_UNITTEST_FRAMEWORK_LIB)
	if [ -e $(CPP_UNITTEST_FRAMEWORK_BUILDDIR) ] ; then \
		rmdir $(CPP_UNITTEST_FRAMEWORK_BUILDDIR) ; \
	fi

stamps/cpp-unit-tests-compile.stamp: $(CPP_UNITTEST_FRAMEWORK_LIB) $(CPP_UNITTEST_SRCDIR) $(addprefix $(PLUGIN_DIR)/,$(PLUGIN_OBJECTS))
	mkdir -p $(CPP_UNITTEST_DIR) && \
	pushd $(CPP_UNITTEST_SRCDIR) && \
	for cppfile in $$(find $(CPP_UNITTEST_SRCDIR) -name '*.cc') ; \
	do \
	objfile="$(CPP_UNITTEST_DIR)/$$(basename $${cppfile%.cc}).o" ; \
	echo "Compiling $$cppfile to $$objfile"; \
	$(CXX) $(CXXFLAGS) \
	   $(DEFS) $(VERSION_DEFS) \
	  -DJDK_UPDATE_VERSION="\"$(JDK_UPDATE_VERSION)\"" \
	  -DPLUGIN_NAME="\"IcedTea-Web Plugin\"" \
	  -DJAVAWS_BIN="\"$(bindir)/$(JAVAWS)\"" \
	  -DJAVAWS_NAME="\"$(javaws)\"" \
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DPACKAGE_URL="\"$(PACKAGE_URL)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
	  -DICEDTEA_WEB_JRE="\"$(SYSTEM_JRE_DIR)\"" \
	  -DPLUGIN_BOOTCLASSPATH=$(PLUGIN_BOOTCLASSPATH) \
	  -DNETX_JAR="\"$(NETX_JAR)\"" \
	  -DPLUGIN_JAR="\"$(PLUGIN_JAR)\"" \
	  -DJSOBJECT_JAR="\"$(JSOBJECT_JAR)\""\
	  $(GLIB_CFLAGS) \
	  $(MOZILLA_CFLAGS) \
	  "-I$(CPP_UNITTEST_FRAMEWORK_SRCDIR)/src" \
	  "-I$(PLUGIN_SRCDIR)" \
	  -o $$objfile -c $$cppfile || exit 1 ; \
	done ; \
	popd ; \
	mkdir -p stamps ; \
	touch $@

$(CPP_UNITTEST_EXECUTABLE): $(CPP_UNITTEST_FRAMEWORK_LIB) stamps/cpp-unit-tests-compile.stamp 
	cd $(CPP_UNITTEST_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  $(addprefix $(PLUGIN_DIR)/,$(PLUGIN_OBJECTS)) \
	  $(CPP_UNITTEST_DIR)/*.o \
	  -lrt \
	  -lpthread \
	  $(GLIB_LIBS) \
	  $(MOZILLA_LIBS) \
	  $(CPP_UNITTEST_FRAMEWORK_LIB)\
	  $(BUILT_CPP_UNIT_TEST_FRAMEWORK) -o $@

clean-cpp-unit-tests-impl: 
	rm -f stamps/cpp-unit-tests-compile.stamp
	rm -f $(CPP_UNITTEST_EXECUTABLE)
	rm -f $(CPP_UNITTEST_DIR)/*.o

run-cpp-unit-tests-impl: $(CPP_UNITTEST_EXECUTABLE)
	$(CPP_UNITTEST_EXECUTABLE)

# End of CPP Unit test targets

clean-IcedTeaPlugin:
	rm -f $(PLUGIN_DIR)/*.o
	rm -f $(PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY)
	if [ $(TOP_SRC_DIR) != $(TOP_BUILD_DIR) ]; then \
	  if [ -e $(TOP_BUILD_DIR)/plugin/icedteanp ] ; then \
	    rmdir $(TOP_BUILD_DIR)/plugin/icedteanp ; \
	    rmdir $(TOP_BUILD_DIR)/plugin ; \
	  fi ; \
	fi
endif

clean-unittest++-placeholder:

clean-cpp-unit-tests-placeholder:
	echo "plugin disabled"

run-cpp-unit-tests-impl-placeholder:
	echo "plugin disabled"

clean-unittest++: $(CPPUNIT_FRAMEWORK_CLEAN_IMPL)

clean-cpp-unit-tests: $(CPPUNIT_CLEAN_IMPL)

run-cpp-unit-tests: $(CPPUNIT_IMPL)


stamps/plugin.stamp: $(ICEDTEAPLUGIN_TARGET)
	mkdir -p stamps
	touch stamps/plugin.stamp

clean-plugin: $(ICEDTEAPLUGIN_CLEAN)
	rm -f stamps/plugin.stamp

liveconnect-source-files.txt:
if WINDOWS
	echo "Using WEmbeddedFrame" ; \
	sed "s;sun.awt.X11.XEmbeddedFrame;sun.awt.windows.WEmbeddedFrame;" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ; \
	sed "s;XEmbeddedFrame;WEmbeddedFrame;" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ; \
	sed "s;super(handle, true);super(handle);" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ;
endif
if MACOS
	echo "Using CEmbeddedFrame" ; \
	sed "s;sun.awt.X11.XEmbeddedFrame;sun.lwawt.macosx.CEmbeddedFrame;" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ; \
	sed "s;XEmbeddedFrame;CEmbeddedFrame;" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ;
	sed "s;super(handle, true);super();" -i $(LIVECONNECT_SRCS)/$(SUN_APPLET_DIRS)/PluginAppletViewer.java ;
else
	echo "Using XEmbeddedFrame" ;
endif
	if test "x${LIVECONNECT_DIR}" != x; then \
	  find $(LIVECONNECT_SRCS) -name '*.java' | sort > $@ ; \
	fi ; \
	touch $@

stamps/liveconnect.stamp: liveconnect-source-files.txt stamps/netx.stamp
	if test "x${LIVECONNECT_DIR}" != x; then \
	  mkdir -p $(TOP_BUILD_DIR)/liveconnect && \
	  $(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	      -d $(TOP_BUILD_DIR)/liveconnect \
	      $(call composeclasspath,$(NETX_DIR) $(MSLINKS_JAR) $(TAGSOUP_JAR) $(RHINO_JAR)) \
	      -sourcepath $(LIVECONNECT_SRCS) \
	      @liveconnect-source-files.txt ; \
	fi
	mkdir -p stamps
	touch $@

# although sun and netscape packages were compiled together, we pack and install them as two jars, to follow java.desktop and jdk.jsobject jdk9 modules
# the all together jar is kept for usage in tests. Feel free to remove and refactor
stamps/liveconnect-dist.stamp: stamps/liveconnect.stamp
	if test "x${LIVECONNECT_DIR}" != x; then \
	  (cd $(TOP_BUILD_DIR)/liveconnect ; \
	   mkdir -p lib ; \
	   $(SYSTEM_JDK_DIR)/bin/jar cf lib/classes.jar          $(LIVECONNECT_DIR) ; \
	   $(SYSTEM_JDK_DIR)/bin/jar cf lib/classes-applet.jar   $(SUN_APPLET_DIRS) ; \
	   $(SYSTEM_JDK_DIR)/bin/jar cf lib/classes-jsobject.jar $(JSOBJECT_DIRS)   ; \
	   cp -pPR $(SRC_DIR_LINK) $(LIVECONNECT_SRCS) src; \
	   find src -type f -exec chmod 640 '{}' ';' -o -type d -exec chmod 750 '{}' ';'; \
	   cd src ; \
	   $(ZIP) -qr $(TOP_BUILD_DIR)/liveconnect/lib/src.zip $(LIVECONNECT_DIR) ) ; \
	fi
	mkdir -p stamps
	touch $@

clean-liveconnect:
	rm -rf $(TOP_BUILD_DIR)/liveconnect
	rm -f stamps/liveconnect-dist.stamp
	rm -f liveconnect-source-files.txt
	rm -f stamps/liveconnect.stamp

# NetX

netx-source-files.txt:
	find $(NETX_SRCDIR) -name '*.java' | sort > $@ ; \
	for src in $(NETX_EXCLUDE_SRCS) ; \
	do \
	  sed -i "/$${src}/ d" $@ ; \
	done
if !WITH_RHINO
	sed -i '/RhinoBasedPacEvaluator/ d' $@
endif

# if 'sed' will be missing on machine, use old IFS hack as on other places
# the 'sed' cycles is changing some custom version like 1.6.2 fedora23.4 to simple 1.6
# if changed, keep in mind that also 1.7 should bubble to 1.7
# also 1.7pre or 1.7preHASH should end up like 1.7
$(TOP_BUILD_DIR)/metadata: $(top_srcdir)/metadata.in
	mkdir -p $(TOP_BUILD_DIR)/metadata-work ; \
	METAFILES=`ls $(top_srcdir)/metadata.in` ; \
	SEDDED_VERSION=`echo "$(FULL_VERSION)" | sed 's/[^0-9]\+/ /g'` ; \
	x=0; for i in $$SEDDED_VERSION ; do  \
	  let x++; \
	  if [ $$x -eq 1 ] ; then V1=$$i; fi; \
	  if [ $$x -eq 2 ] ; then V2=$$i; fi; \
	done; \
	XVERSION=$$V1.$$V2; \
	for F in $$METAFILES ; do \
	  cat $(top_srcdir)/metadata.in/$$F | sed "s;%{name};$(PACKAGE_NAME);g" |  sed "s;%{version};$$XVERSION;g" > $(TOP_BUILD_DIR)/metadata-work/$$F ; \
	done ; \
	mv $(TOP_BUILD_DIR)/metadata-work $(TOP_BUILD_DIR)/metadata ;

check-metadata: $(TOP_BUILD_DIR)/metadata
	xmllint --noout $(TOP_BUILD_DIR)/metadata/* ; \
	if [ $$? -eq 0 ] ; then \
	  echo "Passed: well formed" ; \
	else \
	  echo "FAILED: well formed" ; \
	fi ; \
	xmllint --noout --schema http://maven.apache.org/xsd/maven-4.0.0.xsd $(TOP_BUILD_DIR)/metadata/*.pom ; \
	if [ $$? -eq 0 ] ; then \
	  echo "Passed: valid poms" ; \
	else \
	  echo "FAILED: valid poms" ; \
	fi ; \
	appstream-util validate $(TOP_BUILD_DIR)/metadata/*.appdata.xml  $(TOP_BUILD_DIR)/metadata/*.metainfo.xml ; \
	if [ $$? -eq 0 ] ; then \
	  echo "Passed: valid software descriptors" ; \
	else \
	  echo "FAILED: valid software descriptors" ; \
	fi ;
# very strange results:
# appstreamcli  validate $(TOP_BUILD_DIR)/metadata/*.appdata.xml  $(TOP_BUILD_DIR)/metadata/*.metainfo.xml
	

$(TOP_BUILD_DIR)/completion/itweb-settings.bash: $(TOP_SRC_DIR)/completion/itweb-settings.bash.in stamps/netx.stamp
	OPTIONS_COMMAND="$(SYSTEM_JRE_DIR)/bin/java -cp $(NETX_DIR) net.sourceforge.jnlp.OptionsDefinitions" ;  \
	OPTIONS=`$$OPTIONS_COMMAND itweb-settings`; \
	echo $$OPTIONS ; \
	cat $(TOP_SRC_DIR)/completion/itweb-settings.bash.in | \
	sed "s/@OPTIONS@/$${OPTIONS}/" > $@ ;

$(TOP_BUILD_DIR)/completion/javaws.bash: $(TOP_SRC_DIR)/completion/javaws.bash.in stamps/netx.stamp
	OPTIONS_COMMAND="$(SYSTEM_JRE_DIR)/bin/java -cp $(NETX_DIR) net.sourceforge.jnlp.OptionsDefinitions" ;  \
	OPTIONS=`$$OPTIONS_COMMAND javaws` ; \
	echo $$OPTIONS ; \
	cat $(TOP_SRC_DIR)/completion/javaws.bash.in | \
	sed "s/@OPTIONS@/$${OPTIONS}/" > $@ ;

$(TOP_BUILD_DIR)/completion/policyeditor.bash: $(TOP_SRC_DIR)/completion/policyeditor.bash.in stamps/netx.stamp
	OPTIONS_COMMAND="$(SYSTEM_JRE_DIR)/bin/java -cp $(NETX_DIR) net.sourceforge.jnlp.OptionsDefinitions" ;  \
	OPTIONS=`$$OPTIONS_COMMAND policyeditor` ; \
	echo $$OPTIONS ; \
	cat $(TOP_SRC_DIR)/completion/policyeditor.bash.in | \
	sed "s/@OPTIONS@/$${OPTIONS}/" > $@ ;

stamps/generate-docs.stamp: stamps/netx.stamp
	mkdir -p "$(DOCS_DIR)" ; \
	HTML_DOCS_TARGET_DIR="$(DOCS_DIR)/html"  ; \
	PLAIN_DOCS_TARGET_DIR="$(DOCS_DIR)/plain" ; \
	MAN_DOCS_TARGET_DIR="$(DOCS_DIR)/man"  ; \
	AUTHORS_FILE_TARGET="$(TOP_SRC_DIR)/AUTHORS" ; \
	mkdir "$$HTML_DOCS_TARGET_DIR" ; \
	mkdir "$$PLAIN_DOCS_TARGET_DIR" ; \
	mkdir "$$MAN_DOCS_TARGET_DIR" ; \
	HTML_DOCS_INDEX="$$HTML_DOCS_TARGET_DIR/index.html" ; \
	TP_COMMAND="$(SYSTEM_JRE_DIR)/bin/java --add-exports=java.base/sun.security.action=ALL-UNNAMED -DPLUGIN_JAR=$(PLUGIN_JAR) -cp $(NETX_DIR) net.sourceforge.jnlp.util.docprovider.TextsProvider" ; \
	TP_TAIL="false $(FULL_VERSION)" ; \
	LANG_BACKUP=$$LANG ; \
	echo "<html><head><title>$(PLUGIN_VERSION)</title></head>" > "$$HTML_DOCS_INDEX" ; \
	echo "<body><h3>$(PLUGIN_VERSION) docs:</h3>"  >> "$$HTML_DOCS_INDEX" ; \
	for LANG_ID in en_US.UTF-8 cs_CZ.UTF-8 pl_PL.UTF-8 de_DE.UTF-8 ;  do \
	  ID=`echo "$$LANG_ID" | head -c 2` ; \
	  ENCOD=`echo "$$LANG_ID" | tail -c 6 -` ; \
	  export LANG=$$LANG_ID; \
	  mkdir "$$HTML_DOCS_TARGET_DIR/$$ID" ; \
	  echo "<li><a href='$$ID/icedtea-web.html'>$$LANG_ID</a></li>"  >> "$$HTML_DOCS_INDEX" ; \
	  $$TP_COMMAND html "$$HTML_DOCS_TARGET_DIR/$$ID" $$TP_TAIL "-authorFile=$$AUTHORS_FILE_TARGET" ; \
	  mkdir "$$PLAIN_DOCS_TARGET_DIR/$$ID" ; \
	  $$TP_COMMAND plain "$$PLAIN_DOCS_TARGET_DIR/$$ID" 160 $$TP_TAIL "-authorFile=$$AUTHORS_FILE_TARGET" ; \
	  if [ $$ID = "en" ] ; then \
	    MAN_DESC="$$MAN_DOCS_TARGET_DIR/man1" ; \
	  else \
	    MAN_DESC="$$MAN_DOCS_TARGET_DIR/$$ID/man1" ; \
	  fi ; \
	  mkdir -p "$$MAN_DESC" ; \
	  $$TP_COMMAND man $$ENCOD "$$MAN_DESC" $$TP_TAIL "-authorFile=$$AUTHORS_FILE_TARGET" ; \
	  $$TP_COMMAND htmlIntro "$(NETX_DIR)/net/sourceforge/jnlp/resources/about_$$ID.html" $$TP_TAIL; \
	done ; \
	export LANG=$$LANG_BACKUP ; \
	echo "</body></html>"  >> $$HTML_DOCS_INDEX ; \
	touch $@

stamps/netx-html-gen.stamp:
	(cd $$NETX_SRCDIR/..; \
	mkdir -p html-gen; \
	cp AUTHORS NEWS COPYING ChangeLog html-gen/; \
	export HTML_GEN_DEBUG=true; \
	${SHELL} html-gen.sh 140; \
	unset HTML_GEN_DEBUG)
	${INSTALL_DATA} $(NETX_SRCDIR)/../html-gen/*.html $(NETX_RESOURCE_DIR)
	rm -r $(NETX_SRCDIR)/../html-gen/
	mkdir -p stamps
	touch $@

stamps/netx.stamp: netx-source-files.txt stamps/netx-html-gen.stamp
	mkdir -p $(NETX_DIR)
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
		-d $(NETX_DIR) \
		-sourcepath $(NETX_SRCDIR) \
		$(call composeclasspath, $(MSLINKS_JAR) $(TAGSOUP_JAR) $(RHINO_JAR)) \
	    @netx-source-files.txt
	(cd $(NETX_RESOURCE_DIR); \
	 for files in $$(find . -type f); \
	 do \
	   ${INSTALL_DATA} -D $${files} \
	   $(NETX_DIR)/net/sourceforge/jnlp/resources/$${files}; \
	 done)
	cp $(CPA) $(NETX_SRCDIR)/net/sourceforge/jnlp/runtime/pac-funcs.js \
	  $(NETX_DIR)/net/sourceforge/jnlp/runtime
	cp $(CPA) build.properties $(NETX_DIR)/net/sourceforge/jnlp/
	mkdir -p stamps
	touch $@

stamps/netx-dist.stamp: stamps/netx.stamp $(TOP_BUILD_DIR)/netx.manifest stamps/generate-docs.stamp $(TOP_BUILD_DIR)/completion/itweb-settings.bash $(TOP_BUILD_DIR)/completion/javaws.bash $(TOP_BUILD_DIR)/completion/policyeditor.bash $(TOP_BUILD_DIR)/metadata
	(cd $(NETX_DIR) ; \
	 mkdir -p lib ; \
	 $(SYSTEM_JDK_DIR)/bin/jar cfm lib/classes.jar \
	  $(TOP_BUILD_DIR)/netx.manifest javax/jnlp net $(SUN_DIR); \
	 cp -pPR $(SRC_DIR_LINK) $(NETX_SRCDIR) src; \
	 find src -type f -exec chmod 640 '{}' ';' -o -type d -exec chmod 750 '{}' ';'; \
	 cd src ; \
	 $(ZIP) -qr $(NETX_DIR)/lib/src.zip javax net $(SUN_DIR))
	mkdir -p stamps
	touch $@

clean-netx:
	rm -rf $(NETX_DIR)
	rm -f stamps/netx-dist.stamp
	rm -f netx-source-files.txt
	rm -f stamps/netx.stamp
	rm -f stamps/netx-html-gen.stamp
	rm -f $(NETX_RESOURCE_DIR)/{NEWS,AUTHORS,COPYING,ChangeLog}.html

clean-desktop-files:
	rm -f javaws.desktop
	rm -f itweb-settings.desktop
	rm -f policyeditor.desktop


# launcher run args:
launcher.build/$(MODULARJDK_ARGS_FILE): $(srcdir)/$(MODULARJDK_ARGS_FILE)
	mkdir -p launcher.build
	cp $(srcdir)/$(MODULARJDK_ARGS_FILE) launcher.build/$(MODULARJDK_ARGS_FILE)


if ENABLE_NATIVE_LAUNCHERS
# todo, handling of suffixes
# there is curently harecoded sh, so it can somehow basically work
# see the DESKTOP_SUFFIX for final tuning
launcher.build/$(javaws) launcher.build/$(itweb_settings) launcher.build/$(policyeditor): rust-launcher/src/main.rs rust-launcher/Cargo.toml
	filename=`basename $@` ; \
	type=$${filename%.*} ; \
	export ITW_TMP_REPLACEMENT=$(TESTS_DIR)/rust_tests_tmp/$$type ; \
	mkdir -p $$ITW_TMP_REPLACEMENT; \
	srcs=$(TOP_SRC_DIR)/rust-launcher ; \
	outs=$(TOP_BUILD_DIR)/launcher.in.$$type  ; \
	mkdir -p launcher.build  ; \
	cp -r $$srcs $$outs  ; \
	pushd $$outs ; \
	  export JAVA=$(JAVA) ; \
	  export JRE=$(SYSTEM_JRE_DIR) ; \
	  export SPLASH_PNG=$(SPLASH_PNG) ; \
	  export ITW_LIBS=$(ITW_LIBS) ; \
	  export PLUGIN_JAR=$(PLUGIN_JAR) ; \
	  export NETX_JAR=$(NETX_JAR) ; \
	  export JSOBJECT_JAR=$(JSOBJECT_JAR) ; \
	  export TAGSOUP_JAR=$(TAGSOUP_JAR) ; \
	  export RHINO_JAR=$(RHINO_JAR) ; \
	  export MSLINKS_JAR=$(MSLINKS_JAR) ; \
	  export MODULARJDK_ARGS_LOCATION=$(MODULARJDK_ARGS_LOCATION) ; \
	  unset MAIN_CLASS ; \
	  unset BIN_LOCATION ; \
	  unset PROGRAM_NAME ; \
	  if [ $$filename = $(javaws) ] ; then \
	    export MAIN_CLASS=net.sourceforge.jnlp.runtime.Boot ; \
	    export BIN_LOCATION=$(bindir)/$(javaws) ; \
	    export PROGRAM_NAME=$(javaws) ; \
	  fi ; \
	  if [ $$filename = $(itweb_settings) ] ; then \
	    export MAIN_CLASS=net.sourceforge.jnlp.controlpanel.CommandLine ; \
	    export BIN_LOCATION=$(bindir)/$(itweb_settings) ; \
	    export PROGRAM_NAME=$(itweb_settings) ; \
	  fi ; \
	  if [ $$filename = $(policyeditor) ] ; then \
	    export MAIN_CLASS=net.sourceforge.jnlp.security.policyeditor.PolicyEditor ; \
	    export BIN_LOCATION=$(bindir)/$(policyeditor) ; \
	    export PROGRAM_NAME=$(policyeditor) ; \
	  fi ; \
	  $(CARGO) test ; \
	  r=$$? ; \
	  $(CARGO) build --release ; \
	popd ;\
	cp -v $$outs/target/release/launcher $@ ; \
	if [ $$r -ne 0 ] ; then \
	  mv $@ $@.broken ; \
	fi; \
	exit $$r

# https://github.com/SimonKagstrom/kcov/
# build is not part of upstream, declaring
rust-code-coverage:
	kcov="none" ; \
	if [ -f $(KCOV)/kcov ] ; then \
	  kcov=$(KCOV)/kcov ; \
	elif [ -f $(KCOV)/bin/kcov ] ; then \
	  kcov=$(KCOV)/bin/kcov ; \
	elif [ -f $(KCOV)/build/kcov ] ; then \
	  kcov=$(KCOV)/build/kcov ; \
	elif [ -f $(KCOV)/build/src/kcov ] ; then \
	  kcov=$(KCOV)/build/src/kcov ; \
	else \
	  mkdir $(KCOV)/build ; \
	  pushd $(KCOV)/build ; \
	  cmake .. ; \
	  make ; \
	  popd ; \
	  kcov=$(KCOV)/build/src/kcov ; \
	fi ; \
	export ITW_TMP_REPLACEMENT=$(TESTS_DIR)/rust_tests_tmp ; \
	mkdir -p $$ITW_TMP_REPLACEMENT; \
	srcs=$(TOP_SRC_DIR)/rust-launcher ; \
	outs=$(TOP_BUILD_DIR)/launcher.in.codecoverage  ; \
	rm -rvf $$outs  ; \
	cp -r $$srcs $$outs  ; \
	pushd $$outs ; \
	  export JAVA=$(JAVA) ; \
	  export JRE=$(SYSTEM_JRE_DIR) ; \
	  export SPLASH_PNG=$(SPLASH_PNG) ; \
	  export ITW_LIBS=$(ITW_LIBS) ; \
	  export PLUGIN_JAR=$(PLUGIN_JAR) ; \
	  export NETX_JAR=$(NETX_JAR) ; \
	  export JSOBJECT_JAR=$(JSOBJECT_JAR) ; \
	  export TAGSOUP_JAR=$(TAGSOUP_JAR) ; \
	  export RHINO_JAR=$(RHINO_JAR) ; \
	  export MODULARJDK_ARGS_LOCATION=$(MODULARJDK_ARGS_LOCATION) ; \
	  export MAIN_CLASS=net.sourceforge.jnlp.runtime.Boot ; \
	  export BIN_LOCATION=$(bindir)/none ; \
	  export PROGRAM_NAME=test ; \
	  $(CARGO) test  --no-run ; \
	popd ; \
	rm -fv $(TOP_BUILD_DIR)/launcher.in.codecoverage/target/debug/launcher-*.d ; \
	$$kcov $$outs $(TOP_BUILD_DIR)/launcher.in.codecoverage/target/debug/launcher-*
endif

if ENABLE_SHELL_LAUNCHERS
launcher.build/$(javaws).sh: shell-launcher/launchers.sh.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.runtime.Boot ;\
	BIN_LOCATION=$(bindir)/$(javaws).sh ;\
	PROGRAM_NAME=$(javaws) ;\
	$(edit_launcher_script) < $< > $@

launcher.build/$(itweb_settings).sh: shell-launcher/launchers.sh.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.controlpanel.CommandLine ;\
	BIN_LOCATION=$(bindir)/$(itweb_settings).sh ;\
	PROGRAM_NAME=$(itweb_settings) ;\
	$(edit_launcher_script) < $< > $@

launcher.build/$(policyeditor).sh: shell-launcher/launchers.sh.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.security.policyeditor.PolicyEditor ;\
	BIN_LOCATION=$(bindir)/$(policyeditor).sh ;\
	PROGRAM_NAME=$(policyeditor) ;\
	$(edit_launcher_script) < $< > $@

if WINDOWS_OR_INCLUDE_BATS
# TODO get rid of this copypaste
launcher.build/$(javaws).bat: shell-launcher/launchers.bat.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.runtime.Boot ;\
	BIN_LOCATION=$(bindir)/$(javaws) ;\
	PROGRAM_NAME=$(javaws) ;\
	$(edit_launcher_script) $(fix_CRLF) < $< > $@

# TODO get rid of this copypaste
launcher.build/$(itweb_settings).bat: shell-launcher/launchers.bat.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.controlpanel.CommandLine ;\
	BIN_LOCATION=$(bindir)/$(itweb_settings) ;\
	PROGRAM_NAME=$(itweb_settings) ;\
	$(edit_launcher_script) $(fix_CRLF) < $< > $@

# TODO get rid of this copypaste
launcher.build/$(policyeditor).bat: shell-launcher/launchers.bat.in
	mkdir -p launcher.build
	MAIN_CLASS=net.sourceforge.jnlp.security.policyeditor.PolicyEditor ;\
	BIN_LOCATION=$(bindir)/$(policyeditor) ;\
	PROGRAM_NAME=$(policyeditor) ;\
	$(edit_launcher_script) $(fix_CRLF) < $< > $@
endif
endif

clean-launchers:
	rm -f launcher.build/$(MODULARJDK_ARGS_FILE)
if ENABLE_NATIVE_LAUNCHERS
	rm -f launcher.build/$(javaws)
	rm -f launcher.build/$(itweb_settings)
	rm -f launcher.build/$(policyeditor)
	rm -rf launcher.in*
	rm -rf $(TESTS_DIR)/rust_tests_tmp
endif
if ENABLE_SHELL_LAUNCHERS
	rm -f launcher.build/$(javaws).sh
	rm -f launcher.build/$(itweb_settings).sh
	rm -f launcher.build/$(policyeditor).sh
endif
if WINDOWS_OR_INCLUDE_BATS
	rm -f launcher.build/$(javaws).bat
	rm -f launcher.build/$(itweb_settings).bat
	rm -f launcher.build/$(policyeditor).bat
endif
	if [ -e launcher.build ] ; then \
	  rmdir launcher.build ; \
	fi

javaws.desktop: javaws.desktop.in
	sed "s#PATH_TO_JAVAWS#$(bindir)/$(JAVAWS)#" < $(srcdir)/javaws.desktop.in > javaws.desktop

itweb-settings.desktop: $(srcdir)/itweb-settings.desktop.in
	sed "s#PATH_TO_ITWEB_SETTINGS#$(bindir)/$(ITWEB_SETTINGS)#" \
	  < $(srcdir)/itweb-settings.desktop.in > itweb-settings.desktop

policyeditor.desktop: $(srcdir)/policyeditor.desktop.in
	sed "s#PATH_TO_POLICYEDITOR#$(bindir)/$(POLICYEDITOR)#" \
	< $(srcdir)/policyeditor.desktop.in > policyeditor.desktop

# documentation

stamps/docs.stamp: stamps/netx-docs.stamp stamps/plugin-docs.stamp
	touch stamps/docs.stamp

clean-docs: clean-netx-docs clean-plugin-docs
	if [ -e ${TOP_BUILD_DIR}/docs ] ; then \
	  rmdir ${TOP_BUILD_DIR}/docs ; \
	fi
	rm -f stamps/docs.stamp

stamps/netx-docs.stamp:
if ENABLE_DOCS
	$(SYSTEM_JDK_DIR)/bin/javadoc $(JAVADOC_MEM_OPTS) $(JAVADOC_OPTS) \
	 -d ${TOP_BUILD_DIR}/docs/netx -sourcepath $(NETX_SRCDIR) \
	 -doctitle 'IcedTea-Web: NetX API Specification' \
	 -windowtitle 'IcedTea-Web: NetX ' \
	 -header '<strong>IcedTea-Web<br/>NetX</strong>' \
	 $(call composeclasspath, $(MSLINKS_JAR) $(TAGSOUP_JAR) $(RHINO_JAR)) \
	 $(NETX_PKGS) $(SUN_APPLET_PKGS)
endif
	mkdir -p stamps
	touch stamps/netx-docs.stamp

clean-netx-docs:
	rm -rf ${TOP_BUILD_DIR}/docs/netx
	rm -f stamps/netx-docs.stamp

stamps/plugin-docs.stamp:
if ENABLE_DOCS
if ENABLE_PLUGINJAR
	$(SYSTEM_JDK_DIR)/bin/javadoc $(JAVADOC_MEM_OPTS) $(JAVADOC_OPTS) \
	 -d ${TOP_BUILD_DIR}/docs/plugin -sourcepath "$(NETX_SRCDIR)$(CLASSPATH_SEPARATOR)$(LIVECONNECT_SRCS)" \
	 -doctitle 'IcedTea-Web: Plugin API Specification' \
	 -windowtitle 'IcedTea-Web: Plugin ' \
	 -header '<strong>IcedTea-Web<br/>Plugin</strong>' \
	 $(call composeclasspath, $(MSLINKS_JAR) $(TAGSOUP_JAR) $(RHINO_JAR)) \
	 $(PLUGIN_PKGS)
endif
endif
	mkdir -p stamps
	touch stamps/plugin-docs.stamp

clean-plugin-docs:
	rm -rf ${TOP_BUILD_DIR}/docs/plugin
	rm -f stamps/plugin-docs.stamp

clean-generated-docs:
	rm -rf "$(DOCS_DIR)"
	rm -f stamps/generate-docs.stamp

clean-icedteaweb-completion:
	rm -f $(TOP_BUILD_DIR)/completion/itweb-settings.bash
	rm -f $(TOP_BUILD_DIR)/completion/javaws.bash
	rm -f $(TOP_BUILD_DIR)/completion/policyeditor.bash

clean-metadata:
	rm -rf $(TOP_BUILD_DIR)/metadata

# check
# ==========================

clean-tests: clean-netx-tests clean-cpp-unit-tests clean-unittest++
	if [ -e $(CPP_UNITTEST_DIR) ] ; then \
		rmdir $(CPP_UNITTEST_DIR) ; \
	fi
	if [ -e $(TESTS_DIR) ]; then \
		rmdir $(TESTS_DIR) ; \
	fi

stamps/check-pac-functions.stamp:
	./jrunscript $(TOP_SRC_DIR)/tests/netx/pac/pac-funcs-test.js \
	  $$(readlink -f $(TOP_SRC_DIR)/netx/net/sourceforge/jnlp/runtime/pac-funcs.js) ; \
	mkdir -p stamps && \
	touch $@

junit-runner-source-files.txt:
	find $(JUNIT_RUNNER_SRCDIR) -name '*.java' | sort > $@

jacoco-operator-source-files.txt:
	find $(JACOCO_OPERATOR_SRCDIR) -name '*.java' | sort > $@

$(JUNIT_RUNNER_JAR): junit-runner-source-files.txt stamps/test-extensions-compile.stamp
	mkdir -p $(JUNIT_RUNNER_DIR) && \
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	  -d $(JUNIT_RUNNER_DIR) \
	  $(call composeclasspath, $(JUNIT_RUNTIME) $(TEST_EXTENSIONS_DIR)) \
	  @junit-runner-source-files.txt && \
	$(SYSTEM_JDK_DIR)/bin/jar cf $@  -C $(JUNIT_RUNNER_DIR) .

stamps/junit-jnlp-dist-dirs: junit-jnlp-dist-simple.txt stamps/junit-jnlp-dist-signed.stamp junit-jnlp-dist-custom.txt
	mkdir -p $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)
	mkdir -p $(REPRODUCERS_BUILD_DIR)
	touch $@

junit-jnlp-dist-custom.txt:
	rm -f $(abs_top_builddir)/$@ ; \
	cd $(REPRODUCERS_TESTS_SRCDIR)/$(CUSTOM_REPRODUCERS)/ ; \
	for x in $(COMPILE_WHITELIST_FILTER) ; do \
	  find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" | grep $$x >> $(abs_top_builddir)/$@ || true ; \
	done

junit-jnlp-dist-simple.txt:
	rm -f $(abs_top_builddir)/$@ ; \
	cd $(REPRODUCERS_TESTS_SRCDIR)/simple/ ; \
	for x in $(COMPILE_WHITELIST_FILTER) ; do \
	  find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" | grep $$x >> $(abs_top_builddir)/$@ || true ; \
	done

stamps/junit-jnlp-dist-signed.stamp:
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  rm -f $(abs_top_builddir)/junit-jnlp-dist-$$which.txt ; \
	  pushd $(REPRODUCERS_TESTS_SRCDIR)/$$which/ ; \
	    for x in $(COMPILE_WHITELIST_FILTER) ; do \
	      find .  -maxdepth 1 -mindepth 1 | sed "s/.\/*//" | grep $$x >> $(abs_top_builddir)/junit-jnlp-dist-$$which.txt ; \
	    done ; \
	  popd ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-prepare-reproducers.stamp: stamps/junit-jnlp-dist-dirs stamps/liveconnect-dist.stamp stamps/netx-dist.stamp stamps/plugin.stamp
	types=($(ALL_NONCUSTOM_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    echo "processing: $$dir" ; \
	    mkdir -p "$(REPRODUCERS_BUILD_DIR)/$$dir" ; \
	    if [ -e "$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/srcs/" ]; then \
	      d=`pwd` ; \
	      cd "$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/srcs/" ; \
	      srcFiles=`find . -mindepth 1 -type f -name "*.java" | sed "s/.\/*//"` ; \
	      notSrcFiles=`find . -mindepth 1 -type f \! -name "*.java" | sed "s/.\/*//"` ; \
	      $(SYSTEM_JDK_DIR)/bin/javac -cp $(NETX_DIR)/lib/classes.jar\$(CLASSPATH_SEPARATOR)$(TOP_BUILD_DIR)/liveconnect -d "$(REPRODUCERS_BUILD_DIR)/$$dir/" $$srcFiles ; \
	      if [ -n "$$notSrcFiles" ] ; then \
	        cp -R --parents $$notSrcFiles "$(REPRODUCERS_BUILD_DIR)/$$dir/" ; \
	      fi ; \
	      cd "$(REPRODUCERS_BUILD_DIR)/$$dir/" ; \
	      if [ -f $(META_MANIFEST) ]; \
	      then \
	        $(SYSTEM_JDK_DIR)/bin/jar cfm "$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" $(META_MANIFEST) * ; \
	      else \
	        $(SYSTEM_JDK_DIR)/bin/jar cf "$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/$$dir.jar" * ; \
	      fi; \
	      AN="Application-Name:" ; \
	      grep $$AN $(META_MANIFEST) 2>/dev/null >/dev/null ; \
	      GREP_R=$$?; \
	      if [ ! -f $(META_MANIFEST) -o  $$GREP_R -ne 0  ]; \
	      then \
	       export TMP_MANIFEST=`mktemp` ; \
	       echo "$$AN $$dir" > $$TMP_MANIFEST ; \
	       if  which cygpath &> /dev/null  ; then \
	         export TMP_MANIFEST=$$(cygpath -p -m "$$TMP_MANIFEST") ; \
	       fi ; \
	       $(SYSTEM_JDK_DIR)/bin/jar ufm "$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/$$dir.jar"  $$TMP_MANIFEST ; \
	       rm -f $$TMP_MANIFEST ; \
	      fi ; \
	      cd "$$d" ; \
	    fi; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-sign-some-reproducers.stamp: stamps/netx-dist-tests-prepare-reproducers.stamp
	export keystore=$(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME); \
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  tcaw=$(TEST_CERT_ALIAS)_$$which ; \
	  $(SYSTEM_JRE_DIR)/bin/keytool -genkey -alias $$tcaw -keystore $$keystore -keypass $(PRIVATE_KEYSTORE_PASS) -storepass $(PRIVATE_KEYSTORE_PASS) -dname "cn=$$tcaw, ou=$$tcaw, o=$$tcaw, c=$$tcaw" ; \
	    . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  signedReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	    IFS="$$IFS_BACKUP" ; \
	  for dir in "$${signedReproducers[@]}" ; do \
	   $(SYSTEM_JDK_DIR)/bin/jarsigner -keystore $$keystore -storepass $(PRIVATE_KEYSTORE_PASS) -keypass $(PRIVATE_KEYSTORE_PASS)  "$(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/$$dir.jar"  $$tcaw ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/change-dots-to-paths.stamp: stamps/netx-dist-tests-sign-some-reproducers.stamp
	pushd  $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR); \
	types=($(ALL_NONCUSTOM_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
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
	types=($(SIGNED_REPRODUCERS)) ; \
	PUBLIC_KEYSTORE=$$XDG_CONFIG_HOME ; \
	if test "x$$PUBLIC_KEYSTORE" = x; then \
	  PUBLIC_KEYSTORE=${HOME}/.config ; \
	fi ;\
	export PUBLIC_KEYSTORE=$$PUBLIC_KEYSTORE/$(PUBLIC_KEYSTORE_STUB); \
	if  which cygpath &> /dev/null  ; then \
	  export PUBLIC_KEYSTORE=$$(cygpath -p -m "$$PUBLIC_KEYSTORE") ; \
	fi ; \
	keystoredir=`dirname $$PUBLIC_KEYSTORE`; \
	[ ! -d $$keystoredir ] && mkdir -p $$keystoredir; \
	for which in "$${types[@]}" ; do \
	  $(SYSTEM_JDK_DIR)/bin/keytool -delete -alias $(TEST_CERT_ALIAS)_$$which -keystore $$PUBLIC_KEYSTORE -storepass $(PUBLIC_KEYSTORE_PASS) ; \
	done ;
	types=($(SIGNED_REPRODUCERS)) ; \
	for which in "$${types[@]}" ; do \
	  $(SYSTEM_JDK_DIR)/bin/keytool -export -alias $(TEST_CERT_ALIAS)_$$which -file $(EXPORTED_TEST_CERT_PREFIX)_$$which.$(EXPORTED_TEST_CERT_SUFFIX) -storepass $(PRIVATE_KEYSTORE_PASS) -keystore $(PRIVATE_KEYSTORE_NAME) ; \
	done ;
	mkdir -p stamps && \
	touch $@

stamps/netx-dist-tests-import-cert-to-public: stamps/exported-test-certs.stamp
	types=($(SIGNED_REPRODUCERS)) ; \
	PUBLIC_KEYSTORE=$$XDG_CONFIG_HOME ; \
	if test "x$$PUBLIC_KEYSTORE" = x; then \
	  PUBLIC_KEYSTORE=${HOME}/.config ; \
	fi ;\
	export PUBLIC_KEYSTORE=$$PUBLIC_KEYSTORE/$(PUBLIC_KEYSTORE_STUB); \
	if  which cygpath &> /dev/null  ; then \
	  export PUBLIC_KEYSTORE=$$(cygpath -p -m "$$PUBLIC_KEYSTORE") ; \
	fi ; \
	keystoredir=`dirname $$PUBLIC_KEYSTORE`; \
	[ ! -d $$keystoredir ] && mkdir -p $$keystoredir; \
	for which in "$${types[@]}" ; do \
	  yes | $(SYSTEM_JDK_DIR)/bin/keytool -import -alias $(TEST_CERT_ALIAS)_$$which -keystore $$PUBLIC_KEYSTORE -storepass $(PUBLIC_KEYSTORE_PASS) -file $(EXPORTED_TEST_CERT_PREFIX)_$$which.$(EXPORTED_TEST_CERT_SUFFIX) ;\
	done ;
	mkdir -p stamps && \
	touch $@

netx-dist-tests-remove-cert-from-public:
	types=($(SIGNED_REPRODUCERS)) ; \
	PUBLIC_KEYSTORE=$$XDG_CONFIG_HOME ; \
	if test "x$$PUBLIC_KEYSTORE" = x; then \
	  PUBLIC_KEYSTORE=${HOME}/.config ; \
	fi ;\
	export PUBLIC_KEYSTORE=$$PUBLIC_KEYSTORE/$(PUBLIC_KEYSTORE_STUB); \
	if  which cygpath &> /dev/null  ; then \
	  export PUBLIC_KEYSTORE=$$(cygpath -p -m "$$PUBLIC_KEYSTORE") ; \
	fi ; \
	keystoredir=`dirname $$PUBLIC_KEYSTORE`; \
	[ ! -d $$keystoredir ] && mkdir -p $$keystoredir; \
	for which in "$${types[@]}" ; do \
	  $(SYSTEM_JDK_DIR)/bin/keytool -delete -alias $(TEST_CERT_ALIAS)_$$which -keystore $$PUBLIC_KEYSTORE -storepass $(PUBLIC_KEYSTORE_PASS) ; \
	done ;
	-rm -rf stamps/netx-dist-tests-import-cert-to-public

test-extensions-source-files.txt:
	find $(TEST_EXTENSIONS_SRCDIR) -name '*.java' | sort > $@

stamps/test-extensions-compile.stamp: stamps/netx-dist.stamp stamps/plugin.stamp stamps/junit-jnlp-dist-dirs test-extensions-source-files.txt
	mkdir -p $(TEST_EXTENSIONS_DIR);
	mkdir -p $(NETX_TEST_DIR);
	ln -s $(TEST_EXTENSIONS_DIR) $(TEST_EXTENSIONS_COMPATIBILITY_SYMLINK);
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(TEST_EXTENSIONS_DIR) \
	 $(call composeclasspath, $(JUNIT_RUNTIME) $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar) \
	 @test-extensions-source-files.txt && \
	mkdir -p stamps && \
	touch $@

test-extensions-tests-source-files.txt:
	for x in $(COMPILE_WHITELIST_FILTER) ; do \
	  find $(TEST_EXTENSIONS_TESTS_SRCDIR) -name '*.java' | grep $$x | sort > $@ ; \
	done

stamps/test-extensions-tests-compile.stamp: stamps/junit-jnlp-dist-dirs test-extensions-tests-source-files.txt  stamps/test-extensions-compile.stamp
	mkdir -p $(TEST_EXTENSIONS_TESTS_DIR);
	for x in `cat $(TOP_BUILD_DIR)/test-extensions-tests-source-files.txt` ; do \
	  $(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	   -d $(TEST_EXTENSIONS_TESTS_DIR) \
	   $(call composeclasspath, $(JUNIT_RUNTIME) $(NETX_DIR)/lib/classes.jar $(TEST_EXTENSIONS_DIR)) \
	   $$x ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/compile-reproducers-testcases.stamp: stamps/netx-dist.stamp stamps/plugin.stamp stamps/junit-jnlp-dist-dirs \
 test-extensions-source-files.txt stamps/test-extensions-compile.stamp stamps/test-extensions-tests-compile.stamp
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    TESTCASEPATH="$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/testcases" ; \
	    if [ -d "$$TESTCASEPATH" ]; then \
	        echo "compiling" $$dir ; \
	        pushd "$$TESTCASEPATH" ; \
	        REGEX=".*\\.java$$" ; \
	        JAVAFILES=`find . | grep "$$REGEX"` ; \
	        NONJAVA_RESOURCES=`find . | grep -v "$$REGEX"` ; \
		    echo $$JAVAFILES ; \
		    echo $$NONJAVA_RESOURCES ; \
	        $(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	          -d $(TEST_EXTENSIONS_TESTS_DIR) \
	          $(call composeclasspath, $(JUNIT_RUNTIME) $(NETX_DIR)/lib/classes.jar $(TEST_EXTENSIONS_DIR)) $$JAVAFILES ; \
	        if [ -n "$$NONJAVA_RESOURCES" ]; then \
	            cp --parents $$NONJAVA_RESOURCES $(TEST_EXTENSIONS_TESTS_DIR)/ ; \
	        fi ; \
	        popd ; \
	    fi ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

stamps/copy-reproducers-resources.stamp: stamps/junit-jnlp-dist-dirs
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    cp -R "$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/resources/"*  $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)/ ; \
	  done ; \
	done ; \
	mkdir -p stamps && \
	touch $@

$(REPRODUCERS_CLASS_NAMES):
	whiteListed=$(RUN_WHITELIST_FILTER); \
	cd $(TEST_EXTENSIONS_TESTS_DIR) ; \
	class_names= ; \
	for test in `find -type f` ; do \
	  class_name=`echo $$test | sed -e 's|\.class$$||' -e 's|^\./||'` ; \
	  class_name=`echo $$class_name | sed -e 's|/|.|g' ` ; \
	  INCLUDE="NO" ; \
	  for x in $$whiteListed ; do \
	    q=`expr match "$$class_name" "$$x"`; \
	    r=$$? ; \
	    if [ $$r = 0  ]; then \
	      echo "$$class_name will be included in reproducers testcases because of $$x pattern in $(REPRODUCERS_CLASS_WHITELIST). Matching was  $$q"; \
	      INCLUDE="YES" ; \
	    fi; \
	  done; \
	  if [ "$$INCLUDE" = "YES" ]; then \
	    class_names="$$class_names $$class_name" ; \
	  else \
	    echo "$$class_name had no match in $(REPRODUCERS_CLASS_WHITELIST). Excluding"; \
	  fi; \
	done ; \
	echo $$class_names > $(REPRODUCERS_CLASS_NAMES)

stamps/run-netx-dist-tests.stamp: stamps/netx-dist.stamp stamps/plugin.stamp stamps/docs.stamp \
 $(LAUNCHERS) $(DESKTOP_FILES) \
 stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/test-extensions-compile.stamp stamps/compile-reproducers-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/copy-reproducers-resources.stamp\
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) $(REPRODUCERS_CLASS_NAMES) stamps/process-custom-reproducers.stamp
	cd $(TEST_EXTENSIONS_DIR) ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) . $(TEST_EXTENSIONS_TESTS_DIR) $(TEST_EXTENSIONS_SRCDIR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath/a:$$CLASSPATH CommandLine $$class_names
if WITH_XSLTPROC
	-$(XSLTPROC)  --stringparam logs logs_reproducers.html $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(TEST_EXTENSIONS_DIR)/tests-output.xml > $(TESTS_DIR)/index_reproducers.html
	-$(XSLTPROC)  $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/logs.xsl $(TEST_EXTENSIONS_DIR)/ServerAccess-logs.xml > $(TESTS_DIR)/logs_reproducers.html
	-$(XSLTPROC)  $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/textreport.xsl $(TEST_EXTENSIONS_DIR)/tests-output.xml > $(TESTS_DIR)/summary_reproducers.txt
endif
	touch $@

stamps/process-custom-reproducers.stamp: stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/test-extensions-compile.stamp stamps/compile-reproducers-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/copy-reproducers-resources.stamp\
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME) $(REPRODUCERS_CLASS_NAMES)
	. $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	customReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-custom.txt `); \
	IFS="$$IFS_BACKUP" ; \
	for dir in "$${customReproducers[@]}" ; do \
	  pushd $(REPRODUCERS_TESTS_SRCDIR)/$(CUSTOM_REPRODUCERS)/$$dir/srcs; \
	  $(MAKE) prepare-reproducer ; \
	  popd ; \
	done ; \
	mkdir -p stamps && \
	touch $@

clean-custom-reproducers: junit-jnlp-dist-custom.txt
	. $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	customReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-custom.txt `); \
	IFS="$$IFS_BACKUP" ; \
	for dir in "$${customReproducers[@]}" ; do \
	  pushd $(REPRODUCERS_TESTS_SRCDIR)/custom/$$dir/srcs; \
	  $(MAKE) clean-reproducer ; \
	  popd ; \
	done ; \
	rm -f stamps/process-custom-reproducers.stamp

#for global-links you must be root, for opera there do not exists user-links
#although this targets will indeed create symbolic links to enable 
#icedtea-web plugin inside browser it is intended for testing purposes
if ENABLE_NATIVE_PLUGIN
stamps/user-links.stamp: stamps/netx-dist.stamp stamps/plugin.stamp \
 $(LAUNCHERS) stamps/netx.stamp $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)
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

stamps/global-links.stamp: stamps/netx-dist.stamp stamps/plugin.stamp $(LAUNCHERS) \
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
 netx-unit-tests-source-files.txt stamps/test-extensions-compile.stamp
	mkdir -p $(NETX_UNIT_TEST_DIR) && \
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d $(NETX_UNIT_TEST_DIR) \
	 $(call composeclasspath, $(JUNIT_RUNTIME) $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar $(NETX_DIR)/lib/classes.jar $(MSLINKS_JAR)  $(TEST_EXTENSIONS_DIR) $(TAGSOUP_JAR)) \
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
	CLASSPATH="$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar $(JUNIT_RUNTIME) $(MSLINKS_JAR) $(JUNIT_RUNNER_JAR) $(TEST_EXTENSIONS_DIR) . $(TEST_EXTENSIONS_SRCDIR) $(TAGSOUP_JAR))" ; \
	  $(SYSTEM_JRE_DIR)/bin/java "-Xbootclasspath/a:$$CLASSPATH" CommandLine $$class_names 
if WITH_XSLTPROC
	-$(XSLTPROC) --stringparam logs logs_unit.html $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/jreport.xsl $(NETX_UNIT_TEST_DIR)/tests-output.xml > $(TESTS_DIR)/index_unit.html
	-$(XSLTPROC) $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/logs.xsl $(NETX_UNIT_TEST_DIR)/ServerAccess-logs.xml > $(TESTS_DIR)/logs_unit.html
	-$(XSLTPROC) $(TESTS_SRCDIR)/$(REPORT_STYLES_DIRNAME)/textreport.xsl $(NETX_UNIT_TEST_DIR)/tests-output.xml > $(TESTS_DIR)/summary_unit.txt
endif
	mkdir -p stamps && \
	touch $@

stamps/compile-jacoco-operator.stamp: jacoco-operator-source-files.txt
if WITH_JACOCO	
	mkdir -p $(JACOCO_OPERATOR_DIR) && \
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	  -d $(JACOCO_OPERATOR_DIR) \
	  $(call composeclasspath, $(JACOCO_CLASSPATH)) \
	  @jacoco-operator-source-files.txt ;
else 
	echo "Sorry, jacoco coverage report generator  cant be compiled without jacoco installed. Try installing jacoco or specify with-jacoco value" ;
	exit 5
endif 
	touch $@


#warning,  during this target tests.build/netx/unit/tests-output.xml is backup and rewriten (but not coresponding html file)
#xml results run with jacoco agent however, can be wrong, co the new tests-output.xml is then renamed and orginal one restored
stamps/run-unit-test-code-coverage-jacoco.stamp: stamps/netx-unit-tests-compile.stamp $(JUNIT_RUNNER_JAR)  \
 $(TESTS_DIR)/$(REPORT_STYLES_DIRNAME)  $(UNIT_CLASS_NAMES) stamps/compile-jacoco-operator.stamp
if WITH_JACOCO
	filename=" " ; \
	cd $(NETX_UNIT_TEST_SRCDIR) ; \
	for file in `find . -type f \! -iname "*.java"`; do\
		filename=`echo $$file `; \
		cp --parents $$filename $(NETX_UNIT_TEST_DIR) ; \
	done ; \
	cd $(NETX_UNIT_TEST_DIR) ; \
	for file in $(COVERAGE_MODIFIED_FILES) ; do \
	  mv $(NETX_UNIT_TEST_DIR)/$$file  $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)" ; \
	done ;\
	class_names=`cat $(UNIT_CLASS_NAMES)` ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) $(TEST_EXTENSIONS_DIR) $(JACOCO_CLASSPATH) . $(TEST_EXTENSIONS_SRCDIR) $(MSLINKS_JAR) $(TAGSOUP_JAR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(JACOCO_AGENT_SWITCH) -Xbootclasspath/a:$$CLASSPATH CommandLine $$class_names ; \
	for file in $(COVERAGE_MODIFIED_FILES) ; do \
	  mv $(NETX_UNIT_TEST_DIR)/$$file  $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_SUFFIX)" ; \
	  mv $(NETX_UNIT_TEST_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)"  $(NETX_UNIT_TEST_DIR)/$$file ; \
	done ; \
	$(JACOCO_OPERATOR_EXEC) \
	  report --die-soon --html-output coverage --xml-output coverage.xml --input-file jacoco.exec \
	  --input-srcs  $(NETX_SRCDIR) $(PLUGIN_SRCDIR)/java $(NETX_UNIT_TEST_SRCDIR) $(JUNIT_RUNNER_SRCDIR) $(TEST_EXTENSIONS_SRCDIR) \
	  --input-builds $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar $(NETX_UNIT_TEST_DIR) $(JUNIT_RUNNER_JAR)  $(TEST_EXTENSIONS_DIR) \
	  --title "IcedTea-Web unit-tests codecoverage" ;
else 
	echo "Sorry, coverage report cant be run without jacoco installed. Try installing jacoco or specify with-jacoco value" ;
	exit 5
endif 
	touch $@

$(COVERABLE_PLUGIN_DIR):
	mkdir -p $(COVERABLE_PLUGIN_DIR);
	
$(COVERABLE_PLUGIN_DIR)/%.o: $(PLUGIN_SRCDIR)/%.cc
	cd $(COVERABLE_PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	   $(DEFS) $(VERSION_DEFS) \
	  -DJDK_UPDATE_VERSION="\"$(JDK_UPDATE_VERSION)\"" \
	  -DPLUGIN_NAME="\"IcedTea-Web Plugin with jacoco coverage agent\"" \
	  -DJAVAWS_BIN="\"$(bindir)/$(JAVAWS)\"" \
	  -DJAVAWS_NAME="\"$(javaws)\"" \
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DPACKAGE_URL="\"$(PACKAGE_URL)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
	  -DICEDTEA_WEB_JRE="\"$(SYSTEM_JRE_DIR)\"" \
	  -DPLUGIN_BOOTCLASSPATH=$(PLUGIN_COVERAGE_BOOTCLASSPATH) \
	  -DCOVERAGE_AGENT=$(JACOCO_AGENT_PLUGIN_SWITCH) \
	  -DNETX_JAR="\"$(NETX_JAR)\"" \
	  -DPLUGIN_JAR="\"$(PLUGIN_JAR)\"" \
	  -DJSOBJECT_JAR="\"$(JSOBJECT_JAR)\""\
	  $(GLIB_CFLAGS) \
	  $(MOZILLA_CFLAGS) \
	  -fvisibility=hidden \
	  -fPIC -o $@ -c $<

$(COVERABLE_PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY): $(addprefix $(COVERABLE_PLUGIN_DIR)/,$(PLUGIN_OBJECTS))
	cd $(COVERABLE_PLUGIN_DIR) && \
	$(CXX) $(CXXFLAGS) \
	  $(PLUGIN_OBJECTS) \
	  $(GLIB_LIBS) \
	  $(MOZILLA_LIBS) \
	  -shared -o $@

stamps/build-fake-plugin.stamp:  $(COVERABLE_PLUGIN_DIR)  $(addprefix $(PLUGIN_SRCDIR)/,$(PLUGIN_SRC)) $(addprefix $(COVERABLE_PLUGIN_DIR)/,$(PLUGIN_OBJECTS)) stamps/liveconnect-dist.stamp  $(COVERABLE_PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY)
	touch $@

#warning,  during this target tests.build/netx/jnlp_testsengine/tests-output.xml is backup and rewriten (but not coresponding html file)
#xml results run with jacoco agent, however, can be wrong, co the new tests-output.xml is then renamed and orginal one restored
stamps/run-reproducers-test-code-coverage-jacoco.stamp: stamps/run-netx-dist-tests.stamp $(REPRODUCERS_CLASS_NAMES) \
stamps/compile-jacoco-operator.stamp stamps/build-fake-plugin.stamp
if WITH_JACOCO
	cd $(TESTS_DIR) ; \
	for file in $(COVERAGE_MODIFIED_FILES) ; do \
	  mv $(TEST_EXTENSIONS_DIR)/$$file $(TEST_EXTENSIONS_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)" ; \
	done ;\
	echo "backuping $(JAVAWS) in $(DESTDIR)$(bindir)" ; \
	javaws_backup=$(DESTDIR)$(bindir)/javaws_backup ; \
	mv $(DESTDIR)$(bindir)/$(JAVAWS) $$javaws_backup ;  \
	echo "patching $(JAVAWS)" ; \
	nw_bootclasspath="-Xbootclasspath/a:$(UNIFIED_JACOCO_CLASSPATH)" ; \
	cat $$javaws_backup | sed "s|COMMAND.k.=\"..JAVA.\"|COMMAND[k]=\"\\$$\\{JAVA\\}\" ;  k=1 ; COMMAND[k]=$(JACOCO_AGENT_JAVAWS_SWITCH)|" | sed "s,$(LAUNCHER_BOOTCLASSPATH),$$nw_bootclasspath," > $(DESTDIR)$(bindir)/$(JAVAWS) ; \
	chmod 777 $(DESTDIR)$(bindir)/$(JAVAWS) ; \
	echo "backuping plugin in $(DESTDIR)/$(libdir)$(BUILT_PLUGIN_LIBRARY)" ; \
	plugin_backup=$(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY)_backup ; \
	mv $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) $$plugin_backup ;  \
	echo "fakeing plugin" ; \
	cp $(COVERABLE_PLUGIN_DIR)/$(BUILT_PLUGIN_LIBRARY) $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) ; \
	testcases_srcs=( ) ; \
	k=0 ; \
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    testcases_srcs[k]="$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/testcases/" ; \
	    k=$$((k+1)) ; \
	    done ; \
	  done ; \
	cd $(TEST_EXTENSIONS_DIR) ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) . $(TEST_EXTENSIONS_DIR) $(JACOCO_CLASSPATH) $(TEST_EXTENSIONS_TESTS_DIR) $(TEST_EXTENSIONS_SRCDIR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(JACOCO_AGENT_SWITCH) $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath/a:$$CLASSPATH CommandLine $$class_names ; \
	if [ -f $(JACOCO_JAVAWS_RESULTS) ] ; then \
	  jacoco_javaws_results=$(JACOCO_JAVAWS_RESULTS) ; \
	  $(JACOCO_OPERATOR_EXEC) \
	    report --die-soon --html-output coverage-javaws --xml-output coverage-javaws.xml --input-file $(JACOCO_JAVAWS_RESULTS) \
	    --input-srcs  $(NETX_SRCDIR) \
	    --input-builds $(NETX_DIR)/lib/classes.jar \
	    --title "IcedTea-Web javaws reproducers codecoverage" ; \
	fi; \
	if [ -f $(JACOCO_PLUGIN_RESULTS) ] ; then \
	  jacoco_plugin_results=$(JACOCO_PLUGIN_RESULTS) ; \
	  $(JACOCO_OPERATOR_EXEC) \
	    report --die-soon --html-output coverage-plugin --xml-output coverage-plugin.xml --input-file $(JACOCO_PLUGIN_RESULTS) \
	    --input-srcs  $(NETX_SRCDIR) $(PLUGIN_SRCDIR)/java \
	    --input-builds $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar \
	    --title "IcedTea-Web plugin reproducers codecoverage" ; \
	fi; \
	$(JACOCO_OPERATOR_EXEC) \
	  merge --die-soon --input-files jacoco.exec $$jacoco_javaws_results $$jacoco_plugin_results  --output-file jacoco-merged-reproducers.exec ; \
	$(JACOCO_OPERATOR_EXEC) \
	  report --html-output coverage --xml-output coverage.xml --input-file jacoco-merged-reproducers.exec \
	  --input-srcs  $(NETX_SRCDIR) $(PLUGIN_SRCDIR)/java  $(JUNIT_RUNNER_SRCDIR) $(TEST_EXTENSIONS_SRCDIR) $(TEST_EXTENSIONS_TESTS_SRCDIR) "$${testcases_srcs[@]}" \
	  --input-builds $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar  $(JUNIT_RUNNER_JAR)  $(TEST_EXTENSIONS_DIR) $(TEST_EXTENSIONS_TESTS_DIR) \
	  --title "IcedTea-Web reproducers-tests codecoverage" ; \
	echo "restoring $(JAVAWS) in $(DESTDIR)$(bindir)" ; \
	rm -f $(DESTDIR)$(bindir)/$(javaws);  \
	mv $$javaws_backup $(DESTDIR)$(bindir)/$(JAVAWS);  \
	echo "restoring  plugin in $(DESTDIR)/$(libdir)$(BUILT_PLUGIN_LIBRARY)" ; \
	mv $$plugin_backup $(DESTDIR)$(libdir)/$(BUILT_PLUGIN_LIBRARY) ; \
	for file in $(COVERAGE_MODIFIED_FILES) ; do \
	  mv $(TEST_EXTENSIONS_DIR)/$$file  $(TEST_EXTENSIONS_DIR)/"$$file""$(EMMA_SUFFIX)" ; \
	  mv $(TEST_EXTENSIONS_DIR)/"$$file""$(EMMA_BACKUP_SUFFIX)"  $(TEST_EXTENSIONS_DIR)/$$file ; \
	done ;
else
	echo "Sorry, coverage report cant be run without jacoco installed. Try installing jacoco or specify with-jacoco value" ;
	exit 5
endif
	touch $@

run-test-code-coverage-jacoco: stamps/run-unit-test-code-coverage-jacoco.stamp stamps/run-reproducers-test-code-coverage-jacoco.stamp
if WITH_JACOCO
	cd $(TESTS_DIR) ; \
	k=0 ; \
	types=($(ALL_REPRODUCERS)); \
	for which in "$${types[@]}" ; do \
	  . $(TOP_SRC_DIR)/NEW_LINE_IFS ; \
	  simpleReproducers=(`cat $(TOP_BUILD_DIR)/junit-jnlp-dist-$$which.txt `); \
	  IFS="$$IFS_BACKUP" ; \
	  for dir in "$${simpleReproducers[@]}" ; do \
	    testcases_srcs[k]="$(REPRODUCERS_TESTS_SRCDIR)/$$which/$$dir/testcases/" ; \
	    k=$$((k+1)) ; \
	    done ; \
	  done ; \
	class_names=`cat $(REPRODUCERS_CLASS_NAMES)` ; \
	$(JACOCO_OPERATOR_EXEC) \
	  merge --die-soon --input-files $(TEST_EXTENSIONS_DIR)/jacoco-merged-reproducers.exec $(NETX_UNIT_TEST_DIR)/jacoco.exec  --output-file  jacoco-merged.exec; \
	$(JACOCO_OPERATOR_EXEC) \
	  report --html-output coverage --xml-output coverage.xml --input-file jacoco-merged.exec \
	  --input-srcs  $(NETX_SRCDIR) $(PLUGIN_SRCDIR)/java  $(JUNIT_RUNNER_SRCDIR) $(TEST_EXTENSIONS_SRCDIR) $(TEST_EXTENSIONS_TESTS_SRCDIR) "$${testcases_srcs[@]}" \
	  --input-builds $(NETX_DIR)/lib/classes.jar $(TOP_BUILD_DIR)/liveconnect/lib/classes.jar  $(JUNIT_RUNNER_JAR)  $(TEST_EXTENSIONS_DIR) $(TEST_EXTENSIONS_TESTS_DIR) \
	  --input-srcs  $(NETX_UNIT_TEST_SRCDIR)  \
	  --input-builds $(NETX_UNIT_TEST_DIR)  \
	  --title "IcedTea-Web complete codecoverage" ; 
else
	echo "Sorry, coverage report cant be run without jacoco installed. Try installing jacoco or specify with-jacoco value" ;
	exit 5
endif

run-test-server-on-44321: stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/test-extensions-compile.stamp stamps/compile-reproducers-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/copy-reproducers-resources.stamp
	cd $(TEST_EXTENSIONS_DIR) ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) . $(TEST_EXTENSIONS_TESTS_DIR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath/a:$$CLASSPATH net.sourceforge.jnlp.ServerAccess

run-test-server-on-random-port: stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/test-extensions-compile.stamp stamps/compile-reproducers-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/copy-reproducers-resources.stamp
	cd $(TEST_EXTENSIONS_DIR) ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) . $(TEST_EXTENSIONS_TESTS_DIR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath/a:$$CLASSPATH net.sourceforge.jnlp.ServerAccess randomport

run-test-server-on-itwtestsport: stamps/netx.stamp stamps/junit-jnlp-dist-dirs stamps/netx-dist-tests-import-cert-to-public \
 stamps/test-extensions-compile.stamp stamps/compile-reproducers-testcases.stamp $(JUNIT_RUNNER_JAR) stamps/copy-reproducers-resources.stamp
	cd $(TEST_EXTENSIONS_DIR) ; \
	CLASSPATH=$(call joinsegments, $(NETX_DIR)/lib/classes.jar $(JUNIT_RUNTIME) $(JUNIT_RUNNER_JAR) . $(TEST_EXTENSIONS_TESTS_DIR)) ; \
	  $(SYSTEM_JRE_DIR)/bin/java $(REPRODUCERS_DPARAMETERS) \
	 -Xbootclasspath/a:$$CLASSPATH net.sourceforge.jnlp.ServerAccess $$ITWTESTSPORT  

clean-netx-tests: clean-netx-unit-tests clean-junit-runner clean-netx-dist-tests clean-test-code-coverage-jacoco
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
	rm -f $(TESTS_DIR)/summary_unit.txt
	rm -f $(TESTS_DIR)/summary_reproducers.txt

clean-netx-dist-tests: clean_tests_reports netx-dist-tests-remove-cert-from-public clean-custom-reproducers
	rm -f test-extensions-source-files.txt
	rm -f test-extensions-tests-source-files.txt
	rm -f $(TEST_EXTENSIONS_COMPATIBILITY_SYMLINK)
	rm -rf $(TEST_EXTENSIONS_TESTS_DIR)
	rm -rf $(REPRODUCERS_BUILD_DIR)
	rm -rf $(REPRODUCERS_TESTS_SERVER_DEPLOYDIR)
	rm -rf $(TEST_EXTENSIONS_DIR)
	rm -f stamps/junit-jnlp-dist-dirs
	rm -f stamps/test-extensions-compile.stamp
	rm -f stamps/test-extensions-tests-compile.stamp
	rm -f stamps/netx-dist-tests-prepare-reproducers.stamp
	rm -f stamps/compile-reproducers-testcases.stamp
	rm -f stamps/copy-reproducers-resources.stamp
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
	rm -f $(TOP_BUILD_DIR)/$(PRIVATE_KEYSTORE_NAME)
	rm -f stamps/run-netx-dist-tests.stamp

clean-unit-test-code-coverage-jacoco:
	if [ -e stamps/run-unit-test-code-coverage-jacoco.stamp ]; then \
	  rm -rf $(NETX_UNIT_TEST_DIR)/coverage ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/coverage.xml ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/jacoco.exec ; \
	  rm -f $(NETX_UNIT_TEST_DIR)/tests-output_withEmma.xml ; \
	  rm -f stamps/run-unit-test-code-coverage-jacoco.stamp ; \
	fi	

clean-reproducers-test-code-coverage-jacoco:
	if [ -e stamps/run-reproducers-test-code-coverage-jacoco.stamp ]; then \
	 rm -rf $(TEST_EXTENSIONS_DIR)/coverage-javaws ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/coverage-javaws.xml ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/jacoco_javaws.exec ; \
	 rm -rf $(TEST_EXTENSIONS_DIR)/coverage-plugin ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/coverage-plugin.xml ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/jacoco_plugin.exec ; \
	 rm -rf $(TEST_EXTENSIONS_DIR)/coverage ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/coverage.xml ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/jacoco-merged-reproducers.exec ; \
	 rm -f $(TEST_EXTENSIONS_DIR)/tests-output_withEmma.xml ; \
	 rm -f stamps/run-reproducers-test-code-coverage-jacoco.stamp ; \
	fi

clean-test-code-coverage-jacoco: clean-unit-test-code-coverage-jacoco clean-reproducers-test-code-coverage-jacoco clean-test-code-coverage-tools-jacoco
	if [ -e $(TESTS_DIR)/coverage.xml ]; then \
	  rm -rf $(TESTS_DIR)/coverage  ; \
	  rm -f $(TESTS_DIR)/jacoco-merged.exec; \
	fi

clean-test-code-coverage-tools-jacoco:
	rm -rf  $(JACOCO_OPERATOR_DIR)
	rm -rf  $(COVERABLE_PLUGIN_DIR)
	rm -f stamps/compile-jacoco-operator.stamp;
	rm -f jacoco-operator-source-files.txt
	rm -f stamps/build-fake-plugin.stamp

# plugin tests

if ENABLE_NATIVE_PLUGIN
stamps/plugin-tests.stamp: $(PLUGIN_TEST_SRCS) stamps/plugin.stamp
	mkdir -p plugin/tests/LiveConnect
	$(SYSTEM_JDK_DIR)/bin/javac $(IT_JAVACFLAGS) \
	 -d plugin/tests/LiveConnect \
	 -classpath liveconnect/lib/classes.jar \
	 $(PLUGIN_TEST_SRCS) ;
	$(SYSTEM_JDK_DIR)/bin/jar cf plugin/tests/LiveConnect/PluginTest.jar \
	  plugin/tests/LiveConnect/*.class ;
	cp -pPR $(SRC_DIR_LINK) $(TOP_SRC_DIR)/plugin/tests/LiveConnect/*.{js,html} \
	  plugin/tests/LiveConnect ;
	echo "Done. Now launch \"firefox file://`pwd`/index.html\"" ;
	mkdir -p stamps
	touch stamps/plugin-tests.stamp
endif


printvarsfile:
	$(MAKE) printvars > $@

printvars:
	$(foreach v, $(.VARIABLES), $(info $(v)=$($(v))))



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

run-unit-test-code-coverage-jacoco: stamps/run-unit-test-code-coverage-jacoco.stamp

run-reproducers-test-code-coverage-jacoco: stamps/run-reproducers-test-code-coverage-jacoco.stamp
