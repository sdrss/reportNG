function toggleElement(elementId, displayStyle) {
	var current = getStyle(elementId, 'display');
	elementId.style.display = (current == 'none' ? displayStyle : 'none');
}

function toggleElementRow(elementId, parentStyle) {
	var current = getStyle(elementId, 'display');
	if(parentStyle=='none' && current == 'none'){
		elementId.style.display = 'none';
	}else
	if(parentStyle=='none' && current == 'table-row'){
		elementId.style.display = 'none';
	}else
	if(parentStyle=='table-row' && current == 'table-row'){
		elementId.style.display = 'table-row';
	}
}

function getStyle(elementId, property) {
	var element = elementId;
	return element.currentStyle ? element.currentStyle[property]
			: document.defaultView.getComputedStyle(element, null)
					.getPropertyValue(property);
}

function hidePass(element) {
	var suite = $('table.table-bordered');
	var suitefooter = $('tr.suite');
	var well = $('div.well');
	for (var i = 0; i < suitefooter.length; i++) {
		var foundAllPass = 0;
		var elements = suitefooter[i].parentElement.getElementsByTagName("td");
		for (var j = 5; j < elements.length - 1; j++) {
			if (elements[j].textContent != '0') {
				foundAllPass = 1;
				break;
			}
		}
		if (foundAllPass == 0) {
			toggleElement(suitefooter[i], '');
			toggleElement(suitefooter[i].parentElement.parentElement, '');

			toggleElement(well[i], '');
		}
	}
	var test = $('tr.test');
	for (var i = 0; i < test.length; i++) {
		var pass = 0;
		var elements = test[i].getElementsByTagName("td");
		for (var j = 5; j < elements.length - 1; j++) {
			if (elements[j].textContent != '0') {
				pass = 1;
				break;
			}
		}
		if (pass == 0) {
			toggleElement(test[i], '');
		}
	}
	if ($("#hideResults").hasClass('btn-danger')) {
		$("#hideResults").removeClass('btn-danger').toggleClass('btn-default');
		$("#hideResults").text('Show All');
	} else {
		$("#hideResults").removeClass('btn-default').toggleClass('btn-danger');
		$("#hideResults").text('Show Only Failures');
	}
}

function hidePassSuites(element) {
	var suitefooter = $('tr.test');
	for (var i = 0; i < suitefooter.length; i++) {
		var elements = suitefooter[i].getElementsByTagName("td");
		if (elements[10].innerText == 'PASS') {
			toggleElement(suitefooter[i], '');
		}
	}
	if ($("#hideResults").hasClass('btn-danger')) {
		$("#hideResults").removeClass('btn-danger').toggleClass('btn-default');
		$("#hideResults").text('Show All');
	} else {
		$("#hideResults").removeClass('btn-default').toggleClass('btn-danger');
		$("#hideResults").text('Show Only Failures');
	}
}

function hidePassPackages(element) {
	var suitefooter = $('tr.parent');
	for (var i = 0; i < suitefooter.length; i++) {
		foundFailures = 0;
		var elements = suitefooter[i].getElementsByTagName("td");
		for (var j = 4; j < elements.length; j++) {
			if (elements[j].textContent != '0') {
				foundFailures = 1;
				break;
			}
		}
		if (foundFailures == 0) {
			toggleElement(suitefooter[i], '');
			childElements = $('tr.child-'+suitefooter[i].getAttribute('id'));
			for (var k = 0; k < childElements.length; k++) {
				toggleElementRow(childElements[k], getStyle(suitefooter[i], 'display'));
			}
		}
	}
	
	if ($("#hideResults").hasClass('btn-danger')) {
		$("#hideResults").removeClass('btn-danger').toggleClass('btn-default');
		$("#hideResults").text('Show All');
	} else {
		$("#hideResults").removeClass('btn-default').toggleClass('btn-danger');
		$("#hideResults").text('Show Only Failures');
	}
}