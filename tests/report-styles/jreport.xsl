<?xml version="1.0"?>
<!--

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.

 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
<!--
when parameter is mentioned (no matter of value) eg:
<xsl:param name="logs">none</xsl:param>
then xsltproc is not able to change its value since 2008
This parameter is providing relative path to file with logs which is then linked from this index
Bad luck that xsltproc is not able to use default values.
If there is no need for linking, please use value "none" for this variable
-->
<html>
 <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    </meta>
    <script src="report-styles/index.js">
    </script>
    <link href="report-styles/report.css" rel="stylesheet" type="text/css">
    </link>
  </head>
<body onload='setClassDisplay("trace","none");'>
    <div id="wholePage">
    <h3>Date:</h3>
    <xsl:value-of select="/testsuite/date"/>
    <br/>
    <h2>Result: (<xsl:value-of select="round(sum(/testsuite/testcase/@time))"/>s)</h2>
    <h4>In brackets are KnownToFail values if any</h4>
    <div class="tablee">
      <div class="row">
        <div class="cell1">TOTAL: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/total"/>
          <xsl:choose>
           <xsl:when test="/testsuite/stats/summary/total/@known-to-fail!=0">
             (<xsl:value-of select="/testsuite/stats/summary/total/@known-to-fail"/>)
           </xsl:when>
         </xsl:choose>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row passed">
        <div class="cell1">passed: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/passed"/>
          <xsl:choose>
           <xsl:when test="/testsuite/stats/summary/passed/@known-to-fail!=0">
             (<xsl:value-of select="/testsuite/stats/summary/passed/@known-to-fail"/>)
           </xsl:when>
         </xsl:choose>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row failed">
        <div class="cell1">failed: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/failed"/>
          <xsl:choose>
           <xsl:when test="/testsuite/stats/summary/failed/@known-to-fail!=0">
             (<xsl:value-of select="/testsuite/stats/summary/failed/@known-to-fail"/>)
           </xsl:when>
         </xsl:choose>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row ignored">
        <div class="cell1">ignored: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/ignored"/>
          <xsl:choose>
           <xsl:when test="/testsuite/stats/summary/ignored/@known-to-fail!=0">
             (<xsl:value-of select="/testsuite/stats/summary/ignored/@known-to-fail"/>)
           </xsl:when>
         </xsl:choose>
        </div>
        <div class="space-line"></div>
      </div>
    </div>
    <h2>Classes: <button onclick="negateIdDisplay('ccllaasseess')">show/hide</button></h2>
<div id='ccllaasseess' style="display:block">
    <xsl:for-each select="/testsuite/stats/classes/class">
      <div>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="passed = total">
		passed
	      </xsl:when>
            <xsl:otherwise>
	        failed
	    </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <a class="classSumaryName"><xsl:attribute name="href">
    #<xsl:value-of select="@name"/>
  </xsl:attribute><xsl:value-of select="@name"/>
(<xsl:value-of select="@time"/>ms):
</a>
    <xsl:for-each select="bugs/bug">
      <a>
        <xsl:attribute name="href"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
        <xsl:value-of select="@visibleName"/>
      </a>;
    </xsl:for-each>
      </div>
      <blockquote>
        <div class="tablee">
          <div class="row">
            <div class="cell1">TOTAL: </div>
            <div class="cell2">
              <xsl:value-of select="total"/>
              <xsl:choose>
               <xsl:when test="total/@known-to-fail!=0">
                 (<xsl:value-of select="total/@known-to-fail"/>)
               </xsl:when>
             </xsl:choose>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row passed">
            <div class="cell1">passed: </div>
            <div class="cell2">
              <xsl:value-of select="passed"/>
              <xsl:choose>
               <xsl:when test="passed/@known-to-fail!=0">
                 (<xsl:value-of select="passed/@known-to-fail"/>)
               </xsl:when>
             </xsl:choose>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row failed">
            <div class="cell1">failed: </div>
            <div class="cell2">
              <xsl:value-of select="failed"/>
              <xsl:choose>
               <xsl:when test="failed/@known-to-fail!=0">
                 (<xsl:value-of select="failed/@known-to-fail"/>)
               </xsl:when>
             </xsl:choose>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row ignored">
            <div class="cell1">ignored: </div>
            <div class="cell2">
              <xsl:value-of select="ignored"/>
              <xsl:choose>
               <xsl:when test="ignored/@known-to-fail!=0">
                 (<xsl:value-of select="ignored/@known-to-fail"/>)
               </xsl:when>
             </xsl:choose>
            </div>
            <div class="space-line"></div>
          </div>
        </div>
      </blockquote>
      <hr/>
    </xsl:for-each>
</div>
   
    <h2>Individual results:</h2>
    <button onclick="setClassDisplay('trace','none')">NoneTrace</button>
    <button onclick="setClassDisplay('trace','block')">AllTraces</button>
    <xsl:for-each select="/testsuite/testcase">
      <div>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="@ignored">
           ignored
            </xsl:when>
            <xsl:when test="error">
           failed
            </xsl:when>
            <xsl:otherwise>
           passed 
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <a>
          <xsl:attribute name="name">
            <xsl:value-of select="normalize-space(@classname)"/>
          </xsl:attribute>
        </a>
        <div class="lineHeader">
          <div class="clazz">
            <xsl:choose>
              <xsl:when test="$logs!='none'">
                <a class="logLink" target="new">
                  <xsl:attribute name="href">
                    <xsl:value-of select="$logs"/>#<xsl:value-of select="@classname"/>.<xsl:value-of select="@name"/>
                  </xsl:attribute>
                  <xsl:value-of select="@classname"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@classname"/>
              </xsl:otherwise>
             </xsl:choose>
           </div>
          <xsl:text disable-output-escaping="no"> - </xsl:text>
          <div class="method">
            <xsl:value-of select="@name"/>
            <xsl:for-each select="bugs/bug">
            <xsl:text disable-output-escaping="no"> - </xsl:text>
              <a>
                <xsl:attribute name="href"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
                <xsl:value-of select="@visibleName"/>
              </a>
            </xsl:for-each>
          </div>
        </div>
        <div class="result">
          <xsl:choose>
            <xsl:when test="not(error)">
              <div class="status">
         <xsl:choose>
           <xsl:when test="@ignored">
             IGNORED (<xsl:value-of select="@time"/>s) 
           </xsl:when>
           <xsl:otherwise>
             PASSED (<xsl:value-of select="@time"/>s) 
           </xsl:otherwise>
         </xsl:choose>
         <xsl:choose>
           <xsl:when test="@known-to-fail">
             <xsl:choose>
               <xsl:when test="@known-to-fail=true">
                 <xsl:text>" - WARNING This test is known to fail, but have passed!</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:text> - This test is known to fail</xsl:text>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:when>
         </xsl:choose>
         </div>
            </xsl:when>
            <xsl:otherwise>
              <div class="status">
        FAILED (<xsl:value-of select="@time"/>s) 
         <xsl:choose>
           <xsl:when test="@known-to-fail">
             <xsl:text> - This test is known to fail</xsl:text>
           </xsl:when>
         </xsl:choose>
         </div>
              <div class="wtrace">
                <div class="theader">
                  <xsl:value-of select="error/@type"/>  <xsl:text disable-output-escaping="no"> - </xsl:text>
                  <xsl:value-of select="error/@message"/>  
                  <button onclick="negateIdDisplay('{generate-id(error)}')">StackTrace</button>
                </div>
                <div class="trace" id="{generate-id(error)}">
                  <pre>
                    <xsl:value-of select="error"/>
                  </pre>
                </div>
              </div>
            </xsl:otherwise>
          </xsl:choose>
          <div class="space-line"></div>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="space-line"></div>
    </xsl:for-each>

          <div class="stbound">
            <div class="theader stExt2">
            STD-OUT - <button onclick="negateIdDisplay('{generate-id(/testsuite/system-out)}')">Show/hide</button>
            </div>
            <div class="trace stExt3" id="{generate-id(/testsuite/system-out)}">
              <pre>
                <xsl:value-of select="/testsuite/system-out"/>
              </pre>
            </div>
          </div>
 <div class="space-line"></div>
    <div class="stbound">
            <div class="theader stExt2">
            STD-ERR - <button onclick="negateIdDisplay('{generate-id(/testsuite/system-err)}')">Show/hide</button>
            </div>
            <div class="trace stExt3" id="{generate-id(/testsuite/system-err)}">
              <pre>
                <xsl:value-of select="/testsuite/system-err"/>
              </pre>
            </div>
          </div>
 <div class="space-line"></div>

      </div>
    </body>
   </html>
  </xsl:template>
</xsl:stylesheet>
