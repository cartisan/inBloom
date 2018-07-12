package plotmas;

import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.CentralisedRuntimeServices;

public class PlotAwareCentralisedRuntimeServices extends CentralisedRuntimeServices {

	public PlotAwareCentralisedRuntimeServices(BaseCentralisedMAS masRunner) {
		super(masRunner);
	}

	@Override
    protected CentralisedAgArch newAgInstance() {
        return new PlotAwareCentralisedAgArch();
    }
}
