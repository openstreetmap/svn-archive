/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.io.File;
import java.io.IOException;
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

/**
 * Action that plays an audio file.
 * 
 * @author cdaller
 *
 */
public class PlayAudioAction extends AbstractSurveyorAction {
    private File audioFile = null;

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.SurveyorAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent)
     */
    @Override
    public void actionPerformed(GpsActionEvent event) {
        try {
            if(audioFile == null) {
                audioFile = new File(getParameters().get(0));
                if(!audioFile.exists()) {
                    audioFile = new File(Main.pref.getPreferencesDir(), getParameters().get(0));
                    if(!audioFile.exists()) {
                        System.err.println("Audio file " + getParameters().get(0) + " not found!");
                        return;
                    }
                }
            }
            // From file
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
        
            // From URL
//            stream = AudioSystem.getAudioInputStream(new URL("http://hostname/audiofile"));
        
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
}
