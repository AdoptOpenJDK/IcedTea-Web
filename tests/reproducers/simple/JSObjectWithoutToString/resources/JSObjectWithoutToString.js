function doToStringTest(){
    var applet = document.getElementById('jswithouttostring');

    var null_obj = Object.create(null);

    applet.callJSToString(null_obj);
}

