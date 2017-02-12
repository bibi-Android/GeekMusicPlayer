package me.geek.music.fragment;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.GenericArrayType;

import me.geek.music.R;
import me.geek.music.adapter.LocalMusicAdapter;
import me.geek.music.adapter.OnMoreClickListener;
import me.geek.music.application.AppCache;
import me.geek.music.model.Music;
import me.geek.music.service.PlayService;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.SystemUtils;
import me.geek.music.utils.ToastUtils;

import static me.geek.music.application.AppCache.getContext;

/**
 * 本地音乐列表
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener {

    private ListView lvLocalMusic;
    private TextView tvEmpty;
    private LocalMusicAdapter mAdapter;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_local_music, container, false);
        lvLocalMusic = (ListView)view.findViewById(R.id.lv_local_music);
        tvEmpty = (TextView)view.findViewById(R.id.tv_empty);

        return view;
    }

    @Override
    protected void init() {
        mAdapter = new LocalMusicAdapter();
        mAdapter.setOnMoreClickListener(this);
        lvLocalMusic.setAdapter(mAdapter);
        if(getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL){
            lvLocalMusic.setSelection(getPlayService().getPlayingPosition());//将列表移动到指定的Position处
        }
        updateListView();

        //当下载完歌曲以后,更新本地歌曲列表
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getContext().registerReceiver(mDownloadReceiver, filter);
    }

    private void updateListView() {
        if(PlayService.getsMusicList().isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getPlayService().play(position);//点击ListView的一行就开始播放
    }

    @Override
    //重写onMoreClick,自定义接口的内容
    public void onMoreClick(int position) {
        final Music music = PlayService.getsMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        //如果歌曲正在播放,则不显示删除按钮
        int itemId = position == getPlayService().getPlayingPosition()? R.array.local_music_dialog_without_delete : R.array.local_music_dialog;
        //builder.setItems(int itemsId, final OnClickListener)itemsId为需要显示的资源
        dialog.setItems(itemId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://分享
                        shareMusic(music);
                        break;
                    case 1://设置为铃声
                        setRingtong(music);
                        break;
                    case 2://查看歌曲信息
                        musicInfo(music);
                        break;
                    case 3://删除
                        deleteMusic(music);
                }
            }
        });
        dialog.show();
    }

    /**
     * 分享音乐
     */
    private void shareMusic(Music music){
        File file = new File(music.getUri());
        Intent intent = new Intent(Intent.ACTION_SEND);//系统分享功能
        intent.setType("audio/*");//intent读取的类型
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, "分享"));
    }

    /**
     * 设置铃声
     */
    private void setRingtong(Music music){
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getUri());
        //查询音乐文件在媒体库是否存在
        Cursor cursor = getContext().getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[]{music.getUri()}, null);
        if(cursor==null){
            return;
        }
        if(cursor.moveToFirst() && cursor.getCount() > 0){
            String _id = cursor.getString(0);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_PODCAST, false);

            //更新MediaStore提供器中的Meida信息
            getContext().getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?", new String[]{music.getUri()});
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));//把歌曲的id跟contentUri连接成一个新的Uri
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            ToastUtils.show("设置铃声成功");

        }
        cursor.close();
    }

    /**
     * 歌曲信息
     */
    private void musicInfo(Music music){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        StringBuilder sb = new StringBuilder();
        sb.append("艺术家: ")
                .append(music.getArtist())
                .append("\n\n")//两个回车
                .append("专辑: ")
                .append(music.getAlbum())
                .append("\n\n")
                .append("播放时长: ")
                .append(SystemUtils.formatTime("mm:ss", music.getDuration()))
                .append("\n\n")
                .append("文件名称: ")
                .append(music.getFileName())
                .append("\n\n")
                .append("文件大小: ")
                .append(FileUtils.b2mb((int)music.getFileSize()))
                .append("\n\n")
                .append("文件路径: ")
                .append(new File(music.getUri()).getParent());//获得父目录
        dialog.setMessage(sb.toString());
        dialog.show();
    }

    /**
     * 删除音乐
     */
    private void deleteMusic(final Music music){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        final String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlayService.getsMusicList().remove(music);
                File file = new File(music.getUri());
                if(file.delete()){
                    getPlayService().updatePlayingPosition();
                    updateListView();
                    //刷新媒体库
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + music.getUri()));
                    getContext().sendBroadcast(intent);
                }
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mDownloadReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);//能得到下载完成的文件存在数据库中的ID
            String title = AppCache.getDownloadList().get(id);
            if(TextUtils.isEmpty(title)){
                return;
            }
            //由于系统扫描音乐是异步执行,因此延迟刷新音乐列表
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!isAdded()){
                        return;
                    }
                    getPlayService().updateMusicList();
                    updateListView();
                }
            }, 1000);
        }
    };

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.smoothScrollToPosition(getPlayService().getPlayingPosition());//滑动到正在播放音乐的位置
        }

    }

    private void updateView() {
        if (PlayService.getsMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

}
