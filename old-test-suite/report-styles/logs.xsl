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
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <script src="report-styles/index.js"/>
        <link href="report-styles/output.css" rel="stylesheet" type="text/css"/>
      </head>
      <body onload="setClassDisplay(&quot;fulltrace&quot;,&quot;none&quot;);setClassDisplay(&quot;stamp&quot;,&quot;none&quot;);setClassDisplay(&quot;output&quot;,&quot;none&quot;);openAnchor();">
        <div id="wholePage">
          <button onclick="showAllLogs()">show all</button>
          <button onclick="setClassDisplay('output','none')">hide all</button>
          <button onclick="showAllLogs()">show all logs</button>
          <button onclick="setClassDisplay('output','none')">hide all logs</button>
          <button onclick="setClassDisplay('method','block')">show all methods</button>
          <button onclick="setClassDisplay('method','none')">hide all methods</button>
          <xsl:for-each select="/logs/classlog">
            <div class="classa">
              <xsl:attribute name="id">
                <xsl:value-of select="@className"/>
              </xsl:attribute>
              <h1>
                <a>
                  <xsl:attribute name="name">
                    <xsl:value-of select="@className"/>
                  </xsl:attribute>
                </a>
                <xsl:value-of select="@className"/>
              </h1>
              <button><xsl:attribute name="onclick">
                showhideMethods('<xsl:value-of select="@className"/>','none')
              </xsl:attribute>
hide methods</button>
              <button><xsl:attribute name="onclick">
                showhideMethods('<xsl:value-of select="@className"/>','block')
              </xsl:attribute>
show methods</button>
              <xsl:for-each select="testLog">
                <div class="method">
                  <xsl:attribute name="id">
                    <xsl:value-of select="@fullId"/>
                  </xsl:attribute>
                  <h2>
                    <a>
                      <xsl:attribute name="name">
                        <xsl:value-of select="@fullId"/>
                      </xsl:attribute>
                      <xsl:value-of select="@testMethod"/>
                    </a>
                  </h2>
                  <button><xsl:attribute name="onclick">
                negateIdDisplayInline('<xsl:value-of select="@fullId"/>.out');
                recalcLogsWidth('<xsl:value-of select="@fullId"/>');
              </xsl:attribute>
show/hide stdout</button>
                  <button><xsl:attribute name="onclick">
                negateIdDisplayInline('<xsl:value-of select="@fullId"/>.err');
                recalcLogsWidth('<xsl:value-of select="@fullId"/>');
              </xsl:attribute>
show/hide stderr</button>
                  <button><xsl:attribute name="onclick">
                negateIdDisplayInline('<xsl:value-of select="@fullId"/>.all');
                recalcLogsWidth('<xsl:value-of select="@fullId"/>');
              </xsl:attribute>
show/hide alllog</button>
                  <button><xsl:attribute name="onclick">
                negateClassBlocDisplayIn('<xsl:value-of select="@fullId"/>','fulltrace');
              </xsl:attribute>
show/hide fulltraces</button>
                  <button><xsl:attribute name="onclick">
                negateClassBlocDisplayIn('<xsl:value-of select="@fullId"/>','stamp');
              </xsl:attribute>
show/hide stamps</button>
                  <div class="space-line">
                    <!-- -->
                  </div>
                  <xsl:for-each select="log">
                    <div class="output">
                      <xsl:attribute name="id"><xsl:value-of select="../@fullId"/>.<xsl:value-of select="@id"/></xsl:attribute>
                      <h3>
                        <xsl:value-of select="@id"/>
                      </h3>
                      <xsl:for-each select="item">
                        <div class="item">
                          <xsl:attribute name="id"><xsl:value-of select="../../@fullId"/>.<xsl:value-of select="../@id"/>.<xsl:value-of select="@id"/></xsl:attribute>
                          <div class="stamp"><xsl:value-of select="stamp"/></div>
                          <div class="fulltrace">
                            <pre>
                              <xsl:value-of select="fulltrace"/>
                            </pre>
                          </div>
                          <pre>
                            <xsl:value-of select="text"/>
                          </pre>
                          <!--item-->
                        </div>
                      </xsl:for-each>
                      <!--output-->
                    </div>
                  </xsl:for-each>
                  <div class="space-line">
                    <!-- -->
                  </div>
                  <!--method-->
                </div>
              </xsl:for-each>
              <!--classa-->
            </div>
          </xsl:for-each>
          <!--wholePage-->
        </div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
