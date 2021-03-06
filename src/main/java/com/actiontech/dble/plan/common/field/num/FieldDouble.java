package com.actiontech.dble.plan.common.field.num;

import com.actiontech.dble.plan.common.item.FieldTypes;

/**
 * bigint(%d) |unsigned |zerofilled
 *
 * @author ActionTech
 */
public class FieldDouble extends FieldReal {

    public FieldDouble(String name, String table, int charsetIndex, int fieldLength, int decimals, long flags) {
        super(name, table, charsetIndex, fieldLength, decimals, flags);
    }

    @Override
    public FieldTypes fieldType() {
        return FieldTypes.MYSQL_TYPE_DOUBLE;
    }

}
