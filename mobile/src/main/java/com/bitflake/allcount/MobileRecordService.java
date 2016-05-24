package com.bitflake.allcount;

import com.bitflake.counter.services.CountServiceHelper;
import com.bitflake.counter.services.WearRecordService;

public class MobileRecordService extends WearRecordService {
    @Override
    public void onFinishedRecording() {
        super.onFinishedRecording();
        new CountServiceHelper(this).startServiceAndCounting(MobileCountService.class, getStates(), 1);
    }
}
