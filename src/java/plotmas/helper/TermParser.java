package plotmas.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import jason.asSemantics.Affect;
import jason.asSemantics.Emotion;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import jason.util.Pair;
import plotmas.graph.Edge;

/**
 * Class holding different helper methods regarding
 * the parsing of strings.
 * @author Sven Wilke
 */
public class TermParser {
	
	private static final Pattern EMOTION_PATTERN = Pattern.compile("(?<emotion>\\w*)\\["+ Edge.Type.CAUSALITY.toString() +"\\((?<cause>.*)\\)\\]\\(.\\)");
	public static final String PERSO_PATTERN = "personality\\((?<trait>.+?),(?<scope>.+?)\\)";
	public static final String AFFECT_PATTERN = "affect\\((?<trait>.+?),(?<scope>.+?)\\)";
			
	/**
	 * Creates an emotion object from a textual representation of an emotion
	 * as it is created by {@link Emotion#toString()}. This means that the
	 * resulting emotion does not contain the {@link Emotion#target} of the
	 * original emotion.
	 * @param emo
	 * @return Emotion object
	 */
	public static Emotion emotionFromString(String emo) {
		Matcher matcher = EMOTION_PATTERN.matcher(emo);
		if(matcher.find()) {
			Emotion emotion = Emotion.getEmotion(matcher.group(Emotion.ANNOTATION_FUNCTOR).toLowerCase());
			emotion.setCause(matcher.group(Edge.Type.CAUSALITY.toString()));
			return emotion;
		}
		return null;
	}
	
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
	
	/**
	 * Quick and dirty heuristic to extract personality preconditios from a plan annotation. Matches all instances of
	 * personality(trait, scale) in annot and extracts them as pairs. Returns a list of all these pairs, standing in 
	 * for all potential personalities that could enable this plan to be executed. <br><br>
	 * 
	 * <b>Attention:</b> This ignores all logical operators like <i>and</i>, <i>or</i>, <i>not</i> and does not take
	 * into account the mood part of the annotations. While the latter is acceptable, as it is a dynamic property of the
	 * simulation, the former is undesirable. A FIXME: would be to create a tree representation of the involved 
	 * personality conditions and return all paths of the tree as potential personality conditions.
	 * 
	 * @param annot string representation of an affective plan precondition
	 * @see plotmas.test.helper.TermParserTest
	 * @return
	 */
	public static List<Pair<String,String>> extractPersonalityAnnotation(String annot) {
		Pattern pattern = Pattern.compile(PERSO_PATTERN, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(annot);
		
		List<Pair<String,String>> conditions = new LinkedList<>();
		while (matcher.find()) {
			conditions.add(new Pair<String, String>(matcher.group("trait"), matcher.group("scope")));
		}
		
		return conditions;
	}
	
	// Returns a list of OCEAN tuples
	public static List<List<Pair<String,Double>>> solutionsForPersonalityAnnotation(String annot) {
        Literal annotLiteral = Literal.parseLiteral(annot);
        
        String annFunctor = annotLiteral.getFunctor();
        if(!annFunctor.equals(Affect.ANNOTATION_FUNCTOR)) {
        	throw new RuntimeException("Annotation should have functor 'affect', not: " + annFunctor);
        }
        
        //affect(pers(C,h))    affect(and(p(C,h),p(E,l)))
		Model model = new Model("pers-model");
		Map<String,IntVar> intVarCache = new HashMap<>();
        Constraint personalityConstraint = constrainFromAffectLiteral((Literal) annotLiteral.getTerm(0), model, intVarCache, "");
        personalityConstraint.post();
        
        Solver solver = model.getSolver();
        solver.showSolutions();
        List<Solution> solutions = solver.findAllSolutions();
        
        List<List<Pair<String,Double>>> results = new LinkedList<>();
        for (Solution s:solutions) {
        	List<Pair<String, Double>> oceanConstraints = new LinkedList<>();
        	
        	for(String trait: intVarCache.keySet()) {
        		IntVar var = intVarCache.get(trait);
        		oceanConstraints.add(new Pair<String, Double>(trait, (s.getIntVal(var) / 10.0)));
        		
        	}
        	
        	results.add(oceanConstraints);
        }
        
        return results;
	}
        
		
    private static Constraint constrainFromAffectLiteral(Literal lit, Model model, Map<String,IntVar> varCache, String parentFunc) {
		String func = lit.getFunctor();
		
		switch(func) {
    		case "mood": {
    			switch(parentFunc) {
    			case "or":
    			case "not":	return model.falseConstraint();
    			case "and":
    			default: 	return model.trueConstraint();
    			}
    		}
    		case "personality": {
    			String trait = lit.getTerm(0).toString();
    			String value = lit.getTerm(1).toString();
    			
    			varCache.putIfAbsent(trait, model.intVar(trait, new int[]{-10, -3, 3, 10}));
    			IntVar var = varCache.get(trait);
    			
    			switch(value) {
    			case "positive": return model.arithm(var, ">", 0);
    			case "negative": return model.arithm(var, "<", 0);
    			case "low": 	 return model.arithm(var, "<=", -7);
    			case "medium": 	 return model.absolute(model.intVar(3), var) ;
    			case "high": 	 return model.arithm(var, ">=", 7);
    			default:		 throw new RuntimeException("Illegal trait-value: " + value);
    			}
    			
    		}
    		case "and":	{
    			Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
    			Constraint c1 = constrainFromAffectLiteral((Literal) lit.getTerm(1), model, varCache, func);
    			return model.and(c0, c1);
    		}
    		case "or":	{
    			Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
    			Constraint c1 = constrainFromAffectLiteral((Literal) lit.getTerm(1), model, varCache, func);
    			return model.or(c0, c1);
    		}
    		// for some reason Jason adds a space to our nots
    		case "not ":	{
    			if(((Literal) lit.getTerm(0)).getFunctor() == "mood") {
    				// all mood's resolve to 1, so not(mood(X)) would always be 0 --> fix w/ lookahead 
    				return model.trueConstraint(); 
    			}
    			Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
    			return model.not(c0);
    		}
    		default:	throw new RuntimeException("Illegal functor-value: " + func);
		}
	}
}
