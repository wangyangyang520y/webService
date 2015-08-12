package webService;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class TestServiceImpl implements TestService{

	@Resource
	private JdbcTemplate jdbcTemplate; 
	
	public String sayHi(String name) {
		System.out.println("sayHi....");
		String sql = "select * from alipayBindMemberInfo";
//		Map map = jdbcTemplate.queryForMap(sql);
//		System.out.println("===="+map.get("open_id"));
//		List<Map<String,String>> rows = jdbcTemplate.queryForList(sql);
//		for(int i=0 ;i<rows.size();i++){
//			Map<String,String> userMap=(Map<String,String>)rows.get(i);  
//			System.out.println(userMap.get("open_id"));
//		}
//		String sql = "select count(*) from alipayBindMemberInfo";
//		int count = jdbcTemplate.queryForInt(sql);
//		System.out.println("=============="+count);
		
//		jdbcTemplate.query(sql, new TestRowMapper());
		
		
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("read_all").returningResultSet("result",new TestRowMapper());
		Map map = new HashMap();
		map.put("a", 1);
		jdbcCall.execute(map);
	    return "hi " + name;
	}
	
	class TestRowMapper implements RowMapper{

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			// TODO Auto-generated method stub
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int cout = rsmd.getColumnCount();
//			rsmd.getColumnName(1);
			System.out.println("列数"+cout);
			System.out.println("列名"+rsmd.getColumnName(1));
			System.out.println("列值"+rs.getString(1));
			return null;
		}
		
		
	}
}
