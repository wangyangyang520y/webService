package webService;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import oracle.jdbc.OracleTypes;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.dom4j.DocumentException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import util.XmlHandleUtil;

public class IServiceImpl implements IService{
	@Resource
	private JdbcTemplate jdbcTemplate; 


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String procedureCallService(String procedureID,String paramsXML) throws DocumentException, ParseException
	{
		XmlHandleUtil xmlHandleUtil = new XmlHandleUtil();
		
		Map procedureMap = xmlHandleUtil.getProcedureParamsAndName(procedureID);
		String procedureName = procedureMap.get("procedureName").toString();
		String isResultSet = procedureMap.get("isResultSet").toString();
		List<String> procedureInParamsList = (List<String>)procedureMap.get("inParamsList");
		List<String> procedureOutParamsList = (List<String>)procedureMap.get("outParamsList");
		Map<String,String> paramsDataTypeMap = (Map<String,String>)procedureMap.get("dataTypeMap");
		
		Map<String,String> requestParamsMap = xmlHandleUtil.getRequestParamsMap(paramsXML); 
		//判断传的参数是否和配置文件中一致，如果不是就拋异常
		for(String param : procedureInParamsList){
			if(requestParamsMap.get(param)==null){
				throw new RuntimeException("请求参数param："+param+"不能空null");
			}
		}
		
		List<Map<String,String>> resultSetList = null;
		
		//请求存储过程    没有使用元数据的特性，全部是手动添加参数及参数类型。测试是oracle不支持jdbc元数据，但文档说支持，其他的数据库测试基本都支持。
		SimpleJdbcCall jdbcCall = null;
		Map resultMap = new HashMap();
		if("true".equals(isResultSet)){
			jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName).withoutProcedureColumnMetaDataAccess().returningResultSet("resultSet",new ResultSetRowMapper());
			resultSetList = new ArrayList<Map<String,String>>();
		}else if("false".equals(isResultSet)){
			jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName).withoutProcedureColumnMetaDataAccess();
		}
		if(jdbcCall!=null){
			for(String inParam : procedureInParamsList){
				if("int".equals(paramsDataTypeMap.get(inParam))){
					jdbcCall.addDeclaredParameter(new SqlParameter(inParam, OracleTypes.INTEGER));
				}else if("string".equals(paramsDataTypeMap.get(inParam))){
					jdbcCall.addDeclaredParameter(new SqlParameter(inParam, OracleTypes.VARCHAR));
				}else if("date".equals(paramsDataTypeMap.get(inParam))){
					jdbcCall.addDeclaredParameter(new SqlParameter(inParam, OracleTypes.DATE));
				}else if("float".equals(paramsDataTypeMap.get(inParam))){
					jdbcCall.addDeclaredParameter(new SqlParameter(inParam, OracleTypes.FLOAT));
				}
			}
			for(String outParam : procedureOutParamsList){
				if("int".equals(paramsDataTypeMap.get(outParam))){
					jdbcCall.addDeclaredParameter(new SqlOutParameter(outParam, OracleTypes.INTEGER));
				}else if("string".equals(paramsDataTypeMap.get(outParam))){
					jdbcCall.addDeclaredParameter(new SqlOutParameter(outParam, OracleTypes.VARCHAR));
				}else if("date".equals(paramsDataTypeMap.get(outParam))){
					jdbcCall.addDeclaredParameter(new SqlOutParameter(outParam, OracleTypes.DATE));
				}else if("float".equals(paramsDataTypeMap.get(outParam))){
					jdbcCall.addDeclaredParameter(new SqlOutParameter(outParam, OracleTypes.FLOAT));
				}
				
			}
			resultMap = jdbcCall.execute(requestParamsMap);
		}
		
		//组装输出参数
		Map<String,String> outParmasMap = null;
		if(procedureOutParamsList!=null && procedureOutParamsList.size()>0){
			outParmasMap = new HashMap<String, String>();
			for(String outParam:procedureOutParamsList){
				if(resultMap.get(outParam)!=null){
					outParmasMap.put(outParam,resultMap.get(outParam).toString());
				}
			}
		}
		if(resultSetList!=null){
			resultSetList = (List<Map<String,String>>)resultMap.get("resultSet");
		}
		
		String resultXml = xmlHandleUtil.packageXml(resultSetList,outParmasMap);
		
		return resultXml;
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	class ResultSetRowMapper implements RowMapper{

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			// TODO Auto-generated method stub
			Map<String,String>map = new HashMap<String, String>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			for(int i=1;i<=count;i++){
				map.put(rsmd.getColumnName(i), rs.getString(i));
			}
			return map;
		}
		
		
	}
	
	
	
	public static void main(String args[]) throws Exception {

		/*
		 * 方法三
		 */
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		// 注册WebService接口
		factory.setServiceClass(IService.class);
		// 设置WebService地址
		factory.setAddress("http://localhost:8080/procedureService?wsdl");
		IService dfw = (IService) factory.create();

		// 根据操作代码得到用户
//		String xml = dfw.procedureCallService("prc_query", "<paramsData><d>2012-09-09</d><c>12</c></paramsData>");
		String xml = dfw.procedureCallService("testPro", "<paramsData><userName>%</userName><ifValue>-1</ifValue></paramsData>");
//		String xml = dfw.procedureCallService("olnyOut_procedure", "<paramsData></paramsData>");
		System.out.println(xml);
	}
}
