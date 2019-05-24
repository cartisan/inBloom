package inBloom.framing;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to translate the content of plot vertices into natural language.
 * @author Sven
 */
public class Translator {
	
	private static Map<String, Expr[]> nlMap;
	static {
		nlMap = new HashMap<String, Expr[]>();
		nlMap.put("create_bread", new Expr[]
				{new Expr("create bread"), new Expr("creating bread"), new Expr("creates bread")});
		nlMap.put("help_with", new Expr[]
				{new Expr("help", Form.STOP), new Expr("helping", Form.STOP), new Expr("helps", Form.STOP)});
		nlMap.put("grind", new Expr[]
				{new Expr("grind"), new Expr("grinding"), new Expr("grinds")});
		nlMap.put("bake", new Expr[]
				{new Expr("bake"), new Expr("baking"), new Expr("bakes")});
		nlMap.put("eat", new Expr[]
				{new Expr("eat"), new Expr("eating"), new Expr("eats")});
		nlMap.put("plant", new Expr[]
				{new Expr("plant"), new Expr("planting"), new Expr("plants")});
		nlMap.put("harvest", new Expr[] 
				{new Expr("harvest"), new Expr("harvesting"), new Expr("harvests")});
		nlMap.put("tend", new Expr[]
				{new Expr("tend"), new Expr("tending"), new Expr("tends")});
		nlMap.put("bread", new Expr[]
				{new Expr("bread"), new Expr("bread"), new Expr("bread")});
		nlMap.put("wheat", new Expr[]
				{new Expr("bread"), new Expr("wheat"), new Expr("wheat")});
		nlMap.put("punish", new Expr[]
				{new Expr("punish"), new Expr("punishing"), new Expr("punishes")});
	}
	
	public static String translate(String term, Form form) {
		return Translator.translate(term, form, 0);
	}
	
	public static String translate(String term, Form form, int depth) {
		String[] split = term.split("\\(");
		String result = "";
		if(depth > 0) {
			result += " ";
		}
		if(split[0].startsWith("!")) {
			split[0] = split[0].substring(1);
		}
		Form nextForm = form;
		if(nlMap.containsKey(split[0])) {
			Expr expr = nlMap.get(split[0])[form.ordinal()];
			result += expr.text;
			if(expr.changesForm) {
				nextForm = expr.nextForm;
			}
		} else {
			result += split[0];
		}
		if(split.length > 1) {
			String rest = "";
			for(int i = 1; i < split.length; i++) {
				if(i > 1)
					rest += "(";
				rest += split[i];
			}
			rest = rest.substring(0, rest.length() - 1);
			if(nextForm != Form.STOP)
				result += translate(rest, nextForm, depth + 1);
		}
		return result;
	}
	
	private static class Expr {
		private String text;
		private Form nextForm;
		private boolean changesForm;
		
		public Expr(String text) {
			this.text = text;
			this.changesForm = false;
		}
		
		public Expr(String text, Form form) {
			this.text = text;
			this.nextForm = form;
			this.changesForm = true;
		}
	}
	
	public static enum Form {
		BASE, ING, S, STOP
	}
}
