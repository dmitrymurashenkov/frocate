<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>Frocate</title>
</head>
<body>
    <div class="row-fluid" align="center">
        <div class="span12">
            <%@include file="navbar-inc.html" %>
        </div>
        <div class="span12">
            <div class="jumbotron">
                <div id="jumbotron-logo">
                    <h1 id="jumbotron-title">Frocate</h1>
                    <div id="jumbotron-alpha">Alpha version</div>
                </div>
                <p>Pragmatic programming skills testing platform</p>
                <button type="submit" class="btn btn-primary btn-lg"  onclick="window.location.href='tasks.jsp'">Start solving tasks!</button>
            </div>
        </div>
        <div class="news-block">
            <h2>News</h2>
            <div>
                <ul>
                    <li>
                        <h4>29 Oct 2016</h4>
                        <ul>
                            <li>Frocate source is now on <a href="https://github.com/dmitrymurashenkov/frocate">github</a></li>
                            <li>New tutorial task - http service for summing two numbers</li>
                            <li>Failfast tests - now tests stop after first failure</li>
                            <li>Better description of errors for money transfer task - now all expected and actuall transactions
                            are available in test log</li>
                        </ul>
                    </li>
                    <li>
                        <h4>18 Oct 2016</h4>
                        <ul>
                            <li>Ability to view test logs along with executable logs</li>
                        </ul>
                    </li>
                    <li>
                        <h4>12 Oct 2016</h4>
                        <ul>
                            <li>New task - url shortening service, let's build our own version of bit.ly in 2 hours :)</li>
                        </ul>
                    </li>
                    <li>
                        <h4>10 Sep 2016</h4>
                        <ul>
                            <li>Test results now look pretty </li>
                            <li>Description for metrics added</li>
                        </ul>
                    </li>
                    <li>
                        <h4>28 Aug 2016</h4>
                        <ul>
                            <li>Initial version of service is now operational! </li>
                            <li>Single task "Money transfer" available. </li>
                            <li>Let's roll!</li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
</body>
</html>