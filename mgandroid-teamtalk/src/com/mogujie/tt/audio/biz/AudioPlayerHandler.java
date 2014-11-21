
package com.mogujie.tt.audio.biz;

import java.io.File;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.support.audio.SpeexDecoder;

public class AudioPlayerHandler {
    private String fileName = null;
    private SpeexDecoder speexdec = null;
    private static Thread th = null;

    private static AudioPlayerHandler instance = null;
    private Logger logger = Logger.getLogger(AudioPlayerHandler.class);

    public static synchronized AudioPlayerHandler getInstance() {
        if (null == instance) {
            instance = new AudioPlayerHandler();
        }
        return instance;
    }

    public AudioPlayerHandler() {
    }

    public void stopPlayer() {
        try {
            if (null != th) {
                th.interrupt();
                th = null;
                Thread.currentThread().interrupt();
            } else {
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public boolean isPlaying() {
        return null != th;
    }

    public void startPlay(String filePath) {
        this.fileName = filePath;
        try {
            speexdec = new SpeexDecoder(new File(this.fileName));
            RecordPlayThread rpt = new RecordPlayThread();
            if (null == th)
                th = new Thread(rpt);
            th.start();
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    class RecordPlayThread extends Thread {

        public void run() {
            try {
                if (null != speexdec)
                    speexdec.decode();

            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }
    };
}
