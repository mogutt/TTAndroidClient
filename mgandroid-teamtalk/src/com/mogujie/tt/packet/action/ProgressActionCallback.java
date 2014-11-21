
package com.mogujie.tt.packet.action;

/**
 * 带进度显回调的callback，用于图片或者音频upload、download时候的进度显示
 * 
 * @author dolphinWang
 * @time 2014/04/30
 */
public interface ProgressActionCallback extends ActionCallback {

    /**
     * socket可以根据上传、下载的byte数除以总的byte数给出进度
     * 
     * @param progress 百分比*100后的数值
     */
    void onStep(int progress);
}
