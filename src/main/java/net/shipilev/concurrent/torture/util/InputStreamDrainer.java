/*
 * Copyright (c) 2012 Aleksey Shipilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shipilev.concurrent.torture.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputStreamDrainer extends Thread {

    private static final int BUF_SIZE = 1024;
    private final OutputStream out;
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
        this.out = out;
    }

    /** Drain the stream. */
    public void run() {
        byte[] buf = new byte[BUF_SIZE];
        try {
            int read;
            while ((read = in.read(buf)) != -1) {
                if (out != null) {
                    out.write(buf, 0, read);
                }
            }
            if (out != null) {
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
