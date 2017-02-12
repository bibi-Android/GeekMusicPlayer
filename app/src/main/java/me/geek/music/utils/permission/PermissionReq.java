package me.geek.music.utils.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class PermissionReq {
    private static int sRequestCode = 0;
    //代替HashMap
    private static SparseArray<PermissionResult> sResultArray = new SparseArray<>();

    private Object mObject;
    private String[] mPermissions;
    private PermissionResult mResult;

    private PermissionReq(Object object) {
        mObject = object;
    }

    public static PermissionReq with(@NonNull Activity activity) {
        return new PermissionReq(activity);
    }

    public static PermissionReq with(@NonNull Fragment fragment) {
        return new PermissionReq(fragment);
    }

    /**
     * 传入对应的权限
     */
    public PermissionReq permissions(@NonNull String... permissions) {
        mPermissions = permissions;
        return this;
    }

    /**
     * 传入一个自定义实现的Req接口,里面是外部自定义的onGranted(有权限)与onDenied(不具有权限)的时候所做的操作
     * @param result
     * @return
     */
    public PermissionReq result(@Nullable PermissionResult result) {
        mResult = result;
        return this;
    }

    /**
     * 对外部开放的执行逻辑
     * 1.判断是否已经具有权限
     * 2.如果原本没有权限,则向用户申请
     */
    public void request() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (mResult != null) {
                mResult.onGranted();
            }
            return;
        }

        Activity activity = getActivity(mObject);
        if (activity == null) {
            //不合法的参数异常
            throw new IllegalArgumentException(mObject.getClass().getName() + " is not supported");
        }

        //获得被拒绝权限的List
        List<String> deniedPermissionList = getDeniedPermissions(activity, mPermissions);
        if (deniedPermissionList.isEmpty()) {
            //如果返回的deniedPermissionList为空,则表明应用已经具有该权限
            if (mResult != null) {
                mResult.onGranted();
            }
            return;
        }

        int requestCode = genRequestCode();
        String[] deniedPermissions = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
        requestPermissions(mObject, deniedPermissions, requestCode);
        sResultArray.put(requestCode, mResult);
    }


    /**
     * 用于活动或者碎片启动时候进行首先回调
     * 系统自动回调的情况:
     * 有一些情形下,调用
     * 1.自动授权: 如果用户已经允许了permission group中的一条A权限,那么当下次调用requestPermissions()方法请求同一个group中的B权限时, 系统会直接调用onRequestPermissionsResult() 回调方法, 并传回PERMISSION_GRANTED的结果.
     * 2.自动拒绝: 如果用户选择了不再询问此条权限,那么app再次调用requestPermissions()方法来请求同一条权限的时候,系统会直接调用onRequestPermissionsResult()回调,返回PERMISSION_DENIED.
     */
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionResult result = sResultArray.get(requestCode);

        if (result == null) {
            return;
        }

        sResultArray.remove(requestCode);

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                result.onDenied();
                return;
            }
        }
        result.onGranted();
    }


    /**
     * 向用户申请权限
     * 1.请求权限的方法是: requestPermissions() 传入一个Activity, 一个permission名字的数组, 和一个整型的request code.
     * 2.这个方法是异步的,它会立即返回, 当用户和dialog交互完成之后,系统会调用回调方法,传回用户的选择结果和对应的request code.
     * 3.当我们requestPermissions(permissions, requestCode),会立马回调BaseActivity或者BaseFragment中的onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static void requestPermissions(Object object, String[] permissions, int requestCode) {
        if (object instanceof Activity) {

            ((Activity) object).requestPermissions(permissions, requestCode);//这里是自定义一个申请码
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(permissions, requestCode);
        }
    }

    private static List<String> getDeniedPermissions(Context context, String[] permissions) {
        List<String> deniedPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            //判断应用是否具有某个权限
            //检查权限的方法: ContextCompat.checkSelfPermission()两个参数分别是Context和权限名.
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                //如果应用没有这权限
                deniedPermissionList.add(permission);
            }
        }
        return deniedPermissionList;
    }


    /**
     * 将传进来的object进行强制转型
     */
    private static Activity getActivity(Object object) {
        if (object != null) {
            if (object instanceof Activity) {
                return (Activity) object;
            } else if (object instanceof Fragment) {
                return ((Fragment) object).getActivity();
            }
        }
        return null;
    }

    private static int genRequestCode() {
        return ++sRequestCode;
    }
}
