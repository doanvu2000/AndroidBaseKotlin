package com.example.baseproject.base.utils.lunar.calendar;

import static java.lang.Math.PI;

import android.annotation.SuppressLint;

public class LunisolarCalendar {
    private static final int FIRSTYEAR = 1900;
    private static final int LASTYEAR = 2100;
    private static final int[] SolarCal = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] SolarDays = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365, 396, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366, 397};
    private String date_name;
    private int day;
    private int daySun;
    private int month;
    private int monthSun;
    private String month_name;
    private int year;
    private int yearSun;
    private String year_name;
    private int isLeap;

    public LunisolarCalendar(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public LunisolarCalendar() {

    }

    public LunisolarCalendar(int year, int month, int day, int yearSun, int monthSun, int daySun, int isLeap) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.yearSun = yearSun;
        this.monthSun = monthSun;
        this.daySun = daySun;
        this.isLeap = isLeap;
    }

    public LunisolarCalendar(int year, int month, int day, int yearSun, int monthSun, int daySun) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.yearSun = yearSun;
        this.monthSun = monthSun;
        this.daySun = daySun;
        this.isLeap = 0;
    }

    public static int getCanForToday(int day, int month, int year) {
        return INT((UniversalToJD(day, month, year) + 9.5d) % 10);
    }

    public static int getChiForToday(int day, int month, int year) {
        return INT((UniversalToJD(day, month, year) + 1.5d) % 12);
    }

    public static String getCanForDay(int day, int month, int year) {
        int canId = getCanForToday(day, month, year);
        String can = "";
        switch (canId) {
            case 0:
                can = "Giáp";
                break;
            case 1:
                can = "Ất";
                break;
            case 2:
                can = "Bính";
                break;
            case 3:
                can = "Đinh";
                break;
            case 4:
                can = "Mậu";
                break;
            case 5:
                can = "Kỷ";
                break;
            case 6:
                can = "Canh";
                break;
            case 7:
                can = "Tân";
                break;
            case 8:
                can = "Nhâm";
                break;
            case 9:
                can = "Quý";
                break;
        }
        return can;
    }

    public static String getChiForDay(int day, int month, int year) {
        String chi = "";
        switch (getChiForToday(day, month, year)) {
            case 0:
                chi = "Tý";
                break;
            case 1:
                chi = "Sửu";
                break;
            case 2:
                chi = "Dần";
                break;
            case 3:
                chi = "Mão";
                break;
            case 4:
                chi = "Thìn";
                break;
            case 5:
                chi = "Tỵ";
                break;
            case 6:
                chi = "Ngọ";
                break;
            case 7:
                chi = "Mùi";
                break;
            case 8:
                chi = "Thân";
                break;
            case 9:
                chi = "Dậu";
                break;
            case 10:
                chi = "Tuất";
                break;
            case 11:
                chi = "Hợi";
                break;
        }
        return chi;
    }

    public static String getDateLunarName(int mDay, int mMonth, int mYear) {
        String can = "";
        String chi = "";
        switch (getCanForToday(mDay, mMonth, mYear)) {
            case 0:
                can = "Giáp";
                break;
            case 1:
                can = "Ất";
                break;
            case 2:
                can = "Bính";
                break;
            case 3:
                can = "Đinh";
                break;
            case 4:
                can = "Mậu";
                break;
            case 5:
                can = "Kỷ";
                break;
            case 6:
                can = "Canh";
                break;
            case 7:
                can = "Tân";
                break;
            case 8:
                can = "Nhâm";
                break;
            case 9:
                can = "Quý";
                break;
        }
        switch (INT(UniversalToJD(mDay, mMonth, mYear) + 1.5d) % 12) {
            case 0:
                chi = "Tý";
                break;
            case 1:
                chi = "Sửu";
                break;
            case 2:
                chi = "Dần";
                break;
            case 3:
                chi = "Mão";
                break;
            case 4:
                chi = "Thìn";
                break;
            case 5:
                chi = "Tỵ";
                break;
            case 6:
                chi = "Ngọ";
                break;
            case 7:
                chi = "Mùi";
                break;
            case 8:
                chi = "Thân";
                break;
            case 9:
                chi = "Dậu";
                break;
            case 10:
                chi = "Tuất";
                break;
            case 11:
                chi = "Hợi";
                break;
        }
        return can + " " + chi;
    }

    public static String getConflictAgeForToday(int mCan, int mChi) {
        String can = "";
        String chi = "";
        switch (mCan) {
            case 0:
                can = "Giáp";
                break;
            case 1:
                can = "Ất";
                break;
            case 2:
                can = "Bính";
                break;
            case 3:
                can = "Đinh";
                break;
            case 4:
                can = "Mậu";
                break;
            case 5:
                can = "Kỷ";
                break;
            case 6:
                can = "Canh";
                break;
            case 7:
                can = "Tân";
                break;
            case 8:
                can = "Nhâm";
                break;
            case 9:
                can = "Quý";
                break;
        }
        switch (mChi) {
            case 0:
                chi = "Tý";
                break;
            case 1:
                chi = "Sửu";
                break;
            case 2:
                chi = "Dần";
                break;
            case 3:
                chi = "Mão";
                break;
            case 4:
                chi = "Thìn";
                break;
            case 5:
                chi = "Tỵ";
                break;
            case 6:
                chi = "Ngọ";
                break;
            case 7:
                chi = "Mùi";
                break;
            case 8:
                chi = "Thân";
                break;
            case 9:
                chi = "Dậu";
                break;
            case 10:
                chi = "Tuất";
                break;
            case 11:
                chi = "Hợi";
                break;
        }
        return can + " " + chi;
    }

    public static String getMonthLunarNameV2(int mMonth, int mYear) {
        String can = "";
        String chi = "";
        can = switch ((((mYear * 12) + mMonth) + 3) % 10) {
            case 0 -> "Giáp";
            case 1 -> "Ất";
            case 2 -> "Bính";
            case 3 -> "Đinh";
            case 4 -> "Mậu";
            case 5 -> "Kỷ";
            case 6 -> "Canh";
            case 7 -> "Tân";
            case 8 -> "Nhâm";
            case 9 -> "Quý";
            default -> can;
        };
        chi = switch (mMonth) {
            case 1 -> "Dần";
            case 2 -> "Mão";
            case 3 -> "Thìn";
            case 4 -> "Tỵ";
            case 5 -> "Ngọ";
            case 6 -> "Mùi";
            case 7 -> "Thân";
            case 8 -> "Dậu";
            case 9 -> "Tuất";
            case 10 -> "Hợi";
            case 11 -> "Tý";
            case 12 -> "Sửu";
            default -> chi;
        };
        return can + " " + chi;
    }

    public static String getMonthLunarNameCan(int mMonth, int mYear) {
        String can = "";
        can = switch ((((mYear * 12) + mMonth) + 3) % 10) {
            case 0 -> "Giáp";
            case 1 -> "Ất";
            case 2 -> "Bính";
            case 3 -> "Đinh";
            case 4 -> "Mậu";
            case 5 -> "Kỷ";
            case 6 -> "Canh";
            case 7 -> "Tân";
            case 8 -> "Nhâm";
            case 9 -> "Quý";
            default -> can;
        };
        return can;
    }

    public static String getMonthLunarNameChi(int mMonth) {
        String chi = "";
        chi = switch (mMonth) {
            case 1 -> "Dần";
            case 2 -> "Mão";
            case 3 -> "Thìn";
            case 4 -> "Tỵ";
            case 5 -> "Ngọ";
            case 6 -> "Mùi";
            case 7 -> "Thân";
            case 8 -> "Dậu";
            case 9 -> "Tuất";
            case 10 -> "Hợi";
            case 11 -> "Tý";
            case 12 -> "Sửu";
            default -> chi;
        };
        return chi;
    }

    public static String getMonthLunarName(int mMonth, int mYear) {
        String can = "";
        String chi = "";
        switch ((((mYear * 12) + mMonth) + 3) % 10) {
            case 0:
                can = "Giáp";
                break;
            case 1:
                can = "Ất";
                break;
            case 2:
                can = "Bính";
                break;
            case 3:
                can = "Đinh";
                break;
            case 4:
                can = "Mậu";
                break;
            case 5:
                can = "Kỷ";
                break;
            case 6:
                can = "Canh";
                break;
            case 7:
                can = "Tân";
                break;
            case 8:
                can = "Nhâm";
                break;
            case 9:
                can = "Quý";
                break;
        }
        switch (mMonth) {
            case 1:
                chi = "Dần";
                break;
            case 2:
                chi = "Mão";
                break;
            case 3:
                chi = "Thìn";
                break;
            case 4:
                chi = "Tỵ";
                break;
            case 5:
                chi = "Ngọ";
                break;
            case 6:
                chi = "Mùi";
                break;
            case 7:
                chi = "Thân";
                break;
            case 8:
                chi = "Dậu";
                break;
            case 9:
                chi = "Tuất";
                break;
            case 10:
                chi = "Hợi";
                break;
            case 11:
                chi = "Tý";
                break;
            case 12:
                chi = "Sửu";
                break;
        }
        return "T. " + can + " " + chi;
    }

    public static String getYearName(int year) {
        String can = "";
        String chi = "";
        switch (year % 10) {
            case 0:
                can = "Canh";
                break;
            case 1:
                can = "Tân";
                break;
            case 2:
                can = "Nhâm";
                break;
            case 3:
                can = "Quý";
                break;
            case 4:
                can = "Giáp";
                break;
            case 5:
                can = "Ất";
                break;
            case 6:
                can = "Bính";
                break;
            case 7:
                can = "Đinh";
                break;
            case 8:
                can = "Mậu";
                break;
            case 9:
                can = "Kỷ";
                break;
        }
        int div3 = year % 3;
        int div4 = year % 4;
        if (div3 == 1 && div4 == 0) {
            chi = "Tý";
        }
        if (div3 == 1 && div4 == 1) {
            chi = "Dậu";
        }
        if (div3 == 1 && div4 == 2) {
            chi = "Ngọ";
        }
        if (div3 == 1 && div4 == 3) {
            chi = "Mão";
        }
        if (div3 == 2 && div4 == 0) {
            chi = "Thìn";
        }
        if (div3 == 2 && div4 == 1) {
            chi = "Sửu";
        }
        if (div3 == 2 && div4 == 2) {
            chi = "Tuất";
        }
        if (div3 == 2 && div4 == 3) {
            chi = "Mùi";
        }
        if (div3 == 0 && div4 == 0) {
            chi = "Thân";
        }
        if (div3 == 0 && div4 == 1) {
            chi = "Tị";
        }
        if (div3 == 0 && div4 == 2) {
            chi = "Dần";
        }
        if (div3 == 0 && div4 == 3) {
            chi = "Hợi";
        }
        return can + " " + chi;
    }

    private static int GetLeap(int year) {
        if (year % 400 == 0) {
            return 1;
        }
        if (year % 100 == 0) {
            return 0;
        }
        if (year % 4 != 0) {
            return 0;
        }
        return 1;
    }

    public static double UniversalToJD(int D, int M, int Y) {
        if (Y > 1582 || ((Y == 1582 && M > 10) || (Y == 1582 && M == 10 && D > 14))) {
            return ((double) (((((Y * 367) - INT((double) (((INT((double) ((M + 9) / 12)) + Y) * 7) / 4))) -
                    INT((double) (((INT((double) ((((M - 9) / 7) + Y) / 100)) + 1) * 3) / 4)))
                    + INT((double) ((M * 275) / 9))) + D)) + 1721028.5d;
        }
        return ((double) ((((Y * 367) - INT((double) ((((Y + 5001) + INT((double) ((M - 9) / 7))) * 7) / 4))) + INT((double) ((M * 275) / 9))) + D)) + 1729776.5d;
    }

    private static int INT(double d) {
        return (int) Math.floor(d);
    }

    public static String getTimeName(float h) {
        if (h > 1.0f && h <= 3.0f) {
            return "Sửu";
        }
        if (h > 3.0f && h <= 5.0f) {
            return "Dần";
        }
        if (h > 5.0f && h <= 7.0f) {
            return "Mão";
        }
        if (h > 7.0f && h <= 9.0f) {
            return "Thìn";
        }
        if (h > 9.0f && h <= 11.0f) {
            return "Tị";
        }
        if (h > 11.0f && h <= 13.0f) {
            return "Ngọ";
        }
        if (h > 13.0f && h <= 15.0f) {
            return "Mùi";
        }
        if (h > 15.0f && h <= 17.0f) {
            return "Thân";
        }
        if (h > 17.0f && h <= 19.0f) {
            return "Dậu";
        }
        if (h > 19.0f && h <= 21.0f) {
            return "Tuất";
        }
        if (h > 21.0f && h <= 23.0f) {
            return "Hợi";
        }
        return "Tý";
    }

    public static String getTimeForZodiac(float h) {
        if (h == 0) {
            return "23h- 1h";
        } else if (h == 1) {
            return "1h- 3h";
        } else if (h == 2) {
            return "3h- 5h";
        } else if (h == 3) {
            return "5h- 7h";
        } else if (h == 4) {
            return "7h- 9h";
        } else if (h == 5) {
            return "9h- 11h";
        } else if (h == 6) {
            return "11h- 13h";
        } else if (h == 7) {
            return "13h- 15h";
        } else if (h == 8) {
            return "15h- 17h";
        } else if (h == 9) {
            return "17h- 19h";
        } else if (h == 10) {
            return "19h- 21h";
        } else if (h == 11) {
            return "21h- 23h";
        } else {
            return "23h- 1h";
        }
    }

    private static long jdFromDate(int intNgay, int intThang, int intNam) {
        int a, y, m;
        long jd;
        a = INT(((14 - intThang) / 12));
        y = intNam + 4800 - a;
        m = intThang + 12 * a - 3;
        jd = intNgay + INT((153 * m + 2) / 5) + 365L * y + INT(y / 4) - INT(y / 100) + INT(y / 400) - 32045;
        if (jd < 2299161) {
            jd = intNgay + INT((153 * m + 2) / 5) + 365L * y + INT(y / 4) - 32083;
        }
        return jd;
    }

    private static LunisolarCalendar jdToDate(long jd) {
        long a, b, c, d, e, m;
        int day, month, year;
        if (jd > 2299160) { // After 5/10/1582, Gregorian calendar
            a = jd + 32044;
            b = INT((4 * a + 3) / 146097);
            c = a - INT((b * 146097) / 4);
        } else {
            b = 0;
            c = jd + 32082;
        }
        d = INT((4 * c + 3) / 1461);
        e = c - INT((1461 * d) / 4);
        m = INT((5 * e + 2) / 153);
        day = (int) (e - INT((153 * m + 2) / 5) + 1);
        month = (int) (m + 3 - 12 * INT(m / 10));
        year = (int) (b * 100 + d - 4800 + INT(m / 10));
        return new LunisolarCalendar(year, month, day);
    }

    // Tinh ngay Soc
    private static int getNewMoonDay(double k) {
        // float PI = (float) 3.14;
        double T, T2, T3, dr, Jd1, M, Mpr, F, C1, deltat, JdNew;
        T = k / 1236.85;
        T2 = T * T;
        T3 = T2 * T;
        dr = PI / 180;
        double timeZone = 7.0;

        Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr); // Mean new moon
        M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3; // Sun's mean anomaly
        Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3; // Moon's mean anomaly
        F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3; // Moon's argument of latitude
        C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 = C1 - 0.0004 * Math.sin(dr * 3 * Mpr);
        C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
        C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
        C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
        C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
        if (T < -11) {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3;
        } else {
            deltat = -0.000278 + 0.000265 * T + 0.000262 * T2;
        }
        JdNew = Jd1 + C1 - deltat;
        return INT(JdNew + 0.5 + timeZone / 24);
    }

    //Tính toa do mat troi
    private static int getSunLongitude(double jdn) {
        double timeZone = 7.0;
        double T, T2, dr, M, L0, DL, L;
        // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        T = (jdn - 2451545.5 - timeZone / 24) / 36525;
        T2 = T * T;
        // degree to radian
        dr = PI / 180; // degree to radian
        M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2; // mean anomaly, degree
        L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2; // mean longitude, degree
        DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        L = L0 + DL; // true longitude, degree
        L = L * dr;
        L = L - PI * 2 * (INT(L / (PI * 2))); // Normalize to (0, 2*PI)
        return INT(L / PI * 6);
    }

//    public String getLunarName() {
//        return getYearName() + " " + getMonthName() + " " + getDateName();
//    }

    // Tìm ngày bat dau tháng 11 am lich
    private static int getLunarMonth11(int yy) {
        double k, off, sunLong;
        int nm;
        off = jdFromDate(31, 12, yy) - 2415021;
        k = INT(off / 29.530588853);
        nm = getNewMoonDay(k);
        sunLong = getSunLongitude(nm); // sun longitude at local midnight
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1);
        }
        return nm;
    }

    //Xác dinh thang nhuan
    private static int getLeapMonthOffset(double a11) {
        int last, arc;
        int k, i;
        k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        last = 0;
        i = 1; // We start with the month following lunar month 11
        arc = getSunLongitude(getNewMoonDay(k + i));
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i));
        } while (arc != last && i < 14);
        return i - 1;
    }

    public static int getLunarDay(int day, int month, int year) {

        int k, monthStart, lunarDay;
        long dayNumber;
        dayNumber = jdFromDate(day, month, year);
        k = INT((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }

        lunarDay = (int) (dayNumber - monthStart + 1);

        return lunarDay;
    }

    public static int getLunarMonth(int day, int month, int year) {
        int yy = year;
        int mm = month;
        int dd = day;

        int k, monthStart, a11, b11, lunarDay, lunarMonth, lunarYear, lunarLeap;
        long dayNumber;
        dayNumber = jdFromDate(dd, mm, yy);
        k = INT((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }
        a11 = getLunarMonth11(yy);
        b11 = a11;
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1);
        }
        int diff = INT((monthStart - a11) / 29);
        lunarMonth = diff + 11;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        if (lunarMonth >= 12) {
            lunarMonth = lunarMonth - 12;
        }
        return lunarMonth;

    }

//    public static String getTimeCanChiName(Context context, String canNgay, float h) {
//        String[] canGio = context.getResources().getStringArray(R.array.array_giap_ky);
//        if (canNgay.equalsIgnoreCase("Giáp") || canNgay.equalsIgnoreCase("Kỷ")) {
//            canGio = context.getResources().getStringArray(R.array.array_giap_ky);
//        } else if (canNgay.equalsIgnoreCase("Ất") || canNgay.equalsIgnoreCase("Canh")) {
//            canGio = context.getResources().getStringArray(R.array.array_at_canh);
//        } else if (canNgay.equalsIgnoreCase("Bính") || canNgay.equalsIgnoreCase("Tân")) {
//            canGio = context.getResources().getStringArray(R.array.array_binh_tan);
//        } else if (canNgay.equalsIgnoreCase("Đinh") || canNgay.equalsIgnoreCase("Nhâm")) {
//            canGio = context.getResources().getStringArray(R.array.array_dinh_nham);
//        } else if (canNgay.equalsIgnoreCase("Mậu") || canNgay.equalsIgnoreCase("Quý")) {
//            canGio = context.getResources().getStringArray(R.array.array_mau_ki);
//        }
//        if (h > 1.0f && h <= 3.0f) {
//            return canGio[1] + " Sửu";
//        }
//        if (h > 3.0f && h <= 5.0f) {
//            return canGio[2] + " Dần";
//        }
//        if (h > 5.0f && h <= 7.0f) {
//            return canGio[3] + " Mão";
//        }
//        if (h > 7.0f && h <= 9.0f) {
//            return canGio[4] + " Thìn";
//        }
//        if (h > 9.0f && h <= 11.0f) {
//            return canGio[5] + " Tỵ";
//        }
//        if (h > 11.0f && h <= 13.0f) {
//            return canGio[6] + " Ngọ";
//        }
//        if (h > 13.0f && h <= 15.0f) {
//            return canGio[7] + " Mùi";
//        }
//        if (h > 15.0f && h <= 17.0f) {
//            return canGio[8] + " Thân";
//        }
//        if (h > 17.0f && h <= 19.0f) {
//            return canGio[9] + " Dậu";
//        }
//        if (h > 19.0f && h <= 21.0f) {
//            return canGio[10] + " Tuất";
//        }
//        if (h > 21.0f && h <= 23.0f) {
//            return canGio[11] + " Hợi";
//        }
//        return canGio[0] + " Tý";
//    }

    public static LunisolarCalendar lunar2solar(LunisolarCalendar date) {

        int lunarDay = date.day;
        int lunarMonth = date.month;
        int lunarYear = date.year;
        int lunarLeap = isLeap(date);

        long k, a11, b11, off, leapOff, leapMonth, monthStart;
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1);
            b11 = getLunarMonth11(lunarYear);
        } else {
            a11 = getLunarMonth11(lunarYear);
            b11 = getLunarMonth11(lunarYear + 1);
        }
        off = lunarMonth - 11;
        if (off < 0) {
            off += 12;
        }
        if (b11 - a11 > 365) {
            leapOff = getLeapMonthOffset(a11);
            leapMonth = leapOff - 2;
            if (leapMonth < 0) {
                leapMonth += 12;
            }
            if (lunarLeap != 0 && lunarMonth != leapMonth) {
                return new LunisolarCalendar(0, 0, 0);
            } else if (lunarLeap != 0 || off >= leapOff) {
                off += 1;
            }
        }
        k = INT(0.5 + (a11 - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + off);
        return jdToDate(monthStart + lunarDay - 1);
    }

    private static int isLeap(LunisolarCalendar date) {
        int yy = date.year;
        int mm = date.month;
        int dd = date.day;

        int k, monthStart, a11, b11, lunarLeap;
        long dayNumber;
        dayNumber = jdFromDate(dd, mm, yy);
        k = INT((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }
        a11 = getLunarMonth11(yy);
        b11 = a11;
        if (a11 >= monthStart) {
            a11 = getLunarMonth11(yy - 1);
        } else {
            b11 = getLunarMonth11(yy + 1);
        }
        int diff = INT((monthStart - a11) / 29);
        lunarLeap = 0;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        return lunarLeap;
    }

    public static int getLeapMonth(LunisolarCalendar date) {
        int lunarMonth = date.month;
        int lunarYear = date.year;

        int a11, b11, leapOff;
        int leapMonth = 0;
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1);
            b11 = getLunarMonth11(lunarYear);
        } else {
            a11 = getLunarMonth11(lunarYear);
            b11 = getLunarMonth11(lunarYear + 1);
        }
        if (b11 - a11 > 365) {
            leapOff = getLeapMonthOffset(a11);
            leapMonth = leapOff - 2;
            if (leapMonth < 0) {
                leapMonth += 12;
            }

        }
        return leapMonth;
    }

    // 0 - ty, 1-suu, 2-dan, 3-mao, 4-thin, 5-ti, 6-ngo,
    // 7-mui, 8-than, 9-dau, 10-tuat, 11-hoi
    public static int[] getGioHoangDao(int day, int month, int year) {
        int chiNgay = getChiOfDate(day, month, year);
        switch (chiNgay) {
            case 0, 6:
                return new int[]{0, 1, 3, 6, 8, 9};
            case 1:
                return new int[]{2, 3, 5, 8, 10, 11};
            case 2, 8:
                return new int[]{0, 1, 4, 5, 7, 10};
            case 3, 9:
                return new int[]{0, 2, 3, 6, 7, 9};
            case 4, 10:
                return new int[]{2, 4, 5, 8, 9, 11};
            case 7:
                return new int[]{2, 3, 5, 8, 9, 11};
            case 5:
            default:
                return new int[]{1, 4, 6, 7, 10, 11};
        }
    }

    //input: ngay thang duong lich
    // 0 - ty, 1-suu, 2-dan, 3-mao, 4-thin, 5-ti, 6-ngo, 7-mui, 8-than, 9-dau, 10-tuat, 11-hoi
    public static int getChiOfDate(int day, int month, int year) {
        int jd = INT(jdFromDate(day, month, year));
        return (jd + 1) % 12;
    }

    public static int getGoodDayForCalendarDetail(int day, int month, int year) {
        int[] dateAL = convertSolarToLunar(day, month, year, 7);
        int dayAL = dateAL[0];
        int monthAL = dateAL[1];
        int yearAL = dateAL[2];
        boolean isLunarLeap = dateAL[3] == 1;


        int chiNgay = getChiOfDate(day, month, year);
        if (monthAL == 1 || monthAL == 7) {
            if (chiNgay == 0 || chiNgay == 1 || chiNgay == 5 || chiNgay == 7) {
                return 1;
            } else if (chiNgay == 6 || chiNgay == 3 || chiNgay == 11 || chiNgay == 9) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 2 || monthAL == 8) {
            if (chiNgay == 2 || chiNgay == 3 || chiNgay == 7 || chiNgay == 9) {
                return 1;
            } else if (chiNgay == 8 || chiNgay == 5 || chiNgay == 1 || chiNgay == 11) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 3 || monthAL == 9) {
            if (chiNgay == 4 || chiNgay == 5 || chiNgay == 9 || chiNgay == 11) {
                return 1;
            } else if (chiNgay == 10 || chiNgay == 7 || chiNgay == 1 || chiNgay == 3) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 4 || monthAL == 10) {
            if (chiNgay == 6 || chiNgay == 7 || chiNgay == 1 || chiNgay == 11) {
                return 1;
            } else if (chiNgay == 0 || chiNgay == 9 || chiNgay == 5 || chiNgay == 3) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 5 || monthAL == 11) {
            if (chiNgay == 8 || chiNgay == 9 || chiNgay == 1 || chiNgay == 3) {
                return 1;
            } else if (chiNgay == 2 || chiNgay == 11 || chiNgay == 7 || chiNgay == 5) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 6 || monthAL == 12) {
            if (chiNgay == 10 || chiNgay == 11 || chiNgay == 3 || chiNgay == 0) {
                return 1;
            } else if (chiNgay == 4 || chiNgay == 1 || chiNgay == 9 || chiNgay == 7) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    // doi ngay duong sang ngay am
    public static int[] convertSolarToLunar(int day, int month, int year, double timeZone) {
        int k, dayNumber, monthStart, a11, b11, lunarDay, lunarMonth, lunarYear, lunarLeap;
        dayNumber = INT(jdFromDate(day, month, year));
        k = (int) ((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }
        a11 = getLunarMonth11(year);
        b11 = a11;
        if (a11 >= monthStart) {
            lunarYear = year;
            a11 = getLunarMonth11(year - 1);
        } else {
            lunarYear = year + 1;
            b11 = getLunarMonth11(year + 1);
        }
        lunarDay = dayNumber - monthStart + 1;
        int diff = (monthStart - a11) / 29;
        lunarLeap = 0;
        lunarMonth = diff + 11;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }

        return new int[]{lunarDay, lunarMonth, lunarYear, lunarLeap};
    }

    public int getIsLeap() {
        return isLeap;
    }

    public void setIsLeap(int isLeap) {
        this.isLeap = isLeap;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return Math.abs(month);
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getExactyMonth() {
        return month;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("%d-%02d-%02d", this.year, this.month, this.day);
    }

    //Doi ra ngay am-duong
    public void solar2lunar(int day, int month, int year) {
        int yy = year;
        int mm = month;
        int dd = day;

        int k, monthStart, a11, b11, lunarDay, lunarMonth, lunarYear, lunarLeap;
        long dayNumber;
        dayNumber = jdFromDate(dd, mm, yy);
        k = INT((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }
        a11 = getLunarMonth11(yy);
        b11 = a11;
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1);
        }
        lunarDay = (int) (dayNumber - monthStart + 1);
        int diff = INT((monthStart - a11) / 29);
        lunarLeap = 0;
        lunarMonth = diff + 11;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }
        this.setMonth(lunarMonth);
        this.setDay(lunarDay);
        this.setYear(lunarYear);

    }

    //input: ngay, thang, nam duong lich
    public int getGoodDay(int day, int month, int year) {
        int[] dateAL = convertSolarToLunar(day, month, year, 7);
        int dayAL = dateAL[0];
        int monthAL = dateAL[1];
        int yearAL = dateAL[2];
        boolean isLunarLeap = dateAL[3] == 1;


        int chiNgay = getChiOfDate(day, month, year);
        if (monthAL == 1 || monthAL == 7) {
            if (chiNgay == 0 || chiNgay == 1 || chiNgay == 5 || chiNgay == 7) {
                return 1;
            } else if (chiNgay == 6 || chiNgay == 3 || chiNgay == 11 || chiNgay == 9) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 2 || monthAL == 8) {
            if (chiNgay == 2 || chiNgay == 3 || chiNgay == 7 || chiNgay == 9) {
                return 1;
            } else if (chiNgay == 8 || chiNgay == 5 || chiNgay == 1 || chiNgay == 11) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 3 || monthAL == 9) {
            if (chiNgay == 4 || chiNgay == 5 || chiNgay == 9 || chiNgay == 11) {
                return 1;
            } else if (chiNgay == 10 || chiNgay == 7 || chiNgay == 1 || chiNgay == 3) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 4 || monthAL == 10) {
            if (chiNgay == 6 || chiNgay == 7 || chiNgay == 1 || chiNgay == 11) {
                return 1;
            } else if (chiNgay == 0 || chiNgay == 9 || chiNgay == 5 || chiNgay == 3) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 5 || monthAL == 11) {
            if (chiNgay == 8 || chiNgay == 9 || chiNgay == 1 || chiNgay == 3) {
                return 1;
            } else if (chiNgay == 2 || chiNgay == 11 || chiNgay == 7 || chiNgay == 5) {
                return -1;
            } else {
                return 0;
            }
        } else if (monthAL == 6 || monthAL == 12) {
            if (chiNgay == 10 || chiNgay == 11 || chiNgay == 3 || chiNgay == 5) {
                return 1;
            } else if (chiNgay == 4 || chiNgay == 1 || chiNgay == 9 || chiNgay == 7) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

}
