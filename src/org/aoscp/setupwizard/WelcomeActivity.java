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

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.aoscp.setupwizard.util.EnableAccessibilityController;

public class WelcomeActivity extends BaseSetupWizardActivity {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

	private Button mButton;
    private View mRootView;
    private EnableAccessibilityController mEnableAccessibilityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = findViewById(R.id.root);
		mButton = (Button) findViewById(R.id.start);
		mButton.setText(R.string.start);
		mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onNavigateNext();
            }
        });
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consumeIntercept = mEnableAccessibilityController.onInterceptTouchEvent(event);
                boolean consumeTouch = mEnableAccessibilityController.onTouchEvent(event);
                return consumeIntercept && consumeTouch;
            }
        });
    }
	
	@Override
	public void onNextPressed() {
        nextAction(NEXT_REQUEST);
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
        return R.layout.welcome_activity;
    }
}
