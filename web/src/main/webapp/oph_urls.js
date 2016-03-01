"use strict";

/**
 * window.url("service.info", param1, param2, {key3: value})
 *
 * window.urls(baseUrl).url(key, param)
 * window.urls(baseUrl, {encode: false}).url(key, param)
 * window.urls({baseUrl: baseUrl, encode: false}).url(key, param)
 *
 * Config lookup order: urls_config, window.urls.override, window.urls.properties, window.urls.defaults
 * Lookup key order:
 * * for main url window.url's first parameter: "service.info" from all configs
 * * baseUrl: "service.baseUrl" from all configs and "baseUrl" from all configs
 *
 * window.url_properties = {
 *   "service.status": "/rest/status",
 *   "service.payment": "/rest/payment/$1",
 *   "service.order": "/rest/payment/$orderId"
 *   }
 *
 * window.urls.defaults = {
 *   encode: true
 * }
 */

(function(exportDest) {
    exportDest.urls = function() {
        var urls_config = {}

        for (var i = 0; i < arguments.length;  i++) {
            var arg = arguments[i]
            if(typeof arg === "string" || arg == null) {
                urls_config.baseUrl = arg
            } else {
                Object.keys(arg).forEach(function(key){
                    urls_config[key] = arg[key]
                })
            }
        }

        var resolveConfig = function(key, defaultValue) {
            var configs = [urls_config, exportDest.urls.override, exportDest.urls.properties, exportDest.urls.defaults]
            for (var i = 0; i < configs.length; i++) {
                var c = configs[i]
                if(c.hasOwnProperty(key)) {
                    return c[key]
                }
            }
            if(typeof defaultValue == 'function') {
                return defaultValue()
            }
            if(typeof defaultValue == 'undefined') {
                throw new Error("Could not resolve value for '"+key+"'")
            }
            return defaultValue
        }

        var enc = function(arg) {
            arg = arg !== undefined ? arg : ""
            if(resolveConfig("encode")) {
                arg = encodeURIComponent(arg)
            }
            return arg
        }

        return {
            url: function() {
                var key = Array.prototype.shift.apply(arguments)
                var args = Array.prototype.slice.call(arguments)
                var queryString = "";
                var tmpUrl;
                if(!key) {
                    throw new Error("first parameter 'key' not defined!");
                }
                var url = resolveConfig(key)
                for (var i = args.length; i > 0; i--) {
                    var arg = args[i-1];
                    if(typeof arg === "object") {
                        Object.keys(arg).forEach(function(k){
                            var value = enc(arg[k])
                            tmpUrl = url.replace("$" + k, value)
                            if(tmpUrl == url) {
                                if(queryString.length > 0 ) {
                                    queryString = queryString + "&"
                                } else {
                                    queryString = "?"
                                }
                                queryString = queryString + enc(k) + "=" + value
                            }
                            url = tmpUrl
                        })
                    } else {
                        var value = enc(arg)
                        url = url.replace("$"+i, value)
                    }
                }
                var baseUrl = resolveConfig(parseService(key)+".baseUrl", function(){return resolveConfig("baseUrl", null)})
                if(baseUrl) {
                    url = joinUrl(baseUrl, url)
                }
                return url + queryString
            }
        }
    }

    exportDest.urls.properties = {}

    exportDest.urls.defaults = {
        encode: true
    }
    exportDest.urls.override = {}

    function ajaxJson(method, url, onload, onerror) {
        var oReq = new XMLHttpRequest();
        if(onload) {
            oReq.onload = function (e) {
                onload(e.target.response)
            };
        }
        if(onerror) {
            oReq.onerror = function (e) {
                onload(e.target.response)
            };
        }
        oReq.open(method, url, true);
        oReq.responseType = 'json';
        oReq.send();
    }

    // minimalist angular Promise implementation, returns object with .success(cb)
    var successCBs = []
    var fulfilled = false, fulfillFailed = false
    var fulfillCount = 0, fulfillCountDest = 0
    function checkfulfill() {
        fulfillCount += 1
        if(fulfillCount == fulfillCountDest) {
            fulfilled = true
            if(!fulfillFailed) {
                successCBs.forEach(function(cb){cb()})
            }
        }
    }
    exportDest.urls.success = function(cb) {
        if(fulfilled) {
            if(!fulfillFailed) {
                cb()
            }
        } else {
            successCBs.push(cb)
        }
    }

    exportDest.urls.loadFromUrls = function() {
        var args = Array.prototype.slice.call(arguments)
        var jsonProperties = []
        successCBs.push(function(){
            jsonProperties.forEach(function(json){merge(exportDest.urls.properties, json)})
        })
        fulfillCountDest += args.length
        args.forEach(function(url, index){
            ajaxJson("GET", url, function(data) {
                jsonProperties.splice(index, 0, data)
                checkfulfill()
            }, function() {
                fulfillFailed = true
                checkfulfill()
            })
        })
        return {
            success: exportDest.urls.success
        };
    }

    function merge(dest, from) {
        Object.keys(from).forEach(function(key){
            dest[key]=from[key];
        })
    }

    exportDest.url = exportDest.urls().url

    function parseService (key) {
        return key.substring(0, key.indexOf("."))
    }

    function joinUrl() {
        var args = Array.prototype.slice.call(arguments)
        if(args.length === 0) {
            throw new Error("no arguments");
        }
        var url = null
        args.forEach(function(arg) {
            if(!url) {
                url = arg
            } else {
                if(url.endsWith("/") || arg.startsWith("/")) {
                    url = url + arg
                } else {
                    url = url + "/" + arg
                }
            }
        })
        return url
    }
})(typeof window === 'undefined' ? module.exports : window);

// polyfills for IE

if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
        position = position || 0;
        return this.substr(position, searchString.length) === searchString;
    };
}

if (!String.prototype.endsWith) {
    String.prototype.endsWith = function(searchString, position) {
        var subjectString = this.toString();
        if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
            position = subjectString.length;
        }
        position -= searchString.length;
        var lastIndex = subjectString.indexOf(searchString, position);
        return lastIndex !== -1 && lastIndex === position;
    };
}
