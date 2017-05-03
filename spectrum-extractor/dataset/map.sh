#! /bin/bash

for file in ./**/*
do
  ffmpeg -i "$file" $(sed s/.mp3/.wav/ <<< "$file")
done
