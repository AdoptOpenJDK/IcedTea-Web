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
    <div class="tablee">
      <div class="row">
        <div class="cell1">TOTAL: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/total"/>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row passed">
        <div class="cell1">passed: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/passed"/>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row failed">
        <div class="cell1">failed: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/failed"/>
        </div>
        <div class="space-line"></div>
      </div>
      <div class="row ignored">
        <div class="cell1">ignored: </div>
        <div class="cell2">
          <xsl:value-of select="/testsuite/stats/summary/ignored"/>
        </div>
        <div class="space-line"></div>
      </div>
    </div>
    <h2>Classes:</h2>
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
      </div>
      <blockquote>
        <div class="tablee">
          <div class="row">
            <div class="cell1">TOTAL: </div>
            <div class="cell2">
              <xsl:value-of select="total"/>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row passed">
            <div class="cell1">passed: </div>
            <div class="cell2">
              <xsl:value-of select="passed"/>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row failed">
            <div class="cell1">failed: </div>
            <div class="cell2">
              <xsl:value-of select="failed"/>
            </div>
            <div class="space-line"></div>
          </div>
          <div class="row ignored">
            <div class="cell1">ignored: </div>
            <div class="cell2">
              <xsl:value-of select="ignored"/>
            </div>
            <div class="space-line"></div>
          </div>
        </div>
      </blockquote>
      <hr/>
    </xsl:for-each>

   
    <h2>Individual results:</h2>
    <button onclick="setClassDisplay('trace','none')">NoneTrace</button>
    <button onclick="setClassDisplay('trace','block')">AllTraces</button>
    <xsl:for-each select="/testsuite/testcase">
      <div>
        <xsl:attribute name="class">
          <xsl:choose>
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
            <xsl:value-of select="@classname"/>
          </div>
          <xsl:text disable-output-escaping="no"> - </xsl:text>
          <div class="method">
            <xsl:value-of select="@name"/>
          </div>
        </div>
        <div class="result">
          <xsl:choose>
            <xsl:when test="not(error)">
              <div class="status">
         PASSED (<xsl:value-of select="@time"/>s)
         </div>
            </xsl:when>
            <xsl:otherwise>
              <div class="status">
        FAILED (<xsl:value-of select="@time"/>s)
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
