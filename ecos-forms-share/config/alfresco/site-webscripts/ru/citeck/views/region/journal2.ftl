<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-journal2Control">

<#assign optionParameters = [ "optionsText", "optionsValue" ]>

<#-- createVariant & virtualParent -->
<#assign createVariantsVisibility = params.createVariantsVisibility!"true" />
<#assign createVariantsSource>
	<#if params.createVariantsSource??>
		${params.createVariantsSource}
	<#elseif params.journalTypeId??>
		journal-create-variants
	<#else>create-views</#if>
</#assign>
<#assign virtualParent = params.virtualParent!"false" />

<#-- predicats: startsWith, contains -->
<#assign searchPredicat = params.searchPredicat!"contains">

<#-- step: 10, 15, 20 ...  -->
<#assign step = params.step!"10">

<div id="${controlId}" class="journal2-control" data-bind='component: { name: "select2", params: {
			element: $element,

			name: name,
			nodetype: nodetype,
			disabled: protected,
			multiple: multiple,
			value: value,

			options: options,

			mode: "table",

			<#-- table mode parameters -->
	        <#if params.journalTypeId??>journalTypeId: "${params.journalTypeId}",</#if>
	        <#if params.defaultVisibleAttributes??>defaultVisibleAttributes: "${params.defaultVisibleAttributes}",</#if>
	        <#if params.defaultSearchableAttributes??>defaultSearchableAttributes: "${params.defaultSearchableAttributes}",</#if>

			<#-- list mode parameters -->
			searchPredicat: "${searchPredicat}",
			step: ${step},

	        <#-- Create Object parameters -->
	        createVariantsVisibility: ${createVariantsVisibility},
	        createVariantsSource: "${createVariantsSource?trim}",
	        virtualParent: ${virtualParent},

			<#if params.options??>
				forceOptions: function() {
					var attribute = $data, node = attribute.node();
					return ko.computed(function() { return ${params.options}; });
				},
			</#if>

			getValueTitle: function(value) {
				return ko.computed(function() {
					return $data.getValueTitle.call($data, value);
				});
			}
        }
    }'>
</div>