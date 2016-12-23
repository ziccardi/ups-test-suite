import logging

class MockFeedback(object):

    # TODO: Clean up and refactor

    def __init__(self, queue):
        self.queue = queue

    def __call__(self, sock, address):
        logging.debug("Feedback connection from %s", address)

        while True:
            try:
                token = self.queue.get()
            except Empty:
                break

            logging.debug("Sending feedback for token %s", token)
            sock.write(struct.pack('!IH32s', int(time.time()), 32, token.decode('hex')))