/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aoscp.setupwizard;

import static org.aoscp.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.aoscp.setupwizard.SetupWizardApp.KEY_APPLY_DEFAULT_THEME;
import static org.aoscp.setupwizard.SetupWizardApp.KEY_BUTTON_BACKLIGHT;
import static org.aoscp.setupwizard.SetupWizardApp.KEY_PRIVACY_GUARD;
import static org.aoscp.setupwizard.SetupWizardApp.KEY_SEND_METRICS;
import static org.aoscp.setupwizard.SetupWizardApp.LOGV;

import android.animation.Animator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.setupwizardlib.util.WizardManagerHelper;

import org.aoscp.setupwizard.util.EnableAccessibilityController;
import org.aoscp.setupwizard.util.SetupWizardUtils;

public class FinishActivity extends BaseSetupWizardActivity {

    public static final String TAG = FinishActivity.class.getSimpleName();

    private ImageView mReveal;
    private ProgressBar mFinishingProgressBar;

    private EnableAccessibilityController mEnableAccessibilityController;

    private SetupWizardApp mSetupWizardApp;

    private Button mNext;

    private final Handler mHandler = new Handler();

    private volatile boolean mIsFinishing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=" + savedInstanceState);
        }
        mSetupWizardApp = (SetupWizardApp) getApplication();
        mReveal = (ImageView) findViewById(R.id.reveal);
        mFinishingProgressBar = (ProgressBar)findViewById(R.id.finishing_bar);
        findViewById(R.id.setup_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavigateNext();
            }
        });
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
    }

    @Override
	public void onNavigateNext() {
        onNextPressed();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.finish_activity;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translucent_enter, R.anim.translucent_exit);
    }

    private void finishSetup() {
        if (!mIsFinishing) {
            mIsFinishing = true;
            setupRevealImage();
        }
    }

    private void startFinishSequence() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideBackButton();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        mFinishingProgressBar.setVisibility(View.VISIBLE);
        mFinishingProgressBar.setIndeterminate(true);
        mFinishingProgressBar.startAnimation(fadeIn);
        this.finishSetup();
    }

    public void onFinish(boolean isSuccess) {
        if (isResumed()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startFinishSequence();
                }
            });
        }
    }

    public void onProgress(int progress) {
        if (progress > 0) {
            mFinishingProgressBar.setIndeterminate(false);
            mFinishingProgressBar.setProgress(progress);
        }
    }

    private void setupRevealImage() {
        mFinishingProgressBar.setProgress(100);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        mFinishingProgressBar.startAnimation(fadeOut);
        mFinishingProgressBar.setVisibility(View.INVISIBLE);

        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(this);
        wallpaperManager.forgetLoadedWallpaper();
        final Bitmap wallpaper = wallpaperManager.getBitmap();
        Bitmap cropped = null;
        if (wallpaper != null) {
            cropped = Bitmap.createBitmap(wallpaper, 0,
                    0, Math.min(p.x, wallpaper.getWidth()),
                    Math.min(p.y, wallpaper.getHeight()));
        }
        if (cropped != null) {
            mReveal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mReveal.setImageBitmap(cropped);
        } else {
            mReveal.setBackground(wallpaperManager
                    .getBuiltInDrawable(p.x, p.y, false, 0, 0));
        }
        animateOut();
    }

    private void animateOut() {
        int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
        int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
        int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);
        anim.setDuration(900);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        completeSetup();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    private void completeSetup() {
        if (mEnableAccessibilityController != null) {
            mEnableAccessibilityController.onDestroy();
        }
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(mSetupWizardApp);
        wallpaperManager.forgetLoadedWallpaper();
        finishAllAppTasks();
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(),
                Activity.RESULT_OK);
        startActivityForResult(intent, NEXT_REQUEST);
    }
}
