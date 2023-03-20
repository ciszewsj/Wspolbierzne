import sys
from threading import Thread

from manager import main as manager
from server import execute as do_operation

if __name__ == "__main__":

    k = 5
    server = Thread(target=manager)
    server.setDaemon(True)
    server.start()
    print("Server Started...")
    for n in [1, 2, 4, 8, 16, 32, 64]:
        result = 0
        for i in range(k):
            result += do_operation(n, "A.dat", "X.dat")
        result = result / k
        print('threads: {:2}, time: {}'.format(n, result))
    print("EXIT?")
    sys.exit()
