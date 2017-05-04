#!/bin/bash

java -classpath lib/TarsosDSP-latest.jar:build/classes/main/ ru.ifmo.rain.garder.SpectrumExtractor $1 $2 $3 $4
