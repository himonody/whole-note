package com.litian.dancechar.base.common.constants;

/**
 * redis常量类
 *
 * @author tojson
 * @date 2022/7/28 10:33
 */
public class RedisKeyConstants {

    /**
     * 学生
     */
    public static class Student{
        /**
         * id
         */
        public static  final String ID_KEY = "s:id:%s";
    }

    /**
     * 员工
     */
    public static class Employee{
        /**
         * 员工信息
         */
        public static  final String EMPLOYEE_INFO_KEY = "emp:";

    }
}
