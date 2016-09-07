/**
 * 
 */
package com.fr.bi.sql.analysis.report.widget.field.filtervalue.number;

/**
 * @author Daniel
 *
 */
public class NumberSmallOrEqualsCLFilter extends NumberCalculateLineFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3026020133625295929L;

	/**
	 * @param t
	 */
    public NumberSmallOrEqualsCLFilter() {
		super(SmallOrEquals.INSTANCE);
	}
    protected  void parsClose(boolean isClose){
        t = isClose ? SmallOrEquals.INSTANCE : Small.INSTANCE;
    }

}