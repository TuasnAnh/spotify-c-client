/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.utils;

/**
 *
 * @author ADMIN
 */
public class StringUtils {

    public static boolean isNumberic(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    public static String doubleToTimeDuration(double num) {
        String result = "";
        int dti = (int) Math.round(num);
        int minutes = dti / 60;
        int second = dti % 60;
        result += minutes + ":";
        if(second < 10) {
            result += "0" + second;
        } else {
            result += second;
        }
        return result;
    }
}
