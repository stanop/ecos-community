<@markup id="css" >
   <#if config.global.header?? && config.global.header.dependencies?? && config.global.header.dependencies.css??>
      <#list config.global.header.dependencies.css as cssFile>
         <@link href="${url.context}/res${cssFile}" group="header"/>
      </#list>
   </#if>
</@>

<@markup id="js">
   <#if config.global.header?? && config.global.header.dependencies?? && config.global.header.dependencies.js??>
      <#list config.global.header.dependencies.js as jsFile>
         <@script src="${url.context}/res${jsFile}" group="header"/>
      </#list>
   </#if>

   <#if __alf_current_site__?has_content>
      <@inlineScript group="header">
         var __alf_lastsite__ = "${__alf_current_site__}", expirationDate = new Date();
         expirationDate.setFullYear(expirationDate.getFullYear() + 1);
         document.cookie="alf_lastsite=" + __alf_lastsite__ + "; path=/; expires=" + expirationDate.toUTCString() + ";";
      </@>
   </#if>
</@>

<@markup id="widgets">
   <@inlineScript group="dashlets">
      <#if page.url.templateArgs.site??>
         Alfresco.constants.DASHLET_RESIZE = ${siteData.userIsSiteManager?string} && YAHOO.env.ua.mobile === null;
      <#else>
         Alfresco.constants.DASHLET_RESIZE = ${((page.url.templateArgs.userid!"-") = (user.name!""))?string} && YAHOO.env.ua.mobile === null;
      </#if>
   </@>
   <#if isSlideMenu>
        <script type="text/javascript">//<![CDATA[
        require([
            'js/citeck/modules/header/index'
        ], function(ShareHeader) {
            ShareHeader.render('share-header', {
                userName: "${((user.name)!"")?js_string}",
                userFullname: "${((user.fullName)!"")?js_string}",
                userNodeRef: "${((user.properties.nodeRef)!"")?js_string}",
                userIsAdmin: "${user.isAdmin?string}",
                userIsAvailable: "${((user.properties.available)!"")?string}",
                site: "${((page.url.templateArgs.site)!"")?js_string}"
            });
        });
        //]]></script>
   <#else>
      <@processJsonModel group="share"/>
   </#if>
</@>

<@markup id="html">
   <div id="share-header"></div>
</@>
