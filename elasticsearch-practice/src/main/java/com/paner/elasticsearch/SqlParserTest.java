package com.paner.elasticsearch;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.junit.Test;

/**
 * @User: paner
 * @Date: 18/2/2 上午10:16
 */
public class SqlParserTest {


    @Test
    public  void sqlParser(){

            String sql = "select * from user where name='aan' and (id>10 or type in(3,4,5)) or des=? order by id";

            // 新建 MySQL Parser
            SQLStatementParser parser = new MySqlStatementParser(sql);

            // 使用Parser解析生成AST，这里SQLStatement就是AST
            SQLStatement statement = parser.parseStatement();


            // 使用visitor来访问AST
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            statement.accept(visitor);


            System.out.println(visitor.getColumns());
            System.out.println(visitor.getOrderByColumns());


    }
}
