import sys
import time
from multiprocessing import Process

from manager import main as manager
from server import execute as do_operation
from client import main as client

if __name__ == "__main__":

    k = 1
    server = Process(target=manager)
    server.start()
    print("Server Started...")
    for n in [1, 2, 4, 8, 16, 32, 64]:
        processes = []
        for i in range(0, n):
            processes.append(Process(target=client))
            processes[i].start()

        result = 0
        for i in range(k):
            result += do_operation("A.dat", "X.dat")
        result = result / k
        print('threads: {:2}, time: {}'.format(n, result))

        for process in processes:
            process.kill()
        for process in processes:
            process.join()

    print("EXIT?")
    server.kill()
    sys.exit()
