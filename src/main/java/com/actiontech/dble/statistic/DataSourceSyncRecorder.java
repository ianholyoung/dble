/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.actiontech.dble.statistic;

import com.actiontech.dble.config.model.DataHostConfig;
import com.actiontech.dble.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * record avg time of a,b,c minutes,the default is 1,10,30
 *
 * @author songwie
 */
public class DataSourceSyncRecorder {

    private Map<String, String> records;
    private final List<Record> asynRecords; //value,time
    private static final Logger LOGGER = LoggerFactory.getLogger("DataSourceSyncRecorder");


    private static final long SWAP_TIME = 24 * 60 * 60 * 1000L;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int switchType = 2;

    public DataSourceSyncRecorder() {
        this.records = new HashMap<>();
        this.asynRecords = new LinkedList<>();
    }

    public String get() {
        return records.toString();
    }

    public void set(Map<String, String> resultResult, int switchtype) {
        try {
            long time = TimeUtil.currentTimeMillis();
            this.switchType = switchtype;

            remove(time);

            if (resultResult != null && !resultResult.isEmpty()) {
                this.records = resultResult;
                if (switchtype == DataHostConfig.SYN_STATUS_SWITCH_DS) {  //slave
                    String sencords = resultResult.get("Seconds_Behind_Master");
                    long secondsBehindMaster = -1;
                    if (sencords != null) {
                        secondsBehindMaster = Long.parseLong(sencords);
                    }
                    this.asynRecords.add(new Record(TimeUtil.currentTimeMillis(), secondsBehindMaster));
                }
                if (switchtype == DataHostConfig.CLUSTER_STATUS_SWITCH_DS) { //cluster
                    double wsrepLocalRecvQueueAvg = Double.valueOf(resultResult.get("wsrep_local_recv_queue_avg"));
                    this.asynRecords.add(new Record(TimeUtil.currentTimeMillis(), wsrepLocalRecvQueueAvg));
                }

                return;
            }
        } catch (Exception e) {
            LOGGER.error("record DataSourceSyncRecorder error " + e.getMessage());
        }

    }

    /**
     * remove the old data
     */
    private void remove(long time) {
        final List<Record> recordsAll = this.asynRecords;
        while (recordsAll.size() > 0) {
            Record record = recordsAll.get(0);
            if (time >= record.time + SWAP_TIME) {
                recordsAll.remove(0);
            } else {
                break;
            }
        }
    }

    public int getSwitchType() {
        return this.switchType;
    }

    public void setSwitchType(int switchType) {
        this.switchType = switchType;
    }

    public Map<String, String> getRecords() {
        return this.records;
    }

    public List<Record> getAsynRecords() {
        return this.asynRecords;
    }

    public static SimpleDateFormat getSdf() {
        return SDF;
    }

    /**
     * @author mycat
     */
    public static class Record {
        private Object value;
        private long time;

        Record(long time, Object value) {
            this.time = time;
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public long getTime() {
            return this.time;
        }

        public void setTime(long time) {
            this.time = time;
        }


    }
}
