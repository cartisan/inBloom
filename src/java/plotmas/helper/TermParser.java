package plotmas.helper;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;

public class TermParser {
	
	/**
	 * Retrieves the content of an annotation of a term with
	 * a given functor.
	 * @param term with annotations
	 * @param annot The functor of the annotation to retrieve
	 * @return content of the annotation if available, empty string otherwise
	 */
	public static String getAnnotation(String term, String annot) {
		Literal l;
		try {
			if(term.startsWith("!") || term.startsWith("+") || term.startsWith("-")) {
				term = term.substring(1);
			}
			l = ASSyntax.parseLiteral(term);
			if((l = l.getAnnot(annot)) != null) {
				return l.toString().substring((annot + "(").length(), l.toString().length() - 1);
			}
		} catch (ParseException e) {
		}
		return "";
	}

	public static String removeAnnots(String s) {
		// TODO: Create regex expression to do this task
		String result = "";
		boolean hasArguments = false;
		boolean argumentsClosed = false;
		for(char c : s.toCharArray()) {
			if(c == '[') {
				if(!hasArguments) {
					break;
				} else {
					if(argumentsClosed) {
						break;
					}
				}
			}
			if(c == '(') {
				hasArguments = true;
			}
			if(c == ')' && hasArguments) {
				argumentsClosed = true;
			}
			result += c;
		}
		return result;
	}
}
