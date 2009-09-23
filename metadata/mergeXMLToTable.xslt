<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="text" />
<xsl:param name="table_name" />
<xsl:param name="form_instance_id" />
<xsl:param name="create_table" />
<xsl:param name="create_insert" />
	<xsl:template match="/">
<xsl:if test="$create_table='true'">
CREATE TABLE <xsl:value-of select="$table_name" /> (form_instance_id int(11) NOT NULL,<xsl:apply-templates select="Records/Record/Field" mode="create_table" />
PRIMARY KEY  (form_instance_id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
</xsl:if>
<xsl:if test="$create_insert='true'">
<xsl:variable name="insert_string">
<xsl:apply-templates select="Records/Record/Field" mode="insert" />
</xsl:variable>
INSERT INTO <xsl:value-of select="$table_name" /> VALUES ('<xsl:value-of select="$form_instance_id"/>',
<xsl:value-of select="substring($insert_string,0,string-length($insert_string)-4)"/> 
);
</xsl:if>
	</xsl:template>

     <xsl:template match="Field" mode="create_table"><xsl:value-of select="@id" /> varchar(100) default NULL, 
	</xsl:template>

	<xsl:template match="Field" mode="insert">'<xsl:value-of select="./Value" />',  
	</xsl:template>
</xsl:stylesheet>
