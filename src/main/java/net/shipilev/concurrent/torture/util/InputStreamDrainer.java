package net.shipilev.concurrent.torture.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputStreamDrainer extends Thread {

    private static final int BUF_SIZE = 1024;
    private final List<OutputStream> outs;
    private final InputStream in;

    /**
     * Create a drainer which will discard the read lines.
     *
     * @param in The input stream to drain
     */
    public InputStreamDrainer(InputStream in) {
        this(in, null);
    }

    /**
     * Create a drainer that will echo all read lines to <code>out</code>.
     *
     * @param in  The input stream to drain
     * @param out Where to drain the stream into
     */
    public InputStreamDrainer(InputStream in, OutputStream out) {
        this.in = in;
        outs = new ArrayList<OutputStream>();
        addOutputStream(out);
    }

    /**
     * Adds an output stream to drain the output to.
     *
     * @param out The output stream
     */
    public void addOutputStream(OutputStream out) {
        if (out != null) {
            outs.add(out);
        }
    }

    /** Drain the stream. */
    public void run() {
        byte[] buf = new byte[BUF_SIZE];
        try {
            int read;
            while ((read = in.read(buf)) != -1) {
                for (OutputStream out : outs) {
                    out.write(buf, 0, read);
                }
            }
            for (OutputStream out : outs) {
                out.flush();
            }
        } catch (IOException ioe) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ioe.getMessage(), ioe);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }
    }

}
