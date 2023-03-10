# Wspolbierzne

c1 RUN:

cc gen.c -o gen

./gen 1000

cc c1.c

./a.out {number of process > 0 || default 2}

c2.c RUN:

cc c2.c -lm

./a.out {number of threads || default 4}
