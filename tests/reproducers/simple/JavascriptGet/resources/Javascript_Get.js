function doJToJSGetTests(){

    var applet = document.getElementById('jtojsGetApplet');

    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");
    var func = testParams[0];
    var value = decodeURIComponent(testParams[1]);

    eval('jsvar='+value);
    eval('applet.'+func+'()');

    applet.writeAfterTests();
}
