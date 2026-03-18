

<h1>Complex Relational Community Search</h1>

<p>
<img src="https://img.shields.io/badge/Java-1.8+-blue" alt="Java">
<img src="https://img.shields.io/badge/License-MIT-green" alt="License">
</p>

<p>This project implements <strong>Complex Relational Community Search</strong> for <strong>Heterogeneous Information Networks (HINs)</strong>, developed in <strong>IntelliJ IDEA</strong>. It supports multiple datasets and algorithms described in the corresponding paper.</p>

<hr>

<h2>Features</h2>
<ul>
    <li>Find complex relational communities in heterogeneous networks</li>
    <li>Algorithms implemented:
        <ol>
            <li><strong>Global</strong> (Exact)</li>
            <li><strong>Greedy</strong> (Approximate)</li>
            <li><strong>Local Search Exact</strong></li>
            <li><strong>Local Search Approx</strong></li>
        </ol>
    </li>
    <li>Supports query vertex, meta-path constraints, and community size <code>k</code></li>
    <li>Experiments: DBLP, Instacart, IMDB*, Foursquare*<br>
        <blockquote>*IMDB and Foursquare are not included due to size limits. Please download from the paper's links.</blockquote>
    </li>
</ul>

<hr>

<h2>Project Structure</h2>
<pre>
src/
├─ Dataset/       # Data (DBLP, Instacart, etc.)
├─ algorithm/     # Algorithms (from the paper)
└─ model/         # Core entities: Vertex, Constraint, MetaPath, etc.
</pre>

<hr>

<h2>Usage</h2>
<ol>
    <li><strong>Import Project:</strong> Open in IntelliJ IDEA.</li>
    <li><strong>Run <code>Main.java</code></strong>, choose dataset:
<pre>
1. Simple Example from the Paper
2. Foursquare Dataset
3. DBLP Dataset
4. IMDB Dataset
5. Instacart Dataset
0. Exit
</pre>
    </li>
    <li><strong>Select Algorithm:</strong>
<pre>
1. Global
2. Greedy
3. Local Search Exact
4. Local Search Approx
5. ALL
0. Back
</pre>
    </li>
    <li><strong>Set community size <code>k</code></strong> (e.g., <code>2</code>).</li>
</ol>

<hr>

<h2>Example Run</h2>
<p><strong>Input:</strong></p>
<pre>
Dataset: 3 (DBLP)
Algorithm: 3 (Local Search Exact)
k: 2
</pre>

<p><strong>Output:</strong></p>
<pre>
Total 2216567 edges loaded (Skipped 1783433 duplicates)
Query vertex: name=319455, id=2460, type=USER
Constraint: MetaPath[USER, VENUE, CITY, VENUE, USER], k=2
Running algorithm: LocalSearchExact

--- Result ---
Community Size: 5
  name=319455, id=2460 - USER
  name=161937, id=46145 - USER
  name=4b9088def964a520e58e33e3, id=973978 - VENUE
  name=4bb6d4d86edc76b05d96311c, id=1053744 - VENUE
  name=Quezon City, id=4384486 - CITY
Time: 2502 ms
</pre>

<hr>

<h2>Dataset Info</h2>
<ul>
    <li><strong>DBLP</strong>: Academic network</li>
    <li><strong>Instacart</strong>: E-commerce user transactions</li>
    <li><strong>IMDB</strong>, <strong>Foursquare</strong>: download manually from paper</li>
</ul>

<hr>

<h2>Requirements</h2>
<ul>
    <li>Java 1.8+</li>
    <li>IntelliJ IDEA</li>
    <li>No extra dependencies</li>
</ul>

<hr>

</body>
</html>
