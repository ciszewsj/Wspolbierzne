from multiprocessing.managers import BaseManager
from queue import Queue


class QueueManager(BaseManager):
    pass


def main(ip="localhost", port=5000):
    in_queue = Queue()
    out_queue = Queue()
    QueueManager.register('in_queue', callable=lambda: in_queue)
    QueueManager.register('out_queue', callable=lambda: out_queue)
    manager = QueueManager(address=(ip, int(port)), authkey=b'blah')
    server = manager.get_server()
    print("Server started...")
    server.serve_forever()


if __name__ == '__main__':
    main("localhost", 5000)
