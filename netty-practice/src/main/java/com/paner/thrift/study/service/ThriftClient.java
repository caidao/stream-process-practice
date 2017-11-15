package com.paner.thrift.study.service;

import com.paner.thrift.study.dto.QueryParameter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * @User: paner
 * @Date: 17/11/15 上午9:20
 */
public class ThriftClient {

    public static void main(String[] args) {

        try {
            TTransport transport;
            transport = new TSocket("localhost", 9090);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            DemoService.Client client = new DemoService.Client(protocol);

            System.out.println(client.ping());

            int max = 100000;

            Long start = System.currentTimeMillis();

            for (int i = 0; i < max; i++) {
                call(client);
            }

            Long end = System.currentTimeMillis();

            Long elapse = end - start;

            int perform = Double.valueOf(max / (elapse / 1000d)).intValue();

            System.out.print("thrift " + max + " 次RPC调用，耗时：" + elapse + "毫秒，平均" + perform + "次/秒");

            transport.close();

        } catch (TException x) {
            x.printStackTrace();
        }
    }

    private static void call(DemoService.Client client) throws TException {

        //client.ping();
        //System.out.println("ping()=>" + client.ping());

        QueryParameter parameter = new QueryParameter();
        parameter.setAgeStart(Short.valueOf("5"));
        parameter.setAgeEnd(Short.valueOf("50"));

        client.getPersonList(parameter);
        //System.out.println(client.getPersonList(parameter));
    }
}
