 /**************************************************************
  * Tests for overloaded function resolution when calling Java *
   * functions from JS                                         *
  **************************************************************/

function frCallTests() {

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java Call tests [Overload and casting]:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Available functions";
    columnNames[1] = "Expected reply";
    columnNames[2] = "Reply";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    try {
        row = document.createElement("tr");
        fname = "foo_null_to_nonprim";
        available = fname + " [(Integer), (int)]";
        expectedreply = fname + ":Integer";
        reply = PluginTest.foo_null_to_nonprim(null);
        addResult(available, expectedreply, reply, row);
        check(fname + ":Integer", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_jso_to_jso";
        available = fname + " [(JSObject), (String), (String[]), (Object)]";
        expectedreply = fname + ":JSObject";
        reply = PluginTest.foo_jso_to_jso(window);
        addResult(available, expectedreply, reply, row);
        check(fname + ":JSObject", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_ct_to_ct";
        available = fname + " [(OverloadTestHelper1), (OverloadTestHelper2), (OverloadTestHelper3)]";
        expectedreply = fname + ":OverloadTestHelper2";
        reply = PluginTest.foo_ct_to_ct(new PluginTest.Packages.OverloadTestHelper2());
        addResult(available, expectedreply, reply, row);
        check(fname + ":OverloadTestHelper2", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_multiprim";
        available = fname + " [(double), (String)]";
        expectedreply = fname + ":double";
        reply = PluginTest.foo_multiprim(1.1);
        addResult(available, expectedreply, reply, row);
        check(fname + ":double", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_multiprim";
        available = fname + " [(double), (String)]";
        expectedreply = fname + ":double";
        reply = PluginTest.foo_multiprim(1.1);
        addResult(available, expectedreply, reply, row);
        check(fname + ":double", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_strnum";
        available = fname + " [(double), (OverloadTestHelper1)]";
        expectedreply = fname + ":double";
        reply = PluginTest.foo_strnum(1.1);
        addResult(available, expectedreply, reply, row);
        check(fname + ":double", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_ct_to_sc";
        available = fname + " [(OverloadTestHelper1), (String)]";
        expectedreply = fname + ":double";
        reply = PluginTest.foo_ct_to_sc(new PluginTest.Packages.OverloadTestHelper2());
        addResult(available, expectedreply, reply, row);
        check(fname + ":OverloadTestHelper1", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_jv_to_str";
        available = fname + " [(String), (JSObject)]";
        expectedreply = fname + ":String";
        reply = PluginTest.foo_jv_to_str(new PluginTest.Packages.OverloadTestHelper1());
        addResult(available, expectedreply, reply, row);
        check(fname + ":java.lang.String", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        fname = "foo_jso_to_array";
        available = fname + " [(int[]), (Integer), (Integer[])]";
        expectedreply = fname + ":int[]";
        arr = new Array();
        arr[0] = 10;
        reply = PluginTest.foo_jso_to_array(arr);
        addResult(available, expectedreply, reply, row);
        check(fname + ":int[]", reply, "string", row);
    } catch (e) {
        error(null, null, e, row);
    }
    tblBody.appendChild(row);
    
    // Tests where exceptions are expected
    fname = "foo_null_to_prim";
    available = fname + " [(int)] -- Not allowed";
    
    try {
        row = document.createElement("tr");
        expectedreply = null;
        reply = PluginTest.foo_null_to_prim(null);
        fail(row, "An exception was expected. Instead, got reply: " + reply);
    } catch (e) {
        addResult(available, "[An exception]", e.toString(), row);
        pass(row);
    }
    tblBody.appendChild(row);
    fname = "foo_jso_to_somethingelse";
    available = fname + " [(OverloadTestHelper1)] -- Not allowed";
    
    try {
        row = document.createElement("tr");
        expectedreply = null;
        reply = PluginTest.foo_jso_to_somethingelse(window);
        fail(row, "An exception was expected. Instead, got reply: " + reply);
    } catch (e) {
        addResult(available, "[An exception]", e.toString(), row);
        pass(row);
    }
    tblBody.appendChild(row);
    fname = "foo_unsupported";
    available = fname + " [(Object[] p)] -- Not allowed";
    try {
      row = document.createElement("tr");
      expectedreply = null;
      reply = PluginTest.foo_unsupported(25);
       fail(row, "An exception was expected. Instead, got reply: " + reply);
    } catch (e) {
       addResult(available, "[An exception]", e.toString(), row);
       pass(row);
    }

    tblBody.appendChild(row);
}

