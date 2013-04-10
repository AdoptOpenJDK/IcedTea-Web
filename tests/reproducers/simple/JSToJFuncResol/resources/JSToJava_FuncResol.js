function doFuncResolTests(){

    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");
    var applet = document.getElementById('jstojFuncResolApplet');
    var func = testParams[0];
    var value = decodeURIComponent(testParams[1]);

    eval('applet.' + func + '(' + value + ')');
    applet.writeAfterTests();
}
