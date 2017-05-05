MAX_FILES=35
SAMPLE_RATE=44100
SPECTRUM_EXTRACTOR_DIR=../spectrum-extractor/
PROCESSED_DATA_DIR=../$1/processed_data/
EXTRACTED_DATA_DIR=../$1/extracted_data/

$SPECTRUM_EXTRACTOR_DIR/build.sh

for buffer_size in $(seq 2048 1024 8192)
do
    for overlap in $(seq 0 1024 $buffer_size)
    do
	$SPECTRUM_EXTRACTOR_DIR/run.sh "$PROCESSED_DATA_DIR" $MAX_FILES $SAMPLE_RATE $buffer_size $overlap
	mv "$SPECTRUM_EXTRACTOR_DIR/features.csv" "$EXTRACTED_DATA_DIR/$SAMPLE_RATE $buffer_size $overlap.csv"
    done
done
