// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.runtime;

import java.net.*;
import java.applet.*;
import javax.sound.sampled.*;

// based on Deane Richan's AppletAudioClip

/**
 * An applet audio clip using the javax.sound API.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class AppletAudioClip implements AudioClip {

    /** the clip */
    private Clip clip;

    /**
     * Creates new AudioClip.  If the clip cannot be opened no
     * exception is thrown, instead the methods of the AudioClip
     * return without performing any operations.
     *
     * @param location the clip location
     */
    public AppletAudioClip(URL location) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(location);

            clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            clip.open(stream);
        } catch (Exception ex) {
            System.err.println("Error loading sound:" + location.toString());
            clip = null;
        }
    }

    /**
     * Plays the clip in a continuous loop until the stop method is
     * called.
     */
    public void loop() {
        if (clip == null)
            return;

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Plays the clip from the beginning.
     */
    public void play() {
        if (clip == null)
            return;

        // applet audio clip resets to beginning when played again
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Stops playing the clip.
     */
    public void stop() {
        if (clip == null)
            return;

        clip.stop();
    }

    /**
     * Stops playing the clip and disposes it; the clip cannot be
     * played after being disposed.
     */
    void dispose() {
        if (clip != null) {
            clip.stop();
            clip.flush();
            clip.close();
        }

        clip = null;
    }

}
