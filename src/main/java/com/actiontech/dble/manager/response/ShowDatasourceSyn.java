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
package com.actiontech.dble.manager.response;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.backend.datasource.PhysicalDBPool;
import com.actiontech.dble.backend.datasource.PhysicalDatasource;
import com.actiontech.dble.backend.heartbeat.DBHeartbeat;
import com.actiontech.dble.backend.mysql.PacketUtil;
import com.actiontech.dble.config.Fields;
import com.actiontech.dble.config.ServerConfig;
import com.actiontech.dble.manager.ManagerConnection;
import com.actiontech.dble.net.mysql.EOFPacket;
import com.actiontech.dble.net.mysql.FieldPacket;
import com.actiontech.dble.net.mysql.ResultSetHeaderPacket;
import com.actiontech.dble.net.mysql.RowDataPacket;
import com.actiontech.dble.statistic.DataSourceSyncRecorder;
import com.actiontech.dble.util.LongUtil;
import com.actiontech.dble.util.StringUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author songwie
 */
public final class ShowDatasourceSyn {
    private ShowDatasourceSyn() {
    }

    private static final int FIELD_COUNT = 12;
    private static final ResultSetHeaderPacket HEADER = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] FIELDS = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket EOF = new EOFPacket();


    static {
        int i = 0;
        byte packetId = 0;
        HEADER.setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("MASTER_HOST", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("MASTER_PORT", Fields.FIELD_TYPE_LONG);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("MASTER_USER", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("SECONDS_BEHIND_MASTER", Fields.FIELD_TYPE_LONG);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("SLAVE_IO_RUNNING", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("SLAVE_SQL_RUNNING", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("SLAVE_IO_STATE", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("CONNECT_RETRY", Fields.FIELD_TYPE_LONG);
        FIELDS[i++].setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("LAST_IO_ERROR", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i++].setPacketId(++packetId);

        EOF.setPacketId(++packetId);
    }

    public static void response(ManagerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = HEADER.write(buffer, c, true);

        // write fields
        for (FieldPacket field : FIELDS) {
            buffer = field.write(buffer, c, true);
        }

        // write eof
        buffer = EOF.write(buffer, c, true);

        // write rows
        byte packetId = EOF.getPacketId();

        for (RowDataPacket row : getRows(c.getCharset())) {
            row.setPacketId(++packetId);
            buffer = row.write(buffer, c, true);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.setPacketId(++packetId);
        buffer = lastEof.write(buffer, c, true);

        // post write
        c.write(buffer);
    }

    private static List<RowDataPacket> getRows(String charset) {
        List<RowDataPacket> list = new LinkedList<>();
        ServerConfig conf = DbleServer.getInstance().getConfig();
        // host nodes
        Map<String, PhysicalDBPool> dataHosts = conf.getDataHosts();
        for (PhysicalDBPool pool : dataHosts.values()) {
            for (PhysicalDatasource ds : pool.getAllDataSources()) {
                DBHeartbeat hb = ds.getHeartbeat();
                DataSourceSyncRecorder record = hb.getAsynRecorder();
                Map<String, String> states = record.getRecords();
                RowDataPacket row = new RowDataPacket(FIELD_COUNT);
                if (!states.isEmpty()) {
                    row.add(StringUtil.encode(ds.getName(), charset));
                    row.add(StringUtil.encode(ds.getConfig().getIp(), charset));
                    row.add(LongUtil.toBytes(ds.getConfig().getPort()));
                    row.add(StringUtil.encode(states.get("Master_Host"), charset));
                    row.add(LongUtil.toBytes(Long.parseLong(states.get("Master_Port"))));
                    row.add(StringUtil.encode(states.get("Master_User"), charset));
                    String secords = states.get("Seconds_Behind_Master");
                    row.add(secords == null ? null : LongUtil.toBytes(Long.parseLong(secords)));
                    row.add(StringUtil.encode(states.get("Slave_IO_Running"), charset));
                    row.add(StringUtil.encode(states.get("Slave_SQL_Running"), charset));
                    row.add(StringUtil.encode(states.get("Slave_IO_State"), charset));
                    row.add(LongUtil.toBytes(Long.parseLong(states.get("Connect_Retry"))));
                    row.add(StringUtil.encode(states.get("Last_IO_Error"), charset));

                    list.add(row);
                }
            }
        }
        return list;
    }

}
