package com.actiontech.dble.plan.common.item.function.timefunc;

import com.actiontech.dble.plan.common.item.Item;
import com.actiontech.dble.plan.common.item.function.ItemFunc;
import com.actiontech.dble.plan.common.item.function.primary.ItemIntFunc;
import com.actiontech.dble.plan.common.time.MySQLTime;
import com.actiontech.dble.plan.common.time.MyTime;

import java.math.BigInteger;
import java.util.List;

public class ItemFuncToSeconds extends ItemIntFunc {

    public ItemFuncToSeconds(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "to_seconds";
    }

    @Override
    public BigInteger valInt() {
        MySQLTime ltime = new MySQLTime();
        if (getArg0Date(ltime, MyTime.TIME_NO_ZERO_DATE))
            return BigInteger.ZERO;
        long seconds = ltime.getHour() * 3600L + ltime.getMinute() * 60 + ltime.getSecond();
        seconds = ltime.isNeg() ? -seconds : seconds;
        long days = MyTime.calcDaynr(ltime.getYear(), ltime.getMonth(), ltime.getDay());
        return BigInteger.valueOf(seconds + days * 24L * 3600L);
    }

    @Override
    public void fixLengthAndDec() {
        maxLength = 6;
        maybeNull = true;
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncToSeconds(realArgs);
    }

}
