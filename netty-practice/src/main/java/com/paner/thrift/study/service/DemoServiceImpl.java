package com.paner.thrift.study.service;

import com.paner.thrift.study.dto.Person;
import com.paner.thrift.study.dto.QueryParameter;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

/**
 * @User: paner
 * @Date: 17/11/14 下午10:02
 */
public class DemoServiceImpl implements DemoService.Iface{
    public String ping() throws TException {
        System.out.println("ping()");
        return "pong";
    }

    public List<Person> getPersonList(QueryParameter parameter) throws TException {
        List<Person> list = new ArrayList<Person>(10);
        for (short i = 0; i < 10; i++) {
            Person p = new Person();
            p.setAge(i);
            p.setChildrenCount(Byte.valueOf(i + ""));
            p.setName("test" + i);
            p.setSalary(10000D);
            p.setSex(true);
            list.add(p);
        }
        return list;
    }
}
