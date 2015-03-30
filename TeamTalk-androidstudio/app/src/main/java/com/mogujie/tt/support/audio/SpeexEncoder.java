
package com.mogujie.tt.support.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.task.TaskCallback;

public class SpeexEncoder implements Runnable {

    private Logger log = Logger.getLogger(SpeexEncoder.class);
    private final Object mutex = new Object();
    private Speex speex = new Speex();

    public static int encoder_packagesize = 1024;
    private byte[] processedData = new byte[encoder_packagesize];

    List<ReadData> list = null;
    private volatile boolean isRecording;
    private String fileName;
    private TaskCallback mCallback;

    public SpeexEncoder(String fileName, TaskCallback callback) {
        super();
        speex.init();
        list = Collections.synchronizedList(new LinkedList<ReadData>());
        this.fileName = fileName;
        this.mCallback = callback;
    }

    @SuppressWarnings("unused")
    public void run() {
        SpeexWriter fileWriter = new SpeexWriter(fileName, mCallback);
        Thread consumerThread = new Thread((Runnable) fileWriter);
        fileWriter.setRecording(true);
        consumerThread.start();

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int getSize = 0;
        while (this.isRecording()) {
            if (list.size() == 0) {
                log.d("no data need to do encode");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (list.size() > 0) {
                synchronized (mutex) {
                    ReadData rawdata = list.remove(0);
                    short[] playData = new short[rawdata.size];
                    getSize = speex.encode(rawdata.ready, 0, processedData,
                            rawdata.size);
                    log.i("after encode......................before="
                            + rawdata.size + " after=" + processedData.length
                            + " getsize=" + getSize);
                }
                if (getSize > 0) {
                    fileWriter.putData(processedData, getSize);
                    log.i("............clear....................");
                    processedData = new byte[encoder_packagesize];
                }
            }
        }
        log.d("encode thread exit");
        fileWriter.setRecording(false);
        speex.close();
    }

    public void putData(short[] data, int size) {
        ReadData rd = new ReadData();
        synchronized (mutex) {
            rd.size = size;
            System.arraycopy(data, 0, rd.ready, 0, size);
            list.add(rd);
        }
    }

    public void setRecording(boolean isRecording) {
        synchronized (mutex) {
            this.isRecording = isRecording;
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }

    class ReadData {
        private int size;
        private short[] ready = new short[encoder_packagesize];
    }
}
