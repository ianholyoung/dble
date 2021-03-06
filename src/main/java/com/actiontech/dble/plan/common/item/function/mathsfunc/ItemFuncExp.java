/**
 *
 */
package com.actiontech.dble.plan.common.item.function.mathsfunc;

import com.actiontech.dble.plan.common.item.Item;
import com.actiontech.dble.plan.common.item.function.ItemFunc;
import com.actiontech.dble.plan.common.item.function.primary.ItemDecFunc;

import java.math.BigDecimal;
import java.util.List;

public class ItemFuncExp extends ItemDecFunc {

    /**
     * @param name
     * @param args
     */
    public ItemFuncExp(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "exp";
    }

    @Override
    public BigDecimal valReal() {
        BigDecimal value = args.get(0).valReal();
        if (nullValue = args.get(0).isNullValue()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(Math.exp(value.doubleValue()));
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncExp(realArgs);
    }
}
