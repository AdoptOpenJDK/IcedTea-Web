/*
JSToJ_auxiliary.js
This file contains auxiliary JavaScript functions for LiveConnect tests output
it is used by JSToJGet reproducer.
*/

function check(actual, expected, expectedtype, testid, appletName ) {
    if (actual == expected) { //the same value
        if (typeof(actual) == expectedtype) { //the same type
            passTest( testid, appletName );
        } else {
            failTypeTest( testid, appletName, actual, expectedtype );
        }
    } else {
        failValTest( testid, appletName, actual, expected );                    
    }
}

function passTest( testid, appletName ){
    var passStr = "Test no."+testid+" - passed.";
    //applet stdout
    appletStdOut( appletName, passStr);
    //html page
    appendMessageDiv(passStr);
}

function failValTest( testid, appletName, actual, expected ){
    var failValStr = "Test no."+testid+" - failed, value mismatch. expected:["+expected+"] found:["+actual+"].";
    //applet stdout
    appletStdOut( appletName, failValStr);
    //html page
    appendMessageDiv(failValStr);
}

function failTypeTest( testid, appletName, actual, expectedtype ){
    var failTypeStr = "Test no."+testid+" - failed, type mismatch. expected:["+expectedtype+"] found:["+typeof(actual)+"].";
    //applet stdout
    appletStdOutLn( appletName, failTypeStr);
    //html page
    appendMessageDiv(failTypeStr);
}

function appletStdOut( appletName, str ){
    document.getElementById( appletName ).stdOutWrite( str );
}

function appletStdOutLn( appletName, str ){
    document.getElementById( appletName ).stdOutWriteln( str );
}

function afterTestsMessage( appletName ){
    document.getElementById( appletName ).stdOutWriteln("afterTests");
}

function appendMessageDiv( message ){
    var messageDiv = document.getElementById( 'messageDiv' );
    messageDiv.appendChild( document.createTextNode(message) );
}
