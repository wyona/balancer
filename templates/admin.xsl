<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">

  <xsl:template match="/status">
    <html>
      <head>
        <title>Balancer - Status</title>
        <style type="text/css">
          body { background-color: #ffffff; font-family: sans-serif; color: #000000; }
          h1 { color: #0000ff; }
          table { width: 80%; }
          #heading { background-color: #dedede; }
          #error { color: #ff0000; }
          #ok { color: #00ff00; }
        </style>
      </head>
      <body>
        <h1>Balancer status</h1>
        <xsl:call-template name="worker-overview"/>
        <br/>
        <table>
          <xsl:apply-templates select="failover|balance-type|max-connections|connections|maintain-intervall"/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="worker-overview">
    <table>
      <tr id="heading">
        <td>Name</td>
        <td>Status</td>
        <td>Request Count</td>
        <td>RTT avg. [ms]</td>
        <td>RTT last [ms]</td>
        <td>Type</td>
        <td>URL</td>
      </tr>
      <xsl:apply-templates select="//worker"/>
    </table>
  </xsl:template>

  <xsl:template match="worker">
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td>
        <xsl:call-template name="status2str">
          <xsl:with-param name="code" select="@state"/>
        </xsl:call-template>
      </td>
      <td><xsl:value-of select="@count"/></td>
      <td><xsl:value-of select="@rttavg"/></td>
      <td><xsl:value-of select="@rtt"/></td>
      <td><xsl:value-of select="@type"/></td>
      <td><a href="{@uri}"><xsl:value-of select="@uri"/></a></td>
    </tr>
  </xsl:template>

  <xsl:template name="status2str">
    <xsl:param name="code"/>
    <xsl:choose>
      <xsl:when test="$code=0"><div id="ok">UNUSED</div></xsl:when>
      <xsl:when test="$code=1"><div id="ok">ALIVE</div></xsl:when>
      <xsl:when test="$code=2"><div id="error">TRANSPORT ERROR</div></xsl:when>
      <xsl:when test="$code=3"><div id="error">PROTOCOL ERROR</div></xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="failover">
    <tr>
      <td>failover: <xsl:value-of select="@enabled"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="balance-type">
    <tr>
      <td>balance-type: <xsl:value-of select="@type"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="max-connections">
    <tr>
      <td>max-connections: <xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="connections">
    <tr>
      <td>active-connections: <xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="maintain-intervall">
    <tr>
      <td>maintain-intervall: <xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
