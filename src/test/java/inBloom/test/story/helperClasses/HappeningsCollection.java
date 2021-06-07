package inBloom.test.story.helperClasses;

import java.util.function.Consumer;
import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class HappeningsCollection {

    // Set up positive happening "find friend" that is triggered by (positive) action perception "get(drink)"
    public static Happening<TestModel> findFriendHap = new Happening<>(
    		new Predicate<TestModel>() {
				@Override
				public boolean test(TestModel model) {
					if (model.isDrunk) {
						return true;
					}
					return false;
				}

    		},
    		new Consumer<TestModel>() {
				@Override
				public void accept(TestModel model) {
					model.hasFriend = true;
				}
    		},
    		"jeremy",
    		"isDrunk",
    		"found(friend)"
    	);

    // Set up negative happening "break leg" happening at some time point
    public static Happening<TestModel> breakLeg = new Happening<>(
    		new Predicate<TestModel>() {
				@Override
				public boolean test(TestModel model) {
					return model.getStep() > 11;
				}

    		},
    		new Consumer<TestModel>() {
				@Override
				public void accept(TestModel model) {
					model.brokenLeg = true;
				}
    		},
    		"jeremy",
    		null,
    		"break(leg)"
    	);

    // Set up positive happening "win compensation" caused by happening "break leg"
    public static Happening<TestModel> winDamages = new Happening<>(
    		new Predicate<TestModel>() {
				@Override
				public boolean test(TestModel model) {
					return model.brokenLeg;
				}

    		},
    		new Consumer<TestModel>() {
				@Override
				public void accept(TestModel model) {
					// intentionally blank
				}
    		},
    		"jeremy",
    		"brokenLeg",
    		"win(damages)"
    	);

    public static Happening<TestModel> looseWallet = new Happening<>(
    		new Predicate<TestModel>(){
    			public boolean test(TestModel model) {
    				if(model.step == 1) {
    					return true;
    				}
    				return false;
    			}
    		},
    		new Consumer<TestModel>() {
    			public void accept(TestModel model) {
    				Character chara = model.getCharacter("jeremy");
    				chara.removeFromInventory("wallet");
    			}
    		},
    		"jeremy",
    		null,
    		"lost(wallet)");

    static {
    	findFriendHap.setAnnotation(PerceptAnnotation.fromEmotion("joy"));
    	breakLeg.setAnnotation(PerceptAnnotation.fromEmotion("distress"));
    	winDamages.setAnnotation(PerceptAnnotation.fromEmotion("joy"));
    }
}
