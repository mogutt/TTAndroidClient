
package com.mogujie.tt.task;

public interface ITask {

    public int getTaskType();

    public TaskMode getExecuteMode();

    public Object doTask();

    public void doCallback(Object result);

    public ITask setCallBack(TaskCallback callback);

    public void doProcessing(Object result);

    public ITask setProcessing(IProcessing processing);

    void addParams(String name, Object value);

    Object getParams(String name);
}
