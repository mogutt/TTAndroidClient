
package com.mogujie.tt.task;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseTask implements ITask {

    protected TaskMode executeMode = TaskMode.bgTask;
    protected int taskType = -1;
    protected Map<String, Object> dataHolder = new HashMap<String, Object>();
    protected TaskCallback callback;
    protected IProcessing processing;

    @Override
    public TaskMode getExecuteMode() {
        return executeMode;
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
    public int getTaskType() {
        return taskType;
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

    @Override
    public void doProcessing(Object result) {
        if (null != processing) {
            processing.processing(result);
        }
    }

    @Override
    public ITask setProcessing(IProcessing processing) {
        this.processing = processing;
        return this;
    }

}
