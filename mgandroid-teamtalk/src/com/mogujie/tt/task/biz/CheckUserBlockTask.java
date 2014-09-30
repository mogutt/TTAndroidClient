
package com.mogujie.tt.task.biz;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.config.TaskConstant;
import com.mogujie.tt.https.MoGuHttpClient;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.task.MAsyncTask;
import com.mogujie.tt.utils.StringUtil;

public class CheckUserBlockTask extends MAsyncTask {

    private String mstrShopId;
    private Logger logger = Logger.getLogger(CheckUserBlockTask.class);

    public CheckUserBlockTask(String shopId) {
        this.mstrShopId = shopId;
    }

    @Override
    public int getTaskType() {
        return TaskConstant.TASK_CHECK_USER_BLOCK;
    }

    @SuppressWarnings("unused")
    @Override
    public Object doTask() {
        String url = "http://www.mogujie.com/mtalk/user/isblock";
        MoGuHttpClient httpClient = new MoGuHttpClient();
        httpClient.setTimeout(2);
        HashMap<String, String> hmParams = new HashMap<String, String>();
        hmParams.put("bid", mstrShopId);
        //TokenManager tmInstance = TokenManager.getInstance();
       // hmParams.put("dao", tmInstance.getDao());
        hmParams.put("imclient", "android1.0");
        hmParams.put("_", String.valueOf(System.currentTimeMillis() / 1000));
        HashMap<String, String> hmHeads = new HashMap<String, String>();
        hmHeads.put("Cookie",
                "__mgjuuid="+StringUtil.getUUID());
        hmHeads.put("Content-Type",
                "application/x-www-form-urlencoded; charset=\"UTF-8\";");
        HttpResponse response = httpClient.post(url, hmParams, hmHeads);
        if (null == response) {
            return false;
        }
        try {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObj = new JSONObject(result);
            if (jsonObj.has("status")) {
                JSONObject statusJsonObj = jsonObj.getJSONObject("status");

                int resultCode = statusJsonObj.getInt("code");
                String msg = statusJsonObj.getString("msg");
                if (resultCode == SysConstant.HTTP_SUCCESS_STATUS_CODE)
                {
                    if (jsonObj.has("result"))
                    {

                        JSONObject resultObj = jsonObj.getJSONObject("result");
                        String strBlock = resultObj.getString("isBlock");
                        if ("true".equals(strBlock))
                        {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }

        } catch (ParseException e) {
            logger.e(e.getMessage());
        } catch (IOException e) {
            logger.e(e.getMessage());
        } catch (JSONException e) {
            logger.e(e.getMessage());
        }
        return false;
    }

}
