package com.example.studentdemo.util;

public class GradeUtil {

    public static double calcTotalScore(double usual, double finalExam) {
        return Math.round((usual * 0.3 + finalExam * 0.7) * 10.0) / 10.0;
    }

    public static double calcGradePoint(double totalScore) {
        if (totalScore >= 90) return 4.0;
        if (totalScore >= 85) return 3.7;
        if (totalScore >= 82) return 3.3;
        if (totalScore >= 78) return 3.0;
        if (totalScore >= 75) return 2.7;
        if (totalScore >= 72) return 2.3;
        if (totalScore >= 68) return 2.0;
        if (totalScore >= 64) return 1.5;
        if (totalScore >= 60) return 1.0;
        return 0;
    }

    public static String calcGradeLevel(double totalScore) {
        if (totalScore >= 90) return "优秀";
        if (totalScore >= 80) return "良好";
        if (totalScore >= 70) return "中等";
        if (totalScore >= 60) return "及格";
        return "不及格";
    }
}
