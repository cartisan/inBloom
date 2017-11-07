# plotmas
Plotmas is a system for generating narratives, which is using an extended version of the [Jason multi-agent framework](https://github.com/cartisan/jason/tree/o3a).

## Setup
1. Make sure to have Java 1.8 or later (and preferably Eclipse) installed
1. Check out the repository
1. Import the plotmas project in Eclipse
1. Test that the system is running properly by executing the example simulation, that is, execute the main method of `RedHenLauncher`

## Implementing your own story
To implement your own story and explore it using plotmas you need to extend several classes and implement custom agent reasoning. An example story can be found in the `plotmas.little_red_hen` package. It is advisable to be familiar with the basics of Jason programming, as for instance this [getting started guide](http://jason.sourceforge.net/mini-tutorial/eclipse-plugin/) or [this tutorial](http://jason.sourceforge.net/Jason.pdf).
1. Implement a your custom AgentSpeak reasoning code in a custom `src/asl/agentXYZ.asl`
1. Create a new package for your story
1. Subclass `Model` to create a custom representation of the story world
   1. Implement a method for each ASL action that your agents use
1. Subclass `PlotEnvironment` to create a custom environment responsible for managing the communication between agents and model
   1. Override `initialize(List<LauncherAgent> agents)` and at least execute the super-class initializer and set instance variable `Model model` to an instance of your custom model class
   1. Override `public boolean executeAction(String agentName, Structure action)` to implement which ASL actions are handled by which model method
    ```java
        @Override
        public void initialize(List<LauncherAgent> agents) {
          super.initialize(agents);
            FarmModel model = new FarmModel(agents, this);
            this.setModel(model);
        }

        @Override
        public boolean executeAction(String agentName, Structure action) {
          boolean result = super.executeAction(agentName, action);
          StoryworldAgent agent = getModel().getAgent(agentName);

          if (action.getFunctor().equals("farm_work")) {
            result = getModel().farmWork(agent);
          }
          pauseOnRepeat(agentName, action);
          return result;
        }
    ```
1. Subclass `PlotLauncher` to create a custom class responsible for setting up and starting your simulation. Implement a static main method, which needs to do several things.
   1. Set the static variable `ENV_CLASS` to the class of your custom environment implementation
   1. Instantiate the launcher
   1. Create a list of `LauncherAgent`s that can contain personality definitions, initial beliefs and goals for each agent of your simulation
   1. `run` the launcher using your custom `agentXYZ` agent implementation
    ```java
      public static void main(String[] args) throws JasonException {
        ENV_CLASS = FarmEnvironment.class;
        runner = new RedHenLauncher();

        ImmutableList<LauncherAgent> agents = ImmutableList.of(
          runner.new LauncherAgent("hen",
            new Personality(0,  1, 0.7,  0.3, 0.0)
          ),
          runner.new LauncherAgent("dog",
            new Personality(0, -1, 0, -0.7, -0.8)
          )
        );

        runner.run(args, agents, "agentXYZ");
      }
    ```
    
## Affective reasoning
Plotmas uses the affective reasoning capabilities of a custom [extension of Jason](https://github.com/cartisan/jason/tree/o3a). You have several following tools to address affective phenomena.

### Personality
Each agent has a personality represented on the Big-5 Personality traits scale. To set up an agents personality, define it in your custom Launcher using the `LauncherAgent` class:
`runner.new LauncherAgent("hen", new Personality(0,  1, 0.7,  0.3, 0.0))`. The traits are commonly abbreviated as OCEAN: Openness (to Experience), Conscientiousness, Extraversion, Agreeableness, Neuroticism.
These traits are defined on a floating point scale: [-1.0, 1.0]. An agents personality is used to compute its default mood.

### Emotions
Emotions are used to appraise perceived events, their only effect is that they change an agent's current mood. Emotions are represented using the 22 emotions from the OCC catalog. You can find a current list of implemented emotions in `jason.asSemantics.Emotion`.

Primary emotions are automatic reactions to the environment that do not depend on deliberation and are similar among agents. They are defined by the Model, that is on the Java side, when the model creates a new percept. For instance:
`this.environment.addEventPerception(agentName,"received(bread)[emotion(joy, self)]")` adds a new perception for `agentName` that the agent received some bread (which should be also represented on the Model side by adding an instance of bread to the agent's inventory). This is a positive event, so the primary emotion `joy` is added, and the target if this emotion is the agent itself.
Emotions here are added as ASL Annotations to a perception literal. They are represented as ground 2-valued predicates of form `emotion(EMNAME, TARGET)`.

Secondary emotions are deliberative and thus have to be implemented on the ASL side as part of planning. For this, the internal action `appraise_emotion` is provided. For instance: `.appraise_emotion(anger, Name);` The syntax is again of the form `.appraise_emotion(EMNAME, TARGET);`

### Mood
Mood can be seen an aggregated subjective representation of context. <<< Add details on mood representations >>>

### Reasoning using affect
At the moment no documentation has been provided, see the exhaustive example in `agent.asl`.
<<< Add details on how ASL agents have access to their current emotional state and how they can use this for reasoning >>>

## Architecture
![UML class diagram] (https://github.com/cartisan/plotmas/blob/master/overview_class-diagram.jpg)
