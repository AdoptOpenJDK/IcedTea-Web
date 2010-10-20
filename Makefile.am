# Source directories

NETX_SRCDIR = $(abs_top_srcdir)/netx
NETX_RESOURCE_DIR=$(NETX_SRCDIR)/net/sourceforge/jnlp/resources
NETX_EXTRA_DIR=$(abs_top_srcdir)/extra/net/sourceforge/jnlp/about/resources

# Build directories

BOOT_DIR = $(abs_top_builddir)/bootstrap/jdk1.6.0

# PR43578 - java.security.CodeSource.getCodeSigners() missing
# PR43582 - Missing javax.swing.JTable.setFillsViewportHeight
# PR43585 - java.security.KeyStore.TrustedCertificateEntry class missing
NETX_BOOTSTRAP_CLASSES = \
	$(SHARE)/java/security/CodeSource.java \
	$(SHARE)/javax/swing/JTable.java \
	$(SHARE)/java/security/KeyStore.java

# PR46074 - Missing java.net cookie code required by IcedTea plugin
PLUGIN_BOOTSTRAP_CLASSES = \
	$(SHARE)/java/net/CookieManager.java \
	$(SHARE)/java/net/HttpCookie.java \
	$(SHARE)/java/net/CookieHandler.java

# IT563 - NetX uses sun.security code
# IT564 - NetX depends on sun.misc.BASE64Encoder
# IT570 - NetX depends on sun.applet.AppletViewPanel
# IT571 - NetX depends on com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager.java
NETX_SUN_CLASSES = \
	$(SHARE)/sun/security/provider/X509Factory.java \
	$(SHARE)/sun/security/util/SecurityConstants.java \
	$(SHARE)/sun/security/util/HostnameChecker.java \
	$(SHARE)/sun/security/util/DerValue.java \
	$(SHARE)/sun/security/x509/X500Name.java \
	$(SHARE)/sun/misc/BASE64Encoder.java \
	$(SHARE)/sun/applet/AppletViewerPanel.java \
	$(SHARE)/sun/security/validator/ValidatorException.java \
	$(SHARE)/com/sun/net/ssl/internal/ssl/X509ExtendedTrustManager.java

# IT573 - Plugin depends on sun.awt,X11.XEmbeddedFrame.java
# IT574 - Plugin depends on sun.misc.Ref
# IT575 - Plugin depends on com.sun/jndi.toolkit.url.UrlUtil
# IT576 - Plugin depends on sun.applet.AppletImageRef
PLUGIN_SUN_CLASSES = \
	$(SOLARIS)/sun/awt/X11/XEmbeddedFrame.java \
	$(SHARE)/sun/misc/Ref.java \
	$(SHARE)/com/sun/jndi/toolkit/url/UrlUtil.java \
	$(SHARE)/sun/applet/AppletImageRef.java

RUNTIME = $(BOOT_DIR)/jre/lib/rt.jar:$(BOOT_DIR)/jre/lib/jsse.jar

# Flags
IT_CFLAGS=$(CFLAGS) $(ARCHFLAG)
IT_JAVAC_SETTINGS=-g -encoding utf-8 $(JAVACFLAGS) $(MEMORY_LIMIT) $(PREFER_SOURCE)
IT_LANGUAGE_SOURCE_VERSION=6
IT_CLASS_TARGET_VERSION=6
IT_JAVACFLAGS=$(IT_JAVAC_SETTINGS) -source $(IT_LANGUAGE_SOURCE_VERSION) -target $(IT_CLASS_TARGET_VERSION)

# Sources list

PLUGIN_TEST_SRCS = $(abs_top_srcdir)/plugin/tests/LiveConnect/*.java

# Conditional defintions
if ENABLE_PLUGIN
ICEDTEAPLUGIN_CLEAN = clean-IcedTeaPlugin
LIVECONNECT_DIR = netscape sun/applet
PLUGIN_DIR=$(abs_top_builddir)/plugin/icedteanp
PLUGIN_SRCDIR=$(abs_top_srcdir)/plugin/icedteanp
LIVECONNECT_SRCS = $(PLUGIN_SRCDIR)/java
ICEDTEAPLUGIN_TARGET = $(PLUGIN_DIR)/IcedTeaPlugin.so stamps/liveconnect-dist.stamp \
 $(PLUGIN_DIR)/launcher/pluginappletviewer
endif

if CP_SUPPORTS_REFLINK
  REFLINK = --reflink=auto
endif

if SRC_DIR_HARDLINKABLE
  SRC_DIR_LINK = -l
else
  SRC_DIR_LINK = $(REFLINK)
endif

if HAS_ICEDTEA_REVISION
ICEDTEA_REV = +${ICEDTEA_REVISION}
endif
if HAS_PKGVERSION
ICEDTEA_PKG = $(EMPTY) (${PKGVERSION})
endif

# Launcher

LAUNCHER_SRCDIR = $(abs_top_srcdir)/launcher
LAUNCHER_OBJECTS = java.o java_md.o splashscreen_stubs.o
PLUGIN_LAUNCHER_OBJECTS = $(addprefix $(PLUGIN_DIR)/launcher/,$(LAUNCHER_OBJECTS))
NETX_LAUNCHER_OBJECTS = $(addprefix $(PLUGIN_DIR)/launcher/,$(LAUNCHER_OBJECTS))
LAUNCHER_FLAGS = -O2 -fno-strict-aliasing -fPIC -W -Wall -Wno-unused -Wno-parentheses -pipe -fno-omit-frame-pointer \
	-g -D_LARGEFILE64_SOURCE -D_GNU_SOURCE -D_REENTRANT -DLAUNCHER_NAME='"java"' -I$(LAUNCHER_SRCDIR) \
	-DJDK_MAJOR_VERSION='"1"' -DJDK_MINOR_VERSION='"6"' -DLIBARCHNAME='"$(JRE_ARCH_DIR)"'
LAUNCHER_LINK = -o $@ -Xlinker -O1 -Xlinker -z -Xlinker defs -L$(BOOT_DIR)/lib/$(INSTALL_ARCH_DIR) \
	-Wl,-soname=lib.so  -L $(BOOT_DIR)/jre/lib/$(INSTALL_ARCH_DIR)/jli -Wl,-z -Wl,origin \
	-Wl,--allow-shlib-undefined -Wl,-rpath -Wl,$$ORIGIN/../lib/$(INSTALL_ARCH_DIR)/jli -Wl,-rpath \
	-Wl,$$ORIGIN/../jre/lib/$(INSTALL_ARCH_DIR)/jli -lpthread $(X11_CFLAGS) $(X11_LIBS) -ljli  -ldl -lc -lz
PLUGIN_VERSION = IcedTea $(PACKAGE_VERSION)$(ICEDTEA_REV)$(ICEDTEA_PKG)

EXTRA_DIST = $(NETX_SRCDIR) $(abs_top_srcdir)/plugin javaws.png javaws.desktop extra launcher

# Top-Level Targets
# =================

all-local: stamps/netx-dist.stamp extra-lib/about.jar stamps/plugin.stamp launcher/javaws

clean-local: clean-netx clean-plugin clean-liveconnect clean-extra clean-bootstrap-directory clean-native-ecj

.PHONY: clean-IcedTeaPlugin clean-add-netx clean-add-netx-debug clean-add-plugin clean-add-plugin-debug \
 clean-bootstrap-directory clean-native-ecj

install-exec-local:
	mkdir -p $(DESTDIR)$(bindir)
	mkdir -p $(DESTDIR)$(prefix)/jre/lib/$(INSTALL_ARCH_DIR)
if ENABLE_PLUGIN
	cp -pPRf $(PLUGIN_DIR)/IcedTeaPlugin.so $(DESTDIR)$(prefix)/jre/lib/$(INSTALL_ARCH_DIR)/
	cp -pPRf $(PLUGIN_DIR)/launcher/pluginappletviewer $(DESTDIR)$(bindir)
	cp -pPRf $(abs_top_builddir)/liveconnect/lib/classes.jar $(DESTDIR)$(prefix)/jre/lib/plugin.jar
endif
	cp -pPRf $(abs_top_builddir)/netx.build/lib/classes.jar $(DESTDIR)$(prefix)/jre/lib/netx.jar
	cp -pPRf launcher/javaws $(DESTDIR)$(bindir)
	cp extra-lib/about.jar $(DESTDIR)$(prefix)/jre/lib

install-data-local:
	mkdir -p $(DESTDIR)$(prefix)/man/man1
	cp $(NETX_SRCDIR)/javaws.1 $(DESTDIR)$(prefix)/man/man1
	cp $(NETX_RESOURCE_DIR)/about.jnlp $(DESTDIR)$(prefix)/jre/lib

uninstall-local:
	rm -f $(DESTDIR)$(prefix)/jre/lib/$(INSTALL_ARCH_DIR)/IcedTeaPlugin.so
	rm -f $(DESTDIR)$(prefix)/jre/lib/plugin.jar
	rm -f $(DESTDIR)$(prefix)/jre/lib/netx.jar
	rm -f $(DESTDIR)$(prefix)/jre/lib/about.jnlp
	rm -f $(DESTDIR)$(prefix)/jre/lib/about.jar
	rm -f $(DESTDIR)$(prefix)/man/man1/javaws.1
	rm -f $(DESTDIR)$(bindir)/pluginappletviewer
	rm -f $(DESTDIR)$(bindir)/javaws

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
	  -DPLUGIN_VERSION="\"$(PLUGIN_VERSION)\"" \
	  -DMOZILLA_VERSION_COLLAPSED="$(MOZILLA_VERSION_COLLAPSED)" \
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

$(PLUGIN_DIR)/launcher/%.o: $(LAUNCHER_SRCDIR)/%.c
	mkdir -p $(PLUGIN_DIR)/launcher && \
	$(CC) $(LAUNCHER_FLAGS) -DJAVA_ARGS='{ "sun.applet.PluginMain" }' -DPROGNAME='"pluginappletviewer"'  -c -o $@ $<

$(PLUGIN_DIR)/launcher/pluginappletviewer: $(PLUGIN_LAUNCHER_OBJECTS)
	$(CC) $(LAUNCHER_LINK) $(PLUGIN_LAUNCHER_OBJECTS)

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
	      -classpath $(abs_top_builddir)/netx.build:$(RUNTIME) \
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

stamps/netx.stamp: netx-source-files.txt stamps/bootstrap-directory.stamp
	mkdir -p $(abs_top_builddir)/netx.build
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) \
	    -d $(abs_top_builddir)/netx.build \
	    -sourcepath $(NETX_SRCDIR) \
	    -bootclasspath $(RUNTIME) \
	    @netx-source-files.txt
	cp -r $(NETX_RESOURCE_DIR) $(abs_top_builddir)/netx.build/net/sourceforge/jnlp
	mkdir -p stamps
	touch $@

stamps/netx-dist.stamp: stamps/netx.stamp
	(cd $(abs_top_builddir)/netx.build ; \
	 mkdir -p lib ; \
	 $(BOOT_DIR)/bin/jar cf lib/classes.jar javax/jnlp net ; \
	 cp -pPR $(SRC_DIR_LINK) $(NETX_SRCDIR) src; \
	 find src -type f -exec chmod 640 '{}' ';' -o -type d -exec chmod 750 '{}' ';'; \
	 cd src ; \
	 $(BOOT_DIR)/bin/jar uf $(abs_top_builddir)/netx.build/lib/classes.jar \
	  `find . -type f -not -name '*.java'` ; \
	 $(ZIP) -qr $(abs_top_builddir)/netx.build/lib/src.zip javax net )
	mkdir -p stamps
	touch $@

clean-netx:
	rm -rf $(abs_top_builddir)/netx.build
	rm -f stamps/netx-dist.stamp
	rm -f netx-source-files.txt
	rm -f stamps/netx.stamp

# extras -- used to create about.jar for javaws.
extra-source-files.txt:
	find $(abs_top_srcdir)/extra -name '*.java' | sort > $@

stamps/extra-class-files.stamp: extra-source-files.txt stamps/netx.stamp
	mkdir -p extra-lib
	$(BOOT_DIR)/bin/javac $(IT_JAVACFLAGS) -d extra-lib \
          -sourcepath extra -cp netx.build \
          -bootclasspath $(RUNTIME) @extra-source-files.txt
	cp -r $(NETX_EXTRA_DIR) extra-lib/net/sourceforge/jnlp/about
	mkdir -p stamps
	touch $@

clean-extra:
	rm -rf extra-lib
	rm -f stamps/extra-class-files.stamp
	rm -f extra-source-files.txt

extra-lib/about.jar: stamps/extra-class-files.stamp
	$(BOOT_DIR)/bin/jar cf $@ -C extra-lib net ;

launcher/%.o: $(LAUNCHER_SRCDIR)/%.c
	mkdir -p launcher && \
	$(CC) $(LAUNCHER_FLAGS) -DJAVA_ARGS='{ "-J-ms8m", "net.sourceforge.jnlp.runtime.Boot",  }' -DPROGNAME='"javaws"' \
	-c -o $@ $<

launcher/javaws: $(NETX_LAUNCHER_OBJECTS)
	mkdir -p launcher
	$(CC) $(LAUNCHER_LINK) $(NETX_LAUNCHER_OBJECTS)

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
	ln -sf $(JAR) $(BOOT_DIR)/bin/jar
	ln -sf $(abs_top_builddir)/javac $(BOOT_DIR)/bin/javac
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
	rm -f stamps/bootstrap-directory.stamp

# Target Aliases
# ===============

add-netx: stamps/add-netx.stamp

add-netx-debug: stamps/add-netx-debug.stamp

netx: stamps/netx.stamp

netx-dist: stamps/netx-dist.stamp

plugin: stamps/plugin.stamp

plugin-tests: stamps/plugin-tests.stamp
