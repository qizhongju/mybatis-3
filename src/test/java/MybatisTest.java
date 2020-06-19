import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * DESCRIPTION:TODO <br/>
 *
 * @author qizhongju
 * @Date: 2020/3/16 12:55  <br/>
 */
public class MybatisTest {

  public void test1() throws IOException {

    //1.读取配置文件字节流
    InputStream resourcesStream = Resources.getResourceAsStream("sqlMapConfig.xml");

    //2.解析配置文件，封装Configuration对象，创建DefaultSqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourcesStream);

    //3.生产DefaultSqlSession实例，设置事务不自动提交以及完成executor对象创建
    SqlSession sqlSession = sqlSessionFactory.openSession();

    //4-1根据statementId从Configuration集合中获取到指定的MappedStatement对象
    //4-2将查询任务委派给executor执行器
    sqlSession.selectList("namespace.id");

  }

}
