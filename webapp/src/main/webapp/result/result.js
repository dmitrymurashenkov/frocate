function drawResult(containerId, result) {
    //todo appending date to avoid caching - disable cache some other way
    $('#' + containerId).empty().load('result/result-inc.html?' + new Date().getTime(), fillResult.bind(this, result))
}

function fillResult(testResult) {
    $("#id").html(testResult.id);
    $("#execType").html(testResult.executableType);
    $("#execSize").html((testResult.executableSize/1000000).toFixed(2) + 'Mb');
    $("#startTime").html(testResult.startTime);
    // $("#pass").html(testResult.success);
    $("#appLogsLink").html('<a target="_blank" href="buildLog?buildId=' + testResult.id + '">Open in new tab</a>');
    $("#testLogsLink").html('<a target="_blank" href="testLog?buildId=' + testResult.id + '">Open in new tab</a>');

    if (testResult.failures == 0) {
        $("#pass").html(testResult.success + ' <div class="label passed-failed-label label-good">SOLVED!</div>');
    } else {
        $("#pass").html(testResult.success + ' <div class="label passed-failed-label label-error">FAILED!</div>');
    }

    if (testResult.failures.length > 0) {
        for (var i = 0; i<testResult.failures.length; i++)
        {
            var failure = testResult.failures[i];
            $("#failures").append(
                '<tr class="result-cell-general">' +
                '   <td class="test-result-cell" id="failName"><b>' + failure.testname + '</b></td>' +
                '   <td class="test-result-cell"><div class="label label-error">ERROR</div></td>' +
                '</tr>' +
                '<tr>' +
                '   <td class="metric-desc-cell" id="errormsg"><b>Message: </b>' + failure.errormsg + '</td> ' +
                '</tr>');
        }
    } else {
        $("#failures-label").remove();
        $("#failures").remove();
    }

    for (var j = 0; j < testResult.metrics.length; j++)
    {
        var metric = testResult.metrics[j];
        var ratingHtml = null;
        if (metric.rating == "ERROR")
        {
            ratingHtml = '<div class="label label-error">ERROR</div>';
        } else if (metric.rating == "BAD") {
            ratingHtml = '<div class="label label-bad">BAD</div>';
        } else if (metric.rating == "GOOD") {
            ratingHtml = '<div class="label label-good">GOOD</div>';
        } else if (metric.rating == "EXCELLENT") {
            ratingHtml = '<div class="label label-excellent">EXCELLENT</div>';
        }
        $("#metrics").append(
            '<tr ' + (j > 0 ? 'class="metric-table-row"' : "" )+ '>' +
            '   <td class="test-result-cell"><b>' + metric.name + '</b></td>' +
                '<td class="test-result-cell metric-result-cell">' + (metric.rating == "ERROR" ? "" : metric.value + " " + metric.unit) + '</td>' +
            '   <td class="test-result-cell">' + ratingHtml + '</td>' +
            '</tr>' +
            '<tr>' +
            '   <td class="metric-desc-cell">' + metric.desc + '</td>' +
            '</tr>' +
            '<tr>' +
            '   <td>' +
            '       <table>' +
            '           <tr>' +
            '               <td class="metric-range-cell"><div class="label label-good">GOOD</div></td>' +
            '               <td class="metric-range-cell">' + metric.goodRange.start + '-' + metric.goodRange.end + ' ' + metric.unit + '</td>' +
            '           </tr>' +
            '           <tr>' +
            '               <td class="metric-range-cell"><div class="label label-excellent">EXCELLENT</div></td>' +
            '               <td class="metric-range-cell">' + metric.excellentRange.start + '-' + metric.excellentRange.end + ' ' + metric.unit + '</td>' +
            '           </tr>' +
            '       </table>' +
            '   </td>' +
            '</tr>' +
            '<tr></tr>'
        );
    }
}