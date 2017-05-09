<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:dc="http://purl.org/metadata/dublin_core#">
	<xsl:output method="xml" version="1.0" encoding="ISO-8859-1" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="Form">
		<xsl:element name="Records">
			<xsl:apply-templates select="Meta/dc:Title"/>
			<xsl:element name="Record">
				<xsl:apply-templates select="Page/Entity"/>
				<xsl:element name="Field">
					<xsl:attribute name="id">RecorsSta</xsl:attribute>23
				</xsl:element>
				<xsl:element name="Field">
					<xsl:attribute name="id">PrinterDev</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="dc:Title">
		<xsl:element name="Title">
			<xsl:value-of select="."/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="Entity">
		<xsl:if test="@type='Entry' or @type='Group' or @type='Radio'">
			<xsl:if test="not(@name='')">
				<xsl:element name="Field">
					<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
					<xsl:attribute name="taborder"><xsl:value-of select="@taborder"/></xsl:attribute>
				</xsl:element>
			</xsl:if>
		</xsl:if>
		<xsl:apply-templates select="Entity"/>
	</xsl:template>
</xsl:stylesheet>
