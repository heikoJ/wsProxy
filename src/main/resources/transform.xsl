<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

                xmlns:ws="http://www.w3schools.com/webservices/"
        >

    <xsl:output omit-xml-declaration="no" indent="yes"/>
    <xsl:strip-space elements="*"/>




    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="ws:CelsiusToFahrenheitResult/text()">
        It worked :) <xsl:value-of select="."/>
    </xsl:template>


</xsl:stylesheet>
