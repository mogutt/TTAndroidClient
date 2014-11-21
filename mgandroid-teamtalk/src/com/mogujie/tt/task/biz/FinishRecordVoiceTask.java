
package com.mogujie.tt.task.biz;

import com.mogujie.tt.config.TaskConstant;
import com.mogujie.tt.task.MAsyncTask;

public class FinishRecordVoiceTask extends MAsyncTask {

    @Override
    public int getTaskType() {
        return TaskConstant.TASK_FINISH_RECORD_VOICE;
    }

    @Override
    public Object doTask() {
        return null;
    }

}
