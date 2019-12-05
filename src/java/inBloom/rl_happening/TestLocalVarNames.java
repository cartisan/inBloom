package inBloom.rl_happening;

public class TestLocalVarNames {

	public String aMethod(int arg) {
		String local1 = "a string";
		StringBuilder local2 = new StringBuilder();
		return local2.append(local1).append(arg).toString();
	}


	/**public void bMethod() {
		Field[] fields = YourClass.class.getDeclaredFields();
		//gives no of fields
		System.out.println(fields.length);         
		for (Field field : fields) {
			//gives the names of the fields
			System.out.println(field.getName());   
		}
	}*/
}
