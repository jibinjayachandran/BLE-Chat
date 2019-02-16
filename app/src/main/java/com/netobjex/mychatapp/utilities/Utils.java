package com.netobjex.mychatapp.utilities;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Jibin on 1/27/2019.
 */

public class Utils {
    public static ProgressDialog pd;

    public static void showProgress(Context context,String msg){
        pd = new ProgressDialog(context);
        pd.setMessage(msg);
        pd.show();
    }
    public static void hideProgress(Context context){
        if(pd != null){
            pd.dismiss();
        }
    }
}
