<!DOCTYPE html>
<html lang="en">
<head>
    <%@include file="head-inc.jsp" %>
    <title>FAQ</title>
</head>
<body id="top">
<%@include file="navbar-inc.html" %>
<div class="content-area-centered">
    <div class="left-column">
        <h4 class="column-heading">FAQ</h4>
        <ul id="mynav" class="nav nav-pills nav-stacked">
            <li><a href="javascript:void(0)" onclick="$('#faq-header-general').showAndScrollTo()">General</a></li>
            <li><a href="javascript:void(0)" onclick="$('#faq-header-solving').showAndScrollTo()">Solving a task</a></li>
            <li><a href="javascript:void(0)" onclick="$('#faq-header-evaluation').showAndScrollTo()">Evaluation and results</a></li>
            <li><a href="javascript:void(0)" onclick="$('#top').showAndScrollTo()">Back to top</a></li>
        </ul>
    </div>
    <div class="right-column">
        <ul id="faq-toc" class="nav nav-pills nav-stacked">
            <p class="faq-toc-title">General</p>
            <li><a href="javascript:void(0)" onclick="$('#1').showAndScrollTo()">What is Frocate?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#2').showAndScrollTo()">What is the aim and who is the target audience?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#3').showAndScrollTo()">Why you think your testing approach is better?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#4').showAndScrollTo()">How the solution is rated</a></li>
            <li><a href="javascript:void(0)" onclick="$('#5').showAndScrollTo()">How much it takes to solve one task?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#6').showAndScrollTo()">What is the suggested level of proficiency to take your tests?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#7').showAndScrollTo()">What programming languages are currently supported?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#8').showAndScrollTo()">Is it free?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#9').showAndScrollTo()">I have a suggestion or noticed an error, how do I reach you?</a></li>
            <p class="faq-toc-title">Solving a task</p>
            <li><a href="javascript:void(0)" onclick="$('#10').showAndScrollTo()">What is the process of providing a solution and getting results?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#11').showAndScrollTo()">What technologies can I use in my solution?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#12').showAndScrollTo()">What are supported executable formats?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#13').showAndScrollTo()">What test environment is used to run a solution?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#15').showAndScrollTo()">Why results take so long to load?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#17').showAndScrollTo()">There are failed tests. How do I see what went wrong?</a></li>
            <p class="faq-toc-title">Evaluation and results</p>
            <li><a href="javascript:void(0)" onclick="$('#14').showAndScrollTo()">How solutions are being evaluated?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#16').showAndScrollTo()">What metrics does result contain?</a></li>
            <li><a href="javascript:void(0)" onclick="$('#18').showAndScrollTo()">What do BAD/GOOD/EXCELLENT metric rating mean?</a></li>

        </ul>
        <span>
            <h3 class="faq-side-section" id="faq-header-general">General</h3>
            <section>
                <p class="faq-title" id="1">What is Frocate?</p>
                <p>Frocate is a programming tasks platform which is rather different from existing ones.</p>
                <p>Here you solve realistic tasks in realistic environment: use any library you wish, google anything you want, do whatever you think necessary, just provide the solution.</p>
                <p>No ambiguous multiple choice options, no "what does this unrealistically complex code snippet outputs" questions, no theoretic CS tasks like "find all triplets in an array". Instead tasks sound and feel like the ones you are likely to solve at your job: write a network daemon, process a .csv file, integrate with provided service. </p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="2">What is the aim and who is the target audience?</p>
                <p>Main goal is to provide alternative to existing programming skills evaluation approaches.</p>
                <p>We started with following cases in mind:</p>
                <ul>
                    <li>As part of hiring process you can give it as a pre/post interview task to be done at home</li>
                    <li>Practice problems for teaching and study in CS field</li>
                    </ul>
                <p>Have an idea of other use cases? Mail us or leave a message on the discussion board!</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="3">Why you think your testing approach is better than alternatives?</p>
                <p>Because the main goal of IT guys is to deliver some awesome (and preferably - working) products to the audience and not just compete with each other solving problems unrelated to real life.</p>
                <p>Numerous other ways to test programming skills exist. They can easily verify that one remembers signatures of all methods of all existing frameworks by heart, does not need a compiler/debugger to evaluate result of a program 10 pages long and can calculate a product of all array elements that form a cat-shape in O(1). But since real tasks are difficult because of completely different things few of such testing approaches can assure you that a person would actually perform their intended job well.</p>
                <p>In general we think that existing approaches do have correlation with actual developer performance on day to day tasks, but why rely on statistics when we can test the actual skill directly?</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="4">How the solution is rated?</p>
                <p>The inconvenient truth is that bad code that gets the job done is a good code (at least for business). So we test what can be tested and leave all kinds of subjective evaluation out of scope.</p>
                <p>For each task there is a set of automated tests that verify functional correctness and gather some numeric metrics needed to evaluate an overall application quality. We run them against submitted executable file.</p>
                <p>The task is considered solved when all tests pass no matter how high or low the metric scores are. It means that code would work in a real case. However there still may be a room for improvement and that's what metrics are for.</p>
                <p>Source code is not evaluated at all since it would result in either subjective score or some random metrics like "number of lines per method" which tell nothing about real code quality.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="5">How much time it takes to solve one task?</p>
                <p>From several hours to several days.</p>
                <p>If a person is familiar with the domain and technologies then a simple and inefficient solution that passes all tests can be done in about 2-4 hours, while achieving maximum scores may take up to 8-15 hours for some tasks.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="6">What is the suggested level of proficiency to take your tests?</p>
                <p>Tasks can be completed by programmers of various skill levels, but in some cases would take more time than expected.</p>
                <p>To be really comfortable while solving these tasks you should have around 2 years of development experience, write code very day and be familiar with task-specific domain and technologies required to solve it.</p>
                <p>However lacking any or even all of this points won't prevent you from completing the task, but it would require some learning in the process and would likely result in 5x time spent.</p>
            </section>
            <section>
                <p class="faq-title" id="7">What programming languages are currently supported?</p>
                <p>See <a href="#12">supported executable formats</a></p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="8">Is it free?</p>
                <p>Yes it is free. No registration, no ads, just tasks.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="9">I have a suggestion or noticed an error, how do I reach you?</p>
                <p>You can contact us via email:</p>
                <p>Or leave a comment on a Discussion page.</p>
            </section>
            <hr>
            <h3 class="faq-side-section" id="faq-header-solving">Solving a task</h3>
            <section>
                <p class="faq-title" id="10">What is the process of providing a solution and getting results?</p>
                <ul>
                    <li>Go to task page and read requirements</li>
                    <li>Write some code, test it yourself first</li>
                    <li>Once you are ready (more or less) - build the code and submit the executable</li>
                    <li>Wait for results</li>
                    <li>Fix bugs, refactor and submit another executable</li>
                    <li>Repeat till your are satisfied with results (more or less)</li>
                </ul>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="11">What technologies can I use in my solution?</p>
                <p>No limits - use anything you like. Since you are building the executable you may utilize any libraries and frameworks you want. Just don't forget to pack them into the file you submit.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="12">What are supported executable formats?</p>
                <p>See values in <a onclick="$('#faq-exec-types-dropdown').showAndScrollTo()">dropdown below</a></p>
            </section>
             <hr>
            <section>
                <p class="faq-title" id="13">What test environment is used to run a solution?</p>
                <p>Your executable is started as a separate process on a Linux machine.</p>
                <p>You may write files, bind sockets and do anything you would in a real case scenario. Just don't crash the environment by filling all the disk space, producing a fork bomb or similar stuff.</p>
                <p>To get a detailed description of the OS and command used to run your app select executable type:</p>
                <div id="faq-exec-types-dropdown"><jsp:include page="exec-type-dropdown/exec-type-dropdown-inc.jsp"/></div>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="15">Why results take so long to load?</p>
                <p>Since we are running black box integration testing, it is not always possible to write tests that complete in a matter of milliseconds, so we have something like 10-50 tests per task and each takes around 1-10 seconds.</p>
                <p>Add time to setup and clean environment for an executable and we have a typical evaluation time around 2-3 minutes.</p>
                <p>Also at the moment we are kinda short on processing power and since many tests evaluate performance in some way we take the safe approach and avoid running them in parallel, but we plan on improving this.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="17">The result says there are failed tests. How do I see what exactly went wrong?</p>
                <p>We dump standard output of your process to file and provided it along with results, so your may use these logs to find and fix some errors.</p>
                <p>Also we provide output of tests process along with tests source code (in Java). Feel free to use it.</p>
                <p>If you think that your code is correct and some test fails because of the problem on our side - mail us or write a comment on the task page.</p>
                <p>If you think that particular error message is not informative enough - mail us or write a comment on the task page.</p>
            </section>
            <section>
                <p class="faq-title" id="19">Can I see tests source?</p>
                <p>Yes, full Frocate source is available on <a href="https://github.com/dmitrymurashenkov/frocate">github</a>. If you can read Java code
                then go on and look up anything you want.</p>
                <p>Each task is placed in separate directory and has "tests" folder containing all tests for this particular task.</p>
            </section>
            <h3 class="faq-side-section" id="faq-header-evaluation">Evaluation and results</h3>
             <section>
                <p class="faq-title" id="14">How exactly solutions are evaluated?</p>
                <p>We run your executable and then run some integration tests. Exact way depends on the task type.</p>
                <p>For example, if the task is to take an input file, process it and produce an output file then everything is simple, we run your app multiple times with different inputs and check output, processing time and some other related metrics.</p>
                <p>If the task was to integrate with provided web-service, then we start that service, then your executable, then tests.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="16">What metrics does a result contain?</p>
                <p>We have 2 kind of tests:</p>
                <ul>
                    <li><p><strong>Functional correctness tests</strong> - produce PASS/FAIL result and simply check that code does what it is supposed to according to functional requirements.</p></li>
                    <li><p><strong>Metric tests</strong> - produce numeric value or FAIL result (if an error occurred). Check how optimal the solution is in general.</p></li>
                </ul>
                 <p>Most of the time metrics are performance related in one way or another, but not as simple as pure "requests per second". For example, if an app has to perform some long-running operation and provide an ability to cancel it then we can check how much time passes between the moment cancel is invoked and the time operation is actually canceled - the faster the better.</p>
                <p>Some metrics are critical to application quality while others have minor impact, but nevertheless tell us something about overall quality.</p>
            </section>
            <hr>
            <section>
                <p class="faq-title" id="18">What do BAD/GOOD/EXCELLENT metric rating mean?</p>
                <p>Metric produces 2 values: a numeric one and a rating.</p>
                <p>
                    Numeric value is just the result itself, for example, number of requests per second. Rating is how
                    good the result is (since there is usually no way to know how to interpret the numeric value). Our
                    definition of "good" is based on if this would be acceptable in a typical project and is meant to
                    somewhat discourage meaningless competition in cases where all solutions are already great.
                </p>
                <p>
                    <div class="label label-bad">BAD</div> - solution seems worse-than-average. Sometimes it means that
                    solution would likely be unacceptable in real life, sometimes it is simply meant to push you to
                    make a better one.
                </p>
                <p>
                    <div class="label label-good">GOOD</div> - solution is perfectly fine, but can be
                    improved.
                </p>
                <p>
                    <div class="label label-excellent">EXCELLENT</div> solution works great and even if it can be
                    improved even more this is not needed.
                </p>
            </section>
            <hr>
            </span>
    </div>
</div>
</body>
</html>