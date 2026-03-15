function toggleElement(elementId, displayStyle) {
	var current = getStyle(elementId, 'display');
	elementId.style.display = (current == 'none' ? displayStyle : 'none');
}

function toggleElementRow(elementId, parentStyle) {
	if (parentStyle === 'none') {
		elementId.style.display = 'none';
	} else if (parentStyle === 'table-row') {
		elementId.style.display = 'table-row';
	}
}

function getStyle(element, property) {
	return window.getComputedStyle(element).getPropertyValue(property);
}

function toggleHideButton(btn) {
	var labelShowAll = btn.data('label-show-all') || 'Show All';
	var labelShowFailures = btn.data('label-show-failures') || 'Show Only Failures';
	if (btn.hasClass('btn-danger')) {
		btn.removeClass('btn-danger').addClass('btn-default').text(labelShowAll);
	} else {
		btn.removeClass('btn-default').addClass('btn-danger').text(labelShowFailures);
	}
}

function hidePass(element) {
	var suite = $('table.table-bordered');
	var suitefooter = $('tr.suite');
	var well = $('div.well');
	for (let i = 0; i < suitefooter.length; i++) {
		var foundAllPass = 0;
		let elements = suitefooter[i].parentElement.getElementsByTagName("td");
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
	for (let i = 0; i < test.length; i++) {
		var pass = 0;
		let elements = test[i].getElementsByTagName("td");
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
	toggleHideButton($("#hideResults"));
}

function hidePassSuites(element) {
	var suitefooter = $('tr.test');
	for (let i = 0; i < suitefooter.length; i++) {
		var elements = suitefooter[i].getElementsByTagName("td");
		if (elements[10].innerText == 'PASS') {
			toggleElement(suitefooter[i], '');
		}
	}
	toggleHideButton($("#hideResults"));
}

function hidePassPackages(element) {
	var suitefooter = $('tr.parent');
	for (let i = 0; i < suitefooter.length; i++) {
		var foundFailures = 0;
		var elements = suitefooter[i].getElementsByTagName("td");
		for (var j = 4; j < elements.length; j++) {
			if (elements[j].textContent != '0') {
				foundFailures = 1;
				break;
			}
		}
		if (foundFailures == 0) {
			toggleElement(suitefooter[i], '');
			var childElements = $('tr.child-' + suitefooter[i].getAttribute('id'));
			for (var k = 0; k < childElements.length; k++) {
				toggleElementRow(childElements[k], getStyle(suitefooter[i], 'display'));
			}
		}
	}
	toggleHideButton($("#hideResults"));
}
