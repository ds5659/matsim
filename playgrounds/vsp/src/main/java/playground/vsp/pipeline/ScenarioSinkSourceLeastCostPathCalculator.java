package playground.vsp.pipeline;

import org.matsim.core.router.util.LeastCostPathCalculatorFactory;

public interface ScenarioSinkSourceLeastCostPathCalculator extends ScenarioSinkSource {
	
	public LeastCostPathCalculatorFactory getLeastCostPathCalculatorFactory();

}