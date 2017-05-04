MAX_FILES=70
SAMPLE_RATE=44100
SPECTRUM_EXTRACTOR_DIR=../spectrum-extractor/
PROCESSED_DATA_DIR=../data/processed_data/
EXTRACTED_DATA_DIR=../data/extracted_data/

$SPECTRUM_EXTRACTOR_DIR/build.sh

for buffer_size in $(seq 2048 1000 8192)
do
    for overlap in $(seq 100 1000 $buffer_size)
    do
	$SPECTRUM_EXTRACTOR_DIR/run.sh "$PROCESSED_DATA_DIR" $MAX_FILES $SAMPLE_RATE $buffer_size $overlap
	mv "$SPECTRUM_EXTRACTOR_DIR/features.csv" "$EXTRACTED_DATA_DIR/sr_$SAMPLE_RATE bs_$buffer_size ovl_$overlap.csv"
    done
done
