rpath=$(pwd -P)
#cd /home/twak/Downloads/cw_3_submissions
for d in /home/twak/Downloads/cw_3_submissions/*; do

    echo "doing $d"

    if [[ $d != *.zip ]]; then
        continue;
    fi

    echo "doing $d"

    username=$(echo "$d" | grep -oP "Process_\K[^_]*_attempt")
    username=${username%"_attempt"}
    if [ -z "$username" ]
    then
       continue
    fi
    echo "processing " $username

    unzip "$d" -o -d "/home/twak/Downloads/cw_3_submissions/$username"

    #folder="/home/twak/Downloads/cw2_submissions/$username"

    #cp /home/twak/code/leeds_grader/bash/ "$folder"
    #cd "$folder"
    #./generate.sh

    #cp "$d"/*.cpp "$d"/*.h "$d"/CMakeLists.txt "$username"
    #cp -r "$d/report" "$username"
    #cp generate.sh cpplint.py dumpcpp.py screenshot.cpp screenshot.h "$username"
    #cd "$username"
    #./generate.sh
    cd "$rpath"
done
