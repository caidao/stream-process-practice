package com.paner.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Created by paner on 17/8/13.
 */
public class EsSqlSearchTest {

    @Test
    public void sqlQuery() throws IOException, SQLFeatureNotSupportedException, SqlParseException {

        String sql = "SELECT * FROM bdi_eco_data_index_alias where retail_user_lifecycle ='A10' ";

        Client client = ClientTest.getClient();
        SearchDao searchDao = new SearchDao(client);
        String dsl = searchDao.explain(sql).explain().explain();
        System.out.println("dsl:" + dsl);


        SqlElasticRequestBuilder builder =searchDao.explain(sql).explain();
        SearchResponse response = (SearchResponse) builder.get();
        SearchHits hits = response.getHits();
        System.out.println("hits:"+hits.totalHits());

        ObjectMapper mapper = new ObjectMapper();
//        for (SearchHit hit:hits.getHits()){
//              writeRedis(hit.getId(),mapper.writeValueAsString(hit.getSource()));
//        }

    }

    private void writeRedis(String userId,String profileJson){
        System.out.println("profile:"+profileJson);
        RedisClient.getInstance().getResource().set(userId,profileJson);
    }


    @Test
    public  void redisTest(){
        RedisClient.getInstance().getResource().set("paner","test");
    }
}
