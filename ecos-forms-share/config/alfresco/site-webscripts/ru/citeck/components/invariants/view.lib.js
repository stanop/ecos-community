function getAttributes(view) {
    var attributes = [];
    getAttributesRecursively(view, attributes);
    return attributes;
}

function getAttributesRecursively(element, attributes) {
    if(element.type == "view") {
        for(var i in element.elements) {
            getAttributesRecursively(element.elements[i], attributes);
        }
    } else if(element.type == "field") {
        if(attributes.indexOf(element) == -1) {
            attributes.push(element);
        }
    }
}

function getTabs(view) {
    var tabs = [];

    for(var i in view.elements) {
        if(view.elements[i].type == "view") {
            var attributes = getAttributes(view.elements[i]),
                namesOfAttributes = [];

            for (var a in attributes) { namesOfAttributes.push(attributes[a].attribute) }
            view.elements[i]["attributes"] = namesOfAttributes;
            tabs.push(view.elements[i]);
        }
    }

    return tabs;
}

function getInvariantSet(args, attributes) {
    var response = remote.call('/citeck/invariants?' + (args.nodeRef ? 'nodeRef=' + args.nodeRef : args.type ? 'type=' + args.type : '') + 
        (attributes && attributes.length ? "&attributes=" + attributes.join(',') : ''));
    return eval('(' + response + ')');
}

function getViewScopedInvariants(view) {
    var invariants = [];
    getInvariantsRecursively(view, invariants);
    return invariants
}

function getInvariantsRecursively(element, invariants) {
    if(element.type == "view") {
        for(var i in element.elements) {
            getInvariantsRecursively(element.elements[i], invariants);
        }
    } else if(element.type == "field" && element.invariants && element.invariants.length) {
        for(var i in element.invariants) {
            invariants.push(element.invariants[i]);
        }
    }
}

function getWritePermission(nodeRef) {
  if (!nodeRef) return;

  var serviceURI = "/citeck/has-permission?nodeRef=" + nodeRef + "&permission=Write",
      response = eval('(' + remote.call(serviceURI) + ')'); 

  return response;
}

function getView(args) {
    var serviceURI = '/citeck/invariants/view?';

    // try-block is used to protect from absense of page object in model.
    // typeof page somehow fails with exception:
    // Invalid JavaScript value of type java.util.HashMap
    try {
        for(var name in page.url.args) {
            if(!name.match(/^param_/)) continue;
            serviceURI += name + '=' + encodeURIComponent(page.url.args[name]) + '&';
        }
    } catch(e) {}

    for(var name in args) {
        if(name == 'htmlid') continue;
        serviceURI += name + '=' + encodeURIComponent(args[name]) + '&';
    }

    var response = remote.call(serviceURI);
    var view = eval('(' + response + ')');

    if(response.status == 404) {
        var formUrl = url.context + '/page/components/form?htmlid=' + encodeURIComponent(args.htmlid) + '&submitType=json&showCancelButton=true';
        if(args.type) formUrl += '&itemKind=type&itemId=' + encodeURIComponent(args.type);
        else formUrl += '&itemKind=node&itemId=' + encodeURIComponent(args.nodeRef);
        if(args.viewId) formUrl += '&formId=' + encodeURIComponent(args.viewId);
        if(args.mode) formUrl += '&mode=' + encodeURIComponent(args.mode);
        if(args.param_destination) formUrl += '&destination=' + encodeURIComponent(args.param_destination);

        status.code = 303;
        status.location = formUrl;
        status.redirect = true;
        return null;
    }

    if(response.status != 200) {
        throw 'Can not get view from uri "' + serviceURI + '": ' + view.message;
    }
    
    return view;
}