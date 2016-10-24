$.fn.showAndScrollTo = function() {
    this.show(200, function () {
        $('html, body').animate({
            scrollTop: $('#' + this.id).offset().top
        }, 1000);
    });
};

function hideAllElementsButOne(elementToShow) {
    $("#description").hide();
    $("#result").hide();
    $("#result-pending").hide();
    $("#comments").hide();
    $("#solution").hide();
    elementToShow.show();
}

function removeClassFromAllButOne(elementWithClass) {
    $("#list-solution").removeClass("activeNav");
    $("#list-result").removeClass("activeNav");
    $("#list-description").removeClass("activeNav");
    $("#list-comments").removeClass("activeNav");
    elementWithClass.addClass("activeNav");
}

function navbarSetActive(activelement) {
    $("#navhome").removeClass("active");
    $("#navtasks").removeClass("active");
    $("#navfaq").removeClass("active");
    $("#navabout").removeClass("active");
    $("#navdisq").removeClass("active");
    activelement.addClass("active");
}
// class="active"

function goToTask(taskId) {
    window.location.href = "task.jsp?taskId=" + taskId;
    removeClassFromAllButOne($("#list-description"));
    $("#task-name").html("Description");
}

function filterByTag(tag) {
}

function openSubmitForm() {
    hideAllElementsButOne($("#solution"));
    removeClassFromAllButOne($("#list-solution"));
    $("#task-name").html("Submit solution");
}

function openDescriptionForm() {
    hideAllElementsButOne($("#description"));
    removeClassFromAllButOne($("#list-description"));
    $("#task-name").html("Description");
}

function openCommentsForm() {

    hideAllElementsButOne($("#comments"));
    $("#task-name").html("Your comments");
    removeClassFromAllButOne($("#list-comments"));
}

function openResultPendingForm() {
    if ($("#result-pending").is(':hidden')) {
        hideAllElementsButOne($("#result-pending"));
        removeClassFromAllButOne($("#list-result"));
        $("#task-name").html("Result");
    }
}

function openResultForm() {
    hideAllElementsButOne($("#result"));
    removeClassFromAllButOne($("#list-result"));
    $("#task-name").html("Result");
}

function tryOpenResult() {
    var hash = window.location.hash.substring(1);
    if (hash && hash.indexOf("=") != -1) {
        var buildId = hash.substring(hash.indexOf("=") + 1);
        pollResult(buildId);
    }
}

function onUploadFinished(buildId) {
    window.location.hash = "buildId=" + buildId;
    pollResult(buildId);
}

function pollResult(buildId) {
    openResultPendingForm();
    setTimeout(function () {
        console.log("Requesting results for session: " + buildId);
        $.ajax({
            url: 'result?buildId=' + buildId
        }).done(function (answer) {
            var response = $.parseJSON(answer);
            if (response.status == "resultReady") {
                openResultForm();
                drawResult('result-container', response.data);
            } else if (response.status == "inProgress") {
                openResultPendingForm();
                drawProgress(response.data);
                pollResult(buildId);
            } else if (response.status == "inQueue") {
                openResultPendingForm();
                drawInQueue(response.data);
                pollResult(buildId);
            }
            else if (response.status == "unknownBuildId") {
                openResultPendingForm();
                drawError("Build not found");
            }
            else {
                openResultPendingForm();
                drawError("Unknown response status: " + response.status);
            }
        });
    }, 1000);
}

function drawUploading() {
    $('#progress-text').html('<b>Uploading file</b>');
}

function drawError(message) {
    $('#progress-text').html('<b>Error:</b> ' + message);
}

function drawInQueue(testResult) {
    $('#progress-text').html('<b>Awaiting free VM</b> index in queue: ' + testResult.indexInQueue );
}

function drawProgress(testResult) {
    var percentFinished = Math.round(100*testResult.finishedTests/testResult.totalTests);
    $('#progress-text').html('<b>Tests finished:</b> ' + testResult.finishedTests + '/' + testResult.totalTests + "<br><b>Current test:</b> " + testResult.currentTest);
    $('#progress-bar')
        // .attr('aria-valuenow', percentFinished)
        .width(percentFinished + '%');
}

function uploadFile(jqChooseFileElement, taskId, url, callback) {
    if (validateSubmission()) {
        var data = new FormData();
        data.append('taskId', taskId);
        data.append('executable', jqChooseFileElement[0].files[0]);
        data.append('exectype', getActiveExecType());

        $.ajax({
            url: url,
            method: 'POST',
            //do not try to serialize data to string
            processData: false,
            contentType: false,
            data: data
        }).done(function (answer) {
            var response = $.parseJSON(answer);
            console.log('Uploaded file: ' + answer);
            if (response.status == 'success') {
                callback(response.buildId);
            } else {
                console.log('Upload failed: ' + response.reason);
            }
        });

        openResultPendingForm();
        drawUploading();
    }
}

function validateSubmission() {
    var executable = $("#executable");
    var ok = true;

    if (!getActiveExecType()) {
        markExecTypeWarn();
        $("#warn").show().text("Select executable type!");
        ok = false;
    } else {
        clearExecTypeWarn();
    }

    if (executable.get(0).files.length === 0) {
        executable.addClass("warn");
        $("#warn").show().text("Select file to upload!");
        ok = false;
    } else {
        executable.removeClass("warn");
    }
    return ok;
}

function removeWarn() {
    $("#executable").removeClass("warn")
}

$(document).ready(function () {
    $('a[href^="#"]').on('click', function (e) {
        e.preventDefault();

        var target = this.hash;
        var $target = $(target);

        $('html, body').stop().animate({
            'scrollTop': $target.offsetTop
        }, 900, 'swing', function () {
            window.location.hash = target;
        });
    });
});

$(document).ready(function () {
    $(window).scroll(function () {
        if ($(this).scrollTop() > 100) {
            $('.scrollToTop').fadeIn();
        } else {
            $('.scrollToTop').fadeOut();
        }
    });
    $('.scrollToTop').click(function () {
        $('html, body').animate({scrollTop: 0}, 800);
        return false;
    });
});
