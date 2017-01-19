/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
if (typeof Citeck == "undefined" || !Citeck)
{
   var Citeck = {};
}

Citeck.utils   = Citeck.utils || {};
Citeck.HTML5   = Citeck.HTML5 || {};
Citeck.Browser = Citeck.Browser || {};
Citeck.UI      = Citeck.UI || {};
Citeck.mobile = Citeck.mobile || {};

Citeck.mobile.hasTouchEvent = function() {
    try {
        document.createEvent('TouchEvent');
        return true;
    } catch(e) { return false; }
};

Citeck.mobile.isMobileDevice = function() {
    var ua = (navigator.userAgent || navigator.vendor || window.opera);
    if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(ua) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(ua.substr(0, 4))) {
        return true;
    } else { return false; }
};

Citeck.namespace = function(namespace) {
	var names = namespace.split('.'),
		scope = this;
	for(var i = 0, ii = names.length; i < ii; i++) {
		var name = names[i];
		if(typeof scope[name] == "undefined") {
			scope[name] = {};
		}
		scope = scope[name];
	}
	return scope;
};

// HTML5
// -----

Citeck.HTML5.supportedInputTypes = function() {
  var input = document.createElement("INPUT");

  var inputTypes = [ "search", "number", "range", "color", "tel", "url", "email", "date", "month", "week", "time", "datetime", "datetime-local" ], 
    supportedInputTypes = [];

  for (var i in inputTypes) {
    input.setAttribute("type", inputTypes[i]);
    if (input.type !== "text") {
      supportedInputTypes.push(inputTypes[i]);
    }
  }

  return supportedInputTypes;
}

Citeck.HTML5.supportAttribute = function(attrName) {
    var input = document.createElement('INPUT');
    return attrName in input;
};

Citeck.HTML5.supportInput = function(typeName) {
    var input = document.createElement('INPUT');
    input.setAttribute("type", typeName)
    if (input.type !== "text") return true;
    return false;
};

var supportedInputTypes = Citeck.HTML5.supportedInputTypes();
var inputTypes = [ "search", "number", "range", "color", "tel", "url", "email", "date", "month", "week", "time", "datetime", "datetime-local" ];
for (var i in inputTypes) {
  Citeck.HTML5.supportedInputTypes[inputTypes[i]] = supportedInputTypes.indexOf(inputTypes[i]) != -1;
}
supportedInputTypes = null;
inputTypes = null;


// UTILS
// -----

Citeck.utils.concatOptions = function(defaultOptions, newOptions) {
    for (var key in newOptions) {
        var newValue = newOptions[key],
            oldValue = defaultOptions[key];

        if (newValue && newValue != oldValue) {
            defaultOptions[key] = newOptions[key];
        }
    }
};

/**
 * Format user name
 * @param user{object}
 * {
 *   userName: "userName",
 *   firstName: "firstName", 
 *   lastName: "lastName",
 * }
 * @param plain_text - whether function should return formatted html (false) or just text (true)
 */
Citeck.utils.formatUserName = function(user, plain_text)
{
	var name = Alfresco.util.message("label.none");
	if(user) {
		if(user.firstName) {
			name = user.firstName;
			if(user.lastName) name += " " + user.lastName;
		} else if(user.userName) {
			name = user.userName;
		}
		if(!plain_text) {
			name = Alfresco.util.userProfileLink(user.userName, name);
		}
	}
	return name;
};

/**
 * Format document name
 * @param document{object}
 * {
 *   nodeRef: "workspace://SpacesStore/...",
 *   name: "name",
 * @param plain_text - whether function should return formatted html (false) or just text (true)
 * }
 */
Citeck.utils.formatDocumentName = function(doc, plain_text)
{
	if(!doc) {
		return Alfresco.util.message("label.none");
	} else if(!plain_text) {
		return '<a href="' + Alfresco.util.siteURL('document-details?nodeRef=' + doc.nodeRef) + '">' + doc.name + '</a>';
	} else {
		return doc.name;
	}
};

Citeck.utils.onFieldAvailable = function(object, field, func, scope) {
	var waitFunc = function() {
		if(object[field]) {
			func.call(scope, object[field]);
		} else {
			YAHOO.lang.later(100, this, waitFunc);
		}
	};
	waitFunc.call(this);
};

Citeck.utils.resolvePath = function(object, keys) {
	switch(typeof keys) {
	case "string": 
		keys = keys.split(/\./);
		break;
	case "undefined":
		keys = [];
		break;
	}
	var result = object;
	for(var i = 0, ii = keys.length; i < ii; i++) {
		result = result[keys[i]];
		if(!result) break;
	}
	return result;
};

Citeck.utils.mapObject = function(object, mapping) {
	if(typeof mapping == "function") {
		return mapping(object);
	}
	var result = {};
	_.each(mapping, function(value, key) {
		switch(typeof value) {
		case "string":
			result[key] = Citeck.utils.resolvePath(object, value);
			break;
		case "object":
			result[key] = Citeck.utils.mapObject(object[key], value);
			break;
		default:
			throw "Unsupported value type in mapping: " + (typeof value);
		}
	});
	return result;
};

Citeck.utils.lazyLoad = function(collection, itemKey, urlTemplate, callback, resultPath, keyField) {
	var multipleKeys = YAHOO.lang.isArray(itemKey),
		itemKeys = multipleKeys ? itemKey : [ itemKey ],
		missingKeys = [];
	
	// returning function:
	var getResult = function() {
		if(!multipleKeys) {
			return collection[itemKey];
		}
		var result = [];
		for(var i = 0, ii = itemKeys.length; i < ii; i++) {
			result.push(collection[itemKeys[i]]);
		}
		return result;
	};
	
	// fill missing keys
	for(var i = 0, ii = itemKeys.length; i < ii; i++) {
		var key = itemKeys[i];
		if(typeof collection[key] == "undefined" || collection[key] === null) {
			missingKeys.push(key);
		}
	}
	
	// if there is no missing keys - return result immediately
	if(missingKeys.length == 0) {
		YAHOO.lang.later(0, callback.scope, callback.fn, [ getResult() ]);
		return;
	}
	
	Alfresco.util.Ajax.jsonGet({
		url: YAHOO.lang.substitute(urlTemplate, {
			key: itemKey,
			keys: missingKeys.join(",")
		}),
		successCallback: { 
			fn: function(response) {
				var result = response.json;
				if(resultPath) {
					result = Citeck.utils.resolvePath(result, resultPath);
				}
				if(multipleKeys) {
					if(keyField) {
						for(var i in result) {
							if(!result.hasOwnProperty(i)) continue;
							collection[result[i][keyField]] = result[i];
						}
					} else {
						for(var i in result) {
							if(!result.hasOwnProperty(i)) continue;
							collection[i] = result[i];
						}
					}
				} else {
					collection[itemKey] = result;
				}
				callback.fn.call(callback.scope, getResult());
			}
		}
	});
};

/**
 * Bulk loader - can be used to gather similar (or identical) queries together.
 * 
 * @param config configuration settings (* means mandatory)
 *      *url: url for request
 *      method: HTTP-method for request (default is GET)
 *      *emptyFn: function() - returns empty query object
 *      *addFn: function(query, id) - adds new id to query, returns true, if added
 *      *getFn: function(response) - extracts map { id: result }
 *      timeoutMs: timeout in milliseconds from the first query (default is 10)
 */
Citeck.utils.BulkLoader = function(config) {
    // check config:
    _.each(['url', 'emptyFn', 'addFn', 'getFn'], function(key) {
        if(typeof config[key] == 'undefined') {
            throw "Parameter '" + key + "' must be specified";
        }
    }, this);
    _.extend(this, {
        method: "GET",
        timeoutMs: 10
    }, config);
    
    this.requests = [];
    this.ids = [];
    this.currentQuery = this.emptyFn();
}

Citeck.utils.BulkLoader.prototype = {
    /**
     * Load new object by id.
     * 
     * @param id
     * @param callback object with 'scope' and 'fn' keys.
     *      fn - function(id, result) - specified by caller to process query results
     *   scope - scope to execute function with
     */
    load: function(ids, callback) {
        var requests = this.requests;
        
        if(_.isEmpty(requests)) {
            _.delay(_.bind(this._request, this), this.timeoutMs);
        }
        
        var addId = function(id) {
            if(this.ids.indexOf(id) == -1) {
                this.addFn(this.currentQuery, id);
            }
        };
        
        if(_.isArray(ids)) {
            _.map(ids, addId, this);
        } else {
            addId.call(this, ids);
        }
        
        requests.push({
            ids: ids,
            callback: callback
        });
    },
    
    _reset: function(key, value) {
        var oldValue = this[key];
        this[key] = value;
        return oldValue;
    },
    
    _request: function() {
        if(_.isEmpty(this.requests)) return;
        
        var query = this._reset('currentQuery', this.emptyFn()),
            requests = this._reset('requests', []);
        
        Alfresco.util.Ajax.request({
            url: this.url,
            method: this.method,
            dataObj: query,
            requestContentType: Alfresco.util.Ajax.JSON,
            successCallback: {
                scope: this,
                fn: function(response) {
                    this._response(response, requests);
                }
            }
        });
    },
    
    _response: function(response, requests) {
        var results = this.getFn(response);
        var process = function(request) {
            var callback = request.callback,
                ids = request.ids,
                result = _.isArray(ids) ? _.map(ids, function(id) { return results[id]; }) : results[ids];
            if(_.isFunction(callback)) {
                callback(ids, result);
            } else {
                callback.fn.call(callback.scope, ids, result);
            }
        };
        
        // process old requests
        _.each(requests, process, this);
    }
};

Citeck.utils.nodeInfoLoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/node/nodes",
    method: "POST",
    emptyFn: function() { return { nodeRefs: [] } },
    addFn: function(query, nodeRef) {
        if(query.nodeRefs.indexOf(nodeRef) == -1) {
            query.nodeRefs.push(nodeRef);
            return true;
        } else {
            return false;
        }
    },
    getFn: function(response) {
        var nodes = response.json.nodes;
        return _.object(_.pluck(nodes, 'nodeRef'), nodes);
    }
});

Citeck.utils.attributeValueLoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/attributes/values",
    method: "POST",
    emptyFn: function() { return { requests: [] } },
    addFn: function(query, id) {
        var request = this.buildRequest(id);
        query.requests.push(request);
        return true;
    },
    getFn: function(response) {
        var responses = response.json.attributes;
        return _.object(_.map(responses, this.buildId, this), responses);
    }
});

Citeck.utils.definedAttributesLoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/attributes/defined",
    method: "POST",
    emptyFn: function() { return { types: [], nodes: [] }},
    addFn: function(query, id) {
        if(Citeck.utils.isNodeRef(id)) {
            query.nodes.push(id);
        } else {
            query.types.push(id);
        }
    },
    getFn: function(response) { return response.json; }
})

YAHOO.lang.augmentObject(Citeck.utils.attributeValueLoader, {
    
    separator: "|---|",
    
    loadValue: function(nodeRef, attribute, callback) {
        return this.load(this.buildId({
            nodeRef: nodeRef, 
            attribute: attribute
        }), callback);
    },
    
    buildId: function(request) {
        return request.nodeRef + this.separator + request.attribute;
    },
    
    buildRequest: function(id) {
        var index = id.indexOf(this.separator);
        return {
            nodeRef: id.substring(0, index),
            attribute: id.substring(index + this.separator.length)
        };
    }
    
});

Citeck.utils.messageLoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/util/messages",
    method: "POST",
    emptyFn: function() { return { keys: [] }},
    addFn: function(query, key) {
        if(query.keys.indexOf(key) == -1) {
            query.keys.push(key);
            return true;
        } else {
            return false;
        }
    },
    getFn: function(response) {
        return response.json;
    }
});

Citeck.utils.nsPrefixLoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/namespaces",
    method: "POST",
    emptyFn: function() { return { uris: [] }},
    addFn: function(query, key) {
        if(query.uris.indexOf(key) == -1) {
            query.uris.push(key);
            return true;
        } else {
            return false;
        }
    },
    getFn: function(response) {
        return _.invert(response.json.namespaces);
    }
});

Citeck.utils.nsURILoader = new Citeck.utils.BulkLoader({
    url: Alfresco.constants.PROXY_URI + "citeck/namespaces",
    method: "POST",
    emptyFn: function() { return { prefixes: [] }},
    addFn: function(query, key) {
        if(query.prefixes.indexOf(key) == -1) {
            query.prefixes.push(key);
            return true;
        } else {
            return false;
        }
    },
    getFn: function(response) {
        return response.json.namespaces;
    }
});

Citeck.utils.DoclibRecordLoader = function(view) {
	var url = Alfresco.constants.URL_SERVICECONTEXT
		+ "citeck/components/documentlibrary/data/explicit/type/node/alfresco/user/home?filter=all"
		+ (view ? "&view=" + view : "");
	Citeck.utils.DoclibRecordLoader.superclass.constructor.call(this, {
		url: url,
		method: "POST",
		emptyFn: function() { return { nodeRefs: "" }; },
		addFn: function(query, id) {
			if(id) {
				query.nodeRefs += id + ","; 
				return true;
			} else {
				return false;
			}
		},
		getFn: function(response) {
			var records = response.json.items;
			return _.object(_.pluck(records, 'nodeRef'), records);
		}
	});
};
YAHOO.extend(Citeck.utils.DoclibRecordLoader, Citeck.utils.BulkLoader);


/**
 * Generate doBeforeDialogShow function for Alfresco.module.SimpleDialog.
 * params:
 * - headerId - message to set to header
 * - header - text to set to header
 * - nodeRef - to show edit-metadata link
 */
Citeck.utils.fnBeforeDialogShow = function(params) {
    return function(p_form, p_dialog) {
        // Dialog title
        var headerElementPostfix = "-form-container_h";
        if(params.headerId) {
            var titleSpan = '<span class="light">' + Alfresco.util.encodeHTML(Alfresco.util.message(params.headerId)) + '</span>';
            Alfresco.util.populateHTML([ p_dialog.id + headerElementPostfix, titleSpan ]);
        } else if(params.header) {
            var titleSpan = '<span class="light">' + Alfresco.util.encodeHTML(params.header) + '</span>';
            Alfresco.util.populateHTML([ p_dialog.id + headerElementPostfix, titleSpan ]);
        }
        if(params.nodeRef) {
            // Edit metadata link button (if present)
            this.widgets.editMetadata = Alfresco.util.createYUIButton(p_dialog, "editMetadata", null,
            {
               type: "link",
               label: Alfresco.util.message("edit-details.label.edit-metadata"),
               href: Alfresco.util.siteURL("edit-metadata?nodeRef=" + params.nodeRef)
            });
        }
    };
};

Citeck.utils.isMobile = function() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
};

Citeck.utils.sleep = function(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
};

Citeck.utils.isNodeRef = function(string) {
    return /^.+:\/\/.+\/.+$/.test(string);
};

Citeck.utils.isFilename = function(string) {
  return /\..{3}$/.test(string);
};

Citeck.utils.isShortQName = function(string) {
  return /^(.+)[:](.+)$/.test(string);
};

Citeck.utils.isLongQName = Citeck.utils.isFullQName = function(string) {
  return /^[{](.+)[}](.+)$/.test(string);
};

Citeck.utils.arrayChunk = function(array, count) {
  var arrays = [];

  for (var i = 0; i < array.length; i+=count) { 
    arrays.push(array.slice(i, i+count)); 
  }

  return arrays; 
};

Citeck.utils.getURLParameterByName = function(name) {
  name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
  return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}


// BROWSER
// -------

Citeck.Browser.isIE = function(version) {
    var ua = window.navigator.userAgent, 
        msie = ua.indexOf("MSIE "), trident = ua.indexOf('Trident/'), edge = ua.indexOf('Edge/'),
        ieVersion = false;

    // IE 10 or older 
    if (msie > 0) {
        ieVersion = parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);

    // IE 11
    } else if (trident > 0) {
        var rv = ua.indexOf('rv:');
        ieVersion = parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);

    // Edge (IE 12+)
    } else if (edge > 0) {
        ieVersion = parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
    }

    return ieVersion ? (version ? +ieVersion == +version : true) : false;
}


// UI
// --

Citeck.UI.waitIndicator = function(id, params) {
    this.id = id;
    var self = this;

    this.options = { 
        modal: true, 
        context: null, 
        spinners: "two", 
        size: 200, 
        message: Alfresco.util.message("label.loading")
    };
    for (var p in params) { this.options[p] = params[p]; }


    // build DOM
    this._envelope = $("<div>", { "id": this.id + "-envelope", "class": "wait-indicator-envelope", "style": "visibility: hidden;"  });
    this._container = $("<div>", { "id": this.id + "-container", "class": "wait-indicator-container" });
    this._indicator = $("<div>", { "id": this.id + "-indicator", "class": "wait-indicator-indicator" });
    this._message = $("<div>", { "id": this.id + "-message", "class": "wait-indicator-message", "text": this.options.message });

    // custom color of spinner
    if (this.options.color) {
        this._indicator.css("border-color", this.options.color);
        this._message.css("color", this.options.color);
    }

    this._envelope.append(
        this._container
            .append(this._indicator)
            .append(this._message)
    );

    // public functions
    this.getEl = function() { return this._envelope[0]; }

    this.hide = function() {
        var parent = self._envelope.parent();

        if (parent.attr("data-height-was-modified")) {
            parent.height("");
        }

        self._envelope.css("visibility", "hidden"); 
    }

    this.show = function() { 
        var parent = self._envelope.parent(),
            offset = parent.offset();

        if (parent.css("position") == "static") {
            parent.css("position", "relative").css("overflow", "hidden");

            if (parent.height() <= self.options.size) {
                parent.height(self.options.size + self.options.size / 2).attr("data-height-was-modified", "true");
            }
        }

        // location envelope
        if (parent.css("position") != "relative") {           
            self._envelope.css("height", parent.height()).css("width", parent.width()).css("top", offset.top).css("left", offset.left);
        }

        // options for envelope
        if (self.options.backgroundColor) {
            self._envelope.css("background-color", self.options.backgroundColor);
        }

        // location container
        self._container.css("top", "calc(50% - " + self.options.size / 2 + "px)");
        self._container.css("left", "calc(50% - " + self.options.size / 2 + "px)");

        // resize and mode indicator
        self._indicator
            .css("width", self.options.size)
            .css("height", self.options.size)
            .addClass(self.options.spinners + "-spinner");

        // location message
        self._message
            .css("left", "calc(50% - " + self._message.width() / 2 + "px)")
            .css("top", "calc(50% - " + self._message.height() / 2 + "px)");

        self._envelope.css("visibility", "visible"); 
    }

    this.setMessage = function(newMessage) {
        self.options.message = newMessage;
        self._message.text(self.options.message);
    }

    // private functions
    this._dispose = function() {
        $(self._message).unbind().remove();
        $(self._indicator).unbind().remove();
        $(self._container).unbind().remove();
        $(self._envelope).unbind().remove();
    }

    this._render = function() {
        if (self.options.context) {
            if (self.options.context instanceof HTMLElement) {
                if ($(self.options.context).has(self._envelope).length) self._dispose();
                $(self.options.context).append(self._envelope);
            } else if (typeof self.options.context == "string") {
                if ($("#" + self.options.context).has(self._envelope).length) self._dispose();
                $("#" + self.options.context).append(self._envelope);
            }


        } else {
            if ($(".sticky-wrapper").has(self._envelope).length) self._dispose();
            $(".sticky-wrapper").append(self._envelope);
            self._container.css("position", "fixed");
        }
    }

    this._render();
}


// OVERWRITE
// ---------

/**
 * Online edit url override: generate correct address even in absense of site information.
 */
Alfresco.util.onlineEditUrl = function(vtiServer, location)
{
   // Thor: used by overridden JS to place the tenant domain into the URL.
   var tenant = location.tenant ? location.tenant : "";
   // if site is available:
   if(location.site && location.container) {
      var onlineEditUrl = vtiServer.host + ":" + vtiServer.port + "/" +
         Alfresco.util.combinePaths(vtiServer.contextPath, tenant, location.site ? location.site.name : "", location.container ? location.container.name : "", location.path.replace(/#/g,"%23"), location.file.replace(/#/g,"%23"));
   } else { 
      // assume, that first folder in path is /Sites/
      var onlineEditUrl = vtiServer.host + ":" + vtiServer.port + "/" +
         Alfresco.util.combinePaths(vtiServer.contextPath, tenant, location.path.replace(/^\/[^\/]+\//, "/").replace(/#/g,"%23"), location.file.replace(/#/g,"%23"));
   }
   if (!(/^(http|https):\/\//).test(onlineEditUrl))
   {
      // Did they specify the protocol on the vti server bean?
      var protocol = vtiServer.protocol;
      if (protocol == null)
      {
         // If it's not set, assume it's the same as Share
         protocol = window.location.protocol;
         // Get it without the trailing colon, to match the vti property form
         protocol = protocol.substring(0, protocol.length-1);
      }

      // Build up the full HTTP / HTTPS URL
      onlineEditUrl = protocol + "://" + onlineEditUrl;
   }
   return onlineEditUrl;
};

Alfresco.thirdparty.toISO8601 = function() {
  var toISOString = function() {
    var _ = function(n){ return (n < 10) ? "0" + n : n; };

    return function(dateObject, options) {
       options = options || {};

       var formattedDate = [];
       var getter = options.zulu ? "getUTC" : "get";
       var date = "";

      if (options.selector != "time") {
        var year = dateObject[getter + "FullYear"]();
        date = ["0000".substr((year + "").length) + year, _(dateObject[getter + "Month"]() + 1), _(dateObject[getter + "Date"]())].join('-');
      }
      formattedDate.push(date);

      if (options.selector != "date") {
          var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
          var millis = dateObject[getter+"Milliseconds"]();

          if (options.milliseconds === undefined || options.milliseconds) {
            time += "."+ (millis < 100 ? "0" : "") + _(millis);
          }

          if (options.zulu) {
            time += "Z";
          } else if (options.selector != "time" && !options.hideTimezone) {
            var timezoneOffset = dateObject.getTimezoneOffset();
            var absOffset = Math.abs(timezoneOffset);
            time += (timezoneOffset > 0 ? "-" : "+") + _(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
          }
          formattedDate.push(time);
      }
      
      return formattedDate.join('T'); // String
    };
  }();

  return toISOString.apply(arguments.callee, arguments);
};
