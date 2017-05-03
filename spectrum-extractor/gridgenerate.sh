MAX_FILES=35

sample_rate=44100
for buffer_size in $(seq 2048 1000 8192)
do
    for overlap in $(seq 100 1000 $buffer_size)
    do
	./run.sh dataset $MAX_FILES $sample_rate $buffer_size $overlap
	mv features.csv "calculated_features/sr_$sample_rate bs_$buffer_size ovl_$overlap.csv"
    done
done

