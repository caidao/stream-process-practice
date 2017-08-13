package com.paner.elasticsearch;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Created by paner on 17/8/13.
 */
public class EsSqlSearchTest {

    @Test
    public void sqlQuery() throws IOException, SQLFeatureNotSupportedException, SqlParseException {

        String sql = "SELECT * FROM paner_test where order_city_id =23534 ";

        Client client = ClientTest.getClient();

        SearchDao searchDao = new SearchDao(client);
        String dsl = searchDao.explain(sql).explain().explain();
        System.out.println("dsl:" + dsl);

        SqlElasticRequestBuilder builder =searchDao.explain(sql).explain();
        SearchResponse response = (SearchResponse) builder.get();
        SearchHits hits = response.getHits();
        System.out.println("hits:"+hits.totalHits());

    }
}
