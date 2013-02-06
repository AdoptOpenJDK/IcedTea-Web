function test_get_int(){
    var appletName = 'jstojGetApplet';
    try{
        var i = document.getElementById(appletName).i;    
        check(i, 42, "number", " 1 - (int)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_double()
{
    var appletName = 'jstojGetApplet';
    try{
        var d = document.getElementById(appletName).d;
        check(d, 42.42, "number", " 2 - (double)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_float(){
    var appletName = 'jstojGetApplet';
    try{
        var f = document.getElementById(appletName).f;
        check(f, 42.099998474121094, "number", " 3 - (float)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_long(){
    var appletName = 'jstojGetApplet';
    try{
        var l = document.getElementById(appletName).l;
        check(l, 4294967296, "number", " 4 - (long)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }    
}

function test_get_boolean(){
    var appletName = 'jstojGetApplet';
    try{
        var b = document.getElementById(appletName).b;
        check(b, true, "boolean", " 5 - (boolean)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_char(){
    var appletName = 'jstojGetApplet';
    try{
        var c = document.getElementById(appletName).c;
        check(c, 8995, "number", " 6 - (char)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_byte(){
    var appletName = 'jstojGetApplet';
    try{    
        var by = document.getElementById(appletName).by;
        check(by, 43, "number", " 7 - (byte)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_intArrayElement(){
    var appletName = 'jstojGetApplet';
    try{
        var ia = document.getElementById(appletName).ia[4];
        check(ia, 1024, "number", " 8 - (int[] - element access)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_intArrayBeyond(){
    var appletName = 'jstojGetApplet';
    try{
        var ia2 = document.getElementById(appletName).ia[30];
        check(ia2, null, "undefined", " 9 - (int[] - beyond length)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_regularString(){
    var appletName = 'jstojGetApplet';
    try{
        var rs = document.getElementById(appletName).rs;    
        check(rs, "I'm a string!", "string", "10 - (regular string)", appletName);    
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_specialCharsString(){
    var appletName = 'jstojGetApplet';
    try{
        var ss = document.getElementById(appletName).ss;
        check(ss, "𠁎〒£$ǣ€𝍖", "string", "11 - (string with special characters)",appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_null(){
    var appletName = 'jstojGetApplet';
    try{
        var n = document.getElementById(appletName).n;
        check(n, null, "object","12 - (null)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Integer(){
    var appletName = 'jstojGetApplet';
    try{
        var I = document.getElementById(appletName).I;
        check(I, 24, "object","13 - (Integer)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Double(){
    var appletName = 'jstojGetApplet';
    try{
        var D = document.getElementById(appletName).D;
        check(D, 24.24, "object", "14 - (Double)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Float(){
    var appletName = 'jstojGetApplet';
    try{
        var F = document.getElementById(appletName).F;
        check(F, 24.124, "object", "15 - (Float)", appletName);    
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Long(){
    var appletName = 'jstojGetApplet';
    try{
        var L = document.getElementById(appletName).L;
        check(L, 6927694924, "object", "16 - (Long)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Boolean(){
    var appletName = 'jstojGetApplet';
    try{
        var B = document.getElementById(appletName).B;
        check(B, false, "object", "17 - (Boolean)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Character(){
    var appletName = 'jstojGetApplet';
    try{
        var C = document.getElementById(appletName).C;
        check(C, 'ᔦ',  "object", "18 - (Character)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_Byte(){
    var appletName = 'jstojGetApplet';
    try{
        var By = document.getElementById(appletName).By;
        check(By, 34,  "object", "19 - (Byte)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_DoubleArrayElement(){
    var appletName = 'jstojGetApplet';
    try{
        var DaE = document.getElementById(appletName).Da1[9];
        check(DaE, 24.24,  "object", "20 - (Double[] - element access)", appletName);
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_DoubleFullArray(){
    var appletName = 'jstojGetApplet';
    try{
        var DaStr = document.getElementById(appletName).Da1.toString().substr(0,20);
        var Da = document.getElementById(appletName).Da1;
    
        var appletid = appletName;
        var testid = "21 - (Double[] - full array)";
        
        var expected = "[Ljava.lang.Double;@";
        var expectedtype = "object";
    
        if ( DaStr ==  expected ) { //the same value
            if ( typeof(Da) == expectedtype ) { //the same type
                passTest( testid, appletid );
            } else {
                failTypeTest( testid, appletid, typeof(Da), expectedtype );
            }
        } else {
            failValTest( testid, appletid, DaStr, expected );                
        }
    }catch(e){
        appletStdOut( appletName, e );    
        appendMessageDiv(e);
    }
}

function test_get_JSObject(){
    var appletName = 'jstojGetApplet';
    try{
        var javao = new Object(document.getElementById(appletName).jso);
        check(javao.key1, "value1",  "string", "22 - (JSObject)", appletName);
    }catch(e){
        appletStdOut( appletName, e );
        appendMessageDiv(e);
    }

}
    

