// For conditions of distribution and use, see copyright notice in LICENSE

/**
 * @author Toni Dahl
 */

(function (namespace, undefined) {
    "use strict";

    namespace.Utils = (function () {


        return {

            log: function () {
                var str, el, arg, i;
                var args = Array.prototype.slice.call(arguments);

                for (i = args.length; i--;) {
                    arg = args[i];
                    if(typeof arg === 'object') {
                        arg = JSON.stringify(arg)
                    } else {
                        arg = arg.toString()
                    }
                    args[i] = arg;
                }


                if (args.length > 0) {
                    str = args.join(' ');
                } else {
                    return;
                }

                el = document.getElementById("log");
                if (el !== null) {
                    el.innerHTML +=
                        "[" + new Date().toTimeString().replace(/.*(\d{2}:\d{2}:\d{2}).*/, "$1") + "] " + str + "<br />";
                    el.scrollTop = el.scrollHeight;
                }

                console.log(str);
            },


            //Polyfills

            extend: function (orig) {
                if (orig === null) {
                    return orig;
                }

                var i, argsLen = arguments.length,
                    obj, prop, getter, setter;

                for (i = 1; i < argsLen; i++) {
                    obj = arguments[i];

                    if (obj !== null) {
                        for (prop in obj) {
                            if (obj.hasOwnProperty(prop)) {
                                getter = obj.__lookupGetter__(prop);
                                setter = obj.__lookupSetter__(prop);

                                if (getter || setter) {
                                    if (getter) {
                                        orig.__defineGetter__(prop, getter);
                                    }
                                    if (setter) {
                                        orig.__defineSetter__(prop, setter);
                                    }
                                } else {
                                    orig[prop] = obj[prop];
                                }
                            }
                        }
                    }
                }

                return orig;
            },

            toType: (function (global) {
                return function (obj) {
                    if (obj === global) {
                        return "global";
                    }
                    return ({}).toString.call(obj).match(/\s([a-z|A-Z]+)/)[1].toLowerCase();
                };
            })(this),

            createEnum: (function () {
                function assign(name, i) {
                    this[name] = i;
                }

                var forEach = [].forEach,
                    freeze = Object.freeze || function (o) {
                        return o;
                    };

                return function () {
                    var e = {};
                    forEach.call(arguments, assign, e);
                    return freeze(e);
                };
            }()),

            innerWidth: function (el) {
                if (el instanceof window.Element) {
                    return el.clientWidth;
                } else if (el instanceof window.Document || el instanceof window.Window) {
                    return el.width;
                }
                throw new TypeError(["Could not get innerWidth of given object."]);
            },
            innerHeight: function (el) {
                if (el instanceof window.Element) {
                    return el.clientHeight;
                } else if (el instanceof window.Document || el instanceof window.Window) {
                    return el.height;
                }
                throw new TypeError(["Could not get innerHeight of given object."]);
            }

        };
    }());


}(window['wex'] = window['wex'] || {}));