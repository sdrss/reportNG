function toggleElement(elementId, displayStyle) {
    var element = document.getElementById(elementId);
    var current = window.getComputedStyle(element).getPropertyValue('display');
    element.style.display = (current == 'none' ? displayStyle : 'none');
}

function changeIcon(element) {
	if ($(element).hasClass('glyphicon glyphicon-menu-down')){
		$(element).removeClass('glyphicon glyphicon-menu-down').toggleClass('glyphicon glyphicon-menu-left');
	}else{
		$(element).removeClass('glyphicon glyphicon-menu-left').toggleClass('glyphicon glyphicon-menu-down');
	}
}

function changeSingleIcon(elementId) {
	if ($('#'+elementId).hasClass('glyphicon glyphicon-menu-down')){
		$('#'+elementId).removeClass('glyphicon glyphicon-menu-down').toggleClass('glyphicon glyphicon-menu-left');
	}else{
		$('#'+elementId).removeClass('glyphicon glyphicon-menu-left').toggleClass('glyphicon glyphicon-menu-down');
	}
}

function getStyle(elementId, property) {
    return window.getComputedStyle(document.getElementById(elementId)).getPropertyValue(property);
}

function toggle(toggleId) {
    var toggleElement = document.getElementById(toggleId);
    toggleElement.textContent = toggleElement.textContent == '\u25b6' ? '\u25bc' : '\u25b6';
}

function doParse() {
    setTimeout(doParse2, 50);
}

function doParse2() {
    var x = document.getElementsByClassName("text");
    var output = document.getElementsByClassName("output");
    for (var i = 0; i < x.length; i++) {
        var value = x[i].value;
        var result = parse(escapeHTML(value), null);
        if (result !== null) {
            output[i].innerHTML = result;
        }
    }
}

function parse(str) {
    elementCount = 0;
    arrayCount = 0;
    objectCount = 0;
    var obj = null;
    try {
        obj = JSON.parse(str);
    } catch (e) {
        return;
    }
    return parseValue(obj, null, null);
}

function parseValue(val, parent, level) {
    elementCount++;
    if (parent === null) parent = "";
    if (level === null) level = 1;

    if (typeof(val) == "object") {
        if (level > nestingLevel) nestingLevel = level;
        if (val instanceof Array) {
            arrayCount++;
            parent = parent + (parent !== "" ? " > " : "") + "Array (" + val.length + " item" + (val.length != 1 ? "s)" : ")");
            var out = "<div class='wrapNo'>\n<div class='array'>\n<div class='widgets'></div>\n<h3><span class='titled' title='" + parent + "'>Array</span></h3>\n";
            if (val.length > 0) {
                out += "<table class='arraytable'>\n<tr><th>Index</th><th>Value</th></tr>\n";
                for (var idx = 0; idx < val.length; idx++) {
                    if (typeof(val[idx]) == "function") continue;
                    out += "<tr><td>" + idx + "</td><td>" + parseValue(val[idx], parent, level + 1) + "</td></tr>\n";
                }
                out += "</table>\n";
            } else {
                return "(empty <span class='titled' title='" + parent + "'>Array</span>)\n";
            }
            out += "</div>\n</div>\n";
            return out;
        } else {
            objectCount++;
            var count = 0;
            var prop;
            for (prop in val) {
                if (typeof(val[prop]) != "function") count++;
            }
            parent = parent + (parent !== "" ? " > " : "") + "Object (" + count + " item" + (count != 1 ? "s)" : ")");
            var out = "<div class='wrapNo'>\n<div class='object'>\n<div class='widgets'></div>\n<h3><span class='titled' title='" + parent + "'>Object</span></h3>\n";
            if (count > 0) {
                out += "<table class='objecttable'>\n<tr><th>Name</th><th>Value</th></tr>\n";
                for (prop in val) {
                    if (typeof(val[prop]) == "function") continue;
                    out += "<tr><td>" + prop + "</td><td>" + parseValue(val[prop], parent, level + 1) + "</td></tr>\n";
                }
                out += "</table><div class='clear'></div>\n";
            } else {
                return "(empty <span class='titled' title='" + parent + "'>Object</span>)\n";
            }
            out += "</div>\n</div>\n";
            return out;
        }
    } else {
        if (typeof(val) == "string") return "<span class='string'>" + wordwrap(val.replace(/\n/g, "<br />")) + "</span>";
        else if (typeof(val) == "number") return "<span class='number'>" + val + "</span>";
        else if (typeof(val) == "boolean") return "<span class='boolean'>" + val + "</span>";
        else return "<span class='void'>(null)</span>";
    }
}

function escapeHTML(str) {
    var div = document.createElement('div');
    var text = document.createTextNode(str);
    div.appendChild(text);
    return div.innerHTML;
}

function wordwrap(str) {
    var parts = str.split(" ");
    for (var i = 0; i < parts.length; i++) {
        if (parts[i].length <= 30) continue;
        var p = "";
        for (var j = 0; j < (parts[i].length - 30); j += 30) p += parts[i].substring(j, j + 30) + "<wbr />";
        parts[i] = p + parts[i].substring(j, parts[i].length);
    }
    return parts.join(" ");
}

var elementCount = 0;
var arrayCount = 0;
var objectCount = 0;
var nestingLevel = 0;
var currentlyFocused = null;

function doFocus(event, ele) {
    if (currentlyFocused !== null) currentlyFocused.style.border = "1px solid #000000";
    ele.style.border = "1px solid #ffa000";
    currentlyFocused = ele;
    if (!event) event = window.event;
    event.stopPropagation();
}

function collapseExpand(element) {
    var allStackTraceElements = document.getElementsByClassName("stackTrace");
    for (var i = 0; i < allStackTraceElements.length; i++) {
        toggleElement(allStackTraceElements[i].id, 'block');
    }
    var allelementsIcons = document.getElementsByClassName("glyphicon");
    for (var i = 0; i < allelementsIcons.length; i++) {
        changeIcon(allelementsIcons[i]);
    }
    var btn = $("#hideResults");
    if (btn.hasClass('btn-default')) {
        btn.removeClass('btn-default').addClass('btn-danger');
        btn.text('Show Only Failures');
    } else {
        btn.removeClass('btn-danger').addClass('btn-default');
        btn.text('Show All');
    }
}

function getPercentage(int1, int2, int3, int4, int5, variable) {
    var summary = (Number(int1) + Number(int2) + Number(int3) + Number(int4) + Number(int5));
	var percentage = 100 * variable / summary;
	if (percentage > 0) {
		return percentage.toFixed(2);
	} else {
		return 0;
	}
}

function hidePass(element) {
	var well = $('div.well');
	var suitefooter = $('tr.suite');
	for (var i = 0; i < suitefooter.length; i++) {
		var foundAllPass = 0;
		var elements = suitefooter[i].parentElement.getElementsByTagName("td");
		for (var j = 5; j < elements.length - 1; j++) {
   		if (elements[j].innerText != '0') {
   			foundAllPass = 1;
				break;
   		}
   	}
   	if (foundAllPass == 0) {
			toggleElement(suitefooter[i].id, '');
			toggleElement(suitefooter[i].parentElement.parentElement.id, '');
			toggleElement(well[i].id, '');
		}
	}

	var test = $('tr.test');
	for (var i = 0; i < test.length; i++) {
		var pass = 0;
		var elements = test[i].getElementsByTagName("td");
   	for (var j = 5; j < elements.length - 1; j++) {
   		if (elements[j].innerText != '0') {
   			pass = 1;
				break;
   		}
   	}
   	if (pass == 0) {
			toggleElement(test[i].id, '');
		}
	}

	var btn = $("#hideResults");
	if (btn.hasClass('btn-danger')) {
		btn.removeClass('btn-danger').addClass('btn-default');
		btn.text('Show Only Failures');
	} else {
		btn.removeClass('btn-default').addClass('btn-danger');
		btn.text('Show All');
	}
}

function copyToClipboard(element) {
	var text = $(element).text();
	if (navigator.clipboard && navigator.clipboard.writeText) {
		navigator.clipboard.writeText(text);
	} else {
		var $temp = $("<input>");
		$("body").append($temp);
		$temp.val(text).select();
		document.execCommand("copy");
		$temp.remove();
	}
}
