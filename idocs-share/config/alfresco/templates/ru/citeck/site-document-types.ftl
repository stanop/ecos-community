<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
	<!-- CSS and JavaScript files are linked in here -->
</@>

<@templateBody>
<div id="alf-hd">
	<#include "/ru/citeck/include/header.ftl" />
	<h1 class="sub-title"><#if page.titleId??>${msg(page.titleId)!page.title}<#else>${page.title}</#if></h1>
</div>

<div id="bd">
	<@region id="data" scope="template" />
</div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>