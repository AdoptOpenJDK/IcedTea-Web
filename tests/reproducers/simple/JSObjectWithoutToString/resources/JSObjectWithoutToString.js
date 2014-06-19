function attemptToStringTest() {
    var null_obj = Object.create(null);
    applet.callJSToString(null_obj);
}

doTest(attemptToStringTest, applet);
