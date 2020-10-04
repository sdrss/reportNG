function toggleElement(elementId, displayStyle) {
    var current = getStyle(elementId, 'display');
    document.getElementById(elementId).style.display = (current == 'none' ? displayStyle : 'none');
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
    var element = document.getElementById(elementId);
    return element.currentStyle ? element.currentStyle[property] : document.defaultView.getComputedStyle(element, null).getPropertyValue(property);
}

function toggle(toggleId) {
    var toggleElement;
    if (document.getElementById) {
        toggleElement = document.getElementById(toggleId);
    } else if (document.all) {
        toggleElement = document.all[toggleId];
    }
    toggleElement.textContent = toggle.innerHTML == '\u25b6' ? '\u25bc' : '\u25b6';
}

function doParse() {
    setTimeout(doParse2, 50);
}

function doParse2() {
    var x = document.getElementsByClassName("text");
    var output = document.getElementsByClassName("output");
    var i;
    for (i = 0; i < x.length; i++) {
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
        obj = str.parseJSON();
    } catch (e) {
        if (e instanceof SyntaxError) {
            $("text").focus();
            return;
        }
        $("text").focus();
        return
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
                for (var prop in val) {
                    if (typeof(val[prop]) == "function") continue;
                    out += "<tr><td>" + prop + "</td><td>" + parseValue(val[prop], parent, level + 1) + "</td></tr>\n";
                }
                out += "</table>\n";
            } else {
                return "(empty <span class='titled' title='" + parent + "'>Array</span>)\n";
            }
            out += "</div>\n</div>\n";
            return out;
        } else {
            objectCount++;
            i = 0;
            for (prop in val) {
                if (typeof(val[prop]) != "function") i++;
            }
            parent = parent + (parent !== "" ? " > " : "") + "Object (" + i + " item" + (i != 1 ? "s)" : ")");
            var out = "<div class='wrapNo'>\n<div class='object'>\n<div class='widgets'></div>\n<h3><span class='titled' title='" + parent + "'>Object</span></h3>\n";
            if (i > 0) {
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

function $(ele) {
    var t = document.getElementById(ele);
    if (t === null) t = document.getElementsByName(ele);
    if (t.length == 1) t = t.item(0);
    return t;
}

function escapeHTML(str) {
    var div = document.createElement('div');
    var text = document.createTextNode(str);
    div.appendChild(text);
    return div.innerHTML;
}

function wordwrap(str) {
    parts = str.split(" ");
    for (i = 0; i < parts.length; i++) {
        if (parts[i].length <= 30) continue;
        t = parts[i].length;
        p = "";
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
    event.cancelBubble = true;
    if (event.stopPropagation) event.stopPropagation();
}

function collapseExpand(element) {
    var allStackTraceElements = document.getElementsByClassName("stackTrace");
    for (i = 0; i < allStackTraceElements.length; i++) {
        toggleElement(allStackTraceElements[i].id, 'block');
    }
    var allelementsIcons = document.getElementsByClassName("glyphicon");
    for (i = 0; i < allelementsIcons.length; i++) {
        changeIcon(allelementsIcons[i]);
    }
    if ($("#hideResults").hasClass('btn-default')){
		$("#hideResults").removeClass('btn-default').toggleClass('btn-danger');
		$("#hideResults").text('Show Only Failures');
	}else{
		$("#hideResults").removeClass('btn-danger').toggleClass('btn-default');
		$("#hideResults").text('Show All');
	}
}

function getPercentage(int1, int2, int3, int4, int5, variable) {
    var summary = (Number(int1) + Number(int2) + Number(int3) + Number(int4) + Number(int5));
	var percentage = 100 * variable / summary;
	if(percentage>0){
		return percentage.toFixed(2);
	}else{
		return 0;
	}
}

function hidePass(element) {
	var well = $('div.well');
	var suite = $('div.suite');
	var suitefooter = $('tr.suite');
	for(var i = 0; i < suitefooter.length; i++){
		var foundAllPass = 0;
		var elements = suitefooter[i].parentElement.getElementsByTagName("td");
		for(var j=5; j<elements.length-1; j++){
   		if (elements[j].innerText != '0'){
   			foundAllPass = 1;
				break;
   		}
   	}
   	if(foundAllPass==0){
			toggleElement(suitefooter[i], '');
			toggleElement(suitefooter[i].parentElement.parentElement, '');
			toggleElement(suite[i], '');
			toggleElement(well[i], '');
		}
	 }
	
	var test = $('tr.test');
	for (var i = 0; i < test.length; i++) {
		var pass = 0;
		var elements = test[i].getElementsByTagName("td");
   	for(var j=5; j<elements.length-1; j++){
   		if (elements[j].innerText != '0'){
   			pass = 1;
				break;
   		}
   	}
   	if(pass==0){
			toggleElement(test[i], '');
		}
	}
	
	if ($("#hideResults").hasClass('btn-danger')){
		$("#hideResults").removeClass('btn-danger').toggleClass('btn-default');
		$("#hideResults").text('Show Only Failures');
	}else{
		$("#hideResults").removeClass('btn-default').toggleClass('btn-danger');
		$("#hideResults").text('Show All');
	}
}

function copyToClipboard(element) {
	var $temp = $("<input>");
  	$("body").append($temp);
  	$temp.val($(element).text()).select();
  	document.execCommand("copy");
  	$temp.remove();
}