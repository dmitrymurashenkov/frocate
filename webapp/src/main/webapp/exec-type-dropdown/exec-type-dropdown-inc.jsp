<span class="dropdown">
    <button class="btn btn-default dropdown-toggle" type="button" id="exectypemenu"
            data-toggle="dropdown"
            aria-haspopup="true" aria-expanded="true">
        <span id="ddtitle">Executable type</span>
        <span class="caret"></span>
    </button>
    <ul class="dropdown-menu doc-dropdown" aria-labelledby="exectypemenu">
        <li onclick="checkExecTypeOverride(); selectExecType($('#exec-type-jar-desc')); return false;">
            <a href="#">Jar</a>
        </li>
        <li onclick="checkExecTypeOverride(); selectExecType($('#exec-type-linuxbinary-desc')); return false;">
            <a href="#">Linux binary</a>
        </li>
    </ul>
    <div id="executable-desc" style="display: none">
        <div
                class="exec-type-desc"
                id="exec-type-jar-desc"
                data-execType="jar"
                style="display: none">
            <p class="execTypeTitle">Java .jar</p>
            <ul>
                <li>Launched with java -Xmx256m -Xms256m -jar &lt;your jar file&gt; &lt;task-specific config file&gt;</li>
                <li>Oracle JDK 8</li>
                <li>Can create files, bind sockets and make network connections to any host reachable on virtual network</li>
                <li>No internet access</li>
                <li>Linux Ubuntu 16.04</li>
                <li>We recommend to write logs to stdout (first 5Mb will be provided along with test results)</li>
                <li>Max executable size 20Mb</li>
            </ul>
        </div>
        <div
                class="exec-type-desc"
                id="exec-type-linuxbinary-desc"
                data-execType="linuxbinary"
                style="display: none">
            <p class="execTypeTitle">Linux binary</p>
            <ul>
                <li>Launched with chmod +x &lt;your binary file&gt; && &lt;your binary file&gt; &lt;task-specific config file&gt;</li>
                <li>64-bit architecture</li>
                <li>Preferably statically linked</li>
                <li>Can create files, bind sockets and make network connections to any host reachable on virtual network</li>
                <li>No internet access</li>
                <li>Linux Ubuntu 16.04</li>
                <li>We recommend to write logs to stdout (first 5Mb will be provided along with test results)</li>
                <li>Max executable size 20Mb</li>
            </ul>
        </div>
    </div>
</span>