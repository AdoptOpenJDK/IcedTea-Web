function doJToJSSetTests(){

    var applet = document.getElementById('jtojsSetApplet');

    var urlArgs = document.URL.split("?");
    var func = urlArgs[1];

    //pre-initialization of arrays
    if(func === "jjsSet1DArray"){
        setvar = new Array();
    }else if(func === "jjsSet2DArray" ){
        setvar = new Array();
        setvar[1] = new Array();
    }

    //calling the applet function
    eval('applet.'+func+'()');

    //preparing jsvar value string for output
    if(func === "jjsSet1DArray"){
        str = ""+setvar[1];
    }else if(func === "jjsSet2DArray" ){
        str = ""+setvar[1][1];
    }else if(func === "jjsSetObject" ){
        str = setvar.toString();
    }else{
        var str = ""+setvar;  
    }

    applet.printStrAndFinish(str);
}
