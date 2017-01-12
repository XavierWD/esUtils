## ES重建索引

### 提要
在使用ELK的时候突然发现如下错误：

```java

 [2017-01-11 10:29:00,175][DEBUG][action.search.type       ] [Reignfire] [customer][3], node[Q2O3abhxSs2sfxpt_j8cGA], [P], s[STARTED]: Failed to execute [org.elasticsearch.action.search.SearchRequest@372c1c2c] lastShard [true]
 org.elasticsearch.search.SearchParseException: [customer][3]: from[-1],size[-1]: Parse Failure [Failed to parse source [{"query": {"match":{"value":null} } }]]

```

 Google了一波，说是有修改过删除过数据还是怎么。跟Mapping有关系。最终发现是ES里面存的是Double类型的数据，但是这个类型的Mapping是string引起的（按理说应该没什么问题才对）。

重建索引，迁移数据：

1.查询之前的索引结构
GET host:9200/customer/_mapping

2.创建一个新的索引
PUT host:9200/customer_new

3.把第一步查询到的老结构抄过来,修改你要更改的类型
PUT host:9200/customer_new/_mapping/esdata
例如 body为：
```
{
        "properties": {
          "debug": {
            "type": "boolean"
          },
          "key": {
            "type": "string"
          },
          "recordtime": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "service": {
            "type": "string"
          },
          "value": {
            "type": "double"
          }
        }
      
}
```

4.接下来就是迁移数据了

java版本的：
https://github.com/a328940026/esUtils