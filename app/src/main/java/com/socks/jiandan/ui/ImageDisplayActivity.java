package com.socks.jiandan.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.socks.jiandan.R;
import com.socks.jiandan.base.BaseActivity;
import com.socks.jiandan.base.JDApplication;
import com.socks.jiandan.model.CasualPhoto;
import com.socks.jiandan.net.Request4ImageDetail;
import com.socks.jiandan.net.RequestManager;
import com.socks.jiandan.utils.ShowToast;
import com.socks.jiandan.view.SwipeFrameLayout;
import com.socks.jiandan.view.imageloader.ImageLoadProxy;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.functions.Consumer;

public class ImageDisplayActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, SwipeFrameLayout.SwipeListener {
    public static final String URL = "https://carewatchdev.qkcorp.qiku.com/python/thread/detail?tid=%s";
    //            FansDef.getBBSServerUrl() + "apkapi/coolpynew.php?act=atlas&tid=%s";
    public static SimpleDateFormat timeformat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA);
    private static final String URL_SHARE_KUPAI = "";
    //            FansDef.getBBSServerUrl() + "coolpy/detail.php?tid=%s";
    private TextView tvContent;
    private String tid;
    private String authorId;
    private ViewPager vp;
    private ImageView ivLike;
    private TextView tvPage;
    private int totalPage;
    private CasualPhoto info;
    private RelativeLayout rlMain;
    private LinearLayout llMenu;
    private ScrollView sv;
    private ImageView ivDownload;
    private ImagePagerAdapter adapter;
    private RxPermissions permissions;
    private TextView tvDesc;
    private TextView tvLikeBadge;
    private ProgressBar pbLoading;
    private LinearLayout llRetry;
    private SwipeFrameLayout sfl;

    private Object requestTag = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        permissions = new RxPermissions(this);

        sfl = findViewById(R.id.sfl_image_display);
        tvLikeBadge = findViewById(R.id.tv_image_display_badge_like);
        llRetry = findViewById(R.id.ll_image_display_retry);
        pbLoading = findViewById(R.id.pb_image_display_loading);
        tvContent = findViewById(R.id.tv_image_display_content);
        tvDesc = findViewById(R.id.tv_image_display_desc);
        ivLike = findViewById(R.id.iv_image_display_like);
        tvPage = findViewById(R.id.tv_image_display_page);
        rlMain = findViewById(R.id.rl_image_display_main);
        llMenu = findViewById(R.id.ll_image_display_menu);
        ivDownload = findViewById(R.id.iv_image_display_download);
        sv = findViewById(R.id.sv_image_display_text);
        sfl.setSwipeListener(this);
        findViewById(R.id.iv_image_display_back).setOnClickListener(this);
        ivLike.setOnClickListener(this);
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

        pbLoading.setVisibility(View.VISIBLE);
        llRetry.setVisibility(View.GONE);

        RequestManager.addRequest(new Request4ImageDetail(url, new Response.Listener<CasualPhoto>() {
            @Override
            public void onResponse(CasualPhoto response) {
                if (response == null) {
                    ShowToast.Short("获取页面数据失败");
                } else {
                    setupUI(response);
                }

                pbLoading.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (sfl.getVisibility() == View.GONE)
                    llRetry.setVisibility(View.VISIBLE);

                pbLoading.setVisibility(View.GONE);
            }
        }), requestTag);
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
        if (info.data.liked == 1)
            setViewToLiked();

        if (info.data.likeCount > 0) {
            tvLikeBadge.setVisibility(View.VISIBLE);
            tvLikeBadge.setText(String.valueOf(info.data.likeCount));
        }

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
            ShowToast.Short("解析数据失败");
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        RequestManager.cancelAll(requestTag);
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
                finish();
                break;
            case R.id.iv_image_display_download:
                onDownload();
                break;
            case R.id.iv_image_display_like:
                onLike(v);
                break;
            case R.id.ll_image_display_retry:
                requestData();
                break;
        }
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
                            Toast.makeText(ImageDisplayActivity.this, "需要文件读写权限才能保存图片", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void download(String url) {
        ImageLoadProxy.displayImage4Detail(url, new ImageView(this), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                ShowToast.Short("下载失败");
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                String dir = Environment.getExternalStorageDirectory() + File.separator + "meiliba" + File.separator + "download" + File.separator;
                File d = new File(dir);
                if (!d.isDirectory() && !d.mkdirs()) {
                    ShowToast.Short("创建下载文件夹失败");
                    return;
                }
                String name = dir + "image_" + timeformat.format(new Date(System.currentTimeMillis())) + ".jpg";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(name);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    ShowToast.Long("已下载到文件夹" + dir);

                    JDApplication.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + name)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                ShowToast.Short("已取消下载");
            }
        });
    }

    private void hideOrShowMenu() {
        if (llMenu.getVisibility() == View.INVISIBLE) {
            llMenu.setVisibility(View.VISIBLE);
            sv.setVisibility(View.VISIBLE);
            ivDownload.setVisibility(View.INVISIBLE);
        } else {
            llMenu.setVisibility(View.INVISIBLE);
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
        sv.setAlpha(alpha);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

//    @Override
//    public void onResult(int result, String msg, String tid, boolean doLike) {
//        if (result == 1) { // success
//            addLikeCount();
//            setLikeResult();
//            setViewToLiked();
//        } else {
//            if (msg != null && msg.contains("已经")) {
//                setLikeResult();
//                setViewToLiked();
//            }
//            Toast.makeText(getApplicationContext(), msg == null ? "请求失败" : msg, Toast.LENGTH_SHORT).show();
//        }
//    }

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
        private Context context;

        private ImagePagerAdapter(Context context, List<CasualPhoto.ImageText> data) {
            this.context = context;
            this.data = data;
            views = new ArrayList<>(data.size());

            setupUI();
        }

        private void setupUI() {
            views.clear();
            for (int i = 0; i < data.size(); i++) {
                ViewHolder vh = new ViewHolder();
                FrameLayout fl = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.item_image_display, null, false);
                PhotoView pv = fl.findViewById(R.id.iv_item_image_display);
                pv.setScaleLevels(1, 1.5f, 2.0f);

                vh.fl = fl;
                vh.pv = pv;
                vh.pb = fl.findViewById(R.id.pb_item_image_display);
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

            Glide.with(context)
                    .load(data.get(position).url)
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (views != null && views.size() > position) {
                                views.get(position).tv.setVisibility(View.VISIBLE);
                                views.get(position).pb.setVisibility(View.GONE);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            views.get(position).pb.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(views.get(position).pv);
//            ImageLoadProxy.displayImage4Detail(data.get(position).url, views.get(position).pv, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String s, View view) {
//                    if (views != null && views.size() > position) {
//                        views.get(position).pb.setVisibility(View.VISIBLE);
//                        views.get(position).tv.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void onLoadingFailed(String s, View view, FailReason failReason) {
//                    if (views != null && views.size() > position) {
//                        views.get(position).pb.setVisibility(View.GONE);
//                        views.get(position).tv.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    if (views != null && views.size() > position) {
//                        views.get(position).pb.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void onLoadingCancelled(String s, View view) {
//                    if (views != null && views.size() > position)
//                        views.get(position).pb.setVisibility(View.GONE);
//                }
//        });
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
            TextView tv;
            ProgressBar pb;
        }
    }
}
