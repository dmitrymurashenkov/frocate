<%@ page import="com.frocate.web.task.TaskManagerImpl" %>
<%@ page import="com.frocate.taskrunner.Task" %>
<%@ page import="java.util.Collection" %>

<% TaskManagerImpl taskManager = new TaskManagerImpl(); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>Tasks</title>
</head>
<body>
<%@include file="navbar-inc.html" %>

<div class="content-area-centered">
    <div class="left-column">
    <% Collection<Task> allTasks = taskManager.getTasks().values();%>
        <h4 class="column-heading">Tags</h4>
        <ul class="nav nav-pills nav-stacked">
            <%
                for (Task task : allTasks) {
                    for (String tag : task.getTags()) {

            %>
            <li onclick="filterByTag(tag)"><a href="#"><%= tag %>
            </a></li>
            <%
                    }
                }
            %>
        </ul>
    </div>
    <div class="right-column">
        <h4 class="task-type">Pick yer task, Sir</h4>
        <%
            for (Task task : allTasks) {
                String taskTitle = task.getName();
                String taskId = task.getId();
                String shortDescription = task.getShortDescription();
                Collection<String> tags = task.getTags();
        %>
        <div class="task-card">
            <table>
                <tr>
                    <td class="task-card__title task-card__top-left-cell"><a href="#" onclick="goToTask('<%=taskId%>')"><%= taskTitle%></a></td>
                    <td class="task-card__top-right-cell">
                        <button
                                type="submit"
                                class="btn btn-primary card-button"
                                onclick="goToTask('<%=taskId%>')">
                            Solve me!
                        </button>
                    </td>
                </tr>
                <tr>
                    <td class="task-card__bottom_left-cell">
                        <p><%=shortDescription%></p>
                        <%
                            for (String tag : tags) {
                        %>
                        <span class="label"><%=tag%></span>
                        <%
                            }
                        %>
                    </td>
                    <td class="task-card__bottom-right-cell"></td>
                </tr>
            </table>
        </div>
        <%
            }
        %>
    </div>
</div>
    </div>
</body>
</html>