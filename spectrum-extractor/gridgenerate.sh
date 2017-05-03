MAX_FILES=35

sample_rate=44100
for buffer_size in $(seq 2048 1000 4096)
do
    for overlap in $(seq 1536 100 $buffer_size)
    do
	./run.sh dataset $MAX_FILES $sample_rate $buffer_size $overlap
	mv features.csv "calculated_features/$sample_rate$buffer_size$overlap.csv"
    done
done

