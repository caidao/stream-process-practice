package com.paner.elasticsearch;


import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by paner on 17/8/7.
 */
public class ClientTest {





    public static   TransportClient getClient() throws IOException {

        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("cluster.name", "elasticsearch")
                .build();

        TransportClient client = TransportClient.builder().settings(settings).build();
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }

    @Test
    public void writeDoc() throws IOException {

        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("user_id", 1226)
                .field("order_city_name", "西极4")
                .field("order_city_id", 35304L)
                .endObject();
        IndexResponse response = getClient().prepareIndex("paner_test","eco_user_type","1226")
                .setSource(builder).execute().actionGet();
        System.out.println(response.getIndex() + "," + response.getType() + "," + response.getId() + "," + response.getVersion());
    }

    @Test
    public void readDoc() throws IOException {
        GetResponse response = getClient().prepareGet("paner_test", "eco_user_type", "1226").execute().actionGet();
        System.out.println(""+response.getSource());
    }

    @Test
    public void deleteDoc() throws IOException {
        DeleteResponse response = getClient().prepareDelete("paner_test", "eco_user_type", "1226").execute().actionGet();
        System.out.println("delete doc ,id is "+response.getId());
    }

    @Test
    public void updateDoc() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("order_city_name", "西极-好地方")
                .endObject();
       UpdateResponse response= getClient().prepareUpdate("paner_test", "eco_user_type", "126").setDoc(builder).get();
        System.out.println("update doc,id is "+response.getId());

    }

    @Test
    //doc存在则更新，否则删除
    public void upsertDoc() throws IOException, ExecutionException, InterruptedException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("user_id", 1222226)
                .field("order_city_name", "西极5")
                .field("order_city_id", 3304L)
                .endObject();
        IndexRequest indexRequest = new IndexRequest("paner_test", "eco_user_type", "1222226")
                .source(builder);
        UpdateRequest updateRequest = new UpdateRequest("paner_test", "eco_user_type", "1222226")
                .doc(builder).upsert(indexRequest);
        UpdateResponse response=  getClient().update(updateRequest).get();
        System.out.println("upsert doc,id is "+response.getId());
    }

    @Test
    //bulk API允许开发者在一个请求中索引和删除多个文档
    public void bulkDocs() throws IOException {

        BulkRequestBuilder bulkRequestBuilder = getClient().prepareBulk();

        bulkRequestBuilder.add(getClient().prepareIndex("paner_test", "eco_user_type", "11111").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("user_id", 250152554)
                        .field("order_city_name", "蜀山")
                        .field("order_city_id", 1L)
                        .endObject()
        ));

        bulkRequestBuilder.add(getClient().prepareIndex("paner_test", "eco_user_type", "22222").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("user_id",  250215491)
                        .field("order_city_name", "湖山")
                        .field("order_city_id", 2L)
                        .endObject()
        ));

        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        System.out.println("bulk request is "+bulkResponse.buildFailureMessage());
    }


    @Test
    public void delByQuery() throws IOException {
//        DeleteResponse response = getClient().prepareDeleteBy("test")
//                .setQuery(QueryBuilders.termQuery("user_id", "12346"))
//                .execute()
//                .actionGet();
    }

}
