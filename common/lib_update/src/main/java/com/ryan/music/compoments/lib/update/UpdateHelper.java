package com.ryan.music.compoments.lib.update;

import android.app.Activity;
import android.content.Context;

import com.ryan.music.compoments.lib.CommonDialog;
import com.ryan.music.compoments.lib.CommonOkHttpClient;
import com.ryan.music.compoments.lib.R;
import com.ryan.music.compoments.lib.listener.DisposeDataHandle;
import com.ryan.music.compoments.lib.listener.DisposeDataListener;
import com.ryan.music.compoments.lib.request.CommonRequest;
import com.ryan.music.compoments.lib.update.constant.Constants;
import com.ryan.music.compoments.lib.update.model.UpdateModel;
import com.ryan.music.compoments.lib.update.utils.Utils;
import com.ryan.music.compoments.lib.utils.ResponseEntityToModule;

public final class UpdateHelper {

    public static final String UPDATE_FILE_KEY = "apk";

    public static String UPDATE_ACTION;
    //SDK全局Context, 供子模块用
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        UPDATE_ACTION = mContext.getPackageName() + ".INSTALL";
    }

    public static Context getContext() {
        return mContext;
    }

    //外部检查更新方法
    public static void checkUpdate(final Activity activity) {
        CommonOkHttpClient.get(CommonRequest.
                        createGetRequest(Constants.CHECK_UPDATE, null),
                new DisposeDataHandle(new DisposeDataListener() {
                    @Override public void onSuccess(Object responseObj) {
                        final UpdateModel updateModel = (UpdateModel) responseObj;
                        if (Utils.getVersionCode(mContext) < updateModel.data.currentVersion) {
                            //说明有新版本,开始下载
                            CommonDialog dialog =
                                    new CommonDialog(activity, mContext.getString(R.string.update_new_version),
                                            mContext.getString(R.string.update_title),
                                            mContext.getString(R.string.update_install),
                                            mContext.getString(R.string.cancel), new CommonDialog.DialogClickListener() {
                                        @Override public void onDialogClick() {
                                            UpdateService.startService(mContext);
                                        }
                                    });
                            dialog.show();
                        } else {
                            //弹出一个toast提示当前已经是最新版本等处理
                        }
                    }

                    @Override public void onFailure(Object reasonObj) {
                        onSuccess(
                                ResponseEntityToModule.parseJsonToModule(MockData.UPDATE_DATA, UpdateModel.class));
                    }
                }, UpdateModel.class));
    }
}
