# Prerequisites on Windows

1. Install [Visual C++ Build Tools](https://visualstudio.microsoft.com/thank-you-downloading-visual-studio/?sku=BuildTools&rel=16): select MSVC Build Tools + Windows 10 SDK
2. Install [WIX](https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311.exe)
3. Copy [wixgen.jar](https://github.com/akashche/wixgen/releases/download/1.7/wixgen.jar) to `C:\msi-deps`
4. Copy all 65 files from `C:\Program Files (x86)\WiX Toolset v3.11\bin` to `C:\msi-deps\wix311-binaries`
5. Install Cygwin, AdoptOpenJDK, Maven

# Build using Cygwin64 terminal

6. `export JRE=<path_to_a_jdk>`
7. update PATH so that MSVC link.exe will be used instead of GNU link: `export PATH="/cygdrive/c/Program Files (x86)/Microsoft Visual Studio/2019/BuildTools/VC/Tools/MSVC/14.23.28105/bin/Hostx64/x64:$PATH"`
8. `mvn clean install -P launchers`

# Launch JNLP application

9. `./launchers/target/bin/javaws.exe <path_or_url_to_jnlp_file>`
