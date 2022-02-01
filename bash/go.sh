rpath=$(pwd -P)
#for d in /home/twak/Downloads/swjtu_cw2/*; do
for d in /home/twak/Downloads/gradebook_202122_32871_COMP2811_CW23a20Responsive_2021-11-17-14-30-11/*; do
#for d in /home/twak/Downloads/cw2_test/*; do

    if [[ $d != *.patch ]]; then
        continue;
    fi

    echo "doing $d"

    username=$(echo "$d" | grep -oP "Responsive_\K[^_]*_attempt")
    username=${username%"_attempt"}
    if [ -z "$username" ]
    then
       continue
    fi
    echo "processing " $username

    folder="/home/twak/Downloads/cw2_submissions/$username"
    
#    /home/twak/code/leeds_grader/bash/unpack_cw1 "$d" "$folder"
#    unzip "$d" -d "$username"


    cp /home/twak/code/leeds_grader/bash/* "$folder"
    cd "$folder"

    ./generate.sh

    #cp "$d"/*.cpp "$d"/*.h "$d"/CMakeLists.txt "$username"
    #cp -r "$d/report" "$username"
    #cp generate.sh cpplint.py dumpcpp.py screenshot.cpp screenshot.h "$username"
    #cd "$username"
    cd "$rpath"
done
