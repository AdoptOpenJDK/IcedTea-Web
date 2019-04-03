function test_get_int() {
    try {
        var i = document.getElementById("applet").i;
        check(i, 42, "number", " 1 - (int)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_double() {
    try {
        var d = document.getElementById("applet").d;
        check(d, 42.42, "number", " 2 - (double)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_float() {
    try {
        var f = document.getElementById("applet").f;
        check(f, 42.099998474121094, "number", " 3 - (float)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_long() {
    try {
        var l = document.getElementById("applet").l;
        check(l, 4294967296, "number", " 4 - (long)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_boolean() {
    try {
        var b = document.getElementById("applet").b;
        check(b, true, "boolean", " 5 - (boolean)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_char() {
    try {
        var c = document.getElementById("applet").c;
        check(c, 8995, "number", " 6 - (char)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_byte() {
    try {
        var by = document.getElementById("applet").by;
        check(by, 43, "number", " 7 - (byte)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_intArrayElement() {
    try {
        var ia = document.getElementById("applet").ia[4];
        check(ia, 1024, "number", " 8 - (int[] - element access)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_intArrayBeyond() {
    try {
        var ia2 = document.getElementById("applet").ia[30];
        check(ia2, null, "undefined", " 9 - (int[] - beyond length)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_regularString() {
    try {
        var rs = document.getElementById("applet").rs;
        check(rs, "I'm a string!", "string", "10 - (regular string)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_specialCharsString() {
    try {
        var ss = document.getElementById("applet").ss;
        check(ss, "𠁎〒£$ǣ€𝍖", "string",
                "11 - (string with special characters)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_null() {
    try {
        var n = document.getElementById("applet").n;
        check(n, null, "object", "12 - (null)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Integer() {
    try {
        var I = document.getElementById("applet").I;
        check(I, 24, "object", "13 - (Integer)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Double() {
    try {
        var D = document.getElementById("applet").D;
        check(D, 24.24, "object", "14 - (Double)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Float() {
    try {
        var F = document.getElementById("applet").F;
        check(F, 24.124, "object", "15 - (Float)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Long() {
    try {
        var L = document.getElementById("applet").L;
        check(L, 6927694924, "object", "16 - (Long)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Boolean() {
    try {
        var B = document.getElementById("applet").B;
        check(B, false, "object", "17 - (Boolean)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Character() {
    try {
        var C = document.getElementById("applet").C;
        check(C, 'ᔦ', "object", "18 - (Character)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_Byte() {
    try {
        var By = document.getElementById("applet").By;
        check(By, 34, "object", "19 - (Byte)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_DoubleArrayElement() {
    try {
        var DaE = document.getElementById("applet").Da1[9];
        check(DaE, 24.24, "object", "20 - (Double[] - element access)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_DoubleFullArray() {
    try {
        var DaStr = applet.Da1.toString().substr(0, 20);
        var Da = applet.Da1;

        var testid = "21 - (Double[] - full array)";

        var expected = "[Ljava.lang.Double;@";
        var expectedtype = "object";

        if (DaStr == expected) { // the same value
            if (typeof (Da) == expectedtype) { // the same type
                passTest(testid, applet);
            } else {
                failTypeTest(testid, applet, typeof (Da), expectedtype);
            }
        } else {
            failValTest(testid, applet, DaStr, expected);
        }
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }
}

function test_get_JSObject() {
    try {
        var javao = new Object(applet.jso);
        check(javao.key1, "value1", "string", "22 - (JSObject)", applet);
    } catch (e) {
        appletStdOut(applet, e);
        appendMessageDiv(e);
    }

}

function attemptGetTests() {
    if (applet.Ready()) {
        var urlArgs = document.URL.split("?");
        var testid = urlArgs[1];

        if (testid != null) {
            applet.setStatusLabel(testid);
            appendMessageDiv(testid + "... ");
        } else {
            applet.setStatusLabel("url without ?");
            appendMessageDiv("no url arguments...");
        }
        switch (testid) {
        case "int":
            test_get_int();
            break;
        case "double":
            test_get_double();
            break;
        case "float":
            test_get_float();
            break;
        case "long":
            test_get_long();
            break;
        case "boolean":
            test_get_boolean();
            break;
        case "char":
            test_get_char();
            break;
        case "byte":
            test_get_byte();
            break;
        case "intArrayElement":
            test_get_intArrayElement();
            break;
        case "intArrayBeyond":
            test_get_intArrayBeyond();
            break;
        case "regularString":
            test_get_regularString();
            break;
        case "specialCharsString":
            test_get_specialCharsString();
            break;
        case "null":
            test_get_null();
            break;
        case "Integer":
            test_get_Integer();
            break;
        case "Double":
            test_get_Double();
            break;
        case "Float":
            test_get_Float();
            break;
        case "Long":
            test_get_Long();
            break;
        case "Boolean":
            test_get_Boolean();
            break;
        case "Character":
            test_get_Character();
            break;
        case "Byte":
            test_get_Byte();
            break;
        case "DoubleArrayElement":
            test_get_DoubleArrayElement();
            break;
        case "DoubleFullArray":
            test_get_DoubleFullArray();
            break;
        case "JSObject":
            test_get_JSObject();
            break;
        default:
            appletStdOutLn('applet',
                    "No argument in URL! Should be e.g. JSToJGet.html?int");
            document
                    .getElementById('applet')
                    .setStatusLabel(
                            "Not a valid argument in URL! Should be e.g. JSToJGet.html?int");
            break;
        }
        afterTestsMessage('applet');
    } else {
        setTimeout(attemptGetTests, 100);
    }
}
doTest(attemptGetTests, applet);
