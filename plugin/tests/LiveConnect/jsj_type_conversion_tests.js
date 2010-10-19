/************************************************************
 * Tests for data type conversion from JS to Java variables *
 ************************************************************/

function typeCastingTests() {

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java type casting tests:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Test Type";
    columnNames[1] = "Send Value";
    columnNames[2] = "Expected Value";
    columnNames[3] = "Actual Value";
    columnNames[4] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    try {
        row = document.createElement("tr");
        type = "Numeric -> java.lang.String (Integer)";
        setto = 1;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, setto, now, row);
        check(now, setto, "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Numeric -> java.lang.String (Double)";
        setto = 1.1;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, setto, now, row);
        check(now, setto, "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Numeric -> java.lang.Object (Integer)";
        setto = 1.0;
        PluginTest.Object_type = setto;
        now = PluginTest.Object_type + " | Superclass = " + PluginTest.Object_type.getClass().getSuperclass().getName();
        addResult (type, setto, setto + " | Superclass = java.lang.Number", now, row);
        check(now, setto + " | Superclass = java.lang.Number", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Numeric -> java.lang.Object (Double)";
        setto = 1.1;
        PluginTest.Object_type = setto;
        now = PluginTest.Object_type + " | Superclass = " + PluginTest.Object_type.getClass().getSuperclass().getName();
        addResult (type, setto, setto + " | Superclass = java.lang.Number", now, row);
        check(now, setto + " | Superclass = java.lang.Number", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
   
    try {
        row = document.createElement("tr");
        type = "Numeric -> boolean (0)";
        setto = 0;
        PluginTest.boolean_type = setto;
        now = PluginTest.boolean_type;
        addResult (type, setto, false, now, row);
        check(now, false, "boolean", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Numeric -> boolean (1.1)";
        setto = 1.1;
        PluginTest.boolean_type = setto;
        now = PluginTest.boolean_type;
        addResult (type, setto, true, now, row);
        check(now, true, "boolean", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> java.lang.Boolean (true)";
        setto = true;
        PluginTest.Boolean_type = setto;
        now = PluginTest.Boolean_type;
        addResult (type, setto, "true", now, row);
        check(now, "true", "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> java.lang.Boolean (false)";
        setto = false;
        PluginTest.Boolean_type = setto;
        now = PluginTest.Boolean_type;
        addResult (type, setto, "false", now, row);
        check(now, "false", "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Boolean -> java.lang.Object";
        setto = true;
        PluginTest.Boolean_type = setto;
        now = PluginTest.Boolean_type + " | Class = " + PluginTest.Boolean_type.getClass().getName();
        addResult (type, setto, "true | Class = java.lang.Boolean", now, row);
        check(now, "true | Class = java.lang.Boolean", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> java.lang.String";
        setto = true;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, "true", now, row);
        check(now, "true", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);


    try {
        row = document.createElement("tr");
        type = "Boolean -> java.lang.String";
        setto = true;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, "true", now, row);
        check(now, "true", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> byte (true)";
        setto = true;
        PluginTest.byte_type = setto;
        now = PluginTest.byte_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> char (true)";
        setto = true;
        PluginTest.char_type = setto;
        now = PluginTest.char_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> short (true)";
        setto = true;
        PluginTest.short_type = setto;
        now = PluginTest.short_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> int (true)";
        setto = true;
        PluginTest.int_type = setto;
        now = PluginTest.int_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Boolean -> long (true)";
        setto = true;
        PluginTest.long_type = setto;
        now = PluginTest.long_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> float (true)";
        setto = true;
        PluginTest.float_type = setto;
        now = PluginTest.float_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Boolean -> double (true)";
        setto = true;
        PluginTest.double_type = setto;
        now = PluginTest.double_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> byte (false)";
        setto = false;
        PluginTest.byte_type = setto;
        now = PluginTest.byte_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> char (false)";
        setto = false;
        PluginTest.char_type = setto;
        now = PluginTest.char_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> short (false)";
        setto = false;
        PluginTest.short_type = setto;
        now = PluginTest.short_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> int (false)";
        setto = false;
        PluginTest.int_type = setto;
        now = PluginTest.int_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Boolean -> long (false)";
        setto = false;
        PluginTest.long_type = setto;
        now = PluginTest.long_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean -> float (false)";
        setto = false;
        PluginTest.float_type = setto;
        now = PluginTest.float_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Boolean -> double (false)";
        setto = false;
        PluginTest.double_type = setto;
        now = PluginTest.double_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "String -> Object";
        setto = "𠁎〒£$ǣ€𝍖";
        PluginTest.Object_type = setto;
        
        // Some weird FF bug is causing getClass to not work correctly when set 
        // to a String (hasProperty/hasMethod "getClass" doesn't come through 
        // to the plugin at all, so it is definitely an ff issue). So for now, 
        // we just compare values.

        //now = PluginTest.Object_type + " | Class = " + PluginTest.Object_type.getClass().getSuperclass().getName();
        //addResult (type, setto, setto + " | Class = java.lang.String", now, row);
        //check(now, setto + " | Class = java.lang.String", "string", row);
        
        now = PluginTest.Object_type;
        PluginTest.Object_type.charAt(3); // try a String specific function to be sure it is a String
        addResult (type, setto, setto, now, row);
        check(now, setto, "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> byte";
        setto = "1";
        PluginTest.byte_type = setto;
        now = PluginTest.byte_type;
        addResult (type, setto, 1, now, row);
        check(now, 1, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> short";
        setto = "2";
        PluginTest.short_type = setto;
        now = PluginTest.short_type;
        addResult (type, setto, 2, now, row);
        check(now, 2, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> int";
        setto = "3";
        PluginTest.int_type = setto;
        now = PluginTest.int_type;
        addResult (type, setto, 3, now, row);
        check(now, 3, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> long";
        setto = "4";
        PluginTest.long_type = setto;
        now = PluginTest.long_type;
        addResult (type, setto, 4, now, row);
        check(now, 4, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> float";
        setto = "0.0";
        PluginTest.float_type = setto;
        now = PluginTest.float_type;
        addResult (type, setto, 0, now, row);
        check(now, 0, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "String -> double";
        setto = "6.2";
        PluginTest.double_type = setto;
        now = PluginTest.double_type;
        addResult (type, setto, 6.2, now, row);
        check(now, 6.2, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "String -> char";
        setto = "7";
        PluginTest.char_type = setto;
        now = PluginTest.char_type;
        addResult (type, setto, 7, now, row);
        check(now, 7, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    

    try {
        row = document.createElement("tr");
        type = "String -> boolean (empty/false)";
        setto = "";
        PluginTest.boolean_type = setto;
        now = PluginTest.boolean_type;
        addResult (type, setto, false, now, row);
        check(now, false, "boolean", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    

    try {
        row = document.createElement("tr");
        type = "String -> boolean (non-empty/true)";
        setto = "A non-empty string";
        PluginTest.boolean_type = setto;
        now = PluginTest.boolean_type;
        addResult (type, setto, true, now, row);
        check(now, true, "boolean", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> byte[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.byte_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.byte_array);
        addResult (type, setto, "1,0,2", now, row);
        check(now, "1,0,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> char[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.char_array = setto;

        // For char array, don't convert to string.. the empty/null/0 character messes it up
        now = PluginTest.char_array[0] + "," + PluginTest.char_array[1] + "," + PluginTest.char_array[2];
        addResult (type, setto, "1,0,2", now, row);
        check(now, "1,0,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> short[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.short_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.short_array);
        addResult (type, setto, "1,0,2", now, row);
        check(now, "1,0,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> int[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.int_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.int_array);
        addResult (type, setto, "1,0,2", now, row);
        check(now, "1,0,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> long[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.long_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.long_array);
        addResult (type, setto, "1,0,2", now, row);
        check(now, "1,0,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> float[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.float_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.float_array);
        addResult (type, setto, "1.0,0.0,2.0", now, row);
        check(now, "1.0,0.0,2.0", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> double[]";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.double_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.double_array);
        addResult (type, setto, "1.0,0.0,2.0", now, row);
        check(now, "1.0,0.0,2.0", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Array -> String[] (int)";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.String_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.String_array);
        addResult (type, setto, "1,null,2", now, row);
        check(now, "1,null,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Array -> String[] (int)";
        setto = new Array();
        setto[0] = 1;
        setto[2] = 2;
        PluginTest.String_array = setto;
        now = PluginTest.getArrayAsStr(PluginTest.String_array);
        addResult (type, setto, "1,null,2", now, row);
        check(now, "1,null,2", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
    
        var a = [];
        a[0] = [];
        a[1] = [];
        a[2] = [];
        a[0][0] = "100";
        a[0][2] = "102";
        a[2][0] = "120";
        a[2][1] = "121";
        a[2][3] = "123";
        
        //
        //    a = [[00, , 02]      // normal
        //         []              // empty
        //         [20, 21, , 23]] // length = element0.length + 1
        //

        row = document.createElement("tr");
        type = "Array -> char[][] (string to primitive)";
        PluginTest.char_array_array = a;
        now = PluginTest.char_array_array[0][0] + "," + 
                PluginTest.char_array_array[0][1] + "," + 
                PluginTest.char_array_array[0][2] + "," + 
              PluginTest.char_array_array[1][0] + "," + 
              PluginTest.char_array_array[2][0] + "," + 
                PluginTest.char_array_array[2][1] + "," + 
                PluginTest.char_array_array[2][2] + "," + 
                PluginTest.char_array_array[2][3];
        expected = "100,0,102,undefined,120,121,0,123"
        addResult (type, a, expected, now, row);
        check(now, expected, "string", row);
    } catch (e) {
        error(type, a, e, row);
    }
    tblBody.appendChild(row);

    try {
        var a = [];
        a[0] = [];
        a[1] = [];
        a[2] = [];
        a[0][0] = 100;
        a[0][2] = 102;
        a[2][0] = 120;
        a[2][1] = 121;
        a[2][3] = 123;
        
        //
        //    a = [[00, , 02]     // normal
        //         []          // empty
        //         [20, 21, , 23]] // length = element0.length + 1
        //

        row = document.createElement("tr");
        type = "Array -> String[][] (int to complex)";
        PluginTest.String_array_array = a;
        now = PluginTest.String_array_array[0][0] + "," + 
                PluginTest.String_array_array[0][1] + "," + 
                PluginTest.String_array_array[0][2] + "," + 
              PluginTest.String_array_array[1][0] + "," + 
              PluginTest.String_array_array[2][0] + "," + 
                PluginTest.String_array_array[2][1] + "," + 
                PluginTest.String_array_array[2][2] + "," + 
                PluginTest.String_array_array[2][3];
        expected = "100,null,102,undefined,120,121,null,123";
        addResult (type, a, expected, now, row);
        check(now, expected, "string", row);
    } catch (e) {
        error(type, a, e, row);
    }
    tblBody.appendChild(row);

    try {
        var a = [];
        a[0] = [];
        a[1] = [];
        a[2] = [];
        a[0][0] = 100;
        a[0][2] = 102;
        a[2][0] = 120;
        a[2][1] = 121;
        a[2][3] = 123;
        
        //
        //    a = [[00, , 02]      // normal
        //         []              // empty
        //         [20, 21, , 23]] // length = element0.length + 1
        //

        row = document.createElement("tr");
        type = "Array -> String";
        PluginTest.String_type = a;
        now = PluginTest.String_type;
        expected = "100,,102,,120,121,,123";
        addResult (type, a, expected, now, row);
        check(now, expected, "string", row);
    } catch (e) {
        error(type, a, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "JSObject -> JSObject";
        setto = window;
        PluginTest.JSObject_type = setto;
        now = PluginTest.JSObject_type;
        addResult (type, setto, "[object Window]", now, row);
        check(now, "[object Window]", "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "JSObject -> String";
        setto = window;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, "[object Window]", now, row);
        check(now, "[object Window]", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
        try {
        row = document.createElement("tr");
        type = "Java Object -> Java Object";
        PluginTest.Float_type = 1.111;
        orig_hash = PluginTest.Float_type.hashCode();
        PluginTest.Object_type = PluginTest.Float_type;
        new_hash = PluginTest.Object_type.hashCode();
        addResult (type, "hashcode=" + orig_hash, orig_hash, new_hash, row);
        check(new_hash, orig_hash, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Java Object -> String";
        setto = new PluginTest.Packages.DummyObject("Test object");
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, "Test object", now, row);
        check(now, "Test object", "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "null -> Java Object (String)";
        
        // Assuming the set tests have passed, we know that object is non-null after this
        PluginTest.String_type = "Not Null"; 

        setto = null;
        PluginTest.String_type = setto;
        now = PluginTest.String_type;
        addResult (type, setto, null, now, row);
        check(now, null, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

/*
    // NULL -> primitive tests are disabled for now due to ambiguity.
    // Section 2.2 here: http://java.sun.com/javase/6/webnotes/6u10/plugin2/liveconnect/
    // States that null to primitive is not allowed, yet, section 2.3.7 claims it is..

    try {
        row = document.createElement("tr");
        type = "null -> byte";
        
        // Assuming the set tests have passed, we know that object is non-null after this
        PluginTest.byte_type = "100"; 

        setto = null;
        PluginTest.byte_type = setto;
        now = PluginTest.byte_type;
        addResult (type, setto, null, now, row);
        check(now, null, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
*/

}



