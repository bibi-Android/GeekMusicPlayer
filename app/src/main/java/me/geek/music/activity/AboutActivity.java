package me.geek.music.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import me.geek.music.BuildConfig;
import me.geek.music.R;

/**
 * 关于极客音乐的活动类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getFragmentManager().beginTransaction().replace(R.id.ll_fragment_container_about, new AboutFragment()).commit();
    }

    public static class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference mVersion;
        private Preference mShare;
        private Preference mWeibo;
        private Preference mJianshu;
        private Preference mGithub;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefence_about);

            mVersion = findPreference("version");
            mShare = findPreference("share");
            mWeibo = findPreference("weibo");
            mJianshu = findPreference("jianshu");
            mGithub = findPreference("github");

            mVersion.setSummary("v " + BuildConfig.VERSION_NAME);
            setListener();
        }

        private void setListener() {
            mShare.setOnPreferenceClickListener(this);
            mWeibo.setOnPreferenceClickListener(this);
            mJianshu.setOnPreferenceClickListener(this);
            mGithub.setOnPreferenceClickListener(this);
        }

        /**
         * 设置各类监听器
         */
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == mShare) {
                share();
                return true;
            } else if (preference == mWeibo || preference == mJianshu || preference == mGithub) {
                openUrl(preference.getSummary().toString());
                return true;
            }
            return false;
        }

        /**
         * 分享给朋友
         */
        private void share() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app, getString(R.string.app_name)));
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        }


        /**
         * 打开url链接
         */
        private void openUrl(String url) {
            //打开浏览器
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }
}
