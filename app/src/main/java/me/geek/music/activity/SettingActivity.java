package me.geek.music.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import me.geek.music.R;
import me.geek.music.utils.ToastUtils;

/**
 * 功能设置活动类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getFragmentManager().beginTransaction().replace(R.id.ll_fragment_container_setting, new SettingFragment()).commit();
    }

    /**
     * 使用PreferenceFragment快速实现app设置页面
     * 点击按钮产生的boolean会保存到sharePrefences中,xml文件中的key就是prefences的名
     */
    public static class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference mSoundEffect;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //给这个PreferenceFragment指定了一个xml
            addPreferencesFromResource(R.xml.preference_setting);

            mSoundEffect = findPreference(getString(R.string.setting_key_sound_effect));
            mSoundEffect.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == mSoundEffect) {
                Intent intent = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
                intent.putExtra("android.media.extra.PACKAGE_NAME", getActivity().getPackageName());
                intent.putExtra("android.media.extra.CONTENT_TYPE", 0);
                intent.putExtra("android.media.extra.AUDIO_SESSION", 0);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(R.string.device_not_support);
                }
                return true;
            }
            return false;
        }
    }
}
