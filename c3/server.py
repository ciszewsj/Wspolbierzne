import sys
import time
from multiprocessing.managers import BaseManager
from queue import Queue
from threading import Thread, Event

from client import main as client


class QueueManager(BaseManager):
    pass


def read(file_name, struct: bool):
    global A, X
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
    if struct:
        A = a
    else:
        X = a


event = Event()

if __name__ == "__main__":
    number_cpus = int(sys.argv[1]) if len(sys.argv) > 1 else 2
    file_name_a = sys.argv[2] if len(sys.argv) > 2 else "A.dat"
    file_name_x = sys.argv[3] if len(sys.argv) > 3 else "X.dat"

    sleep_time = time.time_ns()

    for i in range(number_cpus):
        Thread(target=client, args=(event,)).start()
        print(f"Created thread: {i}")

    QueueManager.register('in_queue')
    QueueManager.register('out_queue')
    m = QueueManager(address=("localhost", 5000), authkey=b'blah')
    m.connect()
    q: Queue = m.in_queue()
    o: Queue = m.out_queue()

    A = []
    X = []

    a_thread = Thread(target=read, args=(file_name_a, True,))
    a_thread.start()
    b_thread = Thread(target=read, args=(file_name_x, False,))
    b_thread.start()

    a_thread.join()
    b_thread.join()

    number = len(A)

    result = [None for i in range(number)]

    for i in range(number):
        q.put({"operation": "mult", "i": i, "A": A[i], "X": X})

    for i in range(number):
        info = o.get()
        result[info["i"]] = info["result"]

    event.set()
    print(f"END {time.time_ns() - sleep_time}")
