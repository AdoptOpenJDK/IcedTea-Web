function testJSObjectFromEval() {
	var applet = document.getElementById("applet");	
	var obj;
	
	applet.output("*** Test JSObject from JS ***");

	applet.output("JS create");
	obj = new Object();
	applet.output("Java set");
	applet.setJSMember(obj, "test", 0);
	applet.output("obj.test = " + obj.test);

	applet.output("*** Test JSObject from Java ***");

	applet.output("Java create");
	obj = applet.newJSObject();
	applet.output("Java set");
	applet.setJSMember(obj, "test", 0);	
	applet.output("obj.test = " + obj.test);

	applet.output("*** APPLET FINISHED ***"); //We're done here
}