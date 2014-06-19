function attemptTypeConvTests() {

    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");

    var field = testParams[0];
    var value = decodeURIComponent(testParams[1]);

    eval('applet.' + field + '=' + value);
    applet.printNewValueAndFinish(field);

}

doTest(attemptTypeConvTests, applet);
