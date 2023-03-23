from multiprocessing.managers import BaseManager, convert_to_error
from queue import Queue


class QueueManager(BaseManager):
    pass


def multiply(dane):
    a = dane[0]
    x = dane[1]

    number_cols = len(a)
    result = 0

    for c in range(0, number_cols):
        result += a[c] * x[c][0]

    return result


def main():
    QueueManager.register('in_queue')
    QueueManager.register('out_queue')
    m = QueueManager(address=("localhost", 5000), authkey=b'blah')
    m.connect()
    q: Queue = m.in_queue()
    o: Queue = m.out_queue()

    while True:
        try:
            message = q.get_nowait()
            o.put({"i": message["i"], "result": multiply([message["A"], message["X"]])})
        except:
            pass


if __name__ == "__main__":
    main()
