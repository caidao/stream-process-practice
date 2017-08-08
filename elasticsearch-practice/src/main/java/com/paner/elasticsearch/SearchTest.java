package com.paner.elasticsearch;

import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by paner on 17/8/8.
 */
public class SearchTest {

    @Test
    public void search() throws IOException {
        SearchResponse response = ClientTest.getClient().prepareSearch("paner_test","bdi_eco_data_index1")
                .setTypes("eco_user_type","eco_user_type")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("order_city_id","1"))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();
        System.out.println("hists:"+response.getHits().totalHits());
    }

    @Test
    public void scrollsSearch() throws IOException {

        SearchResponse scrollResp = ClientTest.getClient().prepareSearch("paner_test", "bdi_eco_data_index1")
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
