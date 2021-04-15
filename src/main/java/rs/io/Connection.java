package rs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection implements Runnable {

	private final InputStream in;
	private final OutputStream out;
	private final Socket socket;
	private boolean closed = false;
	private byte[] buffer;
	private int bufLen;
	private int bufPos;
	private boolean writing = false;
	private boolean exception = false;

	public Connection(Socket s) throws IOException {
		socket = s;
		socket.setSoTimeout(30000);
		socket.setTcpNoDelay(true);
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}

	public void close() {
		closed = true;

		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		}

		writing = false;

		synchronized (this) {
			this.notify();
		}

		buffer = null;
	}

	public int read() throws IOException {
		if (closed == true) {
			return 0;
		}
		return in.read();
	}

	public int available() throws IOException {
		if (closed == true) {
			return 0;
		}
		return in.available();
	}

	public void read(byte[] dst, int off, int len) throws IOException {
		if (closed) {
			return;
		}

		int read;
		for (/**/; len > 0; len -= read) {
			read = in.read(dst, off, len);
			if (read <= 0) {
				throw new IOException("EOF");
			}
			off += read;
		}
	}

	public void write(byte[] src, int off, int len) throws IOException {
		if (closed) {
			return;
		}

		if (exception) {
			exception = false;
			throw new IOException("Error in writer thread");
		}

		if (buffer == null) {
			buffer = new byte[5000];
		}

		synchronized (this) {
			for (int i = 0; i < len; i++) {
				buffer[bufPos] = src[i + off];
				bufPos = (bufPos + 1) % 5000;

				if (bufPos == (bufLen + 4900) % 5000) {
					throw new IOException("buffer overflow");
				}
			}

			if (writing == false) {
				writing = true;

				Thread t = new Thread(this);
				t.setPriority(2);
				t.start();
			}
			this.notify();
		}
	}

	@Override
	public void run() {
		while (writing) {
			int off, len;

			synchronized (this) {
				if (bufPos == bufLen) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						/* empty */
					}
				}

				if (!writing) {
					break;
				}

				off = bufLen;

				if (bufPos >= bufLen) {
					len = bufPos - bufLen;
				} else {
					len = 5000 - bufLen;
				}

			}
			if (len > 0) {
				try {
					out.write(buffer, off, len);
				} catch (IOException e) {
					exception = true;
				}

				bufLen = (bufLen + len) % 5000;

				try {
					if (bufPos == bufLen) {
						out.flush();
					}
				} catch (IOException e) {
					exception = true;
				}
			}
		}
	}
}
