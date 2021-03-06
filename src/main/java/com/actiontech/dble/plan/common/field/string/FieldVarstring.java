package com.actiontech.dble.plan.common.field.string;

import com.actiontech.dble.plan.common.item.FieldTypes;

public class FieldVarstring extends FieldString {
    public FieldVarstring(String name, String table, int charsetIndex, int fieldLength, int decimals, long flags) {
        super(name, table, charsetIndex, fieldLength, decimals, flags);
    }

    @Override
    public FieldTypes fieldType() {
        return FieldTypes.MYSQL_TYPE_VAR_STRING;
    }
}
