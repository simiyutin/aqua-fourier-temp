#! /bin/bash

SAMPLE_LENGTH=3 #secs
TRIM_DURATION=10 #secs

RAW_DATA_DIR=../data/raw_data/
PROCESSED_DATA_DIR=../data/processed_data/
EXTRACTED_DATA_DIR=../data/extracted_data/

rm -r $PROCESSED_DATA_DIR
mkdir $PROCESSED_DATA_DIR

rm -r $EXTRACTED_DATA_DIR
mkdir $EXTRACTED_DATA_DIR

for file in $RAW_DATA_DIR**/*
do
    new_file="$(sed s/.mp3/.wav/ <<< "$file" | sed s/raw_data/processed_data/)"
    new_dir=$(dirname "$new_file")
    mkdir -p "$new_dir"
    echo "converting to wav: $file -> $new_file"
    ffmpeg -i "$file" "$new_file"
done


for file in $PROCESSED_DATA_DIR**/*
do
   echo "trimming $file"
   sox "$file" "tmp.wav" trim $TRIM_DURATION
   sox "tmp.wav" "$file" trim 0 -$TRIM_DURATION
   sox "$file" $(sed s/.wav/_small.wav/ <<< "$file") trim 0 $SAMPLE_LENGTH : newfile : restart
   rm "$file"
   rm "tmp.wav"
done
