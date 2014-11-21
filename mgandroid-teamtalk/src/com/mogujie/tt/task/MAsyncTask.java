
package com.mogujie.tt.task;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;

/**
 * @Description: 简单封装AsyncTask
 * @author ziye - ziye[at]mogujie.com
 * @date 2014-4-16 下午5:34:46
 */
public abstract class MAsyncTask extends AsyncTask<Object, Integer, Object> implements ITask {

    protected TaskMode executeMode = TaskMode.masync;
    protected Map<String, Object> dataHolder = new HashMap<String, Object>();
    protected TaskCallback callback;
    protected IProcessing processing;

    @Override
    public TaskMode getExecuteMode() {
        return executeMode;
    }

    @Override
    protected Object doInBackground(Object... params) {
        return doTask();
    }

    @Override
    protected void onPostExecute(Object result) {
        doCallback(result);
    }

    @Override
    public void addParams(String name, Object value) {
        dataHolder.put(name, value);
    }

    @Override
    public Object getParams(String name) {
        return dataHolder.get(name);
    }

    @Override
    public ITask setCallBack(TaskCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void doCallback(Object result) {
        if (null != callback) {
            callback.callback(result);
        }
    }

    public ITask setProcessing(IProcessing processing) {
        this.processing = processing;
        return this;
    }

    public void doProcessing(Object result) {
        if (null != processing) {
            processing.processing(result);
        }
    }
}
