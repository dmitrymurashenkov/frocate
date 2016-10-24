<!DOCTYPE html>
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>Discussion</title>
</head>
<body>
<div class="row-fluid" align="center">
    <div class="span12">
        <%@include file="navbar-inc.html" %>
    </div>
    <div class="span12">
        <div id="disq">
            <div id="disqus_thread"></div>
            <script>
                var disqus_config = function () {
                    this.page.url = "http://frocate.com/discussion.jsp";
                };
                (function() {
                    var d = document, s = d.createElement('script');

                    s.src = '//123-1.disqus.com/embed.js';

                    s.setAttribute('data-timestamp', +new Date());
                    (d.head || d.body).appendChild(s);
                })();
            </script>
            <noscript>Please enable JavaScript to view the <a href="https://disqus.com/?ref_noscript" rel="nofollow">comments powered by Disqus.</a></noscript>
        </div>
    </div>
</div>
</body>
</html>