#! /bin/bash

for dir in ./*/
do
    mkdir -p "$dir orig"

    for file in $dir*
    do
        echo $file
        sox "$file" $(sed s/.wav/_small.wav/ <<< "$file") trim 0 3 : newfile : restart
        mv $file "$dir orig"
    done
    
done
