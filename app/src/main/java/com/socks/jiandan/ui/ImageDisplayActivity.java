package com.socks.jiandan.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra15.universalimageloader.core.assist.FailReason;
import com.nostra15.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra15.universalimageloader.core.assist.LoadedFrom;
import com.qiku.bbs.CoolYouAppState;
import com.qiku.bbs.QikuApplication;
import com.qiku.bbs.ShareConstanse;
import com.qiku.bbs.module.bindphone.PhoneNumberBinder;
import com.qiku.bbs.module.common.Navigator;
import com.qiku.bbs.module.network.OkErrors;
import com.qiku.bbs.module.network.OkGoes;
import com.qiku.bbs.module.qbpatch.QBPatch;
import com.qiku.bbs.module.user.LikeOperation;
import com.qiku.bbs.util.SharedUtil;
import com.qiku.bbs.util.SinaWbShare;
import com.qiku.bbs.util.Util;
import com.qiku.bbs.views.ReplyDialog;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.socks.jiandan.R;
import com.socks.jiandan.base.BaseActivity;
import com.socks.jiandan.model.CasualPhoto;
import com.socks.jiandan.net.RequestManager;
import com.socks.jiandan.view.SwipeFrameLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ImageDisplayActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, SwipeFrameLayout.SwipeListener {
    public static final String URL = "";
    //            FansDef.getBBSServerUrl() + "apkapi/coolpynew.php?act=atlas&tid=%s";
    public static SimpleDateFormat timeformat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA);
    private static final String URL_SHARE_KUPAI = "";
    //            FansDef.getBBSServerUrl() + "coolpy/detail.php?tid=%s";
    private TextView tvContent;
    private String tid;
    private String authorId;
    private ViewPager vp;
    private TextView tvUserName;
    private ImageView ivAvatar;
    private ImageView ivLike;
    private ImageView ivComment;
    private ImageView ivShare;
    private TextView tvFans;
    private TextView tvPage;
    private TextView tvProduction;
    private int totalPage;
    private CasualPhoto info;
    private RelativeLayout rlMain;
    private RelativeLayout rlTop;
    private LinearLayout llMenu;
    private ScrollView sv;
    private ImageView ivDownload;
    private ImagePagerAdapter adapter;
    private RxPermissions permissions;
    private TextView tvDesc;
    private TextView tvReply;
    private TextView tvCommentBadge;
    private TextView tvLikeBadge;
    private ProgressBar pbLoading;
    private LinearLayout llRetry;
    private SwipeFrameLayout sfl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        permissions = new RxPermissions(this);

        sfl = findViewById(R.id.sfl_image_display);
        tvCommentBadge = findViewById(R.id.tv_image_display_badge_comment);
        tvLikeBadge = findViewById(R.id.tv_image_display_badge_like);
        llRetry = findViewById(R.id.ll_image_display_retry);
        pbLoading = findViewById(R.id.pb_image_display_loading);
        tvContent = findViewById(R.id.tv_image_display_content);
        tvUserName = findViewById(R.id.tv_image_display_name);
        tvDesc = findViewById(R.id.tv_image_display_desc);
        tvReply = findViewById(R.id.tv_image_display_reply);
        tvProduction = findViewById(R.id.tv_image_display_production);
        tvFans = findViewById(R.id.tv_image_display_fans);
        ivAvatar = findViewById(R.id.iv_image_display_avatar);
        ivComment = findViewById(R.id.iv_image_display_comment);
        ivLike = findViewById(R.id.iv_image_display_like);
        ivShare = findViewById(R.id.iv_image_display_share);
        tvPage = findViewById(R.id.tv_image_display_page);
        rlMain = findViewById(R.id.rl_image_display_main);
        llMenu = findViewById(R.id.ll_image_display_menu);
        rlTop = findViewById(R.id.rl_image_display_top);
        ivDownload = findViewById(R.id.iv_image_display_download);
        sv = findViewById(R.id.sv_image_display_text);
        sfl.setSwipeListener(this);
        findViewById(R.id.iv_image_display_back).setOnClickListener(this);
        ivAvatar.setOnClickListener(this);
        ivComment.setOnClickListener(this);
        ivLike.setOnClickListener(this);
        ivShare.setOnClickListener(this);
        tvUserName.setOnClickListener(this);
        tvReply.setOnClickListener(this);
        ivDownload.setOnClickListener(this);
        llRetry.setOnClickListener(this);

        vp = findViewById(R.id.vp_image_display);
        vp.addOnPageChangeListener(this);

        initData();
//        Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "onCreate");
    }

    protected void initData() {
        tid = getIntent().getStringExtra("tid");
        requestData();
    }

    private void requestData() {
        final String url = String.format(URL, tid);

        RequestManager.addRequest();

        OkGoes.<String>get(url)
                .cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onFinish() {
                        super.onFinish();
                        pbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        pbLoading.setVisibility(View.VISIBLE);
                        llRetry.setVisibility(View.GONE);
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        CasualPhoto info = parseContent(response.body());
                        if (info == null)
                            return;
                        setupUI(info);
                    }

                    @Override
                    public void onCacheSuccess(Response<String> response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        OkErrors.onErrorPrompt(response);
                        if (sfl.getVisibility() == View.GONE)
                            llRetry.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void setupUI(CasualPhoto info) {
        this.info = info;

        sfl.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.GONE);
        llRetry.setVisibility(View.GONE);

        if (info.data == null || info.data.images == null || info.data.images.size() == 0) {
            Toast.makeText(this, "图片丢失啦，看看其他图集吧~", Toast.LENGTH_SHORT).show();
            return;
        }
        List<CasualPhoto.ImageText> images = info.data.images;
        authorId = info.data.authorId;

        totalPage = images.size();
        if (adapter == null) {
            adapter = new ImagePagerAdapter(this, images);
            vp.setAdapter(adapter);
        } else {
            adapter.setData(images);
        }

        tvContent.setText(info.data.message);
        tvUserName.setText(info.data.author);
        if (info.data.fans > 0)
            tvFans.setText(String.valueOf(info.data.fans) + "粉丝");
        if (info.data.liked == 1)
            setViewToLiked();
        if (info.data.productionCount > 0) {
            tvProduction.setVisibility(View.VISIBLE);
            tvProduction.setText(String.valueOf(info.data.productionCount) + " 作品");
            tvProduction.setOnClickListener(this);
        }

        if (info.data.replyCount > 0) {
            tvCommentBadge.setVisibility(View.VISIBLE);
            tvCommentBadge.setText(String.valueOf(info.data.replyCount));
        }
        if (info.data.likeCount > 0) {
            tvLikeBadge.setVisibility(View.VISIBLE);
            tvLikeBadge.setText(String.valueOf(info.data.likeCount));
        }


        CoolYouAppState.getInstance().getBlockImages().SynDisplayImage(info.data.avatarUrl, ivAvatar);

        onPageSelected(0);
    }

    private void setViewToLiked() {
        ivLike.setImageResource(R.drawable.coolyou_post_recomment);
    }

    private CasualPhoto parseContent(String body) {
        CasualPhoto result = null;
        try {
            Gson g = new Gson();
            result = g.fromJson(body, CasualPhoto.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.parse_json_data_failed, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        OkGo.getInstance().cancelTag(this);
        super.onDestroy();
    }

    @Override
    protected void initView() {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        String head = String.format(Locale.CHINA, "%d/%d  ", i + 1, totalPage);
        SpannableString ss = new SpannableString(head);
        AbsoluteSizeSpan span = new AbsoluteSizeSpan(100);
        ss.setSpan(span, 0, i < 9 ? 1 : 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvPage.setText(ss);

        String desc = adapter.getDesc(i);
        if (!TextUtils.isEmpty(desc)) {
            tvDesc.setVisibility(View.VISIBLE);
            tvDesc.setText(desc);
            tvContent.setVisibility(i != 0 ? View.GONE : View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
            tvContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_image_display_back:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "back");
                finish();
                break;
            case R.id.iv_image_display_download:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "download");
                onDownload();
                break;
            case R.id.iv_image_display_avatar:
            case R.id.tv_image_display_name:
            case R.id.tv_image_display_production:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "avatar");
                toPersonalPhotoPage();
                break;
            case R.id.iv_image_display_like:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "like");
                onLike(v);
                break;
            case R.id.iv_image_display_share:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "share");
                onShare();
                break;
            case R.id.tv_image_display_reply:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "reply");
                onReply();
                break;
            case R.id.iv_image_display_comment:
                Util.onQHStateAgentEvent(getApplicationContext(), "ImageDisplay", "position", "comment");
                Navigator.toRepliesActivity(this, tid);
                break;
            case R.id.ll_image_display_retry:
                requestData();
                break;
        }
    }

    private void onReply() {
        if (TextUtils.isEmpty(tid))
            return;
//        if (rDialog == null)
//            rDialog = new ReplyDialog(this, tid);
//        rDialog.show();
    }

    private void onShare() {
//        if (info == null || info.data == null)
//            return;
//        if (TextUtils.isEmpty(info.data.message))
//            return;
//        if (wbShare == null)
//            wbShare = new SinaWbShare(this);
//        try {
//            String title = info.data.message;
//            SharedUtil.getInstance().sharedBlockConnect(this, wbShare, String.format(URL_SHARE_KUPAI, tid),
//                    title, "", null, null);
//        } catch (Exception e) {
//            Toast.makeText(this, getString(R.string.coolyou_weibosdk_toast_share_failed), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        if (wbShare != null)
//            wbShare.setHandleWeiboResponse(intent, SharedUtil.getCommonWbShareCallback());
    }

    private void toPersonalPhotoPage() {
//        if (info != null && !TextUtils.isEmpty(info.data.authorId))
//            Navigator.toPersonCenterActivityPhotoPage(this, info.data.authorId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (mTencent == null)
//            mTencent = Tencent.createInstance(ShareConstanse.QQ_APP_ID, QikuApplication.getApplication());
//        mTencent.onActivityResult(requestCode, resultCode, data);
//        if (wbShare != null) {
//            SsoHandler ssoHandler = wbShare.getSsoHandler();
//            if (ssoHandler != null)
//                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
//        }
    }

    private void onLike(final View v) {
//        PhoneNumberBinder.onPhoneNumberBind((Activity) tvContent.getContext(), new Runnable() {
//            @Override
//            public void run() {
//                like();
//            }
//        }, null);
    }

    private void like() {
        if (TextUtils.isEmpty(authorId) || TextUtils.isEmpty(tid)) {
            return;
        }
//        LikeOperation operation = new LikeOperation(this, this).asCasualPhotoOperation();
//        operation.like(authorId, tid);
    }

    private void onDownload() {
        if (adapter == null)
            return;
        final String url = adapter.getUrl(vp.getCurrentItem());
        if (TextUtils.isEmpty(url)) {
//            Toast.makeText(this, R.string.cannot_download_the_image, Toast.LENGTH_SHORT).show();
            return;
        }

        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) {
                        if (granted) {
                            download(url);
                        } else
                            Toast.makeText(ImageDisplayActivity.this, R.string.need_sd_wr_permission, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void download(String url) {
        CoolYouAppState.getInstance().getBlockImages().displayImage(url, new ImageView(this), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                Toast.makeText(QikuApplication.getApplication(), R.string.image_download_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap, LoadedFrom loadedFrom, Drawable drawable) {
                String dir = Environment.getExternalStorageDirectory() + File.separator + "360bbs" + File.separator + "download" + File.separator;
                File d = new File(dir);
                if (!d.isDirectory() && !d.mkdirs()) {
                    Toast.makeText(ImageDisplayActivity.this, R.string.cannot_create_download_dir, Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = dir + "image_" + timeformat.format(new Date(System.currentTimeMillis())) + ".jpg";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(name);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(ImageDisplayActivity.this, R.string.saved_to_download_dir, Toast.LENGTH_SHORT).show();
                    QikuApplication.getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + name)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    QBPatch.quietlyClose(fos);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                Toast.makeText(QikuApplication.getApplication(), R.string.canceled_download_image, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideOrShowMenu() {
        if (llMenu.getVisibility() == View.INVISIBLE) {
            llMenu.setVisibility(View.VISIBLE);
            rlTop.setVisibility(View.VISIBLE);
            sv.setVisibility(View.VISIBLE);
            ivDownload.setVisibility(View.INVISIBLE);
        } else {
            llMenu.setVisibility(View.INVISIBLE);
            rlTop.setVisibility(View.INVISIBLE);
            sv.setVisibility(View.INVISIBLE);
            ivDownload.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSwipe(float yOffset) {
        float threshold = 600.0f;
        float offset = threshold - Math.abs(yOffset);
        if (offset < 0)
            offset = 0;
        float alpha = offset / threshold;
        rlMain.getBackground().mutate().setAlpha((int) (alpha * 255));
        llMenu.setAlpha(alpha);
        rlTop.setAlpha(alpha);
        sv.setAlpha(alpha);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void onResult(int result, String msg, String tid, boolean doLike) {
        if (result == 1) { // success
            addLikeCount();
            setLikeResult();
            setViewToLiked();
        } else {
            if (msg != null && msg.contains("已经")) {
                setLikeResult();
                setViewToLiked();
            }
            Toast.makeText(getApplicationContext(), msg == null ? "请求失败" : msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void addLikeCount() {
        info.data.likeCount++;
        if (info.data.likeCount > 0) {
            tvLikeBadge.setVisibility(View.VISIBLE);
            tvLikeBadge.setText(String.valueOf(info.data.likeCount));
        }
    }

    private void setLikeResult() {
        Intent data = new Intent();
        data.putExtra("haveClickedLike", true);
        data.putExtra("tid", tid);
        setResult(111, data);
    }

    private static class ImagePagerAdapter extends PagerAdapter implements View.OnClickListener {
        private List<ViewHolder> views;

        private List<CasualPhoto.ImageText> data;
        private DisplayImageOptions opts;
        private Context context;

        private ImagePagerAdapter(Context context, List<CasualPhoto.ImageText> data) {
            this.context = context;
            this.data = data;
            views = new ArrayList<>(data.size());

            opts = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            setupUI();
        }

        private void setupUI() {
            views.clear();
            for (int i = 0; i < data.size(); i++) {
                ViewHolder vh = new ViewHolder();
                FrameLayout fl = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.item_image_display, null, false);
                PhotoView pv = fl.findViewById(R.id.iv_item_image_display);
                pv.setScaleLevels(1, 1.5f, 2.0f);

                vh.pb = fl.findViewById(R.id.pb_item_image_display);
                vh.fl = fl;
                vh.pv = pv;
                vh.tv = fl.findViewById(R.id.tv_item_image_display_failed);
                vh.pv.setOnClickListener(this);
                vh.tv.setOnClickListener(this);
                vh.tv.setTag(i);
                views.add(vh);
            }
        }

        @Override
        public int getCount() {
            return views == null ? 0 : views.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            displayImage(position);
            container.addView(views.get(position).fl);
            return views.get(position).fl;
        }

        private void displayImage(final int position) {
            if (position >= data.size())
                return;
            load.displayImage(data.get(position).url, views.get(position).pv, opts, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    if (views != null && views.size() > position) {
                        views.get(position).pb.setVisibility(View.VISIBLE);
                        views.get(position).tv.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    if (views != null && views.size() > position) {
                        views.get(position).pb.setVisibility(View.GONE);
                        views.get(position).tv.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap, LoadedFrom loadedFrom, Drawable drawable) {
                    if (views != null && views.size() > position) {
                        views.get(position).pb.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    if (views != null && views.size() > position)
                        views.get(position).pb.setVisibility(View.GONE);
                }
            });
        }

        public String getUrl(int pos) {
            if (data == null || pos >= data.size())
                return null;
            return data.get(pos).url;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position).fl);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_item_image_display:
                    if (context instanceof ImageDisplayActivity)
                        ((ImageDisplayActivity) context).hideOrShowMenu();
                    break;
                case R.id.tv_item_image_display_failed:
                    displayImage((Integer) v.getTag());
                    break;
            }
        }

        private String getDesc(int pos) {
            if (data == null || pos >= data.size())
                return null;
            return data.get(pos).desc;
        }

        public void setData(List<CasualPhoto.ImageText> images) {
            if (data == null)
                data = images;
            else {
                data.clear();
                data.addAll(images);
            }
            notifyDataSetChanged();
        }

        private class ViewHolder {
            FrameLayout fl;
            PhotoView pv;
            ProgressBar pb;
            TextView tv;
        }
    }
}
