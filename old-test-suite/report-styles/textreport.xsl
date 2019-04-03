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
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  <xsl:template match="/">Date:<xsl:value-of select="/testsuite/date"/>

Result: (In brackets are KnownToFail values if any)

TOTAL: <xsl:value-of select="/testsuite/stats/summary/total"/> <xsl:choose><xsl:when test="/testsuite/stats/summary/total/@known-to-fail!=0">(<xsl:value-of select="/testsuite/stats/summary/total/@known-to-fail"/>)</xsl:when></xsl:choose>
passed: <xsl:value-of select="/testsuite/stats/summary/passed"/> <xsl:choose><xsl:when test="/testsuite/stats/summary/passed/@known-to-fail!=0">(<xsl:value-of select="/testsuite/stats/summary/passed/@known-to-fail"/>)</xsl:when></xsl:choose>
failed: <xsl:value-of select="/testsuite/stats/summary/failed"/> <xsl:choose><xsl:when test="/testsuite/stats/summary/failed/@known-to-fail!=0">(<xsl:value-of select="/testsuite/stats/summary/failed/@known-to-fail"/>)</xsl:when></xsl:choose>
ignored: <xsl:value-of select="/testsuite/stats/summary/ignored"/> <xsl:choose><xsl:when test="/testsuite/stats/summary/ignored/@known-to-fail!=0">(<xsl:value-of select="/testsuite/stats/summary/ignored/@known-to-fail"/>)</xsl:when></xsl:choose>

Classes:
    <xsl:for-each select="/testsuite/stats/classes/class"><xsl:sort select="@name"/><xsl:choose><xsl:when test="passed = total">
PASSED  </xsl:when><xsl:otherwise>
FAILED  </xsl:otherwise></xsl:choose><xsl:value-of select="@name"/><xsl:text>  </xsl:text><xsl:value-of select="normalize-space(@classname)"/></xsl:for-each>

   
Individual results:
    <xsl:for-each select="/testsuite/testcase"><xsl:sort select="concat(@classname,@name)"/><xsl:choose><xsl:when test="@ignored">
IGNORED </xsl:when><xsl:when test="error">
FAILED  </xsl:when><xsl:otherwise>
PASSED  </xsl:otherwise></xsl:choose><xsl:value-of select="@classname"/><xsl:text>  </xsl:text><xsl:value-of select="@name"/><xsl:choose><xsl:when test="@known-to-fail"><xsl:choose><xsl:when test="@known-to-fail=true"><xsl:text> - WARNING This test is known to fail, but have passed!</xsl:text></xsl:when><xsl:otherwise><xsl:text> - This test is known to fail</xsl:text></xsl:otherwise></xsl:choose></xsl:when></xsl:choose><xsl:choose><xsl:when test="@remote"><xsl:text> - This test is running remote content.</xsl:text></xsl:when></xsl:choose></xsl:for-each>

  </xsl:template>
</xsl:stylesheet>
