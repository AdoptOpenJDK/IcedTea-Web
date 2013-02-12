//dummy javascript class whose instance is passed as JSObject parameter:
function JSCar(mph,color){
    this.mph = mph;
    this.color = color;
}

//the main routine used for all tests:
function doFuncParamTests( ){

    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");
    var applet = document.getElementById('jstojFuncParamApplet');
    var func = testParams[0];
    var value = decodeURIComponent(testParams[1]);

    eval('applet.' + func + '(' + value + ')');
    applet.writeAfterTest();
}
