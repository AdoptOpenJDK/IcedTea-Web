function attemptFuncResolTests() {
    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");
    var func = testParams[0];
    var value = decodeURIComponent(testParams[1]);

    eval('applet.' + func + '(' + value + ')');
    applet.writeAfterTests();
}

doTest(attemptFuncResolTests, applet);