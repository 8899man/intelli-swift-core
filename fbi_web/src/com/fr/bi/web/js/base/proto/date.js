// full day names
Date._DN = [BI.i18nText("BI-Sunday"),
    BI.i18nText("BI-Monday"),
    BI.i18nText("BI-Tuesday"),
    BI.i18nText("BI-Wednesday"),
    BI.i18nText("BI-Thursday"),
    BI.i18nText("BI-Friday"),
    BI.i18nText("BI-Saturday"),
    BI.i18nText("BI-Sunday")];

// short day names
Date._SDN = [FR.i18n.Sun,
    FR.i18n.Mon,
    FR.i18n.Tue,
    FR.i18n.Wed,
    FR.i18n.Thu,
    FR.i18n.Fri,
    FR.i18n.Sat,
    FR.i18n.Sun];

// Monday first, etc.
Date._FD = 1;

// full month names
Date._MN = [
    FR.i18n.January,
    FR.i18n.February,
    FR.i18n.March,
    FR.i18n.April,
    FR.i18n.May,
    FR.i18n.June,
    FR.i18n.July,
    FR.i18n.August,
    FR.i18n.September,
    FR.i18n.October,
    FR.i18n.November,
    FR.i18n.December];

// short month names
Date._SMN = [FR.i18n.Jan,
    FR.i18n.Feb,
    FR.i18n.Mar,
    FR.i18n.Apr,
    FR.i18n.Short_May,
    FR.i18n.Jun,
    FR.i18n.Jul,
    FR.i18n.Aug,
    FR.i18n.Sep,
    FR.i18n.Oct,
    FR.i18n.Nov,
    FR.i18n.Dec];

Date._QN = ["", BI.i18nText("BI-Quarter_1"),
    BI.i18nText("BI-Quarter_2"),
    BI.i18nText("BI-Quarter_3"),
    BI.i18nText("BI-Quarter_4")];

// tooltips
Date._TT = {};

Date._TT["PREV_YEAR"] = FR.i18n.Prev_Year;
Date._TT["PREV_MONTH"] = FR.i18n.Prev_Month;
Date._TT["GO_TODAY"] = "至今天";
Date._TT["Date"] = "日历";
Date._TT["NEXT_MONTH"] = FR.i18n.Next_Month;
Date._TT["NEXT_YEAR"] = FR.i18n.Next_Year;
Date._TT["SEL_DATE"] = FR.i18n.Select_Date;
Date._TT["DRAG_TO_MOVE"] = FR.i18n.Drag_To_Move;
Date._TT["PART_TODAY"] = FR.i18n.Today;
Date._TT["OK"] = FR.i18n.OK;
Date._TT["Clear"] = FR.i18n.Clear;
Date._TT["Cancel"] = FR.i18n.Cancel;


// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Date._TT["DAY_FIRST"] = "%s " + FR.i18n.First_Of_Week;

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Date._TT["WEEKEND"] = "0,6";

Date._TT["CLOSE"] = FR.i18n.Close;
Date._TT["TODAY"] = FR.i18n.Today;
Date._TT["TIME_PART"] = "(Shift-)" + FR.i18n.Click_To_Change_Value;

// date formats
Date._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Date._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Date._TT["WK"] = FR.i18n.Week;
Date._TT["TIME"] = FR.i18n.Time + ":";


/** Adds the number of days array to the Date object. */
Date._MD = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

/** Constants used for time computations */
Date.SECOND = 1000 /* milliseconds */;
Date.MINUTE = 60 * Date.SECOND;
Date.HOUR = 60 * Date.MINUTE;
Date.DAY = 24 * Date.HOUR;
Date.WEEK = 7 * Date.DAY;

/** Returns the number of days in the current month */
Date.prototype.getMonthDays = function (month) {
    var year = this.getFullYear();
    if (typeof month == "undefined") {
        month = this.getMonth();
    }
    if (((0 == (year % 4)) && ( (0 != (year % 100)) || (0 == (year % 400)))) && month == 1) {
        return 29;
    } else {
        return Date._MD[month];
    }
};

/** Returns the number of day in the year. */
Date.prototype.getDayOfYear = function () {
    var now = new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0, 0, 0);
    var then = new Date(this.getFullYear(), 0, 0, 0, 0, 0);
    var time = now - then;
    return Math.floor(time / Date.DAY);
};

/** Returns the number of the week in year, as defined in ISO 8601. */
Date.prototype.getWeekNumber = function () {
    var d = new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0, 0, 0);
    var DoW = d.getDay();
    d.setDate(d.getDate() - (DoW + 6) % 7 + 3); // Nearest Thu
    var ms = d.valueOf(); // GMT
    d.setMonth(0);
    d.setDate(4); // Thu in Week 1
    return Math.round((ms - d.valueOf()) / (7 * 864e5)) + 1;
};

//离当前时间多少天的时间
Date.prototype.getOffsetDate = function (offset) {
    return new Date(this.getTime() + offset * 864e5);
};

//获得n个季度后的日期
Date.prototype.getAfterMulQuarter = function (n) {
    var dt = new Date(this.getTime());
    dt.setMonth(dt.getMonth() + n * 3);
    return dt;
};
//获得n个季度前的日期
Date.prototype.getBeforeMulQuarter = function (n) {
    var dt = new Date(this.getTime());
    dt.setMonth(dt.getMonth() - n * 3);
    return dt;
};
//得到本季度的起始月份
Date.prototype.getQuarterStartMonth = function () {
    var quarterStartMonth = 0;
    var nowMonth = this.getMonth();
    if (nowMonth < 3) {
        quarterStartMonth = 0;
    }
    if (2 < nowMonth && nowMonth < 6) {
        quarterStartMonth = 3;
    }
    if (5 < nowMonth && nowMonth < 9) {
        quarterStartMonth = 6;
    }
    if (nowMonth > 8) {
        quarterStartMonth = 9;
    }
    return quarterStartMonth;
};
//获得本季度的起始日期
Date.prototype.getQuarterStartDate = function () {
    return new Date(this.getFullYear(), this.getQuarterStartMonth(), 1);
};
//得到本季度的结束日期
Date.prototype.getQuarterEndDate = function () {
    var quarterEndMonth = this.getQuarterStartMonth() + 2;
    return new Date(this.getFullYear(), quarterEndMonth, this.getMonthDays(quarterEndMonth));
};
Date.prototype.getAfterMultiMonth = function (n) {
    var dt = new Date(this.getTime());
    dt.setMonth(dt.getMonth() + n | 0);
    return dt;
};
Date.prototype.getBeforeMultiMonth = function (n) {
    var dt = new Date(this.getTime());
    dt.setMonth(dt.getMonth() - n | 0);
    return dt;
};

/** Checks date and time equality */
Date.prototype.equalsTo = function (date) {
    return ((this.getFullYear() == date.getFullYear()) &&
    (this.getMonth() == date.getMonth()) &&
    (this.getDate() == date.getDate()) &&
    (this.getHours() == date.getHours()) &&
    (this.getMinutes() == date.getMinutes()) &&
    (this.getSeconds() == date.getSeconds()));
};

//指定日期n个月之前或之后的日期
Date.prototype.getOffsetMonth = function (date, n) {
    var day = date.getDate();
    var monthDay = new Date(date.getFullYear(), date.getMonth() + parseInt(n), 1).getMonthDays();
    if (day > monthDay) {
        day = monthDay;
    }
    date.setDate(day);
    date.setMonth(date.getMonth() + parseInt(n));
    return date;
};

//指定日期n个季度之前或之后的日期
Date.prototype.getOffsetQuarter = function (date, n) {
    var dt = new Date(date);
    dt.setMonth(dt.getMonth() + n * 3);
    return dt;
};

/** Prints the date in a string according to the given format. */
Date.prototype.print = function (str) {
    var m = this.getMonth();
    var d = this.getDate();
    var y = this.getFullYear();
    var wn = this.getWeekNumber();
    var w = this.getDay();
    var s = {};
    var hr = this.getHours();
    var pm = (hr >= 12);
    var ir = (pm) ? (hr - 12) : hr;
    var dy = this.getDayOfYear();
    if (ir == 0) {
        ir = 12;
    }
    var min = this.getMinutes();
    var sec = this.getSeconds();
    s["%a"] = Date._SDN[w]; // abbreviated weekday name [FIXME: I18N]
    s["%A"] = Date._DN[w]; // full weekday name
    s["%b"] = Date._SMN[m]; // abbreviated month name [FIXME: I18N]
    s["%B"] = Date._MN[m]; // full month name
    // FIXME: %c : preferred date and time representation for the current locale
    s["%C"] = 1 + Math.floor(y / 100); // the century number
    s["%d"] = (d < 10) ? ("0" + d) : d; // the day of the month (range 01 to 31)
    s["%e"] = d; // the day of the month (range 1 to 31)
    // FIXME: %D : american date style: %m/%d/%y
    // FIXME: %E, %F, %G, %g, %h (man strftime)
    s["%H"] = (hr < 10) ? ("0" + hr) : hr; // hour, range 00 to 23 (24h format)
    s["%I"] = (ir < 10) ? ("0" + ir) : ir; // hour, range 01 to 12 (12h format)
    s["%j"] = (dy < 100) ? ((dy < 10) ? ("00" + dy) : ("0" + dy)) : dy; // day of the year (range 001 to 366)
    s["%k"] = hr;		// hour, range 0 to 23 (24h format)
    s["%l"] = ir;		// hour, range 1 to 12 (12h format)
    s["%X"] = (m < 9) ? ("0" + (1 + m)) : (1 + m); // month, range 01 to 12
    s["%x"] = m + 1 // month, range 1 to 12
    s["%M"] = (min < 10) ? ("0" + min) : min; // minute, range 00 to 59
    s["%n"] = "\n";		// a newline character
    s["%p"] = pm ? "PM" : "AM";
    s["%P"] = pm ? "pm" : "am";
    // FIXME: %r : the time in am/pm notation %I:%M:%S %p
    // FIXME: %R : the time in 24-hour notation %H:%M
    s["%s"] = Math.floor(this.getTime() / 1000);
    s["%S"] = (sec < 10) ? ("0" + sec) : sec; // seconds, range 00 to 59
    s["%t"] = "\t";		// a tab character
    // FIXME: %T : the time in 24-hour notation (%H:%M:%S)
    s["%U"] = s["%W"] = s["%V"] = (wn < 10) ? ("0" + wn) : wn;
    s["%u"] = w + 1;	// the day of the week (range 1 to 7, 1 = MON)
    s["%w"] = w;		// the day of the week (range 0 to 6, 0 = SUN)
    // FIXME: %x : preferred date representation for the current locale without the time
    // FIXME: %X : preferred time representation for the current locale without the date
    s["%y"] = ('' + y).substr(2, 2); // year without the century (range 00 to 99)
    s["%Y"] = y;		// year with the century
    s["%%"] = "%";		// a literal '%' character

    var re = /%./g;
    if (!BI.isKhtml()) {
        return str.replace(re, function (par) {
            return s[par] || par;
        });
    }

    var a = str.match(re);
    for (var i = 0; i < a.length; i++) {
        var tmp = s[a[i]];
        if (tmp) {
            re = new RegExp(a[i], 'g');
            str = str.replace(re, tmp);
        }
    }

    return str;
};

/**
 * 是否是闰年
 * @param year
 * @returns {boolean}
 */
Date.isLeap = function (year) {
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
};

/**
 * 检测是否在有效期
 *
 * @param YY 年
 * @param MM 月
 * @param DD 日
 * @param minDate '1900-01-01'
 * @param maxDate '2099-12-31'
 * @returns {Array} 若无效返回无效状态
 */
Date.checkVoid = function (YY, MM, DD, minDate, maxDate) {
    var back = [];
    YY = YY | 0;
    MM = MM | 0;
    DD = DD | 0;
    minDate = BI.isString(minDate) ? minDate.match(/\d+/g) : minDate;
    maxDate = BI.isString(maxDate) ? maxDate.match(/\d+/g) : maxDate;
    if (YY < minDate[0]) {
        back = ['y'];
    } else if (YY > maxDate[0]) {
        back = ['y', 1];
    } else if (YY >= minDate[0] && YY <= maxDate[0]) {
        if (YY == minDate[0]) {
            if (MM < minDate[1]) {
                back = ['m'];
            } else if (MM == minDate[1]) {
                if (DD < minDate[2]) {
                    back = ['d'];
                }
            }
        }
        if (YY == maxDate[0]) {
            if (MM > maxDate[1]) {
                back = ['m', 1];
            } else if (MM == maxDate[1]) {
                if (DD > maxDate[2]) {
                    back = ['d', 1];
                }
            }
        }
    }
    return back;
};

Date.checkLegal = function (str) {
    var ar = str.match(/\d+/g);
    var YY = ar[0] | 0, MM = ar[1] | 0, DD = ar[2] | 0;
    if (ar.length <= 1) {
        return true;
    }
    if (ar.length <= 2) {
        return MM >= 1 && MM <= 12;
    }
    var MD = BI.clone(Date._MD);
    MD[1] = Date.isLeap(YY) ? 29 : 28;
    return MM >= 1 && MM <= 12 && DD <= MD[MM - 1];
};

Date.parseDateTime = function (str, fmt) {
    var today = new Date();
    var y = 0;
    var m = 0;
    var d = 1;
    //wei : 对于fmt为‘YYYYMM’或者‘YYYYMMdd’的格式，str的值为类似'201111'的形式，因为年月之间没有分隔符，所以正则表达式分割无效，导致bug7376。
    var a = str.split(/\W+/);
    if (fmt.toLowerCase() == '%y%x' || fmt.toLowerCase() == '%y%x%d') {
        var yearlength = 4;
        var otherlength = 2;
        a[0] = str.substring(0, yearlength);
        a[1] = str.substring(yearlength, yearlength + otherlength);
        a[2] = str.substring(yearlength + otherlength, yearlength + otherlength * 2);
    }
    var b = fmt.match(/%./g);
    var i = 0, j = 0;
    var hr = 0;
    var min = 0;
    var sec = 0;
    for (i = 0; i < a.length; ++i) {
        switch (b[i]) {
            case "%d":
            case "%e":
                d = parseInt(a[i], 10);
                break;

            case "%X":
                m = parseInt(a[i], 10) - 1;
                break;
            case "%x":
                m = parseInt(a[i], 10) - 1;
                break;

            case "%Y":
            case "%y":
                y = parseInt(a[i], 10);
                (y < 100) && (y += (y > 29) ? 1900 : 2000);
                break;

            case "%b":
            case "%B":
                for (j = 0; j < 12; ++j) {
                    if (Date._MN[j].substr(0, a[i].length).toLowerCase() == a[i].toLowerCase()) {
                        m = j;
                        break;
                    }
                }
                break;

            case "%H":
            case "%I":
            case "%k":
            case "%l":
                hr = parseInt(a[i], 10);
                break;

            case "%P":
            case "%p":
                if (/pm/i.test(a[i]) && hr < 12) {
                    hr += 12;
                } else if (/am/i.test(a[i]) && hr >= 12) {
                    hr -= 12;
                }
                break;

            case "%M":
                min = parseInt(a[i], 10);
            case "%S":
                sec = parseInt(a[i], 10);
                break;
        }
    }
//    if (!a[i]) {
//        continue;
//	}
    if (isNaN(y)) {
        y = today.getFullYear();
    }
    if (isNaN(m)) {
        m = today.getMonth();
    }
    if (isNaN(d)) {
        d = today.getDate();
    }
    if (isNaN(hr)) {
        hr = today.getHours();
    }
    if (isNaN(min)) {
        min = today.getMinutes();
    }
    if (isNaN(sec)) {
        sec = today.getSeconds();
    }
    if (y != 0) {
        return new Date(y, m, d, hr, min, sec);
    }
    y = 0;
    m = -1;
    d = 0;
    for (i = 0; i < a.length; ++i) {
        if (a[i].search(/[a-zA-Z]+/) != -1) {
            var t = -1;
            for (j = 0; j < 12; ++j) {
                if (Date._MN[j].substr(0, a[i].length).toLowerCase() == a[i].toLowerCase()) {
                    t = j;
                    break;
                }
            }
            if (t != -1) {
                if (m != -1) {
                    d = m + 1;
                }
                m = t;
            }
        } else if (parseInt(a[i], 10) <= 12 && m == -1) {
            m = a[i] - 1;
        } else if (parseInt(a[i], 10) > 31 && y == 0) {
            y = parseInt(a[i], 10);
            (y < 100) && (y += (y > 29) ? 1900 : 2000);
        } else if (d == 0) {
            d = a[i];
        }
    }
    if (y == 0) {
        y = today.getFullYear();
    }
    if (m != -1 && d != 0) {
        return new Date(y, m, d, hr, min, sec);
    }
    return today;
};