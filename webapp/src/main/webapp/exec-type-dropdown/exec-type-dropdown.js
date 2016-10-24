function selectExecType(descriptionElement) {
    initExecTypeDropdown();
    clearExecTypeWarn();
    var execTypeName = descriptionElement.find('.execTypeTitle').text();
    $("#ddtitle").text(execTypeName);
    $('#executable-desc').find('.exec-type-desc').hide().removeClass('active');
    descriptionElement.show().addClass('active');
}

function getActiveExecType() {
    var selectedExecType = $('#executable-desc').find('.active');
    if (selectedExecType.length == 0) {
        return null;
    } else {
        return selectedExecType.data('exectype');
    }
}

function markExecTypeWarn() {
    $('#exectypemenu').addClass('warn');
}

function clearExecTypeWarn() {
    $('#exectypemenu').removeClass('warn');
}

function initExecTypeDropdown() {
    // initially element is hidden to avoid messing layout if override container is provided
    $('#executable-desc').show();
}

function checkExecTypeOverride() {
    var overrideContainer = $('#executable-desc-override');
    var originalContainer = $('#executable-desc');
    if (overrideContainer.length > 0)
    {
        overrideContainer.replaceWith(originalContainer.clone(true, true));
        originalContainer.remove();
    }
}