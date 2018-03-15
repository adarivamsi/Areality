package com.passionateburger.areality;

import com.wikitude.common.camera.CameraSettings;

/**
 * Created by adari on 3/15/2018.
 */

public abstract class AutoHdSampleCamActivity extends SampleCamActivity {
    @Override public CameraSettings.CameraResolution getCameraResolution()
    {
        return CameraSettings.CameraResolution.AUTO;
    }
    protected abstract boolean getCamera2Enabled();
}