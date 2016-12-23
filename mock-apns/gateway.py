import logging
import struct

from gevent.queue import Queue, Empty

class MockGateway(object):

    # TODO: Clean up and refactor

    def __init__(self, send_errors=False, send_feedback=False):
        """
        `send_errors`: Callable sends error response code if >= 0.
        `send_feedback`: Callable returns True if token should be added to feedback
        """
        self.send_errors = send_errors or (lambda: -1)
        self.send_feedback = send_feedback or (lambda: False)

        if not callable(self.send_errors):
            raise TypeError("Expected callable send_errors")

        if not callable(self.send_feedback):
            raise TypeError("Expected callable send_feedback")

        self.feedback = Queue()

    def __call__(self, sock, address):
        logging.debug("APNS connection from %s", address)
        buff = ''

        while True:
            try:
                data = sock.read(1024)
            except AttributeError:
                return

            if not data:
                return

            buff += data
            processed = 0

            while len(buff) - processed >= 45:
                try:
                    envelope = struct.unpack('!BIIH32sH', buff[processed:45])
                except struct.error as e:
                    logging.debug("Error processing payload: %s", e)
                    return

                cmd, ident, expiry, token_len, token, msg_size = envelope

                try:
                    token = token.encode('hex')
                except ValueError as e:
                    logging.debug("Token error: %s", e)
                    sock.write(struct.pack('!BBI', 8, 8, ident))
                    return

                push_size = msg_size + 45
                if len(buff) - processed < push_size:
                    break  # Get more data

                msg = struct.unpack('!%ds' % msg_size, buff[processed + 45:push_size])[0]
                logging.debug("Ident: %s, expiry: %s, token: %s, msg: %s", ident, expiry, token, msg)

                if self.send_feedback():
                    self.feedback.put(token)

                error = self.send_errors()
                if 0 <= error <= 255:
                    sock.write(struct.pack('!BBI', 8, error, ident))
                    return

                processed += push_size

            buff = buff[processed:]