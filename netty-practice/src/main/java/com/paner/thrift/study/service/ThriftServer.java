package com.paner.thrift.study.service;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;


/**
 * @User: paner
 * @Date: 17/11/15 上午9:18
 */
public class ThriftServer {

    public static DemoService.Iface service;

    public static DemoService.Processor processor;

    public static void main(String[] args) {
        try {
            service = new DemoServiceImpl();
            processor = new DemoService.Processor(service);

            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor);
                }
            };
            new Thread(simple).start();

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void simple(DemoService.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
            System.out.println("Starting the simple server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
