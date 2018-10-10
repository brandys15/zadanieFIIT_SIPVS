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
					text-align:right;
					}

					th {
					background-color: #2E9AFE;
					color: white;
					}

				</style>

				<b>Nazov timu: </b>
				<xsl:value-of select="ufl_team/team_name" /><br></br>
				<b>Kapitanov email: </b>
				<xsl:value-of select="ufl_team/email" /><br></br>
				<b>Kapitanove cislo: </b>
				<xsl:value-of select="ufl_team/phone_number" /><br></br>


			</head>

			<body>
				<table class="tfmt">
					<tr>
						<th style="width:250px">Meno:</th>
						<th style="width:350px">Priezvisko:</th>
						<th style="width:250px">Pohlavie:</th>
						<th style="width:250px">Ligista:</th>


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
								<xsl:value-of select="gender" />
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