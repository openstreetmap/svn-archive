/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.openstreetmap.josm.Main;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;
import at.dallermassl.josm.plugin.surveyor.util.ResourceLoader;

/**
 * Action that plays an audio file.
 *
 * @author cdaller
 *
 */
public class PlayAudioAction extends AbstractSurveyorAction {
    private String audioSource = null;

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.SurveyorAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent)
     */
    //@Override
    public void actionPerformed(GpsActionEvent event) {
        // run as a separate thread
        Main.worker.execute(new Runnable() {
            public void run() {
                try {
                    if(audioSource == null) {
                        audioSource = getParameters().get(0);
                        //System.out.println("reading audio from " + audioSource);
                    }
                    InputStream in = new BufferedInputStream(ResourceLoader.getInputStream(audioSource));
                    AudioInputStream stream = AudioSystem.getAudioInputStream(in);

                    // From URL
//                  stream = AudioSystem.getAudioInputStream(new URL("http://hostname/audiofile"));

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
                        Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
                    Clip clip = (Clip) AudioSystem.getLine(info);

                    // This method does not return until the audio file is completely loaded
                    clip.open(stream);

                    // Start playing
                    clip.start();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
