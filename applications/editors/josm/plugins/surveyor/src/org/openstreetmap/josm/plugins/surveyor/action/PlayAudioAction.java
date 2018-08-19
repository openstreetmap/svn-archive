// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor.action;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.surveyor.GpsActionEvent;
import org.openstreetmap.josm.plugins.surveyor.util.ResourceLoader;
import org.openstreetmap.josm.tools.Logging;

/**
 * Action that plays an audio file.
 *
 * @author cdaller
 *
 */
public class PlayAudioAction extends AbstractSurveyorAction {
    private String audioSource = null;

    @Override
    public void actionPerformed(GpsActionEvent event) {
        // run as a separate thread
        MainApplication.worker.execute(() -> {
            try {
                if (audioSource == null) {
                    audioSource = getParameters().get(0);
                }
                InputStream in = new BufferedInputStream(ResourceLoader.getInputStream(audioSource));
                AudioInputStream stream = AudioSystem.getAudioInputStream(in);

                // At present, ALAW and ULAW encodings must be converted
                // to PCM_SIGNED before it can be played
                AudioFormat format = stream.getFormat();
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    format = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        format.getSampleSizeInBits()*2,
                        format.getChannels(),
                        format.getFrameSize()*2,
                        format.getFrameRate(),
                        true);        // big endian
                    stream = AudioSystem.getAudioInputStream(format, stream);
                }

                // Create the clip
                DataLine.Info info = new DataLine.Info(
                    Clip.class, stream.getFormat(), ((int) stream.getFrameLength()*format.getFrameSize()));
                Clip clip = (Clip) AudioSystem.getLine(info);

                // This method does not return until the audio file is completely loaded
                clip.open(stream);

                // Start playing
                clip.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e1) {
                Logging.error(e1);
            }
        });
    }
}
