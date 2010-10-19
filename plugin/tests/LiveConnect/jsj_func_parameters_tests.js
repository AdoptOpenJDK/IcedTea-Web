 /********************************************************************
  * Tests for function parameter coversion when calling Java from JS *
  ********************************************************************/

function fpCallTests() {

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java Call tests [Parameter type]:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Parameter type";
    columnNames[1] = "Sending";
    columnNames[2] = "Expected reply";
    columnNames[3] = "Reply";
    columnNames[4] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    PluginTest.setUpForReturnTests();

     try {
         row = document.createElement("tr");
         type = "int";
         send = 1;
         reply = PluginTest.functioniParamTest(send);
         addResult(type, send, send, reply, row);
         check(send, reply, "number", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "double";
         send = 1.1;
         reply = PluginTest.functiondParamTest(send);
         addResult(type, send, send, reply, row);
         check(send, reply, "number", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
    try {
         row = document.createElement("tr");
         type = "float";
         send = 1.11;
         reply = PluginTest.functionfParamTest(send);
         addResult(type, send, send, reply, row);
         check(send, reply, "number", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "long";
         send = 4294967300;
         reply = PluginTest.functionlParamTest(send);
         addResult(type, send, send, reply, row);
         check(send, reply, "number", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);

     try {
         row = document.createElement("tr");
         type = "boolean";
         send = true;
         reply = PluginTest.functionbParamTest(send);
         addResult(type, send, send, reply, row);
         check("true", reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "char";
         send = 75;
         reply = PluginTest.functioncParamTest(send);
         addResult(type, send, "K", reply, row);
         check("K", reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);

     try {
         row = document.createElement("tr");
         type = "byte";
         send = 76;
         reply = PluginTest.functionbyParamTest(send);
         addResult(type, send, send, reply, row);
         check(send, reply, "number", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);       
     
     try {
         row = document.createElement("tr");
         type = "char[] (simple primitive)";
         arr = new Array();
         arr[0] = 80;
         arr[1] = 81;
         reply = PluginTest.functioncaParamTest(arr);
         addResult(type, "[80,81]", "P:Q", reply, row);
         check(reply, "P:Q", "string", row);
     } catch (e) {
         error(type, "P:Q", e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "String";
         send = "$〒£€𝍖𠁎ǣ";
         expectedreply = "$〒£€𝍖𠁎ǣ:java.lang.String";
         reply = PluginTest.functionsParamTest(send);
         addResult(type, send, expectedreply, reply, row);
         check(expectedreply, reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "Integer";
         send = "32";
         expectedreply = send+":java.lang.Integer";
         reply = PluginTest.functionIParamTest(send);
         addResult(type, send, expectedreply, reply, row);
         check(expectedreply, reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "Double";
         send = 32.0;
         expectedreply = "32.0:java.lang.Double";
         reply = PluginTest.functionDParamTest(send);
         addResult(type, send, expectedreply, reply, row);
         check(expectedreply, reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);             
     
     try {
         row = document.createElement("tr");
         type = "Float";
         send = 32.01;
         expectedreply = send+":java.lang.Float";
         reply = PluginTest.functionFParamTest(send);
         addResult(type, send, expectedreply, reply, row);
         check(expectedreply, reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);

     try {
         row = document.createElement("tr");
         type = "Long";
         send = 4294967301;
         expectedreply = send+":java.lang.Long";
         reply = PluginTest.functionLParamTest(send);
         addResult(type, send, expectedreply, reply, row);
         check(expectedreply, reply, "string", row);
     } catch (e) {
         error(type, send, e, row);
     }
     tblBody.appendChild(row);   
     
     
     try {
         row = document.createElement("tr");
         type = "String/Int [] (mixed)";
         arr = new Array();
         arr[0] = "s1";
         arr[1] = 42;
         reply = PluginTest.functionsiaParamTest(arr);
         addResult(type, "[s1,42]", "s1:42", reply, row);
         check(reply, "s1:42", "string", row);
     } catch (e) {
         error(type, "s1:42", e, row);
     }
     tblBody.appendChild(row);
     
     try {
         row = document.createElement("tr");
         type = "DummyObject[] (complex)";
         arr = new Array();
         arr[0] = new PluginTest.Packages.DummyObject("DummyObject1");
         arr[1] = new PluginTest.Packages.DummyObject("DummyObject2");
         reply = PluginTest.functioncomplexaParamTest(arr);
         addResult(type, "[DummyObject1,DummyObjec2]", "DummyObject1:DummyObject2", reply, row);
         check(reply, "DummyObject1:DummyObject2", "string", row);
     } catch (e) {
         error(type, "DummyObject1:DummyObject2", e, row);
     }

     tblBody.appendChild(row);
}

