<#macro renderElement element>
	<#assign template = element.template!"default" />
	<#assign oldScope = viewScope!{} />
	<#global viewScope = oldScope + { element.type : element } />
	
	<#if element.attribute??>
		<!-- ko with: attribute("${element.attribute}") -->
		<#global fieldId = args.htmlid + "-" + element.attribute?replace(':', '_') />
	</#if>
	
		<div class="form-${element.type} template-${template}" <#if element.attribute??>data-bind="css: { invalid: invalid, hidden: irrelevant, 'with-help': description }"</#if>>
			<@renderContent element />
		</div>
	
	<#if element.attribute??>
		<!-- /ko -->
	</#if>
	
	<#global viewScope = oldScope />
</#macro>

<#macro renderContent element>
	<#assign template = element.template!"default" />
	<#assign template_exists = "ru.citeck.ecos.freemarker.TemplateExistsMethod"?new() />
	<#assign file>/ru/citeck/views/${element.type}/${viewScope.view.mode}/${template}.ftl</#assign>
	<#if template_exists(file)><#include file /><#return /></#if>
	<#assign file>/ru/citeck/views/${element.type}/${template}.ftl</#assign>
	<#if template_exists(file)><#include file /><#return /></#if>
	<#assign file>/ru/citeck/views/${element.type}/${viewScope.view.mode}/default.ftl</#assign>
	<#if template_exists(file)><#include file /><#return /></#if>
	<#assign file>/ru/citeck/views/${element.type}/default.ftl</#assign>
	<#if template_exists(file)><#include file /><#return /></#if>
</#macro>

<#macro renderRegion name>
	<#list viewScope.field.regions as region>
		<#if region.name == name>
			<@renderElement region />
		</#if>
	</#list>
</#macro>

<#macro renderViewContainer view id>
	<div id="${id}-form" class="ecos-form ${view.mode}-form invariants-form form-template-${view.template} loading"
			 data-bind="css: { loading: !loaded() }">
		<div class="loading-container">
			<div class="loading-indicator"></div>
			<div class="loading-message">${msg('message.loading.form')}</div>
		</div>
		
		<!-- ko API: rootObjects -->
		<div class="form-fields" data-bind="with: node().impl">
			<!-- ko if: attributes().length != 0 -->
			<@views.renderElement view />
			<!-- /ko -->
		</div>
		<!-- /ko -->
		
		<#if view.mode != 'view'>
		<div class="form-buttons" data-bind="with: node().impl">
			<input id="${id}-form-submit" type="submit" 
					value="<#if view.mode == "create">${msg("button.create")}<#else/>${msg("button.save")}</#if>" 
					data-bind="enable: valid() && !inSubmitProcess(), click: $root.submit.bind($root)" />

			<#-- TODO support create and continue -->
			<input id="${id}-form-reset"  type="button" value="${msg("button.reset")}" data-bind="enable: changed, click: reset" />
			<input id="${id}-form-cancel" type="button" value="${msg("button.cancel")}" data-bind="enable: true, click: $root.cancel.bind($root)" />
		</div>
		</#if>
	
	</div>
</#macro>

<#function regionIsPresent name>
	<#list viewScope.field.regions as region>
		<#if region.name == name && region.template??>
			<#return true />
		</#if>
	</#list>
	<#return false />
</#function>

<#macro renderQNames qnames>
	[
		<#list qnames as qname>
			"${qname}"<#if qname_has_next>,</#if>
		</#list>
	]
</#macro>

<#macro renderInvariant invariant><#escape x as jsonUtils.encodeJSONString(x)>{
	"scope": {
		<#assign invariantScope = invariant.scope!{} />
		"class": <#if invariantScope.class??>"${invariantScope.class}"<#else>null</#if>,
		"classKind": <#if invariantScope.classKind??>"${invariantScope.classKind}"<#else>null</#if>,
		"attribute": <#if invariantScope.attribute??>"${invariantScope.attribute}"<#else>null</#if>,
		"attributeKind": <#if invariantScope.attributeKind??>"${invariantScope.attributeKind}"<#else>null</#if>
	},
	"feature": "${invariant.feature?string}",
	"description": "${invariant.description!}",
	"final": ${invariant.final?string},
	"language": "${invariant.language}",
	"expression": 
		<#assign value = invariant.expression />
		<#if value?is_sequence>
			[
			<#list value as item>
				<#if item?is_hash>
				{
					"attribute": "${item.attribute}",
					"predicate": "${item.predicate}",
					"value": <#if item.value??>"${item.value}"<#else>null</#if>
				}
				<#elseif item?is_string>
				"${item}"
				</#if><#if item_has_next>,</#if>
			</#list>
			]
		<#elseif value?is_string>
			"${value}"
		<#else>
			null
		</#if>
}</#escape></#macro>

<#macro renderInvariants invariants>
	[
		<#list invariants as invariant>
			<@renderInvariant invariant /><#if invariant_has_next>,</#if>
		</#list>
	]
</#macro>

<#macro renderModel model>
<#escape x as jsonUtils.encodeJSONString(x)>{
	<#list model?keys as key>
		"${key}": <@views.renderValue model[key] /><#if key_has_next>,</#if>
	</#list>
}</#escape>
</#macro>

<#macro renderValue value="">
	<#if !value??>
		null
	<#elseif value?is_hash>
		<@views.renderModel value />
	<#elseif value?is_string>
		"${value}"
	<#elseif value?is_number>
		${value?c}
	<#elseif value?is_boolean>
		${value?string}
	<#elseif value?is_enumerable>
		[ <#list value as item><@views.renderValue item /><#if item_has_next>,</#if></#list> ]
	</#if>
</#macro>

<#macro nodeViewStyles>
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/invariants/invariants.css" group="node-view" />
	<@link rel="stylesheet" href="${url.context}/res/yui/calendar/assets/calendar.css" group="node-view"/>
</#macro>

<#macro nodeViewScripts>
	<@script src="${url.context}/res/yui/resize/resize.js" group="node-view"></@script>
	<@script src="${url.context}/res/yui/calendar/calendar.js" group="node-view"></@script>

	<@script src="${url.context}/res/citeck/components/form/constraints.js" group="node-view"></@script>

	<@script src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/criteria-model.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="node-view"></@script>
</#macro>

<#macro nodeViewWidget nodeRef="" type="">
	<@inlineScript group="node-view">
		<#assign runtimeKey = args.runtimeKey!args.htmlid />
		<#escape x as x?js_string>
		require(['citeck/components/invariants/invariants', 'citeck/utils/knockout.invariants-controls', 'citeck/utils/knockout.yui'], function(InvariantsRuntime) {
			new InvariantsRuntime("${args.htmlid}-form", "${runtimeKey}").setOptions({
				<#if view.template == "tabs">
					tabsModel: {
						lazyLoadingTabs: ${view.params.lazyLoadingTabs!"false"},
						clickLoadingTabs: ${view.params.clickLoadingTabs!"false"},

						tabs: <@views.renderValue tabs />
						<#-- TODO:	hide 'elements' property -->
					},
				</#if>

				formTemplate: "${view.template}",

				model: {
					key: "${runtimeKey}",
					parent: <#if args.param_parentRuntime?has_content>"${args.param_parentRuntime}"<#else>null</#if>,
					node: {
						key: "${runtimeKey}",
						virtualParent: <#if (args.param_virtualParent!"false") == "true">"${args.param_parentRuntime}"<#else>null</#if>,
						nodeRef: <#if nodeRef?has_content>"${nodeRef}"<#else>null</#if>,
						<#if type?has_content>type: "${type}",</#if>
						<#if classNames??>classNames: <@views.renderQNames classNames />,</#if>
						forcedAttributes: <@views.renderQNames attributes />,
						runtime: "${runtimeKey}",
						defaultModel: <@views.renderValue defaultModel />,
					},
					invariantSet: {
						key: "${runtimeKey}",
						invariants: <@views.renderInvariants invariants />
					}
				}
			});
		});
		</#escape>
	</@>
</#macro>