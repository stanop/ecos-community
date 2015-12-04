<#assign controlId = fieldId + "-autocompleteControl">
<#assign controlParams = viewScope.region.params>

<#assign helpMessage  = msg("autocomplete.help-message")>
<#assign emptyMessage = msg("autocomplete.empty-message")>
<#assign labelMessage = msg("autocomplete.label-message")>

<div id="${controlId}" class="autocomplete-control"
    data-bind='component: { name: "autocomplete",
        params: {
            <#if controlParams.minQueryLength??>
                minQueryLength: "${controlParams.minQueryLength}",
            </#if>
            <#if controlParams.queryStringMinial??>
                criterion: { attribute: "${controlParams.attribute}", predicate: "${controlParams.predicate}" },
            </#if>

            protected: protected,
            helpMessage: "${helpMessage}",
            emptyMessage: "${emptyMessage}",
            labelMessage: "${labelMessage}",

            value: singleValue,
            data: $data  
        }
    }'>
</div>