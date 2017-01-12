package com.xavier.es.rebuild;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Xavier_Wei on 2017/1/11.
 * 重建索引用之后的数据迁移.把targetIndex的数据迁移到index
 */
public class ESUtils {
    private static TransportClient client;

    private static String address = "121.40.205.19";
    private static int port = 9300;

    private static String index = "customer";
    private static String type = "esdata";
    private static String targetIndex = "slb";

    static {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, port));
    }

    public static void rebuild() {
        BoolQueryBuilder mustQuery = QueryBuilders.boolQuery();
        mustQuery.must(QueryBuilders.matchAllQuery());
//        long count = client.prepareGet().setIndex(index).setType(type).execute().actionGet().;
        int sum = 0;
        Long total = null;
        while (true) {
            SearchResponse scrollResp = client.prepareSearch(index)
                    .setTypes(type)
                    .setQuery(mustQuery)
                    .setFrom(sum)
                    .setSize(1000).execute().actionGet();
            if (total == null) {
                total = scrollResp.getHits().getTotalHits();
            }
            sum = sum + scrollResp.getHits().getHits().length;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (SearchHit searchHit : scrollResp.getHits().getHits()) {
                bulkRequest.add(client.prepareIndex(targetIndex, type).setSource(searchHit.getSourceAsString()));
            }
            bulkRequest.execute().actionGet();
            if (sum >= total) {
                break;
            }
        }
    }

    public static void main(String... args) {
        rebuild();
    }

}
