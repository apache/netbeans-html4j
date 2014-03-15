// Resource knockout-2.2.1.js included by org.netbeans.html.ko4j.Knockout
// Knockout JavaScript library v2.2.1
// (c) Steven Sanderson - http://knockoutjs.com/
// License: MIT (http://www.opensource.org/licenses/mit-license.php)

(function(){
var DEBUG=true;
(function(window,document,navigator,jQuery,undefined){
!function(factory) {
    // Support three module loading scenarios
    if (typeof require === 'function' && typeof exports === 'object' && typeof module === 'object') {
        // [1] CommonJS/Node.js
        var target = module['exports'] || exports; // module.exports is for Node.js
        factory(target);
    } else if (typeof define === 'function' && define['amd']) {
        // [2] AMD anonymous module
        define(['exports'], factory);
    } else {
        // [3] No module loader (plain <script> tag) - put directly in global namespace
        factory(window['ko'] = {});
    }
}(function(koExports){
// Internally, all KO objects are attached to koExports (even the non-exported ones whose names will be minified by the closure compiler).
// In the future, the following "ko" variable may be made distinct from "koExports" so that private objects are not externally reachable.
var ko = typeof koExports !== 'undefined' ? koExports : {};
// Google Closure Compiler helpers (used only to make the minified file smaller)
ko.exportSymbol = function(koPath, object) {
	var tokens = koPath.split(".");

	// In the future, "ko" may become distinct from "koExports" (so that non-exported objects are not reachable)
	// At that point, "target" would be set to: (typeof koExports !== "undefined" ? koExports : ko)
	var target = ko;

	for (var i = 0; i < tokens.length - 1; i++)
		target = target[tokens[i]];
	target[tokens[tokens.length - 1]] = object;
};
ko.exportProperty = function(owner, publicName, object) {
  owner[publicName] = object;
};
ko.version = "2.2.1";

ko.exportSymbol('version', ko.version);
ko.utils = new (function () {
    var stringTrimRegex = /^(\s|\u00A0)+|(\s|\u00A0)+$/g;

    // Represent the known event types in a compact way, then at runtime transform it into a hash with event name as key (for fast lookup)
    var knownEvents = {}, knownEventTypesByEventName = {};
    var keyEventTypeName = /Firefox\/2/i.test(navigator.userAgent) ? 'KeyboardEvent' : 'UIEvents';
    knownEvents[keyEventTypeName] = ['keyup', 'keydown', 'keypress'];
    knownEvents['MouseEvents'] = ['click', 'dblclick', 'mousedown', 'mouseup', 'mousemove', 'mouseover', 'mouseout', 'mouseenter', 'mouseleave'];
    for (var eventType in knownEvents) {
        var knownEventsForType = knownEvents[eventType];
        if (knownEventsForType.length) {
            for (var i = 0, j = knownEventsForType.length; i < j; i++)
                knownEventTypesByEventName[knownEventsForType[i]] = eventType;
        }
    }
    var eventsThatMustBeRegisteredUsingAttachEvent = { 'propertychange': true }; // Workaround for an IE9 issue - https://github.com/SteveSanderson/knockout/issues/406

    // Detect IE versions for bug workarounds (uses IE conditionals, not UA string, for robustness)
    // Note that, since IE 10 does not support conditional comments, the following logic only detects IE < 10.
    // Currently this is by design, since IE 10+ behaves correctly when treated as a standard browser.
    // If there is a future need to detect specific versions of IE10+, we will amend this.
    var ieVersion = (function() {
        var version = 3, div = document.createElement('div'), iElems = div.getElementsByTagName('i');

        // Keep constructing conditional HTML blocks until we hit one that resolves to an empty fragment
        while (
            div.innerHTML = '<!--[if gt IE ' + (++version) + ']><i></i><![endif]-->',
            iElems[0]
        );
        return version > 4 ? version : undefined;
    }());
    var isIe6 = ieVersion === 6,
        isIe7 = ieVersion === 7;

    function isClickOnCheckableElement(element, eventType) {
        if ((ko.utils.tagNameLower(element) !== "input") || !element.type) return false;
        if (eventType.toLowerCase() != "click") return false;
        var inputType = element.type;
        return (inputType == "checkbox") || (inputType == "radio");
    }

    return {
        fieldsIncludedWithJsonPost: ['authenticity_token', /^__RequestVerificationToken(_.*)?$/],

        arrayForEach: function (array, action) {
            for (var i = 0, j = array.length; i < j; i++)
                action(array[i]);
        },

        arrayIndexOf: function (array, item) {
            if (typeof Array.prototype.indexOf == "function")
                return Array.prototype.indexOf.call(array, item);
            for (var i = 0, j = array.length; i < j; i++)
                if (array[i] === item)
                    return i;
            return -1;
        },

        arrayFirst: function (array, predicate, predicateOwner) {
            for (var i = 0, j = array.length; i < j; i++)
                if (predicate.call(predicateOwner, array[i]))
                    return array[i];
            return null;
        },

        arrayRemoveItem: function (array, itemToRemove) {
            var index = ko.utils.arrayIndexOf(array, itemToRemove);
            if (index >= 0)
                array.splice(index, 1);
        },

        arrayGetDistinctValues: function (array) {
            array = array || [];
            var result = [];
            for (var i = 0, j = array.length; i < j; i++) {
                if (ko.utils.arrayIndexOf(result, array[i]) < 0)
                    result.push(array[i]);
            }
            return result;
        },

        arrayMap: function (array, mapping) {
            array = array || [];
            var result = [];
            for (var i = 0, j = array.length; i < j; i++)
                result.push(mapping(array[i]));
            return result;
        },

        arrayFilter: function (array, predicate) {
            array = array || [];
            var result = [];
            for (var i = 0, j = array.length; i < j; i++)
                if (predicate(array[i]))
                    result.push(array[i]);
            return result;
        },

        arrayPushAll: function (array, valuesToPush) {
            if (valuesToPush instanceof Array)
                array.push.apply(array, valuesToPush);
            else
                for (var i = 0, j = valuesToPush.length; i < j; i++)
                    array.push(valuesToPush[i]);
            return array;
        },

        extend: function (target, source) {
            if (source) {
                for(var prop in source) {
                    if(source.hasOwnProperty(prop)) {
                        target[prop] = source[prop];
                    }
                }
            }
            return target;
        },

        emptyDomNode: function (domNode) {
            while (domNode.firstChild) {
                ko.removeNode(domNode.firstChild);
            }
        },

        moveCleanedNodesToContainerElement: function(nodes) {
            // Ensure it's a real array, as we're about to reparent the nodes and
            // we don't want the underlying collection to change while we're doing that.
            var nodesArray = ko.utils.makeArray(nodes);

            var container = document.createElement('div');
            for (var i = 0, j = nodesArray.length; i < j; i++) {
                container.appendChild(ko.cleanNode(nodesArray[i]));
            }
            return container;
        },

        cloneNodes: function (nodesArray, shouldCleanNodes) {
            for (var i = 0, j = nodesArray.length, newNodesArray = []; i < j; i++) {
                var clonedNode = nodesArray[i].cloneNode(true);
                newNodesArray.push(shouldCleanNodes ? ko.cleanNode(clonedNode) : clonedNode);
            }
            return newNodesArray;
        },

        setDomNodeChildren: function (domNode, childNodes) {
            ko.utils.emptyDomNode(domNode);
            if (childNodes) {
                for (var i = 0, j = childNodes.length; i < j; i++)
                    domNode.appendChild(childNodes[i]);
            }
        },

        replaceDomNodes: function (nodeToReplaceOrNodeArray, newNodesArray) {
            var nodesToReplaceArray = nodeToReplaceOrNodeArray.nodeType ? [nodeToReplaceOrNodeArray] : nodeToReplaceOrNodeArray;
            if (nodesToReplaceArray.length > 0) {
                var insertionPoint = nodesToReplaceArray[0];
                var parent = insertionPoint.parentNode;
                for (var i = 0, j = newNodesArray.length; i < j; i++)
                    parent.insertBefore(newNodesArray[i], insertionPoint);
                for (var i = 0, j = nodesToReplaceArray.length; i < j; i++) {
                    ko.removeNode(nodesToReplaceArray[i]);
                }
            }
        },

        setOptionNodeSelectionState: function (optionNode, isSelected) {
            // IE6 sometimes throws "unknown error" if you try to write to .selected directly, whereas Firefox struggles with setAttribute. Pick one based on browser.
            if (ieVersion < 7)
                optionNode.setAttribute("selected", isSelected);
            else
                optionNode.selected = isSelected;
        },

        stringTrim: function (string) {
            return (string || "").replace(stringTrimRegex, "");
        },

        stringTokenize: function (string, delimiter) {
            var result = [];
            var tokens = (string || "").split(delimiter);
            for (var i = 0, j = tokens.length; i < j; i++) {
                var trimmed = ko.utils.stringTrim(tokens[i]);
                if (trimmed !== "")
                    result.push(trimmed);
            }
            return result;
        },

        stringStartsWith: function (string, startsWith) {
            string = string || "";
            if (startsWith.length > string.length)
                return false;
            return string.substring(0, startsWith.length) === startsWith;
        },

        domNodeIsContainedBy: function (node, containedByNode) {
            if (containedByNode.compareDocumentPosition)
                return (containedByNode.compareDocumentPosition(node) & 16) == 16;
            while (node != null) {
                if (node == containedByNode)
                    return true;
                node = node.parentNode;
            }
            return false;
        },

        domNodeIsAttachedToDocument: function (node) {
            return ko.utils.domNodeIsContainedBy(node, node.ownerDocument);
        },

        tagNameLower: function(element) {
            // For HTML elements, tagName will always be upper case; for XHTML elements, it'll be lower case.
            // Possible future optimization: If we know it's an element from an XHTML document (not HTML),
            // we don't need to do the .toLowerCase() as it will always be lower case anyway.
            return element && element.tagName && element.tagName.toLowerCase();
        },

        registerEventHandler: function (element, eventType, handler) {
            var mustUseAttachEvent = ieVersion && eventsThatMustBeRegisteredUsingAttachEvent[eventType];
            if (!mustUseAttachEvent && typeof jQuery != "undefined") {
                if (isClickOnCheckableElement(element, eventType)) {
                    // For click events on checkboxes, jQuery interferes with the event handling in an awkward way:
                    // it toggles the element checked state *after* the click event handlers run, whereas native
                    // click events toggle the checked state *before* the event handler.
                    // Fix this by intecepting the handler and applying the correct checkedness before it runs.
                    var originalHandler = handler;
                    handler = function(event, eventData) {
                        var jQuerySuppliedCheckedState = this.checked;
                        if (eventData)
                            this.checked = eventData.checkedStateBeforeEvent !== true;
                        originalHandler.call(this, event);
                        this.checked = jQuerySuppliedCheckedState; // Restore the state jQuery applied
                    };
                }
                jQuery(element)['bind'](eventType, handler);
            } else if (!mustUseAttachEvent && typeof element.addEventListener == "function")
                element.addEventListener(eventType, handler, false);
            else if (typeof element.attachEvent != "undefined")
                element.attachEvent("on" + eventType, function (event) {
                    handler.call(element, event);
                });
            else
                throw new Error("Browser doesn't support addEventListener or attachEvent");
        },

        triggerEvent: function (element, eventType) {
            if (!(element && element.nodeType))
                throw new Error("element must be a DOM node when calling triggerEvent");

            if (typeof jQuery != "undefined") {
                var eventData = [];
                if (isClickOnCheckableElement(element, eventType)) {
                    // Work around the jQuery "click events on checkboxes" issue described above by storing the original checked state before triggering the handler
                    eventData.push({ checkedStateBeforeEvent: element.checked });
                }
                jQuery(element)['trigger'](eventType, eventData);
            } else if (typeof document.createEvent == "function") {
                if (typeof element.dispatchEvent == "function") {
                    var eventCategory = knownEventTypesByEventName[eventType] || "HTMLEvents";
                    var event = document.createEvent(eventCategory);
                    event.initEvent(eventType, true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, element);
                    element.dispatchEvent(event);
                }
                else
                    throw new Error("The supplied element doesn't support dispatchEvent");
            } else if (typeof element.fireEvent != "undefined") {
                // Unlike other browsers, IE doesn't change the checked state of checkboxes/radiobuttons when you trigger their "click" event
                // so to make it consistent, we'll do it manually here
                if (isClickOnCheckableElement(element, eventType))
                    element.checked = element.checked !== true;
                element.fireEvent("on" + eventType);
            }
            else
                throw new Error("Browser doesn't support triggering events");
        },

        unwrapObservable: function (value) {
            return ko.isObservable(value) ? value() : value;
        },

        peekObservable: function (value) {
            return ko.isObservable(value) ? value.peek() : value;
        },

        toggleDomNodeCssClass: function (node, classNames, shouldHaveClass) {
            if (classNames) {
                var cssClassNameRegex = /[\w-]+/g,
                    currentClassNames = node.className.match(cssClassNameRegex) || [];
                ko.utils.arrayForEach(classNames.match(cssClassNameRegex), function(className) {
                    var indexOfClass = ko.utils.arrayIndexOf(currentClassNames, className);
                    if (indexOfClass >= 0) {
                        if (!shouldHaveClass)
                            currentClassNames.splice(indexOfClass, 1);
                    } else {
                        if (shouldHaveClass)
                            currentClassNames.push(className);
                    }
                });
                node.className = currentClassNames.join(" ");
            }
        },

        setTextContent: function(element, textContent) {
            var value = ko.utils.unwrapObservable(textContent);
            if ((value === null) || (value === undefined))
                value = "";

            if (element.nodeType === 3) {
                element.data = value;
            } else {
                // We need there to be exactly one child: a text node.
                // If there are no children, more than one, or if it's not a text node,
                // we'll clear everything and create a single text node.
                var innerTextNode = ko.virtualElements.firstChild(element);
                if (!innerTextNode || innerTextNode.nodeType != 3 || ko.virtualElements.nextSibling(innerTextNode)) {
                    ko.virtualElements.setDomNodeChildren(element, [document.createTextNode(value)]);
                } else {
                    innerTextNode.data = value;
                }

                ko.utils.forceRefresh(element);
            }
        },

        setElementName: function(element, name) {
            element.name = name;

            // Workaround IE 6/7 issue
            // - https://github.com/SteveSanderson/knockout/issues/197
            // - http://www.matts411.com/post/setting_the_name_attribute_in_ie_dom/
            if (ieVersion <= 7) {
                try {
                    element.mergeAttributes(document.createElement("<input name='" + element.name + "'/>"), false);
                }
                catch(e) {} // For IE9 with doc mode "IE9 Standards" and browser mode "IE9 Compatibility View"
            }
        },

        forceRefresh: function(node) {
            // Workaround for an IE9 rendering bug - https://github.com/SteveSanderson/knockout/issues/209
            if (ieVersion >= 9) {
                // For text nodes and comment nodes (most likely virtual elements), we will have to refresh the container
                var elem = node.nodeType == 1 ? node : node.parentNode;
                if (elem.style)
                    elem.style.zoom = elem.style.zoom;
            }
        },

        ensureSelectElementIsRenderedCorrectly: function(selectElement) {
            // Workaround for IE9 rendering bug - it doesn't reliably display all the text in dynamically-added select boxes unless you force it to re-render by updating the width.
            // (See https://github.com/SteveSanderson/knockout/issues/312, http://stackoverflow.com/questions/5908494/select-only-shows-first-char-of-selected-option)
            if (ieVersion >= 9) {
                var originalWidth = selectElement.style.width;
                selectElement.style.width = 0;
                selectElement.style.width = originalWidth;
            }
        },

        range: function (min, max) {
            min = ko.utils.unwrapObservable(min);
            max = ko.utils.unwrapObservable(max);
            var result = [];
            for (var i = min; i <= max; i++)
                result.push(i);
            return result;
        },

        makeArray: function(arrayLikeObject) {
            var result = [];
            for (var i = 0, j = arrayLikeObject.length; i < j; i++) {
                result.push(arrayLikeObject[i]);
            };
            return result;
        },

        isIe6 : isIe6,
        isIe7 : isIe7,
        ieVersion : ieVersion,

        getFormFields: function(form, fieldName) {
            var fields = ko.utils.makeArray(form.getElementsByTagName("input")).concat(ko.utils.makeArray(form.getElementsByTagName("textarea")));
            var isMatchingField = (typeof fieldName == 'string')
                ? function(field) { return field.name === fieldName }
                : function(field) { return fieldName.test(field.name) }; // Treat fieldName as regex or object containing predicate
            var matches = [];
            for (var i = fields.length - 1; i >= 0; i--) {
                if (isMatchingField(fields[i]))
                    matches.push(fields[i]);
            };
            return matches;
        },

        parseJson: function (jsonString) {
            if (typeof jsonString == "string") {
                jsonString = ko.utils.stringTrim(jsonString);
                if (jsonString) {
                    if (window.JSON && window.JSON.parse) // Use native parsing where available
                        return window.JSON.parse(jsonString);
                    return (new Function("return " + jsonString))(); // Fallback on less safe parsing for older browsers
                }
            }
            return null;
        },

        stringifyJson: function (data, replacer, space) {   // replacer and space are optional
            if ((typeof JSON == "undefined") || (typeof JSON.stringify == "undefined"))
                throw new Error("Cannot find JSON.stringify(). Some browsers (e.g., IE < 8) don't support it natively, but you can overcome this by adding a script reference to json2.js, downloadable from http://www.json.org/json2.js");
            return JSON.stringify(ko.utils.unwrapObservable(data), replacer, space);
        },

        postJson: function (urlOrForm, data, options) {
            options = options || {};
            var params = options['params'] || {};
            var includeFields = options['includeFields'] || this.fieldsIncludedWithJsonPost;
            var url = urlOrForm;

            // If we were given a form, use its 'action' URL and pick out any requested field values
            if((typeof urlOrForm == 'object') && (ko.utils.tagNameLower(urlOrForm) === "form")) {
                var originalForm = urlOrForm;
                url = originalForm.action;
                for (var i = includeFields.length - 1; i >= 0; i--) {
                    var fields = ko.utils.getFormFields(originalForm, includeFields[i]);
                    for (var j = fields.length - 1; j >= 0; j--)
                        params[fields[j].name] = fields[j].value;
                }
            }

            data = ko.utils.unwrapObservable(data);
            var form = document.createElement("form");
            form.style.display = "none";
            form.action = url;
            form.method = "post";
            for (var key in data) {
                var input = document.createElement("input");
                input.name = key;
                input.value = ko.utils.stringifyJson(ko.utils.unwrapObservable(data[key]));
                form.appendChild(input);
            }
            for (var key in params) {
                var input = document.createElement("input");
                input.name = key;
                input.value = params[key];
                form.appendChild(input);
            }
            document.body.appendChild(form);
            options['submitter'] ? options['submitter'](form) : form.submit();
            setTimeout(function () { form.parentNode.removeChild(form); }, 0);
        }
    }
})();

ko.exportSymbol('utils', ko.utils);
ko.exportSymbol('utils.arrayForEach', ko.utils.arrayForEach);
ko.exportSymbol('utils.arrayFirst', ko.utils.arrayFirst);
ko.exportSymbol('utils.arrayFilter', ko.utils.arrayFilter);
ko.exportSymbol('utils.arrayGetDistinctValues', ko.utils.arrayGetDistinctValues);
ko.exportSymbol('utils.arrayIndexOf', ko.utils.arrayIndexOf);
ko.exportSymbol('utils.arrayMap', ko.utils.arrayMap);
ko.exportSymbol('utils.arrayPushAll', ko.utils.arrayPushAll);
ko.exportSymbol('utils.arrayRemoveItem', ko.utils.arrayRemoveItem);
ko.exportSymbol('utils.extend', ko.utils.extend);
ko.exportSymbol('utils.fieldsIncludedWithJsonPost', ko.utils.fieldsIncludedWithJsonPost);
ko.exportSymbol('utils.getFormFields', ko.utils.getFormFields);
ko.exportSymbol('utils.peekObservable', ko.utils.peekObservable);
ko.exportSymbol('utils.postJson', ko.utils.postJson);
ko.exportSymbol('utils.parseJson', ko.utils.parseJson);
ko.exportSymbol('utils.registerEventHandler', ko.utils.registerEventHandler);
ko.exportSymbol('utils.stringifyJson', ko.utils.stringifyJson);
ko.exportSymbol('utils.range', ko.utils.range);
ko.exportSymbol('utils.toggleDomNodeCssClass', ko.utils.toggleDomNodeCssClass);
ko.exportSymbol('utils.triggerEvent', ko.utils.triggerEvent);
ko.exportSymbol('utils.unwrapObservable', ko.utils.unwrapObservable);

if (!Function.prototype['bind']) {
    // Function.prototype.bind is a standard part of ECMAScript 5th Edition (December 2009, http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-262.pdf)
    // In case the browser doesn't implement it natively, provide a JavaScript implementation. This implementation is based on the one in prototype.js
    Function.prototype['bind'] = function (object) {
        var originalFunction = this, args = Array.prototype.slice.call(arguments), object = args.shift();
        return function () {
            return originalFunction.apply(object, args.concat(Array.prototype.slice.call(arguments)));
        };
    };
}

ko.utils.domData = new (function () {
    var uniqueId = 0;
    var dataStoreKeyExpandoPropertyName = "__ko__" + (new Date).getTime();
    var dataStore = {};
    return {
        get: function (node, key) {
            var allDataForNode = ko.utils.domData.getAll(node, false);
            return allDataForNode === undefined ? undefined : allDataForNode[key];
        },
        set: function (node, key, value) {
            if (value === undefined) {
                // Make sure we don't actually create a new domData key if we are actually deleting a value
                if (ko.utils.domData.getAll(node, false) === undefined)
                    return;
            }
            var allDataForNode = ko.utils.domData.getAll(node, true);
            allDataForNode[key] = value;
        },
        getAll: function (node, createIfNotFound) {
            var dataStoreKey = node[dataStoreKeyExpandoPropertyName];
            var hasExistingDataStore = dataStoreKey && (dataStoreKey !== "null") && dataStore[dataStoreKey];
            if (!hasExistingDataStore) {
                if (!createIfNotFound)
                    return undefined;
                dataStoreKey = node[dataStoreKeyExpandoPropertyName] = "ko" + uniqueId++;
                dataStore[dataStoreKey] = {};
            }
            return dataStore[dataStoreKey];
        },
        clear: function (node) {
            var dataStoreKey = node[dataStoreKeyExpandoPropertyName];
            if (dataStoreKey) {
                delete dataStore[dataStoreKey];
                node[dataStoreKeyExpandoPropertyName] = null;
                return true; // Exposing "did clean" flag purely so specs can infer whether things have been cleaned up as intended
            }
            return false;
        }
    }
})();

ko.exportSymbol('utils.domData', ko.utils.domData);
ko.exportSymbol('utils.domData.clear', ko.utils.domData.clear); // Exporting only so specs can clear up after themselves fully

ko.utils.domNodeDisposal = new (function () {
    var domDataKey = "__ko_domNodeDisposal__" + (new Date).getTime();
    var cleanableNodeTypes = { 1: true, 8: true, 9: true };       // Element, Comment, Document
    var cleanableNodeTypesWithDescendants = { 1: true, 9: true }; // Element, Document

    function getDisposeCallbacksCollection(node, createIfNotFound) {
        var allDisposeCallbacks = ko.utils.domData.get(node, domDataKey);
        if ((allDisposeCallbacks === undefined) && createIfNotFound) {
            allDisposeCallbacks = [];
            ko.utils.domData.set(node, domDataKey, allDisposeCallbacks);
        }
        return allDisposeCallbacks;
    }
    function destroyCallbacksCollection(node) {
        ko.utils.domData.set(node, domDataKey, undefined);
    }

    function cleanSingleNode(node) {
        // Run all the dispose callbacks
        var callbacks = getDisposeCallbacksCollection(node, false);
        if (callbacks) {
            callbacks = callbacks.slice(0); // Clone, as the array may be modified during iteration (typically, callbacks will remove themselves)
            for (var i = 0; i < callbacks.length; i++)
                callbacks[i](node);
        }

        // Also erase the DOM data
        ko.utils.domData.clear(node);

        // Special support for jQuery here because it's so commonly used.
        // Many jQuery plugins (including jquery.tmpl) store data using jQuery's equivalent of domData
        // so notify it to tear down any resources associated with the node & descendants here.
        if ((typeof jQuery == "function") && (typeof jQuery['cleanData'] == "function"))
            jQuery['cleanData']([node]);

        // Also clear any immediate-child comment nodes, as these wouldn't have been found by
        // node.getElementsByTagName("*") in cleanNode() (comment nodes aren't elements)
        if (cleanableNodeTypesWithDescendants[node.nodeType])
            cleanImmediateCommentTypeChildren(node);
    }

    function cleanImmediateCommentTypeChildren(nodeWithChildren) {
        var child, nextChild = nodeWithChildren.firstChild;
        while (child = nextChild) {
            nextChild = child.nextSibling;
            if (child.nodeType === 8)
                cleanSingleNode(child);
        }
    }

    return {
        addDisposeCallback : function(node, callback) {
            if (typeof callback != "function")
                throw new Error("Callback must be a function");
            getDisposeCallbacksCollection(node, true).push(callback);
        },

        removeDisposeCallback : function(node, callback) {
            var callbacksCollection = getDisposeCallbacksCollection(node, false);
            if (callbacksCollection) {
                ko.utils.arrayRemoveItem(callbacksCollection, callback);
                if (callbacksCollection.length == 0)
                    destroyCallbacksCollection(node);
            }
        },

        cleanNode : function(node) {
            // First clean this node, where applicable
            if (cleanableNodeTypes[node.nodeType]) {
                cleanSingleNode(node);

                // ... then its descendants, where applicable
                if (cleanableNodeTypesWithDescendants[node.nodeType]) {
                    // Clone the descendants list in case it changes during iteration
                    var descendants = [];
                    ko.utils.arrayPushAll(descendants, node.getElementsByTagName("*"));
                    for (var i = 0, j = descendants.length; i < j; i++)
                        cleanSingleNode(descendants[i]);
                }
            }
            return node;
        },

        removeNode : function(node) {
            ko.cleanNode(node);
            if (node.parentNode)
                node.parentNode.removeChild(node);
        }
    }
})();
ko.cleanNode = ko.utils.domNodeDisposal.cleanNode; // Shorthand name for convenience
ko.removeNode = ko.utils.domNodeDisposal.removeNode; // Shorthand name for convenience
ko.exportSymbol('cleanNode', ko.cleanNode);
ko.exportSymbol('removeNode', ko.removeNode);
ko.exportSymbol('utils.domNodeDisposal', ko.utils.domNodeDisposal);
ko.exportSymbol('utils.domNodeDisposal.addDisposeCallback', ko.utils.domNodeDisposal.addDisposeCallback);
ko.exportSymbol('utils.domNodeDisposal.removeDisposeCallback', ko.utils.domNodeDisposal.removeDisposeCallback);
(function () {
    var leadingCommentRegex = /^(\s*)<!--(.*?)-->/;

    function simpleHtmlParse(html) {
        // Based on jQuery's "clean" function, but only accounting for table-related elements.
        // If you have referenced jQuery, this won't be used anyway - KO will use jQuery's "clean" function directly

        // Note that there's still an issue in IE < 9 whereby it will discard comment nodes that are the first child of
        // a descendant node. For example: "<div><!-- mycomment -->abc</div>" will get parsed as "<div>abc</div>"
        // This won't affect anyone who has referenced jQuery, and there's always the workaround of inserting a dummy node
        // (possibly a text node) in front of the comment. So, KO does not attempt to workaround this IE issue automatically at present.

        // Trim whitespace, otherwise indexOf won't work as expected
        var tags = ko.utils.stringTrim(html).toLowerCase(), div = document.createElement("div");

        // Finds the first match from the left column, and returns the corresponding "wrap" data from the right column
        var wrap = tags.match(/^<(thead|tbody|tfoot)/)              && [1, "<table>", "</table>"] ||
                   !tags.indexOf("<tr")                             && [2, "<table><tbody>", "</tbody></table>"] ||
                   (!tags.indexOf("<td") || !tags.indexOf("<th"))   && [3, "<table><tbody><tr>", "</tr></tbody></table>"] ||
                   /* anything else */                                 [0, "", ""];

        // Go to html and back, then peel off extra wrappers
        // Note that we always prefix with some dummy text, because otherwise, IE<9 will strip out leading comment nodes in descendants. Total madness.
        var markup = "ignored<div>" + wrap[1] + html + wrap[2] + "</div>";
        if (typeof window['innerShiv'] == "function") {
            div.appendChild(window['innerShiv'](markup));
        } else {
            div.innerHTML = markup;
        }

        // Move to the right depth
        while (wrap[0]--)
            div = div.lastChild;

        return ko.utils.makeArray(div.lastChild.childNodes);
    }

    function jQueryHtmlParse(html) {
        // jQuery's "parseHTML" function was introduced in jQuery 1.8.0 and is a documented public API.
        if (jQuery['parseHTML']) {
            return jQuery['parseHTML'](html);
        } else {
            // For jQuery < 1.8.0, we fall back on the undocumented internal "clean" function.
            var elems = jQuery['clean']([html]);

            // As of jQuery 1.7.1, jQuery parses the HTML by appending it to some dummy parent nodes held in an in-memory document fragment.
            // Unfortunately, it never clears the dummy parent nodes from the document fragment, so it leaks memory over time.
            // Fix this by finding the top-most dummy parent element, and detaching it from its owner fragment.
            if (elems && elems[0]) {
                // Find the top-most parent element that's a direct child of a document fragment
                var elem = elems[0];
                while (elem.parentNode && elem.parentNode.nodeType !== 11 /* i.e., DocumentFragment */)
                    elem = elem.parentNode;
                // ... then detach it
                if (elem.parentNode)
                    elem.parentNode.removeChild(elem);
            }

            return elems;
        }
    }

    ko.utils.parseHtmlFragment = function(html) {
        return typeof jQuery != 'undefined' ? jQueryHtmlParse(html)   // As below, benefit from jQuery's optimisations where possible
                                            : simpleHtmlParse(html);  // ... otherwise, this simple logic will do in most common cases.
    };

    ko.utils.setHtml = function(node, html) {
        ko.utils.emptyDomNode(node);

        // There's no legitimate reason to display a stringified observable without unwrapping it, so we'll unwrap it
        html = ko.utils.unwrapObservable(html);

        if ((html !== null) && (html !== undefined)) {
            if (typeof html != 'string')
                html = html.toString();

            // jQuery contains a lot of sophisticated code to parse arbitrary HTML fragments,
            // for example <tr> elements which are not normally allowed to exist on their own.
            // If you've referenced jQuery we'll use that rather than duplicating its code.
            if (typeof jQuery != 'undefined') {
                jQuery(node)['html'](html);
            } else {
                // ... otherwise, use KO's own parsing logic.
                var parsedNodes = ko.utils.parseHtmlFragment(html);
                for (var i = 0; i < parsedNodes.length; i++)
                    node.appendChild(parsedNodes[i]);
            }
        }
    };
})();

ko.exportSymbol('utils.parseHtmlFragment', ko.utils.parseHtmlFragment);
ko.exportSymbol('utils.setHtml', ko.utils.setHtml);

ko.memoization = (function () {
    var memos = {};

    function randomMax8HexChars() {
        return (((1 + Math.random()) * 0x100000000) | 0).toString(16).substring(1);
    }
    function generateRandomId() {
        return randomMax8HexChars() + randomMax8HexChars();
    }
    function findMemoNodes(rootNode, appendToArray) {
        if (!rootNode)
            return;
        if (rootNode.nodeType == 8) {
            var memoId = ko.memoization.parseMemoText(rootNode.nodeValue);
            if (memoId != null)
                appendToArray.push({ domNode: rootNode, memoId: memoId });
        } else if (rootNode.nodeType == 1) {
            for (var i = 0, childNodes = rootNode.childNodes, j = childNodes.length; i < j; i++)
                findMemoNodes(childNodes[i], appendToArray);
        }
    }

    return {
        memoize: function (callback) {
            if (typeof callback != "function")
                throw new Error("You can only pass a function to ko.memoization.memoize()");
            var memoId = generateRandomId();
            memos[memoId] = callback;
            return "<!--[ko_memo:" + memoId + "]-->";
        },

        unmemoize: function (memoId, callbackParams) {
            var callback = memos[memoId];
            if (callback === undefined)
                throw new Error("Couldn't find any memo with ID " + memoId + ". Perhaps it's already been unmemoized.");
            try {
                callback.apply(null, callbackParams || []);
                return true;
            }
            finally { delete memos[memoId]; }
        },

        unmemoizeDomNodeAndDescendants: function (domNode, extraCallbackParamsArray) {
            var memos = [];
            findMemoNodes(domNode, memos);
            for (var i = 0, j = memos.length; i < j; i++) {
                var node = memos[i].domNode;
                var combinedParams = [node];
                if (extraCallbackParamsArray)
                    ko.utils.arrayPushAll(combinedParams, extraCallbackParamsArray);
                ko.memoization.unmemoize(memos[i].memoId, combinedParams);
                node.nodeValue = ""; // Neuter this node so we don't try to unmemoize it again
                if (node.parentNode)
                    node.parentNode.removeChild(node); // If possible, erase it totally (not always possible - someone else might just hold a reference to it then call unmemoizeDomNodeAndDescendants again)
            }
        },

        parseMemoText: function (memoText) {
            var match = memoText.match(/^\[ko_memo\:(.*?)\]$/);
            return match ? match[1] : null;
        }
    };
})();

ko.exportSymbol('memoization', ko.memoization);
ko.exportSymbol('memoization.memoize', ko.memoization.memoize);
ko.exportSymbol('memoization.unmemoize', ko.memoization.unmemoize);
ko.exportSymbol('memoization.parseMemoText', ko.memoization.parseMemoText);
ko.exportSymbol('memoization.unmemoizeDomNodeAndDescendants', ko.memoization.unmemoizeDomNodeAndDescendants);
ko.extenders = {
    'throttle': function(target, timeout) {
        // Throttling means two things:

        // (1) For dependent observables, we throttle *evaluations* so that, no matter how fast its dependencies
        //     notify updates, the target doesn't re-evaluate (and hence doesn't notify) faster than a certain rate
        target['throttleEvaluation'] = timeout;

        // (2) For writable targets (observables, or writable dependent observables), we throttle *writes*
        //     so the target cannot change value synchronously or faster than a certain rate
        var writeTimeoutInstance = null;
        return ko.dependentObservable({
            'read': target,
            'write': function(value) {
                clearTimeout(writeTimeoutInstance);
                writeTimeoutInstance = setTimeout(function() {
                    target(value);
                }, timeout);
            }
        });
    },

    'notify': function(target, notifyWhen) {
        target["equalityComparer"] = notifyWhen == "always"
            ? function() { return false } // Treat all values as not equal
            : ko.observable["fn"]["equalityComparer"];
        return target;
    }
};

function applyExtenders(requestedExtenders) {
    var target = this;
    if (requestedExtenders) {
        for (var key in requestedExtenders) {
            var extenderHandler = ko.extenders[key];
            if (typeof extenderHandler == 'function') {
                target = extenderHandler(target, requestedExtenders[key]);
            }
        }
    }
    return target;
}

ko.exportSymbol('extenders', ko.extenders);

ko.subscription = function (target, callback, disposeCallback) {
    this.target = target;
    this.callback = callback;
    this.disposeCallback = disposeCallback;
    ko.exportProperty(this, 'dispose', this.dispose);
};
ko.subscription.prototype.dispose = function () {
    this.isDisposed = true;
    this.disposeCallback();
};

ko.subscribable = function () {
    this._subscriptions = {};

    ko.utils.extend(this, ko.subscribable['fn']);
    ko.exportProperty(this, 'subscribe', this.subscribe);
    ko.exportProperty(this, 'extend', this.extend);
    ko.exportProperty(this, 'getSubscriptionsCount', this.getSubscriptionsCount);
}

var defaultEvent = "change";

ko.subscribable['fn'] = {
    subscribe: function (callback, callbackTarget, event) {
        event = event || defaultEvent;
        var boundCallback = callbackTarget ? callback.bind(callbackTarget) : callback;

        var subscription = new ko.subscription(this, boundCallback, function () {
            ko.utils.arrayRemoveItem(this._subscriptions[event], subscription);
        }.bind(this));

        if (!this._subscriptions[event])
            this._subscriptions[event] = [];
        this._subscriptions[event].push(subscription);
        return subscription;
    },

    "notifySubscribers": function (valueToNotify, event) {
        event = event || defaultEvent;
        if (this._subscriptions[event]) {
            ko.dependencyDetection.ignore(function() {
                ko.utils.arrayForEach(this._subscriptions[event].slice(0), function (subscription) {
                    // In case a subscription was disposed during the arrayForEach cycle, check
                    // for isDisposed on each subscription before invoking its callback
                    if (subscription && (subscription.isDisposed !== true))
                        subscription.callback(valueToNotify);
                });
            }, this);
        }
    },

    getSubscriptionsCount: function () {
        var total = 0;
        for (var eventName in this._subscriptions) {
            if (this._subscriptions.hasOwnProperty(eventName))
                total += this._subscriptions[eventName].length;
        }
        return total;
    },

    extend: applyExtenders
};


ko.isSubscribable = function (instance) {
    return typeof instance.subscribe == "function" && typeof instance["notifySubscribers"] == "function";
};

ko.exportSymbol('subscribable', ko.subscribable);
ko.exportSymbol('isSubscribable', ko.isSubscribable);

ko.dependencyDetection = (function () {
    var _frames = [];

    return {
        begin: function (callback) {
            _frames.push({ callback: callback, distinctDependencies:[] });
        },

        end: function () {
            _frames.pop();
        },

        registerDependency: function (subscribable) {
            if (!ko.isSubscribable(subscribable))
                throw new Error("Only subscribable things can act as dependencies");
            if (_frames.length > 0) {
                var topFrame = _frames[_frames.length - 1];
                if (!topFrame || ko.utils.arrayIndexOf(topFrame.distinctDependencies, subscribable) >= 0)
                    return;
                topFrame.distinctDependencies.push(subscribable);
                topFrame.callback(subscribable);
            }
        },

        ignore: function(callback, callbackTarget, callbackArgs) {
            try {
                _frames.push(null);
                return callback.apply(callbackTarget, callbackArgs || []);
            } finally {
                _frames.pop();
            }
        }
    };
})();
var primitiveTypes = { 'undefined':true, 'boolean':true, 'number':true, 'string':true };

ko.observable = function (initialValue) {
    var _latestValue = initialValue;

    function observable() {
        if (arguments.length > 0) {
            // Write

            // Ignore writes if the value hasn't changed
            if ((!observable['equalityComparer']) || !observable['equalityComparer'](_latestValue, arguments[0])) {
                observable.valueWillMutate();
                _latestValue = arguments[0];
                if (DEBUG) observable._latestValue = _latestValue;
                observable.valueHasMutated();
            }
            return this; // Permits chained assignments
        }
        else {
            // Read
            ko.dependencyDetection.registerDependency(observable); // The caller only needs to be notified of changes if they did a "read" operation
            return _latestValue;
        }
    }
    if (DEBUG) observable._latestValue = _latestValue;
    ko.subscribable.call(observable);
    observable.peek = function() { return _latestValue };
    observable.valueHasMutated = function () { observable["notifySubscribers"](_latestValue); }
    observable.valueWillMutate = function () { observable["notifySubscribers"](_latestValue, "beforeChange"); }
    ko.utils.extend(observable, ko.observable['fn']);

    ko.exportProperty(observable, 'peek', observable.peek);
    ko.exportProperty(observable, "valueHasMutated", observable.valueHasMutated);
    ko.exportProperty(observable, "valueWillMutate", observable.valueWillMutate);

    return observable;
}

ko.observable['fn'] = {
    "equalityComparer": function valuesArePrimitiveAndEqual(a, b) {
        var oldValueIsPrimitive = (a === null) || (typeof(a) in primitiveTypes);
        return oldValueIsPrimitive ? (a === b) : false;
    }
};

var protoProperty = ko.observable.protoProperty = "__ko_proto__";
ko.observable['fn'][protoProperty] = ko.observable;

ko.hasPrototype = function(instance, prototype) {
    if ((instance === null) || (instance === undefined) || (instance[protoProperty] === undefined)) return false;
    if (instance[protoProperty] === prototype) return true;
    return ko.hasPrototype(instance[protoProperty], prototype); // Walk the prototype chain
};

ko.isObservable = function (instance) {
    return ko.hasPrototype(instance, ko.observable);
}
ko.isWriteableObservable = function (instance) {
    // Observable
    if ((typeof instance == "function") && instance[protoProperty] === ko.observable)
        return true;
    // Writeable dependent observable
    if ((typeof instance == "function") && (instance[protoProperty] === ko.dependentObservable) && (instance.hasWriteFunction))
        return true;
    // Anything else
    return false;
}


ko.exportSymbol('observable', ko.observable);
ko.exportSymbol('isObservable', ko.isObservable);
ko.exportSymbol('isWriteableObservable', ko.isWriteableObservable);
ko.observableArray = function (initialValues) {
    if (arguments.length == 0) {
        // Zero-parameter constructor initializes to empty array
        initialValues = [];
    }
    if ((initialValues !== null) && (initialValues !== undefined) && !('length' in initialValues))
        throw new Error("The argument passed when initializing an observable array must be an array, or null, or undefined.");

    var result = ko.observable(initialValues);
    ko.utils.extend(result, ko.observableArray['fn']);
    return result;
}

ko.observableArray['fn'] = {
    'remove': function (valueOrPredicate) {
        var underlyingArray = this.peek();
        var removedValues = [];
        var predicate = typeof valueOrPredicate == "function" ? valueOrPredicate : function (value) { return value === valueOrPredicate; };
        for (var i = 0; i < underlyingArray.length; i++) {
            var value = underlyingArray[i];
            if (predicate(value)) {
                if (removedValues.length === 0) {
                    this.valueWillMutate();
                }
                removedValues.push(value);
                underlyingArray.splice(i, 1);
                i--;
            }
        }
        if (removedValues.length) {
            this.valueHasMutated();
        }
        return removedValues;
    },

    'removeAll': function (arrayOfValues) {
        // If you passed zero args, we remove everything
        if (arrayOfValues === undefined) {
            var underlyingArray = this.peek();
            var allValues = underlyingArray.slice(0);
            this.valueWillMutate();
            underlyingArray.splice(0, underlyingArray.length);
            this.valueHasMutated();
            return allValues;
        }
        // If you passed an arg, we interpret it as an array of entries to remove
        if (!arrayOfValues)
            return [];
        return this['remove'](function (value) {
            return ko.utils.arrayIndexOf(arrayOfValues, value) >= 0;
        });
    },

    'destroy': function (valueOrPredicate) {
        var underlyingArray = this.peek();
        var predicate = typeof valueOrPredicate == "function" ? valueOrPredicate : function (value) { return value === valueOrPredicate; };
        this.valueWillMutate();
        for (var i = underlyingArray.length - 1; i >= 0; i--) {
            var value = underlyingArray[i];
            if (predicate(value))
                underlyingArray[i]["_destroy"] = true;
        }
        this.valueHasMutated();
    },

    'destroyAll': function (arrayOfValues) {
        // If you passed zero args, we destroy everything
        if (arrayOfValues === undefined)
            return this['destroy'](function() { return true });

        // If you passed an arg, we interpret it as an array of entries to destroy
        if (!arrayOfValues)
            return [];
        return this['destroy'](function (value) {
            return ko.utils.arrayIndexOf(arrayOfValues, value) >= 0;
        });
    },

    'indexOf': function (item) {
        var underlyingArray = this();
        return ko.utils.arrayIndexOf(underlyingArray, item);
    },

    'replace': function(oldItem, newItem) {
        var index = this['indexOf'](oldItem);
        if (index >= 0) {
            this.valueWillMutate();
            this.peek()[index] = newItem;
            this.valueHasMutated();
        }
    }
}

// Populate ko.observableArray.fn with read/write functions from native arrays
// Important: Do not add any additional functions here that may reasonably be used to *read* data from the array
// because we'll eval them without causing subscriptions, so ko.computed output could end up getting stale
ko.utils.arrayForEach(["pop", "push", "reverse", "shift", "sort", "splice", "unshift"], function (methodName) {
    ko.observableArray['fn'][methodName] = function () {
        // Use "peek" to avoid creating a subscription in any computed that we're executing in the context of
        // (for consistency with mutating regular observables)
        var underlyingArray = this.peek();
        this.valueWillMutate();
        var methodCallResult = underlyingArray[methodName].apply(underlyingArray, arguments);
        this.valueHasMutated();
        return methodCallResult;
    };
});

// Populate ko.observableArray.fn with read-only functions from native arrays
ko.utils.arrayForEach(["slice"], function (methodName) {
    ko.observableArray['fn'][methodName] = function () {
        var underlyingArray = this();
        return underlyingArray[methodName].apply(underlyingArray, arguments);
    };
});

ko.exportSymbol('observableArray', ko.observableArray);
ko.dependentObservable = function (evaluatorFunctionOrOptions, evaluatorFunctionTarget, options) {
    var _latestValue,
        _hasBeenEvaluated = false,
        _isBeingEvaluated = false,
        readFunction = evaluatorFunctionOrOptions;

    if (readFunction && typeof readFunction == "object") {
        // Single-parameter syntax - everything is on this "options" param
        options = readFunction;
        readFunction = options["read"];
    } else {
        // Multi-parameter syntax - construct the options according to the params passed
        options = options || {};
        if (!readFunction)
            readFunction = options["read"];
    }
    if (typeof readFunction != "function")
        throw new Error("Pass a function that returns the value of the ko.computed");

    function addSubscriptionToDependency(subscribable) {
        _subscriptionsToDependencies.push(subscribable.subscribe(evaluatePossiblyAsync));
    }

    function disposeAllSubscriptionsToDependencies() {
        ko.utils.arrayForEach(_subscriptionsToDependencies, function (subscription) {
            subscription.dispose();
        });
        _subscriptionsToDependencies = [];
    }

    function evaluatePossiblyAsync() {
        var throttleEvaluationTimeout = dependentObservable['throttleEvaluation'];
        if (throttleEvaluationTimeout && throttleEvaluationTimeout >= 0) {
            clearTimeout(evaluationTimeoutInstance);
            evaluationTimeoutInstance = setTimeout(evaluateImmediate, throttleEvaluationTimeout);
        } else
            evaluateImmediate();
    }

    function evaluateImmediate() {
        if (_isBeingEvaluated) {
            // If the evaluation of a ko.computed causes side effects, it's possible that it will trigger its own re-evaluation.
            // This is not desirable (it's hard for a developer to realise a chain of dependencies might cause this, and they almost
            // certainly didn't intend infinite re-evaluations). So, for predictability, we simply prevent ko.computeds from causing
            // their own re-evaluation. Further discussion at https://github.com/SteveSanderson/knockout/pull/387
            return;
        }

        // Don't dispose on first evaluation, because the "disposeWhen" callback might
        // e.g., dispose when the associated DOM element isn't in the doc, and it's not
        // going to be in the doc until *after* the first evaluation
        if (_hasBeenEvaluated && disposeWhen()) {
            dispose();
            return;
        }

        _isBeingEvaluated = true;
        try {
            // Initially, we assume that none of the subscriptions are still being used (i.e., all are candidates for disposal).
            // Then, during evaluation, we cross off any that are in fact still being used.
            var disposalCandidates = ko.utils.arrayMap(_subscriptionsToDependencies, function(item) {return item.target;});

            ko.dependencyDetection.begin(function(subscribable) {
                var inOld;
                if ((inOld = ko.utils.arrayIndexOf(disposalCandidates, subscribable)) >= 0)
                    disposalCandidates[inOld] = undefined; // Don't want to dispose this subscription, as it's still being used
                else
                    addSubscriptionToDependency(subscribable); // Brand new subscription - add it
            });

            var newValue = readFunction.call(evaluatorFunctionTarget);

            // For each subscription no longer being used, remove it from the active subscriptions list and dispose it
            for (var i = disposalCandidates.length - 1; i >= 0; i--) {
                if (disposalCandidates[i])
                    _subscriptionsToDependencies.splice(i, 1)[0].dispose();
            }
            _hasBeenEvaluated = true;

            dependentObservable["notifySubscribers"](_latestValue, "beforeChange");
            _latestValue = newValue;
            if (DEBUG) dependentObservable._latestValue = _latestValue;
        } finally {
            ko.dependencyDetection.end();
        }

        dependentObservable["notifySubscribers"](_latestValue);
        _isBeingEvaluated = false;
        if (!_subscriptionsToDependencies.length)
            dispose();
    }

    function dependentObservable() {
        if (arguments.length > 0) {
            if (typeof writeFunction === "function") {
                // Writing a value
                writeFunction.apply(evaluatorFunctionTarget, arguments);
            } else {
                throw new Error("Cannot write a value to a ko.computed unless you specify a 'write' option. If you wish to read the current value, don't pass any parameters.");
            }
            return this; // Permits chained assignments
        } else {
            // Reading the value
            if (!_hasBeenEvaluated)
                evaluateImmediate();
            ko.dependencyDetection.registerDependency(dependentObservable);
            return _latestValue;
        }
    }

    function peek() {
        if (!_hasBeenEvaluated)
            evaluateImmediate();
        return _latestValue;
    }

    function isActive() {
        return !_hasBeenEvaluated || _subscriptionsToDependencies.length > 0;
    }

    // By here, "options" is always non-null
    var writeFunction = options["write"],
        disposeWhenNodeIsRemoved = options["disposeWhenNodeIsRemoved"] || options.disposeWhenNodeIsRemoved || null,
        disposeWhen = options["disposeWhen"] || options.disposeWhen || function() { return false; },
        dispose = disposeAllSubscriptionsToDependencies,
        _subscriptionsToDependencies = [],
        evaluationTimeoutInstance = null;

    if (!evaluatorFunctionTarget)
        evaluatorFunctionTarget = options["owner"];

    dependentObservable.peek = peek;
    dependentObservable.getDependenciesCount = function () { return _subscriptionsToDependencies.length; };
    dependentObservable.hasWriteFunction = typeof options["write"] === "function";
    dependentObservable.dispose = function () { dispose(); };
    dependentObservable.isActive = isActive;
    dependentObservable.valueHasMutated = function() {
        _hasBeenEvaluated = false;
        evaluateImmediate();
    };

    ko.subscribable.call(dependentObservable);
    ko.utils.extend(dependentObservable, ko.dependentObservable['fn']);

    ko.exportProperty(dependentObservable, 'peek', dependentObservable.peek);
    ko.exportProperty(dependentObservable, 'dispose', dependentObservable.dispose);
    ko.exportProperty(dependentObservable, 'isActive', dependentObservable.isActive);
    ko.exportProperty(dependentObservable, 'getDependenciesCount', dependentObservable.getDependenciesCount);

    // Evaluate, unless deferEvaluation is true
    if (options['deferEvaluation'] !== true)
        evaluateImmediate();

    // Build "disposeWhenNodeIsRemoved" and "disposeWhenNodeIsRemovedCallback" option values.
    // But skip if isActive is false (there will never be any dependencies to dispose).
    // (Note: "disposeWhenNodeIsRemoved" option both proactively disposes as soon as the node is removed using ko.removeNode(),
    // plus adds a "disposeWhen" callback that, on each evaluation, disposes if the node was removed by some other means.)
    if (disposeWhenNodeIsRemoved && isActive()) {
        dispose = function() {
            ko.utils.domNodeDisposal.removeDisposeCallback(disposeWhenNodeIsRemoved, arguments.callee);
            disposeAllSubscriptionsToDependencies();
        };
        ko.utils.domNodeDisposal.addDisposeCallback(disposeWhenNodeIsRemoved, dispose);
        var existingDisposeWhenFunction = disposeWhen;
        disposeWhen = function () {
            return !ko.utils.domNodeIsAttachedToDocument(disposeWhenNodeIsRemoved) || existingDisposeWhenFunction();
        }
    }

    return dependentObservable;
};

ko.isComputed = function(instance) {
    return ko.hasPrototype(instance, ko.dependentObservable);
};

var protoProp = ko.observable.protoProperty; // == "__ko_proto__"
ko.dependentObservable[protoProp] = ko.observable;

ko.dependentObservable['fn'] = {};
ko.dependentObservable['fn'][protoProp] = ko.dependentObservable;

ko.exportSymbol('dependentObservable', ko.dependentObservable);
ko.exportSymbol('computed', ko.dependentObservable); // Make "ko.computed" an alias for "ko.dependentObservable"
ko.exportSymbol('isComputed', ko.isComputed);

(function() {
    var maxNestedObservableDepth = 10; // Escape the (unlikely) pathalogical case where an observable's current value is itself (or similar reference cycle)

    ko.toJS = function(rootObject) {
        if (arguments.length == 0)
            throw new Error("When calling ko.toJS, pass the object you want to convert.");

        // We just unwrap everything at every level in the object graph
        return mapJsObjectGraph(rootObject, function(valueToMap) {
            // Loop because an observable's value might in turn be another observable wrapper
            for (var i = 0; ko.isObservable(valueToMap) && (i < maxNestedObservableDepth); i++)
                valueToMap = valueToMap();
            return valueToMap;
        });
    };

    ko.toJSON = function(rootObject, replacer, space) {     // replacer and space are optional
        var plainJavaScriptObject = ko.toJS(rootObject);
        return ko.utils.stringifyJson(plainJavaScriptObject, replacer, space);
    };

    function mapJsObjectGraph(rootObject, mapInputCallback, visitedObjects) {
        visitedObjects = visitedObjects || new objectLookup();

        rootObject = mapInputCallback(rootObject);
        var canHaveProperties = (typeof rootObject == "object") && (rootObject !== null) && (rootObject !== undefined) && (!(rootObject instanceof Date));
        if (!canHaveProperties)
            return rootObject;

        var outputProperties = rootObject instanceof Array ? [] : {};
        visitedObjects.save(rootObject, outputProperties);

        visitPropertiesOrArrayEntries(rootObject, function(indexer) {
            var propertyValue = mapInputCallback(rootObject[indexer]);

            switch (typeof propertyValue) {
                case "boolean":
                case "number":
                case "string":
                case "function":
                    outputProperties[indexer] = propertyValue;
                    break;
                case "object":
                case "undefined":
                    var previouslyMappedValue = visitedObjects.get(propertyValue);
                    outputProperties[indexer] = (previouslyMappedValue !== undefined)
                        ? previouslyMappedValue
                        : mapJsObjectGraph(propertyValue, mapInputCallback, visitedObjects);
                    break;
            }
        });

        return outputProperties;
    }

    function visitPropertiesOrArrayEntries(rootObject, visitorCallback) {
        if (rootObject instanceof Array) {
            for (var i = 0; i < rootObject.length; i++)
                visitorCallback(i);

            // For arrays, also respect toJSON property for custom mappings (fixes #278)
            if (typeof rootObject['toJSON'] == 'function')
                visitorCallback('toJSON');
        } else {
            for (var propertyName in rootObject)
                visitorCallback(propertyName);
        }
    };

    function objectLookup() {
        var keys = [];
        var values = [];
        this.save = function(key, value) {
            var existingIndex = ko.utils.arrayIndexOf(keys, key);
            if (existingIndex >= 0)
                values[existingIndex] = value;
            else {
                keys.push(key);
                values.push(value);
            }
        };
        this.get = function(key) {
            var existingIndex = ko.utils.arrayIndexOf(keys, key);
            return (existingIndex >= 0) ? values[existingIndex] : undefined;
        };
    };
})();

ko.exportSymbol('toJS', ko.toJS);
ko.exportSymbol('toJSON', ko.toJSON);
(function () {
    var hasDomDataExpandoProperty = '__ko__hasDomDataOptionValue__';

    // Normally, SELECT elements and their OPTIONs can only take value of type 'string' (because the values
    // are stored on DOM attributes). ko.selectExtensions provides a way for SELECTs/OPTIONs to have values
    // that are arbitrary objects. This is very convenient when implementing things like cascading dropdowns.
    ko.selectExtensions = {
        readValue : function(element) {
            switch (ko.utils.tagNameLower(element)) {
                case 'option':
                    if (element[hasDomDataExpandoProperty] === true)
                        return ko.utils.domData.get(element, ko.bindingHandlers.options.optionValueDomDataKey);
                    return ko.utils.ieVersion <= 7
                        ? (element.getAttributeNode('value').specified ? element.value : element.text)
                        : element.value;
                case 'select':
                    return element.selectedIndex >= 0 ? ko.selectExtensions.readValue(element.options[element.selectedIndex]) : undefined;
                default:
                    return element.value;
            }
        },

        writeValue: function(element, value) {
            switch (ko.utils.tagNameLower(element)) {
                case 'option':
                    switch(typeof value) {
                        case "string":
                            ko.utils.domData.set(element, ko.bindingHandlers.options.optionValueDomDataKey, undefined);
                            if (hasDomDataExpandoProperty in element) { // IE <= 8 throws errors if you delete non-existent properties from a DOM node
                                delete element[hasDomDataExpandoProperty];
                            }
                            element.value = value;
                            break;
                        default:
                            // Store arbitrary object using DomData
                            ko.utils.domData.set(element, ko.bindingHandlers.options.optionValueDomDataKey, value);
                            element[hasDomDataExpandoProperty] = true;

                            // Special treatment of numbers is just for backward compatibility. KO 1.2.1 wrote numerical values to element.value.
                            element.value = typeof value === "number" ? value : "";
                            break;
                    }
                    break;
                case 'select':
                    for (var i = element.options.length - 1; i >= 0; i--) {
                        if (ko.selectExtensions.readValue(element.options[i]) == value) {
                            element.selectedIndex = i;
                            break;
                        }
                    }
                    break;
                default:
                    if ((value === null) || (value === undefined))
                        value = "";
                    element.value = value;
                    break;
            }
        }
    };
})();

ko.exportSymbol('selectExtensions', ko.selectExtensions);
ko.exportSymbol('selectExtensions.readValue', ko.selectExtensions.readValue);
ko.exportSymbol('selectExtensions.writeValue', ko.selectExtensions.writeValue);
ko.expressionRewriting = (function () {
    var restoreCapturedTokensRegex = /\@ko_token_(\d+)\@/g;
    var javaScriptReservedWords = ["true", "false"];

    // Matches something that can be assigned to--either an isolated identifier or something ending with a property accessor
    // This is designed to be simple and avoid false negatives, but could produce false positives (e.g., a+b.c).
    var javaScriptAssignmentTarget = /^(?:[$_a-z][$\w]*|(.+)(\.\s*[$_a-z][$\w]*|\[.+\]))$/i;

    function restoreTokens(string, tokens) {
        var prevValue = null;
        while (string != prevValue) { // Keep restoring tokens until it no longer makes a difference (they may be nested)
            prevValue = string;
            string = string.replace(restoreCapturedTokensRegex, function (match, tokenIndex) {
                return tokens[tokenIndex];
            });
        }
        return string;
    }

    function getWriteableValue(expression) {
        if (ko.utils.arrayIndexOf(javaScriptReservedWords, ko.utils.stringTrim(expression).toLowerCase()) >= 0)
            return false;
        var match = expression.match(javaScriptAssignmentTarget);
        return match === null ? false : match[1] ? ('Object(' + match[1] + ')' + match[2]) : expression;
    }

    function ensureQuoted(key) {
        var trimmedKey = ko.utils.stringTrim(key);
        switch (trimmedKey.length && trimmedKey.charAt(0)) {
            case "'":
            case '"':
                return key;
            default:
                return "'" + trimmedKey + "'";
        }
    }

    return {
        bindingRewriteValidators: [],

        parseObjectLiteral: function(objectLiteralString) {
            // A full tokeniser+lexer would add too much weight to this library, so here's a simple parser
            // that is sufficient just to split an object literal string into a set of top-level key-value pairs

            var str = ko.utils.stringTrim(objectLiteralString);
            if (str.length < 3)
                return [];
            if (str.charAt(0) === "{")// Ignore any braces surrounding the whole object literal
                str = str.substring(1, str.length - 1);

            // Pull out any string literals and regex literals
            var tokens = [];
            var tokenStart = null, tokenEndChar;
            for (var position = 0; position < str.length; position++) {
                var c = str.charAt(position);
                if (tokenStart === null) {
                    switch (c) {
                        case '"':
                        case "'":
                        case "/":
                            tokenStart = position;
                            tokenEndChar = c;
                            break;
                    }
                } else if ((c == tokenEndChar) && (str.charAt(position - 1) !== "\\")) {
                    var token = str.substring(tokenStart, position + 1);
                    tokens.push(token);
                    var replacement = "@ko_token_" + (tokens.length - 1) + "@";
                    str = str.substring(0, tokenStart) + replacement + str.substring(position + 1);
                    position -= (token.length - replacement.length);
                    tokenStart = null;
                }
            }

            // Next pull out balanced paren, brace, and bracket blocks
            tokenStart = null;
            tokenEndChar = null;
            var tokenDepth = 0, tokenStartChar = null;
            for (var position = 0; position < str.length; position++) {
                var c = str.charAt(position);
                if (tokenStart === null) {
                    switch (c) {
                        case "{": tokenStart = position; tokenStartChar = c;
                                  tokenEndChar = "}";
                                  break;
                        case "(": tokenStart = position; tokenStartChar = c;
                                  tokenEndChar = ")";
                                  break;
                        case "[": tokenStart = position; tokenStartChar = c;
                                  tokenEndChar = "]";
                                  break;
                    }
                }

                if (c === tokenStartChar)
                    tokenDepth++;
                else if (c === tokenEndChar) {
                    tokenDepth--;
                    if (tokenDepth === 0) {
                        var token = str.substring(tokenStart, position + 1);
                        tokens.push(token);
                        var replacement = "@ko_token_" + (tokens.length - 1) + "@";
                        str = str.substring(0, tokenStart) + replacement + str.substring(position + 1);
                        position -= (token.length - replacement.length);
                        tokenStart = null;
                    }
                }
            }

            // Now we can safely split on commas to get the key/value pairs
            var result = [];
            var keyValuePairs = str.split(",");
            for (var i = 0, j = keyValuePairs.length; i < j; i++) {
                var pair = keyValuePairs[i];
                var colonPos = pair.indexOf(":");
                if ((colonPos > 0) && (colonPos < pair.length - 1)) {
                    var key = pair.substring(0, colonPos);
                    var value = pair.substring(colonPos + 1);
                    result.push({ 'key': restoreTokens(key, tokens), 'value': restoreTokens(value, tokens) });
                } else {
                    result.push({ 'unknown': restoreTokens(pair, tokens) });
                }
            }
            return result;
        },

        preProcessBindings: function (objectLiteralStringOrKeyValueArray) {
            var keyValueArray = typeof objectLiteralStringOrKeyValueArray === "string"
                ? ko.expressionRewriting.parseObjectLiteral(objectLiteralStringOrKeyValueArray)
                : objectLiteralStringOrKeyValueArray;
            var resultStrings = [], propertyAccessorResultStrings = [];

            var keyValueEntry;
            for (var i = 0; keyValueEntry = keyValueArray[i]; i++) {
                if (resultStrings.length > 0)
                    resultStrings.push(",");

                if (keyValueEntry['key']) {
                    var quotedKey = ensureQuoted(keyValueEntry['key']), val = keyValueEntry['value'];
                    resultStrings.push(quotedKey);
                    resultStrings.push(":");
                    resultStrings.push(val);

                    if (val = getWriteableValue(ko.utils.stringTrim(val))) {
                        if (propertyAccessorResultStrings.length > 0)
                            propertyAccessorResultStrings.push(", ");
                        propertyAccessorResultStrings.push(quotedKey + " : function(__ko_value) { " + val + " = __ko_value; }");
                    }
                } else if (keyValueEntry['unknown']) {
                    resultStrings.push(keyValueEntry['unknown']);
                }
            }

            var combinedResult = resultStrings.join("");
            if (propertyAccessorResultStrings.length > 0) {
                var allPropertyAccessors = propertyAccessorResultStrings.join("");
                combinedResult = combinedResult + ", '_ko_property_writers' : { " + allPropertyAccessors + " } ";
            }

            return combinedResult;
        },

        keyValueArrayContainsKey: function(keyValueArray, key) {
            for (var i = 0; i < keyValueArray.length; i++)
                if (ko.utils.stringTrim(keyValueArray[i]['key']) == key)
                    return true;
            return false;
        },

        // Internal, private KO utility for updating model properties from within bindings
        // property:            If the property being updated is (or might be) an observable, pass it here
        //                      If it turns out to be a writable observable, it will be written to directly
        // allBindingsAccessor: All bindings in the current execution context.
        //                      This will be searched for a '_ko_property_writers' property in case you're writing to a non-observable
        // key:                 The key identifying the property to be written. Example: for { hasFocus: myValue }, write to 'myValue' by specifying the key 'hasFocus'
        // value:               The value to be written
        // checkIfDifferent:    If true, and if the property being written is a writable observable, the value will only be written if
        //                      it is !== existing value on that writable observable
        writeValueToProperty: function(property, allBindingsAccessor, key, value, checkIfDifferent) {
            if (!property || !ko.isWriteableObservable(property)) {
                var propWriters = allBindingsAccessor()['_ko_property_writers'];
                if (propWriters && propWriters[key])
                    propWriters[key](value);
            } else if (!checkIfDifferent || property.peek() !== value) {
                property(value);
            }
        }
    };
})();

ko.exportSymbol('expressionRewriting', ko.expressionRewriting);
ko.exportSymbol('expressionRewriting.bindingRewriteValidators', ko.expressionRewriting.bindingRewriteValidators);
ko.exportSymbol('expressionRewriting.parseObjectLiteral', ko.expressionRewriting.parseObjectLiteral);
ko.exportSymbol('expressionRewriting.preProcessBindings', ko.expressionRewriting.preProcessBindings);

// For backward compatibility, define the following aliases. (Previously, these function names were misleading because
// they referred to JSON specifically, even though they actually work with arbitrary JavaScript object literal expressions.)
ko.exportSymbol('jsonExpressionRewriting', ko.expressionRewriting);
ko.exportSymbol('jsonExpressionRewriting.insertPropertyAccessorsIntoJson', ko.expressionRewriting.preProcessBindings);(function() {
    // "Virtual elements" is an abstraction on top of the usual DOM API which understands the notion that comment nodes
    // may be used to represent hierarchy (in addition to the DOM's natural hierarchy).
    // If you call the DOM-manipulating functions on ko.virtualElements, you will be able to read and write the state
    // of that virtual hierarchy
    //
    // The point of all this is to support containerless templates (e.g., <!-- ko foreach:someCollection -->blah<!-- /ko -->)
    // without having to scatter special cases all over the binding and templating code.

    // IE 9 cannot reliably read the "nodeValue" property of a comment node (see https://github.com/SteveSanderson/knockout/issues/186)
    // but it does give them a nonstandard alternative property called "text" that it can read reliably. Other browsers don't have that property.
    // So, use node.text where available, and node.nodeValue elsewhere
    var commentNodesHaveTextProperty = document.createComment("test").text === "<!--test-->";

    var startCommentRegex = commentNodesHaveTextProperty ? /^<!--\s*ko(?:\s+(.+\s*\:[\s\S]*))?\s*-->$/ : /^\s*ko(?:\s+(.+\s*\:[\s\S]*))?\s*$/;
    var endCommentRegex =   commentNodesHaveTextProperty ? /^<!--\s*\/ko\s*-->$/ : /^\s*\/ko\s*$/;
    var htmlTagsWithOptionallyClosingChildren = { 'ul': true, 'ol': true };

    function isStartComment(node) {
        return (node.nodeType == 8) && (commentNodesHaveTextProperty ? node.text : node.nodeValue).match(startCommentRegex);
    }

    function isEndComment(node) {
        return (node.nodeType == 8) && (commentNodesHaveTextProperty ? node.text : node.nodeValue).match(endCommentRegex);
    }

    function getVirtualChildren(startComment, allowUnbalanced) {
        var currentNode = startComment;
        var depth = 1;
        var children = [];
        while (currentNode = currentNode.nextSibling) {
            if (isEndComment(currentNode)) {
                depth--;
                if (depth === 0)
                    return children;
            }

            children.push(currentNode);

            if (isStartComment(currentNode))
                depth++;
        }
        if (!allowUnbalanced)
            throw new Error("Cannot find closing comment tag to match: " + startComment.nodeValue);
        return null;
    }

    function getMatchingEndComment(startComment, allowUnbalanced) {
        var allVirtualChildren = getVirtualChildren(startComment, allowUnbalanced);
        if (allVirtualChildren) {
            if (allVirtualChildren.length > 0)
                return allVirtualChildren[allVirtualChildren.length - 1].nextSibling;
            return startComment.nextSibling;
        } else
            return null; // Must have no matching end comment, and allowUnbalanced is true
    }

    function getUnbalancedChildTags(node) {
        // e.g., from <div>OK</div><!-- ko blah --><span>Another</span>, returns: <!-- ko blah --><span>Another</span>
        //       from <div>OK</div><!-- /ko --><!-- /ko -->,             returns: <!-- /ko --><!-- /ko -->
        var childNode = node.firstChild, captureRemaining = null;
        if (childNode) {
            do {
                if (captureRemaining)                   // We already hit an unbalanced node and are now just scooping up all subsequent nodes
                    captureRemaining.push(childNode);
                else if (isStartComment(childNode)) {
                    var matchingEndComment = getMatchingEndComment(childNode, /* allowUnbalanced: */ true);
                    if (matchingEndComment)             // It's a balanced tag, so skip immediately to the end of this virtual set
                        childNode = matchingEndComment;
                    else
                        captureRemaining = [childNode]; // It's unbalanced, so start capturing from this point
                } else if (isEndComment(childNode)) {
                    captureRemaining = [childNode];     // It's unbalanced (if it wasn't, we'd have skipped over it already), so start capturing
                }
            } while (childNode = childNode.nextSibling);
        }
        return captureRemaining;
    }

    ko.virtualElements = {
        allowedBindings: {},

        childNodes: function(node) {
            return isStartComment(node) ? getVirtualChildren(node) : node.childNodes;
        },

        emptyNode: function(node) {
            if (!isStartComment(node))
                ko.utils.emptyDomNode(node);
            else {
                var virtualChildren = ko.virtualElements.childNodes(node);
                for (var i = 0, j = virtualChildren.length; i < j; i++)
                    ko.removeNode(virtualChildren[i]);
            }
        },

        setDomNodeChildren: function(node, childNodes) {
            if (!isStartComment(node))
                ko.utils.setDomNodeChildren(node, childNodes);
            else {
                ko.virtualElements.emptyNode(node);
                var endCommentNode = node.nextSibling; // Must be the next sibling, as we just emptied the children
                for (var i = 0, j = childNodes.length; i < j; i++)
                    endCommentNode.parentNode.insertBefore(childNodes[i], endCommentNode);
            }
        },

        prepend: function(containerNode, nodeToPrepend) {
            if (!isStartComment(containerNode)) {
                if (containerNode.firstChild)
                    containerNode.insertBefore(nodeToPrepend, containerNode.firstChild);
                else
                    containerNode.appendChild(nodeToPrepend);
            } else {
                // Start comments must always have a parent and at least one following sibling (the end comment)
                containerNode.parentNode.insertBefore(nodeToPrepend, containerNode.nextSibling);
            }
        },

        insertAfter: function(containerNode, nodeToInsert, insertAfterNode) {
            if (!insertAfterNode) {
                ko.virtualElements.prepend(containerNode, nodeToInsert);
            } else if (!isStartComment(containerNode)) {
                // Insert after insertion point
                if (insertAfterNode.nextSibling)
                    containerNode.insertBefore(nodeToInsert, insertAfterNode.nextSibling);
                else
                    containerNode.appendChild(nodeToInsert);
            } else {
                // Children of start comments must always have a parent and at least one following sibling (the end comment)
                containerNode.parentNode.insertBefore(nodeToInsert, insertAfterNode.nextSibling);
            }
        },

        firstChild: function(node) {
            if (!isStartComment(node))
                return node.firstChild;
            if (!node.nextSibling || isEndComment(node.nextSibling))
                return null;
            return node.nextSibling;
        },

        nextSibling: function(node) {
            if (isStartComment(node))
                node = getMatchingEndComment(node);
            if (node.nextSibling && isEndComment(node.nextSibling))
                return null;
            return node.nextSibling;
        },

        virtualNodeBindingValue: function(node) {
            var regexMatch = isStartComment(node);
            return regexMatch ? regexMatch[1] : null;
        },

        normaliseVirtualElementDomStructure: function(elementVerified) {
            // Workaround for https://github.com/SteveSanderson/knockout/issues/155
            // (IE <= 8 or IE 9 quirks mode parses your HTML weirdly, treating closing </li> tags as if they don't exist, thereby moving comment nodes
            // that are direct descendants of <ul> into the preceding <li>)
            if (!htmlTagsWithOptionallyClosingChildren[ko.utils.tagNameLower(elementVerified)])
                return;

            // Scan immediate children to see if they contain unbalanced comment tags. If they do, those comment tags
            // must be intended to appear *after* that child, so move them there.
            var childNode = elementVerified.firstChild;
            if (childNode) {
                do {
                    if (childNode.nodeType === 1) {
                        var unbalancedTags = getUnbalancedChildTags(childNode);
                        if (unbalancedTags) {
                            // Fix up the DOM by moving the unbalanced tags to where they most likely were intended to be placed - *after* the child
                            var nodeToInsertBefore = childNode.nextSibling;
                            for (var i = 0; i < unbalancedTags.length; i++) {
                                if (nodeToInsertBefore)
                                    elementVerified.insertBefore(unbalancedTags[i], nodeToInsertBefore);
                                else
                                    elementVerified.appendChild(unbalancedTags[i]);
                            }
                        }
                    }
                } while (childNode = childNode.nextSibling);
            }
        }
    };
})();
ko.exportSymbol('virtualElements', ko.virtualElements);
ko.exportSymbol('virtualElements.allowedBindings', ko.virtualElements.allowedBindings);
ko.exportSymbol('virtualElements.emptyNode', ko.virtualElements.emptyNode);
//ko.exportSymbol('virtualElements.firstChild', ko.virtualElements.firstChild);     // firstChild is not minified
ko.exportSymbol('virtualElements.insertAfter', ko.virtualElements.insertAfter);
//ko.exportSymbol('virtualElements.nextSibling', ko.virtualElements.nextSibling);   // nextSibling is not minified
ko.exportSymbol('virtualElements.prepend', ko.virtualElements.prepend);
ko.exportSymbol('virtualElements.setDomNodeChildren', ko.virtualElements.setDomNodeChildren);
(function() {
    var defaultBindingAttributeName = "data-bind";

    ko.bindingProvider = function() {
        this.bindingCache = {};
    };

    ko.utils.extend(ko.bindingProvider.prototype, {
        'nodeHasBindings': function(node) {
            switch (node.nodeType) {
                case 1: return node.getAttribute(defaultBindingAttributeName) != null;   // Element
                case 8: return ko.virtualElements.virtualNodeBindingValue(node) != null; // Comment node
                default: return false;
            }
        },

        'getBindings': function(node, bindingContext) {
            var bindingsString = this['getBindingsString'](node, bindingContext);
            return bindingsString ? this['parseBindingsString'](bindingsString, bindingContext, node) : null;
        },

        // The following function is only used internally by this default provider.
        // It's not part of the interface definition for a general binding provider.
        'getBindingsString': function(node, bindingContext) {
            switch (node.nodeType) {
                case 1: return node.getAttribute(defaultBindingAttributeName);   // Element
                case 8: return ko.virtualElements.virtualNodeBindingValue(node); // Comment node
                default: return null;
            }
        },

        // The following function is only used internally by this default provider.
        // It's not part of the interface definition for a general binding provider.
        'parseBindingsString': function(bindingsString, bindingContext, node) {
            try {
                var bindingFunction = createBindingsStringEvaluatorViaCache(bindingsString, this.bindingCache);
                return bindingFunction(bindingContext, node);
            } catch (ex) {
                throw new Error("Unable to parse bindings.\nMessage: " + ex + ";\nBindings value: " + bindingsString);
            }
        }
    });

    ko.bindingProvider['instance'] = new ko.bindingProvider();

    function createBindingsStringEvaluatorViaCache(bindingsString, cache) {
        var cacheKey = bindingsString;
        return cache[cacheKey]
            || (cache[cacheKey] = createBindingsStringEvaluator(bindingsString));
    }

    function createBindingsStringEvaluator(bindingsString) {
        // Build the source for a function that evaluates "expression"
        // For each scope variable, add an extra level of "with" nesting
        // Example result: with(sc1) { with(sc0) { return (expression) } }
        var rewrittenBindings = ko.expressionRewriting.preProcessBindings(bindingsString),
            functionBody = "with($context){with($data||{}){return{" + rewrittenBindings + "}}}";
        return new Function("$context", "$element", functionBody);
    }
})();

ko.exportSymbol('bindingProvider', ko.bindingProvider);
(function () {
    ko.bindingHandlers = {};

    ko.bindingContext = function(dataItem, parentBindingContext, dataItemAlias) {
        if (parentBindingContext) {
            ko.utils.extend(this, parentBindingContext); // Inherit $root and any custom properties
            this['$parentContext'] = parentBindingContext;
            this['$parent'] = parentBindingContext['$data'];
            this['$parents'] = (parentBindingContext['$parents'] || []).slice(0);
            this['$parents'].unshift(this['$parent']);
        } else {
            this['$parents'] = [];
            this['$root'] = dataItem;
            // Export 'ko' in the binding context so it will be available in bindings and templates
            // even if 'ko' isn't exported as a global, such as when using an AMD loader.
            // See https://github.com/SteveSanderson/knockout/issues/490
            this['ko'] = ko;
        }
        this['$data'] = dataItem;
        if (dataItemAlias)
            this[dataItemAlias] = dataItem;
    }
    ko.bindingContext.prototype['createChildContext'] = function (dataItem, dataItemAlias) {
        return new ko.bindingContext(dataItem, this, dataItemAlias);
    };
    ko.bindingContext.prototype['extend'] = function(properties) {
        var clone = ko.utils.extend(new ko.bindingContext(), this);
        return ko.utils.extend(clone, properties);
    };

    function validateThatBindingIsAllowedForVirtualElements(bindingName) {
        var validator = ko.virtualElements.allowedBindings[bindingName];
        if (!validator)
            throw new Error("The binding '" + bindingName + "' cannot be used with virtual elements")
    }

    function applyBindingsToDescendantsInternal (viewModel, elementOrVirtualElement, bindingContextsMayDifferFromDomParentElement) {
        var currentChild, nextInQueue = ko.virtualElements.firstChild(elementOrVirtualElement);
        while (currentChild = nextInQueue) {
            // Keep a record of the next child *before* applying bindings, in case the binding removes the current child from its position
            nextInQueue = ko.virtualElements.nextSibling(currentChild);
            applyBindingsToNodeAndDescendantsInternal(viewModel, currentChild, bindingContextsMayDifferFromDomParentElement);
        }
    }

    function applyBindingsToNodeAndDescendantsInternal (viewModel, nodeVerified, bindingContextMayDifferFromDomParentElement) {
        var shouldBindDescendants = true;

        // Perf optimisation: Apply bindings only if...
        // (1) We need to store the binding context on this node (because it may differ from the DOM parent node's binding context)
        //     Note that we can't store binding contexts on non-elements (e.g., text nodes), as IE doesn't allow expando properties for those
        // (2) It might have bindings (e.g., it has a data-bind attribute, or it's a marker for a containerless template)
        var isElement = (nodeVerified.nodeType === 1);
        if (isElement) // Workaround IE <= 8 HTML parsing weirdness
            ko.virtualElements.normaliseVirtualElementDomStructure(nodeVerified);

        var shouldApplyBindings = (isElement && bindingContextMayDifferFromDomParentElement)             // Case (1)
                               || ko.bindingProvider['instance']['nodeHasBindings'](nodeVerified);       // Case (2)
        if (shouldApplyBindings)
            shouldBindDescendants = applyBindingsToNodeInternal(nodeVerified, null, viewModel, bindingContextMayDifferFromDomParentElement).shouldBindDescendants;

        if (shouldBindDescendants) {
            // We're recursing automatically into (real or virtual) child nodes without changing binding contexts. So,
            //  * For children of a *real* element, the binding context is certainly the same as on their DOM .parentNode,
            //    hence bindingContextsMayDifferFromDomParentElement is false
            //  * For children of a *virtual* element, we can't be sure. Evaluating .parentNode on those children may
            //    skip over any number of intermediate virtual elements, any of which might define a custom binding context,
            //    hence bindingContextsMayDifferFromDomParentElement is true
            applyBindingsToDescendantsInternal(viewModel, nodeVerified, /* bindingContextsMayDifferFromDomParentElement: */ !isElement);
        }
    }

    function applyBindingsToNodeInternal (node, bindings, viewModelOrBindingContext, bindingContextMayDifferFromDomParentElement) {
        // Need to be sure that inits are only run once, and updates never run until all the inits have been run
        var initPhase = 0; // 0 = before all inits, 1 = during inits, 2 = after all inits

        // Each time the dependentObservable is evaluated (after data changes),
        // the binding attribute is reparsed so that it can pick out the correct
        // model properties in the context of the changed data.
        // DOM event callbacks need to be able to access this changed data,
        // so we need a single parsedBindings variable (shared by all callbacks
        // associated with this node's bindings) that all the closures can access.
        var parsedBindings;
        function makeValueAccessor(bindingKey) {
            return function () { return parsedBindings[bindingKey] }
        }
        function parsedBindingsAccessor() {
            return parsedBindings;
        }

        var bindingHandlerThatControlsDescendantBindings;
        ko.dependentObservable(
            function () {
                // Ensure we have a nonnull binding context to work with
                var bindingContextInstance = viewModelOrBindingContext && (viewModelOrBindingContext instanceof ko.bindingContext)
                    ? viewModelOrBindingContext
                    : new ko.bindingContext(ko.utils.unwrapObservable(viewModelOrBindingContext));
                var viewModel = bindingContextInstance['$data'];

                // Optimization: Don't store the binding context on this node if it's definitely the same as on node.parentNode, because
                // we can easily recover it just by scanning up the node's ancestors in the DOM
                // (note: here, parent node means "real DOM parent" not "virtual parent", as there's no O(1) way to find the virtual parent)
                if (bindingContextMayDifferFromDomParentElement)
                    ko.storedBindingContextForNode(node, bindingContextInstance);

                // Use evaluatedBindings if given, otherwise fall back on asking the bindings provider to give us some bindings
                var evaluatedBindings = (typeof bindings == "function") ? bindings(bindingContextInstance, node) : bindings;
                parsedBindings = evaluatedBindings || ko.bindingProvider['instance']['getBindings'](node, bindingContextInstance);

                if (parsedBindings) {
                    // First run all the inits, so bindings can register for notification on changes
                    if (initPhase === 0) {
                        initPhase = 1;
                        for (var bindingKey in parsedBindings) {
                            var binding = ko.bindingHandlers[bindingKey];
                            if (binding && node.nodeType === 8)
                                validateThatBindingIsAllowedForVirtualElements(bindingKey);

                            if (binding && typeof binding["init"] == "function") {
                                var handlerInitFn = binding["init"];
                                var initResult = handlerInitFn(node, makeValueAccessor(bindingKey), parsedBindingsAccessor, viewModel, bindingContextInstance);

                                // If this binding handler claims to control descendant bindings, make a note of this
                                if (initResult && initResult['controlsDescendantBindings']) {
                                    if (bindingHandlerThatControlsDescendantBindings !== undefined)
                                        throw new Error("Multiple bindings (" + bindingHandlerThatControlsDescendantBindings + " and " + bindingKey + ") are trying to control descendant bindings of the same element. You cannot use these bindings together on the same element.");
                                    bindingHandlerThatControlsDescendantBindings = bindingKey;
                                }
                            }
                        }
                        initPhase = 2;
                    }

                    // ... then run all the updates, which might trigger changes even on the first evaluation
                    if (initPhase === 2) {
                        for (var bindingKey in parsedBindings) {
                            var binding = ko.bindingHandlers[bindingKey];
                            if (binding && typeof binding["update"] == "function") {
                                var handlerUpdateFn = binding["update"];
                                handlerUpdateFn(node, makeValueAccessor(bindingKey), parsedBindingsAccessor, viewModel, bindingContextInstance);
                            }
                        }
                    }
                }
            },
            null,
            { disposeWhenNodeIsRemoved : node }
        );

        return {
            shouldBindDescendants: bindingHandlerThatControlsDescendantBindings === undefined
        };
    };

    var storedBindingContextDomDataKey = "__ko_bindingContext__";
    ko.storedBindingContextForNode = function (node, bindingContext) {
        if (arguments.length == 2)
            ko.utils.domData.set(node, storedBindingContextDomDataKey, bindingContext);
        else
            return ko.utils.domData.get(node, storedBindingContextDomDataKey);
    }

    ko.applyBindingsToNode = function (node, bindings, viewModel) {
        if (node.nodeType === 1) // If it's an element, workaround IE <= 8 HTML parsing weirdness
            ko.virtualElements.normaliseVirtualElementDomStructure(node);
        return applyBindingsToNodeInternal(node, bindings, viewModel, true);
    };

    ko.applyBindingsToDescendants = function(viewModel, rootNode) {
        if (rootNode.nodeType === 1 || rootNode.nodeType === 8)
            applyBindingsToDescendantsInternal(viewModel, rootNode, true);
    };

    ko.applyBindings = function (viewModel, rootNode) {
        if (rootNode && (rootNode.nodeType !== 1) && (rootNode.nodeType !== 8))
            throw new Error("ko.applyBindings: first parameter should be your view model; second parameter should be a DOM node");
        rootNode = rootNode || window.document.body; // Make "rootNode" parameter optional

        applyBindingsToNodeAndDescendantsInternal(viewModel, rootNode, true);
    };

    // Retrieving binding context from arbitrary nodes
    ko.contextFor = function(node) {
        // We can only do something meaningful for elements and comment nodes (in particular, not text nodes, as IE can't store domdata for them)
        switch (node.nodeType) {
            case 1:
            case 8:
                var context = ko.storedBindingContextForNode(node);
                if (context) return context;
                if (node.parentNode) return ko.contextFor(node.parentNode);
                break;
        }
        return undefined;
    };
    ko.dataFor = function(node) {
        var context = ko.contextFor(node);
        return context ? context['$data'] : undefined;
    };

    ko.exportSymbol('bindingHandlers', ko.bindingHandlers);
    ko.exportSymbol('applyBindings', ko.applyBindings);
    ko.exportSymbol('applyBindingsToDescendants', ko.applyBindingsToDescendants);
    ko.exportSymbol('applyBindingsToNode', ko.applyBindingsToNode);
    ko.exportSymbol('contextFor', ko.contextFor);
    ko.exportSymbol('dataFor', ko.dataFor);
})();
var attrHtmlToJavascriptMap = { 'class': 'className', 'for': 'htmlFor' };
ko.bindingHandlers['attr'] = {
    'update': function(element, valueAccessor, allBindingsAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor()) || {};
        for (var attrName in value) {
            if (typeof attrName == "string") {
                var attrValue = ko.utils.unwrapObservable(value[attrName]);

                // To cover cases like "attr: { checked:someProp }", we want to remove the attribute entirely
                // when someProp is a "no value"-like value (strictly null, false, or undefined)
                // (because the absence of the "checked" attr is how to mark an element as not checked, etc.)
                var toRemove = (attrValue === false) || (attrValue === null) || (attrValue === undefined);
                if (toRemove)
                    element.removeAttribute(attrName);

                // In IE <= 7 and IE8 Quirks Mode, you have to use the Javascript property name instead of the
                // HTML attribute name for certain attributes. IE8 Standards Mode supports the correct behavior,
                // but instead of figuring out the mode, we'll just set the attribute through the Javascript
                // property for IE <= 8.
                if (ko.utils.ieVersion <= 8 && attrName in attrHtmlToJavascriptMap) {
                    attrName = attrHtmlToJavascriptMap[attrName];
                    if (toRemove)
                        element.removeAttribute(attrName);
                    else
                        element[attrName] = attrValue;
                } else if (!toRemove) {
                    try {
                        element.setAttribute(attrName, attrValue.toString());
                    } catch (err) {
                        // ignore for now
                        if (console) {
                            console.log("Can't set attribute " + attrName + " to " + attrValue + " error: " + err);
                        }
                    }
                }

                // Treat "name" specially - although you can think of it as an attribute, it also needs
                // special handling on older versions of IE (https://github.com/SteveSanderson/knockout/pull/333)
                // Deliberately being case-sensitive here because XHTML would regard "Name" as a different thing
                // entirely, and there's no strong reason to allow for such casing in HTML.
                if (attrName === "name") {
                    ko.utils.setElementName(element, toRemove ? "" : attrValue.toString());
                }
            }
        }
    }
};
ko.bindingHandlers['checked'] = {
    'init': function (element, valueAccessor, allBindingsAccessor) {
        var updateHandler = function() {
            var valueToWrite;
            if (element.type == "checkbox") {
                valueToWrite = element.checked;
            } else if ((element.type == "radio") && (element.checked)) {
                valueToWrite = element.value;
            } else {
                return; // "checked" binding only responds to checkboxes and selected radio buttons
            }

            var modelValue = valueAccessor(), unwrappedValue = ko.utils.unwrapObservable(modelValue);
            if ((element.type == "checkbox") && (unwrappedValue instanceof Array)) {
                // For checkboxes bound to an array, we add/remove the checkbox value to that array
                // This works for both observable and non-observable arrays
                var existingEntryIndex = ko.utils.arrayIndexOf(unwrappedValue, element.value);
                if (element.checked && (existingEntryIndex < 0))
                    modelValue.push(element.value);
                else if ((!element.checked) && (existingEntryIndex >= 0))
                    modelValue.splice(existingEntryIndex, 1);
            } else {
                ko.expressionRewriting.writeValueToProperty(modelValue, allBindingsAccessor, 'checked', valueToWrite, true);
            }
        };
        ko.utils.registerEventHandler(element, "click", updateHandler);

        // IE 6 won't allow radio buttons to be selected unless they have a name
        if ((element.type == "radio") && !element.name)
            ko.bindingHandlers['uniqueName']['init'](element, function() { return true });
    },
    'update': function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());

        if (element.type == "checkbox") {
            if (value instanceof Array) {
                // When bound to an array, the checkbox being checked represents its value being present in that array
                element.checked = ko.utils.arrayIndexOf(value, element.value) >= 0;
            } else {
                // When bound to anything other value (not an array), the checkbox being checked represents the value being trueish
                element.checked = value;
            }
        } else if (element.type == "radio") {
            element.checked = (element.value == value);
        }
    }
};
var classesWrittenByBindingKey = '__ko__cssValue';
ko.bindingHandlers['css'] = {
    'update': function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        if (typeof value == "object") {
            for (var className in value) {
                var shouldHaveClass = ko.utils.unwrapObservable(value[className]);
                ko.utils.toggleDomNodeCssClass(element, className, shouldHaveClass);
            }
        } else {
            value = String(value || ''); // Make sure we don't try to store or set a non-string value
            ko.utils.toggleDomNodeCssClass(element, element[classesWrittenByBindingKey], false);
            element[classesWrittenByBindingKey] = value;
            ko.utils.toggleDomNodeCssClass(element, value, true);
        }
    }
};
ko.bindingHandlers['enable'] = {
    'update': function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        if (value && element.disabled)
            element.removeAttribute("disabled");
        else if ((!value) && (!element.disabled))
            element.disabled = true;
    }
};

ko.bindingHandlers['disable'] = {
    'update': function (element, valueAccessor) {
        ko.bindingHandlers['enable']['update'](element, function() { return !ko.utils.unwrapObservable(valueAccessor()) });
    }
};
// For certain common events (currently just 'click'), allow a simplified data-binding syntax
// e.g. click:handler instead of the usual full-length event:{click:handler}
function makeEventHandlerShortcut(eventName) {
    ko.bindingHandlers[eventName] = {
        'init': function(element, valueAccessor, allBindingsAccessor, viewModel) {
            var newValueAccessor = function () {
                var result = {};
                result[eventName] = valueAccessor();
                return result;
            };
            return ko.bindingHandlers['event']['init'].call(this, element, newValueAccessor, allBindingsAccessor, viewModel);
        }
    }
}

ko.bindingHandlers['event'] = {
    'init' : function (element, valueAccessor, allBindingsAccessor, viewModel) {
        var eventsToHandle = valueAccessor() || {};
        for(var eventNameOutsideClosure in eventsToHandle) {
            (function() {
                var eventName = eventNameOutsideClosure; // Separate variable to be captured by event handler closure
                if (typeof eventName == "string") {
                    ko.utils.registerEventHandler(element, eventName, function (event) {
                        var handlerReturnValue;
                        var handlerFunction = valueAccessor()[eventName];
                        if (!handlerFunction)
                            return;
                        var allBindings = allBindingsAccessor();

                        try {
                            // Take all the event args, and prefix with the viewmodel
                            var argsForHandler = ko.utils.makeArray(arguments);
                            argsForHandler.unshift(viewModel);
                            handlerReturnValue = handlerFunction.apply(viewModel, argsForHandler);
                        } finally {
                            if (handlerReturnValue !== true) { // Normally we want to prevent default action. Developer can override this be explicitly returning true.
                                if (event.preventDefault)
                                    event.preventDefault();
                                else
                                    event.returnValue = false;
                            }
                        }

                        var bubble = allBindings[eventName + 'Bubble'] !== false;
                        if (!bubble) {
                            event.cancelBubble = true;
                            if (event.stopPropagation)
                                event.stopPropagation();
                        }
                    });
                }
            })();
        }
    }
};
// "foreach: someExpression" is equivalent to "template: { foreach: someExpression }"
// "foreach: { data: someExpression, afterAdd: myfn }" is equivalent to "template: { foreach: someExpression, afterAdd: myfn }"
ko.bindingHandlers['foreach'] = {
    makeTemplateValueAccessor: function(valueAccessor) {
        return function() {
            var modelValue = valueAccessor(),
                unwrappedValue = ko.utils.peekObservable(modelValue);    // Unwrap without setting a dependency here

            // If unwrappedValue is the array, pass in the wrapped value on its own
            // The value will be unwrapped and tracked within the template binding
            // (See https://github.com/SteveSanderson/knockout/issues/523)
            if ((!unwrappedValue) || typeof unwrappedValue.length == "number")
                return { 'foreach': modelValue, 'templateEngine': ko.nativeTemplateEngine.instance };

            // If unwrappedValue.data is the array, preserve all relevant options and unwrap again value so we get updates
            ko.utils.unwrapObservable(modelValue);
            return {
                'foreach': unwrappedValue['data'],
                'as': unwrappedValue['as'],
                'includeDestroyed': unwrappedValue['includeDestroyed'],
                'afterAdd': unwrappedValue['afterAdd'],
                'beforeRemove': unwrappedValue['beforeRemove'],
                'afterRender': unwrappedValue['afterRender'],
                'beforeMove': unwrappedValue['beforeMove'],
                'afterMove': unwrappedValue['afterMove'],
                'templateEngine': ko.nativeTemplateEngine.instance
            };
        };
    },
    'init': function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
        return ko.bindingHandlers['template']['init'](element, ko.bindingHandlers['foreach'].makeTemplateValueAccessor(valueAccessor));
    },
    'update': function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
        return ko.bindingHandlers['template']['update'](element, ko.bindingHandlers['foreach'].makeTemplateValueAccessor(valueAccessor), allBindingsAccessor, viewModel, bindingContext);
    }
};
ko.expressionRewriting.bindingRewriteValidators['foreach'] = false; // Can't rewrite control flow bindings
ko.virtualElements.allowedBindings['foreach'] = true;
var hasfocusUpdatingProperty = '__ko_hasfocusUpdating';
ko.bindingHandlers['hasfocus'] = {
    'init': function(element, valueAccessor, allBindingsAccessor) {
        var handleElementFocusChange = function(isFocused) {
            // Where possible, ignore which event was raised and determine focus state using activeElement,
            // as this avoids phantom focus/blur events raised when changing tabs in modern browsers.
            // However, not all KO-targeted browsers (Firefox 2) support activeElement. For those browsers,
            // prevent a loss of focus when changing tabs/windows by setting a flag that prevents hasfocus
            // from calling 'blur()' on the element when it loses focus.
            // Discussion at https://github.com/SteveSanderson/knockout/pull/352
            element[hasfocusUpdatingProperty] = true;
            var ownerDoc = element.ownerDocument;
            if ("activeElement" in ownerDoc) {
                isFocused = (ownerDoc.activeElement === element);
            }
            var modelValue = valueAccessor();
            ko.expressionRewriting.writeValueToProperty(modelValue, allBindingsAccessor, 'hasfocus', isFocused, true);
            element[hasfocusUpdatingProperty] = false;
        };
        var handleElementFocusIn = handleElementFocusChange.bind(null, true);
        var handleElementFocusOut = handleElementFocusChange.bind(null, false);

        ko.utils.registerEventHandler(element, "focus", handleElementFocusIn);
        ko.utils.registerEventHandler(element, "focusin", handleElementFocusIn); // For IE
        ko.utils.registerEventHandler(element, "blur",  handleElementFocusOut);
        ko.utils.registerEventHandler(element, "focusout",  handleElementFocusOut); // For IE
    },
    'update': function(element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        if (!element[hasfocusUpdatingProperty]) {
            value ? element.focus() : element.blur();
            ko.dependencyDetection.ignore(ko.utils.triggerEvent, null, [element, value ? "focusin" : "focusout"]); // For IE, which doesn't reliably fire "focus" or "blur" events synchronously
        }
    }
};
ko.bindingHandlers['html'] = {
    'init': function() {
        // Prevent binding on the dynamically-injected HTML (as developers are unlikely to expect that, and it has security implications)
        return { 'controlsDescendantBindings': true };
    },
    'update': function (element, valueAccessor) {
        // setHtml will unwrap the value if needed
        ko.utils.setHtml(element, valueAccessor());
    }
};
var withIfDomDataKey = '__ko_withIfBindingData';
// Makes a binding like with or if
function makeWithIfBinding(bindingKey, isWith, isNot, makeContextCallback) {
    ko.bindingHandlers[bindingKey] = {
        'init': function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
            ko.utils.domData.set(element, withIfDomDataKey, {});
            return { 'controlsDescendantBindings': true };
        },
        'update': function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
            var withIfData = ko.utils.domData.get(element, withIfDomDataKey),
                dataValue = ko.utils.unwrapObservable(valueAccessor()),
                shouldDisplay = !isNot !== !dataValue, // equivalent to isNot ? !dataValue : !!dataValue
                isFirstRender = !withIfData.savedNodes,
                needsRefresh = isFirstRender || isWith || (shouldDisplay !== withIfData.didDisplayOnLastUpdate);

            if (needsRefresh) {
                if (isFirstRender) {
                    withIfData.savedNodes = ko.utils.cloneNodes(ko.virtualElements.childNodes(element), true /* shouldCleanNodes */);
                }

                if (shouldDisplay) {
                    if (!isFirstRender) {
                        ko.virtualElements.setDomNodeChildren(element, ko.utils.cloneNodes(withIfData.savedNodes));
                    }
                    ko.applyBindingsToDescendants(makeContextCallback ? makeContextCallback(bindingContext, dataValue) : bindingContext, element);
                } else {
                    ko.virtualElements.emptyNode(element);
                }

                withIfData.didDisplayOnLastUpdate = shouldDisplay;
            }
        }
    };
    ko.expressionRewriting.bindingRewriteValidators[bindingKey] = false; // Can't rewrite control flow bindings
    ko.virtualElements.allowedBindings[bindingKey] = true;
}

// Construct the actual binding handlers
makeWithIfBinding('if');
makeWithIfBinding('ifnot', false /* isWith */, true /* isNot */);
makeWithIfBinding('with', true /* isWith */, false /* isNot */,
    function(bindingContext, dataValue) {
        return bindingContext['createChildContext'](dataValue);
    }
);
function ensureDropdownSelectionIsConsistentWithModelValue(element, modelValue, preferModelValue) {
    if (preferModelValue) {
        if (modelValue !== ko.selectExtensions.readValue(element))
            ko.selectExtensions.writeValue(element, modelValue);
    }

    // No matter which direction we're syncing in, we want the end result to be equality between dropdown value and model value.
    // If they aren't equal, either we prefer the dropdown value, or the model value couldn't be represented, so either way,
    // change the model value to match the dropdown.
    if (modelValue !== ko.selectExtensions.readValue(element))
        ko.dependencyDetection.ignore(ko.utils.triggerEvent, null, [element, "change"]);
};

ko.bindingHandlers['options'] = {
    'update': function (element, valueAccessor, allBindingsAccessor) {
        if (ko.utils.tagNameLower(element) !== "select")
            throw new Error("options binding applies only to SELECT elements");

        var selectWasPreviouslyEmpty = element.length == 0;
        var previousSelectedValues = ko.utils.arrayMap(ko.utils.arrayFilter(element.childNodes, function (node) {
            return node.tagName && (ko.utils.tagNameLower(node) === "option") && node.selected;
        }), function (node) {
            return ko.selectExtensions.readValue(node) || node.innerText || node.textContent;
        });
        var previousScrollTop = element.scrollTop;

        var value = ko.utils.unwrapObservable(valueAccessor());
        var selectedValue = element.value;

        // Remove all existing <option>s.
        // Need to use .remove() rather than .removeChild() for <option>s otherwise IE behaves oddly (https://github.com/SteveSanderson/knockout/issues/134)
        while (element.length > 0) {
            ko.cleanNode(element.options[0]);
            element.remove(0);
        }

        if (value) {
            var allBindings = allBindingsAccessor(),
                includeDestroyed = allBindings['optionsIncludeDestroyed'];

            if (typeof value.length != "number")
                value = [value];
            if (allBindings['optionsCaption']) {
                var option = document.createElement("option");
                ko.utils.setHtml(option, allBindings['optionsCaption']);
                ko.selectExtensions.writeValue(option, undefined);
                element.appendChild(option);
            }

            for (var i = 0, j = value.length; i < j; i++) {
                // Skip destroyed items
                var arrayEntry = value[i];
                if (arrayEntry && arrayEntry['_destroy'] && !includeDestroyed)
                    continue;

                var option = document.createElement("option");

                function applyToObject(object, predicate, defaultValue) {
                    var predicateType = typeof predicate;
                    if (predicateType == "function")    // Given a function; run it against the data value
                        return predicate(object);
                    else if (predicateType == "string") // Given a string; treat it as a property name on the data value
                        return object[predicate];
                    else                                // Given no optionsText arg; use the data value itself
                        return defaultValue;
                }

                // Apply a value to the option element
                var optionValue = applyToObject(arrayEntry, allBindings['optionsValue'], arrayEntry);
                ko.selectExtensions.writeValue(option, ko.utils.unwrapObservable(optionValue));

                // Apply some text to the option element
                var optionText = applyToObject(arrayEntry, allBindings['optionsText'], optionValue);
                ko.utils.setTextContent(option, optionText);

                element.appendChild(option);
            }

            // IE6 doesn't like us to assign selection to OPTION nodes before they're added to the document.
            // That's why we first added them without selection. Now it's time to set the selection.
            var newOptions = element.getElementsByTagName("option");
            var countSelectionsRetained = 0;
            for (var i = 0, j = newOptions.length; i < j; i++) {
                if (ko.utils.arrayIndexOf(previousSelectedValues, ko.selectExtensions.readValue(newOptions[i])) >= 0) {
                    ko.utils.setOptionNodeSelectionState(newOptions[i], true);
                    countSelectionsRetained++;
                }
            }

            element.scrollTop = previousScrollTop;

            if (selectWasPreviouslyEmpty && ('value' in allBindings)) {
                // Ensure consistency between model value and selected option.
                // If the dropdown is being populated for the first time here (or was otherwise previously empty),
                // the dropdown selection state is meaningless, so we preserve the model value.
                ensureDropdownSelectionIsConsistentWithModelValue(element, ko.utils.peekObservable(allBindings['value']), /* preferModelValue */ true);
            }

            // Workaround for IE9 bug
            ko.utils.ensureSelectElementIsRenderedCorrectly(element);
        }
    }
};
ko.bindingHandlers['options'].optionValueDomDataKey = '__ko.optionValueDomData__';
ko.bindingHandlers['selectedOptions'] = {
    'init': function (element, valueAccessor, allBindingsAccessor) {
        ko.utils.registerEventHandler(element, "change", function () {
            var value = valueAccessor(), valueToWrite = [];
            ko.utils.arrayForEach(element.getElementsByTagName("option"), function(node) {
                if (node.selected)
                    valueToWrite.push(ko.selectExtensions.readValue(node));
            });
            ko.expressionRewriting.writeValueToProperty(value, allBindingsAccessor, 'value', valueToWrite);
        });
    },
    'update': function (element, valueAccessor) {
        if (ko.utils.tagNameLower(element) != "select")
            throw new Error("values binding applies only to SELECT elements");

        var newValue = ko.utils.unwrapObservable(valueAccessor());
        if (newValue && typeof newValue.length == "number") {
            ko.utils.arrayForEach(element.getElementsByTagName("option"), function(node) {
                var isSelected = ko.utils.arrayIndexOf(newValue, ko.selectExtensions.readValue(node)) >= 0;
                ko.utils.setOptionNodeSelectionState(node, isSelected);
            });
        }
    }
};
ko.bindingHandlers['style'] = {
    'update': function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor() || {});
        for (var styleName in value) {
            if (typeof styleName == "string") {
                var styleValue = ko.utils.unwrapObservable(value[styleName]);
                element.style[styleName] = styleValue || ""; // Empty string removes the value, whereas null/undefined have no effect
            }
        }
    }
};
ko.bindingHandlers['submit'] = {
    'init': function (element, valueAccessor, allBindingsAccessor, viewModel) {
        if (typeof valueAccessor() != "function")
            throw new Error("The value for a submit binding must be a function");
        ko.utils.registerEventHandler(element, "submit", function (event) {
            var handlerReturnValue;
            var value = valueAccessor();
            try { handlerReturnValue = value.call(viewModel, element); }
            finally {
                if (handlerReturnValue !== true) { // Normally we want to prevent default action. Developer can override this be explicitly returning true.
                    if (event.preventDefault)
                        event.preventDefault();
                    else
                        event.returnValue = false;
                }
            }
        });
    }
};
ko.bindingHandlers['text'] = {
    'update': function (element, valueAccessor) {
        ko.utils.setTextContent(element, valueAccessor());
    }
};
ko.virtualElements.allowedBindings['text'] = true;
ko.bindingHandlers['uniqueName'] = {
    'init': function (element, valueAccessor) {
        if (valueAccessor()) {
            var name = "ko_unique_" + (++ko.bindingHandlers['uniqueName'].currentIndex);
            ko.utils.setElementName(element, name);
        }
    }
};
ko.bindingHandlers['uniqueName'].currentIndex = 0;
ko.bindingHandlers['value'] = {
    'init': function (element, valueAccessor, allBindingsAccessor) {
        // Always catch "change" event; possibly other events too if asked
        var eventsToCatch = ["change"];
        var requestedEventsToCatch = allBindingsAccessor()["valueUpdate"];
        var propertyChangedFired = false;
        if (requestedEventsToCatch) {
            if (typeof requestedEventsToCatch == "string") // Allow both individual event names, and arrays of event names
                requestedEventsToCatch = [requestedEventsToCatch];
            ko.utils.arrayPushAll(eventsToCatch, requestedEventsToCatch);
            eventsToCatch = ko.utils.arrayGetDistinctValues(eventsToCatch);
        }

        var valueUpdateHandler = function() {
            propertyChangedFired = false;
            var modelValue = valueAccessor();
            var elementValue = ko.selectExtensions.readValue(element);
            ko.expressionRewriting.writeValueToProperty(modelValue, allBindingsAccessor, 'value', elementValue);
        }

        // Workaround for https://github.com/SteveSanderson/knockout/issues/122
        // IE doesn't fire "change" events on textboxes if the user selects a value from its autocomplete list
        var ieAutoCompleteHackNeeded = ko.utils.ieVersion && element.tagName.toLowerCase() == "input" && element.type == "text"
                                       && element.autocomplete != "off" && (!element.form || element.form.autocomplete != "off");
        if (ieAutoCompleteHackNeeded && ko.utils.arrayIndexOf(eventsToCatch, "propertychange") == -1) {
            ko.utils.registerEventHandler(element, "propertychange", function () { propertyChangedFired = true });
            ko.utils.registerEventHandler(element, "blur", function() {
                if (propertyChangedFired) {
                    valueUpdateHandler();
                }
            });
        }

        ko.utils.arrayForEach(eventsToCatch, function(eventName) {
            // The syntax "after<eventname>" means "run the handler asynchronously after the event"
            // This is useful, for example, to catch "keydown" events after the browser has updated the control
            // (otherwise, ko.selectExtensions.readValue(this) will receive the control's value *before* the key event)
            var handler = valueUpdateHandler;
            if (ko.utils.stringStartsWith(eventName, "after")) {
                handler = function() { setTimeout(valueUpdateHandler, 0) };
                eventName = eventName.substring("after".length);
            }
            ko.utils.registerEventHandler(element, eventName, handler);
        });
    },
    'update': function (element, valueAccessor) {
        var valueIsSelectOption = ko.utils.tagNameLower(element) === "select";
        var newValue = ko.utils.unwrapObservable(valueAccessor());
        var elementValue = ko.selectExtensions.readValue(element);
        var valueHasChanged = (newValue != elementValue);

        // JavaScript's 0 == "" behavious is unfortunate here as it prevents writing 0 to an empty text box (loose equality suggests the values are the same).
        // We don't want to do a strict equality comparison as that is more confusing for developers in certain cases, so we specifically special case 0 != "" here.
        if ((newValue === 0) && (elementValue !== 0) && (elementValue !== "0"))
            valueHasChanged = true;

        if (valueHasChanged) {
            var applyValueAction = function () { ko.selectExtensions.writeValue(element, newValue); };
            applyValueAction();

            // Workaround for IE6 bug: It won't reliably apply values to SELECT nodes during the same execution thread
            // right after you've changed the set of OPTION nodes on it. So for that node type, we'll schedule a second thread
            // to apply the value as well.
            var alsoApplyAsynchronously = valueIsSelectOption;
            if (alsoApplyAsynchronously)
                setTimeout(applyValueAction, 0);
        }

        // If you try to set a model value that can't be represented in an already-populated dropdown, reject that change,
        // because you're not allowed to have a model value that disagrees with a visible UI selection.
        if (valueIsSelectOption && (element.length > 0))
            ensureDropdownSelectionIsConsistentWithModelValue(element, newValue, /* preferModelValue */ false);
    }
};
ko.bindingHandlers['visible'] = {
    'update': function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        var isCurrentlyVisible = !(element.style.display == "none");
        if (value && !isCurrentlyVisible)
            element.style.display = "";
        else if ((!value) && isCurrentlyVisible)
            element.style.display = "none";
    }
};
// 'click' is just a shorthand for the usual full-length event:{click:handler}
makeEventHandlerShortcut('click');
// If you want to make a custom template engine,
//
// [1] Inherit from this class (like ko.nativeTemplateEngine does)
// [2] Override 'renderTemplateSource', supplying a function with this signature:
//
//        function (templateSource, bindingContext, options) {
//            // - templateSource.text() is the text of the template you should render
//            // - bindingContext.$data is the data you should pass into the template
//            //   - you might also want to make bindingContext.$parent, bindingContext.$parents,
//            //     and bindingContext.$root available in the template too
//            // - options gives you access to any other properties set on "data-bind: { template: options }"
//            //
//            // Return value: an array of DOM nodes
//        }
//
// [3] Override 'createJavaScriptEvaluatorBlock', supplying a function with this signature:
//
//        function (script) {
//            // Return value: Whatever syntax means "Evaluate the JavaScript statement 'script' and output the result"
//            //               For example, the jquery.tmpl template engine converts 'someScript' to '${ someScript }'
//        }
//
//     This is only necessary if you want to allow data-bind attributes to reference arbitrary template variables.
//     If you don't want to allow that, you can set the property 'allowTemplateRewriting' to false (like ko.nativeTemplateEngine does)
//     and then you don't need to override 'createJavaScriptEvaluatorBlock'.

ko.templateEngine = function () { };

ko.templateEngine.prototype['renderTemplateSource'] = function (templateSource, bindingContext, options) {
    throw new Error("Override renderTemplateSource");
};

ko.templateEngine.prototype['createJavaScriptEvaluatorBlock'] = function (script) {
    throw new Error("Override createJavaScriptEvaluatorBlock");
};

ko.templateEngine.prototype['makeTemplateSource'] = function(template, templateDocument) {
    // Named template
    if (typeof template == "string") {
        templateDocument = templateDocument || document;
        var elem = templateDocument.getElementById(template);
        if (!elem)
            throw new Error("Cannot find template with ID " + template);
        return new ko.templateSources.domElement(elem);
    } else if ((template.nodeType == 1) || (template.nodeType == 8)) {
        // Anonymous template
        return new ko.templateSources.anonymousTemplate(template);
    } else
        throw new Error("Unknown template type: " + template);
};

ko.templateEngine.prototype['renderTemplate'] = function (template, bindingContext, options, templateDocument) {
    var templateSource = this['makeTemplateSource'](template, templateDocument);
    return this['renderTemplateSource'](templateSource, bindingContext, options);
};

ko.templateEngine.prototype['isTemplateRewritten'] = function (template, templateDocument) {
    // Skip rewriting if requested
    if (this['allowTemplateRewriting'] === false)
        return true;
    return this['makeTemplateSource'](template, templateDocument)['data']("isRewritten");
};

ko.templateEngine.prototype['rewriteTemplate'] = function (template, rewriterCallback, templateDocument) {
    var templateSource = this['makeTemplateSource'](template, templateDocument);
    var rewritten = rewriterCallback(templateSource['text']());
    templateSource['text'](rewritten);
    templateSource['data']("isRewritten", true);
};

ko.exportSymbol('templateEngine', ko.templateEngine);

ko.templateRewriting = (function () {
    var memoizeDataBindingAttributeSyntaxRegex = /(<[a-z]+\d*(\s+(?!data-bind=)[a-z0-9\-]+(=(\"[^\"]*\"|\'[^\']*\'))?)*\s+)data-bind=(["'])([\s\S]*?)\5/gi;
    var memoizeVirtualContainerBindingSyntaxRegex = /<!--\s*ko\b\s*([\s\S]*?)\s*-->/g;

    function validateDataBindValuesForRewriting(keyValueArray) {
        var allValidators = ko.expressionRewriting.bindingRewriteValidators;
        for (var i = 0; i < keyValueArray.length; i++) {
            var key = keyValueArray[i]['key'];
            if (allValidators.hasOwnProperty(key)) {
                var validator = allValidators[key];

                if (typeof validator === "function") {
                    var possibleErrorMessage = validator(keyValueArray[i]['value']);
                    if (possibleErrorMessage)
                        throw new Error(possibleErrorMessage);
                } else if (!validator) {
                    throw new Error("This template engine does not support the '" + key + "' binding within its templates");
                }
            }
        }
    }

    function constructMemoizedTagReplacement(dataBindAttributeValue, tagToRetain, templateEngine) {
        var dataBindKeyValueArray = ko.expressionRewriting.parseObjectLiteral(dataBindAttributeValue);
        validateDataBindValuesForRewriting(dataBindKeyValueArray);
        var rewrittenDataBindAttributeValue = ko.expressionRewriting.preProcessBindings(dataBindKeyValueArray);

        // For no obvious reason, Opera fails to evaluate rewrittenDataBindAttributeValue unless it's wrapped in an additional
        // anonymous function, even though Opera's built-in debugger can evaluate it anyway. No other browser requires this
        // extra indirection.
        var applyBindingsToNextSiblingScript =
            "ko.__tr_ambtns(function($context,$element){return(function(){return{ " + rewrittenDataBindAttributeValue + " } })()})";
        return templateEngine['createJavaScriptEvaluatorBlock'](applyBindingsToNextSiblingScript) + tagToRetain;
    }

    return {
        ensureTemplateIsRewritten: function (template, templateEngine, templateDocument) {
            if (!templateEngine['isTemplateRewritten'](template, templateDocument))
                templateEngine['rewriteTemplate'](template, function (htmlString) {
                    return ko.templateRewriting.memoizeBindingAttributeSyntax(htmlString, templateEngine);
                }, templateDocument);
        },

        memoizeBindingAttributeSyntax: function (htmlString, templateEngine) {
            return htmlString.replace(memoizeDataBindingAttributeSyntaxRegex, function () {
                return constructMemoizedTagReplacement(/* dataBindAttributeValue: */ arguments[6], /* tagToRetain: */ arguments[1], templateEngine);
            }).replace(memoizeVirtualContainerBindingSyntaxRegex, function() {
                return constructMemoizedTagReplacement(/* dataBindAttributeValue: */ arguments[1], /* tagToRetain: */ "<!-- ko -->", templateEngine);
            });
        },

        applyMemoizedBindingsToNextSibling: function (bindings) {
            return ko.memoization.memoize(function (domNode, bindingContext) {
                if (domNode.nextSibling)
                    ko.applyBindingsToNode(domNode.nextSibling, bindings, bindingContext);
            });
        }
    }
})();


// Exported only because it has to be referenced by string lookup from within rewritten template
ko.exportSymbol('__tr_ambtns', ko.templateRewriting.applyMemoizedBindingsToNextSibling);
(function() {
    // A template source represents a read/write way of accessing a template. This is to eliminate the need for template loading/saving
    // logic to be duplicated in every template engine (and means they can all work with anonymous templates, etc.)
    //
    // Two are provided by default:
    //  1. ko.templateSources.domElement       - reads/writes the text content of an arbitrary DOM element
    //  2. ko.templateSources.anonymousElement - uses ko.utils.domData to read/write text *associated* with the DOM element, but
    //                                           without reading/writing the actual element text content, since it will be overwritten
    //                                           with the rendered template output.
    // You can implement your own template source if you want to fetch/store templates somewhere other than in DOM elements.
    // Template sources need to have the following functions:
    //   text() 			- returns the template text from your storage location
    //   text(value)		- writes the supplied template text to your storage location
    //   data(key)			- reads values stored using data(key, value) - see below
    //   data(key, value)	- associates "value" with this template and the key "key". Is used to store information like "isRewritten".
    //
    // Optionally, template sources can also have the following functions:
    //   nodes()            - returns a DOM element containing the nodes of this template, where available
    //   nodes(value)       - writes the given DOM element to your storage location
    // If a DOM element is available for a given template source, template engines are encouraged to use it in preference over text()
    // for improved speed. However, all templateSources must supply text() even if they don't supply nodes().
    //
    // Once you've implemented a templateSource, make your template engine use it by subclassing whatever template engine you were
    // using and overriding "makeTemplateSource" to return an instance of your custom template source.

    ko.templateSources = {};

    // ---- ko.templateSources.domElement -----

    ko.templateSources.domElement = function(element) {
        this.domElement = element;
    }

    ko.templateSources.domElement.prototype['text'] = function(/* valueToWrite */) {
        var tagNameLower = ko.utils.tagNameLower(this.domElement),
            elemContentsProperty = tagNameLower === "script" ? "text"
                                 : tagNameLower === "textarea" ? "value"
                                 : "innerHTML";

        if (arguments.length == 0) {
            return this.domElement[elemContentsProperty];
        } else {
            var valueToWrite = arguments[0];
            if (elemContentsProperty === "innerHTML")
                ko.utils.setHtml(this.domElement, valueToWrite);
            else
                this.domElement[elemContentsProperty] = valueToWrite;
        }
    };

    ko.templateSources.domElement.prototype['data'] = function(key /*, valueToWrite */) {
        if (arguments.length === 1) {
            return ko.utils.domData.get(this.domElement, "templateSourceData_" + key);
        } else {
            ko.utils.domData.set(this.domElement, "templateSourceData_" + key, arguments[1]);
        }
    };

    // ---- ko.templateSources.anonymousTemplate -----
    // Anonymous templates are normally saved/retrieved as DOM nodes through "nodes".
    // For compatibility, you can also read "text"; it will be serialized from the nodes on demand.
    // Writing to "text" is still supported, but then the template data will not be available as DOM nodes.

    var anonymousTemplatesDomDataKey = "__ko_anon_template__";
    ko.templateSources.anonymousTemplate = function(element) {
        this.domElement = element;
    }
    ko.templateSources.anonymousTemplate.prototype = new ko.templateSources.domElement();
    ko.templateSources.anonymousTemplate.prototype['text'] = function(/* valueToWrite */) {
        if (arguments.length == 0) {
            var templateData = ko.utils.domData.get(this.domElement, anonymousTemplatesDomDataKey) || {};
            if (templateData.textData === undefined && templateData.containerData)
                templateData.textData = templateData.containerData.innerHTML;
            return templateData.textData;
        } else {
            var valueToWrite = arguments[0];
            ko.utils.domData.set(this.domElement, anonymousTemplatesDomDataKey, {textData: valueToWrite});
        }
    };
    ko.templateSources.domElement.prototype['nodes'] = function(/* valueToWrite */) {
        if (arguments.length == 0) {
            var templateData = ko.utils.domData.get(this.domElement, anonymousTemplatesDomDataKey) || {};
            return templateData.containerData;
        } else {
            var valueToWrite = arguments[0];
            ko.utils.domData.set(this.domElement, anonymousTemplatesDomDataKey, {containerData: valueToWrite});
        }
    };

    ko.exportSymbol('templateSources', ko.templateSources);
    ko.exportSymbol('templateSources.domElement', ko.templateSources.domElement);
    ko.exportSymbol('templateSources.anonymousTemplate', ko.templateSources.anonymousTemplate);
})();
(function () {
    var _templateEngine;
    ko.setTemplateEngine = function (templateEngine) {
        if ((templateEngine != undefined) && !(templateEngine instanceof ko.templateEngine))
            throw new Error("templateEngine must inherit from ko.templateEngine");
        _templateEngine = templateEngine;
    }

    function invokeForEachNodeOrCommentInContinuousRange(firstNode, lastNode, action) {
        var node, nextInQueue = firstNode, firstOutOfRangeNode = ko.virtualElements.nextSibling(lastNode);
        while (nextInQueue && ((node = nextInQueue) !== firstOutOfRangeNode)) {
            nextInQueue = ko.virtualElements.nextSibling(node);
            if (node.nodeType === 1 || node.nodeType === 8)
                action(node);
        }
    }

    function activateBindingsOnContinuousNodeArray(continuousNodeArray, bindingContext) {
        // To be used on any nodes that have been rendered by a template and have been inserted into some parent element
        // Walks through continuousNodeArray (which *must* be continuous, i.e., an uninterrupted sequence of sibling nodes, because
        // the algorithm for walking them relies on this), and for each top-level item in the virtual-element sense,
        // (1) Does a regular "applyBindings" to associate bindingContext with this node and to activate any non-memoized bindings
        // (2) Unmemoizes any memos in the DOM subtree (e.g., to activate bindings that had been memoized during template rewriting)

        if (continuousNodeArray.length) {
            var firstNode = continuousNodeArray[0], lastNode = continuousNodeArray[continuousNodeArray.length - 1];

            // Need to applyBindings *before* unmemoziation, because unmemoization might introduce extra nodes (that we don't want to re-bind)
            // whereas a regular applyBindings won't introduce new memoized nodes
            invokeForEachNodeOrCommentInContinuousRange(firstNode, lastNode, function(node) {
                ko.applyBindings(bindingContext, node);
            });
            invokeForEachNodeOrCommentInContinuousRange(firstNode, lastNode, function(node) {
                ko.memoization.unmemoizeDomNodeAndDescendants(node, [bindingContext]);
            });
        }
    }

    function getFirstNodeFromPossibleArray(nodeOrNodeArray) {
        return nodeOrNodeArray.nodeType ? nodeOrNodeArray
                                        : nodeOrNodeArray.length > 0 ? nodeOrNodeArray[0]
                                        : null;
    }

    function executeTemplate(targetNodeOrNodeArray, renderMode, template, bindingContext, options) {
        options = options || {};
        var firstTargetNode = targetNodeOrNodeArray && getFirstNodeFromPossibleArray(targetNodeOrNodeArray);
        var templateDocument = firstTargetNode && firstTargetNode.ownerDocument;
        var templateEngineToUse = (options['templateEngine'] || _templateEngine);
        ko.templateRewriting.ensureTemplateIsRewritten(template, templateEngineToUse, templateDocument);
        var renderedNodesArray = templateEngineToUse['renderTemplate'](template, bindingContext, options, templateDocument);

        // Loosely check result is an array of DOM nodes
        if ((typeof renderedNodesArray.length != "number") || (renderedNodesArray.length > 0 && typeof renderedNodesArray[0].nodeType != "number"))
            throw new Error("Template engine must return an array of DOM nodes");

        var haveAddedNodesToParent = false;
        switch (renderMode) {
            case "replaceChildren":
                ko.virtualElements.setDomNodeChildren(targetNodeOrNodeArray, renderedNodesArray);
                haveAddedNodesToParent = true;
                break;
            case "replaceNode":
                ko.utils.replaceDomNodes(targetNodeOrNodeArray, renderedNodesArray);
                haveAddedNodesToParent = true;
                break;
            case "ignoreTargetNode": break;
            default:
                throw new Error("Unknown renderMode: " + renderMode);
        }

        if (haveAddedNodesToParent) {
            activateBindingsOnContinuousNodeArray(renderedNodesArray, bindingContext);
            if (options['afterRender'])
                ko.dependencyDetection.ignore(options['afterRender'], null, [renderedNodesArray, bindingContext['$data']]);
        }

        return renderedNodesArray;
    }

    ko.renderTemplate = function (template, dataOrBindingContext, options, targetNodeOrNodeArray, renderMode) {
        options = options || {};
        if ((options['templateEngine'] || _templateEngine) == undefined)
            throw new Error("Set a template engine before calling renderTemplate");
        renderMode = renderMode || "replaceChildren";

        if (targetNodeOrNodeArray) {
            var firstTargetNode = getFirstNodeFromPossibleArray(targetNodeOrNodeArray);

            var whenToDispose = function () { return (!firstTargetNode) || !ko.utils.domNodeIsAttachedToDocument(firstTargetNode); }; // Passive disposal (on next evaluation)
            var activelyDisposeWhenNodeIsRemoved = (firstTargetNode && renderMode == "replaceNode") ? firstTargetNode.parentNode : firstTargetNode;

            return ko.dependentObservable( // So the DOM is automatically updated when any dependency changes
                function () {
                    // Ensure we've got a proper binding context to work with
                    var bindingContext = (dataOrBindingContext && (dataOrBindingContext instanceof ko.bindingContext))
                        ? dataOrBindingContext
                        : new ko.bindingContext(ko.utils.unwrapObservable(dataOrBindingContext));

                    // Support selecting template as a function of the data being rendered
                    var templateName = typeof(template) == 'function' ? template(bindingContext['$data'], bindingContext) : template;

                    var renderedNodesArray = executeTemplate(targetNodeOrNodeArray, renderMode, templateName, bindingContext, options);
                    if (renderMode == "replaceNode") {
                        targetNodeOrNodeArray = renderedNodesArray;
                        firstTargetNode = getFirstNodeFromPossibleArray(targetNodeOrNodeArray);
                    }
                },
                null,
                { disposeWhen: whenToDispose, disposeWhenNodeIsRemoved: activelyDisposeWhenNodeIsRemoved }
            );
        } else {
            // We don't yet have a DOM node to evaluate, so use a memo and render the template later when there is a DOM node
            return ko.memoization.memoize(function (domNode) {
                ko.renderTemplate(template, dataOrBindingContext, options, domNode, "replaceNode");
            });
        }
    };

    ko.renderTemplateForEach = function (template, arrayOrObservableArray, options, targetNode, parentBindingContext) {
        // Since setDomNodeChildrenFromArrayMapping always calls executeTemplateForArrayItem and then
        // activateBindingsCallback for added items, we can store the binding context in the former to use in the latter.
        var arrayItemContext;

        // This will be called by setDomNodeChildrenFromArrayMapping to get the nodes to add to targetNode
        var executeTemplateForArrayItem = function (arrayValue, index) {
            // Support selecting template as a function of the data being rendered
            arrayItemContext = parentBindingContext['createChildContext'](ko.utils.unwrapObservable(arrayValue), options['as']);
            arrayItemContext['$index'] = index;
            var templateName = typeof(template) == 'function' ? template(arrayValue, arrayItemContext) : template;
            return executeTemplate(null, "ignoreTargetNode", templateName, arrayItemContext, options);
        }

        // This will be called whenever setDomNodeChildrenFromArrayMapping has added nodes to targetNode
        var activateBindingsCallback = function(arrayValue, addedNodesArray, index) {
            activateBindingsOnContinuousNodeArray(addedNodesArray, arrayItemContext);
            if (options['afterRender'])
                options['afterRender'](addedNodesArray, arrayValue);
        };

        return ko.dependentObservable(function () {
            var unwrappedArray = ko.utils.unwrapObservable(arrayOrObservableArray) || [];
            if (typeof unwrappedArray.length == "undefined") // Coerce single value into array
                unwrappedArray = [unwrappedArray];

            // Filter out any entries marked as destroyed
            var filteredArray = ko.utils.arrayFilter(unwrappedArray, function(item) {
                return options['includeDestroyed'] || item === undefined || item === null || !ko.utils.unwrapObservable(item['_destroy']);
            });

            // Call setDomNodeChildrenFromArrayMapping, ignoring any observables unwrapped within (most likely from a callback function).
            // If the array items are observables, though, they will be unwrapped in executeTemplateForArrayItem and managed within setDomNodeChildrenFromArrayMapping.
            ko.dependencyDetection.ignore(ko.utils.setDomNodeChildrenFromArrayMapping, null, [targetNode, filteredArray, executeTemplateForArrayItem, options, activateBindingsCallback]);

        }, null, { disposeWhenNodeIsRemoved: targetNode });
    };

    var templateComputedDomDataKey = '__ko__templateComputedDomDataKey__';
    function disposeOldComputedAndStoreNewOne(element, newComputed) {
        var oldComputed = ko.utils.domData.get(element, templateComputedDomDataKey);
        if (oldComputed && (typeof(oldComputed.dispose) == 'function'))
            oldComputed.dispose();
        ko.utils.domData.set(element, templateComputedDomDataKey, (newComputed && newComputed.isActive()) ? newComputed : undefined);
    }

    ko.bindingHandlers['template'] = {
        'init': function(element, valueAccessor) {
            // Support anonymous templates
            var bindingValue = ko.utils.unwrapObservable(valueAccessor());
            if ((typeof bindingValue != "string") && (!bindingValue['name']) && (element.nodeType == 1 || element.nodeType == 8)) {
                // It's an anonymous template - store the element contents, then clear the element
                var templateNodes = element.nodeType == 1 ? element.childNodes : ko.virtualElements.childNodes(element),
                    container = ko.utils.moveCleanedNodesToContainerElement(templateNodes); // This also removes the nodes from their current parent
                new ko.templateSources.anonymousTemplate(element)['nodes'](container);
            }
            return { 'controlsDescendantBindings': true };
        },
        'update': function (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
            var templateName = ko.utils.unwrapObservable(valueAccessor()),
                options = {},
                shouldDisplay = true,
                dataValue,
                templateComputed = null;

            if (typeof templateName != "string") {
                options = templateName;
                templateName = options['name'];

                // Support "if"/"ifnot" conditions
                if ('if' in options)
                    shouldDisplay = ko.utils.unwrapObservable(options['if']);
                if (shouldDisplay && 'ifnot' in options)
                    shouldDisplay = !ko.utils.unwrapObservable(options['ifnot']);

                dataValue = ko.utils.unwrapObservable(options['data']);
            }

            if ('foreach' in options) {
                // Render once for each data point (treating data set as empty if shouldDisplay==false)
                var dataArray = (shouldDisplay && options['foreach']) || [];
                templateComputed = ko.renderTemplateForEach(templateName || element, dataArray, options, element, bindingContext);
            } else if (!shouldDisplay) {
                ko.virtualElements.emptyNode(element);
            } else {
                // Render once for this single data point (or use the viewModel if no data was provided)
                var innerBindingContext = ('data' in options) ?
                    bindingContext['createChildContext'](dataValue, options['as']) :  // Given an explitit 'data' value, we create a child binding context for it
                    bindingContext;                                                        // Given no explicit 'data' value, we retain the same binding context
                templateComputed = ko.renderTemplate(templateName || element, innerBindingContext, options, element);
            }

            // It only makes sense to have a single template computed per element (otherwise which one should have its output displayed?)
            disposeOldComputedAndStoreNewOne(element, templateComputed);
        }
    };

    // Anonymous templates can't be rewritten. Give a nice error message if you try to do it.
    ko.expressionRewriting.bindingRewriteValidators['template'] = function(bindingValue) {
        var parsedBindingValue = ko.expressionRewriting.parseObjectLiteral(bindingValue);

        if ((parsedBindingValue.length == 1) && parsedBindingValue[0]['unknown'])
            return null; // It looks like a string literal, not an object literal, so treat it as a named template (which is allowed for rewriting)

        if (ko.expressionRewriting.keyValueArrayContainsKey(parsedBindingValue, "name"))
            return null; // Named templates can be rewritten, so return "no error"
        return "This template engine does not support anonymous templates nested within its templates";
    };

    ko.virtualElements.allowedBindings['template'] = true;
})();

ko.exportSymbol('setTemplateEngine', ko.setTemplateEngine);
ko.exportSymbol('renderTemplate', ko.renderTemplate);

ko.utils.compareArrays = (function () {
    var statusNotInOld = 'added', statusNotInNew = 'deleted';

    // Simple calculation based on Levenshtein distance.
    function compareArrays(oldArray, newArray, dontLimitMoves) {
        oldArray = oldArray || [];
        newArray = newArray || [];

        if (oldArray.length <= newArray.length)
            return compareSmallArrayToBigArray(oldArray, newArray, statusNotInOld, statusNotInNew, dontLimitMoves);
        else
            return compareSmallArrayToBigArray(newArray, oldArray, statusNotInNew, statusNotInOld, dontLimitMoves);
    }

    function compareSmallArrayToBigArray(smlArray, bigArray, statusNotInSml, statusNotInBig, dontLimitMoves) {
        var myMin = Math.min,
            myMax = Math.max,
            editDistanceMatrix = [],
            smlIndex, smlIndexMax = smlArray.length,
            bigIndex, bigIndexMax = bigArray.length,
            compareRange = (bigIndexMax - smlIndexMax) || 1,
            maxDistance = smlIndexMax + bigIndexMax + 1,
            thisRow, lastRow,
            bigIndexMaxForRow, bigIndexMinForRow;

        for (smlIndex = 0; smlIndex <= smlIndexMax; smlIndex++) {
            lastRow = thisRow;
            editDistanceMatrix.push(thisRow = []);
            bigIndexMaxForRow = myMin(bigIndexMax, smlIndex + compareRange);
            bigIndexMinForRow = myMax(0, smlIndex - 1);
            for (bigIndex = bigIndexMinForRow; bigIndex <= bigIndexMaxForRow; bigIndex++) {
                if (!bigIndex)
                    thisRow[bigIndex] = smlIndex + 1;
                else if (!smlIndex)  // Top row - transform empty array into new array via additions
                    thisRow[bigIndex] = bigIndex + 1;
                else if (smlArray[smlIndex - 1] === bigArray[bigIndex - 1])
                    thisRow[bigIndex] = lastRow[bigIndex - 1];                  // copy value (no edit)
                else {
                    var northDistance = lastRow[bigIndex] || maxDistance;       // not in big (deletion)
                    var westDistance = thisRow[bigIndex - 1] || maxDistance;    // not in small (addition)
                    thisRow[bigIndex] = myMin(northDistance, westDistance) + 1;
                }
            }
        }

        var editScript = [], meMinusOne, notInSml = [], notInBig = [];
        for (smlIndex = smlIndexMax, bigIndex = bigIndexMax; smlIndex || bigIndex;) {
            meMinusOne = editDistanceMatrix[smlIndex][bigIndex] - 1;
            if (bigIndex && meMinusOne === editDistanceMatrix[smlIndex][bigIndex-1]) {
                notInSml.push(editScript[editScript.length] = {     // added
                    'status': statusNotInSml,
                    'value': bigArray[--bigIndex],
                    'index': bigIndex });
            } else if (smlIndex && meMinusOne === editDistanceMatrix[smlIndex - 1][bigIndex]) {
                notInBig.push(editScript[editScript.length] = {     // deleted
                    'status': statusNotInBig,
                    'value': smlArray[--smlIndex],
                    'index': smlIndex });
            } else {
                editScript.push({
                    'status': "retained",
                    'value': bigArray[--bigIndex] });
                --smlIndex;
            }
        }

        if (notInSml.length && notInBig.length) {
            // Set a limit on the number of consecutive non-matching comparisons; having it a multiple of
            // smlIndexMax keeps the time complexity of this algorithm linear.
            var limitFailedCompares = smlIndexMax * 10, failedCompares,
                a, d, notInSmlItem, notInBigItem;
            // Go through the items that have been added and deleted and try to find matches between them.
            for (failedCompares = a = 0; (dontLimitMoves || failedCompares < limitFailedCompares) && (notInSmlItem = notInSml[a]); a++) {
                for (d = 0; notInBigItem = notInBig[d]; d++) {
                    if (notInSmlItem['value'] === notInBigItem['value']) {
                        notInSmlItem['moved'] = notInBigItem['index'];
                        notInBigItem['moved'] = notInSmlItem['index'];
                        notInBig.splice(d,1);       // This item is marked as moved; so remove it from notInBig list
                        failedCompares = d = 0;     // Reset failed compares count because we're checking for consecutive failures
                        break;
                    }
                }
                failedCompares += d;
            }
        }
        return editScript.reverse();
    }

    return compareArrays;
})();

ko.exportSymbol('utils.compareArrays', ko.utils.compareArrays);

(function () {
    // Objective:
    // * Given an input array, a container DOM node, and a function from array elements to arrays of DOM nodes,
    //   map the array elements to arrays of DOM nodes, concatenate together all these arrays, and use them to populate the container DOM node
    // * Next time we're given the same combination of things (with the array possibly having mutated), update the container DOM node
    //   so that its children is again the concatenation of the mappings of the array elements, but don't re-map any array elements that we
    //   previously mapped - retain those nodes, and just insert/delete other ones

    // "callbackAfterAddingNodes" will be invoked after any "mapping"-generated nodes are inserted into the container node
    // You can use this, for example, to activate bindings on those nodes.

    function fixUpNodesToBeMovedOrRemoved(contiguousNodeArray) {
        // Before moving, deleting, or replacing a set of nodes that were previously outputted by the "map" function, we have to reconcile
        // them against what is in the DOM right now. It may be that some of the nodes have already been removed from the document,
        // or that new nodes might have been inserted in the middle, for example by a binding. Also, there may previously have been
        // leading comment nodes (created by rewritten string-based templates) that have since been removed during binding.
        // So, this function translates the old "map" output array into its best guess of what set of current DOM nodes should be removed.
        //
        // Rules:
        //   [A] Any leading nodes that aren't in the document any more should be ignored
        //       These most likely correspond to memoization nodes that were already removed during binding
        //       See https://github.com/SteveSanderson/knockout/pull/440
        //   [B] We want to output a contiguous series of nodes that are still in the document. So, ignore any nodes that
        //       have already been removed, and include any nodes that have been inserted among the previous collection

        // Rule [A]
        while (contiguousNodeArray.length && !ko.utils.domNodeIsAttachedToDocument(contiguousNodeArray[0]))
            contiguousNodeArray.splice(0, 1);

        // Rule [B]
        if (contiguousNodeArray.length > 1) {
            // Build up the actual new contiguous node set
            var current = contiguousNodeArray[0], last = contiguousNodeArray[contiguousNodeArray.length - 1], newContiguousSet = [current];
            while (current !== last) {
                current = current.nextSibling;
                if (!current) // Won't happen, except if the developer has manually removed some DOM elements (then we're in an undefined scenario)
                    return;
                newContiguousSet.push(current);
            }

            // ... then mutate the input array to match this.
            // (The following line replaces the contents of contiguousNodeArray with newContiguousSet)
            Array.prototype.splice.apply(contiguousNodeArray, [0, contiguousNodeArray.length].concat(newContiguousSet));
        }
        return contiguousNodeArray;
    }

    function mapNodeAndRefreshWhenChanged(containerNode, mapping, valueToMap, callbackAfterAddingNodes, index) {
        // Map this array value inside a dependentObservable so we re-map when any dependency changes
        var mappedNodes = [];
        var dependentObservable = ko.dependentObservable(function() {
            var newMappedNodes = mapping(valueToMap, index) || [];

            // On subsequent evaluations, just replace the previously-inserted DOM nodes
            if (mappedNodes.length > 0) {
                ko.utils.replaceDomNodes(fixUpNodesToBeMovedOrRemoved(mappedNodes), newMappedNodes);
                if (callbackAfterAddingNodes)
                    ko.dependencyDetection.ignore(callbackAfterAddingNodes, null, [valueToMap, newMappedNodes, index]);
            }

            // Replace the contents of the mappedNodes array, thereby updating the record
            // of which nodes would be deleted if valueToMap was itself later removed
            mappedNodes.splice(0, mappedNodes.length);
            ko.utils.arrayPushAll(mappedNodes, newMappedNodes);
        }, null, { disposeWhenNodeIsRemoved: containerNode, disposeWhen: function() { return (mappedNodes.length == 0) || !ko.utils.domNodeIsAttachedToDocument(mappedNodes[0]) } });
        return { mappedNodes : mappedNodes, dependentObservable : (dependentObservable.isActive() ? dependentObservable : undefined) };
    }

    var lastMappingResultDomDataKey = "setDomNodeChildrenFromArrayMapping_lastMappingResult";

    ko.utils.setDomNodeChildrenFromArrayMapping = function (domNode, array, mapping, options, callbackAfterAddingNodes) {
        // Compare the provided array against the previous one
        array = array || [];
        options = options || {};
        var isFirstExecution = ko.utils.domData.get(domNode, lastMappingResultDomDataKey) === undefined;
        var lastMappingResult = ko.utils.domData.get(domNode, lastMappingResultDomDataKey) || [];
        var lastArray = ko.utils.arrayMap(lastMappingResult, function (x) { return x.arrayEntry; });
        var editScript = ko.utils.compareArrays(lastArray, array);

        // Build the new mapping result
        var newMappingResult = [];
        var lastMappingResultIndex = 0;
        var newMappingResultIndex = 0;

        var nodesToDelete = [];
        var itemsToProcess = [];
        var itemsForBeforeRemoveCallbacks = [];
        var itemsForMoveCallbacks = [];
        var itemsForAfterAddCallbacks = [];
        var mapData;

        function itemMovedOrRetained(editScriptIndex, oldPosition) {
            mapData = lastMappingResult[oldPosition];
            if (newMappingResultIndex !== oldPosition)
                itemsForMoveCallbacks[editScriptIndex] = mapData;
            // Since updating the index might change the nodes, do so before calling fixUpNodesToBeMovedOrRemoved
            mapData.indexObservable(newMappingResultIndex++);
            fixUpNodesToBeMovedOrRemoved(mapData.mappedNodes);
            newMappingResult.push(mapData);
            itemsToProcess.push(mapData);
        }

        function callCallback(callback, items) {
            if (callback) {
                for (var i = 0, n = items.length; i < n; i++) {
                    if (items[i]) {
                        ko.utils.arrayForEach(items[i].mappedNodes, function(node) {
                            callback(node, i, items[i].arrayEntry);
                        });
                    }
                }
            }
        }

        for (var i = 0, editScriptItem, movedIndex; editScriptItem = editScript[i]; i++) {
            movedIndex = editScriptItem['moved'];
            switch (editScriptItem['status']) {
                case "deleted":
                    if (movedIndex === undefined) {
                        mapData = lastMappingResult[lastMappingResultIndex];

                        // Stop tracking changes to the mapping for these nodes
                        if (mapData.dependentObservable)
                            mapData.dependentObservable.dispose();

                        // Queue these nodes for later removal
                        nodesToDelete.push.apply(nodesToDelete, fixUpNodesToBeMovedOrRemoved(mapData.mappedNodes));
                        if (options['beforeRemove']) {
                            itemsForBeforeRemoveCallbacks[i] = mapData;
                            itemsToProcess.push(mapData);
                        }
                    }
                    lastMappingResultIndex++;
                    break;

                case "retained":
                    itemMovedOrRetained(i, lastMappingResultIndex++);
                    break;

                case "added":
                    if (movedIndex !== undefined) {
                        itemMovedOrRetained(i, movedIndex);
                    } else {
                        mapData = { arrayEntry: editScriptItem['value'], indexObservable: ko.observable(newMappingResultIndex++) };
                        newMappingResult.push(mapData);
                        itemsToProcess.push(mapData);
                        if (!isFirstExecution)
                            itemsForAfterAddCallbacks[i] = mapData;
                    }
                    break;
            }
        }

        // Call beforeMove first before any changes have been made to the DOM
        callCallback(options['beforeMove'], itemsForMoveCallbacks);

        // Next remove nodes for deleted items (or just clean if there's a beforeRemove callback)
        ko.utils.arrayForEach(nodesToDelete, options['beforeRemove'] ? ko.cleanNode : ko.removeNode);

        // Next add/reorder the remaining items (will include deleted items if there's a beforeRemove callback)
        for (var i = 0, nextNode = ko.virtualElements.firstChild(domNode), lastNode, node; mapData = itemsToProcess[i]; i++) {
            // Get nodes for newly added items
            if (!mapData.mappedNodes)
                ko.utils.extend(mapData, mapNodeAndRefreshWhenChanged(domNode, mapping, mapData.arrayEntry, callbackAfterAddingNodes, mapData.indexObservable));

            // Put nodes in the right place if they aren't there already
            for (var j = 0; node = mapData.mappedNodes[j]; nextNode = node.nextSibling, lastNode = node, j++) {
                if (node !== nextNode)
                    ko.virtualElements.insertAfter(domNode, node, lastNode);
            }

            // Run the callbacks for newly added nodes (for example, to apply bindings, etc.)
            if (!mapData.initialized && callbackAfterAddingNodes) {
                callbackAfterAddingNodes(mapData.arrayEntry, mapData.mappedNodes, mapData.indexObservable);
                mapData.initialized = true;
            }
        }

        // If there's a beforeRemove callback, call it after reordering.
        // Note that we assume that the beforeRemove callback will usually be used to remove the nodes using
        // some sort of animation, which is why we first reorder the nodes that will be removed. If the
        // callback instead removes the nodes right away, it would be more efficient to skip reordering them.
        // Perhaps we'll make that change in the future if this scenario becomes more common.
        callCallback(options['beforeRemove'], itemsForBeforeRemoveCallbacks);

        // Finally call afterMove and afterAdd callbacks
        callCallback(options['afterMove'], itemsForMoveCallbacks);
        callCallback(options['afterAdd'], itemsForAfterAddCallbacks);

        // Store a copy of the array items we just considered so we can difference it next time
        ko.utils.domData.set(domNode, lastMappingResultDomDataKey, newMappingResult);
    }
})();

ko.exportSymbol('utils.setDomNodeChildrenFromArrayMapping', ko.utils.setDomNodeChildrenFromArrayMapping);
ko.nativeTemplateEngine = function () {
    this['allowTemplateRewriting'] = false;
}

ko.nativeTemplateEngine.prototype = new ko.templateEngine();
ko.nativeTemplateEngine.prototype['renderTemplateSource'] = function (templateSource, bindingContext, options) {
    var useNodesIfAvailable = !(ko.utils.ieVersion < 9), // IE<9 cloneNode doesn't work properly
        templateNodesFunc = useNodesIfAvailable ? templateSource['nodes'] : null,
        templateNodes = templateNodesFunc ? templateSource['nodes']() : null;

    if (templateNodes) {
        return ko.utils.makeArray(templateNodes.cloneNode(true).childNodes);
    } else {
        var templateText = templateSource['text']();
        return ko.utils.parseHtmlFragment(templateText);
    }
};

ko.nativeTemplateEngine.instance = new ko.nativeTemplateEngine();
ko.setTemplateEngine(ko.nativeTemplateEngine.instance);

ko.exportSymbol('nativeTemplateEngine', ko.nativeTemplateEngine);
(function() {
    ko.jqueryTmplTemplateEngine = function () {
        // Detect which version of jquery-tmpl you're using. Unfortunately jquery-tmpl
        // doesn't expose a version number, so we have to infer it.
        // Note that as of Knockout 1.3, we only support jQuery.tmpl 1.0.0pre and later,
        // which KO internally refers to as version "2", so older versions are no longer detected.
        var jQueryTmplVersion = this.jQueryTmplVersion = (function() {
            if ((typeof(jQuery) == "undefined") || !(jQuery['tmpl']))
                return 0;
            // Since it exposes no official version number, we use our own numbering system. To be updated as jquery-tmpl evolves.
            try {
                if (jQuery['tmpl']['tag']['tmpl']['open'].toString().indexOf('__') >= 0) {
                    // Since 1.0.0pre, custom tags should append markup to an array called "__"
                    return 2; // Final version of jquery.tmpl
                }
            } catch(ex) { /* Apparently not the version we were looking for */ }

            return 1; // Any older version that we don't support
        })();

        function ensureHasReferencedJQueryTemplates() {
            if (jQueryTmplVersion < 2)
                throw new Error("Your version of jQuery.tmpl is too old. Please upgrade to jQuery.tmpl 1.0.0pre or later.");
        }

        function executeTemplate(compiledTemplate, data, jQueryTemplateOptions) {
            return jQuery['tmpl'](compiledTemplate, data, jQueryTemplateOptions);
        }

        this['renderTemplateSource'] = function(templateSource, bindingContext, options) {
            options = options || {};
            ensureHasReferencedJQueryTemplates();

            // Ensure we have stored a precompiled version of this template (don't want to reparse on every render)
            var precompiled = templateSource['data']('precompiled');
            if (!precompiled) {
                var templateText = templateSource['text']() || "";
                // Wrap in "with($whatever.koBindingContext) { ... }"
                templateText = "{{ko_with $item.koBindingContext}}" + templateText + "{{/ko_with}}";

                precompiled = jQuery['template'](null, templateText);
                templateSource['data']('precompiled', precompiled);
            }

            var data = [bindingContext['$data']]; // Prewrap the data in an array to stop jquery.tmpl from trying to unwrap any arrays
            var jQueryTemplateOptions = jQuery['extend']({ 'koBindingContext': bindingContext }, options['templateOptions']);

            var resultNodes = executeTemplate(precompiled, data, jQueryTemplateOptions);
            resultNodes['appendTo'](document.createElement("div")); // Using "appendTo" forces jQuery/jQuery.tmpl to perform necessary cleanup work

            jQuery['fragments'] = {}; // Clear jQuery's fragment cache to avoid a memory leak after a large number of template renders
            return resultNodes;
        };

        this['createJavaScriptEvaluatorBlock'] = function(script) {
            return "{{ko_code ((function() { return " + script + " })()) }}";
        };

        this['addTemplate'] = function(templateName, templateMarkup) {
            document.write("<script type='text/html' id='" + templateName + "'>" + templateMarkup + "</script>");
        };

        if (jQueryTmplVersion > 0) {
            jQuery['tmpl']['tag']['ko_code'] = {
                open: "__.push($1 || '');"
            };
            jQuery['tmpl']['tag']['ko_with'] = {
                open: "with($1) {",
                close: "} "
            };
        }
    };

    ko.jqueryTmplTemplateEngine.prototype = new ko.templateEngine();

    // Use this one by default *only if jquery.tmpl is referenced*
    var jqueryTmplTemplateEngineInstance = new ko.jqueryTmplTemplateEngine();
    if (jqueryTmplTemplateEngineInstance.jQueryTmplVersion > 0)
        ko.setTemplateEngine(jqueryTmplTemplateEngineInstance);

    ko.exportSymbol('jqueryTmplTemplateEngine', ko.jqueryTmplTemplateEngine);
})();
});
})(window,document,navigator,window["jQuery"]);
})();

// TeaVM generated classes
/*
 *  Copyright 2013 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
$rt_lastObjectId = 0;
$rt_nextId = function() {
    return $rt_lastObjectId++;
}
$rt_compare = function(a, b) {
    return a > b ? 1 : a < b ? -1 : 0;
}
$rt_isInstance = function(obj, cls) {
    return obj != null && obj.constructor.$meta && $rt_isAssignable(obj.constructor, cls);
}
$rt_isAssignable = function(from, to) {
    if (from === to) {
        return true;
    }
    var supertypes = from.$meta.supertypes;
    for (var i = 0; i < supertypes.length; i = (i + 1) | 0) {
        if ($rt_isAssignable(supertypes[i], to)) {
            return true;
        }
    }
    return false;
}
$rt_createArray = function(cls, sz) {
    var data = new Array(sz);
    var arr = new ($rt_arraycls(cls))(data);
    for (var i = 0; i < sz; i = (i + 1) | 0) {
        data[i] = null;
    }
    return arr;
}
$rt_wrapArray = function(cls, data) {
    var arr = new ($rt_arraycls(cls))(data);
    return arr;
}
$rt_createUnfilledArray = function(cls, sz) {
    return new ($rt_arraycls(cls))(new Array(sz));
}
$rt_createLongArray = function(sz) {
    var data = new Array(sz);
    var arr = new ($rt_arraycls($rt_longcls()))(data);
    for (var i = 0; i < sz; i = (i + 1) | 0) {
        data[i] = Long.ZERO;
    }
    return arr;
}
if (ArrayBuffer) {
    $rt_createNumericArray = function(cls, nativeArray) {
        return new ($rt_arraycls(cls))(nativeArray);
    }
    $rt_createByteArray = function(sz) {
        return $rt_createNumericArray($rt_bytecls(), new Int8Array(new ArrayBuffer(sz)), 0);
    };
    $rt_createShortArray = function(sz) {
        return $rt_createNumericArray($rt_shortcls(), new Int16Array(new ArrayBuffer(sz << 1)), 0);
    };
    $rt_createIntArray = function(sz) {
        return $rt_createNumericArray($rt_intcls(), new Int32Array(new ArrayBuffer(sz << 2)), 0);
    };
    $rt_createBooleanArray = function(sz) {
        return $rt_createNumericArray($rt_booleancls(), new Int8Array(new ArrayBuffer(sz)), 0);
    };
    $rt_createFloatArray = function(sz) {
        return $rt_createNumericArray($rt_floatcls(), new Float32Array(new ArrayBuffer(sz << 2)), 0);
    };
    $rt_createDoubleArray = function(sz) {
        return $rt_createNumericArray($rt_doublecls(), new Float64Array(new ArrayBuffer(sz << 3)), 0);
    };
    $rt_createCharArray = function(sz) {
        return $rt_createNumericArray($rt_charcls(), new Uint16Array(new ArrayBuffer(sz << 1)), 0);
    };
} else {
    $rt_createNumericArray = function(cls, sz) {
        var data = new Array(sz);
        var arr = new ($rt_arraycls(cls))(data);
        for (var i = 0; i < sz; i = (i + 1) | 0) {
            data[i] = 0;
        }
        return arr;
    }
    $rt_createByteArray = function(sz) { return $rt_createNumericArray($rt_bytecls(), sz); }
    $rt_createShortArray = function(sz) { return $rt_createNumericArray($rt_shortcls(), sz); }
    $rt_createIntArray = function(sz) { return $rt_createNumericArray($rt_intcls(), sz); }
    $rt_createBooleanArray = function(sz) { return $rt_createNumericArray($rt_booleancls(), sz); }
    $rt_createFloatArray = function(sz) { return $rt_createNumericArray($rt_floatcls(), sz); }
    $rt_createDoubleArray = function(sz) { return $rt_createNumericArray($rt_doublecls(), sz); }
    $rt_createCharArray = function(sz) { return $rt_createNumericArray($rt_charcls(), sz); }
}
$rt_arraycls = function(cls) {
    if (cls.$array == undefined) {
        var arraycls = function(data) {
            this.data = data;
            this.$id = $rt_nextId();
        };
        arraycls.prototype = new ($rt_objcls())();
        arraycls.prototype.constructor = arraycls;
        arraycls.$meta = { item : cls, supertypes : [$rt_objcls()], primitive : false, superclass : $rt_objcls() };
        cls.$array = arraycls;
    }
    return cls.$array;
}
$rt_createcls = function() {
    return {
        $meta : {
            supertypes : []
        }
    };
}
$rt_booleanclsCache = null;
$rt_booleancls = function() {
    if ($rt_booleanclsCache == null) {
        $rt_booleanclsCache = $rt_createcls();
        $rt_booleanclsCache.primitive = true;
        $rt_booleanclsCache.name = "boolean";
    }
    return $rt_booleanclsCache;
}
$rt_charclsCache = null;
$rt_charcls = function() {
    if ($rt_charclsCache == null) {
        $rt_charclsCache = $rt_createcls();
        $rt_charclsCache.primitive = true;
        $rt_charclsCache.name = "char";
    }
    return $rt_charclsCache;
}
$rt_byteclsCache = null;
$rt_bytecls = function() {
    if ($rt_byteclsCache == null) {
        $rt_byteclsCache = $rt_createcls();
        $rt_byteclsCache.primitive = true;
        $rt_byteclsCache.name = "byte";
    }
    return $rt_byteclsCache;
}
$rt_shortclsCache = null;
$rt_shortcls = function() {
    if ($rt_shortclsCache == null) {
        $rt_shortclsCache = $rt_createcls();
        $rt_shortclsCache.primitive = true;
        $rt_shortclsCache.name = "short";
    }
    return $rt_shortclsCache;
}
$rt_intclsCache = null;
$rt_intcls = function() {
    if ($rt_intclsCache === null) {
        $rt_intclsCache = $rt_createcls();
        $rt_intclsCache.primitive = true;
        $rt_intclsCache.name = "int";
    }
    return $rt_intclsCache;
}
$rt_longclsCache = null;
$rt_longcls = function() {
    if ($rt_longclsCache === null) {
        $rt_longclsCache = $rt_createcls();
        $rt_longclsCache.primitive = true;
        $rt_longclsCache.name = "long";
    }
    return $rt_longclsCache;
}
$rt_floatclsCache = null;
$rt_floatcls = function() {
    if ($rt_floatclsCache === null) {
        $rt_floatclsCache = $rt_createcls();
        $rt_floatclsCache.primitive = true;
        $rt_floatclsCache.name = "float";
    }
    return $rt_floatclsCache;
}
$rt_doubleclsCache = null;
$rt_doublecls = function() {
    if ($rt_doubleclsCache === null) {
        $rt_doubleclsCache = $rt_createcls();
        $rt_doubleclsCache.primitive = true;
        $rt_doubleclsCache.name = "double";
    }
    return $rt_doubleclsCache;
}
$rt_voidclsCache = null;
$rt_voidcls = function() {
    if ($rt_voidclsCache === null) {
        $rt_voidclsCache = $rt_createcls();
        $rt_voidclsCache.primitive = true;
        $rt_voidclsCache.name = "void";
    }
    return $rt_voidclsCache;
}
$rt_equals = function(a, b) {
    if (a === b) {
        return true;
    }
    if (a === null || b === null) {
        return false;
    }
    if (typeof(a) == 'object') {
        return a.equals(b);
    } else {
        return false;
    }
}
$rt_clinit = function(cls) {
    if (cls.$clinit) {
        var f = cls.$clinit;
        delete cls.$clinit;
        f();
    }
    return cls;
}
$rt_init = function(cls, constructor, args) {
    var obj = new cls();
    cls.prototype[constructor].apply(obj, args);
    return obj;
}
$rt_throw = function(ex) {
    var err = ex.$jsException;
    if (!err) {
        var err = new Error("Java exception thrown");
        err.$javaException = ex;
        ex.$jsException = err;
    }
    throw err;
}
$rt_byteToInt = function(value) {
    return value > 0xFF ? value | 0xFFFFFF00 : value;
}
$rt_shortToInt = function(value) {
    return value > 0xFFFF ? value | 0xFFFF0000 : value;
}
$rt_createMultiArray = function(cls, dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createArray(cls, firstDim);
    }
    return $rt_createMultiArrayImpl(cls, arrays, dimensions);
}
$rt_createByteMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createByteArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_bytecls(), arrays, dimensions);
}
$rt_createBooleanMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createBooleanArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_booleancls(), arrays, dimensions);
}
$rt_createShortMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createShortArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_shortcls(), arrays, dimensions);
}
$rt_createIntMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createIntArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_intcls(), arrays, dimensions);
}
$rt_createLongMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createLongArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_longcls(), arrays, dimensions);
}
$rt_createFloatMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createFloatArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_floatcls(), arrays, dimensions);
}
$rt_createDoubleMultiArray = function(dimensions) {
    var arrays = new Array($rt_primitiveArrayCount(dimensions));
    var firstDim = dimensions[0] | 0;
    for (var i = 0 | 0; i < arrays.length; i = (i + 1) | 0) {
        arrays[i] = $rt_createDoubleArray(firstDim);
    }
    return $rt_createMultiArrayImpl($rt_doublecls(), arrays, dimensions);
}
$rt_primitiveArrayCount = function(dimensions) {
    var val = dimensions[1] | 0;
    for (var i = 2 | 0; i < dimensions.length; i = (i + 1) | 0) {
        val = (val * (dimensions[i] | 0)) | 0;
    }
    return val;
}
$rt_createMultiArrayImpl = function(cls, arrays, dimensions) {
    var limit = arrays.length;
    for (var i = 1 | 0; i < dimensions.length; i = (i + 1) | 0) {
        cls = $rt_arraycls(cls);
        var dim = dimensions[i];
        var index = 0;
        var packedIndex = 0;
        while (index < limit) {
            var arr = $rt_createUnfilledArray(cls, dim);
            for (var j = 0; j < dim; j = (j + 1) | 0) {
                arr.data[j] = arrays[index];
                index = (index + 1) | 0;
            }
            arrays[packedIndex] = arr;
            packedIndex = (packedIndex + 1) | 0;
        }
        limit = packedIndex;
    }
    return arrays[0];
}
$rt_assertNotNaN = function(value) {
    if (typeof value == 'number' && isNaN(value)) {
        throw "NaN";
    }
    return value;
}
$rt_methodStubs = function(clinit, names) {
    for (var i = 0; i < names.length; i = (i + 1) | 0) {
        window[names[i]] = (function(name) {
            return function() {
                clinit();
                return window[name].apply(window, arguments);
            }
        })(names[i]);
    }
}
$rt_stdoutBuffer = "";
$rt_putStdout = function(ch) {
    if (ch === 0xA) {
        if (console) {
            console.info($rt_stdoutBuffer);
        }
        $rt_stdoutBuffer = "";
    } else {
        $rt_stdoutBuffer += String.fromCharCode(ch);
    }
}
$rt_stderrBuffer = "";
$rt_putStderr = function(ch) {
    if (ch === 0xA) {
        if (console) {
            console.info($rt_stderrBuffer);
        }
        $rt_stderrBuffer = "";
    } else {
        $rt_stderrBuffer += String.fromCharCode(ch);
    }
}
function $rt_declClass(cls, data) {
    cls.name = data.name;
    cls.$meta = {};
    cls.$meta.superclass = data.superclass;
    cls.$meta.supertypes = data.interfaces ? data.interfaces.slice() : [];
    if (data.superclass) {
        cls.$meta.supertypes.push(data.superclass);
        cls.prototype = new data.superclass();
    } else {
        cls.prototype = new Object();
    }
    cls.$meta.name = data.name;
    cls.$meta.enum = data.enum;
    cls.prototype.constructor = cls;
    cls.$clinit = data.clinit;
}
function $rt_virtualMethods(cls) {
    for (var i = 1; i < arguments.length; i += 2) {
        var name = arguments[i];
        var func = arguments[i + 1];
        if (typeof name == 'string') {
            cls.prototype[name] = func;
        } else {
            for (var j = 0; j < name.length; ++j) {
                cls.prototype[name[j]] = func;
            }
        }
    }
}

Long = function(lo, hi) {
    this.lo = lo | 0;
    this.hi = hi | 0;
}
Long_ZERO = new Long(0, 0);
Long_fromInt = function(val) {
    return val >= 0 ? new Long(val, 0) : new Long(val, -1);
}
Long_fromNumber = function(val) {
    return new Long(val | 0, (val / 0x100000000) | 0);
}
Long_toNumber = function(val) {
    return val.lo + 0x100000000 * val.hi;
}
Long_add = function(a, b) {
    var a_lolo = a.lo & 0xFFFF;
    var a_lohi = a.lo >>> 16;
    var a_hilo = a.hi & 0xFFFF;
    var a_hihi = a.hi >>> 16;
    var b_lolo = b.lo & 0xFFFF;
    var b_lohi = b.lo >>> 16;
    var b_hilo = b.hi & 0xFFFF;
    var b_hihi = b.hi >>> 16;

    var lolo = (a_lolo + b_lolo) | 0;
    var lohi = (a_lohi + b_lohi + (lolo >> 16)) | 0;
    var hilo = (a_hilo + b_hilo + (lohi >> 16)) | 0;
    var hihi = (a_hihi + b_hihi + (hilo >> 16)) | 0;
    return new Long((lolo & 0xFFFF) | ((lohi & 0xFFFF) << 16),
            (hilo & 0xFFFF) | ((hihi & 0xFFFF) << 16));
}
Long_inc = function(a) {
    var lo = (a.lo + 1) | 0;
    var hi = a.hi;
    if (lo === 0) {
        hi = (hi + 1) | 0;
    }
    return new Long(lo, hi);
}
Long_dec = function(a) {
    var lo = (a.lo - 1) | 0;
    var hi = a.hi;
    if (lo === -1) {
        hi = (hi - 1) | 0;
    }
    return new Long(lo, hi);
}
Long_neg = function(a) {
    return Long_inc(new Long(a.lo ^ 0xFFFFFFFF, a.hi ^ 0xFFFFFFFF));
}
Long_sub = function(a, b) {
    var a_lolo = a.lo & 0xFFFF;
    var a_lohi = a.lo >>> 16;
    var a_hilo = a.hi & 0xFFFF;
    var a_hihi = a.hi >>> 16;
    var b_lolo = b.lo & 0xFFFF;
    var b_lohi = b.lo >>> 16;
    var b_hilo = b.hi & 0xFFFF;
    var b_hihi = b.hi >>> 16;

    var lolo = (a_lolo - b_lolo) | 0;
    var lohi = (a_lohi - b_lohi + (lolo >> 16)) | 0;
    var hilo = (a_hilo - b_hilo + (lohi >> 16)) | 0;
    var hihi = (a_hihi - b_hihi + (hilo >> 16)) | 0;
    return new Long((lolo & 0xFFFF) | ((lohi & 0xFFFF) << 16),
            (hilo & 0xFFFF) | ((hihi & 0xFFFF) << 16));
}
Long_compare = function(a, b) {
    var r = a.hi - b.hi;
    if (r !== 0) {
        return r;
    }
    var r = (a.lo >>> 1) - (b.lo >>> 1);
    if (r !== 0) {
        return r;
    }
    return (a.lo & 1) - (b.lo & 1);
}
Long_isPositive = function(a) {
    return (a.hi & 0x80000000) === 0;
}
Long_isNegative = function(a) {
    return (a.hi & 0x80000000) !== 0;
}
Long_mul = function(a, b) {
    var a_lolo = a.lo & 0xFFFF;
    var a_lohi = a.lo >>> 16;
    var a_hilo = a.hi & 0xFFFF;
    var a_hihi = a.hi >>> 16;
    var b_lolo = b.lo & 0xFFFF;
    var b_lohi = b.lo >>> 16;
    var b_hilo = b.hi & 0xFFFF;
    var b_hihi = b.hi >>> 16;

    var lolo = (a_lolo * b_lolo) | 0;
    var lohi = (a_lohi * b_lolo + a_lolo * b_lohi + (lolo >> 16)) | 0;
    var hilo = (a_hilo * b_lolo + a_lohi * b_lohi + a_lolo * b_hilo + (lohi >> 16)) | 0;
    var hihi = (a_hihi * b_lolo + a_hilo * b_lohi + a_lohi * b_hilo + a_lolo * b_hihi + (hilo >> 16)) | 0;
    return new Long((lolo & 0xFFFF) | ((lohi & 0xFFFF) << 16), (hilo & 0xFFFF) | ((hihi & 0xFFFF) << 16));
}
Long_div = function(a, b) {
    return Long_divRem(a, b)[0];
}
Long_rem = function(a, b) {
    return Long_divRem(a, b)[1];
}
Long_divRem = function(a, b) {
    var positive = Long_isNegative(a) === Long_isNegative(b);
    if (Long_isNegative(a)) {
        a = Long_neg(a);
    }
    if (Long_isNegative(b)) {
        b = Long_neg(b);
    }
    a = new LongInt(a.lo, a.hi, 0);
    b = new LongInt(b.lo, b.hi, 0);
    var q = LongInt_div(a, b);
    a = new Long(a.lo, a.hi);
    q = new Long(q.lo, q.hi);
    return positive ? [q, a] : [Long_neg(q), Long_neg(a)];
}
Long_shiftLeft16 = function(a) {
    return new Long(a.lo << 16, (a.lo >>> 16) | (a.hi << 16));
}
Long_shiftRight16 = function(a) {
    return new Long((a.lo >>> 16) | (a.hi << 16), a.hi >>> 16);
}
Long_and = function(a, b) {
    return new Long(a.lo & b.lo, a.hi & b.hi);
}
Long_or = function(a, b) {
    return new Long(a.lo | b.lo, a.hi | b.hi);
}
Long_xor = function(a, b) {
    return new Long(a.lo ^ b.lo, a.hi ^ b.hi);
}
Long_shl = function(a, b) {
    if (b < 32) {
        return new Long(a.lo << b, (a.lo >>> (32 - b)) | (a.hi << b));
    } else {
        return new Long(0, a.lo << (b - 32));
    }
}
Long_shr = function(a, b) {
    if (b < 32) {
        return new Long((a.lo >>> b) | (a.hi << (32 - b)), a.hi >> b);
    } else {
        return new Long((a.hi >> (b - 32)), -1);
    }
}
Long_shru = function(a, b) {
    if (b < 32) {
        return new Long((a.lo >>> b) | (a.hi << (32 - b)), a.hi >>> b);
    } else {
        return new Long((a.hi >>> (b - 32)), 0);
    }
}

// Represents a mutable 80-bit unsigned integer
LongInt = function(lo, hi, sup) {
    this.lo = lo;
    this.hi = hi;
    this.sup = sup;
}
LongInt_mul = function(a, b) {
    var a_lolo = ((a.lo & 0xFFFF) * b) | 0;
    var a_lohi = ((a.lo >>> 16) * b) | 0;
    var a_hilo = ((a.hi & 0xFFFF) * b) | 0;
    var a_hihi = ((a.hi >>> 16) * b) | 0;
    var sup = (a.sup * b) | 0;

    a_lohi = (a_lohi + (a_lolo >> 16)) | 0;
    a_hilo = (a_hilo + (a_lohi >> 16)) | 0;
    a_hihi = (a_hihi + (a_hilo >> 16)) | 0;
    sup = (sup + (a_hihi >> 16)) | 0;
    a.lo = (a_lolo & 0xFFFF) | (a_lohi << 16);
    a.hi = (a_hilo & 0xFFFF) | (a_hihi << 16);
    a.sup = sup & 0xFFFF;
}
LongInt_sub = function(a, b) {
    var a_lolo = a.lo & 0xFFFF;
    var a_lohi = a.lo >>> 16;
    var a_hilo = a.hi & 0xFFFF;
    var a_hihi = a.hi >>> 16;
    var b_lolo = b.lo & 0xFFFF;
    var b_lohi = b.lo >>> 16;
    var b_hilo = b.hi & 0xFFFF;
    var b_hihi = b.hi >>> 16;

    a_lolo = (a_lolo - b_lolo) | 0;
    a_lohi = (a_lohi - b_lohi + (a_lolo >> 16)) | 0;
    a_hilo = (a_hilo - b_hilo + (a_lohi >> 16)) | 0;
    a_hihi = (a_hihi - b_hihi + (a_hilo >> 16)) | 0;
    sup = (a.sup - b.sup + (a_hihi >> 16)) | 0;
    a.lo = (a_lolo & 0xFFFF) | ((a_lohi & 0xFFFF) << 16);
    a.hi = (a_hilo & 0xFFFF) | ((a_hihi & 0xFFFF) << 16);
    a.sup = sup;
}
LongInt_add = function(a, b) {
    var a_lolo = a.lo & 0xFFFF;
    var a_lohi = a.lo >>> 16;
    var a_hilo = a.hi & 0xFFFF;
    var a_hihi = a.hi >>> 16;
    var b_lolo = b.lo & 0xFFFF;
    var b_lohi = b.lo >>> 16;
    var b_hilo = b.hi & 0xFFFF;
    var b_hihi = b.hi >>> 16;

    a_lolo = (a_lolo + b_lolo) | 0;
    a_lohi = (a_lohi + b_lohi + (a_lolo >> 16)) | 0;
    a_hilo = (a_hilo + b_hilo + (a_lohi >> 16)) | 0;
    a_hihi = (a_hihi + b_hihi + (a_hilo >> 16)) | 0;
    sup = (a.sup + b.sup + (a_hihi >> 16)) | 0;
    a.lo = (a_lolo & 0xFFFF) | (a_lohi << 16);
    a.hi = (a_hilo & 0xFFFF) | (a_hihi << 16);
    a.sup = sup;
}
LongInt_ucompare = function(a, b) {
    var r = (a.sup - b.sup);
    if (r != 0) {
        return r;
    }
    var r = (a.hi >>> 1) - (b.hi >>> 1);
    if (r != 0) {
        return r;
    }
    var r = (a.hi & 1) - (b.hi & 1);
    if (r != 0) {
        return r;
    }
    var r = (a.lo >>> 1) - (b.lo >>> 1);
    if (r != 0) {
        return r;
    }
    return (a.lo & 1) - (b.lo & 1);
}
LongInt_numOfLeadingZeroBits = function(a) {
    var n = 0;
    var d = 16;
    while (d > 0) {
        if ((a >>> d) !== 0) {
            a >>>= d;
            n = (n + d) | 0;
        }
        d = (d / 2) | 0;
    }
    return 31 - n;
}
LongInt_shl = function(a, b) {
    if (b < 32) {
        a.sup = ((a.hi >>> (32 - b)) | (a.sup << b)) & 0xFFFF;
        a.hi = (a.lo >>> (32 - b)) | (a.hi << b);
        a.lo <<= b;
    } else if (b < 64) {
        a.sup = ((a.lo >>> (64 - b)) | (a.hi << (b - 32))) & 0xFFFF;
        a.hi = a.lo << b;
        a.lo = 0;
    } else {
        a.sup = (a.lo << (b - 64)) & 0xFFFF;
        a.hi = 0;
        a.lo = 0;
    }
}
LongInt_shr = function(a, b) {
    if (b < 32) {
        a.lo = (a.lo >>> b) | (a.hi << (32 - b));
        a.hi = (a.hi >>> b) | (a.sup << (32 - b));
        a.sup >>>= b;
    } else if (b < 64) {
        a.lo = (a.hi >>> (b - 32)) | (a.sup << (64 - b));
        a.hi = a.sup >>> (b - 32);
        a.sup = 0;
    } else {
        a.lo = a.sup >>> (b - 64);
        a.hi = 0;
        a.sup = 0;
    }
}
LongInt_copy = function(a) {
    return new LongInt(a.lo, a.hi, a.sup);
}
LongInt_div = function(a, b) {
    // Normalize divisor
    var bits = b.hi !== 0 ? LongInt_numOfLeadingZeroBits(b.hi) : LongInt_numOfLeadingZeroBits(b.lo) + 32;
    var sz = 1 + ((bits / 16) | 0);
    var dividentBits = bits % 16;
    LongInt_shl(b, bits);
    LongInt_shl(a, dividentBits);
    q = new LongInt(0, 0, 0);
    while (sz-- > 0) {
        LongInt_shl(q, 16);
        // Calculate approximate q
        var digitA = (a.hi >>> 16) + (0x10000 * a.sup);
        var digitB = b.hi >>> 16;
        var digit = (digitA / digitB) | 0;
        var t = LongInt_copy(b);
        LongInt_mul(t, digit);
        // Adjust q either down or up
        if (LongInt_ucompare(t, a) >= 0) {
            while (LongInt_ucompare(t, a) > 0) {
                LongInt_sub(t, b);
                q = (q - 1) | 0;
            }
        } else {
            while (true) {
                var nextT = LongInt_copy(t);
                LongInt_add(nextT, b);
                if (LongInt_ucompare(nextT, a) > 0) {
                    break;
                }
                t = nextT;
                q = (q + 1) | 0;
            }
        }
        LongInt_sub(a, t);
        q.lo |= digit;
        LongInt_shl(a, 16);
    }
    LongInt_shr(a, bits + 16);
    return q;
}
$rt_cls = function(clsProto) {
    var cls = clsProto.classObject;
    if (cls === undefined) {
        cls = jl_Class0_createNew1();
        cls.$data = clsProto;
        cls.name2 = clsProto.$meta.name !== undefined ? $rt_str(clsProto.$meta.name) : null;
        clsProto.classObject = cls;
    }
    return cls;
}
$rt_str = function(str) {
    var characters = $rt_createCharArray(str.length);
    var charsBuffer = characters.data;
    for (var i = 0; i < str.length; i = (i + 1) | 0) {
        charsBuffer[i] = str.charCodeAt(i) & 0xFFFF;
    }
    return jl_String3.$init4(characters);
}
$rt_ustr = function(str) {
    var result = "";
    var sz = jl_String3_length5(str);
    var array = $rt_createCharArray(sz);
    jl_String3_getChars6(str, 0, sz, array, 0);
    for (var i = 0; i < sz; i = (i + 1) | 0) {
        result += String.fromCharCode(array.data[i]);
    }
    return result;
}
$rt_objcls = function() { return jl_Object7; }
$rt_nullCheck = function(val) {
    if (val === null) {
        $rt_throw(jl_NullPointerException8.$init9());
    }
    return val;
}
function jl_Object7() {
}
$rt_declClass(jl_Object7, {
    name : "java.lang.Object",
    clinit : function() { jl_Object7_$clinit(); } });
function jl_Object7_$clinit() {
    jl_Object7_$clinit = function(){};
    jl_Object7_$init10 = function($this) {
        $this.$id = $rt_nextId();
    }
}
$rt_methodStubs(jl_Object7_$clinit, ['jl_Object7_$init10']);
function jl_Object7_identity11($this) {
    return $this.$id;
}
function jl_Object7_clone12($this) {
    var copy = new $this.constructor();
    for (var field in $this) {
        if (!$this.hasOwnProperty(field)) {
            continue;
        }
        copy[field] = $this[field];
    }
    return copy;
}
function jl_Object7_equals13($this, a) {
    if (($this !== a)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function jl_Object7_hashCode14($this) {
    return $this.$id;
}
function jl_Object7_toString15($this) {
    var a, b;
    a = jl_StringBuilder16.$init17();
    b = $rt_nullCheck($this);
    return jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append18($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(a), jl_Class0_getName20($rt_nullCheck($rt_cls(b.constructor))))), $rt_str("@"))), jl_Object7_identity11(b))));
}
jl_Object7.$init10 = function() {
    var result = new jl_Object7();
    result.$init10();
    return result;
}
$rt_virtualMethods(jl_Object7,
    "identity11", function() { return jl_Object7_identity11(this); },
    "clone12", function() { return jl_Object7_clone12(this); },
    "equals13", function(a) { return jl_Object7_equals13(this, a); },
    "hashCode14", function() { return jl_Object7_hashCode14(this); },
    "$init10", function() { jl_Object7_$init10(this); },
    "toString15", function() { return jl_Object7_toString15(this); });
function oahjs_PropertyBinding21() {
}
$rt_declClass(oahjs_PropertyBinding21, {
    name : "org.apidesign.html.json.spi.PropertyBinding",
    superclass : jl_Object7,
    clinit : function() { oahjs_PropertyBinding21_$clinit(); } });
function oahjs_PropertyBinding21_$clinit() {
    oahjs_PropertyBinding21_$clinit = function(){};
    oahjs_PropertyBinding21_$clinit22 = function() {
        oahjs_PropertyBinding$123_$init24(new oahjs_PropertyBinding$123());
        return;
    }
    oahjs_PropertyBinding21_$init25 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oahjs_PropertyBinding21_$clinit22();
}
$rt_methodStubs(oahjs_PropertyBinding21_$clinit, ['oahjs_PropertyBinding21_$clinit22', 'oahjs_PropertyBinding21_$init25']);
oahjs_PropertyBinding21.$init25 = function() {
    var result = new oahjs_PropertyBinding21();
    result.$init25();
    return result;
}
$rt_virtualMethods(oahjs_PropertyBinding21,
    "$init25", function() { oahjs_PropertyBinding21_$init25(this); });
function oahjs_PropertyBinding$Impl26() {
    this.index27 = 0;
    this.model28 = null;
    this.readOnly29 = false;
    this.bindings30 = null;
    this.name31 = null;
    this.access32 = null;
}
$rt_declClass(oahjs_PropertyBinding$Impl26, {
    name : "org.apidesign.html.json.spi.PropertyBinding$Impl",
    superclass : oahjs_PropertyBinding21,
    clinit : function() { oahjs_PropertyBinding$Impl26_$clinit(); } });
function oahjs_PropertyBinding$Impl26_$clinit() {
    oahjs_PropertyBinding$Impl26_$clinit = function(){};
    oahjs_PropertyBinding$Impl26_$init33 = function($this, a, b, c, d, e, f) {
        oahjs_PropertyBinding21_$init25($this);
        $this.bindings30 = a;
        $this.name31 = b;
        $this.index27 = c;
        $this.model28 = d;
        $this.access32 = e;
        $this.readOnly29 = f;
        return;
    }
}
$rt_methodStubs(oahjs_PropertyBinding$Impl26_$clinit, ['oahjs_PropertyBinding$Impl26_$init33']);
function oahjs_PropertyBinding$Impl26_isReadOnly34($this) {
    return $this.readOnly29;
}
function oahjs_PropertyBinding$Impl26_setValue35($this, a) {
    var b, c, d;
    b = $this.access32;
    c = $this.model28;
    d = $this.index27;
    $rt_nullCheck(b).setValue37(c, d, a);
    return;
}
function oahjs_PropertyBinding$Impl26_getValue38($this) {
    var a, b;
    a = $rt_nullCheck($this.access32).getValue39($this.model28, $this.index27);
    b = onhji_JSON40_find41(a, $this.bindings30);
    if ((b === null)) {
        b = a;
    }
    return b;
}
function oahjs_PropertyBinding$Impl26_getPropertyName42($this) {
    return $this.name31;
}
oahjs_PropertyBinding$Impl26.$init33 = function(a, b, c, d, e, f) {
    var result = new oahjs_PropertyBinding$Impl26();
    result.$init33(a, b, c, d, e, f);
    return result;
}
$rt_virtualMethods(oahjs_PropertyBinding$Impl26,
    "isReadOnly34", function() { return oahjs_PropertyBinding$Impl26_isReadOnly34(this); },
    "setValue35", function(a) { oahjs_PropertyBinding$Impl26_setValue35(this, a); },
    "getValue38", function() { return oahjs_PropertyBinding$Impl26_getValue38(this); },
    "getPropertyName42", function() { return oahjs_PropertyBinding$Impl26_getPropertyName42(this); },
    "$init33", function(a, b, c, d, e, f) { oahjs_PropertyBinding$Impl26_$init33(this, a, b, c, d, e, f); });
function oadm_MinesModel43() {
}
$rt_declClass(oadm_MinesModel43, {
    name : "org.apidesign.demo.minesweeper.MinesModel",
    superclass : jl_Object7,
    clinit : function() { oadm_MinesModel43_$clinit(); } });
function oadm_MinesModel43_$clinit() {
    oadm_MinesModel43_$clinit = function(){};
    oadm_MinesModel43_giveUp44 = function(a) {
        oadm_MinesModel$SquareType45_$clinit();
        oadm_MinesModel43_showAllBombs46(a, oadm_MinesModel$SquareType45.EXPLOSION47);
        return;
    }
    oadm_MinesModel43_smallGame48 = function(a) {
        var b, c, d;
        b = 5;
        c = 5;
        d = 5;
        oadm_Mines49_init50($rt_nullCheck(a), b, c, d);
        return;
    }
    oadm_MinesModel43_normalGame51 = function(a) {
        var b, c, d;
        b = 10;
        c = 10;
        d = 10;
        oadm_Mines49_init50($rt_nullCheck(a), b, c, d);
        return;
    }
    oadm_MinesModel43_expandKnown52 = function(a, b) {
        var c, d, e, f, g;
        c = oadm_Mines49_getRows53($rt_nullCheck(a));
        d = 0;
        while (true) {
            e = $rt_nullCheck(c);
            if ((d >= ju_ArrayList54_size55(e))) {
                break;
            }
            f = oadm_Row56_getColumns57($rt_nullCheck(ju_ArrayList54_get58(e, d)));
            g = 0;
            block3: {
                while (true) {
                    e = $rt_nullCheck(f);
                    if ((g >= ju_ArrayList54_size55(e))) {
                        break block3;
                    }
                    if ((ju_ArrayList54_get58(e, g) === b)) {
                        break;
                    }
                    g = ((g + 1) | 0);
                }
                oadm_MinesModel43_expandKnown59(a, g, d);
                return;
            }
            d = ((d + 1) | 0);
        }
        return;
    }
    oadm_MinesModel43_init60 = function(a, b, c, d) {
        var e, f, g, h, i;
        e = ju_ArrayList54.$init61(c);
        f = 0;
        while ((f < c)) {
            g = $rt_createArray(oadm_Square62, b);
            h = 0;
            while ((h < b)) {
                i = new oadm_Square62();
                oadm_MinesModel$SquareType45_$clinit();
                oadm_Square62_$init63(i, oadm_MinesModel$SquareType45.UNKNOWN64, 0);
                g.data[h] = i;
                h = ((h + 1) | 0);
            }
            h = oadm_Row56.$init65(g);
            ju_AbstractList66_add67($rt_nullCheck(e), h);
            f = ((f + 1) | 0);
        }
        f = ju_Random68.$init69();
        while ((d > 0)) {
            h = $rt_nullCheck(f);
            h = $rt_nullCheck(ju_ArrayList54_get58($rt_nullCheck(oadm_Row56_getColumns57($rt_nullCheck(ju_ArrayList54_get58($rt_nullCheck(e), ju_Random68_nextInt70(h, c))))), ju_Random68_nextInt70(h, b)));
            if ((oadm_Square62_isMine71(h) == 0)) {
                oadm_Square62_setMine72(h, 1);
                d = ((d + -1) | 0);
                continue;
            }
        }
        oadm_MinesModel$GameState73_$clinit();
        b = oadm_MinesModel$GameState73.IN_PROGRESS74;
        c = $rt_nullCheck(a);
        oadm_Mines49_setState75(c, b);
        onhji_JSONList76_clear77($rt_nullCheck(oadm_Mines49_getRows53(c)));
        onhji_JSONList76_addAll78($rt_nullCheck(oadm_Mines49_getRows53(c)), e);
        return;
    }
    oadm_MinesModel43_computeMines79 = function(a) {
        var b, c, d, e, f, g, h, i, j, k, m, n;
        b = ju_ArrayList54.$init80();
        c = ju_ArrayList54.$init80();
        d = $rt_nullCheck(a);
        e = oadm_Mines49_getRows53(d);
        f = 0;
        g = $rt_nullCheck(e);
        h = $rt_createArray($rt_arraycls(oadm_MinesModel$SquareType45), ju_ArrayList54_size55(g));
        i = 0;
        while ((i < ju_ArrayList54_size55(g))) {
            j = $rt_nullCheck(oadm_Row56_getColumns57($rt_nullCheck(ju_ArrayList54_get58(g, i))));
            k = $rt_createArray(oadm_MinesModel$SquareType45, ju_ArrayList54_size55(j));
            e = h.data;
            e[i] = k;
            k = 0;
            while ((k < ju_ArrayList54_size55(j))) {
                m = $rt_nullCheck(ju_ArrayList54_get58(j, k));
                if ((oadm_Square62_isMine71(m) != 0)) {
                    ju_AbstractList66_add67($rt_nullCheck(b), jl_Integer81_valueOf82(k));
                    ju_AbstractList66_add67($rt_nullCheck(c), jl_Integer81_valueOf82(i));
                }
                if ((oadm_MinesModel$SquareType45_isVisible83($rt_nullCheck(oadm_Square62_getState84(m))) != 0)) {
                    n = e[i];
                    oadm_MinesModel$SquareType45_$clinit();
                    n.data[k] = oadm_MinesModel$SquareType45.N_085;
                } else if ((oadm_Square62_isMine71(m) == 0)) {
                    f = 1;
                }
                k = ((k + 1) | 0);
            }
            i = ((i + 1) | 0);
        }
        j = 0;
        while (true) {
            k = $rt_nullCheck(b);
            if ((j >= ju_ArrayList54_size55(k))) {
                break;
            }
            oadm_MinesModel43_incrementAround86(h, jl_Integer81_intValue87($rt_nullCheck(ju_ArrayList54_get58(k, j))), jl_Integer81_intValue87($rt_nullCheck(ju_ArrayList54_get58($rt_nullCheck(c), j))));
            j = ((j + 1) | 0);
        }
        j = 0;
        while ((j < ju_ArrayList54_size55(g))) {
            k = oadm_Row56_getColumns57($rt_nullCheck(ju_ArrayList54_get58(g, j)));
            e = 0;
            while (true) {
                c = $rt_nullCheck(k);
                if ((e >= ju_ArrayList54_size55(c))) {
                    break;
                }
                b = ju_ArrayList54_get58(c, e);
                c = h.data[j].data[e];
                if ((c !== null)) {
                    i = $rt_nullCheck(b);
                    if ((c !== oadm_Square62_getState84(i))) {
                        oadm_Square62_setState88(i, c);
                    }
                }
                e = ((e + 1) | 0);
            }
            j = ((j + 1) | 0);
        }
        if ((f == 0)) {
            oadm_MinesModel$GameState73_$clinit();
            oadm_Mines49_setState75(d, oadm_MinesModel$GameState73.WON89);
            oadm_MinesModel$SquareType45_$clinit();
            oadm_MinesModel43_showAllBombs46(a, oadm_MinesModel$SquareType45.DISCOVERED90);
        }
        return;
    }
    oadm_MinesModel43_fieldShowing91 = function(a) {
        if ((a === null)) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    oadm_MinesModel43_main92 = function(a) {
        var b, c, d;
        a = oadm_Mines49.$init93();
        b = 10;
        c = 10;
        d = 10;
        a = $rt_nullCheck(a);
        oadm_Mines49_init50(a, b, c, d);
        oadm_Mines49_applyBindings94(a);
        return;
    }
    oadm_MinesModel43_incrementAt95 = function(a, b, c) {
        if ((c >= 0)) {
            a = a.data;
            if ((c < a.length)) {
                a = a[c];
                if ((b >= 0)) {
                    a = a.data;
                    if ((b < a.length)) {
                        c = a[b];
                        if ((c !== null)) {
                            a[b] = oadm_MinesModel$SquareType45_moreBombsAround96($rt_nullCheck(c));
                        }
                    }
                }
            }
        }
        return;
    }
    oadm_MinesModel43_$init97 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oadm_MinesModel43_click98 = function(a, b) {
        var c, d, e;
        c = $rt_nullCheck(a);
        d = oadm_Mines49_getState99(c);
        oadm_MinesModel$GameState73_$clinit();
        if ((d === oadm_MinesModel$GameState73.IN_PROGRESS74)) {
            block2: {
                oadm_MinesModel$1100_$clinit();
                d = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                e = $rt_nullCheck(b);
                switch (d.data[jl_Enum102_ordinal103($rt_nullCheck(oadm_Square62_getState84(e)))]) {
                    case 2:
                        break;
                    default:
                        break block2;
                }
                if ((oadm_Square62_isMine71(e) == 0)) {
                    oadm_MinesModel43_expandKnown52(a, b);
                } else {
                    oadm_MinesModel$SquareType45_$clinit();
                    oadm_MinesModel43_showAllBombs46(a, oadm_MinesModel$SquareType45.EXPLOSION47);
                    oadm_MinesModel$GameState73_$clinit();
                    oadm_Mines49_setState75(c, oadm_MinesModel$GameState73.LOST104);
                }
            }
            return;
        }
        oadm_Mines49_init50(c, 10, 10, 10);
        return;
    }
    oadm_MinesModel43_showHelp105 = function(a) {
        var b;
        b = null;
        oadm_Mines49_setState75($rt_nullCheck(a), b);
        return;
    }
    oadm_MinesModel43_incrementAround86 = function(a, b, c) {
        oadm_MinesModel43_incrementAt95(a, ((b - 1) | 0), ((c - 1) | 0));
        oadm_MinesModel43_incrementAt95(a, ((b - 1) | 0), c);
        oadm_MinesModel43_incrementAt95(a, ((b - 1) | 0), ((c + 1) | 0));
        oadm_MinesModel43_incrementAt95(a, ((b + 1) | 0), ((c - 1) | 0));
        oadm_MinesModel43_incrementAt95(a, ((b + 1) | 0), c);
        oadm_MinesModel43_incrementAt95(a, ((b + 1) | 0), ((c + 1) | 0));
        oadm_MinesModel43_incrementAt95(a, b, ((c - 1) | 0));
        oadm_MinesModel43_incrementAt95(a, b, ((c + 1) | 0));
        return;
    }
    oadm_MinesModel43_expandKnown59 = function(a, b, c) {
        var d, e, f;
        if ((c >= 0)) {
            d = $rt_nullCheck(a);
            if ((c < ju_ArrayList54_size55($rt_nullCheck(oadm_Mines49_getRows53(d))))) {
                e = oadm_Row56_getColumns57($rt_nullCheck(ju_ArrayList54_get58($rt_nullCheck(oadm_Mines49_getRows53(d)), c)));
                if ((b >= 0)) {
                    e = $rt_nullCheck(e);
                    if ((b < ju_ArrayList54_size55(e))) {
                        e = $rt_nullCheck(ju_ArrayList54_get58(e, b));
                        f = oadm_Square62_getState84(e);
                        oadm_MinesModel$SquareType45_$clinit();
                        if ((f === oadm_MinesModel$SquareType45.UNKNOWN64)) {
                            oadm_MinesModel$SquareType45_$clinit();
                            oadm_Square62_setState88(e, oadm_MinesModel$SquareType45.N_085);
                            oadm_Mines49_computeMines106(d);
                            d = oadm_Square62_getState84(e);
                            oadm_MinesModel$SquareType45_$clinit();
                            if ((d === oadm_MinesModel$SquareType45.N_085)) {
                                oadm_MinesModel43_expandKnown59(a, ((b - 1) | 0), ((c - 1) | 0));
                                oadm_MinesModel43_expandKnown59(a, ((b - 1) | 0), c);
                                oadm_MinesModel43_expandKnown59(a, ((b - 1) | 0), ((c + 1) | 0));
                                oadm_MinesModel43_expandKnown59(a, b, ((c - 1) | 0));
                                oadm_MinesModel43_expandKnown59(a, b, ((c + 1) | 0));
                                oadm_MinesModel43_expandKnown59(a, ((b + 1) | 0), ((c - 1) | 0));
                                oadm_MinesModel43_expandKnown59(a, ((b + 1) | 0), c);
                                oadm_MinesModel43_expandKnown59(a, ((b + 1) | 0), ((c + 1) | 0));
                            }
                        }
                        return;
                    }
                }
                return;
            }
        }
        return;
    }
    oadm_MinesModel43_showAllBombs46 = function(a, b) {
        var c, d, e;
        a = ju_AbstractList66_iterator107($rt_nullCheck(oadm_Mines49_getRows53($rt_nullCheck(a))));
        while (true) {
            c = $rt_nullCheck(a);
            if ((ju_AbstractList$1108_hasNext109(c) == 0)) {
                break;
            }
            d = ju_AbstractList66_iterator107($rt_nullCheck(oadm_Row56_getColumns57($rt_nullCheck(ju_AbstractList$1108_next110(c)))));
            while (true) {
                e = $rt_nullCheck(d);
                if ((ju_AbstractList$1108_hasNext109(e) == 0)) {
                    break;
                }
                e = $rt_nullCheck(ju_AbstractList$1108_next110(e));
                if ((oadm_Square62_isMine71(e) != 0)) {
                    oadm_Square62_setState88(e, b);
                }
            }
        }
        return;
    }
}
$rt_methodStubs(oadm_MinesModel43_$clinit, ['oadm_MinesModel43_giveUp44', 'oadm_MinesModel43_smallGame48', 'oadm_MinesModel43_normalGame51', 'oadm_MinesModel43_expandKnown52', 'oadm_MinesModel43_init60', 'oadm_MinesModel43_computeMines79', 'oadm_MinesModel43_fieldShowing91', 'oadm_MinesModel43_main92', 'oadm_MinesModel43_incrementAt95', 'oadm_MinesModel43_$init97', 'oadm_MinesModel43_click98', 'oadm_MinesModel43_showHelp105', 'oadm_MinesModel43_incrementAround86', 'oadm_MinesModel43_expandKnown59', 'oadm_MinesModel43_showAllBombs46']);
oadm_MinesModel43.$init97 = function() {
    var result = new oadm_MinesModel43();
    result.$init97();
    return result;
}
$rt_virtualMethods(oadm_MinesModel43,
    "$init97", function() { oadm_MinesModel43_$init97(this); });
function jl_Throwable111() {
    this.message112 = null;
    this.cause113 = null;
    this.suppressionEnabled114 = false;
    this.writableStackTrace115 = false;
}
$rt_declClass(jl_Throwable111, {
    name : "java.lang.Throwable",
    superclass : jl_Object7,
    clinit : function() { jl_Throwable111_$clinit(); } });
function jl_Throwable111_$clinit() {
    jl_Throwable111_$clinit = function(){};
    jl_Throwable111_$init116 = function($this, a) {
        $this.suppressionEnabled114 = 1;
        $this.writableStackTrace115 = 1;
        jl_Throwable111_fillInStackTrace117($rt_nullCheck($this));
        $this.cause113 = a;
        return;
    }
    jl_Throwable111_$init118 = function($this) {
        $this.suppressionEnabled114 = 1;
        $this.writableStackTrace115 = 1;
        jl_Throwable111_fillInStackTrace117($rt_nullCheck($this));
        return;
    }
    jl_Throwable111_$init119 = function($this, a) {
        $this.suppressionEnabled114 = 1;
        $this.writableStackTrace115 = 1;
        jl_Throwable111_fillInStackTrace117($rt_nullCheck($this));
        $this.message112 = a;
        return;
    }
}
$rt_methodStubs(jl_Throwable111_$clinit, ['jl_Throwable111_$init116', 'jl_Throwable111_$init118', 'jl_Throwable111_$init119']);
function jl_Throwable111_printStackTrace120($this, a) {
    var b, c;
    b = jl_StringBuilder16.$init17();
    c = $rt_nullCheck($this);
    b = jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(b), jl_Class0_getName20($rt_nullCheck($rt_cls(c.constructor))))), $rt_str(": "))), jl_Throwable111_getMessage121(c))));
    ji_PrintStream122_println123($rt_nullCheck(a), b);
    return;
}
function jl_Throwable111_printStackTrace124($this) {
    var a;
    jl_System125_$clinit();
    a = jl_System125.err126;
    jl_Throwable111_printStackTrace120($rt_nullCheck($this), a);
    return;
}
function jl_Throwable111_fillInStackTrace117($this) {
    return $this;
}
function jl_Throwable111_getMessage121($this) {
    return $this.message112;
}
jl_Throwable111.$init116 = function(a) {
    var result = new jl_Throwable111();
    result.$init116(a);
    return result;
}
jl_Throwable111.$init118 = function() {
    var result = new jl_Throwable111();
    result.$init118();
    return result;
}
jl_Throwable111.$init119 = function(a) {
    var result = new jl_Throwable111();
    result.$init119(a);
    return result;
}
$rt_virtualMethods(jl_Throwable111,
    "$init116", function(a) { jl_Throwable111_$init116(this, a); },
    "printStackTrace120", function(a) { jl_Throwable111_printStackTrace120(this, a); },
    "printStackTrace124", function() { jl_Throwable111_printStackTrace124(this); },
    "fillInStackTrace117", function() { return jl_Throwable111_fillInStackTrace117(this); },
    "$init118", function() { jl_Throwable111_$init118(this); },
    "$init119", function(a) { jl_Throwable111_$init119(this, a); },
    "getMessage121", function() { return jl_Throwable111_getMessage121(this); });
function jl_Exception127() {
}
$rt_declClass(jl_Exception127, {
    name : "java.lang.Exception",
    superclass : jl_Throwable111,
    clinit : function() { jl_Exception127_$clinit(); } });
function jl_Exception127_$clinit() {
    jl_Exception127_$clinit = function(){};
    jl_Exception127_$init128 = function($this) {
        jl_Throwable111_$init118($this);
        return;
    }
    jl_Exception127_$init129 = function($this, a) {
        jl_Throwable111_$init116($this, a);
        return;
    }
    jl_Exception127_$init130 = function($this, a) {
        jl_Throwable111_$init119($this, a);
        return;
    }
}
$rt_methodStubs(jl_Exception127_$clinit, ['jl_Exception127_$init128', 'jl_Exception127_$init129', 'jl_Exception127_$init130']);
jl_Exception127.$init128 = function() {
    var result = new jl_Exception127();
    result.$init128();
    return result;
}
jl_Exception127.$init129 = function(a) {
    var result = new jl_Exception127();
    result.$init129(a);
    return result;
}
jl_Exception127.$init130 = function(a) {
    var result = new jl_Exception127();
    result.$init130(a);
    return result;
}
$rt_virtualMethods(jl_Exception127,
    "$init128", function() { jl_Exception127_$init128(this); },
    "$init129", function(a) { jl_Exception127_$init129(this, a); },
    "$init130", function(a) { jl_Exception127_$init130(this, a); });
function jl_RuntimeException131() {
}
$rt_declClass(jl_RuntimeException131, {
    name : "java.lang.RuntimeException",
    superclass : jl_Exception127,
    clinit : function() { jl_RuntimeException131_$clinit(); } });
function jl_RuntimeException131_$clinit() {
    jl_RuntimeException131_$clinit = function(){};
    jl_RuntimeException131_$init132 = function($this) {
        jl_Exception127_$init128($this);
        return;
    }
    jl_RuntimeException131_$init133 = function($this, a) {
        jl_Exception127_$init130($this, a);
        return;
    }
}
$rt_methodStubs(jl_RuntimeException131_$clinit, ['jl_RuntimeException131_$init132', 'jl_RuntimeException131_$init133']);
jl_RuntimeException131.$init132 = function() {
    var result = new jl_RuntimeException131();
    result.$init132();
    return result;
}
jl_RuntimeException131.$init133 = function(a) {
    var result = new jl_RuntimeException131();
    result.$init133(a);
    return result;
}
$rt_virtualMethods(jl_RuntimeException131,
    "$init132", function() { jl_RuntimeException131_$init132(this); },
    "$init133", function(a) { jl_RuntimeException131_$init133(this, a); });
function jl_IllegalArgumentException134() {
}
$rt_declClass(jl_IllegalArgumentException134, {
    name : "java.lang.IllegalArgumentException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_IllegalArgumentException134_$clinit(); } });
function jl_IllegalArgumentException134_$clinit() {
    jl_IllegalArgumentException134_$clinit = function(){};
    jl_IllegalArgumentException134_$init135 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
    jl_IllegalArgumentException134_$init136 = function($this, a) {
        jl_RuntimeException131_$init133($this, a);
        return;
    }
}
$rt_methodStubs(jl_IllegalArgumentException134_$clinit, ['jl_IllegalArgumentException134_$init135', 'jl_IllegalArgumentException134_$init136']);
jl_IllegalArgumentException134.$init135 = function() {
    var result = new jl_IllegalArgumentException134();
    result.$init135();
    return result;
}
jl_IllegalArgumentException134.$init136 = function(a) {
    var result = new jl_IllegalArgumentException134();
    result.$init136(a);
    return result;
}
$rt_virtualMethods(jl_IllegalArgumentException134,
    "$init135", function() { jl_IllegalArgumentException134_$init135(this); },
    "$init136", function(a) { jl_IllegalArgumentException134_$init136(this, a); });
function jl_NumberFormatException137() {
}
$rt_declClass(jl_NumberFormatException137, {
    name : "java.lang.NumberFormatException",
    superclass : jl_IllegalArgumentException134,
    clinit : function() { jl_NumberFormatException137_$clinit(); } });
function jl_NumberFormatException137_$clinit() {
    jl_NumberFormatException137_$clinit = function(){};
    jl_NumberFormatException137_$init138 = function($this) {
        jl_IllegalArgumentException134_$init135($this);
        return;
    }
    jl_NumberFormatException137_$init139 = function($this, a) {
        jl_IllegalArgumentException134_$init136($this, a);
        return;
    }
}
$rt_methodStubs(jl_NumberFormatException137_$clinit, ['jl_NumberFormatException137_$init138', 'jl_NumberFormatException137_$init139']);
jl_NumberFormatException137.$init138 = function() {
    var result = new jl_NumberFormatException137();
    result.$init138();
    return result;
}
jl_NumberFormatException137.$init139 = function(a) {
    var result = new jl_NumberFormatException137();
    result.$init139(a);
    return result;
}
$rt_virtualMethods(jl_NumberFormatException137,
    "$init138", function() { jl_NumberFormatException137_$init138(this); },
    "$init139", function(a) { jl_NumberFormatException137_$init139(this, a); });
function oadm_MinesModel$RowModel140() {
}
$rt_declClass(oadm_MinesModel$RowModel140, {
    name : "org.apidesign.demo.minesweeper.MinesModel$RowModel",
    superclass : jl_Object7,
    clinit : function() { oadm_MinesModel$RowModel140_$clinit(); } });
function oadm_MinesModel$RowModel140_$clinit() {
    oadm_MinesModel$RowModel140_$clinit = function(){};
    oadm_MinesModel$RowModel140_$init141 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(oadm_MinesModel$RowModel140_$clinit, ['oadm_MinesModel$RowModel140_$init141']);
oadm_MinesModel$RowModel140.$init141 = function() {
    var result = new oadm_MinesModel$RowModel140();
    result.$init141();
    return result;
}
$rt_virtualMethods(oadm_MinesModel$RowModel140,
    "$init141", function() { oadm_MinesModel$RowModel140_$init141(this); });
function ju_Arrays142() {
}
$rt_declClass(ju_Arrays142, {
    name : "java.util.Arrays",
    superclass : jl_Object7,
    clinit : function() { ju_Arrays142_$clinit(); } });
function ju_Arrays142_$clinit() {
    ju_Arrays142_$clinit = function(){};
    ju_Arrays142_merge143 = function(a, b, c, d, e, f) {
        var g, h, i, j, k;
        g = c;
        h = d;
        block1: {
            block2: {
                while (true) {
                    if ((c == d)) {
                        break block2;
                    }
                    if ((h == e)) {
                        break;
                    }
                    i = a.data;
                    j = i[c];
                    k = i[h];
                    if ((ju_Collections$8144_compare145($rt_nullCheck(f), j, k) > 0)) {
                        i = ((g + 1) | 0);
                        b.data[g] = k;
                        h = ((h + 1) | 0);
                    } else {
                        i = ((g + 1) | 0);
                        b.data[g] = j;
                        c = ((c + 1) | 0);
                    }
                    g = i;
                }
                while (true) {
                    if ((c >= d)) {
                        break block1;
                    }
                    e = ((g + 1) | 0);
                    f = ((c + 1) | 0);
                    c = a.data[c];
                    b.data[g] = c;
                    g = e;
                    c = f;
                }
            }
            while ((h < e)) {
                c = ((g + 1) | 0);
                d = ((h + 1) | 0);
                f = a.data[h];
                b.data[g] = f;
                g = c;
                h = d;
            }
        }
        return;
    }
    ju_Arrays142_copyOf146 = function(a, b) {
        var c, d, e;
        c = $rt_createCharArray(b);
        a = a.data;
        b = jl_Math147_min148(b, a.length);
        d = 0;
        while ((d < b)) {
            e = a[d];
            c.data[d] = e;
            d = ((d + 1) | 0);
        }
        return c;
    }
    ju_Arrays142_copyOf149 = function(a, b) {
        var c, d, e;
        c = jlr_Array150_newInstance151(jl_Class0_getComponentType152($rt_nullCheck($rt_cls($rt_nullCheck(a).constructor))), b);
        a = a.data;
        b = jl_Math147_min148(b, a.length);
        d = 0;
        while ((d < b)) {
            e = a[d];
            c.data[d] = e;
            d = ((d + 1) | 0);
        }
        return c;
    }
    ju_Arrays142_fill153 = function(a, b, c, d) {
        var e;
        if ((b <= c)) {
            while ((b < c)) {
                e = ((b + 1) | 0);
                a.data[b] = d;
                b = e;
            }
            return;
        }
        $rt_throw(jl_IllegalArgumentException134.$init135());
    }
    ju_Arrays142_sort154 = function(a, b) {
        var c, d, e, f, g, h;
        c = a.data.length;
        if ((c != 0)) {
            d = $rt_createArray(jl_Object7, c);
            e = 1;
            f = a;
            while ((e < c)) {
                g = 0;
                while (true) {
                    h = f.data.length;
                    if ((g >= h)) {
                        break;
                    }
                    ju_Arrays142_merge143(f, d, g, jl_Math147_min148(h, ((g + e) | 0)), jl_Math147_min148(h, ((g + ((2 * e) | 0)) | 0)), b);
                    g = ((g + ((e * 2) | 0)) | 0);
                }
                e = ((e * 2) | 0);
                g = f;
                f = d;
                d = g;
            }
            if ((f !== a)) {
                a = 0;
                while (true) {
                    b = f.data;
                    if ((a >= b.length)) {
                        break;
                    }
                    d.data[a] = b[a];
                    a = ((a + 1) | 0);
                }
            }
            return;
        }
        return;
    }
    ju_Arrays142_$init155 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ju_Arrays142_$clinit, ['ju_Arrays142_merge143', 'ju_Arrays142_copyOf146', 'ju_Arrays142_copyOf149', 'ju_Arrays142_fill153', 'ju_Arrays142_sort154', 'ju_Arrays142_$init155']);
ju_Arrays142.$init155 = function() {
    var result = new ju_Arrays142();
    result.$init155();
    return result;
}
$rt_virtualMethods(ju_Arrays142,
    "$init155", function() { ju_Arrays142_$init155(this); });
function oahjs_Technology156() {
}
$rt_declClass(oahjs_Technology156, {
    name : "org.apidesign.html.json.spi.Technology",
    superclass : jl_Object7 });
function jl_IndexOutOfBoundsException157() {
}
$rt_declClass(jl_IndexOutOfBoundsException157, {
    name : "java.lang.IndexOutOfBoundsException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_IndexOutOfBoundsException157_$clinit(); } });
function jl_IndexOutOfBoundsException157_$clinit() {
    jl_IndexOutOfBoundsException157_$clinit = function(){};
    jl_IndexOutOfBoundsException157_$init158 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
    jl_IndexOutOfBoundsException157_$init159 = function($this, a) {
        jl_RuntimeException131_$init133($this, a);
        return;
    }
}
$rt_methodStubs(jl_IndexOutOfBoundsException157_$clinit, ['jl_IndexOutOfBoundsException157_$init158', 'jl_IndexOutOfBoundsException157_$init159']);
jl_IndexOutOfBoundsException157.$init158 = function() {
    var result = new jl_IndexOutOfBoundsException157();
    result.$init158();
    return result;
}
jl_IndexOutOfBoundsException157.$init159 = function(a) {
    var result = new jl_IndexOutOfBoundsException157();
    result.$init159(a);
    return result;
}
$rt_virtualMethods(jl_IndexOutOfBoundsException157,
    "$init158", function() { jl_IndexOutOfBoundsException157_$init158(this); },
    "$init159", function(a) { jl_IndexOutOfBoundsException157_$init159(this, a); });
function jl_ArrayIndexOutOfBoundsException160() {
}
$rt_declClass(jl_ArrayIndexOutOfBoundsException160, {
    name : "java.lang.ArrayIndexOutOfBoundsException",
    superclass : jl_IndexOutOfBoundsException157,
    clinit : function() { jl_ArrayIndexOutOfBoundsException160_$clinit(); } });
function jl_ArrayIndexOutOfBoundsException160_$clinit() {
    jl_ArrayIndexOutOfBoundsException160_$clinit = function(){};
    jl_ArrayIndexOutOfBoundsException160_$init161 = function($this) {
        jl_IndexOutOfBoundsException157_$init158($this);
        return;
    }
}
$rt_methodStubs(jl_ArrayIndexOutOfBoundsException160_$clinit, ['jl_ArrayIndexOutOfBoundsException160_$init161']);
jl_ArrayIndexOutOfBoundsException160.$init161 = function() {
    var result = new jl_ArrayIndexOutOfBoundsException160();
    result.$init161();
    return result;
}
$rt_virtualMethods(jl_ArrayIndexOutOfBoundsException160,
    "$init161", function() { jl_ArrayIndexOutOfBoundsException160_$init161(this); });
function jl_CharSequence162() {
}
$rt_declClass(jl_CharSequence162, {
    name : "java.lang.CharSequence",
    superclass : jl_Object7 });
function jl_Comparable163() {
}
$rt_declClass(jl_Comparable163, {
    name : "java.lang.Comparable",
    superclass : jl_Object7 });
function ji_Serializable164() {
}
$rt_declClass(ji_Serializable164, {
    name : "java.io.Serializable",
    superclass : jl_Object7 });
function jl_String3() {
    this.hashCode165 = 0;
    this.characters166 = null;
}
$rt_declClass(jl_String3, {
    name : "java.lang.String",
    interfaces : [jl_CharSequence162, jl_Comparable163, ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jl_String3_$clinit(); } });
function jl_String3_$clinit() {
    jl_String3_$clinit = function(){};
    jl_String3_$init167 = function($this, a, b, c) {
        var d, e, f;
        jl_Object7_$init10($this);
        $this.characters166 = $rt_createCharArray(c);
        d = 0;
        while ((d < c)) {
            e = $this.characters166;
            f = a.data[((d + b) | 0)];
            e.data[d] = f;
            d = ((d + 1) | 0);
        }
        return;
    }
    jl_String3_$init168 = function($this, a, b, c) {
        var d, e, f, g, h;
        jl_Object7_$init10($this);
        $this.characters166 = $rt_createCharArray(((c * 2) | 0));
        d = 0;
        e = 0;
        while ((e < c)) {
            f = ((b + 1) | 0);
            b = a.data[b];
            if ((b < 65536)) {
                g = $this.characters166;
                h = ((d + 1) | 0);
                g.data[d] = (b & 65535);
            } else {
                h = $this.characters166;
                g = ((d + 1) | 0);
                h.data[d] = otcic_UTF16Helper169_highSurrogate170(b);
                d = $this.characters166;
                h = ((g + 1) | 0);
                d.data[g] = otcic_UTF16Helper169_lowSurrogate171(b);
            }
            e = ((e + 1) | 0);
            b = f;
            d = h;
        }
        if ((d < $this.characters166.data.length)) {
            $this.characters166 = ju_Arrays142_copyOf146($this.characters166, d);
        }
        return;
    }
    jl_String3_$init4 = function($this, a) {
        var b, c, d, e;
        jl_Object7_$init10($this);
        a = a.data;
        b = a.length;
        $this.characters166 = $rt_createCharArray(b);
        c = 0;
        while ((c < b)) {
            d = $this.characters166;
            e = a[c];
            d.data[c] = e;
            c = ((c + 1) | 0);
        }
        return;
    }
    jl_String3_$init172 = function($this) {
        jl_Object7_$init10($this);
        $this.characters166 = $rt_createCharArray(0);
        return;
    }
    jl_String3_$init173 = function($this, a) {
        jl_Object7_$init10($this);
        $this.characters166 = a.characters166;
        return;
    }
}
$rt_methodStubs(jl_String3_$clinit, ['jl_String3_$init167', 'jl_String3_$init168', 'jl_String3_$init4', 'jl_String3_$init172', 'jl_String3_$init173']);
function jl_String3_startsWith174($this, a) {
    if (($this !== a)) {
        return jl_String3_startsWith175($rt_nullCheck($this), a, 0);
    }
    return 1;
}
function jl_String3_charAt176($this, a) {
    if (((a >= 0) && (a < $this.characters166.data.length))) {
        return $this.characters166.data[a];
    }
    $rt_throw(jl_StringIndexOutOfBoundsException177.$init178());
}
function jl_String3_substring179($this, a, b) {
    if ((a <= b)) {
        return jl_String3.$init167($this.characters166, a, ((b - a) | 0));
    }
    $rt_throw(jl_IndexOutOfBoundsException157.$init158());
}
function jl_String3_hashCode14($this) {
    var a, b, c, d;
    if (($this.hashCode165 == 0)) {
        $this.hashCode165 = ($this.hashCode165 ^ 734262231);
        a = $this.characters166.data;
        b = a.length;
        c = 0;
        while ((c < b)) {
            d = a[c];
            $this.hashCode165 = (($this.hashCode165 << 4) | ($this.hashCode165 >>> 28));
            $this.hashCode165 = ($this.hashCode165 ^ (347236277 ^ d));
            if (($this.hashCode165 == 0)) {
                $this.hashCode165 = (($this.hashCode165 + 1) | 0);
            }
            c = ((c + 1) | 0);
        }
    }
    return $this.hashCode165;
}
function jl_String3_indexOf180($this, a, b) {
    var c;
    if ((a >= 65536)) {
        c = otcic_UTF16Helper169_highSurrogate170(a);
        a = otcic_UTF16Helper169_lowSurrogate171(a);
        block2: {
            while (true) {
                if ((b >= (($this.characters166.data.length - 1) | 0))) {
                    break block2;
                }
                if ((($this.characters166.data[b] == c) && ($this.characters166.data[((b + 1) | 0)] == a))) {
                    break;
                }
                b = ((b + 1) | 0);
            }
            return b;
        }
        return -1;
    }
    a = (a & 65535);
    block6: {
        while (true) {
            if ((b >= $this.characters166.data.length)) {
                break block6;
            }
            if (($this.characters166.data[b] == a)) {
                break;
            }
            b = ((b + 1) | 0);
        }
        return b;
    }
    return -1;
}
function jl_String3_compareTo181($this, a) {
    return jl_String3_compareTo182($rt_nullCheck($this), a);
}
function jl_String3_toLowerCase183($this) {
    var a, b, c, d;
    if ((jl_String3_isEmpty184($rt_nullCheck($this)) == 0)) {
        a = $rt_createIntArray($this.characters166.data.length);
        b = 0;
        c = 0;
        while ((c < $this.characters166.data.length)) {
            if (((c != (($this.characters166.data.length - 1) | 0)) && ((otcic_UTF16Helper169_isHighSurrogate185($this.characters166.data[c]) != 0) && (otcic_UTF16Helper169_isLowSurrogate186($this.characters166.data[((c + 1) | 0)]) != 0)))) {
                d = ((b + 1) | 0);
                a.data[b] = jl_Character187_toLowerCase188(otcic_UTF16Helper169_buildCodePoint189($this.characters166.data[c], $this.characters166.data[((c + 1) | 0)]));
                c = ((c + 1) | 0);
            } else {
                d = ((b + 1) | 0);
                a.data[b] = jl_Character187_toLowerCase190($this.characters166.data[c]);
            }
            c = ((c + 1) | 0);
            b = d;
        }
        return jl_String3.$init168(a, 0, b);
    }
    return $this;
}
function jl_String3_getChars6($this, a, b, c, d) {
    var e, f, g;
    if (((a >= 0) && (a <= b))) {
        e = $rt_nullCheck($this);
        if (((b <= e.length5()) && (d >= 0))) {
            f = ((d + ((b - a) | 0)) | 0);
            g = c.data;
            if ((f <= g.length)) {
                while ((a < b)) {
                    f = ((d + 1) | 0);
                    c = ((a + 1) | 0);
                    g[d] = e.charAt176(a);
                    d = f;
                    a = c;
                }
                return;
            }
        }
    }
    $rt_throw(jl_IndexOutOfBoundsException157.$init158());
}
function jl_String3_isEmpty184($this) {
    var a;
    if (($this.characters166.data.length != 0)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function jl_String3_startsWith175($this, a, b) {
    var c, d, e, f;
    c = $rt_nullCheck(a);
    a = ((b + jl_String3_length5(c)) | 0);
    d = $rt_nullCheck($this);
    if ((a <= jl_String3_length5(d))) {
        a = 0;
        block2: {
            while (true) {
                if ((a >= jl_String3_length5(c))) {
                    break block2;
                }
                e = jl_String3_charAt176(c, a);
                f = ((b + 1) | 0);
                if ((e != jl_String3_charAt176(d, b))) {
                    break;
                }
                a = ((a + 1) | 0);
                b = f;
            }
            return 0;
        }
        return 1;
    }
    return 0;
}
function jl_String3_length5($this) {
    return $this.characters166.data.length;
}
function jl_String3_equals13($this, a) {
    var b, c;
    if (($this !== a)) {
        if (((a instanceof jl_String3) != 0)) {
            b = $rt_nullCheck(a);
            a = jl_String3_length5(b);
            c = $rt_nullCheck($this);
            if ((a == jl_String3_length5(c))) {
                a = 0;
                block4: {
                    while (true) {
                        if ((a >= jl_String3_length5(b))) {
                            break block4;
                        }
                        if ((jl_String3_charAt176(c, a) != jl_String3_charAt176(b, a))) {
                            break;
                        }
                        a = ((a + 1) | 0);
                    }
                    return 0;
                }
                return 1;
            }
            return 0;
        }
        return 0;
    }
    return 1;
}
function jl_String3_lastIndexOf191($this, a, b) {
    var c, d;
    if ((a >= 65536)) {
        c = otcic_UTF16Helper169_highSurrogate170(a);
        d = otcic_UTF16Helper169_lowSurrogate171(a);
        block2: {
            while (true) {
                if ((b < 1)) {
                    break block2;
                }
                if ((($this.characters166.data[b] == d) && ($this.characters166.data[((b - 1) | 0)] == c))) {
                    break;
                }
                b = ((b + -1) | 0);
            }
            return b;
        }
        return -1;
    }
    a = (a & 65535);
    block6: {
        while (true) {
            if ((b < 0)) {
                break block6;
            }
            if (($this.characters166.data[b] == a)) {
                break;
            }
            b = ((b + -1) | 0);
        }
        return b;
    }
    return -1;
}
function jl_String3_trim192($this) {
    var a, b, c;
    a = 0;
    b = $rt_nullCheck($this);
    c = ((jl_String3_length5(b) - 1) | 0);
    block1: {
        block2: {
            while (true) {
                if ((a > c)) {
                    break block2;
                }
                if ((jl_String3_charAt176(b, a) > 32)) {
                    break;
                }
                a = ((a + 1) | 0);
            }
            break block1;
        }
    }
    while (((a <= c) && (jl_String3_charAt176(b, c) <= 32))) {
        c = ((c + -1) | 0);
    }
    return jl_String3_substring179(b, a, ((c + 1) | 0));
}
function jl_String3_lastIndexOf193($this, a) {
    var b;
    b = $rt_nullCheck($this);
    return jl_String3_lastIndexOf191(b, a, ((jl_String3_length5(b) - 1) | 0));
}
function jl_String3_compareTo182($this, a) {
    var b, c, d, e;
    if (($this !== a)) {
        b = $rt_nullCheck($this);
        c = jl_String3_length5(b);
        d = $rt_nullCheck(a);
        a = jl_Math147_min148(c, jl_String3_length5(d));
        c = 0;
        block2: {
            while (true) {
                if ((c >= a)) {
                    break block2;
                }
                e = ((jl_String3_charAt176(b, c) - jl_String3_charAt176(d, c)) | 0);
                if ((e != 0)) {
                    break;
                }
                c = ((c + 1) | 0);
            }
            return e;
        }
        return ((jl_String3_length5(b) - jl_String3_length5(d)) | 0);
    }
    return 0;
}
function jl_String3_toString15($this) {
    return $this;
}
function jl_String3_endsWith194($this, a) {
    var b, c, d, e, f;
    if (($this !== a)) {
        b = $rt_nullCheck(a);
        a = jl_String3_length5(b);
        c = $rt_nullCheck($this);
        if ((a <= jl_String3_length5(c))) {
            a = 0;
            d = ((jl_String3_length5(c) - jl_String3_length5(b)) | 0);
            block3: {
                while (true) {
                    if ((d >= jl_String3_length5(c))) {
                        break block3;
                    }
                    e = jl_String3_charAt176(c, d);
                    f = ((a + 1) | 0);
                    if ((e != jl_String3_charAt176(b, a))) {
                        break;
                    }
                    d = ((d + 1) | 0);
                    a = f;
                }
                return 0;
            }
            return 1;
        }
        return 0;
    }
    return 1;
}
jl_String3.$init167 = function(a, b, c) {
    var result = new jl_String3();
    result.$init167(a, b, c);
    return result;
}
jl_String3.$init168 = function(a, b, c) {
    var result = new jl_String3();
    result.$init168(a, b, c);
    return result;
}
jl_String3.$init4 = function(a) {
    var result = new jl_String3();
    result.$init4(a);
    return result;
}
jl_String3.$init172 = function() {
    var result = new jl_String3();
    result.$init172();
    return result;
}
jl_String3.$init173 = function(a) {
    var result = new jl_String3();
    result.$init173(a);
    return result;
}
$rt_virtualMethods(jl_String3,
    "$init167", function(a, b, c) { jl_String3_$init167(this, a, b, c); },
    "startsWith174", function(a) { return jl_String3_startsWith174(this, a); },
    "charAt176", function(a) { return jl_String3_charAt176(this, a); },
    "substring179", function(a, b) { return jl_String3_substring179(this, a, b); },
    "hashCode14", function() { return jl_String3_hashCode14(this); },
    "indexOf180", function(a, b) { return jl_String3_indexOf180(this, a, b); },
    "compareTo181", function(a) { return jl_String3_compareTo181(this, a); },
    "toLowerCase183", function() { return jl_String3_toLowerCase183(this); },
    "getChars6", function(a, b, c, d) { jl_String3_getChars6(this, a, b, c, d); },
    "isEmpty184", function() { return jl_String3_isEmpty184(this); },
    "startsWith175", function(a, b) { return jl_String3_startsWith175(this, a, b); },
    "length5", function() { return jl_String3_length5(this); },
    "equals13", function(a) { return jl_String3_equals13(this, a); },
    "lastIndexOf191", function(a, b) { return jl_String3_lastIndexOf191(this, a, b); },
    "trim192", function() { return jl_String3_trim192(this); },
    "lastIndexOf193", function(a) { return jl_String3_lastIndexOf193(this, a); },
    "compareTo182", function(a) { return jl_String3_compareTo182(this, a); },
    "toString15", function() { return jl_String3_toString15(this); },
    "$init168", function(a, b, c) { jl_String3_$init168(this, a, b, c); },
    "$init4", function(a) { jl_String3_$init4(this, a); },
    "endsWith194", function(a) { return jl_String3_endsWith194(this, a); },
    "$init172", function() { jl_String3_$init172(this); },
    "$init173", function(a) { jl_String3_$init173(this, a); });
function ju_Collections195() {
}
$rt_declClass(ju_Collections195, {
    name : "java.util.Collections",
    superclass : jl_Object7,
    clinit : function() { ju_Collections195_$clinit(); } });
function ju_Collections195_$clinit() {
    ju_Collections195_$clinit = function(){};
    ju_Collections195_sort196 = function(a) {
        ju_Collections195_sort197(a, ju_Collections$8144.$init198());
        return;
    }
    ju_Collections195_$init199 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    ju_Collections195_unmodifiableList200 = function(a) {
        return ju_Collections$6201.$init202(a);
    }
    ju_Collections195_sort197 = function(a, b) {
        var c, d;
        c = $rt_nullCheck(a);
        a = $rt_createArray(jl_Object7, c.size55());
        ju_AbstractCollection204_toArray205(c, a);
        ju_Arrays142_sort154(a, b);
        b = 0;
        while (true) {
            d = a.data;
            if ((b >= d.length)) {
                break;
            }
            c.set207(b, d[b]);
            b = ((b + 1) | 0);
        }
        return;
    }
}
$rt_methodStubs(ju_Collections195_$clinit, ['ju_Collections195_sort196', 'ju_Collections195_$init199', 'ju_Collections195_unmodifiableList200', 'ju_Collections195_sort197']);
ju_Collections195.$init199 = function() {
    var result = new ju_Collections195();
    result.$init199();
    return result;
}
$rt_virtualMethods(ju_Collections195,
    "$init199", function() { ju_Collections195_$init199(this); });
function jl_Error208() {
}
$rt_declClass(jl_Error208, {
    name : "java.lang.Error",
    superclass : jl_Throwable111,
    clinit : function() { jl_Error208_$clinit(); } });
function jl_Error208_$clinit() {
    jl_Error208_$clinit = function(){};
    jl_Error208_$init209 = function($this) {
        jl_Throwable111_$init118($this);
        return;
    }
}
$rt_methodStubs(jl_Error208_$clinit, ['jl_Error208_$init209']);
jl_Error208.$init209 = function() {
    var result = new jl_Error208();
    result.$init209();
    return result;
}
$rt_virtualMethods(jl_Error208,
    "$init209", function() { jl_Error208_$init209(this); });
function jl_LinkageError210() {
}
$rt_declClass(jl_LinkageError210, {
    name : "java.lang.LinkageError",
    superclass : jl_Error208,
    clinit : function() { jl_LinkageError210_$clinit(); } });
function jl_LinkageError210_$clinit() {
    jl_LinkageError210_$clinit = function(){};
    jl_LinkageError210_$init211 = function($this) {
        jl_Error208_$init209($this);
        return;
    }
}
$rt_methodStubs(jl_LinkageError210_$clinit, ['jl_LinkageError210_$init211']);
jl_LinkageError210.$init211 = function() {
    var result = new jl_LinkageError210();
    result.$init211();
    return result;
}
$rt_virtualMethods(jl_LinkageError210,
    "$init211", function() { jl_LinkageError210_$init211(this); });
function jl_IncompatibleClassChangeError212() {
}
$rt_declClass(jl_IncompatibleClassChangeError212, {
    name : "java.lang.IncompatibleClassChangeError",
    superclass : jl_LinkageError210,
    clinit : function() { jl_IncompatibleClassChangeError212_$clinit(); } });
function jl_IncompatibleClassChangeError212_$clinit() {
    jl_IncompatibleClassChangeError212_$clinit = function(){};
    jl_IncompatibleClassChangeError212_$init213 = function($this) {
        jl_LinkageError210_$init211($this);
        return;
    }
}
$rt_methodStubs(jl_IncompatibleClassChangeError212_$clinit, ['jl_IncompatibleClassChangeError212_$init213']);
jl_IncompatibleClassChangeError212.$init213 = function() {
    var result = new jl_IncompatibleClassChangeError212();
    result.$init213();
    return result;
}
$rt_virtualMethods(jl_IncompatibleClassChangeError212,
    "$init213", function() { jl_IncompatibleClassChangeError212_$init213(this); });
function jl_AutoCloseable214() {
}
$rt_declClass(jl_AutoCloseable214, {
    name : "java.lang.AutoCloseable",
    superclass : jl_Object7 });
function ji_Closeable215() {
}
$rt_declClass(ji_Closeable215, {
    name : "java.io.Closeable",
    interfaces : [jl_AutoCloseable214],
    superclass : jl_Object7 });
function jl_NoSuchFieldError216() {
}
$rt_declClass(jl_NoSuchFieldError216, {
    name : "java.lang.NoSuchFieldError",
    superclass : jl_IncompatibleClassChangeError212,
    clinit : function() { jl_NoSuchFieldError216_$clinit(); } });
function jl_NoSuchFieldError216_$clinit() {
    jl_NoSuchFieldError216_$clinit = function(){};
    jl_NoSuchFieldError216_$init217 = function($this) {
        jl_IncompatibleClassChangeError212_$init213($this);
        return;
    }
}
$rt_methodStubs(jl_NoSuchFieldError216_$clinit, ['jl_NoSuchFieldError216_$init217']);
jl_NoSuchFieldError216.$init217 = function() {
    var result = new jl_NoSuchFieldError216();
    result.$init217();
    return result;
}
$rt_virtualMethods(jl_NoSuchFieldError216,
    "$init217", function() { jl_NoSuchFieldError216_$init217(this); });
function jl_Iterable218() {
}
$rt_declClass(jl_Iterable218, {
    name : "java.lang.Iterable",
    superclass : jl_Object7 });
function ju_Collection203() {
}
$rt_declClass(ju_Collection203, {
    name : "java.util.Collection",
    interfaces : [jl_Iterable218],
    superclass : jl_Object7 });
function ju_AbstractCollection204() {
}
$rt_declClass(ju_AbstractCollection204, {
    name : "java.util.AbstractCollection",
    interfaces : [ju_Collection203],
    superclass : jl_Object7,
    clinit : function() { ju_AbstractCollection204_$clinit(); } });
function ju_AbstractCollection204_$clinit() {
    ju_AbstractCollection204_$clinit = function(){};
    ju_AbstractCollection204_$init219 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ju_AbstractCollection204_$clinit, ['ju_AbstractCollection204_$init219']);
function ju_AbstractCollection204_addAll78($this, a) {
    var b, c;
    b = 0;
    a = ju_AbstractList66_iterator107($rt_nullCheck(a));
    while (true) {
        c = $rt_nullCheck(a);
        if ((ju_AbstractList$1108_hasNext109(c) == 0)) {
            break;
        }
        if ((onhji_JSONList76_add67($rt_nullCheck($this), ju_AbstractList$1108_next110(c)) == 0)) {
            continue;
        }
        b = 1;
    }
    return b;
}
function ju_AbstractCollection204_toArray205($this, a) {
    var b, c, d, e, f;
    b = $rt_nullCheck($this);
    c = b.size55();
    d = a.data;
    e = d.length;
    if ((e >= c)) {
        while ((c < e)) {
            d[c] = null;
            c = ((c + 1) | 0);
        }
    } else {
        a = jlr_Array150_newInstance151(jl_Class0_getComponentType152($rt_nullCheck($rt_cls($rt_nullCheck(a).constructor))), c);
    }
    c = 0;
    d = ju_AbstractList66_iterator107(b);
    while (true) {
        f = $rt_nullCheck(d);
        if ((ju_AbstractList$1108_hasNext109(f) == 0)) {
            break;
        }
        e = ((c + 1) | 0);
        f = ju_AbstractList$1108_next110(f);
        a.data[c] = f;
        c = e;
    }
    return a;
}
ju_AbstractCollection204.$init219 = function() {
    var result = new ju_AbstractCollection204();
    result.$init219();
    return result;
}
$rt_virtualMethods(ju_AbstractCollection204,
    "$init219", function() { ju_AbstractCollection204_$init219(this); },
    "addAll78", function(a) { return ju_AbstractCollection204_addAll78(this, a); },
    "toArray205", function(a) { return ju_AbstractCollection204_toArray205(this, a); });
function ju_List206() {
}
$rt_declClass(ju_List206, {
    name : "java.util.List",
    interfaces : [ju_Collection203],
    superclass : jl_Object7 });
function ju_AbstractList66() {
    this.modCount220 = 0;
}
$rt_declClass(ju_AbstractList66, {
    name : "java.util.AbstractList",
    interfaces : [ju_List206],
    superclass : ju_AbstractCollection204,
    clinit : function() { ju_AbstractList66_$clinit(); } });
function ju_AbstractList66_$clinit() {
    ju_AbstractList66_$clinit = function(){};
    ju_AbstractList66_$init221 = function($this) {
        ju_AbstractCollection204_$init219($this);
        return;
    }
}
$rt_methodStubs(ju_AbstractList66_$clinit, ['ju_AbstractList66_$init221']);
function ju_AbstractList66_set207($this, a, b) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function ju_AbstractList66_add224($this, a, b) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function ju_AbstractList66_add67($this, a) {
    var b;
    b = $rt_nullCheck($this);
    b.add224(b.size55(), a);
    return 1;
}
function ju_AbstractList66_iterator107($this) {
    return ju_AbstractList$1108.$init225($this);
}
ju_AbstractList66.$init221 = function() {
    var result = new ju_AbstractList66();
    result.$init221();
    return result;
}
$rt_virtualMethods(ju_AbstractList66,
    "set207", function(a, b) { return ju_AbstractList66_set207(this, a, b); },
    "add224", function(a, b) { ju_AbstractList66_add224(this, a, b); },
    "add67", function(a) { return ju_AbstractList66_add67(this, a); },
    "iterator107", function() { return ju_AbstractList66_iterator107(this); },
    "$init221", function() { ju_AbstractList66_$init221(this); });
function oahjs_Technology$BatchInit226() {
}
$rt_declClass(oahjs_Technology$BatchInit226, {
    name : "org.apidesign.html.json.spi.Technology$BatchInit",
    interfaces : [oahjs_Technology156],
    superclass : jl_Object7 });
function oadm_MainBrwsr227() {
}
$rt_declClass(oadm_MainBrwsr227, {
    name : "org.apidesign.demo.minesweeper.MainBrwsr",
    superclass : jl_Object7,
    clinit : function() { oadm_MainBrwsr227_$clinit(); } });
function oadm_MainBrwsr227_$clinit() {
    oadm_MainBrwsr227_$clinit = function(){};
    oadm_MainBrwsr227_$clinit228 = function() {
        var a;
        block1: {
            try {
                oadm_MinesModel43_main92($rt_createArray(jl_String3, 0));
            } catch ($e) {
                $je = $e.$javaException;
                if ($je && $je instanceof jl_Exception127) {
                    a = $je;
                    break block1;
                } else {
                    throw $e;
                }
            }
            return;
        }
        $rt_throw(jl_IllegalStateException229.$init230(a));
    }
    oadm_MainBrwsr227_$init231 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oadm_MainBrwsr227_$clinit228();
}
$rt_methodStubs(oadm_MainBrwsr227_$clinit, ['oadm_MainBrwsr227_$clinit228', 'oadm_MainBrwsr227_$init231']);
oadm_MainBrwsr227.$init231 = function() {
    var result = new oadm_MainBrwsr227();
    result.$init231();
    return result;
}
$rt_virtualMethods(oadm_MainBrwsr227,
    "$init231", function() { oadm_MainBrwsr227_$init231(this); });
function jl_Runnable232() {
}
$rt_declClass(jl_Runnable232, {
    name : "java.lang.Runnable",
    superclass : jl_Object7 });
function jl_Thread233() {
    this.name234 = null;
    this.target235 = null;
}
jl_Thread233.currentThread236 = null;
$rt_declClass(jl_Thread233, {
    name : "java.lang.Thread",
    interfaces : [jl_Runnable232],
    superclass : jl_Object7,
    clinit : function() { jl_Thread233_$clinit(); } });
function jl_Thread233_$clinit() {
    jl_Thread233_$clinit = function(){};
    jl_Thread233_$clinit237 = function() {
        jl_Thread233.currentThread236 = jl_Thread233.$init238($rt_str("main"));
        return;
    }
    jl_Thread233_currentThread239 = function() {
        return jl_Thread233.currentThread236;
    }
    jl_Thread233_$init240 = function($this) {
        jl_Thread233_$init241($this, null, null);
        return;
    }
    jl_Thread233_$init238 = function($this, a) {
        jl_Thread233_$init241($this, a, null);
        return;
    }
    jl_Thread233_$init241 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.name234 = a;
        $this.target235 = b;
        return;
    }
    jl_Thread233_$clinit237();
}
$rt_methodStubs(jl_Thread233_$clinit, ['jl_Thread233_$clinit237', 'jl_Thread233_currentThread239', 'jl_Thread233_$init240', 'jl_Thread233_$init238', 'jl_Thread233_$init241']);
function jl_Thread233_getId242($this) {
    return Long_fromInt(1);
}
jl_Thread233.$init240 = function() {
    var result = new jl_Thread233();
    result.$init240();
    return result;
}
jl_Thread233.$init238 = function(a) {
    var result = new jl_Thread233();
    result.$init238(a);
    return result;
}
jl_Thread233.$init241 = function(a, b) {
    var result = new jl_Thread233();
    result.$init241(a, b);
    return result;
}
$rt_virtualMethods(jl_Thread233,
    "getId242", function() { return jl_Thread233_getId242(this); },
    "$init240", function() { jl_Thread233_$init240(this); },
    "$init238", function(a) { jl_Thread233_$init238(this, a); },
    "$init241", function(a, b) { jl_Thread233_$init241(this, a, b); });
function jl_Character187() {
    this.value243 = 0;
}
jl_Character187.digitMapping244 = null;
jl_Character187.TYPE245 = null;
jl_Character187.characterCache246 = null;
$rt_declClass(jl_Character187, {
    name : "java.lang.Character",
    interfaces : [jl_Comparable163],
    superclass : jl_Object7,
    clinit : function() { jl_Character187_$clinit(); } });
function jl_Character187_$clinit() {
    jl_Character187_$clinit = function(){};
    jl_Character187_getNumericValue247 = function(a) {
        var b, c, d, e, f;
        b = jl_Character187_getDigitMapping248();
        c = 0;
        b = b.data;
        d = ((((b.length / 2) | 0) - 1) | 0);
        while ((d >= c)) {
            block3: {
                e = ((((c + d) | 0) / 2) | 0);
                f = $rt_compare(a, b[((e * 2) | 0)]);
                if ((f <= 0)) {
                    if ((f < 0)) {
                        d = ((e - 1) | 0);
                        break block3;
                    }
                    return b[((((e * 2) | 0) + 1) | 0)];
                }
                c = ((e + 1) | 0);
            }
        }
        return -1;
    }
    jl_Character187_toChars249 = function(a) {
        var b, c, d, e;
        if ((a < 65536)) {
            b = $rt_createCharArray(1);
            b.data[0] = (a & 65535);
            return b;
        }
        b = $rt_createCharArray(2);
        c = 0;
        d = otcic_UTF16Helper169_highSurrogate170(a);
        e = b.data;
        e[c] = d;
        e[1] = otcic_UTF16Helper169_lowSurrogate171(a);
        return b;
    }
    jl_Character187_obtainDigitMapping250 = function() {
        return $rt_str("zzzªzzzz«{zzz¬|zzz­}zzz®~zzz¯zzz°zzz±zzz²zzz³zzz»zzz¼zzz½zzz¾zzz¿zzzÀzzzÁzzzÂzzzÃzzzÄzzzÅzzzÆzzzÇzzzÈzzzÉzzzÊzzzËzzzÌzzzÍzzzÎzzzÏzzzÐzzzÑzzzÒzzzÓzzzÔzzzÛzzzÜzzzÝzzzÞzzzßzzzàzzzázzzâzzzãzzzäzzzåzzzæzzzçzzzèzzzézzzêzzzëzzzìzzzízzzîzzzïzzzðzzzñzzzòzzzózzzôzzÚzzzÛ{zzÜ|zzÝ}zzÞ~zzßzzàzzázzâzzãzzŪzzzū{zzŬ|zzŭ}zzŮ~zzůzzŰzzűzzŲzzųzzĺzzzĻ{zzļ|zzĽ}zzľ~zzĿzzŀzzŁzzłzzŃzzàzzzá{zzâ|zzã}zzä~zzåzzæzzçzzèzzézz" +
        "Šzzzš{zzŢ|zzţ}zzŤ~zzťzzŦzzŧzzŨzzũzzàzzzá{zzâ|zzã}zzä~zzåzzæzzçzzèzzézzŠzzzš{zzŢ|zzţ}zzŤ~zzťzzŦzzŧzzŨzzũzzàzzzá{zzâ|zzã}zzä~zzåzzæzzçzzèzzézzŠzzzš{zzŢ|zzţ}zzŤ~zzťzzŦzzŧzzŨzzũzzàzzzá{zzâ|zzã}zzä~zzåzzæzzçzzèzzézzŠzzzš{zzŢ|zzţ}zzŤ~zzťzzŦzzŧzzŨzzũzzàzzzá{zzâ|zzã}zzä~zzåzzæzzçzzèzzézzÊzzzË{zzÌ|zzÍ}zzÎ~zzÏzzÐzzÑzzÒzzÓzzŊzzzŋ{zzŌ|zzō}zzŎ~zzŏzzŐzzőzzŒzzœzzzzz{zz" +
        "|zz}zz~zzzz zz¡zz¢zz£zzºzzz»{zz¼|zz½}zz¾~zz¿zzÀzzÁzzÂzzÃzzĊzzzċ{zzČ|zzč}zzĎ~zzďzzĐzzđzzĒzzēzzŚzzzś{zzŜ|zzŝ}zzŞ~zzşzzŠzzšzzŢzzţzzzzz{zz|zz}zz~zzzzzzzzzzzzÀzzzÁ{zzÂ|zzÃ}zzÄ~zzÅzzÆzzÇzzÈzzÉzzŊzzzŋ{zzŌ|zzō}zzŎ~zzŏzzŐzzőzzŒzzœzzúzzzû{zzü|zzý}zzþ~zzÿzzĀzzāzzĂzzăzzĊzzzċ{zzČ|zzč}zzĎ~zzďzzĐzzđzzĒzzēzzÊzzzË{zzÌ|zzÍ}zzÎ~zzÏzzÐzzÑzzÒzzÓzzĪzzzī{zzĬ|zzĭ}zzĮ~z" +
        "zįzzİzzızzĲzzĳzzºzzz»{zz¼|zz½}zz¾~zz¿zzÀzzÁzzÂzzÃzzÊzzzË{zzÌ|zzÍ}zzÎ~zzÏzzÐzzÑzzÒzzÓzzĠzzzĠ{zzĠ|zzĠ}zzĠ~zzĠzzĠ zzĠ¡zzĠ¢zzĠ£zzĢŊzzzĢŋ{zzĢŌ|zzĢō}zzĢŎ~zzĢŏzzĢŐzzĢőzzĢŒzzĢœzzģzzzzģ{{zzģ||zzģ}}zzģ~~zzģzzģzzģzzģzzģzzģŊzzzģŋ{zzģŌ|zzģō}zzģŎ~zzģŏzzģŐzzģőzzģŒzzģœzzĤÊzzzĤË{zzĤÌ|zzĤÍ}zzĤÎ~zzĤÏzzĤÐzzĤÑzzĤÒzzĤÓzzĥŪzzzĥū{zzĥŬ|zzĥŭ}zzĥŮ~zzĥůzzĥŰzzĥűzzĥŲzzĥųzzŹzzzŹ{zzŹ|zzŹ}zzŹ~zzŹzzŹzzŹzzŹzzŹzzŹzzŹzzŹzzŹzzŹzzŹ zzŹ¡zzŹ" +
        "¢zzŹ£zzŹ¤zzŹ¥zzŹ¦zzŹ§zzŹ¨zzŹ©zzŹªzzŹ«zzŹ¬zzŹ­zzŹ®zzŹ¯zzŹ°zzŹ±zzŹ²zzŹ³zzŹ´zzŹ»zzŹ¼zzŹ½zzŹ¾zzŹ¿zzŹÀzzŹÁzzŹÂzzŹÃzzŹÄzzŹÅzzŹÆzzŹÇzzŹÈzzŹÉzzŹÊzzŹËzzŹÌzzŹÍzzŹÎzzŹÏzzŹÐzzŹÑzzŹÒzzŹÓzzŹÔzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz" +
        "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
    }
    jl_Character187_toLowerCase190 = function(a) {
        return String.fromCharCode(a).toLowerCase().charCodeAt(0)|0;
    }
    jl_Character187_compare251 = function(a, b) {
        return ((a - b) | 0);
    }
    jl_Character187_forDigit252 = function(a, b) {
        if (((b >= 2) && ((b <= 36) && (a < b)))) {
            if ((a >= 10)) {
                a = (((((97 + a) | 0) - 10) | 0) & 65535);
            } else {
                a = (((48 + a) | 0) & 65535);
            }
            return a;
        }
        return 0;
    }
    jl_Character187_valueOf253 = function(a) {
        var b;
        if ((a >= jl_Character187.characterCache246.data.length)) {
            return jl_Character187.$init254(a);
        }
        b = jl_Character187.characterCache246.data[a];
        if ((b === null)) {
            b = jl_Character187.$init254(a);
            jl_Character187.characterCache246.data[a] = b;
        }
        return b;
    }
    jl_Character187_$clinit255 = function() {
        jl_Character187.TYPE245 = $rt_cls($rt_charcls());
        jl_Character187.characterCache246 = $rt_createArray(jl_Character187, 128);
        return;
    }
    jl_Character187_getDigitMapping248 = function() {
        if ((jl_Character187.digitMapping244 === null)) {
            jl_Character187.digitMapping244 = otciu_UnicodeHelper256_decodeIntByte257(jl_Character187_obtainDigitMapping250());
        }
        return jl_Character187.digitMapping244;
    }
    jl_Character187_toLowerCase188 = function(a) {
        return String.fromCharCode(a).toLowerCase().charCodeAt(0)|0;
    }
    jl_Character187_$init254 = function($this, a) {
        jl_Object7_$init10($this);
        $this.value243 = a;
        return;
    }
    jl_Character187_getNumericValue258 = function(a) {
        return jl_Character187_getNumericValue247(a);
    }
    jl_Character187_$clinit255();
}
$rt_methodStubs(jl_Character187_$clinit, ['jl_Character187_getNumericValue247', 'jl_Character187_toChars249', 'jl_Character187_obtainDigitMapping250', 'jl_Character187_toLowerCase190', 'jl_Character187_compare251', 'jl_Character187_forDigit252', 'jl_Character187_valueOf253', 'jl_Character187_$clinit255', 'jl_Character187_getDigitMapping248', 'jl_Character187_toLowerCase188', 'jl_Character187_$init254', 'jl_Character187_getNumericValue258']);
function jl_Character187_compareTo259($this, a) {
    return jl_Character187_compare251($this.value243, a.value243);
}
function jl_Character187_compareTo181($this, a) {
    return jl_Character187_compareTo259($rt_nullCheck($this), a);
}
function jl_Character187_charValue260($this) {
    return $this.value243;
}
jl_Character187.$init254 = function(a) {
    var result = new jl_Character187();
    result.$init254(a);
    return result;
}
$rt_virtualMethods(jl_Character187,
    "compareTo259", function(a) { return jl_Character187_compareTo259(this, a); },
    "compareTo181", function(a) { return jl_Character187_compareTo181(this, a); },
    "charValue260", function() { return jl_Character187_charValue260(this); },
    "$init254", function(a) { jl_Character187_$init254(this, a); });
function ju_Map$Entry261() {
}
$rt_declClass(ju_Map$Entry261, {
    name : "java.util.Map$Entry",
    superclass : jl_Object7 });
function jl_Cloneable262() {
}
$rt_declClass(jl_Cloneable262, {
    name : "java.lang.Cloneable",
    superclass : jl_Object7 });
function ju_MapEntry263() {
    this.value264 = null;
    this.key265 = null;
}
$rt_declClass(ju_MapEntry263, {
    name : "java.util.MapEntry",
    interfaces : [ju_Map$Entry261, jl_Cloneable262],
    superclass : jl_Object7,
    clinit : function() { ju_MapEntry263_$clinit(); } });
function ju_MapEntry263_$clinit() {
    ju_MapEntry263_$clinit = function(){};
    ju_MapEntry263_$init266 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.key265 = a;
        $this.value264 = b;
        return;
    }
}
$rt_methodStubs(ju_MapEntry263_$clinit, ['ju_MapEntry263_$init266']);
ju_MapEntry263.$init266 = function(a, b) {
    var result = new ju_MapEntry263();
    result.$init266(a, b);
    return result;
}
$rt_virtualMethods(ju_MapEntry263,
    "$init266", function(a, b) { ju_MapEntry263_$init266(this, a, b); });
function jl_ClassCastException267() {
}
$rt_declClass(jl_ClassCastException267, {
    name : "java.lang.ClassCastException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_ClassCastException267_$clinit(); } });
function jl_ClassCastException267_$clinit() {
    jl_ClassCastException267_$clinit = function(){};
    jl_ClassCastException267_$init268 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
    jl_ClassCastException267_$init269 = function($this, a) {
        jl_RuntimeException131_$init133($this, a);
        return;
    }
}
$rt_methodStubs(jl_ClassCastException267_$clinit, ['jl_ClassCastException267_$init268', 'jl_ClassCastException267_$init269']);
jl_ClassCastException267.$init268 = function() {
    var result = new jl_ClassCastException267();
    result.$init268();
    return result;
}
jl_ClassCastException267.$init269 = function(a) {
    var result = new jl_ClassCastException267();
    result.$init269(a);
    return result;
}
$rt_virtualMethods(jl_ClassCastException267,
    "$init268", function() { jl_ClassCastException267_$init268(this); },
    "$init269", function(a) { jl_ClassCastException267_$init269(this, a); });
function onhci_CtxImpl$Bind270() {
    this.impl271 = null;
    this.priority272 = 0;
    this.clazz273 = null;
}
$rt_declClass(onhci_CtxImpl$Bind270, {
    name : "org.netbeans.html.context.impl.CtxImpl$Bind",
    interfaces : [jl_Comparable163],
    superclass : jl_Object7,
    clinit : function() { onhci_CtxImpl$Bind270_$clinit(); } });
function onhci_CtxImpl$Bind270_$clinit() {
    onhci_CtxImpl$Bind270_$clinit = function(){};
    onhci_CtxImpl$Bind270_$init274 = function($this, a, b, c) {
        jl_Object7_$init10($this);
        $this.clazz273 = a;
        $this.impl271 = b;
        $this.priority272 = c;
        return;
    }
    onhci_CtxImpl$Bind270_access$000275 = function(a) {
        return a.clazz273;
    }
    onhci_CtxImpl$Bind270_access$100276 = function(a) {
        return a.impl271;
    }
}
$rt_methodStubs(onhci_CtxImpl$Bind270_$clinit, ['onhci_CtxImpl$Bind270_$init274', 'onhci_CtxImpl$Bind270_access$000275', 'onhci_CtxImpl$Bind270_access$100276']);
function onhci_CtxImpl$Bind270_compareTo181($this, a) {
    return onhci_CtxImpl$Bind270_compareTo277($rt_nullCheck($this), a);
}
function onhci_CtxImpl$Bind270_compareTo277($this, a) {
    if (($this.priority272 == a.priority272)) {
        return jl_String3_compareTo182($rt_nullCheck(jl_Class0_getName20($rt_nullCheck($this.clazz273))), jl_Class0_getName20($rt_nullCheck(a.clazz273)));
    }
    return (($this.priority272 - a.priority272) | 0);
}
onhci_CtxImpl$Bind270.$init274 = function(a, b, c) {
    var result = new onhci_CtxImpl$Bind270();
    result.$init274(a, b, c);
    return result;
}
$rt_virtualMethods(onhci_CtxImpl$Bind270,
    "compareTo181", function(a) { return onhci_CtxImpl$Bind270_compareTo181(this, a); },
    "compareTo277", function(a) { return onhci_CtxImpl$Bind270_compareTo277(this, a); },
    "$init274", function(a, b, c) { onhci_CtxImpl$Bind270_$init274(this, a, b, c); });
function ju_Iterator278() {
}
$rt_declClass(ju_Iterator278, {
    name : "java.util.Iterator",
    superclass : jl_Object7 });
function ju_ServiceLoader$1279() {
    this.index280 = 0;
    this.this$0281 = null;
}
$rt_declClass(ju_ServiceLoader$1279, {
    name : "java.util.ServiceLoader$1",
    interfaces : [ju_Iterator278],
    superclass : jl_Object7,
    clinit : function() { ju_ServiceLoader$1279_$clinit(); } });
function ju_ServiceLoader$1279_$clinit() {
    ju_ServiceLoader$1279_$clinit = function(){};
    ju_ServiceLoader$1279_$init282 = function($this, a) {
        $this.this$0281 = a;
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ju_ServiceLoader$1279_$clinit, ['ju_ServiceLoader$1279_$init282']);
function ju_ServiceLoader$1279_hasNext109($this) {
    var a;
    if (($this.index280 >= ju_ServiceLoader283_access$000284($this.this$0281).data.length)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function ju_ServiceLoader$1279_next110($this) {
    var a, b;
    if (($this.index280 != ju_ServiceLoader283_access$000284($this.this$0281).data.length)) {
        a = ju_ServiceLoader283_access$000284($this.this$0281);
        b = $this.index280;
        $this.index280 = ((b + 1) | 0);
        return a.data[b];
    }
    $rt_throw(ju_NoSuchElementException285.$init286());
}
ju_ServiceLoader$1279.$init282 = function(a) {
    var result = new ju_ServiceLoader$1279();
    result.$init282(a);
    return result;
}
$rt_virtualMethods(ju_ServiceLoader$1279,
    "$init282", function(a) { ju_ServiceLoader$1279_$init282(this, a); },
    "hasNext109", function() { return ju_ServiceLoader$1279_hasNext109(this); },
    "next110", function() { return ju_ServiceLoader$1279_next110(this); });
function otcic_CharBuffer287() {
    this.data288 = null;
    this.end289 = 0;
    this.pos290 = 0;
}
$rt_declClass(otcic_CharBuffer287, {
    name : "org.teavm.classlib.impl.charset.CharBuffer",
    superclass : jl_Object7,
    clinit : function() { otcic_CharBuffer287_$clinit(); } });
function otcic_CharBuffer287_$clinit() {
    otcic_CharBuffer287_$clinit = function(){};
    otcic_CharBuffer287_$init291 = function($this, a, b, c) {
        jl_Object7_$init10($this);
        $this.data288 = a;
        $this.end289 = c;
        $this.pos290 = b;
        return;
    }
}
$rt_methodStubs(otcic_CharBuffer287_$clinit, ['otcic_CharBuffer287_$init291']);
function otcic_CharBuffer287_get292($this) {
    var a, b;
    a = $this.data288;
    b = $this.pos290;
    $this.pos290 = ((b + 1) | 0);
    return a.data[b];
}
function otcic_CharBuffer287_end293($this) {
    var a;
    if (($this.pos290 != $this.end289)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function otcic_CharBuffer287_back294($this, a) {
    $this.pos290 = (($this.pos290 - a) | 0);
    return;
}
otcic_CharBuffer287.$init291 = function(a, b, c) {
    var result = new otcic_CharBuffer287();
    result.$init291(a, b, c);
    return result;
}
$rt_virtualMethods(otcic_CharBuffer287,
    "get292", function() { return otcic_CharBuffer287_get292(this); },
    "end293", function() { return otcic_CharBuffer287_end293(this); },
    "$init291", function(a, b, c) { otcic_CharBuffer287_$init291(this, a, b, c); },
    "back294", function(a) { otcic_CharBuffer287_back294(this, a); });
function jlr_Array150() {
}
$rt_declClass(jlr_Array150, {
    name : "java.lang.reflect.Array",
    superclass : jl_Object7,
    clinit : function() { jlr_Array150_$clinit(); } });
function jlr_Array150_$clinit() {
    jlr_Array150_$clinit = function(){};
    jlr_Array150_newInstanceImpl295 = function(a, b) {
        var cls = a.$data;
        if (cls.primitive) {
            if (cls == $rt_bytecls()) {
                return $rt_createByteArray(b);
            }
            if (cls == $rt_shortcls()) {
                return $rt_createShortArray(b);
            }
            if (cls == $rt_charcls()) {
                return $rt_createCharArray(b);
            }
            if (cls == $rt_intcls()) {
                return $rt_createIntArray(b);
            }
            if (cls == $rt_longcls()) {
                return $rt_createLongArray(b);
            }
            if (cls == $rt_floatcls()) {
                return $rt_createFloatArray(b);
            }
            if (cls == $rt_doublecls()) {
                return $rt_createDoubleArray(b);
            }
            if (cls == $rt_booleancls()) {
                return $rt_createBooleanArray(b);
            }
        } else {
            return $rt_createArray(cls, b)
        }
    }
    jlr_Array150_getImpl296 = function(a, b) {
        var item = a.data[b];
        var type = a.constructor.$meta.item;
        if (type === $rt_intcls()) {
            item = jl_Integer81_valueOf82(item);
        } else if (type === $rt_charcls()) {
            item = jl_Character187_valueOf253(item);
        }
        return item;
    }
    jlr_Array150_getLength297 = function(a) {
        if (a === null || a.constructor.$meta.item === undefined) {
            $rt_throw(jl_IllegalArgumentException134.$init135());
        }
        return a.data.length;
    }
    jlr_Array150_newInstance151 = function(a, b) {
        if ((a !== null)) {
            jl_Void298_$clinit();
            if ((a !== jl_Void298.TYPE299)) {
                if ((b >= 0)) {
                    return jlr_Array150_newInstanceImpl295(a, b);
                }
                $rt_throw(jl_NegativeArraySizeException300.$init301());
            }
            $rt_throw(jl_IllegalArgumentException134.$init135());
        }
        $rt_throw(jl_NullPointerException8.$init9());
    }
    jlr_Array150_$init302 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    jlr_Array150_get303 = function(a, b) {
        if (((b >= 0) && (b < jlr_Array150_getLength297(a)))) {
            return jlr_Array150_getImpl296(a, b);
        }
        $rt_throw(jl_ArrayIndexOutOfBoundsException160.$init161());
    }
}
$rt_methodStubs(jlr_Array150_$clinit, ['jlr_Array150_newInstanceImpl295', 'jlr_Array150_getImpl296', 'jlr_Array150_getLength297', 'jlr_Array150_newInstance151', 'jlr_Array150_$init302', 'jlr_Array150_get303']);
jlr_Array150.$init302 = function() {
    var result = new jlr_Array150();
    result.$init302();
    return result;
}
$rt_virtualMethods(jlr_Array150,
    "$init302", function() { jlr_Array150_$init302(this); });
function ju_Map304() {
}
$rt_declClass(ju_Map304, {
    name : "java.util.Map",
    superclass : jl_Object7 });
function ju_AbstractMap305() {
}
$rt_declClass(ju_AbstractMap305, {
    name : "java.util.AbstractMap",
    interfaces : [ju_Map304],
    superclass : jl_Object7,
    clinit : function() { ju_AbstractMap305_$clinit(); } });
function ju_AbstractMap305_$clinit() {
    ju_AbstractMap305_$clinit = function(){};
    ju_AbstractMap305_$init306 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ju_AbstractMap305_$clinit, ['ju_AbstractMap305_$init306']);
ju_AbstractMap305.$init306 = function() {
    var result = new ju_AbstractMap305();
    result.$init306();
    return result;
}
$rt_virtualMethods(ju_AbstractMap305,
    "$init306", function() { ju_AbstractMap305_$init306(this); });
function oahjs_Proto$Type36() {
    this.functions307 = null;
    this.propertyReadOnly308 = null;
    this.propertyNames309 = null;
    this.clazz310 = null;
}
oahjs_Proto$Type36.$assertionsDisabled311 = false;
$rt_declClass(oahjs_Proto$Type36, {
    name : "org.apidesign.html.json.spi.Proto$Type",
    superclass : jl_Object7,
    clinit : function() { oahjs_Proto$Type36_$clinit(); } });
function oahjs_Proto$Type36_$clinit() {
    oahjs_Proto$Type36_$clinit = function(){};
    oahjs_Proto$Type36_access$300312 = function(a) {
        return a.propertyReadOnly308;
    }
    oahjs_Proto$Type36_$init313 = function($this, a, b, c, d) {
        jl_Object7_$init10($this);
        if ((!((oahjs_Proto$Type36.$assertionsDisabled311 == 0) && (jl_String3_endsWith194($rt_nullCheck(jl_Class0_getName20($rt_nullCheck($rt_cls($rt_nullCheck($this).constructor)))), $rt_str("$Html4JavaType")) == 0)))) {
            block3: {
                block4: {
                    block5: {
                        block6: {
                            try {
                                if ((oahjs_Proto$Type36.$assertionsDisabled311 != 0)) {
                                    break block6;
                                }
                            } catch ($e) {
                                $je = $e.$javaException;
                                if ($je && $je instanceof jl_SecurityException314) {
                                    a = $je;
                                    break block4;
                                } else {
                                    throw $e;
                                }
                            }
                            try {
                                if ((jl_Class0_getDeclaringClass315($rt_nullCheck($rt_cls($rt_nullCheck($this).constructor))) !== a)) {
                                    break block5;
                                }
                            } catch ($e) {
                                $je = $e.$javaException;
                                if ($je && $je instanceof jl_SecurityException314) {
                                    a = $je;
                                    break block4;
                                } else {
                                    throw $e;
                                }
                            }
                        }
                        break block3;
                    }
                    try {
                        $rt_throw(jl_AssertionError316.$init317());
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof jl_SecurityException314) {
                            a = $je;
                        } else {
                            throw $e;
                        }
                    }
                }
            }
            $this.clazz310 = a;
            $this.propertyNames309 = $rt_createArray(jl_String3, c);
            $this.propertyReadOnly308 = $rt_createBooleanArray(c);
            $this.functions307 = $rt_createArray(jl_String3, d);
            onhji_JSON40_register318(a, $this);
            return;
        }
        $rt_throw(jl_AssertionError316.$init317());
    }
    oahjs_Proto$Type36_$clinit319 = function() {
        var a;
        if ((jl_Class0_desiredAssertionStatus320($rt_nullCheck($rt_cls(oahjs_Proto321))) != 0)) {
            a = 0;
        } else {
            a = 1;
        }
        oahjs_Proto$Type36.$assertionsDisabled311 = a;
        return;
    }
    oahjs_Proto$Type36_access$400322 = function(a) {
        return a.functions307;
    }
    oahjs_Proto$Type36_access$200323 = function(a) {
        return a.propertyNames309;
    }
    oahjs_Proto$Type36_$clinit319();
}
$rt_methodStubs(oahjs_Proto$Type36_$clinit, ['oahjs_Proto$Type36_access$300312', 'oahjs_Proto$Type36_$init313', 'oahjs_Proto$Type36_$clinit319', 'oahjs_Proto$Type36_access$400322', 'oahjs_Proto$Type36_access$200323']);
function oahjs_Proto$Type36_extractValue324($this, a, b) {
    if (($rt_isAssignable(a.$data, $rt_nullCheck($rt_cls(jl_Number325)).$data) != 0)) {
        b = oahjs_Proto$Type36_numberValue326($rt_nullCheck($this), b);
    }
    if (($rt_cls(jl_Boolean327) === a)) {
        b = oahjs_Proto$Type36_boolValue328($rt_nullCheck($this), b);
    }
    if (($rt_cls(jl_String3) === a)) {
        b = oahjs_Proto$Type36_stringValue329($rt_nullCheck($this), b);
    }
    if (($rt_cls(jl_Character187) === a)) {
        b = oahjs_Proto$Type36_charValue330($rt_nullCheck($this), b);
    }
    if (($rt_cls(jl_Integer81) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = 0;
        } else {
            b = $rt_nullCheck(b).intValue87();
        }
        b = jl_Integer81_valueOf82(b);
    }
    if (($rt_cls(jl_Long331) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = Long_ZERO;
        } else {
            b = $rt_nullCheck(b).longValue332();
        }
        b = jl_Long331_valueOf333(b);
    }
    if (($rt_cls(jl_Short334) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = 0;
        } else {
            b = $rt_nullCheck(b).shortValue335();
        }
        b = jl_Short334_valueOf336(b);
    }
    if (($rt_cls(jl_Byte337) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = 0;
        } else {
            b = $rt_nullCheck(b).byteValue338();
        }
        b = jl_Byte337_valueOf339(b);
    }
    if (($rt_cls(jl_Double340) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = NaN;
        } else {
            b = $rt_nullCheck(b).doubleValue341();
        }
        b = jl_Double340_valueOf342(b);
    }
    if (($rt_cls(jl_Float343) === a)) {
        if (((b instanceof jl_Number325) == 0)) {
            b = NaN;
        } else {
            b = $rt_nullCheck(b).floatValue344();
        }
        b = jl_Float343_valueOf345(b);
    }
    return jl_Class0_cast346($rt_nullCheck(a), b);
}
function oahjs_Proto$Type36_registerProperty347($this, a, b, c) {
    if ((!((oahjs_Proto$Type36.$assertionsDisabled311 == 0) && ($this.propertyNames309.data[b] !== null)))) {
        $this.propertyNames309.data[b] = a;
        $this.propertyReadOnly308.data[b] = c;
        return;
    }
    $rt_throw(jl_AssertionError316.$init317());
}
function oahjs_Proto$Type36_numberValue326($this, a) {
    return onhji_JSON40_numberValue348(a);
}
function oahjs_Proto$Type36_createProto349($this, a, b) {
    return oahjs_Proto321.$init350(a, $this, b);
}
function oahjs_Proto$Type36_boolValue328($this, a) {
    return onhji_JSON40_boolValue351(a);
}
function oahjs_Proto$Type36_stringValue329($this, a) {
    return onhji_JSON40_stringValue352(a);
}
function oahjs_Proto$Type36_charValue330($this, a) {
    return onhji_JSON40_charValue353(a);
}
function oahjs_Proto$Type36_registerFunction354($this, a, b) {
    if ((!((oahjs_Proto$Type36.$assertionsDisabled311 == 0) && ($this.functions307.data[b] !== null)))) {
        $this.functions307.data[b] = a;
        return;
    }
    $rt_throw(jl_AssertionError316.$init317());
}
function oahjs_Proto$Type36_isSame355($this, a, b) {
    if ((a !== b)) {
        if (((a !== null) && (b !== null))) {
            return $rt_nullCheck(a).equals13(b);
        }
        return 0;
    }
    return 1;
}
oahjs_Proto$Type36.$init313 = function(a, b, c, d) {
    var result = new oahjs_Proto$Type36();
    result.$init313(a, b, c, d);
    return result;
}
$rt_virtualMethods(oahjs_Proto$Type36,
    "extractValue324", function(a, b) { return oahjs_Proto$Type36_extractValue324(this, a, b); },
    "registerProperty347", function(a, b, c) { oahjs_Proto$Type36_registerProperty347(this, a, b, c); },
    "$init313", function(a, b, c, d) { oahjs_Proto$Type36_$init313(this, a, b, c, d); },
    "numberValue326", function(a) { return oahjs_Proto$Type36_numberValue326(this, a); },
    "createProto349", function(a, b) { return oahjs_Proto$Type36_createProto349(this, a, b); },
    "boolValue328", function(a) { return oahjs_Proto$Type36_boolValue328(this, a); },
    "stringValue329", function(a) { return oahjs_Proto$Type36_stringValue329(this, a); },
    "charValue330", function(a) { return oahjs_Proto$Type36_charValue330(this, a); },
    "registerFunction354", function(a, b) { oahjs_Proto$Type36_registerFunction354(this, a, b); },
    "isSame355", function(a, b) { return oahjs_Proto$Type36_isSame355(this, a, b); });
function oadm_Mines$Html4JavaType356() {
}
$rt_declClass(oadm_Mines$Html4JavaType356, {
    name : "org.apidesign.demo.minesweeper.Mines$Html4JavaType",
    superclass : oahjs_Proto$Type36,
    clinit : function() { oadm_Mines$Html4JavaType356_$clinit(); } });
function oadm_Mines$Html4JavaType356_$clinit() {
    oadm_Mines$Html4JavaType356_$clinit = function(){};
    oadm_Mines$Html4JavaType356_$init357 = function($this) {
        var a, b, c, d;
        oahjs_Proto$Type36_$init313($this, $rt_cls(oadm_Mines49), $rt_cls(oadm_MinesModel43), 3, 5);
        a = $rt_str("fieldShowing");
        b = 0;
        c = 1;
        d = $rt_nullCheck($this);
        oahjs_Proto$Type36_registerProperty347(d, a, b, c);
        oahjs_Proto$Type36_registerProperty347(d, $rt_str("state"), 1, 0);
        oahjs_Proto$Type36_registerProperty347(d, $rt_str("rows"), 2, 1);
        oahjs_Proto$Type36_registerFunction354(d, $rt_str("showHelp"), 0);
        oahjs_Proto$Type36_registerFunction354(d, $rt_str("smallGame"), 1);
        oahjs_Proto$Type36_registerFunction354(d, $rt_str("normalGame"), 2);
        oahjs_Proto$Type36_registerFunction354(d, $rt_str("giveUp"), 3);
        oahjs_Proto$Type36_registerFunction354(d, $rt_str("click"), 4);
        return;
    }
    oadm_Mines$Html4JavaType356_$init358 = function($this, a) {
        oadm_Mines$Html4JavaType356_$init357($this);
        return;
    }
}
$rt_methodStubs(oadm_Mines$Html4JavaType356_$clinit, ['oadm_Mines$Html4JavaType356_$init357', 'oadm_Mines$Html4JavaType356_$init358']);
function oadm_Mines$Html4JavaType356_call359($this, a, b, c, d) {
    a = a;
    oadm_Mines$Html4JavaType356_call360($rt_nullCheck($this), a, b, c, d);
    return;
}
function oadm_Mines$Html4JavaType356_setValue37($this, a, b, c) {
    a = a;
    oadm_Mines$Html4JavaType356_setValue361($rt_nullCheck($this), a, b, c);
    return;
}
function oadm_Mines$Html4JavaType356_getValue362($this, a, b) {
    block1: {
        block2: {
            block3: {
                switch (b) {
                    case 0:
                        break;
                    case 1:
                        break block3;
                    case 2:
                        break block2;
                    default:
                        break block1;
                }
                return jl_Boolean327_valueOf363(oadm_Mines49_isFieldShowing364($rt_nullCheck(a)));
            }
            return oadm_Mines49_getState99($rt_nullCheck(a));
        }
        return oadm_Mines49_getRows53($rt_nullCheck(a));
    }
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Mines$Html4JavaType356_call360($this, a, b, c, d) {
    block1: {
        block2: {
            block3: {
                block4: {
                    block5: {
                        switch (b) {
                            case 0:
                                break;
                            case 1:
                                break block5;
                            case 2:
                                break block4;
                            case 3:
                                break block3;
                            case 4:
                                break block2;
                            default:
                                break block1;
                        }
                        oadm_Mines49_access$200365(a, c, d);
                        return;
                    }
                    oadm_Mines49_access$300366(a, c, d);
                    return;
                }
                oadm_Mines49_access$400367(a, c, d);
                return;
            }
            oadm_Mines49_access$500368(a, c, d);
            return;
        }
        oadm_Mines49_access$600369(a, c, d);
        return;
    }
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Mines$Html4JavaType356_protoFor370($this, a) {
    return oadm_Mines49_access$700371(a);
}
function oadm_Mines$Html4JavaType356_onChange372($this, a, b) {
    a = a;
    oadm_Mines$Html4JavaType356_onChange373($rt_nullCheck($this), a, b);
    return;
}
function oadm_Mines$Html4JavaType356_getValue39($this, a, b) {
    return oadm_Mines$Html4JavaType356_getValue362($rt_nullCheck($this), a, b);
}
function oadm_Mines$Html4JavaType356_onChange373($this, a, b) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Mines$Html4JavaType356_setValue361($this, a, b, c) {
    block1: {
        switch (b) {
            case 1:
                break;
            default:
                break block1;
        }
        b = oahjs_Proto$Type36_extractValue324($rt_nullCheck(oadm_Mines49_access$100374()), $rt_cls(oadm_MinesModel$GameState73), c);
        oadm_Mines49_setState75($rt_nullCheck(a), b);
        return;
    }
    return;
}
oadm_Mines$Html4JavaType356.$init357 = function() {
    var result = new oadm_Mines$Html4JavaType356();
    result.$init357();
    return result;
}
oadm_Mines$Html4JavaType356.$init358 = function(a) {
    var result = new oadm_Mines$Html4JavaType356();
    result.$init358(a);
    return result;
}
$rt_virtualMethods(oadm_Mines$Html4JavaType356,
    "call359", function(a, b, c, d) { oadm_Mines$Html4JavaType356_call359(this, a, b, c, d); },
    "setValue37", function(a, b, c) { oadm_Mines$Html4JavaType356_setValue37(this, a, b, c); },
    "getValue362", function(a, b) { return oadm_Mines$Html4JavaType356_getValue362(this, a, b); },
    "call360", function(a, b, c, d) { oadm_Mines$Html4JavaType356_call360(this, a, b, c, d); },
    "protoFor370", function(a) { return oadm_Mines$Html4JavaType356_protoFor370(this, a); },
    "onChange372", function(a, b) { oadm_Mines$Html4JavaType356_onChange372(this, a, b); },
    "getValue39", function(a, b) { return oadm_Mines$Html4JavaType356_getValue39(this, a, b); },
    "$init357", function() { oadm_Mines$Html4JavaType356_$init357(this); },
    "$init358", function(a) { oadm_Mines$Html4JavaType356_$init358(this, a); },
    "onChange373", function(a, b) { oadm_Mines$Html4JavaType356_onChange373(this, a, b); },
    "setValue361", function(a, b, c) { oadm_Mines$Html4JavaType356_setValue361(this, a, b, c); });
function oadm_Mines$1375() {
    this.val$mines376 = 0;
    this.val$width377 = 0;
    this.val$height378 = 0;
    this.this$0379 = null;
}
$rt_declClass(oadm_Mines$1375, {
    name : "org.apidesign.demo.minesweeper.Mines$1",
    interfaces : [jl_Runnable232],
    superclass : jl_Object7,
    clinit : function() { oadm_Mines$1375_$clinit(); } });
function oadm_Mines$1375_$clinit() {
    oadm_Mines$1375_$clinit = function(){};
    oadm_Mines$1375_$init380 = function($this, a, b, c, d) {
        $this.this$0379 = a;
        $this.val$width377 = b;
        $this.val$height378 = c;
        $this.val$mines376 = d;
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(oadm_Mines$1375_$clinit, ['oadm_Mines$1375_$init380']);
function oadm_Mines$1375_run381($this) {
    oadm_MinesModel43_init60($this.this$0379, $this.val$width377, $this.val$height378, $this.val$mines376);
    return;
}
oadm_Mines$1375.$init380 = function(a, b, c, d) {
    var result = new oadm_Mines$1375();
    result.$init380(a, b, c, d);
    return result;
}
$rt_virtualMethods(oadm_Mines$1375,
    "run381", function() { oadm_Mines$1375_run381(this); },
    "$init380", function(a, b, c, d) { oadm_Mines$1375_$init380(this, a, b, c, d); });
function otcic_Charset382() {
}
$rt_declClass(otcic_Charset382, {
    name : "org.teavm.classlib.impl.charset.Charset",
    superclass : jl_Object7,
    clinit : function() { otcic_Charset382_$clinit(); } });
function otcic_Charset382_$clinit() {
    otcic_Charset382_$clinit = function(){};
    otcic_Charset382_$init383 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    otcic_Charset382_get384 = function(a) {
        if ((jl_String3_equals13($rt_nullCheck(a), $rt_str("UTF-8")) == 0)) {
            return null;
        }
        return otcic_UTF8Charset385.$init386();
    }
}
$rt_methodStubs(otcic_Charset382_$clinit, ['otcic_Charset382_$init383', 'otcic_Charset382_get384']);
otcic_Charset382.$init383 = function() {
    var result = new otcic_Charset382();
    result.$init383();
    return result;
}
$rt_virtualMethods(otcic_Charset382,
    "$init383", function() { otcic_Charset382_$init383(this); });
function onhji_JSON40() {
}
onhji_JSON40.modelTypes387 = null;
$rt_declClass(onhji_JSON40, {
    name : "org.netbeans.html.json.impl.JSON",
    superclass : jl_Object7,
    clinit : function() { onhji_JSON40_$clinit(); } });
function onhji_JSON40_$clinit() {
    onhji_JSON40_$clinit = function(){};
    onhji_JSON40_toModel388 = function(a, b, c, d) {
        return jl_Class0_cast346($rt_nullCheck(b), $rt_nullCheck(onhji_JSON40_findTechnology390(a)).toModel389(b, c));
    }
    onhji_JSON40_runInBrowser391 = function(a, b) {
        $rt_nullCheck(onhji_JSON40_findTechnology390(a)).runSafe392(b);
        return;
    }
    onhji_JSON40_findType393 = function(a) {
        var b, c;
        b = 0;
        block1: {
            while (true) {
                if ((b >= 2)) {
                    break block1;
                }
                c = ju_HashMap394_get395($rt_nullCheck(onhji_JSON40.modelTypes387), a);
                if ((c !== null)) {
                    break;
                }
                onhji_JSON40_initClass396(a);
                b = ((b + 1) | 0);
            }
            return c;
        }
        return null;
    }
    onhji_JSON40_register318 = function(a, b) {
        ju_HashMap394_put397($rt_nullCheck(onhji_JSON40.modelTypes387), a, b);
        return;
    }
    onhji_JSON40_findTechnology390 = function(a) {
        a = oahcs_Contexts398_find399(a, $rt_cls(oahjs_Technology156));
        if ((a === null)) {
            a = onhji_JSON$EmptyTech400_access$000401();
        }
        return a;
    }
    onhji_JSON40_isNumeric402 = function(a) {
        if ((((a instanceof jl_Integer81) == 0) && (((a instanceof jl_Long331) == 0) && (((a instanceof jl_Short334) == 0) && ((a instanceof jl_Byte337) == 0))))) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    onhji_JSON40_find41 = function(a, b) {
        if ((a !== null)) {
            if (((a instanceof onhji_JSONList76) == 0)) {
                if (($rt_isInstance(a, ju_Collection203) == 0)) {
                    a = onhji_JSON40_findProto403(a);
                    if ((a !== null)) {
                        a = onhji_PropertyBindingAccessor404_getBindings405(a, 1);
                        if ((a !== null)) {
                            a = onhji_Bindings406_koData407($rt_nullCheck(a));
                        } else {
                            a = null;
                        }
                        return a;
                    }
                    return null;
                }
                return onhji_JSONList76_koData408(a, b);
            }
            return onhji_JSONList76_koData407($rt_nullCheck(a));
        }
        return null;
    }
    onhji_JSON40_numberValue348 = function(a) {
        if (((a instanceof jl_String3) == 0)) {
            if (((a instanceof jl_Boolean327) == 0)) {
                return a;
            }
            if ((jl_Boolean327_booleanValue409($rt_nullCheck(a)) == 0)) {
                a = 0;
            } else {
                a = 1;
            }
            return jl_Integer81_valueOf82(a);
        }
        block5: {
            try {
                a = jl_Double340_valueOf410(a);
            } catch ($e) {
                $je = $e.$javaException;
                if ($je && $je instanceof jl_NumberFormatException137) {
                    a = $je;
                    break block5;
                } else {
                    throw $e;
                }
            }
            return a;
        }
        return jl_Double340_valueOf342(NaN);
    }
    onhji_JSON40_$clinit411 = function() {
        onhji_JSON40.modelTypes387 = ju_HashMap394.$init412();
        return;
    }
    onhji_JSON40_boolValue351 = function(a) {
        if (((a instanceof jl_String3) == 0)) {
            if (((a instanceof jl_Number325) == 0)) {
                jl_Boolean327_$clinit();
                return jl_Boolean327_valueOf363(jl_Boolean327_equals13($rt_nullCheck(jl_Boolean327.TRUE413), a));
            }
            if (($rt_nullCheck(onhji_JSON40_numberValue348(a)).doubleValue341() == 0.0)) {
                a = 0;
            } else {
                a = 1;
            }
            return jl_Boolean327_valueOf363(a);
        }
        return jl_Boolean327_valueOf363(jl_Boolean327_parseBoolean414(a));
    }
    onhji_JSON40_$init415 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    onhji_JSON40_stringValue352 = function(a) {
        if (((a instanceof jl_Boolean327) == 0)) {
            if ((onhji_JSON40_isNumeric402(a) == 0)) {
                if (((a instanceof jl_Float343) == 0)) {
                    if (((a instanceof jl_Double340) == 0)) {
                        return a;
                    }
                    return jl_Double340_toString416(jl_Double340_doubleValue341($rt_nullCheck(a)));
                }
                return jl_Float343_toString417(jl_Float343_floatValue344($rt_nullCheck(a)));
            }
            return jl_Long331_toString418($rt_nullCheck(a).longValue332());
        }
        if ((jl_Boolean327_booleanValue409($rt_nullCheck(a)) == 0)) {
            a = $rt_str("false");
        } else {
            a = $rt_str("true");
        }
        return a;
    }
    onhji_JSON40_charValue353 = function(a) {
        var b;
        if (((a instanceof jl_Number325) == 0)) {
            if (((a instanceof jl_Boolean327) == 0)) {
                if (((a instanceof jl_String3) == 0)) {
                    return a;
                }
                b = $rt_nullCheck(a);
                if ((jl_String3_isEmpty184(b) == 0)) {
                    a = jl_String3_charAt176(b, 0);
                } else {
                    a = 0;
                }
                return jl_Character187_valueOf253(a);
            }
            if ((jl_Boolean327_booleanValue409($rt_nullCheck(a)) == 0)) {
                a = 0;
            } else {
                a = 1;
            }
            return jl_Character187_valueOf253(a);
        }
        return jl_Character187_valueOf253(jl_Character187_toChars249($rt_nullCheck(onhji_JSON40_numberValue348(a)).intValue87()).data[0]);
    }
    onhji_JSON40_findProto403 = function(a) {
        var b;
        b = onhji_JSON40_findType393($rt_cls($rt_nullCheck(a).constructor));
        if ((b !== null)) {
            return onhji_PropertyBindingAccessor404_protoFor419(b, a);
        }
        return null;
    }
    onhji_JSON40_initClass396 = function(a) {
        var b;
        block1: {
            block2: {
                block3: {
                    block4: {
                        try {
                            b = $rt_nullCheck(a);
                            a = jl_Class0_getClassLoader420(b);
                        } catch ($e) {
                            $je = $e.$javaException;
                            if ($je && $je instanceof jl_Exception127) {
                                a = $je;
                                break block2;
                            } else if ($je && $je instanceof jl_SecurityException314) {
                                a = $je;
                                break block4;
                            } else {
                                throw $e;
                            }
                        }
                        try {
                            break block3;
                        } catch ($e) {
                            $je = $e.$javaException;
                            if ($je && $je instanceof jl_Exception127) {
                                a = $je;
                                break block2;
                            } else {
                                throw $e;
                            }
                        }
                    }
                    try {
                        a = null;
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof jl_Exception127) {
                            a = $je;
                            break block2;
                        } else {
                            throw $e;
                        }
                    }
                }
                block5: {
                    try {
                        if ((a === null)) {
                            break block5;
                        }
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof jl_Exception127) {
                            a = $je;
                            break block2;
                        } else {
                            throw $e;
                        }
                    }
                    try {
                        jl_Class0_forName421(jl_Class0_getName20(b), 1, a);
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof jl_Exception127) {
                            a = $je;
                            break block2;
                        } else {
                            throw $e;
                        }
                    }
                }
                try {
                    jl_Class0_newInstance422(b);
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_Exception127) {
                        a = $je;
                        break block2;
                    } else {
                        throw $e;
                    }
                }
                break block1;
            }
        }
        return;
    }
    onhji_JSON40_$clinit411();
}
$rt_methodStubs(onhji_JSON40_$clinit, ['onhji_JSON40_toModel388', 'onhji_JSON40_runInBrowser391', 'onhji_JSON40_findType393', 'onhji_JSON40_register318', 'onhji_JSON40_findTechnology390', 'onhji_JSON40_isNumeric402', 'onhji_JSON40_find41', 'onhji_JSON40_numberValue348', 'onhji_JSON40_$clinit411', 'onhji_JSON40_boolValue351', 'onhji_JSON40_$init415', 'onhji_JSON40_stringValue352', 'onhji_JSON40_charValue353', 'onhji_JSON40_findProto403', 'onhji_JSON40_initClass396']);
onhji_JSON40.$init415 = function() {
    var result = new onhji_JSON40();
    result.$init415();
    return result;
}
$rt_virtualMethods(onhji_JSON40,
    "$init415", function() { onhji_JSON40_$init415(this); });
function jl_NegativeArraySizeException300() {
}
$rt_declClass(jl_NegativeArraySizeException300, {
    name : "java.lang.NegativeArraySizeException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_NegativeArraySizeException300_$clinit(); } });
function jl_NegativeArraySizeException300_$clinit() {
    jl_NegativeArraySizeException300_$clinit = function(){};
    jl_NegativeArraySizeException300_$init301 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(jl_NegativeArraySizeException300_$clinit, ['jl_NegativeArraySizeException300_$init301']);
jl_NegativeArraySizeException300.$init301 = function() {
    var result = new jl_NegativeArraySizeException300();
    result.$init301();
    return result;
}
$rt_virtualMethods(jl_NegativeArraySizeException300,
    "$init301", function() { jl_NegativeArraySizeException300_$init301(this); });
function jl_Number325() {
}
$rt_declClass(jl_Number325, {
    name : "java.lang.Number",
    interfaces : [ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jl_Number325_$clinit(); } });
function jl_Number325_$clinit() {
    jl_Number325_$clinit = function(){};
    jl_Number325_$init423 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(jl_Number325_$clinit, ['jl_Number325_$init423']);
function jl_Number325_shortValue335($this) {
    return ($rt_nullCheck($this).intValue87() & 65535);
}
function jl_Number325_byteValue338($this) {
    return ($rt_nullCheck($this).intValue87() & 255);
}
jl_Number325.$init423 = function() {
    var result = new jl_Number325();
    result.$init423();
    return result;
}
$rt_virtualMethods(jl_Number325,
    "$init423", function() { jl_Number325_$init423(this); },
    "shortValue335", function() { return jl_Number325_shortValue335(this); },
    "byteValue338", function() { return jl_Number325_byteValue338(this); });
function jl_Float343() {
    this.value424 = 0.0;
}
jl_Float343.NaN425 = 0.0;
jl_Float343.TYPE426 = null;
$rt_declClass(jl_Float343, {
    name : "java.lang.Float",
    interfaces : [jl_Comparable163],
    superclass : jl_Number325,
    clinit : function() { jl_Float343_$clinit(); } });
function jl_Float343_$clinit() {
    jl_Float343_$clinit = function(){};
    jl_Float343_isNaN427 = function(a) {
        return (isNaN(a) ? 1 : 0 );
    }
    jl_Float343_toString417 = function(a) {
        return jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append428($rt_nullCheck(jl_StringBuilder16.$init17()), a)));
    }
    jl_Float343_valueOf345 = function(a) {
        return jl_Float343.$init429(a);
    }
    jl_Float343_isInfinite430 = function(a) {
        return (isFinite(a) ? 0 : 1);
    }
    jl_Float343_$init429 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value424 = a;
        return;
    }
    jl_Float343_compare431 = function(a, b) {
        if ((a <= b)) {
            if ((b >= a)) {
                a = 0;
            } else {
                a = -1;
            }
        } else {
            a = 1;
        }
        return a;
    }
    jl_Float343_getNaN432 = function() {
    }
    jl_Float343_$clinit433 = function() {
        jl_Float343.NaN425 = jl_Float343_getNaN432();
        jl_Float343.TYPE426 = $rt_cls($rt_floatcls());
        return;
    }
    jl_Float343_$clinit433();
}
$rt_methodStubs(jl_Float343_$clinit, ['jl_Float343_isNaN427', 'jl_Float343_toString417', 'jl_Float343_valueOf345', 'jl_Float343_isInfinite430', 'jl_Float343_$init429', 'jl_Float343_compare431', 'jl_Float343_getNaN432', 'jl_Float343_$clinit433']);
function jl_Float343_intValue87($this) {
    return ($this.value424 | 0);
}
function jl_Float343_floatValue344($this) {
    return $this.value424;
}
function jl_Float343_compareTo434($this, a) {
    return jl_Float343_compare431($this.value424, a.value424);
}
function jl_Float343_doubleValue341($this) {
    return $this.value424;
}
function jl_Float343_longValue332($this) {
    return Long_fromNumber($this.value424);
}
function jl_Float343_compareTo181($this, a) {
    return jl_Float343_compareTo434($rt_nullCheck($this), a);
}
jl_Float343.$init429 = function(a) {
    var result = new jl_Float343();
    result.$init429(a);
    return result;
}
$rt_virtualMethods(jl_Float343,
    "intValue87", function() { return jl_Float343_intValue87(this); },
    "floatValue344", function() { return jl_Float343_floatValue344(this); },
    "$init429", function(a) { jl_Float343_$init429(this, a); },
    "compareTo434", function(a) { return jl_Float343_compareTo434(this, a); },
    "doubleValue341", function() { return jl_Float343_doubleValue341(this); },
    "longValue332", function() { return jl_Float343_longValue332(this); },
    "compareTo181", function(a) { return jl_Float343_compareTo181(this, a); });
function jl_SecurityException314() {
}
$rt_declClass(jl_SecurityException314, {
    name : "java.lang.SecurityException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_SecurityException314_$clinit(); } });
function jl_SecurityException314_$clinit() {
    jl_SecurityException314_$clinit = function(){};
    jl_SecurityException314_$init435 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(jl_SecurityException314_$clinit, ['jl_SecurityException314_$init435']);
jl_SecurityException314.$init435 = function() {
    var result = new jl_SecurityException314();
    result.$init435();
    return result;
}
$rt_virtualMethods(jl_SecurityException314,
    "$init435", function() { jl_SecurityException314_$init435(this); });
function oahjs_Proto321() {
    this.ko436 = null;
    this.context437 = null;
    this.obj438 = null;
    this.locked439 = false;
    this.type440 = null;
}
$rt_declClass(oahjs_Proto321, {
    name : "org.apidesign.html.json.spi.Proto",
    superclass : jl_Object7,
    clinit : function() { oahjs_Proto321_$clinit(); } });
function oahjs_Proto321_$clinit() {
    oahjs_Proto321_$clinit = function(){};
    oahjs_Proto321_$init350 = function($this, a, b, c) {
        jl_Object7_$init10($this);
        $this.obj438 = a;
        $this.type440 = b;
        $this.context437 = c;
        return;
    }
}
$rt_methodStubs(oahjs_Proto321_$clinit, ['oahjs_Proto321_$init350']);
function oahjs_Proto321_getBindings441($this) {
    return $this.ko436;
}
function oahjs_Proto321_createList442($this, a, b, c) {
    return onhji_JSONList76.$init443($this, a, b, c);
}
function oahjs_Proto321_runInBrowser444($this, a) {
    onhji_JSON40_runInBrowser391($this.context437, a);
    return;
}
function oahjs_Proto321_initBindings445($this) {
    var a, b, c, d, e;
    if (($this.ko436 === null)) {
        a = onhji_Bindings406_apply446($this.context437, $this.obj438);
        b = $rt_createArray(oahjs_PropertyBinding21, oahjs_Proto$Type36_access$200323($this.type440).data.length);
        c = 0;
        while (true) {
            d = b.data;
            if ((c >= d.length)) {
                break;
            }
            d[c] = onhji_Bindings406_registerProperty447($rt_nullCheck(a), oahjs_Proto$Type36_access$200323($this.type440).data[c], c, $this.obj438, $this.type440, $rt_byteToInt(oahjs_Proto$Type36_access$300312($this.type440).data[c]));
            c = ((c + 1) | 0);
        }
        c = $rt_createArray(oahjs_FunctionBinding448, oahjs_Proto$Type36_access$400322($this.type440).data.length);
        d = 0;
        while (true) {
            e = c.data;
            if ((d >= e.length)) {
                break;
            }
            e[d] = oahjs_FunctionBinding448_registerFunction449(oahjs_Proto$Type36_access$400322($this.type440).data[d], d, $this.obj438, $this.type440);
            d = ((d + 1) | 0);
        }
        $this.ko436 = a;
        onhji_Bindings406_finish450($rt_nullCheck(a), $this.obj438, b, c);
    }
    return $this.ko436;
}
function oahjs_Proto321_acquireLock451($this) {
    if (($this.locked439 == 0)) {
        $this.locked439 = 1;
        return;
    }
    $rt_throw(jl_IllegalStateException229.$init452());
}
function oahjs_Proto321_verifyUnlocked453($this) {
    if (($this.locked439 == 0)) {
        return;
    }
    $rt_throw(jl_IllegalStateException229.$init452());
}
function oahjs_Proto321_releaseLock454($this) {
    $this.locked439 = 0;
    return;
}
function oahjs_Proto321_onChange455($this, a) {
    var b, c;
    b = $this.type440;
    c = $this.obj438;
    $rt_nullCheck(b).onChange372(c, a);
    return;
}
function oahjs_Proto321_valueHasMutated456($this, a) {
    if (($this.ko436 !== null)) {
        onhji_Bindings406_valueHasMutated456($rt_nullCheck($this.ko436), a);
    }
    return;
}
function oahjs_Proto321_initTo457($this, a, b) {
    if (($this.ko436 === null)) {
        if (((a instanceof onhji_JSONList76) == 0)) {
            onhji_JSONList76_init458(a, b);
        } else {
            onhji_JSONList76_init459($rt_nullCheck(a), b);
        }
        return;
    }
    $rt_throw(jl_IllegalStateException229.$init452());
}
function oahjs_Proto321_applyBindings460($this) {
    onhji_Bindings406_applyBindings460($rt_nullCheck(oahjs_Proto321_initBindings445($rt_nullCheck($this))));
    return;
}
function oahjs_Proto321_toModel389($this, a, b) {
    return onhji_JSON40_toModel388($this.context437, a, b, null);
}
oahjs_Proto321.$init350 = function(a, b, c) {
    var result = new oahjs_Proto321();
    result.$init350(a, b, c);
    return result;
}
$rt_virtualMethods(oahjs_Proto321,
    "getBindings441", function() { return oahjs_Proto321_getBindings441(this); },
    "createList442", function(a, b, c) { return oahjs_Proto321_createList442(this, a, b, c); },
    "$init350", function(a, b, c) { oahjs_Proto321_$init350(this, a, b, c); },
    "runInBrowser444", function(a) { oahjs_Proto321_runInBrowser444(this, a); },
    "initBindings445", function() { return oahjs_Proto321_initBindings445(this); },
    "acquireLock451", function() { oahjs_Proto321_acquireLock451(this); },
    "verifyUnlocked453", function() { oahjs_Proto321_verifyUnlocked453(this); },
    "releaseLock454", function() { oahjs_Proto321_releaseLock454(this); },
    "onChange455", function(a) { oahjs_Proto321_onChange455(this, a); },
    "valueHasMutated456", function(a) { oahjs_Proto321_valueHasMutated456(this, a); },
    "initTo457", function(a, b) { oahjs_Proto321_initTo457(this, a, b); },
    "applyBindings460", function() { oahjs_Proto321_applyBindings460(this); },
    "toModel389", function(a, b) { return oahjs_Proto321_toModel389(this, a, b); });
function jl_Math147() {
}
jl_Math147.E461 = 0.0;
jl_Math147.PI462 = 0.0;
$rt_declClass(jl_Math147, {
    name : "java.lang.Math",
    superclass : jl_Object7,
    clinit : function() { jl_Math147_$clinit(); } });
function jl_Math147_$clinit() {
    jl_Math147_$clinit = function(){};
    jl_Math147_max463 = function(a, b) {
        if ((a > b)) {
            b = a;
        }
        return b;
    }
    jl_Math147_$clinit464 = function() {
        jl_Math147.E461 = 2.718281828459045;
        jl_Math147.PI462 = 3.141592653589793;
        return;
    }
    jl_Math147_min148 = function(a, b) {
        if ((a < b)) {
            b = a;
        }
        return b;
    }
    jl_Math147_$init465 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    jl_Math147_$clinit464();
}
$rt_methodStubs(jl_Math147_$clinit, ['jl_Math147_max463', 'jl_Math147_$clinit464', 'jl_Math147_min148', 'jl_Math147_$init465']);
jl_Math147.$init465 = function() {
    var result = new jl_Math147();
    result.$init465();
    return result;
}
$rt_virtualMethods(jl_Math147,
    "$init465", function() { jl_Math147_$init465(this); });
function oahbs_Fn466() {
    this.presenter467 = null;
}
oahbs_Fn466.LOADED468 = null;
$rt_declClass(oahbs_Fn466, {
    name : "org.apidesign.html.boot.spi.Fn",
    superclass : jl_Object7,
    clinit : function() { oahbs_Fn466_$clinit(); } });
function oahbs_Fn466_$clinit() {
    oahbs_Fn466_$clinit = function(){};
    oahbs_Fn466_$clinit469 = function() {
        oahbs_Fn466.LOADED468 = ju_HashMap394.$init412();
        return;
    }
    oahbs_Fn466_$init470 = function($this, a) {
        jl_Object7_$init10($this);
        $this.presenter467 = a;
        return;
    }
    oahbs_Fn466_$init471 = function($this) {
        oahbs_Fn466_$init470($this, null);
        return;
    }
    oahbs_Fn466_activate472 = function(a) {
        return onhbi_FnContext473_activate474(a);
    }
    oahbs_Fn466_activePresenter475 = function() {
        return onhbi_FnContext473_currentPresenter476(0);
    }
    oahbs_Fn466_$clinit469();
}
$rt_methodStubs(oahbs_Fn466_$clinit, ['oahbs_Fn466_$clinit469', 'oahbs_Fn466_$init470', 'oahbs_Fn466_$init471', 'oahbs_Fn466_activate472', 'oahbs_Fn466_activePresenter475']);
oahbs_Fn466.$init470 = function(a) {
    var result = new oahbs_Fn466();
    result.$init470(a);
    return result;
}
oahbs_Fn466.$init471 = function() {
    var result = new oahbs_Fn466();
    result.$init471();
    return result;
}
$rt_virtualMethods(oahbs_Fn466,
    "$init470", function(a) { oahbs_Fn466_$init470(this, a); },
    "$init471", function() { oahbs_Fn466_$init471(this); });
function ji_Flushable477() {
}
$rt_declClass(ji_Flushable477, {
    name : "java.io.Flushable",
    superclass : jl_Object7 });
function jl_NullPointerException8() {
}
$rt_declClass(jl_NullPointerException8, {
    name : "java.lang.NullPointerException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_NullPointerException8_$clinit(); } });
function jl_NullPointerException8_$clinit() {
    jl_NullPointerException8_$clinit = function(){};
    jl_NullPointerException8_$init9 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(jl_NullPointerException8_$clinit, ['jl_NullPointerException8_$init9']);
jl_NullPointerException8.$init9 = function() {
    var result = new jl_NullPointerException8();
    result.$init9();
    return result;
}
$rt_virtualMethods(jl_NullPointerException8,
    "$init9", function() { jl_NullPointerException8_$init9(this); });
function oadm_Mines49() {
    this.prop_rows478 = null;
    this.prop_state479 = null;
    this.proto480 = null;
}
oadm_Mines49.TYPE481 = null;
$rt_declClass(oadm_Mines49, {
    name : "org.apidesign.demo.minesweeper.Mines",
    interfaces : [jl_Cloneable262],
    superclass : jl_Object7,
    clinit : function() { oadm_Mines49_$clinit(); } });
function oadm_Mines49_$clinit() {
    oadm_Mines49_$clinit = function(){};
    oadm_Mines49_access$200365 = function(a, b, c) {
        oadm_Mines49_showHelp482(a, b, c);
        return;
    }
    oadm_Mines49_access$500368 = function(a, b, c) {
        oadm_Mines49_giveUp483(a, b, c);
        return;
    }
    oadm_Mines49_access$300366 = function(a, b, c) {
        oadm_Mines49_smallGame484(a, b, c);
        return;
    }
    oadm_Mines49_$clinit485 = function() {
        oadm_Mines49.TYPE481 = oadm_Mines$Html4JavaType356.$init358(null);
        return;
    }
    oadm_Mines49_$init486 = function($this, a) {
        jl_Object7_$init10($this);
        $this.proto480 = oahjs_Proto$Type36_createProto349($rt_nullCheck(oadm_Mines49.TYPE481), $this, a);
        $this.prop_rows478 = oahjs_Proto321_createList442($rt_nullCheck($this.proto480), $rt_str("rows"), -1, $rt_createArray(jl_String3, 0));
        return;
    }
    oadm_Mines49_access$100374 = function() {
        return oadm_Mines49.TYPE481;
    }
    oadm_Mines49_access$700371 = function(a) {
        return a.proto480;
    }
    oadm_Mines49_access$600369 = function(a, b, c) {
        oadm_Mines49_click487(a, b, c);
        return;
    }
    oadm_Mines49_$init93 = function($this) {
        oadm_Mines49_$init486($this, njh_BrwsrCtx488_findDefault489($rt_cls(oadm_Mines49)));
        return;
    }
    oadm_Mines49_access$400367 = function(a, b, c) {
        oadm_Mines49_normalGame490(a, b, c);
        return;
    }
    oadm_Mines49_$clinit485();
}
$rt_methodStubs(oadm_Mines49_$clinit, ['oadm_Mines49_access$200365', 'oadm_Mines49_access$500368', 'oadm_Mines49_access$300366', 'oadm_Mines49_$clinit485', 'oadm_Mines49_$init486', 'oadm_Mines49_access$100374', 'oadm_Mines49_access$700371', 'oadm_Mines49_access$600369', 'oadm_Mines49_$init93', 'oadm_Mines49_access$400367']);
function oadm_Mines49_showHelp482($this, a, b) {
    oadm_MinesModel43_showHelp105($this);
    return;
}
function oadm_Mines49_giveUp483($this, a, b) {
    oadm_MinesModel43_giveUp44($this);
    return;
}
function oadm_Mines49_getState99($this) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto480));
    return $this.prop_state479;
}
function oadm_Mines49_normalGame490($this, a, b) {
    oadm_MinesModel43_normalGame51($this);
    return;
}
function oadm_Mines49_click487($this, a, b) {
    oadm_MinesModel43_click98($this, oahjs_Proto321_toModel389($rt_nullCheck($this.proto480), $rt_cls(oadm_Square62), a));
    return;
}
function oadm_Mines49_setState75($this, a) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto480));
    if ((oahjs_Proto$Type36_isSame355($rt_nullCheck(oadm_Mines49.TYPE481), $this.prop_state479, a) == 0)) {
        $this.prop_state479 = a;
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto480), $rt_str("state"));
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto480), $rt_str("fieldShowing"));
        return;
    }
    return;
}
function oadm_Mines49_applyBindings94($this) {
    oahjs_Proto321_applyBindings460($rt_nullCheck($this.proto480));
    return $this;
}
function oadm_Mines49_computeMines106($this) {
    var a, b;
    a = $this.proto480;
    b = oadm_Mines$2491.$init492($this);
    oahjs_Proto321_runInBrowser444($rt_nullCheck(a), b);
    return;
}
function oadm_Mines49_init50($this, a, b, c) {
    var d, e;
    d = $this.proto480;
    e = oadm_Mines$1375.$init380($this, a, b, c);
    oahjs_Proto321_runInBrowser444($rt_nullCheck(d), e);
    return;
}
function oadm_Mines49_smallGame484($this, a, b) {
    oadm_MinesModel43_smallGame48($this);
    return;
}
function oadm_Mines49_isFieldShowing364($this) {
    var a, b;
    a = oadm_Mines49_getState99($rt_nullCheck($this));
    block1: {
        try {
            oahjs_Proto321_acquireLock451($rt_nullCheck($this.proto480));
            a = oadm_MinesModel43_fieldShowing91(a);
        } catch ($e) {
            $je = $e.$javaException;
            if ($je) {
                b = $je;
                break block1;
            } else {
                throw $e;
            }
        }
        oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto480));
        return a;
    }
    oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto480));
    $rt_throw(b);
}
function oadm_Mines49_getRows53($this) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto480));
    return $this.prop_rows478;
}
oadm_Mines49.$init486 = function(a) {
    var result = new oadm_Mines49();
    result.$init486(a);
    return result;
}
oadm_Mines49.$init93 = function() {
    var result = new oadm_Mines49();
    result.$init93();
    return result;
}
$rt_virtualMethods(oadm_Mines49,
    "showHelp482", function(a, b) { oadm_Mines49_showHelp482(this, a, b); },
    "giveUp483", function(a, b) { oadm_Mines49_giveUp483(this, a, b); },
    "getState99", function() { return oadm_Mines49_getState99(this); },
    "normalGame490", function(a, b) { oadm_Mines49_normalGame490(this, a, b); },
    "click487", function(a, b) { oadm_Mines49_click487(this, a, b); },
    "setState75", function(a) { oadm_Mines49_setState75(this, a); },
    "applyBindings94", function() { return oadm_Mines49_applyBindings94(this); },
    "computeMines106", function() { oadm_Mines49_computeMines106(this); },
    "$init486", function(a) { oadm_Mines49_$init486(this, a); },
    "init50", function(a, b, c) { oadm_Mines49_init50(this, a, b, c); },
    "$init93", function() { oadm_Mines49_$init93(this); },
    "smallGame484", function(a, b) { oadm_Mines49_smallGame484(this, a, b); },
    "isFieldShowing364", function() { return oadm_Mines49_isFieldShowing364(this); },
    "getRows53", function() { return oadm_Mines49_getRows53(this); });
function jl_ReflectiveOperationException493() {
}
$rt_declClass(jl_ReflectiveOperationException493, {
    name : "java.lang.ReflectiveOperationException",
    superclass : jl_Exception127,
    clinit : function() { jl_ReflectiveOperationException493_$clinit(); } });
function jl_ReflectiveOperationException493_$clinit() {
    jl_ReflectiveOperationException493_$clinit = function(){};
    jl_ReflectiveOperationException493_$init494 = function($this) {
        jl_Exception127_$init128($this);
        return;
    }
}
$rt_methodStubs(jl_ReflectiveOperationException493_$clinit, ['jl_ReflectiveOperationException493_$init494']);
jl_ReflectiveOperationException493.$init494 = function() {
    var result = new jl_ReflectiveOperationException493();
    result.$init494();
    return result;
}
$rt_virtualMethods(jl_ReflectiveOperationException493,
    "$init494", function() { jl_ReflectiveOperationException493_$init494(this); });
function onhji_PropertyBindingAccessor404() {
}
onhji_PropertyBindingAccessor404.DEFAULT495 = null;
$rt_declClass(onhji_PropertyBindingAccessor404, {
    name : "org.netbeans.html.json.impl.PropertyBindingAccessor",
    superclass : jl_Object7,
    clinit : function() { onhji_PropertyBindingAccessor404_$clinit(); } });
function onhji_PropertyBindingAccessor404_$clinit() {
    onhji_PropertyBindingAccessor404_$clinit = function(){};
    onhji_PropertyBindingAccessor404_$clinit496 = function() {
        onhji_JSON40_initClass396($rt_cls(oahjs_PropertyBinding21));
        return;
    }
    onhji_PropertyBindingAccessor404_create497 = function(a, b, c, d, e, f) {
        return oahjs_PropertyBinding$123_newBinding498($rt_nullCheck(onhji_PropertyBindingAccessor404.DEFAULT495), a, b, c, d, e, f);
    }
    onhji_PropertyBindingAccessor404_$init499 = function($this) {
        jl_Object7_$init10($this);
        if ((onhji_PropertyBindingAccessor404.DEFAULT495 === null)) {
            onhji_PropertyBindingAccessor404.DEFAULT495 = $this;
            return;
        }
        $rt_throw(jl_IllegalStateException229.$init452());
    }
    onhji_PropertyBindingAccessor404_protoFor419 = function(a, b) {
        return oahjs_PropertyBinding$123_findProto500($rt_nullCheck(onhji_PropertyBindingAccessor404.DEFAULT495), a, b);
    }
    onhji_PropertyBindingAccessor404_getBindings405 = function(a, b) {
        return oahjs_PropertyBinding$123_bindings501($rt_nullCheck(onhji_PropertyBindingAccessor404.DEFAULT495), a, b);
    }
    onhji_PropertyBindingAccessor404_notifyProtoChange502 = function(a, b) {
        oahjs_PropertyBinding$123_notifyChange503($rt_nullCheck(onhji_PropertyBindingAccessor404.DEFAULT495), a, b);
        return;
    }
    onhji_PropertyBindingAccessor404_$clinit496();
}
$rt_methodStubs(onhji_PropertyBindingAccessor404_$clinit, ['onhji_PropertyBindingAccessor404_$clinit496', 'onhji_PropertyBindingAccessor404_create497', 'onhji_PropertyBindingAccessor404_$init499', 'onhji_PropertyBindingAccessor404_protoFor419', 'onhji_PropertyBindingAccessor404_getBindings405', 'onhji_PropertyBindingAccessor404_notifyProtoChange502']);
onhji_PropertyBindingAccessor404.$init499 = function() {
    var result = new onhji_PropertyBindingAccessor404();
    result.$init499();
    return result;
}
$rt_virtualMethods(onhji_PropertyBindingAccessor404,
    "$init499", function() { onhji_PropertyBindingAccessor404_$init499(this); });
function ju_ServiceLoader283() {
    this.services504 = null;
}
$rt_declClass(ju_ServiceLoader283, {
    name : "java.util.ServiceLoader",
    interfaces : [jl_Iterable218],
    superclass : jl_Object7,
    clinit : function() { ju_ServiceLoader283_$clinit(); } });
function ju_ServiceLoader283_$clinit() {
    ju_ServiceLoader283_$clinit = function(){};
    ju_ServiceLoader283_loadServices505 = function(a) {
        if (!ju_ServiceLoader283.$$services$$) {
            ju_ServiceLoader283$$services$$ = true;
            oahcs_Contexts$Provider506.$$serviceList$$ = [[onhk_KO4J507, onhk_KO4J507_$init508]];
        }
        var cls = a.$data;
        if (!cls.$$serviceList$$) {
            return $rt_createArray($rt_objcls(), 0);
        }
        var result = $rt_createArray($rt_objcls(), cls.$$serviceList$$.length);
        for (var i = 0; i < result.data.length; ++i) {
            var serviceDesc = cls.$$serviceList$$[i];
            result.data[i] = new serviceDesc[0]();
            serviceDesc[1](result.data[i]);
        }
        return result;
    }
    ju_ServiceLoader283_access$000284 = function(a) {
        return a.services504;
    }
    ju_ServiceLoader283_load509 = function(a) {
        return ju_ServiceLoader283.$init510(ju_ServiceLoader283_loadServices505(a));
    }
    ju_ServiceLoader283_load511 = function(a, b) {
        return ju_ServiceLoader283_load509(a);
    }
    ju_ServiceLoader283_$init510 = function($this, a) {
        jl_Object7_$init10($this);
        $this.services504 = a;
        return;
    }
}
$rt_methodStubs(ju_ServiceLoader283_$clinit, ['ju_ServiceLoader283_loadServices505', 'ju_ServiceLoader283_access$000284', 'ju_ServiceLoader283_load509', 'ju_ServiceLoader283_load511', 'ju_ServiceLoader283_$init510']);
function ju_ServiceLoader283_iterator107($this) {
    return ju_ServiceLoader$1279.$init282($this);
}
ju_ServiceLoader283.$init510 = function(a) {
    var result = new ju_ServiceLoader283();
    result.$init510(a);
    return result;
}
$rt_virtualMethods(ju_ServiceLoader283,
    "iterator107", function() { return ju_ServiceLoader283_iterator107(this); },
    "$init510", function(a) { ju_ServiceLoader283_$init510(this, a); });
function jl_ClassLoader512() {
    this.parent513 = null;
}
jl_ClassLoader512.systemClassLoader514 = null;
$rt_declClass(jl_ClassLoader512, {
    name : "java.lang.ClassLoader",
    superclass : jl_Object7,
    clinit : function() { jl_ClassLoader512_$clinit(); } });
function jl_ClassLoader512_$clinit() {
    jl_ClassLoader512_$clinit = function(){};
    jl_ClassLoader512_$init515 = function($this, a) {
        jl_Object7_$init10($this);
        $this.parent513 = a;
        return;
    }
    jl_ClassLoader512_$clinit516 = function() {
        jl_ClassLoader512.systemClassLoader514 = jl_SystemClassLoader517.$init518();
        return;
    }
    jl_ClassLoader512_$init519 = function($this) {
        jl_ClassLoader512_$init515($this, null);
        return;
    }
    jl_ClassLoader512_getSystemClassLoader520 = function() {
        return jl_ClassLoader512.systemClassLoader514;
    }
    jl_ClassLoader512_$clinit516();
}
$rt_methodStubs(jl_ClassLoader512_$clinit, ['jl_ClassLoader512_$init515', 'jl_ClassLoader512_$clinit516', 'jl_ClassLoader512_$init519', 'jl_ClassLoader512_getSystemClassLoader520']);
jl_ClassLoader512.$init515 = function(a) {
    var result = new jl_ClassLoader512();
    result.$init515(a);
    return result;
}
jl_ClassLoader512.$init519 = function() {
    var result = new jl_ClassLoader512();
    result.$init519();
    return result;
}
$rt_virtualMethods(jl_ClassLoader512,
    "$init515", function(a) { jl_ClassLoader512_$init515(this, a); },
    "$init519", function() { jl_ClassLoader512_$init519(this); });
function oahjs_FunctionBinding448() {
}
$rt_declClass(oahjs_FunctionBinding448, {
    name : "org.apidesign.html.json.spi.FunctionBinding",
    superclass : jl_Object7,
    clinit : function() { oahjs_FunctionBinding448_$clinit(); } });
function oahjs_FunctionBinding448_$clinit() {
    oahjs_FunctionBinding448_$clinit = function(){};
    oahjs_FunctionBinding448_$init521 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oahjs_FunctionBinding448_registerFunction449 = function(a, b, c, d) {
        return oahjs_FunctionBinding$Impl522.$init523(a, b, c, d);
    }
}
$rt_methodStubs(oahjs_FunctionBinding448_$clinit, ['oahjs_FunctionBinding448_$init521', 'oahjs_FunctionBinding448_registerFunction449']);
oahjs_FunctionBinding448.$init521 = function() {
    var result = new oahjs_FunctionBinding448();
    result.$init521();
    return result;
}
$rt_virtualMethods(oahjs_FunctionBinding448,
    "$init521", function() { oahjs_FunctionBinding448_$init521(this); });
function ji_OutputStream524() {
}
$rt_declClass(ji_OutputStream524, {
    name : "java.io.OutputStream",
    interfaces : [ji_Closeable215, ji_Flushable477],
    superclass : jl_Object7,
    clinit : function() { ji_OutputStream524_$clinit(); } });
function ji_OutputStream524_$clinit() {
    ji_OutputStream524_$clinit = function(){};
    ji_OutputStream524_$init525 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ji_OutputStream524_$clinit, ['ji_OutputStream524_$init525']);
function ji_OutputStream524_write526($this, a, b, c) {
    var d, e;
    d = 0;
    while ((d < c)) {
        e = ((b + 1) | 0);
        b = $rt_byteToInt(a.data[b]);
        $rt_nullCheck($this).write527(b);
        d = ((d + 1) | 0);
        b = e;
    }
    return;
}
ji_OutputStream524.$init525 = function() {
    var result = new ji_OutputStream524();
    result.$init525();
    return result;
}
$rt_virtualMethods(ji_OutputStream524,
    "$init525", function() { ji_OutputStream524_$init525(this); },
    "write526", function(a, b, c) { ji_OutputStream524_write526(this, a, b, c); });
function jl_ConsoleOutputStream_stdout528() {
}
$rt_declClass(jl_ConsoleOutputStream_stdout528, {
    name : "java.lang.ConsoleOutputStream_stdout",
    superclass : ji_OutputStream524,
    clinit : function() { jl_ConsoleOutputStream_stdout528_$clinit(); } });
function jl_ConsoleOutputStream_stdout528_$clinit() {
    jl_ConsoleOutputStream_stdout528_$clinit = function(){};
    jl_ConsoleOutputStream_stdout528_$init529 = function($this) {
        ji_OutputStream524_$init525($this);
        return;
    }
}
$rt_methodStubs(jl_ConsoleOutputStream_stdout528_$clinit, ['jl_ConsoleOutputStream_stdout528_$init529']);
function jl_ConsoleOutputStream_stdout528_write527($this, a) {
    $rt_putStdout(a);
}
jl_ConsoleOutputStream_stdout528.$init529 = function() {
    var result = new jl_ConsoleOutputStream_stdout528();
    result.$init529();
    return result;
}
$rt_virtualMethods(jl_ConsoleOutputStream_stdout528,
    "write527", function(a) { jl_ConsoleOutputStream_stdout528_write527(this, a); },
    "$init529", function() { jl_ConsoleOutputStream_stdout528_$init529(this); });
function ju_ArrayList54() {
    this.array530 = null;
    this.size531 = 0;
}
$rt_declClass(ju_ArrayList54, {
    name : "java.util.ArrayList",
    interfaces : [ji_Serializable164, jl_Cloneable262],
    superclass : ju_AbstractList66,
    clinit : function() { ju_ArrayList54_$clinit(); } });
function ju_ArrayList54_$clinit() {
    ju_ArrayList54_$clinit = function(){};
    ju_ArrayList54_$init61 = function($this, a) {
        ju_AbstractList66_$init221($this);
        $this.array530 = $rt_createArray(jl_Object7, a);
        return;
    }
    ju_ArrayList54_$init80 = function($this) {
        ju_ArrayList54_$init61($this, 10);
        return;
    }
}
$rt_methodStubs(ju_ArrayList54_$clinit, ['ju_ArrayList54_$init61', 'ju_ArrayList54_$init80']);
function ju_ArrayList54_checkIndexForAdd532($this, a) {
    if (((a >= 0) && (a <= $this.size531))) {
        return;
    }
    $rt_throw(jl_IndexOutOfBoundsException157.$init158());
}
function ju_ArrayList54_ensureCapacity533($this, a) {
    if (($this.array530.data.length < a)) {
        $this.array530 = ju_Arrays142_copyOf149($this.array530, (($this.array530.data.length + jl_Math147_min148(5, (($this.array530.data.length / 2) | 0))) | 0));
    }
    return;
}
function ju_ArrayList54_size55($this) {
    return $this.size531;
}
function ju_ArrayList54_clear77($this) {
    ju_Arrays142_fill153($this.array530, 0, $this.size531, null);
    $this.size531 = 0;
    return;
}
function ju_ArrayList54_set207($this, a, b) {
    var c;
    ju_ArrayList54_checkIndex534($this, a);
    c = $this.array530.data[a];
    $this.array530.data[a] = b;
    return c;
}
function ju_ArrayList54_add224($this, a, b) {
    var c, d, e, f;
    ju_ArrayList54_checkIndexForAdd532($this, a);
    c = (($this.size531 + 1) | 0);
    ju_ArrayList54_ensureCapacity533($rt_nullCheck($this), c);
    c = $this.size531;
    while ((c > a)) {
        d = $this.array530;
        e = $this.array530;
        f = ((c - 1) | 0);
        e = e.data[f];
        d.data[c] = e;
        c = ((c + -1) | 0);
    }
    $this.array530.data[a] = b;
    $this.size531 = (($this.size531 + 1) | 0);
    $this.modCount220 = (($this.modCount220 + 1) | 0);
    return;
}
function ju_ArrayList54_checkIndex534($this, a) {
    if (((a >= 0) && (a < $this.size531))) {
        return;
    }
    $rt_throw(jl_IndexOutOfBoundsException157.$init158());
}
function ju_ArrayList54_get58($this, a) {
    ju_ArrayList54_checkIndex534($this, a);
    return $this.array530.data[a];
}
ju_ArrayList54.$init61 = function(a) {
    var result = new ju_ArrayList54();
    result.$init61(a);
    return result;
}
ju_ArrayList54.$init80 = function() {
    var result = new ju_ArrayList54();
    result.$init80();
    return result;
}
$rt_virtualMethods(ju_ArrayList54,
    "$init61", function(a) { ju_ArrayList54_$init61(this, a); },
    "checkIndexForAdd532", function(a) { ju_ArrayList54_checkIndexForAdd532(this, a); },
    "ensureCapacity533", function(a) { ju_ArrayList54_ensureCapacity533(this, a); },
    "size55", function() { return ju_ArrayList54_size55(this); },
    "clear77", function() { ju_ArrayList54_clear77(this); },
    "set207", function(a, b) { return ju_ArrayList54_set207(this, a, b); },
    "add224", function(a, b) { ju_ArrayList54_add224(this, a, b); },
    "checkIndex534", function(a) { ju_ArrayList54_checkIndex534(this, a); },
    "get58", function(a) { return ju_ArrayList54_get58(this, a); },
    "$init80", function() { ju_ArrayList54_$init80(this); });
function jl_Long331() {
    this.value535 = Long_ZERO;
}
$rt_declClass(jl_Long331, {
    name : "java.lang.Long",
    superclass : jl_Number325,
    clinit : function() { jl_Long331_$clinit(); } });
function jl_Long331_$clinit() {
    jl_Long331_$clinit = function(){};
    jl_Long331_$init536 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value535 = a;
        return;
    }
    jl_Long331_valueOf333 = function(a) {
        return jl_Long331.$init536(a);
    }
}
$rt_methodStubs(jl_Long331_$clinit, ['jl_Long331_$init536', 'jl_Long331_valueOf333']);
function jl_Long331_toString418($this, a) {
    return jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append537($rt_nullCheck(jl_StringBuilder16.$init17()), a)));
}
function jl_Long331_doubleValue341($this) {
    return Long_toNumber($this.value535);
}
function jl_Long331_intValue87($this) {
    return Long_toNumber($this.value535);
}
function jl_Long331_floatValue344($this) {
    return Long_toNumber($this.value535);
}
function jl_Long331_longValue332($this) {
    return $this.value535;
}
jl_Long331.$init536 = function(a) {
    var result = new jl_Long331();
    result.$init536(a);
    return result;
}
$rt_virtualMethods(jl_Long331,
    "toString418", function(a) { return jl_Long331_toString418(this, a); },
    "doubleValue341", function() { return jl_Long331_doubleValue341(this); },
    "$init536", function(a) { jl_Long331_$init536(this, a); },
    "intValue87", function() { return jl_Long331_intValue87(this); },
    "floatValue344", function() { return jl_Long331_floatValue344(this); },
    "longValue332", function() { return jl_Long331_longValue332(this); });
function ji_IOException538() {
}
$rt_declClass(ji_IOException538, {
    name : "java.io.IOException",
    superclass : jl_Exception127,
    clinit : function() { ji_IOException538_$clinit(); } });
function ji_IOException538_$clinit() {
    ji_IOException538_$clinit = function(){};
    ji_IOException538_$init539 = function($this) {
        jl_Exception127_$init128($this);
        return;
    }
}
$rt_methodStubs(ji_IOException538_$clinit, ['ji_IOException538_$init539']);
ji_IOException538.$init539 = function() {
    var result = new ji_IOException538();
    result.$init539();
    return result;
}
$rt_virtualMethods(ji_IOException538,
    "$init539", function() { ji_IOException538_$init539(this); });
function jul_LogRecord540() {
    this.message541 = null;
    this.threadID542 = Long_ZERO;
    this.level543 = null;
    this.parameters544 = null;
    this.millis545 = Long_ZERO;
    this.sequenceNumber546 = Long_ZERO;
}
jul_LogRecord540.sequenceNumberGenerator547 = Long_ZERO;
$rt_declClass(jul_LogRecord540, {
    name : "java.util.logging.LogRecord",
    interfaces : [ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jul_LogRecord540_$clinit(); } });
function jul_LogRecord540_$clinit() {
    jul_LogRecord540_$clinit = function(){};
    jul_LogRecord540_$init548 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.level543 = a;
        $this.message541 = b;
        $this.millis545 = jl_System125_currentTimeMillis549();
        a = jul_LogRecord540.sequenceNumberGenerator547;
        jul_LogRecord540.sequenceNumberGenerator547 = Long_add(a, Long_fromInt(1));
        $this.sequenceNumber546 = a;
        $this.threadID542 = jl_Thread233_getId242($rt_nullCheck(jl_Thread233_currentThread239()));
        return;
    }
}
$rt_methodStubs(jul_LogRecord540_$clinit, ['jul_LogRecord540_$init548']);
function jul_LogRecord540_getParameters550($this) {
    return $this.parameters544;
}
function jul_LogRecord540_getLevel551($this) {
    return $this.level543;
}
function jul_LogRecord540_getMessage121($this) {
    return $this.message541;
}
jul_LogRecord540.$init548 = function(a, b) {
    var result = new jul_LogRecord540();
    result.$init548(a, b);
    return result;
}
$rt_virtualMethods(jul_LogRecord540,
    "getParameters550", function() { return jul_LogRecord540_getParameters550(this); },
    "$init548", function(a, b) { jul_LogRecord540_$init548(this, a, b); },
    "getLevel551", function() { return jul_LogRecord540_getLevel551(this); },
    "getMessage121", function() { return jul_LogRecord540_getMessage121(this); });
function oadm_Mines$2491() {
    this.this$0552 = null;
}
$rt_declClass(oadm_Mines$2491, {
    name : "org.apidesign.demo.minesweeper.Mines$2",
    interfaces : [jl_Runnable232],
    superclass : jl_Object7,
    clinit : function() { oadm_Mines$2491_$clinit(); } });
function oadm_Mines$2491_$clinit() {
    oadm_Mines$2491_$clinit = function(){};
    oadm_Mines$2491_$init492 = function($this, a) {
        $this.this$0552 = a;
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(oadm_Mines$2491_$clinit, ['oadm_Mines$2491_$init492']);
function oadm_Mines$2491_run381($this) {
    oadm_MinesModel43_computeMines79($this.this$0552);
    return;
}
oadm_Mines$2491.$init492 = function(a) {
    var result = new oadm_Mines$2491();
    result.$init492(a);
    return result;
}
$rt_virtualMethods(oadm_Mines$2491,
    "$init492", function(a) { oadm_Mines$2491_$init492(this, a); },
    "run381", function() { oadm_Mines$2491_run381(this); });
function ju_Comparator553() {
}
$rt_declClass(ju_Comparator553, {
    name : "java.util.Comparator",
    superclass : jl_Object7 });
function ju_HashMap$Entry554() {
    this.next555 = null;
    this.origKeyHash556 = 0;
}
$rt_declClass(ju_HashMap$Entry554, {
    name : "java.util.HashMap$Entry",
    superclass : ju_MapEntry263,
    clinit : function() { ju_HashMap$Entry554_$clinit(); } });
function ju_HashMap$Entry554_$clinit() {
    ju_HashMap$Entry554_$clinit = function(){};
    ju_HashMap$Entry554_$init557 = function($this, a, b) {
        ju_MapEntry263_$init266($this, a, null);
        $this.origKeyHash556 = b;
        return;
    }
}
$rt_methodStubs(ju_HashMap$Entry554_$clinit, ['ju_HashMap$Entry554_$init557']);
ju_HashMap$Entry554.$init557 = function(a, b) {
    var result = new ju_HashMap$Entry554();
    result.$init557(a, b);
    return result;
}
$rt_virtualMethods(ju_HashMap$Entry554,
    "$init557", function(a, b) { ju_HashMap$Entry554_$init557(this, a, b); });
function otcic_UTF16Helper169() {
}
$rt_declClass(otcic_UTF16Helper169, {
    name : "org.teavm.classlib.impl.charset.UTF16Helper",
    superclass : jl_Object7,
    clinit : function() { otcic_UTF16Helper169_$clinit(); } });
function otcic_UTF16Helper169_$clinit() {
    otcic_UTF16Helper169_$clinit = function(){};
    otcic_UTF16Helper169_$init558 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    otcic_UTF16Helper169_buildCodePoint189 = function(a, b) {
        return (((a & 1023) << 10) | (((b & 1023) + 65536) | 0));
    }
    otcic_UTF16Helper169_isLowSurrogate186 = function(a) {
        if (((a & 64512) != 56320)) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    otcic_UTF16Helper169_isSurrogate559 = function(a) {
        if (((a & 63488) != 55296)) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    otcic_UTF16Helper169_lowSurrogate171 = function(a) {
        return ((56320 | (a & 1023)) & 65535);
    }
    otcic_UTF16Helper169_highSurrogate170 = function(a) {
        return ((55296 | ((a >> 10) & 1023)) & 65535);
    }
    otcic_UTF16Helper169_isHighSurrogate185 = function(a) {
        if (((a & 64512) != 55296)) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
}
$rt_methodStubs(otcic_UTF16Helper169_$clinit, ['otcic_UTF16Helper169_$init558', 'otcic_UTF16Helper169_buildCodePoint189', 'otcic_UTF16Helper169_isLowSurrogate186', 'otcic_UTF16Helper169_isSurrogate559', 'otcic_UTF16Helper169_lowSurrogate171', 'otcic_UTF16Helper169_highSurrogate170', 'otcic_UTF16Helper169_isHighSurrogate185']);
otcic_UTF16Helper169.$init558 = function() {
    var result = new otcic_UTF16Helper169();
    result.$init558();
    return result;
}
$rt_virtualMethods(otcic_UTF16Helper169,
    "$init558", function() { otcic_UTF16Helper169_$init558(this); });
function ju_Random68() {
}
$rt_declClass(ju_Random68, {
    name : "java.util.Random",
    interfaces : [ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { ju_Random68_$clinit(); } });
function ju_Random68_$clinit() {
    ju_Random68_$clinit = function(){};
    ju_Random68_$init69 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    ju_Random68_random560 = function() {
        return Math.random();
    }
}
$rt_methodStubs(ju_Random68_$clinit, ['ju_Random68_$init69', 'ju_Random68_random560']);
function ju_Random68_nextInt70($this, a) {
    return ((ju_Random68_random560() * a) | 0);
}
ju_Random68.$init69 = function() {
    var result = new ju_Random68();
    result.$init69();
    return result;
}
$rt_virtualMethods(ju_Random68,
    "$init69", function() { ju_Random68_$init69(this); },
    "nextInt70", function(a) { return ju_Random68_nextInt70(this, a); });
function jl_UnsupportedOperationException222() {
}
$rt_declClass(jl_UnsupportedOperationException222, {
    name : "java.lang.UnsupportedOperationException",
    superclass : jl_RuntimeException131,
    clinit : function() { jl_UnsupportedOperationException222_$clinit(); } });
function jl_UnsupportedOperationException222_$clinit() {
    jl_UnsupportedOperationException222_$clinit = function(){};
    jl_UnsupportedOperationException222_$init223 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(jl_UnsupportedOperationException222_$clinit, ['jl_UnsupportedOperationException222_$init223']);
jl_UnsupportedOperationException222.$init223 = function() {
    var result = new jl_UnsupportedOperationException222();
    result.$init223();
    return result;
}
$rt_virtualMethods(jl_UnsupportedOperationException222,
    "$init223", function() { jl_UnsupportedOperationException222_$init223(this); });
function oadm_MinesModel$1100() {
}
oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101 = null;
$rt_declClass(oadm_MinesModel$1100, {
    name : "org.apidesign.demo.minesweeper.MinesModel$1",
    superclass : jl_Object7,
    clinit : function() { oadm_MinesModel$1100_$clinit(); } });
function oadm_MinesModel$1100_$clinit() {
    oadm_MinesModel$1100_$clinit = function(){};
    oadm_MinesModel$1100_$clinit561 = function() {
        var a, b, c;
        oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101 = $rt_createIntArray(oadm_MinesModel$SquareType45_values562().data.length);
        block1: {
            block2: {
                try {
                    a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                    oadm_MinesModel$SquareType45_$clinit();
                    b = jl_Enum102_ordinal103($rt_nullCheck(oadm_MinesModel$SquareType45.EXPLOSION47));
                    c = 1;
                    a.data[b] = c;
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NoSuchFieldError216) {
                        a = $je;
                        break block2;
                    } else {
                        throw $e;
                    }
                }
                break block1;
            }
        }
        block3: {
            block4: {
                try {
                    a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                    oadm_MinesModel$SquareType45_$clinit();
                    b = jl_Enum102_ordinal103($rt_nullCheck(oadm_MinesModel$SquareType45.UNKNOWN64));
                    c = 2;
                    a.data[b] = c;
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NoSuchFieldError216) {
                        a = $je;
                        break block4;
                    } else {
                        throw $e;
                    }
                }
                break block3;
            }
        }
        block5: {
            block6: {
                try {
                    a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                    oadm_MinesModel$SquareType45_$clinit();
                    b = jl_Enum102_ordinal103($rt_nullCheck(oadm_MinesModel$SquareType45.DISCOVERED90));
                    c = 3;
                    a.data[b] = c;
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NoSuchFieldError216) {
                        a = $je;
                        break block6;
                    } else {
                        throw $e;
                    }
                }
                break block5;
            }
        }
        block7: {
            block8: {
                try {
                    a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                    oadm_MinesModel$SquareType45_$clinit();
                    b = jl_Enum102_ordinal103($rt_nullCheck(oadm_MinesModel$SquareType45.N_085));
                    c = 4;
                    a.data[b] = c;
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NoSuchFieldError216) {
                        a = $je;
                        break block8;
                    } else {
                        throw $e;
                    }
                }
                break block7;
            }
        }
        block9: {
            block10: {
                try {
                    a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                    oadm_MinesModel$SquareType45_$clinit();
                    b = jl_Enum102_ordinal103($rt_nullCheck(oadm_MinesModel$SquareType45.N_8563));
                    c = 5;
                    a.data[b] = c;
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NoSuchFieldError216) {
                        a = $je;
                        break block10;
                    } else {
                        throw $e;
                    }
                }
                break block9;
            }
        }
        return;
    }
    oadm_MinesModel$1100_$clinit561();
}
$rt_methodStubs(oadm_MinesModel$1100_$clinit, ['oadm_MinesModel$1100_$clinit561']);
function ju_AbstractList$1108() {
    this.index564 = 0;
    this.this$0565 = null;
    this.modCount566 = 0;
    this.size567 = 0;
}
$rt_declClass(ju_AbstractList$1108, {
    name : "java.util.AbstractList$1",
    interfaces : [ju_Iterator278],
    superclass : jl_Object7,
    clinit : function() { ju_AbstractList$1108_$clinit(); } });
function ju_AbstractList$1108_$clinit() {
    ju_AbstractList$1108_$clinit = function(){};
    ju_AbstractList$1108_$init225 = function($this, a) {
        $this.this$0565 = a;
        jl_Object7_$init10($this);
        $this.modCount566 = $this.this$0565.modCount220;
        $this.size567 = $rt_nullCheck($this.this$0565).size55();
        return;
    }
}
$rt_methodStubs(ju_AbstractList$1108_$clinit, ['ju_AbstractList$1108_$init225']);
function ju_AbstractList$1108_hasNext109($this) {
    var a;
    if (($this.index564 >= $this.size567)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function ju_AbstractList$1108_next110($this) {
    var a, b;
    ju_AbstractList$1108_checkConcurrentModification568($this);
    a = $this.this$0565;
    b = $this.index564;
    $this.index564 = ((b + 1) | 0);
    return $rt_nullCheck(a).get58(b);
}
function ju_AbstractList$1108_checkConcurrentModification568($this) {
    if (($this.modCount566 >= $this.this$0565.modCount220)) {
        return;
    }
    $rt_throw(ju_ConcurrentModificationException569.$init570());
}
ju_AbstractList$1108.$init225 = function(a) {
    var result = new ju_AbstractList$1108();
    result.$init225(a);
    return result;
}
$rt_virtualMethods(ju_AbstractList$1108,
    "$init225", function(a) { ju_AbstractList$1108_$init225(this, a); },
    "hasNext109", function() { return ju_AbstractList$1108_hasNext109(this); },
    "next110", function() { return ju_AbstractList$1108_next110(this); },
    "checkConcurrentModification568", function() { ju_AbstractList$1108_checkConcurrentModification568(this); });
function onhk_Knockout571() {
}
$rt_declClass(onhk_Knockout571, {
    name : "org.netbeans.html.ko4j.Knockout",
    superclass : jl_Object7,
    clinit : function() { onhk_Knockout571_$clinit(); } });
function onhk_Knockout571_$clinit() {
    onhk_Knockout571_$clinit = function(){};
    onhk_Knockout571_wrapModel572 = function(a, b, c, d, e, f, g) {
        var result = (function(model, propNames, propReadOnly, propValues, propArr, funcNames, funcArr) {
            var ret = {};
ret['ko-fx.model'] = model;
function koComputed(name, readOnly, value, prop) {
  function realGetter() {
    try {
      var v = (function($this) { return oth_JavaScriptConv573_toJavaScript574($this.getValue38()); })(prop);
      return v;
    } catch (e) {
      alert("Cannot call getValue on " + model + " prop: " + name + " error: " + e);
    }
  }
  var activeGetter = function() { return value; };
  var bnd = {
    read: function() {
      var r = activeGetter();
      activeGetter = realGetter;
      return r == null ? null : r.valueOf();
    },
    owner: ret
  };
  if (!readOnly) {
    bnd.write = function(val) {
      (function($this, p0) { return oth_JavaScriptConv573_toJavaScript574($this.setValue35(oth_JavaScriptConv573_fromJavaScript575(p0, jl_Object7))); })(prop,val);
    };
  };
  ret[name] = ko.computed(bnd);
}
for (var i = 0; i < propNames.length; i++) {
  koComputed(propNames[i], propReadOnly[i], propValues[i], propArr[i]);
}
function koExpose(name, func) {
  ret[name] = function(data, ev) {
    (function($this, p0, p1) { return oth_JavaScriptConv573_toJavaScript574($this.call576(oth_JavaScriptConv573_fromJavaScript575(p0, jl_Object7), oth_JavaScriptConv573_fromJavaScript575(p1, jl_Object7))); })(func,data, ev);
  };
}
for (var i = 0; i < funcNames.length; i++) {
  koExpose(funcNames[i], funcArr[i]);
}
return ret;

        }).call(null, oth_JavaScriptConv573_toJavaScript574(a), oth_JavaScriptConv573_toJavaScript574(b), oth_JavaScriptConv573_toJavaScript574(c), oth_JavaScriptConv573_toJavaScript574(d), oth_JavaScriptConv573_toJavaScript574(e), oth_JavaScriptConv573_toJavaScript574(f), oth_JavaScriptConv573_toJavaScript574(g));
        return oth_JavaScriptConv573_fromJavaScript575(result, jl_Object7);
    }
    onhk_Knockout571_toModel577 = function(a) {
        var result = (function(o) {
            return o['ko-fx.model'] ? o['ko-fx.model'] : o;
        }).call(null, oth_JavaScriptConv573_toJavaScript574(a));
        return oth_JavaScriptConv573_fromJavaScript575(result, jl_Object7);
    }
    onhk_Knockout571_$init578 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    onhk_Knockout571_applyBindings579 = function(a) {
        var result = (function(bindings) {
            ko.applyBindings(bindings);

        }).call(null, oth_JavaScriptConv573_toJavaScript574(a));
        return oth_JavaScriptConv573_fromJavaScript575(result, $rt_voidcls());
    }
    onhk_Knockout571_valueHasMutated580 = function(a, b) {
        var result = (function(model, prop) {
            if (model) {
  var koProp = model[prop];
  if (koProp && koProp['valueHasMutated']) {
    koProp['valueHasMutated']();
  }
}

        }).call(null, oth_JavaScriptConv573_toJavaScript574(a), oth_JavaScriptConv573_toJavaScript574(b));
        return oth_JavaScriptConv573_fromJavaScript575(result, $rt_voidcls());
    }
}
$rt_methodStubs(onhk_Knockout571_$clinit, ['onhk_Knockout571_wrapModel572', 'onhk_Knockout571_toModel577', 'onhk_Knockout571_$init578', 'onhk_Knockout571_applyBindings579', 'onhk_Knockout571_valueHasMutated580']);
onhk_Knockout571.$init578 = function() {
    var result = new onhk_Knockout571();
    result.$init578();
    return result;
}
$rt_virtualMethods(onhk_Knockout571,
    "$init578", function() { onhk_Knockout571_$init578(this); });
function jl_SystemClassLoader517() {
}
$rt_declClass(jl_SystemClassLoader517, {
    name : "java.lang.SystemClassLoader",
    superclass : jl_ClassLoader512,
    clinit : function() { jl_SystemClassLoader517_$clinit(); } });
function jl_SystemClassLoader517_$clinit() {
    jl_SystemClassLoader517_$clinit = function(){};
    jl_SystemClassLoader517_$init518 = function($this) {
        jl_ClassLoader512_$init519($this);
        return;
    }
}
$rt_methodStubs(jl_SystemClassLoader517_$clinit, ['jl_SystemClassLoader517_$init518']);
jl_SystemClassLoader517.$init518 = function() {
    var result = new jl_SystemClassLoader517();
    result.$init518();
    return result;
}
$rt_virtualMethods(jl_SystemClassLoader517,
    "$init518", function() { jl_SystemClassLoader517_$init518(this); });
function jul_Level581() {
    this.name582 = null;
    this.value583 = 0;
}
jul_Level581.CONFIG584 = null;
jul_Level581.FINE585 = null;
jul_Level581.FINEST586 = null;
jul_Level581.FINER587 = null;
jul_Level581.WARNING588 = null;
jul_Level581.SEVERE589 = null;
jul_Level581.OFF590 = null;
jul_Level581.INFO591 = null;
jul_Level581.ALL592 = null;
$rt_declClass(jul_Level581, {
    name : "java.util.logging.Level",
    interfaces : [ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jul_Level581_$clinit(); } });
function jul_Level581_$clinit() {
    jul_Level581_$clinit = function(){};
    jul_Level581_$clinit593 = function() {
        jul_Level581.OFF590 = jul_Level581.$init594($rt_str("OFF"), 2147483647);
        jul_Level581.SEVERE589 = jul_Level581.$init594($rt_str("SEVERE"), 1000);
        jul_Level581.WARNING588 = jul_Level581.$init594($rt_str("WARNING"), 900);
        jul_Level581.INFO591 = jul_Level581.$init594($rt_str("INFO"), 800);
        jul_Level581.CONFIG584 = jul_Level581.$init594($rt_str("CONFIG"), 700);
        jul_Level581.FINE585 = jul_Level581.$init594($rt_str("FINE"), 500);
        jul_Level581.FINER587 = jul_Level581.$init594($rt_str("FINER"), 400);
        jul_Level581.FINEST586 = jul_Level581.$init594($rt_str("FINEST"), 300);
        jul_Level581.ALL592 = jul_Level581.$init594($rt_str("FINEST"), -2147483648);
        return;
    }
    jul_Level581_$init594 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.name582 = a;
        $this.value583 = b;
        return;
    }
    jul_Level581_$clinit593();
}
$rt_methodStubs(jul_Level581_$clinit, ['jul_Level581_$clinit593', 'jul_Level581_$init594']);
function jul_Level581_intValue87($this) {
    return $this.value583;
}
jul_Level581.$init594 = function(a, b) {
    var result = new jul_Level581();
    result.$init594(a, b);
    return result;
}
$rt_virtualMethods(jul_Level581,
    "intValue87", function() { return jul_Level581_intValue87(this); },
    "$init594", function(a, b) { jul_Level581_$init594(this, a, b); });
function jl_ConsoleOutputStream_stderr595() {
}
$rt_declClass(jl_ConsoleOutputStream_stderr595, {
    name : "java.lang.ConsoleOutputStream_stderr",
    superclass : ji_OutputStream524,
    clinit : function() { jl_ConsoleOutputStream_stderr595_$clinit(); } });
function jl_ConsoleOutputStream_stderr595_$clinit() {
    jl_ConsoleOutputStream_stderr595_$clinit = function(){};
    jl_ConsoleOutputStream_stderr595_$init596 = function($this) {
        ji_OutputStream524_$init525($this);
        return;
    }
}
$rt_methodStubs(jl_ConsoleOutputStream_stderr595_$clinit, ['jl_ConsoleOutputStream_stderr595_$init596']);
function jl_ConsoleOutputStream_stderr595_write527($this, a) {
    $rt_putStderr(a);
}
jl_ConsoleOutputStream_stderr595.$init596 = function() {
    var result = new jl_ConsoleOutputStream_stderr595();
    result.$init596();
    return result;
}
$rt_virtualMethods(jl_ConsoleOutputStream_stderr595,
    "write527", function(a) { jl_ConsoleOutputStream_stderr595_write527(this, a); },
    "$init596", function() { jl_ConsoleOutputStream_stderr595_$init596(this); });
function onhci_CtxAccssr597() {
}
onhci_CtxAccssr597.DEFAULT598 = null;
$rt_declClass(onhci_CtxAccssr597, {
    name : "org.netbeans.html.context.impl.CtxAccssr",
    superclass : jl_Object7,
    clinit : function() { onhci_CtxAccssr597_$clinit(); } });
function onhci_CtxAccssr597_$clinit() {
    onhci_CtxAccssr597_$clinit = function(){};
    onhci_CtxAccssr597_$clinit599 = function() {
        var a;
        block1: {
            block2: {
                try {
                    njh_BrwsrCtx488_$clinit();
                    $rt_cls($rt_nullCheck(njh_BrwsrCtx488.EMPTY600).constructor);
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_NullPointerException8) {
                        a = $je;
                        break block2;
                    } else {
                        throw $e;
                    }
                }
                break block1;
            }
        }
        return;
    }
    onhci_CtxAccssr597_$init601 = function($this) {
        jl_Object7_$init10($this);
        if ((onhci_CtxAccssr597.DEFAULT598 === null)) {
            onhci_CtxAccssr597.DEFAULT598 = $this;
            return;
        }
        $rt_throw(jl_IllegalStateException229.$init452());
    }
    onhci_CtxAccssr597_getDefault602 = function() {
        return onhci_CtxAccssr597.DEFAULT598;
    }
    onhci_CtxAccssr597_$clinit599();
}
$rt_methodStubs(onhci_CtxAccssr597_$clinit, ['onhci_CtxAccssr597_$clinit599', 'onhci_CtxAccssr597_$init601', 'onhci_CtxAccssr597_getDefault602']);
onhci_CtxAccssr597.$init601 = function() {
    var result = new onhci_CtxAccssr597();
    result.$init601();
    return result;
}
$rt_virtualMethods(onhci_CtxAccssr597,
    "$init601", function() { onhci_CtxAccssr597_$init601(this); });
function njh_BrwsrCtx$1603() {
}
$rt_declClass(njh_BrwsrCtx$1603, {
    name : "net.java.html.BrwsrCtx$1",
    superclass : onhci_CtxAccssr597,
    clinit : function() { njh_BrwsrCtx$1603_$clinit(); } });
function njh_BrwsrCtx$1603_$clinit() {
    njh_BrwsrCtx$1603_$clinit = function(){};
    njh_BrwsrCtx$1603_$init604 = function($this) {
        onhci_CtxAccssr597_$init601($this);
        return;
    }
}
$rt_methodStubs(njh_BrwsrCtx$1603_$clinit, ['njh_BrwsrCtx$1603_$init604']);
function njh_BrwsrCtx$1603_newContext605($this, a) {
    return njh_BrwsrCtx488.$init606(a, null);
}
function njh_BrwsrCtx$1603_find607($this, a) {
    return njh_BrwsrCtx488_access$100608(a);
}
njh_BrwsrCtx$1603.$init604 = function() {
    var result = new njh_BrwsrCtx$1603();
    result.$init604();
    return result;
}
$rt_virtualMethods(njh_BrwsrCtx$1603,
    "$init604", function() { njh_BrwsrCtx$1603_$init604(this); },
    "newContext605", function(a) { return njh_BrwsrCtx$1603_newContext605(this, a); },
    "find607", function(a) { return njh_BrwsrCtx$1603_find607(this, a); });
function jl_Enum102() {
    this.name609 = null;
    this.ordinal610 = 0;
}
$rt_declClass(jl_Enum102, {
    name : "java.lang.Enum",
    interfaces : [jl_Comparable163, ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jl_Enum102_$clinit(); } });
function jl_Enum102_$clinit() {
    jl_Enum102_$clinit = function(){};
    jl_Enum102_$init611 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.name609 = a;
        $this.ordinal610 = b;
        return;
    }
}
$rt_methodStubs(jl_Enum102_$clinit, ['jl_Enum102_$init611']);
function jl_Enum102_compareTo181($this, a) {
    return jl_Enum102_compareTo612($rt_nullCheck($this), a);
}
function jl_Enum102_compareTo612($this, a) {
    var b, c;
    b = $rt_nullCheck(a);
    a = jl_Enum102_getDeclaringClass315(b);
    c = $rt_nullCheck($this);
    if ((a === jl_Enum102_getDeclaringClass315(c))) {
        return jl_Integer81_compare613($this.ordinal610, jl_Enum102_ordinal103(b));
    }
    $rt_throw(jl_IllegalArgumentException134.$init136(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("Can\'t compare "))), jl_String3_toString15($rt_nullCheck(jl_Class0_getName20($rt_nullCheck(jl_Enum102_getDeclaringClass315(c))))))), $rt_str(" to "))), jl_String3_toString15($rt_nullCheck(jl_Class0_getName20($rt_nullCheck(jl_Enum102_getDeclaringClass315(b))))))))));
}
function jl_Enum102_toString15($this) {
    return $this.name609;
}
function jl_Enum102_ordinal103($this) {
    return $this.ordinal610;
}
function jl_Enum102_equals13($this, a) {
    if (($this !== a)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function jl_Enum102_getDeclaringClass315($this) {
    return $rt_cls($rt_nullCheck($this).constructor);
}
function jl_Enum102_name614($this) {
    return $this.name609;
}
jl_Enum102.$init611 = function(a, b) {
    var result = new jl_Enum102();
    result.$init611(a, b);
    return result;
}
$rt_virtualMethods(jl_Enum102,
    "compareTo181", function(a) { return jl_Enum102_compareTo181(this, a); },
    "compareTo612", function(a) { return jl_Enum102_compareTo612(this, a); },
    "toString15", function() { return jl_Enum102_toString15(this); },
    "ordinal103", function() { return jl_Enum102_ordinal103(this); },
    "equals13", function(a) { return jl_Enum102_equals13(this, a); },
    "getDeclaringClass315", function() { return jl_Enum102_getDeclaringClass315(this); },
    "name614", function() { return jl_Enum102_name614(this); },
    "$init611", function(a, b) { jl_Enum102_$init611(this, a, b); });
function otcic_ByteBuffer615() {
    this.data616 = null;
    this.end617 = 0;
    this.pos618 = 0;
}
$rt_declClass(otcic_ByteBuffer615, {
    name : "org.teavm.classlib.impl.charset.ByteBuffer",
    superclass : jl_Object7,
    clinit : function() { otcic_ByteBuffer615_$clinit(); } });
function otcic_ByteBuffer615_$clinit() {
    otcic_ByteBuffer615_$clinit = function(){};
    otcic_ByteBuffer615_$init619 = function($this, a) {
        otcic_ByteBuffer615_$init620($this, a, 0, a.data.length);
        return;
    }
    otcic_ByteBuffer615_$init620 = function($this, a, b, c) {
        jl_Object7_$init10($this);
        $this.data616 = a;
        $this.end617 = c;
        $this.pos618 = b;
        return;
    }
}
$rt_methodStubs(otcic_ByteBuffer615_$clinit, ['otcic_ByteBuffer615_$init619', 'otcic_ByteBuffer615_$init620']);
function otcic_ByteBuffer615_available621($this) {
    return (($this.end617 - $this.pos618) | 0);
}
function otcic_ByteBuffer615_rewind622($this, a) {
    $this.pos618 = a;
    return;
}
function otcic_ByteBuffer615_put623($this, a) {
    var b, c;
    b = $this.data616;
    c = $this.pos618;
    $this.pos618 = ((c + 1) | 0);
    b.data[c] = a;
    return;
}
function otcic_ByteBuffer615_position624($this) {
    return $this.pos618;
}
otcic_ByteBuffer615.$init619 = function(a) {
    var result = new otcic_ByteBuffer615();
    result.$init619(a);
    return result;
}
otcic_ByteBuffer615.$init620 = function(a, b, c) {
    var result = new otcic_ByteBuffer615();
    result.$init620(a, b, c);
    return result;
}
$rt_virtualMethods(otcic_ByteBuffer615,
    "$init619", function(a) { otcic_ByteBuffer615_$init619(this, a); },
    "available621", function() { return otcic_ByteBuffer615_available621(this); },
    "rewind622", function(a) { otcic_ByteBuffer615_rewind622(this, a); },
    "put623", function(a) { otcic_ByteBuffer615_put623(this, a); },
    "$init620", function(a, b, c) { otcic_ByteBuffer615_$init620(this, a, b, c); },
    "position624", function() { return otcic_ByteBuffer615_position624(this); });
function onhji_Bindings406() {
    this.bp625 = null;
    this.data626 = null;
}
onhji_Bindings406.$assertionsDisabled627 = false;
$rt_declClass(onhji_Bindings406, {
    name : "org.netbeans.html.json.impl.Bindings",
    superclass : jl_Object7,
    clinit : function() { onhji_Bindings406_$clinit(); } });
function onhji_Bindings406_$clinit() {
    onhji_Bindings406_$clinit = function(){};
    onhji_Bindings406_$clinit628 = function() {
        var a;
        if ((jl_Class0_desiredAssertionStatus320($rt_nullCheck($rt_cls(onhji_Bindings406))) != 0)) {
            a = 0;
        } else {
            a = 1;
        }
        onhji_Bindings406.$assertionsDisabled627 = a;
        return;
    }
    onhji_Bindings406_apply446 = function(a, b) {
        return onhji_Bindings406_apply629(onhji_JSON40_findTechnology390(a));
    }
    onhji_Bindings406_apply629 = function(a) {
        return onhji_Bindings406.$init630(a);
    }
    onhji_Bindings406_$init630 = function($this, a) {
        jl_Object7_$init10($this);
        $this.bp625 = a;
        return;
    }
    onhji_Bindings406_$clinit628();
}
$rt_methodStubs(onhji_Bindings406_$clinit, ['onhji_Bindings406_$clinit628', 'onhji_Bindings406_apply446', 'onhji_Bindings406_apply629', 'onhji_Bindings406_$init630']);
function onhji_Bindings406_valueHasMutated456($this, a) {
    var b, c;
    b = $this.bp625;
    c = $this.data626;
    $rt_nullCheck(b).valueHasMutated631(c, a);
    return;
}
function onhji_Bindings406_registerProperty447($this, a, b, c, d, e) {
    return onhji_PropertyBindingAccessor404_create497(d, $this, a, b, c, e);
}
function onhji_Bindings406_koData407($this) {
    return $this.data626;
}
function onhji_Bindings406_wrapArray632($this, a) {
    return $rt_nullCheck($this.bp625).wrapArray632(a);
}
function onhji_Bindings406_finish450($this, a, b, c) {
    var d, e;
    if ((!((onhji_Bindings406.$assertionsDisabled627 == 0) && ($this.data626 !== null)))) {
        block3: {
            if (($rt_isInstance($this.bp625, oahjs_Technology$BatchInit226) == 0)) {
                $this.data626 = $rt_nullCheck($this.bp625).wrapModel633(a);
                b = b.data;
                d = b.length;
                e = 0;
                while ((e < d)) {
                    $rt_nullCheck($this.bp625).bind634(b[e], a, $this.data626);
                    e = ((e + 1) | 0);
                }
                b = c.data;
                c = b.length;
                d = 0;
                while (true) {
                    if ((d >= c)) {
                        break block3;
                    }
                    $rt_nullCheck($this.bp625).expose635(b[d], a, $this.data626);
                    d = ((d + 1) | 0);
                }
            }
            $this.data626 = onhk_FXContext636_wrapModel637($rt_nullCheck($this.bp625), a, b, c);
        }
        return;
    }
    $rt_throw(jl_AssertionError316.$init317());
}
function onhji_Bindings406_applyBindings460($this) {
    var a, b;
    a = $this.bp625;
    b = $this.data626;
    $rt_nullCheck(a).applyBindings638(b);
    return;
}
onhji_Bindings406.$init630 = function(a) {
    var result = new onhji_Bindings406();
    result.$init630(a);
    return result;
}
$rt_virtualMethods(onhji_Bindings406,
    "valueHasMutated456", function(a) { onhji_Bindings406_valueHasMutated456(this, a); },
    "registerProperty447", function(a, b, c, d, e) { return onhji_Bindings406_registerProperty447(this, a, b, c, d, e); },
    "koData407", function() { return onhji_Bindings406_koData407(this); },
    "$init630", function(a) { onhji_Bindings406_$init630(this, a); },
    "wrapArray632", function(a) { return onhji_Bindings406_wrapArray632(this, a); },
    "finish450", function(a, b, c) { onhji_Bindings406_finish450(this, a, b, c); },
    "applyBindings460", function() { onhji_Bindings406_applyBindings460(this); });
function oahjs_Transfer639() {
}
$rt_declClass(oahjs_Transfer639, {
    name : "org.apidesign.html.json.spi.Transfer",
    superclass : jl_Object7 });
function juc_Executor640() {
}
$rt_declClass(juc_Executor640, {
    name : "java.util.concurrent.Executor",
    superclass : jl_Object7 });
function oahcs_Contexts$Provider506() {
}
$rt_declClass(oahcs_Contexts$Provider506, {
    name : "org.apidesign.html.context.spi.Contexts$Provider",
    superclass : jl_Object7 });
function jl_IllegalStateException229() {
}
$rt_declClass(jl_IllegalStateException229, {
    name : "java.lang.IllegalStateException",
    superclass : jl_Exception127,
    clinit : function() { jl_IllegalStateException229_$clinit(); } });
function jl_IllegalStateException229_$clinit() {
    jl_IllegalStateException229_$clinit = function(){};
    jl_IllegalStateException229_$init452 = function($this) {
        jl_Exception127_$init128($this);
        return;
    }
    jl_IllegalStateException229_$init230 = function($this, a) {
        jl_Exception127_$init129($this, a);
        return;
    }
    jl_IllegalStateException229_$init641 = function($this, a) {
        jl_Exception127_$init130($this, a);
        return;
    }
}
$rt_methodStubs(jl_IllegalStateException229_$clinit, ['jl_IllegalStateException229_$init452', 'jl_IllegalStateException229_$init230', 'jl_IllegalStateException229_$init641']);
jl_IllegalStateException229.$init452 = function() {
    var result = new jl_IllegalStateException229();
    result.$init452();
    return result;
}
jl_IllegalStateException229.$init230 = function(a) {
    var result = new jl_IllegalStateException229();
    result.$init230(a);
    return result;
}
jl_IllegalStateException229.$init641 = function(a) {
    var result = new jl_IllegalStateException229();
    result.$init641(a);
    return result;
}
$rt_virtualMethods(jl_IllegalStateException229,
    "$init452", function() { jl_IllegalStateException229_$init452(this); },
    "$init230", function(a) { jl_IllegalStateException229_$init230(this, a); },
    "$init641", function(a) { jl_IllegalStateException229_$init641(this, a); });
function ju_NoSuchElementException285() {
}
$rt_declClass(ju_NoSuchElementException285, {
    name : "java.util.NoSuchElementException",
    superclass : jl_RuntimeException131,
    clinit : function() { ju_NoSuchElementException285_$clinit(); } });
function ju_NoSuchElementException285_$clinit() {
    ju_NoSuchElementException285_$clinit = function(){};
    ju_NoSuchElementException285_$init286 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(ju_NoSuchElementException285_$clinit, ['ju_NoSuchElementException285_$init286']);
ju_NoSuchElementException285.$init286 = function() {
    var result = new ju_NoSuchElementException285();
    result.$init286();
    return result;
}
$rt_virtualMethods(ju_NoSuchElementException285,
    "$init286", function() { ju_NoSuchElementException285_$init286(this); });
function jl_Boolean327() {
    this.value642 = false;
}
jl_Boolean327.FALSE643 = null;
jl_Boolean327.TRUE413 = null;
jl_Boolean327.TYPE644 = null;
$rt_declClass(jl_Boolean327, {
    name : "java.lang.Boolean",
    interfaces : [jl_Comparable163, ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jl_Boolean327_$clinit(); } });
function jl_Boolean327_$clinit() {
    jl_Boolean327_$clinit = function(){};
    jl_Boolean327_$clinit645 = function() {
        jl_Boolean327.TRUE413 = jl_Boolean327.$init646(1);
        jl_Boolean327.FALSE643 = jl_Boolean327.$init646(0);
        jl_Boolean327.TYPE644 = $rt_cls($rt_booleancls());
        return;
    }
    jl_Boolean327_valueOf363 = function(a) {
        if ((a == 0)) {
            a = jl_Boolean327.FALSE643;
        } else {
            a = jl_Boolean327.TRUE413;
        }
        return a;
    }
    jl_Boolean327_compare647 = function(a, b) {
        block1: {
            block2: {
                if ((a == 0)) {
                    if ((b == 0)) {
                        break block2;
                    }
                    return -1;
                }
                if ((b == 0)) {
                    break block1;
                }
            }
            return 0;
        }
        return 1;
    }
    jl_Boolean327_parseBoolean414 = function(a) {
        if ((!((a !== null) && (jl_String3_equals13($rt_nullCheck(jl_String3_toLowerCase183($rt_nullCheck(a))), $rt_str("true")) != 0)))) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    jl_Boolean327_$init646 = function($this, a) {
        jl_Object7_$init10($this);
        $this.value642 = a;
        return;
    }
    jl_Boolean327_$clinit645();
}
$rt_methodStubs(jl_Boolean327_$clinit, ['jl_Boolean327_$clinit645', 'jl_Boolean327_valueOf363', 'jl_Boolean327_compare647', 'jl_Boolean327_parseBoolean414', 'jl_Boolean327_$init646']);
function jl_Boolean327_equals13($this, a) {
    if (($this !== a)) {
        if ((!(((a instanceof jl_Boolean327) != 0) && (a.value642 == $this.value642)))) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    return 1;
}
function jl_Boolean327_compareTo181($this, a) {
    return jl_Boolean327_compareTo648($rt_nullCheck($this), a);
}
function jl_Boolean327_booleanValue409($this) {
    return $this.value642;
}
function jl_Boolean327_compareTo648($this, a) {
    return jl_Boolean327_compare647($this.value642, a.value642);
}
jl_Boolean327.$init646 = function(a) {
    var result = new jl_Boolean327();
    result.$init646(a);
    return result;
}
$rt_virtualMethods(jl_Boolean327,
    "equals13", function(a) { return jl_Boolean327_equals13(this, a); },
    "compareTo181", function(a) { return jl_Boolean327_compareTo181(this, a); },
    "$init646", function(a) { jl_Boolean327_$init646(this, a); },
    "booleanValue409", function() { return jl_Boolean327_booleanValue409(this); },
    "compareTo648", function(a) { return jl_Boolean327_compareTo648(this, a); });
function oth_JavaScriptConv573() {
}
$rt_declClass(oth_JavaScriptConv573, {
    name : "org.teavm.html4j.JavaScriptConv",
    superclass : jl_Object7,
    clinit : function() { oth_JavaScriptConv573_$clinit(); } });
function oth_JavaScriptConv573_$clinit() {
    oth_JavaScriptConv573_$clinit = function(){};
    oth_JavaScriptConv573_fromJavaScript575 = function(a, b) {
        if (a === null || a === undefined) {
            return a;
        } else if (b.$meta.item) {
            var arr = $rt_createArray(b.$meta.item, a.length);
            for (var i = 0; i < arr.data.length; ++i) {
                arr.data[i] = oth_JavaScriptConv573_fromJavaScript575(a[i], b.$meta.item);
            }
            return arr;
        } else if (b === jl_String3) {
            return $rt_str(a);
        } else if (b === jl_Integer81) {
            return jl_Integer81_valueOf82(a);
        } else if (b === jl_Double340) {
            return jl_Double340_valueOf342(a);
        } else if (b === $rt_intcls()) {
            return a|0;
        } else if (b === jl_Boolean327) {
            return jl_Boolean327_valueOf363(a?1:0);
        } else if (b === jl_Character187) {
            return jl_Character187_valueOf253(typeof a === 'number' ? a0xFFFF : a.charCodeAt(0));
        } else if (b === $rt_booleancls()) {
            return a?1:0;
        } else if (a instanceof Array) {
            var arr = $rt_createArray($rt_objcls(), a.length);
            for (var i = 0; i < arr.data.length; ++i) {
                arr.data[i] = oth_JavaScriptConv573_fromJavaScript575(a[i], $rt_objcls());
            }
            return arr;
        } else if (typeof a === 'string') {
            return $rt_str(a);
        } else if (typeof a === 'number') {
            if (a|0 === a) {
                return jl_Integer81_valueOf82(a);
            } else {
                return jl_Double340_valueOf342(a);
            }
        } else if (typeof a === 'boolean') {
            return jl_Boolean327_valueOf363(a?1:0);
        } else {
            return a;
        }
    }
    oth_JavaScriptConv573_toJavaScript574 = function(a) {
        if (a === null || a === undefined) {
            return a;
        } else if (typeof a === 'number') {
            return a;
        } else if (a.constructor.$meta && a.constructor.$meta.item) {
            var arr = new Array(a.data.length);
            for (var i = 0; i < arr.length; ++i) {
                arr[i] = oth_JavaScriptConv573_toJavaScript574(a.data[i]);
            }
            return arr;
        } else if (a.constructor === jl_String3) {
            var result = "";
            var data = a.characters166.data;
            for (var i = 0; i < data.length; i = (i + 1) | 0) {
                result += String.fromCharCode(data[i]);
            }
            return result;
        } else if (a.constructor === jl_Integer81) {
            return jl_Integer81_intValue87(a)|0;
        } else if (a.constructor === jl_Boolean327) {
            return jl_Boolean327_booleanValue409(a)!==0;
        } else if (a.constructor === jl_Double340) {
            return jl_Double340_doubleValue341(a);
        } else if (a.constructor === jl_Character187) {
            return jl_Character187_charValue260(a);
        } else {
            return a;
        }
    }
    oth_JavaScriptConv573_$init649 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(oth_JavaScriptConv573_$clinit, ['oth_JavaScriptConv573_fromJavaScript575', 'oth_JavaScriptConv573_toJavaScript574', 'oth_JavaScriptConv573_$init649']);
oth_JavaScriptConv573.$init649 = function() {
    var result = new oth_JavaScriptConv573();
    result.$init649();
    return result;
}
$rt_virtualMethods(oth_JavaScriptConv573,
    "$init649", function() { oth_JavaScriptConv573_$init649(this); });
function oahjs_WSTransfer650() {
}
$rt_declClass(oahjs_WSTransfer650, {
    name : "org.apidesign.html.json.spi.WSTransfer",
    superclass : jl_Object7 });
function onhji_JSON$EmptyTech400() {
}
onhji_JSON$EmptyTech400.EMPTY651 = null;
$rt_declClass(onhji_JSON$EmptyTech400, {
    name : "org.netbeans.html.json.impl.JSON$EmptyTech",
    interfaces : [oahjs_Transfer639, oahjs_WSTransfer650, oahjs_Technology156],
    superclass : jl_Object7,
    clinit : function() { onhji_JSON$EmptyTech400_$clinit(); } });
function onhji_JSON$EmptyTech400_$clinit() {
    onhji_JSON$EmptyTech400_$clinit = function(){};
    onhji_JSON$EmptyTech400_$clinit652 = function() {
        onhji_JSON$EmptyTech400.EMPTY651 = onhji_JSON$EmptyTech400.$init653();
        return;
    }
    onhji_JSON$EmptyTech400_access$000401 = function() {
        return onhji_JSON$EmptyTech400.EMPTY651;
    }
    onhji_JSON$EmptyTech400_$init653 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    onhji_JSON$EmptyTech400_$clinit652();
}
$rt_methodStubs(onhji_JSON$EmptyTech400_$clinit, ['onhji_JSON$EmptyTech400_$clinit652', 'onhji_JSON$EmptyTech400_access$000401', 'onhji_JSON$EmptyTech400_$init653']);
function onhji_JSON$EmptyTech400_wrapArray632($this, a) {
    return a;
}
function onhji_JSON$EmptyTech400_runSafe392($this, a) {
    $rt_nullCheck(a).run381();
    return;
}
function onhji_JSON$EmptyTech400_wrapModel633($this, a) {
    return a;
}
function onhji_JSON$EmptyTech400_bind634($this, a, b, c) {
    return;
}
function onhji_JSON$EmptyTech400_expose635($this, a, b, c) {
    return;
}
function onhji_JSON$EmptyTech400_valueHasMutated631($this, a, b) {
    return;
}
function onhji_JSON$EmptyTech400_applyBindings638($this, a) {
    return;
}
function onhji_JSON$EmptyTech400_toModel389($this, a, b) {
    return jl_Class0_cast346($rt_nullCheck(a), b);
}
onhji_JSON$EmptyTech400.$init653 = function() {
    var result = new onhji_JSON$EmptyTech400();
    result.$init653();
    return result;
}
$rt_virtualMethods(onhji_JSON$EmptyTech400,
    "wrapArray632", function(a) { return onhji_JSON$EmptyTech400_wrapArray632(this, a); },
    "$init653", function() { onhji_JSON$EmptyTech400_$init653(this); },
    "runSafe392", function(a) { onhji_JSON$EmptyTech400_runSafe392(this, a); },
    "wrapModel633", function(a) { return onhji_JSON$EmptyTech400_wrapModel633(this, a); },
    "bind634", function(a, b, c) { onhji_JSON$EmptyTech400_bind634(this, a, b, c); },
    "expose635", function(a, b, c) { onhji_JSON$EmptyTech400_expose635(this, a, b, c); },
    "valueHasMutated631", function(a, b) { onhji_JSON$EmptyTech400_valueHasMutated631(this, a, b); },
    "applyBindings638", function(a) { onhji_JSON$EmptyTech400_applyBindings638(this, a); },
    "toModel389", function(a, b) { return onhji_JSON$EmptyTech400_toModel389(this, a, b); });
function jl_InstantiationException654() {
}
$rt_declClass(jl_InstantiationException654, {
    name : "java.lang.InstantiationException",
    superclass : jl_ReflectiveOperationException493,
    clinit : function() { jl_InstantiationException654_$clinit(); } });
function jl_InstantiationException654_$clinit() {
    jl_InstantiationException654_$clinit = function(){};
    jl_InstantiationException654_$init655 = function($this) {
        jl_ReflectiveOperationException493_$init494($this);
        return;
    }
}
$rt_methodStubs(jl_InstantiationException654_$clinit, ['jl_InstantiationException654_$init655']);
jl_InstantiationException654.$init655 = function() {
    var result = new jl_InstantiationException654();
    result.$init655();
    return result;
}
$rt_virtualMethods(jl_InstantiationException654,
    "$init655", function() { jl_InstantiationException654_$init655(this); });
function onhk_FXContext$1Wrap656() {
    this.val$r657 = null;
    this.this$0658 = null;
}
$rt_declClass(onhk_FXContext$1Wrap656, {
    name : "org.netbeans.html.ko4j.FXContext$1Wrap",
    interfaces : [jl_Runnable232],
    superclass : jl_Object7,
    clinit : function() { onhk_FXContext$1Wrap656_$clinit(); } });
function onhk_FXContext$1Wrap656_$clinit() {
    onhk_FXContext$1Wrap656_$clinit = function(){};
    onhk_FXContext$1Wrap656_$init659 = function($this, a, b) {
        $this.this$0658 = a;
        $this.val$r657 = b;
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(onhk_FXContext$1Wrap656_$clinit, ['onhk_FXContext$1Wrap656_$init659']);
function onhk_FXContext$1Wrap656_run381($this) {
    var a, b;
    a = oahbs_Fn466_activate472(onhk_FXContext636_access$100660($this.this$0658));
    block1: {
        try {
            $rt_nullCheck($this.val$r657).run381();
        } catch ($e) {
            $je = $e.$javaException;
            if ($je) {
                b = $je;
                break block1;
            } else {
                throw $e;
            }
        }
        block2: {
            block3: {
                try {
                    onhbi_FnContext473_close661($rt_nullCheck(a));
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof ji_IOException538) {
                        b = $je;
                        break block3;
                    } else {
                        throw $e;
                    }
                }
                break block2;
            }
        }
        return;
    }
    block4: {
        block5: {
            try {
                onhbi_FnContext473_close661($rt_nullCheck(a));
            } catch ($e) {
                $je = $e.$javaException;
                if ($je && $je instanceof ji_IOException538) {
                    b = $je;
                    break block5;
                } else {
                    throw $e;
                }
            }
            break block4;
        }
    }
    $rt_throw(b);
}
onhk_FXContext$1Wrap656.$init659 = function(a, b) {
    var result = new onhk_FXContext$1Wrap656();
    result.$init659(a, b);
    return result;
}
$rt_virtualMethods(onhk_FXContext$1Wrap656,
    "$init659", function(a, b) { onhk_FXContext$1Wrap656_$init659(this, a, b); },
    "run381", function() { onhk_FXContext$1Wrap656_run381(this); });
function oahcs_Contexts$Builder662() {
    this.impl663 = null;
}
$rt_declClass(oahcs_Contexts$Builder662, {
    name : "org.apidesign.html.context.spi.Contexts$Builder",
    superclass : jl_Object7,
    clinit : function() { oahcs_Contexts$Builder662_$clinit(); } });
function oahcs_Contexts$Builder662_$clinit() {
    oahcs_Contexts$Builder662_$clinit = function(){};
    oahcs_Contexts$Builder662_$init664 = function($this) {
        jl_Object7_$init10($this);
        $this.impl663 = onhci_CtxImpl665.$init666();
        return;
    }
}
$rt_methodStubs(oahcs_Contexts$Builder662_$clinit, ['oahcs_Contexts$Builder662_$init664']);
function oahcs_Contexts$Builder662_build667($this) {
    return onhci_CtxImpl665_build667($rt_nullCheck($this.impl663));
}
function oahcs_Contexts$Builder662_register668($this, a, b, c) {
    if ((b !== null)) {
        if ((c > 0)) {
            onhci_CtxImpl665_register669($rt_nullCheck($this.impl663), a, b, c);
            return $this;
        }
        $rt_throw(jl_IllegalStateException229.$init452());
    }
    return $this;
}
oahcs_Contexts$Builder662.$init664 = function() {
    var result = new oahcs_Contexts$Builder662();
    result.$init664();
    return result;
}
$rt_virtualMethods(oahcs_Contexts$Builder662,
    "$init664", function() { oahcs_Contexts$Builder662_$init664(this); },
    "build667", function() { return oahcs_Contexts$Builder662_build667(this); },
    "register668", function(a, b, c) { return oahcs_Contexts$Builder662_register668(this, a, b, c); });
function jl_ThreadLocal670() {
    this.value671 = null;
    this.initialized672 = false;
}
$rt_declClass(jl_ThreadLocal670, {
    name : "java.lang.ThreadLocal",
    superclass : jl_Object7,
    clinit : function() { jl_ThreadLocal670_$clinit(); } });
function jl_ThreadLocal670_$clinit() {
    jl_ThreadLocal670_$clinit = function(){};
    jl_ThreadLocal670_$init673 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(jl_ThreadLocal670_$clinit, ['jl_ThreadLocal670_$init673']);
function jl_ThreadLocal670_initialValue674($this) {
    return null;
}
function jl_ThreadLocal670_get675($this) {
    if (($this.initialized672 == 0)) {
        $this.value671 = jl_ThreadLocal670_initialValue674($rt_nullCheck($this));
        $this.initialized672 = 1;
    }
    return $this.value671;
}
function jl_ThreadLocal670_set676($this, a) {
    $this.value671 = a;
    return;
}
jl_ThreadLocal670.$init673 = function() {
    var result = new jl_ThreadLocal670();
    result.$init673();
    return result;
}
$rt_virtualMethods(jl_ThreadLocal670,
    "initialValue674", function() { return jl_ThreadLocal670_initialValue674(this); },
    "$init673", function() { jl_ThreadLocal670_$init673(this); },
    "get675", function() { return jl_ThreadLocal670_get675(this); },
    "set676", function(a) { jl_ThreadLocal670_set676(this, a); });
function njh_BrwsrCtx488() {
    this.impl677 = null;
}
njh_BrwsrCtx488.LOG678 = null;
njh_BrwsrCtx488.CURRENT679 = null;
njh_BrwsrCtx488.EMPTY600 = null;
$rt_declClass(njh_BrwsrCtx488, {
    name : "net.java.html.BrwsrCtx",
    interfaces : [juc_Executor640],
    superclass : jl_Object7,
    clinit : function() { njh_BrwsrCtx488_$clinit(); } });
function njh_BrwsrCtx488_$clinit() {
    njh_BrwsrCtx488_$clinit = function(){};
    njh_BrwsrCtx488_$clinit680 = function() {
        njh_BrwsrCtx488.LOG678 = jul_Logger681_getLogger682(jl_Class0_getName20($rt_nullCheck($rt_cls(njh_BrwsrCtx488))));
        njh_BrwsrCtx$1603_$init604(new njh_BrwsrCtx$1603());
        njh_BrwsrCtx488.EMPTY600 = oahcs_Contexts$Builder662_build667($rt_nullCheck(oahcs_Contexts398_newBuilder683()));
        njh_BrwsrCtx488.CURRENT679 = jl_ThreadLocal670.$init673();
        return;
    }
    njh_BrwsrCtx488_findDefault489 = function(a) {
        var b;
        b = jl_ThreadLocal670_get675($rt_nullCheck(njh_BrwsrCtx488.CURRENT679));
        if ((b === null)) {
            b = oahcs_Contexts398_newBuilder683();
            if ((oahcs_Contexts398_fillInByProviders684(a, b) != 0)) {
                return oahcs_Contexts$Builder662_build667($rt_nullCheck(b));
            }
            jul_Logger681_warning685($rt_nullCheck(njh_BrwsrCtx488.LOG678), $rt_str("No browser context found. Returning empty technology!"));
            return njh_BrwsrCtx488.EMPTY600;
        }
        return b;
    }
    njh_BrwsrCtx488_$init606 = function($this, a, b) {
        njh_BrwsrCtx488_$init686($this, a);
        return;
    }
    njh_BrwsrCtx488_$init686 = function($this, a) {
        jl_Object7_$init10($this);
        $this.impl677 = a;
        return;
    }
    njh_BrwsrCtx488_access$100608 = function(a) {
        return a.impl677;
    }
    njh_BrwsrCtx488_$clinit680();
}
$rt_methodStubs(njh_BrwsrCtx488_$clinit, ['njh_BrwsrCtx488_$clinit680', 'njh_BrwsrCtx488_findDefault489', 'njh_BrwsrCtx488_$init606', 'njh_BrwsrCtx488_$init686', 'njh_BrwsrCtx488_access$100608']);
njh_BrwsrCtx488.$init606 = function(a, b) {
    var result = new njh_BrwsrCtx488();
    result.$init606(a, b);
    return result;
}
njh_BrwsrCtx488.$init686 = function(a) {
    var result = new njh_BrwsrCtx488();
    result.$init686(a);
    return result;
}
$rt_virtualMethods(njh_BrwsrCtx488,
    "$init606", function(a, b) { njh_BrwsrCtx488_$init606(this, a, b); },
    "$init686", function(a) { njh_BrwsrCtx488_$init686(this, a); });
function oadm_MinesModel$SquareModel687() {
}
$rt_declClass(oadm_MinesModel$SquareModel687, {
    name : "org.apidesign.demo.minesweeper.MinesModel$SquareModel",
    superclass : jl_Object7,
    clinit : function() { oadm_MinesModel$SquareModel687_$clinit(); } });
function oadm_MinesModel$SquareModel687_$clinit() {
    oadm_MinesModel$SquareModel687_$clinit = function(){};
    oadm_MinesModel$SquareModel687_$init688 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oadm_MinesModel$SquareModel687_style689 = function(a) {
        if ((a !== null)) {
            a = jl_Enum102_toString15($rt_nullCheck(a));
        } else {
            a = null;
        }
        return a;
    }
    oadm_MinesModel$SquareModel687_html690 = function(a) {
        var b, c;
        if ((a !== null)) {
            block2: {
                block3: {
                    block4: {
                        block5: {
                            oadm_MinesModel$1100_$clinit();
                            b = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
                            c = $rt_nullCheck(a);
                            switch (b.data[jl_Enum102_ordinal103(c)]) {
                                case 1:
                                    break;
                                case 2:
                                    break block5;
                                case 3:
                                    break block4;
                                case 4:
                                    break block3;
                                default:
                                    break block2;
                            }
                            return $rt_str("&#x2717;");
                        }
                        return $rt_str("&nbsp;");
                    }
                    return $rt_str("&#x2714;");
                }
                return $rt_str("&nbsp;");
            }
            return jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append18($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("&#x278"))), ((jl_Enum102_ordinal103(c) - 1) | 0))));
        }
        return $rt_str("&nbsp;");
    }
}
$rt_methodStubs(oadm_MinesModel$SquareModel687_$clinit, ['oadm_MinesModel$SquareModel687_$init688', 'oadm_MinesModel$SquareModel687_style689', 'oadm_MinesModel$SquareModel687_html690']);
oadm_MinesModel$SquareModel687.$init688 = function() {
    var result = new oadm_MinesModel$SquareModel687();
    result.$init688();
    return result;
}
$rt_virtualMethods(oadm_MinesModel$SquareModel687,
    "$init688", function() { oadm_MinesModel$SquareModel687_$init688(this); });
function jul_Logger681() {
    this.name691 = null;
    this.parent692 = null;
}
jul_Logger681.GLOBAL_LOGGER_NAME693 = null;
jul_Logger681.loggerCache694 = null;
$rt_declClass(jul_Logger681, {
    name : "java.util.logging.Logger",
    superclass : jl_Object7,
    clinit : function() { jul_Logger681_$clinit(); } });
function jul_Logger681_$clinit() {
    jul_Logger681_$clinit = function(){};
    jul_Logger681_$clinit695 = function() {
        jul_Logger681.GLOBAL_LOGGER_NAME693 = $rt_str("global");
        jul_Logger681.loggerCache694 = ju_HashMap394.$init412();
        return;
    }
    jul_Logger681_digits696 = function(a, b) {
        var c, d;
        block1: {
            while (true) {
                c = $rt_nullCheck(b);
                if ((a >= jl_String3_length5(c))) {
                    break block1;
                }
                d = ((a + 1) | 0);
                a = jl_String3_charAt176(c, a);
                if ((a <= 48)) {
                    break;
                }
                if ((a >= 57)) {
                    break;
                }
                a = d;
            }
            return d;
        }
        return -1;
    }
    jul_Logger681_$init697 = function($this, a) {
        jl_Object7_$init10($this);
        $this.name691 = a;
        return;
    }
    jul_Logger681_getLogger682 = function(a) {
        var b, c, d;
        b = ju_HashMap394_get395($rt_nullCheck(jul_Logger681.loggerCache694), a);
        if ((b === null)) {
            b = jul_Logger681.$init697(a);
            c = 46;
            d = $rt_nullCheck(a);
            c = jl_String3_lastIndexOf193(d, c);
            if ((c >= 0)) {
                b.parent692 = jul_Logger681_getLogger682(jl_String3_substring179(d, 0, c));
            } else if ((jl_String3_isEmpty184(d) == 0)) {
                b.parent692 = jul_Logger681_getLogger682($rt_str(""));
            }
            ju_HashMap394_put397($rt_nullCheck(jul_Logger681.loggerCache694), a, b);
        }
        return b;
    }
    jul_Logger681_$clinit695();
}
$rt_methodStubs(jul_Logger681_$clinit, ['jul_Logger681_$clinit695', 'jul_Logger681_digits696', 'jul_Logger681_$init697', 'jul_Logger681_getLogger682']);
function jul_Logger681_warn698($this, a) {
    if (console) {
        console.warn($rt_ustr(a));
    }
}
function jul_Logger681_log699($this, a) {
    var b, c;
    b = $rt_nullCheck(a);
    a = jul_Logger681_format700($this, jul_LogRecord540_getMessage121(b), jul_LogRecord540_getParameters550(b));
    c = jul_Level581_intValue87($rt_nullCheck(jul_LogRecord540_getLevel551(b)));
    jul_Level581_$clinit();
    if ((c < jul_Level581_intValue87($rt_nullCheck(jul_Level581.SEVERE589)))) {
        c = jul_Level581_intValue87($rt_nullCheck(jul_LogRecord540_getLevel551(b)));
        jul_Level581_$clinit();
        if ((c < jul_Level581_intValue87($rt_nullCheck(jul_Level581.WARNING588)))) {
            jul_Logger681_info701($rt_nullCheck($this), a);
        } else {
            jul_Logger681_warn698($this, a);
        }
    } else {
        jul_Logger681_error702($this, a);
    }
    return;
}
function jul_Logger681_warning685($this, a) {
    var b;
    jul_Level581_$clinit();
    b = jul_Level581.WARNING588;
    jul_Logger681_log703($rt_nullCheck($this), b, a);
    return;
}
function jul_Logger681_info701($this, a) {
    if (console) {
        console.info($rt_ustr(a));
    }
}
function jul_Logger681_error702($this, a) {
    if (console) {
        console.error($rt_ustr(a));
    }
}
function jul_Logger681_log703($this, a, b) {
    var c;
    c = jul_LogRecord540.$init548(a, b);
    jul_Logger681_log699($rt_nullCheck($this), c);
    return;
}
function jul_Logger681_format700($this, a, b) {
    var c, d, e, f, g, h;
    if ((b !== null)) {
        c = jl_StringBuilder16.$init17();
        d = 0;
        block2: {
            block3: {
                while (true) {
                    e = $rt_nullCheck(a);
                    if ((d >= jl_String3_length5(e))) {
                        break block2;
                    }
                    f = jl_String3_indexOf180(e, 123, d);
                    if ((f < 0)) {
                        break block3;
                    }
                    f = jul_Logger681_digits696(((f + 1) | 0), a);
                    if ((f < 0)) {
                        break;
                    }
                    if ((jl_String3_charAt176(e, f) == 125)) {
                        g = jl_Integer81_parseInt704(jl_String3_substring179(e, d, ((f - 1) | 0)));
                        h = b.data;
                        if ((g < h.length)) {
                            jl_StringBuilder16_append705($rt_nullCheck(c), h[g]);
                            d = ((f + 1) | 0);
                            continue;
                        }
                        jl_StringBuilder16_append19($rt_nullCheck(c), jl_String3_substring179(e, d, f));
                        d = f;
                        continue;
                    }
                    jl_StringBuilder16_append19($rt_nullCheck(c), jl_String3_substring179(e, d, f));
                    d = f;
                }
                break block2;
            }
        }
        return jl_StringBuilder16_toString15($rt_nullCheck(c));
    }
    return a;
}
jul_Logger681.$init697 = function(a) {
    var result = new jul_Logger681();
    result.$init697(a);
    return result;
}
$rt_virtualMethods(jul_Logger681,
    "warn698", function(a) { jul_Logger681_warn698(this, a); },
    "log699", function(a) { jul_Logger681_log699(this, a); },
    "warning685", function(a) { jul_Logger681_warning685(this, a); },
    "info701", function(a) { jul_Logger681_info701(this, a); },
    "error702", function(a) { jul_Logger681_error702(this, a); },
    "log703", function(a, b) { jul_Logger681_log703(this, a, b); },
    "$init697", function(a) { jul_Logger681_$init697(this, a); },
    "format700", function(a, b) { return jul_Logger681_format700(this, a, b); });
function otciu_UnicodeHelper256() {
}
$rt_declClass(otciu_UnicodeHelper256, {
    name : "org.teavm.classlib.impl.unicode.UnicodeHelper",
    superclass : jl_Object7,
    clinit : function() { otciu_UnicodeHelper256_$clinit(); } });
function otciu_UnicodeHelper256_$clinit() {
    otciu_UnicodeHelper256_$clinit = function(){};
    otciu_UnicodeHelper256_$init706 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    otciu_UnicodeHelper256_decodeIntByte257 = function(a) {
        var b, c, d, e, f, g, h, i;
        b = 2;
        c = $rt_nullCheck(a);
        a = $rt_createIntArray(((b * ((jl_String3_length5(c) / 5) | 0)) | 0));
        b = 0;
        d = 0;
        while (true) {
            e = a.data;
            if ((d >= e.length)) {
                break;
            }
            f = 0;
            g = 0;
            while ((g < 4)) {
                h = (f << 8);
                i = ((b + 1) | 0);
                f = (h | ((jl_String3_charAt176(c, b) - 122) | 0));
                g = ((g + 1) | 0);
                b = i;
            }
            g = ((d + 1) | 0);
            e[d] = f;
            d = ((g + 1) | 0);
            f = ((b + 1) | 0);
            e[g] = ((jl_String3_charAt176(c, b) - 122) | 0);
            b = f;
        }
        return a;
    }
}
$rt_methodStubs(otciu_UnicodeHelper256_$clinit, ['otciu_UnicodeHelper256_$init706', 'otciu_UnicodeHelper256_decodeIntByte257']);
otciu_UnicodeHelper256.$init706 = function() {
    var result = new otciu_UnicodeHelper256();
    result.$init706();
    return result;
}
$rt_virtualMethods(otciu_UnicodeHelper256,
    "$init706", function() { otciu_UnicodeHelper256_$init706(this); });
function ju_Collections$8144() {
}
$rt_declClass(ju_Collections$8144, {
    name : "java.util.Collections$8",
    interfaces : [ju_Comparator553],
    superclass : jl_Object7,
    clinit : function() { ju_Collections$8144_$clinit(); } });
function ju_Collections$8144_$clinit() {
    ju_Collections$8144_$clinit = function(){};
    ju_Collections$8144_$init198 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
}
$rt_methodStubs(ju_Collections$8144_$clinit, ['ju_Collections$8144_$init198']);
function ju_Collections$8144_compare145($this, a, b) {
    return ju_Collections$8144_compare707($rt_nullCheck($this), a, b);
}
function ju_Collections$8144_compare707($this, a, b) {
    if ((a === null)) {
        a = ((-$rt_nullCheck(b).compareTo181(a)) | 0);
    } else {
        a = $rt_nullCheck(a).compareTo181(b);
    }
    return a;
}
ju_Collections$8144.$init198 = function() {
    var result = new ju_Collections$8144();
    result.$init198();
    return result;
}
$rt_virtualMethods(ju_Collections$8144,
    "$init198", function() { ju_Collections$8144_$init198(this); },
    "compare145", function(a, b) { return ju_Collections$8144_compare145(this, a, b); },
    "compare707", function(a, b) { return ju_Collections$8144_compare707(this, a, b); });
function oadm_Row$Html4JavaType708() {
}
$rt_declClass(oadm_Row$Html4JavaType708, {
    name : "org.apidesign.demo.minesweeper.Row$Html4JavaType",
    superclass : oahjs_Proto$Type36,
    clinit : function() { oadm_Row$Html4JavaType708_$clinit(); } });
function oadm_Row$Html4JavaType708_$clinit() {
    oadm_Row$Html4JavaType708_$clinit = function(){};
    oadm_Row$Html4JavaType708_$init709 = function($this) {
        var a, b, c;
        oahjs_Proto$Type36_$init313($this, $rt_cls(oadm_Row56), $rt_cls(oadm_MinesModel$RowModel140), 1, 0);
        a = $rt_str("columns");
        b = 0;
        c = 1;
        oahjs_Proto$Type36_registerProperty347($rt_nullCheck($this), a, b, c);
        return;
    }
    oadm_Row$Html4JavaType708_$init710 = function($this, a) {
        oadm_Row$Html4JavaType708_$init709($this);
        return;
    }
}
$rt_methodStubs(oadm_Row$Html4JavaType708_$clinit, ['oadm_Row$Html4JavaType708_$init709', 'oadm_Row$Html4JavaType708_$init710']);
function oadm_Row$Html4JavaType708_call359($this, a, b, c, d) {
    a = a;
    oadm_Row$Html4JavaType708_call711($rt_nullCheck($this), a, b, c, d);
    return;
}
function oadm_Row$Html4JavaType708_onChange712($this, a, b) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Row$Html4JavaType708_setValue37($this, a, b, c) {
    a = a;
    oadm_Row$Html4JavaType708_setValue713($rt_nullCheck($this), a, b, c);
    return;
}
function oadm_Row$Html4JavaType708_protoFor370($this, a) {
    return oadm_Row56_access$100714(a);
}
function oadm_Row$Html4JavaType708_onChange372($this, a, b) {
    a = a;
    oadm_Row$Html4JavaType708_onChange712($rt_nullCheck($this), a, b);
    return;
}
function oadm_Row$Html4JavaType708_getValue39($this, a, b) {
    return oadm_Row$Html4JavaType708_getValue715($rt_nullCheck($this), a, b);
}
function oadm_Row$Html4JavaType708_call711($this, a, b, c, d) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Row$Html4JavaType708_getValue715($this, a, b) {
    block1: {
        switch (b) {
            case 0:
                break;
            default:
                break block1;
        }
        return oadm_Row56_getColumns57($rt_nullCheck(a));
    }
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Row$Html4JavaType708_setValue713($this, a, b, c) {
    return;
}
oadm_Row$Html4JavaType708.$init709 = function() {
    var result = new oadm_Row$Html4JavaType708();
    result.$init709();
    return result;
}
oadm_Row$Html4JavaType708.$init710 = function(a) {
    var result = new oadm_Row$Html4JavaType708();
    result.$init710(a);
    return result;
}
$rt_virtualMethods(oadm_Row$Html4JavaType708,
    "call359", function(a, b, c, d) { oadm_Row$Html4JavaType708_call359(this, a, b, c, d); },
    "onChange712", function(a, b) { oadm_Row$Html4JavaType708_onChange712(this, a, b); },
    "setValue37", function(a, b, c) { oadm_Row$Html4JavaType708_setValue37(this, a, b, c); },
    "protoFor370", function(a) { return oadm_Row$Html4JavaType708_protoFor370(this, a); },
    "onChange372", function(a, b) { oadm_Row$Html4JavaType708_onChange372(this, a, b); },
    "getValue39", function(a, b) { return oadm_Row$Html4JavaType708_getValue39(this, a, b); },
    "call711", function(a, b, c, d) { oadm_Row$Html4JavaType708_call711(this, a, b, c, d); },
    "getValue715", function(a, b) { return oadm_Row$Html4JavaType708_getValue715(this, a, b); },
    "$init709", function() { oadm_Row$Html4JavaType708_$init709(this); },
    "$init710", function(a) { oadm_Row$Html4JavaType708_$init710(this, a); },
    "setValue713", function(a, b, c) { oadm_Row$Html4JavaType708_setValue713(this, a, b, c); });
function ji_FilterOutputStream716() {
    this.out717 = null;
}
$rt_declClass(ji_FilterOutputStream716, {
    name : "java.io.FilterOutputStream",
    superclass : ji_OutputStream524,
    clinit : function() { ji_FilterOutputStream716_$clinit(); } });
function ji_FilterOutputStream716_$clinit() {
    ji_FilterOutputStream716_$clinit = function(){};
    ji_FilterOutputStream716_$init718 = function($this, a) {
        ji_OutputStream524_$init525($this);
        $this.out717 = a;
        return;
    }
}
$rt_methodStubs(ji_FilterOutputStream716_$clinit, ['ji_FilterOutputStream716_$init718']);
ji_FilterOutputStream716.$init718 = function(a) {
    var result = new ji_FilterOutputStream716();
    result.$init718(a);
    return result;
}
$rt_virtualMethods(ji_FilterOutputStream716,
    "$init718", function(a) { ji_FilterOutputStream716_$init718(this, a); });
function ji_PrintStream122() {
    this.buffer719 = null;
    this.autoFlush720 = false;
    this.errorState721 = false;
    this.sb722 = null;
    this.charset723 = null;
}
$rt_declClass(ji_PrintStream122, {
    name : "java.io.PrintStream",
    superclass : ji_FilterOutputStream716,
    clinit : function() { ji_PrintStream122_$clinit(); } });
function ji_PrintStream122_$clinit() {
    ji_PrintStream122_$clinit = function(){};
    ji_PrintStream122_$init724 = function($this, a, b) {
        ji_FilterOutputStream716_$init718($this, a);
        $this.sb722 = jl_StringBuilder16.$init17();
        $this.buffer719 = $rt_createCharArray(32);
        $this.autoFlush720 = b;
        $this.charset723 = otcic_Charset382_get384($rt_str("UTF-8"));
        return;
    }
}
$rt_methodStubs(ji_PrintStream122_$clinit, ['ji_PrintStream122_$init724']);
function ji_PrintStream122_println123($this, a) {
    var b;
    a = jl_StringBuilder16_append19($rt_nullCheck($this.sb722), a);
    b = 10;
    jl_StringBuilder16_append725($rt_nullCheck(a), b);
    ji_PrintStream122_printSB726($this);
    return;
}
function ji_PrintStream122_printSB726($this) {
    var a, b, c, d, e;
    if ((jl_StringBuilder16_length5($rt_nullCheck($this.sb722)) <= $this.buffer719.data.length)) {
        a = $this.buffer719;
    } else {
        a = $rt_createCharArray(jl_StringBuilder16_length5($rt_nullCheck($this.sb722)));
    }
    b = $this.sb722;
    c = 0;
    d = jl_StringBuilder16_length5($rt_nullCheck($this.sb722));
    e = 0;
    jl_StringBuilder16_getChars6($rt_nullCheck(b), c, d, a, e);
    ji_PrintStream122_print727($this, a, 0, jl_StringBuilder16_length5($rt_nullCheck($this.sb722)));
    b = $this.sb722;
    c = 0;
    jl_StringBuilder16_setLength728($rt_nullCheck(b), c);
    return;
}
function ji_PrintStream122_print727($this, a, b, c) {
    var d, e, f;
    d = otcic_CharBuffer287.$init291(a, b, c);
    a = $rt_createByteArray(jl_Math147_max463(16, jl_Math147_min148(a.data.length, 1024)));
    b = otcic_ByteBuffer615.$init619(a);
    while ((otcic_CharBuffer287_end293($rt_nullCheck(d)) == 0)) {
        otcic_UTF8Charset385_encode729($rt_nullCheck($this.charset723), d, b);
        c = 0;
        e = $rt_nullCheck(b);
        f = otcic_ByteBuffer615_position624(e);
        ji_PrintStream122_write526($rt_nullCheck($this), a, c, f);
        otcic_ByteBuffer615_rewind622(e, 0);
    }
    return;
}
function ji_PrintStream122_check730($this) {
    var a;
    if (($this.out717 === null)) {
        $this.errorState721 = 1;
    }
    if (($this.errorState721 != 0)) {
        a = 0;
    } else {
        a = 1;
    }
    return a;
}
function ji_PrintStream122_write526($this, a, b, c) {
    if ((ji_PrintStream122_check730($this) != 0)) {
        block2: {
            block3: {
                try {
                    ji_OutputStream524_write526($rt_nullCheck($this.out717), a, b, c);
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof ji_IOException538) {
                        a = $je;
                        break block3;
                    } else {
                        throw $e;
                    }
                }
                break block2;
            }
            $this.errorState721 = 1;
        }
        return;
    }
    return;
}
ji_PrintStream122.$init724 = function(a, b) {
    var result = new ji_PrintStream122();
    result.$init724(a, b);
    return result;
}
$rt_virtualMethods(ji_PrintStream122,
    "$init724", function(a, b) { ji_PrintStream122_$init724(this, a, b); },
    "println123", function(a) { ji_PrintStream122_println123(this, a); },
    "printSB726", function() { ji_PrintStream122_printSB726(this); },
    "print727", function(a, b, c) { ji_PrintStream122_print727(this, a, b, c); },
    "check730", function() { return ji_PrintStream122_check730(this); },
    "write526", function(a, b, c) { ji_PrintStream122_write526(this, a, b, c); });
function jl_StringIndexOutOfBoundsException177() {
}
$rt_declClass(jl_StringIndexOutOfBoundsException177, {
    name : "java.lang.StringIndexOutOfBoundsException",
    superclass : jl_IndexOutOfBoundsException157,
    clinit : function() { jl_StringIndexOutOfBoundsException177_$clinit(); } });
function jl_StringIndexOutOfBoundsException177_$clinit() {
    jl_StringIndexOutOfBoundsException177_$clinit = function(){};
    jl_StringIndexOutOfBoundsException177_$init178 = function($this) {
        jl_IndexOutOfBoundsException157_$init158($this);
        return;
    }
}
$rt_methodStubs(jl_StringIndexOutOfBoundsException177_$clinit, ['jl_StringIndexOutOfBoundsException177_$init178']);
jl_StringIndexOutOfBoundsException177.$init178 = function() {
    var result = new jl_StringIndexOutOfBoundsException177();
    result.$init178();
    return result;
}
$rt_virtualMethods(jl_StringIndexOutOfBoundsException177,
    "$init178", function() { jl_StringIndexOutOfBoundsException177_$init178(this); });
function jl_Appendable731() {
}
$rt_declClass(jl_Appendable731, {
    name : "java.lang.Appendable",
    superclass : jl_Object7 });
function jl_Byte337() {
    this.value732 = 0;
}
jl_Byte337.TYPE733 = null;
$rt_declClass(jl_Byte337, {
    name : "java.lang.Byte",
    interfaces : [jl_Comparable163],
    superclass : jl_Number325,
    clinit : function() { jl_Byte337_$clinit(); } });
function jl_Byte337_$clinit() {
    jl_Byte337_$clinit = function(){};
    jl_Byte337_valueOf339 = function(a) {
        return jl_Byte337.$init734(a);
    }
    jl_Byte337_$clinit735 = function() {
        jl_Byte337.TYPE733 = $rt_cls($rt_bytecls());
        return;
    }
    jl_Byte337_$init734 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value732 = a;
        return;
    }
    jl_Byte337_compare736 = function(a, b) {
        return ((a - b) | 0);
    }
    jl_Byte337_$clinit735();
}
$rt_methodStubs(jl_Byte337_$clinit, ['jl_Byte337_valueOf339', 'jl_Byte337_$clinit735', 'jl_Byte337_$init734', 'jl_Byte337_compare736']);
function jl_Byte337_doubleValue341($this) {
    return $this.value732;
}
function jl_Byte337_intValue87($this) {
    return $this.value732;
}
function jl_Byte337_byteValue338($this) {
    return $this.value732;
}
function jl_Byte337_longValue332($this) {
    return Long_fromInt($this.value732);
}
function jl_Byte337_compareTo737($this, a) {
    return jl_Byte337_compare736($this.value732, a.value732);
}
function jl_Byte337_compareTo181($this, a) {
    return jl_Byte337_compareTo737($rt_nullCheck($this), a);
}
function jl_Byte337_floatValue344($this) {
    return $this.value732;
}
jl_Byte337.$init734 = function(a) {
    var result = new jl_Byte337();
    result.$init734(a);
    return result;
}
$rt_virtualMethods(jl_Byte337,
    "doubleValue341", function() { return jl_Byte337_doubleValue341(this); },
    "$init734", function(a) { jl_Byte337_$init734(this, a); },
    "intValue87", function() { return jl_Byte337_intValue87(this); },
    "byteValue338", function() { return jl_Byte337_byteValue338(this); },
    "longValue332", function() { return jl_Byte337_longValue332(this); },
    "compareTo737", function(a) { return jl_Byte337_compareTo737(this, a); },
    "compareTo181", function(a) { return jl_Byte337_compareTo181(this, a); },
    "floatValue344", function() { return jl_Byte337_floatValue344(this); });
function jl_System125() {
}
jl_System125.err126 = null;
jl_System125.out738 = null;
$rt_declClass(jl_System125, {
    name : "java.lang.System",
    superclass : jl_Object7,
    clinit : function() { jl_System125_$clinit(); } });
function jl_System125_$clinit() {
    jl_System125_$clinit = function(){};
    jl_System125_$clinit739 = function() {
        jl_System125.out738 = ji_PrintStream122.$init724(jl_ConsoleOutputStream_stdout528.$init529(), 0);
        jl_System125.err126 = ji_PrintStream122.$init724(jl_ConsoleOutputStream_stderr595.$init596(), 0);
        return;
    }
    jl_System125_currentTimeMillis549 = function() {
        return Long_fromNumber(new Date().getTime());
    }
    jl_System125_$init740 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    jl_System125_$clinit739();
}
$rt_methodStubs(jl_System125_$clinit, ['jl_System125_$clinit739', 'jl_System125_currentTimeMillis549', 'jl_System125_$init740']);
jl_System125.$init740 = function() {
    var result = new jl_System125();
    result.$init740();
    return result;
}
$rt_virtualMethods(jl_System125,
    "$init740", function() { jl_System125_$init740(this); });
function jl_AbstractStringBuilder741() {
    this.buffer742 = null;
    this.length743 = 0;
}
jl_AbstractStringBuilder741.longPowersOfTen744 = null;
jl_AbstractStringBuilder741.doublePowersOfTen745 = null;
jl_AbstractStringBuilder741.negDoublePowersOfTen746 = null;
jl_AbstractStringBuilder741.longLogPowersOfTen747 = null;
jl_AbstractStringBuilder741.intPowersOfTen748 = null;
jl_AbstractStringBuilder741.negPowersOfTen749 = null;
jl_AbstractStringBuilder741.powersOfTen750 = null;
$rt_declClass(jl_AbstractStringBuilder741, {
    name : "java.lang.AbstractStringBuilder",
    interfaces : [jl_CharSequence162, ji_Serializable164],
    superclass : jl_Object7,
    clinit : function() { jl_AbstractStringBuilder741_$clinit(); } });
function jl_AbstractStringBuilder741_$clinit() {
    jl_AbstractStringBuilder741_$clinit = function(){};
    jl_AbstractStringBuilder741_$init751 = function($this, a) {
        jl_Object7_$init10($this);
        $this.buffer742 = $rt_createCharArray(a);
        return;
    }
    jl_AbstractStringBuilder741_trailingDecimalZeros752 = function(a) {
        var b, c, d, e;
        b = Long_fromInt(1);
        c = 0;
        d = 16;
        e = ((jl_AbstractStringBuilder741.longLogPowersOfTen747.data.length - 1) | 0);
        while ((e >= 0)) {
            if ((Long_compare(Long_rem(a, Long_mul(b, jl_AbstractStringBuilder741.longLogPowersOfTen747.data[e])), Long_ZERO) == 0)) {
                c = (c | d);
                b = Long_mul(b, jl_AbstractStringBuilder741.longLogPowersOfTen747.data[e]);
            }
            d = (d >>> 1);
            e = ((e + -1) | 0);
        }
        return c;
    }
    jl_AbstractStringBuilder741_trailingDecimalZeros753 = function(a) {
        var b, c;
        if (((a % 1000000000) != 0)) {
            b = 0;
            c = 1;
            if (((a % 100000000) == 0)) {
                b = (b | 8);
                c = ((c * 100000000) | 0);
            }
            if (((a % ((c * 10000) | 0)) == 0)) {
                b = (b | 4);
                c = ((c * 10000) | 0);
            }
            if (((a % ((c * 100) | 0)) == 0)) {
                b = (b | 2);
                c = ((c * 100) | 0);
            }
            if (((a % ((c * 10) | 0)) == 0)) {
                b = (b | 1);
            }
            return b;
        }
        return 9;
    }
    jl_AbstractStringBuilder741_$clinit754 = function() {
        var a, b, c, d;
        a = $rt_createFloatArray(6);
        b = 0;
        c = 10.0;
        d = a.data;
        d[b] = c;
        d[1] = 100.0;
        d[2] = 10000.0;
        d[3] = 1.0E8;
        d[4] = 1.00000003E16;
        d[5] = 1.0E32;
        jl_AbstractStringBuilder741.powersOfTen750 = a;
        a = $rt_createDoubleArray(9);
        b = 0;
        c = 10.0;
        d = a.data;
        d[b] = c;
        d[1] = 100.0;
        d[2] = 10000.0;
        d[3] = 1.0E8;
        d[4] = 1.0E16;
        d[5] = 1.0E32;
        d[6] = 1.0E64;
        d[7] = 1.0E128;
        d[8] = 1.0E256;
        jl_AbstractStringBuilder741.doublePowersOfTen745 = a;
        a = $rt_createFloatArray(6);
        b = 0;
        c = 0.1;
        d = a.data;
        d[b] = c;
        d[1] = 0.01;
        d[2] = 1.0E-4;
        d[3] = 1.0E-8;
        d[4] = 1.0E-16;
        d[5] = 1.0E-32;
        jl_AbstractStringBuilder741.negPowersOfTen749 = a;
        a = $rt_createDoubleArray(9);
        b = 0;
        c = 0.1;
        d = a.data;
        d[b] = c;
        d[1] = 0.01;
        d[2] = 1.0E-4;
        d[3] = 1.0E-8;
        d[4] = 1.0E-16;
        d[5] = 1.0E-32;
        d[6] = 1.0E-64;
        d[7] = 1.0E-128;
        d[8] = 1.0E-256;
        jl_AbstractStringBuilder741.negDoublePowersOfTen746 = a;
        a = $rt_createIntArray(10);
        b = 0;
        c = 1;
        d = a.data;
        d[b] = c;
        d[1] = 10;
        d[2] = 100;
        d[3] = 1000;
        d[4] = 10000;
        d[5] = 100000;
        d[6] = 1000000;
        d[7] = 10000000;
        d[8] = 100000000;
        d[9] = 1000000000;
        jl_AbstractStringBuilder741.intPowersOfTen748 = a;
        a = $rt_createLongArray(19);
        b = 0;
        c = Long_fromInt(1);
        d = a.data;
        d[b] = c;
        d[1] = Long_fromInt(10);
        d[2] = Long_fromInt(100);
        d[3] = Long_fromInt(1000);
        d[4] = Long_fromInt(10000);
        d[5] = Long_fromInt(100000);
        d[6] = Long_fromInt(1000000);
        d[7] = Long_fromInt(10000000);
        d[8] = Long_fromInt(100000000);
        d[9] = Long_fromInt(1000000000);
        d[10] = new Long(1410065408, 2);
        d[11] = new Long(1215752192, 23);
        d[12] = new Long(3567587328, 232);
        d[13] = new Long(1316134912, 2328);
        d[14] = new Long(276447232, 23283);
        d[15] = new Long(2764472320, 232830);
        d[16] = new Long(1874919424, 2328306);
        d[17] = new Long(1569325056, 23283064);
        d[18] = new Long(2808348672, 232830643);
        jl_AbstractStringBuilder741.longPowersOfTen744 = a;
        a = $rt_createLongArray(6);
        b = 0;
        c = Long_fromInt(1);
        d = a.data;
        d[b] = c;
        d[1] = Long_fromInt(10);
        d[2] = Long_fromInt(100);
        d[3] = Long_fromInt(10000);
        d[4] = Long_fromInt(100000000);
        d[5] = new Long(1874919424, 2328306);
        jl_AbstractStringBuilder741.longLogPowersOfTen747 = a;
        return;
    }
    jl_AbstractStringBuilder741_$init755 = function($this) {
        jl_AbstractStringBuilder741_$init751($this, 16);
        return;
    }
    jl_AbstractStringBuilder741_$clinit754();
}
$rt_methodStubs(jl_AbstractStringBuilder741_$clinit, ['jl_AbstractStringBuilder741_$init751', 'jl_AbstractStringBuilder741_trailingDecimalZeros752', 'jl_AbstractStringBuilder741_trailingDecimalZeros753', 'jl_AbstractStringBuilder741_$clinit754', 'jl_AbstractStringBuilder741_$init755']);
function jl_AbstractStringBuilder741_ensureCapacity756($this, a) {
    if (($this.buffer742.data.length < a)) {
        $this.buffer742 = ju_Arrays142_copyOf146($this.buffer742, ((((a * 2) | 0) + 1) | 0));
        return;
    }
    return;
}
function jl_AbstractStringBuilder741_length5($this) {
    return $this.length743;
}
function jl_AbstractStringBuilder741_append757($this, a) {
    var b, c, d;
    b = 1;
    if ((Long_compare(a, Long_ZERO) < 0)) {
        b = 0;
        a = Long_neg(a);
    }
    block3: {
        if ((Long_compare(a, Long_fromInt(10)) >= 0)) {
            c = Long_fromInt(10);
            d = 1;
            while (((Long_compare(c, new Long(2808348672, 232830643)) < 0) && (Long_compare(Long_mul(c, Long_fromInt(10)), a) <= 0))) {
                c = Long_mul(c, Long_fromInt(10));
                d = ((d + 1) | 0);
            }
            if ((b == 0)) {
                d = ((d + 1) | 0);
            }
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + d) | 0));
            if ((b == 0)) {
                b = $this.buffer742;
                d = $this.length743;
                $this.length743 = ((d + 1) | 0);
                b.data[d] = 45;
            }
            while (true) {
                if ((Long_compare(c, Long_ZERO) <= 0)) {
                    break block3;
                }
                b = $this.buffer742;
                d = $this.length743;
                $this.length743 = ((d + 1) | 0);
                b.data[d] = (Long_toNumber(Long_add(Long_fromInt(48), Long_div(a, c))) & 65535);
                a = Long_rem(a, c);
                c = Long_div(c, Long_fromInt(10));
            }
        }
        if ((b != 0)) {
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 1) | 0));
        } else {
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 2) | 0));
            c = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            c.data[b] = 45;
        }
        c = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a = (Long_toNumber(Long_add(Long_fromInt(48), a)) & 65535);
        c.data[b] = a;
    }
    return $this;
}
function jl_AbstractStringBuilder741_insert758($this, a, b) {
    var c, d, e;
    if (((a >= 0) && (a <= $this.length743))) {
        block2: {
            if ((b !== null)) {
                if ((jl_String3_isEmpty184($rt_nullCheck(b)) == 0)) {
                    break block2;
                }
                return $this;
            }
            b = $rt_str("null");
        }
        c = $this.length743;
        d = $rt_nullCheck(b);
        jl_AbstractStringBuilder741_ensureCapacity756($this, ((c + jl_String3_length5(d)) | 0));
        if ((a < $this.length743)) {
            b = (($this.length743 - 1) | 0);
            while ((b >= a)) {
                $this.buffer742.data[((b + jl_String3_length5(d)) | 0)] = $this.buffer742.data[b];
                b = ((b + -1) | 0);
            }
            $this.length743 = (($this.length743 + jl_String3_length5(d)) | 0);
        }
        b = 0;
        while ((b < jl_String3_length5(d))) {
            c = $this.buffer742;
            e = ((a + 1) | 0);
            c.data[a] = jl_String3_charAt176(d, b);
            b = ((b + 1) | 0);
            a = e;
        }
        $this.length743 = a;
        return $this;
    }
    $rt_throw(jl_StringIndexOutOfBoundsException177.$init178());
}
function jl_AbstractStringBuilder741_append759($this, a) {
    var b, c, d, e, f, g, h, i;
    if ((a != 0.0)) {
        if ((a != 0.0)) {
            if ((jl_Float343_isNaN427(a) == 0)) {
                if ((jl_Float343_isInfinite430(a) == 0)) {
                    b = 0;
                    c = 1;
                    if ((a < 0.0)) {
                        b = 1;
                        a = (-a);
                        c = ((c + 1) | 0);
                    }
                    d = 1;
                    if ((a < 1.0)) {
                        c = ((c + 1) | 0);
                        e = 32;
                        f = 0;
                        g = 1.0;
                        h = ((jl_AbstractStringBuilder741.negPowersOfTen749.data.length - 1) | 0);
                        while ((h >= 0)) {
                            i = (f | e);
                            if (((i <= 38) && (((jl_AbstractStringBuilder741.negPowersOfTen749.data[h] * g) * 10.0) > a))) {
                                g = (g * jl_AbstractStringBuilder741.negPowersOfTen749.data[h]);
                                f = i;
                            }
                            e = (e >> 1);
                            h = ((h + -1) | 0);
                        }
                        f = ((-f) | 0);
                        g = ((((a * 1000000.0) / g) + 0.5) | 0);
                    } else {
                        e = 32;
                        f = 0;
                        g = 1.0;
                        h = ((jl_AbstractStringBuilder741.powersOfTen750.data.length - 1) | 0);
                        while ((h >= 0)) {
                            i = (f | e);
                            if (((i <= 38) && ((jl_AbstractStringBuilder741.powersOfTen750.data[h] * g) < a))) {
                                g = (g * jl_AbstractStringBuilder741.powersOfTen750.data[h]);
                                f = i;
                            }
                            e = (e >> 1);
                            h = ((h + -1) | 0);
                        }
                        g = (((a / (g / 1000000.0)) + 0.5) | 0);
                    }
                    h = 7;
                    a = jl_AbstractStringBuilder741_trailingDecimalZeros753(g);
                    if ((a > 0)) {
                        h = ((h - a) | 0);
                    }
                    if (((f < 7) && (f >= -3))) {
                        if ((f >= 0)) {
                            d = ((f + 1) | 0);
                            h = jl_Math147_max463(h, ((d + 1) | 0));
                            f = 0;
                        } else if ((f < 0)) {
                            g = ((g / jl_AbstractStringBuilder741.intPowersOfTen748.data[((-f) | 0)]) | 0);
                            h = ((h - f) | 0);
                            f = 0;
                        }
                    }
                    a = ((c + h) | 0);
                    if ((f != 0)) {
                        a = ((a + 2) | 0);
                        if ((!((f > -10) && (f < 10)))) {
                            a = ((a + 1) | 0);
                        }
                    }
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + a) | 0));
                    if ((b != 0)) {
                        a = $this.buffer742;
                        b = $this.length743;
                        $this.length743 = ((b + 1) | 0);
                        a.data[b] = 45;
                    }
                    a = 1000000;
                    b = 0;
                    while ((b < h)) {
                        if ((a <= 0)) {
                            e = 0;
                        } else {
                            e = ((g / a) | 0);
                            g = (g % a);
                        }
                        i = $this.buffer742;
                        c = $this.length743;
                        $this.length743 = ((c + 1) | 0);
                        i.data[c] = (((48 + e) | 0) & 65535);
                        d = ((d + -1) | 0);
                        if ((d == 0)) {
                            e = $this.buffer742;
                            i = $this.length743;
                            $this.length743 = ((i + 1) | 0);
                            e.data[i] = 46;
                        }
                        a = ((a / 10) | 0);
                        b = ((b + 1) | 0);
                    }
                    if ((f != 0)) {
                        a = $this.buffer742;
                        b = $this.length743;
                        $this.length743 = ((b + 1) | 0);
                        a.data[b] = 69;
                        if ((f < 0)) {
                            f = ((-f) | 0);
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = 45;
                        }
                        if ((f >= 10)) {
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = (((48 + ((f / 10) | 0)) | 0) & 65535);
                        }
                        a = $this.buffer742;
                        b = $this.length743;
                        $this.length743 = ((b + 1) | 0);
                        a.data[b] = (((48 + (f % 10)) | 0) & 65535);
                    }
                    return $this;
                }
                if ((a <= 0.0)) {
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 9) | 0));
                    a = $this.buffer742;
                    b = $this.length743;
                    $this.length743 = ((b + 1) | 0);
                    a.data[b] = 45;
                } else {
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 8) | 0));
                }
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 73;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 110;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 102;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 105;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 110;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 105;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 116;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 121;
                return $this;
            }
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 3) | 0));
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 78;
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 97;
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 78;
            return $this;
        }
        jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 4) | 0));
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 45;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 48;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 46;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 48;
        return $this;
    }
    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 3) | 0));
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 48;
    a.data[b] = e;
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 46;
    a.data[b] = e;
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 48;
    a.data[b] = e;
    return $this;
}
function jl_AbstractStringBuilder741_append760($this, a) {
    return jl_AbstractStringBuilder741_append761($rt_nullCheck($this), a, 10);
}
function jl_AbstractStringBuilder741_toString15($this) {
    return jl_String3.$init167($this.buffer742, 0, $this.length743);
}
function jl_AbstractStringBuilder741_append761($this, a, b) {
    var c, d, e, f;
    c = 1;
    if ((a < 0)) {
        c = 0;
        a = ((-a) | 0);
    }
    block3: {
        if ((a >= b)) {
            d = 1;
            e = 1;
            f = a;
            while ((f > b)) {
                d = ((d * b) | 0);
                f = ((f / b) | 0);
                e = ((e + 1) | 0);
            }
            if ((c == 0)) {
                e = ((e + 1) | 0);
            }
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + e) | 0));
            if ((c == 0)) {
                f = $this.buffer742;
                e = $this.length743;
                $this.length743 = ((e + 1) | 0);
                f.data[e] = 45;
            }
            while (true) {
                if ((d <= 0)) {
                    break block3;
                }
                f = $this.buffer742;
                e = $this.length743;
                $this.length743 = ((e + 1) | 0);
                f.data[e] = jl_Character187_forDigit252(((a / d) | 0), b);
                a = (a % d);
                d = ((d / b) | 0);
            }
        }
        if ((c != 0)) {
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 1) | 0));
        } else {
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 2) | 0));
            b = $this.buffer742;
            f = $this.length743;
            $this.length743 = ((f + 1) | 0);
            b.data[f] = 45;
        }
        b = $this.buffer742;
        f = $this.length743;
        $this.length743 = ((f + 1) | 0);
        a = (((48 + a) | 0) & 65535);
        b.data[f] = a;
    }
    return $this;
}
function jl_AbstractStringBuilder741_append762($this, a) {
    var b, c;
    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 1) | 0));
    b = $this.buffer742;
    c = $this.length743;
    $this.length743 = ((c + 1) | 0);
    b.data[c] = a;
    return $this;
}
function jl_AbstractStringBuilder741_setLength728($this, a) {
    $this.length743 = a;
    return;
}
function jl_AbstractStringBuilder741_append763($this, a) {
    var b, c, d, e, f, g, h, i;
    if ((a != 0.0)) {
        if ((a != 0.0)) {
            if ((jl_Double340_isNaN764(a) == 0)) {
                if ((jl_Double340_isInfinite765(a) == 0)) {
                    b = 0;
                    c = 1;
                    if ((a < 0.0)) {
                        b = 1;
                        a = (-a);
                        c = ((c + 1) | 0);
                    }
                    d = 1;
                    if ((a < 1.0)) {
                        c = ((c + 1) | 0);
                        e = 256;
                        f = 0;
                        g = 1.0;
                        h = ((jl_AbstractStringBuilder741.negDoublePowersOfTen746.data.length - 1) | 0);
                        while ((h >= 0)) {
                            i = (f | e);
                            if (((i <= 308) && (((jl_AbstractStringBuilder741.negDoublePowersOfTen746.data[h] * g) * 10.0) > a))) {
                                g = (g * jl_AbstractStringBuilder741.negDoublePowersOfTen746.data[h]);
                                f = i;
                            }
                            e = (e >> 1);
                            h = ((h + -1) | 0);
                        }
                        h = ((-f) | 0);
                        a = Long_fromNumber((((a * 1.0E15) / g) + 0.5));
                    } else {
                        g = 256;
                        h = 0;
                        i = 1.0;
                        f = ((jl_AbstractStringBuilder741.doublePowersOfTen745.data.length - 1) | 0);
                        while ((f >= 0)) {
                            e = (h | g);
                            if (((e <= 308) && ((jl_AbstractStringBuilder741.doublePowersOfTen745.data[f] * i) < a))) {
                                i = (i * jl_AbstractStringBuilder741.doublePowersOfTen745.data[f]);
                                h = e;
                            }
                            g = (g >> 1);
                            f = ((f + -1) | 0);
                        }
                        a = Long_fromNumber((((a / i) * 1.0E15) + 0.5));
                    }
                    e = 16;
                    f = jl_AbstractStringBuilder741_trailingDecimalZeros752(a);
                    if ((f > 0)) {
                        e = ((e - f) | 0);
                    }
                    if (((h < 7) && (h >= -3))) {
                        if ((h >= 0)) {
                            d = ((h + 1) | 0);
                            e = jl_Math147_max463(e, ((d + 1) | 0));
                            h = 0;
                        } else if ((h < 0)) {
                            a = Long_div(a, jl_AbstractStringBuilder741.longPowersOfTen744.data[((-h) | 0)]);
                            e = ((e - h) | 0);
                            h = 0;
                        }
                    }
                    f = ((c + e) | 0);
                    if ((h != 0)) {
                        f = ((f + 2) | 0);
                        if ((!((h > -10) && (h < 10)))) {
                            f = ((f + 1) | 0);
                        }
                        if ((!((h > -100) && (h < 100)))) {
                            f = ((f + 1) | 0);
                        }
                    }
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + f) | 0));
                    if ((b != 0)) {
                        b = $this.buffer742;
                        f = $this.length743;
                        $this.length743 = ((f + 1) | 0);
                        b.data[f] = 45;
                    }
                    b = new Long(2764472320, 232830);
                    f = 0;
                    while ((f < e)) {
                        if ((Long_compare(b, Long_ZERO) <= 0)) {
                            c = 0;
                        } else {
                            c = Long_toNumber(Long_div(a, b));
                            a = Long_rem(a, b);
                        }
                        g = $this.buffer742;
                        i = $this.length743;
                        $this.length743 = ((i + 1) | 0);
                        g.data[i] = (((48 + c) | 0) & 65535);
                        d = ((d + -1) | 0);
                        if ((d == 0)) {
                            c = $this.buffer742;
                            g = $this.length743;
                            $this.length743 = ((g + 1) | 0);
                            c.data[g] = 46;
                        }
                        b = Long_div(b, Long_fromInt(10));
                        f = ((f + 1) | 0);
                    }
                    if ((h != 0)) {
                        a = $this.buffer742;
                        b = $this.length743;
                        $this.length743 = ((b + 1) | 0);
                        a.data[b] = 69;
                        if ((h < 0)) {
                            h = ((-h) | 0);
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = 45;
                        }
                        if ((h >= 100)) {
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = (((48 + ((h / 100) | 0)) | 0) & 65535);
                            h = (h % 100);
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = (((48 + ((h / 10) | 0)) | 0) & 65535);
                        } else if ((h >= 10)) {
                            a = $this.buffer742;
                            b = $this.length743;
                            $this.length743 = ((b + 1) | 0);
                            a.data[b] = (((48 + ((h / 10) | 0)) | 0) & 65535);
                        }
                        a = $this.buffer742;
                        b = $this.length743;
                        $this.length743 = ((b + 1) | 0);
                        a.data[b] = (((48 + (h % 10)) | 0) & 65535);
                    }
                    return $this;
                }
                if ((a <= 0.0)) {
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 9) | 0));
                    a = $this.buffer742;
                    b = $this.length743;
                    $this.length743 = ((b + 1) | 0);
                    a.data[b] = 45;
                } else {
                    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 8) | 0));
                }
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 73;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 110;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 102;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 105;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 110;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 105;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 116;
                a = $this.buffer742;
                b = $this.length743;
                $this.length743 = ((b + 1) | 0);
                a.data[b] = 121;
                return $this;
            }
            jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 3) | 0));
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 78;
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 97;
            a = $this.buffer742;
            b = $this.length743;
            $this.length743 = ((b + 1) | 0);
            a.data[b] = 78;
            return $this;
        }
        jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 4) | 0));
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 45;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 48;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 46;
        a = $this.buffer742;
        b = $this.length743;
        $this.length743 = ((b + 1) | 0);
        a.data[b] = 48;
        return $this;
    }
    jl_AbstractStringBuilder741_ensureCapacity756($this, (($this.length743 + 3) | 0));
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 48;
    a.data[b] = e;
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 46;
    a.data[b] = e;
    a = $this.buffer742;
    b = $this.length743;
    $this.length743 = ((b + 1) | 0);
    e = 48;
    a.data[b] = e;
    return $this;
}
function jl_AbstractStringBuilder741_append766($this, a) {
    if ((a === null)) {
        a = $rt_str("null");
    } else {
        a = $rt_nullCheck(a).toString15();
    }
    return jl_StringBuilder16_append767($rt_nullCheck($this), a);
}
function jl_AbstractStringBuilder741_append767($this, a) {
    return jl_StringBuilder16_insert758($rt_nullCheck($this), $this.length743, a);
}
function jl_AbstractStringBuilder741_getChars6($this, a, b, c, d) {
    var e, f, g;
    if ((a <= b)) {
        while ((a < b)) {
            e = ((d + 1) | 0);
            f = $this.buffer742;
            g = ((a + 1) | 0);
            c.data[d] = f.data[a];
            d = e;
            a = g;
        }
        return;
    }
    $rt_throw(jl_IndexOutOfBoundsException157.$init159($rt_str("Index out of bounds")));
}
jl_AbstractStringBuilder741.$init751 = function(a) {
    var result = new jl_AbstractStringBuilder741();
    result.$init751(a);
    return result;
}
jl_AbstractStringBuilder741.$init755 = function() {
    var result = new jl_AbstractStringBuilder741();
    result.$init755();
    return result;
}
$rt_virtualMethods(jl_AbstractStringBuilder741,
    "$init751", function(a) { jl_AbstractStringBuilder741_$init751(this, a); },
    "ensureCapacity756", function(a) { jl_AbstractStringBuilder741_ensureCapacity756(this, a); },
    "length5", function() { return jl_AbstractStringBuilder741_length5(this); },
    "append757", function(a) { return jl_AbstractStringBuilder741_append757(this, a); },
    "insert758", function(a, b) { return jl_AbstractStringBuilder741_insert758(this, a, b); },
    "append759", function(a) { return jl_AbstractStringBuilder741_append759(this, a); },
    "append760", function(a) { return jl_AbstractStringBuilder741_append760(this, a); },
    "toString15", function() { return jl_AbstractStringBuilder741_toString15(this); },
    "append761", function(a, b) { return jl_AbstractStringBuilder741_append761(this, a, b); },
    "append762", function(a) { return jl_AbstractStringBuilder741_append762(this, a); },
    "setLength728", function(a) { jl_AbstractStringBuilder741_setLength728(this, a); },
    "append763", function(a) { return jl_AbstractStringBuilder741_append763(this, a); },
    "$init755", function() { jl_AbstractStringBuilder741_$init755(this); },
    "append766", function(a) { return jl_AbstractStringBuilder741_append766(this, a); },
    "append767", function(a) { return jl_AbstractStringBuilder741_append767(this, a); },
    "getChars6", function(a, b, c, d) { jl_AbstractStringBuilder741_getChars6(this, a, b, c, d); });
function oadm_MinesModel$SquareType45() {
}
oadm_MinesModel$SquareType45.UNKNOWN64 = null;
oadm_MinesModel$SquareType45.DISCOVERED90 = null;
oadm_MinesModel$SquareType45.$VALUES768 = null;
oadm_MinesModel$SquareType45.EXPLOSION47 = null;
oadm_MinesModel$SquareType45.N_085 = null;
oadm_MinesModel$SquareType45.N_4769 = null;
oadm_MinesModel$SquareType45.N_3770 = null;
oadm_MinesModel$SquareType45.N_2771 = null;
oadm_MinesModel$SquareType45.N_1772 = null;
oadm_MinesModel$SquareType45.N_8563 = null;
oadm_MinesModel$SquareType45.N_7773 = null;
oadm_MinesModel$SquareType45.N_6774 = null;
oadm_MinesModel$SquareType45.N_5775 = null;
$rt_declClass(oadm_MinesModel$SquareType45, {
    name : "org.apidesign.demo.minesweeper.MinesModel$SquareType",
    enum : true,
    superclass : jl_Enum102,
    clinit : function() { oadm_MinesModel$SquareType45_$clinit(); } });
function oadm_MinesModel$SquareType45_$clinit() {
    oadm_MinesModel$SquareType45_$clinit = function(){};
    oadm_MinesModel$SquareType45_values562 = function() {
        return oadm_MinesModel$SquareType45.$VALUES768.clone12();
    }
    oadm_MinesModel$SquareType45_$clinit776 = function() {
        var a, b, c, d;
        oadm_MinesModel$SquareType45.N_085 = oadm_MinesModel$SquareType45.$init777($rt_str("N_0"), 0);
        oadm_MinesModel$SquareType45.N_1772 = oadm_MinesModel$SquareType45.$init777($rt_str("N_1"), 1);
        oadm_MinesModel$SquareType45.N_2771 = oadm_MinesModel$SquareType45.$init777($rt_str("N_2"), 2);
        oadm_MinesModel$SquareType45.N_3770 = oadm_MinesModel$SquareType45.$init777($rt_str("N_3"), 3);
        oadm_MinesModel$SquareType45.N_4769 = oadm_MinesModel$SquareType45.$init777($rt_str("N_4"), 4);
        oadm_MinesModel$SquareType45.N_5775 = oadm_MinesModel$SquareType45.$init777($rt_str("N_5"), 5);
        oadm_MinesModel$SquareType45.N_6774 = oadm_MinesModel$SquareType45.$init777($rt_str("N_6"), 6);
        oadm_MinesModel$SquareType45.N_7773 = oadm_MinesModel$SquareType45.$init777($rt_str("N_7"), 7);
        oadm_MinesModel$SquareType45.N_8563 = oadm_MinesModel$SquareType45.$init777($rt_str("N_8"), 8);
        oadm_MinesModel$SquareType45.UNKNOWN64 = oadm_MinesModel$SquareType45.$init777($rt_str("UNKNOWN"), 9);
        oadm_MinesModel$SquareType45.EXPLOSION47 = oadm_MinesModel$SquareType45.$init777($rt_str("EXPLOSION"), 10);
        oadm_MinesModel$SquareType45.DISCOVERED90 = oadm_MinesModel$SquareType45.$init777($rt_str("DISCOVERED"), 11);
        a = $rt_createArray(oadm_MinesModel$SquareType45, 12);
        b = 0;
        c = oadm_MinesModel$SquareType45.N_085;
        d = a.data;
        d[b] = c;
        d[1] = oadm_MinesModel$SquareType45.N_1772;
        d[2] = oadm_MinesModel$SquareType45.N_2771;
        d[3] = oadm_MinesModel$SquareType45.N_3770;
        d[4] = oadm_MinesModel$SquareType45.N_4769;
        d[5] = oadm_MinesModel$SquareType45.N_5775;
        d[6] = oadm_MinesModel$SquareType45.N_6774;
        d[7] = oadm_MinesModel$SquareType45.N_7773;
        d[8] = oadm_MinesModel$SquareType45.N_8563;
        d[9] = oadm_MinesModel$SquareType45.UNKNOWN64;
        d[10] = oadm_MinesModel$SquareType45.EXPLOSION47;
        d[11] = oadm_MinesModel$SquareType45.DISCOVERED90;
        oadm_MinesModel$SquareType45.$VALUES768 = a;
        return;
    }
    oadm_MinesModel$SquareType45_$init777 = function($this, a, b) {
        jl_Enum102_$init611($this, a, b);
        return;
    }
    oadm_MinesModel$SquareType45_$clinit776();
}
oadm_MinesModel$SquareType45.values562 = function() {
    return oadm_MinesModel$SquareType45_values562();
}
oadm_MinesModel$SquareType45.values = oadm_MinesModel$SquareType45.values562;
$rt_methodStubs(oadm_MinesModel$SquareType45_$clinit, ['oadm_MinesModel$SquareType45_values562', 'oadm_MinesModel$SquareType45_$clinit776', 'oadm_MinesModel$SquareType45_$init777']);
function oadm_MinesModel$SquareType45_moreBombsAround96($this) {
    var a, b;
    block1: {
        oadm_MinesModel$1100_$clinit();
        a = oadm_MinesModel$1100.$SwitchMap$org$apidesign$demo$minesweeper$MinesModel$SquareType101;
        b = $rt_nullCheck($this);
        switch (a.data[jl_Enum102_ordinal103(b)]) {
            case 1:
            case 2:
            case 3:
            case 5:
                break;
            case 4:
                break block1;
            default:
                break block1;
        }
        return $this;
    }
    return oadm_MinesModel$SquareType45_values562().data[((jl_Enum102_ordinal103(b) + 1) | 0)];
}
function oadm_MinesModel$SquareType45_isVisible83($this) {
    return jl_String3_startsWith174($rt_nullCheck(jl_Enum102_name614($rt_nullCheck($this))), $rt_str("N_"));
}
oadm_MinesModel$SquareType45.$init777 = function(a, b) {
    var result = new oadm_MinesModel$SquareType45();
    result.$init777(a, b);
    return result;
}
$rt_virtualMethods(oadm_MinesModel$SquareType45,
    "moreBombsAround96", function() { return oadm_MinesModel$SquareType45_moreBombsAround96(this); },
    "isVisible83", function() { return oadm_MinesModel$SquareType45_isVisible83(this); },
    "$init777", function(a, b) { oadm_MinesModel$SquareType45_$init777(this, a, b); });
function otcic_UTF8Charset385() {
}
$rt_declClass(otcic_UTF8Charset385, {
    name : "org.teavm.classlib.impl.charset.UTF8Charset",
    superclass : otcic_Charset382,
    clinit : function() { otcic_UTF8Charset385_$clinit(); } });
function otcic_UTF8Charset385_$clinit() {
    otcic_UTF8Charset385_$clinit = function(){};
    otcic_UTF8Charset385_$init386 = function($this) {
        otcic_Charset382_$init383($this);
        return;
    }
}
$rt_methodStubs(otcic_UTF8Charset385_$clinit, ['otcic_UTF8Charset385_$init386']);
function otcic_UTF8Charset385_encode729($this, a, b) {
    var c, d, e, f;
    while (true) {
        c = $rt_nullCheck(a);
        if ((otcic_CharBuffer287_end293(c) != 0)) {
            break;
        }
        d = $rt_nullCheck(b);
        if ((otcic_ByteBuffer615_available621(d) < 4)) {
            break;
        }
        e = otcic_CharBuffer287_get292(c);
        if ((e >= 128)) {
            if ((e >= 1024)) {
                if ((otcic_UTF16Helper169_isSurrogate559(e) != 0)) {
                    if ((otcic_UTF16Helper169_isHighSurrogate185(e) == 0)) {
                        otcic_ByteBuffer615_put623(d, 63);
                    } else {
                        f = otcic_CharBuffer287_get292(c);
                        if ((otcic_UTF16Helper169_isLowSurrogate186(e) != 0)) {
                            e = otcic_UTF16Helper169_buildCodePoint189(e, f);
                            otcic_ByteBuffer615_put623(d, ((240 | (e >> 18)) & 255));
                            otcic_ByteBuffer615_put623(d, ((128 | ((e >> 12) & 63)) & 255));
                            otcic_ByteBuffer615_put623(d, ((128 | ((e >> 6) & 63)) & 255));
                            otcic_ByteBuffer615_put623(d, ((128 | (e & 63)) & 255));
                        } else {
                            otcic_CharBuffer287_back294(c, 1);
                            otcic_ByteBuffer615_put623(d, 63);
                        }
                    }
                } else {
                    otcic_ByteBuffer615_put623(d, ((224 | (e >> 12)) & 255));
                    otcic_ByteBuffer615_put623(d, ((128 | ((e >> 6) & 63)) & 255));
                    otcic_ByteBuffer615_put623(d, ((128 | (e & 63)) & 255));
                }
            } else {
                otcic_ByteBuffer615_put623(d, ((192 | (e >> 6)) & 255));
                otcic_ByteBuffer615_put623(d, ((128 | (e & 63)) & 255));
            }
        } else {
            otcic_ByteBuffer615_put623(d, (e & 255));
        }
    }
    return;
}
otcic_UTF8Charset385.$init386 = function() {
    var result = new otcic_UTF8Charset385();
    result.$init386();
    return result;
}
$rt_virtualMethods(otcic_UTF8Charset385,
    "$init386", function() { otcic_UTF8Charset385_$init386(this); },
    "encode729", function(a, b) { otcic_UTF8Charset385_encode729(this, a, b); });
function jl_Double340() {
    this.value778 = 0.0;
}
jl_Double340.NaN779 = 0.0;
jl_Double340.TYPE780 = null;
$rt_declClass(jl_Double340, {
    name : "java.lang.Double",
    interfaces : [jl_Comparable163],
    superclass : jl_Number325,
    clinit : function() { jl_Double340_$clinit(); } });
function jl_Double340_$clinit() {
    jl_Double340_$clinit = function(){};
    jl_Double340_decimalExponent781 = function(a) {
        var b, c;
        if ((a >= 0)) {
            b = 10.0;
        } else {
            b = 0.1;
            a = ((-a) | 0);
        }
        c = 1.0;
        while ((a != 0)) {
            if (((a % 2) != 0)) {
                c = (c * b);
            }
            b = (b * b);
            a = ((a / 2) | 0);
        }
        return c;
    }
    jl_Double340_compare782 = function(a, b) {
        a = $rt_compare(a, a);
        if ((a <= 0)) {
            if ((a >= 0)) {
                a = 0;
            } else {
                a = -1;
            }
        } else {
            a = 1;
        }
        return a;
    }
    jl_Double340_isInfinite765 = function(a) {
        return (isFinite(a) ? 0 : 1);
    }
    jl_Double340_valueOf410 = function(a) {
        return jl_Double340_valueOf342(jl_Double340_parseDouble783(a));
    }
    jl_Double340_$clinit784 = function() {
        jl_Double340.NaN779 = NaN;
        jl_Double340.TYPE780 = $rt_cls($rt_doublecls());
        return;
    }
    jl_Double340_toString416 = function(a) {
        return jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append785($rt_nullCheck(jl_StringBuilder16.$init17()), a)));
    }
    jl_Double340_parseDouble783 = function(a) {
        var b, c, d, e, f, g, h, i;
        a = jl_String3_trim192($rt_nullCheck(a));
        b = 0;
        c = 0;
        d = $rt_nullCheck(a);
        if ((jl_String3_charAt176(d, c) == 45)) {
            c = ((c + 1) | 0);
            b = 1;
        } else if ((jl_String3_charAt176(d, c) == 43)) {
            c = ((c + 1) | 0);
        }
        a = jl_String3_charAt176(d, c);
        if (((a >= 48) && (a <= 57))) {
            e = Long_ZERO;
            a = 0;
            block5: {
                while (true) {
                    if ((jl_String3_charAt176(d, c) != 48)) {
                        break block5;
                    }
                    c = ((c + 1) | 0);
                    if ((c == jl_String3_length5(d))) {
                        break;
                    }
                }
                return 0.0;
            }
            block8: {
                while (true) {
                    if ((c >= jl_String3_length5(d))) {
                        break block8;
                    }
                    f = jl_String3_charAt176(d, c);
                    if ((f < 48)) {
                        break block8;
                    }
                    if ((f > 57)) {
                        break;
                    }
                    if ((Long_toNumber(e) >= 1.0E17)) {
                        a = ((a + 1) | 0);
                    } else {
                        e = Long_add(Long_mul(e, Long_fromInt(10)), Long_fromInt(((f - 48) | 0)));
                    }
                    c = ((c + 1) | 0);
                }
            }
            block13: {
                if (((c < jl_String3_length5(d)) && (jl_String3_charAt176(d, c) == 46))) {
                    c = ((c + 1) | 0);
                    f = 0;
                    block17: {
                        while (true) {
                            if ((c >= jl_String3_length5(d))) {
                                break block17;
                            }
                            g = jl_String3_charAt176(d, c);
                            if ((g < 48)) {
                                break block17;
                            }
                            if ((g > 57)) {
                                break;
                            }
                            if ((Long_toNumber(e) < 1.0E17)) {
                                e = Long_add(Long_mul(e, Long_fromInt(10)), Long_fromInt(((g - 48) | 0)));
                                a = ((a + -1) | 0);
                            }
                            c = ((c + 1) | 0);
                            f = 1;
                        }
                    }
                    if ((f == 0)) {
                        break block13;
                    }
                }
                block22: {
                    block23: {
                        if ((c < jl_String3_length5(d))) {
                            f = jl_String3_charAt176(d, c);
                            if (((f != 101) && (f != 69))) {
                                break block22;
                            }
                            c = ((c + 1) | 0);
                            f = 0;
                            if ((jl_String3_charAt176(d, c) == 45)) {
                                c = ((c + 1) | 0);
                                f = 1;
                            } else if ((jl_String3_charAt176(d, c) == 43)) {
                                c = ((c + 1) | 0);
                            }
                            g = 0;
                            h = 0;
                            block30: {
                                while (true) {
                                    if ((c >= jl_String3_length5(d))) {
                                        break block30;
                                    }
                                    i = jl_String3_charAt176(d, c);
                                    if ((i < 48)) {
                                        break block30;
                                    }
                                    if ((i > 57)) {
                                        break;
                                    }
                                    g = ((((10 * g) | 0) + ((i - 48) | 0)) | 0);
                                    h = 1;
                                    c = ((c + 1) | 0);
                                }
                            }
                            if ((h == 0)) {
                                break block23;
                            }
                            if ((f != 0)) {
                                g = ((-g) | 0);
                            }
                            a = ((a + g) | 0);
                        }
                        if (((a <= 308) && (!((a == 308) && (Long_compare(e, new Long(2133831477, 4185580)) > 0))))) {
                            if ((b != 0)) {
                                e = Long_neg(e);
                            }
                            return (Long_toNumber(e) * jl_Double340_decimalExponent781(a));
                        }
                        if ((b != 0)) {
                            a = -Infinity;
                        } else {
                            a = Infinity;
                        }
                        return a;
                    }
                    $rt_throw(jl_NumberFormatException137.$init138());
                }
                $rt_throw(jl_NumberFormatException137.$init138());
            }
            $rt_throw(jl_NumberFormatException137.$init138());
        }
        $rt_throw(jl_NumberFormatException137.$init138());
    }
    jl_Double340_valueOf342 = function(a) {
        return jl_Double340.$init786(a);
    }
    jl_Double340_$init786 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value778 = a;
        return;
    }
    jl_Double340_isNaN764 = function(a) {
        return (isNaN(a) ? 1 : 0 );
    }
    jl_Double340_$clinit784();
}
$rt_methodStubs(jl_Double340_$clinit, ['jl_Double340_decimalExponent781', 'jl_Double340_compare782', 'jl_Double340_isInfinite765', 'jl_Double340_valueOf410', 'jl_Double340_$clinit784', 'jl_Double340_toString416', 'jl_Double340_parseDouble783', 'jl_Double340_valueOf342', 'jl_Double340_$init786', 'jl_Double340_isNaN764']);
function jl_Double340_intValue87($this) {
    return ($this.value778 | 0);
}
function jl_Double340_floatValue344($this) {
    return $this.value778;
}
function jl_Double340_doubleValue341($this) {
    return $this.value778;
}
function jl_Double340_longValue332($this) {
    return Long_fromNumber($this.value778);
}
function jl_Double340_compareTo787($this, a) {
    return jl_Double340_compare782($this.value778, a.value778);
}
function jl_Double340_compareTo181($this, a) {
    return jl_Double340_compareTo787($rt_nullCheck($this), a);
}
jl_Double340.$init786 = function(a) {
    var result = new jl_Double340();
    result.$init786(a);
    return result;
}
$rt_virtualMethods(jl_Double340,
    "intValue87", function() { return jl_Double340_intValue87(this); },
    "floatValue344", function() { return jl_Double340_floatValue344(this); },
    "doubleValue341", function() { return jl_Double340_doubleValue341(this); },
    "$init786", function(a) { jl_Double340_$init786(this, a); },
    "longValue332", function() { return jl_Double340_longValue332(this); },
    "compareTo787", function(a) { return jl_Double340_compareTo787(this, a); },
    "compareTo181", function(a) { return jl_Double340_compareTo181(this, a); });
function oahcs_Contexts398() {
}
$rt_declClass(oahcs_Contexts398, {
    name : "org.apidesign.html.context.spi.Contexts",
    superclass : jl_Object7,
    clinit : function() { oahcs_Contexts398_$clinit(); } });
function oahcs_Contexts398_$clinit() {
    oahcs_Contexts398_$clinit = function(){};
    oahcs_Contexts398_fillInByProviders684 = function(a, b) {
        var c, d, e, f, g;
        c = 0;
        block1: {
            block2: {
                try {
                    d = jl_Class0_getClassLoader420($rt_nullCheck(a));
                } catch ($e) {
                    $je = $e.$javaException;
                    if ($je && $je instanceof jl_SecurityException314) {
                        a = $je;
                        break block2;
                    } else {
                        throw $e;
                    }
                }
                break block1;
            }
            d = null;
        }
        d = ju_ServiceLoader283_iterator107($rt_nullCheck(ju_ServiceLoader283_load511($rt_cls(oahcs_Contexts$Provider506), d)));
        while (true) {
            e = $rt_nullCheck(d);
            if ((ju_ServiceLoader$1279_hasNext109(e) == 0)) {
                break;
            }
            onhk_KO4J507_fillContext788($rt_nullCheck(ju_ServiceLoader$1279_next110(e)), b, a);
            c = 1;
        }
        block5: {
            block6: {
                block7: {
                    try {
                        d = ju_ServiceLoader283_iterator107($rt_nullCheck(ju_ServiceLoader283_load511($rt_cls(oahcs_Contexts$Provider506), jl_Class0_getClassLoader420($rt_nullCheck($rt_cls(oahcs_Contexts$Provider506))))));
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof jl_SecurityException314) {
                            f = $je;
                            break block7;
                        } else {
                            throw $e;
                        }
                    }
                    while (true) {
                        try {
                            e = $rt_nullCheck(d);
                            if ((ju_ServiceLoader$1279_hasNext109(e) == 0)) {
                                break;
                            }
                        } catch ($e) {
                            $je = $e.$javaException;
                            if ($je && $je instanceof jl_SecurityException314) {
                                f = $je;
                                break block7;
                            } else {
                                throw $e;
                            }
                        }
                        try {
                            onhk_KO4J507_fillContext788($rt_nullCheck(ju_ServiceLoader$1279_next110(e)), b, a);
                        } catch ($e) {
                            $je = $e.$javaException;
                            if ($je && $je instanceof jl_SecurityException314) {
                                f = $je;
                                break block7;
                            } else {
                                throw $e;
                            }
                        }
                    }
                    break block6;
                }
                if ((c == 0)) {
                    break block5;
                }
            }
            if ((c == 0)) {
                f = ju_ServiceLoader283_iterator107($rt_nullCheck(ju_ServiceLoader283_load509($rt_cls(oahcs_Contexts$Provider506))));
                while (true) {
                    g = $rt_nullCheck(f);
                    if ((ju_ServiceLoader$1279_hasNext109(g) == 0)) {
                        break;
                    }
                    onhk_KO4J507_fillContext788($rt_nullCheck(ju_ServiceLoader$1279_next110(g)), b, a);
                    c = 1;
                }
            }
            return c;
        }
        $rt_throw(f);
    }
    oahcs_Contexts398_newBuilder683 = function() {
        return oahcs_Contexts$Builder662.$init664();
    }
    oahcs_Contexts398_$init789 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    oahcs_Contexts398_find399 = function(a, b) {
        return onhci_CtxImpl665_find790(a, b);
    }
}
$rt_methodStubs(oahcs_Contexts398_$clinit, ['oahcs_Contexts398_fillInByProviders684', 'oahcs_Contexts398_newBuilder683', 'oahcs_Contexts398_$init789', 'oahcs_Contexts398_find399']);
oahcs_Contexts398.$init789 = function() {
    var result = new oahcs_Contexts398();
    result.$init789();
    return result;
}
$rt_virtualMethods(oahcs_Contexts398,
    "$init789", function() { oahcs_Contexts398_$init789(this); });
function jl_Class0() {
    this.componentTypeDirty791 = false;
    this.name2 = null;
    this.componentType792 = null;
}
$rt_declClass(jl_Class0, {
    name : "java.lang.Class",
    superclass : jl_Object7,
    clinit : function() { jl_Class0_$clinit(); } });
function jl_Class0_$clinit() {
    jl_Class0_$clinit = function(){};
    jl_Class0_forNameImpl793 = function(a) {
        switch ($rt_ustr(a)) {
            case "org.apidesign.html.json.spi.PropertyBinding$Impl": oahjs_PropertyBinding$Impl26.$clinit(); return $rt_cls(oahjs_PropertyBinding$Impl26);
            case "org.apidesign.demo.minesweeper.MinesModel": oadm_MinesModel43.$clinit(); return $rt_cls(oadm_MinesModel43);
            case "java.lang.NumberFormatException": jl_NumberFormatException137.$clinit(); return $rt_cls(jl_NumberFormatException137);
            case "org.apidesign.demo.minesweeper.MinesModel$RowModel": oadm_MinesModel$RowModel140.$clinit(); return $rt_cls(oadm_MinesModel$RowModel140);
            case "java.util.Arrays": ju_Arrays142.$clinit(); return $rt_cls(ju_Arrays142);
            case "org.apidesign.html.json.spi.Technology": oahjs_Technology156.$clinit(); return $rt_cls(oahjs_Technology156);
            case "java.lang.ArrayIndexOutOfBoundsException": jl_ArrayIndexOutOfBoundsException160.$clinit(); return $rt_cls(jl_ArrayIndexOutOfBoundsException160);
            case "java.lang.String": jl_String3.$clinit(); return $rt_cls(jl_String3);
            case "java.util.Collections": ju_Collections195.$clinit(); return $rt_cls(ju_Collections195);
            case "java.lang.IncompatibleClassChangeError": jl_IncompatibleClassChangeError212.$clinit(); return $rt_cls(jl_IncompatibleClassChangeError212);
            case "java.io.Closeable": ji_Closeable215.$clinit(); return $rt_cls(ji_Closeable215);
            case "java.lang.NoSuchFieldError": jl_NoSuchFieldError216.$clinit(); return $rt_cls(jl_NoSuchFieldError216);
            case "java.util.AbstractList": ju_AbstractList66.$clinit(); return $rt_cls(ju_AbstractList66);
            case "org.apidesign.html.json.spi.Technology$BatchInit": oahjs_Technology$BatchInit226.$clinit(); return $rt_cls(oahjs_Technology$BatchInit226);
            case "org.apidesign.demo.minesweeper.MainBrwsr": oadm_MainBrwsr227.$clinit(); return $rt_cls(oadm_MainBrwsr227);
            case "java.lang.Thread": jl_Thread233.$clinit(); return $rt_cls(jl_Thread233);
            case "java.lang.Character": jl_Character187.$clinit(); return $rt_cls(jl_Character187);
            case "java.lang.Throwable": jl_Throwable111.$clinit(); return $rt_cls(jl_Throwable111);
            case "java.util.MapEntry": ju_MapEntry263.$clinit(); return $rt_cls(ju_MapEntry263);
            case "java.lang.ClassCastException": jl_ClassCastException267.$clinit(); return $rt_cls(jl_ClassCastException267);
            case "org.netbeans.html.context.impl.CtxImpl$Bind": onhci_CtxImpl$Bind270.$clinit(); return $rt_cls(onhci_CtxImpl$Bind270);
            case "java.util.ServiceLoader$1": ju_ServiceLoader$1279.$clinit(); return $rt_cls(ju_ServiceLoader$1279);
            case "org.teavm.classlib.impl.charset.CharBuffer": otcic_CharBuffer287.$clinit(); return $rt_cls(otcic_CharBuffer287);
            case "java.lang.AutoCloseable": jl_AutoCloseable214.$clinit(); return $rt_cls(jl_AutoCloseable214);
            case "java.lang.Cloneable": jl_Cloneable262.$clinit(); return $rt_cls(jl_Cloneable262);
            case "java.lang.reflect.Array": jlr_Array150.$clinit(); return $rt_cls(jlr_Array150);
            case "java.util.AbstractMap": ju_AbstractMap305.$clinit(); return $rt_cls(ju_AbstractMap305);
            case "org.apidesign.demo.minesweeper.Mines$Html4JavaType": oadm_Mines$Html4JavaType356.$clinit(); return $rt_cls(oadm_Mines$Html4JavaType356);
            case "org.apidesign.demo.minesweeper.Mines$1": oadm_Mines$1375.$clinit(); return $rt_cls(oadm_Mines$1375);
            case "org.teavm.classlib.impl.charset.Charset": otcic_Charset382.$clinit(); return $rt_cls(otcic_Charset382);
            case "java.lang.IndexOutOfBoundsException": jl_IndexOutOfBoundsException157.$clinit(); return $rt_cls(jl_IndexOutOfBoundsException157);
            case "org.netbeans.html.json.impl.JSON": onhji_JSON40.$clinit(); return $rt_cls(onhji_JSON40);
            case "java.lang.NegativeArraySizeException": jl_NegativeArraySizeException300.$clinit(); return $rt_cls(jl_NegativeArraySizeException300);
            case "java.lang.Float": jl_Float343.$clinit(); return $rt_cls(jl_Float343);
            case "java.lang.SecurityException": jl_SecurityException314.$clinit(); return $rt_cls(jl_SecurityException314);
            case "java.lang.CharSequence": jl_CharSequence162.$clinit(); return $rt_cls(jl_CharSequence162);
            case "org.apidesign.html.json.spi.Proto": oahjs_Proto321.$clinit(); return $rt_cls(oahjs_Proto321);
            case "java.lang.RuntimeException": jl_RuntimeException131.$clinit(); return $rt_cls(jl_RuntimeException131);
            case "java.lang.Math": jl_Math147.$clinit(); return $rt_cls(jl_Math147);
            case "org.apidesign.html.boot.spi.Fn": oahbs_Fn466.$clinit(); return $rt_cls(oahbs_Fn466);
            case "java.io.Flushable": ji_Flushable477.$clinit(); return $rt_cls(ji_Flushable477);
            case "java.util.Map": ju_Map304.$clinit(); return $rt_cls(ju_Map304);
            case "java.lang.NullPointerException": jl_NullPointerException8.$clinit(); return $rt_cls(jl_NullPointerException8);
            case "org.apidesign.demo.minesweeper.Mines": oadm_Mines49.$clinit(); return $rt_cls(oadm_Mines49);
            case "java.lang.ReflectiveOperationException": jl_ReflectiveOperationException493.$clinit(); return $rt_cls(jl_ReflectiveOperationException493);
            case "org.netbeans.html.json.impl.PropertyBindingAccessor": onhji_PropertyBindingAccessor404.$clinit(); return $rt_cls(onhji_PropertyBindingAccessor404);
            case "java.util.ServiceLoader": ju_ServiceLoader283.$clinit(); return $rt_cls(ju_ServiceLoader283);
            case "java.lang.Number": jl_Number325.$clinit(); return $rt_cls(jl_Number325);
            case "java.lang.ClassLoader": jl_ClassLoader512.$clinit(); return $rt_cls(jl_ClassLoader512);
            case "org.apidesign.html.json.spi.FunctionBinding": oahjs_FunctionBinding448.$clinit(); return $rt_cls(oahjs_FunctionBinding448);
            case "java.util.List": ju_List206.$clinit(); return $rt_cls(ju_List206);
            case "java.lang.ConsoleOutputStream_stdout": jl_ConsoleOutputStream_stdout528.$clinit(); return $rt_cls(jl_ConsoleOutputStream_stdout528);
            case "java.util.ArrayList": ju_ArrayList54.$clinit(); return $rt_cls(ju_ArrayList54);
            case "java.lang.Long": jl_Long331.$clinit(); return $rt_cls(jl_Long331);
            case "java.lang.LinkageError": jl_LinkageError210.$clinit(); return $rt_cls(jl_LinkageError210);
            case "java.io.IOException": ji_IOException538.$clinit(); return $rt_cls(ji_IOException538);
            case "java.util.logging.LogRecord": jul_LogRecord540.$clinit(); return $rt_cls(jul_LogRecord540);
            case "org.apidesign.demo.minesweeper.Mines$2": oadm_Mines$2491.$clinit(); return $rt_cls(oadm_Mines$2491);
            case "java.util.Comparator": ju_Comparator553.$clinit(); return $rt_cls(ju_Comparator553);
            case "java.util.HashMap$Entry": ju_HashMap$Entry554.$clinit(); return $rt_cls(ju_HashMap$Entry554);
            case "org.teavm.classlib.impl.charset.UTF16Helper": otcic_UTF16Helper169.$clinit(); return $rt_cls(otcic_UTF16Helper169);
            case "java.util.Random": ju_Random68.$clinit(); return $rt_cls(ju_Random68);
            case "java.lang.Iterable": jl_Iterable218.$clinit(); return $rt_cls(jl_Iterable218);
            case "java.lang.UnsupportedOperationException": jl_UnsupportedOperationException222.$clinit(); return $rt_cls(jl_UnsupportedOperationException222);
            case "org.apidesign.demo.minesweeper.MinesModel$1": oadm_MinesModel$1100.$clinit(); return $rt_cls(oadm_MinesModel$1100);
            case "java.util.AbstractList$1": ju_AbstractList$1108.$clinit(); return $rt_cls(ju_AbstractList$1108);
            case "org.netbeans.html.ko4j.Knockout": onhk_Knockout571.$clinit(); return $rt_cls(onhk_Knockout571);
            case "java.lang.Error": jl_Error208.$clinit(); return $rt_cls(jl_Error208);
            case "java.lang.SystemClassLoader": jl_SystemClassLoader517.$clinit(); return $rt_cls(jl_SystemClassLoader517);
            case "java.util.logging.Level": jul_Level581.$clinit(); return $rt_cls(jul_Level581);
            case "java.lang.ConsoleOutputStream_stderr": jl_ConsoleOutputStream_stderr595.$clinit(); return $rt_cls(jl_ConsoleOutputStream_stderr595);
            case "java.lang.IllegalArgumentException": jl_IllegalArgumentException134.$clinit(); return $rt_cls(jl_IllegalArgumentException134);
            case "net.java.html.BrwsrCtx$1": njh_BrwsrCtx$1603.$clinit(); return $rt_cls(njh_BrwsrCtx$1603);
            case "java.lang.Enum": jl_Enum102.$clinit(); return $rt_cls(jl_Enum102);
            case "org.teavm.classlib.impl.charset.ByteBuffer": otcic_ByteBuffer615.$clinit(); return $rt_cls(otcic_ByteBuffer615);
            case "org.netbeans.html.json.impl.Bindings": onhji_Bindings406.$clinit(); return $rt_cls(onhji_Bindings406);
            case "org.apidesign.html.json.spi.Transfer": oahjs_Transfer639.$clinit(); return $rt_cls(oahjs_Transfer639);
            case "java.util.concurrent.Executor": juc_Executor640.$clinit(); return $rt_cls(juc_Executor640);
            case "org.apidesign.html.context.spi.Contexts$Provider": oahcs_Contexts$Provider506.$clinit(); return $rt_cls(oahcs_Contexts$Provider506);
            case "java.lang.IllegalStateException": jl_IllegalStateException229.$clinit(); return $rt_cls(jl_IllegalStateException229);
            case "java.util.Map$Entry": ju_Map$Entry261.$clinit(); return $rt_cls(ju_Map$Entry261);
            case "java.util.NoSuchElementException": ju_NoSuchElementException285.$clinit(); return $rt_cls(ju_NoSuchElementException285);
            case "org.netbeans.html.context.impl.CtxAccssr": onhci_CtxAccssr597.$clinit(); return $rt_cls(onhci_CtxAccssr597);
            case "java.lang.Boolean": jl_Boolean327.$clinit(); return $rt_cls(jl_Boolean327);
            case "org.teavm.html4j.JavaScriptConv": oth_JavaScriptConv573.$clinit(); return $rt_cls(oth_JavaScriptConv573);
            case "java.util.Collection": ju_Collection203.$clinit(); return $rt_cls(ju_Collection203);
            case "org.netbeans.html.json.impl.JSON$EmptyTech": onhji_JSON$EmptyTech400.$clinit(); return $rt_cls(onhji_JSON$EmptyTech400);
            case "java.lang.InstantiationException": jl_InstantiationException654.$clinit(); return $rt_cls(jl_InstantiationException654);
            case "org.netbeans.html.ko4j.FXContext$1Wrap": onhk_FXContext$1Wrap656.$clinit(); return $rt_cls(onhk_FXContext$1Wrap656);
            case "org.apidesign.html.context.spi.Contexts$Builder": oahcs_Contexts$Builder662.$clinit(); return $rt_cls(oahcs_Contexts$Builder662);
            case "java.lang.ThreadLocal": jl_ThreadLocal670.$clinit(); return $rt_cls(jl_ThreadLocal670);
            case "org.apidesign.html.json.spi.Proto$Type": oahjs_Proto$Type36.$clinit(); return $rt_cls(oahjs_Proto$Type36);
            case "net.java.html.BrwsrCtx": njh_BrwsrCtx488.$clinit(); return $rt_cls(njh_BrwsrCtx488);
            case "org.apidesign.demo.minesweeper.MinesModel$SquareModel": oadm_MinesModel$SquareModel687.$clinit(); return $rt_cls(oadm_MinesModel$SquareModel687);
            case "java.util.logging.Logger": jul_Logger681.$clinit(); return $rt_cls(jul_Logger681);
            case "org.teavm.classlib.impl.unicode.UnicodeHelper": otciu_UnicodeHelper256.$clinit(); return $rt_cls(otciu_UnicodeHelper256);
            case "java.util.Collections$8": ju_Collections$8144.$clinit(); return $rt_cls(ju_Collections$8144);
            case "org.apidesign.demo.minesweeper.Row$Html4JavaType": oadm_Row$Html4JavaType708.$clinit(); return $rt_cls(oadm_Row$Html4JavaType708);
            case "java.io.PrintStream": ji_PrintStream122.$clinit(); return $rt_cls(ji_PrintStream122);
            case "java.lang.StringIndexOutOfBoundsException": jl_StringIndexOutOfBoundsException177.$clinit(); return $rt_cls(jl_StringIndexOutOfBoundsException177);
            case "java.lang.Appendable": jl_Appendable731.$clinit(); return $rt_cls(jl_Appendable731);
            case "org.apidesign.html.json.spi.PropertyBinding": oahjs_PropertyBinding21.$clinit(); return $rt_cls(oahjs_PropertyBinding21);
            case "java.lang.Byte": jl_Byte337.$clinit(); return $rt_cls(jl_Byte337);
            case "java.lang.System": jl_System125.$clinit(); return $rt_cls(jl_System125);
            case "org.apidesign.html.json.spi.WSTransfer": oahjs_WSTransfer650.$clinit(); return $rt_cls(oahjs_WSTransfer650);
            case "java.io.FilterOutputStream": ji_FilterOutputStream716.$clinit(); return $rt_cls(ji_FilterOutputStream716);
            case "java.lang.AbstractStringBuilder": jl_AbstractStringBuilder741.$clinit(); return $rt_cls(jl_AbstractStringBuilder741);
            case "java.lang.Runnable": jl_Runnable232.$clinit(); return $rt_cls(jl_Runnable232);
            case "java.util.AbstractCollection": ju_AbstractCollection204.$clinit(); return $rt_cls(ju_AbstractCollection204);
            case "org.apidesign.demo.minesweeper.MinesModel$SquareType": oadm_MinesModel$SquareType45.$clinit(); return $rt_cls(oadm_MinesModel$SquareType45);
            case "java.io.OutputStream": ji_OutputStream524.$clinit(); return $rt_cls(ji_OutputStream524);
            case "org.teavm.classlib.impl.charset.UTF8Charset": otcic_UTF8Charset385.$clinit(); return $rt_cls(otcic_UTF8Charset385);
            case "java.util.Iterator": ju_Iterator278.$clinit(); return $rt_cls(ju_Iterator278);
            case "java.lang.Double": jl_Double340.$clinit(); return $rt_cls(jl_Double340);
            case "org.apidesign.html.context.spi.Contexts": oahcs_Contexts398.$clinit(); return $rt_cls(oahcs_Contexts398);
            case "java.lang.Class": jl_Class0.$clinit(); return $rt_cls(jl_Class0);
            case "org.netbeans.html.ko4j.FXContext$TrueFn": onhk_FXContext$TrueFn794.$clinit(); return $rt_cls(onhk_FXContext$TrueFn794);
            case "java.lang.Void": jl_Void298.$clinit(); return $rt_cls(jl_Void298);
            case "org.apidesign.html.json.spi.PropertyBinding$1": oahjs_PropertyBinding$123.$clinit(); return $rt_cls(oahjs_PropertyBinding$123);
            case "org.netbeans.html.ko4j.KO4J": onhk_KO4J507.$clinit(); return $rt_cls(onhk_KO4J507);
            case "java.lang.Comparable": jl_Comparable163.$clinit(); return $rt_cls(jl_Comparable163);
            case "java.lang.Short": jl_Short334.$clinit(); return $rt_cls(jl_Short334);
            case "org.apidesign.demo.minesweeper.Square$Html4JavaType": oadm_Square$Html4JavaType795.$clinit(); return $rt_cls(oadm_Square$Html4JavaType795);
            case "java.util.HashMap": ju_HashMap394.$clinit(); return $rt_cls(ju_HashMap394);
            case "org.netbeans.html.ko4j.FXContext": onhk_FXContext636.$clinit(); return $rt_cls(onhk_FXContext636);
            case "java.lang.ClassNotFoundException": jl_ClassNotFoundException796.$clinit(); return $rt_cls(jl_ClassNotFoundException796);
            case "java.io.Serializable": ji_Serializable164.$clinit(); return $rt_cls(ji_Serializable164);
            case "java.lang.Integer": jl_Integer81.$clinit(); return $rt_cls(jl_Integer81);
            case "java.lang.Object": jl_Object7.$clinit(); return $rt_cls(jl_Object7);
            case "org.netbeans.html.context.impl.CtxImpl": onhci_CtxImpl665.$clinit(); return $rt_cls(onhci_CtxImpl665);
            case "org.apidesign.demo.minesweeper.Row": oadm_Row56.$clinit(); return $rt_cls(oadm_Row56);
            case "org.apidesign.html.json.spi.FunctionBinding$Impl": oahjs_FunctionBinding$Impl522.$clinit(); return $rt_cls(oahjs_FunctionBinding$Impl522);
            case "java.lang.StringBuilder": jl_StringBuilder16.$clinit(); return $rt_cls(jl_StringBuilder16);
            case "org.apidesign.demo.minesweeper.Square": oadm_Square62.$clinit(); return $rt_cls(oadm_Square62);
            case "org.apidesign.html.boot.spi.Fn$Presenter": oahbs_Fn$Presenter797.$clinit(); return $rt_cls(oahbs_Fn$Presenter797);
            case "java.util.Collections$6": ju_Collections$6201.$clinit(); return $rt_cls(ju_Collections$6201);
            case "org.netbeans.html.json.impl.JSONList": onhji_JSONList76.$clinit(); return $rt_cls(onhji_JSONList76);
            case "java.lang.AssertionError": jl_AssertionError316.$clinit(); return $rt_cls(jl_AssertionError316);
            case "org.netbeans.html.boot.impl.FnContext": onhbi_FnContext473.$clinit(); return $rt_cls(onhbi_FnContext473);
            case "java.util.ConcurrentModificationException": ju_ConcurrentModificationException569.$clinit(); return $rt_cls(ju_ConcurrentModificationException569);
            case "java.lang.Exception": jl_Exception127.$clinit(); return $rt_cls(jl_Exception127);
            case "org.apidesign.demo.minesweeper.MinesModel$GameState": oadm_MinesModel$GameState73.$clinit(); return $rt_cls(oadm_MinesModel$GameState73);
            default: return null;
        }
    }
    jl_Class0_forName421 = function(a, b, c) {
        return jl_Class0_forName798(a);
    }
    jl_Class0_createNew1 = function() {
        return jl_Class0.$init799();
    }
    jl_Class0_$init799 = function($this) {
        jl_Object7_$init10($this);
        $this.componentTypeDirty791 = 1;
        return;
    }
    jl_Class0_forName798 = function(a) {
        a = jl_Class0_forNameImpl793(a);
        if ((a !== null)) {
            return a;
        }
        $rt_throw(jl_ClassNotFoundException796.$init800());
    }
}
$rt_methodStubs(jl_Class0_$clinit, ['jl_Class0_forNameImpl793', 'jl_Class0_forName421', 'jl_Class0_createNew1', 'jl_Class0_$init799', 'jl_Class0_forName798']);
function jl_Class0_cast346($this, a) {
    var b;
    block1: {
        if ((a !== null)) {
            b = $rt_nullCheck(a);
            if (($rt_isAssignable($rt_cls(b.constructor).$data, $rt_nullCheck($this).$data) == 0)) {
                break block1;
            }
        }
        return a;
    }
    $rt_throw(jl_ClassCastException267.$init269(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), jl_Class0_getName20($rt_nullCheck($rt_cls(b.constructor))))), $rt_str(" is not subtype of "))), $this.name2)))));
}
function jl_Class0_getName20($this) {
    return jl_String3.$init173($this.name2);
}
function jl_Class0_getClassLoader420($this) {
    return jl_ClassLoader512_getSystemClassLoader520();
}
function jl_Class0_getComponentType152($this) {
    if (($this.componentTypeDirty791 != 0)) {
        $this.componentType792 = jl_Class0_getComponentType0801($this);
        $this.componentTypeDirty791 = 0;
    }
    return $this.componentType792;
}
function jl_Class0_getComponentType0801($this) {
    var item = $this.$data.$meta.item;
    return item != null ? $rt_cls(item) : null;
}
function jl_Class0_desiredAssertionStatus320($this) {
    return 1;
}
function jl_Class0_newInstance422($this) {
    if (!jl_Class0.$$constructors$$) {
        jl_Class0.$$constructors$$ = true;
        oadm_MinesModel43.$$constructor$$ = oadm_MinesModel43_$init97;
        jl_NumberFormatException137.$$constructor$$ = jl_NumberFormatException137_$init138;
        oadm_MinesModel$RowModel140.$$constructor$$ = oadm_MinesModel$RowModel140_$init141;
        ju_Arrays142.$$constructor$$ = ju_Arrays142_$init155;
        jl_ArrayIndexOutOfBoundsException160.$$constructor$$ = jl_ArrayIndexOutOfBoundsException160_$init161;
        jl_String3.$$constructor$$ = jl_String3_$init172;
        ju_Collections195.$$constructor$$ = ju_Collections195_$init199;
        jl_IncompatibleClassChangeError212.$$constructor$$ = jl_IncompatibleClassChangeError212_$init213;
        jl_NoSuchFieldError216.$$constructor$$ = jl_NoSuchFieldError216_$init217;
        ju_AbstractList66.$$constructor$$ = ju_AbstractList66_$init221;
        oadm_MainBrwsr227.$$constructor$$ = oadm_MainBrwsr227_$init231;
        jl_Thread233.$$constructor$$ = jl_Thread233_$init240;
        jl_Throwable111.$$constructor$$ = jl_Throwable111_$init118;
        jl_ClassCastException267.$$constructor$$ = jl_ClassCastException267_$init268;
        jlr_Array150.$$constructor$$ = jlr_Array150_$init302;
        ju_AbstractMap305.$$constructor$$ = ju_AbstractMap305_$init306;
        oadm_Mines$Html4JavaType356.$$constructor$$ = oadm_Mines$Html4JavaType356_$init357;
        otcic_Charset382.$$constructor$$ = otcic_Charset382_$init383;
        jl_IndexOutOfBoundsException157.$$constructor$$ = jl_IndexOutOfBoundsException157_$init158;
        onhji_JSON40.$$constructor$$ = onhji_JSON40_$init415;
        jl_NegativeArraySizeException300.$$constructor$$ = jl_NegativeArraySizeException300_$init301;
        jl_SecurityException314.$$constructor$$ = jl_SecurityException314_$init435;
        jl_RuntimeException131.$$constructor$$ = jl_RuntimeException131_$init132;
        jl_Math147.$$constructor$$ = jl_Math147_$init465;
        oahbs_Fn466.$$constructor$$ = oahbs_Fn466_$init471;
        jl_NullPointerException8.$$constructor$$ = jl_NullPointerException8_$init9;
        oadm_Mines49.$$constructor$$ = oadm_Mines49_$init93;
        jl_ReflectiveOperationException493.$$constructor$$ = jl_ReflectiveOperationException493_$init494;
        onhji_PropertyBindingAccessor404.$$constructor$$ = onhji_PropertyBindingAccessor404_$init499;
        jl_Number325.$$constructor$$ = jl_Number325_$init423;
        jl_ClassLoader512.$$constructor$$ = jl_ClassLoader512_$init519;
        oahjs_FunctionBinding448.$$constructor$$ = oahjs_FunctionBinding448_$init521;
        jl_ConsoleOutputStream_stdout528.$$constructor$$ = jl_ConsoleOutputStream_stdout528_$init529;
        ju_ArrayList54.$$constructor$$ = ju_ArrayList54_$init80;
        jl_LinkageError210.$$constructor$$ = jl_LinkageError210_$init211;
        ji_IOException538.$$constructor$$ = ji_IOException538_$init539;
        otcic_UTF16Helper169.$$constructor$$ = otcic_UTF16Helper169_$init558;
        ju_Random68.$$constructor$$ = ju_Random68_$init69;
        jl_UnsupportedOperationException222.$$constructor$$ = jl_UnsupportedOperationException222_$init223;
        onhk_Knockout571.$$constructor$$ = onhk_Knockout571_$init578;
        jl_Error208.$$constructor$$ = jl_Error208_$init209;
        jl_SystemClassLoader517.$$constructor$$ = jl_SystemClassLoader517_$init518;
        jl_ConsoleOutputStream_stderr595.$$constructor$$ = jl_ConsoleOutputStream_stderr595_$init596;
        jl_IllegalArgumentException134.$$constructor$$ = jl_IllegalArgumentException134_$init135;
        njh_BrwsrCtx$1603.$$constructor$$ = njh_BrwsrCtx$1603_$init604;
        jl_IllegalStateException229.$$constructor$$ = jl_IllegalStateException229_$init452;
        ju_NoSuchElementException285.$$constructor$$ = ju_NoSuchElementException285_$init286;
        onhci_CtxAccssr597.$$constructor$$ = onhci_CtxAccssr597_$init601;
        oth_JavaScriptConv573.$$constructor$$ = oth_JavaScriptConv573_$init649;
        onhji_JSON$EmptyTech400.$$constructor$$ = onhji_JSON$EmptyTech400_$init653;
        jl_InstantiationException654.$$constructor$$ = jl_InstantiationException654_$init655;
        oahcs_Contexts$Builder662.$$constructor$$ = oahcs_Contexts$Builder662_$init664;
        jl_ThreadLocal670.$$constructor$$ = jl_ThreadLocal670_$init673;
        oadm_MinesModel$SquareModel687.$$constructor$$ = oadm_MinesModel$SquareModel687_$init688;
        otciu_UnicodeHelper256.$$constructor$$ = otciu_UnicodeHelper256_$init706;
        ju_Collections$8144.$$constructor$$ = ju_Collections$8144_$init198;
        oadm_Row$Html4JavaType708.$$constructor$$ = oadm_Row$Html4JavaType708_$init709;
        jl_StringIndexOutOfBoundsException177.$$constructor$$ = jl_StringIndexOutOfBoundsException177_$init178;
        oahjs_PropertyBinding21.$$constructor$$ = oahjs_PropertyBinding21_$init25;
        jl_System125.$$constructor$$ = jl_System125_$init740;
        jl_AbstractStringBuilder741.$$constructor$$ = jl_AbstractStringBuilder741_$init755;
        ju_AbstractCollection204.$$constructor$$ = ju_AbstractCollection204_$init219;
        ji_OutputStream524.$$constructor$$ = ji_OutputStream524_$init525;
        otcic_UTF8Charset385.$$constructor$$ = otcic_UTF8Charset385_$init386;
        oahcs_Contexts398.$$constructor$$ = oahcs_Contexts398_$init789;
        jl_Class0.$$constructor$$ = jl_Class0_$init799;
        onhk_FXContext$TrueFn794.$$constructor$$ = onhk_FXContext$TrueFn794_$init802;
        jl_Void298.$$constructor$$ = jl_Void298_$init803;
        oahjs_PropertyBinding$123.$$constructor$$ = oahjs_PropertyBinding$123_$init24;
        onhk_KO4J507.$$constructor$$ = onhk_KO4J507_$init508;
        oadm_Square$Html4JavaType795.$$constructor$$ = oadm_Square$Html4JavaType795_$init804;
        ju_HashMap394.$$constructor$$ = ju_HashMap394_$init412;
        jl_ClassNotFoundException796.$$constructor$$ = jl_ClassNotFoundException796_$init800;
        jl_Object7.$$constructor$$ = jl_Object7_$init10;
        onhci_CtxImpl665.$$constructor$$ = onhci_CtxImpl665_$init666;
        oadm_Row56.$$constructor$$ = oadm_Row56_$init805;
        jl_StringBuilder16.$$constructor$$ = jl_StringBuilder16_$init17;
        oadm_Square62.$$constructor$$ = oadm_Square62_$init806;
        jl_AssertionError316.$$constructor$$ = jl_AssertionError316_$init317;
        ju_ConcurrentModificationException569.$$constructor$$ = ju_ConcurrentModificationException569_$init570;
        jl_Exception127.$$constructor$$ = jl_Exception127_$init128;
    }
    var cls = $this.$data;
    var ctor = cls.$$constructor$$;
    if (!ctor) {
        var ex = new jl_InstantiationException654();
        jl_InstantiationException654_$init655(ex);
        $rt_throw(ex);
    }
    var instance = new cls();
    ctor(instance);
    return instance;
}
function jl_Class0_getDeclaringClass315($this) {
    if (!jl_Class0.$$owners$$) {
        jl_Class0.$$owners$$ = true;
        oahjs_PropertyBinding$Impl26.$$owner$$ = oahjs_PropertyBinding21;
        oadm_MinesModel43.$$owner$$ = null;
        jl_NumberFormatException137.$$owner$$ = null;
        oadm_MinesModel$RowModel140.$$owner$$ = oadm_MinesModel43;
        ju_Arrays142.$$owner$$ = null;
        oahjs_Technology156.$$owner$$ = null;
        jl_ArrayIndexOutOfBoundsException160.$$owner$$ = null;
        jl_String3.$$owner$$ = null;
        ju_Collections195.$$owner$$ = null;
        jl_IncompatibleClassChangeError212.$$owner$$ = null;
        ji_Closeable215.$$owner$$ = null;
        jl_NoSuchFieldError216.$$owner$$ = null;
        ju_AbstractList66.$$owner$$ = null;
        oahjs_Technology$BatchInit226.$$owner$$ = oahjs_Technology156;
        oadm_MainBrwsr227.$$owner$$ = null;
        jl_Thread233.$$owner$$ = null;
        jl_Character187.$$owner$$ = null;
        jl_Throwable111.$$owner$$ = null;
        ju_MapEntry263.$$owner$$ = null;
        jl_ClassCastException267.$$owner$$ = null;
        onhci_CtxImpl$Bind270.$$owner$$ = onhci_CtxImpl665;
        ju_ServiceLoader$1279.$$owner$$ = ju_ServiceLoader283;
        otcic_CharBuffer287.$$owner$$ = null;
        jl_AutoCloseable214.$$owner$$ = null;
        jl_Cloneable262.$$owner$$ = null;
        jlr_Array150.$$owner$$ = null;
        ju_AbstractMap305.$$owner$$ = null;
        oadm_Mines$Html4JavaType356.$$owner$$ = oadm_Mines49;
        oadm_Mines$1375.$$owner$$ = oadm_Mines49;
        otcic_Charset382.$$owner$$ = null;
        jl_IndexOutOfBoundsException157.$$owner$$ = null;
        onhji_JSON40.$$owner$$ = null;
        jl_NegativeArraySizeException300.$$owner$$ = null;
        jl_Float343.$$owner$$ = null;
        jl_SecurityException314.$$owner$$ = null;
        jl_CharSequence162.$$owner$$ = null;
        oahjs_Proto321.$$owner$$ = null;
        jl_RuntimeException131.$$owner$$ = null;
        jl_Math147.$$owner$$ = null;
        oahbs_Fn466.$$owner$$ = null;
        ji_Flushable477.$$owner$$ = null;
        ju_Map304.$$owner$$ = null;
        jl_NullPointerException8.$$owner$$ = null;
        oadm_Mines49.$$owner$$ = null;
        jl_ReflectiveOperationException493.$$owner$$ = null;
        onhji_PropertyBindingAccessor404.$$owner$$ = null;
        ju_ServiceLoader283.$$owner$$ = null;
        jl_Number325.$$owner$$ = null;
        jl_ClassLoader512.$$owner$$ = null;
        oahjs_FunctionBinding448.$$owner$$ = null;
        ju_List206.$$owner$$ = null;
        jl_ConsoleOutputStream_stdout528.$$owner$$ = null;
        ju_ArrayList54.$$owner$$ = null;
        jl_Long331.$$owner$$ = null;
        jl_LinkageError210.$$owner$$ = null;
        ji_IOException538.$$owner$$ = null;
        jul_LogRecord540.$$owner$$ = null;
        oadm_Mines$2491.$$owner$$ = oadm_Mines49;
        ju_Comparator553.$$owner$$ = null;
        ju_HashMap$Entry554.$$owner$$ = ju_HashMap394;
        otcic_UTF16Helper169.$$owner$$ = null;
        ju_Random68.$$owner$$ = null;
        jl_Iterable218.$$owner$$ = null;
        jl_UnsupportedOperationException222.$$owner$$ = null;
        oadm_MinesModel$1100.$$owner$$ = oadm_MinesModel43;
        ju_AbstractList$1108.$$owner$$ = ju_AbstractList66;
        onhk_Knockout571.$$owner$$ = null;
        jl_Error208.$$owner$$ = null;
        jl_SystemClassLoader517.$$owner$$ = null;
        jul_Level581.$$owner$$ = null;
        jl_ConsoleOutputStream_stderr595.$$owner$$ = null;
        jl_IllegalArgumentException134.$$owner$$ = null;
        njh_BrwsrCtx$1603.$$owner$$ = njh_BrwsrCtx488;
        jl_Enum102.$$owner$$ = null;
        otcic_ByteBuffer615.$$owner$$ = null;
        onhji_Bindings406.$$owner$$ = null;
        oahjs_Transfer639.$$owner$$ = null;
        juc_Executor640.$$owner$$ = null;
        oahcs_Contexts$Provider506.$$owner$$ = oahcs_Contexts398;
        jl_IllegalStateException229.$$owner$$ = null;
        ju_Map$Entry261.$$owner$$ = ju_Map304;
        ju_NoSuchElementException285.$$owner$$ = null;
        onhci_CtxAccssr597.$$owner$$ = null;
        jl_Boolean327.$$owner$$ = null;
        oth_JavaScriptConv573.$$owner$$ = null;
        ju_Collection203.$$owner$$ = null;
        onhji_JSON$EmptyTech400.$$owner$$ = onhji_JSON40;
        jl_InstantiationException654.$$owner$$ = null;
        onhk_FXContext$1Wrap656.$$owner$$ = onhk_FXContext636;
        oahcs_Contexts$Builder662.$$owner$$ = oahcs_Contexts398;
        jl_ThreadLocal670.$$owner$$ = null;
        oahjs_Proto$Type36.$$owner$$ = oahjs_Proto321;
        njh_BrwsrCtx488.$$owner$$ = null;
        oadm_MinesModel$SquareModel687.$$owner$$ = oadm_MinesModel43;
        jul_Logger681.$$owner$$ = null;
        otciu_UnicodeHelper256.$$owner$$ = null;
        ju_Collections$8144.$$owner$$ = ju_Collections195;
        oadm_Row$Html4JavaType708.$$owner$$ = oadm_Row56;
        ji_PrintStream122.$$owner$$ = null;
        jl_StringIndexOutOfBoundsException177.$$owner$$ = null;
        jl_Appendable731.$$owner$$ = null;
        oahjs_PropertyBinding21.$$owner$$ = null;
        jl_Byte337.$$owner$$ = null;
        jl_System125.$$owner$$ = null;
        oahjs_WSTransfer650.$$owner$$ = null;
        ji_FilterOutputStream716.$$owner$$ = null;
        jl_AbstractStringBuilder741.$$owner$$ = null;
        jl_Runnable232.$$owner$$ = null;
        ju_AbstractCollection204.$$owner$$ = null;
        oadm_MinesModel$SquareType45.$$owner$$ = oadm_MinesModel43;
        ji_OutputStream524.$$owner$$ = null;
        otcic_UTF8Charset385.$$owner$$ = null;
        ju_Iterator278.$$owner$$ = null;
        jl_Double340.$$owner$$ = null;
        oahcs_Contexts398.$$owner$$ = null;
        jl_Class0.$$owner$$ = null;
        onhk_FXContext$TrueFn794.$$owner$$ = onhk_FXContext636;
        jl_Void298.$$owner$$ = null;
        oahjs_PropertyBinding$123.$$owner$$ = oahjs_PropertyBinding21;
        onhk_KO4J507.$$owner$$ = null;
        jl_Comparable163.$$owner$$ = null;
        jl_Short334.$$owner$$ = null;
        oadm_Square$Html4JavaType795.$$owner$$ = oadm_Square62;
        ju_HashMap394.$$owner$$ = null;
        onhk_FXContext636.$$owner$$ = null;
        jl_ClassNotFoundException796.$$owner$$ = null;
        ji_Serializable164.$$owner$$ = null;
        jl_Integer81.$$owner$$ = null;
        jl_Object7.$$owner$$ = null;
        onhci_CtxImpl665.$$owner$$ = null;
        oadm_Row56.$$owner$$ = null;
        oahjs_FunctionBinding$Impl522.$$owner$$ = oahjs_FunctionBinding448;
        jl_StringBuilder16.$$owner$$ = null;
        oadm_Square62.$$owner$$ = null;
        oahbs_Fn$Presenter797.$$owner$$ = oahbs_Fn466;
        ju_Collections$6201.$$owner$$ = ju_Collections195;
        onhji_JSONList76.$$owner$$ = null;
        jl_AssertionError316.$$owner$$ = null;
        onhbi_FnContext473.$$owner$$ = null;
        ju_ConcurrentModificationException569.$$owner$$ = null;
        jl_Exception127.$$owner$$ = null;
        oadm_MinesModel$GameState73.$$owner$$ = oadm_MinesModel43;
    }
    var cls = $this.$data;
    return cls.$$owner$$ != null ? $rt_cls(cls.$$owner$$) : null;
}
jl_Class0.$init799 = function() {
    var result = new jl_Class0();
    result.$init799();
    return result;
}
$rt_virtualMethods(jl_Class0,
    "cast346", function(a) { return jl_Class0_cast346(this, a); },
    "getName20", function() { return jl_Class0_getName20(this); },
    "getClassLoader420", function() { return jl_Class0_getClassLoader420(this); },
    "getComponentType152", function() { return jl_Class0_getComponentType152(this); },
    "getComponentType0801", function() { return jl_Class0_getComponentType0801(this); },
    "desiredAssertionStatus320", function() { return jl_Class0_desiredAssertionStatus320(this); },
    "$init799", function() { jl_Class0_$init799(this); },
    "newInstance422", function() { return jl_Class0_newInstance422(this); },
    "getDeclaringClass315", function() { return jl_Class0_getDeclaringClass315(this); });
function oahbs_Fn$Presenter797() {
}
$rt_declClass(oahbs_Fn$Presenter797, {
    name : "org.apidesign.html.boot.spi.Fn$Presenter",
    superclass : jl_Object7 });
function onhk_FXContext$TrueFn794() {
}
$rt_declClass(onhk_FXContext$TrueFn794, {
    name : "org.netbeans.html.ko4j.FXContext$TrueFn",
    interfaces : [oahbs_Fn$Presenter797],
    superclass : oahbs_Fn466,
    clinit : function() { onhk_FXContext$TrueFn794_$clinit(); } });
function onhk_FXContext$TrueFn794_$clinit() {
    onhk_FXContext$TrueFn794_$clinit = function(){};
    onhk_FXContext$TrueFn794_$init802 = function($this) {
        oahbs_Fn466_$init471($this);
        return;
    }
    onhk_FXContext$TrueFn794_$init807 = function($this, a) {
        onhk_FXContext$TrueFn794_$init802($this);
        return;
    }
}
$rt_methodStubs(onhk_FXContext$TrueFn794_$clinit, ['onhk_FXContext$TrueFn794_$init802', 'onhk_FXContext$TrueFn794_$init807']);
onhk_FXContext$TrueFn794.$init802 = function() {
    var result = new onhk_FXContext$TrueFn794();
    result.$init802();
    return result;
}
onhk_FXContext$TrueFn794.$init807 = function(a) {
    var result = new onhk_FXContext$TrueFn794();
    result.$init807(a);
    return result;
}
$rt_virtualMethods(onhk_FXContext$TrueFn794,
    "$init802", function() { onhk_FXContext$TrueFn794_$init802(this); },
    "$init807", function(a) { onhk_FXContext$TrueFn794_$init807(this, a); });
function jl_Void298() {
}
jl_Void298.TYPE299 = null;
$rt_declClass(jl_Void298, {
    name : "java.lang.Void",
    superclass : jl_Object7,
    clinit : function() { jl_Void298_$clinit(); } });
function jl_Void298_$clinit() {
    jl_Void298_$clinit = function(){};
    jl_Void298_$clinit808 = function() {
        jl_Void298.TYPE299 = $rt_cls($rt_voidcls());
        return;
    }
    jl_Void298_$init803 = function($this) {
        jl_Object7_$init10($this);
        return;
    }
    jl_Void298_$clinit808();
}
$rt_methodStubs(jl_Void298_$clinit, ['jl_Void298_$clinit808', 'jl_Void298_$init803']);
jl_Void298.$init803 = function() {
    var result = new jl_Void298();
    result.$init803();
    return result;
}
$rt_virtualMethods(jl_Void298,
    "$init803", function() { jl_Void298_$init803(this); });
function oahjs_PropertyBinding$123() {
}
$rt_declClass(oahjs_PropertyBinding$123, {
    name : "org.apidesign.html.json.spi.PropertyBinding$1",
    superclass : onhji_PropertyBindingAccessor404,
    clinit : function() { oahjs_PropertyBinding$123_$clinit(); } });
function oahjs_PropertyBinding$123_$clinit() {
    oahjs_PropertyBinding$123_$clinit = function(){};
    oahjs_PropertyBinding$123_$init24 = function($this) {
        onhji_PropertyBindingAccessor404_$init499($this);
        return;
    }
}
$rt_methodStubs(oahjs_PropertyBinding$123_$clinit, ['oahjs_PropertyBinding$123_$init24']);
function oahjs_PropertyBinding$123_newBinding498($this, a, b, c, d, e, f) {
    return oahjs_PropertyBinding$Impl26.$init33(b, c, d, e, a, f);
}
function oahjs_PropertyBinding$123_findProto500($this, a, b) {
    return $rt_nullCheck(a).protoFor370(b);
}
function oahjs_PropertyBinding$123_notifyChange503($this, a, b) {
    oahjs_Proto321_onChange455($rt_nullCheck(a), b);
    return;
}
function oahjs_PropertyBinding$123_bindings501($this, a, b) {
    if ((b == 0)) {
        a = oahjs_Proto321_getBindings441($rt_nullCheck(a));
    } else {
        a = oahjs_Proto321_initBindings445($rt_nullCheck(a));
    }
    return a;
}
oahjs_PropertyBinding$123.$init24 = function() {
    var result = new oahjs_PropertyBinding$123();
    result.$init24();
    return result;
}
$rt_virtualMethods(oahjs_PropertyBinding$123,
    "$init24", function() { oahjs_PropertyBinding$123_$init24(this); },
    "newBinding498", function(a, b, c, d, e, f) { return oahjs_PropertyBinding$123_newBinding498(this, a, b, c, d, e, f); },
    "findProto500", function(a, b) { return oahjs_PropertyBinding$123_findProto500(this, a, b); },
    "notifyChange503", function(a, b) { oahjs_PropertyBinding$123_notifyChange503(this, a, b); },
    "bindings501", function(a, b) { return oahjs_PropertyBinding$123_bindings501(this, a, b); });
function onhk_KO4J507() {
    this.c809 = null;
    this.presenter810 = null;
}
$rt_declClass(onhk_KO4J507, {
    name : "org.netbeans.html.ko4j.KO4J",
    interfaces : [oahcs_Contexts$Provider506],
    superclass : jl_Object7,
    clinit : function() { onhk_KO4J507_$clinit(); } });
function onhk_KO4J507_$clinit() {
    onhk_KO4J507_$clinit = function(){};
    onhk_KO4J507_$init508 = function($this) {
        onhk_KO4J507_$init811($this, null);
        return;
    }
    onhk_KO4J507_$init811 = function($this, a) {
        jl_Object7_$init10($this);
        $this.presenter810 = a;
        return;
    }
}
$rt_methodStubs(onhk_KO4J507_$clinit, ['onhk_KO4J507_$init508', 'onhk_KO4J507_$init811']);
function onhk_KO4J507_fillContext788($this, a, b) {
    var c, d, e, f;
    if ((onhk_FXContext636_isJavaScriptEnabled812() != 0)) {
        b = $rt_cls(oahjs_Technology156);
        c = $rt_nullCheck($this);
        d = onhk_KO4J507_knockout813(c);
        e = 100;
        f = $rt_nullCheck(a);
        oahcs_Contexts$Builder662_register668(f, b, d, e);
        oahcs_Contexts$Builder662_register668(f, $rt_cls(oahjs_Transfer639), onhk_KO4J507_transfer814(c), 100);
        if ((onhk_FXContext636_areWebSocketsSupported815($rt_nullCheck($this.c809)) != 0)) {
            oahcs_Contexts$Builder662_register668(f, $rt_cls(oahjs_WSTransfer650), onhk_KO4J507_websockets816(c), 100);
        }
    }
    return;
}
function onhk_KO4J507_getKO817($this) {
    var a, b;
    if (($this.c809 === null)) {
        a = new onhk_FXContext636();
        if (($this.presenter810 !== null)) {
            b = $this.presenter810;
        } else {
            b = oahbs_Fn466_activePresenter475();
        }
        onhk_FXContext636_$init818(a, b);
        $this.c809 = a;
    }
    return $this.c809;
}
function onhk_KO4J507_transfer814($this) {
    return onhk_KO4J507_getKO817($this);
}
function onhk_KO4J507_knockout813($this) {
    return onhk_KO4J507_getKO817($this);
}
function onhk_KO4J507_websockets816($this) {
    var a;
    if ((onhk_FXContext636_areWebSocketsSupported815($rt_nullCheck(onhk_KO4J507_getKO817($this))) == 0)) {
        a = null;
    } else {
        a = onhk_KO4J507_getKO817($this);
    }
    return a;
}
onhk_KO4J507.$init508 = function() {
    var result = new onhk_KO4J507();
    result.$init508();
    return result;
}
onhk_KO4J507.$init811 = function(a) {
    var result = new onhk_KO4J507();
    result.$init811(a);
    return result;
}
$rt_virtualMethods(onhk_KO4J507,
    "fillContext788", function(a, b) { onhk_KO4J507_fillContext788(this, a, b); },
    "getKO817", function() { return onhk_KO4J507_getKO817(this); },
    "$init508", function() { onhk_KO4J507_$init508(this); },
    "transfer814", function() { return onhk_KO4J507_transfer814(this); },
    "knockout813", function() { return onhk_KO4J507_knockout813(this); },
    "$init811", function(a) { onhk_KO4J507_$init811(this, a); },
    "websockets816", function() { return onhk_KO4J507_websockets816(this); });
function jl_Short334() {
    this.value819 = 0;
}
jl_Short334.TYPE820 = null;
$rt_declClass(jl_Short334, {
    name : "java.lang.Short",
    interfaces : [jl_Comparable163],
    superclass : jl_Number325,
    clinit : function() { jl_Short334_$clinit(); } });
function jl_Short334_$clinit() {
    jl_Short334_$clinit = function(){};
    jl_Short334_$clinit821 = function() {
        jl_Short334.TYPE820 = $rt_cls($rt_shortcls());
        return;
    }
    jl_Short334_compare822 = function(a, b) {
        return ((a - b) | 0);
    }
    jl_Short334_$init823 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value819 = a;
        return;
    }
    jl_Short334_valueOf336 = function(a) {
        return jl_Short334.$init823(a);
    }
    jl_Short334_$clinit821();
}
$rt_methodStubs(jl_Short334_$clinit, ['jl_Short334_$clinit821', 'jl_Short334_compare822', 'jl_Short334_$init823', 'jl_Short334_valueOf336']);
function jl_Short334_doubleValue341($this) {
    return $this.value819;
}
function jl_Short334_intValue87($this) {
    return $this.value819;
}
function jl_Short334_shortValue335($this) {
    return $this.value819;
}
function jl_Short334_longValue332($this) {
    return Long_fromInt($this.value819);
}
function jl_Short334_compareTo181($this, a) {
    return jl_Short334_compareTo824($rt_nullCheck($this), a);
}
function jl_Short334_floatValue344($this) {
    return $this.value819;
}
function jl_Short334_compareTo824($this, a) {
    return jl_Short334_compare822($this.value819, a.value819);
}
jl_Short334.$init823 = function(a) {
    var result = new jl_Short334();
    result.$init823(a);
    return result;
}
$rt_virtualMethods(jl_Short334,
    "doubleValue341", function() { return jl_Short334_doubleValue341(this); },
    "intValue87", function() { return jl_Short334_intValue87(this); },
    "shortValue335", function() { return jl_Short334_shortValue335(this); },
    "longValue332", function() { return jl_Short334_longValue332(this); },
    "$init823", function(a) { jl_Short334_$init823(this, a); },
    "compareTo181", function(a) { return jl_Short334_compareTo181(this, a); },
    "floatValue344", function() { return jl_Short334_floatValue344(this); },
    "compareTo824", function(a) { return jl_Short334_compareTo824(this, a); });
function oadm_Square$Html4JavaType795() {
}
$rt_declClass(oadm_Square$Html4JavaType795, {
    name : "org.apidesign.demo.minesweeper.Square$Html4JavaType",
    superclass : oahjs_Proto$Type36,
    clinit : function() { oadm_Square$Html4JavaType795_$clinit(); } });
function oadm_Square$Html4JavaType795_$clinit() {
    oadm_Square$Html4JavaType795_$clinit = function(){};
    oadm_Square$Html4JavaType795_$init804 = function($this) {
        var a, b, c, d;
        oahjs_Proto$Type36_$init313($this, $rt_cls(oadm_Square62), $rt_cls(oadm_MinesModel$SquareModel687), 4, 0);
        a = $rt_str("html");
        b = 0;
        c = 1;
        d = $rt_nullCheck($this);
        oahjs_Proto$Type36_registerProperty347(d, a, b, c);
        oahjs_Proto$Type36_registerProperty347(d, $rt_str("style"), 1, 1);
        oahjs_Proto$Type36_registerProperty347(d, $rt_str("state"), 2, 0);
        oahjs_Proto$Type36_registerProperty347(d, $rt_str("mine"), 3, 0);
        return;
    }
    oadm_Square$Html4JavaType795_$init825 = function($this, a) {
        oadm_Square$Html4JavaType795_$init804($this);
        return;
    }
}
$rt_methodStubs(oadm_Square$Html4JavaType795_$clinit, ['oadm_Square$Html4JavaType795_$init804', 'oadm_Square$Html4JavaType795_$init825']);
function oadm_Square$Html4JavaType795_call359($this, a, b, c, d) {
    a = a;
    oadm_Square$Html4JavaType795_call826($rt_nullCheck($this), a, b, c, d);
    return;
}
function oadm_Square$Html4JavaType795_call826($this, a, b, c, d) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Square$Html4JavaType795_setValue37($this, a, b, c) {
    a = a;
    oadm_Square$Html4JavaType795_setValue827($rt_nullCheck($this), a, b, c);
    return;
}
function oadm_Square$Html4JavaType795_onChange828($this, a, b) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Square$Html4JavaType795_getValue829($this, a, b) {
    block1: {
        block2: {
            block3: {
                block4: {
                    switch (b) {
                        case 0:
                            break;
                        case 1:
                            break block4;
                        case 2:
                            break block3;
                        case 3:
                            break block2;
                        default:
                            break block1;
                    }
                    return oadm_Square62_getHtml830($rt_nullCheck(a));
                }
                return oadm_Square62_getStyle831($rt_nullCheck(a));
            }
            return oadm_Square62_getState84($rt_nullCheck(a));
        }
        return jl_Boolean327_valueOf363(oadm_Square62_isMine71($rt_nullCheck(a)));
    }
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function oadm_Square$Html4JavaType795_protoFor370($this, a) {
    return oadm_Square62_access$200832(a);
}
function oadm_Square$Html4JavaType795_onChange372($this, a, b) {
    a = a;
    oadm_Square$Html4JavaType795_onChange828($rt_nullCheck($this), a, b);
    return;
}
function oadm_Square$Html4JavaType795_getValue39($this, a, b) {
    return oadm_Square$Html4JavaType795_getValue829($rt_nullCheck($this), a, b);
}
function oadm_Square$Html4JavaType795_setValue827($this, a, b, c) {
    block1: {
        block2: {
            switch (b) {
                case 2:
                    break;
                case 3:
                    break block2;
                default:
                    break block1;
            }
            b = oahjs_Proto$Type36_extractValue324($rt_nullCheck(oadm_Square62_access$100833()), $rt_cls(oadm_MinesModel$SquareType45), c);
            oadm_Square62_setState88($rt_nullCheck(a), b);
            return;
        }
        b = jl_Boolean327_booleanValue409($rt_nullCheck(oahjs_Proto$Type36_extractValue324($rt_nullCheck(oadm_Square62_access$100833()), $rt_cls(jl_Boolean327), c)));
        oadm_Square62_setMine72($rt_nullCheck(a), b);
        return;
    }
    return;
}
oadm_Square$Html4JavaType795.$init804 = function() {
    var result = new oadm_Square$Html4JavaType795();
    result.$init804();
    return result;
}
oadm_Square$Html4JavaType795.$init825 = function(a) {
    var result = new oadm_Square$Html4JavaType795();
    result.$init825(a);
    return result;
}
$rt_virtualMethods(oadm_Square$Html4JavaType795,
    "call359", function(a, b, c, d) { oadm_Square$Html4JavaType795_call359(this, a, b, c, d); },
    "call826", function(a, b, c, d) { oadm_Square$Html4JavaType795_call826(this, a, b, c, d); },
    "setValue37", function(a, b, c) { oadm_Square$Html4JavaType795_setValue37(this, a, b, c); },
    "onChange828", function(a, b) { oadm_Square$Html4JavaType795_onChange828(this, a, b); },
    "getValue829", function(a, b) { return oadm_Square$Html4JavaType795_getValue829(this, a, b); },
    "protoFor370", function(a) { return oadm_Square$Html4JavaType795_protoFor370(this, a); },
    "onChange372", function(a, b) { oadm_Square$Html4JavaType795_onChange372(this, a, b); },
    "getValue39", function(a, b) { return oadm_Square$Html4JavaType795_getValue39(this, a, b); },
    "$init804", function() { oadm_Square$Html4JavaType795_$init804(this); },
    "setValue827", function(a, b, c) { oadm_Square$Html4JavaType795_setValue827(this, a, b, c); },
    "$init825", function(a) { oadm_Square$Html4JavaType795_$init825(this, a); });
function ju_HashMap394() {
    this.elementData834 = null;
    this.elementCount835 = 0;
    this.loadFactor836 = 0.0;
    this.modCount837 = 0;
    this.threshold838 = 0;
}
$rt_declClass(ju_HashMap394, {
    name : "java.util.HashMap",
    interfaces : [ji_Serializable164],
    superclass : ju_AbstractMap305,
    clinit : function() { ju_HashMap394_$clinit(); } });
function ju_HashMap394_$clinit() {
    ju_HashMap394_$clinit = function(){};
    ju_HashMap394_$init839 = function($this, a) {
        ju_HashMap394_$init840($this, a, 0.75);
        return;
    }
    ju_HashMap394_areEqualKeys841 = function(a, b) {
        if (((a !== b) && ($rt_nullCheck(a).equals13(b) == 0))) {
            a = 0;
        } else {
            a = 1;
        }
        return a;
    }
    ju_HashMap394_calculateCapacity842 = function(a) {
        if ((a < 1073741824)) {
            if ((a != 0)) {
                a = ((a - 1) | 0);
                a = (a | (a >> 1));
                a = (a | (a >> 2));
                a = (a | (a >> 4));
                a = (a | (a >> 8));
                return (((a | (a >> 16)) + 1) | 0);
            }
            return 16;
        }
        return 1073741824;
    }
    ju_HashMap394_$init412 = function($this) {
        ju_HashMap394_$init839($this, 16);
        return;
    }
    ju_HashMap394_$init840 = function($this, a, b) {
        ju_AbstractMap305_$init306($this);
        $this.modCount837 = 0;
        if ((!((a >= 0) && (b > 0.0)))) {
            $rt_throw(jl_IllegalArgumentException134.$init135());
        }
        a = ju_HashMap394_calculateCapacity842(a);
        $this.elementCount835 = 0;
        $this.elementData834 = ju_HashMap394_newElementArray843($rt_nullCheck($this), a);
        $this.loadFactor836 = b;
        ju_HashMap394_computeThreshold844($this);
        return;
    }
    ju_HashMap394_computeHashCode845 = function(a) {
        return $rt_nullCheck(a).hashCode14();
    }
}
$rt_methodStubs(ju_HashMap394_$clinit, ['ju_HashMap394_$init839', 'ju_HashMap394_areEqualKeys841', 'ju_HashMap394_calculateCapacity842', 'ju_HashMap394_$init412', 'ju_HashMap394_$init840', 'ju_HashMap394_computeHashCode845']);
function ju_HashMap394_rehash846($this, a) {
    var b, c, d, e, f, g;
    if ((a != 0)) {
        a = (a << 1);
    } else {
        a = 1;
    }
    a = ju_HashMap394_calculateCapacity842(a);
    b = ju_HashMap394_newElementArray843($rt_nullCheck($this), a);
    c = 0;
    while ((c < $this.elementData834.data.length)) {
        d = $this.elementData834.data[c];
        e = $this.elementData834;
        f = null;
        e.data[c] = f;
        while ((d !== null)) {
            f = (d.origKeyHash556 & ((a - 1) | 0));
            e = d.next555;
            g = b.data;
            d.next555 = g[f];
            g[f] = d;
            d = e;
        }
        c = ((c + 1) | 0);
    }
    $this.elementData834 = b;
    ju_HashMap394_computeThreshold844($this);
    return;
}
function ju_HashMap394_putImpl847($this, a, b) {
    var c, d, e, f;
    if ((a !== null)) {
        c = ju_HashMap394_computeHashCode845(a);
        d = (c & (($this.elementData834.data.length - 1) | 0));
        e = $rt_nullCheck($this);
        f = ju_HashMap394_findNonNullKeyEntry848(e, a, d, c);
        if ((f === null)) {
            $this.modCount837 = (($this.modCount837 + 1) | 0);
            f = ju_HashMap394_createHashedEntry849(e, a, d, c);
            a = (($this.elementCount835 + 1) | 0);
            $this.elementCount835 = a;
            if ((a > $this.threshold838)) {
                ju_HashMap394_rehash850(e);
            }
        }
    } else {
        e = $rt_nullCheck($this);
        f = ju_HashMap394_findNullKeyEntry851(e);
        if ((f === null)) {
            $this.modCount837 = (($this.modCount837 + 1) | 0);
            f = ju_HashMap394_createHashedEntry849(e, null, 0, 0);
            d = (($this.elementCount835 + 1) | 0);
            $this.elementCount835 = d;
            if ((d > $this.threshold838)) {
                ju_HashMap394_rehash850(e);
            }
        }
    }
    a = f.value264;
    f.value264 = b;
    return a;
}
function ju_HashMap394_put397($this, a, b) {
    return ju_HashMap394_putImpl847($rt_nullCheck($this), a, b);
}
function ju_HashMap394_createHashedEntry849($this, a, b, c) {
    var d;
    d = ju_HashMap$Entry554.$init557(a, c);
    d.next555 = $this.elementData834.data[b];
    $this.elementData834.data[b] = d;
    return d;
}
function ju_HashMap394_newElementArray843($this, a) {
    return $rt_createArray(ju_HashMap$Entry554, a);
}
function ju_HashMap394_computeThreshold844($this) {
    $this.threshold838 = (($this.elementData834.data.length * $this.loadFactor836) | 0);
    return;
}
function ju_HashMap394_get395($this, a) {
    a = ju_HashMap394_getEntry852($rt_nullCheck($this), a);
    if ((a === null)) {
        return null;
    }
    return a.value264;
}
function ju_HashMap394_rehash850($this) {
    var a;
    a = $this.elementData834.data.length;
    ju_HashMap394_rehash846($rt_nullCheck($this), a);
    return;
}
function ju_HashMap394_findNullKeyEntry851($this) {
    var a, b;
    a = $this.elementData834;
    b = 0;
    a = a.data[b];
    while (((a !== null) && (a.key265 !== null))) {
        a = a.next555;
    }
    return a;
}
function ju_HashMap394_getEntry852($this, a) {
    var b;
    if ((a !== null)) {
        b = ju_HashMap394_computeHashCode845(a);
        a = ju_HashMap394_findNonNullKeyEntry848($rt_nullCheck($this), a, (b & (($this.elementData834.data.length - 1) | 0)), b);
    } else {
        a = ju_HashMap394_findNullKeyEntry851($rt_nullCheck($this));
    }
    return a;
}
function ju_HashMap394_findNonNullKeyEntry848($this, a, b, c) {
    b = $this.elementData834.data[b];
    while (((b !== null) && (!((b.origKeyHash556 == c) && (ju_HashMap394_areEqualKeys841(a, b.key265) != 0))))) {
        b = b.next555;
    }
    return b;
}
ju_HashMap394.$init839 = function(a) {
    var result = new ju_HashMap394();
    result.$init839(a);
    return result;
}
ju_HashMap394.$init412 = function() {
    var result = new ju_HashMap394();
    result.$init412();
    return result;
}
ju_HashMap394.$init840 = function(a, b) {
    var result = new ju_HashMap394();
    result.$init840(a, b);
    return result;
}
$rt_virtualMethods(ju_HashMap394,
    "$init839", function(a) { ju_HashMap394_$init839(this, a); },
    "rehash846", function(a) { ju_HashMap394_rehash846(this, a); },
    "putImpl847", function(a, b) { return ju_HashMap394_putImpl847(this, a, b); },
    "put397", function(a, b) { return ju_HashMap394_put397(this, a, b); },
    "createHashedEntry849", function(a, b, c) { return ju_HashMap394_createHashedEntry849(this, a, b, c); },
    "newElementArray843", function(a) { return ju_HashMap394_newElementArray843(this, a); },
    "computeThreshold844", function() { ju_HashMap394_computeThreshold844(this); },
    "get395", function(a) { return ju_HashMap394_get395(this, a); },
    "rehash850", function() { ju_HashMap394_rehash850(this); },
    "findNullKeyEntry851", function() { return ju_HashMap394_findNullKeyEntry851(this); },
    "getEntry852", function(a) { return ju_HashMap394_getEntry852(this, a); },
    "$init412", function() { ju_HashMap394_$init412(this); },
    "$init840", function(a, b) { ju_HashMap394_$init840(this, a, b); },
    "findNonNullKeyEntry848", function(a, b, c) { return ju_HashMap394_findNonNullKeyEntry848(this, a, b, c); });
function onhk_FXContext636() {
    this.browserContext853 = null;
}
onhk_FXContext636.javaScriptEnabled854 = null;
onhk_FXContext636.LOG855 = null;
$rt_declClass(onhk_FXContext636, {
    name : "org.netbeans.html.ko4j.FXContext",
    interfaces : [oahjs_Transfer639, oahjs_WSTransfer650, oahjs_Technology$BatchInit226],
    superclass : jl_Object7,
    clinit : function() { onhk_FXContext636_$clinit(); } });
function onhk_FXContext636_$clinit() {
    onhk_FXContext636_$clinit = function(){};
    onhk_FXContext636_$clinit856 = function() {
        onhk_FXContext636.LOG855 = jul_Logger681_getLogger682(jl_Class0_getName20($rt_nullCheck($rt_cls(onhk_FXContext636))));
        return;
    }
    onhk_FXContext636_isWebSocket857 = function() {
        var result = (function() {
            if (window.WebSocket) return true; else return false;
        }).call(null);
        return oth_JavaScriptConv573_fromJavaScript575(result, $rt_booleancls());
    }
    onhk_FXContext636_isJavaScriptEnabled812 = function() {
        var a, b;
        if ((onhk_FXContext636.javaScriptEnabled854 === null)) {
            block2: {
                block3: {
                    a = jl_Boolean327_valueOf363(onhk_FXContext636_isJavaScriptEnabledJs858());
                    onhk_FXContext636.javaScriptEnabled854 = a;
                    if ((jl_Boolean327_booleanValue409($rt_nullCheck(a)) == 0)) {
                        a = oahbs_Fn466_activate472(onhk_FXContext$TrueFn794.$init807(null));
                        try {
                            onhk_FXContext636.javaScriptEnabled854 = jl_Boolean327_valueOf363(onhk_FXContext636_isJavaScriptEnabledJs858());
                        } catch ($e) {
                            $je = $e.$javaException;
                            if ($je) {
                                b = $je;
                                break block2;
                            } else {
                                throw $e;
                            }
                        }
                        block4: {
                            try {
                                onhbi_FnContext473_close661($rt_nullCheck(a));
                            } catch ($e) {
                                $je = $e.$javaException;
                                if ($je && $je instanceof ji_IOException538) {
                                    a = $je;
                                    break block4;
                                } else {
                                    throw $e;
                                }
                            }
                            break block3;
                        }
                    }
                }
                return jl_Boolean327_booleanValue409($rt_nullCheck(onhk_FXContext636.javaScriptEnabled854));
            }
            block5: {
                block6: {
                    try {
                        onhbi_FnContext473_close661($rt_nullCheck(a));
                    } catch ($e) {
                        $je = $e.$javaException;
                        if ($je && $je instanceof ji_IOException538) {
                            a = $je;
                            break block6;
                        } else {
                            throw $e;
                        }
                    }
                    break block5;
                }
            }
            $rt_throw(b);
        }
        return jl_Boolean327_booleanValue409($rt_nullCheck(onhk_FXContext636.javaScriptEnabled854));
    }
    onhk_FXContext636_$init818 = function($this, a) {
        jl_Object7_$init10($this);
        $this.browserContext853 = a;
        return;
    }
    onhk_FXContext636_access$100660 = function(a) {
        return a.browserContext853;
    }
    onhk_FXContext636_isJavaScriptEnabledJs858 = function() {
        var result = (function() {
            if (window) return true; else return false;
        }).call(null);
        return oth_JavaScriptConv573_fromJavaScript575(result, $rt_booleancls());
    }
    onhk_FXContext636_$clinit856();
}
$rt_methodStubs(onhk_FXContext636_$clinit, ['onhk_FXContext636_$clinit856', 'onhk_FXContext636_isWebSocket857', 'onhk_FXContext636_isJavaScriptEnabled812', 'onhk_FXContext636_$init818', 'onhk_FXContext636_access$100660', 'onhk_FXContext636_isJavaScriptEnabledJs858']);
function onhk_FXContext636_areWebSocketsSupported815($this) {
    return onhk_FXContext636_isWebSocket857();
}
function onhk_FXContext636_wrapArray632($this, a) {
    return a;
}
function onhk_FXContext636_wrapModel637($this, a, b, c) {
    var d, e, f, g, h, i, j;
    d = b.data;
    e = d.length;
    f = $rt_createArray(jl_String3, e);
    g = $rt_createBooleanArray(e);
    h = $rt_createArray(jl_Object7, e);
    i = 0;
    while (true) {
        j = f.data;
        if ((i >= j.length)) {
            break;
        }
        j[i] = oahjs_PropertyBinding$Impl26_getPropertyName42($rt_nullCheck(d[i]));
        e = oahjs_PropertyBinding$Impl26_isReadOnly34($rt_nullCheck(d[i]));
        g.data[i] = e;
        e = oahjs_PropertyBinding$Impl26_getValue38($rt_nullCheck(d[i]));
        h.data[i] = e;
        i = ((i + 1) | 0);
    }
    e = c.data;
    d = $rt_createArray(jl_String3, e.length);
    i = 0;
    while (true) {
        j = d.data;
        if ((i >= j.length)) {
            break;
        }
        j[i] = oahjs_FunctionBinding$Impl522_getFunctionName859($rt_nullCheck(e[i]));
        i = ((i + 1) | 0);
    }
    return onhk_Knockout571_wrapModel572(a, f, g, h, b, d, c);
}
function onhk_FXContext636_runSafe392($this, a) {
    var b;
    b = onhk_FXContext$1Wrap656.$init659($this, a);
    if (($rt_isInstance($this.browserContext853, juc_Executor640) == 0)) {
        onhk_FXContext$1Wrap656_run381($rt_nullCheck(b));
    } else {
        $rt_nullCheck($this.browserContext853).execute860(b);
    }
    return;
}
function onhk_FXContext636_wrapModel633($this, a) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function onhk_FXContext636_bind634($this, a, b, c) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function onhk_FXContext636_expose635($this, a, b, c) {
    $rt_throw(jl_UnsupportedOperationException222.$init223());
}
function onhk_FXContext636_valueHasMutated631($this, a, b) {
    onhk_Knockout571_valueHasMutated580(a, b);
    return;
}
function onhk_FXContext636_applyBindings638($this, a) {
    onhk_Knockout571_applyBindings579(a);
    return;
}
function onhk_FXContext636_toModel389($this, a, b) {
    return jl_Class0_cast346($rt_nullCheck(a), onhk_Knockout571_toModel577(b));
}
onhk_FXContext636.$init818 = function(a) {
    var result = new onhk_FXContext636();
    result.$init818(a);
    return result;
}
$rt_virtualMethods(onhk_FXContext636,
    "areWebSocketsSupported815", function() { return onhk_FXContext636_areWebSocketsSupported815(this); },
    "$init818", function(a) { onhk_FXContext636_$init818(this, a); },
    "wrapArray632", function(a) { return onhk_FXContext636_wrapArray632(this, a); },
    "wrapModel637", function(a, b, c) { return onhk_FXContext636_wrapModel637(this, a, b, c); },
    "runSafe392", function(a) { onhk_FXContext636_runSafe392(this, a); },
    "wrapModel633", function(a) { return onhk_FXContext636_wrapModel633(this, a); },
    "bind634", function(a, b, c) { onhk_FXContext636_bind634(this, a, b, c); },
    "expose635", function(a, b, c) { onhk_FXContext636_expose635(this, a, b, c); },
    "valueHasMutated631", function(a, b) { onhk_FXContext636_valueHasMutated631(this, a, b); },
    "applyBindings638", function(a) { onhk_FXContext636_applyBindings638(this, a); },
    "toModel389", function(a, b) { return onhk_FXContext636_toModel389(this, a, b); });
function jl_ClassNotFoundException796() {
}
$rt_declClass(jl_ClassNotFoundException796, {
    name : "java.lang.ClassNotFoundException",
    superclass : jl_ReflectiveOperationException493,
    clinit : function() { jl_ClassNotFoundException796_$clinit(); } });
function jl_ClassNotFoundException796_$clinit() {
    jl_ClassNotFoundException796_$clinit = function(){};
    jl_ClassNotFoundException796_$init800 = function($this) {
        jl_ReflectiveOperationException493_$init494($this);
        return;
    }
}
$rt_methodStubs(jl_ClassNotFoundException796_$clinit, ['jl_ClassNotFoundException796_$init800']);
jl_ClassNotFoundException796.$init800 = function() {
    var result = new jl_ClassNotFoundException796();
    result.$init800();
    return result;
}
$rt_virtualMethods(jl_ClassNotFoundException796,
    "$init800", function() { jl_ClassNotFoundException796_$init800(this); });
function jl_Integer81() {
    this.value861 = 0;
}
jl_Integer81.integerCache862 = null;
jl_Integer81.TYPE863 = null;
$rt_declClass(jl_Integer81, {
    name : "java.lang.Integer",
    interfaces : [jl_Comparable163],
    superclass : jl_Number325,
    clinit : function() { jl_Integer81_$clinit(); } });
function jl_Integer81_$clinit() {
    jl_Integer81_$clinit = function(){};
    jl_Integer81_ensureIntegerCache864 = function() {
        var a;
        if ((jl_Integer81.integerCache862 === null)) {
            jl_Integer81.integerCache862 = $rt_createArray(jl_Integer81, 256);
            a = 0;
            while ((a < jl_Integer81.integerCache862.data.length)) {
                jl_Integer81.integerCache862.data[a] = jl_Integer81.$init865(((a - 128) | 0));
                a = ((a + 1) | 0);
            }
        }
        return;
    }
    jl_Integer81_$init865 = function($this, a) {
        jl_Number325_$init423($this);
        $this.value861 = a;
        return;
    }
    jl_Integer81_parseInt704 = function(a) {
        return jl_Integer81_parseInt866(a, 10);
    }
    jl_Integer81_$clinit867 = function() {
        jl_Integer81.TYPE863 = $rt_cls($rt_intcls());
        return;
    }
    jl_Integer81_valueOf82 = function(a) {
        if ((!((a >= -128) && (a <= 127)))) {
            return jl_Integer81.$init865(a);
        }
        jl_Integer81_ensureIntegerCache864();
        return jl_Integer81.integerCache862.data[((a + 128) | 0)];
    }
    jl_Integer81_compare613 = function(a, b) {
        a = $rt_compare(a, b);
        if ((a <= 0)) {
            if ((a >= 0)) {
                a = 0;
            } else {
                a = -1;
            }
        } else {
            a = 1;
        }
        return a;
    }
    jl_Integer81_parseInt866 = function(a, b) {
        var c, d, e, f, g;
        if (((b >= 2) && (b <= 36))) {
            if ((a !== null)) {
                c = $rt_nullCheck(a);
                if ((jl_String3_isEmpty184(c) == 0)) {
                    block3: {
                        block4: {
                            block5: {
                                d = 0;
                                e = 0;
                                switch (jl_String3_charAt176(c, 0)) {
                                    case 43:
                                        break;
                                    case 45:
                                        break block5;
                                    default:
                                        break block4;
                                }
                                e = 1;
                                break block3;
                            }
                            d = 1;
                            e = 1;
                            break block3;
                        }
                    }
                    f = 0;
                    block6: {
                        block7: {
                            block8: {
                                while (true) {
                                    if ((e >= jl_String3_length5(c))) {
                                        break block6;
                                    }
                                    g = ((e + 1) | 0);
                                    e = jl_Character187_getNumericValue258(jl_String3_charAt176(c, e));
                                    if ((e < 0)) {
                                        break block7;
                                    }
                                    if ((e >= b)) {
                                        break block8;
                                    }
                                    f = ((((b * f) | 0) + e) | 0);
                                    if ((f < 0)) {
                                        break;
                                    }
                                    e = g;
                                }
                                if ((!((g == jl_String3_length5(c)) && ((f == -2147483648) && (d != 0))))) {
                                    $rt_throw(jl_NumberFormatException137.$init139(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append705($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("The value is too big for int type: "))), a)))));
                                }
                                return -2147483648;
                            }
                            $rt_throw(jl_NumberFormatException137.$init139(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append705($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16_append18($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("String contains digits out of radix "))), b)), $rt_str(": "))), a)))));
                        }
                        $rt_throw(jl_NumberFormatException137.$init139(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append705($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("String contains invalid digits: "))), a)))));
                    }
                    if ((d != 0)) {
                        f = ((-f) | 0);
                    }
                    return f;
                }
            }
            $rt_throw(jl_NumberFormatException137.$init139($rt_str("String is null or empty")));
        }
        $rt_throw(jl_NumberFormatException137.$init139(jl_StringBuilder16_toString15($rt_nullCheck(jl_StringBuilder16_append18($rt_nullCheck(jl_StringBuilder16_append19($rt_nullCheck(jl_StringBuilder16.$init17()), $rt_str("Illegal radix: "))), b)))));
    }
    jl_Integer81_$clinit867();
}
$rt_methodStubs(jl_Integer81_$clinit, ['jl_Integer81_ensureIntegerCache864', 'jl_Integer81_$init865', 'jl_Integer81_parseInt704', 'jl_Integer81_$clinit867', 'jl_Integer81_valueOf82', 'jl_Integer81_compare613', 'jl_Integer81_parseInt866']);
function jl_Integer81_intValue87($this) {
    return $this.value861;
}
function jl_Integer81_floatValue344($this) {
    return $this.value861;
}
function jl_Integer81_doubleValue341($this) {
    return $this.value861;
}
function jl_Integer81_longValue332($this) {
    return Long_fromInt($this.value861);
}
function jl_Integer81_compareTo868($this, a) {
    return jl_Integer81_compare613($this.value861, a.value861);
}
function jl_Integer81_compareTo181($this, a) {
    return jl_Integer81_compareTo868($rt_nullCheck($this), a);
}
jl_Integer81.$init865 = function(a) {
    var result = new jl_Integer81();
    result.$init865(a);
    return result;
}
$rt_virtualMethods(jl_Integer81,
    "$init865", function(a) { jl_Integer81_$init865(this, a); },
    "intValue87", function() { return jl_Integer81_intValue87(this); },
    "floatValue344", function() { return jl_Integer81_floatValue344(this); },
    "doubleValue341", function() { return jl_Integer81_doubleValue341(this); },
    "longValue332", function() { return jl_Integer81_longValue332(this); },
    "compareTo868", function(a) { return jl_Integer81_compareTo868(this, a); },
    "compareTo181", function(a) { return jl_Integer81_compareTo181(this, a); });
function onhci_CtxImpl665() {
    this.techs869 = null;
}
$rt_declClass(onhci_CtxImpl665, {
    name : "org.netbeans.html.context.impl.CtxImpl",
    superclass : jl_Object7,
    clinit : function() { onhci_CtxImpl665_$clinit(); } });
function onhci_CtxImpl665_$clinit() {
    onhci_CtxImpl665_$clinit = function(){};
    onhci_CtxImpl665_$init870 = function($this, a) {
        jl_Object7_$init10($this);
        $this.techs869 = a;
        return;
    }
    onhci_CtxImpl665_$init666 = function($this) {
        jl_Object7_$init10($this);
        $this.techs869 = ju_ArrayList54.$init80();
        return;
    }
    onhci_CtxImpl665_find790 = function(a, b) {
        var c, d;
        a = ju_AbstractList66_iterator107($rt_nullCheck(njh_BrwsrCtx$1603_find607($rt_nullCheck(onhci_CtxAccssr597_getDefault602()), a).techs869));
        block1: {
            while (true) {
                c = $rt_nullCheck(a);
                if ((ju_AbstractList$1108_hasNext109(c) == 0)) {
                    break block1;
                }
                d = ju_AbstractList$1108_next110(c);
                if ((b === onhci_CtxImpl$Bind270_access$000275(d))) {
                    break;
                }
            }
            return jl_Class0_cast346($rt_nullCheck(b), onhci_CtxImpl$Bind270_access$100276(d));
        }
        return null;
    }
}
$rt_methodStubs(onhci_CtxImpl665_$clinit, ['onhci_CtxImpl665_$init870', 'onhci_CtxImpl665_$init666', 'onhci_CtxImpl665_find790']);
function onhci_CtxImpl665_register669($this, a, b, c) {
    var d, e;
    d = $this.techs869;
    e = onhci_CtxImpl$Bind270.$init274(a, b, c);
    ju_AbstractList66_add67($rt_nullCheck(d), e);
    return;
}
function onhci_CtxImpl665_build667($this) {
    ju_Collections195_sort196($this.techs869);
    return njh_BrwsrCtx$1603_newContext605($rt_nullCheck(onhci_CtxAccssr597_getDefault602()), onhci_CtxImpl665.$init870(ju_Collections195_unmodifiableList200($this.techs869)));
}
onhci_CtxImpl665.$init870 = function(a) {
    var result = new onhci_CtxImpl665();
    result.$init870(a);
    return result;
}
onhci_CtxImpl665.$init666 = function() {
    var result = new onhci_CtxImpl665();
    result.$init666();
    return result;
}
$rt_virtualMethods(onhci_CtxImpl665,
    "$init870", function(a) { onhci_CtxImpl665_$init870(this, a); },
    "register669", function(a, b, c) { onhci_CtxImpl665_register669(this, a, b, c); },
    "$init666", function() { onhci_CtxImpl665_$init666(this); },
    "build667", function() { return onhci_CtxImpl665_build667(this); });
function oadm_Row56() {
    this.prop_columns871 = null;
    this.proto872 = null;
}
oadm_Row56.TYPE873 = null;
$rt_declClass(oadm_Row56, {
    name : "org.apidesign.demo.minesweeper.Row",
    interfaces : [jl_Cloneable262],
    superclass : jl_Object7,
    clinit : function() { oadm_Row56_$clinit(); } });
function oadm_Row56_$clinit() {
    oadm_Row56_$clinit = function(){};
    oadm_Row56_$clinit874 = function() {
        oadm_Row56.TYPE873 = oadm_Row$Html4JavaType708.$init710(null);
        return;
    }
    oadm_Row56_$init875 = function($this, a) {
        jl_Object7_$init10($this);
        $this.proto872 = oahjs_Proto$Type36_createProto349($rt_nullCheck(oadm_Row56.TYPE873), $this, a);
        $this.prop_columns871 = oahjs_Proto321_createList442($rt_nullCheck($this.proto872), $rt_str("columns"), -1, $rt_createArray(jl_String3, 0));
        return;
    }
    oadm_Row56_$init65 = function($this, a) {
        var b, c;
        oadm_Row56_$init875($this, njh_BrwsrCtx488_findDefault489($rt_cls(oadm_Row56)));
        b = $this.proto872;
        c = $this.prop_columns871;
        oahjs_Proto321_initTo457($rt_nullCheck(b), c, a);
        return;
    }
    oadm_Row56_$init805 = function($this) {
        oadm_Row56_$init875($this, njh_BrwsrCtx488_findDefault489($rt_cls(oadm_Row56)));
        return;
    }
    oadm_Row56_access$100714 = function(a) {
        return a.proto872;
    }
    oadm_Row56_$clinit874();
}
$rt_methodStubs(oadm_Row56_$clinit, ['oadm_Row56_$clinit874', 'oadm_Row56_$init875', 'oadm_Row56_$init65', 'oadm_Row56_$init805', 'oadm_Row56_access$100714']);
function oadm_Row56_getColumns57($this) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto872));
    return $this.prop_columns871;
}
oadm_Row56.$init875 = function(a) {
    var result = new oadm_Row56();
    result.$init875(a);
    return result;
}
oadm_Row56.$init65 = function(a) {
    var result = new oadm_Row56();
    result.$init65(a);
    return result;
}
oadm_Row56.$init805 = function() {
    var result = new oadm_Row56();
    result.$init805();
    return result;
}
$rt_virtualMethods(oadm_Row56,
    "$init875", function(a) { oadm_Row56_$init875(this, a); },
    "$init65", function(a) { oadm_Row56_$init65(this, a); },
    "$init805", function() { oadm_Row56_$init805(this); },
    "getColumns57", function() { return oadm_Row56_getColumns57(this); });
function oahjs_FunctionBinding$Impl522() {
    this.index876 = 0;
    this.model877 = null;
    this.name878 = null;
    this.access879 = null;
}
$rt_declClass(oahjs_FunctionBinding$Impl522, {
    name : "org.apidesign.html.json.spi.FunctionBinding$Impl",
    superclass : oahjs_FunctionBinding448,
    clinit : function() { oahjs_FunctionBinding$Impl522_$clinit(); } });
function oahjs_FunctionBinding$Impl522_$clinit() {
    oahjs_FunctionBinding$Impl522_$clinit = function(){};
    oahjs_FunctionBinding$Impl522_$init523 = function($this, a, b, c, d) {
        oahjs_FunctionBinding448_$init521($this);
        $this.name878 = a;
        $this.index876 = b;
        $this.model877 = c;
        $this.access879 = d;
        return;
    }
}
$rt_methodStubs(oahjs_FunctionBinding$Impl522_$clinit, ['oahjs_FunctionBinding$Impl522_$init523']);
function oahjs_FunctionBinding$Impl522_call576($this, a, b) {
    var c, d, e, f;
    block1: {
        block2: {
            try {
                c = $this.access879;
                d = $this.model877;
                e = $this.index876;
                $rt_nullCheck(c).call359(d, e, a, b);
            } catch ($e) {
                $je = $e.$javaException;
                if ($je && $je instanceof jl_Throwable111) {
                    f = $je;
                    break block2;
                } else {
                    throw $e;
                }
            }
            break block1;
        }
        jl_Throwable111_printStackTrace124($rt_nullCheck(f));
    }
    return;
}
function oahjs_FunctionBinding$Impl522_getFunctionName859($this) {
    return $this.name878;
}
oahjs_FunctionBinding$Impl522.$init523 = function(a, b, c, d) {
    var result = new oahjs_FunctionBinding$Impl522();
    result.$init523(a, b, c, d);
    return result;
}
$rt_virtualMethods(oahjs_FunctionBinding$Impl522,
    "call576", function(a, b) { oahjs_FunctionBinding$Impl522_call576(this, a, b); },
    "getFunctionName859", function() { return oahjs_FunctionBinding$Impl522_getFunctionName859(this); },
    "$init523", function(a, b, c, d) { oahjs_FunctionBinding$Impl522_$init523(this, a, b, c, d); });
function jl_StringBuilder16() {
}
$rt_declClass(jl_StringBuilder16, {
    name : "java.lang.StringBuilder",
    interfaces : [jl_Appendable731],
    superclass : jl_AbstractStringBuilder741,
    clinit : function() { jl_StringBuilder16_$clinit(); } });
function jl_StringBuilder16_$clinit() {
    jl_StringBuilder16_$clinit = function(){};
    jl_StringBuilder16_$init17 = function($this) {
        jl_AbstractStringBuilder741_$init755($this);
        return;
    }
}
$rt_methodStubs(jl_StringBuilder16_$clinit, ['jl_StringBuilder16_$init17']);
function jl_StringBuilder16_append19($this, a) {
    jl_AbstractStringBuilder741_append767($this, a);
    return $this;
}
function jl_StringBuilder16_length5($this) {
    return jl_AbstractStringBuilder741_length5($this);
}
function jl_StringBuilder16_insert880($this, a, b) {
    jl_AbstractStringBuilder741_insert758($this, a, b);
    return $this;
}
function jl_StringBuilder16_insert758($this, a, b) {
    return jl_StringBuilder16_insert880($rt_nullCheck($this), a, b);
}
function jl_StringBuilder16_append428($this, a) {
    jl_AbstractStringBuilder741_append759($this, a);
    return $this;
}
function jl_StringBuilder16_append725($this, a) {
    jl_AbstractStringBuilder741_append762($this, a);
    return $this;
}
function jl_StringBuilder16_toString15($this) {
    return jl_AbstractStringBuilder741_toString15($this);
}
function jl_StringBuilder16_append705($this, a) {
    jl_AbstractStringBuilder741_append766($this, a);
    return $this;
}
function jl_StringBuilder16_append18($this, a) {
    jl_AbstractStringBuilder741_append760($this, a);
    return $this;
}
function jl_StringBuilder16_append785($this, a) {
    jl_AbstractStringBuilder741_append763($this, a);
    return $this;
}
function jl_StringBuilder16_append537($this, a) {
    jl_AbstractStringBuilder741_append757($this, a);
    return $this;
}
function jl_StringBuilder16_setLength728($this, a) {
    jl_AbstractStringBuilder741_setLength728($this, a);
    return;
}
function jl_StringBuilder16_append767($this, a) {
    return jl_StringBuilder16_append19($rt_nullCheck($this), a);
}
function jl_StringBuilder16_getChars6($this, a, b, c, d) {
    jl_AbstractStringBuilder741_getChars6($this, a, b, c, d);
    return;
}
jl_StringBuilder16.$init17 = function() {
    var result = new jl_StringBuilder16();
    result.$init17();
    return result;
}
$rt_virtualMethods(jl_StringBuilder16,
    "append19", function(a) { return jl_StringBuilder16_append19(this, a); },
    "length5", function() { return jl_StringBuilder16_length5(this); },
    "insert880", function(a, b) { return jl_StringBuilder16_insert880(this, a, b); },
    "insert758", function(a, b) { return jl_StringBuilder16_insert758(this, a, b); },
    "append428", function(a) { return jl_StringBuilder16_append428(this, a); },
    "append725", function(a) { return jl_StringBuilder16_append725(this, a); },
    "toString15", function() { return jl_StringBuilder16_toString15(this); },
    "append705", function(a) { return jl_StringBuilder16_append705(this, a); },
    "append18", function(a) { return jl_StringBuilder16_append18(this, a); },
    "append785", function(a) { return jl_StringBuilder16_append785(this, a); },
    "append537", function(a) { return jl_StringBuilder16_append537(this, a); },
    "setLength728", function(a) { jl_StringBuilder16_setLength728(this, a); },
    "$init17", function() { jl_StringBuilder16_$init17(this); },
    "append767", function(a) { return jl_StringBuilder16_append767(this, a); },
    "getChars6", function(a, b, c, d) { jl_StringBuilder16_getChars6(this, a, b, c, d); });
function oadm_Square62() {
    this.prop_mine881 = false;
    this.prop_state882 = null;
    this.proto883 = null;
}
oadm_Square62.TYPE884 = null;
$rt_declClass(oadm_Square62, {
    name : "org.apidesign.demo.minesweeper.Square",
    interfaces : [jl_Cloneable262],
    superclass : jl_Object7,
    clinit : function() { oadm_Square62_$clinit(); } });
function oadm_Square62_$clinit() {
    oadm_Square62_$clinit = function(){};
    oadm_Square62_$clinit885 = function() {
        oadm_Square62.TYPE884 = oadm_Square$Html4JavaType795.$init825(null);
        return;
    }
    oadm_Square62_$init886 = function($this, a) {
        jl_Object7_$init10($this);
        $this.proto883 = oahjs_Proto$Type36_createProto349($rt_nullCheck(oadm_Square62.TYPE884), $this, a);
        return;
    }
    oadm_Square62_access$100833 = function() {
        return oadm_Square62.TYPE884;
    }
    oadm_Square62_$init806 = function($this) {
        oadm_Square62_$init886($this, njh_BrwsrCtx488_findDefault489($rt_cls(oadm_Square62)));
        return;
    }
    oadm_Square62_access$200832 = function(a) {
        return a.proto883;
    }
    oadm_Square62_$init63 = function($this, a, b) {
        oadm_Square62_$init886($this, njh_BrwsrCtx488_findDefault489($rt_cls(oadm_Square62)));
        $this.prop_state882 = a;
        $this.prop_mine881 = b;
        return;
    }
    oadm_Square62_$clinit885();
}
$rt_methodStubs(oadm_Square62_$clinit, ['oadm_Square62_$clinit885', 'oadm_Square62_$init886', 'oadm_Square62_access$100833', 'oadm_Square62_$init806', 'oadm_Square62_access$200832', 'oadm_Square62_$init63']);
function oadm_Square62_getState84($this) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto883));
    return $this.prop_state882;
}
function oadm_Square62_getStyle831($this) {
    var a, b;
    a = oadm_Square62_getState84($rt_nullCheck($this));
    block1: {
        try {
            oahjs_Proto321_acquireLock451($rt_nullCheck($this.proto883));
            a = oadm_MinesModel$SquareModel687_style689(a);
        } catch ($e) {
            $je = $e.$javaException;
            if ($je) {
                b = $je;
                break block1;
            } else {
                throw $e;
            }
        }
        oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto883));
        return a;
    }
    oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto883));
    $rt_throw(b);
}
function oadm_Square62_isMine71($this) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto883));
    return $this.prop_mine881;
}
function oadm_Square62_setMine72($this, a) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto883));
    if ((oahjs_Proto$Type36_isSame355($rt_nullCheck(oadm_Square62.TYPE884), jl_Boolean327_valueOf363($this.prop_mine881), jl_Boolean327_valueOf363(a)) == 0)) {
        $this.prop_mine881 = a;
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto883), $rt_str("mine"));
        return;
    }
    return;
}
function oadm_Square62_setState88($this, a) {
    oahjs_Proto321_verifyUnlocked453($rt_nullCheck($this.proto883));
    if ((oahjs_Proto$Type36_isSame355($rt_nullCheck(oadm_Square62.TYPE884), $this.prop_state882, a) == 0)) {
        $this.prop_state882 = a;
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto883), $rt_str("state"));
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto883), $rt_str("html"));
        oahjs_Proto321_valueHasMutated456($rt_nullCheck($this.proto883), $rt_str("style"));
        return;
    }
    return;
}
function oadm_Square62_getHtml830($this) {
    var a, b;
    a = oadm_Square62_getState84($rt_nullCheck($this));
    block1: {
        try {
            oahjs_Proto321_acquireLock451($rt_nullCheck($this.proto883));
            a = oadm_MinesModel$SquareModel687_html690(a);
        } catch ($e) {
            $je = $e.$javaException;
            if ($je) {
                b = $je;
                break block1;
            } else {
                throw $e;
            }
        }
        oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto883));
        return a;
    }
    oahjs_Proto321_releaseLock454($rt_nullCheck($this.proto883));
    $rt_throw(b);
}
oadm_Square62.$init886 = function(a) {
    var result = new oadm_Square62();
    result.$init886(a);
    return result;
}
oadm_Square62.$init806 = function() {
    var result = new oadm_Square62();
    result.$init806();
    return result;
}
oadm_Square62.$init63 = function(a, b) {
    var result = new oadm_Square62();
    result.$init63(a, b);
    return result;
}
$rt_virtualMethods(oadm_Square62,
    "getState84", function() { return oadm_Square62_getState84(this); },
    "getStyle831", function() { return oadm_Square62_getStyle831(this); },
    "isMine71", function() { return oadm_Square62_isMine71(this); },
    "$init886", function(a) { oadm_Square62_$init886(this, a); },
    "setMine72", function(a) { oadm_Square62_setMine72(this, a); },
    "$init806", function() { oadm_Square62_$init806(this); },
    "setState88", function(a) { oadm_Square62_setState88(this, a); },
    "$init63", function(a, b) { oadm_Square62_$init63(this, a, b); },
    "getHtml830", function() { return oadm_Square62_getHtml830(this); });
function ju_Collections$6201() {
    this.val$list887 = null;
}
$rt_declClass(ju_Collections$6201, {
    name : "java.util.Collections$6",
    superclass : ju_AbstractList66,
    clinit : function() { ju_Collections$6201_$clinit(); } });
function ju_Collections$6201_$clinit() {
    ju_Collections$6201_$clinit = function(){};
    ju_Collections$6201_$init202 = function($this, a) {
        $this.val$list887 = a;
        ju_AbstractList66_$init221($this);
        return;
    }
}
$rt_methodStubs(ju_Collections$6201_$clinit, ['ju_Collections$6201_$init202']);
function ju_Collections$6201_get58($this, a) {
    return $rt_nullCheck($this.val$list887).get58(a);
}
function ju_Collections$6201_size55($this) {
    return $rt_nullCheck($this.val$list887).size55();
}
ju_Collections$6201.$init202 = function(a) {
    var result = new ju_Collections$6201();
    result.$init202(a);
    return result;
}
$rt_virtualMethods(ju_Collections$6201,
    "$init202", function(a) { ju_Collections$6201_$init202(this, a); },
    "get58", function(a) { return ju_Collections$6201_get58(this, a); },
    "size55", function() { return ju_Collections$6201_size55(this); });
function onhji_JSONList76() {
    this.index888 = 0;
    this.name889 = null;
    this.deps890 = null;
    this.proto891 = null;
}
$rt_declClass(onhji_JSONList76, {
    name : "org.netbeans.html.json.impl.JSONList",
    superclass : ju_ArrayList54,
    clinit : function() { onhji_JSONList76_$clinit(); } });
function onhji_JSONList76_$clinit() {
    onhji_JSONList76_$clinit = function(){};
    onhji_JSONList76_koData408 = function(a, b) {
        var c, d, e;
        c = $rt_nullCheck(a);
        a = ju_AbstractCollection204_toArray205(c, $rt_createArray(jl_Object7, c.size55()));
        c = 0;
        while (true) {
            d = a.data;
            if ((c >= d.length)) {
                break;
            }
            e = onhji_JSON40_find41(d[c], b);
            if ((e !== null)) {
                d[c] = e;
            }
            c = ((c + 1) | 0);
        }
        return onhji_Bindings406_wrapArray632($rt_nullCheck(b), a);
    }
    onhji_JSONList76_init458 = function(a, b) {
        var c, d;
        if ((b !== null)) {
            c = jlr_Array150_getLength297(b);
            if ((c != 0)) {
                d = 0;
                while ((d < c)) {
                    onhji_JSONList76_add67($rt_nullCheck(a), jlr_Array150_get303(b, d));
                    d = ((d + 1) | 0);
                }
                return;
            }
        }
        return;
    }
    onhji_JSONList76_$init443 = function($this, a, b, c, d) {
        ju_ArrayList54_$init80($this);
        $this.proto891 = a;
        $this.name889 = b;
        $this.deps890 = d;
        $this.index888 = c;
        return;
    }
}
$rt_methodStubs(onhji_JSONList76_$clinit, ['onhji_JSONList76_koData408', 'onhji_JSONList76_init458', 'onhji_JSONList76_$init443']);
function onhji_JSONList76_clear77($this) {
    ju_ArrayList54_clear77($this);
    onhji_JSONList76_notifyChange892($this);
    return;
}
function onhji_JSONList76_add224($this, a, b) {
    ju_ArrayList54_add224($this, a, b);
    onhji_JSONList76_notifyChange892($this);
    return;
}
function onhji_JSONList76_notifyChange892($this) {
    var a, b, c, d;
    a = onhji_PropertyBindingAccessor404_getBindings405($this.proto891, 0);
    if ((a !== null)) {
        b = $this.name889;
        c = $rt_nullCheck(a);
        onhji_Bindings406_valueHasMutated456(c, b);
        a = $this.deps890.data;
        b = a.length;
        d = 0;
        while ((d < b)) {
            onhji_Bindings406_valueHasMutated456(c, a[d]);
            d = ((d + 1) | 0);
        }
        if (($this.index888 >= 0)) {
            onhji_PropertyBindingAccessor404_notifyProtoChange502($this.proto891, $this.index888);
        }
    }
    return;
}
function onhji_JSONList76_add67($this, a) {
    a = ju_AbstractList66_add67($this, a);
    onhji_JSONList76_notifyChange892($this);
    return a;
}
function onhji_JSONList76_addAll78($this, a) {
    a = ju_AbstractCollection204_addAll78($this, a);
    onhji_JSONList76_notifyChange892($this);
    return a;
}
function onhji_JSONList76_koData407($this) {
    return onhji_JSONList76_koData408($this, onhji_PropertyBindingAccessor404_getBindings405($this.proto891, 1));
}
function onhji_JSONList76_init459($this, a) {
    var b, c;
    if ((a !== null)) {
        b = jlr_Array150_getLength297(a);
        if ((b != 0)) {
            c = 0;
            while ((c < b)) {
                ju_AbstractList66_add67($this, jlr_Array150_get303(a, c));
                c = ((c + 1) | 0);
            }
            return;
        }
    }
    return;
}
onhji_JSONList76.$init443 = function(a, b, c, d) {
    var result = new onhji_JSONList76();
    result.$init443(a, b, c, d);
    return result;
}
$rt_virtualMethods(onhji_JSONList76,
    "clear77", function() { onhji_JSONList76_clear77(this); },
    "add224", function(a, b) { onhji_JSONList76_add224(this, a, b); },
    "notifyChange892", function() { onhji_JSONList76_notifyChange892(this); },
    "add67", function(a) { return onhji_JSONList76_add67(this, a); },
    "$init443", function(a, b, c, d) { onhji_JSONList76_$init443(this, a, b, c, d); },
    "addAll78", function(a) { return onhji_JSONList76_addAll78(this, a); },
    "koData407", function() { return onhji_JSONList76_koData407(this); },
    "init459", function(a) { onhji_JSONList76_init459(this, a); });
function jl_AssertionError316() {
}
$rt_declClass(jl_AssertionError316, {
    name : "java.lang.AssertionError",
    superclass : jl_Error208,
    clinit : function() { jl_AssertionError316_$clinit(); } });
function jl_AssertionError316_$clinit() {
    jl_AssertionError316_$clinit = function(){};
    jl_AssertionError316_$init317 = function($this) {
        jl_Error208_$init209($this);
        return;
    }
}
$rt_methodStubs(jl_AssertionError316_$clinit, ['jl_AssertionError316_$init317']);
jl_AssertionError316.$init317 = function() {
    var result = new jl_AssertionError316();
    result.$init317();
    return result;
}
$rt_virtualMethods(jl_AssertionError316,
    "$init317", function() { jl_AssertionError316_$init317(this); });
function onhbi_FnContext473() {
    this.current893 = null;
    this.prev894 = null;
}
onhbi_FnContext473.LOG895 = null;
onhbi_FnContext473.CURRENT896 = null;
onhbi_FnContext473.DUMMY897 = null;
$rt_declClass(onhbi_FnContext473, {
    name : "org.netbeans.html.boot.impl.FnContext",
    interfaces : [ji_Closeable215],
    superclass : jl_Object7,
    clinit : function() { onhbi_FnContext473_$clinit(); } });
function onhbi_FnContext473_$clinit() {
    onhbi_FnContext473_$clinit = function(){};
    onhbi_FnContext473_$clinit898 = function() {
        onhbi_FnContext473.LOG895 = jul_Logger681_getLogger682(jl_Class0_getName20($rt_nullCheck($rt_cls(onhbi_FnContext473))));
        onhbi_FnContext473.DUMMY897 = onhbi_FnContext473.$init899(null, null);
        onhbi_FnContext473.DUMMY897.prev894 = onhbi_FnContext473.DUMMY897;
        onhbi_FnContext473.CURRENT896 = jl_ThreadLocal670.$init673();
        return;
    }
    onhbi_FnContext473_$init899 = function($this, a, b) {
        jl_Object7_$init10($this);
        $this.current893 = b;
        $this.prev894 = a;
        return;
    }
    onhbi_FnContext473_currentPresenter476 = function(a) {
        var b;
        b = jl_ThreadLocal670_get675($rt_nullCheck(onhbi_FnContext473.CURRENT896));
        if ((!((b === null) && (a != 0)))) {
            return b;
        }
        $rt_throw(jl_IllegalStateException229.$init641($rt_str("No current WebView context around!")));
    }
    onhbi_FnContext473_activate474 = function(a) {
        var b;
        b = onhbi_FnContext473_currentPresenter900(a);
        if ((b !== a)) {
            return onhbi_FnContext473.$init899(b, a);
        }
        return onhbi_FnContext473.DUMMY897;
    }
    onhbi_FnContext473_currentPresenter900 = function(a) {
        var b;
        b = jl_ThreadLocal670_get675($rt_nullCheck(onhbi_FnContext473.CURRENT896));
        jl_ThreadLocal670_set676($rt_nullCheck(onhbi_FnContext473.CURRENT896), a);
        return b;
    }
    onhbi_FnContext473_$clinit898();
}
$rt_methodStubs(onhbi_FnContext473_$clinit, ['onhbi_FnContext473_$clinit898', 'onhbi_FnContext473_$init899', 'onhbi_FnContext473_currentPresenter476', 'onhbi_FnContext473_activate474', 'onhbi_FnContext473_currentPresenter900']);
function onhbi_FnContext473_close661($this) {
    if (($this.prev894 !== $this)) {
        onhbi_FnContext473_currentPresenter900($this.prev894);
        $this.prev894 = $this;
        if (($rt_isInstance($this.current893, ji_Flushable477) != 0)) {
            $rt_nullCheck($this.current893).flush901();
        }
    }
    return;
}
onhbi_FnContext473.$init899 = function(a, b) {
    var result = new onhbi_FnContext473();
    result.$init899(a, b);
    return result;
}
$rt_virtualMethods(onhbi_FnContext473,
    "$init899", function(a, b) { onhbi_FnContext473_$init899(this, a, b); },
    "close661", function() { onhbi_FnContext473_close661(this); });
function ju_ConcurrentModificationException569() {
}
$rt_declClass(ju_ConcurrentModificationException569, {
    name : "java.util.ConcurrentModificationException",
    superclass : jl_RuntimeException131,
    clinit : function() { ju_ConcurrentModificationException569_$clinit(); } });
function ju_ConcurrentModificationException569_$clinit() {
    ju_ConcurrentModificationException569_$clinit = function(){};
    ju_ConcurrentModificationException569_$init570 = function($this) {
        jl_RuntimeException131_$init132($this);
        return;
    }
}
$rt_methodStubs(ju_ConcurrentModificationException569_$clinit, ['ju_ConcurrentModificationException569_$init570']);
ju_ConcurrentModificationException569.$init570 = function() {
    var result = new ju_ConcurrentModificationException569();
    result.$init570();
    return result;
}
$rt_virtualMethods(ju_ConcurrentModificationException569,
    "$init570", function() { ju_ConcurrentModificationException569_$init570(this); });
function oadm_MinesModel$GameState73() {
}
oadm_MinesModel$GameState73.WON89 = null;
oadm_MinesModel$GameState73.LOST104 = null;
oadm_MinesModel$GameState73.$VALUES902 = null;
oadm_MinesModel$GameState73.IN_PROGRESS74 = null;
$rt_declClass(oadm_MinesModel$GameState73, {
    name : "org.apidesign.demo.minesweeper.MinesModel$GameState",
    enum : true,
    superclass : jl_Enum102,
    clinit : function() { oadm_MinesModel$GameState73_$clinit(); } });
function oadm_MinesModel$GameState73_$clinit() {
    oadm_MinesModel$GameState73_$clinit = function(){};
    oadm_MinesModel$GameState73_$clinit903 = function() {
        var a, b, c, d;
        oadm_MinesModel$GameState73.IN_PROGRESS74 = oadm_MinesModel$GameState73.$init904($rt_str("IN_PROGRESS"), 0);
        oadm_MinesModel$GameState73.WON89 = oadm_MinesModel$GameState73.$init904($rt_str("WON"), 1);
        oadm_MinesModel$GameState73.LOST104 = oadm_MinesModel$GameState73.$init904($rt_str("LOST"), 2);
        a = $rt_createArray(oadm_MinesModel$GameState73, 3);
        b = 0;
        c = oadm_MinesModel$GameState73.IN_PROGRESS74;
        d = a.data;
        d[b] = c;
        d[1] = oadm_MinesModel$GameState73.WON89;
        d[2] = oadm_MinesModel$GameState73.LOST104;
        oadm_MinesModel$GameState73.$VALUES902 = a;
        return;
    }
    oadm_MinesModel$GameState73_$init904 = function($this, a, b) {
        jl_Enum102_$init611($this, a, b);
        return;
    }
    oadm_MinesModel$GameState73_$clinit903();
}
$rt_methodStubs(oadm_MinesModel$GameState73_$clinit, ['oadm_MinesModel$GameState73_$clinit903', 'oadm_MinesModel$GameState73_$init904']);
oadm_MinesModel$GameState73.$init904 = function(a, b) {
    var result = new oadm_MinesModel$GameState73();
    result.$init904(a, b);
    return result;
}
$rt_virtualMethods(oadm_MinesModel$GameState73,
    "$init904", function(a, b) { oadm_MinesModel$GameState73_$init904(this, a, b); });
function VM() {
}
VM.prototype.loadClass = function(className) {
    switch (className) {
        case "org.apidesign.demo.minesweeper.MainBrwsr": oadm_MainBrwsr227.$clinit(); break;
        default: throw "Can't load class " + className;
    }
}
