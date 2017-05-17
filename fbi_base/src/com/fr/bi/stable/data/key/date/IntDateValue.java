package com.fr.bi.stable.data.key.date;

import com.fr.bi.base.BICore;
import com.fr.bi.base.BICoreGenerator;
import com.fr.bi.base.annotation.BICoreField;

/**
 * Created by 小灰灰 on 2016/1/6.
 */
public abstract class IntDateValue implements BIDateValue <Integer>{
    private static final long serialVersionUID = 1132369972153619547L;
    @BICoreField
    protected int value;
    public IntDateValue(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public BICore fetchObjectCore() {
        return new BICoreGenerator(this).fetchObjectCore();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntDateValue that = (IntDateValue) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}