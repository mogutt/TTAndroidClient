
package com.mogujie.tt.task;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskCenter {

    private Queue<ITask> taskQueue;

    public TaskCenter() {
        taskQueue = new ConcurrentLinkedQueue<ITask>();
    }

    /**
     * @Description: 新增一个task
     * @param task
     * @return
     */
    public boolean put(ITask task) {
        if (task != null) {
            taskQueue.offer(task);
            return true;
        }
        return false;
    }

    /**
     * @Description: 返回一个task，并删除该元素
     * @return
     */
    public ITask get() {
        return taskQueue.poll();
    }

    /**
     * @Description: 获取queue长度,慎用
     * @return
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
}
