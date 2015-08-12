package webService;

import javax.jws.WebService;

@WebService
public interface TestService {

	public String sayHi(String name);
}
