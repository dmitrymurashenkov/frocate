<%@ page import="com.frocate.web.task.TaskManagerImpl" %>
<%@ page import="com.frocate.taskrunner.Task" %>

<% TaskManagerImpl taskManager = new TaskManagerImpl(); %>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>Task</title>
</head>
<body id="top" onload="tryOpenResult();">
<%@include file="navbar-inc.html" %>
<div class="content-area-centered">
    <div class="left-column">
        <%
            String taskId = request.getParameter("taskId");
            Task task = taskManager.getTaskById(taskId);
            String taskDescription = task.getDescription();
            String taskTitle = task.getName();
            String link = "/task.jsp?taskId=" + taskId;
        %>
        <h4 class="column-heading">Tasks / <%=taskTitle%>
        </h4>
        <ul id="mynav" class="nav nav-pills nav-stacked">
            <li id="list-description" onclick="openDescriptionForm()"><a href="#description">Description</a></li>
            <li onclick="pollResult('<%=taskId%>__sample-result')"><a href="#">Sample result</a></li>
            <li id="list-solution" onclick="openSubmitForm()"><a href="#solution">Submit solution</a></li>
            <li id="list-result" onclick="openResultForm()"><a href="#result">Result</a></li>
            <li id="list-comments" onclick="openCommentsForm()"><a href="#comments">Comments</a></li>
            <li onclick="$('#top').showAndScrollTo()"><a href="#">Back to top</a></li>
        </ul>
    </div>
    <div class="right-column">
        <h4 class="column-heading" id="task-name">
            Description
        </h4>
        <div class="panel panel-default task-description-full" id="description">
            <div class="panel-body">
                <%=taskDescription%>
                <button type="submit" class="btn btn-primary submit-solution" onclick="openSubmitForm()">
                    Submit solution!
                </button>
            </div>
        </div>
        <div class="panel panel-default panel-submit" id="solution" style="display:none">
            <div class="panel-body">
                <div class="upload-controls">
                <input type="file" class="custom-file-input" id="executable" onclick="removeWarn()">
                <jsp:include page="exec-type-dropdown/exec-type-dropdown-inc.jsp"/>
                <button type="submit" class="btn btn-primary submit-button"
                        onclick="uploadFile($('#executable'), '<%=taskId%>','/upload', onUploadFinished)">Submit
                    solution!
                </button>
                </div>
                <div id="warn" class="warn-text" style="display: none"></div>
                <div id="executable-desc-override" style="display: none"><br><br>
                </div>
            </div>
        </div>
        <div class="panel panel-default" id="result-pending" style="display:none">
            <div class="panel-body">
                <span id="progress-text">Checking status...</span>
                <br><br>
                <div class="progress">
                    <div id="progress-bar" class="progress-bar progress-bar-striped active progress-bar-info"
                         role="progressbar"
                         aria-valuenow="0"
                         aria-valuemin="0" aria-valuemax="100" style="width: 0%">
                    </div>
                </div>
            </div>
        </div>
        <div class="panel panel-default" id="result" style="display:none">
            <div class="panel-body" id="result-container">
            </div>
        </div>

        <div class="panel panel-default" id="comments" style="display:none">
            <div class="panel-body" id="disqus">
                <div id="disqus_thread"></div>
                <script>
                    var disqus_config = function () {
                        this.page.url = "http://frocate.com/task.jsp?taskId=" + <%=taskId%> +"#disqus";  // Replace PAGE_URL with your page's canonical URL variable
                        this.page.identifier = <%=taskId%>;
                    };
                    (function () {
                        var d = document, s = d.createElement('script');

                        s.src = '//123-1.disqus.com/embed.js';

                        s.setAttribute('data-timestamp', +new Date());
                        (d.head || d.body).appendChild(s);
                    })();
                </script>
                <noscript>Please enable JavaScript to view the <a href="https://disqus.com/?ref_noscript"
                                                                  rel="nofollow">comments powered by Disqus.</a>
                </noscript>
            </div>
        </div>
    </div>
</div>
</body>
</html>