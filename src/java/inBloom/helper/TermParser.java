package inBloom.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import inBloom.ERcycle.AdaptPersonality;
import inBloom.ERcycle.AdaptPersonality.OCEANConstraints;
import inBloom.graph.Edge;
import inBloom.graph.Vertex;
import jason.asSemantics.Affect;
import jason.asSemantics.AffectiveDimensionChecks;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import jason.asSemantics.Personality;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import jason.util.Pair;

/**
 * Class holding different helper methods regarding
 * the parsing of strings.
 * @author Sven Wilke
 */
public class TermParser {
	
	private static final Pattern EMOTION_PATTERN = Pattern.compile("(?<emotion>\\w*)\\["+ Edge.Type.CAUSALITY.toString() +"\\((?<cause>.*)\\)\\]\\(.\\)");
	public static final Pattern CROSSCHAR_PATTERN = Pattern.compile(Edge.Type.CROSSCHARACTER + "\\((<?id>.+)\\)");
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
			emotion.setCause(TermParser.removeAnnots(matcher.group(Edge.Type.CAUSALITY.toString())));
			return emotion;
		}
		return null;
	}
	
	/**
	 * Retrieves the content of an annotation of a term with
	 * a given functor. When applied to vertices, always base it on {@link Vertex#getLabel()} and not on
	 * {@link Vertex#toString()}.
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
		String result = "";
		int openParens = 0;
		
		for(char c : s.toCharArray()) {
			if( (c == '[') & (openParens == 0)) {
				break;
			}
			
			if(c == '(') {
				openParens += 1;
			} else if(c == ')' ) {
				openParens -= 1;
			}
			result += c;
		}
		return result;
	}
	
	/**
	 * Returns the annotations of a ASL term, ignoring any embedded annotations. Convenience
	 * function that keeps outer brackets on extracted annotations.
	 * E.g. "at(loc(tree)[level(top)])[source(self)]" -> "[source(self)]"
	 * @param s ASL term in string form
	 * @return the annotations in string form
	 */
	public static String getAnnots(String s) {
		return TermParser.getAnnots(s, false);
	}
	
	/**
	 * Returns the annotations of a ASL term, ignoring any embedded annotations. You can choose
	 * to also strip surrounding square brackets. E.g.
	 * {@code getAnnots("at(loc(tree)[level(top)])[source(self)]", true)} -> {@code "source(self)" }
	 * @param s ASL term in string form
	 * @return the annotations in string form
	 */
	public static String getAnnots(String s, boolean removeOuterBrackets) {
		String termNoAnnots = TermParser.removeAnnots(s);
		String annots =  s.substring(termNoAnnots.length());
		
		if(removeOuterBrackets) {
			return annots.substring(1, annots.length() - 1);
		}
		return annots;
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
	 * @see inBloom.test.helper.TermParserTest
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
	
	/**
	 * Takes an affective plan annotation and translates it into a CSP, which is then solved for valid personality
	 * using the possible domain of {-1, -0.3, 0, 0.3, 1} for personality traits.
	 * @param annot String of plan annotation, form: "affect(...), other_annotations"
	 * @return A List of OCEAN n-tuples, each representing a valid personality diff, e.g. [[(E:1), (O:-1)], [(E:-1),(O:1)]]
	 */
	public static Set<OCEANConstraints> solutionsForPersonalityAnnotation(Literal annot) {
        String annFunctor = annot.getFunctor();
        if(!annFunctor.equals(Affect.ANNOTATION_FUNCTOR)) {
        	throw new RuntimeException("Annotation should have functor 'affect', not: " + annFunctor);
        }
        
        //examples: affect(pers(C,h))   ||   affect(and(p(C,h),p(E,l)))
		Model model = new Model("step-model");
		Map<String,IntVar> intVarCache = new HashMap<>();
        Constraint personalityConstraint = constrainFromAffectLiteral((Literal) annot.getTerm(0), model, intVarCache, "");
        personalityConstraint.post();
        
        Solver solver = model.getSolver();
        solver.showSolutions();
        List<Solution> solutions = solver.findAllSolutions();
        
        return OCEANConstraints.toConstraintsSet(solutions, intVarCache.values());
	}
        
		
    private static Constraint constrainFromAffectLiteral(Literal lit, Model model, Map<String,IntVar> varCache, String parentFunc) {
		String func = lit.getFunctor();
		
		switch(func) {
			case AffectiveDimensionChecks.AND:	{
				Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
				Constraint c1 = constrainFromAffectLiteral((Literal) lit.getTerm(1), model, varCache, func);
				return model.and(c0, c1);
			}
			case AffectiveDimensionChecks.OR:	{
				Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
				Constraint c1 = constrainFromAffectLiteral((Literal) lit.getTerm(1), model, varCache, func);
				return model.or(c0, c1);
			}
			case AffectiveDimensionChecks.NOT:	{
				Constraint c0 = constrainFromAffectLiteral((Literal) lit.getTerm(0), model, varCache, func);
				return model.not(c0);
			}
    		case Personality.ANNOTATION_FUNCTOR: {
    			String trait = lit.getTerm(0).toString();
    			String value = lit.getTerm(1).toString();
    			
				if(!varCache.containsKey(trait)) {
					varCache.put(trait, model.intVar(trait, AdaptPersonality.PERSONALITY_INT_DOMAIN));
				}
    			IntVar var = varCache.get(trait);
    			
    			switch(value) {
	    			case AffectiveDimensionChecks.POS: return model.arithm(var, ">", 0);
	    			case AffectiveDimensionChecks.NEG: return model.arithm(var, "<", 0);
	    			case AffectiveDimensionChecks.LOW: return model.arithm(var, "<=", -7);
	    			case AffectiveDimensionChecks.MED: return model.absolute(model.intVar(3), var) ;
	    			case AffectiveDimensionChecks.HIG: return model.arithm(var, ">=", 7);
	    			default:		 				   throw new RuntimeException("Illegal trait-value: " + value);
    			}
    		}
			case Mood.ANNOTATION_FUNCTOR: {
    			// mood needs to be treated as irrelevant
    			switch(parentFunc) {
	    			case AffectiveDimensionChecks.OR:
	    			case AffectiveDimensionChecks.NOT:	return model.falseConstraint();	// or(X,0) makes only X relevant, not(0) returns 1
	    			
	    			case AffectiveDimensionChecks.AND:
	    			default: 							return model.trueConstraint(); // and(X,1) allows X to be evaluated no matter the mood
    			}
    		}
    		default:	throw new RuntimeException("Illegal functor-value: " + func);
		}
	}
}
