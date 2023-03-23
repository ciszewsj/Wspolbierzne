import sys
import time
from multiprocessing.managers import BaseManager
from queue import Queue
from threading import Thread


class QueueManager(BaseManager):
    pass


def read(file_name, isA: bool):
    f = open(file_name, "r")
    nr = int(f.readline())
    nc = int(f.readline())

    a = [[0] * nc for _ in range(nr)]
    r = 0
    c = 0
    for i in range(0, nr * nc):
        a[r][c] = float(f.readline())
        c += 1
        if c == nc:
            c = 0
            r += 1
    if isA:
        global A
        A = a
    else:
        global X
        X = a


A, X = [], []


def main(file_name_a, file_name_x) -> int:
    global A
    global X

    QueueManager.register('in_queue')
    QueueManager.register('out_queue')
    m = QueueManager(address=("localhost", 5000), authkey=b'blah')
    m.connect()
    q: Queue = m.in_queue()
    o: Queue = m.out_queue()

    a_thread = Thread(target=read, args=(file_name_a, True,))
    a_thread.start()
    b_thread = Thread(target=read, args=(file_name_x, False,))
    b_thread.start()
    a_thread.join()
    b_thread.join()

    sleep_time = time.time_ns()

    number = len(A)

    result = [None for _ in range(number)]

    for i in range(0, number):
        q.put({"operation": "mult", "i": i, "A": A[i], "X": X})

    r = [i for i in range(0, number)]

    for i in range(0, number):
        info = o.get()
        result[info["i"]] = info["result"]
        r.remove(info["i"])
    end_time = time.time_ns() - sleep_time

    return end_time


def execute(file_a, file_b):
    k = main(file_a, file_b)
    return k


if __name__ == "__main__":
    file_name_a = sys.argv[1] if len(sys.argv) > 1 else "A.dat"
    file_name_x = sys.argv[2] if len(sys.argv) > 2 else "X.dat"
