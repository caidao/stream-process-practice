package com.paner.elasticsearch;


import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by paner on 17/8/8.
 */
public class SearchTest {


    @Test
    public void  getMetaInfo() throws IOException, ExecutionException, InterruptedException {

        IndicesAdminClient indicesAdminClient = ClientTest.getClient().admin().indices();
        GetMappingsRequestBuilder getMappingsRequestBuilder = indicesAdminClient.
                prepareGetMappings("bdi_eco_data_index1").setTypes("eco_user_type");
        GetMappingsResponse response = getMappingsRequestBuilder.get();
// 结果
        for(ObjectCursor<String> key : response.getMappings().keys()){
            ImmutableOpenMap<String, MappingMetaData> mapping = response.getMappings().get(key.value);
            for(ObjectCursor<String> key2 : mapping.keys()){
                try {
                    System.out.println("rt:"+ mapping.get(key2.value).sourceAsMap().toString());
                    Object objectMap=mapping.get(key2.value).sourceAsMap().get("properties");
                    if (objectMap instanceof Map){
                        Map<String,Object> maps = (Map<String, Object>) objectMap;
                        System.out.println(""+maps.keySet());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Test
    public void search() throws IOException {
        SearchResponse response = ClientTest.getClient().prepareSearch("bdi_eco_data_index1")
                .setTypes("eco_user_type")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTimeout(TimeValue.timeValueMillis(10))
                .setQuery(QueryBuilders.termQuery("order_city_id","1"))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();
        System.out.println("hists:"+response.getHits().totalHits()+",tookInMillis:"+response.getTookInMillis());
    }

    @Test
    public void scrollSearch() throws IOException {

        SearchResponse scrollResp = ClientTest.getClient().prepareSearch("bdi_eco_data_index1")
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("order_city_id", "1"))
                .setSize(10).execute().actionGet();


        while (true){
            System.out.println("scrollId:"+scrollResp.getScrollId());
            for (SearchHit hit : scrollResp.getHits()){
                System.out.println("doc id :"+hit.getId());
            }
            //继续取值
            scrollResp = ClientTest.getClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            if (scrollResp.getHits().getHits().length==0){
                System.out.println("scroll end.... ");
                break;
            }
        }
    }


    @Test
    public void countSearch() throws IOException {
        CountResponse response = ClientTest.getClient().prepareCount("bdi_eco_data_index1")
                .setQuery(QueryBuilders.termQuery("_type", "eco_user_type"))
                .execute()
                .actionGet();
        System.out.println("total:"+response.getCount());
    }
}
