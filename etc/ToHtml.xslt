<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0">

	<xsl:template match="/">
		<html>

			<head>
				<style type="text/css">
					table.tfmt {
					border: 1px ;
					}

					td.colfmt {
					border: 1px ;
					background-color: white;
					color: black;
					text-align: left;
					border: 1px solid black;
					}

					th {
					background-color: white;
					color: black;
					text-align: left;
					}

					td {
					border: 1px solid black;
					}
				</style>
				<h1>Prihlasovací formulár do UFL ligy</h1>
				<table class="main">
				<tr>
					<th style="width:150px">Názov tímu</th>
					<td style="width:120px"> <xsl:value-of select="ufl_team/team_name" /> </td>
 				</tr>
 				<tr>
					<th style="width:120px">E-mail na kapitána</th>
					<td> <xsl:value-of select="ufl_team/email" /> </td>
				</tr>
				<tr>
					<th style="width:120px">Tel. číslo na kapitána</th>
					<td> <xsl:value-of select="ufl_team/phone_number" /></td>
				</tr>
				</table>
			<br></br>
			</head>

			<body>
				<table class="tfmt">
					<tr>
						<th style="width:90px">Meno</th>
						<th style="width:120px">Priezvisko</th>
						<th style="width:60px">Pohlavie</th>
						<th style="width:60px">Ligista</th>


					</tr>

					<xsl:for-each select="ufl_team/players/player">

						<tr>
							<td class="colfmt">
								<xsl:value-of select="firstname" />
							</td>
							<td class="colfmt">
								<xsl:value-of select="lastname" />
							</td>

							<td class="colfmt">
								<xsl:value-of select="@gender" />
							</td>
							<td class="colfmt">
								<xsl:value-of select="leaguest" />
							</td>
						</tr>

					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>