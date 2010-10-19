/******************************************************
 * Tests for parameter conversion between Java and JS *
 ******************************************************/

function JJSParameterTypeCallTest(type_parameter) {
    return type_parameter + ":" + typeof(type_parameter);
}

function runSingleJjsCallParameterTest(type, control_arg, row) {
    try {
        expectedvalue = JJSParameterTypeCallTest(control_arg);
        actualvalue = PluginTest.jjsCallParamTest(type);
        addResult(type, expectedvalue, actualvalue, row);
        check(actualvalue, expectedvalue, "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
}

function jjsCallParameterTests() {

    document.getElementById("results").innerHTML +=  "<h2>Java -> JS Call tests (Parameter Type):</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Parameter Type (Java side)";
    columnNames[1] = "Expecting Java to receive";
    columnNames[2] = "Java Received";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    row = document.createElement("tr");
    runSingleJjsCallParameterTest("int", 1, row);
    tblBody.appendChild(row);

    row = document.createElement("tr");
    runSingleJjsCallParameterTest("double", 1.1, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("float", 1.2000000476837158, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("long", 4294967296, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("short", 2, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("byte", 3, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("char", 8995, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("boolean", true, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Integer", 4, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Double", 4.1, row);
    tblBody.appendChild(row);

    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Float", 4.199999809265137, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Long", 4294967297, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Short", 5, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Byte", 6, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Boolean", false, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.Character", 8996, row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("java.lang.String", "𠁎〒£$ǣ€𝍖", row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJjsCallParameterTest("PluginTest.Packages.DummyObject", (new PluginTest.Packages.DummyObject("d1")), row);
    tblBody.appendChild(row);
}

