
package com.mogujie.tt.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.timer.ITimerProcessor;

public class TaskTrigger implements ITimerProcessor {

    private ExecutorService cachedThreadPool = Executors.newFixedThreadPool(1);

    private TaskCenter taskCenter = new TaskCenter();

    private static Logger logger = Logger.getLogger(TaskTrigger.class);

    private boolean isStarted = false;

    @Override
    public void process() {
        doTrigger();
    }

    public void doTrigger() {
        while (!taskCenter.isEmpty()) {
            final ITask task = taskCenter.get();
            if (null != task) {
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        task.doCallback(task.doTask());
                    }
                });
            } else {
                logger.e("task is null.");
            }
        }
    }

    /**
     * @Description: 提交一个任务
     * @param task
     * @return
     */
    public boolean trigger(ITask task) {
        if (task.getExecuteMode() == TaskMode.masync) {
            MAsyncTask mtask = (MAsyncTask) task;
            mtask.execute();
        } else if (task.getExecuteMode() == TaskMode.bgTask) {
            if (null != taskCenter) {
                taskCenter.put(task);
                return true;
            }
        }
        return false;
    }

    public boolean isStarted() {
        return isStarted;
    }
}
