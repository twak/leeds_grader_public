#!/bin/bash
#module add clion qt/5.3.1 cmake/3.8.2 
export QT_SELECT="qt5.15"
rm -f ./submission.html
rm -rf ./images
echo "<html><head><link href='https://cdnjs.cloudflare.com/ajax/libs/prism/1.17.1/themes/prism-okaidia.css' rel='stylesheet'/> <link href='https://cdnjs.cloudflare.com/ajax/libs/prism/1.17.1/plugins/line-numbers/prism-line-numbers.css' rel='stylesheet'/>  <link href='prism-line-highlight.css' rel='stylesheet'/><style>" >> ./submission.html
cat prism-line-highlight.css >> ./submission.html
echo "</style></head><body>" >> ./submission.html
echo "<h3>Compiling</h3><h4>qmake output</h4><pre>" >> ./submission.html
echo "running qmake..."
PWD=$(pwd -P)
qmake -o "./Makefile" "./responsive.pro" &>> ./submission.html
echo "running make..."
make clean &>> ./submission.html
echo "</pre>" >> ./submission.html
echo "<h4>make -j 8 output</h4><pre>" >> ./submission.html
make &>> ./submission.html
echo "</pre>" >> ./submission.html
echo "<h3>Program Output</h3>" >> ./submission.html
echo "running coursework..."
./responsive . test  &>> ./submission.html
echo "<h3>Code Summary</h3>" >> ./submission.html
echo "checking cpp..."
python3 dumpcpp.py >> ./submission.html
echo "done"
echo "<br/><h3>Code</h3>" >> ./submission.html
echo "<br/>" >> ./submission.html
shopt -s extglob
FILES="responsive_layout.cpp responsive_window.h !(responsive_window).h !(responsive_layout).cpp"
for i in $FILES; do
    if [[ $i == screenshot* ]]; then
	continue;
    fi;
    if [[ $i == moc_* ]]; then
	continue;
    fi;
    if [[ $i == main.* ]]; then
	continue;
    fi;
    if [[ $i == folder_compressor.* ]]; then
	continue;
    fi;
    if [[ (${file: -4} != ".cpp") && (${file: -4} == ".h")  ]]; then
    continue;
    fi;
    
    echo $i	
    echo "<br/><br/><h4>$i</h4><pre>" >> ./submission.html
    LINT=$(python2 ./cpplint.py --filter=-runtime,-legal,-build,-readability,-whitespace,+readability/fn_size,+readability/multiline_comment,+build/filename,+whitespace/line_length,,+whitespace/newline,+whitespace/braces2 --linelength=102 --output=cw1 $i 2>&1)
    LAST=$(echo "$LINT" | tail -n 1)
    LINT=$(echo "$LINT" | head -n -1)
    echo "$LINT" &>> ./submission.html
    echo -e "</pre><pre data-line='$LAST' class='line-numbers'><code class='language-cpp'>\c" >> ./submission.html
    cat "$i" | sed -e 's/</\&lt;/' | sed -e 's/>/\&gt;/' >> ./submission.html
    echo "</code></pre>" >> ./submission.html
done

echo "<script src='https://cdnjs.cloudflare.com/ajax/libs/prism/1.22.0/prism.js'></script>	<script src='https://cdnjs.cloudflare.com/ajax/libs/prism/1.22.0/plugins/autoloader/prism-autoloader.min.js'> </script>	<script src='https://cdnjs.cloudflare.com/ajax/libs/prism/1.22.0/plugins/line-numbers/prism-line-numbers.js'></script><script src='https://cdnjs.cloudflare.com/ajax/libs/prism/1.22.0/plugins/line-highlight/prism-line-highlight.js'></script></body></html>" >> ./submission.html
