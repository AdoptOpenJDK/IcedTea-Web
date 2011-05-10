
if(typeof String.prototype.trim !== 'function') {   String.prototype.trim = function() {     return this.replace(/^\s+|\s+$/g, '');    } }


function negateIdDisplay(which){
	var e = document.getElementById(which);
 		if (e.style.display=="block") {
			e.style.display="none"
 		}else{
			 e.style.display="block"
		 }
	 }


 function setClassDisplay(which,what) {
	 var e = document.getElementsByClassName(which);
		 for ( var i = 0; i < e.length; i++ ){
			 e[i].style.display=what
			 }
		 }


 function loadXMLDoc(dname) {
	 if (window.XMLHttpRequest) {
		 xhttp=new XMLHttpRequest();
	 }else{
		 xhttp=new ActiveXObject("Microsoft.XMLHTTP");
 	}
 	xhttp.open("GET",dname,false);
	xhttp.send("");
	return xhttp.responseXML;
 }


 function xslt(sheet,data,dest) {
	 var sheetName=sheet;
	 var xmlName=data;
	 var htmlDest=dest;
	 // code for IE
	 if (window.ActiveXObject) {
		 var XML = new ActiveXObject("MSXML2.FreeThreadedDomDocument");
		 XML.async = "false";
		 XML.load(xmlName);
		 var XSL = new ActiveXObject("MSXML2.FreeThreadedDomDocument");
		 XSL.async = "false";
		 XSL.load(sheetName);
		 var XSLTCompiled = new ActiveXObject("MSXML2.XSLTemplate");
		 //Add the stylesheet information
		 XSLTCompiled.stylesheet = XSL.documentElement;
		 //Create the XSLT processor
		 var msSheet = XSLTCompiled.createProcessor();
		 msSheet.input = XML
		 //Perform the transform
		 msSheet.transform();
		 document.getElementById(htmlDest).innerHTML=msSheet.output;
	 }
	 // code for Mozilla, Firefox, Opera, etc.
	 else if (document.implementation && document.implementation.createDocument){
		 xsl=loadXMLDoc(sheetName);
		 xml=loadXMLDoc(xmlName);
		 xsltProcessor=new XSLTProcessor();
		 xsltProcessor.importStylesheet(xsl);
		 resultDocument = xsltProcessor.transformToFragment(xml,document);
 		document.getElementById(htmlDest).appendChild(resultDocument);
	 }
	 setClassDisplay("trace","none"); //by default allare visible to protect disabled javascript
 }
