<!DOCTYPE html>
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>Supported executable types</title>
</head>
<body>
<%@include file="navbar-inc.html" %>
<div class="content-area-centered">
    <div class="single-column">
        <h4 class="left-column__title">Supported executable types</h4>
        <div class="panel panel-default" id="description">
            <div class="panel-body">
                <jsp:include page="exec-type-dropdown/exec-type-dropdown-inc.jsp"/>
            </div>
        </div>
    </div>
</div>
</body>
</html>