package me.geek.music.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import me.geek.music.R;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.constants.Extras;
import me.geek.music.enums.LoadStateEnum;
import me.geek.music.model.JsonArtisitInfo;
import me.geek.music.utils.ViewUtils;
import okhttp3.Call;

/**
 * 歌手详细信息活动类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class ArtistInfoActivity extends BaseActivity {
    private ScrollView svArtistInfo;
    private LinearLayout llArtistInfoContainer;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;

    /**
     * 向外部提供启动这个活动的方法
     */
    public static void start(Context context, String tingUid){
        Intent intent = new Intent(context, ArtistInfoActivity.class);
        intent.putExtra(Extras.TING_UID, tingUid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        svArtistInfo = (ScrollView)findViewById(R.id.sv_artist_info);
        llArtistInfoContainer = (LinearLayout)findViewById(R.id.ll_artist_info_container);
        llLoading = (LinearLayout)findViewById(R.id.ll_loading_artist_info);
        llLoadFail = (LinearLayout)findViewById(R.id.ll_load_fail_artist_info);

        String tingUid = getIntent().getStringExtra(Extras.TING_UID);
        getArtistInfo(tingUid);
        ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }


    /**
     * 获取歌手信息
     */
    private void getArtistInfo(String tingUid) {

        /**
         * 通过以下API获得
         * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.artist.getInfo&tinguid=1376
         */
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_ARTIST_INFO)
                .addParams(Constants.PARAM_TING_UID, tingUid)
                .build()
                .execute(new JsonCallback<JsonArtisitInfo>(JsonArtisitInfo.class) {
                    @Override
                    public void onResponse(JsonArtisitInfo response) {
                        if (response == null) {
                            //加载成功
                            ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                            return;
                        }
                        ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                        onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    }
                });


    }

    private void onSuccess(JsonArtisitInfo jsonArtistInfo) {
        String name = jsonArtistInfo.getName();
        String avatarUri = jsonArtistInfo.getAvatar_s1000();//头像
        String country = jsonArtistInfo.getCountry();
        String constellation = jsonArtistInfo.getConstellation();//星座
        float stature = jsonArtistInfo.getStature();//身高
        float weight = jsonArtistInfo.getWeight();
        String birth = jsonArtistInfo.getBirth();
        String intro = jsonArtistInfo.getIntro();//歌手简介
        String url = jsonArtistInfo.getUrl();//歌手链接,在底部查看更多

        if(!TextUtils.isEmpty(avatarUri)){
            ImageView ivAvatar = new ImageView(this);
            ivAvatar.setImageResource(R.drawable.default_artist);
            ivAvatar.setScaleType(ImageView.ScaleType.FIT_START);//把图片按比例扩大(缩小)到View的宽度，显示在View的上部分位置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.default_artist)
                    .showImageForEmptyUri(R.drawable.default_artist)
                    .showImageOnFail(R.drawable.default_artist)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(avatarUri, ivAvatar, options);
            llArtistInfoContainer.addView(ivAvatar);//在动态添加View
        }

        if (!TextUtils.isEmpty(name)){
            setTitle(name);

            /**
             * inflate(int resource, ViewGroup root, boolean attachToRoot)
             * 使用LayoutInflate的inflate方法的时候一定要保证root参数不能为null，其实这个root就是父View的意思，就是说你把xml转换为一个VIew的时候，该VIew的Parent是root，如果你不想把该View添加到该root里，那么让第三个参数 attachToRoot为false，如果要添加则为true.
             */

            TextView tvName = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvName.setText(getString(R.string.artist_info_name, name));
            llArtistInfoContainer.addView(tvName);
        }

        if (!TextUtils.isEmpty(country)) {
            TextView tvCountry = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvCountry.setText(getString(R.string.artist_info_country, country));
            llArtistInfoContainer.addView(tvCountry);
        }
        if (!TextUtils.isEmpty(constellation) && !constellation.equals("未知")) {
            TextView tvConstellation = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvConstellation.setText(getString(R.string.artist_info_constellation, constellation));
            llArtistInfoContainer.addView(tvConstellation);
        }

        if (stature != 0f) {
            TextView tvStature = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvStature.setText(getString(R.string.artist_info_stature, String.valueOf(stature)));
            llArtistInfoContainer.addView(tvStature);
        }
        if (weight != 0f) {
            TextView tvWeight = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvWeight.setText(getString(R.string.artist_info_weight, String.valueOf(weight)));
            llArtistInfoContainer.addView(tvWeight);
        }
        if (!TextUtils.isEmpty(birth) && !birth.equals("0000-00-00")) {
            TextView tvBirth = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvBirth.setText(getString(R.string.artist_info_birth, birth));
            llArtistInfoContainer.addView(tvBirth);
        }
        if (!TextUtils.isEmpty(intro)) {
            TextView tvIntro = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvIntro.setText(getString(R.string.artist_info_intro, intro));
            llArtistInfoContainer.addView(tvIntro);
        }
        if (!TextUtils.isEmpty(url)){
            TextView tvUrl = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            String html = "<font color='#2196F3'><a href='%s'>查看更多信息</a></font>";//<a href="indx.htm">显示超链接的文字</a>,%s格式对应字符串
            /**
             * 将比如文本框中的字符串进行HTML格式化
             * <font color='#2196F3'><a href='http:\/\/music.baidu.com\/artist\/1376'>查看更多信息</a></font>
             */
            tvUrl.setText(Html.fromHtml(String.format(html, url)));
            tvUrl.setMovementMethod(LinkMovementMethod.getInstance());//设置超链接
            tvUrl.setGravity(Gravity.CENTER);
            llArtistInfoContainer.addView(tvUrl);
        }

        if (llArtistInfoContainer.getChildCount() == 0) {//返回的是现实层面上所包含的子View个数。
            ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            ((TextView) llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.artist_info_empty);
        }

    }


}
