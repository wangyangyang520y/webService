package util;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

public class XmlHandleUtil {

	/**
	 * 获取配置文件中存储过程的名称和参数
	 * @param procedureID
	 * @return
	 * @throws DocumentException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getProcedureParamsAndName(String procedureID) throws DocumentException{
		Map map = new HashMap();
		List<String> inList = new ArrayList<String>();
		List<String> outList = new ArrayList<String>();
		Map<String,String> dataTypeMap = new HashMap<String, String>(); 
		Document doc = loadXml("procedureSet/procedureSet.xml");
		Element element = doc.elementByID(procedureID);
		if(element==null){
			throw new RuntimeException("没有procedureID："+procedureID+"对应的存储过程");
		}
//		Node node =  doc.elementByID(procedureID);
		String procedureName = element.attribute("name").getText().trim();
		String isResultSet = element.attribute("isResultSet").getText().trim();
		List<Element> childs = element.elements();
		for(Element e:childs){
			if("in".equals(e.attribute("type").getText().trim())){
				inList.add(e.getTextTrim());
				
			}else if("out".equals(e.attribute("type").getText().trim())){
				outList.add(e.getTextTrim());
			}
			dataTypeMap.put(e.getTextTrim(), e.attribute("dataType").getText());
		}
		map.put("procedureName", procedureName);
		map.put("isResultSet", isResultSet);
		map.put("inParamsList", inList);
		map.put("outParamsList", outList);
		map.put("dataTypeMap", dataTypeMap);
		return map;
	}
	
	/**
	 * 获取请求参数xml中的存储过程的参数
	 * @param paramsXML
	 * @return
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public Map<String,String> getRequestParamsMap(String paramsXML) throws DocumentException{
		Map<String,String> map = new HashMap<String, String>();
		Document doc = DocumentHelper.parseText(paramsXML);
		Element root = doc.getRootElement();
		List<Element> childs = root.elements();
		for(Element e:childs){
			String key = e.getName();
			String value = null;
			System.out.print("-================:"+e.getTextTrim() );
			if(e.getTextTrim() != null){
				value = e.getTextTrim();
			}else{
				value = "";
			}
			map.put(key, value);
		}
		return map;
	}
	
	/**
	 * 讲请求结果集封装成xml格式，并以string返回
	 * @param list
	 * @return
	 */
	public String packageXml(List<Map<String,String>>resultSetlist,Map<String,String> outParamsMap){
		Document document = DocumentHelper.createDocument();  
        Element root = document.addElement("dataResult");//添加文档根
        Element outParams = root.addElement("outParams");
        Element resultSet = root.addElement("resultSet");
        if(resultSetlist!=null){
        	for(Map<String,String> map : resultSetlist){
            	Element row = resultSet.addElement("row");
            	for(String key:map.keySet()){
            		Element column = row.addElement(key);
            		if(map.get(key)!=null){
            			column.setText(map.get(key));
            		}else{
            			column.setText("");
            		}
            	}
            }
        }
        if(outParamsMap!=null){
        	for(String key : outParamsMap.keySet()){
            	Element param = outParams.addElement(key.trim());
            	if(outParamsMap.get(key)!=null){
            		param.setText(outParamsMap.get(key));
            	}else{
            		param.setText("");
            	}
            	
            }
        }
		return document.asXML();
	}
	
	private Document loadXml(String url) throws DocumentException {  
        InputSource input = new InputSource(this.getClass().getClassLoader().getResourceAsStream(url)) ;        
        SAXReader saxReader = new SAXReader();        
//        saxReader.setEntityResolver(new OfflineEntityResover()) ;  
        Document doc = saxReader.read(input);  
        return doc; 
	}
}
