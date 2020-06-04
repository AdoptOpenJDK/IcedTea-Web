# IcedTeaWeb release for AdoptOpenJDK

For a release several steps are needed:

- For a 1.x release the news, release-doc and config must be updated. An example can be found here: https://github.com/AdoptOpenJDK/IcedTea-Web/commit/6b2d51873acc745016311712074fde89a629ccb8
- A tag for the version must be created
- The `icedtea-web_build_x86-64_linux` build job in the Adopt CI server must be triggered: https://ci.adoptopenjdk.net/view/IcedTea-Web/
- The `icedtea-web_build_x86-64_portable` build job in the Adopt CI server must be triggered: https://ci.adoptopenjdk.net/view/IcedTea-Web/
- The `icedtea-web_build_x86-64_windows` build job in the Adopt CI server must be triggered: https://ci.adoptopenjdk.net/view/IcedTea-Web/ This build will automatically trigger the `icedtea-web_sign_x86-64_windows` build if it finished successfully
- All artifacts of the following jenkins build must be downloaded: `icedtea-web_build_x86-64_linux`, `icedtea-web_build_x86-64_portable` and `icedtea-web_sign_x86-64_windows`
- A release in the GitHub repo must be created and all downloaded artifacts must be uploaded to the build. Example: https://github.com/AdoptOpenJDK/IcedTea-Web/releases/tag/icedtea-web-1.8.3
- The AdoptOpenJDK download page for IcedTeaWeb (https://adoptopenjdk.net/icedtea-web.html) must be updated. See https://github.com/AdoptOpenJDK/openjdk-website/blob/master/src/handlebars/icedtea-web.handlebars
- The IcedTeaWeb version in Wikipedia must be updated: https://en.wikipedia.org/wiki/IcedTea

Once all steps above are done a new release of IcedTeaWeb has been successfully created. To bundle the release in future versions of AdoptOpenJDK some additional steps are needed.
