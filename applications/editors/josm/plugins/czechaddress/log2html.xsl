<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <body>
    <table border="1">
      <tr bgcolor="#9acd32">
        <th>Date</th>
        <th>Level</th>
        <th>Message</th>
        <th>Method</th>
        <th>Param</th>
      </tr>
      <xsl:for-each select="log/record">
      <tr>
        <td><font size="-3"><xsl:value-of select="date"/></font></td>
        <xsl:choose>
            <xsl:when test="level = 'FINEST'">
                <td><font color='#AAAAAA'><i><xsl:value-of select="level"/></i></font></td>
		<td><i><xsl:value-of select="message"/></i></td>
            </xsl:when>
            <xsl:when test="level = 'FINER'">
                <td><font color='#999999'><xsl:value-of select="level"/></font></td>
                <td><xsl:value-of select="message"/></td>
            </xsl:when>
            <xsl:when test="level = 'FINE'">
                <td><font color='#444444'><b><xsl:value-of select="level"/></b></font></td>
                <td><b><xsl:value-of select="message"/></b></td>
            </xsl:when>
            <xsl:when test="level = 'INFO'">
                <td><font color='black'><b><xsl:value-of select="level"/></b></font></td>
                <td><b><xsl:value-of select="message"/></b></td>
            </xsl:when>
            <xsl:when test="level = 'WARNING'">
                <td><font color='red'><b><xsl:value-of select="level"/></b></font></td>
                <td><b><xsl:value-of select="message"/></b></td>
            </xsl:when>
            <xsl:otherwise>
                <td><xsl:value-of select="level"/></td>
            </xsl:otherwise>
        </xsl:choose>
        <td><tt><xsl:value-of select="method"/></tt></td>
        <td><xsl:value-of select="param"/></td>
      </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>


