/***********************************************************************
 * Tests to process various return types from Java side function calls *
 ***********************************************************************/

function rtCallTests() {

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java Call tests [Return Type]:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Function return type";
    columnNames[1] = "Expected Value";
    columnNames[2] = "Actual Value";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    PluginTest.setUpForParameterTests();

    try {
        row = document.createElement("tr");
        type = "int";
        expectedvalue = 41;
        addResult(type, expectedvalue, PluginTest.intReturnTest(), row);
        check(PluginTest.intReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "double";
        expectedvalue = 41.41;
        addResult(type, expectedvalue, PluginTest.doubleReturnTest(), row);
        check(PluginTest.doubleReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "float";
        expectedvalue = 41.4109992980957;
        addResult(type, expectedvalue, PluginTest.floatReturnTest(), row);
        check(PluginTest.floatReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "long";
        expectedvalue = 4294967297;
        addResult(type, expectedvalue, PluginTest.longReturnTest(), row);
        check(PluginTest.longReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "boolean";
        expectedvalue = true;
        addResult(type, expectedvalue, PluginTest.booleanReturnTest(), row);
        check(PluginTest.booleanReturnTest(), expectedvalue, "boolean", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "char";
        expectedvalue = 9001;
        addResult(type, expectedvalue, PluginTest.charReturnTest(), row);
        check(PluginTest.charReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "byte";
        expectedvalue = 44;
        addResult(type, expectedvalue, PluginTest.byteReturnTest(), row);
        check(PluginTest.byteReturnTest(), expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "char[] (direct element access)";
        expectedvalue = 9234;
        addResult(type, expectedvalue, PluginTest.charArrayReturnTest()[2], row);
        check(PluginTest.charArrayReturnTest()[2], expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Regular char string";
        expectedvalue = "I'm a string too!";
        addResult(type, expectedvalue, PluginTest.regularStringReturnTest(), row);
        check(PluginTest.regularStringReturnTest(), expectedvalue, "string", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Special char string";
        expectedvalue = "𠁎〒£$ǣ€𝍖";
        addResult(type, expectedvalue, PluginTest.specialStringReturnTest(), row);
        check(PluginTest.specialStringReturnTest(), expectedvalue, "string", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "void";
        expectedvalue = null;
        addResult(type, "undefined", PluginTest.voidReturnTest(), row);
        check(PluginTest.voidReturnTest(), expectedvalue, "undefined", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "null";
        expectedvalue = null;
        addResult(type, expectedvalue, PluginTest.nullReturnTest(), row);
        check(PluginTest.nullReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Integer";
        expectedvalue = 14;
        addResult(type, expectedvalue, PluginTest.IntegerReturnTest(), row);
        check(PluginTest.IntegerReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Double";
        expectedvalue = 14.14;
        addResult(type, expectedvalue, PluginTest.DoubleReturnTest(), row);
        check(PluginTest.DoubleReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Float";
        expectedvalue = 14.114;
        addResult(type, expectedvalue, PluginTest.FloatReturnTest(), row);
        check(PluginTest.FloatReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Long";
        expectedvalue = 6927694925;
        addResult(type, expectedvalue, PluginTest.LongReturnTest(), row);
        check(PluginTest.LongReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean";
        expectedvalue = "false";
        addResult(type, expectedvalue, PluginTest.BooleanReturnTest(), row);
        check(PluginTest.BooleanReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Character";
        expectedvalue = "␗";
        addResult(type, expectedvalue, PluginTest.CharacterReturnTest(), row);
        check(PluginTest.CharacterReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Byte";
        expectedvalue = 46;
        addResult(type, expectedvalue, PluginTest.ByteReturnTest(), row);
        check(PluginTest.ByteReturnTest(), expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Character[] (direct element access)";
        expectedvalue = "⍑";
        addResult(type, expectedvalue, PluginTest.CharacterArrayReturnTest()[1], row);
        check(PluginTest.CharacterArrayReturnTest()[1], expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Character[] (Full array)";
        expectedvalue = "[Ljava.lang.Character;@";
        addResult(type, expectedvalue+"*", PluginTest.CharacterArrayReturnTest(), row);
        if (PluginTest.CharacterArrayReturnTest().toString().substr(0,23)  == "[Ljava.lang.Character;@")
            if (typeof(PluginTest.CharacterArrayReturnTest()) == "object") {
                pass(row);
            } else {
                fail(row, "Type mismatch: " + typeof(SMPluginTest.Da) + " != object");
            }
        else
            fail(row, "");                

    } catch (e) {
        error(type, expectedvalue, e, row);
    }

    tblBody.appendChild(row);
}

