
package com.mogujie.tt.task.biz;

import java.io.InputStream;
import java.net.MalformedURLException;

import com.mogujie.tt.config.TaskConstant;
import com.mogujie.tt.https.MoGuHttpClient;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.task.MAsyncTask;

public class DownloadImageTask extends MAsyncTask {
    private String strUrl;
    @SuppressWarnings("unused")
    private String strPath;
    private Logger logger = Logger.getLogger(DownloadImageTask.class);

    public DownloadImageTask(String url, String savePath)
    {
        strUrl = url;
        strPath = savePath;
    }

    @Override
    public int getTaskType() {
        return TaskConstant.TASK_DOWNLOAD_IMAGE;
    }

    @Override
    public Object doTask() {
        return doDownLoadImage();
    }

    private InputStream doDownLoadImage() {
        MoGuHttpClient httpClient = new MoGuHttpClient();
        InputStream inputStream = null;

        try {
            inputStream = httpClient.download(strUrl);
        } catch (MalformedURLException e) {
            logger.e(e.getMessage());
        }
        return inputStream;
    }

}
