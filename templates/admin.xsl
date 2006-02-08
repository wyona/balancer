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
          #overview { border: 1px solid #dedede; width: 88%; border-collapse: collapse; }
          #heading { background-color: #dedede; }
          #error { color: #ff0000; }
          #deactivated { color: #0000ff; }
          #ok { color: #00ff00; }
          input { border: #dedede 1px solid ; background-color: #ffffff; }
        </style>
      </head>
      <body>
        <h1>Balancer status</h1>
        <xsl:call-template name="worker-overview"/>
        <br/>
        <table id="opts">
          <xsl:apply-templates select="failover|balance-type|max-connections|refused-connections|connections|maintain-intervall"/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="worker-overview">
    <form method="GET" action="admin.html">
      <table id="overview">
        <tr id="heading">
          <td>Name</td>
          <td>Status</td>
          <td>Request Count</td>
          <td>RTT avg. [ms]</td>
          <td>RTT last [ms]</td>
          <td>Type</td>
          <td>URL</td>
          <td></td>
        </tr>
        <xsl:apply-templates select="//worker"/>
      </table>
    </form>
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
      <td align="center">
        <xsl:choose>
          <xsl:when test="@state=4">
            <input type="submit" name="{@name}" value="On"/>
          </xsl:when>
          <xsl:otherwise>
            <input type="submit" name="{@name}" value="Off"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="status2str">
    <xsl:param name="code"/>
    <xsl:choose>
      <xsl:when test="$code=0"><div id="ok">UNUSED</div></xsl:when>
      <xsl:when test="$code=1"><div id="ok">ALIVE</div></xsl:when>
      <xsl:when test="$code=2"><div id="error">TRANSPORT ERROR</div></xsl:when>
      <xsl:when test="$code=3"><div id="error">PROTOCOL ERROR</div></xsl:when>
      <xsl:when test="$code=4"><div id="deactivated">DEACTIVAED</div></xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="failover">
    <tr>
      <td>Failover</td>
      <td><xsl:value-of select="@enabled"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="balance-type">
    <tr>
      <td>Balance type</td>
      <td><xsl:value-of select="@type"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="max-connections">
    <tr>
      <td>Max. connections</td>
      <td><xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="refused-connections">
    <tr>
      <td><div id="error">Refused connections</div></td>
      <td><div id="error"><xsl:value-of select="@count"/></div></td>
    </tr>
  </xsl:template>

  <xsl:template match="connections">
    <tr>
      <td>Active connections</td>
      <td><xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="maintain-intervall">
    <tr>
      <td>Maintain intervall</td>
      <td><xsl:value-of select="@count"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
