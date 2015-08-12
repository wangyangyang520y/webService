package webService;

import java.text.ParseException;

import javax.jws.WebService;

import org.dom4j.DocumentException;

@WebService
public interface IService {
	public String procedureCallService(String procedureID,String paramsXML) throws DocumentException,ParseException;
}
