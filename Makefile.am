# Source directories

NETX_DIR = $(abs_top_builddir)/netx.build
NETX_SRCDIR = $(abs_top_srcdir)/netx
NETX_RESOURCE_DIR=$(NETX_SRCDIR)/net/sourceforge/jnlp/resources
NETX_EXTRA_DIR=$(abs_top_srcdir)/extra/net/sourceforge/jnlp/about/resources

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

JRE='"$(SYSTEM_JDK_DIR)/jre"'
LAUNCHER_BOOTCLASSPATH="-J-Xbootclasspath/a:$(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar$(RHINO_RUNTIME)"
PLUGIN_BOOTCLASSPATH='"-Xbootclasspath/a:$(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar:$(DESTDIR)$(datadir)/$(PACKAGE_NAME)/plugin.jar$(RHINO_RUNTIME)"'

# Fake update version to shut up the plugin detector hosted by Oracle.
# If Oracle ever release a JDK update greater than 50, this needs to be increased.
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

# Launcher

LAUNCHER_SRCDIR = $(abs_top_srcdir)/launcher
LAUNCHER_OBJECTS = java.o java_md.o splashscreen_stubs.o jli_util.o parse_manifest.o version_comp.o wildcard.o
NETX_LAUNCHER_OBJECTS = $(addprefix $(NETX_DIR)/launcher/,$(LAUNCHER_OBJECTS))
CONTROLPANEL_LAUNCHER_OBJECTS = $(addprefix $(NETX_DIR)/launcher/controlpanel/,$(LAUNCHER_OBJECTS))
LAUNCHER_FLAGS = -O2 -fno-strict-aliasing -fPIC -pthread -W -Wall -Wno-unused -Wno-parentheses -pipe -fno-omit-frame-pointer \
	-g -D_LARGEFILE64_SOURCE -D_GNU_SOURCE -D_REENTRANT -DLAUNCHER_NAME='"java"' -I$(LAUNCHER_SRCDIR) \
	-DJDK_MAJOR_VERSION='"1"' -DJDK_MINOR_VERSION='"6"' -DLIBARCHNAME='"$(JRE_ARCH_DIR)"' \
	-DEXPAND_CLASSPATH_WILDCARDS
LAUNCHER_LINK = -o $@ -pthread -Xlinker -O1 -Xlinker -z -Xlinker defs -L$(BOOT_DIR)/lib/$(INSTALL_ARCH_DIR) \
	-Wl,-soname=lib.so -Wl,-z -Wl,origin -Wl,--allow-shlib-undefined $(X11_CFLAGS) $(X11_LIBS) -ldl -lz
PLUGIN_VERSION = IcedTea-Web $(FULL_VERSION)

EXTRA_DIST = $(top_srcdir)/netx $(top_srcdir)/plugin javaws.png javaws.desktop.in extra launcher \
 itweb-settings.desktop.in

# Top-Level Targets
# =================

all-local: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp $(NETX_DIR)/launcher/javaws \
 javaws.desktop stamps/docs.stamp $(NETX_DIR)/launcher/controlpanel/itweb-settings itweb-settings.desktop

check-local: check-pac-functions

clean-local: clean-netx clean-plugin clean-liveconnect clean-extra clean-bootstrap-directory \
 clean-native-ecj clean-desktop-files clean-docs clean-tests
	if [ -e stamps ] ; then \
	  rmdir stamps ; \
	fi

.PHONY: clean-IcedTeaPlugin clean-add-netx clean-add-netx-debug clean-add-plugin clean-add-plugin-debug \
 clean-bootstrap-directory clean-native-ecj clean-desktop-files clean-netx-docs clean-docs clean-plugin-docs \
 clean-tests clean-jrunscript check-local

install-exec-local:
	${mkinstalldirs} $(DESTDIR)$(bindir) $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/ $(DESTDIR)$(libdir)
if ENABLE_PLUGIN
	${INSTALL_PROGRAM} $(PLUGIN_DIR)/IcedTeaPlugin.so $(DESTDIR)$(libdir)
	${INSTALL_DATA} $(abs_top_builddir)/liveconnect/lib/classes.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/plugin.jar
endif
	${INSTALL_DATA} $(NETX_DIR)/lib/classes.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/netx.jar
	${INSTALL_PROGRAM} $(NETX_DIR)/launcher/javaws $(DESTDIR)$(bindir)
	${INSTALL_DATA} extra-lib/about.jar $(DESTDIR)$(datadir)/$(PACKAGE_NAME)/about.jar
	${INSTALL_PROGRAM} $(NETX_DIR)/launcher/controlpanel/itweb-settings $(DESTDIR)$(bindir)

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
	rm -f $(DESTDIR)$(bindir)/javaws
	rm -f $(DESTDIR)$(bindir)/itweb-settings
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

build.properties:
	echo "# This contains build-time settings " > $@
if WITH_RHINO
	echo "rhino.available=true" >> $@
else
	echo "rhino.available=false" >> $@
endif

stamps/netx.stamp: netx-source-files.txt stamps/bootstrap-directory.stamp \
 build.properties
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
	rm -f build.properties
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
	   ${abs_top_builddir}/extra-lib/net/sourceforge/jnlp/about/resources/$${files}; \
	 done)
	mkdir -p stamps
	touch $@

clean-extra:
	rm -rf extra-lib
	rm -f stamps/extra-class-files.stamp
	rm -f extra-source-files.txt

extra-lib/about.jar: stamps/extra-class-files.stamp
	$(BOOT_DIR)/bin/jar cf $@ -C extra-lib net ;

$(NETX_DIR)/launcher/%.o: $(LAUNCHER_SRCDIR)/%.c
	mkdir -p $(NETX_DIR)/launcher && \
	$(CC) $(LAUNCHER_FLAGS) \
	  -DJAVA_ARGS='{ $(LAUNCHER_BOOTCLASSPATH), "-J-ms8m", "-J-Djava.icedtea-web.bin=$(DESTDIR)$(bindir)/javaws", "net.sourceforge.jnlp.runtime.Boot",  }' \
	  -DICEDTEA_WEB_JRE=$(JRE) -DPROGNAME='"javaws"' -c -o $@ $<

$(NETX_DIR)/launcher/controlpanel/%.o: $(LAUNCHER_SRCDIR)/%.c
	mkdir -p $(NETX_DIR)/launcher/controlpanel && \
	$(CC) $(LAUNCHER_FLAGS) \
	-DJAVA_ARGS='{ $(LAUNCHER_BOOTCLASSPATH), "-J-ms8m", "-Dprogram.name=itweb-settings", "net.sourceforge.jnlp.controlpanel.CommandLine",  }' \
	-DICEDTEA_WEB_JRE=$(JRE) -DPROGNAME='"itweb-settings"' -c -o $@ $<

$(NETX_DIR)/launcher/javaws: $(NETX_LAUNCHER_OBJECTS)
	$(CC) $(NETX_LAUNCHER_OBJECTS) $(LAUNCHER_LINK)

$(NETX_DIR)/launcher/controlpanel/itweb-settings: $(CONTROLPANEL_LAUNCHER_OBJECTS)
	$(CC) $(CONTROLPANEL_LAUNCHER_OBJECTS) $(LAUNCHER_LINK)

javaws.desktop: javaws.desktop.in
	sed "s#PATH_TO_JAVAWS#$(DESTDIR)$(bindir)/javaws#" < $(srcdir)/javaws.desktop.in > javaws.desktop

itweb-settings.desktop: $(srcdir)/itweb-settings.desktop.in
	sed "s#PATH_TO_ITWEB_SETTINGS#$(DESTDIR)$(bindir)/itweb-settings#" \
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

jrunscript:
if WITH_RHINO
	echo '$(BOOT_DIR)/bin/java -cp $(RHINO_JAR) org.mozilla.javascript.tools.shell.Main $$@' > jrunscript
	chmod u+x jrunscript
else
	echo "jrunscript requires rhino support"
	exit 1
endif

check-pac-functions: stamps/bootstrap-directory.stamp jrunscript
	./jrunscript $(abs_top_srcdir)/tests/netx/pac/pac-funcs-test.js \
	  $$(readlink -f $(abs_top_srcdir)/netx/net/sourceforge/jnlp/runtime/pac-funcs.js)

clean-tests: clean-jrunscript

clean-jrunscript:
	rm -f jrunscript

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
